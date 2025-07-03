package io.github.jspinak.brobot.navigation.service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StateServiceTest {

    @Mock
    private StateStore stateStore;
    
    private StateService stateService;
    
    private State testState1;
    private State testState2;
    private State unknownState;
    
    @BeforeEach
    void setUp() {
        stateService = new StateService(stateStore);
        
        // Create test states
        testState1 = new State.Builder("state1").build();
        testState1.setId(1L);
                
        testState2 = new State.Builder("state2").build();
        testState2.setId(2L);
                
        unknownState = new State.Builder("unknown").build();
        unknownState.setId(999L);
    }
    
    @Test
    void getState_byName_returnsStateWhenFound() {
        // Arrange
        when(stateStore.getState("state1")).thenReturn(Optional.of(testState1));
        
        // Act
        Optional<State> result = stateService.getState("state1");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testState1, result.get());
        verify(stateStore).getState("state1");
    }
    
    @Test
    void getState_byName_returnsEmptyWhenNotFound() {
        // Arrange
        when(stateStore.getState("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Optional<State> result = stateService.getState("nonexistent");
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore).getState("nonexistent");
    }
    
    @Test
    void getState_byId_returnsStateWhenFound() {
        // Arrange
        when(stateStore.getState(1L)).thenReturn(Optional.of(testState1));
        
        // Act
        Optional<State> result = stateService.getState(1L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testState1, result.get());
        verify(stateStore).getState(1L);
    }
    
    @Test
    void getState_byId_returnsEmptyWhenNotFound() {
        // Arrange
        when(stateStore.getState(999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<State> result = stateService.getState(999L);
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore).getState(999L);
    }
    
    @Test
    void getStateName_returnsNameWhenStateExists() {
        // Arrange
        when(stateStore.getState(1L)).thenReturn(Optional.of(testState1));
        
        // Act
        String name = stateService.getStateName(1L);
        
        // Assert
        assertEquals("state1", name);
        verify(stateStore).getState(1L);
    }
    
    @Test
    void getStateName_returnsNullWhenStateNotFound() {
        // Arrange
        when(stateStore.getState(999L)).thenReturn(Optional.empty());
        
        // Act
        String name = stateService.getStateName(999L);
        
        // Assert
        assertNull(name);
        verify(stateStore).getState(999L);
    }
    
    @Test
    void getStateId_returnsIdWhenStateExists() {
        // Arrange
        when(stateStore.getState("state1")).thenReturn(Optional.of(testState1));
        
        // Act
        Long id = stateService.getStateId("state1");
        
        // Assert
        assertEquals(1L, id);
        verify(stateStore).getState("state1");
    }
    
    @Test
    void getStateId_returnsNullWhenStateNotFound() {
        // Arrange
        when(stateStore.getState("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Long id = stateService.getStateId("nonexistent");
        
        // Assert
        assertNull(id);
        verify(stateStore).getState("nonexistent");
    }
    
    @Test
    void getAllStates_returnsAllStatesFromStore() {
        // Arrange
        List<State> states = Arrays.asList(testState1, testState2);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        List<State> result = stateService.getAllStates();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(testState1));
        assertTrue(result.contains(testState2));
        verify(stateStore).getAllStates();
    }
    
    @Test
    void onlyTheUnknownStateExists_returnsTrueWhenOnlyUnknownExists() {
        // Arrange
        List<State> states = Collections.singletonList(unknownState);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        boolean result = stateService.onlyTheUnknownStateExists();
        
        // Assert
        assertTrue(result);
        verify(stateStore, times(2)).getAllStates();
    }
    
    @Test
    void onlyTheUnknownStateExists_returnsFalseWhenMultipleStatesExist() {
        // Arrange
        List<State> states = Arrays.asList(unknownState, testState1);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        boolean result = stateService.onlyTheUnknownStateExists();
        
        // Assert
        assertFalse(result);
        verify(stateStore).getAllStates();
    }
    
    @Test
    void onlyTheUnknownStateExists_returnsFalseWhenNoUnknownState() {
        // Arrange
        List<State> states = Collections.singletonList(testState1);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        boolean result = stateService.onlyTheUnknownStateExists();
        
        // Assert
        assertFalse(result);
        verify(stateStore, times(2)).getAllStates();
    }
    
    @Test
    void getAllStateIds_returnsAllIds() {
        // Arrange
        List<State> states = Arrays.asList(testState1, testState2);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        List<Long> ids = stateService.getAllStateIds();
        
        // Assert
        assertEquals(2, ids.size());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
    }
    
    @Test
    void getAllStateNames_returnsAllNames() {
        // Arrange
        List<State> states = Arrays.asList(testState1, testState2);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        Set<String> names = stateService.getAllStateNames();
        
        // Assert
        assertEquals(2, names.size());
        assertTrue(names.contains("state1"));
        assertTrue(names.contains("state2"));
    }
    
    @Test
    void findSetById_varargs_returnsFoundStates() {
        // Arrange
        when(stateStore.getState(1L)).thenReturn(Optional.of(testState1));
        when(stateStore.getState(2L)).thenReturn(Optional.of(testState2));
        when(stateStore.getState(999L)).thenReturn(Optional.empty());
        
        // Act
        Set<State> result = stateService.findSetById(1L, 2L, 999L);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(testState1));
        assertTrue(result.contains(testState2));
        verify(stateStore).getState(1L);
        verify(stateStore).getState(2L);
        verify(stateStore).getState(999L);
    }
    
    @Test
    void findSetById_set_returnsFoundStates() {
        // Arrange
        Set<Long> ids = new HashSet<>(Arrays.asList(1L, 2L));
        when(stateStore.getState(1L)).thenReturn(Optional.of(testState1));
        when(stateStore.getState(2L)).thenReturn(Optional.of(testState2));
        
        // Act
        Set<State> result = stateService.findSetById(ids);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(testState1));
        assertTrue(result.contains(testState2));
    }
    
    @Test
    void findArrayByName_varargs_returnsFoundStates() {
        // Arrange
        when(stateStore.getState("state1")).thenReturn(Optional.of(testState1));
        when(stateStore.getState("state2")).thenReturn(Optional.of(testState2));
        when(stateStore.getState("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        State[] result = stateService.findArrayByName("state1", "state2", "nonexistent");
        
        // Assert
        assertEquals(2, result.length);
        assertEquals(testState1, result[0]);
        assertEquals(testState2, result[1]);
        verify(stateStore).getState("state1");
        verify(stateStore).getState("state2");
        verify(stateStore).getState("nonexistent");
    }
    
    @Test
    void findArrayByName_set_returnsFoundStates() {
        // Arrange
        Set<String> names = new HashSet<>(Arrays.asList("state1", "state2"));
        when(stateStore.getState("state1")).thenReturn(Optional.of(testState1));
        when(stateStore.getState("state2")).thenReturn(Optional.of(testState2));
        
        // Act
        State[] result = stateService.findArrayByName(names);
        
        // Assert
        assertEquals(2, result.length);
        // Note: Set order is not guaranteed, so we check both possibilities
        assertTrue((result[0] == testState1 && result[1] == testState2) ||
                   (result[0] == testState2 && result[1] == testState1));
    }
    
    @Test
    void save_callsStoreWhenStateNotNull() {
        // Act
        stateService.save(testState1);
        
        // Assert
        verify(stateStore).save(testState1);
    }
    
    @Test
    void save_doesNotCallStoreWhenStateNull() {
        // Act
        stateService.save(null);
        
        // Assert
        verify(stateStore, never()).save(any());
    }
    
    @Test
    void resetTimesVisited_resetsAllStateCounters() {
        // Arrange
        testState1.setTimesVisited(5);
        testState2.setTimesVisited(10);
        List<State> states = Arrays.asList(testState1, testState2);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act
        stateService.resetTimesVisited();
        
        // Assert
        assertEquals(0, testState1.getTimesVisited());
        assertEquals(0, testState2.getTimesVisited());
        verify(stateStore).getAllStates();
    }
    
    @Test
    void deleteAllStates_callsStoreDeleteAll() {
        // Act
        stateService.deleteAllStates();
        
        // Assert
        verify(stateStore).deleteAll();
    }
    
    @Test
    void removeState_callsStoreDelete() {
        // Act
        stateService.removeState(testState1);
        
        // Assert
        verify(stateStore).delete(testState1);
    }
    
    @Test
    void printAllStates_printsStateNames() {
        // Arrange
        List<State> states = Arrays.asList(testState1, testState2);
        when(stateStore.getAllStates()).thenReturn(states);
        
        // Act - This will print to console, we're just verifying it doesn't throw
        stateService.printAllStates();
        
        // Assert
        verify(stateStore).getAllStates();
    }
    
    @Test
    void findSetById_emptyArray_returnsEmptySet() {
        // Act
        Set<State> result = stateService.findSetById();
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore, never()).getState(anyLong());
    }
    
    @Test
    void findArrayByName_emptyArray_returnsEmptyArray() {
        // Act
        State[] result = stateService.findArrayByName();
        
        // Assert
        assertEquals(0, result.length);
        verify(stateStore, never()).getState(anyString());
    }
    
    @Test
    void getAllStates_emptyStore_returnsEmptyList() {
        // Arrange
        when(stateStore.getAllStates()).thenReturn(Collections.emptyList());
        
        // Act
        List<State> result = stateService.getAllStates();
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore).getAllStates();
    }
    
    @Test
    void getAllStateIds_emptyStore_returnsEmptyList() {
        // Arrange
        when(stateStore.getAllStates()).thenReturn(Collections.emptyList());
        
        // Act
        List<Long> result = stateService.getAllStateIds();
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore).getAllStates();
    }
    
    @Test
    void getAllStateNames_emptyStore_returnsEmptySet() {
        // Arrange
        when(stateStore.getAllStates()).thenReturn(Collections.emptyList());
        
        // Act
        Set<String> result = stateService.getAllStateNames();
        
        // Assert
        assertTrue(result.isEmpty());
        verify(stateStore).getAllStates();
    }
}