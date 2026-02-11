#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LOCAL SPEED AUDIT — SAFE DB
# Backend : http://localhost:8082
# Frontend: http://localhost:5500
# GET only / No stress / No auth / No DB writes
# ============================================================

API_BASE="http://localhost:8082"
FRONT_BASE="http://localhost:5500"

ITERATIONS=6
OUT_DIR="./local_speed_audit_$(date +%Y%m%d_%H%M%S)"
mkdir -p "$OUT_DIR"

REPORT="$OUT_DIR/report.md"
RAW="$OUT_DIR/raw.log"

log(){ echo -e "$*" | tee -a "$RAW" >/dev/null; }

curl_time() {
  curl -s -o /dev/null \
    -w "%{http_code} %{time_starttransfer} %{time_total} %{size_download}\n" \
    "$1"
}

avg(){ awk '{s+=$1} END{printf "%.4f", s/NR}'; }
minv(){ awk 'NR==1{m=$1}{if($1<m)m=$1} END{printf "%.4f", m}'; }
maxv(){ awk 'NR==1{m=$1}{if($1>m)m=$1} END{printf "%.4f", m}'; }

bytes_to_human() {
  python3 - <<'PY' "$1"
import sys
b=float(sys.argv[1])
for u in ["B","KB","MB","GB"]:
    if b<1024: print(f"{b:.1f}{u}"); break
    b/=1024
PY
}

cat > "$REPORT" <<EOF
# Local Speed Audit (SAFE DB)

- Backend: \`$API_BASE\`
- Frontend: \`$FRONT_BASE\`
- Iterations: \`$ITERATIONS\`
- Date: \`$(date -Iseconds)\`

This audit is **100% read-only** and safe for databases.
EOF

measure() {
  local name="$1"
  local url="$2"

  local ttfb=() total=() size=() code=""

  for _ in $(seq 1 "$ITERATIONS"); do
    read c t x s <<<"$(curl_time "$url")"
    code="$c"
    ttfb+=("$t")
    total+=("$x")
    size+=("$s")
  done

  printf "%s\n" "${ttfb[@]}" > "$OUT_DIR/${name}_ttfb.txt"
  printf "%s\n" "${total[@]}" > "$OUT_DIR/${name}_total.txt"
  printf "%s\n" "${size[@]}"  > "$OUT_DIR/${name}_size.txt"

  local t_avg t_min t_max x_avg x_min x_max s_avg s_h
  t_avg="$(avg < "$OUT_DIR/${name}_ttfb.txt")"
  t_min="$(minv < "$OUT_DIR/${name}_ttfb.txt")"
  t_max="$(maxv < "$OUT_DIR/${name}_ttfb.txt")"

  x_avg="$(avg < "$OUT_DIR/${name}_total.txt")"
  x_min="$(minv < "$OUT_DIR/${name}_total.txt")"
  x_max="$(maxv < "$OUT_DIR/${name}_total.txt")"

  s_avg="$(avg < "$OUT_DIR/${name}_size.txt")"
  s_h="$(bytes_to_human "$s_avg")"

  cat >> "$REPORT" <<EOF

## $name
URL: \`$url\`
HTTP code: \`$code\`

- TTFB avg: \`$t_avg s\` (min \`$t_min\`, max \`$t_max\`)
- Total avg: \`$x_avg s\` (min \`$x_min\`, max \`$x_max\`)
- Avg size: \`$s_h\`
EOF
}

# ----------------------------
# FRONTEND
# ----------------------------
measure "Frontend root" "$FRONT_BASE/"

# Discover assets (ignore .zip)
HTML="$OUT_DIR/front.html"
curl -s "$FRONT_BASE/" -o "$HTML"

ASSETS="$OUT_DIR/assets.txt"
grep -Eoi '(src|href)=["'"'"'][^"'"'"']+["'"'"']' "$HTML" \
 | sed -E 's/^(src|href)=["'"'"']([^"'"'"']+)["'"'"']$/\2/i' \
 | grep -Ev '\.zip([?#].*)?$' \
 | grep -Ev '^(#|mailto:|tel:|javascript:)' \
 | sort -u > "$ASSETS" || true

cat >> "$REPORT" <<EOF

### Frontend assets (excluding .zip)
EOF

while read -r asset; do
  [[ -z "$asset" ]] && continue
  [[ "$asset" =~ ^http ]] && url="$asset" || url="$FRONT_BASE/$asset"
  name="asset_$(echo "$asset" | tr '/.' '__')"
  measure "$name" "$url"
done < "$ASSETS"

# ----------------------------
# BACKEND API (SAFE)
# ----------------------------
measure "API health" "$API_BASE/api/health"
measure "API hello" "$API_BASE/api/hello"
measure "API last20" "$API_BASE/api/historique/last20"

echo ""
echo "✅ Local speed audit terminé"
echo "📄 Rapport : $REPORT"
echo "🧾 Logs    : $RAW"
