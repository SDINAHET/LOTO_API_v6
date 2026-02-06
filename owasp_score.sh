#!/usr/bin/env bash
# OWASP SAFE AUTO-SCORER v2 (non destructif)
# - Front: 127.0.0.1:5500
# - API  : 127.0.0.1:8082
#
# Usage:
#   chmod +x owasp_score_v2.sh
#   ./owasp_score_v2.sh
#
# Optional (improves A01/A07 and enables deeper checks):
#   USER_TOKEN="..." ADMIN_TOKEN="..." ./owasp_score_v2.sh
#
# Optional override endpoints:
#   TICKETS_ENDPOINT="/api/v2/tickets" ADMIN_ENDPOINT="/api/v2/admin" ./owasp_score_v2.sh
#
# SAFE: HEAD/GET only + one URL-encoded "injection-like" query (no fuzzing, no brute force, no writes)

set -euo pipefail

FRONT_URL="${FRONT_URL:-http://127.0.0.1:5500}"
API_URL="${API_URL:-http://127.0.0.1:8082}"

TICKETS_ENDPOINT="${TICKETS_ENDPOINT:-/api/tickets}"
ADMIN_ENDPOINT="${ADMIN_ENDPOINT:-/api/admin}"
SEARCH_ENDPOINT="${SEARCH_ENDPOINT:-/api/search?q=__PAYLOAD__}"
NOTFOUND_ENDPOINT="${NOTFOUND_ENDPOINT:-/api/does-not-exist}"
HEALTH_ENDPOINT="${HEALTH_ENDPOINT:-/actuator/health}"   # if exists, great. If not, ignored.

USER_TOKEN="${USER_TOKEN:-}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"

MODE="${MODE:-safe}"  # safe | strict (strict only changes scoring thresholds, still no harmful tests)

# ---------------------------
# Helpers
# ---------------------------
curl_head() {
  local url="$1"
  curl -sS -I --max-time 8 "$url" || true
}
curl_get() {
  local url="$1"
  shift || true
  curl -sS -i --max-time 10 "$@" "$url" || true
}
status_from_response() {
  awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}'
}
has_header() {
  local headers="$1"
  local name="$2"
  echo "$headers" | awk -v n="$name" 'BEGIN{IGNORECASE=1} $0 ~ ("^" n ":") {found=1} END{print found?1:0}'
}
header_value() {
  local headers="$1"
  local name="$2"
  echo "$headers" | awk -v n="$name" 'BEGIN{IGNORECASE=1} $0 ~ ("^" n ":") {sub("^[^:]+:[[:space:]]*",""); print; exit}'
}
body_from_response() {
  awk 'BEGIN{inbody=0} /^[\r]*$/{inbody=1; next} {if(inbody) print}'
}
contains_any() {
  local hay="$1"; shift
  for needle in "$@"; do
    if echo "$hay" | grep -qi -- "$needle"; then return 0; fi
  done
  return 1
}
min() { (( $1 < $2 )) && echo "$1" || echo "$2"; }
clamp10() { local x="$1"; ((x<0)) && x=0; ((x>10)) && x=10; echo "$x"; }

# ---------------------------
# Collect headers
# ---------------------------
FRONT_H="$(curl_head "$FRONT_URL/")"
API_H="$(curl_head "$API_URL/")"

FRONT_CODE="$(echo "$FRONT_H" | status_from_response)"
API_CODE="$(echo "$API_H" | status_from_response)"

