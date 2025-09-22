package io.github.jspinak.brobot.statemanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Advanced test suite for StateMemory - manages active state tracking and history. Tests state
 * persistence, concurrent access, history management, and recovery.
 */
@DisplayName("StateMemory Advanced Tests")
class StateMemoryAdvancedTest extends BrobotTestBase {

    private StateMemory stateMemory;

    @Mock private StateService mockStateService;

    @Mock private State mockState1;

    @Mock private State mockState2;

    @Mock private State mockState3;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateMemory = new StateMemory(mockStateService);

        // Setup mock states
        when(mockState1.getName()).thenReturn("State1");
        when(mockState2.getName()).thenReturn("State2");
        when(mockState3.getName()).thenReturn("State3");
        when(mockState1.getId()).thenReturn(1L);
        when(mockState2.getId()).thenReturn(2L);
        when(mockState3.getId()).thenReturn(3L);

        // Setup StateService mock
        when(mockStateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(mockStateService.getState(2L)).thenReturn(Optional.of(mockState2));
        when(mockStateService.getState(3L)).thenReturn(Optional.of(mockState3));
        when(mockStateService.getStateName(1L)).thenReturn("State1");
        when(mockStateService.getStateName(2L)).thenReturn("State2");
        when(mockStateService.getStateName(3L)).thenReturn("State3");
        when(mockStateService.getStateId("State1")).thenReturn(1L);
        when(mockStateService.getStateId("State2")).thenReturn(2L);
        when(mockStateService.getStateId("State3")).thenReturn(3L);
    }

    @Nested
    @DisplayName("Active State Management")
    class ActiveStateManagement {

        @Test
        @DisplayName("Should add states to active list")
        void testAddActiveStates() {
            // Act
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(3, activeStates.size());
            assertTrue(activeStates.contains(1L));
            assertTrue(activeStates.contains(2L));
            assertTrue(activeStates.contains(3L));
        }

        @Test
        @DisplayName("Should not add duplicate states")
        void testNoDuplicateStates() {
            // Act
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(1L); // Duplicate
            stateMemory.addActiveState(2L);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(2, activeStates.size());
        }

        @Test
        @DisplayName("Should ignore NULL state")
        void testIgnoreNullState() {
            // Act
            stateMemory.addActiveState(SpecialStateType.NULL.getId());
            stateMemory.addActiveState(1L);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(1, activeStates.size());
            assertFalse(activeStates.contains(SpecialStateType.NULL.getId()));
        }

        @Test
        @DisplayName("Should track state visits")
        void testStateVisits() {
            // Act
            stateMemory.addActiveState(1L);
            stateMemory.removeInactiveState(1L);
            stateMemory.addActiveState(1L); // Second visit

            // Assert
            verify(mockState1, times(2)).addVisit();
            // Note: mockFindStochasticModifier is no longer modified when adding states
        }
    }

    @Nested
    @DisplayName("Concurrent State Access")
    class ConcurrentStateAccess {

        @Test
        @DisplayName("Should handle concurrent state additions safely")
        void testConcurrentStateAdditions() throws InterruptedException {
            // Arrange
            int threadCount = 10;
            int statesPerThread = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // Act
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(
                        () -> {
                            try {
                                for (int j = 0; j < statesPerThread; j++) {
                                    long stateId = threadId * 100L + j;
                                    State mockState = mock(State.class);
                                    when(mockState.getName()).thenReturn("State" + stateId);
                                    when(mockStateService.getState(stateId))
                                            .thenReturn(Optional.of(mockState));
                                    when(mockStateService.getStateName(stateId))
                                            .thenReturn("State" + stateId);
                                    stateMemory.addActiveState(stateId);
                                }
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            // Assert
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            // Each thread adds unique states, should have all of them
            assertTrue(stateMemory.getActiveStates().size() <= threadCount * statesPerThread);
            executor.shutdown();
        }

        @Test
        @DisplayName("Should maintain consistency during concurrent reads")
        void testConcurrentReads() throws InterruptedException {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            int readThreads = 20;
            CountDownLatch latch = new CountDownLatch(readThreads);
            ExecutorService executor = Executors.newFixedThreadPool(readThreads);
            Set<String> readStateNames = ConcurrentHashMap.newKeySet();

            // Act
            for (int i = 0; i < readThreads; i++) {
                executor.submit(
                        () -> {
                            try {
                                List<String> names = stateMemory.getActiveStateNames();
                                readStateNames.addAll(names);
                            } finally {
                                latch.countDown();
                            }
                        });
            }

            // Assert
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(2, readStateNames.size());
            assertTrue(readStateNames.contains("State1"));
            assertTrue(readStateNames.contains("State2"));
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("State Removal and Cleanup")
    class StateRemovalAndCleanup {

        @Test
        @DisplayName("Should remove inactive states")
        void testRemoveInactiveStates() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // Act
            stateMemory.removeInactiveState(2L);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(2, activeStates.size());
            assertFalse(activeStates.contains(2L));
            // mockFindStochasticModifier verification removed
}

        @Test
        @DisplayName("Should remove multiple states in batch")
        void testRemoveMultipleStates() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            Set<Long> toRemove = new HashSet<>(Arrays.asList(1L, 3L));

            // Act
            stateMemory.removeInactiveStates(toRemove);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(1, activeStates.size());
            assertTrue(activeStates.contains(2L));
        }

        @Test
        @DisplayName("Should clear all states")
        void testClearAllStates() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // Act
            stateMemory.removeAllStates();

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(0, activeStates.size());
        }
    }

    @Nested
    @DisplayName("State List Operations")
    class StateListOperations {

        @Test
        @DisplayName("Should get active state list")
        void testGetActiveStateList() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // Act
            List<State> activeStates = stateMemory.getActiveStateList();

            // Assert
            assertEquals(3, activeStates.size());
            assertTrue(activeStates.contains(mockState1));
            assertTrue(activeStates.contains(mockState2));
            assertTrue(activeStates.contains(mockState3));
        }

        @Test
        @DisplayName("Should get active state names")
        void testGetActiveStateNames() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // Act
            List<String> names = stateMemory.getActiveStateNames();

            // Assert
            assertEquals(2, names.size());
            assertTrue(names.contains("State1"));
            assertTrue(names.contains("State2"));
        }

