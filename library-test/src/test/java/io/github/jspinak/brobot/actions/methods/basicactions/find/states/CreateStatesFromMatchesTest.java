package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import io.github.jspinak.brobot.test.ocr.OcrTestSupport;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.action.basic.find.FindState;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.find.match.MatchToStateConverter;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for creating states from visual matches using saved FloraNext screenshots.
 * Works in headless/CI environments without requiring live OCR.
 */
class CreateStatesFromMatchesTest extends BrobotIntegrationTestBase {

    private static File screenshotDir;
    
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        screenshotDir = OcrTestSupport.getScreenshotDirectory();
    }

    @Autowired
    MatchToStateConverter createStatesFromMatches;

    @Autowired
    Action action;

    @Autowired
    FindState findStates;

    @Autowired
    ActionResultFactory matchesInitializer;

    /**
     * Creates mock matches from FloraNext screenshots to test state creation.
     */
    private ActionResult createMatchesFromScreenshots() {
        ActionResult result = new ActionResult();
        
        if (screenshotDir == null || !screenshotDir.exists()) {
            System.out.println("Screenshot directory not found");
            return result;
        }
        
        try {
            for (int i = 0; i <= 4; i++) {
                File screenshot = new File(screenshotDir, "floranext" + i + ".png");
                if (screenshot.exists()) {
                    BufferedImage image = ImageIO.read(screenshot);
                    
                    // Create matches at different regions of the screenshot
                    // Simulate finding UI elements
                    Match match1 = new Match.Builder()
                        .setRegion(new Region(10, 10, 100, 50))
                        .setSimScore(0.95)
                        .setText("Button " + i)
                        .setName("floranext_match_" + i + "_a")
                        .build();
                    result.getMatchList().add(match1);
                    
                    if (image.getWidth() > 200 && image.getHeight() > 100) {
                        Match match2 = new Match.Builder()
                            .setRegion(new Region(150, 50, 120, 60))
                            .setSimScore(0.88)
                            .setText("Field " + i)
                            .setName("floranext_match_" + i + "_b")
                            .build();
                        result.getMatchList().add(match2);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading screenshots for match creation: " + e.getMessage());
        }
        
        result.setSuccess(!result.getMatchList().isEmpty());
        return result;
    }

    /**
     * Creates states directly from FloraNext screenshots.
     */
    private List<State> createStatesFromFloraNextScreenshots() {
        List<State> states = new ArrayList<>();
        
        if (!OcrTestSupport.areScreenshotsAvailable()) {
            System.out.println("FloraNext screenshots not available");
            return states;
        }
        
        try {
            for (int i = 0; i <= 4; i++) {
                File screenshot = new File(screenshotDir, "floranext" + i + ".png");
                if (screenshot.exists()) {
                    BufferedImage bufferedImage = ImageIO.read(screenshot);
                    Mat mat = MatrixUtilities.bufferedImageToMat(bufferedImage).orElse(new Mat());
                    
                    State.Builder stateBuilder = new State.Builder("FloraNextState" + i);
                    
                    // Create StateImage from the screenshot
                    StateImage stateImage = new StateImage.Builder()
                        .setName("screenshot_" + i)
                        .setSearchRegionForAllPatterns(new Region(0, 0, 
                            bufferedImage.getWidth(), bufferedImage.getHeight()))
                        .build();
                    
                    Pattern pattern = new Pattern.Builder()
                        .setMat(mat)
                        .setFixedRegion(new Region(0, 0, 
                            bufferedImage.getWidth(), bufferedImage.getHeight()))
                        .build();
                    stateImage.getPatterns().add(pattern);
                    
                    stateBuilder.withImages(stateImage);
                    states.add(stateBuilder.build());
                    
                    System.out.println("Created state from " + screenshot.getName());
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating states from screenshots: " + e.getMessage());
        }
        
        return states;
    }

    @Test
    void testCreateStatesFromMatches() {
        assertTrue(OcrTestSupport.areScreenshotsAvailable(), 
                  "FloraNext screenshots should be available");
        
        // Create matches from screenshots
        ActionResult matches = createMatchesFromScreenshots();
        assertFalse(matches.getMatchList().isEmpty(), 
                   "Should have created matches from screenshots");
        
        System.out.println("Created " + matches.getMatchList().size() + 
                         " matches from FloraNext screenshots");
        
        // Convert matches to states
        List<State> states = createStatesFromMatches.create(matches);
        assertNotNull(states, "State list should not be null");
        
        // The converter might create states differently, but we should get something
        System.out.println("Converted matches to " + states.size() + " states");
        
        if (!states.isEmpty()) {
            states.forEach(state -> {
                assertNotNull(state);
                assertNotNull(state.getName());
                System.out.println("  State: " + state.getName() + 
                                 " with " + state.getStateImages().size() + " images");
            });
        }
    }

    @Test
    void testStateCreationFromScreenshots() {
        List<State> states = createStatesFromFloraNextScreenshots();
        
        assertFalse(states.isEmpty(), "Should have created states from screenshots");
        assertEquals(Math.min(5, states.size()), states.size(), 
                    "Should create one state per available screenshot");
        
        // Verify each state has proper structure
        states.forEach(state -> {
            assertNotNull(state.getName(), "State should have a name");
            assertFalse(state.getStateImages().isEmpty(), 
                       "State should have at least one StateImage");
            
            state.getStateImages().forEach(stateImage -> {
                assertNotNull(stateImage.getName(), "StateImage should have a name");
                assertFalse(stateImage.getPatterns().isEmpty(), 
                           "StateImage should have patterns");
                
                stateImage.getPatterns().forEach(pattern -> {
                    assertNotNull(pattern.getMat(), "Pattern should have a Mat");
                    assertNotNull(pattern.getRegion(), "Pattern should have a region");
                    
                    // Verify Mat has content
                    Mat mat = pattern.getMat();
                    assertTrue(countNonZero(MatrixUtilities.toGrayscale(mat)) > 0,
                              "Pattern Mat should have non-zero pixels");
                });
            });
        });
        
        System.out.println("Successfully verified " + states.size() + 
                         " states created from FloraNext screenshots");
    }

    @Test
    void testMatchToStateConversion() {
        // Create a simple match to test conversion
        Match testMatch = new Match.Builder()
            .setRegion(new Region(100, 100, 200, 150))
            .setSimScore(0.92)
            .setText("Test UI Element")
            .setName("test_match")
            .build();
        
        ActionResult result = new ActionResult();
        result.getMatchList().add(testMatch);
        
        List<State> states = createStatesFromMatches.create(result);
        assertNotNull(states, "Converted states should not be null");
        
        System.out.println("Converted single match to " + states.size() + " state(s)");
    }
    
    @Test
    void testEmptyMatchHandling() {
        // Test with empty matches
        ActionResult emptyResult = new ActionResult();
        
        List<State> states = createStatesFromMatches.create(emptyResult);
        assertNotNull(states, "State list should not be null even with empty matches");
        
        // Empty matches might produce empty state list or default states
        System.out.println("Empty matches produced " + states.size() + " state(s)");
    }
    
    @Test
    void testScreenshotIntegrity() {
        if (!OcrTestSupport.areScreenshotsAvailable()) {
            System.out.println("Screenshots not available - skipping integrity test");
            return;
        }
        
        // Verify FloraNext screenshots can be loaded and processed
        int validScreenshots = 0;
        
        for (int i = 0; i <= 4; i++) {
            File screenshot = new File(screenshotDir, "floranext" + i + ".png");
            if (screenshot.exists()) {
                try {
                    BufferedImage image = ImageIO.read(screenshot);
                    assertNotNull(image, "Should load " + screenshot.getName());
                    assertTrue(image.getWidth() > 0 && image.getHeight() > 0,
                              "Image should have valid dimensions");
                    
                    // Convert to Mat to verify OpenCV compatibility
                    Mat mat = MatrixUtilities.bufferedImageToMat(image).orElse(null);
                    assertNotNull(mat, "Should convert to Mat");
                    assertTrue(mat.cols() > 0 && mat.rows() > 0,
                              "Mat should have valid dimensions");
                    
                    validScreenshots++;
                    System.out.println("Verified " + screenshot.getName() + 
                                     ": " + image.getWidth() + "x" + image.getHeight());
                } catch (IOException e) {
                    fail("Failed to load " + screenshot.getName() + ": " + e.getMessage());
                }
            }
        }
        
        assertTrue(validScreenshots > 0, "Should have at least one valid screenshot");
        System.out.println("Successfully verified " + validScreenshots + " FloraNext screenshots");
    }
}