# ---------------------------
# A02 - Security Misconfiguration (headers + CORS hints)
# We score FRONT and API separately then average.
#
# Header points (max 10):
# +2 CSP present
# +1 CSP has default-src
# +1 CSP has script-src
# +1 frame-ancestors present in CSP OR X-Frame-Options present
# +1 X-Content-Type-Options present
# +1 Referrer-Policy present
# +1 Permissions-Policy present
# +1 COOP/COEP/CORP present (any 2 of them)
# +1 HSTS present (usually only on HTTPS prod; in local may be absent)
# Cap at 10
# ---------------------------
score_a02_headers() {
  local H="$1"
  local score=0

  local csp_has="$(has_header "$H" "Content-Security-Policy")"
  local xfo_has="$(has_header "$H" "X-Frame-Options")"
  local xcto_has="$(has_header "$H" "X-Content-Type-Options")"
  local ref_has="$(has_header "$H" "Referrer-Policy")"
  local perm_has="$(has_header "$H" "Permissions-Policy")"
  local coop_has="$(has_header "$H" "Cross-Origin-Opener-Policy")"
  local coep_has="$(has_header "$H" "Cross-Origin-Embedder-Policy")"
  local corp_has="$(has_header "$H" "Cross-Origin-Resource-Policy")"
  local hsts_has="$(has_header "$H" "Strict-Transport-Security")"

  if [[ "$csp_has" -eq 1 ]]; then
    ((score += 2))
    local csp="$(header_value "$H" "Content-Security-Policy")"
    echo "$csp" | grep -qi "default-src" && ((score += 1))
    echo "$csp" | grep -qi "script-src"  && ((score += 1))
    echo "$csp" | grep -qi "frame-ancestors" && ((score += 1))
  fi

  # frame protection via CSP or XFO
  if [[ "$xfo_has" -eq 1 ]]; then ((score += 1)); fi

  [[ "$xcto_has" -eq 1 ]] && ((score += 1))
  [[ "$ref_has" -eq 1 ]]  && ((score += 1))
  [[ "$perm_has" -eq 1 ]] && ((score += 1))

  # COOP/COEP/CORP (modern browser isolation)
  local co_count=0
  [[ "$coop_has" -eq 1 ]] && ((co_count++))
  [[ "$coep_has" -eq 1 ]] && ((co_count++))
  [[ "$corp_has" -eq 1 ]] && ((co_count++))
  ((co_count >= 2)) && ((score += 1))

  [[ "$hsts_has" -eq 1 ]] && ((score += 1))

  clamp10 "$score"
}

A02_FRONT="$(score_a02_headers "$FRONT_H")"
A02_API="$(score_a02_headers "$API_H")"
A02=$(( (A02_FRONT + A02_API) / 2 ))

# ---------------------------
# A01 - Broken Access Control
# - tickets/admin should be 401/403 without auth
# - with USER_TOKEN: admin should be 403 (not 200)
# - with ADMIN_TOKEN: admin should be 200/204 (or at least not 401/403)
# ---------------------------
A01=5
T_NOAUTH="$(curl_get "$API_URL$TICKETS_ENDPOINT")"
A_NOAUTH="$(curl_get "$API_URL$ADMIN_ENDPOINT")"
T_NOAUTH_CODE="$(echo "$T_NOAUTH" | status_from_response)"
A_NOAUTH_CODE="$(echo "$A_NOAUTH" | status_from_response)"

if [[ "$T_NOAUTH_CODE" =~ ^(401|403)$ ]] && [[ "$A_NOAUTH_CODE" =~ ^(401|403)$ ]]; then
  A01=8
fi
if [[ "$T_NOAUTH_CODE" == "200" ]] || [[ "$A_NOAUTH_CODE" == "200" ]]; then
  A01=2
fi

if [[ -n "$USER_TOKEN" ]]; then
  A_USER="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  A_USER_CODE="$(echo "$A_USER" | status_from_response)"
  [[ "$A_USER_CODE" == "403" ]] && A01=$((A01+1))
fi

if [[ -n "$ADMIN_TOKEN" ]]; then
  A_ADMIN="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $ADMIN_TOKEN")"
  A_ADMIN_CODE="$(echo "$A_ADMIN" | status_from_response)"
  if [[ ! "$A_ADMIN_CODE" =~ ^(401|403)$ ]] && [[ "$A_ADMIN_CODE" != "0" ]]; then
    A01=$((A01+1))
  fi
fi

A01="$(clamp10 "$A01")"

# ---------------------------
# A07 - Authentication Failures (SAFE inference)
# - Protected endpoints return 401/403 without auth => ok baseline
# - With USER_TOKEN: tickets should become 200/204 => better
# - Optional: try health endpoint; if actuator exposed without auth -> reduce
# ---------------------------
A07=5
if [[ "$T_NOAUTH_CODE" =~ ^(401|403)$ ]]; then A07=6; fi

if [[ -n "$USER_TOKEN" ]]; then
  T_USER="$(curl_get "$API_URL$TICKETS_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  T_USER_CODE="$(echo "$T_USER" | status_from_response)"
  if [[ ! "$T_USER_CODE" =~ ^(401|403)$ ]] && [[ "$T_USER_CODE" != "0" ]]; then
    A07=8
  fi
