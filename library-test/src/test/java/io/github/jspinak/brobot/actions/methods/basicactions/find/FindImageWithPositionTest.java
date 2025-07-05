package io.github.jspinak.brobot.actions.methods.basicactions.find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests find operations with position settings.
 * Uses Brobot's unit testing mode for real pattern matching without physical actions.
 */
@SpringBootTest
public class FindImageWithPositionTest extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
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
    Action action;

    /**
     * Test finding an image with different target positions.
     * Verifies that different positions return different match locations.
     */
    @Test
    void findImageWithPositionTest() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation (enables hybrid mode)
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Test with position (100, 100)
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(100, 100))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ActionResult matches = action.perform(findOptions, objColl);
        
        // Test with position (0, 0)
        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(0, 0))
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        PatternFindOptions findOptions2 = new PatternFindOptions.Builder().build();
        ActionResult matches2 = action.perform(findOptions2, objColl2);
        
        // Verify both finds succeeded
        assertFalse(matches.isEmpty(), "Should find pattern with position (100,100)");
        assertFalse(matches2.isEmpty(), "Should find pattern with position (0,0)");
        
        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);
        
        System.out.println("Location with position (100,100): " + loc1);
        System.out.println("Location with position (0,0): " + loc2);
        
        // Different positions should give different results
        assertNotEquals(loc1.getCalculatedX(), loc2.getCalculatedX(), 
                       "Different target positions should result in different X coordinates");
    }

    /**
     * Test that different Positions in Patterns give different results.
     * In the new API, positions are only set on Patterns, not in options.
     */
    @Test
    void findWithDifferentPositionsInPatterns() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Test 1: Pattern with position (100, 100)
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(100, 100))
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ActionResult matches = action.perform(findOptions, objColl);
        
        // Test 2: Pattern with position (50,50) to compare with (100,100)
        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(50, 50))  // Different position
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        PatternFindOptions findOptions3 = new PatternFindOptions.Builder()
                .build();
        ActionResult matches2 = action.perform(findOptions3, objColl2);
        
        // Verify both finds succeeded
        assertFalse(matches.isEmpty(), "Should find pattern with position (100,100)");
        assertFalse(matches2.isEmpty(), "Should find pattern with position (50,50)");
        
        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);
        
        System.out.println("Location from pattern position (100,100): " + loc1);
        System.out.println("Location from pattern position (50,50): " + loc2);
        
        // Different positions should give different results
        assertNotEquals(loc1.getCalculatedX(), loc2.getCalculatedX(), 
                    "Different target positions should result in different X coordinates");
        assertNotEquals(loc1.getCalculatedY(), loc2.getCalculatedY(), 
                    "Different target positions should result in different Y coordinates");
    }
}
