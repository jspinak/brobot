package io.github.jspinak.brobot.navigation.service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for StateService - manages states within the automation project.
 * Tests CRUD operations, name/ID resolution, bulk operations, and state validation.
 */
@DisplayName("StateService Tests")
public class StateServiceTest extends BrobotTestBase {
    
    @Mock
    private StateStore mockStateStore;
    
    @Mock
    private State mockState1;
    
    @Mock
    private State mockState2;
    
    @Mock
    private State mockState3;
    
    @Mock
    private State mockUnknownState;
    
    private StateService stateService;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        stateService = new StateService(mockStateStore);
        
        // Setup mock states
        when(mockState1.getId()).thenReturn(1L);
        when(mockState1.getName()).thenReturn("MainMenu");
        
        when(mockState2.getId()).thenReturn(2L);
        when(mockState2.getName()).thenReturn("LoginScreen");
        
        when(mockState3.getId()).thenReturn(3L);
        when(mockState3.getName()).thenReturn("Dashboard");
        
        when(mockUnknownState.getId()).thenReturn(0L);
        when(mockUnknownState.getName()).thenReturn("unknown");
    }
    
    @Nested
    @DisplayName("State Retrieval by Name")
    class StateRetrievalByName {
        
        @Test
        @DisplayName("Get existing state by name")
        public void testGetExistingStateByName() {
            when(mockStateStore.getState("MainMenu")).thenReturn(Optional.of(mockState1));
            
            Optional<State> result = stateService.getState("MainMenu");
            
            assertTrue(result.isPresent());
            assertEquals(mockState1, result.get());
            verify(mockStateStore).getState("MainMenu");
        }
        
        @Test
        @DisplayName("Get non-existent state by name returns empty")
        public void testGetNonExistentStateByName() {
            when(mockStateStore.getState("NonExistent")).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState("NonExistent");
            
            assertFalse(result.isPresent());
            verify(mockStateStore).getState("NonExistent");
        }
        
        @ParameterizedTest
        @NullSource
        @DisplayName("Get state with null name")
        public void testGetStateWithNullName(String name) {
            when(mockStateStore.getState((String) null)).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState(name);
            
            assertFalse(result.isPresent());
        }
        
        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("Get state with empty or whitespace name")
        public void testGetStateWithEmptyName(String name) {
            when(mockStateStore.getState(anyString())).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState(name);
            
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Get state with case-sensitive name")
        public void testCaseSensitiveName() {
            when(mockStateStore.getState("MainMenu")).thenReturn(Optional.of(mockState1));
            when(mockStateStore.getState("mainmenu")).thenReturn(Optional.empty());
            when(mockStateStore.getState("MAINMENU")).thenReturn(Optional.empty());
            
            assertTrue(stateService.getState("MainMenu").isPresent());
            assertFalse(stateService.getState("mainmenu").isPresent());
            assertFalse(stateService.getState("MAINMENU").isPresent());
        }
    }
    
    @Nested
    @DisplayName("State Retrieval by ID")
    class StateRetrievalById {
        
        @Test
        @DisplayName("Get existing state by ID")
        public void testGetExistingStateById() {
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(mockState1));
            
            Optional<State> result = stateService.getState(1L);
            
            assertTrue(result.isPresent());
            assertEquals(mockState1, result.get());
            verify(mockStateStore).getState(1L);
        }
        
        @Test
        @DisplayName("Get non-existent state by ID returns empty")
        public void testGetNonExistentStateById() {
            when(mockStateStore.getState(999L)).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState(999L);
            
            assertFalse(result.isPresent());
            verify(mockStateStore).getState(999L);
        }
        
        @Test
        @DisplayName("Get state with null ID")
        public void testGetStateWithNullId() {
            when(mockStateStore.getState((Long) null)).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState((Long) null);
            
            assertFalse(result.isPresent());
        }
        
        @ParameterizedTest
        @ValueSource(longs = {-1L, 0L, Long.MIN_VALUE, Long.MAX_VALUE})
        @DisplayName("Get state with edge case IDs")
        public void testGetStateWithEdgeCaseIds(Long id) {
            when(mockStateStore.getState(id)).thenReturn(Optional.empty());
            
            Optional<State> result = stateService.getState(id);
            
            assertFalse(result.isPresent());
            verify(mockStateStore).getState(id);
        }
    }
    
    @Nested
    @DisplayName("State Name Resolution")
    class StateNameResolution {
        
        @Test
        @DisplayName("Get state name for existing ID")
        public void testGetStateNameForExistingId() {
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(mockState1));
            
            String name = stateService.getStateName(1L);
            
            assertEquals("MainMenu", name);
            verify(mockStateStore).getState(1L);
        }
        
        @Test
        @DisplayName("Get state name for non-existent ID returns null")
        public void testGetStateNameForNonExistentId() {
            when(mockStateStore.getState(999L)).thenReturn(Optional.empty());
            
            String name = stateService.getStateName(999L);
            
            assertNull(name);
            verify(mockStateStore).getState(999L);
        }
        
        @Test
        @DisplayName("Get state name with null ID returns null")
        public void testGetStateNameWithNullId() {
            when(mockStateStore.getState((Long) null)).thenReturn(Optional.empty());
            
            String name = stateService.getStateName(null);
            
            assertNull(name);
        }
        
        @ParameterizedTest
        @CsvSource({
            "1, MainMenu",
            "2, LoginScreen",
            "3, Dashboard",
            "0, unknown"
        })
        @DisplayName("Get state names for multiple IDs")
        public void testGetStateNamesForMultipleIds(Long id, String expectedName) {
            State mockState = mock(State.class);
            when(mockState.getName()).thenReturn(expectedName);
            when(mockStateStore.getState(id)).thenReturn(Optional.of(mockState));
            
            String name = stateService.getStateName(id);
            
            assertEquals(expectedName, name);
        }
    }
    
    @Nested
    @DisplayName("State ID Resolution")
    class StateIdResolution {
        
        @Test
        @DisplayName("Get state ID for existing name")
        public void testGetStateIdForExistingName() {
            when(mockStateStore.getState("MainMenu")).thenReturn(Optional.of(mockState1));
            
            Long id = stateService.getStateId("MainMenu");
            
            assertEquals(1L, id);
            verify(mockStateStore).getState("MainMenu");
        }
        
        @Test
        @DisplayName("Get state ID for non-existent name returns null")
        public void testGetStateIdForNonExistentName() {
            when(mockStateStore.getState("NonExistent")).thenReturn(Optional.empty());
            
            Long id = stateService.getStateId("NonExistent");
            
            assertNull(id);
            verify(mockStateStore).getState("NonExistent");
        }
        
        @Test
        @DisplayName("Get state ID with null name returns null")
        public void testGetStateIdWithNullName() {
            when(mockStateStore.getState((String) null)).thenReturn(Optional.empty());
            
            Long id = stateService.getStateId(null);
            
            assertNull(id);
        }
        
        @ParameterizedTest
        @CsvSource({
            "MainMenu, 1",
            "LoginScreen, 2",
            "Dashboard, 3",
            "unknown, 0"
        })
        @DisplayName("Get state IDs for multiple names")
        public void testGetStateIdsForMultipleNames(String name, Long expectedId) {
            State mockState = mock(State.class);
            when(mockState.getId()).thenReturn(expectedId);
            when(mockStateStore.getState(name)).thenReturn(Optional.of(mockState));
            
            Long id = stateService.getStateId(name);
            
            assertEquals(expectedId, id);
        }
    }
    
    @Nested
    @DisplayName("Bulk State Operations")
    class BulkStateOperations {
        
        @Test
        @DisplayName("Get all states returns full list")
        public void testGetAllStates() {
            List<State> allStates = Arrays.asList(mockState1, mockState2, mockState3);
            when(mockStateStore.getAllStates()).thenReturn(allStates);
            
            List<State> result = stateService.getAllStates();
            
            assertEquals(3, result.size());
            assertTrue(result.contains(mockState1));
            assertTrue(result.contains(mockState2));
            assertTrue(result.contains(mockState3));
            verify(mockStateStore).getAllStates();
        }
        
        @Test
        @DisplayName("Get all states when empty returns empty list")
        public void testGetAllStatesWhenEmpty() {
            when(mockStateStore.getAllStates()).thenReturn(new ArrayList<>());
            
            List<State> result = stateService.getAllStates();
            
            assertTrue(result.isEmpty());
            verify(mockStateStore).getAllStates();
        }
        
        @Test
        @DisplayName("Get all state IDs")
        public void testGetAllStateIds() {
            List<State> allStates = Arrays.asList(mockState1, mockState2, mockState3);
            when(mockStateStore.getAllStates()).thenReturn(allStates);
            
            List<Long> result = stateService.getAllStateIds();
            
            assertEquals(3, result.size());
            assertTrue(result.contains(1L));
            assertTrue(result.contains(2L));
            assertTrue(result.contains(3L));
        }
        
        @Test
        @DisplayName("Get all state IDs when empty")
        public void testGetAllStateIdsWhenEmpty() {
            when(mockStateStore.getAllStates()).thenReturn(new ArrayList<>());
            
            List<Long> result = stateService.getAllStateIds();
            
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Get all state IDs preserves order")
        public void testGetAllStateIdsPreservesOrder() {
            List<State> orderedStates = Arrays.asList(mockState3, mockState1, mockState2);
            when(mockStateStore.getAllStates()).thenReturn(orderedStates);
            
            List<Long> result = stateService.getAllStateIds();
            
            assertEquals(Arrays.asList(3L, 1L, 2L), result);
        }
    }
    
    @Nested
    @DisplayName("Unknown State Validation")
    class UnknownStateValidation {
        
        @Test
        @DisplayName("Only unknown state exists returns true")
        public void testOnlyUnknownStateExists() {
            List<State> onlyUnknown = Arrays.asList(mockUnknownState);
            when(mockStateStore.getAllStates()).thenReturn(onlyUnknown);
            
            boolean result = stateService.onlyTheUnknownStateExists();
            
            assertTrue(result);
            verify(mockStateStore).getAllStates();
        }
        
        @Test
        @DisplayName("Multiple states including unknown returns false")
        public void testMultipleStatesIncludingUnknown() {
            List<State> multipleStates = Arrays.asList(mockUnknownState, mockState1);
            when(mockStateStore.getAllStates()).thenReturn(multipleStates);
            
            boolean result = stateService.onlyTheUnknownStateExists();
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("No unknown state returns false")
        public void testNoUnknownState() {
            List<State> noUnknown = Arrays.asList(mockState1, mockState2);
            when(mockStateStore.getAllStates()).thenReturn(noUnknown);
            
            boolean result = stateService.onlyTheUnknownStateExists();
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Empty state list returns false")
        public void testEmptyStateList() {
            when(mockStateStore.getAllStates()).thenReturn(new ArrayList<>());
            
            boolean result = stateService.onlyTheUnknownStateExists();
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("State named unknown but not first returns false")
        public void testUnknownNotFirst() {
            State anotherUnknown = mock(State.class);
            when(anotherUnknown.getName()).thenReturn("unknown");
            List<State> states = Arrays.asList(mockState1, anotherUnknown);
            when(mockStateStore.getAllStates()).thenReturn(states);
            
            boolean result = stateService.onlyTheUnknownStateExists();
            
            assertFalse(result);
        }
    }
    
    @Nested
    @DisplayName("State Collection Operations")
    class StateCollectionOperations {
        
        @Test
        @DisplayName("Get states by ID set")
        public void testGetStatesByIdSet() {
            Set<Long> ids = new HashSet<>(Arrays.asList(1L, 2L, 3L));
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(mockState1));
            when(mockStateStore.getState(2L)).thenReturn(Optional.of(mockState2));
            when(mockStateStore.getState(3L)).thenReturn(Optional.of(mockState3));
            
            Set<State> result = stateService.findSetById(ids.toArray(new Long[0]));
            
            assertEquals(3, result.size());
            assertTrue(result.contains(mockState1));
            assertTrue(result.contains(mockState2));
            assertTrue(result.contains(mockState3));
        }
        
        @Test
        @DisplayName("Get states with some missing IDs")
        public void testGetStatesWithSomeMissing() {
            Set<Long> ids = new HashSet<>(Arrays.asList(1L, 999L, 2L));
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(mockState1));
            when(mockStateStore.getState(999L)).thenReturn(Optional.empty());
            when(mockStateStore.getState(2L)).thenReturn(Optional.of(mockState2));
            
            Set<State> result = stateService.findSetById(ids.toArray(new Long[0]));
            
            assertEquals(2, result.size());
            assertTrue(result.contains(mockState1));
            assertTrue(result.contains(mockState2));
        }
        
        @Test
        @DisplayName("Get states with empty ID set")
        public void testGetStatesWithEmptySet() {
            // Using findSetById with empty array instead of non-existent getStates method
            Set<State> result = stateService.findSetById();
            
            assertTrue(result.isEmpty());
            verify(mockStateStore, never()).getState(anyLong());
        }
        
        @Test
        @DisplayName("Get states with null ID set")
        public void testGetStatesWithNullSet() {
            // Using findSetById with null instead of non-existent getStates method
            Set<State> result = stateService.findSetById((Long[]) null);
            
            assertTrue(result.isEmpty());
            verify(mockStateStore, never()).getState(anyLong());
        }
        
        @Test
        @DisplayName("Get state array by IDs")
        public void testGetStateArrayByIds() {
            Long[] ids = {1L, 2L, 3L};
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(mockState1));
            when(mockStateStore.getState(2L)).thenReturn(Optional.of(mockState2));
            when(mockStateStore.getState(3L)).thenReturn(Optional.of(mockState3));
            
            // Convert Set to array since getStates(Long[]) doesn't exist
            Set<State> resultSet = stateService.findSetById(ids);
            State[] result = resultSet.toArray(new State[0]);
            
            assertEquals(3, result.length);
            assertTrue(resultSet.contains(mockState1));
            assertTrue(resultSet.contains(mockState2));
            assertTrue(resultSet.contains(mockState3));
        }
    }
    
    @Nested
    @DisplayName("Save State Operations")
    class SaveStateOperations {
        
        @Test
        @DisplayName("Save new state")
        public void testSaveNewState() {
            State newState = mock(State.class);
            when(newState.getName()).thenReturn("NewState");
            
            stateService.save(newState);
            
            verify(mockStateStore).save(newState);
        }
        
        @Test
        @DisplayName("Save null state")
        public void testSaveNullState() {
            stateService.save(null);
            
            // save method returns early for null
        }
        
        @Test
        @DisplayName("Save multiple states")
        public void testSaveMultipleStates() {
            State[] states = {mockState1, mockState2, mockState3};
            
            for (State state : states) {
                stateService.save(state);
            }
            
            verify(mockStateStore).save(mockState1);
            verify(mockStateStore).save(mockState2);
            verify(mockStateStore).save(mockState3);
        }
    }
    
    @Nested
    @DisplayName("Delete State Operations")
    class DeleteStateOperations {
        
        @Test
        @DisplayName("Delete all states")
        public void testDeleteAllStates() {
            stateService.deleteAllStates();
            
            verify(mockStateStore).deleteAll();
        }
    }
    
    // Visit Counter Operations removed - resetVisitCounts method doesn't exist in StateService
    
    @Nested
    @DisplayName("Performance and Edge Cases")
    class PerformanceAndEdgeCases {
        
        @Test
        @DisplayName("Handle large number of states")
        public void testLargeNumberOfStates() {
            List<State> manyStates = LongStream.range(1, 1001)
                .mapToObj(id -> {
                    State state = mock(State.class);
                    when(state.getId()).thenReturn(id);
                    when(state.getName()).thenReturn("State" + id);
                    return state;
                })
                .collect(Collectors.toList());
            
            when(mockStateStore.getAllStates()).thenReturn(manyStates);
            
            List<State> result = stateService.getAllStates();
            List<Long> ids = stateService.getAllStateIds();
            
            assertEquals(1000, result.size());
            assertEquals(1000, ids.size());
        }
        
        @Test
        @DisplayName("Handle concurrent access")
        public void testConcurrentAccess() throws InterruptedException {
            when(mockStateStore.getState(anyLong())).thenReturn(Optional.of(mockState1));
            
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    Optional<State> state = stateService.getState((long) index);
                    results[index] = state.isPresent();
                });
                threads[i].start();
            }
            
            for (Thread thread : threads) {
                thread.join();
            }
            
            for (boolean result : results) {
                assertTrue(result);
            }
            
            verify(mockStateStore, times(threadCount)).getState(anyLong());
        }
        
        @Test
        @DisplayName("Handle special characters in state names")
        public void testSpecialCharactersInNames() {
            String specialName = "State-With_Special.Characters!@#$%";
            State specialState = mock(State.class);
            when(specialState.getName()).thenReturn(specialName);
            when(mockStateStore.getState(specialName)).thenReturn(Optional.of(specialState));
            
            Optional<State> result = stateService.getState(specialName);
            
            assertTrue(result.isPresent());
            assertEquals(specialName, result.get().getName());
        }
    }
}