# Rapport tâche 3

# Justification et Validation des Modifications du Workflow GitHub Actions

## I. Introduction
La modification du workflow **GitHub Actions** de GraphHopper a pour objectif d'introduire une validation de la qualité du code strict basé sur le score de **Mutation Testing (PIT)**. On garanti que le processus de **"build"** échoue si la couverture de mutation baisse après un **"commit"**, protégeant ainsi la qualité du code sur toutes les branches, y compris `master`.

---

## II. Choix de Conception et d'Implémentation
Les choix d'implémentation sont concentrés sur la **stabilité de la baseline** et la **précision de la comparaison** dans l'environnement CI de GitHub Actions.

### 1. Implémentation : Synthèse des Choix

| Composant Clé | Méthode d'Implémentation | Justification |
| :--- | :--- | :--- |
| **Mesure de Qualité** | Plugin PIT Mutation Testing (sur module `core` seulement par simplicité). | Fournit une mesure de qualité **plus fiable** que la simple couverture de lignes, ciblant l'efficacité des tests. |
| **Gestion de la Baseline** | Action `actions/cache@v4` pour `core/prev-pit-reports`. | Mécanisme le **plus fiable** dans GitHub Actions pour maintenir l'état persistant des rapports (`mutations.xml`) entre les exécutions. |
| **Logique de Comparaison** | Script shell (`core/compare_pit.sh`). | Isole la complexité de l'extraction des scores et de la comparaison numérique (**nombres flottants**) dans un script testable, assurant une détection précise de la moindre régression. |
| **Validation** | Étape **`Compare current vs baseline (fail on regression)`**. | S'applique de manière **universelle** (sans condition de branche). Tout "commit" doit réussir la comparaison. |
| **Mise à jour de la Baseline** | Condictionné à `if: github.ref == 'refs/heads/master'` et succès du build. | Garantit que **seule la branche principale**, si elle est "green", peut enregistrer une nouvelle baseline (pas de régression). |
| **Gestion des Échecs** | Job séparé `rickroll-on-failure` (`if: failure()`). | Isole l'effet secondaire de l'échec et utilise une action réutilisable (.github/actions/rickroll/action.yml)|

---

## III. Méthode de Validation
Nous avons testé notre implémentation en simulant **deux scénarios critiques** dans GitHub Actions pour prouver la fonctionnalité de la validation sur l'environnement réel.

### 1. Scénario de Succès : Établissement de la Baseline

| Étape | Méthode | Résultat Observé |
| :--- | :--- | :--- |
| **Action** | Fusion du nouveau workflow dans la branche `master` (sans modification du code testé). | Le "run" sur `master` a **réussi**. |
| **Preuve de Fonctionnement** | Étapes `Prepare baseline` et `Save PIT baseline cache` exécutées. | Confirmation de l'enregistrement de la nouvelle "**baseline**" de référence dans le cache. |

### 2. Scénario d'Échec : Détection de Régression (Validation Échouée)

| Étape | Méthode | Résultat Observé |
| :--- | :--- | :--- |
| **Action** | Création d'une branche de fonctionnalité (`test-regression`) avec une régression intentionnelle (méthode de test unitaire commentée) puis push | Le système exécute le code |
| **Le Contrôle de Qualité** | Étape **`Compare current vs baseline (fail on regression)`** exécutée. | **Échec** de cette étape. Les logs en clair affichent : `Baseline (master): xx% vs current build: yy% with Failure: mutation score regressed.` |
| **Effet Secondaire** | Le job **`Rickroll (on failure)`** déclenché. | Prouve que la validation de qualité fonctionne et que l'action réutilisable est invoquée sur échec. |

### Conclusion de la validation
L'implémentation est **correcte, sécurisée** et atteint les objectifs de la tâche en garantissant qu'aucune régression de la couverture de mutation n'est acceptée sur la branche principale.

---

## IV. Détails de l'implémentation

Voici la configuration du workflow GitHub Actions utilisé pour compiler, tester et analyser la qualité du code via **PIT Mutation Testing**. Il détaille chaque étape du fichier `build.yml` et son rôle.

---

## Permissions

```yaml
permissions:
    contents: read
    pull-requests: write
```

Ces permissions permettent au workflow de lire le code du dépôt et d’écrire des commentaires dans les pull requests (utile pour les notifications en cas d’échec).

## Build and Test Workflow

Le workflow est déclenché à chaque `push` et `pull_request`.

### Build job

Le job principal, `build`, exécute la compilation, les tests mutés, la comparaison avec la baseline et la sauvegarde automatique de la baseline sur `master`.

1. Setup et caches

- Checkout du dépôt
```yaml
- uses: actions/checkout@v4
```
Récupère les sources nécessaires à toute la suite du pipeline.

