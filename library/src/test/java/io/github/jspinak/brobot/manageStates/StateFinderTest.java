package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateFinder;
import io.github.jspinak.brobot.manageStates.StateMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateFinderTest {

    @Mock
    private AllStatesInProjectService allStatesInProjectService;
    @Mock
    private StateMemory stateMemory;
    @Mock
    private Action action;

    private StateFinder stateFinder;

    @BeforeEach
    void setUp() {
        stateFinder = new StateFinder(allStatesInProjectService, stateMemory, action);
    }

    @Test
    void should_use_STATE_DETECTION_log_type_when_finding_state() {
        // Arrange
        long stateId = 1L;
        State mockState = mock(State.class);
        when(allStatesInProjectService.getState(stateId))
                .thenReturn(Optional.of(mockState));

        Matches mockMatches = mock(Matches.class);
        when(mockMatches.isSuccess()).thenReturn(true);

        when(action.perform(
                eq(ActionOptions.Action.FIND),
                any(ObjectCollection.class)
        )).thenReturn(mockMatches);

        // Act
        stateFinder.findState(stateId);

        // Assert
        verify(action).perform(
                eq(ActionOptions.Action.FIND),
                any(ObjectCollection.class)
        );
    }
}