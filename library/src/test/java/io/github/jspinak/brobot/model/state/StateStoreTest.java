package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for StateStore - manages state persistence and retrieval.
 * Tests state storage, retrieval, deletion, and ID assignment.
 */
@DisplayName("StateStore Tests")
class StateStoreTest extends BrobotTestBase {
    
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
            .setName("image1")
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
            .setName("region1")
            .build();
        state.getStateRegions().add(stateRegion);
        
        // Add StateString
        StateString stateString = new StateString.Builder()
            .setString("testString")
            .build();
        state.getStateStrings().add(stateString);
        
        return state;
    }
    
    @Nested
    @DisplayName("State Storage Operations")
    class StateStorageOperations {
        
        @Test
        @DisplayName("Should save single state")
        void testSaveSingleState() {
            // Act
            stateStore.save(testState1);
            
            // Assert
            List<State> allStates = stateStore.getAllStates();
            assertEquals(1, allStates.size());
            assertEquals(testState1, allStates.get(0));
            assertNotNull(testState1.getId());
            assertEquals(0L, testState1.getId());
        }
        
        @Test
        @DisplayName("Should save multiple states with sequential IDs")
        void testSaveMultipleStates() {
            // Act
            stateStore.save(testState1);
            stateStore.save(testState2);
            stateStore.save(testState3);
            
            // Assert
            List<State> allStates = stateStore.getAllStates();
            assertEquals(3, allStates.size());
            assertEquals(0L, testState1.getId());
            assertEquals(1L, testState2.getId());
            assertEquals(2L, testState3.getId());
        }
        
        @Test
        @DisplayName("Should preserve existing state ID")
        void testPreserveExistingId() {
            // Arrange
            testState1.setId(999L);
            
            // Act
            stateStore.save(testState1);
            
            // Assert
            assertEquals(999L, testState1.getId());
        }
        
        @Test
        @DisplayName("Should set owner state ID for all child objects")
        void testSetOwnerStateIdForChildren() {
            // Arrange
            State complexState = createStateWithObjects("ComplexState");
            
            // Act
            stateStore.save(complexState);
            
            // Assert
            Long stateId = complexState.getId();
            assertNotNull(stateId);
            
            // Check StateImages
            complexState.getStateImages().forEach(image -> {
                assertEquals(stateId, image.getOwnerStateId());
            });
            
            // Check StateLocations
            complexState.getStateLocations().forEach(location -> {
                assertEquals(stateId, location.getOwnerStateId());
            });
            
            // Check StateRegions
            complexState.getStateRegions().forEach(region -> {
                assertEquals(stateId, region.getOwnerStateId());
            });
            
            // Check StateStrings
            complexState.getStateStrings().forEach(string ->
                assertEquals(stateId, string.getOwnerStateId())
            );
        }
    }
    
    @Nested
    @DisplayName("State Retrieval Operations")
    class StateRetrievalOperations {
        
        @BeforeEach
        void setupStates() {
            stateStore.save(testState1);
            stateStore.save(testState2);
            stateStore.save(testState3);
        }
        
        @Test
        @DisplayName("Should retrieve state by name")
        void testGetStateByName() {
            // Act
            Optional<State> result = stateStore.getState("State2");
            
            // Assert
            assertTrue(result.isPresent());
            assertEquals(testState2, result.get());
        }
        
        @Test
        @DisplayName("Should return empty for non-existent name")
        void testGetStateByNonExistentName() {
            // Act
            Optional<State> result = stateStore.getState("NonExistent");
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should retrieve state by ID")
        void testGetStateById() {
            // Act
            Optional<State> result = stateStore.getState(1L);
            
            // Assert
            assertTrue(result.isPresent());
            assertEquals(testState2, result.get());
        }
        
        @Test
        @DisplayName("Should return empty for non-existent ID")
        void testGetStateByNonExistentId() {
            // Act
            Optional<State> result = stateStore.getState(999L);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty for null ID")
        void testGetStateByNullId() {
            // Act
            Optional<State> result = stateStore.getState((Long) null);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should get all states")
        void testGetAllStates() {
            // Act
            List<State> allStates = stateStore.getAllStates();
            
            // Assert
            assertEquals(3, allStates.size());
            assertTrue(allStates.contains(testState1));
            assertTrue(allStates.contains(testState2));
            assertTrue(allStates.contains(testState3));
        }
    }
    
    @Nested
    @DisplayName("State Deletion Operations")
    class StateDeletionOperations {
        
        @BeforeEach
        void setupStates() {
            stateStore.save(testState1);
            stateStore.save(testState2);
            stateStore.save(testState3);
        }
        
        @Test
        @DisplayName("Should delete existing state")
        void testDeleteExistingState() {
            // Act
            boolean result = stateStore.delete(testState2);
            
            // Assert
            assertTrue(result);
            List<State> allStates = stateStore.getAllStates();
            assertEquals(2, allStates.size());
            assertFalse(allStates.contains(testState2));
            assertTrue(allStates.contains(testState1));
            assertTrue(allStates.contains(testState3));
        }
        
        @Test
        @DisplayName("Should return false when deleting non-existent state")
        void testDeleteNonExistentState() {
            // Arrange
            State nonExistentState = createTestState("NonExistent");
            
            // Act
            boolean result = stateStore.delete(nonExistentState);
            
            // Assert
            assertFalse(result);
            assertEquals(3, stateStore.getAllStates().size());
        }
        
        @Test
        @DisplayName("Should delete all states")
        void testDeleteAll() {
            // Act
            stateStore.deleteAll();
            
            // Assert
            List<State> allStates = stateStore.getAllStates();
            assertEquals(0, allStates.size());
        }
        
        @Test
        @DisplayName("Should handle delete after deleteAll")
        void testDeleteAfterDeleteAll() {
            // Arrange
            stateStore.deleteAll();
            
            // Act
            boolean result = stateStore.delete(testState1);
            
            // Assert
            assertFalse(result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Should handle empty store operations")
        void testEmptyStoreOperations() {
            // Assert
            assertEquals(0, stateStore.getAllStates().size());
            assertFalse(stateStore.getState("Any").isPresent());
            assertFalse(stateStore.getState(0L).isPresent());
            assertFalse(stateStore.delete(testState1));
        }
        
        @Test
        @DisplayName("Should handle state with empty name")
        void testStateWithEmptyName() {
            // Arrange
            State emptyNameState = new State.Builder("").build();
            
            // Act
            stateStore.save(emptyNameState);
            
            // Assert
            assertEquals(1, stateStore.getAllStates().size());
            assertNotNull(emptyNameState.getId());
        }
        
        @Test
        @DisplayName("Should handle state with empty collections")
        void testStateWithEmptyCollections() {
            // Arrange
            State emptyState = new State.Builder("EmptyState")
                .build();
            
            // Ensure collections are empty but not null
            assertTrue(emptyState.getStateImages().isEmpty());
            assertTrue(emptyState.getStateLocations().isEmpty());
            assertTrue(emptyState.getStateRegions().isEmpty());
            assertTrue(emptyState.getStateStrings().isEmpty());
            
            // Act
            stateStore.save(emptyState);
            
            // Assert
            assertEquals(0L, emptyState.getId());
            assertEquals(1, stateStore.getAllStates().size());
        }
        
        @Test
        @DisplayName("Should handle sequential operations correctly")
        void testSequentialOperations() {
            // Save, delete, save again
            stateStore.save(testState1);
            assertEquals(0L, testState1.getId());
            
            stateStore.delete(testState1);
            assertEquals(0, stateStore.getAllStates().size());
            
            stateStore.save(testState2);
            assertEquals(0L, testState2.getId()); // Should reuse ID 0
            
            stateStore.save(testState3);
            assertEquals(1L, testState3.getId());
        }
    }
    
    @Nested
    @DisplayName("Concurrent Access Scenarios")
    class ConcurrentAccessScenarios {
        
        @Test
        @DisplayName("Should maintain consistency during rapid additions")
        void testRapidAdditions() {
            // Arrange
            List<State> states = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                states.add(createTestState("State" + i));
            }
            
            // Act
            states.forEach(stateStore::save);
            
            // Assert
            assertEquals(100, stateStore.getAllStates().size());
            for (int i = 0; i < 100; i++) {
                assertEquals((long) i, states.get(i).getId());
                assertTrue(stateStore.getState("State" + i).isPresent());
            }
        }
        
        @Test
        @DisplayName("Should handle interleaved operations")
        void testInterleavedOperations() {
            // Add some states
            stateStore.save(testState1);
            stateStore.save(testState2);
            
            // Delete one
            stateStore.delete(testState1);
            
            // Add more
            stateStore.save(testState3);
            State testState4 = createTestState("State4");
            stateStore.save(testState4);
            
            // Verify final state
            List<State> finalStates = stateStore.getAllStates();
            assertEquals(3, finalStates.size());
            assertFalse(finalStates.contains(testState1));
            assertTrue(finalStates.contains(testState2));
            assertTrue(finalStates.contains(testState3));
            assertTrue(finalStates.contains(testState4));
        }
    }
    
    @ParameterizedTest
    @DisplayName("Should handle various state names")
    @ValueSource(strings = {"", " ", "State-With-Dashes", "State_With_Underscores", "State.With.Dots", "123NumericStart"})
    void testVariousStateNames(String name) {
        // Arrange
        State state = createTestState(name);
        
        // Act
        stateStore.save(state);
        
        // Assert
        Optional<State> retrieved = stateStore.getState(name);
        assertTrue(retrieved.isPresent());
        assertEquals(name, retrieved.get().getName());
    }
    
    @ParameterizedTest
    @DisplayName("Should handle null parameters gracefully")
    @NullSource
    void testNullParameters(String nullName) {
        // Act & Assert
        assertFalse(stateStore.getState(nullName).isPresent());
    }
}