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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.carrotsearch.hppc.IntArrayList;
import com.graphhopper.coll.GHBitSet;
import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.coll.GHTBitSet;
import com.graphhopper.storage.BaseGraph;

/**
 * @author Peter Karich
 */
public class BreadthFirstSearchTest {
    int counter;
    GHIntHashSet set = new GHIntHashSet();
    IntArrayList list = new IntArrayList();

    @BeforeEach
    public void setup() {
        counter = 0;
    }
/*
    // Pour vérifier que rickroll commence quand fail
    @Test
    void forceFailure() {
        assertEquals(1, 2);
    }
*/
    @Test
    public void testBFS() {
        BreadthFirstSearch bfs = new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "v " + v + " is already contained in set. iteration:" + counter);
                set.add(v);
                list.add(v);
                return super.goFurther(v);
            }
        };

        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(0, 1);
        g.edge(0, 2);
        g.edge(0, 3);
        g.edge(0, 5);
        g.edge(1, 6);
        g.edge(2, 7);
        g.edge(3, 8);
        g.edge(4, 8);
        g.edge(8, 10);
        g.edge(6, 9);
        g.edge(9, 10);
        g.edge(5, 10);

        bfs.start(g.createEdgeExplorer(), 0);

        assertTrue(counter > 0);
        assertEquals(g.getNodes(), counter);
        assertEquals("[0, 5, 3, 2, 1, 10, 8, 7, 6, 9, 4]", list.toString());
    }

    @Test
    public void testBFS2() {
        BreadthFirstSearch bfs = new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "v " + v + " is already contained in set. iteration:" + counter);
                set.add(v);
                list.add(v);
                return super.goFurther(v);
            }
        };

        BaseGraph g = new BaseGraph.Builder(1).create();
        g.edge(1, 2);
        g.edge(2, 3);
        g.edge(3, 4);
        g.edge(1, 5);
        g.edge(5, 6);
        g.edge(6, 4);

        bfs.start(g.createEdgeExplorer(), 1);

        assertTrue(counter > 0);
        assertEquals("[1, 5, 2, 6, 3, 4]", list.toString());
    }
    

    /**
     * Nouveau test : vérifie l'ordre de visite BFS en simulant complètement
     * le graphe avec des mocks Mockito (EdgeExplorer + EdgeIterator).
     *
     * Graphe simulé :
     *  0 -> 1, 2
     *  1 -> 3
     *  2 -> 3
     *  3 -> ∅
     *
     * Ordre BFS attendu : [0, 1, 2, 3]
     */
    @Test
    public void testBFSWithMockedExplorer() {
        // sous-classe pour enregistrer l'ordre des visites
        BreadthFirstSearch bfs = new BreadthFirstSearch() {
            @Override
            protected GHBitSet createBitSet() {
                return new GHTBitSet();
            }

            @Override
            public boolean goFurther(int v) {
                counter++;
                assertFalse(set.contains(v), "v " + v + " is already contained in set. iteration:" + counter);
                set.add(v);
                list.add(v);
                return true; // continuer toujours l'exploration
            }
        };

        // mocks des classes de navigation du graphe
        EdgeExplorer explorer = mock(EdgeExplorer.class);
        EdgeIterator iter0 = mock(EdgeIterator.class);
        EdgeIterator iter1 = mock(EdgeIterator.class);
        EdgeIterator iter2 = mock(EdgeIterator.class);
        EdgeIterator iter3 = mock(EdgeIterator.class);

        // 0 -> 1, 2
        when(iter0.next()).thenReturn(true, true, false);
        when(iter0.getAdjNode()).thenReturn(1, 2);

        // 1 -> 3
        when(iter1.next()).thenReturn(true, false);
        when(iter1.getAdjNode()).thenReturn(3);

        // 2 -> 3
        when(iter2.next()).thenReturn(true, false);
        when(iter2.getAdjNode()).thenReturn(3);

        // 3 -> ∅
        when(iter3.next()).thenReturn(false);

        // EdgeExplorer renvoie l'itérateur correspondant au "base node"
        when(explorer.setBaseNode(0)).thenReturn(iter0);
        when(explorer.setBaseNode(1)).thenReturn(iter1);
        when(explorer.setBaseNode(2)).thenReturn(iter2);
        when(explorer.setBaseNode(3)).thenReturn(iter3);

        bfs.start(explorer, 0);

        // on a bien visité au moins un noeud
        assertTrue(counter > 0);

        // ordre BFS attendu
        assertEquals("[0, 1, 2, 3]", list.toString());

        // on peut aussi vérifier quelques interactions Mockito si tu veux
        verify(explorer).setBaseNode(0);
        verify(explorer).setBaseNode(1);
        verify(explorer).setBaseNode(2);
        verify(explorer).setBaseNode(3);
    }
}