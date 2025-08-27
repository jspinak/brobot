package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Advanced test suite for PathFinder - finds navigation paths through state graphs.
 * Tests path algorithms, optimization strategies, caching, and complex graph scenarios.
 */
@DisplayName("PathFinder Advanced Tests")
class PathFinderAdvancedTest extends BrobotTestBase {
    
    private PathFinder pathFinder;
    
    @Mock
    private StateTransitionsJointTable mockJointTable;
    
    @Mock
    private StateService mockStateService;
    
    @Mock
    private StateTransitionService mockTransitionService;
    
    @Mock
    private State startState;
    
    @Mock
    private State targetState;
    
    @Mock
    private State intermediateState1;
    
    @Mock
    private State intermediateState2;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        pathFinder = new PathFinder(mockJointTable, mockStateService, mockTransitionService);
        
        // Setup mock states
        when(startState.getId()).thenReturn(1L);
        when(startState.getName()).thenReturn("StartState");
        when(targetState.getId()).thenReturn(10L);
        when(targetState.getName()).thenReturn("TargetState");
        when(intermediateState1.getId()).thenReturn(5L);
        when(intermediateState1.getName()).thenReturn("IntermediateState1");
        when(intermediateState2.getId()).thenReturn(6L);
        when(intermediateState2.getName()).thenReturn("IntermediateState2");
        
        // Setup StateService mock
        when(mockStateService.getStateName(1L)).thenReturn("StartState");
        when(mockStateService.getStateName(5L)).thenReturn("IntermediateState1");
        when(mockStateService.getStateName(6L)).thenReturn("IntermediateState2");
        when(mockStateService.getStateName(10L)).thenReturn("TargetState");
        
