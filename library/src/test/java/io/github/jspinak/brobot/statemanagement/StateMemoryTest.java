package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for StateMemory - the runtime memory of active states.
 * Tests state tracking, transitions, and probability management.
 */
@DisplayName("StateMemory Tests")
public class StateMemoryTest extends BrobotTestBase {
    
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
    
    @Nested
    @DisplayName("Active State Management")
    class ActiveStateManagement {
        
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
        @DisplayName("Should add multiple active states")
        public void testAddMultipleActiveStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            assertEquals(3, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
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
        @DisplayName("Should replace active states")
        public void testReplaceActiveStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            Set<Long> newStates = new HashSet<>(Arrays.asList(3L, 4L, 5L));
            
            // Can't directly set, need to clear and add
            stateMemory.removeAllStates();
            for (Long stateId : newStates) {
                stateMemory.addActiveState(stateId);
            }
            
            assertEquals(3, stateMemory.getActiveStates().size());
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertFalse(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
            assertTrue(stateMemory.getActiveStates().contains(4L));
            assertTrue(stateMemory.getActiveStates().contains(5L));
        }
        
        @Test
        @DisplayName("Should remove inactive states from set")
        public void testRemoveInactiveStates() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            stateMemory.addActiveState(4L);
            
            Set<Long> inactiveStates = new HashSet<>(Arrays.asList(2L, 3L));
            stateMemory.removeInactiveStates(inactiveStates);
            
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertFalse(stateMemory.getActiveStates().contains(2L));
            assertFalse(stateMemory.getActiveStates().contains(3L));
            assertTrue(stateMemory.getActiveStates().contains(4L));
            assertEquals(2, stateMemory.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should remove inactive state by name")
        public void testRemoveInactiveStateByName() {
            State mockState = mock(State.class);
            when(mockState.getId()).thenReturn(1L);
            when(mockState.getName()).thenReturn("TestState");
            when(stateService.getState("TestState")).thenReturn(Optional.of(mockState));
            
            stateMemory.addActiveState(1L);
            stateMemory.removeInactiveState("TestState");
            
            assertFalse(stateMemory.getActiveStates().contains(1L));
        }
    }
    
    @Nested
    @DisplayName("State Object Retrieval")
    class StateObjectRetrieval {
        
        @Test
        @DisplayName("Should retrieve active state objects")
        public void testGetActiveStateObjects() {
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
            
            List<State> activeStates = stateMemory.getActiveStateList();
            
            assertEquals(2, activeStates.size());
            assertTrue(activeStates.contains(state1));
            assertTrue(activeStates.contains(state2));
        }
        
        @Test
        @DisplayName("Should handle missing state objects gracefully")
        public void testGetActiveStateObjectsWithMissing() {
            State state1 = mock(State.class);
            when(state1.getId()).thenReturn(1L);
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.empty()); // State not found
            when(stateService.getState(3L)).thenReturn(Optional.empty()); // State not found
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            List<State> activeStates = stateMemory.getActiveStateList();
            
            assertEquals(1, activeStates.size());
            assertTrue(activeStates.contains(state1));
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
        @DisplayName("Should get active state names as string")
        public void testGetActiveStateNamesAsString() {
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
            
            String namesString = stateMemory.getActiveStateNamesAsString();
            
            assertNotNull(namesString);
            assertTrue(namesString.contains("State1") || namesString.contains("State2"));
        }
    }
    
    @Nested
    @DisplayName("Match-Based State Adjustment")
    class MatchBasedAdjustment {
        
        @Test
        @DisplayName("Should adjust active states with matches")
        public void testAdjustActiveStatesWithMatches() {
            ActionResult matches = mock(ActionResult.class);
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            
            // Create mock matches with state object data
            Match match1 = mock(Match.class);
            Match match2 = mock(Match.class);
            StateObjectMetadata data1 = mock(StateObjectMetadata.class);
            StateObjectMetadata data2 = mock(StateObjectMetadata.class);
            
            when(data1.getOwnerStateId()).thenReturn(1L);
            when(data2.getOwnerStateId()).thenReturn(2L);
            when(match1.getStateObjectData()).thenReturn(data1);
            when(match2.getStateObjectData()).thenReturn(data2);
            
            List<Match> matchList = Arrays.asList(match1, match2);
            when(matches.getMatchList()).thenReturn(matchList);
            
            stateMemory.adjustActiveStatesWithMatches(matches);
            
            // Verify the states were processed
            verify(matches).getMatchList();
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
        }
        
        @Test
        @DisplayName("Should handle null matches gracefully")
        public void testAdjustActiveStatesWithNullMatches() {
            assertDoesNotThrow(() -> stateMemory.adjustActiveStatesWithMatches(null));
        }
    }
    
    @Nested
    @DisplayName("State Query Operations")
    class StateQueryOperations {
        
        @Test
        @DisplayName("Should check if state is active")
        public void testIsStateActive() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            assertTrue(isActive(1L));
            assertTrue(isActive(2L));
            assertFalse(isActive(3L));
        }
        
        @Test
        @DisplayName("Should check if any state from list is active")
        public void testIsAnyStateActive() {
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(4L);
            
            List<Long> checkList1 = Arrays.asList(1L, 2L, 3L);
            List<Long> checkList2 = Arrays.asList(5L, 6L, 7L);
            
            assertTrue(isAnyActive(checkList1));
            assertFalse(isAnyActive(checkList2));
        }
        
        @Test
        @DisplayName("Should check if all states from list are active")
        public void testAreAllStatesActive() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            
            List<Long> checkList1 = Arrays.asList(1L, 2L);
            List<Long> checkList2 = Arrays.asList(1L, 2L, 4L);
            
            assertTrue(areAllActive(checkList1));
            assertFalse(areAllActive(checkList2));
        }
        
