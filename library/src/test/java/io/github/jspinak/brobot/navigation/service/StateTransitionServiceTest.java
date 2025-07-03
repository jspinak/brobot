package io.github.jspinak.brobot.navigation.service;

import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateTransitionServiceTest {

    @Mock
    private StateTransitionStore stateTransitionsRepository;
    
    @Mock
    private StateTransitionsJointTable stateTransitionsJointTable;
    
    private StateTransitionService stateTransitionService;
    
    private StateTransition transition1to2;
    private StateTransition transition2to3;
    private StateTransitions stateTransitions;
    
    @BeforeEach
    void setUp() {
        stateTransitionService = new StateTransitionService(stateTransitionsRepository, stateTransitionsJointTable);
        
        // Create test transitions
        transition1to2 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate("State2")
                .build();
                
        transition2to3 = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate("State3")
                .build();
                
        stateTransitions = new StateTransitions();
        stateTransitions.addTransition((JavaStateTransition) transition1to2);
    }
    
    @Test
    void testGetAllStateTransitionsInstances() {
        List<StateTransitions> allTransitions = Arrays.asList(stateTransitions);
        when(stateTransitionsRepository.getAllStateTransitionsAsCopy()).thenReturn(allTransitions);
        
        List<StateTransitions> result = stateTransitionService.getAllStateTransitionsInstances();
        
        assertEquals(allTransitions, result);
        verify(stateTransitionsRepository).getAllStateTransitionsAsCopy();
    }
    
    @Test
    void testGetAllStateTransitions() {
        List<StateTransitions> allTransitions = Arrays.asList(stateTransitions);
        when(stateTransitionsRepository.getRepo()).thenReturn(allTransitions);
        
        List<StateTransitions> result = stateTransitionService.getAllStateTransitions();
        
        assertEquals(allTransitions, result);
        verify(stateTransitionsRepository).getRepo();
    }
    
    @Test
    void testSetupRepo() {
        stateTransitionService.setupRepo();
        
        verify(stateTransitionsRepository).populateStateTransitionsJointTable();
    }
    
    @Test
    void testGetAllIndividualTransitions() {
        List<StateTransition> allTransitions = Arrays.asList(transition1to2, transition2to3);
        when(stateTransitionsRepository.getAllTransitions()).thenReturn(allTransitions);
        
        List<StateTransition> result = stateTransitionService.getAllIndividualTransitions();
        
        assertEquals(allTransitions, result);
        verify(stateTransitionsRepository).getAllTransitions();
    }
    
    @Test
    void testGetTransitionToEnum_DirectTransition() {
        Long fromState = 1L;
        Long toState = 2L;
        Set<Long> transitions = new HashSet<>(Arrays.asList(2L, 3L));
        
        when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                .thenReturn(transitions);
        
        Long result = stateTransitionService.getTransitionToEnum(fromState, toState);
        
        assertEquals(toState, result);
    }
    
    @Test
    void testGetTransitionToEnum_HiddenStateTransition() {
        Long fromState = 1L;
        Long toState = 2L;
        Set<Long> directTransitions = new HashSet<>(Arrays.asList(3L, 4L));
        Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();
        incomingToPrevious.put(toState, new HashSet<>(Arrays.asList(fromState)));
        
        when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                .thenReturn(directTransitions);
        when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                .thenReturn(incomingToPrevious);
        
        Long result = stateTransitionService.getTransitionToEnum(fromState, toState);
        
        assertEquals(SpecialStateType.PREVIOUS.getId(), result);
    }
    
    @Test
    void testGetTransitionToEnum_NoTransition() {
        Long fromState = 1L;
        Long toState = 2L;
        Set<Long> directTransitions = new HashSet<>(Arrays.asList(3L, 4L));
        Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();
        
        when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                .thenReturn(directTransitions);
        when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                .thenReturn(incomingToPrevious);
        
        Long result = stateTransitionService.getTransitionToEnum(fromState, toState);
        
        assertEquals(SpecialStateType.NULL.getId(), result);
    }
    
    @Test
    void testGetTransitionToEnum_NoHiddenStateForTarget() {
        Long fromState = 1L;
        Long toState = 2L;
        Set<Long> directTransitions = new HashSet<>();
        Map<Long, Set<Long>> incomingToPrevious = new HashMap<>();
        incomingToPrevious.put(3L, new HashSet<>(Arrays.asList(fromState))); // Different state has hidden transition
        
        when(stateTransitionsJointTable.getStatesWithTransitionsFrom(fromState))
                .thenReturn(directTransitions);
        when(stateTransitionsJointTable.getIncomingTransitionsToPREVIOUS())
                .thenReturn(incomingToPrevious);
        
        Long result = stateTransitionService.getTransitionToEnum(fromState, toState);
        
        assertEquals(SpecialStateType.NULL.getId(), result);
    }
    
    @Test
    void testGetTransitions_Found() {
        Long stateId = 1L;
        when(stateTransitionsRepository.get(stateId)).thenReturn(Optional.of(stateTransitions));
        
        Optional<StateTransitions> result = stateTransitionService.getTransitions(stateId);
        
        assertTrue(result.isPresent());
        assertEquals(stateTransitions, result.get());
    }
    
    @Test
    void testGetTransitions_NotFound() {
        Long stateId = 1L;
        when(stateTransitionsRepository.get(stateId)).thenReturn(Optional.empty());
        
        Optional<StateTransitions> result = stateTransitionService.getTransitions(stateId);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetTransition_Found() {
        Long fromState = 1L;
        Long toState = 2L;
        
        // Create a JavaStateTransition that activates toState
        JavaStateTransition transitionToTarget = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transitionToTarget.setActivate(new HashSet<>(Arrays.asList(toState)));
        
        StateTransitions stateTransitionsWithTarget = new StateTransitions();
        stateTransitionsWithTarget.setStateId(fromState);
        stateTransitionsWithTarget.addTransition(transitionToTarget);
        
        when(stateTransitionsRepository.get(fromState)).thenReturn(Optional.of(stateTransitionsWithTarget));
        
        Optional<StateTransition> result = stateTransitionService.getTransition(fromState, toState);
        
        assertTrue(result.isPresent());
        assertEquals(transitionToTarget, result.get());
    }
    
    @Test
    void testGetTransition_NoTransitionsForState() {
        Long fromState = 1L;
        Long toState = 2L;
        
        when(stateTransitionsRepository.get(fromState)).thenReturn(Optional.empty());
        
        Optional<StateTransition> result = stateTransitionService.getTransition(fromState, toState);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetTransition_NoSpecificTransition() {
        Long fromState = 1L;
        Long toState = 2L;
        Long differentState = 3L;
        
        // Create a JavaStateTransition that activates a different state
        JavaStateTransition transitionToDifferent = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .build();
        transitionToDifferent.setActivate(new HashSet<>(Arrays.asList(differentState)));
        
        StateTransitions stateTransitionsNonMatching = new StateTransitions();
        stateTransitionsNonMatching.setStateId(fromState);
        stateTransitionsNonMatching.addTransition(transitionToDifferent);
        
        when(stateTransitionsRepository.get(fromState)).thenReturn(Optional.of(stateTransitionsNonMatching));
        
        Optional<StateTransition> result = stateTransitionService.getTransition(fromState, toState);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testResetTimesSuccessful() {
        List<StateTransition> allTransitions = Arrays.asList(transition1to2, transition2to3);
        when(stateTransitionsRepository.getAllTransitions()).thenReturn(allTransitions);
        
        // Set some initial values
        transition1to2.setTimesSuccessful(5);
        transition2to3.setTimesSuccessful(10);
        
        stateTransitionService.resetTimesSuccessful();
        
        assertEquals(0, transition1to2.getTimesSuccessful());
        assertEquals(0, transition2to3.getTimesSuccessful());
        verify(stateTransitionsRepository).getAllTransitions();
    }
    
    @Test
    void testPrintAllTransitions() {
        List<StateTransition> allTransitions = Arrays.asList(transition1to2, transition2to3);
        when(stateTransitionsRepository.getAllTransitions()).thenReturn(allTransitions);
        
        // Should not throw exception
        assertDoesNotThrow(() -> stateTransitionService.printAllTransitions());
        
        verify(stateTransitionsRepository).getAllTransitions();
    }
    
    @Test
    void testGetStatesToActivate() {
        Set<Long> statesToActivate = stateTransitionService.getStatesToActivate();
        
        assertNotNull(statesToActivate);
        assertTrue(statesToActivate.isEmpty());
        
        // Test that it's mutable
        statesToActivate.add(1L);
        assertEquals(1, statesToActivate.size());
    }
}