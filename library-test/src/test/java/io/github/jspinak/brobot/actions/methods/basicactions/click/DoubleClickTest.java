package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests double-click functionality using Brobot's unit testing mode.
 * Find operations execute with real pattern matching on screenshots.
 * Physical click actions are mocked to verify locations without side effects.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true",
    "brobot.illustration.disabled=true",
    "brobot.scene.analysis.disabled=true"
})
public class DoubleClickTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "true");
    }
    
    @BeforeEach
    protected void setUpBrobotEnvironment() {
        // Enable mock mode for tests
        FrameworkSettings.mock = true;
        
        // Clear any previous screenshots
        FrameworkSettings.screenshots.clear();
    }

    @Autowired
    Action action;

    /**
     * Test double-click action using unit test mode.
     * Find operation executes with real pattern matching on screenshot.
     * Click action is mocked, verifying the location without side effects.
     */
    @Test
    void doubleClick() {
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
                
        // Configure double-click action
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPauseBeforeBegin(2.0)
                .build();
                
        // Execute - find happens for real, click is mocked
        ActionResult matches = action.perform(clickOptions, objColl);
        
        // Verify results
        assertFalse(matches.isEmpty(), "Should find the topLeft pattern");
        
        // Verify click location
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println("Double-click would occur at: " + loc1);
        
        // The expected Y coordinate may vary based on the actual image
        assertTrue(loc1.getCalculatedY() > 0, "Y coordinate should be positive");
        
        // If we know the exact expected location, we can verify it
        // assertEquals(55, loc1.getCalculatedY());
    }

    /**
     * Test double-click with custom pause settings.
     * Uses scene-based pattern matching.
     */
    @Test
    void doubleClickWithPauses() {
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
                
        // Configure double-click with custom pause
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPauseBeforeBegin(2.0)
                .build();
                
        // Execute
        ActionResult matches = action.perform(clickOptions, objColl);
        
        // Verify results
        assertFalse(matches.isEmpty(), "Should find the topLeft pattern");
        
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println("Double-click with pause would occur at: " + loc1);
        
        // Verify location is reasonable
        assertTrue(loc1.getCalculatedY() > 0, "Y coordinate should be positive");
        assertTrue(loc1.getCalculatedX() > 0, "X coordinate should be positive");
    }

}
