package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.config.profile.IntegrationTestMinimalConfig;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Integration test for state detection and probability management. Tests how Brobot detects active
 * states and manages state probabilities.
 */
@SpringBootTest(classes = IntegrationTestMinimalConfig.class)
@Import({
    StateDetectionIntegrationTest.LoginState.class,
    StateDetectionIntegrationTest.DashboardState.class,
    StateDetectionIntegrationTest.SettingsState.class
})
@TestPropertySource(
        properties = {
            "brobot.core.mock=true",
            "brobot.mock.enabled=true",
            "logging.level.io.github.jspinak.brobot=DEBUG",
            "spring.main.allow-bean-definition-overriding=true"
        })
@Slf4j
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Integration test requires non-CI environment")
public class StateDetectionIntegrationTest extends BrobotTestBase {

    @Autowired private StateService stateService;

    @Autowired private StateMemory stateMemory;

    @Autowired private StateDetector stateDetector;

    @Autowired private Action action;

    /** Test state representing a login screen */
    @Component
    @State(initial = true)
    @Getter
    public static class LoginState {
        private final StateImage usernameField;
        private final StateImage passwordField;
        private final StateImage loginButton;

        public LoginState() {
            usernameField = new StateImage.Builder().setName("UsernameField").build();

            passwordField = new StateImage.Builder().setName("PasswordField").build();

            loginButton = new StateImage.Builder().setName("LoginButton").build();
        }
    }

    /** Test state representing a dashboard */
    @Component
    @State
    @Getter
    public static class DashboardState {
        private final StateImage dashboardHeader;
        private final StateImage menuBar;
        private final StateString welcomeText;

        public DashboardState() {
            dashboardHeader = new StateImage.Builder().setName("DashboardHeader").build();

            menuBar = new StateImage.Builder().setName("MenuBar").build();

            welcomeText =
                    new StateString.Builder().setName("WelcomeText").setString("Welcome").build();
        }
    }

    /** Test state representing settings screen */
    @Component
    @State
    @Getter
    public static class SettingsState {
        private final StateImage settingsIcon;
        private final StateImage saveButton;

