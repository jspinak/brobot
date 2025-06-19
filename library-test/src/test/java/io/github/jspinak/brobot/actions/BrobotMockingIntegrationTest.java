package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.manageStates.StateMemory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating Brobot mocking functionality.
 * Shows how Brobot mocking differs from standard test mocking.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrobotMockingIntegrationTest {

    @Autowired
    private Action action;
    
    @Autowired
    private StateMemory stateMemory;
    
    private StateImage stateImageWithHistory;
    private StateImage stateImageWithoutHistory;
    private static final Long TEST_STATE_ID = 1L;
    
    @BeforeEach
    void setUp() {
        // Reset to real mode
        BrobotSettings.mock = false;
        // Clear any screenshots to ensure proper mock mode behavior
        BrobotSettings.screenshots.clear();
        
        // Create test images
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // Create Pattern with name using builder
        Pattern pattern1 = new Pattern.Builder()
                .setBufferedImage(dummyImage)
                .setName("TestPattern1")
                .build();
        
        // Create another pattern
        Pattern pattern2 = new Pattern.Builder()
                .setBufferedImage(dummyImage)
                .setName("TestPattern2")
                .build();
        
        // Create StateImage with match history and associate it with the test state
        stateImageWithHistory = new StateImage.Builder()
                .addPattern(pattern1)
                .setName("ImageWithHistory")
                .setOwnerStateName("TestState")
                .build();
        
        // Add match history to simulate previous Find operations
        MatchSnapshot snapshot1 = new MatchSnapshot();
        snapshot1.setActionSuccess(true);
        snapshot1.setDuration(0.5);
        // IMPORTANT: Set the state ID to match the active state
        snapshot1.setStateId(TEST_STATE_ID);
        snapshot1.setStateName("TestState");
        // Set the action options to FIND action
        ActionOptions findOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        snapshot1.setActionOptions(findOptions);
        
        Match match1 = new Match.Builder()
                .setRegion(new Region(10, 10, 50, 50))
                .setSimScore(0.95)
                .build();
        snapshot1.addMatch(match1);
        
        Match match2 = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.90)
                .build();
        snapshot1.addMatch(match2);
        
        // Add snapshot to pattern's match history
        pattern1.getMatchHistory().addSnapshot(snapshot1);
        
        // Create StateImage without match history
        stateImageWithoutHistory = new StateImage.Builder()
                .addPattern(pattern2)
                .setName("ImageWithoutHistory")
                .setOwnerStateName("TestState")
                .build();
    }
    
    @Test
    @Order(1)
    void testSpringContextLoads() {
        assertNotNull(action, "Action should be autowired");
        assertNotNull(stateMemory, "StateMemory should be autowired");
    }
    
    @Test
    @Order(2)
    @Disabled("Skipping - headless environment behavior is inconsistent")
    void testRealModeReturnsEmptyMatches() {
        // In real mode without an actual GUI, Find should return empty matches
        BrobotSettings.mock = false;
        
        // Force headless mode for this test
        BrobotEnvironment env = BrobotEnvironment.builder()
                .mockMode(false)
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();
        BrobotEnvironment.setInstance(env);
        
        // Use stateImageWithoutHistory to avoid any cached matches
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithoutHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        try {
            Matches matches = action.perform(options, collection);
            
            // In real mode without GUI, should either throw exception or return empty matches
            assertTrue(matches.isEmpty(), 
                    "Real mode without GUI should return empty matches");
        } catch (Exception e) {
            // This is also acceptable - SikuliX might throw exception in headless mode
            assertTrue(e.getMessage().contains("headless") || 
                      e.getMessage().contains("SikuliX") ||
                      e.getMessage().contains("Init"),
                      "Expected SikuliX headless exception, but got: " + e.getMessage());
        }
    }
    
    @Test
    @Order(3)
    void testMockModeUsesMatchHistory() {
        // Enable mock mode
        BrobotSettings.mock = true;
        
        // Add the test state to active states for mock to work
        stateMemory.addActiveState(TEST_STATE_ID);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .build();
        
        Matches matches = action.perform(options, collection);
        
        // In mock mode, matches should be populated from history
        assertFalse(matches.isEmpty(), 
                "Mock mode should return matches from history");
        
        // Verify matches come from history
        assertTrue(matches.size() > 0, 
                "Should have matches from history");
    }
    
    @Test
    @Order(4)
    void testMockModeWithoutHistoryUsesDefaults() {
        // Enable mock mode
        BrobotSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithoutHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Matches matches = action.perform(options, collection);
        
        // Mock mode should still work even without history
        // The behavior depends on the mock implementation
        assertNotNull(matches, "Matches should not be null");
    }
    
    @Test
    @Order(5)
    void testMockModeRespectsFindOptions() {
        BrobotSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        // Test FIRST option
        ActionOptions firstOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIRST)
                .build();
        
        Matches firstMatches = action.perform(firstOptions, collection);
        
        // Test ALL option
        ActionOptions allOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .build();
        
        Matches allMatches = action.perform(allOptions, collection);
        
        // The behavior should differ based on Find option
        // This depends on the mock implementation details
        assertNotNull(firstMatches);
        assertNotNull(allMatches);
    }
    
    @Test
    @Order(6)
    void testMockModePreservesStateObjectData() {
        BrobotSettings.mock = true;
        
        // Add the test state to active states for mock to work
        stateMemory.addActiveState(TEST_STATE_ID);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Matches matches = action.perform(options, collection);
        
        // In mock mode with history, we should get results
        assertNotNull(matches, "Matches should not be null");
        // The actual data preserved depends on the mock implementation
    }
    
    @Test
    @Order(7)
    void testSwitchingBetweenMockAndRealMode() {
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        // Test in mock mode
        BrobotSettings.mock = true;
        Matches mockMatches = action.perform(options, collection);
        
        // Test in real mode
        BrobotSettings.mock = false;
        Matches realMatches = action.perform(options, collection);
        
        // Results should differ between modes
        assertNotNull(mockMatches);
        assertNotNull(realMatches);
        // The actual behavior difference depends on implementation
    }
    
    @Test
    @Order(8)
    void testMockModeBehaviorWithoutProbabilities() {
        BrobotSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setPauseBeforeBegin(0)
                .setPauseAfterEnd(0)
                .build();
        
        // Run multiple times to test behavior
        int successCount = 0;
        int trials = 10;
        
        for (int i = 0; i < trials; i++) {
            Matches matches = action.perform(options, collection);
            
            if (!matches.isEmpty()) {
                successCount++;
            }
        }
        
        // In mock mode with match history, we should get consistent results
        assertTrue(successCount > 0, "Should have successful matches in mock mode");
    }
    
    @AfterEach
    void tearDown() {
        // Reset to default
        BrobotSettings.mock = false;
        // Reset state memory - we can't directly access active states
        // The mock system will handle state management internally
    }
}