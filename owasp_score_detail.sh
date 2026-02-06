#!/usr/bin/env bash
# OWASP SAFE AUTO-SCORER v2.1 (non destructif) + DETAIL MODE
#
# File: owasp_score_detail.sh
#
# Usage:
#   chmod +x owasp_score_detail.sh
#   ./owasp_score_detail.sh
#   ./owasp_score_detail.sh --detail
#
# Optional env:
#   MODE=strict DETAIL=1 USER_TOKEN="..." ADMIN_TOKEN="..." ./owasp_score_detail.sh
#
# SAFE: HEAD/GET only + 1 URL-encoded query (no brute force, no fuzzing, no writes)

set -euo pipefail

FRONT_URL="${FRONT_URL:-http://127.0.0.1:5500}"
API_URL="${API_URL:-http://127.0.0.1:8082}"

TICKETS_ENDPOINT="${TICKETS_ENDPOINT:-/api/tickets}"
ADMIN_ENDPOINT="${ADMIN_ENDPOINT:-/api/admin}"
SEARCH_ENDPOINT="${SEARCH_ENDPOINT:-/api/search?q=__PAYLOAD__}"
NOTFOUND_ENDPOINT="${NOTFOUND_ENDPOINT:-/api/does-not-exist}"
HEALTH_ENDPOINT="${HEALTH_ENDPOINT:-/actuator/health}"

USER_TOKEN="${USER_TOKEN:-}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"

MODE="${MODE:-safe}"  # safe | strict (still safe tests)
DETAIL="${DETAIL:-0}"

if [[ "${1:-}" == "--detail" ]]; then DETAIL=1; fi

# ---------------------------
# Helpers
# ---------------------------
curl_head() { curl -sS -I --max-time 8 "$1" || true; }
curl_get()  { local url="$1"; shift || true; curl -sS -i --max-time 10 "$@" "$url" || true; }

status_from_response() { awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}'; }

has_header() {
  local headers="$1" name="$2"
  echo "$headers" | awk -v n="$name" 'BEGIN{IGNORECASE=1} $0 ~ ("^" n ":") {found=1} END{print found?1:0}'
}
header_value() {
  local headers="$1" name="$2"
  echo "$headers" | awk -v n="$name" 'BEGIN{IGNORECASE=1} $0 ~ ("^" n ":") {sub("^[^:]+:[[:space:]]*",""); print; exit}'
}
body_from_response() { awk 'BEGIN{inbody=0} /^[\r]*$/{inbody=1; next} {if(inbody) print}'; }

contains_any() {
  local hay="$1"; shift
  for needle in "$@"; do echo "$hay" | grep -qi -- "$needle" && return 0; done
  return 1
}
clamp10() { local x="$1"; ((x<0)) && x=0; ((x>10)) && x=10; echo "$x"; }

print_header_presence() {
  local label="$1" H="$2"
  echo "[$label] header presence:"
  for k in \
    "Content-Security-Policy" \
    "Strict-Transport-Security" \
    "X-Frame-Options" \
    "X-Content-Type-Options" \
    "Referrer-Policy" \
    "Permissions-Policy" \
    "Cross-Origin-Opener-Policy" \
    "Cross-Origin-Embedder-Policy" \
    "Cross-Origin-Resource-Policy" \
    "Access-Control-Allow-Origin" \
    "Set-Cookie"
  do
    local ok
    ok="$(has_header "$H" "$k")"
    if [[ "$ok" -eq 1 ]]; then
      echo "  ✅ $k: $(header_value "$H" "$k")"
    else
      echo "  ❌ $k: (absent)"
    fi
  done
}

# ---------------------------
# Collect headers
# ---------------------------
FRONT_H="$(curl_head "$FRONT_URL/")"
API_H="$(curl_head "$API_URL/")"

FRONT_CODE="$(echo "$FRONT_H" | status_from_response)"
API_CODE="$(echo "$API_H" | status_from_response)"

