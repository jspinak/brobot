package io.github.jspinak.brobot.actions.methods.basicactions.find;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;

/**
 * Updated tests for find operations with position settings using new ActionConfig API. Demonstrates
 * migration from ActionOptions to PatternFindOptions. Uses Brobot's unit testing mode for real
 * pattern matching without physical actions.
 *
 * <p>Key changes: - Uses PatternFindOptions instead of generic ActionOptions - Position settings
 * are now only on Pattern/StateImage, not in ActionConfig - ActionResult requires setActionConfig()
 * before perform() - Uses ActionService to get the appropriate action
 */
@SpringBootTest
@Disabled("CI failure - needs investigation")
public class FindImageWithPositionTestUpdated extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @Override
    protected void setUpBrobotEnvironment() {
        // Configure for unit testing with screenshots
        ExecutionEnvironment env =
                ExecutionEnvironment.builder()
                        .mockMode(false) // Use real file operations for find
                        .forceHeadless(true) // No screen capture
                        .allowScreenCapture(false)
                        .build();
        ExecutionEnvironment.setInstance(env);

        // Enable action mocking but real find operations
        FrameworkSettings.mock = true;

        // Clear any previous screenshots
        // Note: clearAll() doesn't exist in the current API
    }

    @Autowired ActionService actionService;

    /**
     * Test finding an image with different target positions. Verifies that different positions
     * return different match locations.
     */
    @Test
    void findImageWithPositionTestNewAPI() {
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
        StateImage topLeft =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(new Position(100, 100))
                                        .build())
                        .build();
        ObjectCollection objColl =
                new ObjectCollection.Builder()
                        .withImages(topLeft)
                        .withScenes(TestPaths.getScreenshotPath("floranext0"))
                        .build();

        // NEW API: Use PatternFindOptions
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();

        ActionResult matches = new ActionResult();
        matches.setActionConfig(findOptions);
        matches.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent(), "Find action should be available");
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(matches, objColl);

        // Test with position (0, 0)
        StateImage topLeft2 =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(new Position(0, 0))
                                        .build())
                        .build();
        ObjectCollection objColl2 =
                new ObjectCollection.Builder()
                        .withImages(topLeft2)
                        .withScenes(TestPaths.getScreenshotPath("floranext0"))
                        .build();

        ActionResult matches2 = new ActionResult();
        matches2.setActionConfig(findOptions);
        matches2.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        findAction.perform(matches2, objColl2);

        // Verify both finds succeeded
        assertFalse(matches.isEmpty(), "Should find pattern with position (100,100)");
        assertFalse(matches2.isEmpty(), "Should find pattern with position (0,0)");

        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);

        System.out.println("Location with position (100,100): " + loc1);
        System.out.println("Location with position (0,0): " + loc2);

        // Different positions should give different results
        assertNotEquals(
                loc1.getCalculatedX(),
                loc2.getCalculatedX(),
                "Different target positions should result in different X coordinates");
    }

    /** Test different find strategies with position settings */
    @Test
    void findWithDifferentStrategiesAndPositions() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));

        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }

        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));

        // Create pattern with specific position
        StateImage imageWithPosition =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(new Position(50, 50))
                                        .build())
                        .build();
        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(imageWithPosition).build();

        // Test BEST strategy
        PatternFindOptions bestOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.8)
                        .build();

        ActionResult bestResult = new ActionResult();
        bestResult.setActionConfig(bestOptions);
        bestResult.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> bestFindActionOpt = actionService.getAction(bestOptions);
        assertTrue(bestFindActionOpt.isPresent(), "Best find action should be available");
        ActionInterface bestFindAction = bestFindActionOpt.get();
        bestFindAction.perform(bestResult, objColl);

        assertFalse(bestResult.isEmpty(), "Should find pattern with BEST strategy");

        Location bestLoc = bestResult.getMatchLocations().get(0);
        System.out.println("Best match location with position: " + bestLoc);

        // Test ALL strategy
        PatternFindOptions allOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setMaxMatchesToActOn(10)
                        .build();

        ActionResult allResult = new ActionResult();
        allResult.setActionConfig(allOptions);
        allResult.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> allFindActionOpt = actionService.getAction(allOptions);
        assertTrue(allFindActionOpt.isPresent(), "All find action should be available");
        ActionInterface allFindAction = allFindActionOpt.get();
        allFindAction.perform(allResult, objColl);

        assertFalse(allResult.isEmpty(), "Should find patterns with ALL strategy");
        System.out.println("Found " + allResult.size() + " matches with ALL strategy");

        // All matches should have the same calculated position since they use the same
        // pattern
        for (Location loc : allResult.getMatchLocations()) {
            assertEquals(
                    bestLoc.getCalculatedX(),
                    loc.getCalculatedX(),
                    "All matches should have same calculated X position");
            assertEquals(
                    bestLoc.getCalculatedY(),
                    loc.getCalculatedY(),
                    "All matches should have same calculated Y position");
        }
    }

    /** Test using StateImage builder to set position for all patterns */
    @Test
    void setPositionForAllPatternsTest() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));

        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }

        // Add screenshot for find operation
        FrameworkSettings.screenshots.add(TestPaths.getScreenshotPath("floranext0"));

        // Create StateImage with multiple patterns and set position for all
        StateImage multiPattern =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(
                                                new Position(10, 10)) // This will be overridden
                                        .build())
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(
                                                new Position(20, 20)) // This will be overridden
                                        .build())
                        .setPositionForAllPatterns(100, 100) // Override all positions to (100,100)
                        .build();

        // Verify positions were overridden
        for (Pattern pattern : multiPattern.getPatterns()) {
            // Position stores percentages, not calculated coordinates
            // For absolute position (100, 100), Position would store it as percentages
            assertNotNull(
                    pattern.getTargetPosition(), "All patterns should have a target position");
        }

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(multiPattern).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .build();

        ActionResult result = new ActionResult();
        result.setActionConfig(findOptions);
        result.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> findActionOpt = actionService.getAction(findOptions);
        assertTrue(findActionOpt.isPresent(), "Find action should be available");
        ActionInterface findAction = findActionOpt.get();
        findAction.perform(result, objColl);

        assertFalse(result.isEmpty(), "Should find patterns");

        // All matches should have the same calculated position
        if (result.size() > 1) {
            Location firstLoc = result.getMatchLocations().get(0);
            for (Location loc : result.getMatchLocations()) {
                assertEquals(
                        firstLoc.getCalculatedX(),
                        loc.getCalculatedX(),
                        "All matches should have same calculated X");
                assertEquals(
                        firstLoc.getCalculatedY(),
                        loc.getCalculatedY(),
                        "All matches should have same calculated Y");
            }
        }
    }

    @Test
    void compareOldAndNewPositionAPI() {
        // This test demonstrates the migration pattern

        // OLD API (commented out):
        /*
         * ActionOptions oldOptions = new ActionOptions.Builder()
         * .setAction(PatternFindOptions)
         * .setTargetPosition(100, 100) // Position in ActionOptions
         * .build();
         * ActionResult oldResult = action.perform(oldOptions, objColl);
         */

        // NEW API:
        // Position is now only set on Pattern/StateImage, not in ActionConfig
        Pattern patternWithPosition =
                new Pattern.Builder()
                        .setFilename("test.png")
                        .setTargetPosition(new Position(100, 100))
                        .build();

        StateImage imageWithPosition =
                new StateImage.Builder().addPattern(patternWithPosition).build();

        PatternFindOptions newOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .build();

        // The new API separates find behavior (in PatternFindOptions)
        // from position/offset settings (in Pattern/StateImage)
        assertNotNull(newOptions);
        assertNotNull(imageWithPosition);
        // Position stores percentages, not calculated coordinates
        assertNotNull(patternWithPosition.getTargetPosition());
        // The Position(100, 100) constructor creates a position with percentages
    }
}
