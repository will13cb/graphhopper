package com.graphhopper.routing.weighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.util.EdgeIteratorState;

/**
 * Unit tests for {@link AvoidEdgesWeighting} that verify its interaction with
 * mocked dependencies. The tests uses Mockito to ensure that the penalty logic works as expected.
 */
public class AvoidEdgesWeightingTest {

    /**
     * Verifies that {@link AvoidEdgesWeighting#calcEdgeWeight(EdgeIteratorState, boolean)}
     * multiplies the base weight when the edge ID is contained in the avoided set.
     */
    @Test
    public void testCalcEdgeWeightWithPenalty() {
        // create mocks for the underlying weighting and the edge state
        Weighting superWeighting = mock(Weighting.class);
        EdgeIteratorState edgeState = mock(EdgeIteratorState.class);

        // configure the mocks: the underlying weighting returns a base weight,
        // and the edge state returns a specific edge ID
        double baseWeight = 10.0;
        when(superWeighting.calcEdgeWeight(edgeState, false)).thenReturn(baseWeight);
        when(edgeState.getEdge()).thenReturn(42);

        // instantiate the weighting under test and configure its penalty
        AvoidEdgesWeighting weighting = new AvoidEdgesWeighting(superWeighting);
        weighting.setEdgePenaltyFactor(3.0);

        // set up the avoided edges to include the edge ID returned by the mock
        GHIntHashSet avoided = new GHIntHashSet();
        avoided.add(42);
        weighting.setAvoidedEdges(avoided);

        // call the method under test
        double result = weighting.calcEdgeWeight(edgeState, false);

        // assert that the base weight is multiplied by the penalty factor
        assertEquals(baseWeight * 3.0, result, 1e-6);
        // also verify that the getName method returns the expected name
        assertEquals("avoid_edges", weighting.getName());

        // verify that the underlying weighting's calcEdgeWeight was invoked exactly once
        verify(superWeighting, times(1)).calcEdgeWeight(edgeState, false);
    }

    /**
     * Verifies that the calculated weight is unchanged when the edge ID is not
     * contained in the avoided set.
     */
    @Test
    public void testCalcEdgeWeightWithoutPenalty() {
        Weighting superWeighting = mock(Weighting.class);
        EdgeIteratorState edgeState = mock(EdgeIteratorState.class);

        double baseWeight = 5.0;
        when(superWeighting.calcEdgeWeight(edgeState, false)).thenReturn(baseWeight);
        when(edgeState.getEdge()).thenReturn(7);

        AvoidEdgesWeighting weighting = new AvoidEdgesWeighting(superWeighting);
        weighting.setEdgePenaltyFactor(4.0);

        // avoided set does not contain the mocked edge ID
        GHIntHashSet avoided = new GHIntHashSet();
        avoided.add(42);
        weighting.setAvoidedEdges(avoided);

        double result = weighting.calcEdgeWeight(edgeState, false);

        // since the edge is not avoided the result should equal the base weight
        assertEquals(baseWeight, result, 1e-6);
        // verify the underlying weighting is still called
        verify(superWeighting, times(1)).calcEdgeWeight(edgeState, false);
    }
}
