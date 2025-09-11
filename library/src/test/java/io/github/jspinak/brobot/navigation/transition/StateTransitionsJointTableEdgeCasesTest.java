package io.github.jspinak.brobot.navigation.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Edge case and comprehensive tests for StateTransitionsJointTable. Tests graph management, dynamic
 * transitions, and complex scenarios.
 */
@DisplayName("StateTransitionsJointTable Edge Cases")
class StateTransitionsJointTableEdgeCasesTest extends BrobotTestBase {

    private StateTransitionsJointTable jointTable;

    @Mock private StateService mockStateService;

    @Mock private State mockState;

    @Mock private StateTransitions mockStateTransitions;

    @Mock private StateTransition mockTransition;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        jointTable = new StateTransitionsJointTable(mockStateService);
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should initialize with empty maps")
        void testInitialization() {
            assertNotNull(jointTable.getIncomingTransitions());
            assertNotNull(jointTable.getOutgoingTransitions());
            assertNotNull(jointTable.getIncomingTransitionsToPREVIOUS());
            assertTrue(jointTable.getIncomingTransitions().isEmpty());
            assertTrue(jointTable.getOutgoingTransitions().isEmpty());
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().isEmpty());
        }

        @Test
        @DisplayName("Should clear all repos")
        void testEmptyRepos() {
            // Add some data first
            jointTable.add(1L, 2L);
            jointTable.add(3L, 4L);
            assertFalse(jointTable.getIncomingTransitions().isEmpty());
            assertFalse(jointTable.getOutgoingTransitions().isEmpty());

            // Clear repos
            jointTable.emptyRepos();

            // All should be empty
            assertTrue(jointTable.getIncomingTransitions().isEmpty());
            assertTrue(jointTable.getOutgoingTransitions().isEmpty());
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().isEmpty());
        }

        @Test
        @DisplayName("Should add single transition")
        void testAddSingleTransition() {
            jointTable.add(100L, 200L);

            // Check incoming
            assertTrue(jointTable.getIncomingTransitions().containsKey(100L));
            assertTrue(jointTable.getIncomingTransitions().get(100L).contains(200L));

            // Check outgoing
            assertTrue(jointTable.getOutgoingTransitions().containsKey(200L));
            assertTrue(jointTable.getOutgoingTransitions().get(200L).contains(100L));
        }

        @Test
        @DisplayName("Should not add incoming for PREVIOUS state")
        void testNoIncomingForPreviousState() {
            Long previousId = SpecialStateType.PREVIOUS.getId();
            jointTable.add(100L, previousId);

            // Should not have incoming transition from PREVIOUS
            assertFalse(jointTable.getIncomingTransitions().containsKey(100L));

            // Should have outgoing transition from PREVIOUS
            assertTrue(jointTable.getOutgoingTransitions().containsKey(previousId));
            assertTrue(jointTable.getOutgoingTransitions().get(previousId).contains(100L));
        }
    }

    @Nested
    @DisplayName("Hidden State Management")
    class HiddenStateManagement {

        @Test
        @DisplayName("Should add transitions to hidden states")
        void testAddTransitionsToHiddenStates() {
            Set<Long> hiddenStates = new HashSet<>(Arrays.asList(10L, 20L, 30L));
            when(mockState.getHiddenStateIds()).thenReturn(hiddenStates);
            when(mockState.getId()).thenReturn(100L);

            jointTable.addTransitionsToHiddenStates(mockState);

            // Check each hidden state has transition from active state
            for (Long hiddenId : hiddenStates) {
                assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().containsKey(hiddenId));
                assertTrue(
                        jointTable.getIncomingTransitionsToPREVIOUS().get(hiddenId).contains(100L));
            }
        }

        @Test
        @DisplayName("Should handle multiple states hiding same state")
        void testMultipleStatesHidingSameState() {
            State state1 = mock(State.class);
            when(state1.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(10L)));
            when(state1.getId()).thenReturn(100L);

            State state2 = mock(State.class);
            when(state2.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(10L)));
            when(state2.getId()).thenReturn(200L);

            jointTable.addTransitionsToHiddenStates(state1);
            jointTable.addTransitionsToHiddenStates(state2);

            // Hidden state 10L should be accessible from both states
            Set<Long> accessibleFrom = jointTable.getIncomingTransitionsToPREVIOUS().get(10L);
            assertEquals(2, accessibleFrom.size());
            assertTrue(accessibleFrom.contains(100L));
            assertTrue(accessibleFrom.contains(200L));
        }

        @Test
        @DisplayName("Should remove transitions to hidden states")
        void testRemoveTransitionsToHiddenStates() {
            Set<Long> hiddenStates = new HashSet<>(Arrays.asList(10L, 20L));
            when(mockState.getHiddenStateIds()).thenReturn(hiddenStates);
            when(mockState.getId()).thenReturn(100L);

            // Add transitions
            jointTable.addTransitionsToHiddenStates(mockState);
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().get(10L).contains(100L));

            // Remove transitions
            jointTable.removeTransitionsToHiddenStates(mockState);

            // Should no longer contain the transition
            assertFalse(jointTable.getIncomingTransitionsToPREVIOUS().get(10L).contains(100L));
            assertFalse(jointTable.getIncomingTransitionsToPREVIOUS().get(20L).contains(100L));
        }

        @Test
        @DisplayName("Should handle removing non-existent hidden state transitions")
        void testRemoveNonExistentHiddenTransitions() {
            Set<Long> hiddenStates = new HashSet<>(Arrays.asList(10L, 20L));
            when(mockState.getHiddenStateIds()).thenReturn(hiddenStates);
            when(mockState.getId()).thenReturn(100L);

            // Remove without adding first - should not throw
            assertDoesNotThrow(() -> jointTable.removeTransitionsToHiddenStates(mockState));

            // Map should still be empty
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().isEmpty());
        }

        @Test
        @DisplayName("Should handle empty hidden states set")
        void testEmptyHiddenStates() {
            when(mockState.getHiddenStateIds()).thenReturn(new HashSet<>());
            when(mockState.getId()).thenReturn(100L);

            jointTable.addTransitionsToHiddenStates(mockState);

            // Should not add any transitions
            assertTrue(jointTable.getIncomingTransitionsToPREVIOUS().isEmpty());
        }
    }

    @Nested
    @DisplayName("StateTransitions Processing")
    class StateTransitionsProcessing {

        @Test
        @DisplayName("Should add all transitions from StateTransitions")
        void testAddToJointTable() {
            // Setup multiple transitions
            StateTransition transition1 = mock(StateTransition.class);
            when(transition1.getActivate()).thenReturn(new HashSet<>(Arrays.asList(10L, 20L)));

            StateTransition transition2 = mock(StateTransition.class);
            when(transition2.getActivate()).thenReturn(new HashSet<>(Arrays.asList(30L)));

            List<StateTransition> transitions = Arrays.asList(transition1, transition2);
            when(mockStateTransitions.getTransitions()).thenReturn(transitions);
            when(mockStateTransitions.getStateId()).thenReturn(100L);

            jointTable.addToJointTable(mockStateTransitions);

            // Verify all transitions added
            assertTrue(jointTable.getIncomingTransitions().get(10L).contains(100L));
            assertTrue(jointTable.getIncomingTransitions().get(20L).contains(100L));
            assertTrue(jointTable.getIncomingTransitions().get(30L).contains(100L));

            assertTrue(jointTable.getOutgoingTransitions().get(100L).contains(10L));
            assertTrue(jointTable.getOutgoingTransitions().get(100L).contains(20L));
            assertTrue(jointTable.getOutgoingTransitions().get(100L).contains(30L));
        }

        @Test
        @DisplayName("Should handle empty transitions list")
        void testEmptyTransitionsList() {
            when(mockStateTransitions.getTransitions()).thenReturn(new ArrayList<>());
            when(mockStateTransitions.getStateId()).thenReturn(100L);

            jointTable.addToJointTable(mockStateTransitions);

            // Should not add any transitions
            assertTrue(jointTable.getIncomingTransitions().isEmpty());
            assertTrue(jointTable.getOutgoingTransitions().isEmpty());
        }

        @Test
        @DisplayName("Should handle transitions with empty activate sets")
        void testTransitionsWithEmptyActivateSets() {
            StateTransition transition = mock(StateTransition.class);
            when(transition.getActivate()).thenReturn(new HashSet<>());

            when(mockStateTransitions.getTransitions()).thenReturn(Arrays.asList(transition));
            when(mockStateTransitions.getStateId()).thenReturn(100L);

            jointTable.addToJointTable(mockStateTransitions);

            // Should not add any transitions
            assertTrue(jointTable.getIncomingTransitions().isEmpty());
            assertTrue(jointTable.getOutgoingTransitions().isEmpty());
        }
    }

    @Nested
    @DisplayName("Query Operations")
    class QueryOperations {

        @BeforeEach
        void setupTransitions() {
            // Create a simple graph: 1->2, 1->3, 2->3, 3->4
            jointTable.add(2L, 1L);
            jointTable.add(3L, 1L);
            jointTable.add(3L, 2L);
            jointTable.add(4L, 3L);
        }

        @Test
        @DisplayName("Should find states with transitions to target")
        void testGetStatesWithTransitionsTo() {
            Set<Long> parents = jointTable.getStatesWithTransitionsTo(3L);

            assertEquals(2, parents.size());
            assertTrue(parents.contains(1L));
            assertTrue(parents.contains(2L));
        }

        @Test
        @DisplayName("Should find states with transitions to multiple targets")
        void testGetStatesWithTransitionsToMultiple() {
            Set<Long> parents = jointTable.getStatesWithTransitionsTo(2L, 4L);

            assertEquals(2, parents.size());
            assertTrue(parents.contains(1L)); // Can reach 2
            assertTrue(parents.contains(3L)); // Can reach 4
        }

        @Test
        @DisplayName("Should include hidden state transitions in query")
        void testGetStatesWithTransitionsIncludesHidden() {
            // Add hidden state transition
            when(mockState.getHiddenStateIds()).thenReturn(new HashSet<>(Arrays.asList(5L)));
            when(mockState.getId()).thenReturn(10L);
            jointTable.addTransitionsToHiddenStates(mockState);

            Set<Long> parents = jointTable.getStatesWithTransitionsTo(5L);

            assertEquals(1, parents.size());
            assertTrue(parents.contains(10L));
        }

        @Test
        @DisplayName("Should return empty set for unknown state")
        void testGetStatesWithTransitionsToUnknown() {
            Set<Long> parents = jointTable.getStatesWithTransitionsTo(999L);

            assertNotNull(parents);
            assertTrue(parents.isEmpty());
        }

        @Test
        @DisplayName("Should find states with transitions from source")
        void testGetStatesWithTransitionsFrom() {
            Set<Long> children = jointTable.getStatesWithTransitionsFrom(1L);

            assertEquals(2, children.size());
            assertTrue(children.contains(2L));
            assertTrue(children.contains(3L));
        }

        @Test
        @DisplayName("Should find states with transitions from multiple sources")
        void testGetStatesWithTransitionsFromMultiple() {
            Set<Long> children = jointTable.getStatesWithTransitionsFrom(1L, 3L);

            assertEquals(3, children.size());
            assertTrue(children.contains(2L)); // From 1
            assertTrue(children.contains(3L)); // From 1
            assertTrue(children.contains(4L)); // From 3
        }

        @Test
        @DisplayName("Should return empty set for state with no outgoing transitions")
        void testGetStatesWithTransitionsFromLeaf() {
            Set<Long> children = jointTable.getStatesWithTransitionsFrom(4L);

            assertNotNull(children);
            assertTrue(children.isEmpty());
        }
    }

    @Nested
    @DisplayName("Complex Graph Scenarios")
    class ComplexGraphScenarios {

        @Test
        @DisplayName("Should handle self-transitions")
        void testSelfTransitions() {
            jointTable.add(100L, 100L);

            // Should appear in both incoming and outgoing
            assertTrue(jointTable.getIncomingTransitions().get(100L).contains(100L));
            assertTrue(jointTable.getOutgoingTransitions().get(100L).contains(100L));

            // Should be found in queries
            Set<Long> parents = jointTable.getStatesWithTransitionsTo(100L);
            assertTrue(parents.contains(100L));

            Set<Long> children = jointTable.getStatesWithTransitionsFrom(100L);
            assertTrue(children.contains(100L));
        }

        @Test
        @DisplayName("Should handle bidirectional transitions")
        void testBidirectionalTransitions() {
            jointTable.add(100L, 200L); // 200 -> 100
            jointTable.add(200L, 100L); // 100 -> 200

            // Both should have each other as incoming and outgoing
            assertTrue(jointTable.getIncomingTransitions().get(100L).contains(200L));
            assertTrue(jointTable.getIncomingTransitions().get(200L).contains(100L));
            assertTrue(jointTable.getOutgoingTransitions().get(100L).contains(200L));
            assertTrue(jointTable.getOutgoingTransitions().get(200L).contains(100L));
        }

        @Test
        @DisplayName("Should handle large graph efficiently")
        void testLargeGraph() {
            // Create a large graph with 1000 nodes
            for (long i = 0; i < 1000; i++) {
                jointTable.add(i + 1, i); // Chain: 0->1->2->...->999
                if (i % 10 == 0 && i + 100 < 1000) {
                    jointTable.add(i + 100, i); // Every 10th node has extra connection
                }
            }

            // Verify structure - incoming has main chain plus extra connections
            assertTrue(jointTable.getIncomingTransitions().size() >= 1000);
            assertTrue(jointTable.getOutgoingTransitions().size() >= 100);

            // Check specific patterns
            Set<Long> parentsOf500 = jointTable.getStatesWithTransitionsTo(500L);
            assertTrue(parentsOf500.contains(499L));

            Set<Long> childrenOf100 = jointTable.getStatesWithTransitionsFrom(100L);
            assertTrue(childrenOf100.contains(101L));
            assertTrue(childrenOf100.contains(200L)); // Extra connection
        }

        @Test
        @DisplayName("Should handle duplicate additions correctly")
        void testDuplicateAdditions() {
            jointTable.add(100L, 200L);
            jointTable.add(100L, 200L); // Duplicate
            jointTable.add(100L, 200L); // Another duplicate

            // Should only have one entry
            assertEquals(1, jointTable.getIncomingTransitions().get(100L).size());
            assertEquals(1, jointTable.getOutgoingTransitions().get(200L).size());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundaries")
    class EdgeCasesAndBoundaries {

        @Test
        @DisplayName("Should handle Long.MAX_VALUE as state ID")
        void testMaxLongStateId() {
            Long maxId = Long.MAX_VALUE;
            jointTable.add(maxId, 1L);
            jointTable.add(1L, maxId);

            assertTrue(jointTable.getIncomingTransitions().containsKey(maxId));
            assertTrue(jointTable.getOutgoingTransitions().containsKey(maxId));
        }

        @Test
        @DisplayName("Should handle negative state IDs")
        void testNegativeStateIds() {
            jointTable.add(-100L, -200L);

            assertTrue(jointTable.getIncomingTransitions().containsKey(-100L));
            assertTrue(jointTable.getOutgoingTransitions().containsKey(-200L));
        }

        @Test
        @DisplayName("Should handle null state service gracefully")
        void testNullStateService() {
            // Create joint table with null state service
            StateTransitionsJointTable tableWithNullService = new StateTransitionsJointTable(null);

            // Should still function for basic operations
            assertDoesNotThrow(() -> tableWithNullService.add(1L, 2L));
            assertNotNull(tableWithNullService.getIncomingTransitions());
        }

        @Test
        @DisplayName("Should handle concurrent modifications")
        void testConcurrentModifications() throws InterruptedException {
            List<Thread> threads = new ArrayList<>();

            // Create multiple threads adding transitions
            for (int i = 0; i < 10; i++) {
                final int threadId = i;
                Thread thread =
                        new Thread(
                                () -> {
                                    for (int j = 0; j < 100; j++) {
                                        jointTable.add(
                                                (long) (threadId * 100 + j), (long) threadId);
                                    }
                                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Verify all transitions were added
            for (int i = 0; i < 10; i++) {
                assertTrue(jointTable.getOutgoingTransitions().containsKey((long) i));
                assertEquals(100, jointTable.getOutgoingTransitions().get((long) i).size());
            }
        }
    }
}
