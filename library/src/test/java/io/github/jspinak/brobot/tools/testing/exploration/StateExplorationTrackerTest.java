package io.github.jspinak.brobot.tools.testing.exploration;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateExplorationTracker.
 * Tests the path optimization and unvisited state tracking logic.
 */
@DisplayName("StateExplorationTracker Tests")
class StateExplorationTrackerTest extends BrobotTestBase {

    private StateExplorationTracker tracker;
    
    // Mocked dependencies
    private StateMemory stateMemory;
    private StateService stateService;
    private PathFinder pathFinder;
    
    // Test states
    private State visitedState;
    private State unvisitedState1;
    private State unvisitedState2;
    private State unvisitedState3;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create mocks
        stateMemory = mock(StateMemory.class);
        stateService = mock(StateService.class);
        pathFinder = mock(PathFinder.class);
        
        // Initialize tracker
        tracker = new StateExplorationTracker(stateMemory, stateService, pathFinder);
        
        // Create test states
        visitedState = createMockState(1L, "VisitedState", 5);  // Visited 5 times
        unvisitedState1 = createMockState(2L, "UnvisitedState1", 0);
        unvisitedState2 = createMockState(3L, "UnvisitedState2", 0);
        unvisitedState3 = createMockState(4L, "UnvisitedState3", 0);
    }
    
    private State createMockState(Long id, String name, int timesVisited) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.getTimesVisited()).thenReturn(timesVisited);
        return state;
    }

    @Nested
    @DisplayName("Unvisited States Discovery Tests")
    class UnvisitedStatesDiscoveryTests {
        
        @Test
        @DisplayName("Should identify all unvisited states")
        void shouldIdentifyAllUnvisitedStates() {
            // Arrange
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2,
                unvisitedState3
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Act
            Set<Long> unvisited = tracker.getUnvisitedStates();
            
            // Assert
            assertEquals(3, unvisited.size());
            assertFalse(unvisited.contains(1L)); // Visited state
            assertTrue(unvisited.contains(2L));  // Unvisited states
            assertTrue(unvisited.contains(3L));
            assertTrue(unvisited.contains(4L));
        }
        
        @Test
        @DisplayName("Should return empty set when all states are visited")
        void shouldReturnEmptyWhenAllStatesVisited() {
            // Arrange
            State visitedState2 = createMockState(2L, "State2", 2);
            State visitedState3 = createMockState(3L, "State3", 1);
            
            List<State> allStates = Arrays.asList(
                visitedState,
                visitedState2,
                visitedState3
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Act
            Set<Long> unvisited = tracker.getUnvisitedStates();
            
            // Assert
            assertTrue(unvisited.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty state list")
        void shouldHandleEmptyStateList() {
            // Arrange
            when(stateService.getAllStates()).thenReturn(Collections.emptyList());
            
            // Act
            Set<Long> unvisited = tracker.getUnvisitedStates();
            
            // Assert
            assertTrue(unvisited.isEmpty());
        }
        
        @Test
        @DisplayName("Should consider zero visits as unvisited")
        void shouldConsiderZeroVisitsAsUnvisited() {
            // Arrange
            State zeroVisitState = createMockState(5L, "ZeroVisitState", 0);
            List<State> allStates = Arrays.asList(visitedState, zeroVisitState);
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Act
            Set<Long> unvisited = tracker.getUnvisitedStates();
            
            // Assert
            assertEquals(1, unvisited.size());
            assertTrue(unvisited.contains(5L));
        }
    }

    @Nested
    @DisplayName("Closest Unvisited State Tests")
    class ClosestUnvisitedStateTests {
        
        @Test
        @DisplayName("Should find closest unvisited state from given start states")
        void shouldFindClosestUnvisitedFromStartStates() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2,
                unvisitedState3
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Mock path finding - state 2 is closest (score 10), state 3 is farther (score 20)
            Paths pathsToState2 = mock(Paths.class);
            when(pathsToState2.getBestScore()).thenReturn(10);
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(pathsToState2);
            
            Paths pathsToState3 = mock(Paths.class);
            when(pathsToState3.getBestScore()).thenReturn(20);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(pathsToState3);
            
            Paths pathsToState4 = mock(Paths.class);
            when(pathsToState4.getBestScore()).thenReturn(30);
            when(pathFinder.getPathsToState(startStates, 4L)).thenReturn(pathsToState4);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(2L, closest.get()); // State 2 has the lowest score
        }
        
        @Test
        @DisplayName("Should exclude unreachable states with zero or negative scores")
        void shouldExcludeUnreachableStates() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // State 2 is unreachable (score 0)
            Paths unreachablePath = mock(Paths.class);
            when(unreachablePath.getBestScore()).thenReturn(0);
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(unreachablePath);
            
            // State 3 is reachable (score 15)
            Paths reachablePath = mock(Paths.class);
            when(reachablePath.getBestScore()).thenReturn(15);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(reachablePath);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(3L, closest.get()); // Only state 3 is reachable
        }
        
        @Test
        @DisplayName("Should return empty when no unvisited states are reachable")
        void shouldReturnEmptyWhenNoReachableUnvisited() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // All unvisited states are unreachable
            Paths unreachablePath = mock(Paths.class);
            when(unreachablePath.getBestScore()).thenReturn(0);
            when(pathFinder.getPathsToState(eq(startStates), anyLong())).thenReturn(unreachablePath);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertFalse(closest.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when all states are visited")
        void shouldReturnEmptyWhenAllStatesVisited() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            State visitedState2 = createMockState(2L, "State2", 3);
            List<State> allStates = Arrays.asList(visitedState, visitedState2);
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertFalse(closest.isPresent());
            verify(pathFinder, never()).getPathsToState(any(), anyLong());
        }
        
        @Test
        @DisplayName("Should handle multiple states with same score")
        void shouldHandleMultipleStatesWithSameScore() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Both states have same score
            Paths samePath = mock(Paths.class);
            when(samePath.getBestScore()).thenReturn(10);
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(samePath);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(samePath);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            // TreeMap will return one of them (implementation dependent)
            assertTrue(closest.get() == 2L || closest.get() == 3L);
        }
    }

    @Nested
    @DisplayName("Active States Integration Tests")
    class ActiveStatesIntegrationTests {
        
        @Test
        @DisplayName("Should use active states when no start states specified")
        void shouldUseActiveStatesWhenNoStartStatesSpecified() {
            // Arrange
            Set<Long> activeStates = Set.of(1L, 5L);
            when(stateMemory.getActiveStates()).thenReturn(activeStates);
            
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            Paths paths = mock(Paths.class);
            when(paths.getBestScore()).thenReturn(10);
            when(pathFinder.getPathsToState(activeStates, 2L)).thenReturn(paths);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited();
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(2L, closest.get());
            
            // Verify active states were used
            verify(stateMemory).getActiveStates();
            verify(pathFinder).getPathsToState(activeStates, 2L);
        }
        
        @Test
        @DisplayName("Should handle empty active states")
        void shouldHandleEmptyActiveStates() {
            // Arrange
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
            
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            Paths paths = mock(Paths.class);
            when(paths.getBestScore()).thenReturn(0); // No path from empty start
            when(pathFinder.getPathsToState(any(), anyLong())).thenReturn(paths);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited();
            
            // Assert
            assertFalse(closest.isPresent());
        }
    }

    @Nested
    @DisplayName("Path Scoring and Prioritization Tests")
    class PathScoringTests {
        
        @Test
        @DisplayName("Should prioritize states by path score")
        void shouldPrioritizeStatesByPathScore() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            State unvisited4 = createMockState(5L, "Unvisited4", 0);
            State unvisited5 = createMockState(6L, "Unvisited5", 0);
            
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2,
                unvisitedState3,
                unvisited4,
                unvisited5
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Create paths with different scores
            Paths path50 = mock(Paths.class);
            when(path50.getBestScore()).thenReturn(50);
            
            Paths path10 = mock(Paths.class);
            when(path10.getBestScore()).thenReturn(10);
            
            Paths path30 = mock(Paths.class);
            when(path30.getBestScore()).thenReturn(30);
            
            Paths path0 = mock(Paths.class);
            when(path0.getBestScore()).thenReturn(0);
            
            Paths path20 = mock(Paths.class);
            when(path20.getBestScore()).thenReturn(20);
            
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(path50);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(path10); // Closest
            when(pathFinder.getPathsToState(startStates, 4L)).thenReturn(path30);
            when(pathFinder.getPathsToState(startStates, 5L)).thenReturn(path0);  // Unreachable
            when(pathFinder.getPathsToState(startStates, 6L)).thenReturn(path20);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(3L, closest.get()); // State with score 10 is closest
        }
        
        @Test
        @DisplayName("Should handle negative path scores")
        void shouldHandleNegativePathScores() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Negative score should be excluded
            Paths negativePath = mock(Paths.class);
            when(negativePath.getBestScore()).thenReturn(-10);
            
            Paths positivePath = mock(Paths.class);
            when(positivePath.getBestScore()).thenReturn(20);
            
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(negativePath);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(positivePath);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(3L, closest.get()); // Only positive score state
        }
        
        @Test
        @DisplayName("Should recalculate paths on each call")
        void shouldRecalculatePathsOnEachCall() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // First call - state 2 is closest
            Paths firstPath = mock(Paths.class);
            when(firstPath.getBestScore()).thenReturn(10);
            
            Paths secondPath = mock(Paths.class);
            when(secondPath.getBestScore()).thenReturn(50);
            
            when(pathFinder.getPathsToState(startStates, 2L))
                .thenReturn(firstPath)
                .thenReturn(secondPath); // Second call - different score
            
            // Act
            Optional<Long> closest1 = tracker.getClosestUnvisited(startStates);
            Optional<Long> closest2 = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest1.isPresent());
            assertTrue(closest2.isPresent());
            
            // Paths should be recalculated
            verify(pathFinder, times(2)).getPathsToState(startStates, 2L);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null start states gracefully")
        void shouldHandleNullStartStates() {
            // Arrange
            List<State> allStates = Arrays.asList(unvisitedState1);
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                tracker.getClosestUnvisited(null);
            });
        }
        
        @Test
        @DisplayName("Should handle very large scores")
        void shouldHandleVeryLargeScores() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = Arrays.asList(
                visitedState,
                unvisitedState1,
                unvisitedState2
            );
            when(stateService.getAllStates()).thenReturn(allStates);
            
            Paths pathMax = mock(Paths.class);
            when(pathMax.getBestScore()).thenReturn(Integer.MAX_VALUE);
            
            Paths path100 = mock(Paths.class);
            when(path100.getBestScore()).thenReturn(100);
            
            when(pathFinder.getPathsToState(startStates, 2L)).thenReturn(pathMax);
            when(pathFinder.getPathsToState(startStates, 3L)).thenReturn(path100);
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(3L, closest.get()); // Lower score wins
        }
        
        @Test
        @DisplayName("Should maintain TreeMap ordering with many states")
        void shouldMaintainTreeMapOrderingWithManyStates() {
            // Arrange
            Set<Long> startStates = Set.of(1L);
            List<State> allStates = new ArrayList<>();
            allStates.add(visitedState);
            
            // Create many unvisited states
            for (long i = 10; i <= 20; i++) {
                allStates.add(createMockState(i, "State" + i, 0));
            }
            when(stateService.getAllStates()).thenReturn(allStates);
            
            // Mock paths with varying scores
            for (long i = 10; i <= 20; i++) {
                int score = (int)(30 - i); // Inverse relationship
                Paths path = mock(Paths.class);
                when(path.getBestScore()).thenReturn(score);
                when(pathFinder.getPathsToState(startStates, i)).thenReturn(path);
            }
            
            // Act
            Optional<Long> closest = tracker.getClosestUnvisited(startStates);
            
            // Assert
            assertTrue(closest.isPresent());
            assertEquals(20L, closest.get()); // State 20 has lowest score (10)
        }
    }
}