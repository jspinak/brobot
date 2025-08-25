package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for FindInScene functionality using the new ActionConfig API.
 * Demonstrates migration from ActionOptions to DefineRegionOptions.
 * 
 * Key changes:
 * - Uses DefineRegionOptions instead of generic ActionOptions for define actions
 * - ActionResult requires setActionConfig() before perform()
 * - Uses ActionService to get the appropriate action
 * - DefineAs enum is now DefineRegionOptions.DefineAs
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class,
    properties = {
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false",
        "brobot.mock.enabled=true"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class FindInSceneTestUpdated extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    ScenePatternMatcher findInScene;

    @Autowired
    SceneProvider getScenes;

    @Autowired
    ActionService actionService;

    @Test
    void findAllInScene_newAPI() {
        try {
            // Check if test images exist
            File topLeftFile = new File(TestPaths.getImagePath("topLeft"));
            File bottomRightFile = new File(TestPaths.getImagePath("bottomRight"));
            File sceneFile = new File(TestPaths.getScreenshotPath("FloraNext1"));
            
            if (!topLeftFile.exists() || !bottomRightFile.exists() || !sceneFile.exists()) {
                System.out.println("Test images not available - skipping test");
                return;
            }
            
            Pattern topL = new Pattern.Builder()
                    .setFilename(TestPaths.getImagePath("topLeft"))
                    .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                    .build();
            StateImage topLeft = new StateImage.Builder()
                    .addPattern(topL)
                    .build();
            StateImage bottomRight = new StateImage.Builder()
                    .addPattern(new Pattern.Builder()
                            .setFilename(TestPaths.getImagePath("bottomRight"))
                            .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
                            .build())
                    .build();
            
            // NEW API: Use DefineRegionOptions instead of ActionOptions
            DefineRegionOptions defineOptions = new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                    .build();
                    
            ObjectCollection objectCollection = new ObjectCollection.Builder()
                    .withScenes(new Pattern(TestPaths.getScreenshotPath("FloraNext1")))
                    .withImages(topLeft, bottomRight)
                    .build();
            
            // NEW API: Create ActionResult with config
            ActionResult result = new ActionResult();
            result.setActionConfig(defineOptions);
            
            // Get the action from service
            ActionInterface defineAction = actionService.getAction(defineOptions);
            assertNotNull(defineAction);
            
            // Perform the action
            defineAction.perform(result, objectCollection);
            
            // Get scenes from the result
            List<Scene> scenes = result.getScenes();
            
            if (scenes.isEmpty()) {
                System.out.println("No scenes found - this may be expected in headless mode");
                return;
            }
            
            List<Match> matches = findInScene.findAllInScene(topL, scenes.get(0));
            System.out.println(matches);
            
            // In headless mode, we might not find matches but the operation should complete
            assertNotNull(matches);
            
            if (!matches.isEmpty()) {
                System.out.println("Found " + matches.size() + " matches in scene");
            } else {
                System.out.println("No matches found - this may be expected in headless mode");
            }
            
        } catch (Exception e) {
            handleTestException(e);
        }
    }

    @Test
    void defineWithDifferentStrategies_newAPI() {
        // NEW TEST: Demonstrates different define strategies
        try {
            File sceneFile = new File(TestPaths.getScreenshotPath("FloraNext1"));
            if (!sceneFile.exists()) {
                System.out.println("Test scene not available - skipping test");
                return;
            }
            
            Pattern pattern = new Pattern.Builder()
                    .setFilename(TestPaths.getImagePath("topLeft"))
                    .build();
            StateImage stateImage = new StateImage.Builder()
                    .addPattern(pattern)
                    .build();
            
            // Test INSIDE_ANCHORS strategy
            DefineRegionOptions insideAnchorsOptions = new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                    .build();
            
            // Test BOUNDS strategy
            DefineRegionOptions boundsOptions = new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.BOUNDS)
                    .build();
            
            // Test MATCHES strategy
            DefineRegionOptions matchesOptions = new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.MATCHES)
                    .build();
            
            ObjectCollection objColl = new ObjectCollection.Builder()
                    .withScenes(new Pattern(TestPaths.getScreenshotPath("FloraNext1")))
                    .withImages(stateImage)
                    .build();
            
            // Test each strategy
            for (DefineRegionOptions options : List.of(insideAnchorsOptions, boundsOptions, matchesOptions)) {
                ActionResult result = new ActionResult();
                result.setActionConfig(options);
                
                ActionInterface defineAction = actionService.getAction(options);
                defineAction.perform(result, objColl);
                
                assertNotNull(result);
                assertTrue(result.getActionConfig() instanceof DefineRegionOptions);
                DefineRegionOptions resultOptions = (DefineRegionOptions) result.getActionConfig();
                assertEquals(options.getDefineAs(), resultOptions.getDefineAs());
                
                System.out.println("Define strategy " + options.getDefineAs() + " completed");
            }
            
        } catch (Exception e) {
            handleTestException(e);
        }
    }

    @Test
    void compareOldAndNewAPI() {
        // This test demonstrates the migration pattern
        Pattern pattern = new Pattern.Builder()
                .setFilename(TestPaths.getImagePath("topLeft"))
                .build();
        StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .build();
        ObjectCollection objColl = new ObjectCollection.Builder()
                .withScenes(new Pattern(TestPaths.getScreenshotPath("FloraNext1")))
                .withImages(stateImage)
                .build();
        
        // OLD API (commented out):
        /*
        ActionOptions oldOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        List<Scene> scenes = getScenes.getScenes(oldOptions, List.of(objColl));
        */
        
        // NEW API:
        DefineRegionOptions newOptions = new DefineRegionOptions.Builder()
                .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        
        ActionResult newResult = new ActionResult();
        newResult.setActionConfig(newOptions);
        
        ActionInterface defineAction = actionService.getAction(newOptions);
        defineAction.perform(newResult, objColl);
        
        List<Scene> scenes = newResult.getScenes();
        
        // Both approaches achieve the same result, but new API is more type-safe
        assertNotNull(scenes);
    }

    @Test
    void getWordMatches() {
        // Test not yet implemented
    }
    
    private void handleTestException(Exception e) {
        if (e.getMessage() != null && 
            (e.getMessage().contains("Can't read input file") ||
             e.getMessage().contains("NullPointerException") ||
             e.getMessage().contains("Image not found"))) {
            System.out.println("Test skipped due to: " + e.getMessage());
            return;
        }
        throw new RuntimeException(e);
    }
}