fi

# actuator/health check (if present)
HCHK="$(curl_get "$API_URL$HEALTH_ENDPOINT")"
HCHK_CODE="$(echo "$HCHK" | status_from_response)"
if [[ "$HCHK_CODE" == "200" ]]; then
  # Exposed health endpoint can be fine, but if actuator is wide open it's a risk.
  # We only penalize lightly, and only in strict mode.
  [[ "$MODE" == "strict" ]] && A07=$((A07-1))
fi
A07="$(clamp10 "$A07")"

# ---------------------------
# A10 - Exceptional Conditions
# - Call a non-existent endpoint
# - Ensure no stacktrace keywords
# ---------------------------
A10=5
NF="$(curl_get "$API_URL$NOTFOUND_ENDPOINT")"
NF_CODE="$(echo "$NF" | status_from_response)"
NF_BODY="$(echo "$NF" | body_from_response)"

if [[ "$NF_CODE" =~ ^(401|403|404)$ ]]; then A10=7; fi
if ! contains_any "$NF_BODY" "Exception" "Stacktrace" "at org." "java.lang" "Caused by" "Whitelabel Error Page"; then
  A10=$((A10+1))
fi
A10="$(clamp10 "$A10")"

# ---------------------------
# A05 - Injection (SAFE)
# We attempt a URL-encoded payload; scoring:
# - If endpoint is protected (401/403): score = 6 (protected but not proven validation)
# - If 400: score = 8 (input validation likely)
# - If 200 with error leaks: score = 2
# - If 200 clean: score = 5 (inconclusive)
# ---------------------------
A05=5
PAYLOAD="%27%20OR%201%3D1" # ' OR 1=1
SEARCH_URL="${SEARCH_ENDPOINT/__PAYLOAD__/$PAYLOAD}"
INJ="$(curl_get "$API_URL$SEARCH_URL")"
INJ_CODE="$(echo "$INJ" | status_from_response)"
INJ_BODY="$(echo "$INJ" | body_from_response)"

if [[ "$INJ_CODE" == "400" ]]; then
  A05=8
elif [[ "$INJ_CODE" =~ ^(401|403)$ ]]; then
  A05=6
elif [[ "$INJ_CODE" == "200" ]]; then
  if contains_any "$INJ_BODY" "SQL" "syntax" "Mongo" "SQLException" "QueryFailed" "Whitelabel"; then
    A05=2
  else
    A05=5
  fi
fi
A05="$(clamp10 "$A05")"

# ---------------------------
# A02 extra: quick CORS check (SAFE)
# If API replies with wildcard ACAO on preflight, that's a misconfig.
# This lightly affects A02 (API side).
# ---------------------------
CORS_PRE="$(curl_get "$API_URL$TICKETS_ENDPOINT" -X OPTIONS \
  -H "Origin: http://evil.local" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization")"
ACAO="$(echo "$CORS_PRE" | awk 'BEGIN{IGNORECASE=1} /^Access-Control-Allow-Origin:/{print $0}')"
if echo "$ACAO" | grep -qi "\*"; then
  # Penalize A02 a bit if wildcard
  A02=$((A02-1))
  ((A02<0)) && A02=0
fi

# ---------------------------
# The remaining categories need repo/CI/log proof. Keep conservative defaults.
# You can override via env if you want "declared" scoring:
#   A03_OVERRIDE=8 A09_OVERRIDE=7 ./owasp_score_v2.sh
# ---------------------------
A03="${A03_OVERRIDE:-5}"
A04="${A04_OVERRIDE:-6}"
A06="${A06_OVERRIDE:-5}"
A08="${A08_OVERRIDE:-5}"
A09="${A09_OVERRIDE:-3}"

# Clamp overrides too
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
echo " OWASP SAFE AUTO-SCORE v2 (Top 10:2025)"
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
echo
echo "Quick hints to raise score:"
echo "- If FRONT headers are missing, fix A02 quickly by adding CSP/XFO/XCTO/Referrer/Permissions on :5500."
echo "- Provide USER_TOKEN/ADMIN_TOKEN to prove role separation and valid auth flows."
echo "- Provide log evidence to raise A09 (e.g., failed login and 403 entries)."
