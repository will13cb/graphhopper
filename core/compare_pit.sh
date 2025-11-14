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

# Calcule le score PIT à partir du XML:
# score = (# detected="true") / (total # mutations) * 100
extract_score() {
  local file="$1"

  local total detected
  total=$(grep -c '<mutation ' "$file" || true)
  if [ "$total" -eq 0 ]; then
    echo "0"
    return
  fi

  detected=$(grep -c 'detected="true"' "$file" || true)

  # imprime un pourcentage avec 2 décimales
  awk -v d="$detected" -v t="$total" 'BEGIN { printf "%.2f", (d * 100.0) / t }'
}

CURRENT_SCORE=$(extract_score "$CURRENT_REPORT")
PREVIOUS_SCORE=$(extract_score "$PREVIOUS_REPORT")

echo "PIT comparison:"
echo "  Baseline (master): ${PREVIOUS_SCORE}%"
echo "  Current build:     ${CURRENT_SCORE}%"

# Comparaison numérique via awk
cmp=$(awk -v c="$CURRENT_SCORE" -v p="$PREVIOUS_SCORE" 'BEGIN { if (c < p) print "lt"; else print "ge" }')

if [ "$cmp" = "lt" ]; then
  diff=$(awk -v c="$CURRENT_SCORE" -v p="$PREVIOUS_SCORE" 'BEGIN { printf "%.2f", c - p }')
  echo "Failure: le score PIT a diminué (${diff}%)."
  exit 1
fi

echo "Success: aucun recul du score PIT."