# ---------------------------
# A02 scoring (headers)
# Rubric:
# +2 CSP present
# +1 CSP has default-src
# +1 CSP has script-src
# +1 CSP has frame-ancestors
# +1 X-Frame-Options present
# +1 X-Content-Type-Options present
# +1 Referrer-Policy present
# +1 Permissions-Policy present
# +1 COOP/COEP/CORP present (>=2)
# +1 HSTS present
# Cap 10
# ---------------------------
score_a02_headers() {
  local H="$1"
  local score=0
  local reasons=()

  local csp_has xfo_has xcto_has ref_has perm_has coop_has coep_has corp_has hsts_has
  csp_has="$(has_header "$H" "Content-Security-Policy")"
  xfo_has="$(has_header "$H" "X-Frame-Options")"
  xcto_has="$(has_header "$H" "X-Content-Type-Options")"
  ref_has="$(has_header "$H" "Referrer-Policy")"
  perm_has="$(has_header "$H" "Permissions-Policy")"
  coop_has="$(has_header "$H" "Cross-Origin-Opener-Policy")"
  coep_has="$(has_header "$H" "Cross-Origin-Embedder-Policy")"
  corp_has="$(has_header "$H" "Cross-Origin-Resource-Policy")"
  hsts_has="$(has_header "$H" "Strict-Transport-Security")"

  if [[ "$csp_has" -eq 1 ]]; then
    ((score += 2)); reasons+=("+2 CSP present")
    local csp; csp="$(header_value "$H" "Content-Security-Policy")"
    echo "$csp" | grep -qi "default-src" && { ((score+=1)); reasons+=("+1 CSP has default-src"); }
    echo "$csp" | grep -qi "script-src"  && { ((score+=1)); reasons+=("+1 CSP has script-src"); }
    echo "$csp" | grep -qi "frame-ancestors" && { ((score+=1)); reasons+=("+1 CSP has frame-ancestors"); }
  fi

  [[ "$xfo_has" -eq 1 ]] && { ((score+=1)); reasons+=("+1 X-Frame-Options"); }
  [[ "$xcto_has" -eq 1 ]] && { ((score+=1)); reasons+=("+1 X-Content-Type-Options"); }
  [[ "$ref_has" -eq 1 ]] && { ((score+=1)); reasons+=("+1 Referrer-Policy"); }
  [[ "$perm_has" -eq 1 ]] && { ((score+=1)); reasons+=("+1 Permissions-Policy"); }

  local co_count=0
  [[ "$coop_has" -eq 1 ]] && ((co_count++))
  [[ "$coep_has" -eq 1 ]] && ((co_count++))
  [[ "$corp_has" -eq 1 ]] && ((co_count++))
  ((co_count >= 2)) && { ((score+=1)); reasons+=("+1 COOP/COEP/CORP (>=2)"); }

  [[ "$hsts_has" -eq 1 ]] && { ((score+=1)); reasons+=("+1 HSTS"); }

  score="$(clamp10 "$score")"

  if [[ "$DETAIL" -eq 1 ]]; then
    echo "__SCORE__=$score"
    printf "__REASONS__=%s\n" "$(IFS='; '; echo "${reasons[*]:-none}")"
  else
    echo "$score"
  fi
}

if [[ "$DETAIL" -eq 1 ]]; then
  A02_FRONT_RAW="$(score_a02_headers "$FRONT_H")"
  A02_API_RAW="$(score_a02_headers "$API_H")"
  A02_FRONT="$(echo "$A02_FRONT_RAW" | awk -F= '/__SCORE__/{print $2}')"
  A02_API="$(echo "$A02_API_RAW" | awk -F= '/__SCORE__/{print $2}')"
  A02_FRONT_REASONS="$(echo "$A02_FRONT_RAW" | awk -F= '/__REASONS__/{print $2}')"
  A02_API_REASONS="$(echo "$A02_API_RAW" | awk -F= '/__REASONS__/{print $2}')"
else
  A02_FRONT="$(score_a02_headers "$FRONT_H")"
  A02_API="$(score_a02_headers "$API_H")"
fi
A02=$(( (A02_FRONT + A02_API) / 2 ))

# ---------------------------
# A01 Access Control
# ---------------------------
A01=5
T_NOAUTH="$(curl_get "$API_URL$TICKETS_ENDPOINT")"
A_NOAUTH="$(curl_get "$API_URL$ADMIN_ENDPOINT")"
T_NOAUTH_CODE="$(echo "$T_NOAUTH" | status_from_response)"
A_NOAUTH_CODE="$(echo "$A_NOAUTH" | status_from_response)"

