package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateVisibilityManagerTest {

    @Mock
    private StateService stateService;
    
    @Mock
    private StateMemory stateMemory;
    
    private StateVisibilityManager visibilityManager;
    
    @BeforeEach
    void setUp() {
        visibilityManager = new StateVisibilityManager(stateService, stateMemory);
    }
    
    @Test
    void testSet_InvalidStateId() {
        // Setup
        Long invalidStateId = 999L;
        when(stateService.getState(invalidStateId)).thenReturn(Optional.empty());
        
        // Execute
        boolean result = visibilityManager.set(invalidStateId);
        
        // Verify
        assertFalse(result);
        verify(stateService).getState(invalidStateId);
        verifyNoInteractions(stateMemory);
    }
    
    @Test
    void testSet_ValidStateWithNoActiveStates() {
        // Setup
        Long stateToSetId = 1L;
        State stateToSet = new State.Builder("DialogState")
                .build();
        stateToSet.setId(stateToSetId);
        
        when(stateService.getState(stateToSetId)).thenReturn(Optional.of(stateToSet));
        when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
        
        // Execute
        boolean result = visibilityManager.set(stateToSetId);
        
        // Verify
        assertTrue(result);
        verify(stateService).getState(stateToSetId);
        verify(stateMemory).getActiveStates();
        verify(stateMemory, never()).removeInactiveState(any(String.class));
    }
    
    @Test
    void testSet_HidesOneActiveState() {
        // Setup
        Long dialogStateId = 1L;
        Long mainPageId = 2L;
        
        State dialogState = new State.Builder("DialogState")
                .build();
        dialogState.setId(dialogStateId);
        dialogState.setCanHideIds(new HashSet<>(Collections.singletonList(mainPageId)));
        
        when(stateService.getState(dialogStateId)).thenReturn(Optional.of(dialogState));
        when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Collections.singletonList(mainPageId)));
        
        // Execute
        boolean result = visibilityManager.set(dialogStateId);
        
        // Verify
        assertTrue(result);
        verify(stateService).getState(dialogStateId);
        verify(stateMemory).getActiveStates();
        verify(stateMemory).removeInactiveState(mainPageId);
        assertTrue(dialogState.getHiddenStateIds().contains(mainPageId));
    }
    
    @Test
    void testSet_HidesMultipleStates() {
        // Setup
        Long overlayStateId = 1L;
        Long mainPageId = 2L;
        Long sidebarId = 3L;
        Long toolbarId = 4L;
        
        State overlayState = new State.Builder("FullScreenOverlay")
                .build();
        overlayState.setId(overlayStateId);
        overlayState.setCanHideIds(new HashSet<>(Arrays.asList(mainPageId, sidebarId)));
        
        when(stateService.getState(overlayStateId)).thenReturn(Optional.of(overlayState));
        when(stateMemory.getActiveStates()).thenReturn(
                new HashSet<>(Arrays.asList(mainPageId, sidebarId, toolbarId))
        );
        
        // Execute
        boolean result = visibilityManager.set(overlayStateId);
        
        // Verify
        assertTrue(result);
        verify(stateMemory).removeInactiveState(mainPageId);
        verify(stateMemory).removeInactiveState(sidebarId);
        verify(stateMemory, never()).removeInactiveState(toolbarId); // Should not hide toolbar
        
        Set<Long> hiddenStates = overlayState.getHiddenStateIds();
        assertEquals(2, hiddenStates.size());
        assertTrue(hiddenStates.contains(mainPageId));
        assertTrue(hiddenStates.contains(sidebarId));
    }
    
    @Test
    void testSet_NoMatchingStatesToHide() {
        // Setup
        Long newStateId = 1L;
        Long activeState1 = 2L;
        Long activeState2 = 3L;
        Long canHideState = 4L; // Not currently active
        
        State newState = new State.Builder("NewState")
                .build();
        newState.setId(newStateId);
        newState.setCanHideIds(new HashSet<>(Collections.singletonList(canHideState)));
        
        when(stateService.getState(newStateId)).thenReturn(Optional.of(newState));
        when(stateMemory.getActiveStates()).thenReturn(
                new HashSet<>(Arrays.asList(activeState1, activeState2))
        );
        
        // Execute
        boolean result = visibilityManager.set(newStateId);
        
        // Verify
        assertTrue(result);
        verify(stateMemory, never()).removeInactiveState(any(String.class));
        assertTrue(newState.getHiddenStateIds().isEmpty());
    }
    
    @Test
    void testSet_PreservesExistingHiddenStates() {
        // Setup
        Long modalStateId = 1L;
        Long mainPageId = 2L;
        Long previouslyHiddenId = 3L;
        
        State modalState = new State.Builder("ModalDialog")
                .build();
        modalState.setId(modalStateId);
        modalState.setCanHideIds(new HashSet<>(Collections.singletonList(mainPageId)));
        modalState.addHiddenState(previouslyHiddenId); // Already has a hidden state
        
        when(stateService.getState(modalStateId)).thenReturn(Optional.of(modalState));
        when(stateMemory.getActiveStates()).thenReturn(
                new HashSet<>(Collections.singletonList(mainPageId))
        );
        
        // Execute
        boolean result = visibilityManager.set(modalStateId);
        
        // Verify
        assertTrue(result);
        verify(stateMemory).removeInactiveState(mainPageId);
        
        Set<Long> hiddenStates = modalState.getHiddenStateIds();
        assertEquals(2, hiddenStates.size());
        assertTrue(hiddenStates.contains(previouslyHiddenId));
        assertTrue(hiddenStates.contains(mainPageId));
    }
    
    @Test
    void testSet_EmptyCanHideList() {
        // Setup
        Long stateId = 1L;
        Long activeStateId = 2L;
        
        State state = new State.Builder("SimpleState")
                .build();
        state.setId(stateId);
        state.setCanHideIds(new HashSet<>()); // Empty can hide list
        
        when(stateService.getState(stateId)).thenReturn(Optional.of(state));
        when(stateMemory.getActiveStates()).thenReturn(
                new HashSet<>(Collections.singletonList(activeStateId))
        );
        
        // Execute
        boolean result = visibilityManager.set(stateId);
        
        // Verify
        assertTrue(result);
        verify(stateMemory, never()).removeInactiveState(any(String.class));
        assertTrue(state.getHiddenStateIds().isEmpty());
    }
    
    @Test
    void testSet_ConcurrentModificationHandling() {
        // Setup - Simulates concurrent modification scenario
        Long overlayId = 1L;
        Set<Long> activeStatesSet = new HashSet<>(Arrays.asList(2L, 3L, 4L, 5L));
        
        State overlay = new State.Builder("Overlay")
                .build();
        overlay.setId(overlayId);
        overlay.setCanHideIds(new HashSet<>(Arrays.asList(2L, 3L, 4L, 5L)));
        
        when(stateService.getState(overlayId)).thenReturn(Optional.of(overlay));
        when(stateMemory.getActiveStates()).thenReturn(activeStatesSet);
        
        // Execute
        boolean result = visibilityManager.set(overlayId);
        
        // Verify - Should handle iteration over copy without ConcurrentModificationException
        assertTrue(result);
        verify(stateMemory).removeInactiveState(2L);
        verify(stateMemory).removeInactiveState(3L);
        verify(stateMemory).removeInactiveState(4L);
        verify(stateMemory).removeInactiveState(5L);
        assertEquals(4, overlay.getHiddenStateIds().size());
    }
    
    @Test
    void testSet_ComplexScenario_TabSwitching() {
        // Setup - Simulating tab switching where new tab hides previous tab
        Long tab1Id = 1L;
        Long tab2Id = 2L;
        Long headerStateId = 3L;
        Long footerStateId = 4L;
        
        State tab2State = new State.Builder("Tab2")
                .build();
        tab2State.setId(tab2Id);
        // Tab2 can hide Tab1 but not header or footer
        tab2State.setCanHideIds(new HashSet<>(Collections.singletonList(tab1Id)));
        
        when(stateService.getState(tab2Id)).thenReturn(Optional.of(tab2State));
        when(stateMemory.getActiveStates()).thenReturn(
                new HashSet<>(Arrays.asList(tab1Id, headerStateId, footerStateId))
        );
        
        // Execute
        boolean result = visibilityManager.set(tab2Id);
        
        // Verify
        assertTrue(result);
        verify(stateMemory).removeInactiveState(tab1Id);
        verify(stateMemory, never()).removeInactiveState(headerStateId);
        verify(stateMemory, never()).removeInactiveState(footerStateId);
        
        assertEquals(1, tab2State.getHiddenStateIds().size());
        assertTrue(tab2State.getHiddenStateIds().contains(tab1Id));
    }
}