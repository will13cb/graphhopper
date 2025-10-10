/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * @author Johannes Pelzer
 * @author Peter Karich
 */
public class AngleCalcTest {
    private final AngleCalc AC = AngleCalc.ANGLE_CALC;

    @Test
    public void testOrientationExact() {
        assertEquals(90.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 0)), 0.01);
        assertEquals(45.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 1)), 0.01);
        assertEquals(0.0, Math.toDegrees(AC.calcOrientation(0, 0, 0, 1)), 0.01);
        assertEquals(-45.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, 1)), 0.01);
        assertEquals(-135.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, -1)), 0.01);

        // is symmetric?
        assertEquals(90 - 32.76, Math.toDegrees(AC.calcOrientation(49.942, 11.580, 49.944, 11.582)), 0.01);
        assertEquals(-90 - 32.76, Math.toDegrees(AC.calcOrientation(49.944, 11.582, 49.942, 11.580)), 0.01);
    }

    @Test
    public void testOrientationFast() {
        assertEquals(90.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 0, false)), 0.01);
        assertEquals(45.0, Math.toDegrees(AC.calcOrientation(0, 0, 1, 1, false)), 0.01);
        assertEquals(0.0, Math.toDegrees(AC.calcOrientation(0, 0, 0, 1, false)), 0.01);
        assertEquals(-45.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, 1, false)), 0.01);
        assertEquals(-135.0, Math.toDegrees(AC.calcOrientation(0, 0, -1, -1, false)), 0.01);

        // is symmetric?
        assertEquals(90 - 32.92, Math.toDegrees(AC.calcOrientation(49.942, 11.580, 49.944, 11.582, false)), 0.01);
        assertEquals(-90 - 32.92, Math.toDegrees(AC.calcOrientation(49.944, 11.582, 49.942, 11.580, false)), 0.01);
    }

    @Test
    public void testAlignOrientation() {
        assertEquals(90.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(90), Math.toRadians(90))), 0.001);
        assertEquals(225.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(90), Math.toRadians(-135))), 0.001);
        assertEquals(-45.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(-135), Math.toRadians(-45))), 0.001);
        assertEquals(-270.0, Math.toDegrees(AC.alignOrientation(Math.toRadians(-135), Math.toRadians(90))), 0.001);
    }

    @Test
    public void testCombined() {
        double orientation = AC.calcOrientation(52.414918, 13.244221, 52.415333, 13.243595);
        assertEquals(132.7, Math.toDegrees(AC.alignOrientation(0, orientation)), 1);

        orientation = AC.calcOrientation(52.414918, 13.244221, 52.414573, 13.243627);
        assertEquals(-136.38, Math.toDegrees(AC.alignOrientation(0, orientation)), 1);
    }

    @Test
    public void testCalcAzimuth() {
        assertEquals(45.0, AC.calcAzimuth(0, 0, 1, 1), 0.001);
        assertEquals(90.0, AC.calcAzimuth(0, 0, 0, 1), 0.001);
        assertEquals(180.0, AC.calcAzimuth(0, 0, -1, 0), 0.001);
        assertEquals(270.0, AC.calcAzimuth(0, 0, 0, -1), 0.001);
        assertEquals(0.0, AC.calcAzimuth(49.942, 11.580, 49.944, 11.580), 0.001);
    }

    @Test
    public void testAzimuthCompassPoint() {
        assertEquals("S", AC.azimuth2compassPoint(199));
    }

    @Test
    public void testAtan2() {
        // assertEquals(0, AngleCalc.atan2(0, 0), 1e-4);
        // assertEquals(0, AngleCalc.atan2(-0.002, 0), 1e-4);
        assertEquals(45, AngleCalc.atan2(5, 5) * 180 / Math.PI, 1e-2);
        assertEquals(-45, AngleCalc.atan2(-5, 5) * 180 / Math.PI, 1e-2);
        assertEquals(11.14, AngleCalc.atan2(1, 5) * 180 / Math.PI, 1);
        assertEquals(180, AngleCalc.atan2(0, -5) * 180 / Math.PI, 1e-2);
        assertEquals(-90, AngleCalc.atan2(-5, 0) * 180 / Math.PI, 1e-2);

        assertEquals(90, Math.atan2(1, 0) * 180 / Math.PI, 1e-2);
        assertEquals(90, AngleCalc.atan2(1, 0) * 180 / Math.PI, 1e-2);
    }

    @Test
    public void testConvertAzimuth2xAxisAngle() {
        assertEquals(Math.PI / 2, AC.convertAzimuth2xaxisAngle(0), 1E-6);
        assertEquals(Math.PI / 2, Math.abs(AC.convertAzimuth2xaxisAngle(360)), 1E-6);
        assertEquals(0, AC.convertAzimuth2xaxisAngle(90), 1E-6);
        assertEquals(-Math.PI / 2, AC.convertAzimuth2xaxisAngle(180), 1E-6);
        assertEquals(Math.PI, Math.abs(AC.convertAzimuth2xaxisAngle(270)), 1E-6);
        assertEquals(-3 * Math.PI / 4, AC.convertAzimuth2xaxisAngle(225), 1E-6);
        assertEquals(3 * Math.PI / 4, AC.convertAzimuth2xaxisAngle(315), 1E-6);
    }

    @Test
    public void checkAzimuthConsistency() {
        double azimuthDegree = AC.calcAzimuth(0, 0, 1, 1);
        double radianXY = AC.calcOrientation(0, 0, 1, 1);
        double radian2 = AC.convertAzimuth2xaxisAngle(azimuthDegree);
        assertEquals(radianXY, radian2, 1E-3);

        azimuthDegree = AC.calcAzimuth(0, 4, 1, 3);
        radianXY = AC.calcOrientation(0, 4, 1, 3);
        radian2 = AC.convertAzimuth2xaxisAngle(azimuthDegree);
        assertEquals(radianXY, radian2, 1E-3);
    }

    @Test
    public void testIsClockwise() {
        Coordinate a = new Coordinate(0.1, 1);
        Coordinate b = new Coordinate(0.2, 0.8);
        Coordinate c = new Coordinate(0.6, 0.3);
        assertTrue(isClockwise(a, b, c));
        assertTrue(isClockwise(b, c, a));
        assertTrue(isClockwise(c, a, b));
        assertFalse(isClockwise(c, b, a));
        assertFalse(isClockwise(a, c, b));
        assertFalse(isClockwise(b, a, c));
    }

    private boolean isClockwise(Coordinate a, Coordinate b, Coordinate c) {
        return AC.isClockwise(a.x, a.y, b.x, b.y, c.x, c.y);
    }

    private static class Coordinate {
        final double x;
        final double y;

        Coordinate(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }


    // NOUVEAUX TESTS AJOUTÉS
    
    @Test
    public void testConvertAzimuthBoundaryValidation() {
        /**
         * NOUVEAU TEST TÂCHE 2 - Test 1/7
         * Intention: Tester la validation des limites dans convertAzimuth2xaxisAngle
         * Données: Valeurs invalides (< 0 et > 360)
         * Oracle: Doit lever IllegalArgumentException
         * Justification: Couvre les branches de validation non testées
         */
        assertThrows(IllegalArgumentException.class, () -> 
            AC.convertAzimuth2xaxisAngle(-1.0));
        assertThrows(IllegalArgumentException.class, () -> 
            AC.convertAzimuth2xaxisAngle(361.0));
        assertThrows(IllegalArgumentException.class, () -> 
            AC.convertAzimuth2xaxisAngle(-0.1));
        assertThrows(IllegalArgumentException.class, () -> 
            AC.convertAzimuth2xaxisAngle(360.1));
    }

    @Test
    public void testAlignOrientationNegativeBase() {
        /**
         * NOUVEAU TEST TÂCHE 2 - Test 2/7
         * Intention: Tester alignOrientation avec baseOrientation négatif
         * Données: baseOrientation < 0 avec différentes orientations
         * Oracle: Vérifier les ajustements de 2π selon les conditions
         * Justification: Couvre les branches conditionnelles non testées
         */
        // Test avec baseOrientation négatif et orientation > +Math.PI + baseOrientation
        double baseOrientation = -Math.PI / 2; // -π/2
        double orientation = Math.PI; // π
        // Condition: orientation > +Math.PI + baseOrientation
        // π > π + (-π/2) = π > π/2 = true, donc soustrait 2π
        double result = AC.alignOrientation(baseOrientation, orientation);
        double expected = orientation - 2 * Math.PI; // π - 2π = -π
        assertEquals(expected, result, 1e-6);
        
        // Test avec baseOrientation négatif et orientation normale (pas de modification)
        baseOrientation = -Math.PI / 2;
        orientation = -Math.PI / 4; // -π/4
        // Condition: orientation > +Math.PI + baseOrientation
        // -π/4 > π + (-π/2) = -π/4 > π/2 = false, donc pas de modification
        result = AC.alignOrientation(baseOrientation, orientation);
        assertEquals(orientation, result, 1e-6); // Pas de modification
        
        // Test cas limite avec baseOrientation = -π
        baseOrientation = -Math.PI;
        orientation = Math.PI / 2; // π/2
        // Condition: orientation > +Math.PI + baseOrientation
        // π/2 > π + (-π) = π/2 > 0 = true, donc soustrait 2π
        result = AC.alignOrientation(baseOrientation, orientation);
        expected = orientation - 2 * Math.PI; // π/2 - 2π = -3π/2
        assertEquals(expected, result, 1e-6);
    }

    @Test
    public void testGeographicCoordinatesWithFaker() {
        /**
         * NOUVEAU TEST TÂCHE 2 - Test 3/7 - UTILISE JAVA-FAKER
         * Intention: Tester calcOrientation avec coordonnées géographiques réalistes
         * Données: Coordonnées générées aléatoirement dans les limites géographiques
         * Oracle: Vérifier cohérence entre méthodes exacte et rapide
         * Justification: Utilise faker pour générer des cas de test réalistes et tester
         *                la précision relative entre les deux algorithmes
         */
        com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
        
        for (int i = 0; i < 50; i++) {
            // Générer des coordonnées géographiques valides
            double lat1 = faker.number().randomDouble(6, -90, 90);
            double lon1 = faker.number().randomDouble(6, -180, 180);
            double lat2 = faker.number().randomDouble(6, -90, 90);
            double lon2 = faker.number().randomDouble(6, -180, 180);
            
            // Éviter les cas où les points sont identiques
            if (Math.abs(lat1 - lat2) < 1e-10 && Math.abs(lon1 - lon2) < 1e-10) {
                continue;
            }
            
            double exactOrientation = AC.calcOrientation(lat1, lon1, lat2, lon2, true);
            double fastOrientation = AC.calcOrientation(lat1, lon1, lat2, lon2, false);
            
            // Vérifier que les deux méthodes donnent des résultats cohérents
            assertTrue(Math.abs(exactOrientation) <= Math.PI, "Orientation exacte hors limites");
            assertTrue(Math.abs(fastOrientation) <= Math.PI, "Orientation rapide hors limites");
            
            // La différence doit rester dans une marge acceptable (algorithme rapide moins précis)
            double diff = Math.abs(exactOrientation - fastOrientation);
            assertTrue(diff < 0.1, String.format(
                "Différence trop importante entre méthodes exacte et rapide: %.6f " +
                "pour coordonnées (%.6f,%.6f) -> (%.6f,%.6f)", 
                diff, lat1, lon1, lat2, lon2));
        }
    }
}