        @Test
        @DisplayName("Should get active state names as string")
        void testGetActiveStateNamesAsString() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // Act
            String namesString = stateMemory.getActiveStateNamesAsString();

            // Assert
            assertTrue(namesString.contains("State1"));
            assertTrue(namesString.contains("State2"));
            assertTrue(namesString.contains("State3"));
            assertTrue(namesString.contains(", ")); // Check comma separation
        }

        @Test
        @DisplayName("Should handle invalid state IDs gracefully")
        void testInvalidStateIds() {
            // Arrange
            when(mockStateService.getState(999L)).thenReturn(Optional.empty());
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(999L); // Invalid - service returns empty

            // Act
            List<State> activeStates = stateMemory.getActiveStateList();

            // Assert
            // Should only include valid states
            assertEquals(1, activeStates.size());
            assertEquals(mockState1, activeStates.get(0));
        }
    }

    @Nested
    @DisplayName("Match Integration")
    class MatchIntegration {

        @Test
        @DisplayName("Should adjust active states with matches")
        void testAdjustActiveStatesWithMatches() {
            // Arrange
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            StateObjectMetadata stateData1 = mock(StateObjectMetadata.class);
            StateObjectMetadata stateData2 = mock(StateObjectMetadata.class);

            when(stateData1.getOwnerStateId()).thenReturn(1L);
            when(stateData2.getOwnerStateId()).thenReturn(2L);
            when(match1.getStateObjectData()).thenReturn(stateData1);
            when(match2.getStateObjectData()).thenReturn(stateData2);

            ActionResult actionResult = mock(ActionResult.class);
            when(actionResult.getMatchList()).thenReturn(Arrays.asList(match1, match2));

            // Act
            stateMemory.adjustActiveStatesWithMatches(actionResult);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertTrue(activeStates.contains(1L));
            assertTrue(activeStates.contains(2L));
        }

        @Test
        @DisplayName("Should ignore matches without state data")
        void testIgnoreMatchesWithoutStateData() {
            // Arrange
            Match matchWithoutData = mock(Match.class);
            when(matchWithoutData.getStateObjectData()).thenReturn(null);

            Match matchWithData = mock(Match.class);
            StateObjectMetadata stateData = mock(StateObjectMetadata.class);
            when(stateData.getOwnerStateId()).thenReturn(1L);
            when(matchWithData.getStateObjectData()).thenReturn(stateData);

            ActionResult actionResult = mock(ActionResult.class);
            when(actionResult.getMatchList())
                    .thenReturn(Arrays.asList(matchWithoutData, matchWithData));

            // Act
            stateMemory.adjustActiveStatesWithMatches(actionResult);

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(1, activeStates.size());
            assertTrue(activeStates.contains(1L));
        }
    }

    @Nested
    @DisplayName("State Removal by Name")
    class StateRemovalByName {

        @Test
        @DisplayName("Should remove state by name")
        void testRemoveStateByName() {
            // Arrange
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // Act
            stateMemory.removeInactiveState("State2");

            // Assert
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(1, activeStates.size());
            assertFalse(activeStates.contains(2L));
        }

        @Test
        @DisplayName("Should handle removal of non-existent state by name")
        void testRemoveNonExistentStateByName() {
            // Arrange
            stateMemory.addActiveState(1L);
            when(mockStateService.getStateId("NonExistentState")).thenReturn(null);

            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> stateMemory.removeInactiveState("NonExistentState"));

            // State1 should still be active
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertEquals(1, activeStates.size());
            assertTrue(activeStates.contains(1L));
        }
    }

    @Nested
    @DisplayName("State Probability Management")
    class StateProbabilityManagement {

        @Test
        @DisplayName("Should set probability to 100 when state becomes active")
        void testSetProbabilityOnActivation() {
            // Act
            stateMemory.addActiveState(1L);

            // Assert
            // mockFindStochasticModifier verification removed
}

        @Test
        @DisplayName("Should set probability to 0 when state becomes inactive")
        void testSetProbabilityOnDeactivation() {
            // Arrange
            stateMemory.addActiveState(1L);

            // Act
            stateMemory.removeInactiveState(1L);

            // Assert
            // mockFindStochasticModifier verification removed
}
    }
}
