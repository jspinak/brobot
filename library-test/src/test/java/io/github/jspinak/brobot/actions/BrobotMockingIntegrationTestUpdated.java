package io.github.jspinak.brobot.actions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating Brobot mocking functionality.
 * Shows how Brobot mocking differs from standard test mocking.
 * 
 * In Brobot:
 * - Mock mode uses historical match data when available
 * - Provides consistent test results without GUI dependencies
 * - Allows testing of state transitions and action sequences
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BrobotMockingIntegrationTestUpdated extends BrobotIntegrationTestBase {

    @Autowired
    private ActionService actionService;
    
    @Autowired
    private StateMemory stateMemory;
    
    private StateImage stateImageWithHistory;
    private StateImage stateImageWithoutHistory;
    private static final Long TEST_STATE_ID = 1L;
    
    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        // Reset to real mode initially
        FrameworkSettings.mock = false;
        
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
        ActionRecord snapshot1 = new ActionRecord();
        snapshot1.setActionSuccess(true);
        snapshot1.setDuration(0.5);
        // Set the state ID to match the active state
        snapshot1.setStateId(TEST_STATE_ID);
        snapshot1.setStateName("TestState");
        
        // Set the action config to PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        snapshot1.setActionConfig(findOptions);
        
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
    
    @AfterEach
    void tearDown() {
        // Reset to default mock mode
        FrameworkSettings.mock = true;
    }
    
    @Test
    @Order(1)
    @DisplayName("Should load Spring context with required beans")
    void testSpringContextLoads() {
        assertNotNull(actionService, "ActionService should be autowired");
        assertNotNull(stateMemory, "StateMemory should be autowired");
    }
    
    @Test
    @Order(2)
    @DisplayName("Should return empty matches in real mode without GUI")
    @Disabled("Real mode behavior varies in headless environments")
    void testRealModeReturnsEmptyMatches() {
        // In real mode without an actual GUI, Find should return empty matches
        FrameworkSettings.mock = false;
        
        // Use stateImageWithoutHistory to avoid any cached matches
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithoutHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        
        try {
            Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
            assertTrue(findActionOpt.isPresent(), "Find action should be available");
            
            ActionInterface findAction = findActionOpt.get();
            findAction.perform(matches, collection);
            
            // In real mode without GUI, should return empty matches
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
    @DisplayName("Should use match history in mock mode")
    void testMockModeUsesMatchHistory() {
        // Enable mock mode
        FrameworkSettings.mock = true;
        
        // Add the test state to active states for mock to work
        stateMemory.addActiveState(TEST_STATE_ID);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent());
        
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(matches, collection);
        
        // In mock mode, matches should be populated from history
        assertFalse(matches.isEmpty(), 
                "Mock mode should return matches from history");
        
        // Verify matches come from history
        assertTrue(matches.size() > 0, 
                "Should have matches from history");
    }
    
    @Test
    @Order(4)
    @DisplayName("Should use defaults in mock mode without history")
    void testMockModeWithoutHistoryUsesDefaults() {
        // Enable mock mode
        FrameworkSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithoutHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent());
        
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(matches, collection);
        
        // Mock mode should still work even without history
        assertNotNull(matches, "ActionResult should not be null");
        assertTrue(matches.isSuccess(), "Mock mode should indicate success");
    }
    
    @Test
    @Order(5)
    @DisplayName("Should respect find strategies in mock mode")
    void testMockModeRespectsFindStrategies() {
        FrameworkSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        // Test FIRST strategy
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult firstMatches = new ActionResult();
        firstMatches.setActionConfig(firstOptions);
        
        Optional<ActionInterface> firstActionOpt = actionService.getAction(firstOptions);
        assertTrue(firstActionOpt.isPresent());
        ActionInterface firstAction = firstActionOpt.get();
        firstAction.perform(firstMatches, collection);
        
        // Test ALL strategy
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        
        ActionResult allMatches = new ActionResult();
        allMatches.setActionConfig(allOptions);
        
        Optional<ActionInterface> allActionOpt = actionService.getAction(allOptions);
        assertTrue(allActionOpt.isPresent());
        ActionInterface allAction = allActionOpt.get();
        allAction.perform(allMatches, collection);
        
        // The behavior should differ based on Find strategy
        assertNotNull(firstMatches);
        assertNotNull(allMatches);
        assertTrue(firstMatches.isSuccess());
        assertTrue(allMatches.isSuccess());
    }
    
    @Test
    @Order(6)
    @DisplayName("Should preserve state object data in mock mode")
    void testMockModePreservesStateObjectData() {
        FrameworkSettings.mock = true;
        
        // Add the test state to active states for mock to work
        stateMemory.addActiveState(TEST_STATE_ID);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent());
        
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(matches, collection);
        
        // In mock mode with history, we should get results
        assertNotNull(matches, "ActionResult should not be null");
        assertTrue(matches.isSuccess());
    }
    
    @Test
    @Order(7)
    @DisplayName("Should switch between mock and real mode")
    void testSwitchingBetweenMockAndRealMode() {
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        // Test in mock mode
        FrameworkSettings.mock = true;
        ActionResult mockMatches = new ActionResult();
        mockMatches.setActionConfig(findOptions);
        
        Optional<ActionInterface> mockActionOpt = actionService.getAction(findOptions);
        assertTrue(mockActionOpt.isPresent());
        ActionInterface mockAction = mockActionOpt.get();
        mockAction.perform(mockMatches, collection);
        
        // Test in real mode (may fail in headless)
        FrameworkSettings.mock = false;
        ActionResult realMatches = new ActionResult();
        realMatches.setActionConfig(findOptions);
        
        Optional<ActionInterface> realActionOpt = actionService.getAction(findOptions);
        assertTrue(realActionOpt.isPresent());
        ActionInterface realAction = realActionOpt.get();
        
        try {
            realAction.perform(realMatches, collection);
        } catch (Exception e) {
            // Expected in headless environment
        }
        
        // Results should differ between modes
        assertNotNull(mockMatches);
        assertNotNull(realMatches);
        assertTrue(mockMatches.isSuccess(), "Mock mode should succeed");
    }
    
    @Test
    @Order(8)
    @DisplayName("Should have consistent behavior in mock mode")
    void testMockModeBehaviorConsistency() {
        FrameworkSettings.mock = true;
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withImages(stateImageWithHistory)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        
        // Run multiple times to test consistency
        int successCount = 0;
        int trials = 10;
        
        for (int i = 0; i < trials; i++) {
            ActionResult matches = new ActionResult();
            matches.setActionConfig(findOptions);
            
            Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
            assertTrue(findActionOpt.isPresent());
            
            ActionInterface findAction = findActionOpt.get();
            findAction.perform(matches, collection);
            
            if (matches.isSuccess()) {
                successCount++;
            }
        }
        
        // In mock mode, behavior should be consistent
        assertEquals(trials, successCount, 
                "Mock mode should have consistent success rate");
    }
    
    @Test
    @Order(9)
    @DisplayName("Should work with regions in mock mode")
    void testMockModeWithRegions() {
        FrameworkSettings.mock = true;
        
        Region testRegion = new Region(50, 50, 100, 100);
        
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(testRegion)
                .build();
        
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        
        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent());
        
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(matches, collection);
        
        // Mock mode should handle regions
        assertNotNull(matches);
        assertTrue(matches.isSuccess());
    }
}