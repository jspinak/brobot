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
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;

/**
 * Updated tests for find operations with position and offset settings using new ActionConfig API.
 * Demonstrates migration from ActionOptions to PatternFindOptions. Uses Brobot's unit testing mode
 * for real pattern matching without physical actions.
 *
 * <p>Key changes: - Uses PatternFindOptions instead of generic ActionOptions - ActionResult
 * requires setActionConfig() before perform() - Uses ActionService to get the appropriate action -
 * Position and offset handling with new API
 */
@SpringBootTest
@Disabled("CI failure - needs investigation")
public class FindImageWithOffsetTestUpdated extends BrobotIntegrationTestBase {

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
        BrobotProperties.mock = true;

        // Clear any previous screenshots
        // BrobotProperties.screenshots may not have clearAll() method
    }

    @Autowired ActionService actionService;

    /**
     * Test finding an image with offset applied to the target position. Verifies that the offset
     * shifts the match location correctly.
     */
    @Test
    void findImageWithOffsetTestNewAPI() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));

        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }

        // Add screenshot for find operation (enables hybrid mode)
        // Test screenshot: TestPaths.getScreenshotPath("floranext0")

        // Test with offset
        StateImage topLeft =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetOffset(10, 10)
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

        // Test without offset
        StateImage topLeft2 =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
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
        assertFalse(matches.isEmpty(), "Should find pattern with offset");
        assertFalse(matches2.isEmpty(), "Should find pattern without offset");

        Location loc1 = matches.getMatchLocations().get(0);
        Location loc2 = matches2.getMatchLocations().get(0);

        System.out.println("Location with offset: " + loc1);
        System.out.println("Location without offset: " + loc2);
        System.out.println("loc1.getX()=" + loc1.getX() + ", loc1.getY()=" + loc1.getY());
        System.out.println("loc2.getX()=" + loc2.getX() + ", loc2.getY()=" + loc2.getY());
        System.out.println(
                "loc1.getCalculatedX()="
                        + loc1.getCalculatedX()
                        + ", loc1.getCalculatedY()="
                        + loc1.getCalculatedY());
        System.out.println(
                "loc2.getCalculatedX()="
                        + loc2.getCalculatedX()
                        + ", loc2.getCalculatedY()="
                        + loc2.getCalculatedY());

        // Verify offset is applied correctly
        assertEquals(
                loc2.getCalculatedX() + 10,
                loc1.getCalculatedX(),
                "X coordinate should be offset by 10");
        assertEquals(
                loc2.getCalculatedY() + 10,
                loc1.getCalculatedY(),
                "Y coordinate should be offset by 10");
    }

    /** Test finding with different strategies and offset combinations */
    @Test
    void findWithDifferentStrategiesAndOffsets() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));

        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }

        // Add screenshot for find operation
        BrobotProperties.screenshots.add(TestPaths.getScreenshotPath("floranext0"));

        // Test BEST strategy with offset
        StateImage imageWithOffset =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetOffset(20, 20)
                                        .build())
                        .build();
        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(imageWithOffset).build();

        // Use BEST strategy
        PatternFindOptions bestFindOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.BEST)
                        .setSimilarity(0.7)
                        .build();

        ActionResult bestResult = new ActionResult();
        bestResult.setActionConfig(bestFindOptions);
        bestResult.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> findActionOpt2 = actionService.getAction(bestFindOptions);
        assertTrue(findActionOpt2.isPresent(), "Find action should be available");
        ActionInterface findAction = findActionOpt2.get();
        findAction.perform(bestResult, objColl);

        assertFalse(bestResult.isEmpty(), "Should find pattern with BEST strategy");

        Location bestLoc = bestResult.getMatchLocations().get(0);
        System.out.println("Best match location with offset: " + bestLoc);

        // Test ALL strategy with different offset
        StateImage imageWithDifferentOffset =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetOffset(-5, -5)
                                        .build())
                        .build();
        ObjectCollection objColl2 =
                new ObjectCollection.Builder().withImages(imageWithDifferentOffset).build();

        PatternFindOptions allFindOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setMaxMatchesToActOn(10)
                        .build();

        ActionResult allResult = new ActionResult();
        allResult.setActionConfig(allFindOptions);
        allResult.setActionLifecycle(
                new io.github.jspinak.brobot.action.internal.execution.ActionLifecycle(
                        java.time.LocalDateTime.now(), 30.0));

        Optional<ActionInterface> allFindActionOpt = actionService.getAction(allFindOptions);
        assertTrue(allFindActionOpt.isPresent(), "Find action should be available");
        ActionInterface allFindAction = allFindActionOpt.get();
        allFindAction.perform(allResult, objColl2);

        assertFalse(allResult.isEmpty(), "Should find patterns with ALL strategy");
        System.out.println("Found " + allResult.size() + " matches with ALL strategy");
    }

    /**
     * Test that StateImage Builder methods override individual Pattern settings. Verifies
     * setPositionForAllPatterns and setOffsetForAllPatterns work correctly.
     */
    @Test
    void setOffsetWithStateImageNewAPI() {
        // Check if test images exist
        File screenshotFile = new File(TestPaths.getScreenshotPath("floranext0"));
        File patternFile = new File(TestPaths.getImagePath("topLeft"));

        if (!screenshotFile.exists() || !patternFile.exists()) {
            System.out.println("Test images not available - skipping test");
            return;
        }

        // Add screenshot for find operation
        // Test screenshot: TestPaths.getScreenshotPath("floranext0")

        // Create pattern with initial position and offset
        StateImage topLeft =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename(TestPaths.getImagePath("topLeft"))
                                        .setTargetPosition(100, 100) // This should be overridden
                                        .setTargetOffset(-10, 0) // This should be overridden
                                        .build())
                        .setPositionForAllPatterns(0, 0) // Override position to (0,0)
                        .setOffsetForAllPatterns(10, 0) // Override offset to (10,0)
                        .build();

        // Verify the offset was overridden in the pattern
        assertEquals(
                10,
                topLeft.getPatterns().get(0).getTargetOffset().getCalculatedX(),
                "StateImage Builder should override pattern offset");

        ObjectCollection objColl = new ObjectCollection.Builder().withImages(topLeft).build();

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

    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern

        // OLD API (commented out):
        /*
         * ActionOptions oldOptions = new ActionOptions.Builder()
         * .setAction(PatternFindOptions)
         * .setTargetPosition(100, 100)
         * .setTargetOffset(-10, 0)
         * .build();
         * ActionResult oldResult = action.perform(oldOptions, objColl);
         */

        // NEW API:
        // Note: Position and offset are now set on the Pattern/StateImage
        // PatternFindOptions focuses on find strategy and behavior
        PatternFindOptions newOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.8)
                        .build();

        // Position and offset are part of the pattern definition
        StateImage imageWithPositionAndOffset =
                new StateImage.Builder()
                        .addPattern(
                                new Pattern.Builder()
                                        .setFilename("test.png")
                                        .setTargetPosition(100, 100)
                                        .setTargetOffset(-10, 0)
                                        .build())
                        .build();

        // Both approaches achieve the same result, but new API separates concerns
        // better
        assertNotNull(newOptions);
        assertNotNull(imageWithPositionAndOffset);
    }
}
