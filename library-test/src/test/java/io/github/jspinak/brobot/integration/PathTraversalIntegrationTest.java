package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.path.PathFinder;
import io.github.jspinak.brobot.navigation.path.PathManager;
import io.github.jspinak.brobot.navigation.path.PathTraverser;
import io.github.jspinak.brobot.navigation.path.Paths;
import io.github.jspinak.brobot.navigation.path.Path;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for path finding and traversal.
 * Tests the complete navigation system including path discovery and execution.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@Import({
    PathTraversalIntegrationTest.HomeState.class,
    PathTraversalIntegrationTest.LoginState.class,
    PathTraversalIntegrationTest.DashboardState.class,
    PathTraversalIntegrationTest.ProfileState.class,
    PathTraversalIntegrationTest.SettingsState.class,
    PathTraversalIntegrationTest.HomeToLoginTransition.class,
    PathTraversalIntegrationTest.LoginToDashboardTransition.class,
    PathTraversalIntegrationTest.DashboardToProfileTransition.class,
    PathTraversalIntegrationTest.DashboardToSettingsTransition.class,
    PathTraversalIntegrationTest.ProfileToDashboardTransition.class,
    PathTraversalIntegrationTest.SettingsToDashboardTransition.class,
    io.github.jspinak.brobot.annotations.AnnotationProcessor.class
})
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
@Slf4j
public class PathTraversalIntegrationTest extends BrobotTestBase {

    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateTransitionService transitionService;
    
    @Autowired
    private StateMemory stateMemory;
    
    @Autowired
    private PathFinder pathFinder;
    
    @Autowired
    private PathManager pathManager;
    
    @Autowired
    private PathTraverser pathTraverser;
    
    @Autowired
    private Action action;
    
    // Test States - simulating a typical application flow
    
    @Component
    @State(initial = true)
    @Getter
    public static class HomeState {
        private final StateImage homeLogo = new StateImage.Builder()
            .setName("HomeLogo")
            .build();
    }
    
    @Component
    @State
    @Getter
    public static class LoginState {
        private final StateImage loginForm = new StateImage.Builder()
            .setName("LoginForm")
            .build();
    }
    
    @Component
    @State
    @Getter
    public static class DashboardState {
        private final StateImage dashboardMenu = new StateImage.Builder()
            .setName("DashboardMenu")
            .build();
    }
    
    @Component
    @State
    @Getter
    public static class ProfileState {
        private final StateImage profileHeader = new StateImage.Builder()
            .setName("ProfileHeader")
            .build();
    }
    
    @Component
    @State
    @Getter
    public static class SettingsState {
        private final StateImage settingsPanel = new StateImage.Builder()
            .setName("SettingsPanel")
            .build();
    }
    
    // Test Transitions
    
