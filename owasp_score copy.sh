#!/usr/bin/env bash
# OWASP SAFE AUTO-SCORER (non destructif) - Front 5500 + API 8082
# Usage:
#   chmod +x owasp_score.sh
#   ./owasp_score.sh
#
# Optionnel (pour pousser A01/A07 plus haut si tu as un token USER et/ou ADMIN):
#   USER_TOKEN="eyJ..." ADMIN_TOKEN="eyJ..." ./owasp_score.sh
#
# ⚠️ Ce script fait uniquement des requêtes HEAD/GET + 1 test injection "safe" encodé.
# Pas de bruteforce, pas de scan lourd, pas d'écriture en base.

set -euo pipefail

FRONT_URL="${FRONT_URL:-http://127.0.0.1:5500}"
API_URL="${API_URL:-http://127.0.0.1:8082}"

# Adapte si tes routes diffèrent :
TICKETS_ENDPOINT="${TICKETS_ENDPOINT:-/api/tickets}"
ADMIN_ENDPOINT="${ADMIN_ENDPOINT:-/api/admin}"
SEARCH_ENDPOINT="${SEARCH_ENDPOINT:-/api/search?q=__PAYLOAD__}"
NOTFOUND_ENDPOINT="${NOTFOUND_ENDPOINT:-/api/does-not-exist}"

USER_TOKEN="${USER_TOKEN:-}"
ADMIN_TOKEN="${ADMIN_TOKEN:-}"

