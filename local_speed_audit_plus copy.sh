#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LOCAL SPEED AUDIT PLUS — SAFE DB
# - Compare BEFORE/AFTER (baseline vs current)
# - Global score 0..100
# - Export JSON for Admin dashboard
#
# Backend : http://localhost:8082
# Frontend: http://localhost:5500
# GET only / No stress / No auth / No DB writes
# ============================================================

API_BASE="${API_BASE:-http://localhost:8082}"
FRONT_BASE="${FRONT_BASE:-http://localhost:5500}"

ITERATIONS="${ITERATIONS:-6}"
OUT_DIR="${OUT_DIR:-./local_speed_audit}"
mkdir -p "$OUT_DIR"

BASELINE_JSON="$OUT_DIR/baseline.json"
CURRENT_JSON="$OUT_DIR/current.json"
DIFF_JSON="$OUT_DIR/diff.json"
REPORT_MD="$OUT_DIR/report.md"

MODE="${1:-run}" # run | baseline | compare

# ---- SAFE endpoints only ----
API_ENDPOINTS=(
  "/api/health"
  "/api/hello"
  "/api/historique/last20"
)

FRONT_PAGES=(
  "/"
)

# ---- Helpers ----
have(){ command -v "$1" >/dev/null 2>&1; }

curl_time_line() {
  # http_code ttfb total size_download
  curl -sS -o /dev/null \
    -w "%{http_code} %{time_starttransfer} %{time_total} %{size_download}\n" \
    "$1" || echo "000 0 0 0"
}

headers_block() {
  curl -sS -D - -o /dev/null "$1" 2>/dev/null || true
}

avg(){ awk '{s+=$1} END{ if(NR==0) print "0"; else printf "%.6f", s/NR }'; }
minv(){ awk 'NR==1{m=$1} {if($1<m)m=$1} END{ if(NR==0) print "0"; else printf "%.6f", m }'; }
maxv(){ awk 'NR==1{m=$1} {if($1>m)m=$1} END{ if(NR==0) print "0"; else printf "%.6f", m }'; }

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

escape_json() {
  python3 - <<'PY'
import json,sys
print(json.dumps(sys.stdin.read()))
PY
}

