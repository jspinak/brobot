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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateMemory.
 * Tests runtime state tracking and management functionality.
 */
@DisplayName("StateMemory Tests")
public class StateMemoryTest extends BrobotTestBase {
    
    @Mock
    private StateService stateService;
    
    private StateMemory stateMemory;
    private AutoCloseable mocks;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);
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
            when(state1.getName()).thenReturn("State1");
            when(state2.getName()).thenReturn("State2");
            when(state3.getName()).thenReturn("State3");
            
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
        @DisplayName("Should not add duplicate active states")
        public void testNoDuplicateActiveStates() {
            State mockState = mock(State.class);
            when(mockState.getId()).thenReturn(1L);
            when(stateService.getState(1L)).thenReturn(Optional.of(mockState));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(1L); // Add same state again
            
            assertEquals(1, stateMemory.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should not add NULL special state")
        public void testDoNotAddNullState() {
            stateMemory.addActiveState(SpecialStateType.NULL.getId()); // NULL state ID is -5L
            
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
        
        @Test
        @DisplayName("Should add state with newLine parameter")
        public void testAddActiveStateWithNewLine() {
            State mockState = mock(State.class);
            when(mockState.getId()).thenReturn(1L);
            when(mockState.getName()).thenReturn("TestState");
            when(stateService.getState(1L)).thenReturn(Optional.of(mockState));
            
            stateMemory.addActiveState(1L, true);
            
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertEquals(1, stateMemory.getActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("State Removal")
    class StateRemoval {
        
        @BeforeEach
        void setupActiveStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            when(state1.getName()).thenReturn("State1");
            when(state2.getName()).thenReturn("State2");
            when(state3.getName()).thenReturn("State3");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
            when(stateService.getState("State1")).thenReturn(Optional.of(state1));
            when(stateService.getState("State2")).thenReturn(Optional.of(state2));
            when(stateService.getState("State3")).thenReturn(Optional.of(state3));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            stateMemory.addActiveState(3L);
        }
        
        @Test
        @DisplayName("Should remove inactive state by ID")
        public void testRemoveInactiveStateById() {
            stateMemory.removeInactiveState(2L);
            
            assertEquals(2, stateMemory.getActiveStates().size());
            assertFalse(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
        }
        
        @Test
        @DisplayName("Should remove inactive state by name")
        public void testRemoveInactiveStateByName() {
            when(stateService.getStateId("State2")).thenReturn(2L);
            
            stateMemory.removeInactiveState("State2");
            
            assertEquals(2, stateMemory.getActiveStates().size());
            assertFalse(stateMemory.getActiveStates().contains(2L));
        }
        
        @Test
        @DisplayName("Should remove multiple inactive states")
        public void testRemoveMultipleInactiveStates() {
            Set<Long> inactiveStates = new HashSet<>(Arrays.asList(1L, 3L));
            
            stateMemory.removeInactiveStates(inactiveStates);
            
            assertEquals(1, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertFalse(stateMemory.getActiveStates().contains(1L));
            assertFalse(stateMemory.getActiveStates().contains(3L));
        }
        
        @Test
        @DisplayName("Should remove all states")
        public void testRemoveAllStates() {
            stateMemory.removeAllStates();
            
            assertTrue(stateMemory.getActiveStates().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle removing non-existent state")
        public void testRemoveNonExistentState() {
            int initialSize = stateMemory.getActiveStates().size();
            
            stateMemory.removeInactiveState(999L);
            
            assertEquals(initialSize, stateMemory.getActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("State Retrieval")
    class StateRetrieval {
        
        @BeforeEach
        void setupActiveStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state1.getName()).thenReturn("LoginState");
            when(state2.getName()).thenReturn("DashboardState");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
        }
        
        @Test
        @DisplayName("Should get active state list")
        public void testGetActiveStateList() {
            List<State> activeStateList = stateMemory.getActiveStateList();
            
            assertNotNull(activeStateList);
            assertEquals(2, activeStateList.size());
            
            List<String> names = new ArrayList<>();
            for (State state : activeStateList) {
                names.add(state.getName());
            }
            assertTrue(names.contains("LoginState"));
            assertTrue(names.contains("DashboardState"));
        }
        
        @Test
        @DisplayName("Should get active state names")
        public void testGetActiveStateNames() {
            List<String> names = stateMemory.getActiveStateNames();
            
            assertNotNull(names);
            assertEquals(2, names.size());
            assertTrue(names.contains("LoginState"));
            assertTrue(names.contains("DashboardState"));
        }
        
        @Test
        @DisplayName("Should get active state names as string")
        public void testGetActiveStateNamesAsString() {
            String namesString = stateMemory.getActiveStateNamesAsString();
            
            assertNotNull(namesString);
            assertTrue(namesString.contains("LoginState"));
            assertTrue(namesString.contains("DashboardState"));
        }
        
        @Test
        @DisplayName("Should return empty list when no active states")
        public void testGetActiveStateListWhenEmpty() {
            stateMemory.removeAllStates();
            
            List<State> activeStateList = stateMemory.getActiveStateList();
            List<String> names = stateMemory.getActiveStateNames();
            String namesString = stateMemory.getActiveStateNamesAsString();
            
            assertNotNull(activeStateList);
            assertTrue(activeStateList.isEmpty());
            assertNotNull(names);
            assertTrue(names.isEmpty());
            assertNotNull(namesString);
            assertTrue(namesString.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle missing states gracefully")
        public void testHandleMissingStates() {
            // Add an ID that doesn't exist in StateService
            when(stateService.getState(999L)).thenReturn(Optional.empty());
            stateMemory.getActiveStates().add(999L);
            
            List<State> activeStateList = stateMemory.getActiveStateList();
            
            // Should only return valid states
            assertEquals(2, activeStateList.size());
        }
    }
    
    @Nested
    @DisplayName("Match Integration")
    class MatchIntegration {
        
        @BeforeEach
        void setupStates() {
            State state1 = mock(State.class);
            State state2 = mock(State.class);
            State state3 = mock(State.class);
            
            when(state1.getId()).thenReturn(1L);
            when(state2.getId()).thenReturn(2L);
            when(state3.getId()).thenReturn(3L);
            when(state1.getName()).thenReturn("State1");
            when(state2.getName()).thenReturn("State2");
            when(state3.getName()).thenReturn("State3");
            
            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateService.getState(2L)).thenReturn(Optional.of(state2));
            when(stateService.getState(3L)).thenReturn(Optional.of(state3));
        }
        
        @Test
        @DisplayName("Should adjust active states with matches")
        public void testAdjustActiveStatesWithMatches() {
            // Setup initial active states
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            // Create matches with states found
            ActionResult matches = new ActionResult();
            
            // Create match with state 2
            Match match1 = mock(Match.class);
            StateObjectMetadata metadata1 = mock(StateObjectMetadata.class);
            when(metadata1.getOwnerStateId()).thenReturn(2L);
            when(match1.getStateObjectData()).thenReturn(metadata1);
            
            // Create match with state 3
            Match match2 = mock(Match.class);
            StateObjectMetadata metadata2 = mock(StateObjectMetadata.class);
            when(metadata2.getOwnerStateId()).thenReturn(3L);
            when(match2.getStateObjectData()).thenReturn(metadata2);
            
            matches.add(match1);
            matches.add(match2);
            
            // Adjust active states based on matches
            stateMemory.adjustActiveStatesWithMatches(matches);
            
            // States 1, 2, and 3 should all be active
            // (adjustActiveStatesWithMatches only adds, doesn't remove)
            assertEquals(3, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
            assertTrue(stateMemory.getActiveStates().contains(3L));
        }
        
        @Test
        @DisplayName("Should handle empty matches")
        public void testAdjustActiveStatesWithEmptyMatches() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            
            ActionResult matches = new ActionResult();
            // No matches added - empty match list
            
            stateMemory.adjustActiveStatesWithMatches(matches);
            
            // States should remain unchanged (method only adds, doesn't remove)
            assertEquals(2, stateMemory.getActiveStates().size());
            assertTrue(stateMemory.getActiveStates().contains(1L));
            assertTrue(stateMemory.getActiveStates().contains(2L));
        }
        
        @Test
        @DisplayName("Should handle null matches")
        public void testAdjustActiveStatesWithNullMatches() {
            stateMemory.addActiveState(1L);
            stateMemory.addActiveState(2L);
            int initialSize = stateMemory.getActiveStates().size();
            
            ActionResult matches = new ActionResult();
            // Don't set state IDs, leaving it null
            
            stateMemory.adjustActiveStatesWithMatches(matches);
            
            // Should handle null gracefully
            assertTrue(stateMemory.getActiveStates().size() <= initialSize);
        }
    }
    
    @Nested
    @DisplayName("Special State Handling")
    class SpecialStateHandling {
        
        @Test
        @DisplayName("Should handle PREVIOUS state enum")
        public void testPreviousStateEnum() {
            assertNotNull(StateMemory.Enum.PREVIOUS);
            assertEquals("PREVIOUS", StateMemory.Enum.PREVIOUS.toString());
        }
        
        @Test
        @DisplayName("Should handle CURRENT state enum")
        public void testCurrentStateEnum() {
            assertNotNull(StateMemory.Enum.CURRENT);
            assertEquals("CURRENT", StateMemory.Enum.CURRENT.toString());
        }
        
        @Test
        @DisplayName("Should handle EXPECTED state enum")
        public void testExpectedStateEnum() {
            assertNotNull(StateMemory.Enum.EXPECTED);
            assertEquals("EXPECTED", StateMemory.Enum.EXPECTED.toString());
        }
        
        @ParameterizedTest
        @ValueSource(longs = {-5L})
        @DisplayName("Should not add NULL special state")
        public void testNullStateNotAdded(long stateId) {
            stateMemory.addActiveState(stateId);
            
            assertFalse(stateMemory.getActiveStates().contains(stateId));
        }
    }
    
    @Nested
    @DisplayName("Concurrency and Thread Safety")
    class ConcurrencyTests {
        
        @Test
        @DisplayName("Should handle concurrent state additions")
        public void testConcurrentStateAdditions() throws InterruptedException {
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final long stateId = i + 1;
                State mockState = mock(State.class);
                when(mockState.getId()).thenReturn(stateId);
                when(stateService.getState(stateId)).thenReturn(Optional.of(mockState));
                
                threads[i] = new Thread(() -> {
                    stateMemory.addActiveState(stateId);
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // All states should be added
            assertEquals(threadCount, stateMemory.getActiveStates().size());
        }
    }
}