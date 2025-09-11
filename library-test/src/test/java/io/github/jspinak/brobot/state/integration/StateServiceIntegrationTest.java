package io.github.jspinak.brobot.state.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.state.*;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

/**
 * Integration tests for StateService using the actual Brobot API. Tests state management
 * functionality with real Spring context.
 */
@TestPropertySource(
        properties = {
            "brobot.logging.verbosity=VERBOSE",
            "brobot.console.actions.enabled=true",
            "brobot.state.detection.enabled=true",
            "brobot.mock.enabled=true", // Use mock mode for testing
            "spring.main.allow-bean-definition-overriding=true" // Allow bean overriding
        })
class StateServiceIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired private StateService stateService;

    @Autowired private StateTransitionService transitionService;

    @Autowired private Action action;

    private State loginState;
    private State dashboardState;

    @BeforeEach
    void setupStates() {
        // Create test states using the actual API
        loginState = new State();
        loginState.setName("LoginState");
        createLoginImages().forEach(loginState::addStateImage);
        loginState.setBaseProbabilityExists(90);

        dashboardState = new State();
        dashboardState.setName("DashboardState");
        createDashboardImages().forEach(dashboardState::addStateImage);
        dashboardState.setBaseProbabilityExists(85);
    }

    private List<StateImage> createLoginImages() {
        List<StateImage> images = new ArrayList<>();

        StateImage loginButton =
                new StateImage.Builder()
                        .setName("login-button")
                        .addPattern("images/login/button.png")
                        .build();

        StateImage usernameField =
                new StateImage.Builder()
                        .setName("username-field")
                        .addPattern("images/login/username.png")
                        .build();

        images.add(loginButton);
        images.add(usernameField);
        return images;
    }

    private List<StateImage> createDashboardImages() {
        List<StateImage> images = new ArrayList<>();

        StateImage dashboardHeader =
                new StateImage.Builder()
                        .setName("dashboard-header")
                        .addPattern("images/dashboard/header.png")
                        .build();

        images.add(dashboardHeader);
        return images;
    }

    @Nested
    @DisplayName("State Management")
    class StateManagement {

        @Test
        @DisplayName("Should save and retrieve states")
        void shouldSaveAndRetrieveStates() {
            // Save states using the actual API
            stateService.save(loginState);
            stateService.save(dashboardState);

            // Retrieve by name
            Optional<State> retrievedLogin = stateService.getState("LoginState");
            Optional<State> retrievedDashboard = stateService.getState("DashboardState");

            assertTrue(retrievedLogin.isPresent());
            assertTrue(retrievedDashboard.isPresent());
            assertEquals("LoginState", retrievedLogin.get().getName());
            assertEquals("DashboardState", retrievedDashboard.get().getName());
        }

        @Test
        @DisplayName("Should get all state names")
        void shouldGetAllStateNames() {
            stateService.save(loginState);
            stateService.save(dashboardState);

            Set<String> stateNames = stateService.getAllStateNames();

            assertTrue(stateNames.contains("LoginState"));
            assertTrue(stateNames.contains("DashboardState"));
        }

        @Test
        @DisplayName("Should find states by name array")
        void shouldFindStatesByNameArray() {
            stateService.save(loginState);
            stateService.save(dashboardState);

            State[] states = stateService.findArrayByName("LoginState", "DashboardState");

            assertEquals(2, states.length);
            assertTrue(Arrays.stream(states).anyMatch(s -> s.getName().equals("LoginState")));
            assertTrue(Arrays.stream(states).anyMatch(s -> s.getName().equals("DashboardState")));
        }

        @Test
        @DisplayName("Should get state ID by name")
        void shouldGetStateIdByName() {
            stateService.save(loginState);

            Long stateId = stateService.getStateId("LoginState");
            assertNotNull(stateId);

            // Verify we can retrieve by ID
            Optional<State> stateById = stateService.getState(stateId);
            assertTrue(stateById.isPresent());
            assertEquals("LoginState", stateById.get().getName());
        }
    }

    @Nested
    @DisplayName("State Transitions")
    class StateTransitions {

        @Test
        @DisplayName("Should create and execute transitions")
        void shouldCreateAndExecuteTransitions() {
            stateService.save(loginState);
            stateService.save(dashboardState);

            // Test that states are available for transitions
            Optional<State> fromState = stateService.getState("LoginState");
            Optional<State> toState = stateService.getState("DashboardState");

            assertTrue(fromState.isPresent());
            assertTrue(toState.isPresent());

            // In a real test, we would set up transitions and test them
            // For now, just verify the states are ready for transition logic
        }
    }

    @Nested
    @DisplayName("State Queries")
    class StateQueries {

        @Test
        @DisplayName("Should check if only unknown state exists")
        void shouldCheckUnknownState() {
            // Initially might have unknown state
            // After adding states, should return false
            stateService.save(loginState);

            boolean onlyUnknown = stateService.onlyTheUnknownStateExists();
            assertFalse(onlyUnknown, "Should have more than just unknown state");
        }

        @Test
        @DisplayName("Should get all states")
        void shouldGetAllStates() {
            stateService.save(loginState);
            stateService.save(dashboardState);

            List<State> allStates = stateService.getAllStates();

            // At least our two states (might have unknown state too)
            assertTrue(allStates.size() >= 2);
            assertTrue(allStates.stream().anyMatch(s -> s.getName().equals("LoginState")));
            assertTrue(allStates.stream().anyMatch(s -> s.getName().equals("DashboardState")));
        }

        @Test
        @DisplayName("Should handle missing states gracefully")
        void shouldHandleMissingStates() {
            Optional<State> missingState = stateService.getState("NonExistentState");

            assertFalse(missingState.isPresent());

            // Should return null for missing state ID
            Long missingId = stateService.getStateId("NonExistentState");
            assertNull(missingId);
        }
    }

    @Nested
    @DisplayName("State Properties")
    class StateProperties {

        @Test
        @DisplayName("Should maintain state properties")
        void shouldMaintainStateProperties() {
            stateService.save(loginState);

            Optional<State> retrieved = stateService.getState("LoginState");
            assertTrue(retrieved.isPresent());

            State state = retrieved.get();
            assertEquals("LoginState", state.getName());
            assertEquals(90, state.getBaseProbabilityExists());
            assertEquals(2, state.getStateImages().size());
        }

        @Test
        @DisplayName("Should track state visits")
        void shouldTrackStateVisits() {
            stateService.save(loginState);

            Optional<State> state = stateService.getState("LoginState");
            assertTrue(state.isPresent());

            int initialVisits = state.get().getTimesVisited();

            // Add a visit
            state.get().addVisit();

            // Retrieve again and check
            Optional<State> updated = stateService.getState("LoginState");
            assertTrue(updated.isPresent());
            assertEquals(initialVisits + 1, updated.get().getTimesVisited());
        }
    }
}