- Setup Java
```yaml
- uses: actions/setup-java@v4
    with:
        java-version: ${{ matrix.java-version }}
        distribution: temurin
```
Installe la version de Java utilisée pour le build et l’exécution de PIT.

- Cache Maven
```yaml
- name: Cache Maven artifacts
    uses: actions/cache@v4
```
Mise en cache du dépôt `~/.m2/repository` pour réduire le temps de build.

- Cache node et node_modules
Deux caches distincts : `web-bundle/node` et `web-bundle/node_modules`. Objectifs : accélérer la construction du bundle web et éviter la réinstallation des dépendances front-end.

2. Compilation du projet Maven

```yaml
- name: Build ${{ matrix.java-version }}
    run: mvn -B clean install -DskipTests
```
Compile tout le projet multi-modules afin que les dépendances du module `core` soient disponibles avant l'exécution de PIT.

3. Restauration de la baseline PIT

```yaml
- name: Restore PIT baseline cache
    uses: actions/cache@v4
    with:
        path: core/prev-pit-reports
        key: pit-baseline-master-${{ github.run_id }}-restore-sentinel
        restore-keys: |
            pit-baseline-master-
```
Cette étape restaure le dernier rapport PIT enregistré sur la branche `master`. Le cache sert de référence de comparaison pour toutes les branches. Remarques :
- Fonctionne même sur les branches non-master (fallback via `restore-keys`).
- Les fichiers restaurés sont placés dans `core/prev-pit-reports`.

4. Exécution des tests de mutation PIT

```yaml
- name: Run PIT Mutation Tests
    run: |
        mvn -B -f core/pom.xml org.pitest:pitest-maven:mutationCoverage \
            -DreportsDirectory=target/pit-reports \
            -DtimestampedReports=false
```
Exécute les tests mutés dans le module `core`, génère les rapports sous `core/target/pit-reports` et désactive les timestamps pour une comparaison stable. PIT produit notamment `index.html` et `mutations.xml`, utilisés par `compare_pit.sh`.

5. Upload du rapport PIT

```yaml
- uses: actions/upload-artifact@v4
    with:
        name: pitest-report-${{ github.run_id }}-${{ matrix.java-version }}
        path: core/target/pit-reports
```
Permet d’inspecter les rapports de mutation dans l’onglet Artifacts, facilitant le débogage en cas de baisse du score.

6. Préparation du script de comparaison

```yaml
- name: Make compare_pit.sh executable
    run: chmod +x core/compare_pit.sh
```
Rend le script exécutable dans le runner. Le script :
- lit la baseline,
- lit les nouveaux résultats,
- compare les scores,
- et échoue si le nouveau score est plus bas.

7. Comparaison du score de mutation (obligatoire pour toutes les branches)

```yaml
- name: Compare current vs baseline (fail on regression)
    run: |
        (cd core && ./compare_pit.sh)
```
Étape exécutée sans condition. Elle fait échouer le workflow si :
- aucune baseline n'a été trouvée ;
- ou le score actuel est plus faible que celui enregistré.

Ce mécanisme garantit l'interdiction de régression et la cohérence des scores sur toutes les branches.

8. Mise à jour de la baseline (sur master uniquement)

Préparation de la baseline
```yaml
- name: Prepare baseline (master only)
    if: github.ref == 'refs/heads/master'
    run: |
        rm -rf core/prev-pit-reports
        mkdir -p core/prev-pit-reports
        cp -f core/target/pit-reports/index.html core/prev-pit-reports/index.html
        cp -f core/target/pit-reports/mutations.xml core/prev-pit-reports/mutations.xml
```
Supprime l’ancienne baseline et copie les nouveaux rapports PIT. Ne s’exécute que si la comparaison précédente a réussi — garantie que la baseline n’est jamais écrasée par un score plus faible.

Sauvegarde de la baseline
```yaml
- name: Save PIT baseline cache (master only)
    if: github.ref == 'refs/heads/master'
    uses: actions/cache@v4
    with:
        path: core/prev-pit-reports
        key: pit-baseline-master-${{ github.run_id }}
```
Enregistre la baseline fraîche dans le cache pour les comparaisons futures.

9. Rickroll on Failure

```yaml
rickroll-on-failure:
    name: Rickroll (on failure)
    runs-on: ubuntu-latest
    needs: [build]
    if: failure()
```
Le job s’exécute uniquement si `build` échoue. Il insère un message humoristique dans la PR ou le résumé de job :

```yaml
- name: Rickroll the PR or Job Summary
    uses: ./.github/actions/rickroll
    with:
        pr-number: ${{ github.event.pull_request.number }}
        message: "Tests or gates failed. Get Rickrolled"
```

Objectif : ajouter un Easter egg amusant en cas d’échec CI.


---

## V. Tests Mockito

## 1. Choix des classes testées

### 1.1 BreadthFirstSearch (module core, package util)

