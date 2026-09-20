#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "ERROR: Maven no está instalado o no está disponible en PATH."
  echo "Instale Maven 3.9 o superior y vuelva a ejecutar este archivo."
  exit 1
fi

if [[ -z "${REQRES_API_KEY:-}" ]]; then
  echo "ERROR: Defina REQRES_API_KEY para ejecutar los escenarios API de Reqres."
  exit 1
fi

echo "Ejecutando todos los escenarios Web y API..."
PROPERTY_LOGS="$(awk -F= '/^[[:space:]]*execution\.logs[[:space:]]*=/ {gsub(/[[:space:]]/, "", $2); print tolower($2); exit}' serenity.properties)"
EFFECTIVE_LOGS="${EXECUTION_LOGS:-${PROPERTY_LOGS:-false}}"

if [[ "$EFFECTIVE_LOGS" == "true" ]]; then
  mvn clean verify "-Dcucumber.filter.tags=@automation"
else
  mvn -q clean verify "-Dcucumber.filter.tags=@automation"
fi
echo "Ejecución finalizada correctamente."