# ---------------------------
# Helpers
# ---------------------------
curl_head() {
  local url="$1"
  curl -sS -I "$url" || true
}
curl_get() {
  local url="$1"
  shift || true
  curl -sS -i "$@" "$url" || true
}
http_status_from_headers() {
  # Parse HTTP status from a curl -I output
  awk 'BEGIN{code=0} /^HTTP\//{code=$2} END{print code}'
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
contains_any() {
  local hay="$1"
  shift
  for needle in "$@"; do
    if echo "$hay" | grep -qi -- "$needle"; then
      return 0
    fi
  done
  return 1
}
pct() {
  # percent = total/100 already, keep integer
  local x="$1"
  echo "$x"
}

# ---------------------------
# Collect headers
# ---------------------------
FRONT_H="$(curl_head "$FRONT_URL/")"
API_H="$(curl_head "$API_URL/")"

FRONT_CODE="$(echo "$FRONT_H" | http_status_from_headers)"
API_CODE="$(echo "$API_H" | http_status_from_headers)"

# ---------------------------
# A02 - Security Misconfiguration (headers)
# Score /10
# Rubric:
#  - +1 each for these headers: CSP, XFO(or frame-ancestors), XCTO, Referrer-Policy, Permissions-Policy
#  - +2 if CSP exists and contains at least default-src + script-src
#  - +1 if CSP contains frame-ancestors OR X-Frame-Options present
#  - Max 10, computed separately for FRONT and API then averaged
# ---------------------------
score_a02_for_headers() {
  local H="$1"
  local score=0

  local has_csp="$(has_header "$H" "Content-Security-Policy")"
  local has_xfo="$(has_header "$H" "X-Frame-Options")"
  local has_xcto="$(has_header "$H" "X-Content-Type-Options")"
  local has_ref="$(has_header "$H" "Referrer-Policy")"
  local has_perm="$(has_header "$H" "Permissions-Policy")"

  (( score += has_csp ))
  (( score += has_xfo ))
  (( score += has_xcto ))
  (( score += has_ref ))
  (( score += has_perm ))

  if [[ "$has_csp" -eq 1 ]]; then
    local csp="$(header_value "$H" "Content-Security-Policy")"
    if echo "$csp" | grep -qi "default-src" && echo "$csp" | grep -qi "script-src"; then
      (( score += 2 ))
    else
      (( score += 1 ))
    fi
    if echo "$csp" | grep -qi "frame-ancestors"; then
      (( score += 1 ))
    fi
  fi

  # Bonus if either CSP frame-ancestors or XFO exists
  if [[ "$has_xfo" -eq 1 ]] || ( [[ "$has_csp" -eq 1 ]] && echo "$(header_value "$H" "Content-Security-Policy")" | grep -qi "frame-ancestors" ); then
    (( score += 1 ))
  fi

  # Cap at 10
  if (( score > 10 )); then score=10; fi
  echo "$score"
}

A02_FRONT="$(score_a02_for_headers "$FRONT_H")"
A02_API="$(score_a02_for_headers "$API_H")"
A02=$(( (A02_FRONT + A02_API) / 2 ))

# ---------------------------
# A01 - Broken Access Control
# Tests:
#  - GET tickets/admin without auth must be 401/403
#  - If USER_TOKEN provided: admin should be 403 (not 200)
#  - If ADMIN_TOKEN provided: admin should be 200 (or at least not 401/403)
# Score /10
# ---------------------------
A01=5  # baseline "unknown"
T_NOAUTH="$(curl_get "$API_URL$TICKETS_ENDPOINT")"
A_NOAUTH="$(curl_get "$API_URL$ADMIN_ENDPOINT")"
T_NOAUTH_CODE="$(echo "$T_NOAUTH" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"
A_NOAUTH_CODE="$(echo "$A_NOAUTH" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"

if [[ "$T_NOAUTH_CODE" =~ ^(401|403)$ ]] && [[ "$A_NOAUTH_CODE" =~ ^(401|403)$ ]]; then
  A01=8
else
  # If anything is 200 without auth -> bad
  if [[ "$T_NOAUTH_CODE" == "200" ]] || [[ "$A_NOAUTH_CODE" == "200" ]]; then
    A01=2
  fi
fi

if [[ -n "$USER_TOKEN" ]]; then
  A_USER="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  A_USER_CODE="$(echo "$A_USER" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"
  # Good if still forbidden
  if [[ "$A_USER_CODE" == "403" ]]; then
    A01=$(( A01 + 1 ))
  fi
fi

if [[ -n "$ADMIN_TOKEN" ]]; then
  A_ADMIN="$(curl_get "$API_URL$ADMIN_ENDPOINT" -H "Authorization: Bearer $ADMIN_TOKEN")"
  A_ADMIN_CODE="$(echo "$A_ADMIN" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"
  # Good if admin can access
  if [[ ! "$A_ADMIN_CODE" =~ ^(401|403)$ ]] && [[ "$A_ADMIN_CODE" != "0" ]]; then
    A01=$(( A01 + 1 ))
  fi
fi

if (( A01 > 10 )); then A01=10; fi

# ---------------------------
# A07 - Authentication Failures (SAFE)
# We can only infer basics safely:
#  - Protected endpoints return 401 without auth -> good
#  - If token provided, protected endpoint works -> better
# Score /10 (conservative)
# ---------------------------
A07=5
if [[ "$T_NOAUTH_CODE" == "401" ]] || [[ "$T_NOAUTH_CODE" == "403" ]]; then
  A07=6
fi
if [[ -n "$USER_TOKEN" ]]; then
  T_USER="$(curl_get "$API_URL$TICKETS_ENDPOINT" -H "Authorization: Bearer $USER_TOKEN")"
  T_USER_CODE="$(echo "$T_USER" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"
  if [[ ! "$T_USER_CODE" =~ ^(401|403)$ ]] && [[ "$T_USER_CODE" != "0" ]]; then
    A07=7
  fi
fi
if (( A07 > 10 )); then A07=10; fi

# ---------------------------
# A10 - Mishandling Exceptional Conditions
# Test:
#  - Request a non-existent endpoint and check:
#    * returns 404 or 401 (if auth required) but
#    * body should NOT contain stacktrace-like strings
# Score /10
# ---------------------------
A10=5
NF="$(curl_get "$API_URL$NOTFOUND_ENDPOINT")"
NF_CODE="$(echo "$NF" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"

# Remove headers for body scan
NF_BODY="$(echo "$NF" | awk 'BEGIN{inbody=0} /^[\r]*$/{inbody=1; next} {if(inbody) print}')"

if [[ "$NF_CODE" =~ ^(401|403|404)$ ]]; then
  A10=7
fi

# If no obvious stacktrace terms, add points
if ! contains_any "$NF_BODY" "Exception" "Stacktrace" "at org." "java.lang" "Caused by" "Whitelabel Error Page"; then
  A10=$(( A10 + 1 ))
fi
if (( A10 > 10 )); then A10=10; fi

# ---------------------------
# A05 - Injection (SAFE validation check)
# We send URL-encoded payload and only evaluate status:
# Good if 400/401/403; suspicious if 200 with large/odd output
# Score /10 (conservative)
# ---------------------------
A05=5
PAYLOAD="%27%20OR%201%3D1" # ' OR 1=1
SEARCH_URL="${SEARCH_ENDPOINT/__PAYLOAD__/$PAYLOAD}"
INJ="$(curl_get "$API_URL$SEARCH_URL")"
INJ_CODE="$(echo "$INJ" | awk 'BEGIN{c=0} /^HTTP\//{c=$2} END{print c}')"
INJ_BODY="$(echo "$INJ" | awk 'BEGIN{inbody=0} /^[\r]*$/{inbody=1; next} {if(inbody) print}')"

if [[ "$INJ_CODE" =~ ^(400|401|403)$ ]]; then
  A05=8
elif [[ "$INJ_CODE" == "200" ]]; then
  # If it returns 200 and contains obvious SQL/NoSQL error, that's bad
  if contains_any "$INJ_BODY" "SQL" "syntax" "Mongo" "E11000" "SQLException" "QueryFailed" "\$ne" "\$where"; then
    A05=2
  else
    # 200 but no error is inconclusive (could be harmless search)
    A05=5
  fi
fi
if (( A05 > 10 )); then A05=10; fi

# ---------------------------
# The remaining categories are not safely testable without repo/CI/credentials.
# We keep them conservative defaults (you can later "unlock" with evidence).
# ---------------------------
A02="$A02"
A03=5
A04=6
A06=5
A08=5
A09=3

TOTAL=$((A01 + A02 + A03 + A04 + A05 + A06 + A07 + A08 + A09 + A10))
PERCENT="$TOTAL" # /100 already

# ---------------------------
# Output
# ---------------------------
echo "=============================="
echo " OWASP SAFE AUTO-SCORE (Top 10:2025)"
echo " FRONT: $FRONT_URL  (HTTP $FRONT_CODE)"
echo " API  : $API_URL    (HTTP $API_CODE)"
echo "=============================="
echo
printf "A01 Broken Access Control........: %2d/10\n" "$A01"
printf "A02 Security Misconfiguration.....: %2d/10  (front %d, api %d)\n" "$A02" "$A02_FRONT" "$A02_API"
printf "A03 Supply Chain Failures.........: %2d/10  (default: needs CI/repo proof)\n" "$A03"
printf "A04 Cryptographic Failures........: %2d/10  (default: needs TLS/JWT proof)\n" "$A04"
printf "A05 Injection.....................: %2d/10  (status %s)\n" "$A05" "${INJ_CODE:-?}"
printf "A06 Insecure Design...............: %2d/10  (default: needs business-rule review)\n" "$A06"
printf "A07 Authentication Failures.......: %2d/10\n" "$A07"
printf "A08 Integrity Failures............: %2d/10  (default: needs pipeline proof)\n" "$A08"
printf "A09 Logging & Alerting............: %2d/10  (default: needs log evidence)\n" "$A09"
printf "A10 Exceptional Conditions.........: %2d/10  (status %s)\n" "$A10" "${NF_CODE:-?}"
echo
echo "------------------------------"
echo "TOTAL..........................: $TOTAL/100  =>  Protection: $(pct "$PERCENT")%"
echo "------------------------------"
echo
echo "Notes:"
echo "- Ce score est 'SAFE & PROUVÉ' uniquement via requêtes passives/GET."
echo "- Pour augmenter A03/A08/A09, il faut fournir des preuves CI/logs (pas des tests agressifs)."
echo "- Pour augmenter A01/A07, relance avec USER_TOKEN et ADMIN_TOKEN."
echo
echo "Examples:"
echo "  USER_TOKEN='...' ADMIN_TOKEN='...' ./owasp_score.sh"
