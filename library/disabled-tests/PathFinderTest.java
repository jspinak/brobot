package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PathFinder Tests")
public class PathFinderTest extends BrobotTestBase {
    
    @Mock
    private StateTransitionsJointTable stateTransitionsJointTable;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateTransitionService stateTransitionService;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private PathFinder pathFinder;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        pathFinder = new PathFinder(stateTransitionsJointTable, stateService, stateTransitionService);
    }
    
    @Nested
    @DisplayName("Simple Path Finding")
    class SimplePathFinding {
        
        @Test
        @DisplayName("Should find direct path between two states")
        public void testFindDirectPath() {
            State startState = createMockState(1L, "Start");
            State targetState = createMockState(2L, "Target");
            
            StateTransitions transitions = mock(StateTransitions.class);
            StateTransition directTransition = mock(StateTransition.class);
            
            when(directTransition.getActivate()).thenReturn("Target");
            when(transitions.getActivateTransitions()).thenReturn(Collections.singletonMap("Target", directTransition));
            when(stateTransitionService.findByFromState(1L)).thenReturn(Optional.of(transitions));
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(startState), targetState);
            
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
            assertEquals(1, paths.getPaths().size());
            
            Path path = paths.getPaths().get(0);
            assertEquals(2, path.getStateTransitions().size()); // Start -> Target
            assertEquals(1L, path.getStateTransitions().get(0));
            assertEquals(2L, path.getStateTransitions().get(1));
        }
        
        @Test
        @DisplayName("Should return empty paths when no route exists")
        public void testNoPathExists() {
            State startState = createMockState(1L, "Start");
            State targetState = createMockState(2L, "Target");
            
            // No transitions from start to target
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>());
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(startState), targetState);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle self-transition (state to itself)")
        public void testSelfTransition() {
            State state = createMockState(1L, "SelfState");
            
            StateTransitions transitions = mock(StateTransitions.class);
            StateTransition selfTransition = mock(StateTransition.class);
            
            when(selfTransition.getActivate()).thenReturn("SelfState");
            when(transitions.getActivateTransitions()).thenReturn(Collections.singletonMap("SelfState", selfTransition));
            when(stateTransitionService.findByFromState(1L)).thenReturn(Optional.of(transitions));
            
            // State can transition to itself
            when(stateTransitionsJointTable.getActivatedBy(1L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateService.getStateName(1L)).thenReturn("SelfState");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(state), state);
            
            assertNotNull(paths);
            // Should find the self-transition as a valid path
            assertFalse(paths.getPaths().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Multi-Step Path Finding")
    class MultiStepPathFinding {
        
        @Test
        @DisplayName("Should find multi-step path through intermediate states")
        public void testMultiStepPath() {
            State startState = createMockState(1L, "Start");
            State middleState = createMockState(2L, "Middle");
            State targetState = createMockState(3L, "Target");
            
            // Setup transitions: Start -> Middle -> Target
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(2L)));
            
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("Middle");
            when(stateService.getStateName(3L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(startState), targetState);
            
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
            
            Path path = paths.getPaths().get(0);
            assertEquals(3, path.getStateTransitions().size());
            assertEquals(1L, path.getStateTransitions().get(0)); // Start
            assertEquals(2L, path.getStateTransitions().get(1)); // Middle
            assertEquals(3L, path.getStateTransitions().get(2)); // Target
        }
        
        @Test
        @DisplayName("Should find path through complex chain of states")
        public void testComplexChainPath() {
            State s1 = createMockState(1L, "S1");
            State s2 = createMockState(2L, "S2");
            State s3 = createMockState(3L, "S3");
            State s4 = createMockState(4L, "S4");
            State s5 = createMockState(5L, "S5");
            
            // Chain: S1 -> S2 -> S3 -> S4 -> S5
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(stateTransitionsJointTable.getActivatedBy(4L)).thenReturn(new HashSet<>(Arrays.asList(3L)));
            when(stateTransitionsJointTable.getActivatedBy(5L)).thenReturn(new HashSet<>(Arrays.asList(4L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "S" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(s1), s5);
            
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
            
            Path path = paths.getPaths().get(0);
            assertEquals(5, path.getStateTransitions().size());
            for (int i = 0; i < 5; i++) {
                assertEquals(i + 1, path.getStateTransitions().get(i).longValue());
            }
        }
    }
    
    @Nested
    @DisplayName("Multiple Paths")
    class MultiplePaths {
        
        @Test
        @DisplayName("Should find multiple paths to same target")
        public void testMultiplePaths() {
            State startState = createMockState(1L, "Start");
            State pathA = createMockState(2L, "PathA");
            State pathB = createMockState(3L, "PathB");
            State targetState = createMockState(4L, "Target");
            
            // Two paths: Start -> PathA -> Target and Start -> PathB -> Target
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(4L)).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));
            
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("PathA");
            when(stateService.getStateName(3L)).thenReturn("PathB");
            when(stateService.getStateName(4L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(startState), targetState);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().size() >= 2);
            
            // Verify both paths exist
            boolean hasPathThroughA = false;
            boolean hasPathThroughB = false;
            
            for (Path path : paths.getPaths()) {
                if (path.getStateTransitions().contains(2L)) {
                    hasPathThroughA = true;
                }
                if (path.getStateTransitions().contains(3L)) {
                    hasPathThroughB = true;
                }
            }
            
            assertTrue(hasPathThroughA);
            assertTrue(hasPathThroughB);
        }
        
        @Test
        @DisplayName("Should rank paths by score")
        public void testPathScoring() {
            State startState = createMockState(1L, "Start");
            State shortPath = createMockState(2L, "ShortPath");
            State longPath1 = createMockState(3L, "LongPath1");
            State longPath2 = createMockState(4L, "LongPath2");
            State targetState = createMockState(5L, "Target");
            
            // Short path: Start -> ShortPath -> Target (score 2)
            // Long path: Start -> LongPath1 -> LongPath2 -> Target (score 5)
            shortPath.setPathScore(1);
            longPath1.setPathScore(2);
            longPath2.setPathScore(2);
            
            when(stateService.findById(2L)).thenReturn(Optional.of(shortPath));
            when(stateService.findById(3L)).thenReturn(Optional.of(longPath1));
            when(stateService.findById(4L)).thenReturn(Optional.of(longPath2));
            
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(1L)));
            when(stateTransitionsJointTable.getActivatedBy(4L)).thenReturn(new HashSet<>(Arrays.asList(3L)));
            when(stateTransitionsJointTable.getActivatedBy(5L)).thenReturn(new HashSet<>(Arrays.asList(2L, 4L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "State" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(startState), targetState);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().size() >= 2);
            
            // First path should be the one with better score (lower is better)
            Path bestPath = paths.getPaths().get(0);
            assertTrue(bestPath.getScore() <= paths.getPaths().get(1).getScore());
        }
    }
    
    @Nested
    @DisplayName("Multiple Start States")
    class MultipleStartStates {
        
        @Test
        @DisplayName("Should find paths from multiple start states")
        public void testMultipleStartStates() {
            State start1 = createMockState(1L, "Start1");
            State start2 = createMockState(2L, "Start2");
            State start3 = createMockState(3L, "Start3");
            State targetState = createMockState(4L, "Target");
            
            // All start states can reach target
            when(stateTransitionsJointTable.getActivatedBy(4L))
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            
            when(stateService.getStateName(1L)).thenReturn("Start1");
            when(stateService.getStateName(2L)).thenReturn("Start2");
            when(stateService.getStateName(3L)).thenReturn("Start3");
            when(stateService.getStateName(4L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(start1, start2, start3), targetState);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().size() >= 3);
            
            // Verify paths from each start state
            Set<Long> foundStartStates = new HashSet<>();
            for (Path path : paths.getPaths()) {
                foundStartStates.add(path.getStateTransitions().get(0));
            }
            
            assertTrue(foundStartStates.contains(1L));
            assertTrue(foundStartStates.contains(2L));
            assertTrue(foundStartStates.contains(3L));
        }
        
        @Test
        @DisplayName("Should handle mix of reachable and unreachable start states")
        public void testMixedReachability() {
            State reachable1 = createMockState(1L, "Reachable1");
            State reachable2 = createMockState(2L, "Reachable2");
            State unreachable = createMockState(3L, "Unreachable");
            State targetState = createMockState(4L, "Target");
            
            // Only states 1 and 2 can reach target
            when(stateTransitionsJointTable.getActivatedBy(4L))
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "State" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(reachable1, reachable2, unreachable), targetState);
            
            assertNotNull(paths);
            
            // Only paths from reachable states should be found
            for (Path path : paths.getPaths()) {
                Long startState = path.getStateTransitions().get(0);
                assertTrue(startState == 1L || startState == 2L);
                assertNotEquals(3L, startState);
            }
        }
    }
    
    @Nested
    @DisplayName("Cycle Prevention")
    class CyclePrevention {
        
        @Test
        @DisplayName("Should prevent infinite loops in circular paths")
        public void testPreventCircularPath() {
            State s1 = createMockState(1L, "S1");
            State s2 = createMockState(2L, "S2");
            State s3 = createMockState(3L, "S3");
            State targetState = createMockState(4L, "Target");
            
            // Create circular dependency: S1 -> S2 -> S3 -> S1
            // Plus path to target: S2 -> Target
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L, 3L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(2L)));
            when(stateTransitionsJointTable.getActivatedBy(1L)).thenReturn(new HashSet<>(Arrays.asList(3L)));
            when(stateTransitionsJointTable.getActivatedBy(4L)).thenReturn(new HashSet<>(Arrays.asList(2L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "S" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(s1), targetState);
            
            assertNotNull(paths);
            
            // Should find path without getting stuck in cycle
            for (Path path : paths.getPaths()) {
                // Path should not contain duplicates (no cycles)
                Set<Long> uniqueStates = new HashSet<>(path.getStateTransitions());
                assertEquals(uniqueStates.size(), path.getStateTransitions().size());
            }
        }
        
        @Test
        @DisplayName("Should handle complex graph with multiple cycles")
        public void testComplexGraphWithCycles() {
            // Create a complex graph with multiple interconnected cycles
            State s1 = createMockState(1L, "S1");
            State s2 = createMockState(2L, "S2");
            State s3 = createMockState(3L, "S3");
            State s4 = createMockState(4L, "S4");
            State s5 = createMockState(5L, "S5");
            State targetState = createMockState(6L, "Target");
            
            // Complex interconnections
            when(stateTransitionsJointTable.getActivatedBy(2L)).thenReturn(new HashSet<>(Arrays.asList(1L, 3L, 4L)));
            when(stateTransitionsJointTable.getActivatedBy(3L)).thenReturn(new HashSet<>(Arrays.asList(2L, 5L)));
            when(stateTransitionsJointTable.getActivatedBy(4L)).thenReturn(new HashSet<>(Arrays.asList(2L, 5L)));
            when(stateTransitionsJointTable.getActivatedBy(5L)).thenReturn(new HashSet<>(Arrays.asList(3L, 4L)));
            when(stateTransitionsJointTable.getActivatedBy(6L)).thenReturn(new HashSet<>(Arrays.asList(2L, 3L, 4L, 5L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "S" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(s1), targetState);
            
            assertNotNull(paths);
            
            // All paths should be valid without cycles
            for (Path path : paths.getPaths()) {
                Set<Long> visitedStates = new HashSet<>();
                for (Long stateId : path.getStateTransitions()) {
                    assertFalse(visitedStates.contains(stateId), "Path contains cycle at state " + stateId);
                    visitedStates.add(stateId);
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle empty start states")
        public void testEmptyStartStates() {
            State targetState = createMockState(1L, "Target");
            
            when(stateService.getStateName(1L)).thenReturn("Target");
            
            Paths paths = pathFinder.getPathsToState(new ArrayList<>(), targetState);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null target state")
        public void testNullTargetState() {
            State startState = createMockState(1L, "Start");
            
            assertThrows(NullPointerException.class, () -> 
                pathFinder.getPathsToState(Arrays.asList(startState), null)
            );
        }
        
        @Test
        @DisplayName("Should handle isolated states (no connections)")
        public void testIsolatedStates() {
            State isolated1 = createMockState(1L, "Isolated1");
            State isolated2 = createMockState(2L, "Isolated2");
            
            // No transitions between states
            when(stateTransitionsJointTable.getActivatedBy(anyLong())).thenReturn(new HashSet<>());
            
            when(stateService.getStateName(1L)).thenReturn("Isolated1");
            when(stateService.getStateName(2L)).thenReturn("Isolated2");
            
            Paths paths = pathFinder.getPathsToState(Arrays.asList(isolated1), isolated2);
            
            assertNotNull(paths);
            assertTrue(paths.getPaths().isEmpty());
        }
        
        @Test
        @DisplayName("Should use Set interface for start states")
        public void testSetBasedStartStates() {
            Set<Long> startStateIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            Long targetStateId = 4L;
            
            when(stateTransitionsJointTable.getActivatedBy(4L))
                .thenReturn(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
            
            when(stateService.getStateName(anyLong())).thenAnswer(invocation -> "State" + invocation.getArgument(0));
            
            Paths paths = pathFinder.getPathsToState(startStateIds, targetStateId);
            
            assertNotNull(paths);
            assertFalse(paths.getPaths().isEmpty());
        }
    }
    
    // Helper methods
    private State createMockState(Long id, String name) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.getPathScore()).thenReturn(1);
        return state;
    }
}