package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdjacentStatesTest {

    @Mock
    private StateService stateService;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private StateTransitionService stateTransitionService;
    
    private AdjacentStates adjacentStates;
    
    @BeforeEach
    void setUp() {
        adjacentStates = new AdjacentStates(stateService, stateMemory, stateTransitionService);
    }
    
    @Test
    void testGetAdjacentStates_SingleState_NoTransitions() {
        // Setup
        Long stateId = 1L;
        when(stateTransitionService.getTransitions(stateId)).thenReturn(Optional.empty());
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(stateId);
        
        // Verify
        assertTrue(result.isEmpty());
        verify(stateTransitionService).getTransitions(stateId);
    }
    
    @Test
    void testGetAdjacentStates_SingleState_WithTransitions() {
        // Setup
        Long sourceStateId = 1L;
        Long targetState1 = 2L;
        Long targetState2 = 3L;
        
        JavaStateTransition transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition1.setActivate(new HashSet<>(Arrays.asList(targetState1)));
        
        JavaStateTransition transition2 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition2.setActivate(new HashSet<>(Arrays.asList(targetState2)));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(transition1);
        stateTransitions.addTransition(transition2);
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains(targetState1));
        assertTrue(result.contains(targetState2));
    }
    
    @Test
    void testGetAdjacentStates_TransitionWithEmptyActivate() {
        // Setup
        Long sourceStateId = 1L;
        Long targetState1 = 2L;
        
        // Transition with empty activate list (should be ignored)
        JavaStateTransition emptyTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        emptyTransition.setActivate(new HashSet<>());
        
        // Valid transition
        JavaStateTransition validTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        validTransition.setActivate(new HashSet<>(Arrays.asList(targetState1)));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(emptyTransition);
        stateTransitions.addTransition(validTransition);
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify - Only valid transition should be included
        assertEquals(1, result.size());
        assertTrue(result.contains(targetState1));
    }
    
    @Test
    void testGetAdjacentStates_WithPREVIOUSState() {
        // Setup
        Long sourceStateId = 1L;
        Long hiddenState1 = 10L;
        Long hiddenState2 = 11L;
        Long regularTargetState = 2L;
        
        JavaStateTransition transitionToPrevious = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transitionToPrevious.setActivate(new HashSet<>(Arrays.asList(SpecialStateType.PREVIOUS.getId())));
        
        JavaStateTransition regularTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        regularTransition.setActivate(new HashSet<>(Arrays.asList(regularTargetState)));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(transitionToPrevious);
        stateTransitions.addTransition(regularTransition);
        
        State sourceState = new State.Builder("SourceState")
                .build();
        sourceState.setId(sourceStateId);
        sourceState.addHiddenState(hiddenState1);
        sourceState.addHiddenState(hiddenState2);
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        when(stateService.getState(sourceStateId))
                .thenReturn(Optional.of(sourceState));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify - PREVIOUS should be replaced with hidden states
        assertEquals(3, result.size());
        assertTrue(result.contains(regularTargetState));
        assertTrue(result.contains(hiddenState1));
        assertTrue(result.contains(hiddenState2));
        assertFalse(result.contains(SpecialStateType.PREVIOUS.getId()));
    }
    
    @Test
    void testGetAdjacentStates_WithPREVIOUSState_NoHiddenStates() {
        // Setup
        Long sourceStateId = 1L;
        Long regularTargetState = 2L;
        
        JavaStateTransition transitionToPrevious = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transitionToPrevious.setActivate(new HashSet<>(Arrays.asList(SpecialStateType.PREVIOUS.getId())));
        
        JavaStateTransition regularTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        regularTransition.setActivate(new HashSet<>(Arrays.asList(regularTargetState)));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(transitionToPrevious);
        stateTransitions.addTransition(regularTransition);
        
        State sourceState = new State.Builder("SourceState")
                .build();
        sourceState.setId(sourceStateId);
        // No hidden states
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        when(stateService.getState(sourceStateId))
                .thenReturn(Optional.of(sourceState));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify - PREVIOUS removed but no hidden states to add
        assertEquals(1, result.size());
        assertTrue(result.contains(regularTargetState));
        assertFalse(result.contains(SpecialStateType.PREVIOUS.getId()));
    }
    
    @Test
    void testGetAdjacentStates_WithPREVIOUSState_StateNotFound() {
        // Setup
        Long sourceStateId = 1L;
        
        JavaStateTransition transitionToPrevious = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transitionToPrevious.setActivate(new HashSet<>(Arrays.asList(SpecialStateType.PREVIOUS.getId())));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(transitionToPrevious);
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        when(stateService.getState(sourceStateId))
                .thenReturn(Optional.empty());
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify - PREVIOUS removed, no state found to get hidden states from
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetAdjacentStates_MultipleTargetsInSingleTransition() {
        // Setup
        Long sourceStateId = 1L;
        Long targetState1 = 2L;
        Long targetState2 = 3L;
        Long targetState3 = 4L;
        
        JavaStateTransition multiTargetTransition = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        multiTargetTransition.setActivate(new HashSet<>(Arrays.asList(
                targetState1, targetState2, targetState3
        )));
        
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.addTransition(multiTargetTransition);
        
        when(stateTransitionService.getTransitions(sourceStateId))
                .thenReturn(Optional.of(stateTransitions));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStateId);
        
        // Verify
        assertEquals(3, result.size());
        assertTrue(result.contains(targetState1));
        assertTrue(result.contains(targetState2));
        assertTrue(result.contains(targetState3));
    }
    
    @Test
    void testGetAdjacentStates_SetOfStates() {
        // Setup
        Long state1 = 1L;
        Long state2 = 2L;
        Long adjacentToState1 = 10L;
        Long adjacentToState2_1 = 20L;
        Long adjacentToState2_2 = 21L;
        
        // State 1 transitions
        JavaStateTransition transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition1.setActivate(new HashSet<>(Arrays.asList(adjacentToState1)));
        
        StateTransitions stateTransitions1 = new StateTransitions();
        stateTransitions1.addTransition(transition1);
        
        // State 2 transitions
        JavaStateTransition transition2 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition2.setActivate(new HashSet<>(Arrays.asList(adjacentToState2_1, adjacentToState2_2)));
        
        StateTransitions stateTransitions2 = new StateTransitions();
        stateTransitions2.addTransition(transition2);
        
        when(stateTransitionService.getTransitions(state1))
                .thenReturn(Optional.of(stateTransitions1));
        when(stateTransitionService.getTransitions(state2))
                .thenReturn(Optional.of(stateTransitions2));
        
        Set<Long> sourceStates = new HashSet<>(Arrays.asList(state1, state2));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(sourceStates);
        
        // Verify - Should combine adjacent states from all sources
        assertEquals(3, result.size());
        assertTrue(result.contains(adjacentToState1));
        assertTrue(result.contains(adjacentToState2_1));
        assertTrue(result.contains(adjacentToState2_2));
    }
    
    @Test
    void testGetAdjacentStates_FromActiveStates() {
        // Setup
        Long activeState1 = 1L;
        Long activeState2 = 2L;
        Long adjacentState1 = 10L;
        Long adjacentState2 = 20L;
        
        Set<Long> activeStates = new HashSet<>(Arrays.asList(activeState1, activeState2));
        when(stateMemory.getActiveStates()).thenReturn(activeStates);
        
        // Setup transitions for active states
        JavaStateTransition transition1 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition1.setActivate(new HashSet<>(Arrays.asList(adjacentState1)));
        
        StateTransitions stateTransitions1 = new StateTransitions();
        stateTransitions1.addTransition(transition1);
        
        JavaStateTransition transition2 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transition2.setActivate(new HashSet<>(Arrays.asList(adjacentState2)));
        
        StateTransitions stateTransitions2 = new StateTransitions();
        stateTransitions2.addTransition(transition2);
        
        when(stateTransitionService.getTransitions(activeState1))
                .thenReturn(Optional.of(stateTransitions1));
        when(stateTransitionService.getTransitions(activeState2))
                .thenReturn(Optional.of(stateTransitions2));
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates();
        
        // Verify
        assertEquals(2, result.size());
        assertTrue(result.contains(adjacentState1));
        assertTrue(result.contains(adjacentState2));
        verify(stateMemory).getActiveStates();
    }
    
    @Test
    void testGetAdjacentStates_EmptySet() {
        // Setup
        Set<Long> emptySet = new HashSet<>();
        
        // Execute
        Set<Long> result = adjacentStates.getAdjacentStates(emptySet);
        
        // Verify
        assertTrue(result.isEmpty());
        verifyNoInteractions(stateTransitionService);
    }
}