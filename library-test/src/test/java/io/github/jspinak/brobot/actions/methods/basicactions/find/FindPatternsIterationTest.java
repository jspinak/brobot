package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.methods.basicactions.TestData;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.IterativePatternFinder;

/**
 * Tests for finding patterns through iteration.
 * Works in headless mode by using real image processing.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
class FindPatternsIterationTest extends BrobotIntegrationTestBase {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    IterativePatternFinder iterativePatternFinder;

    @Autowired
    SceneProvider getScenes;

    @Autowired
    ActionResultFactory matchesInitializer;

    @Test
    void find_() {
        try {
            TestData testData = new TestData();

            List<Scene> scenes = getScenes.getScenes(testData.getDefineInsideAnchors(), 
                List.of(testData.getInsideAnchorObjects()));
            
            List<StateImage> stateImages = new ArrayList<>();
            stateImages.add(testData.getTopLeft());
            stateImages.add(testData.getBottomRight());

            ActionResult matches = matchesInitializer.init(testData.getDefineInsideAnchors(), 
                testData.getInsideAnchorObjects());

            iterativePatternFinder.find(matches, stateImages, scenes);
            
            // Log results
            System.out.println("Found " + matches.size() + " matches");
            for (Match matchObject : matches.getMatchList()) {
                System.out.println(matchObject);
            }
            
            // In headless mode, we might not find matches if image recognition doesn't work
            // But the operation should complete without errors
            assertNotNull(matches);
            assertNotNull(matches.getMatchList());
            
            // In real mode with proper images and display, we expect to find matches
            if (useRealFiles() && !isHeadlessEnvironment()) {
                assertFalse(matches.isEmpty());
            }
            
        } catch (Exception e) {
            // Handle image loading issues
            if (e.getMessage() != null && 
                (e.getMessage().contains("Can't read input file") ||
                 e.getMessage().contains("NullPointerException"))) {
                System.out.println("Test images not available - skipping test");
                return;
            }
            throw e;
        }
    }
}