package io.github.jspinak.brobot.statemanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("Simple InitialStates Tests")
public class SimpleInitialStatesTest extends BrobotTestBase {

    @Mock private BrobotProperties brobotProperties;

    @Mock private StateDetector stateDetector;

    @Mock private StateMemory stateMemory;

    @Mock private StateService stateService;

    private InitialStates initialStates;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Setup BrobotProperties mock
        BrobotProperties.Core core = new BrobotProperties.Core();
        core.setMock(true);
        when(brobotProperties.getCore()).thenReturn(core);

        initialStates = new InitialStates(brobotProperties, stateDetector, stateMemory, stateService);
    }

    @Test
    @DisplayName("Should add state set with State objects")
    public void testAddStateSetWithStates() {
        State state1 = mock(State.class);
        State state2 = mock(State.class);
        when(state1.getId()).thenReturn(1L);
        when(state2.getId()).thenReturn(2L);

        initialStates.addStateSet(50, state1, state2);

        assertEquals(50, initialStates.sumOfProbabilities);
    }

    @Test
    @DisplayName("Should add multiple state sets with cumulative probabilities")
    public void testAddMultipleStateSets() {
        State state1 = mock(State.class);
        State state2 = mock(State.class);
        State state3 = mock(State.class);
        when(state1.getId()).thenReturn(1L);
        when(state2.getId()).thenReturn(2L);
        when(state3.getId()).thenReturn(3L);

        initialStates.addStateSet(50, state1);
        initialStates.addStateSet(30, state2);
        initialStates.addStateSet(20, state3);

        assertEquals(100, initialStates.sumOfProbabilities);
    }

    @Test
    @DisplayName("Should add state set with state names")
    public void testAddStateSetWithNames() {
        State state1 = mock(State.class);
        State state2 = mock(State.class);
        when(state1.getId()).thenReturn(1L);
        when(state2.getId()).thenReturn(2L);

        when(stateService.getState("LoginPage")).thenReturn(Optional.of(state1));
        when(stateService.getState("Dashboard")).thenReturn(Optional.of(state2));

        initialStates.addStateSet(70, "LoginPage", "Dashboard");

        assertEquals(70, initialStates.sumOfProbabilities);
    }

    @Test
    @DisplayName("Should ignore zero or negative probabilities")
    public void testIgnoreInvalidProbabilities() {
        State state = mock(State.class);
        when(state.getId()).thenReturn(1L);

        initialStates.addStateSet(0, state);
        initialStates.addStateSet(-10, state);

        assertEquals(0, initialStates.sumOfProbabilities);
    }

    @Test
    @DisplayName("Should find initial states in mock mode")
    public void testFindInitialStatesInMockMode() {
        // Set mock mode for this test
        // Mock mode is now enabled via BrobotTestBase

        State state1 = mock(State.class);
        when(state1.getId()).thenReturn(1L);
        when(state1.getName()).thenReturn("TestState");
        when(stateService.getState(1L)).thenReturn(Optional.of(state1));

        initialStates.addStateSet(100, state1);

        // Method has typo in actual implementation
        initialStates.findInitialStates();

        // Should have added a state in mock mode with two parameters
        verify(stateMemory, atLeastOnce()).addActiveState(eq(1L), eq(true));
    }

    @Test
    @DisplayName("Should handle empty state sets in mock mode")
    public void testEmptyStateSetsInMockMode() {
        // Set mock mode for this test
        // Mock mode is now enabled via BrobotTestBase

        // No state sets added - potentialActiveStates is empty

        initialStates.findInitialStates();

        // In mock mode with empty potentialActiveStates, it just returns
        // without calling stateDetector or stateMemory
        verify(stateDetector, never()).refreshActiveStates();
        verify(stateMemory, never()).addActiveState(anyLong(), anyBoolean());
    }
}
