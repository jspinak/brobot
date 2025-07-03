package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialStatesTest {

    @Mock
    private StateDetector stateDetector;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private StateService stateService;
    
    private InitialStates initialStates;
    
    @BeforeEach
    void setUp() {
        initialStates = new InitialStates(stateDetector, stateMemory, stateService);
    }
    
    @Test
    void testAddStateSet_WithStates() {
        // Setup
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        
        // Execute
        initialStates.addStateSet(50, state1, state2);
        
        // Verify
        assertEquals(50, initialStates.sumOfProbabilities);
        // Note: potentialActiveStates is private, so we can't directly verify it
    }
    
    @Test
    void testAddStateSet_MultipleSets() {
        // Setup
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        State state3 = createState(3L, "State3");
        
        // Execute
        initialStates.addStateSet(30, state1);
        initialStates.addStateSet(50, state2, state3);
        initialStates.addStateSet(20, state1, state2, state3);
        
        // Verify
        assertEquals(100, initialStates.sumOfProbabilities);
    }
    
    @Test
    void testAddStateSet_WithZeroProbability() {
        // Setup
        State state1 = createState(1L, "State1");
        
        // Execute
        initialStates.addStateSet(0, state1);
        
        // Verify - Should be ignored
        assertEquals(0, initialStates.sumOfProbabilities);
    }
    
    @Test
    void testAddStateSet_WithNegativeProbability() {
        // Setup
        State state1 = createState(1L, "State1");
        
        // Execute
        initialStates.addStateSet(-10, state1);
        
        // Verify - Should be ignored
        assertEquals(0, initialStates.sumOfProbabilities);
    }
    
    @Test
    void testAddStateSet_WithStateNames() {
        // Setup
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        
        lenient().when(stateService.getState("State1")).thenReturn(Optional.of(state1));
        lenient().when(stateService.getState("State2")).thenReturn(Optional.of(state2));
        lenient().when(stateService.getState("InvalidState")).thenReturn(Optional.empty());
        
        // Execute
        initialStates.addStateSet(40, "State1", "State2", "InvalidState");
        
        // Verify
        assertEquals(40, initialStates.sumOfProbabilities);
        verify(stateService).getState("State1");
        verify(stateService).getState("State2");
        verify(stateService).getState("InvalidState");
    }
    
    @Test
    void testFindInitialStates_NormalMode() {
        // Setup
        FrameworkSettings.mock = false;
        
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        
        lenient().when(stateService.getState("State1")).thenReturn(Optional.of(state1));
        lenient().when(stateService.getState("State2")).thenReturn(Optional.of(state2));
        
        initialStates.addStateSet(60, "State1");
        initialStates.addStateSet(40, "State2");
        
        lenient().when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(1L)));
        
        // Execute
        initialStates.findIntialStates();
        
        // Verify - Should search for both states
        verify(stateDetector, atLeastOnce()).findState(1L);
        verify(stateDetector, atLeastOnce()).findState(2L);
    }
    
    @Test
    void testFindInitialStates_NormalMode_NoActiveStatesFound() {
        // Setup
        FrameworkSettings.mock = false;
        
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        State state3 = createState(3L, "State3");
        
        lenient().when(stateService.getState("State1")).thenReturn(Optional.of(state1));
        lenient().when(stateService.getState("State2")).thenReturn(Optional.of(state2));
        
        initialStates.addStateSet(50, "State1", "State2");
        
        // First search finds nothing
        lenient().when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
        
        // All states in system
        lenient().when(stateService.getAllStateIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        
        // Execute
        initialStates.findIntialStates();
        
        // Verify - Should search for predefined states first, then all states
        verify(stateDetector, atLeastOnce()).findState(1L);
        verify(stateDetector, atLeastOnce()).findState(2L);
        verify(stateDetector, atLeastOnce()).findState(3L); // Fallback search
    }
    
    @Test
    void testFindInitialStates_MockMode_SingleStateSet() {
        // Setup
        FrameworkSettings.mock = true;
        
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        
        lenient().when(stateService.getState(1L)).thenReturn(Optional.of(state1));
        lenient().when(stateService.getState(2L)).thenReturn(Optional.of(state2));
        
        initialStates.addStateSet(100, state1, state2);
        
        // Execute
        initialStates.findIntialStates();
        
        // Verify - In mock mode, should activate states without searching
        verify(stateMemory).addActiveState(1L, true);
        verify(stateMemory).addActiveState(2L, true);
        verify(state1).setProbabilityToBaseProbability();
        verify(state2).setProbabilityToBaseProbability();
        verify(stateDetector, never()).findState(anyLong());
    }
    
    @Test
    void testFindInitialStates_MockMode_EmptyStateSets() {
        // Setup
        FrameworkSettings.mock = true;
        
        // Execute - No state sets added
        initialStates.findIntialStates();
        
        // Verify - Should handle gracefully
        verify(stateMemory, never()).addActiveState(any(), anyBoolean());
        verify(stateDetector, never()).findState(anyLong());
    }
    
    @Test
    void testFindInitialStates_MockMode_MultipleStateSets() {
        // Setup
        FrameworkSettings.mock = true;
        
        State state1 = createState(1L, "State1");
        State state2 = createState(2L, "State2");
        State state3 = createState(3L, "State3");
        
        lenient().when(stateService.getState(1L)).thenReturn(Optional.of(state1));
        lenient().when(stateService.getState(2L)).thenReturn(Optional.of(state2));
        lenient().when(stateService.getState(3L)).thenReturn(Optional.of(state3));
        
        // Add multiple state sets with different probabilities
        initialStates.addStateSet(30, state1);      // 1-30
        initialStates.addStateSet(50, state2);      // 31-80
        initialStates.addStateSet(20, state3);      // 81-100
        
        lenient().when(stateMemory.getActiveStateNames()).thenReturn(new ArrayList<>());
        
        // Execute
        initialStates.findIntialStates();
        
        // Verify - One of the sets should be activated
        // We can't predict which one due to randomness, but exactly one should be chosen
        int totalActivations = 0;
        if (mockingDetails(stateMemory).getInvocations().stream()
                .anyMatch(inv -> inv.getMethod().getName().equals("addActiveState") && 
                         inv.getArguments()[0].equals(1L))) {
            totalActivations++;
        }
        if (mockingDetails(stateMemory).getInvocations().stream()
                .anyMatch(inv -> inv.getMethod().getName().equals("addActiveState") && 
                         inv.getArguments()[0].equals(2L))) {
            totalActivations++;
        }
        if (mockingDetails(stateMemory).getInvocations().stream()
                .anyMatch(inv -> inv.getMethod().getName().equals("addActiveState") && 
                         inv.getArguments()[0].equals(3L))) {
            totalActivations++;
        }
        
        assertTrue(totalActivations > 0, "At least one state should be activated");
    }
    
    @Test
    void testComplexScenario_MixedStateAddition() {
        // Setup
        FrameworkSettings.mock = false;
        
        State loginState = createState(1L, "Login");
        State dashboardState = createState(2L, "Dashboard");
        State settingsState = createState(3L, "Settings");
        
        lenient().when(stateService.getState("Login")).thenReturn(Optional.of(loginState));
        lenient().when(stateService.getState("Dashboard")).thenReturn(Optional.of(dashboardState));
        lenient().when(stateService.getState("Settings")).thenReturn(Optional.of(settingsState));
        
        // Add states using both methods
        initialStates.addStateSet(70, loginState);
        initialStates.addStateSet(20, "Dashboard", "Settings");
        initialStates.addStateSet(10, dashboardState);
        
        lenient().when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());
        lenient().when(stateService.getAllStateIds()).thenReturn(Arrays.asList(1L, 2L, 3L));
        
        // Execute
        initialStates.findIntialStates();
        
        // Verify - All states should be searched
        verify(stateDetector, times(2)).findState(1L); // Once in initial, once in fallback
        verify(stateDetector, times(2)).findState(2L);
        verify(stateDetector, times(2)).findState(3L);
    }
    
    private State createState(Long id, String name) {
        State state = mock(State.class);
        lenient().when(state.getId()).thenReturn(id);
        lenient().when(state.getName()).thenReturn(name);
        return state;
    }
}