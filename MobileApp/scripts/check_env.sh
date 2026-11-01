#!/usr/bin/env bash
#
# Check the local Android SDK path and store-publication metadata.
#
# Usage:
#   ./scripts/check_env.sh [--phase getting-started|publishing|all] [--strict]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MOBILE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCAL_PROPS="$MOBILE_DIR/local.properties"
APP_CONFIG="$MOBILE_DIR/shared/src/commonMain/kotlin/io/github/vietdsk6/porta/root/AppConfiguration.kt"

PHASE="all"
STRICT=false

while [ $# -gt 0 ]; do
  case "$1" in
    --phase) PHASE="${2:-}"; shift 2 ;;
    --strict) STRICT=true; shift ;;
    -h|--help)
      sed -n '2,7s/^# \{0,1\}//p' "${BASH_SOURCE[0]}"
      exit 0
      ;;
    *) echo "Unknown argument: $1" >&2; exit 2 ;;
  esac
done

case "$PHASE" in
  getting-started|publishing|all) ;;
  *) echo "Invalid phase '$PHASE'. Use getting-started, publishing, or all." >&2; exit 2 ;;
esac

prop() {
  local file="$1" key="$2" line
  [ -f "$file" ] || return 0
  line="$(sed -nE "s/^[[:space:]]*${key}[[:space:]]*=[[:space:]]*(.*)$/\\1/p" "$file" | tail -1)"
  printf '%s' "$line"
}

const_string() {
  local name="$1"
  sed -nE "s/.*const val ${name}[[:space:]]*=[[:space:]]*\"([^\"]*)\".*/\\1/p" "$APP_CONFIG" | tail -1
}

WARN_COUNT=0

required() {
  local label="$1" value="$2" hint="$3"
  if [ -z "$value" ] || [ "$value" = "support@example.com" ]; then
    printf '  ⚠️  %-42s missing or placeholder\n' "$label"
    printf '      %s\n' "$hint"
    WARN_COUNT=$((WARN_COUNT + 1))
  else
    printf '  ✅ %-42s set\n' "$label"
  fi
}

if [ "$PHASE" = "getting-started" ] || [ "$PHASE" = "all" ]; then
  echo "[getting-started]"
  required "sdk.dir" "$(prop "$LOCAL_PROPS" sdk.dir)" "Set your Android SDK path in local.properties."
  echo
fi

if [ "$PHASE" = "publishing" ] || [ "$PHASE" = "all" ]; then
  echo "[publishing]"
  required "AppConfiguration.URL_PRIVACY_POLICY" "$(const_string URL_PRIVACY_POLICY)" "Set the live privacy-policy URL."
  required "AppConfiguration.URL_TERMS_CONDITIONS" "$(const_string URL_TERMS_CONDITIONS)" "Set the live terms URL."
  required "AppConfiguration.CONTACT_EMAIL" "$(const_string CONTACT_EMAIL)" "Replace support@example.com with the real support address."
  echo
fi

if [ "$WARN_COUNT" -gt 0 ]; then
  echo "⚠️  $WARN_COUNT required value(s) still need attention."
  if [ "$STRICT" = true ]; then exit 1; fi
else
  echo "✅ Environment values for '$PHASE' are ready."
fi