make_url() {
  local base="$1"
  local path="$2"
  base="${base%/}"
  if [[ "$path" == /* ]]; then echo "${base}${path}"; else echo "${base}/${path}"; fi
}

# ---- Scoring (simple + effective) ----
# Score per endpoint uses average TTFB and total.
# - Perfect-ish: TTFB <= 0.12s and Total <= 0.30s
# - Bad:        TTFB >= 1.00s or Total >= 2.00s
score_endpoint() {
  local ttfb="$1" total="$2"
  python3 - <<'PY' "$ttfb" "$total"
import sys, math
ttfb=float(sys.argv[1]); total=float(sys.argv[2])

def clamp(x,a,b): return max(a,min(b,x))

# Normalize: 0 (best) to 1 (worst)
n_t = (ttfb - 0.12) / (1.00 - 0.12)
n_x = (total - 0.30) / (2.00 - 0.30)
n_t = clamp(n_t, 0.0, 1.0)
n_x = clamp(n_x, 0.0, 1.0)

# Weighted penalty, then convert to score
penalty = 0.65*n_t + 0.35*n_x
score = 100.0 * (1.0 - penalty)

print(f"{score:.1f}")
PY
}

score_headers() {
  # Input: headers text
  # +10 if gzip/br
  # +10 if cache-control on static-ish (front root often not cached; we apply soft bonus)
  # +5 if etag present
  python3 - <<'PY'
import sys,re
h=sys.stdin.read().lower()
score=0
if "content-encoding: gzip" in h or "content-encoding: br" in h:
    score += 10
if "cache-control:" in h:
    score += 10
if re.search(r"\betag:\b", h):
    score += 5
print(score)
PY
}

measure_one() {
  local name="$1"
  local url="$2"

  local tmp_t="$OUT_DIR/.tmp_${name}_ttfb.txt"
  local tmp_x="$OUT_DIR/.tmp_${name}_total.txt"
  local tmp_s="$OUT_DIR/.tmp_${name}_size.txt"
  : > "$tmp_t"; : > "$tmp_x"; : > "$tmp_s"

  local code="000"
  for _ in $(seq 1 "$ITERATIONS"); do
    read c t x s <<<"$(curl_time_line "$url")"
    code="$c"
    echo "$t" >> "$tmp_t"
    echo "$x" >> "$tmp_x"
    echo "$s" >> "$tmp_s"
  done

  local t_avg x_avg s_avg
  t_avg="$(avg < "$tmp_t")"
  x_avg="$(avg < "$tmp_x")"
  s_avg="$(avg < "$tmp_s")"

  local headers
  headers="$(headers_block "$url")"
  local hscore
  hscore="$(printf "%s" "$headers" | score_headers)"

  local esc_headers
  esc_headers="$(printf "%s" "$headers" | escape_json)"

  local epscore
  epscore="$(score_endpoint "$t_avg" "$x_avg")"

  # Emit a JSON object fragment
  cat <<EOF
{
  "name": "$(printf "%s" "$name")",
  "url": "$(printf "%s" "$url")",
  "http_code": "$(printf "%s" "$code")",
  "avg_ttfb_s": $t_avg,
  "avg_total_s": $x_avg,
  "avg_size_bytes": $s_avg,
  "endpoint_score": $epscore,
  "headers_score": $hscore,
  "headers_raw": $esc_headers
}
EOF
}

discover_assets_front() {
  # Parse FRONT_BASE root HTML and extract href/src assets (ignore .zip)
  local root_url
  root_url="$(make_url "$FRONT_BASE" "/")"
  local html="$OUT_DIR/front_root.html"
  curl -sS "$root_url" -o "$html" 2>/dev/null || true

  if [[ ! -s "$html" ]]; then
    echo ""
    return 0
  fi

  grep -Eoi '(src|href)=["'"'"'][^"'"'"']+["'"'"']' "$html" \
    | sed -E 's/^(src|href)=["'"'"']([^"'"'"']+)["'"'"']$/\2/i' \
    | grep -Ev '\.zip([?#].*)?$' \
    | grep -Ev '^(#|mailto:|tel:|javascript:)' \
    | sort -u
}

build_current_json() {
  local started
  started="$(date -Iseconds)"

  # Reachability quick check
  local api_health
  api_health="$(make_url "$API_BASE" "/api/health")"
  read hc _ <<<"$(curl_time_line "$api_health")"
  if [[ "$hc" == "000" ]]; then
    echo "ERROR: backend not reachable at $API_BASE" >&2
    exit 1
  fi

  local front_root
  front_root="$(make_url "$FRONT_BASE" "/")"
  read fc _ <<<"$(curl_time_line "$front_root")"
  if [[ "$fc" == "000" ]]; then
    echo "ERROR: frontend not reachable at $FRONT_BASE" >&2
    exit 1
  fi

  # Measure items
  local items_json=""

  # Front pages
  for p in "${FRONT_PAGES[@]}"; do
    local url name
    url="$(make_url "$FRONT_BASE" "$p")"
    name="front$(echo "$p" | tr '/.' '__')"
    local obj
    obj="$(measure_one "$name" "$url")"
    items_json+="${obj},"
  done

  # Discovered assets
  local assets
  assets="$(discover_assets_front || true)"
  if [[ -n "$assets" ]]; then
    while IFS= read -r a; do
      [[ -z "$a" ]] && continue
      local url name
      if [[ "$a" =~ ^https?:// ]]; then url="$a"; else url="$(make_url "$FRONT_BASE" "$a")"; fi
      name="asset$(echo "$a" | tr '/.' '__')"
      items_json+="$(measure_one "$name" "$url"),"
    done <<< "$assets"
  fi

  # API endpoints
  for ep in "${API_ENDPOINTS[@]}"; do
    local url name
    url="$(make_url "$API_BASE" "$ep")"
    name="api$(echo "$ep" | tr '/.' '__')"
    items_json+="$(measure_one "$name" "$url"),"
  done

  # Remove trailing comma
  items_json="${items_json%,}"

  # Compute global score = average(endpoint_score) + average(headers_score)/25*10 (small bonus)
  python3 - <<PY > "$CURRENT_JSON"
import json, statistics, datetime

started = "$started"
items = json.loads("[" + """$items_json""" + "]")

ep_scores = [float(i.get("endpoint_score", 0)) for i in items]
hdr_scores = [float(i.get("headers_score", 0)) for i in items]

ep_avg = statistics.mean(ep_scores) if ep_scores else 0.0
hdr_avg = statistics.mean(hdr_scores) if hdr_scores else 0.0

# headers_score max = 25; we map it to a 0..10 bonus (soft)
bonus = max(0.0, min(10.0, (hdr_avg/25.0)*10.0))

global_score = max(0.0, min(100.0, ep_avg*0.9 + bonus*0.1))

out = {
  "meta": {
    "mode": "current",
    "started_at": started,
    "api_base": "$API_BASE",
    "front_base": "$FRONT_BASE",
    "iterations": int("$ITERATIONS"),
  },
  "summary": {
    "endpoint_score_avg": round(ep_avg, 1),
    "headers_score_avg": round(hdr_avg, 1),
    "global_score": round(global_score, 1),
    "items_count": len(items),
  },
  "items": items
}

print(json.dumps(out, indent=2))
PY

  # Minimal markdown report
  python3 - <<'PY' "$CURRENT_JSON" > "$REPORT_MD"
import json,sys
p=sys.argv[1]
d=json.load(open(p,"r"))
s=d["summary"]; m=d["meta"]
print("# Local Speed Audit — Report\n")
print(f"- API: `{m['api_base']}`")
print(f"- Front: `{m['front_base']}`")
print(f"- Iterations: `{m['iterations']}`")
print(f"- Global score: **{s['global_score']} / 100**")
print("")
print("## Summary")
print(f"- Endpoint score avg: `{s['endpoint_score_avg']}`")
print(f"- Headers score avg: `{s['headers_score_avg']}`")
print(f"- Items: `{s['items_count']}`")
print("")
print("## Slowest by TTFB (top 10)")
items=sorted(d["items"], key=lambda x: x.get("avg_ttfb_s",0), reverse=True)[:10]
for i in items:
    print(f"- {i['name']}: ttfb={i['avg_ttfb_s']:.3f}s total={i['avg_total_s']:.3f}s code={i['http_code']} url={i['url']}")
print("")
print("## Slowest by Total (top 10)")
items=sorted(d["items"], key=lambda x: x.get("avg_total_s",0), reverse=True)[:10]
for i in items:
    print(f"- {i['name']}: total={i['avg_total_s']:.3f}s ttfb={i['avg_ttfb_s']:.3f}s code={i['http_code']} url={i['url']}")
PY
}

compare_with_baseline() {
  if [[ ! -f "$BASELINE_JSON" ]]; then
    echo "ERROR: baseline missing. Run: $0 baseline" >&2
    exit 1
  fi
  if [[ ! -f "$CURRENT_JSON" ]]; then
    echo "ERROR: current missing. Run: $0 run" >&2
    exit 1
  fi

  python3 - <<'PY' "$BASELINE_JSON" "$CURRENT_JSON" > "$DIFF_JSON"
import json,sys

b=json.load(open(sys.argv[1],"r"))
c=json.load(open(sys.argv[2],"r"))

b_items={i["name"]: i for i in b.get("items",[])}
c_items={i["name"]: i for i in c.get("items",[])}

all_names=sorted(set(b_items)|set(c_items))

diff=[]
for n in all_names:
    bi=b_items.get(n)
    ci=c_items.get(n)
    if not bi or not ci:
        diff.append({"name": n, "status": "added" if ci and not bi else "missing"})
        continue
    dt_ttfb=ci["avg_ttfb_s"]-bi["avg_ttfb_s"]
    dt_total=ci["avg_total_s"]-bi["avg_total_s"]
    ds=ci["avg_size_bytes"]-bi["avg_size_bytes"]
    diff.append({
        "name": n,
        "url": ci.get("url", bi.get("url","")),
        "ttfb_before": bi["avg_ttfb_s"],
        "ttfb_after": ci["avg_ttfb_s"],
        "ttfb_delta": dt_ttfb,
        "total_before": bi["avg_total_s"],
        "total_after": ci["avg_total_s"],
        "total_delta": dt_total,
        "size_before": bi["avg_size_bytes"],
        "size_after": ci["avg_size_bytes"],
        "size_delta": ds,
        "score_before": bi.get("endpoint_score", 0),
        "score_after": ci.get("endpoint_score", 0),
        "score_delta": float(ci.get("endpoint_score",0))-float(bi.get("endpoint_score",0))
    })

# Summary deltas
sb=b["summary"]; sc=c["summary"]
out={
  "meta": {
    "baseline_started_at": b["meta"].get("started_at"),
    "current_started_at": c["meta"].get("started_at"),
    "api_base": c["meta"].get("api_base"),
    "front_base": c["meta"].get("front_base"),
  },
  "summary": {
    "global_before": sb.get("global_score"),
    "global_after": sc.get("global_score"),
    "global_delta": (sc.get("global_score",0) - sb.get("global_score",0)),
    "endpoint_avg_before": sb.get("endpoint_score_avg"),
    "endpoint_avg_after": sc.get("endpoint_score_avg"),
    "endpoint_avg_delta": (sc.get("endpoint_score_avg",0) - sb.get("endpoint_score_avg",0)),
    "items_count": len(diff),
  },
  "diff": diff
}
print(json.dumps(out, indent=2))
PY
}

case "$MODE" in
  baseline)
    build_current_json
    cp "$CURRENT_JSON" "$BASELINE_JSON"
    echo "✅ Baseline saved: $BASELINE_JSON"
    echo "📄 Report: $REPORT_MD"
    ;;
  run)
    build_current_json
    echo "✅ Current saved: $CURRENT_JSON"
    echo "📄 Report: $REPORT_MD"
    ;;
  compare)
    build_current_json
    compare_with_baseline
    echo "✅ Current saved: $CURRENT_JSON"
    echo "✅ Diff saved: $DIFF_JSON"
    echo "📄 Report: $REPORT_MD"
    ;;
  *)
    echo "Usage: $0 [baseline|run|compare]" >&2
    exit 1
    ;;
esac
