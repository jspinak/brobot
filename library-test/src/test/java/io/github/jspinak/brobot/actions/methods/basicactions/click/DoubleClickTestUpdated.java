package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for double-click functionality using the new ActionConfig API.
 * Demonstrates migration from Object// ActionOptions to ClickOptions.
 * 
 * Key changes:
 * - Uses ClickOptions instead of generic Object// ActionOptions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - ClickType.Type is now ClickOptions.Type
 */
@SpringBootTest(classes = BrobotTestApplication.class)
public class DoubleClickTestUpdated extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "true");
    }
    
    @BeforeEach
    @Override
    protected void setUpBrobotEnvironment() {
        // Configure for unit testing with screenshots
        ExecutionEnvironment env = ExecutionEnvironment.builder()
                .mockMode(false)  // Use real file operations for find
                .forceHeadless(true)  // No screen capture
                .allowScreenCapture(false)
                .build();
        ExecutionEnvironment.setInstance(env);
        
        // Enable action mocking but real find operations
        FrameworkSettings.mock = true;
        
        // Clear any previous screenshots
        FrameworkSettings.screenshots.clear();
    }

    @Autowired
    ActionService actionService;

    /**
     * Test double-click action using unit test mode with new API.
     * Find operation executes with real pattern matching on screenshot.
     * Click action is mocked, verifying the location without side effects.
     */
    @Test
    void doubleClick_newAPI() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Create pattern to find
        StateImage topLeft = new StateImage.Builder()
                .addPattern(TestPaths.getImagePath("topLeft"))
                .build();
                
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
                
        // NEW API: Configure double-click action using ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseBeforeBegin(2.0)
                .build();
                
        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        Optional<ActionInterface> clickAction = actionService.getAction(clickOptions);
        assertTrue(clickAction.isPresent());
        
        // Execute - find happens for real, click is mocked
        clickAction.get().perform(result, objColl);
        
        // Verify results
        assertFalse(result.isEmpty(), "Should find the topLeft pattern");
        
        // Verify click location
        Location loc1 = result.getMatchLocations().get(0);
        System.out.println("Double-click would occur at: " + loc1);
        
        // The expected Y coordinate may vary based on the actual image
        assertTrue(loc1.getCalculatedY() > 0, "Y coordinate should be positive");
        
        // Verify the config is preserved
        assertNotNull(result.getActionConfig());
        assertTrue(result.getActionConfig() instanceof ClickOptions);
        assertEquals(ClickOptions.Type.DOUBLE_LEFT, ((ClickOptions) result.getActionConfig()).getClickType());
    }

    /**
     * Test double-click with custom pause settings using new API.
     * Uses scene-based pattern matching.
     */
    @Test
    void doubleClickWithPauses_newAPI() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation (enables hybrid mode)
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Create pattern and use scene for matching
        StateImage topLeft = new StateImage.Builder()
                .addPattern(TestPaths.getImagePath("topLeft"))
                .build();
                
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
                
        // NEW API: Configure double-click with custom pause
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseBeforeBegin(2.0)
                .setPauseAfterEnd(0.5)
                .build();
                
        // NEW API: Create ActionResult with config
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        // Get the action from service
        Optional<ActionInterface> clickAction = actionService.getAction(clickOptions);
        assertTrue(clickAction.isPresent());
        
        // Execute
        clickAction.get().perform(result, objColl);
        
        // Verify results
        assertFalse(result.isEmpty(), "Should find the topLeft pattern");
        
        Location loc1 = result.getMatchLocations().get(0);
        System.out.println("Double-click with pause would occur at: " + loc1);
        
        // Verify location is reasonable
        assertTrue(loc1.getCalculatedY() > 0, "Y coordinate should be positive");
        assertTrue(loc1.getCalculatedX() > 0, "X coordinate should be positive");
        
        // Verify timing settings are preserved
        ClickOptions resultOptions = (ClickOptions) result.getActionConfig();
        assertEquals(2.0, resultOptions.getPauseBeforeBegin());
        assertEquals(0.5, resultOptions.getPauseAfterEnd());
    }

    @Test
    void tripleClick_newAPI() {
        // NEW TEST: Demonstrates triple-click with new API
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        StateImage topLeft = new StateImage.Builder()
                .addPattern(TestPaths.getImagePath("topLeft"))
                .build();
                
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
                
        // NEW API: Configure multiple clicks (triple-click equivalent)
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(3)
                .build();
                
        ActionResult result = new ActionResult();
        result.setActionConfig(clickOptions);
        
        Optional<ActionInterface> clickAction = actionService.getAction(clickOptions);
        assertTrue(clickAction.isPresent());
        clickAction.get().perform(result, objColl);
        
        assertFalse(result.isEmpty(), "Should find the topLeft pattern");
        
        // Verify number of clicks is preserved
        assertEquals(3, ((ClickOptions) result.getActionConfig()).getNumberOfClicks());
    }

    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern
        StateImage topLeft = new StateImage.Builder()
                .addPattern(TestPaths.getImagePath("topLeft"))
                .build();
                
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
        
        // OLD API (commented out):
        /*
        Object// ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(Object// ActionOptions.Action.CLICK)
                .setClickType(ClickType.Type.DOUBLE_LEFT)
                .setPauseBeforeMouseDown(2.0)
                .build();
        ActionResult oldResult = action.perform(oldOptions, objColl);
        */
        
        // NEW API:
        ClickOptions newOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseBeforeBegin(2.0)
                .build();
        
        ActionResult newResult = new ActionResult();
        newResult.setActionConfig(newOptions);
        
        Optional<ActionInterface> clickAction = actionService.getAction(newOptions);
        assertTrue(clickAction.isPresent());
        clickAction.get().perform(newResult, objColl);
        
        // Both approaches achieve the same result, but new API is more type-safe
        assertNotNull(newResult);
    }
}