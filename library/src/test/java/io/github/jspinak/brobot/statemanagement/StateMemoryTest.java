package io.github.jspinak.brobot.statemanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

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

@DisplayName("StateMemory Tests")
public class StateMemoryTest extends BrobotTestBase {

    @Mock private StateService stateService;

    @Mock private State mockState1;

    @Mock private State mockState2;

    @Mock private State mockState3;

    private StateMemory stateMemory;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateMemory = new StateMemory(stateService);

        // Setup common mock behaviors
        when(mockState1.getName()).thenReturn("State1");
        when(mockState2.getName()).thenReturn("State2");
        when(mockState3.getName()).thenReturn("State3");

        when(stateService.getState(1L)).thenReturn(Optional.of(mockState1));
        when(stateService.getState(2L)).thenReturn(Optional.of(mockState2));
        when(stateService.getState(3L)).thenReturn(Optional.of(mockState3));

        when(stateService.getStateName(1L)).thenReturn("State1");
        when(stateService.getStateName(2L)).thenReturn("State2");
        when(stateService.getStateName(3L)).thenReturn("State3");

        when(stateService.getStateId("State1")).thenReturn(1L);
        when(stateService.getStateId("State2")).thenReturn(2L);
        when(stateService.getStateId("State3")).thenReturn(3L);
    }

    @Nested
    @DisplayName("Active State Management")
    class ActiveStateManagement {

        @Test
        @DisplayName("Should start with empty active states")
        void shouldStartWithEmptyActiveStates() {
            assertTrue(stateMemory.getActiveStates().isEmpty());
            assertTrue(stateMemory.getActiveStateList().isEmpty());
            assertTrue(stateMemory.getActiveStateNames().isEmpty());
        }

        @Test
        @DisplayName("Should add state to active states")
        void shouldAddStateToActiveStates() {
            stateMemory.addActiveState(1L);

            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertEquals(1, stateMemory.getActiveStates().size());
            verify(mockState1).addVisit();
        }

        @Test
        @DisplayName("Should not add duplicate active state")
        void shouldNotAddDuplicateActiveState() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(1L);

            assertEquals(1, stateMemory.getActiveStates().size());
            verify(mockState1, times(1)).addVisit();
        }

        @Test
        @DisplayName("Should not add NULL state")
        void shouldNotAddNullState() {
            stateMemory.addActiveState(SpecialStateType.NULL.getId());

            assertTrue(stateMemory.getActiveStates().isEmpty());
        }

        @Test
        @DisplayName("Should remove inactive state")
        void shouldRemoveInactiveState() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            stateMemory.removeInactiveState(1L);

            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            }

        @Test
        @DisplayName("Should remove inactive state by name")
        void shouldRemoveInactiveStateByName() {
            stateMemory.addActiveState(1L);

            stateMemory.removeInactiveState("State1");

            assertFalse(stateMemory.getActiveStates().contains(1L));
            }

        @Test
        @DisplayName("Should remove multiple inactive states")
        void shouldRemoveMultipleInactiveStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            Set<Long> toRemove = new HashSet<>(Arrays.asList(1L, 2L));
            stateMemory.removeInactiveStates(toRemove);

            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertFalse(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
        }

        @Test
        @DisplayName("Should remove all states")
        void shouldRemoveAllStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            stateMemory.removeAllStates();

            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
    }

    @Nested
    @DisplayName("State Retrieval")
    class StateRetrieval {

        @Test
        @DisplayName("Should get active state list")
        void shouldGetActiveStateList() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            List<State> activeStates = stateMemory.getActiveStateList();

            assertEquals(2, activeStates.size());
            assertTrue(activeStates.contains(mockState1));
            assertTrue(activeStates.contains(mockState2));
        }

        @Test
        @DisplayName("Should get active state names")
        void shouldGetActiveStateNames() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            List<String> names = stateMemory.getActiveStateNames();

            assertEquals(2, names.size());
            assertTrue(names.contains("State1"));
            assertTrue(names.contains("State2"));
        }

        @Test
        @DisplayName("Should get active state names as string")
        void shouldGetActiveStateNamesAsString() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);

            String namesString = stateMemory.getActiveStateNamesAsString();

            assertTrue(namesString.contains("State1"));
            assertTrue(namesString.contains("State2"));
            assertTrue(namesString.contains(",") || namesString.length() > 0);
        }

        @Test
        @DisplayName("Should handle missing states gracefully")
        void shouldHandleMissingStatesGracefully() {
            when(stateService.getState(99L)).thenReturn(Optional.empty());

            stateMemory.getActiveStates().add(99L); // Directly add to test edge case

            List<State> activeStates = stateMemory.getActiveStateList();
            List<String> names = stateMemory.getActiveStateNames();

            assertTrue(activeStates.isEmpty());
            assertTrue(names.isEmpty());
        }
    }

    @Nested
    @DisplayName("Match Integration")
    class MatchIntegration {

        @Test
        @DisplayName("Should adjust active states with matches")
        void shouldAdjustActiveStatesWithMatches() {
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            StateObjectMetadata data1 = mock(StateObjectMetadata.class);
            StateObjectMetadata data2 = mock(StateObjectMetadata.class);

            when(match1.getStateObjectData()).thenReturn(data1);
            when(match2.getStateObjectData()).thenReturn(data2);
            when(data1.getOwnerStateId()).thenReturn(1L);
            when(data2.getOwnerStateId()).thenReturn(2L);

            ActionResult result = mock(ActionResult.class);
            when(result.getMatchList()).thenReturn(Arrays.asList(match1, match2));

            stateMemory.adjustActiveStatesWithMatches(result);

            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
        }

        @Test
        @DisplayName("Should ignore matches without state object data")
        void shouldIgnoreMatchesWithoutStateObjectData() {
            Match match = mock(Match.class);
            when(match.getStateObjectData()).thenReturn(null);

            ActionResult result = mock(ActionResult.class);
            when(result.getMatchList()).thenReturn(Collections.singletonList(match));

            stateMemory.adjustActiveStatesWithMatches(result);

            assertTrue(stateMemory.getActiveStates().isEmpty());
        }

        @Test
        @DisplayName("Should ignore matches with invalid owner state ID")
        void shouldIgnoreMatchesWithInvalidOwnerStateId() {
            Match match = mock(Match.class);
            StateObjectMetadata data = mock(StateObjectMetadata.class);

            when(match.getStateObjectData()).thenReturn(data);
            when(data.getOwnerStateId()).thenReturn(0L);

            ActionResult result = mock(ActionResult.class);
            when(result.getMatchList()).thenReturn(Collections.singletonList(match));

            stateMemory.adjustActiveStatesWithMatches(result);

            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null state name removal gracefully")
        void shouldHandleNullStateNameRemoval() {
            when(stateService.getStateId("NonExistent")).thenReturn(null);

            assertDoesNotThrow(() -> stateMemory.removeInactiveState("NonExistent"));
        }

        @Test
        @DisplayName("Should not remove state that is not active")
        void shouldNotRemoveStateNotActive() {
            stateMemory.addActiveState(1L);

            stateMemory.removeInactiveState(2L);

            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertEquals(1, stateMemory.getActiveStates().size());
            // mockFindStochasticModifier is not modified on state removal
        }

        @Test
        @DisplayName("Should handle multiple state additions")
        void shouldHandleMultipleStateAdditions() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);

            assertEquals(3, stateMemory.getActiveStates().size());
            verify(mockState1).addVisit();
            verify(mockState2).addVisit();
            verify(mockState3).addVisit();
        }
    }
}
