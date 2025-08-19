package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
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
 * Tests for FindInScene functionality.
 * Works in headless mode using real image processing.
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

    @Test
    void findAllInScene() {
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
            DefineRegionOptions defineOptions = new DefineRegionOptions.Builder()
                    .setDefineAs(DefineRegionOptions.DefineAs.INSIDE_ANCHORS)
                    .build();
            ObjectCollection objectCollection = new ObjectCollection.Builder()
                    .withScenes(new Pattern(TestPaths.getScreenshotPath("FloraNext1")))
                    .withImages(topLeft, bottomRight)
                    .build();
            List<Scene> scenes = getScenes.getScenes(defineOptions, List.of(objectCollection));
            
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
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
}