Cette classe implémente un parcours en largeur (BFS) générique sur un graphe, en s’appuyant exclusivement sur les interfaces `EdgeExplorer` et `EdgeIterator` pour accéder à la structure du graphe.

Elle est un excellent choix pour des tests unitaires avec mocks, car :

- son comportement dépend entièrement des interactions avec ces interfaces ;
- elle ne contient pas de logique de stockage de graphe interne ;
- il est possible de simuler n’importe quelle topologie sans construire réellement un `BaseGraph`.

Tester BFS avec des mocks permet donc de contrôler exactement la structure du graphe et de vérifier que le moteur BFS utilise correctement l’API de navigation.

### 1.2 AvoidEdgesWeighting (module core, package routing.weighting)

Cette classe encapsule un `Weighting` existant et modifie le poids des arêtes afin de pénaliser celles qui appartiennent à un ensemble “à éviter”.

Elle est très appropriée car :

- son comportement est entièrement déterminé par un `Weighting` externe et un `EdgeIteratorState` → parfait pour du mocking ;
- la logique de pénalisation est simple mais critique pour les algorithmes de plus court chemin ;
- il est facile d’isoler le calcul du poids sans construire de graphe.

Elle offre donc un bon exemple de test unitaire ciblé sur une logique fonctionnelle précise.

---

## 2. Choix des classes simulées (mocks)

### Pour BreadthFirstSearch :

#### `EdgeExplorer`

Interface utilisée par BFS pour obtenir un itérateur (`EdgeIterator`) sur les arêtes sortantes d’un nœud.

→ en la mockant, on contrôle exactement quelles arêtes sont exposées à BFS.

#### `EdgeIterator`

Interface représentant un curseur sur les arêtes d’un nœud.

→ en la mockant, on peut définir précisément :

- dans quel ordre les voisins sont retournés,
- combien de voisins un nœud possède,
- quand l’itération se termine.

Ces deux mocks suffisent pour simuler complètement la navigation dans un graphe, sans structure réelle.

---

### Pour AvoidEdgesWeighting :

#### `Weighting`

Le poids “normal” d’une arête dépend de ce composant.

→ mocker le `Weighting` permet de vérifier que la pénalité est appliquée **uniquement quand nécessaire**.

#### `EdgeIteratorState`

Représente une arête.

→ mocker cette classe permet de définir l’ID d’arête retourné par `getEdge()`, ce qui conditionne l’application de la pénalité.

Ces deux mocks permettent d’isoler la logique de pénalisation sans dépendre d’un graphe réel.

---

## 3. Définition des mocks

### 3.1 BreadthFirstSearch

**Exemple (pour le nœud 0) :**

```java
when(iter0.next()).thenReturn(true, true, false);
when(iter0.getAdjNode()).thenReturn(1, 2);
````

**Justification :**

* `next()` retourne `true` deux fois → deux voisins.
* puis `false` → fin d’itération.
* `getAdjNode()` renvoie les voisins dans l’ordre exact souhaité pour vérifier l’ordre BFS.

Cela permet de simuler la structure :

```
0 → 1, 2
1 → 3
2 → 3
3 → ∅
```

---

### 3.2 AvoidEdgesWeighting

```java
when(superWeighting.calcEdgeWeight(edgeState, false)).thenReturn(baseWeight);
when(edgeState.getEdge()).thenReturn(42);
```

**Justification :**

* On choisit un poids simple (5 ou 10) pour faciliter les vérifications.
* On choisit un ID d’arête arbitraire (42) facilement identifiable.
* On place ou non cet ID dans l’ensemble `avoided`, selon le scénario.

---

## 4. Choix des valeurs simulées

### Simplicité et lisibilité

Les valeurs simulées sont volontairement petites, lisibles et isolées :

* **IDs d’arêtes :** `42`, `7`
  → faciles à distinguer.
* **Poids de base :** `5.0`, `10.0`
  → permettent de vérifier rapidement les résultats (multiplication par 3 ou 4).
* **Facteurs de pénalité :** `3.0`, `4.0`
  → choisis pour identifier clairement la modification du poids.

---

### Contrôle maximal pour BFS

Les graphes simulés sont volontairement petits et acycliques afin de :

* contrôler totalement l’ordre de visite attendu,
* éviter les ambiguïtés liées à des cycles ou à la structure interne de `BaseGraph`,
* démontrer que BFS dépend strictement de l’ordre fourni par `EdgeIterator`.

---

## Conclusion

Les deux classes choisies (`BreadthFirstSearch` et `AvoidEdgesWeighting`) répondent parfaitement aux exigences du devoir :

* deux classes du module *core* ;
* au moins un nouveau test par classe ;
* utilisation de plusieurs mocks pour chaque classe (`EdgeExplorer`, `EdgeIterator`, `Weighting`, `EdgeIteratorState`) ;
* contrôle complet du comportement grâce à des valeurs simulées pertinentes.