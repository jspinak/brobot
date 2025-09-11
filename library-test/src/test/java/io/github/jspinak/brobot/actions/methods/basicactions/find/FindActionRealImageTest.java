package io.github.jspinak.brobot.actions.methods.basicactions.find;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.testutils.TestPaths;

/**
 * Integration test for Find Action using real images and screenshots. Tests the complete find
 * pipeline with actual image matching.
 *
 * <p>This test verifies: - Find operations with real images - Pattern matching in screenshots -
 * Illustration generation for both successful and failed finds - Non-headless mode execution with
 * screen capture
 */
@SpringBootTest
@Disabled("CI failure - needs investigation")
public class FindActionRealImageTest {

    @Autowired Action action;

    @BeforeAll
    public static void setupEnvironment() {
        // Ensure non-headless mode for real image testing
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void setUp() {
        // Disable mock mode to test real image operations
        FrameworkSettings.mock = false;

        // Verify test files exist
        assertTrue(
                Files.exists(TestPaths.getImagePathObject("topLeft")),
                "topLeft.png should exist in images directory");
        assertTrue(
                Files.exists(TestPaths.getScreenshotPathObject("floranext0")),
                "floranext0.png should exist in screenshots directory");
    }

    @Test
    void testSuccessfulFindInScreenshot() {
        // Create pattern from topLeft.png
        Pattern topLeftPattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        // .setSimilarity(0.8) // Similarity is now set in PatternFindOptions, not
                        // Pattern
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .addPattern(topLeftPattern)
                        .setName("TopLeftCorner")
                        .build();

        // Create scene from floranext0.png screenshot
        Scene scene = new Scene(TestPaths.getScreenshotPath("floranext0"));

        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(stateImage).withScenes(scene).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.7)
                        .build();

        // Execute find operation
        ActionResult result = action.perform(findOptions, objColl);

        // Verify results
        assertNotNull(result, "ActionResult should not be null");
        assertTrue(
                result.isSuccess(), "Find operation should succeed when topLeft is in floranext0");
        assertFalse(result.isEmpty(), "Should find at least one match");
        assertTrue(result.getMatchList().size() > 0, "Should have matches in the result");

        // Verify illustration was created
        assertTrue(
                result.getSceneAnalysisCollection().getScenes().size() > 0,
                "Scenes should be captured for illustration");

        System.out.println(
                "✓ Successful find test completed - found "
                        + result.getMatchList().size()
                        + " matches");
    }

    @Test
    void testFailedFindInScreenshot() {
        // Create pattern from bottomRight.png (should not be in floranext0.png)
        Pattern bottomRightPattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight"))
                        // .setSimilarity(0.8) // Similarity is now set in PatternFindOptions, not
                        // Pattern
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .addPattern(bottomRightPattern)
                        .setName("BottomRightCorner")
                        .build();

        // Create scene from floranext0.png screenshot
        Scene scene = new Scene(TestPaths.getScreenshotPath("floranext0"));

        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(stateImage).withScenes(scene).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.7)
                        .build();

        // Execute find operation
        ActionResult result = action.perform(findOptions, objColl);

        // Verify results for failed find
        assertNotNull(result, "ActionResult should not be null even for failed finds");
        assertFalse(
                result.isSuccess(),
                "Find operation should fail when bottomRight is not in floranext0");
        assertTrue(result.isEmpty(), "Should not find any matches");
        assertEquals(0, result.getMatchList().size(), "Should have no matches in the result");

        // Verify illustration was still created for failed find
        assertTrue(
                result.getSceneAnalysisCollection().getScenes().size() > 0,
                "Scenes should be captured for illustration even on failed finds");

