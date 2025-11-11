#!/usr/bin/env bash
# compare_pit.sh — Compare les métriques PIT entre la build actuelle et précédente

set -e

CURRENT_REPORT="target/pit-reports/mutations.xml"
PREVIOUS_REPORT="prev-pit-reports/mutations.xml"

if [ ! -f "$CURRENT_REPORT" ]; then
  echo "❌ Aucun rapport PIT trouvé dans $CURRENT_REPORT"
  exit 1
fi

if [ ! -f "$PREVIOUS_REPORT" ]; then
  echo "⚠️ Aucun rapport PIT précédent trouvé — comparaison ignorée."
  exit 0
fi

# Fonction pour extraire le pourcentage de mutationCoverage depuis un fichier XML
extract_score() {
  local file="$1"
  grep -oP '(?<=<mutationCoverage>)[0-9]+(\.[0-9]+)?(?=%</mutationCoverage>)' "$file" | head -1
}

CURRENT_SCORE=$(extract_score "$CURRENT_REPORT")
PREVIOUS_SCORE=$(extract_score "$PREVIOUS_REPORT")

if [ -z "$CURRENT_SCORE" ] || [ -z "$PREVIOUS_SCORE" ]; then
  echo "❌ Impossible d'extraire les scores PIT."
  exit 1
fi

echo "🧪 PIT Comparison:"
echo "   Ancien score : ${PREVIOUS_SCORE}%"
echo "   Nouveau score : ${CURRENT_SCORE}%"

# Comparaison numérique
improvement=$(echo "$CURRENT_SCORE - $PREVIOUS_SCORE" | bc)

if (( $(echo "$improvement < 0" | bc -l) )); then
  echo "❌ Régression : le taux de mutation a baissé de ${improvement}%."
  exit 1
elif (( $(echo "$improvement == 0" | bc -l) )); then
  echo "ℹ️  Pas de changement du taux de mutation."
else
  echo "✅ Amélioration de ${improvement}% du taux de mutation."
fi
