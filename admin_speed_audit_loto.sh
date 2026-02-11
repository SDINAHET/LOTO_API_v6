#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# Loto Tracker - Speed Audit (server + API + assets)
# Default target: https://stephanedinahet.fr
# Ignore .zip assets (old versions)
# ============================================================

BASE_URL="${1:-https://stephanedinahet.fr}"

# Endpoints "névralgiques" trouvés dans ton code (safe GET only)
ENDPOINTS=(
  "/"                             # page root (si front servi)
  "/api/health"                   # HealthController
  "/api/hello"                    # HelloController
  "/api/historique/last20"        # Historique20Controller
  "/api/historique/last20/Detail/tirages"  # Historique20DetailController
  "/api/auth/ping"                # AuthController ping
  "/admin/ping"                   # AdminDashboardController ping
  "/admin/logs"                   # AdminDashboardController logs (text/plain)
)

ITERATIONS="${ITERATIONS:-8}"        # nb de mesures par URL
DISCOVER_ASSETS="${DISCOVER_ASSETS:-1}" # 1=scan HTML root pour assets, 0=skip
ASSETS_LIMIT="${ASSETS_LIMIT:-50}"

# Load test (optionnel)
AB_REQUESTS="${AB_REQUESTS:-300}"
AB_CONCURRENCY="${AB_CONCURRENCY:-20}"

OUT_DIR="${OUT_DIR:-./speed_audit_$(date +%Y%m%d_%H%M%S)}"
mkdir -p "$OUT_DIR"

REPORT="$OUT_DIR/report.md"
RAW="$OUT_DIR/raw.log"

log(){ echo -e "$*" | tee -a "$RAW" >/dev/null; }
hr(){ log "\n------------------------------------------------------------"; }

