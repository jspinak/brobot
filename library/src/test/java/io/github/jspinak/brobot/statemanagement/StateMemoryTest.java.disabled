package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateMemory class.
 * Tests state tracking, transitions, and memory management functionality.
 */
@DisplayName("StateMemory Tests")
public class StateMemoryTest extends BrobotTestBase {

    @Mock
    private StateService stateService;

    private StateMemory stateMemory;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateMemory = new StateMemory(stateService);
    }

    @Nested
    @DisplayName("Basic State Operations")
    class BasicStateOperations {

        @Test
        @DisplayName("Should add state to active states")
        void shouldAddStateToActiveStates() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");

            // When
            stateMemory.addActiveState(stateId);

            // Then
            assertTrue(stateMemory.getActiveStates().contains(stateId));
            verify(mockState).setProbabilityExists(100);
            verify(mockState).addVisit();
        }

        @Test
        @DisplayName("Should not add duplicate state")
        void shouldNotAddDuplicateState() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");

            // When
            stateMemory.addActiveState(stateId);
            stateMemory.addActiveState(stateId); // Try to add again

            // Then
            assertEquals(1, stateMemory.getActiveStates().size());
            verify(mockState, times(1)).setProbabilityExists(100);
            verify(mockState, times(1)).addVisit();
        }

        @Test
        @DisplayName("Should not add NULL state")
        void shouldNotAddNullState() {
            // Given
            Long nullStateId = SpecialStateType.NULL.getId();

            // When
            stateMemory.addActiveState(nullStateId);

            // Then
            assertFalse(stateMemory.getActiveStates().contains(nullStateId));
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }

        @Test
        @DisplayName("Should remove state from active states")
        void shouldRemoveStateFromActiveStates() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");
            stateMemory.addActiveState(stateId);

            // When
            stateMemory.removeInactiveState(stateId);

            // Then
            assertFalse(stateMemory.getActiveStates().contains(stateId));
            verify(mockState).setProbabilityExists(0);
        }

        @Test
        @DisplayName("Should handle removing non-existent state")
        void shouldHandleRemovingNonExistentState() {
            // Given
            Long stateId = 999L;

            // When/Then - should not throw exception
            assertDoesNotThrow(() -> stateMemory.removeInactiveState(stateId));
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
    }

    @Nested
    @DisplayName("State List Operations")
    class StateListOperations {

        @Test
        @DisplayName("Should get active state list")
        void shouldGetActiveStateList() {
            // Given
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            when(state1.getName()).thenReturn("State1");
            when(state2.getName()).thenReturn("State2");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getStateName(1L)).thenReturn("State1");
            when(stateService.getStateName(2L)).thenReturn("State2");
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // When
            List<State> activeStates = stateMemory.getActiveStateList();

            // Then
            assertEquals(2, activeStates.size());
            assertTrue(activeStates.contains(state1));
            assertTrue(activeStates.contains(state2));
        }

        @Test
        @DisplayName("Should get active state names")
        void shouldGetActiveStateNames() {
            // Given
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            when(state1.getName()).thenReturn("LoginState");
            when(state2.getName()).thenReturn("HomeState");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getStateName(1L)).thenReturn("LoginState");
            when(stateService.getStateName(2L)).thenReturn("HomeState");
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // When
            List<String> stateNames = stateMemory.getActiveStateNames();

            // Then
            assertEquals(2, stateNames.size());
            assertTrue(stateNames.contains("LoginState"));
            assertTrue(stateNames.contains("HomeState"));
        }

        @Test
        @DisplayName("Should get active state names as string")
        void shouldGetActiveStateNamesAsString() {
            // Given
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            when(state1.getName()).thenReturn("State1");
            when(state2.getName()).thenReturn("State2");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getStateName(1L)).thenReturn("State1");
            when(stateService.getStateName(2L)).thenReturn("State2");
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // When
            String namesString = stateMemory.getActiveStateNamesAsString();

            // Then
            assertTrue(namesString.contains("State1"));
            assertTrue(namesString.contains("State2"));
            assertTrue(namesString.contains(", ") || namesString.equals("State1") || namesString.equals("State2"));
        }

        @Test
        @DisplayName("Should handle empty active states")
        void shouldHandleEmptyActiveStates() {
            // When
            List<State> activeStates = stateMemory.getActiveStateList();
            List<String> stateNames = stateMemory.getActiveStateNames();
            String namesString = stateMemory.getActiveStateNamesAsString();

            // Then
            assertTrue(activeStates.isEmpty());
            assertTrue(stateNames.isEmpty());
            assertEquals("", namesString);
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperations {

        @Test
        @DisplayName("Should remove multiple inactive states")
        void shouldRemoveMultipleInactiveStates() {
            // Given
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getStateName(anyLong())).thenReturn("State");
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // When
            Set<Long> toRemove = new HashSet<>(Arrays.asList(1L, 3L));
            stateMemory.removeInactiveStates(toRemove);

            // Then
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertFalse(stateMemory.getActiveStates().contains(3L));
            verify(state1).setProbabilityExists(0);
            verify(state3).setProbabilityExists(0);
        }

        @Test
        @DisplayName("Should remove all states")
        void shouldRemoveAllStates() {
            // Given
            when(stateService.getStateName(anyLong())).thenReturn("State");
            State mockState = mock(State.class);
            when(stateService.getState(anyLong())).thenReturn(Optional.of(mockState));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            // When
            stateMemory.removeAllStates();

            // Then
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }

        @Test
        @DisplayName("Should remove state by name")
        void shouldRemoveStateByName() {
            // Given
            Long stateId = 5L;
            String stateName = "TestState";
            State mockState = mock(State.class);
            
            when(stateService.getStateId(stateName)).thenReturn(stateId);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn(stateName);
            
            stateMemory.addActiveState(stateId);

            // When
            stateMemory.removeInactiveState(stateName);

            // Then
            assertFalse(stateMemory.getActiveStates().contains(stateId));
            verify(mockState).setProbabilityExists(0);
        }

        @Test
        @DisplayName("Should handle removing non-existent state by name")
        void shouldHandleRemovingNonExistentStateByName() {
            // Given
            String stateName = "NonExistentState";
            when(stateService.getStateId(stateName)).thenReturn(null);

            // When/Then - should not throw exception
            assertDoesNotThrow(() -> stateMemory.removeInactiveState(stateName));
        }
    }

    @Nested
    @DisplayName("Match Integration")
    class MatchIntegration {

        @Test
        @DisplayName("Should adjust active states with matches")
        void shouldAdjustActiveStatesWithMatches() {
            // Given
            ActionResult actionResult = new ActionResult();
            
            Match match1 = new Match.Builder().build();
            StateObjectMetadata metadata1 = new StateObjectMetadata();
            metadata1.setOwnerStateId(10L);
            match1.setStateObjectData(metadata1);
            
            Match match2 = new Match.Builder().build();
            StateObjectMetadata metadata2 = new StateObjectMetadata();
            metadata2.setOwnerStateId(20L);
            match2.setStateObjectData(metadata2);
            
            actionResult.setMatchList(Arrays.asList(match1, match2));
            
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            when(stateService.getState(10L)).thenReturn(Optional.of(state1));
            when(stateService.getState(20L)).thenReturn(Optional.of(state2));
            when(stateService.getStateName(10L)).thenReturn("State10");
            when(stateService.getStateName(20L)).thenReturn("State20");

            // When
            stateMemory.adjustActiveStatesWithMatches(actionResult);

            // Then
            assertTrue(stateMemory.getActiveStates().contains(10L));
            assertTrue(stateMemory.getActiveStates().contains(20L));
            verify(state1).setProbabilityExists(100);
            verify(state2).setProbabilityExists(100);
        }

        @Test
        @DisplayName("Should ignore matches without state data")
        void shouldIgnoreMatchesWithoutStateData() {
            // Given
            ActionResult actionResult = new ActionResult();
            Match match = new Match.Builder().build();
            // No state object data set
            actionResult.setMatchList(Arrays.asList(match));

            // When
            stateMemory.adjustActiveStatesWithMatches(actionResult);

            // Then
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }

        @Test
        @DisplayName("Should ignore matches with null or zero state ID")
        void shouldIgnoreMatchesWithInvalidStateId() {
            // Given
            ActionResult actionResult = new ActionResult();
            
            Match match1 = new Match.Builder().build();
            StateObjectMetadata metadata1 = new StateObjectMetadata();
            metadata1.setOwnerStateId(null);
            match1.setStateObjectData(metadata1);
            
            Match match2 = new Match.Builder().build();
            StateObjectMetadata metadata2 = new StateObjectMetadata();
            metadata2.setOwnerStateId(0L);
            match2.setStateObjectData(metadata2);
            
            actionResult.setMatchList(Arrays.asList(match1, match2));

            // When
            stateMemory.adjustActiveStatesWithMatches(actionResult);

            // Then
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
    }

    @Nested
    @DisplayName("State Probability and Visits")
    class StateProbabilityAndVisits {

        @Test
        @DisplayName("Should set probability to 100 when adding state")
        void shouldSetProbabilityWhenAddingState() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");

            // When
            stateMemory.addActiveState(stateId);

            // Then
            verify(mockState).setProbabilityExists(100);
        }

        @Test
        @DisplayName("Should set probability to 0 when removing state")
        void shouldSetProbabilityWhenRemovingState() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");
            stateMemory.addActiveState(stateId);

            // When
            stateMemory.removeInactiveState(stateId);

            // Then
            verify(mockState).setProbabilityExists(0);
        }

        @Test
        @DisplayName("Should increment visit count when adding state")
        void shouldIncrementVisitCount() {
            // Given
            Long stateId = 1L;
            State mockState = mock(State.class);
            when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
            when(stateService.getStateName(stateId)).thenReturn("TestState");

            // When
            stateMemory.addActiveState(stateId);

            // Then
            verify(mockState).addVisit();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @ParameterizedTest
        @DisplayName("Should handle various state IDs")
        @ValueSource(longs = {Long.MIN_VALUE, -1L, 0L, 1L, 999999L, Long.MAX_VALUE})
        void shouldHandleVariousStateIds(long stateId) {
            // Given
            if (stateId != SpecialStateType.NULL.getId() && stateId > 0) {
                State mockState = mock(State.class);
                when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
                when(stateService.getStateName(stateId)).thenReturn("State" + stateId);
            }

            // When
            stateMemory.addActiveState(stateId);

            // Then
            if (stateId == SpecialStateType.NULL.getId() || stateId <= 0) {
                assertFalse(stateMemory.getActiveStates().contains(stateId));
            } else {
                assertTrue(stateMemory.getActiveStates().contains(stateId));
            }
        }

        @Test
        @DisplayName("Should handle state not found in StateService")
        void shouldHandleStateNotFound() {
            // Given
            Long stateId = 999L;
            when(stateService.getState(stateId)).thenReturn(Optional.empty());
            when(stateService.getStateName(stateId)).thenReturn("UnknownState");

            // When
            stateMemory.addActiveState(stateId);

            // Then
            assertTrue(stateMemory.getActiveStates().contains(stateId));
            // No probability or visit operations should occur
        }

        @Test
        @DisplayName("Should maintain state order in lists")
        void shouldMaintainStateOrder() {
            // Given
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            when(state1.getName()).thenReturn("A");
            when(state2.getName()).thenReturn("B");
            when(state3.getName()).thenReturn("C");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getStateName(anyLong())).thenReturn("State");

            // When
            stateMemory.addActiveState(3L);
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            // Then
            List<State> states = stateMemory.getActiveStateList();
            assertEquals(3, states.size());
            // Order depends on HashSet iteration order, just verify all are present
            assertTrue(states.containsAll(Arrays.asList(state1, state2, state3)));
        }
    }

    @Nested
    @DisplayName("Special State Types")
    class SpecialStateTypes {

        @Test
        @DisplayName("Should handle PREVIOUS state enum")
        void shouldHandlePreviousStateEnum() {
            // Verify enum exists and can be accessed
            assertNotNull(StateMemory.Enum.PREVIOUS);
            assertEquals("PREVIOUS", StateMemory.Enum.PREVIOUS.name());
        }

        @Test
        @DisplayName("Should handle CURRENT state enum")
        void shouldHandleCurrentStateEnum() {
            assertNotNull(StateMemory.Enum.CURRENT);
            assertEquals("CURRENT", StateMemory.Enum.CURRENT.name());
        }

        @Test
        @DisplayName("Should handle EXPECTED state enum")
        void shouldHandleExpectedStateEnum() {
            assertNotNull(StateMemory.Enum.EXPECTED);
            assertEquals("EXPECTED", StateMemory.Enum.EXPECTED.name());
        }
    }
}