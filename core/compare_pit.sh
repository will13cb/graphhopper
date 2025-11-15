#!/usr/bin/env bash

set -euo pipefail

CURRENT_HTML="target/pit-reports/index.html"
PREVIOUS_HTML="prev-pit-reports/index.html"

if [ ! -f "$CURRENT_HTML" ]; then
  echo "Error: PIT HTML report missing ($CURRENT_HTML)"
  exit 1
fi

if [ ! -f "$PREVIOUS_HTML" ]; then
  echo "Info: No previous PIT baseline — first run. Skipping comparison."
  exit 0
fi

extract_score() {
    grep -oE '[0-9]+(\.[0-9]+)?%' "$1" | head -1 | tr -d '%'
}

CURRENT_SCORE=$(extract_score "$CURRENT_HTML")
PREVIOUS_SCORE=$(extract_score "$PREVIOUS_HTML")

echo "PIT comparison:"
echo "  Baseline (master): $PREVIOUS_SCORE%"
echo "  Current build:     $CURRENT_SCORE%"

if awk "BEGIN {exit !($CURRENT_SCORE < $PREVIOUS_SCORE)}"; then
  echo "Failure: mutation score regressed."
  exit 1
else
  echo "Success: no regression."
fi
