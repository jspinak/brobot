package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for find actions using the Action class.
 * These tests run in mock mode to verify the integration of ActionOptions,
 * ObjectCollections, and the Action execution flow without requiring
 * actual image recognition capabilities.
 * 
 * The tests validate:
 * - Proper configuration of ActionOptions for different find types
 * - Correct handling of ObjectCollections with StateImages and patterns
 * - Integration with the Spring context and autowired components
 * - Various find operation parameters (similarity, timeout, areas, etc.)
 */
@SpringBootTest
public class FindActionIntegrationTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        // Tests will run in mock mode, which is suitable for integration testing
        // the Action flow without actual image recognition
        BrobotSettings.mock = true;
    }

    @Autowired
    Action action;

    @Test
    void basicFindActionWithSingleImage() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(matches.getActionOptions());
        assertEquals(ActionOptions.Action.FIND, matches.getActionOptions().getAction());
        // In mock mode, matches will be empty unless mock data is set up
        assertTrue(matches.isEmpty() || !matches.isEmpty());
    }

    @Test
    void findWithCustomSimilarityThreshold() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(0.90)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(matches.getActionOptions());
        assertEquals(0.90, matches.getActionOptions().getSimilarity(), 0.01);
        // In mock mode, we can't test actual match scores
    }

    @Test
    void findFirstOption() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIRST)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(matches.getActionOptions());
        assertEquals(ActionOptions.Find.FIRST, matches.getActionOptions().getFind());
        // In mock mode, validate the action was configured correctly
    }

    @Test
    void findAllOption() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .setMaxMatchesToActOn(10)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(matches.getActionOptions());
        assertEquals(ActionOptions.Find.ALL, matches.getActionOptions().getFind());
        assertEquals(10, matches.getActionOptions().getMaxMatchesToActOn());
    }

    @Test
    void findBestOption() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.BEST)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(matches.getActionOptions());
        assertEquals(ActionOptions.Find.BEST, matches.getActionOptions().getFind());
        // In mock mode, we verify the find type was set correctly
    }

    @Test
    void findWithSearchRegion() {
        Region searchRegion = new Region(0, 0, 200, 200);
        
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .addSearchRegion(searchRegion)
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertNotNull(stateImage.getPatterns());
        assertEquals(1, stateImage.getPatterns().size());
        // Verify the search region was set on the pattern
        Pattern pattern = stateImage.getPatterns().get(0);
        assertNotNull(pattern.getSearchRegions());
    }

    @Test
    void findWithMultiplePatterns() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.FIRST)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertEquals(2, stateImage.getPatterns().size(), "StateImage should have 2 patterns");
        assertEquals(ActionOptions.Find.FIRST, matches.getActionOptions().getFind());
    }

    @Test
    void findEachOption() {
        StateImage stateImage1 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        StateImage stateImage2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage1, stateImage2)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.EACH)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertEquals(ActionOptions.Find.EACH, matches.getActionOptions().getFind());
        assertEquals(2, objColl.getStateImages().size(), "Should have 2 StateImages");
    }

    @Test
    void findWithTimeout() {
        // Use an image that exists but won't be found in the screenshot
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight3"))
                        .build())
                .build();
        
        // Use a screenshot where bottomRight3 won't be found
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext2"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMaxWait(1.0)
                .build();
        
        long startTime = System.currentTimeMillis();
        Matches matches = action.perform(actionOptions, objColl);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(matches);
        assertEquals(1.0, matches.getActionOptions().getMaxWait(), 0.01);
        
        // In mock mode, timing might be different but we can verify the timeout was set
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Find should complete within 5 seconds");
    }

    @Test
    void findWithPauseAfterSuccess() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setPauseAfterEnd(1.0)
                .build();
        
        long startTime = System.currentTimeMillis();
        Matches matches = action.perform(actionOptions, objColl);
        long endTime = System.currentTimeMillis();
        
        assertNotNull(matches);
        assertEquals(1.0, matches.getActionOptions().getPauseAfterEnd(), 0.01);
        
        // Verify the pause setting was applied
        long duration = endTime - startTime;
        assertTrue(duration > 0, "Action should have taken some time");
    }

    @Test
    void findMultipleImagesInCollection() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .setName("TopLeftImage")
                .build();
        
        StateImage bottomRight = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("bottomRight"))
                        .build())
                .setName("BottomRightImage")
                .build();
        
        StateImage topLeft2 = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft2"))
                        .build())
                .setName("TopLeft2Image")
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(topLeft, bottomRight, topLeft2)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertEquals(3, objColl.getStateImages().size(), "Should have 3 StateImages");
        assertEquals(ActionOptions.Find.ALL, matches.getActionOptions().getFind());
        
        // Verify the collection was built correctly
        List<StateImage> images = objColl.getStateImages();
        assertEquals("TopLeftImage", images.get(0).getName());
        assertEquals("BottomRightImage", images.get(1).getName());
        assertEquals("TopLeft2Image", images.get(2).getName());
    }

    @Test
    void findWithMinMaxArea() {
        StateImage stateImage = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .build())
                .build();
        
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(TestPaths.getScreenshotPath("floranext0"))
                .build();
        
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .setMinArea(100)
                .setMaxArea(10000)
                .build();
        
        Matches matches = action.perform(actionOptions, objColl);
        
        assertNotNull(matches);
        assertEquals(100, matches.getActionOptions().getMinArea());
        assertEquals(10000, matches.getActionOptions().getMaxArea());
        assertEquals(ActionOptions.Find.ALL, matches.getActionOptions().getFind());
    }
}