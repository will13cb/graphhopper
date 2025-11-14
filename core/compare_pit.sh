#!/usr/bin/env bash
# compare_pit.sh — Compare les scores PIT entre la build actuelle et la baseline précédente

set -euo pipefail

CURRENT_REPORT="target/pit-reports/mutations.xml"
PREVIOUS_REPORT="prev-pit-reports/mutations.xml"

if [ ! -f "$CURRENT_REPORT" ]; then
  echo "Error: Rapport PIT actuel introuvable ($CURRENT_REPORT)"
  exit 1
fi

if [ ! -f "$PREVIOUS_REPORT" ]; then
  echo "Info: Aucun rapport PIT précédent trouvé — première exécution, comparaison ignorée."
  exit 0
fi

# Fonction pour extraire le pourcentage de mutationCoverage à partir du XML
extract_score() {
  local file="$1"
  grep -oP '(?<=<mutationCoverage>)[0-9]+(\.[0-9]+)?(?=%</mutationCoverage>)' "$file" | head -1
}

CURRENT_SCORE=$(extract_score "$CURRENT_REPORT" || true)
PREVIOUS_SCORE=$(extract_score "$PREVIOUS_REPORT" || true)

if [ -z "$CURRENT_SCORE" ] || [ -z "$PREVIOUS_SCORE" ]; then
  echo "Error: Impossible d'extraire les scores PIT depuis les rapports."
  exit 1
fi

echo "PIT comparison:"
echo "  Baseline (master): ${PREVIOUS_SCORE}%"
echo "  Current build:   ${CURRENT_SCORE}%"

# Comparaison numérique
if (( $(echo "$CURRENT_SCORE < $PREVIOUS_SCORE" | bc -l) )); then
  diff=$(echo "$CURRENT_SCORE - $PREVIOUS_SCORE" | bc -l)
  echo "Failure: le score PIT a diminué (${diff}%)."
  exit 1
fi

echo "Success: aucun recul du score PIT."
