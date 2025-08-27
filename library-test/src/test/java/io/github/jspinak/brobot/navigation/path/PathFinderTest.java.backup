package io.github.jspinak.brobot.navigation.path;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for PathFinder class.
 * Tests path finding algorithms for state navigation.
 * 
 * NOTE: These tests have complex mock interactions with the PathFinder implementation.
 * Disabled as they require refactoring to match the actual implementation behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PathFinder Tests")
class PathFinderTest {
    
    @Mock
    private StateTransitionsJointTable stateTransitionsJointTable;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateTransitionService stateTransitionService;
    
    private PathFinder pathFinder;
    
    @BeforeEach
    void setUp() {
        pathFinder = new PathFinder(stateTransitionsJointTable, stateService, stateTransitionService);
    }
    
    @Nested
    @DisplayName("Basic Path Finding Tests")
    class BasicPathFindingTests {
        
        @Test
        @DisplayName("Should find direct path between two states")
        void testFindDirectPath() {
            // Setup states
            State startState = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(targetState.getId()).thenReturn(2L);
            when(startState.getPathScore()).thenReturn(10);
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("StartState");
            when(stateService.getStateName(2L)).thenReturn("TargetState");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState));
            when(stateService.getState(2L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(1L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(parentStates);
            
            StateTransition transition = mock(StateTransition.class);
            lenient().when(transition.getScore()).thenReturn(5);
            when(stateTransitionService.getTransition(1L, 2L)).thenReturn(Optional.of(transition));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(2, path.getStates().size());
            assertTrue(path.getStates().contains(1L));
            assertTrue(path.getStates().contains(2L));
            
            // Verify method calls
            verify(stateTransitionsJointTable).getStatesWithTransitionsTo(2L);
            verify(stateTransitionService).getTransition(1L, 2L);
        }
        
        @Test
        @DisplayName("Should find path through intermediate states")
        void testFindPathThroughIntermediateStates() {
            // Setup states
            State startState = mock(State.class);
            State intermediateState = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(intermediateState.getId()).thenReturn(2L);
            when(targetState.getId()).thenReturn(3L);
            when(startState.getPathScore()).thenReturn(10);
            when(intermediateState.getPathScore()).thenReturn(5);
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("StartState");
            when(stateService.getStateName(2L)).thenReturn("IntermediateState");
            when(stateService.getStateName(3L)).thenReturn("TargetState");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState));
            when(stateService.getState(2L)).thenReturn(Optional.of(intermediateState));
            when(stateService.getState(3L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions: 1 -> 2 -> 3
            Set<Long> parentsOfTarget = new HashSet<>();
            parentsOfTarget.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(3L)).thenReturn(parentsOfTarget);
            
            Set<Long> parentsOfIntermediate = new HashSet<>();
            parentsOfIntermediate.add(1L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(parentsOfIntermediate);
            
            StateTransition transition1 = mock(StateTransition.class);
            StateTransition transition2 = mock(StateTransition.class);
            lenient().when(transition1.getScore()).thenReturn(5);
            lenient().when(transition2.getScore()).thenReturn(5);
            when(stateTransitionService.getTransition(1L, 2L)).thenReturn(Optional.of(transition1));
            when(stateTransitionService.getTransition(2L, 3L)).thenReturn(Optional.of(transition2));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(3, path.getStates().size());
            assertEquals(List.of(1L, 2L, 3L), path.getStates());
        }
        
        @Test
        @DisplayName("Should handle no path found scenario")
        void testNoPathFound() {
            // Setup states
            State startState = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(targetState.getId()).thenReturn(2L);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("StartState");
            when(stateService.getStateName(2L)).thenReturn("TargetState");
            
            // No transitions to target state
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(new HashSet<>());
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify
            assertNotNull(paths);
            assertTrue(paths.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Multiple Path Tests")
    class MultiplePathTests {
        
        @Test
        @DisplayName("Should find multiple paths to target state")
        void testFindMultiplePaths() {
            // Setup states
            State startState1 = mock(State.class);
            State startState2 = mock(State.class);
            State targetState = mock(State.class);
            when(startState1.getId()).thenReturn(1L);
            when(startState2.getId()).thenReturn(2L);
            when(targetState.getId()).thenReturn(3L);
            when(startState1.getPathScore()).thenReturn(10);
            when(startState2.getPathScore()).thenReturn(15);
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("StartState1");
            when(stateService.getStateName(2L)).thenReturn("StartState2");
            when(stateService.getStateName(3L)).thenReturn("TargetState");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState1));
            when(stateService.getState(2L)).thenReturn(Optional.of(startState2));
            when(stateService.getState(3L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions: both start states can reach target
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(1L);
            parentStates.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(3L)).thenReturn(parentStates);
            
            StateTransition transition1 = mock(StateTransition.class);
            StateTransition transition2 = mock(StateTransition.class);
            lenient().when(transition1.getScore()).thenReturn(5);
            lenient().when(transition2.getScore()).thenReturn(3);
            when(stateTransitionService.getTransition(1L, 3L)).thenReturn(Optional.of(transition1));
            when(stateTransitionService.getTransition(2L, 3L)).thenReturn(Optional.of(transition2));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState1, startState2), targetState);
            
            // Verify
            assertNotNull(paths);
            assertEquals(2, paths.getPaths().size());
            
            // Verify both paths exist
            boolean hasPath1 = paths.getPaths().stream().anyMatch(p -> p.getStates().equals(List.of(1L, 3L)));
            boolean hasPath2 = paths.getPaths().stream().anyMatch(p -> p.getStates().equals(List.of(2L, 3L)));
            assertTrue(hasPath1);
            assertTrue(hasPath2);
        }
        
        @Test
        @DisplayName("Should find paths from multiple start states")
        void testMultipleStartStates() {
            // Setup
            Set<Long> startStateIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            Long targetStateId = 4L;
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("Start1");
            when(stateService.getStateName(2L)).thenReturn("Start2");
            when(stateService.getStateName(3L)).thenReturn("Start3");
            when(stateService.getStateName(4L)).thenReturn("Target");
            
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            State state4 = mock(State.class);
            when(state1.getPathScore()).thenReturn(10);
            when(state2.getPathScore()).thenReturn(10);
            when(state3.getPathScore()).thenReturn(10);
            when(state4.getPathScore()).thenReturn(10);
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getState(4L)).thenReturn(Optional.of(state4));
            
            // Only state 2 has transition to target
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(4L)).thenReturn(parentStates);
            
            StateTransition transition = mock(StateTransition.class);
            lenient().when(transition.getScore()).thenReturn(5);
            when(stateTransitionService.getTransition(2L, 4L)).thenReturn(Optional.of(transition));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(startStateIds, targetStateId);
            
            // Verify
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(List.of(2L, 4L), path.getStates());
        }
    }
    
    @Nested
    @DisplayName("Cycle Prevention Tests")
    class CyclePreventionTests {
        
        @Test
        @DisplayName("Should prevent cycles in path finding")
        void testPreventCycles() {
            // Setup states with cycle: 1 -> 2 -> 3 -> 2 (cycle)
            State startState = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            when(targetState.getId()).thenReturn(4L);
            when(startState.getPathScore()).thenReturn(10);
            when(state2.getPathScore()).thenReturn(10);
            when(state3.getPathScore()).thenReturn(10);
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("State2");
            when(stateService.getStateName(3L)).thenReturn("State3");
            when(stateService.getStateName(4L)).thenReturn("Target");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getState(4L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions with cycle
            Set<Long> parentsOfTarget = new HashSet<>();
            parentsOfTarget.add(3L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(4L)).thenReturn(parentsOfTarget);
            
            Set<Long> parentsOf3 = new HashSet<>();
            parentsOf3.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(3L)).thenReturn(parentsOf3);
            
            Set<Long> parentsOf2 = new HashSet<>();
            parentsOf2.add(1L);
            parentsOf2.add(3L); // Creates cycle
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(parentsOf2);
            
            StateTransition transition1 = mock(StateTransition.class);
            StateTransition transition2 = mock(StateTransition.class);
            StateTransition transition3 = mock(StateTransition.class);
            lenient().when(transition1.getScore()).thenReturn(5);
            lenient().when(transition2.getScore()).thenReturn(5);
            lenient().when(transition3.getScore()).thenReturn(5);
            when(stateTransitionService.getTransition(1L, 2L)).thenReturn(Optional.of(transition1));
            when(stateTransitionService.getTransition(2L, 3L)).thenReturn(Optional.of(transition2));
            when(stateTransitionService.getTransition(3L, 4L)).thenReturn(Optional.of(transition3));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify - should find path without infinite loop
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(List.of(1L, 2L, 3L, 4L), path.getStates());
            
            // Verify no duplicate states in path
            Set<Long> uniqueStates = new HashSet<>(path.getStates());
            assertEquals(path.getStates().size(), uniqueStates.size());
        }
    }
    
    @Nested
    @DisplayName("Path Scoring Tests")
    class PathScoringTests {
        
        @Test
        @DisplayName("Should calculate path score correctly")
        void testPathScoring() {
            // Setup states with different scores
            State startState = mock(State.class);
            State intermediateState = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(intermediateState.getId()).thenReturn(2L);
            when(targetState.getId()).thenReturn(3L);
            when(startState.getPathScore()).thenReturn(10);
            when(intermediateState.getPathScore()).thenReturn(20);
            when(targetState.getPathScore()).thenReturn(15);
            
            // Setup state names and states
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("Intermediate");
            when(stateService.getStateName(3L)).thenReturn("Target");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState));
            when(stateService.getState(2L)).thenReturn(Optional.of(intermediateState));
            when(stateService.getState(3L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions
            Set<Long> parentsOfTarget = new HashSet<>();
            parentsOfTarget.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(3L)).thenReturn(parentsOfTarget);
            
            Set<Long> parentsOfIntermediate = new HashSet<>();
            parentsOfIntermediate.add(1L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(parentsOfIntermediate);
            
            StateTransition transition1 = mock(StateTransition.class);
            StateTransition transition2 = mock(StateTransition.class);
            lenient().when(transition1.getScore()).thenReturn(5);
            lenient().when(transition2.getScore()).thenReturn(8);
            when(stateTransitionService.getTransition(1L, 2L)).thenReturn(Optional.of(transition1));
            when(stateTransitionService.getTransition(2L, 3L)).thenReturn(Optional.of(transition2));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            
            // Expected score: states (10 + 20 + 15) + transitions (5 + 8) = 58
            assertEquals(58, path.getScore());
        }
        
        @Test
        @DisplayName("Should sort paths by score")
        void testPathSorting() {
            // Setup two paths with different scores
            State start1 = mock(State.class);
            State start2 = mock(State.class);
            State targetState = mock(State.class);
            when(start1.getId()).thenReturn(1L);
            when(start2.getId()).thenReturn(2L);
            when(targetState.getId()).thenReturn(3L);
            when(start1.getPathScore()).thenReturn(5);  // Lower score path
            when(start2.getPathScore()).thenReturn(20); // Higher score path
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("Start1");
            when(stateService.getStateName(2L)).thenReturn("Start2");
            when(stateService.getStateName(3L)).thenReturn("Target");
            when(stateService.getState(1L)).thenReturn(Optional.of(start1));
            when(stateService.getState(2L)).thenReturn(Optional.of(start2));
            when(stateService.getState(3L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(1L);
            parentStates.add(2L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(3L)).thenReturn(parentStates);
            
            StateTransition transition1 = mock(StateTransition.class);
            StateTransition transition2 = mock(StateTransition.class);
            lenient().when(transition1.getScore()).thenReturn(3);
            lenient().when(transition2.getScore()).thenReturn(3);
            when(stateTransitionService.getTransition(1L, 3L)).thenReturn(Optional.of(transition1));
            when(stateTransitionService.getTransition(2L, 3L)).thenReturn(Optional.of(transition2));
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(start1, start2), targetState);
            
            // Verify paths are sorted by score
            assertNotNull(paths);
            assertEquals(2, paths.getPaths().size());
            
            // First path should have lower score (better)
            Path firstPath = paths.getPaths().get(0);
            Path secondPath = paths.getPaths().get(1);
            assertTrue(firstPath.getScore() <= secondPath.getScore());
        }
    }
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle empty start states")
        void testEmptyStartStates() {
            State targetState = mock(State.class);
            when(targetState.getId()).thenReturn(1L);
            when(stateService.getStateName(1L)).thenReturn("Target");
            
            // Execute
            Paths paths = pathFinder.getPathsToState(new ArrayList<>(), targetState);
            
            // Verify
            assertNotNull(paths);
            assertTrue(paths.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle self-loop transitions")
        void testSelfLoopTransition() {
            // Setup state with self-loop
            State state = mock(State.class);
            when(state.getId()).thenReturn(1L);
            when(state.getPathScore()).thenReturn(10);
            
            when(stateService.getStateName(1L)).thenReturn("State");
            when(stateService.getState(1L)).thenReturn(Optional.of(state));
            
            // State has transition to itself
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(1L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(1L)).thenReturn(parentStates);
            
            StateTransition selfTransition = mock(StateTransition.class);
            when(selfTransition.getScore()).thenReturn(5);
            when(stateTransitionService.getTransition(1L, 1L)).thenReturn(Optional.of(selfTransition));
            
            // Execute - find path from state to itself
            Paths paths = pathFinder.getPathsToState(List.of(state), state);
            
            // Verify - should find simple path without infinite loop
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(List.of(1L), path.getStates());
        }
        
        @Test
        @DisplayName("Should handle missing transition information")
        void testMissingTransitionInfo() {
            // Setup states
            State startState = mock(State.class);
            State targetState = mock(State.class);
            when(startState.getId()).thenReturn(1L);
            when(targetState.getId()).thenReturn(2L);
            when(startState.getPathScore()).thenReturn(10);
            when(targetState.getPathScore()).thenReturn(10);
            
            // Setup state names
            when(stateService.getStateName(1L)).thenReturn("Start");
            when(stateService.getStateName(2L)).thenReturn("Target");
            when(stateService.getState(1L)).thenReturn(Optional.of(startState));
            when(stateService.getState(2L)).thenReturn(Optional.of(targetState));
            
            // Setup transitions but no transition object
            Set<Long> parentStates = new HashSet<>();
            parentStates.add(1L);
            when(stateTransitionsJointTable.getStatesWithTransitionsTo(2L)).thenReturn(parentStates);
            
            // No transition object available
            when(stateTransitionService.getTransition(1L, 2L)).thenReturn(Optional.empty());
            
            // Execute
            Paths paths = pathFinder.getPathsToState(List.of(startState), targetState);
            
            // Verify - should still find path even without transition details
            assertNotNull(paths);
            assertEquals(1, paths.getPaths().size());
            Path path = paths.getPaths().get(0);
            assertEquals(List.of(1L, 2L), path.getStates());
            assertTrue(path.getTransitions().isEmpty());
        }
    }
}