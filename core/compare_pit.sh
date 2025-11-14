#!/usr/bin/env bash
# compare_pit.sh — Compare le score PIT courant avec le score précédent (baseline)
set -euo pipefail

CURRENT_REPORT="core/target/pit-reports/mutations.xml"
PREVIOUS_REPORT=".baseline/pit/mutations.xml"

if [ ! -f "$CURRENT_REPORT" ]; then
  echo "Erreur: Rapport PIT courant introuvable: $CURRENT_REPORT"
  exit 1
fi

if [ ! -f "$PREVIOUS_REPORT" ]; then
  echo "Erreur: Baseline introuvable ($PREVIOUS_REPORT) — première exécution probable. Pas de comparaison."
  exit 0
fi

extract_score() {
  local file="$1"
  grep -oP '(?<=<mutationCoverage>)[0-9]+(\.[0-9]+)?(?=%</mutationCoverage>)' "$file" | head -1
}

CURRENT_SCORE=$(extract_score "$CURRENT_REPORT" || true)
PREVIOUS_SCORE=$(extract_score "$PREVIOUS_REPORT" || true)

if [ -z "${CURRENT_SCORE:-}" ] || [ -z "${PREVIOUS_SCORE:-}" ]; then
  echo "Erreur: Impossible d'extraire les scores PIT (XML manquant ou invalide)."
  exit 1
fi

echo "🧪 PIT"
echo "   Baseline (main) : ${PREVIOUS_SCORE}%"
echo "   Courant         : ${CURRENT_SCORE}%"

# Si le score courant < baseline -> échec
if (( $(echo "$CURRENT_SCORE < $PREVIOUS_SCORE" | bc -l) )); then
  delta=$(echo "$CURRENT_SCORE - $PREVIOUS_SCORE" | bc -l)
  echo "Erreur: Régression: score PIT ${delta}% plus bas que la baseline."
  exit 1
fi

echo "Pas de régression (score ≥ baseline)."
