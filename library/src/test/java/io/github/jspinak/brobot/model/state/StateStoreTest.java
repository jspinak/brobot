package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the StateStore class which manages the collection 
 * of states in the Brobot framework.
 */
@DisplayName("StateStore Repository Tests")
public class StateStoreTest extends BrobotTestBase {

    private StateStore stateStore;
    private State testState1;
    private State testState2;
    private State testState3;
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateStore = new StateStore();
        
        // Create test states
        testState1 = createTestState("State1");
        testState2 = createTestState("State2");
        testState3 = createTestState("State3");
    }
    
    private State createTestState(String name) {
        State state = new State.Builder(name)
            .build();
        return state;
    }
    
    private State createStateWithObjects(String name) {
        State state = new State.Builder(name)
            .build();
            
        // Add StateImage
        StateImage stateImage = new StateImage.Builder()
            .build();
        state.getStateImages().add(stateImage);
        
        // Add StateLocation
        StateLocation stateLocation = new StateLocation.Builder()
            .setLocation(new Location(100, 200))
            .setName("location1")
            .build();
        state.getStateLocations().add(stateLocation);
        
        // Add StateRegion
        StateRegion stateRegion = new StateRegion.Builder()
            .setSearchRegion(new Region(0, 0, 100, 100))
            .build();
        state.getStateRegions().add(stateRegion);
        
        return state;
    }

    @Test
    @DisplayName("Should assign IDs to states when saving")
    void testStateIdAssignment() {
        // When
        stateStore.save(testState1);
        stateStore.save(testState2);
        stateStore.save(testState3);
        
        // Then - IDs should be assigned based on position
        assertEquals(0L, testState1.getId());
        assertEquals(1L, testState2.getId());
        assertEquals(2L, testState3.getId());
    }

    @Test
    @DisplayName("Should preserve existing state ID")
    void testPreserveExistingStateId() {
        // Given
        testState1.setId(99L);
        
        // When
        stateStore.save(testState1);
        
        // Then - ID should not be changed
        assertEquals(99L, testState1.getId());
    }

    @Test
    @DisplayName("Should get state by name")
    void testGetStateByName() {
        // Given
        stateStore.save(testState1);
        stateStore.save(testState2);
        
        // When
        Optional<State> found = stateStore.getState("State1");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(testState1, found.get());
        assertEquals("State1", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty when state name not found")
    void testGetStateByNameNotFound() {
        // Given
        stateStore.save(testState1);
        
        // When
        Optional<State> notFound = stateStore.getState("nonexistent");
        
        // Then
        assertFalse(notFound.isPresent());
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("Should get state by ID")
    void testGetStateById() {
        // Given
        stateStore.save(testState1);
        stateStore.save(testState2);
        Long stateId = testState1.getId();
        
        // When
        Optional<State> found = stateStore.getState(stateId);
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(testState1, found.get());
        assertEquals(stateId, found.get().getId());
    }

    @Test
    @DisplayName("Should return empty when state ID not found")
    void testGetStateByIdNotFound() {
        // Given
        stateStore.save(testState1);
        
        // When
        Optional<State> notFound = stateStore.getState(999L);
        
        // Then
        assertFalse(notFound.isPresent());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should handle null ID gracefully")
    void testGetStateByNullId(Long nullId) {
        // Given
        stateStore.save(testState1);
        
        // When
        Optional<State> result = stateStore.getState(nullId);
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should delete all states")
    void testDeleteAll() {
        // Given
        stateStore.save(testState1);
        stateStore.save(testState2);
        stateStore.save(testState3);
        assertEquals(3, stateStore.getAllStates().size());
        
        // When
        stateStore.deleteAll();
        
        // Then
        assertTrue(stateStore.getAllStates().isEmpty());
        assertEquals(0, stateStore.getAllStates().size());
    }

    @Test
    @DisplayName("Should delete specific state")
    void testDeleteSpecificState() {
        // Given
        stateStore.save(testState1);
        stateStore.save(testState2);
        stateStore.save(testState3);
        
        // When
        boolean deleted = stateStore.delete(testState2);
        
        // Then
        assertTrue(deleted);
        assertEquals(2, stateStore.getAllStates().size());
        assertFalse(stateStore.getAllStates().contains(testState2));
        assertTrue(stateStore.getAllStates().contains(testState1));
        assertTrue(stateStore.getAllStates().contains(testState3));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent state")
    void testDeleteNonExistentState() {
        // Given
        stateStore.save(testState1);
        
        // When
        boolean deleted = stateStore.delete(testState2);
        
        // Then
        assertFalse(deleted);
        assertEquals(1, stateStore.getAllStates().size());
    }

    @Test
    @DisplayName("Should update state object IDs when saving")
    void testUpdateStateObjectIds() {
        // Given
        StateImage stateImage = new StateImage.Builder()
            .setName("testImage")
            .build();
        StateLocation stateLocation = new StateLocation.Builder()
            .setName("testLocation")
            .build();
        StateRegion stateRegion = new StateRegion.Builder()
            .setName("testRegion")
            .build();
        StateString stateString = new StateString.Builder()
            .setName("testString")
            .build();
        
        State state = new State.Builder("complexState")
            .build();
        state.addStateImage(stateImage);
        state.addStateLocation(stateLocation);
        state.addStateRegion(stateRegion);
        state.addStateString(stateString);
        
        // When
        stateStore.save(state);
        
        // Then - All state objects should have owner state ID set
        assertEquals(state.getId(), stateImage.getOwnerStateId());
        assertEquals(state.getId(), stateLocation.getOwnerStateId());
        assertEquals(state.getId(), stateRegion.getOwnerStateId());
        assertEquals(state.getId(), stateString.getOwnerStateId());
    }

    @TestFactory
    @DisplayName("State management scenarios")
    Stream<DynamicTest> testStateManagementScenarios() {
        return Stream.of(
            dynamicTest("Add and retrieve multiple states", () -> {
                StateStore store = new StateStore();
                State s1 = new State.Builder("first").build();
                State s2 = new State.Builder("second").build();
                
                store.save(s1);
                store.save(s2);
                
                assertEquals(2, store.getAllStates().size());
                assertTrue(store.getState("first").isPresent());
                assertTrue(store.getState("second").isPresent());
            }),
            
            dynamicTest("Handle duplicate state names", () -> {
                StateStore store = new StateStore();
                State s1 = new State.Builder("duplicate").build();
                State s2 = new State.Builder("duplicate").build();
                
                store.save(s1);
                store.save(s2);
                
                // Both states saved
                assertEquals(2, store.getAllStates().size());
                
                // getState returns first match
                Optional<State> found = store.getState("duplicate");
                assertTrue(found.isPresent());
                assertSame(s1, found.get());
            }),
            
            dynamicTest("Clear and rebuild state store", () -> {
                StateStore store = new StateStore();
                store.save(new State.Builder("old1").build());
                store.save(new State.Builder("old2").build());
                
                store.deleteAll();
                
                store.save(new State.Builder("new1").build());
                store.save(new State.Builder("new2").build());
                
                assertEquals(2, store.getAllStates().size());
                assertFalse(store.getState("old1").isPresent());
                assertTrue(store.getState("new1").isPresent());
            })
        );
    }

    @Test
    @DisplayName("Should handle state with match history")
    void testStateWithMatchHistory() {
        // Given
        StateLocation location = new StateLocation.Builder()
            .setName("locationWithHistory")
            .build();
        
        ActionHistory history = location.getMatchHistory();
        history.addSnapshot(new ActionRecord());
        
        State state = new State.Builder("stateWithHistory")
            .build();
        state.addStateLocation(location);
        
        // When
        stateStore.save(state);
        
        // Then
        assertEquals(state.getId(), location.getOwnerStateId());
        // Match history snapshots should have state ID set
        history.getSnapshots().forEach(snapshot -> 
            assertEquals(state.getId(), snapshot.getStateId()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "state1", "STATE1", "State1", "state 1", "state-1"})
    @DisplayName("Should handle various state name formats")
    void testVariousStateNameFormats(String name) {
        // Given
        State state = new State.Builder(name).build();
        
        // When
        stateStore.save(state);
        
        // Then
        Optional<State> found = stateStore.getState(name);
        assertTrue(found.isPresent());
        assertEquals(name, found.get().getName());
    }

    @Test
    @DisplayName("Should get all states reference")
    void testGetAllStatesReference() {
        // Given
        stateStore.save(testState1);
        
        // When
        List<State> states1 = stateStore.getAllStates();
        List<State> states2 = stateStore.getAllStates();
        
        // Then - Same reference returned
        assertSame(states1, states2);
        
        // Modifications affect store
        states1.clear();
        assertTrue(stateStore.getAllStates().isEmpty());
    }

    @Test
    @DisplayName("Should handle Spring component annotation")
    void testSpringComponentAnnotation() {
        // StateStore is annotated with @Component
        // This test verifies basic Spring compatibility
        
        StateStore componentStore = new StateStore();
        
        // Should be instantiable as Spring bean
        assertNotNull(componentStore);
        assertNotNull(componentStore.getAllStates());
    }

    @Test
    @DisplayName("Should maintain insertion order")
    void testInsertionOrderMaintained() {
        // Given
        State[] states = new State[10];
        for (int i = 0; i < 10; i++) {
            states[i] = new State.Builder("state" + i).build();
            stateStore.save(states[i]);
        }
        
        // When
        List<State> allStates = stateStore.getAllStates();
        
        // Then - Order preserved
        for (int i = 0; i < 10; i++) {
            assertSame(states[i], allStates.get(i));
            assertEquals("state" + i, allStates.get(i).getName());
        }
    }

    @Test
    @DisplayName("Should handle edge case with Unknown state ID 0")
    void testUnknownStateIdZero() {
        // Given - Comment mentions Unknown State has id of 0
        State unknownState = new State.Builder("unknown").build();
        
        // When
        stateStore.save(unknownState);
        
        // Then - First state gets ID 0
        assertEquals(0L, unknownState.getId());
        
        // Additional states get sequential IDs
        stateStore.save(testState1);
        assertEquals(1L, testState1.getId());
    }
}