url_join() {
  local base="${1%/}"
  local path="$2"
  if [[ "$path" == /* ]]; then echo "${base}${path}"; else echo "${base}/${path}"; fi
}

have(){ command -v "$1" >/dev/null 2>&1; }

curl_time() {
  # http_code ttfb total size content_type
  local url="$1"
  curl -sS -o /dev/null \
    -w "%{http_code} %{time_starttransfer} %{time_total} %{size_download} %{content_type}\n" \
    "$url"
}

curl_headers() {
  local url="$1"
  curl -sS -D - -o /dev/null "$url"
}

avg(){ awk '{s+=$1} END{ if(NR==0) print "NaN"; else printf "%.4f", s/NR }'; }
minv(){ awk 'NR==1{m=$1} {if($1<m)m=$1} END{printf "%.4f", m}'; }
maxv(){ awk 'NR==1{m=$1} {if($1>m)m=$1} END{printf "%.4f", m}'; }

median(){
  awk '{a[NR]=$1} END{
    n=NR; if(n==0){print "NaN"; exit}
    asort(a)
    if(n%2==1) printf "%.4f", a[(n+1)/2]
    else printf "%.4f", (a[n/2]+a[n/2+1])/2
  }'
}

bytes_to_human() {
  python3 - <<'PY' "$1"
import sys
b=float(sys.argv[1])
for u in ["B","KB","MB","GB","TB"]:
    if b<1024 or u=="TB":
        print(f"{b:.1f}{u}")
        break
    b/=1024
PY
}

: > "$RAW"

cat > "$REPORT" <<EOF
# Loto Tracker - Speed Audit Report

- Base URL: \`$BASE_URL\`
- Date: \`$(date -Iseconds)\`
- Iterations per URL: \`$ITERATIONS\`

This audit covers: **TTFB**, **total time**, **headers (cache/compression)**, optional **assets scan** and optional **load test**.
EOF

hr
log "Base URL: $BASE_URL"
log "Output: $OUT_DIR"

# Basic reachability
ROOT="$(url_join "$BASE_URL" "/")"
hr
log "Reachability check: $ROOT"
root_line="$(curl_time "$ROOT" || true)"
log "Root timing: $root_line"
ROOT_CODE="$(echo "$root_line" | awk '{print $1}')"
if [[ "$ROOT_CODE" == "000" ]]; then
  echo "❌ Impossible de joindre $BASE_URL" | tee -a "$RAW"
  exit 1
fi

# Root headers summary + compression test
hr
log "Root headers..."
ROOT_HEADERS="$(curl_headers "$ROOT")"
echo "$ROOT_HEADERS" > "$OUT_DIR/root_headers.txt"

PROTO_LINE="$(echo "$ROOT_HEADERS" | head -n1 | tr -d '\r')"
SERVER_LINE="$(echo "$ROOT_HEADERS" | grep -i '^server:' | head -n1 | tr -d '\r' || true)"
ENC_LINE="$(echo "$ROOT_HEADERS" | grep -i '^content-encoding:' | head -n1 | tr -d '\r' || true)"
CACHE_LINE="$(echo "$ROOT_HEADERS" | grep -i '^cache-control:' | head -n1 | tr -d '\r' || true)"
ETAG_LINE="$(echo "$ROOT_HEADERS" | grep -i '^etag:' | head -n1 | tr -d '\r' || true)"

HTTP2="unknown"
if have curl; then
  if curl -sS --http2 -I "$ROOT" >/dev/null 2>&1; then HTTP2="yes (curl --http2 ok)"; else HTTP2="no/unknown"; fi
fi

SIZE_ID="$(curl -sS -H "Accept-Encoding: identity" -o /dev/null -w "%{size_download}\n" "$ROOT")"
SIZE_GZ="$(curl -sS -H "Accept-Encoding: gzip" -o /dev/null -w "%{size_download}\n" "$ROOT")"
H_ID="$(bytes_to_human "$SIZE_ID")"
H_GZ="$(bytes_to_human "$SIZE_GZ")"

cat >> "$REPORT" <<EOF

## 1) Root headers / network
- Status line: \`$PROTO_LINE\`
- Server: \`${SERVER_LINE:-N/A}\`
- HTTP/2: \`$HTTP2\`
- Content-Encoding: \`${ENC_LINE:-none}\`
- Cache-Control: \`${CACHE_LINE:-none}\`
- ETag: \`${ETAG_LINE:-none}\`

Compression (root):
- identity: \`$H_ID\`
- gzip request: \`$H_GZ\`

Headers saved: \`$OUT_DIR/root_headers.txt\`
EOF

measure_url() {
  local name="$1"
  local url="$2"

  local ttfb_file="$OUT_DIR/${name}_ttfb.txt"
  local total_file="$OUT_DIR/${name}_total.txt"
  local size_file="$OUT_DIR/${name}_size.txt"
  : > "$ttfb_file"; : > "$total_file"; : > "$size_file"

  local last_code=""

  for _ in $(seq 1 "$ITERATIONS"); do
    line="$(curl_time "$url" || true)"
    code="$(echo "$line" | awk '{print $1}')"
    ttfb="$(echo "$line" | awk '{print $2}')"
    total="$(echo "$line" | awk '{print $3}')"
    size="$(echo "$line" | awk '{print $4}')"
    last_code="$code"
    echo "$ttfb" >> "$ttfb_file"
    echo "$total" >> "$total_file"
    echo "$size" >> "$size_file"
  done

  local t_avg t_med t_min t_max
  local x_avg x_med x_min x_max
  local s_avg s_h

  t_avg="$(avg < "$ttfb_file")"
  t_med="$(median < "$ttfb_file")"
  t_min="$(minv < "$ttfb_file")"
  t_max="$(maxv < "$ttfb_file")"

  x_avg="$(avg < "$total_file")"
  x_med="$(median < "$total_file")"
  x_min="$(minv < "$total_file")"
  x_max="$(maxv < "$total_file")"

  s_avg="$(awk '{s+=$1} END{ if(NR==0) print 0; else print s/NR }' < "$size_file")"
  s_h="$(bytes_to_human "$s_avg")"

  cat >> "$REPORT" <<EOF

## Timings — $name
URL: \`$url\`
Last HTTP code: \`$last_code\`

**TTFB (s)** avg \`$t_avg\` | median \`$t_med\` | min \`$t_min\` | max \`$t_max\`
**Total (s)** avg \`$x_avg\` | median \`$x_med\` | min \`$x_min\` | max \`$x_max\`
**Avg downloaded size**: \`$s_h\`
EOF
}

hr
log "Measuring endpoints..."
for ep in "${ENDPOINTS[@]}"; do
  name="$(echo "$ep" | sed 's#[^a-zA-Z0-9]#_#g' | sed 's/^_//;s/_$//')"
  url="$(url_join "$BASE_URL" "$ep")"
  log " -> $ep"
  curl_headers "$url" > "$OUT_DIR/headers_${name}.txt" 2>/dev/null || true
  measure_url "$name" "$url"
done

# Assets scan (ignore *.zip)
if [[ "$DISCOVER_ASSETS" == "1" ]]; then
  hr
  log "Assets discovery from root HTML (ignoring *.zip)..."
  HTML="$OUT_DIR/root.html"
  curl -sS "$ROOT" -o "$HTML"

  ASSETS="$OUT_DIR/assets.txt"
  grep -Eoi '(href|src)=["'"'"'][^"'"'"']+["'"'"']' "$HTML" \
    | sed -E 's/^(href|src)=["'"'"']([^"'"'"']+)["'"'"']$/\2/i' \
    | grep -Ev '^(#|mailto:|tel:|javascript:)' \
    | grep -Evi '\.zip([?#].*)?$' \
    | sort -u > "$ASSETS" || true

  count="$(wc -l < "$ASSETS" | tr -d ' ')"
  if [[ "$count" -gt "$ASSETS_LIMIT" ]]; then
    head -n "$ASSETS_LIMIT" "$ASSETS" > "$OUT_DIR/assets_limited.txt"
    ASSETS="$OUT_DIR/assets_limited.txt"
  fi

  cat >> "$REPORT" <<EOF

## 2) Assets (from root HTML, ignoring .zip)
Assets detected: \`$count\` (limited to \`$ASSETS_LIMIT\` if needed)
List: \`$ASSETS\`

| asset | code | ttfb(s) | total(s) | size | encoding | cache-control |
|---|---:|---:|---:|---:|---|---|
EOF

  if [[ -s "$ASSETS" ]]; then
    while IFS= read -r asset; do
      [[ -z "$asset" ]] && continue
      if [[ "$asset" =~ ^https?:// ]]; then asset_url="$asset"; else asset_url="$(url_join "$BASE_URL" "$asset")"; fi

      tline="$(curl_time "$asset_url" || echo "000 0 0 0 -")"
      code="$(echo "$tline" | awk '{print $1}')"
      ttfb="$(echo "$tline" | awk '{print $2}')"
      total="$(echo "$tline" | awk '{print $3}')"
      size="$(echo "$tline" | awk '{print $4}')"
      size_h="$(bytes_to_human "$size")"

      h="$(curl_headers "$asset_url" 2>/dev/null || true)"
      enc="$(echo "$h" | grep -i '^content-encoding:' | head -n1 | cut -d: -f2- | xargs || echo "-")"
      cc="$(echo "$h" | grep -i '^cache-control:' | head -n1 | cut -d: -f2- | xargs || echo "-")"

      echo "| \`$asset\` | $code | $ttfb | $total | $size_h | \`$enc\` | \`$cc\` |" >> "$REPORT"
    done < "$ASSETS"
  else
    echo "_No assets detected (maybe loaded dynamically)_" >> "$REPORT"
  fi
fi

# Optional load test
hr
if have ab; then
  log "Load test (ab) on /api/health ..."
  AB_OUT="$OUT_DIR/ab_api_health.txt"
  ab -n "$AB_REQUESTS" -c "$AB_CONCURRENCY" "$(url_join "$BASE_URL" "/api/health")" > "$AB_OUT" 2>&1 || true
  RPS="$(grep -i 'Requests per second' "$AB_OUT" | awk -F: '{print $2}' | xargs || true)"
  FAIL="$(grep -i 'Failed requests' "$AB_OUT" | awk -F: '{print $2}' | xargs || true)"

  cat >> "$REPORT" <<EOF

## 3) Load test (ab) — /api/health
- Requests: \`$AB_REQUESTS\`
- Concurrency: \`$AB_CONCURRENCY\`
- Requests/sec: \`${RPS:-N/A}\`
- Failed requests: \`${FAIL:-N/A}\`
Full output: \`$AB_OUT\`
EOF
else
  cat >> "$REPORT" <<EOF

## 3) Load test (ab)
ApacheBench not installed. Install:
\`\`\`bash
sudo apt-get update && sudo apt-get install -y apache2-utils
\`\`\`
EOF
fi

cat >> "$REPORT" <<'EOF'

---

# Lecture rapide
- **TTFB élevé** sur /api/* → backend/DB/proxy/cache à optimiser
- **Pas de Content-Encoding** → activer gzip/brotli
- **Cache-Control absent** sur assets → activer cache navigateur côté Apache
- **Load test avec erreurs** → CPU/RAM, DB indexes, limites proxy/timeouts

EOF

echo ""
echo "✅ Audit terminé"
echo "📄 Rapport: $REPORT"
echo "🧾 Log brut: $RAW"