        public SettingsState() {
            settingsIcon = new StateImage.Builder().setName("SettingsIcon").build();

            saveButton = new StateImage.Builder().setName("SaveButton").build();
        }
    }

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateMemory.removeAllStates();
        log.info("=== STATE DETECTION TEST SETUP COMPLETE ===");
    }

    @Test
    public void testInitialStateDetection() {
        log.info("=== TESTING INITIAL STATE DETECTION ===");

        // Initial state should be Login (marked with initial = true)
        Set<Long> activeStates = stateMemory.getActiveStates();

        // In mock mode, initial states should be set
        assertFalse(activeStates.isEmpty(), "Should have initial state active");

        List<String> activeStateNames = stateMemory.getActiveStateNames();
        log.info("Initial active states: {}", activeStateNames);

        // Verify Login is in the initial states
        assertTrue(activeStateNames.contains("Login"), "Login should be in initial states");
    }

    @Test
    public void testStateProbability() {
        log.info("=== TESTING STATE PROBABILITY ===");

        // Get the Login state
        io.github.jspinak.brobot.model.state.State loginState =
                stateService.getState("Login").orElseThrow();

        // Get the Dashboard state
        io.github.jspinak.brobot.model.state.State dashboardState =
                stateService.getState("Dashboard").orElseThrow();

        // Initially, Login should have higher probability (it's the initial state)
        log.info("Login initial probability: {}", loginState.getProbabilityExists());
        log.info("Dashboard initial probability: {}", dashboardState.getProbabilityExists());

        // Add Dashboard to active states
        stateMemory.addActiveState(dashboardState.getId());

        // Dashboard should now have 100% probability
        assertEquals(
                100,
                dashboardState.getProbabilityExists(),
                "Active state should have 100% probability");

        // Remove Login from active states
        stateMemory.removeInactiveState(loginState.getId());

        // Login should now have 0% probability
        assertEquals(
                0, loginState.getProbabilityExists(), "Inactive state should have 0% probability");
    }

    @Test
    public void testMultipleActiveStates() {
        log.info("=== TESTING MULTIPLE ACTIVE STATES ===");

        // Get all states
        io.github.jspinak.brobot.model.state.State loginState =
                stateService.getState("Login").orElseThrow();
        io.github.jspinak.brobot.model.state.State dashboardState =
                stateService.getState("Dashboard").orElseThrow();
        io.github.jspinak.brobot.model.state.State settingsState =
                stateService.getState("Settings").orElseThrow();

        // Clear all active states
        stateMemory.removeAllStates();

        // Add multiple states as active
        stateMemory.addActiveState(dashboardState.getId());
        stateMemory.addActiveState(settingsState.getId());

        Set<Long> activeStates = stateMemory.getActiveStates();
        assertEquals(2, activeStates.size(), "Should have 2 active states");

        assertTrue(activeStates.contains(dashboardState.getId()), "Dashboard should be active");
        assertTrue(activeStates.contains(settingsState.getId()), "Settings should be active");
        assertFalse(activeStates.contains(loginState.getId()), "Login should not be active");

        log.info("Active states: {}", stateMemory.getActiveStateNames());
    }

    @Test
    public void testStateDetectionWithMatches() {
        log.info("=== TESTING STATE DETECTION WITH MATCHES ===");

        // Get Dashboard state
        io.github.jspinak.brobot.model.state.State dashboardState =
                stateService.getState("Dashboard").orElseThrow();

        // Clear active states
        stateMemory.removeAllStates();

        // Find dashboard header (this should add Dashboard to active states)
        DashboardState dashboard = applicationContext.getBean(DashboardState.class);

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();

        ObjectCollection objects =
                new ObjectCollection.Builder().withImages(dashboard.getDashboardHeader()).build();

        ActionResult result = action.perform(findOptions, objects);

        if (result.isSuccess() && !result.getMatchList().isEmpty()) {
            // In mock mode, this should update active states based on matches
            stateMemory.adjustActiveStatesWithMatches(result);

            // Dashboard should now be active
            Set<Long> activeStates = stateMemory.getActiveStates();
            assertTrue(
                    activeStates.contains(dashboardState.getId()),
                    "Dashboard should be active after finding its elements");

            log.info("States activated by matches: {}", stateMemory.getActiveStateNames());
        }
    }

    @Test
    public void testStateVisitCounter() {
        log.info("=== TESTING STATE VISIT COUNTER ===");

        io.github.jspinak.brobot.model.state.State loginState =
                stateService.getState("Login").orElseThrow();
        io.github.jspinak.brobot.model.state.State dashboardState =
                stateService.getState("Dashboard").orElseThrow();

        int initialLoginVisits = loginState.getTimesVisited();
        int initialDashboardVisits = dashboardState.getTimesVisited();

        log.info(
                "Initial visit counts - Login: {}, Dashboard: {}",
                initialLoginVisits,
                initialDashboardVisits);

        // Add states multiple times (simulating navigation)
        stateMemory.addActiveState(loginState.getId());
        stateMemory.removeInactiveState(loginState.getId());
        stateMemory.addActiveState(dashboardState.getId());
        stateMemory.removeInactiveState(dashboardState.getId());
        stateMemory.addActiveState(loginState.getId());

        // Visit counts should increase
        assertTrue(
                loginState.getTimesVisited() > initialLoginVisits,
                "Login visit count should increase");
        assertTrue(
                dashboardState.getTimesVisited() > initialDashboardVisits,
                "Dashboard visit count should increase");

        log.info(
                "Updated visit counts - Login: {}, Dashboard: {}",
                loginState.getTimesVisited(),
                dashboardState.getTimesVisited());
    }

    @Test
    public void testNullStateHandling() {
        log.info("=== TESTING NULL STATE HANDLING ===");

        // NULL state should not be added to active states
        Long nullStateId = SpecialStateType.NULL.getId();

        int initialSize = stateMemory.getActiveStates().size();
        stateMemory.addActiveState(nullStateId);
        int afterSize = stateMemory.getActiveStates().size();

        assertEquals(initialSize, afterSize, "NULL state should not be added to active states");

        assertFalse(
                stateMemory.getActiveStates().contains(nullStateId),
                "Active states should not contain NULL state");

        log.info("NULL state correctly filtered from active states");
    }

    @Test
    public void testStateTransitionDetection() {
        log.info("=== TESTING STATE TRANSITION DETECTION ===");

        // Start with Login state
        io.github.jspinak.brobot.model.state.State loginState =
                stateService.getState("Login").orElseThrow();
        io.github.jspinak.brobot.model.state.State dashboardState =
                stateService.getState("Dashboard").orElseThrow();

        stateMemory.removeAllStates();
        stateMemory.addActiveState(loginState.getId());

        assertEquals(1, stateMemory.getActiveStates().size(), "Should have only Login active");

        // Simulate transition to Dashboard
        stateMemory.removeInactiveState(loginState.getId());
        stateMemory.addActiveState(dashboardState.getId());

        assertEquals(1, stateMemory.getActiveStates().size(), "Should have only Dashboard active");
        assertTrue(
                stateMemory.getActiveStates().contains(dashboardState.getId()),
                "Dashboard should be active after transition");
        assertFalse(
                stateMemory.getActiveStates().contains(loginState.getId()),
                "Login should not be active after transition");

        log.info("State transition completed: Login -> Dashboard");
    }

    @Autowired private org.springframework.context.ApplicationContext applicationContext;
}
