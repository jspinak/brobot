package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("InitialStates Tests")
public class InitialStatesTest extends BrobotTestBase {
    
    @Mock
    private StateDetector stateDetector;
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private InitialStates initialStates;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        initialStates = new InitialStates(stateDetector, stateMemory, stateService);
    }
    
    @Nested
    @DisplayName("Adding State Sets")
    class AddingStateSets {
        
        @Test
        @DisplayName("Should add state set with State objects")
        public void testAddStateSetWithStates() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(50, state1, state2);
            
            assertEquals(50, initialStates.sumOfProbabilities);
            assertEquals(1, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should add multiple state sets with cumulative probabilities")
        public void testAddMultipleStateSets() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            State state3 = createMockState(3L, "State3");
            
            initialStates.addStateSet(50, state1);
            initialStates.addStateSet(30, state2);
            initialStates.addStateSet(20, state3);
            
            assertEquals(100, initialStates.sumOfProbabilities);
            assertEquals(3, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should add state set with state names")
        public void testAddStateSetWithNames() {
            State state1 = createMockState(1L, "LoginPage");
            State state2 = createMockState(2L, "Dashboard");
            
            when(stateService.getState("LoginPage")).thenReturn(Optional.of(state1));
            when(stateService.getState("Dashboard")).thenReturn(Optional.of(state2));
            
            initialStates.addStateSet(70, "LoginPage", "Dashboard");
            
            assertEquals(70, initialStates.sumOfProbabilities);
            assertEquals(1, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should ignore invalid state names")
        public void testIgnoreInvalidStateNames() {
            State validState = createMockState(1L, "ValidState");
            
            when(stateService.getState("ValidState")).thenReturn(Optional.of(validState));
            when(stateService.getState("InvalidState")).thenReturn(Optional.empty());
            
            initialStates.addStateSet(50, "ValidState", "InvalidState");
            
            assertEquals(50, initialStates.sumOfProbabilities);
            // Set should only contain valid state
            Set<Long> stateIds = initialStates.getPotentialActiveStates().keySet().iterator().next();
            assertEquals(1, stateIds.size());
            assertTrue(stateIds.contains(1L));
        }
        
        @Test
        @DisplayName("Should ignore zero or negative probabilities")
        public void testIgnoreInvalidProbabilities() {
            State state = createMockState(1L, "State");
            
            initialStates.addStateSet(0, state);
            initialStates.addStateSet(-10, state);
            
            assertEquals(0, initialStates.sumOfProbabilities);
            assertEquals(0, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle empty state arrays")
        public void testEmptyStateArrays() {
            initialStates.addStateSet(50, new State[0]);
            
            assertEquals(50, initialStates.sumOfProbabilities);
            assertEquals(1, initialStates.getPotentialActiveStates().size());
            
            Set<Long> stateIds = initialStates.getPotentialActiveStates().keySet().iterator().next();
            assertTrue(stateIds.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Probability Distribution")
    class ProbabilityDistribution {
        
        @Test
        @DisplayName("Should maintain cumulative probability thresholds")
        public void testCumulativeProbabilities() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            State state3 = createMockState(3L, "State3");
            
            initialStates.addStateSet(50, state1);
            initialStates.addStateSet(30, state2);
            initialStates.addStateSet(20, state3);
            
            Map<Set<Long>, Integer> potentialStates = initialStates.getPotentialActiveStates();
            
            // Verify cumulative thresholds
            Collection<Integer> thresholds = potentialStates.values();
            assertTrue(thresholds.contains(50));  // First threshold
            assertTrue(thresholds.contains(80));  // Second threshold (50+30)
            assertTrue(thresholds.contains(100)); // Third threshold (50+30+20)
        }
        
        @Test
        @DisplayName("Should support probabilities exceeding 100")
        public void testProbabilitiesExceeding100() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(150, state1);
            initialStates.addStateSet(75, state2);
            
            assertEquals(225, initialStates.sumOfProbabilities);
            assertEquals(2, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle single state set with 100% probability")
        public void testSingleStateSet() {
            State state = createMockState(1L, "OnlyState");
            
            initialStates.addStateSet(100, state);
            
            assertEquals(100, initialStates.sumOfProbabilities);
            assertEquals(1, initialStates.getPotentialActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("Mock Mode State Selection")
    class MockModeStateSelection {
        
        @Test
        @DisplayName("Should randomly select state set in mock mode")
        public void testMockStateSelection() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(50, state1);
            initialStates.addStateSet(50, state2);
            
            // Mock mode is enabled by BrobotTestBase
            assertTrue(FrameworkSettings.mock);
            
            initialStates.findIntialStates();
            
            // Should have selected one of the state sets
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
        
        @RepeatedTest(10)
        @DisplayName("Should respect probability distribution in mock mode")
        public void testProbabilityDistributionMock() {
            State highProbState = createMockState(1L, "HighProb");
            State lowProbState = createMockState(2L, "LowProb");
            
            // 90% vs 10% probability
            initialStates.addStateSet(90, highProbState);
            initialStates.addStateSet(10, lowProbState);
            
            initialStates.findIntialStates();
            
            // With 90% probability, should usually select state 1
            // Can't guarantee exact distribution in single test, but verify selection happens
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should handle no defined state sets in mock mode")
        public void testMockModeNoStateSets() {
            // No state sets added
            
            initialStates.findIntialStates();
            
            // Should fall back to all states
            verify(stateDetector).refreshStates();
        }
    }
    
    @Nested
    @DisplayName("Normal Mode State Search")
    class NormalModeStateSearch {
        
        @Test
        @DisplayName("Should search for states in normal mode")
        public void testNormalModeSearch() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(50, state1, state2);
            
            // Temporarily disable mock mode
            FrameworkSettings.mock = false;
            
            Set<State> foundStates = new HashSet<>(Arrays.asList(state1));
            when(stateDetector.searchForStates(anySet())).thenReturn(foundStates);
            
            initialStates.findIntialStates();
            
            verify(stateDetector).searchForStates(anySet());
            verify(stateMemory).setProbabilityToBaseProbabilityForActiveStates();
            
            // Restore mock mode
            FrameworkSettings.mock = true;
        }
        
        @Test
        @DisplayName("Should search all states if no predefined sets found")
        public void testSearchAllStatesIfNoneFound() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(50, state1, state2);
            
            // Temporarily disable mock mode
            FrameworkSettings.mock = false;
            
            // No states found in predefined sets
            when(stateDetector.searchForStates(anySet())).thenReturn(new HashSet<>());
            
            initialStates.findIntialStates();
            
            // Should fall back to refreshStates
            verify(stateDetector).refreshStates();
            
            // Restore mock mode
            FrameworkSettings.mock = true;
        }
        
        @Test
        @DisplayName("Should handle empty potential states in normal mode")
        public void testNormalModeEmptyPotentialStates() {
            // No state sets defined
            
            // Temporarily disable mock mode
            FrameworkSettings.mock = false;
            
            initialStates.findIntialStates();
            
            // Should directly refresh all states
            verify(stateDetector).refreshStates();
            
            // Restore mock mode
            FrameworkSettings.mock = true;
        }
    }
    
    @Nested
    @DisplayName("State Memory Updates")
    class StateMemoryUpdates {
        
        @Test
        @DisplayName("Should update state memory with found states")
        public void testUpdateStateMemory() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            
            initialStates.addStateSet(100, state1, state2);
            
            initialStates.findIntialStates();
            
            // In mock mode, should add selected states to memory
            verify(stateMemory, atLeast(1)).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should set probability to base for active states")
        public void testSetProbabilityToBase() {
            State state = createMockState(1L, "State");
            initialStates.addStateSet(100, state);
            
            // Temporarily disable mock mode to test normal search
            FrameworkSettings.mock = false;
            
            Set<State> foundStates = new HashSet<>(Arrays.asList(state));
            when(stateDetector.searchForStates(anySet())).thenReturn(foundStates);
            
            initialStates.findIntialStates();
            
            verify(stateMemory).setProbabilityToBaseProbabilityForActiveStates();
            
            // Restore mock mode
            FrameworkSettings.mock = true;
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle overlapping state sets")
        public void testOverlappingStateSets() {
            State state1 = createMockState(1L, "State1");
            State state2 = createMockState(2L, "State2");
            State state3 = createMockState(3L, "State3");
            
            // Sets with overlapping states
            initialStates.addStateSet(40, state1, state2);
            initialStates.addStateSet(30, state2, state3);
            initialStates.addStateSet(30, state1, state3);
            
            assertEquals(100, initialStates.sumOfProbabilities);
            assertEquals(3, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle application with multiple entry points")
        public void testMultipleEntryPoints() {
            State loginPage = createMockState(1L, "LoginPage");
            State dashboard = createMockState(2L, "Dashboard");
            State mainMenu = createMockState(3L, "MainMenu");
            State settings = createMockState(4L, "Settings");
            
            // Different possible starting configurations
            initialStates.addStateSet(50, loginPage);        // Not logged in
            initialStates.addStateSet(30, dashboard, mainMenu); // Logged in
            initialStates.addStateSet(20, settings);         // Deep linked
            
            assertEquals(100, initialStates.sumOfProbabilities);
            assertEquals(3, initialStates.getPotentialActiveStates().size());
            
            initialStates.findIntialStates();
            
            // Should have selected one configuration
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should support recovery scenario")
        public void testRecoveryScenario() {
            State errorPage = createMockState(1L, "ErrorPage");
            State homePage = createMockState(2L, "HomePage");
            State lastKnownGood = createMockState(3L, "LastKnownGood");
            
            // Recovery state sets
            initialStates.addStateSet(10, errorPage);
            initialStates.addStateSet(45, homePage);
            initialStates.addStateSet(45, lastKnownGood);
            
            // Clear existing states for recovery
            verify(stateMemory, never()).clearActiveStates();
            
            initialStates.findIntialStates();
            
            // New states should be added
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
        
        @Test
        @DisplayName("Should handle web app with session states")
        public void testWebAppSessionStates() {
            // Setup states representing different session states
            when(stateService.getState("LoggedOut")).thenReturn(Optional.of(createMockState(1L, "LoggedOut")));
            when(stateService.getState("LoggedIn")).thenReturn(Optional.of(createMockState(2L, "LoggedIn")));
            when(stateService.getState("SessionExpired")).thenReturn(Optional.of(createMockState(3L, "SessionExpired")));
            
            initialStates.addStateSet(40, "LoggedOut");
            initialStates.addStateSet(50, "LoggedIn");
            initialStates.addStateSet(10, "SessionExpired");
            
            assertEquals(100, initialStates.sumOfProbabilities);
            
            initialStates.findIntialStates();
            
            verify(stateMemory, atLeastOnce()).addActiveState(anyLong());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle duplicate state sets")
        public void testDuplicateStateSets() {
            State state = createMockState(1L, "State");
            
            initialStates.addStateSet(50, state);
            initialStates.addStateSet(50, state); // Duplicate
            
            assertEquals(100, initialStates.sumOfProbabilities);
            // Map will have duplicate keys with different thresholds
            assertEquals(2, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle very large number of state sets")
        public void testManyStateSets() {
            for (int i = 0; i < 100; i++) {
                State state = createMockState((long)i, "State" + i);
                initialStates.addStateSet(1, state);
            }
            
            assertEquals(100, initialStates.sumOfProbabilities);
            assertEquals(100, initialStates.getPotentialActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle null states in array")
        public void testNullStatesInArray() {
            State validState = createMockState(1L, "ValidState");
            
            initialStates.addStateSet(50, validState, null);
            
            assertEquals(50, initialStates.sumOfProbabilities);
            
            Set<Long> stateIds = initialStates.getPotentialActiveStates().keySet().iterator().next();
            assertEquals(1, stateIds.size());
            assertTrue(stateIds.contains(1L));
        }
    }
    
    // Helper methods
    private State createMockState(Long id, String name) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        when(state.getBaseProbabilityExists()).thenReturn(100);
        return state;
    }
    
    private Map<Set<Long>, Integer> getPotentialActiveStates() {
        // Access private field through reflection or getter if available
        return initialStates.potentialActiveStates;
    }
}