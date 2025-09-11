package io.github.jspinak.brobot.navigation.path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Edge case and boundary tests for PathFinder. Tests complex path finding scenarios and error
 * handling.
 */
@DisplayName("PathFinder Edge Cases")
class PathFinderEdgeCasesTest extends BrobotTestBase {

    private PathFinder pathFinder;

    @Mock private StateTransitionsJointTable mockJointTable;

    @Mock private StateService mockStateService;

    @Mock private StateTransitionService mockStateTransitionService;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        pathFinder = new PathFinder(mockJointTable, mockStateService, mockStateTransitionService);
    }

    @Nested
    @DisplayName("Empty and Null Inputs")
    class EmptyAndNullInputs {

        @Test
        @DisplayName("Should handle empty from states")
        void testEmptyFromStates() {
            Set<Long> emptyFrom = new HashSet<>();

            // Mock the joint table to return empty when queried
            when(mockJointTable.getStatesWithTransitionsTo(100L)).thenReturn(new HashSet<>());

            Paths result = pathFinder.getPathsToState(emptyFrom, 100L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null target state")
        void testNullTargetState() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L, 2L));

            Paths result = pathFinder.getPathsToState(fromStates, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle from state equals target state")
        void testFromEqualsTarget() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(100L));

            Paths result = pathFinder.getPathsToState(fromStates, 100L);

            assertNotNull(result);
            // Should return a path with just the single state
            if (!result.isEmpty()) {
                Path path = result.getPaths().get(0);
                assertEquals(1, path.size());
                assertEquals(100L, path.get(0));
            }
        }
    }

    @Nested
    @DisplayName("Disconnected Graph")
    class DisconnectedGraph {

        @Test
        @DisplayName("Should return empty paths for unreachable target")
        void testUnreachableTarget() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            // No parents for target state
            when(mockJointTable.getStatesWithTransitionsTo(100L)).thenReturn(new HashSet<>());

            Paths result = pathFinder.getPathsToState(fromStates, 100L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle isolated state clusters")
        void testIsolatedClusters() {
            // Cluster 1: 1 -> 2 -> 3
            // Cluster 2: 10 -> 11 -> 12
            // Try to find path from cluster 1 to cluster 2

            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            when(mockJointTable.getStatesWithTransitionsTo(12L))
                    .thenReturn(new HashSet<>(Arrays.asList(11L)));
            when(mockJointTable.getStatesWithTransitionsTo(11L))
                    .thenReturn(new HashSet<>(Arrays.asList(10L)));
            when(mockJointTable.getStatesWithTransitionsTo(10L))
                    .thenReturn(new HashSet<>()); // No incoming

            Paths result = pathFinder.getPathsToState(fromStates, 12L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Cyclic Graphs")
    class CyclicGraphs {

        @Test
        @DisplayName("Should handle self-loop")
        void testSelfLoop() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            // State 1 has a self-loop
            when(mockJointTable.getStatesWithTransitionsTo(1L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));

            Paths result = pathFinder.getPathsToState(fromStates, 1L);

            assertNotNull(result);
            // Should find the trivial path (already at target)
            if (!result.isEmpty()) {
                Path path = result.getPaths().get(0);
                assertEquals(1, path.size());
                assertEquals(1L, path.get(0));
            }
        }

        @Test
        @DisplayName("Should handle simple cycle")
        void testSimpleCycle() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            // 1 -> 2 -> 3 -> 1 (cycle)
            when(mockJointTable.getStatesWithTransitionsTo(3L))
                    .thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(mockJointTable.getStatesWithTransitionsTo(1L))
                    .thenReturn(new HashSet<>(Arrays.asList(3L)));

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(10);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, 3L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Should find path 1 -> 2 -> 3
            Path path = result.getPaths().get(0);
            assertEquals(3, path.size());
        }

        @Test
        @DisplayName("Should handle complex cycle with multiple entry points")
        void testComplexCycle() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L, 2L));

            // Complex graph with cycle
            // 1 -> 3
            // 2 -> 3
            // 3 -> 4 -> 5 -> 3 (cycle)
            // 5 -> 6 (exit from cycle)

            when(mockJointTable.getStatesWithTransitionsTo(6L))
                    .thenReturn(new HashSet<>(Arrays.asList(5L)));
            when(mockJointTable.getStatesWithTransitionsTo(5L))
                    .thenReturn(new HashSet<>(Arrays.asList(4L)));
            when(mockJointTable.getStatesWithTransitionsTo(4L))
                    .thenReturn(new HashSet<>(Arrays.asList(3L)));
            when(mockJointTable.getStatesWithTransitionsTo(3L))
                    .thenReturn(
                            new HashSet<>(
                                    Arrays.asList(1L, 2L, 5L))); // Multiple parents including cycle

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(5);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, 6L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Should find paths without infinite loop
            List<Path> allPaths = result.getPaths();
            for (Path path : allPaths) {
                assertTrue(path.size() <= 10); // Reasonable path length
                assertEquals(6L, path.get(path.size() - 1)); // Ends at target
            }
        }
    }

    @Nested
    @DisplayName("Large Graphs")
    class LargeGraphs {

        @Test
        @DisplayName("Should handle graph with many states")
        void testLargeGraph() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(0L));
            Long target = 999L;

            // Create chain 0 -> 1 -> 2 -> ... -> 999
            for (long i = 1; i <= 999; i++) {
                final long current = i;
                when(mockJointTable.getStatesWithTransitionsTo(current))
                        .thenReturn(new HashSet<>(Arrays.asList(current - 1)));
            }

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(1);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, target);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            Path path = result.getPaths().get(0);
            assertEquals(1000, path.size()); // Full chain
            assertEquals(0L, path.get(0));
            assertEquals(999L, path.get(999));
        }

        @Test
        @DisplayName("Should handle graph with many branches")
        void testHighlyBranchedGraph() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(0L));

            // Each state has 3 parents (except root)
            // Creates exponential growth in paths
            when(mockJointTable.getStatesWithTransitionsTo(10L))
                    .thenReturn(new HashSet<>(Arrays.asList(7L, 8L, 9L)));
            when(mockJointTable.getStatesWithTransitionsTo(7L))
                    .thenReturn(new HashSet<>(Arrays.asList(4L, 5L, 6L)));
            when(mockJointTable.getStatesWithTransitionsTo(8L))
                    .thenReturn(new HashSet<>(Arrays.asList(4L, 5L, 6L)));
            when(mockJointTable.getStatesWithTransitionsTo(9L))
                    .thenReturn(new HashSet<>(Arrays.asList(4L, 5L, 6L)));
            when(mockJointTable.getStatesWithTransitionsTo(4L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            when(mockJointTable.getStatesWithTransitionsTo(5L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            when(mockJointTable.getStatesWithTransitionsTo(6L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            when(mockJointTable.getStatesWithTransitionsTo(1L))
                    .thenReturn(new HashSet<>(Arrays.asList(0L)));
            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(0L)));
            when(mockJointTable.getStatesWithTransitionsTo(3L))
                    .thenReturn(new HashSet<>(Arrays.asList(0L)));

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(1);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, 10L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Should find multiple paths
            List<Path> allPaths = result.getPaths();
            assertTrue(allPaths.size() > 1);

            // All paths should be valid
            for (Path path : allPaths) {
                assertEquals(0L, path.get(0));
                assertEquals(10L, path.get(path.size() - 1));
            }
        }
    }

    @Nested
    @DisplayName("State Scoring Edge Cases")
    class StateScoringEdgeCases {

        @Test
        @DisplayName("Should handle states with no score")
        void testStatesWithNoScore() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            when(mockJointTable.getStatesWithTransitionsTo(3L))
                    .thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));

            // State service returns empty for some states
            when(mockStateService.getState(1L)).thenReturn(Optional.empty());
            when(mockStateService.getState(2L)).thenReturn(Optional.empty());
            when(mockStateService.getState(3L)).thenReturn(Optional.empty());

            Paths result = pathFinder.getPathsToState(fromStates, 3L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            Path path = result.getPaths().get(0);
            assertEquals(0, path.getScore()); // Default score
        }

        @Test
        @DisplayName("Should handle states with negative scores")
        void testNegativeScores() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));

            State state1 = mock(State.class);
            when(state1.getPathScore()).thenReturn(-10);
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(-5);

            when(mockStateService.getState(1L)).thenReturn(Optional.of(state1));
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));

            Paths result = pathFinder.getPathsToState(fromStates, 2L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            Path path = result.getPaths().get(0);
            assertEquals(-15, path.getScore()); // Sum of negative scores
        }

        @Test
        @DisplayName("Should handle very large scores")
        void testVeryLargeScores() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L));

            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));

            State state1 = mock(State.class);
            when(state1.getPathScore()).thenReturn(Integer.MAX_VALUE / 2);
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(Integer.MAX_VALUE / 2);

            when(mockStateService.getState(1L)).thenReturn(Optional.of(state1));
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));

            Paths result = pathFinder.getPathsToState(fromStates, 2L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            Path path = result.getPaths().get(0);
            assertTrue(path.getScore() > 0); // Should handle large scores
        }
    }

    @Nested
    @DisplayName("Multiple Starting Points")
    class MultipleStartingPoints {

        @Test
        @DisplayName("Should find paths from multiple starting states")
        void testMultipleStartingStates() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));

            // All three starting states can reach target through different paths
            when(mockJointTable.getStatesWithTransitionsTo(10L))
                    .thenReturn(new HashSet<>(Arrays.asList(7L, 8L, 9L)));
            when(mockJointTable.getStatesWithTransitionsTo(7L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(mockJointTable.getStatesWithTransitionsTo(8L))
                    .thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(mockJointTable.getStatesWithTransitionsTo(9L))
                    .thenReturn(new HashSet<>(Arrays.asList(3L)));

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(5);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, 10L);

            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Should find paths from all starting points
            List<Path> allPaths = result.getPaths();
            assertEquals(3, allPaths.size());

            // Verify each starting state is represented
            Set<Long> startingStatesFound = new HashSet<>();
            for (Path path : allPaths) {
                startingStatesFound.add(path.get(0));
            }
            assertEquals(fromStates, startingStatesFound);
        }

        @Test
        @DisplayName("Should prefer shortest path among multiple starts")
        void testShortestPathFromMultipleStarts() {
            Set<Long> fromStates = new HashSet<>(Arrays.asList(1L, 5L));

            // Path from 1: 1 -> 2 -> 3 -> 4 -> 10
            // Path from 5: 5 -> 10 (direct)

            when(mockJointTable.getStatesWithTransitionsTo(10L))
                    .thenReturn(new HashSet<>(Arrays.asList(4L, 5L)));
            when(mockJointTable.getStatesWithTransitionsTo(4L))
                    .thenReturn(new HashSet<>(Arrays.asList(3L)));
            when(mockJointTable.getStatesWithTransitionsTo(3L))
                    .thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(mockJointTable.getStatesWithTransitionsTo(2L))
                    .thenReturn(new HashSet<>(Arrays.asList(1L)));

            State mockState = mock(State.class);
            when(mockState.getPathScore()).thenReturn(10);
            when(mockStateService.getState(anyLong())).thenReturn(Optional.of(mockState));

            Paths result = pathFinder.getPathsToState(fromStates, 10L);

            assertNotNull(result);
            assertEquals(2, result.getPaths().size());

            // Find the shortest path
            Path shortestPath =
                    result.getPaths().stream()
                            .min(Comparator.comparingInt(Path::size))
                            .orElseThrow();

            assertEquals(2, shortestPath.size());
            assertEquals(5L, shortestPath.get(0)); // Starts from 5
            assertEquals(10L, shortestPath.get(1));
        }
    }
}
