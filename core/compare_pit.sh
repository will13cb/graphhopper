permissions:
  contents: read
  pull-requests: write

name: Build and Test

on:
  push:
    branches:
      - '**'
  pull_request:
    branches:
      - '**'

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        java-version: [24]

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: temurin

      - name: Cache Maven artifacts
        uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-

      - name: Cache node
        uses: actions/cache@v4
        with:
          path: web-bundle/node
          key: ${{ runner.os }}-node-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os}}-node-

      - name: Cache node_modules
        uses: actions/cache@v4
        with:
          path: web-bundle/node_modules
          key: ${{ runner.os }}-node-${{ hashFiles('**/pom.xml', '**/package.json') }}
          restore-keys: |
            ${{ runner.os}}-node_modules-

      # Build complet pour que core ait ses dépendances pour PIT
      - name: Build ${{ matrix.java-version }}
        run: mvn -B clean install -DskipTests

      # 🔁 Restaure la baseline PIT (quel que soit la branche / le type d’évènement)
      - name: Restore PIT baseline cache
        uses: actions/cache@v4
        with:
          path: core/prev-pit-reports
          # clé unique par run, avec prefix commun pour retrouver la dernière baseline de master
          key: pit-baseline-master-${{ github.run_id }}-restore-sentinel
          restore-keys: |
            pit-baseline-master-

      # Lance PIT sur core (index.html + mutations.xml sous core/target/pit-reports)
      - name: Run PIT Mutation Tests
        run: |
          mvn -B -f core/pom.xml org.pitest:pitest-maven:mutationCoverage \
            -DreportsDirectory=target/pit-reports \
            -DtimestampedReports=false

      - name: Upload PIT Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: pitest-report-${{ github.run_id }}-${{ matrix.java-version }}
          path: core/target/pit-reports

      - name: Make compare_pit.sh executable
        run: chmod +x core/compare_pit.sh

      # ✅ COMPARAISON POUR TOUT COMMIT (toutes branches, push ou PR)
      - name: Compare current vs baseline (fail on regression)
        run: |
          (cd core && ./compare_pit.sh)

      # Sur master seulement : on met à jour la baseline pour les futurs commits
      - name: Prepare baseline (master only)
        if: github.ref == 'refs/heads/master'
        run: |
          rm -rf core/prev-pit-reports
          mkdir -p core/prev-pit-reports
          cp -f core/target/pit-reports/index.html core/prev-pit-reports/index.html
          cp -f core/target/pit-reports/mutations.xml core/prev-pit-reports/mutations.xml || true

      - name: Save PIT baseline cache (master only)
        if: github.ref == 'refs/heads/master'
        uses: actions/cache@v4
        with:
          path: core/prev-pit-reports
          key: pit-baseline-master-${{ github.run_id }}

  rickroll-on-failure:
    name: Rickroll (on failure)
    runs-on: ubuntu-latest
    needs: [build]
    if: failure()
    steps:
      - uses: actions/checkout@v4
      - name: Rickroll the PR or Job Summary
        uses: ./.github/actions/rickroll
        with:
          pr-number: ${{ github.event.pull_request.number }}
          message: "Tests or gates failed. Get Rickrolled"
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
