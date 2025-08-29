package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateTransitionsJointTable.
 * Tests the state transition graph management and navigation queries.
 */
@DisplayName("StateTransitionsJointTable Tests")
class StateTransitionsJointTableTest extends BrobotTestBase {
    
    @Mock
    private StateService stateService;
    
    @Mock
    private State mockState;
    
    private StateTransitionsJointTable jointTable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        jointTable = new StateTransitionsJointTable(stateService);
        
        // Setup default state service behavior
        when(stateService.getStateName(1L)).thenReturn("State1");
        when(stateService.getStateName(2L)).thenReturn("State2");
        when(stateService.getStateName(3L)).thenReturn("State3");
        when(stateService.getStateName(4L)).thenReturn("State4");
        when(stateService.getStateName(5L)).thenReturn("State5");
    }
    
    @Nested
    @DisplayName("Basic Transition Management")
    class BasicTransitionManagement {
        
        @Test
        @DisplayName("Should add single transition")
        void testAddSingleTransition() {
            // Act
            jointTable.add(2L, 1L);
            
            // Assert
            assertTrue(jointTable.getIncomingTransitions().containsKey(2L));
            assertTrue(jointTable.getIncomingTransitions().get(2L).contains(1L));
            assertTrue(jointTable.getOutgoingTransitions().containsKey(1L));
            assertTrue(jointTable.getOutgoingTransitions().get(1L).contains(2L));
        }
        
        @Test
        @DisplayName("Should add multiple transitions from same source")
        void testAddMultipleTransitionsFromSameSource() {
            // Act
            jointTable.add(2L, 1L);
            jointTable.add(3L, 1L);
            jointTable.add(4L, 1L);
            
            // Assert
            Set<Long> outgoing = jointTable.getOutgoingTransitions().get(1L);
            assertNotNull(outgoing);
            assertEquals(3, outgoing.size());
            assertTrue(outgoing.containsAll(Arrays.asList(2L, 3L, 4L)));
        }
        
        @Test
        @DisplayName("Should add multiple transitions to same target")
        void testAddMultipleTransitionsToSameTarget() {
            // Act
            jointTable.add(3L, 1L);
            jointTable.add(3L, 2L);
            jointTable.add(3L, 4L);
            
            // Assert
            Set<Long> incoming = jointTable.getIncomingTransitions().get(3L);
            assertNotNull(incoming);
            assertEquals(3, incoming.size());
            assertTrue(incoming.containsAll(Arrays.asList(1L, 2L, 4L)));
        }
        
        @Test
        @DisplayName("Should handle PREVIOUS state transitions specially")
        void testPreviousStateTransitions() {
            // Act
            jointTable.add(2L, SpecialStateType.PREVIOUS.getId());
            
            // Assert
            // PREVIOUS should not appear in incoming transitions
            assertFalse(jointTable.getIncomingTransitions().containsKey(2L) &&
                       jointTable.getIncomingTransitions().get(2L).contains(SpecialStateType.PREVIOUS.getId()));
            
            // But should appear in outgoing transitions
            assertTrue(jointTable.getOutgoingTransitions().containsKey(SpecialStateType.PREVIOUS.getId()));
            assertTrue(jointTable.getOutgoingTransitions().get(SpecialStateType.PREVIOUS.getId()).contains(2L));
        }
        
        @Test
        @DisplayName("Should clear all transitions when emptying repos")
        void testEmptyRepos() {
            // Arrange
            jointTable.add(2L, 1L);
            jointTable.add(3L, 2L);
            jointTable.addTransitionsToHiddenStates(mockState);
            
            // Act
            jointTable.emptyRepos();
            
            // Assert
            assertTrue(jointTable.getIncomingTransitions().isEmpty());
            assertTrue(jointTable.getOutgoingTransitions().isEmpty());
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("StateTransitions Integration")
    class StateTransitionsIntegration {
        
        @Test
        @DisplayName("Should add all transitions from StateTransitions object")
        void testAddToJointTable() {
            // Arrange
            StateTransitions transitions = new StateTransitions();
            transitions.setStateId(1L);
            transitions.setStateName("State1");
            
            TaskSequenceStateTransition transition1 = new TaskSequenceStateTransition();
            transition1.setActivate(new HashSet<>(Arrays.asList(2L, 3L)));
            transitions.getTransitions().add(transition1);
            
            TaskSequenceStateTransition transition2 = new TaskSequenceStateTransition();
            transition2.setActivate(new HashSet<>(Arrays.asList(4L)));
            transitions.getTransitions().add(transition2);
            
            // Act
            jointTable.addToJointTable(transitions);
            
            // Assert
            Set<Long> outgoing = jointTable.getOutgoingTransitions().get(1L);
            assertNotNull(outgoing);
            assertEquals(3, outgoing.size());
            assertTrue(outgoing.containsAll(Arrays.asList(2L, 3L, 4L)));
            
            assertTrue(jointTable.getIncomingTransitions().get(2L).contains(1L));
            assertTrue(jointTable.getIncomingTransitions().get(3L).contains(1L));
            assertTrue(jointTable.getIncomingTransitions().get(4L).contains(1L));
        }
        
        @Test
        @DisplayName("Should handle empty transitions list")
        void testAddEmptyTransitions() {
            // Arrange
            StateTransitions transitions = new StateTransitions();
            transitions.setStateId(1L);
            
            // Act
            jointTable.addToJointTable(transitions);
            
            // Assert
            assertFalse(jointTable.getOutgoingTransitions().containsKey(1L));
        }
    }
    
    @Nested
    @DisplayName("Hidden State Management")
    class HiddenStateManagement {
        
        @Test
        @DisplayName("Should add transitions to hidden states")
        void testAddTransitionsToHiddenStates() {
            // Arrange
            when(mockState.getId()).thenReturn(1L);
            when(mockState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));
            
            // Act
            jointTable.addTransitionsToHiddenStates(mockState);
            
            // Assert
            Map<Long, Set<Long>> previousTransitions = jointTable.getIncomingTransitionsToPREVIOUS();
            assertTrue(previousTransitions.containsKey(2L));
            assertTrue(previousTransitions.containsKey(3L));
            assertTrue(previousTransitions.get(2L).contains(1L));
            assertTrue(previousTransitions.get(3L).contains(1L));
        }
        
        @Test
        @DisplayName("Should remove transitions to hidden states")
        void testRemoveTransitionsToHiddenStates() {
            // Arrange
            when(mockState.getId()).thenReturn(1L);
            when(mockState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));
            
            // First add transitions
            jointTable.addTransitionsToHiddenStates(mockState);
            
            // Act
            jointTable.removeTransitionsToHiddenStates(mockState);
            
            // Assert
            Map<Long, Set<Long>> previousTransitions = jointTable.getIncomingTransitionsToPREVIOUS();
            if (previousTransitions.containsKey(2L)) {
                assertFalse(previousTransitions.get(2L).contains(1L));
            }
            if (previousTransitions.containsKey(3L)) {
                assertFalse(previousTransitions.get(3L).contains(1L));
            }
        }
        
        @Test
        @DisplayName("Should handle multiple states hiding same state")
        void testMultipleStatesHidingSameState() {
            // Arrange
            State state1 = mock(State.class);
            when(state1.getId()).thenReturn(1L);
            when(state1.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(3L)));
            
            State state2 = mock(State.class);
            when(state2.getId()).thenReturn(2L);
            when(state2.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(3L)));
            
            // Act
            jointTable.addTransitionsToHiddenStates(state1);
            jointTable.addTransitionsToHiddenStates(state2);
            
            // Assert
            Set<Long> hidingStates = jointTable.getIncomingTransitionsToPREVIOUS().get(3L);
            assertNotNull(hidingStates);
            assertEquals(2, hidingStates.size());
            assertTrue(hidingStates.containsAll(Arrays.asList(1L, 2L)));
        }
    }
    
    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {
        
        @BeforeEach
        void setupTransitions() {
            // Create a sample graph:
            // 1 -> 2, 3
            // 2 -> 3, 4
            // 3 -> 4
            jointTable.add(2L, 1L);
            jointTable.add(3L, 1L);
            jointTable.add(3L, 2L);
            jointTable.add(4L, 2L);
            jointTable.add(4L, 3L);
        }
        
        @Test
        @DisplayName("Should find states with transitions to target")
        void testGetStatesWithTransitionsTo() {
            // Act
            Set<Long> parentsOf3 = jointTable.getStatesWithTransitionsTo(3L);
            Set<Long> parentsOf4 = jointTable.getStatesWithTransitionsTo(4L);
            
            // Assert
            assertEquals(2, parentsOf3.size());
            assertTrue(parentsOf3.containsAll(Arrays.asList(1L, 2L)));
            
            assertEquals(2, parentsOf4.size());
            assertTrue(parentsOf4.containsAll(Arrays.asList(2L, 3L)));
        }
        
        @Test
        @DisplayName("Should find states with transitions to multiple targets")
        void testGetStatesWithTransitionsToMultiple() {
            // Act
            Set<Long> parents = jointTable.getStatesWithTransitionsTo(2L, 3L, 4L);
            
            // Assert
            assertEquals(3, parents.size());
            assertTrue(parents.containsAll(Arrays.asList(1L, 2L, 3L)));
        }
        
        @Test
        @DisplayName("Should find states with transitions from source")
        void testGetStatesWithTransitionsFrom() {
            // Act
            Set<Long> childrenOf1 = jointTable.getStatesWithTransitionsFrom(1L);
            Set<Long> childrenOf2 = jointTable.getStatesWithTransitionsFrom(2L);
            
            // Assert
            assertEquals(2, childrenOf1.size());
            assertTrue(childrenOf1.containsAll(Arrays.asList(2L, 3L)));
            
            assertEquals(2, childrenOf2.size());
            assertTrue(childrenOf2.containsAll(Arrays.asList(3L, 4L)));
        }
        
        @Test
        @DisplayName("Should find states with transitions from multiple sources")
        void testGetStatesWithTransitionsFromMultiple() {
            // Act
            Set<Long> children = jointTable.getStatesWithTransitionsFrom(
                new HashSet<>(Arrays.asList(1L, 2L)));
            
            // Assert
            assertEquals(3, children.size());
            assertTrue(children.containsAll(Arrays.asList(2L, 3L, 4L)));
        }
        
        @Test
        @DisplayName("Should return empty set for non-existent states")
        void testNonExistentStates() {
            // Act
            Set<Long> parentsOf99 = jointTable.getStatesWithTransitionsTo(99L);
            Set<Long> childrenOf99 = jointTable.getStatesWithTransitionsFrom(99L);
            
            // Assert
            assertTrue(parentsOf99.isEmpty());
            assertTrue(childrenOf99.isEmpty());
        }
        
        @Test
        @DisplayName("Should include PREVIOUS transitions in queries")
        void testQueryWithPreviousTransitions() {
            // Arrange
            State activeState = mock(State.class);
            when(activeState.getId()).thenReturn(5L);
            when(activeState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L)));
            
            jointTable.addTransitionsToHiddenStates(activeState);
            
            // Act
            Set<Long> parentsOf2 = jointTable.getStatesWithTransitionsTo(2L);
            
            // Assert
            assertEquals(2, parentsOf2.size());
            assertTrue(parentsOf2.containsAll(Arrays.asList(1L, 5L))); // 1L from regular, 5L from PREVIOUS
        }
    }
    
    @Nested
    @DisplayName("Combined Transitions")
    class CombinedTransitions {
        
        @Test
        @DisplayName("Should merge incoming transitions with hidden transitions")
        void testGetIncomingTransitionsWithHiddenTransitions() {
            // Arrange
            jointTable.add(2L, 1L);
            jointTable.add(3L, 2L);
            
            State activeState = mock(State.class);
            when(activeState.getId()).thenReturn(4L);
            when(activeState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));
            jointTable.addTransitionsToHiddenStates(activeState);
            
            // Act
            Map<Long, Set<Long>> combined = jointTable.getIncomingTransitionsWithHiddenTransitions();
            
            // Assert
            assertTrue(combined.containsKey(2L));
            assertEquals(2, combined.get(2L).size());
            assertTrue(combined.get(2L).containsAll(Arrays.asList(1L, 4L)));
            
            assertTrue(combined.containsKey(3L));
            assertEquals(2, combined.get(3L).size());
            assertTrue(combined.get(3L).containsAll(Arrays.asList(2L, 4L)));
        }
        
        @Test
        @DisplayName("Should handle overlapping static and dynamic transitions")
        void testOverlappingTransitions() {
            // Arrange
            jointTable.add(2L, 1L);
            
            State activeState = mock(State.class);
            when(activeState.getId()).thenReturn(1L); // Same state
            when(activeState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L)));
            jointTable.addTransitionsToHiddenStates(activeState);
            
            // Act
            Map<Long, Set<Long>> combined = jointTable.getIncomingTransitionsWithHiddenTransitions();
            
            // Assert
            assertTrue(combined.containsKey(2L));
            assertEquals(1, combined.get(2L).size()); // Should not duplicate
            assertTrue(combined.get(2L).contains(1L));
        }
    }
    
    @Nested
    @DisplayName("Formatting and Display")
    class FormattingAndDisplay {
        
        @Test
        @DisplayName("Should format state name and ID correctly")
        void testFormatStateNameAndId() {
            // Arrange
            jointTable.add(2L, 1L);
            
            // Act
            String output = jointTable.print();
            
            // Assert
            assertNotNull(output);
            assertTrue(output.contains("StateTransitionsJointTable"));
            assertTrue(output.contains("incoming transitions"));
            assertTrue(output.contains("outgoing transitions"));
        }
        
        @Test
        @DisplayName("Should print empty table correctly")
        void testPrintEmptyTable() {
            // Act
            String output = jointTable.print();
            
            // Assert
            assertNotNull(output);
            assertTrue(output.contains("StateTransitionsJointTable"));
        }
        
        @Test
        @DisplayName("Should print complex table with all transition types")
        void testPrintComplexTable() {
            // Arrange
            jointTable.add(2L, 1L);
            jointTable.add(3L, 2L);
            
            State activeState = mock(State.class);
            when(activeState.getId()).thenReturn(4L);
            when(activeState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(2L)));
            jointTable.addTransitionsToHiddenStates(activeState);
            
            // Act
            String output = jointTable.print();
            
            // Assert
            assertNotNull(output);
            assertTrue(output.contains("incoming transitions"));
            assertTrue(output.contains("outgoing transitions"));
            assertTrue(output.contains("PREVIOUS"));
        }
    }
}