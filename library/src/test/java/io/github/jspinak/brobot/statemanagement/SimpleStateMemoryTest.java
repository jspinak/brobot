package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Simple StateMemory Tests")
public class SimpleStateMemoryTest extends BrobotTestBase {
    
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
    
    @Test
    @DisplayName("Should initialize with empty active states")
    public void testInitialState() {
        assertTrue(stateMemory.getActiveStates().isEmpty());
        assertTrue(stateMemory.getActiveStateList().isEmpty());
    }
    
    @Test
    @DisplayName("Should add state to active states")
    public void testAddActiveState() {
        State mockState = mock(State.class);
        when(mockState.getId()).thenReturn(1L);
        when(mockState.getName()).thenReturn("TestState");
        when(stateService.getState(1L)).thenReturn(Optional.of(mockState));
        
        stateMemory.addActiveState(1L);
        
        assertTrue(stateMemory.getActiveStates().contains(1L));
        assertEquals(1, stateMemory.getActiveStates().size());
    }
    
    @Test
    @DisplayName("Should remove state from active states")
    public void testRemoveActiveState() {
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);
        
        stateMemory.removeInactiveState(1L);
        
        assertFalse(stateMemory.getActiveStates().contains(1L));
        assertTrue(stateMemory.getActiveStates().contains(2L));
        assertEquals(1, stateMemory.getActiveStates().size());
    }
    
    @Test
    @DisplayName("Should clear all active states")
    public void testClearActiveStates() {
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);
        stateMemory.addActiveState(3L);
        
        stateMemory.removeAllStates();
        
        assertTrue(stateMemory.getActiveStates().isEmpty());
    }
    
    @Test
    @DisplayName("Should get active state names")
    public void testGetActiveStateNames() {
        State state1 = mock(State.class);
        State state2 = mock(State.class);
        
        when(state1.getId()).thenReturn(1L);
        when(state1.getName()).thenReturn("State1");
        when(state2.getId()).thenReturn(2L);
        when(state2.getName()).thenReturn("State2");
        
        when(stateService.getState(1L)).thenReturn(Optional.of(state1));
        when(stateService.getState(2L)).thenReturn(Optional.of(state2));
        
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);
        
        List<String> names = stateMemory.getActiveStateNames();
        
        assertEquals(2, names.size());
        assertTrue(names.contains("State1"));
        assertTrue(names.contains("State2"));
    }
    
    @Test
    @DisplayName("Should handle removing inactive states in bulk")
    public void testRemoveInactiveStatesInBulk() {
        stateMemory.addActiveState(1L);
        stateMemory.addActiveState(2L);
        stateMemory.addActiveState(3L);
        stateMemory.addActiveState(4L);
        
        Set<Long> toRemove = new HashSet<>(Arrays.asList(2L, 3L));
        stateMemory.removeInactiveStates(toRemove);
        
        assertEquals(2, stateMemory.getActiveStates().size());
        assertTrue(stateMemory.getActiveStates().contains(1L));
        assertTrue(stateMemory.getActiveStates().contains(4L));
        assertFalse(stateMemory.getActiveStates().contains(2L));
        assertFalse(stateMemory.getActiveStates().contains(3L));
    }
}