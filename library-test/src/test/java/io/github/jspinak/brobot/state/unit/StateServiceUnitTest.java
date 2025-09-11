package io.github.jspinak.brobot.state.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.navigation.service.StateService;

/**
 * Unit tests for StateService that don't require Spring context. These tests verify the service
 * logic without integration complexity.
 */
@ExtendWith(MockitoExtension.class)
class StateServiceUnitTest {

    @Mock private StateStore mockStateStore;

    private StateService stateService;

    private State testState;
    private StateImage testImage;

    @BeforeEach
    void setupTest() {
        // Enable mock mode to avoid loading actual images
        FrameworkSettings.mock = true;

        // Create service with mocked dependencies
        stateService = new StateService(mockStateStore);

        // Create test data
        testState = new State();
        testState.setName("TestState");
        testState.setId(1L);
        testState.setBaseProbabilityExists(90);

        // Create StateImage without loading actual image file
        testImage = new StateImage.Builder().setName("test-image").build();
        testState.addStateImage(testImage);
    }

    @AfterEach
    void tearDown() {
        // Reset mock mode
        FrameworkSettings.mock = false;
    }

    @Nested
    @DisplayName("State Retrieval Tests")
    class StateRetrievalTests {

        @Test
        @DisplayName("Should retrieve state by name")
        void shouldRetrieveStateByName() {
            // Given
            when(mockStateStore.getState("TestState")).thenReturn(Optional.of(testState));

            // When
            Optional<State> result = stateService.getState("TestState");

            // Then
            assertTrue(result.isPresent());
            assertEquals("TestState", result.get().getName());
            verify(mockStateStore).getState("TestState");
        }

        @Test
        @DisplayName("Should retrieve state by ID")
        void shouldRetrieveStateById() {
            // Given
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(testState));

            // When
            Optional<State> result = stateService.getState(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            verify(mockStateStore).getState(1L);
        }

        @Test
        @DisplayName("Should return empty for non-existent state")
        void shouldReturnEmptyForNonExistentState() {
            // Given
            when(mockStateStore.getState("NonExistent")).thenReturn(Optional.empty());

            // When
            Optional<State> result = stateService.getState("NonExistent");

            // Then
            assertFalse(result.isPresent());
            verify(mockStateStore).getState("NonExistent");
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should save state")
        void shouldSaveState() {
            // When
            stateService.save(testState);

            // Then
            verify(mockStateStore).save(testState);
        }

        @Test
        @DisplayName("Should not save null state")
        void shouldNotSaveNullState() {
            // When
            stateService.save(null);

            // Then
            verify(mockStateStore, never()).save(any());
        }

        @Test
        @DisplayName("Should get all states")
        void shouldGetAllStates() {
            // Given
            List<State> states = Arrays.asList(testState, new State());
            when(mockStateStore.getAllStates()).thenReturn(states);

            // When
            List<State> result = stateService.getAllStates();

            // Then
            assertEquals(2, result.size());
            verify(mockStateStore).getAllStates();
        }

        @Test
        @DisplayName("Should get all state names")
        void shouldGetAllStateNames() {
            // Given
            State state2 = new State();
            state2.setName("AnotherState");
            List<State> states = Arrays.asList(testState, state2);
            when(mockStateStore.getAllStates()).thenReturn(states);

            // When
            Set<String> names = stateService.getAllStateNames();

            // Then
            assertEquals(2, names.size());
            assertTrue(names.contains("TestState"));
            assertTrue(names.contains("AnotherState"));
        }
    }

    @Nested
    @DisplayName("State Query Tests")
    class StateQueryTests {

        @Test
        @DisplayName("Should detect unknown-only state")
        void shouldDetectUnknownOnlyState() {
            // Given
            State unknownState = new State();
            unknownState.setName("unknown");
            when(mockStateStore.getAllStates()).thenReturn(Arrays.asList(unknownState));

            // When
            boolean onlyUnknown = stateService.onlyTheUnknownStateExists();

            // Then
            assertTrue(onlyUnknown);
        }

        @Test
        @DisplayName("Should detect multiple states exist")
        void shouldDetectMultipleStatesExist() {
            // Given
            State unknownState = new State();
            unknownState.setName("unknown");
            when(mockStateStore.getAllStates()).thenReturn(Arrays.asList(unknownState, testState));

            // When
            boolean onlyUnknown = stateService.onlyTheUnknownStateExists();

            // Then
            assertFalse(onlyUnknown);
        }

        @Test
        @DisplayName("Should find states by name array")
        void shouldFindStatesByNameArray() {
            // Given
            State state2 = new State();
            state2.setName("State2");
            when(mockStateStore.getState("TestState")).thenReturn(Optional.of(testState));
            when(mockStateStore.getState("State2")).thenReturn(Optional.of(state2));
            when(mockStateStore.getState("NonExistent")).thenReturn(Optional.empty());

            // When
            State[] result = stateService.findArrayByName("TestState", "State2", "NonExistent");

            // Then
            assertEquals(2, result.length);
            assertEquals("TestState", result[0].getName());
            assertEquals("State2", result[1].getName());
        }
    }

    @Nested
    @DisplayName("State ID/Name Resolution Tests")
    class StateResolutionTests {

        @Test
        @DisplayName("Should get state ID by name")
        void shouldGetStateIdByName() {
            // Given
            when(mockStateStore.getState("TestState")).thenReturn(Optional.of(testState));

            // When
            Long id = stateService.getStateId("TestState");

            // Then
            assertEquals(1L, id);
        }

        @Test
        @DisplayName("Should return null for non-existent state ID")
        void shouldReturnNullForNonExistentStateId() {
            // Given
            when(mockStateStore.getState("NonExistent")).thenReturn(Optional.empty());

            // When
            Long id = stateService.getStateId("NonExistent");

            // Then
            assertNull(id);
        }

        @Test
        @DisplayName("Should get state name by ID")
        void shouldGetStateNameById() {
            // Given
            when(mockStateStore.getState(1L)).thenReturn(Optional.of(testState));

            // When
            String name = stateService.getStateName(1L);

            // Then
            assertEquals("TestState", name);
        }
    }

    @Nested
    @DisplayName("State Reset Tests")
    class StateResetTests {

        @Test
        @DisplayName("Should reset times visited for all states")
        void shouldResetTimesVisitedForAllStates() {
            // Given
            State state2 = new State();
            state2.setTimesVisited(5);
            testState.setTimesVisited(3);

            when(mockStateStore.getAllStates()).thenReturn(Arrays.asList(testState, state2));

            // When
            stateService.resetTimesVisited();

            // Then
            assertEquals(0, testState.getTimesVisited());
            assertEquals(0, state2.getTimesVisited());
        }

        @Test
        @DisplayName("Should delete all states")
        void shouldDeleteAllStates() {
            // When
            stateService.deleteAllStates();

            // Then
            verify(mockStateStore).deleteAll();
        }

        @Test
        @DisplayName("Should remove specific state")
        void shouldRemoveSpecificState() {
            // When
            stateService.removeState(testState);

            // Then
            verify(mockStateStore).delete(testState);
        }
    }
}
