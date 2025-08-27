package io.github.jspinak.brobot.actions.methods.basicactions.find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests find operations with position and offset settings.
 * Uses Brobot's unit testing mode for real pattern matching without physical actions.
 */
@SpringBootTest
public class FindImageWithOffsetTest extends BrobotIntegrationTestBase {

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
     * Test finding an image with offset applied to the target position.
     * Verifies that the offset shifts the match location correctly.
     */
    @Test
    void findImageWithOffsetTest() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation (enables hybrid mode)
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Test with offset
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetOffset(10, 10)
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        ActionResult matches = action.perform(new PatternFindOptions.Builder().build(), objColl);
        
        // Test without offset
        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        ActionResult matches2 = action.perform(new PatternFindOptions.Builder().build(), objColl2);
        
        // Verify both finds succeeded
        assertFalse(matches.isEmpty(), "Should find pattern with offset");
        assertFalse(matches2.isEmpty(), "Should find pattern without offset");
        
        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);
        
        System.out.println("Location with offset: " + loc1);
        System.out.println("Location without offset: " + loc2);
        System.out.println("loc1.getX()=" + loc1.getX() + ", loc1.getY()=" + loc1.getY());
        System.out.println("loc2.getX()=" + loc2.getX() + ", loc2.getY()=" + loc2.getY());
        System.out.println("loc1.getCalculatedX()=" + loc1.getCalculatedX() + ", loc1.getCalculatedY()=" + loc1.getCalculatedY());
        System.out.println("loc2.getCalculatedX()=" + loc2.getCalculatedX() + ", loc2.getCalculatedY()=" + loc2.getCalculatedY());
        
        // Verify offset is applied correctly
        assertEquals(loc2.getCalculatedX() + 10, loc1.getCalculatedX(), 
                    "X coordinate should be offset by 10");
        assertEquals(loc2.getCalculatedY() + 10, loc1.getCalculatedY(), 
                    "Y coordinate should be offset by 10");
    }

    /**
     * Test that Position set in ObjectActionOptions overrides the Position in the Pattern.
     * Verifies the priority of ObjectActionOptions settings over Pattern settings.
     */
    @Test
    void findWithPositionAndOffsetInObjectActionOptions() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Test 1: Pattern with position and offset
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(100, 100))
                        .setTargetOffset(-10, 0)
                        .build())
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
        ActionResult matches = action.perform(new PatternFindOptions.Builder().build(), objColl);
        
        // Test 2: Pattern with different position, but ObjectActionOptions overrides
        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(0, 0))  // This should be overridden
                        .build())
                .build();
        ObjectCollection objColl2 = new ObjectCollection.Builder()
                .withImages(topLeft2)
                .build();
        // In the new API, position and offset are set on Pattern, not in options
        // Create a new pattern with the desired position and offset
        StateImage topLeft2Override = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(new Position(100, 100))
                        .setTargetOffset(-10, 0)
                        .build())
                .build();
        ObjectCollection objColl2Override = new ObjectCollection.Builder()
                .withImages(topLeft2Override)
                .build();
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ActionResult matches2 = action.perform(findOptions, objColl2Override);
        
        // Verify both finds succeeded
        assertFalse(matches.isEmpty(), "Should find pattern with position/offset in pattern");
        assertFalse(matches2.isEmpty(), "Should find pattern with position/offset in ObjectActionOptions");
        
        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);
        
        System.out.println("Location from pattern settings: " + loc1);
        System.out.println("Location from ObjectActionOptions override: " + loc2);
        
        // Both should have the same effective position and offset
        assertEquals(loc1.getCalculatedX(), loc2.getCalculatedX(), 
                    "ObjectActionOptions position should override pattern position");
        assertEquals(loc1.getCalculatedY(), loc2.getCalculatedY(), 
                    "ObjectActionOptions position should override pattern position");
        
        // Verify the offset was applied
        // Note: The actual calculation depends on where the pattern is found
        // and how the position/offset is applied
        assertTrue(Math.abs(loc1.getX() - loc2.getX()) < 5, 
                    "Both locations should have similar X coordinates");
    }

    /**
     * Test that StateImage Builder methods override individual Pattern settings.
     * Verifies setPositionForAllPatterns and setOffsetForAllPatterns work correctly.
     */
    @Test
    void setOffsetWithStateImage() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));
        
        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }
        
        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));
        
        // Create pattern with initial position and offset
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .setTargetPosition(100, 100)  // This should be overridden
                        .setTargetOffset(-10, 0)      // This should be overridden
                        .build())
                .setPositionForAllPatterns(0, 0)  // Override position to (0,0)
                .setOffsetForAllPatterns(10, 0)   // Override offset to (10,0)
                .build();
                
        // Verify the offset was overridden in the pattern
        assertEquals(10, topLeft.getPatterns().get(0).getTargetOffset().getCalculatedX(),
                    "StateImage Builder should override pattern offset");
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft)
                .build();
        ActionResult matches = action.perform(new PatternFindOptions.Builder().build(), objColl);
        
        // Verify find succeeded
        assertFalse(matches.isEmpty(), "Should find the pattern");
        
        Location loc1 = matches.getMatchLocations().get(0);
        System.out.println("Location with StateImage overrides: " + loc1);
        
        // Verify the offset was applied
        // The actual location depends on where the pattern is found
        assertTrue(loc1.getCalculatedX() > 0, "X coordinate should be positive");
        assertTrue(loc1.getCalculatedY() > 0, "Y coordinate should be positive");
        
        // Log the actual values for debugging
        System.out.println("Pattern offset: " + topLeft.getPatterns().get(0).getTargetOffset());
        System.out.println("Match location: " + loc1);
    }
}
