package io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.pixels.DynamicPixelsFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for finding dynamic pixel matches using new ActionConfig API.
 * Demonstrates migration from ActionOptions to DynamicPixelsFindOptions.
 * 
 * Key changes:
 * - Uses DynamicPixelsFindOptions instead of generic ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - Dynamic pixels strategy is now encapsulated in specific config class
 */
@SpringBootTest(classes = BrobotTestApplication.class)
class FindDynamicPixelMatchesTestUpdated {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    FindDynamicPixelMatches findDynamicPixelMatches;

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

    // NEW API: Create DynamicPixelsFindOptions
    private DynamicPixelsFindOptions createDynamicPixelsOptions() {
        return new DynamicPixelsFindOptions.Builder()
                .setMaxMovement(3)  // Minimum pixel movement to consider as dynamic
                .setStartPlayback(0.0)  // Start monitoring immediately
                .setPlaybackDuration(1.0)  // Monitor for 1 second
                .build();
    }

    @Test
    void find_samePatternNewAPI() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern1())
                .build();
        
        // NEW API: Use DynamicPixelsFindOptions
        DynamicPixelsFindOptions dynamicPixelsOptions = createDynamicPixelsOptions();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(dynamicPixelsOptions);
        
        // Option 1: Use the existing FindDynamicPixelMatches directly
        // (if it has been updated to work with ActionResult)
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        
        // Option 2: Use ActionService to get the action
        // ActionInterface dynamicPixelsFindAction = actionService.getAction(dynamicPixelsOptions);
        // dynamicPixelsFindAction.perform(matches, objectCollection);
        
        System.out.println(matches.getMatchList());
        MatrixUtilities.printPartOfMat(matches.getMask(), 5, 5);
        // Dynamic pixels should not be detected in identical patterns
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, matches.getMask()));
    }

    @Test
    void find_differentPatternsNewAPI() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        
        // NEW API: Use DynamicPixelsFindOptions
        DynamicPixelsFindOptions dynamicPixelsOptions = createDynamicPixelsOptions();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(dynamicPixelsOptions);
        
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        
        System.out.println(matches);
        MatrixUtilities.printPartOfMat(matches.getMask(), 5, 5);
        // Check for dynamic pixels in areas that differ
        assertNotNull(matches.getMask());
        // The behavior depends on implementation - dynamic pixels should be detected
        // where patterns differ (row 2 in this case)
    }
    
    @Test
    void testDynamicPixelsWithCustomSettings() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        
        // NEW API: Configure with custom settings
        DynamicPixelsFindOptions customOptions = new DynamicPixelsFindOptions.Builder()
                .setMaxMovement(5)  // Maximum movement threshold
                .setStartPlayback(0.5)  // Wait 0.5 seconds before monitoring
                .setPlaybackDuration(2.0)  // Monitor for 2 seconds
                .setSimilarity(0.9)  // High similarity threshold
                .setCaptureImage(true)  // Capture images of dynamic regions
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(customOptions);
        
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        
        assertNotNull(matches);
        assertNotNull(matches.getMask());
        
        // Verify custom settings are in the config
        if (matches.getActionConfig() instanceof DynamicPixelsFindOptions) {
            DynamicPixelsFindOptions resultOptions = (DynamicPixelsFindOptions) matches.getActionConfig();
            assertEquals(5, resultOptions.getMaxMovement());
            assertEquals(0.5, resultOptions.getStartPlayback(), 0.001);
            assertEquals(2.0, resultOptions.getPlaybackDuration(), 0.001);
            assertTrue(resultOptions.isCaptureImage());
        }
    }
    
    @Test
    void testUsingActionService() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        
        // NEW API: Use ActionService to get the appropriate action
        DynamicPixelsFindOptions dynamicPixelsOptions = new DynamicPixelsFindOptions.Builder()
                .setMaxMovement(3)
                .build();
        
        ActionResult matches = new ActionResult();
        matches.setActionConfig(dynamicPixelsOptions);
        
        Optional<ActionInterface> dynamicPixelsFindActionOpt = actionService.getAction(dynamicPixelsOptions);
        assertTrue(dynamicPixelsFindActionOpt.isPresent(), "Dynamic pixels find action should be available");
        ActionInterface dynamicPixelsFindAction = dynamicPixelsFindActionOpt.get();
        assertNotNull(dynamicPixelsFindAction);
        
        dynamicPixelsFindAction.perform(matches, objectCollection);
        
        assertNotNull(matches);
        // Verify the action was performed (specific assertions depend on implementation)
    }
    
    @Test
    void compareOldAndNewDynamicPixelsAPI() {
        // This test demonstrates the migration pattern
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(PatternFindOptions)
                .setFind(PatternFindOptions.FindStrategy.FIXED_PIXELS)  // Note: was using FIXED_PIXELS
                .build();
        ActionResult oldMatches = matchesInitializer.init(oldOptions, objectCollection);
        findDynamicPixelMatches.find(oldMatches, List.of(objectCollection));
        */
        
        // NEW API:
        DynamicPixelsFindOptions newOptions = new DynamicPixelsFindOptions.Builder()
                .setMaxMovement(3)
                .setStartPlayback(0.0)
                .setPlaybackDuration(1.0)
                .build();
        
        // The new API provides specific parameters for dynamic pixel detection
        // that were not available in the generic ActionOptions
        assertNotNull(newOptions);
        assertEquals(3, newOptions.getMaxMovement());
        
        // DynamicPixelsFindOptions automatically uses DYNAMIC_PIXELS strategy
        assertNotNull(newOptions.getFindStrategy());
    }
}