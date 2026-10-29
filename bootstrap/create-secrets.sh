#!/usr/bin/env bash
# Creates the Secrets the chart expects when secrets.create=false (values-minikube.yaml),
# so no password is stored in Git. Values come from env vars or are asked interactively.
#   GHCR_USER / GHCR_TOKEN (PAT with read:packages) - optional, only for a private image
set -euo pipefail

NS=${NS:-library}
RELEASE=${RELEASE:-library}

ask() { # ask VAR "prompt"
  local var=$1 prompt=$2
  if [ -z "${!var:-}" ]; then
    read -r -s -p "$prompt: " "$var"; echo
  fi
}

ask POSTGRES_PASSWORD "PostgreSQL password"
ask OIDC_CLIENT_SECRET "Keycloak client secret (library-app)"
ask KEYCLOAK_ADMIN_PASSWORD "Keycloak admin console password"
ask LIBRARY_USER_PASSWORD "Password of demo user 'user'"
ask LIBRARY_ADMIN_PASSWORD "Password of demo user 'admin'"

apply() { kubectl -n "$NS" apply -f -; }

kubectl create namespace "$NS" --dry-run=client -o yaml | kubectl apply -f -

kubectl -n "$NS" create secret generic "$RELEASE-postgres-secret" \
  --from-literal=POSTGRES_DB=library \
  --from-literal=POSTGRES_USER=library \
  --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  --dry-run=client -o yaml | apply

kubectl -n "$NS" create secret generic "$RELEASE-secret" \
  --from-literal=OIDC_CLIENT_SECRET="$OIDC_CLIENT_SECRET" \
  --dry-run=client -o yaml | apply

kubectl -n "$NS" create secret generic "$RELEASE-keycloak-secret" \
  --from-literal=KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  --from-literal=KC_BOOTSTRAP_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD" \
  --from-literal=LIBRARY_USER_PASSWORD="$LIBRARY_USER_PASSWORD" \
  --from-literal=LIBRARY_ADMIN_PASSWORD="$LIBRARY_ADMIN_PASSWORD" \
  --dry-run=client -o yaml | apply

if [ -n "${GHCR_TOKEN:-}" ]; then
  kubectl -n "$NS" create secret docker-registry ghcr-pull \
    --docker-server=ghcr.io \
    --docker-username="${GHCR_USER:?set GHCR_USER}" \
    --docker-password="$GHCR_TOKEN" \
    --dry-run=client -o yaml | apply
else
  echo "GHCR_TOKEN not set: skipping ghcr-pull (fine if the image is public)"
fi