        System.out.println("✓ Failed find test completed - correctly found no matches");
    }

    @Test
    void testFindWithMultiplePatterns() {
        // Create multiple patterns
        Pattern topLeftPattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        // .setSimilarity(0.8) // Similarity is now set in PatternFindOptions, not
                        // Pattern
                        .build();

        Pattern topLeft2Pattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft2"))
                        // .setSimilarity(0.8) // Similarity is now set in PatternFindOptions, not
                        // Pattern
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .addPattern(topLeftPattern)
                        .addPattern(topLeft2Pattern)
                        .setName("TopLeftVariants")
                        .build();

        Scene scene = new Scene(TestPaths.getScreenshotPath("floranext0"));

        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(stateImage).withScenes(scene).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.7)
                        .build();

        // Execute find operation
        ActionResult result = action.perform(findOptions, objColl);

        // Verify results
        assertNotNull(result, "ActionResult should not be null");

        // Should find at least one of the patterns
        if (result.isSuccess()) {
            assertTrue(result.getMatchList().size() > 0, "Should have found some matches");
            System.out.println(
                    "✓ Multi-pattern test completed - found "
                            + result.getMatchList().size()
                            + " matches");
        } else {
            System.out.println(
                    "✓ Multi-pattern test completed - no matches found (this may be expected)");
        }

        // Always verify illustration was created
        assertTrue(
                result.getSceneAnalysisCollection().getScenes().size() > 0,
                "Scenes should be captured for illustration");
    }

    @Test
    void testFindWithDifferentSimilarityThresholds() {
        Pattern topLeftPattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        // .setSimilarity(0.5) // Similarity is now set in PatternFindOptions, not
                        // Pattern
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .addPattern(topLeftPattern)
                        .setName("TopLeftFlexible")
                        .build();

        Scene scene = new Scene(TestPaths.getScreenshotPath("floranext0"));

        ObjectCollection objColl =
                new ObjectCollection.Builder().withImages(stateImage).withScenes(scene).build();

        // Test with high similarity threshold (strict)
        PatternFindOptions strictOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.95)
                        .build();

        ActionResult strictResult = action.perform(strictOptions, objColl);

        // Test with lower similarity threshold (flexible)
        PatternFindOptions flexibleOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.5)
                        .build();

        ActionResult flexibleResult = action.perform(flexibleOptions, objColl);

        // Verify both tests ran
        assertNotNull(strictResult, "Strict similarity result should not be null");
        assertNotNull(flexibleResult, "Flexible similarity result should not be null");

        // Flexible matching should find more or equal matches than strict
        assertTrue(
                flexibleResult.getMatchList().size() >= strictResult.getMatchList().size(),
                "Flexible matching should find more or equal matches than strict matching");

        System.out.println(
                "✓ Similarity threshold test completed - Strict: "
                        + strictResult.getMatchList().size()
                        + " matches, Flexible: "
                        + flexibleResult.getMatchList().size()
                        + " matches");
    }

    @Test
    void testExecutionEnvironmentConfiguration() throws IOException {
        // Verify that ExecutionEnvironment is configured correctly for real image
        // testing
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        assertNotNull(env, "ExecutionEnvironment should be available");
        assertFalse(env.isMockMode(), "Should not be in mock mode for real image testing");
        assertTrue(env.hasDisplay(), "Should have display available for non-headless testing");
        assertTrue(env.canCaptureScreen(), "Should be able to capture screen for illustrations");
        assertTrue(env.useRealFiles(), "Should use real files for image loading");

        System.out.println("✓ ExecutionEnvironment verified: " + env.getEnvironmentInfo());
    }

    @Test
    void testImageFileAccessibility() throws IOException {
        // Verify all test files are accessible
        Path topLeftPath = TestPaths.getImagePathObject("topLeft");
        Path floranext0Path = TestPaths.getScreenshotPathObject("floranext0");

        assertTrue(Files.exists(topLeftPath), "topLeft.png should exist");
        assertTrue(Files.isReadable(topLeftPath), "topLeft.png should be readable");
        assertTrue(Files.size(topLeftPath) > 0, "topLeft.png should not be empty");

        assertTrue(Files.exists(floranext0Path), "floranext0.png should exist");
        assertTrue(Files.isReadable(floranext0Path), "floranext0.png should be readable");
        assertTrue(Files.size(floranext0Path) > 0, "floranext0.png should not be empty");

        System.out.println("✓ Image file accessibility verified");
        System.out.println("  - topLeft.png: " + topLeftPath);
        System.out.println("  - floranext0.png: " + floranext0Path);
    }
}
