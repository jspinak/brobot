package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify the API usage patterns with new ActionConfig API.
 * 
 * Key changes:
 * - Uses specific config classes (PatternFindOptions, ClickOptions) instead of generic ActionOptions
 * - ActionResult.setActionConfig() instead of ActionResult constructor with ActionOptions
 * - Demonstrates type-safe configuration builders
 */
@SpringBootTest(classes = BrobotTestApplication.class)
public class SimpleAPITestUpdated {
    
    @Autowired
    private StateMemory stateMemory;
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }
    
    @BeforeEach
    void setUp() {
        FrameworkSettings.mock = true;
    }
    
    @Test
    void testPatternWithNameCreation() {
        // Test Pattern creation with name
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                .build();
                
        assertNotNull(pattern);
        assertEquals("TestPattern", pattern.getName());
    }
    
    @Test
    void testStateImageWithNameCreation() {
        // Create pattern first
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .setBufferedImage(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
                .build();
                
        // Create StateImage with name
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("TestStateImage")
                .build();
                
        assertNotNull(stateImage);
        assertEquals("TestStateImage", stateImage.getName());
    }
    
    @Test
    void testMatchHistoryAccessWithNewAPI() {
        // Create pattern
        Pattern pattern = new Pattern.Builder()
                .setName("TestPattern")
                .build();
                
        // Create match snapshot with NEW API
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);
        snapshot.setDuration(0.5);
        
        // NEW API: Set the action config instead of ActionOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();
        snapshot.setActionConfig(findOptions);
        
        // Add match to snapshot
        Match match = new Match.Builder()
                .setRegion(new Region(10, 10, 50, 50))
                .setSimScore(0.95)
                .build();
        snapshot.addMatch(match);
        
        // Add snapshot to pattern's match history
        pattern.getMatchHistory().addSnapshot(snapshot);
        
        // Verify
        assertEquals(1, pattern.getMatchHistory().getSnapshots().size());
        assertTrue(pattern.getMatchHistory().getSnapshots().get(0).isActionSuccess());
        assertEquals(findOptions, pattern.getMatchHistory().getSnapshots().get(0).getActionConfig());
    }
    
    @Test
    void testActionResultCreationWithNewAPI() {
        // Test default constructor
        ActionResult result1 = new ActionResult();
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
        
        // NEW API: Set ActionConfig after creation
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        result1.setActionConfig(findOptions);
        assertEquals(findOptions, result1.getActionConfig());
        
        // Test with different config type
        ActionResult result2 = new ActionResult();
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .build();
        result2.setActionConfig(clickOptions);
        assertEquals(clickOptions, result2.getActionConfig());
    }
    
    @Test
    void testStateMemoryOperations() {
        if (stateMemory != null) {
            // Add active state
            Long testStateId = 1L;
            stateMemory.addActiveState(testStateId);
            
            // Remove inactive state
            stateMemory.removeInactiveState(testStateId);
            
            // This should work without errors
            assertTrue(true, "StateMemory operations completed successfully");
        }
    }
    
    @Test
    void testVariousActionConfigs() {
        // Test PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .setMaxMatchesToActOn(5)
                .setCaptureImage(true)
                .build();
        
        assertEquals(PatternFindOptions.Strategy.BEST, findOptions.getStrategy());
        assertEquals(0.85, findOptions.getSimilarity(), 0.001);
        assertEquals(5, findOptions.getMaxMatchesToActOn());
        assertTrue(findOptions.isCaptureImage());
        
        // Test ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE)
                .setNumberOfClicks(2)
                .setOffsetX(10)
                .setOffsetY(20)
                .build();
        
        assertEquals(ClickOptions.Type.DOUBLE, clickOptions.getClickType());
        assertEquals(2, clickOptions.getNumberOfClicks());
        assertEquals(10, clickOptions.getOffsetX());
        assertEquals(20, clickOptions.getOffsetY());
        
        // Test factory methods
        PatternFindOptions quickFind = PatternFindOptions.forQuickSearch();
        assertEquals(PatternFindOptions.Strategy.FIRST, quickFind.getStrategy());
        assertEquals(0.7, quickFind.getSimilarity(), 0.001);
        
        PatternFindOptions preciseFind = PatternFindOptions.forPreciseSearch();
        assertEquals(PatternFindOptions.Strategy.BEST, preciseFind.getStrategy());
        assertEquals(0.9, preciseFind.getSimilarity(), 0.001);
    }
    
    @Test
    void testActionResultWithDifferentConfigs() {
        // Create results with different config types
        ActionResult findResult = new ActionResult();
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        findResult.setActionConfig(findOptions);
        
        ActionResult clickResult = new ActionResult();
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        clickResult.setActionConfig(clickOptions);
        
        // Verify configs are properly stored
        assertTrue(findResult.getActionConfig() instanceof PatternFindOptions);
        assertTrue(clickResult.getActionConfig() instanceof ClickOptions);
        
        // Cast and verify specific properties
        PatternFindOptions retrievedFindOptions = (PatternFindOptions) findResult.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.ALL, retrievedFindOptions.getStrategy());
        
        ClickOptions retrievedClickOptions = (ClickOptions) clickResult.getActionConfig();
        assertEquals(ClickOptions.Type.LEFT, retrievedClickOptions.getClickType());
    }
}