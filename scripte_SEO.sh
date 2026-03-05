#!/usr/bin/env bash
set -euo pipefail

# BASE="https://loto-tracker.fr"
BASE="localhost:8082"

URLS=(
  "$BASE/tirage/2026-02-28"   # draw day
  "$BASE/tirage/2026-03-28"   # future draw day (pending)
  "$BASE/tirage/2026-03-04"   # non draw day (should be 404)
)

echo "=============================="
echo "SEO TEST SUITE - LOTO TRACKER"
echo "=============================="
echo "BASE = $BASE"
echo

for U in "${URLS[@]}"; do
  echo "============================================================"
  echo "URL: $U"
  echo "============================================================"

  # --- Headers + Status
  echo
  echo "[HEADERS]"
  curl -sS -D - -o /tmp/page.html "$U" | sed -n '1,30p'
  STATUS=$(curl -sS -o /dev/null -w "%{http_code}" "$U")
  echo "HTTP_STATUS=$STATUS"

  # --- Basic SEO tags
  echo
  echo "[TITLE]"
  grep -oP '(?<=<title>).*?(?=</title>)' /tmp/page.html | head -n 1 || echo "❌ title not found"

  echo
  echo "[META DESCRIPTION]"
  grep -oP '<meta\s+name="description"\s+content="\K[^"]+' /tmp/page.html | head -n 1 || echo "❌ meta description not found"

  echo
  echo "[CANONICAL]"
  grep -oP '<link\s+rel="canonical"\s+href="\K[^"]+' /tmp/page.html | head -n 1 || echo "❌ canonical not found"

  echo
  echo "[ROBOTS]"
  grep -oP '<meta\s+name="robots"\s+content="\K[^"]+' /tmp/page.html | head -n 1 || echo "❌ robots meta not found"

  echo
  echo "[H1]"
  grep -oP '<h1[^>]*>\K.*?(?=</h1>)' /tmp/page.html | head -n 1 || echo "❌ h1 not found"

  echo
  echo "[TIME TAGS]"
  grep -oP '<time[^>]*datetime="[^"]+"[^>]*>.*?</time>' /tmp/page.html | head -n 5 || echo "ℹ️ no <time> tag"

  # --- JSON-LD extraction + quick sanity checks
  echo
  echo "[JSON-LD: presence]"
  if grep -q 'type="application/ld+json"' /tmp/page.html; then
    echo "✅ JSON-LD script tag found"
  else
    echo "❌ JSON-LD script tag NOT found"
  fi

  echo
  echo "[JSON-LD: checks (Event/Article)]"
  # display a small excerpt around key fields
  awk 'BEGIN{RS="</script>"} /application\/ld\+json/{print $0}' /tmp/page.html \
    | sed -n '1,120p' \
    | grep -E '"@type"|eventStatus|startDate|datePublished|dateModified|mainEntityOfPage|headline|description' \
    | head -n 40 || echo "ℹ️ JSON-LD content not readable by grep"

  # --- CSP warning for JSON-LD
  echo
  echo "[CSP INLINE SCRIPT WARNING]"
  CSP=$(curl -sS -I "$U" | tr -d '\r' | awk -F': ' 'tolower($1)=="content-security-policy"{print $2}')
  if echo "$CSP" | grep -q "script-src"; then
    if echo "$CSP" | grep -q "script-src 'self'" && ! echo "$CSP" | grep -q "unsafe-inline" && ! echo "$CSP" | grep -q "nonce-"; then
      echo "⚠️ CSP seems to block inline scripts => JSON-LD may be ignored (script-src 'self' only)"
      echo "   CSP=$CSP"
    else
      echo "✅ CSP script-src seems OK for inline (unsafe-inline or nonce present)"
    fi
  else
    echo "ℹ️ No CSP header found"
  fi

  echo
  echo "[CONTENT-LANGUAGE]"
  LANG=$(curl -sS -I "$U" | tr -d '\r' | awk -F': ' 'tolower($1)=="content-language"{print $2}')
  echo "${LANG:-❌ Content-Language header not found}"

  echo
done

# echo
# echo "=============================="
# echo "SITEMAP TESTS"
# echo "=============================="
# for S in "$BASE/sitemap.xml" "$BASE/sitemap-tirages.xml" "$BASE/sitemap-pages.xml"; do
#   echo
#   echo "SITEMAP: $S"
#   CODE=$(curl -sS -o /dev/null -w "%{http_code}" "$S")
#   echo "HTTP_STATUS=$CODE"
#   curl -sS "$S" | head -n 25
# done

echo
echo "✅ Done."
