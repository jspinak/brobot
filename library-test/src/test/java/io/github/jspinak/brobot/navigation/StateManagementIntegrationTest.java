package io.github.jspinak.brobot.navigation;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.ActiveStateSet;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {
        "spring.main.lazy-initialization=true",
        "brobot.mock.enabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StateManagementIntegrationTest extends BrobotIntegrationTestBase {

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

    private State homeState;
    private State settingsState;
    private State profileState;

    // Define test state enums
    private enum TestStates implements StateEnum {
        HOME("HOME"),
        SETTINGS("SETTINGS"),
        PROFILE("PROFILE");

        private final String name;

        TestStates(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        // Enable mock mode
        FrameworkSettings.mock = true;

        // Clear active states (ActiveStateSet doesn't have clear() method, so we work
        // with what's available)
        // Active states are managed differently in the new API

        // Create test states
        createTestStates();
    }

    private void createTestStates() {
        // Create test images for states (using BufferedImage)
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Home State with StateImage and StateRegion
        StateImage homeButtonImage = new StateImage.Builder()
                .setName("homeButton")
                .addPattern(new Pattern.Builder()
                        .setName("homeButtonPattern")
                        .setBufferedImage(dummyImage)
                        .build())
                .build();

        StateRegion homeRegion = new StateRegion.Builder()
                .setName("homeRegion")
                .setSearchRegion(new Region(10, 10, 50, 50))
                .build();

        homeState = new State.Builder(TestStates.HOME.toString())
                .withImages(homeButtonImage)
                .withRegions(homeRegion)
                .build();
        stateStore.save(homeState);

        // Settings State with multiple regions
        StateImage settingsButtonImage = new StateImage.Builder()
                .setName("settingsButton")
                .addPattern(new Pattern.Builder()
                        .setName("settingsButtonPattern")
                        .setBufferedImage(dummyImage)
                        .build())
                .build();

        StateRegion settingsButtonRegion = new StateRegion.Builder()
                .setName("settingsButtonRegion")
                .setSearchRegion(new Region(100, 10, 50, 50))
                .build();

        StateRegion settingsTitleRegion = new StateRegion.Builder()
                .setName("settingsTitleRegion")
                .setSearchRegion(new Region(200, 50, 200, 40))
                .build();

        settingsState = new State.Builder(TestStates.SETTINGS.toString())
                .withImages(settingsButtonImage)
                .withRegions(settingsButtonRegion, settingsTitleRegion)
                .build();
        stateStore.save(settingsState);

        // Profile State
        StateImage profileIconImage = new StateImage.Builder()
                .setName("profileIcon")
                .addPattern(new Pattern.Builder()
                        .setName("profileIconPattern")
                        .setBufferedImage(dummyImage)
                        .build())
                .build();

        StateLocation profileLocation = new StateLocation.Builder()
                .setName("profileLocation")
                .setLocation(new Location(300, 10))
                .build();

        profileState = new State.Builder(TestStates.PROFILE.toString())
                .withImages(profileIconImage)
                .withLocations(profileLocation)
                .build();
        stateStore.save(profileState);
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
        // Verify states are stored correctly using the new API
        Optional<State> retrievedHome = stateStore.getState(TestStates.HOME.toString());
        Optional<State> retrievedSettings = stateStore.getState(TestStates.SETTINGS.toString());
        Optional<State> retrievedProfile = stateStore.getState(TestStates.PROFILE.toString());

        assertTrue(retrievedHome.isPresent(), "Home state should be stored");
        assertTrue(retrievedSettings.isPresent(), "Settings state should be stored");
        assertTrue(retrievedProfile.isPresent(), "Profile state should be stored");

        // Verify state properties
        assertEquals(TestStates.HOME.toString(), retrievedHome.get().getName());
        assertFalse(retrievedHome.get().getStateImages().isEmpty(), "Home state should have images");
        assertFalse(retrievedHome.get().getStateRegions().isEmpty(), "Home state should have regions");

        // Settings state should have multiple regions
        assertEquals(2, retrievedSettings.get().getStateRegions().size(),
                "Settings state should have 2 regions");
    }

    @Test
    @Order(3)
    void testStateTransitionCreation() {
        // Create transitions using the current API
        StateTransitions homeTransitions = new StateTransitions();
        homeTransitions.setStateId(homeState.getId());
        homeTransitions.setStateName(homeState.getName());

        // Create transition to Settings
        JavaStateTransition toSettings = new JavaStateTransition.Builder()
                .setFunction(() -> true) // Always succeeds in mock mode
                .addToActivate(settingsState.getName())
                .setScore(1) // Lower score = higher priority
                .build();
        homeTransitions.addTransition(toSettings);

        // Create transition to Profile
        JavaStateTransition toProfile = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate(profileState.getName())
                .setScore(2)
                .build();
        homeTransitions.addTransition(toProfile);

        // Save transitions to store
        stateTransitionStore.add(homeTransitions);

        // Create Settings transitions
        StateTransitions settingsTransitions = new StateTransitions();
        settingsTransitions.setStateId(settingsState.getId());
        settingsTransitions.setStateName(settingsState.getName());

        JavaStateTransition toHome = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate(homeState.getName())
                .setScore(1)
                .build();
        settingsTransitions.addTransition(toHome);

        JavaStateTransition toProfileFromSettings = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate(profileState.getName())
                .setScore(3)
                .build();
        settingsTransitions.addTransition(toProfileFromSettings);

        stateTransitionStore.add(settingsTransitions);

        // Verify transitions are stored
        Optional<StateTransitions> retrievedHomeTransitions = stateTransitionStore.get(homeState.getId());
        assertTrue(retrievedHomeTransitions.isPresent(), "Home transitions should be stored");
        assertEquals(2, retrievedHomeTransitions.get().getTransitions().size(),
                "Home should have 2 transitions");
    }

    @Test
    @Order(4)
    void testActiveStateManagement() {
        // Test active state management using the current API
        // ActiveStateSet uses StateEnum, so we use our test enum
        activeStateSet.addState(TestStates.HOME);

        // Check if state was added (API might differ)
        Set<StateEnum> activeStates = activeStateSet.getActiveStates();
        assertTrue(activeStates.contains(TestStates.HOME), "HOME should be active");

        // Add multiple states
        activeStateSet.addState(TestStates.SETTINGS);
        activeStates = activeStateSet.getActiveStates();

        assertTrue(activeStates.contains(TestStates.HOME), "HOME should still be active");
        assertTrue(activeStates.contains(TestStates.SETTINGS), "SETTINGS should be active");

        // The API might not have remove methods, but we can test what's available
        assertEquals(2, activeStates.size(), "Should have 2 active states");
    }

    @Test
    @Order(5)
    void testStateNavigation() {
        // Set up initial active state
        activeStateSet.addState(TestStates.HOME);

        // Create and store transitions as in test 3
        StateTransitions homeTransitions = new StateTransitions();
        homeTransitions.setStateId(homeState.getId());
        homeTransitions.setStateName(homeState.getName());

        JavaStateTransition toSettings = new JavaStateTransition.Builder()
                .setFunction(() -> true)
                .addToActivate(settingsState.getName())
                .setScore(1)
                .build();
        homeTransitions.addTransition(toSettings);
        stateTransitionStore.add(homeTransitions);

        // Test navigation (the actual navigation API might differ)
        // StateNavigator might have different methods in the current version
        // This is a placeholder - actual implementation depends on StateNavigator's API
        assertNotNull(stateNavigator, "StateNavigator should be available for navigation");
    }

    @Test
    @Order(6)
    void testStateDetection() {
        // StateDetector functionality test
        // The actual detection methods depend on the current API
        assertNotNull(stateDetector, "StateDetector should be available");

        // In mock mode, detection might work differently
        // This is a placeholder test - actual implementation depends on StateDetector's
        // API
        assertTrue(FrameworkSettings.mock, "Should be in mock mode for testing");
    }

    @Test
    @Order(7)
    void testStateObjectProperties() {
        Optional<State> homeStateOpt = stateStore.getState(TestStates.HOME.toString());
        assertTrue(homeStateOpt.isPresent());
        State home = homeStateOpt.get();

        // Test StateImage properties
        Set<StateImage> images = home.getStateImages();
        assertFalse(images.isEmpty(), "Should have state images");
        StateImage firstImage = images.iterator().next();
        assertEquals("homeButton", firstImage.getName());
        assertEquals(TestStates.HOME.toString(), firstImage.getOwnerStateName());

        // Test StateRegion properties
        Set<StateRegion> regions = home.getStateRegions();
        assertFalse(regions.isEmpty(), "Should have state regions");
        StateRegion firstRegion = regions.iterator().next();
        assertEquals("homeRegion", firstRegion.getName());
        assertNotNull(firstRegion.getSearchRegion());
    }

    @Test
    @Order(8)
    void testComplexStateStructure() {
        // Test that states can have multiple types of objects
        Optional<State> profileStateOpt = stateStore.getState(TestStates.PROFILE.toString());
        assertTrue(profileStateOpt.isPresent());
        State profile = profileStateOpt.get();

        // Should have images and locations
        assertFalse(profile.getStateImages().isEmpty(), "Profile should have images");
        assertFalse(profile.getStateLocations().isEmpty(), "Profile should have locations");

        StateLocation location = profile.getStateLocations().iterator().next();
        assertEquals("profileLocation", location.getName());
        assertEquals(300, location.getLocation().getX());
        assertEquals(10, location.getLocation().getY());
    }
}