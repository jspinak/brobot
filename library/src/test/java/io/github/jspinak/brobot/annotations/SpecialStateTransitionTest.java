package io.github.jspinak.brobot.annotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.CurrentState;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;

/** Test that special state marker classes work correctly with the annotation processor. */
@ExtendWith(MockitoExtension.class)
class SpecialStateTransitionTest {

    @Mock private StateTransitionsJointTable jointTable;

    @Mock private StateService stateService;

    @Mock private StateTransitionService transitionService;

    @Mock private StateTransitionStore repository;

    private TransitionSetProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TransitionSetProcessor(jointTable, stateService, transitionService);
        lenient().when(transitionService.getStateTransitionsRepository()).thenReturn(repository);
    }

    /** Test state with a transition to PreviousState marker class. */
    static class TestState {
        // Just a marker for testing
    }

    /** Test transition set with special state transitions. */
    @TransitionSet(state = TestState.class)
    @Component
    static class TestTransitionSet {

        @IncomingTransition
        public boolean verifyArrival() {
            return true;
        }

        @OutgoingTransition(
                activate = {PreviousState.class},
                pathCost = 0,
                description = "Return to previous state")
        public boolean returnToPrevious() {
            return true;
        }

        @OutgoingTransition(
                activate = {CurrentState.class},
                pathCost = 5,
                description = "Stay in current state")
        public boolean refreshCurrent() {
            return true;
        }
    }

    @Test
    void testPreviousStateTransition() {
        // Setup
        TestTransitionSet transitionSet = new TestTransitionSet();
        TransitionSet annotation = TestTransitionSet.class.getAnnotation(TransitionSet.class);

        State testState = new State.Builder("Test").build();
        testState.setId(100L);
        when(stateService.getState("Test")).thenReturn(Optional.of(testState));
        when(transitionService.getTransitions(100L)).thenReturn(Optional.empty());

        // Execute
        boolean result = processor.processTransitionSet(transitionSet, annotation);

        // Verify
        assertTrue(result);

        // Capture the transition that was added
        ArgumentCaptor<StateTransitions> captor = ArgumentCaptor.forClass(StateTransitions.class);
        verify(repository).add(captor.capture());

        StateTransitions transitions = captor.getValue();
        assertNotNull(transitions);

        // Check that transitions were added (we can't easily check the actual transition
        // details without accessing private fields, but we can verify the method was called)
        verify(jointTable).addToJointTable(any(StateTransitions.class));
    }

    @Test
    void testSpecialStateIdMapping() {
        // Test that special state IDs are correctly mapped
        assertEquals(SpecialStateType.PREVIOUS.getId(), PreviousState.ID);
        assertEquals(SpecialStateType.CURRENT.getId(), CurrentState.ID);

        // These should be negative values
        assertTrue(PreviousState.ID < 0);
        assertTrue(CurrentState.ID < 0);

        // Specific values based on SpecialStateType enum
        assertEquals(-2L, PreviousState.ID);
        assertEquals(-3L, CurrentState.ID);
    }

    @Test
    void testMultipleSpecialTransitions() {
        // This tests that a state can have multiple special transitions
        TestTransitionSet transitionSet = new TestTransitionSet();
        TransitionSet annotation = TestTransitionSet.class.getAnnotation(TransitionSet.class);

        State testState = new State.Builder("Test").build();
        testState.setId(100L);
        when(stateService.getState("Test")).thenReturn(Optional.of(testState));
        when(transitionService.getTransitions(100L)).thenReturn(Optional.empty());

        // Execute
        boolean result = processor.processTransitionSet(transitionSet, annotation);
        assertTrue(result);

        // The processor should have processed both OutgoingTransitions
        // We can't easily verify the details, but we can check that processing succeeded
        verify(repository, times(1)).add(any(StateTransitions.class));
        // Should only add to joint table once per state, not per transition
        verify(jointTable, times(1)).addToJointTable(any(StateTransitions.class));
    }
}