A01_REASONS=()
if [[ "$T_NOAUTH_CODE" =~ ^(401|403)$ ]] && [[ "$A_NOAUTH_CODE" =~ ^(401|403)$ ]]; then
  A01=8
  A01_REASONS+=("tickets/admin protected (401/403)")
fi
if [[ "$T_NOAUTH_CODE" == "200" ]] || [[ "$A_NOAUTH_CODE" == "200" ]]; then
  A01=2
  A01_REASONS=("public access detected (200)")
fi
if [[ -n "$USER_TOKEN" ]]; then
  A_USER="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  A_USER_CODE="$(echo "$A_USER" | status_from_response)"
  [[ "$A_USER_CODE" == "403" ]] && { A01=$((A01+1)); A01_REASONS+=("USER blocked from admin (403)"); }
fi
if [[ -n "$ADMIN_TOKEN" ]]; then
  A_ADMIN="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $ADMIN_TOKEN")"
  A_ADMIN_CODE="$(echo "$A_ADMIN" | status_from_response)"
  if [[ ! "$A_ADMIN_CODE" =~ ^(401|403)$ ]] && [[ "$A_ADMIN_CODE" != "0" ]]; then
    A01=$((A01+1))
    A01_REASONS+=("ADMIN can access admin endpoint")
  fi
fi
A01="$(clamp10 "$A01")"

# ---------------------------
# A07 Authentication
# ---------------------------
A07=5
A07_REASONS=()
[[ "$T_NOAUTH_CODE" =~ ^(401|403)$ ]] && { A07=6; A07_REASONS+=("protected endpoint returns 401/403"); }

if [[ -n "$USER_TOKEN" ]]; then
  T_USER="$(curl_get "$API_URL$TICKETS_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  T_USER_CODE="$(echo "$T_USER" | status_from_response)"
  if [[ ! "$T_USER_CODE" =~ ^(401|403)$ ]] && [[ "$T_USER_CODE" != "0" ]]; then
    A07=8
    A07_REASONS+=("USER token works on tickets endpoint")
  fi
fi

HCHK="$(curl_get "$API_URL$HEALTH_ENDPOINT")"
HCHK_CODE="$(echo "$HCHK" | status_from_response)"
if [[ "$HCHK_CODE" == "200" ]]; then
  A07_REASONS+=("actuator health is public (200)")
  [[ "$MODE" == "strict" ]] && A07=$((A07-1))
fi
A07="$(clamp10 "$A07")"

# ---------------------------
# A10 Exceptions
# ---------------------------
A10=5
A10_REASONS=()
NF="$(curl_get "$API_URL$NOTFOUND_ENDPOINT")"
NF_CODE="$(echo "$NF" | status_from_response)"
NF_BODY="$(echo "$NF" | body_from_response)"

[[ "$NF_CODE" =~ ^(401|403|404)$ ]] && { A10=7; A10_REASONS+=("notfound returns 401/403/404"); }
if ! contains_any "$NF_BODY" "Exception" "Stacktrace" "at org." "java.lang" "Caused by" "Whitelabel Error Page"; then
  A10=$((A10+1))
  A10_REASONS+=("no stacktrace keywords found")
else
  A10_REASONS+=("possible stacktrace leak detected")
fi
A10="$(clamp10 "$A10")"

# ---------------------------
# A05 Injection (safe)
# ---------------------------
A05=5
A05_REASONS=()
PAYLOAD="%27%20OR%201%3D1"
SEARCH_URL="${SEARCH_ENDPOINT/__PAYLOAD__/$PAYLOAD}"
INJ="$(curl_get "$API_URL$SEARCH_URL")"
INJ_CODE="$(echo "$INJ" | status_from_response)"
INJ_BODY="$(echo "$INJ" | body_from_response)"

if [[ "$INJ_CODE" == "400" ]]; then
  A05=8; A05_REASONS+=("400 suggests input validation")
elif [[ "$INJ_CODE" =~ ^(401|403)$ ]]; then
  A05=6; A05_REASONS+=("401/403 means endpoint protected (validation not proven)")
elif [[ "$INJ_CODE" == "200" ]]; then
  if contains_any "$INJ_BODY" "SQL" "syntax" "Mongo" "SQLException" "QueryFailed" "Whitelabel"; then
    A05=2; A05_REASONS+=("error leak on 200 suggests injection risk")
  else
    A05=5; A05_REASONS+=("200 without leaks (inconclusive)")
  fi
