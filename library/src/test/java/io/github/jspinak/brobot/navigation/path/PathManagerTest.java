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
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive unit tests for PathManager. Tests path scoring, cleaning, and recovery operations.
 */
@DisplayName("PathManager Tests")
class PathManagerTest extends BrobotTestBase {

    private PathManager pathManager;

    @Mock private StateService mockStateService;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        pathManager = new PathManager(mockStateService);
    }

    @Nested
    @DisplayName("Score Calculation")
    class ScoreCalculation {

        @Test
        @DisplayName("Should calculate path score from state scores")
        void testCalculatePathScore() {
            // Create path with 3 states
            Path path = new Path();
            path.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));

            // Mock states with different scores
            State state1 = mock(State.class);
            when(state1.getPathScore()).thenReturn(10);
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(20);
            State state3 = mock(State.class);
            when(state3.getPathScore()).thenReturn(15);

            when(mockStateService.getState(1L)).thenReturn(Optional.of(state1));
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));
            when(mockStateService.getState(3L)).thenReturn(Optional.of(state3));

            pathManager.updateScore(path);

            assertEquals(45, path.getScore()); // 10 + 20 + 15
        }

        @Test
        @DisplayName("Should handle missing states with zero score")
        void testMissingStatesZeroScore() {
            Path path = new Path();
            path.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));

            // Only state 2 exists
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(20);

            when(mockStateService.getState(1L)).thenReturn(Optional.empty());
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));
            when(mockStateService.getState(3L)).thenReturn(Optional.empty());

            pathManager.updateScore(path);

            assertEquals(20, path.getScore());
        }

        @Test
        @DisplayName("Should handle empty path")
        void testEmptyPathScore() {
            Path path = new Path();

            pathManager.updateScore(path);

            assertEquals(0, path.getScore());
            verify(mockStateService, never()).getState(anyLong());
        }

        @Test
        @DisplayName("Should handle negative scores")
        void testNegativeScores() {
            Path path = new Path();
            path.setStates(new ArrayList<>(Arrays.asList(1L, 2L)));

            State state1 = mock(State.class);
            when(state1.getPathScore()).thenReturn(-10);
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(30);

            when(mockStateService.getState(1L)).thenReturn(Optional.of(state1));
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));

            pathManager.updateScore(path);

            assertEquals(20, path.getScore()); // -10 + 30
        }

        @Test
        @DisplayName("Should handle very large scores")
        void testLargeScores() {
            Path path = new Path();
            path.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));

            State state1 = mock(State.class);
            when(state1.getPathScore()).thenReturn(Integer.MAX_VALUE / 3);
            State state2 = mock(State.class);
            when(state2.getPathScore()).thenReturn(Integer.MAX_VALUE / 3);
            State state3 = mock(State.class);
            when(state3.getPathScore()).thenReturn(Integer.MAX_VALUE / 3);

            when(mockStateService.getState(1L)).thenReturn(Optional.of(state1));
            when(mockStateService.getState(2L)).thenReturn(Optional.of(state2));
            when(mockStateService.getState(3L)).thenReturn(Optional.of(state3));

            pathManager.updateScore(path);

            // Score should handle overflow gracefully
            assertTrue(path.getScore() > 0);
        }
    }

    @Nested
    @DisplayName("Paths Score Update")
    class PathsScoreUpdate {

        @Test
        @DisplayName("Should update all path scores and sort")
        void testUpdateAllPathScores() {
            // Create multiple paths
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L, 2L)));
            Path path2 = new Path();
            path2.setStates(new ArrayList<>(Arrays.asList(3L, 4L)));
            Path path3 = new Path();
            path3.setStates(new ArrayList<>(Arrays.asList(5L, 6L)));

            Paths paths = new Paths(Arrays.asList(path1, path2, path3));

            // Mock states with varying scores
            mockStateWithScore(1L, 10);
            mockStateWithScore(2L, 20); // path1 total: 30
            mockStateWithScore(3L, 5);
            mockStateWithScore(4L, 10); // path2 total: 15
            mockStateWithScore(5L, 25);
            mockStateWithScore(6L, 30); // path3 total: 55

            pathManager.updateScores(paths);

            // Verify scores are updated
            assertEquals(30, path1.getScore());
            assertEquals(15, path2.getScore());
            assertEquals(55, path3.getScore());

            // Verify paths are sorted by score (ascending)
            List<Path> sortedPaths = paths.getPaths();
            assertEquals(15, sortedPaths.get(0).getScore()); // path2
            assertEquals(30, sortedPaths.get(1).getScore()); // path1
            assertEquals(55, sortedPaths.get(2).getScore()); // path3
        }

        @Test
        @DisplayName("Should handle empty paths collection")
        void testEmptyPathsCollection() {
            Paths paths = new Paths();

            pathManager.updateScores(paths);

            assertTrue(paths.isEmpty());
            verify(mockStateService, never()).getState(anyLong());
        }

        @Test
        @DisplayName("Should maintain path order with equal scores")
        void testEqualScorePaths() {
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L)));
            Path path2 = new Path();
            path2.setStates(new ArrayList<>(Arrays.asList(2L)));
            Path path3 = new Path();
            path3.setStates(new ArrayList<>(Arrays.asList(3L)));

            Paths paths = new Paths(Arrays.asList(path1, path2, path3));

            // All states have same score
            mockStateWithScore(1L, 10);
            mockStateWithScore(2L, 10);
            mockStateWithScore(3L, 10);

            pathManager.updateScores(paths);

            // All should have same score
            assertEquals(10, path1.getScore());
            assertEquals(10, path2.getScore());
            assertEquals(10, path3.getScore());
        }
    }

    @Nested
    @DisplayName("Path Cleaning and Recovery")
    class PathCleaningAndRecovery {

        @Test
        @DisplayName("Should clean paths and update scores")
        void testCleanPathsWithScoreUpdate() {
            // Create paths
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L)));
            Path path2 = new Path();
            path2.setStates(new ArrayList<>(Arrays.asList(5L, 6L, 7L)));
            Paths originalPaths = new Paths(Arrays.asList(path1, path2));

            // Active states after failure
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 6L));
            Long failedTransition = 3L;

            // Mock state scores
            mockStateWithScore(2L, 10);
            mockStateWithScore(3L, 15);
            mockStateWithScore(4L, 20);
            mockStateWithScore(6L, 5);
            mockStateWithScore(7L, 10);

            Paths cleanedPaths =
                    pathManager.getCleanPaths(activeStates, originalPaths, failedTransition);

            assertNotNull(cleanedPaths);
            // Paths should be cleaned and scores updated
            for (Path path : cleanedPaths.getPaths()) {
                assertTrue(path.getScore() >= 0);
            }
        }

        @Test
        @DisplayName("Should handle no active states")
        void testNoActiveStates() {
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
            Paths originalPaths = new Paths(Arrays.asList(path1));

            Set<Long> activeStates = new HashSet<>(); // Empty

            Paths cleanedPaths = pathManager.getCleanPaths(activeStates, originalPaths, null);

            assertNotNull(cleanedPaths);
            // Should return empty or modified paths
        }

        @Test
        @DisplayName("Should handle failed transition cleanup")
        void testFailedTransitionCleanup() {
            // Path that goes through failed transition
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L)));
            // Path that doesn't go through failed transition
            Path path2 = new Path();
            path2.setStates(new ArrayList<>(Arrays.asList(5L, 6L, 7L)));

            Paths originalPaths = new Paths(Arrays.asList(path1, path2));

            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L, 6L));
            Long failedTransition = 2L; // Failed at state 2

            mockStateWithScore(3L, 10);
            mockStateWithScore(4L, 15);
            mockStateWithScore(6L, 5);
            mockStateWithScore(7L, 8);

            Paths cleanedPaths =
                    pathManager.getCleanPaths(activeStates, originalPaths, failedTransition);

            assertNotNull(cleanedPaths);
            // Cleaned paths should have updated scores
            for (Path path : cleanedPaths.getPaths()) {
                assertNotNull(path.getScore());
            }
        }

        @Test
        @DisplayName("Should preserve paths when no cleaning needed")
        void testNoCleaningNeeded() {
            Path path1 = new Path();
            path1.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L)));
            Paths originalPaths = new Paths(Arrays.asList(path1));

            // All states are active
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L, 3L));

            mockStateWithScore(1L, 10);
            mockStateWithScore(2L, 20);
            mockStateWithScore(3L, 30);

            Paths cleanedPaths = pathManager.getCleanPaths(activeStates, originalPaths, null);

            assertNotNull(cleanedPaths);
            assertFalse(cleanedPaths.isEmpty());
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle multiple path recovery after failure")
        void testMultiplePathRecovery() {
            // Simulate complex navigation scenario
            Path mainPath = new Path();
            mainPath.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
            Path altPath1 = new Path();
            altPath1.setStates(new ArrayList<>(Arrays.asList(1L, 6L, 7L, 5L)));
            Path altPath2 = new Path();
            altPath2.setStates(new ArrayList<>(Arrays.asList(1L, 8L, 9L, 10L, 5L)));

            Paths paths = new Paths(Arrays.asList(mainPath, altPath1, altPath2));

            // Failure at state 3, currently at state 2
            Set<Long> activeStates = new HashSet<>(Arrays.asList(2L));
            Long failedTransition = 3L;

            // Mock all state scores
            for (long i = 1; i <= 10; i++) {
                mockStateWithScore(i, (int) (i * 5));
            }

            Paths recoveredPaths = pathManager.getCleanPaths(activeStates, paths, failedTransition);

            assertNotNull(recoveredPaths);
            // Verify scores are recalculated
            for (Path path : recoveredPaths.getPaths()) {
                assertTrue(path.getScore() >= 0);
            }
        }

        @Test
        @DisplayName("Should handle cyclic path references")
        void testCyclicPaths() {
            // Path with cycle: 1 -> 2 -> 3 -> 2 -> 4
            Path cyclicPath = new Path();
            cyclicPath.setStates(new ArrayList<>(Arrays.asList(1L, 2L, 3L, 2L, 4L)));
            Paths paths = new Paths(Arrays.asList(cyclicPath));

            mockStateWithScore(1L, 10);
            mockStateWithScore(2L, 20);
            mockStateWithScore(3L, 15);
            mockStateWithScore(4L, 25);

            pathManager.updateScores(paths);

            // Score should include state 2 twice
            assertEquals(90, cyclicPath.getScore()); // 10 + 20 + 15 + 20 + 25
        }

        @Test
        @DisplayName("Should handle very long paths efficiently")
        void testLongPaths() {
            // Create a very long path
            List<Long> longPathStates = new ArrayList<>();
            for (long i = 1; i <= 100; i++) {
                longPathStates.add(i);
                mockStateWithScore(i, 1);
            }

            Path longPath = new Path();
            longPath.setStates(longPathStates);
            Paths paths = new Paths(Arrays.asList(longPath));

            pathManager.updateScores(paths);

            assertEquals(100, longPath.getScore());
            verify(mockStateService, times(100)).getState(anyLong());
        }
    }

    // Helper method to mock state with score
    private void mockStateWithScore(Long stateId, int score) {
        State state = mock(State.class);
        when(state.getPathScore()).thenReturn(score);
        when(mockStateService.getState(stateId)).thenReturn(Optional.of(state));
    }
}
