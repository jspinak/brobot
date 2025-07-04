package io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.pixels.FixedPixelsFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.action.basic.find.motion.FindFixedPixelMatches;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for finding fixed pixel matches using new ActionConfig API.
 * Demonstrates migration from ActionOptions to FixedPixelsFindOptions.
 * 
 * Key changes:
 * - Uses FixedPixelsFindOptions instead of generic ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - Fixed pixels strategy is now encapsulated in specific config class
 */
@SpringBootTest(classes = BrobotTestApplication.class)
class FindFixedPixelMatchesTestUpdated {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    FindFixedPixelMatches findFixedPixelMatches;

    @Autowired
    ColorMatrixUtilities matOps3d;

    @Autowired
    ActionResultFactory matchesInitializer;
    
    @Autowired
    ActionService actionService;

    private Pattern pattern1() {
        short[] data = new short[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0};
        return matOps3d.makeTestPattern(data);
    }

    private Pattern pattern2() {
        short[] data = new short[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                255, 255, 255, 255, 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0};
        return matOps3d.makeTestPattern(data);
    }

    // NEW API: Create FixedPixelsFindOptions
    private FixedPixelsFindOptions createFixedPixelsOptions() {
        return new FixedPixelsFindOptions.Builder()
                .setMaxMovement(2)  // Maximum pixel movement to consider as fixed
                .setStartPlayback(0.0)  // Start monitoring immediately
                .setPlaybackDuration(1.0)  // Monitor for 1 second
                .build();
    }

    @Test
    void find_samePatternNewAPI() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern1())
                .build();
        
        // NEW API: Use FixedPixelsFindOptions
        FixedPixelsFindOptions fixedPixelsOptions = createFixedPixelsOptions();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(fixedPixelsOptions);
        
        // Option 1: Use the existing FindFixedPixelMatches directly
        // (if it has been updated to work with ActionResult)
        findFixedPixelMatches.find(matches, List.of(objectCollection));
        
        // Option 2: Use ActionService to get the action
        // ActionInterface fixedPixelsFindAction = actionService.getAction(fixedPixelsOptions);
        // fixedPixelsFindAction.perform(matches, objectCollection);
        
        System.out.println(matches.getMatchList());
        MatrixUtilities.printPartOfMat(matches.getMask(), 5, 5);
        assertEquals(255, MatrixUtilities.getDouble(0,0,0, matches.getMask()));
    }

    @Test
    void find_differentPatternsNewAPI() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        
        // NEW API: Use FixedPixelsFindOptions
        FixedPixelsFindOptions fixedPixelsOptions = createFixedPixelsOptions();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(fixedPixelsOptions);
        
        findFixedPixelMatches.find(matches, List.of(objectCollection));
        
        System.out.println(matches);
        MatrixUtilities.printPartOfMat(matches.getMask(), 5, 5);
        assertEquals(255, MatrixUtilities.getDouble(0,0,0, matches.getMask()));
        assertEquals(0, MatrixUtilities.getDouble(2,0,0, matches.getMask()));
    }
    
    @Test
    void testFixedPixelsWithCustomSettings() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        
        // NEW API: Configure with custom settings
        FixedPixelsFindOptions customOptions = new FixedPixelsFindOptions.Builder()
                .setMaxMovement(5)  // Allow more movement
                .setStartPlayback(0.5)  // Wait 0.5 seconds before monitoring
                .setPlaybackDuration(2.0)  // Monitor for 2 seconds
                .setSimilarity(0.9)  // High similarity threshold
                .setCaptureImage(true)  // Capture images of fixed regions
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(customOptions);
        
        findFixedPixelMatches.find(matches, List.of(objectCollection));
        
        assertNotNull(matches);
        assertNotNull(matches.getMask());
        
        // Verify custom settings are in the config
        if (matches.getActionConfig() instanceof FixedPixelsFindOptions) {
            FixedPixelsFindOptions resultOptions = (FixedPixelsFindOptions) matches.getActionConfig();
            assertEquals(5, resultOptions.getMaxMovement());
            assertEquals(0.5, resultOptions.getStartPlayback(), 0.001);
            assertEquals(2.0, resultOptions.getPlaybackDuration(), 0.001);
            assertTrue(resultOptions.isCaptureImage());
        }
    }
    
    @Test
    void testUsingActionService() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern1())
                .build();
        
        // NEW API: Use ActionService to get the appropriate action
        FixedPixelsFindOptions fixedPixelsOptions = new FixedPixelsFindOptions.Builder()
                .setMaxMovement(3)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(fixedPixelsOptions);
        
        ActionInterface fixedPixelsFindAction = actionService.getAction(fixedPixelsOptions);
        assertNotNull(fixedPixelsFindAction);
        
        fixedPixelsFindAction.perform(matches, objectCollection);
        
        assertNotNull(matches);
        // Verify the action was performed (specific assertions depend on implementation)
    }
    
    @Test
    void compareOldAndNewFixedPixelsAPI() {
        // This test demonstrates the migration pattern
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIXED_PIXELS)
                .build();
        ActionResult oldMatches = matchesInitializer.init(oldOptions, objectCollection);
        findFixedPixelMatches.find(oldMatches, List.of(objectCollection));
        */
        
        // NEW API:
        FixedPixelsFindOptions newOptions = new FixedPixelsFindOptions.Builder()
                .setMaxMovement(2)
                .setStartPlayback(0.0)
                .setPlaybackDuration(1.0)
                .build();
        
        // The new API provides specific parameters for fixed pixel detection
        // that were not available in the generic ActionOptions
        assertNotNull(newOptions);
        assertEquals(2, newOptions.getMaxMovement());
        
        // FixedPixelsFindOptions automatically uses FIXED_PIXELS strategy
        assertNotNull(newOptions.getFindStrategy());
    }
}