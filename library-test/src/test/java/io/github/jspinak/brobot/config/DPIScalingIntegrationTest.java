package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.utils.MatTestUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for DPI scaling functionality.
 * 
 * Tests that a 125x125 white square pattern can match a 100x100 white square
 * in a screenshot when using a resize factor of 0.8 (simulating 125% DPI scaling).
 * 
 * This test simulates the real-world scenario where:
 * - Patterns are captured at physical resolution (125x125 at 125% DPI)
 * - Screenshots are captured at logical resolution (100x100 appears as 100x100)
 * - The resize factor of 0.8 scales the pattern to match (125 * 0.8 = 100)
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.dpi.resize-factor=0.8",
    "brobot.action.similarity=0.70",
    "brobot.core.mock=true"  // Use mock mode for testing
})
public class DPIScalingIntegrationTest extends BrobotTestBase {
    
    @Autowired
    private Action action;
    
    @Autowired
    private ScenePatternMatcher scenePatternMatcher;
    
    @Autowired(required = false)
    private BrobotDPIConfiguration dpiConfiguration;
    
    private Mat patternMat;
    private Mat screenshotMat;
    private Pattern testPattern;
    private Scene testScene;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Create test Mats
        createTestMats();
        
        // Verify resize factor is set correctly
        assertEquals(0.8f, Settings.AlwaysResize, 0.01f, 
            "Resize factor should be set to 0.8 for this test");
    }
    
    /**
     * Creates the test Mats:
     * 1. Pattern: 200x200 with a 125x125 white square centered
     * 2. Screenshot: 200x200 with a 100x100 white square centered
     */
    private void createTestMats() {
        // Create pattern Mat: 200x200 with 125x125 white square
        patternMat = MatTestUtils.createColorMat(200, 200, 0, 0, 0); // Black background
        
        // Draw white square (125x125) centered
        int patternSquareSize = 125;
        int patternOffset = (200 - patternSquareSize) / 2; // Center the square
        Rect patternRect = new Rect(patternOffset, patternOffset, patternSquareSize, patternSquareSize);
        rectangle(patternMat, patternRect, new Scalar(255, 255, 255, 0), -1, 8, 0); // White filled rectangle
        
        // Create screenshot Mat: 200x200 with 100x100 white square
        screenshotMat = MatTestUtils.createColorMat(200, 200, 0, 0, 0); // Black background
        
        // Draw white square (100x100) centered
        int screenshotSquareSize = 100;
        int screenshotOffset = (200 - screenshotSquareSize) / 2; // Center the square
        Rect screenshotRect = new Rect(screenshotOffset, screenshotOffset, screenshotSquareSize, screenshotSquareSize);
        rectangle(screenshotMat, screenshotRect, new Scalar(255, 255, 255, 0), -1, 8, 0); // White filled rectangle
        
        // Validate the created Mats
        MatTestUtils.validateMat(patternMat, "pattern mat");
        MatTestUtils.validateMat(screenshotMat, "screenshot mat");
        
        // Create Pattern and Scene objects
        testPattern = new Pattern(patternMat);
        testPattern.setName("test-pattern-125x125");
        
        testScene = new Scene();
        testScene.setPattern(new Pattern(screenshotMat));
        testScene.setName("test-scene-100x100");
    }
    
    @Test
    public void testDPIScalingWithResizeFactor() {
        System.out.println("\n=== DPI Scaling Integration Test ===");
        System.out.println("Testing that 125x125 pattern matches 100x100 target with 0.8 resize factor");
        System.out.println("Current Settings.AlwaysResize: " + Settings.AlwaysResize);
        
        // Perform pattern matching directly
        List<Match> matches = scenePatternMatcher.findInScene(testPattern, testScene);
        
        // Verify results
        assertFalse(matches.isEmpty(), "Pattern should be found with resize factor 0.8");
        
        // Check similarity score
        Match bestMatch = matches.get(0);
        double similarity = bestMatch.getScore();
        System.out.println("Match similarity score: " + similarity);
        
        // With perfect scaling (125 * 0.8 = 100), we should get very high similarity
        assertTrue(similarity >= 0.99, 
            "Similarity should be >= 0.99 for perfectly scaled match, but was: " + similarity);
        
        // Verify match location (should be centered)
        System.out.println("Match found at: " + bestMatch.getRegion());
        
        // The match should be approximately centered (accounting for scaling)
        // After scaling, the 125x125 pattern becomes 100x100, matching the target
        int expectedX = 50; // Center of 100x100 square in 200x200 image
        int expectedY = 50;
        
        // Allow some tolerance for match position
        assertEquals(expectedX, bestMatch.getRegion().x, 5, 
            "Match X coordinate should be near center");
        assertEquals(expectedY, bestMatch.getRegion().y, 5, 
            "Match Y coordinate should be near center");
        
        System.out.println("✓ Test passed: Pattern scaling works correctly");
    }
    
    @Test
    public void testWithoutResizeFactor() {
        System.out.println("\n=== Testing WITHOUT resize factor (should fail) ===");
        
        // Temporarily set resize factor to 1.0 (no scaling)
        float originalResize = Settings.AlwaysResize;
        Settings.AlwaysResize = 1.0f;
        
        try {
            // Perform pattern matching
            List<Match> matches = scenePatternMatcher.findInScene(testPattern, testScene);
            
            // Without scaling, 125x125 pattern should NOT match 100x100 target well
            if (!matches.isEmpty()) {
                double similarity = matches.get(0).getScore();
                System.out.println("Match found with similarity: " + similarity);
                
                // Similarity should be lower without proper scaling
                assertTrue(similarity < 0.95, 
                    "Without scaling, similarity should be lower than 0.95");
            } else {
                System.out.println("No match found (expected behavior without scaling)");
            }
            
        } finally {
            // Restore original resize factor
            Settings.AlwaysResize = originalResize;
        }
    }
    
    @Test
    public void testDPIConfigurationBean() {
        // Verify that BrobotDPIConfiguration bean is available
        if (dpiConfiguration != null) {
            System.out.println("\n=== DPI Configuration Bean Test ===");
            float currentFactor = dpiConfiguration.getCurrentResizeFactor();
            System.out.println("Current resize factor from configuration: " + currentFactor);
            assertEquals(0.8f, currentFactor, 0.01f, 
                "DPI configuration should report 0.8 resize factor");
            
            // Test manual override
            dpiConfiguration.setResizeFactor(0.5f);
            assertEquals(0.5f, Settings.AlwaysResize, 0.01f, 
                "Should be able to manually set resize factor");
            
            // Restore
            dpiConfiguration.setResizeFactor(0.8f);
        } else {
            System.out.println("BrobotDPIConfiguration bean not available (may not be in component scan)");
        }
    }
    
    @Test 
    public void testVariousScaleFactors() {
        System.out.println("\n=== Testing Various Scale Factors ===");
        
        float originalResize = Settings.AlwaysResize;
        
        // Test different scale factors
        float[] scaleFactors = {0.5f, 0.667f, 0.8f, 1.0f, 1.25f};
        
        for (float factor : scaleFactors) {
            Settings.AlwaysResize = factor;
            System.out.printf("Testing with resize factor %.3f: ", factor);
            
            List<Match> matches = scenePatternMatcher.findInScene(testPattern, testScene);
            
            if (!matches.isEmpty()) {
                System.out.printf("FOUND (similarity: %.3f)\n", matches.get(0).getScore());
            } else {
                System.out.println("NOT FOUND");
            }
        }
        
        // Restore original
        Settings.AlwaysResize = originalResize;
    }
    
    @AfterEach
    public void cleanup() {
        // Clean up Mats
        MatTestUtils.safeReleaseAll(patternMat, screenshotMat);
    }
}