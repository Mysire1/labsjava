#!/bin/sh
set -e
KC_HOME="${KC_HOME:-$HOME/keycloak-26.4.7}"
export JAVA_HOME="${KC_JAVA_HOME:-/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home}"
export KC_BOOTSTRAP_ADMIN_USERNAME=admin
export KC_BOOTSTRAP_ADMIN_PASSWORD=admin

if lsof -iTCP:8180 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port 8180 already in use (Keycloak already running?): $(lsof -tiTCP:8180 -sTCP:LISTEN)"
  exit 1
fi

mkdir -p "$KC_HOME/data/import"
cp "$(dirname "$0")/../keycloak/library-realm.json" "$KC_HOME/data/import/"
exec "$KC_HOME/bin/kc.sh" start-dev --http-port=8180 --import-realm