    @Component
    @Transition(from = HomeState.class, to = LoginState.class)
    @RequiredArgsConstructor
    public static class HomeToLoginTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Home to Login");
            return true;
        }
    }
    
    @Component
    @Transition(from = LoginState.class, to = DashboardState.class)
    @RequiredArgsConstructor
    public static class LoginToDashboardTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Login to Dashboard");
            return true;
        }
    }
    
    @Component
    @Transition(from = DashboardState.class, to = ProfileState.class)
    @RequiredArgsConstructor
    public static class DashboardToProfileTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Dashboard to Profile");
            return true;
        }
    }
    
    @Component
    @Transition(from = DashboardState.class, to = SettingsState.class, priority = 10)
    @RequiredArgsConstructor
    public static class DashboardToSettingsTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Dashboard to Settings");
            return true;
        }
    }
    
    @Component
    @Transition(from = ProfileState.class, to = DashboardState.class)
    @RequiredArgsConstructor
    public static class ProfileToDashboardTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Profile to Dashboard");
            return true;
        }
    }
    
    @Component
    @Transition(from = SettingsState.class, to = DashboardState.class)
    @RequiredArgsConstructor
    public static class SettingsToDashboardTransition {
        private final Action action;
        
        public boolean execute() {
            log.info("Transitioning from Settings to Dashboard");
            return true;
        }
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateMemory.removeAllStates();
        log.info("=== PATH TRAVERSAL TEST SETUP COMPLETE ===");
    }
    
    @Test
    public void testSimplePathFinding() {
        log.info("=== TESTING SIMPLE PATH FINDING ===");
        
        // Get states
        io.github.jspinak.brobot.model.state.State homeState = 
            stateService.getState("Home").orElseThrow();
        io.github.jspinak.brobot.model.state.State loginState = 
            stateService.getState("Login").orElseThrow();
        
        // Set Home as active
        stateMemory.addActiveState(homeState.getId());
        
        // Find path from Home to Login
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            loginState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find path from Home to Login");
        
        Path firstPath = paths.getPaths().get(0);
        log.info("Found path with {} transitions", firstPath.getTransitions().size());
        
        // Should be a direct path (one transition)
        assertEquals(1, firstPath.getTransitions().size(), 
                    "Should have direct transition from Home to Login");
    }
    
    @Test
    public void testMultiStepPathFinding() {
        log.info("=== TESTING MULTI-STEP PATH FINDING ===");
        
        // Get states
        io.github.jspinak.brobot.model.state.State homeState = 
            stateService.getState("Home").orElseThrow();
        io.github.jspinak.brobot.model.state.State profileState = 
            stateService.getState("Profile").orElseThrow();
        
        // Set Home as active
        stateMemory.addActiveState(homeState.getId());
        
        // Find path from Home to Profile (requires multiple steps)
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            profileState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find path from Home to Profile");
        
        Path firstPath = paths.getPaths().get(0);
        log.info("Found path with {} transitions", firstPath.getTransitions().size());
        
        // Should require 3 transitions: Home -> Login -> Dashboard -> Profile
        assertEquals(3, firstPath.getTransitions().size(), 
                    "Should require 3 transitions to reach Profile from Home");
        
        // Log the path
        log.info("Path: Home -> Login -> Dashboard -> Profile");
        firstPath.getStates().forEach(stateId -> {
            String stateName = stateService.getStateName(stateId);
            log.info("  State: {}", stateName);
        });
    }
    
    @Test
    public void testMultiplePathsToSameTarget() {
        log.info("=== TESTING MULTIPLE PATHS TO SAME TARGET ===");
        
        // Get states
        io.github.jspinak.brobot.model.state.State dashboardState = 
            stateService.getState("Dashboard").orElseThrow();
        io.github.jspinak.brobot.model.state.State profileState = 
            stateService.getState("Profile").orElseThrow();
        io.github.jspinak.brobot.model.state.State settingsState = 
            stateService.getState("Settings").orElseThrow();
        
        // Set both Profile and Settings as active (multiple starting points)
        stateMemory.addActiveState(profileState.getId());
        stateMemory.addActiveState(settingsState.getId());
        
        // Find paths to Dashboard
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            dashboardState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find paths to Dashboard");
        
        // Should find at least 2 paths (one from each starting state)
        assertTrue(paths.getPaths().size() >= 2, 
                  "Should find multiple paths to Dashboard");
        
        log.info("Found {} paths to Dashboard", paths.getPaths().size());
        
        // Check path scores (lower is better)
        paths.sort();
        Path bestPath = paths.getPaths().get(0);
        log.info("Best path score: {}", bestPath.getScore());
    }
    
    @Test
    public void testPathWithPriority() {
        log.info("=== TESTING PATH WITH PRIORITY ===");
        
        // Dashboard to Settings transition has priority 10 (higher score)
        io.github.jspinak.brobot.model.state.State dashboardState = 
            stateService.getState("Dashboard").orElseThrow();
        io.github.jspinak.brobot.model.state.State settingsState = 
            stateService.getState("Settings").orElseThrow();
        
        // Set Dashboard as active
        stateMemory.addActiveState(dashboardState.getId());
        
        // Find path to Settings
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            settingsState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find path to Settings");
        
        Path path = paths.getPaths().get(0);
        
        // Check that the transition has the expected score
        assertTrue(path.getScore() >= 10, 
                  "Path score should reflect transition priority");
        
        log.info("Path to Settings has score: {}", path.getScore());
    }
    
    @Test
    public void testNoPathScenario() {
        log.info("=== TESTING NO PATH SCENARIO ===");
        
        // Create an isolated state with no incoming transitions
        io.github.jspinak.brobot.model.state.State isolatedState = 
            new io.github.jspinak.brobot.model.state.State.Builder("IsolatedState")
                .build();
        stateService.save(isolatedState);
        
        // Set Home as active
        io.github.jspinak.brobot.model.state.State homeState = 
            stateService.getState("Home").orElseThrow();
        stateMemory.addActiveState(homeState.getId());
        
        // Try to find path to isolated state
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            isolatedState.getId()
        );
        
        assertTrue(paths.isEmpty(), 
                  "Should not find path to isolated state");
        
        log.info("Correctly identified no path to isolated state");
    }
    
    @Test
    public void testPathCleaning() {
        log.info("=== TESTING PATH CLEANING ===");
        
        // Get states
        io.github.jspinak.brobot.model.state.State homeState = 
            stateService.getState("Home").orElseThrow();
        io.github.jspinak.brobot.model.state.State dashboardState = 
            stateService.getState("Dashboard").orElseThrow();
        
        // Set Home as active
        stateMemory.addActiveState(homeState.getId());
        
        // Find paths to Dashboard
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            dashboardState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find paths");
        
        int originalPathCount = paths.getPaths().size();
        
        // Clean paths (remove invalid paths based on active states)
        Set<Long> activeStates = new HashSet<>(stateMemory.getActiveStates());
        Paths cleanedPaths = paths.cleanPaths(activeStates, null);
        
        // In mock mode, all paths should remain valid
        assertEquals(originalPathCount, cleanedPaths.getPaths().size(),
                    "Cleaned paths should maintain valid paths");
        
        log.info("Path cleaning: {} paths before, {} after", 
                originalPathCount, cleanedPaths.getPaths().size());
    }
    
    @Test
    public void testBidirectionalTransitions() {
        log.info("=== TESTING BIDIRECTIONAL TRANSITIONS ===");
        
        // Dashboard <-> Profile have bidirectional transitions
        io.github.jspinak.brobot.model.state.State dashboardState = 
            stateService.getState("Dashboard").orElseThrow();
        io.github.jspinak.brobot.model.state.State profileState = 
            stateService.getState("Profile").orElseThrow();
        
        // Test Dashboard -> Profile
        stateMemory.removeAllStates();
        stateMemory.addActiveState(dashboardState.getId());
        
        Paths toProfile = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            profileState.getId()
        );
        
        assertFalse(toProfile.isEmpty(), 
                   "Should find path from Dashboard to Profile");
        
        // Test Profile -> Dashboard
        stateMemory.removeAllStates();
        stateMemory.addActiveState(profileState.getId());
        
        Paths toDashboard = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            dashboardState.getId()
        );
        
        assertFalse(toDashboard.isEmpty(), 
                   "Should find path from Profile to Dashboard");
        
        log.info("Bidirectional transitions verified");
    }
    
    @Test
    public void testPathTraversalExecution() {
        log.info("=== TESTING PATH TRAVERSAL EXECUTION ===");
        
        // Get states
        io.github.jspinak.brobot.model.state.State homeState = 
            stateService.getState("Home").orElseThrow();
        io.github.jspinak.brobot.model.state.State dashboardState = 
            stateService.getState("Dashboard").orElseThrow();
        
        // Set Home as active
        stateMemory.removeAllStates();
        stateMemory.addActiveState(homeState.getId());
        
        // Find path to Dashboard
        Paths paths = pathFinder.getPathsToState(
            stateMemory.getActiveStates(), 
            dashboardState.getId()
        );
        
        assertFalse(paths.isEmpty(), "Should find path");
        
        Path selectedPath = paths.getPaths().get(0);
        
        // In a real scenario, PathTraverser would execute each transition
        // In mock mode, we verify the path structure
        log.info("Path ready for traversal:");
        log.info("  States in path: {}", selectedPath.getStates().size());
        log.info("  Transitions: {}", selectedPath.getTransitions().size());
        
        // Verify path ends at target
        Long lastState = selectedPath.getStates().get(
            selectedPath.getStates().size() - 1);
        assertEquals(dashboardState.getId(), lastState, 
                    "Path should end at Dashboard");
    }
}