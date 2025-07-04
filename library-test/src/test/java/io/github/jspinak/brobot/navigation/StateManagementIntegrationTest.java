package io.github.jspinak.brobot.navigation;

import io.github.jspinak.brobot.action.BrobotSettings;
import io.github.jspinak.brobot.navigation.navigation.StateNavigator;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.stateDetection.StateDetector;
import io.github.jspinak.brobot.navigation.stateMachine.ActiveStateSet;
import io.github.jspinak.brobot.navigation.stateMachine.StateIdRegulator;
import io.github.jspinak.brobot.datatypes.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.services.Init;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the State Management system.
 * 
 * These tests verify the integration between:
 * - State creation and storage
 * - State transitions and navigation
 * - State detection
 * - Active state management
 * - Spring context and dependency injection
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StateManagementIntegrationTest {

    @Autowired
    private StateStore stateStore;
    
    @Autowired
    private StateTransitionStore stateTransitionStore;
    
    @Autowired
    private StateNavigator stateNavigator;
    
    @Autowired
    private StateDetector stateDetector;
    
    @Autowired
    private ActiveStateSet activeStateSet;
    
    @Autowired
    private StateIdRegulator stateIdRegulator;
    
    @Autowired
    private BrobotSettings brobotSettings;
    
    @MockBean
    private Init init;
    
    private State homeState;
    private State settingsState;
    private State profileState;
    
    @BeforeEach
    void setUp() {
        // Configure mock mode
        when(init.setGlobalMock()).thenReturn(true);
        brobotSettings.mock = true;
        
        // Clear repositories
        stateStore.emptyRepos();
        stateTransitionStore.emptyRepos();
        activeStateSet.emptyRepos();
        
        // Create test states
        createTestStates();
    }
    
    private void createTestStates() {
        // Home State
        StateObject homeButton = new StateObject.Builder()
            .withName("homeButton")
            .withRegion(new Region(10, 10, 50, 50))
            .build();
            
        homeState = new State.Builder("HOME")
            .withStateObjects(homeButton)
            .setHidden(false)
            .build();
        stateStore.add(homeState);
        
        // Settings State
        StateObject settingsButton = new StateObject.Builder()
            .withName("settingsButton")
            .withRegion(new Region(100, 10, 50, 50))
            .build();
            
        StateObject settingsTitle = new StateObject.Builder()
            .withName("settingsTitle")
            .withRegion(new Region(200, 50, 200, 40))
            .build();
            
        settingsState = new State.Builder("SETTINGS")
            .withStateObjects(settingsButton, settingsTitle)
            .setHidden(false)
            .build();
        stateStore.add(settingsState);
        
        // Profile State
        StateObject profileIcon = new StateObject.Builder()
            .withName("profileIcon")
            .withRegion(new Region(300, 10, 50, 50))
            .build();
            
        profileState = new State.Builder("PROFILE")
            .withStateObjects(profileIcon)
            .setHidden(false)
            .build();
        stateStore.add(profileState);
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(stateStore, "StateStore should be autowired");
        assertNotNull(stateTransitionStore, "StateTransitionStore should be autowired");
        assertNotNull(stateNavigator, "StateNavigator should be autowired");
        assertNotNull(stateDetector, "StateDetector should be autowired");
        assertNotNull(activeStateSet, "ActiveStateSet should be autowired");
    }
    
    @Test
    @Order(2)
    void testStateStorage() {
        // Verify states are stored correctly
        assertTrue(stateStore.get("HOME").isPresent());
        assertTrue(stateStore.get("SETTINGS").isPresent());
        assertTrue(stateStore.get("PROFILE").isPresent());
        
        // Verify state properties
        State retrievedHome = stateStore.get("HOME").get();
        assertEquals("HOME", retrievedHome.getName());
        assertFalse(retrievedHome.getStateObjects().isEmpty());
        assertEquals(1, retrievedHome.getStateObjects().size());
    }
    
    @Test
    @Order(3)
    void testStateTransitionCreation() {
        // Create transitions
        StateTransitions homeTransitions = new StateTransitions();
        homeTransitions.setStateId(homeState.getId());
        homeTransitions.setStateName(homeState.getName());
        homeTransitions.addTransition(settingsState.getId(), 0.9f);
        homeTransitions.addTransition(profileState.getId(), 0.8f);
        
        StateTransitions settingsTransitions = new StateTransitions();
        settingsTransitions.setStateId(settingsState.getId());
        settingsTransitions.setStateName(settingsState.getName());
        settingsTransitions.addTransition(homeState.getId(), 0.95f);
        settingsTransitions.addTransition(profileState.getId(), 0.7f);
        
        // Store transitions
        stateTransitionStore.add(homeTransitions);
        stateTransitionStore.add(settingsTransitions);
        
        // Verify transitions
        assertTrue(stateTransitionStore.get(homeState.getId()).isPresent());
        StateTransitions retrievedHomeTransitions = stateTransitionStore.get(homeState.getId()).get();
        assertEquals(2, retrievedHomeTransitions.getTransitions().size());
        assertTrue(retrievedHomeTransitions.hasTransition(settingsState.getId()));
    }
    
    @Test
    @Order(4)
    void testActiveStateManagement() {
        // Initially no active states
        assertTrue(activeStateSet.getActiveStates().isEmpty());
        
        // Activate home state
        activeStateSet.add(homeState);
        assertEquals(1, activeStateSet.getActiveStates().size());
        assertTrue(activeStateSet.contains(homeState.getId()));
        
        // Activate multiple states
        activeStateSet.add(settingsState);
        assertEquals(2, activeStateSet.getActiveStates().size());
        
        // Remove state
        activeStateSet.remove(homeState);
        assertEquals(1, activeStateSet.getActiveStates().size());
        assertFalse(activeStateSet.contains(homeState.getId()));
        assertTrue(activeStateSet.contains(settingsState.getId()));
    }
    
    @Test
    @Order(5)
    void testStateDetection() {
        // Activate home state for detection
        activeStateSet.add(homeState);
        activeStateSet.add(settingsState);
        
        // In mock mode, state detector should find active states
        Set<State> detectedStates = stateDetector.findActiveStates();
        
        assertNotNull(detectedStates);
        assertFalse(detectedStates.isEmpty(), "Should detect states in mock mode");
    }
    
    @Test
    @Order(6)
    void testStateNavigation() {
        // Setup transitions for navigation
        StateTransitions homeTransitions = new StateTransitions();
        homeTransitions.setStateId(homeState.getId());
        homeTransitions.setStateName(homeState.getName());
        homeTransitions.addTransition(settingsState.getId(), 0.9f);
        stateTransitionStore.add(homeTransitions);
        
        StateTransitions settingsTransitions = new StateTransitions();
        settingsTransitions.setStateId(settingsState.getId());
        settingsTransitions.setStateName(settingsState.getName());
        settingsTransitions.addTransition(homeState.getId(), 0.9f);
        stateTransitionStore.add(settingsTransitions);
        
        // Set initial active state
        activeStateSet.add(homeState);
        
        // Navigate to settings
        boolean navigated = stateNavigator.goToState(settingsState);
        
        // In mock mode, navigation typically succeeds
        assertTrue(navigated, "Navigation should succeed in mock mode");
    }
    
    @Test
    @Order(7)
    void testStateIdRegulation() {
        // Test state ID assignment and regulation
        State newState = new State.Builder("NEW_STATE")
            .build();
            
        // Store state - ID should be assigned
        stateStore.add(newState);
        assertNotEquals(0, newState.getId(), "State should have been assigned an ID");
        
        // Verify ID is unique
        State anotherState = new State.Builder("ANOTHER_STATE")
            .build();
        stateStore.add(anotherState);
        
        assertNotEquals(newState.getId(), anotherState.getId(), 
            "States should have unique IDs");
    }
    
    @Test
    @Order(8)
    void testStateObjectRetrieval() {
        // Test retrieving state objects
        State home = stateStore.get("HOME").orElseThrow();
        
        List<StateObject> stateObjects = home.getStateObjects();
        assertEquals(1, stateObjects.size());
        
        StateObject homeButton = stateObjects.get(0);
        assertEquals("homeButton", homeButton.getName());
        assertNotNull(homeButton.getRegion());
        assertEquals(10, homeButton.getRegion().getX());
        assertEquals(10, homeButton.getRegion().getY());
    }
    
    @Test
    @Order(9)
    void testHiddenStates() {
        // Create a hidden state
        State hiddenState = new State.Builder("HIDDEN")
            .setHidden(true)
            .build();
        stateStore.add(hiddenState);
        
        // Verify hidden state properties
        assertTrue(hiddenState.isHidden());
        
        // Hidden states should still be retrievable
        assertTrue(stateStore.get("HIDDEN").isPresent());
    }
    
    @Test
    @Order(10)
    void testStateTransitionProbabilities() {
        // Create transitions with specific probabilities
        StateTransitions transitions = new StateTransitions();
        transitions.setStateId(homeState.getId());
        transitions.setStateName(homeState.getName());
        
        // Add transitions with probabilities
        transitions.addTransition(settingsState.getId(), 0.95f);
        transitions.addTransition(profileState.getId(), 0.60f);
        
        stateTransitionStore.add(transitions);
        
        // Retrieve and verify
        StateTransitions retrieved = stateTransitionStore.get(homeState.getId()).orElseThrow();
        assertEquals(0.95f, retrieved.getProbability(settingsState.getId()), 0.001);
        assertEquals(0.60f, retrieved.getProbability(profileState.getId()), 0.001);
        
        // Test best transition
        Long bestTransition = retrieved.getBestTransition();
        assertEquals(settingsState.getId(), bestTransition, 
            "Best transition should be to settings (highest probability)");
    }
    
    @Test
    @Order(11)
    void testComplexStateNetwork() {
        // Create a more complex state network
        State menuState = new State.Builder("MENU")
            .withStateObjects(new StateObject.Builder()
                .withName("menuIcon")
                .withRegion(new Region(400, 10, 50, 50))
                .build())
            .build();
        stateStore.add(menuState);
        
        // Create interconnected transitions
        StateTransitions menuTransitions = new StateTransitions();
        menuTransitions.setStateId(menuState.getId());
        menuTransitions.setStateName(menuState.getName());
        menuTransitions.addTransition(homeState.getId(), 0.9f);
        menuTransitions.addTransition(settingsState.getId(), 0.85f);
        menuTransitions.addTransition(profileState.getId(), 0.8f);
        stateTransitionStore.add(menuTransitions);
        
        // Update home transitions to include menu
        StateTransitions homeTransitions = stateTransitionStore.get(homeState.getId())
            .orElse(new StateTransitions());
        homeTransitions.setStateId(homeState.getId());
        homeTransitions.setStateName(homeState.getName());
        homeTransitions.addTransition(menuState.getId(), 0.88f);
        stateTransitionStore.add(homeTransitions);
        
        // Verify the network
        assertTrue(stateTransitionStore.get(menuState.getId()).isPresent());
        StateTransitions retrievedMenuTransitions = stateTransitionStore.get(menuState.getId()).get();
        assertEquals(3, retrievedMenuTransitions.getTransitions().size());
    }
}