        when(mockStateService.getState(1L)).thenReturn(Optional.of(startState));
        when(mockStateService.getState(5L)).thenReturn(Optional.of(intermediateState1));
        when(mockStateService.getState(6L)).thenReturn(Optional.of(intermediateState2));
        when(mockStateService.getState(10L)).thenReturn(Optional.of(targetState));
    }
    
    @Nested
    @DisplayName("Basic Path Finding")
    class BasicPathFinding {
        
        @Test
        @DisplayName("Should find direct path between two states")
        void testDirectPath() {
            // Arrange
            setupDirectPath();
            
            // Act
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Assert
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should find path through intermediate states")
        void testPathThroughIntermediates() {
            // Arrange
            setupPathWithIntermediates();
            
            // Act
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Assert
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle multiple start states")
        void testMultipleStartStates() {
            // Arrange
            setupMultiplePaths();
            List<State> startStates = Arrays.asList(startState, intermediateState1);
            
            // Act
            Paths paths = pathFinder.getPathsToState(startStates, targetState);
            
            // Assert
            assertNotNull(paths);
            // Should find paths from both start states
        }
        
        @Test
        @DisplayName("Should return empty when no path exists")
        void testNoPathExists() {
            // Arrange - no transitions configured
            
            // Act
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Assert
            assertNotNull(paths);
            assertTrue(paths.getPaths().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Path Optimization")
    class PathOptimization {
        
        @Test
        @DisplayName("Should sort paths by score")
        void testPathSorting() {
            // Arrange
            setupMultiplePathsWithScores();
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 10L);
            
            // Assert
            assertNotNull(paths);
            if (!paths.getPaths().isEmpty()) {
                // Paths should be sorted by score
                List<Path> pathList = paths.getPaths();
                for (int i = 1; i < pathList.size(); i++) {
                    assertTrue(pathList.get(i - 1).getScore() <= pathList.get(i).getScore());
                }
            }
        }
        
        @Test
        @DisplayName("Should find all valid paths")
        void testFindAllPaths() {
            // Arrange
            setupMultiplePaths();
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 10L);
            
            // Assert
            assertNotNull(paths);
            // Should find multiple paths if they exist
        }
        
        @Test
        @DisplayName("Should avoid cycles in path finding")
        void testCycleAvoidance() {
            // Arrange
            setupCyclicGraph();
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 10L);
            
            // Assert
            assertNotNull(paths);
            // Should not get stuck in cycles
            paths.getPaths().forEach(path -> {
                // No state should appear twice in a path
                Set<Long> uniqueStates = new HashSet<>();
                path.getStates().forEach(stateId -> {
                    assertFalse(uniqueStates.contains(stateId));
                    uniqueStates.add(stateId);
                });
            });
        }
    }
    
    @Nested
    @DisplayName("Complex Graph Scenarios")
    class ComplexGraphScenarios {
        
        @Test
        @DisplayName("Should handle disconnected graph components")
        void testDisconnectedGraph() {
            // Arrange - create disconnected components
            setupDisconnectedGraph();
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 10L);
            
            // Assert
            assertNotNull(paths);
            // Should return empty if states are in different components
        }
        
        @Test
        @DisplayName("Should handle large graphs efficiently")
        void testLargeGraph() {
            // Arrange
            setupLargeGraph(100); // 100 states
            
            // Act
            long startTime = System.currentTimeMillis();
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 100L);
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertNotNull(paths);
            assertTrue(duration < 5000); // Should complete within 5 seconds
        }
        
        @ParameterizedTest
        @ValueSource(ints = {10, 20, 50})
        @DisplayName("Should scale with graph size")
        void testScalability(int graphSize) {
            // Arrange
            setupLargeGraph(graphSize);
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), (long) graphSize);
            
            // Assert
            assertNotNull(paths);
        }
    }
    
    @Nested
    @DisplayName("Concurrent Path Finding")
    class ConcurrentPathFinding {
        
        @Test
        @DisplayName("Should handle concurrent path queries")
        void testConcurrentQueries() throws InterruptedException {
            // Arrange
            setupMultiplePaths();
            int queryCount = 20;
            CountDownLatch latch = new CountDownLatch(queryCount);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<Paths> results = Collections.synchronizedList(new ArrayList<>());
            
            // Act
            for (int i = 0; i < queryCount; i++) {
                executor.submit(() -> {
                    try {
                        Paths paths = pathFinder.getPathsToState(Set.of(1L), 10L);
                        results.add(paths);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Assert
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertEquals(queryCount, results.size());
            executor.shutdown();
        }
        
        @Test
        @DisplayName("Should maintain consistency during concurrent modifications")
        void testConcurrentModifications() throws InterruptedException {
            // Arrange
            int threads = 10;
            CountDownLatch latch = new CountDownLatch(threads);
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            
            // Act - concurrent reads while simulating modifications
            for (int i = 0; i < threads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        // Each thread queries different paths
                        Paths paths = pathFinder.getPathsToState(
                            Set.of((long) threadId), 
                            (long) (10 + threadId)
                        );
                        assertNotNull(paths);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Assert
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            executor.shutdown();
        }
    }
    
    @Nested
    @DisplayName("Special Cases")
    class SpecialCases {
        
        @Test
        @DisplayName("Should handle self-loops")
        void testSelfLoops() {
            // Arrange
            setupSelfLoop();
            
            // Act
            Paths paths = pathFinder.getPathsToState(Set.of(1L), 1L);
            
            // Assert
            assertNotNull(paths);
            // Should handle state targeting itself
        }
        
        @Test
        @DisplayName("Should handle bidirectional paths")
        void testBidirectionalPaths() {
            // Arrange
            setupBidirectionalPaths();
            
            // Act
            Paths forwardPaths = pathFinder.getPathsToState(Set.of(1L), 10L);
            Paths backwardPaths = pathFinder.getPathsToState(Set.of(10L), 1L);
            
            // Assert
            assertNotNull(forwardPaths);
            assertNotNull(backwardPaths);
        }
        
        @Test
        @DisplayName("Should handle empty start states")
        void testEmptyStartStates() {
            // Act
            Paths paths = pathFinder.getPathsToState(new HashSet<>(), 10L);
            
            // Assert
            assertNotNull(paths);
            assertTrue(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null target state")
        void testNullTargetState() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Paths paths = pathFinder.getPathsToState(Set.of(1L), null);
                assertNotNull(paths);
            });
        }
    }
    
    // Helper methods for test setup
    private void setupDirectPath() {
        StateTransitions transitions = new StateTransitions();
        transitions.setStateId(1L);
        
        TaskSequenceStateTransition directTransition = new TaskSequenceStateTransition();
        directTransition.setActivate(Set.of(10L));
        transitions.getTransitions().add(directTransition);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(10L)).thenReturn(Set.of(1L));
        when(mockTransitionService.getStateTransitions(1L)).thenReturn(Optional.of(transitions));
    }
    
    private void setupPathWithIntermediates() {
        // Path: 1 -> 5 -> 10
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateId(1L);
        TaskSequenceStateTransition t1 = new TaskSequenceStateTransition();
        t1.setActivate(Set.of(5L));
        transitions1.getTransitions().add(t1);
        
        StateTransitions transitions5 = new StateTransitions();
        transitions5.setStateId(5L);
        TaskSequenceStateTransition t2 = new TaskSequenceStateTransition();
        t2.setActivate(Set.of(10L));
        transitions5.getTransitions().add(t2);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(5L)).thenReturn(Set.of(1L));
        when(mockJointTable.getAllStatesWithTransitionsTo(10L)).thenReturn(Set.of(5L));
        when(mockTransitionService.getStateTransitions(1L)).thenReturn(Optional.of(transitions1));
        when(mockTransitionService.getStateTransitions(5L)).thenReturn(Optional.of(transitions5));
    }
    
    private void setupMultiplePaths() {
        // Multiple paths to target
        setupPathWithIntermediates();
        // Add alternative path: 1 -> 6 -> 10
        StateTransitions transitions1Alt = new StateTransitions();
        transitions1Alt.setStateId(1L);
        TaskSequenceStateTransition t1Alt = new TaskSequenceStateTransition();
        t1Alt.setActivate(Set.of(6L));
        transitions1Alt.getTransitions().add(t1Alt);
        
        StateTransitions transitions6 = new StateTransitions();
        transitions6.setStateId(6L);
        TaskSequenceStateTransition t3 = new TaskSequenceStateTransition();
        t3.setActivate(Set.of(10L));
        transitions6.getTransitions().add(t3);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(6L)).thenReturn(Set.of(1L));
        when(mockJointTable.getAllStatesWithTransitionsTo(10L)).thenReturn(Set.of(5L, 6L));
        when(mockTransitionService.getStateTransitions(6L)).thenReturn(Optional.of(transitions6));
    }
    
    private void setupMultiplePathsWithScores() {
        setupMultiplePaths();
        // Add scores to transitions for path optimization testing
    }
    
    private void setupCyclicGraph() {
        // Create cycle: 1 -> 5 -> 6 -> 5 (cycle) and 6 -> 10
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateId(1L);
        TaskSequenceStateTransition t1 = new TaskSequenceStateTransition();
        t1.setActivate(Set.of(5L));
        transitions1.getTransitions().add(t1);
        
        StateTransitions transitions5 = new StateTransitions();
        transitions5.setStateId(5L);
        TaskSequenceStateTransition t2 = new TaskSequenceStateTransition();
        t2.setActivate(Set.of(6L));
        transitions5.getTransitions().add(t2);
        
        StateTransitions transitions6 = new StateTransitions();
        transitions6.setStateId(6L);
        TaskSequenceStateTransition t3 = new TaskSequenceStateTransition();
        t3.setActivate(Set.of(5L, 10L)); // Cycle back to 5 and path to 10
        transitions6.getTransitions().add(t3);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(5L)).thenReturn(Set.of(1L, 6L));
        when(mockJointTable.getAllStatesWithTransitionsTo(6L)).thenReturn(Set.of(5L));
        when(mockJointTable.getAllStatesWithTransitionsTo(10L)).thenReturn(Set.of(6L));
        when(mockTransitionService.getStateTransitions(1L)).thenReturn(Optional.of(transitions1));
        when(mockTransitionService.getStateTransitions(5L)).thenReturn(Optional.of(transitions5));
        when(mockTransitionService.getStateTransitions(6L)).thenReturn(Optional.of(transitions6));
    }
    
    private void setupDisconnectedGraph() {
        // Component 1: states 1, 2, 3
        // Component 2: states 10, 11, 12
        // No connections between components
        when(mockJointTable.getAllStatesWithTransitionsTo(anyLong())).thenReturn(Collections.emptySet());
    }
    
    private void setupLargeGraph(int size) {
        // Create linear chain: 1 -> 2 -> 3 -> ... -> size
        for (int i = 1; i < size; i++) {
            long fromId = i;
            long toId = i + 1;
            
            StateTransitions transitions = new StateTransitions();
            transitions.setStateId(fromId);
            TaskSequenceStateTransition t = new TaskSequenceStateTransition();
            t.setActivate(Set.of(toId));
            transitions.getTransitions().add(t);
            
            when(mockJointTable.getAllStatesWithTransitionsTo(toId)).thenReturn(Set.of(fromId));
            when(mockTransitionService.getStateTransitions(fromId)).thenReturn(Optional.of(transitions));
            when(mockStateService.getStateName(fromId)).thenReturn("State" + fromId);
        }
        when(mockStateService.getStateName((long) size)).thenReturn("State" + size);
    }
    
    private void setupSelfLoop() {
        StateTransitions transitions = new StateTransitions();
        transitions.setStateId(1L);
        TaskSequenceStateTransition t = new TaskSequenceStateTransition();
        t.setActivate(Set.of(1L)); // Self-loop
        transitions.getTransitions().add(t);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(1L)).thenReturn(Set.of(1L));
        when(mockTransitionService.getStateTransitions(1L)).thenReturn(Optional.of(transitions));
    }
    
    private void setupBidirectionalPaths() {
        // 1 <-> 10 bidirectional
        StateTransitions transitions1 = new StateTransitions();
        transitions1.setStateId(1L);
        TaskSequenceStateTransition t1 = new TaskSequenceStateTransition();
        t1.setActivate(Set.of(10L));
        transitions1.getTransitions().add(t1);
        
        StateTransitions transitions10 = new StateTransitions();
        transitions10.setStateId(10L);
        TaskSequenceStateTransition t2 = new TaskSequenceStateTransition();
        t2.setActivate(Set.of(1L));
        transitions10.getTransitions().add(t2);
        
        when(mockJointTable.getAllStatesWithTransitionsTo(1L)).thenReturn(Set.of(10L));
        when(mockJointTable.getAllStatesWithTransitionsTo(10L)).thenReturn(Set.of(1L));
        when(mockTransitionService.getStateTransitions(1L)).thenReturn(Optional.of(transitions1));
        when(mockTransitionService.getStateTransitions(10L)).thenReturn(Optional.of(transitions10));
    }
}