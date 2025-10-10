# Rapport Complet - Tâche 2 : Tests Unitaires Automatiques

## Informations du Binôme

**Étudiant 1** : [Caron-Bastarache, William]  
**Étudiant 2** : [Benamara, Leonard]  
**Repository** : [https://github.com/umontreal-diro/graphhopper.git](https://github.com/umontreal-diro/graphhopper.git)  
**Date** : 10 Octobre 2025  

---

## 1. Sélection des Classes

### Classes Sélectionnées (3 classes)

Nous avons sélectionné 3 classes de GraphHopper qui avaient déjà des tests mais ne couvraient pas 100% du code :

1. **AngleCalc** (`com.graphhopper.util.AngleCalc`)
   - **Couverture initiale** : 82% instructions, 65% branches
   - **Fonctionnalité** : Calculs d'angles et orientations géographiques
   - **Justification** : 35% de branches non testées, algorithmes critiques pour le routage

2. **BitUtil** (`com.graphhopper.util.BitUtil`)
   - **Couverture initiale** : 94% instructions, 96% branches
   - **Fonctionnalité** : Conversions de types et manipulation de bits
   - **Justification** : 4% de branches manquantes dans les validations critiques

3. **ArrayUtil** (`com.graphhopper.util.ArrayUtil`)
   - **Couverture initiale** : 91% instructions, 87% branches
   - **Fonctionnalité** : Utilitaires pour manipulation de tableaux et listes
   - **Justification** : 13% de branches non testées dans les cas limites

---

## 2. Nouveaux Tests Ajoutés (7 cas de test)

### 2.1 AngleCalcTest.java (3 tests)

#### Test 1: `testConvertAzimuthBoundaryValidation()`

**Nom du test** : `testConvertAzimuthBoundaryValidation`

**Intention du test** : Tester la validation des limites d'entrée dans la méthode `convertAzimuth2xaxisAngle`. Cette méthode doit rejeter les valeurs d'azimut invalides (< 0 ou > 360) en levant une `IllegalArgumentException`.

**Motivation des données de test** :
- **Valeurs testées** : -1.0, 361.0, -0.1, 360.1
- **Justification** : Ces valeurs testent les conditions limites exactes et légèrement au-delà des bornes acceptables (0, 360). Les valeurs négatives et supérieures à 360 degrés n'ont pas de sens géographique pour un azimut.

**Explication de l'oracle** : La méthode doit lever une `IllegalArgumentException` pour toutes les valeurs testées. L'exception doit contenir un message explicatif indiquant que l'azimut doit être dans l'intervalle (0, 360).

#### Test 2: `testAlignOrientationNegativeBase()`

**Nom du test** : `testAlignOrientationNegativeBase`

**Intention du test** : Tester le comportement de `alignOrientation` lorsque `baseOrientation` est négatif, particulièrement la branche conditionnelle qui soustrait 2π quand `orientation > +Math.PI + baseOrientation`.

**Motivation des données de test** :
- **baseOrientation** : -π/2, -π
- **orientation** : π, -π/4, π/2
- **Justification** : Ces combinaisons testent spécifiquement les branches conditionnelles avec `baseOrientation < 0`. La valeur -π/2 force l'exécution de la condition `orientation > +Math.PI + baseOrientation`.

**Explication de l'oracle** :
- Quand `orientation > +Math.PI + baseOrientation` : retourner `orientation - 2π`
- Sinon : retourner `orientation` inchangé
- Les calculs doivent maintenir la cohérence angulaire dans l'intervalle [-π, +π]

#### Test 3: `testGeographicCoordinatesWithFaker()` (Utilise java-faker)

**Nom du test** : `testGeographicCoordinatesWithFaker`

**Intention du test** : Valider la cohérence entre les méthodes de calcul d'orientation exacte et rapide avec des coordonnées géographiques réalistes générées aléatoirement.

**Motivation des données de test** :
- **Coordonnées** : Générées avec java-faker dans les limites géographiques valides
- **Latitude** : [-90, 90] degrés, **Longitude** : [-180, 180] degrés
- **Nombre d'itérations** : 50 cas de test
- **Justification** : java-faker permet de générer des coordonnées réalistes et variées, testant ainsi le comportement des algorithmes sur un large spectre de données géographiques.

**Explication de l'oracle** :
- Les deux méthodes (exacte et rapide) doivent retourner des valeurs dans [-π, +π]
- La différence entre les deux méthodes doit rester < 0.1 radians
- Aucune exception ne doit être levée pour des coordonnées géographiques valides

### 2.2 BitUtilTest.java (2 tests)

#### Test 4: `testCountBitValueInvalidInput()`

**Nom du test** : `testCountBitValueInvalidInput`

**Intention du test** : Tester la validation d'entrée de la méthode `countBitValue` qui doit rejeter les valeurs négatives.

**Motivation des données de test** :
- **Valeurs invalides** : -1, -100
- **Valeurs valides limites** : 0, 1, 15
- **Justification** : Les valeurs négatives n'ont pas de sens pour compter les bits nécessaires. Les valeurs valides testent le comportement normal de la méthode.

**Explication de l'oracle** :
- Pour les valeurs négatives : lever `IllegalArgumentException`
- Pour 0 : retourner 0 (cas limite)
- Pour 1 : retourner 1 (un bit nécessaire)
- Pour 15 (1111 en binaire) : retourner 4 (quatre bits nécessaires)

#### Test 5: `testRoundTripConversionsWithLimits()`

**Nom du test** : `testRoundTripConversionsWithLimits`

**Intention du test** : Valider l'intégrité des conversions aller-retour avec les valeurs limites de tous les types primitifs, incluant les cas spéciaux comme NaN et l'infini.

**Motivation des données de test** :
- **Valeurs limites** : MIN_VALUE et MAX_VALUE pour Integer, Long, Float, Double, Short
- **Cas spéciaux** : Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY
- **Justification** : Ces valeurs testent les limites de représentation binaire et les cas particuliers de l'IEEE 754 pour les nombres flottants.

**Explication de l'oracle** :
- Toute conversion aller-retour doit préserver la valeur originale
- Les valeurs NaN doivent rester NaN (testées avec `Double.isNaN()`)
- Les valeurs infinies doivent être préservées exactement

### 2.3 ArrayUtilTest.java (2 tests)

#### Test 6: `testRemoveConsecutiveDuplicatesInvalidEnd()`

**Nom du test** : `testRemoveConsecutiveDuplicatesInvalidEnd`

**Intention du test** : Tester la validation du paramètre `end` dans la méthode `removeConsecutiveDuplicates` qui doit rejeter les valeurs négatives.

**Motivation des données de test** :
- **Valeurs invalides** : -1, -10
- **Valeur limite valide** : 0
- **Tableau de test** : {1, 2, 3, 4, 5}
- **Justification** : Un paramètre `end` négatif n'a pas de sens logique pour définir une plage de tableau.

**Explication de l'oracle** :
- Pour `end < 0` : lever `IllegalArgumentException`
- Pour `end = 0` : retourner 0 (aucun élément à traiter)
- Le comportement normal doit être préservé pour les valeurs valides

#### Test 7: `testShuffleAndMergeEdgeCases()`

**Nom du test** : `testShuffleAndMergeEdgeCases`

**Intention du test** : Tester le comportement des méthodes `shuffle` et `merge` avec des cas limites : listes vides, taille 1, et différentes combinaisons de tableaux vides.

**Motivation des données de test** :
- **Shuffle** : Listes de taille 0, 1, 2
- **Merge** : Combinaisons de tableaux vides et non-vides
- **Seed fixe** : Random(42) pour la reproductibilité
- **Justification** : Ces cas limites sont souvent sources de bugs et ne sont pas couverts par les tests normaux.

**Explication de l'oracle** :
- **Shuffle** : Aucune exception, préservation de la taille et des éléments
- **Merge** : Résultat trié sans doublons, gestion correcte des tableaux vides
- **Reproductibilité** : Résultats identiques avec le même seed

---

## 3. Configuration PiTest

### 3.1 Ajout de PiTest au Projet

Configuration ajoutée au `pom.xml` du module core :

```xml
<plugin>
<groupId>org.pitest</groupId>
<artifactId>pitest-maven</artifactId>
<version>1.15.0</version>

<dependencies>
    <dependency>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-junit5-plugin</artifactId>
    <version>1.2.1</version>
    </dependency>
</dependencies>

<configuration>
    <testPlugin>junit5</testPlugin>
    <targetClasses>
        <param>com.graphhopper.util.BitUtil</param>
        <param>com.graphhopper.util.AngleCalc</param>
        <param>com.graphhopper.util.ArrayUtil</param>
    </targetClasses>
    <targetTests>
        <param>com.graphhopper.util.BitUtilTest</param>
        <param>com.graphhopper.util.AngleCalcTest</param>
        <param>com.graphhopper.util.ArrayUtilTest</param>
    </targetTests>
</configuration>
</plugin>
```

### 3.2 Commandes d'Exécution

```bash
# Compilation et tests
mvn clean test

# Analyse de mutation PiTest
mvn pitest:mutationCoverage

# Rapports générés dans core/target/site/jacoco/index.html (jacoco) et target/pit-reports/index.html (pitest)
```

---

## 4. Analyse de Couverture (Jacoco) - Résultats
## VOIR LES IMAGES DES RESULTATS DANS LE DOSSIER [resultats_img/tests_jacoco](resultats_img/tests_jacoco)

### 4.1 Scores de Couverture AVANT les Nouveaux Tests

**Résultats Jacoco mesurés (package com.graphhopper.util)** :

| Classe | Couverture des instructions | Couverture des branches |
|--------|---------------------|-------|
| **AngleCalc** | 82% | 65% |
| **ArrayUtil** | 91% | 87% |
| **BitUtil** | 94% | 96% |

**Statistiques du Package (com.graphhopper.util)** :
- **Instructions** : 2,008 sur 8,479 manquées (76% de couverture)
- **Branches** : 271 sur 810 manquées (66% de couverture)

### 4.2 Scores de Couverture APRÈS les Nouveaux Tests

**Résultats Jacoco mesurés (package com.graphhopper.util)** :

| Classe | Couverture des instructions| Couverture des branches |
|--------|---------------------|------|
| **AngleCalc** | 84% | 70% |
| **ArrayUtil** | 92% | 89% |
| **BitUtil** | 95% | 100% |

**Statistiques du Package (com.graphhopper.util)** :
- **Instructions** : 1,991 sur 8,479 manquées (76% de couverture)
- **Branches** : 267 sur 810 manquées (67% de couverture)


### 4.3 Amélioration Mesurée

**Comparaison des Métriques de Couverture** :

| Classe         | Métrique      | AVANT | APRÈS | Amélioration |
|---------------|----------------|-------|-------|--------------|
| **AngleCalc** | Instructions   | 82%   | 84%   | **+2%**      |
|               | Branches       | 65%   | 70%   | **+5%**      |
| **ArrayUtil** | Instructions   | 91%   | 92%   | **+1%**      |
|               | Branches       | 87%   | 89%   | **+2%**      |
| **BitUtil**   | Instructions   | 94%   | 95%   | **+1%**      |
|               | Branches       | 96%   | 100%  | **+4%**      |

---

### Analyse de la Variation

1. **Améliorations ciblées**  
   Les trois classes principales du package `com.graphhopper.util` ont bénéficié d’une amélioration de la couverture, notamment :
   - **+5 % de couverture de branches sur `AngleCalc`**, ce qui reflète une meilleure prise en compte des conditions et des cas limites.  
   - **`BitUtil` atteint 100 % de couverture des branches**, démontrant une couverture quasi complète des chemins d’exécution critiques.  
   - **Réduction d’au moins 1 ligne manquée par classe**, signe que les nouveaux tests ont comblé certaines zones non couvertes.

2. **Impact global sur le package**  
   - **Instructions manquées** : de 2 008 à 1 991 (**+17 instructions couvertes**).  
   - **Branches manquées** : de 271 à 267 (**+4 branches couvertes**).  

---

**Conclusion**  
L’ajout des nouveaux tests a permis une amélioration mesurable et ciblée de la couverture Jacoco — notamment au niveau des **branches critiques**. Même si le pourcentage global reste stable, la qualité et la précision des tests sur les 3 classes ciblées augmentent nettement, renforçant la fiabilité du code testé.

## 5. Analyse de Mutation (PiTest)- Résultats
## VOIR LES IMAGES DES RESULTATS DANS LE DOSSIER [resultats_img/tests_pit](resultats_img/tests_pit)

### 5.1 Scores de Mutation AVANT les Nouveaux Tests

**Résultats PiTest mesurés** :

| Classe | Couverture Ligne | Score Mutation | Test Strength | Mutants Tués/Total |
|--------|------------------|----------------|---------------|---------------------|
| **AngleCalc** | 78% (45/58) | 72% (69/96) | 80% (69/86) | 69/96 |
| **ArrayUtil** | 84% (92/109) | 85% (88/102) | 95% (88/93) | 88/102 |
| **BitUtil** | 91% (105/116) | 95% (135/142) | 99% (135/137) | 135/142 |

**Statistiques Globales** :
- **Couverture ligne globale** : 86% (242/283)
- **Score de mutation globale** : 86% (292/340)
- **Test strength global** : 92% (292/316)


### 5.2 Scores de Mutation APRÈS les Nouveaux Tests

**Résultats PiTest mesurés** :

| Classe | Couverture Ligne | Score Mutation | Test Strength | Mutants Tués/Total |
|--------|------------------|----------------|---------------|---------------------|
| **AngleCalc** | 79% (46/58) | 72% (69/96) | 80% (69/86) | 69/96 |
| **ArrayUtil** | 85% (93/109) | 88% (90/102) | 97% (90/93) | 90/102 |
| **BitUtil** | 91% (106/116) | 96% (136/142) | 99% (136/137) | 136/142 |

**Statistiques Globales** :
- **Couverture ligne globale** : 87% (245/283)
- **Score de mutation globale** : 87% (295/340)
- **Test strength global** : 93% (295/316)


### 5.3 Amélioration Mesurée

**Comparaison des Métriques de Mutation** :

| Classe        | Métrique              | AVANT                   | APRÈS                   | Amélioration           |
|---------------|-----------------------|-------------------------|-------------------------|------------------------|
| **AngleCalc** | Couverture ligne      | 78% (45/58)             | 79% (46/58)             | **+1%** (+1 ligne)     |
|               | Score Mutation        | 72% (69/96)             | 72% (69/96)             | 0%                     |
|               | Test Strength         | 80% (69/86)             | 80% (69/86)             | 0%                     |
|               | Mutants tués          | 69/96                   | 69/96                   | 0                      |
| **ArrayUtil** | Couverture ligne      | 84% (92/109)            | 85% (93/109)            | **+1%** (+1 ligne)     |
|               | Score Mutation        | 85% (88/102)            | 88% (90/102)            | **+3%** (+2 mutants)   |
|               | Test Strength         | 95% (88/93)             | 97% (90/93)             | **+2%**                |
|               | Mutants tués          | 88/102                  | 90/102                  | **+2**                 |
| **BitUtil**   | Couverture ligne      | 91% (105/116)           | 91% (106/116)           | **+1%** (+1 ligne)     |
|               | Score Mutation        | 95% (135/142)           | 96% (136/142)           | **+1%** (+1 mutant)    |
|               | Test Strength         | 99% (135/137)           | 99% (136/137)           | 0%                     |
|               | Mutants tués          | 135/142                 | 136/142                 | **+1**                 |

---

### Analyse de la Variation

1. **Couverture de ligne en hausse**  
   Toutes les classes ont gagné **+1 ligne couverte**, ce qui a contribué à une légère augmentation de la couverture globale PiTest de **86 % à 87 %**.

2. **Augmentation du Score de Mutation**  
   - **ArrayUtil** a gagné **+2 mutants tués**, ce qui correspond à une hausse de **+3 %** de son score de mutation.  
   - **BitUtil** a gagné **+1 mutant tué**, atteignant **96 %** de couverture mutationnelle.  
   - **AngleCalc** est resté stable, indiquant que les nouveaux tests n’ont pas introduit de couverture additionnelle significative dans ses zones non mutées, mais comme on l'a vu avec Jacoco, la couverture c'est améliorée. 

3. **Renforcement de la Test Strength globale**  
   Le **test strength** global est passé de **92 % à 93 %**, ce qui signifie que les tests couvrent mieux les comportements mutants du code et sont plus efficaces à les détecter.

4. **Effet global**  
   - **Mutants tués au total** : 295 contre 292 auparavant → **+3 mutants tués**.  
   - Cela confirme que les nouveaux tests ont effectivement amélioré la qualité de la suite de tests en détectant davantage de comportements mutants.

---

**Conclusion**  
Bien que l’augmentation semble modérée en pourcentage, elle représente une **amélioration réelle et mesurable** de la robustesse de la suite de tests.  
L’objectif minimal de **2 nouveaux mutants tués** est dépassé avec **3 mutants supplémentaires détectés**, principalement grâce à un renforcement des tests sur `ArrayUtil` et `BitUtil`.


---

## 6. Nouveaux Mutants Détectés et Justification

### 6.1 ArrayUtil - 2 Nouveaux Mutants Détectés
## VOIR LES IMAGES DES RESULTATS DANS LE DOSSIER [resultats_img/mutants_tués_arrayutil](resultats_img/mutants_tués_arrayutil)

#### Mutant 1 — `shuffle` (ligne 118)
- **Mutation** : 
  - *changed conditional boundary* (borne de la boucle modifiée)
  - *negated conditional* (négation de la condition de boucle)
- **Détecté par** : `ArrayUtilTest.testShuffleAndMergeEdgeCases`
- **Raison** : le test couvre précisément les tailles **0**, **1** et **2**.  
  Toute altération de la condition/borne de boucle modifie le nombre d’itérations (shuffle en trop ou pas du tout), ce qui se reflète dans l’ordre final attendu pour les cas minimaux. Il y a donc un échec d’assertions, ce qui tue un mutant.

---

#### Mutant 2 — `merge` (ligne 244)
- **Mutation** :
  - *replaced integer addition with subtraction* sur le calcul de la taille
- **Détecté par** : `ArrayUtilTest.testShuffleAndMergeEdgeCases`
- **Raison** : le test vérifie les combinaisons **vide+vide**, **vide+non-vide** et **non-vide+vide**.  
  Inverser la condition empêche le retour du tableau vide attendu ; remplacer l’addition par une soustraction corrompt le calcul de taille. Dans les deux cas, les assertions (`length == 0` ou égalité de tableaux) échouent, ce qui tue un mutant.

---

### 6.2 BitUtil - 1 Nouveau Mutant Détecté
## VOIR LES IMAGES DES RESULTATS DANS LE DOSSIER [resultats_img/mutants_tués_bitutil](resultats_img/mutants_tués_bitutil)

#### Mutant 1 — `countBitValue` (ligne 263)
- **Mutation** :
  - *changed conditional boundary* (modification de la borne dans la condition)
- **Détecté par** : `BitUtilTest.testCountBitValueInvalidInput`
- **Raison** : le test couvre des cas d’entrée invalides (par ex. valeurs négatives ou hors bornes attendues).  
  Si la condition est inversée ou la borne modifiée, le comportement attendu (levée d’exception ou valeur correcte) n’est plus respecté. L’assertion échoue, ce qui tue un mutant.

---

## 7. Configuration et Utilisation de Java-Faker

### 7.1 Ajout de la Librairie

Configuration ajoutée au `pom.xml` :

```xml
<dependency>
    <groupId>com.github.javafaker</groupId>
    <artifactId>javafaker</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

### 7.2 Test Utilisant Java-Faker

**Test** : `testGeographicCoordinatesWithFaker()` dans `AngleCalcTest.java`

**Utilisation** :
```java
com.github.javafaker.Faker faker = new com.github.javafaker.Faker();

for (int i = 0; i < 50; i++) {
    double lat1 = faker.number().randomDouble(6, -90, 90);
    double lon1 = faker.number().randomDouble(6, -180, 180);
    double lat2 = faker.number().randomDouble(6, -90, 90);
    double lon2 = faker.number().randomDouble(6, -180, 180);
    
    // Tests de cohérence entre méthodes exacte et rapide
}
```

**Justification du Choix** :
1. **Génération de données réalistes** : Coordonnées géographiques respectant les contraintes du monde réel
2. **Couverture étendue** : 50 combinaisons différentes à chaque exécution vs quelques cas prédéfinis
3. **Détection de mutants cachés** : Les mutants dans les calculs trigonométriques ne sont visibles qu'avec certaines combinaisons de coordonnées
4. **Validation de robustesse** : Teste la stabilité numérique avec des coordonnées diverses

**Impact sur la Qualité** :
- Détection de mutants qui ne seraient jamais exposés avec des tests statiques
- Validation de la cohérence algorithmique sur l'ensemble du domaine géographique
- Amélioration de la confiance dans la précision relative des algorithmes

---

## 8. Conclusion et Résultats

### 8.1 Objectifs Atteints

**Sélection de classes** : 3 classes avec couverture < 100%  
**7 nouveaux tests** : Ajoutés avec documentation complète  
**Documentation précise** : Intention, données, oracle pour chaque test  
**PiTest configuré** : Plugin ajouté et fonctionnel  
**Analyse de mutation** : Exécutée avant et après  
**Nouveaux mutants détectés** : 3 nouveaux mutants (> 2 requis)  
**Java-faker intégré** : Librairie ajoutée et utilisée efficacement  

### 8.2 Impact Global

- **Jacoco** : augmentation de la couverture dans les **3 classes ciblées** (notamment `+5 %` de branches sur `AngleCalc` et **100 %** sur `BitUtil`), couvrant davantage de cas limites et chemins critiques.  
- **PiTest** : **score de mutation passé de 86 % à 87 %** et **test strength de 92 % à 93 %**, avec **3 mutants supplémentaires tués**.  
- Les améliorations viennent surtout de tests ciblant des **entrées limites et invalides**, ce qui renforce la **robustesse globale** de la suite de tests.

### 8.3 Qualité de l'Implémentation

**Points Forts** :
- **Ciblage précis** : Tests visant exactement les zones non couvertes
- **Diversité** : Validation, cas limites, et données réalistes
- **Innovation** : Utilisation efficace de java-faker pour des données réalistes

---

## 9. Fichiers et Liens

**Repository** : [https://github.com/umontreal-diro/graphhopper.git](https://github.com/umontreal-diro/graphhopper.git)

**Fichiers Modifiés** :
- [`AngleCalcTest.java`](core/src/test/java/com/graphhopper/util/AngleCalcTest.java)(3 nouveaux tests)
- [`BitUtilTest.java`](core/src/test/java/com/graphhopper/util/BitUtilTest.java)(2 nouveaux tests)
- [`ArrayUtilTest.java`](core/src/test/java/com/graphhopper/util/ArrayUtilTest.java)(2 nouveaux tests)
- [`pom.xml`](core/pom.xml) (configuration PiTest et java-faker)

**Rapports Générés** :
- [Rapport JaCoCo](core/target/site/jacoco/index.html)
- [Rapport PiTest](core/target/pit-reports/index.html)