fi
A05="$(clamp10 "$A05")"

# ---------------------------
# CORS preflight check (affects A02 lightly)
# ---------------------------
CORS_PRE="$(curl_get "$API_URL$TICKETS_ENDPOINT" -X OPTIONS \
  -H "Origin: http://evil.local" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization")"
ACAO_LINE="$(echo "$CORS_PRE" | awk 'BEGIN{IGNORECASE=1} /^Access-Control-Allow-Origin:/{print $0; exit}')"
if echo "${ACAO_LINE:-}" | grep -qi "\*"; then
  A02=$((A02-1)); ((A02<0)) && A02=0
fi

# Defaults for non-testable categories
A03="${A03_OVERRIDE:-5}"
A04="${A04_OVERRIDE:-6}"
A06="${A06_OVERRIDE:-5}"
A08="${A08_OVERRIDE:-5}"
A09="${A09_OVERRIDE:-3}"

A03="$(clamp10 "$A03")"
A04="$(clamp10 "$A04")"
A06="$(clamp10 "$A06")"
A08="$(clamp10 "$A08")"
A09="$(clamp10 "$A09")"

TOTAL=$((A01 + A02 + A03 + A04 + A05 + A06 + A07 + A08 + A09 + A10))

# ---------------------------
# Output
# ---------------------------
echo "=============================="
echo " OWASP SAFE AUTO-SCORE v2.1 (Top 10:2025)"
echo " MODE : $MODE"
echo " FRONT: $FRONT_URL  (HTTP $FRONT_CODE)"
echo " API  : $API_URL    (HTTP $API_CODE)"
echo "=============================="
echo
printf "A01 Broken Access Control........: %2d/10 (tickets:%s admin:%s)\n" "$A01" "$T_NOAUTH_CODE" "$A_NOAUTH_CODE"
printf "A02 Security Misconfiguration.....: %2d/10 (front:%d api:%d)\n" "$A02" "$A02_FRONT" "$A02_API"
printf "A03 Supply Chain Failures.........: %2d/10\n" "$A03"
printf "A04 Cryptographic Failures........: %2d/10\n" "$A04"
printf "A05 Injection.....................: %2d/10 (search status:%s)\n" "$A05" "${INJ_CODE:-?}"
printf "A06 Insecure Design...............: %2d/10\n" "$A06"
printf "A07 Authentication Failures.......: %2d/10 (health:%s)\n" "$A07" "${HCHK_CODE:-?}"
printf "A08 Integrity Failures............: %2d/10\n" "$A08"
printf "A09 Logging & Alerting............: %2d/10\n" "$A09"
printf "A10 Exceptional Conditions.........: %2d/10 (notfound:%s)\n" "$A10" "${NF_CODE:-?}"
echo
echo "------------------------------"
echo "TOTAL..........................: $TOTAL/100  =>  Protection: ${TOTAL}%"
echo "------------------------------"

if [[ "$DETAIL" -eq 1 ]]; then
  echo
  echo "========== DETAIL =========="
  echo
  print_header_presence "FRONT" "$FRONT_H"
  echo
  print_header_presence "API" "$API_H"
  echo
  echo "[A01] reasons: ${A01_REASONS[*]:-none}"
  echo "[A02] FRONT reasons: ${A02_FRONT_REASONS:-n/a}"
  echo "[A02] API   reasons: ${A02_API_REASONS:-n/a}"
  echo "[A05] reasons: ${A05_REASONS[*]:-none}"
  echo "[A07] reasons: ${A07_REASONS[*]:-none}"
  echo "[A10] reasons: ${A10_REASONS[*]:-none}"
  echo
  echo "JSON-like summary:"
  cat <<EOF
{
  "front_url": "$FRONT_URL",
  "api_url": "$API_URL",
  "status": {"front": "$FRONT_CODE", "api": "$API_CODE"},
  "scores": {"A01": $A01, "A02": $A02, "A03": $A03, "A04": $A04, "A05": $A05, "A06": $A06, "A07": $A07, "A08": $A08, "A09": $A09, "A10": $A10},
  "total": $TOTAL
}
EOF
fi