        @Test
        @DisplayName("Should count active states")
        public void testCountActiveStates() {
            assertEquals(0, getActiveStateCount());
            
            stateMemory.addActiveState(1L);
            assertEquals(1, getActiveStateCount());
            
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
            assertEquals(3, getActiveStateCount());
            
            stateMemory.removeInactiveState(2L);
            assertEquals(2, getActiveStateCount());
        }
    }
    
    @Nested
    @DisplayName("Special State Handling")
    class SpecialStateHandling {
        
        @Test
        @DisplayName("Should handle NULL state properly")
        public void testHandleNullState() {
            State nullState = mock(State.class);
            when(nullState.getName()).thenReturn(SpecialStateType.NULL.toString());
            when(nullState.getId()).thenReturn(999L);
            
            when(stateService.getState(999L)).thenReturn(Optional.of(nullState));
            
            // NULL state should not affect active state tracking
            stateMemory.addActiveState(999L);
            
            // Verify NULL state handling logic
            assertTrue(stateMemory.getActiveStates().contains(999L));
        }
        
        @Test
        @DisplayName("Should handle empty state operations")
        public void testEmptyStateOperations() {
            // Test operations on empty state memory
            assertTrue(stateMemory.getActiveStateList().isEmpty());
            assertTrue(stateMemory.getActiveStateNames().isEmpty());
            assertNotNull(stateMemory.getActiveStateNamesAsString());
            assertEquals(0, getActiveStateCount());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle duplicate state additions")
        public void testDuplicateStateAdditions() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(1L); // Duplicate
            stateMemory.addActiveState(1L); // Another duplicate
            
            assertEquals(1, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
        }
        
        @Test
        @DisplayName("Should handle removal of non-existent state")
        public void testRemoveNonExistentState() {
            stateMemory.addActiveState(1L);
            
            // Should not throw when removing non-existent state
            assertDoesNotThrow(() -> stateMemory.removeInactiveState(99L));
            
            // Original state should still be there
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertEquals(1, stateMemory.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle empty set removal")
        public void testEmptySetRemoval() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            Set<Long> emptySet = new HashSet<>();
            stateMemory.removeInactiveStates(emptySet);
            
            // No states should be removed
            assertEquals(2, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
        }
    }
    
    // Helper methods for tests
    private boolean isActive(Long stateId) {
        return stateMemory.getActiveStates().contains(stateId);
    }
    
    private boolean isAnyActive(List<Long> stateIds) {
        for (Long id : stateIds) {
            if (stateMemory.getActiveStates().contains(id)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean areAllActive(List<Long> stateIds) {
        return stateMemory.getActiveStates().containsAll(stateIds);
    }
    
    private int getActiveStateCount() {
        return stateMemory.getActiveStates().size();
    }
}