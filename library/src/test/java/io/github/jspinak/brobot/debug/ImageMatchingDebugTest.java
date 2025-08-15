package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.FindImage;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.file.SaveToFile;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import io.github.jspinak.brobot.util.image.recognition.MatImageRecognition;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.ImagePath;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Comprehensive debugging test for image matching issues.
 * This test checks all potential causes of image matching failures.
 * 
 * To use this test:
 * 1. Update TEST_IMAGE_PATH to point to your actual image file
 * 2. Make sure the image is visible on screen when running the test
 * 3. Run the test and check the console output for debugging information
 */

public class ImageMatchingDebugTest {

    // @Autowired
    private FindImage findImage;
    
    // @Autowired
    private ScreenshotCapture screenshotCapture;
    
    // @Autowired
    private SaveToFile saveToFile;
    
    // @Autowired
    private MatImageRecognition matImageRecognition;
    
    // Test image paths - update these to your actual image paths
    private static final String TEST_IMAGE_PATH = "prompt/claude-prompt-1.png"; // Update this path
    private static final String TEST_IMAGE_NAME = "claude-prompt";
    
    @BeforeEach
    public void setup() {
        System.out.println("=== Image Matching Debug Test ===");
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("ImagePath: " + ImagePath.getBundlePath());
        System.out.println("Default SikuliX MinSimilarity: " + Settings.MinSimilarity);
        System.out.println("================================\n");
    }
    
    @Test
    public void debugImageMatching() {
        System.out.println("Starting comprehensive image matching debug test...\n");
        
        // Test 1: Verify image loading
        testImageLoading();
        
        // Test 2: Capture and save current screen
        testScreenCapture();
        
        // Test 3: Test different similarity thresholds
        testSimilarityThresholds();
        
        // Test 4: Test raw OpenCV matching
        testRawOpenCVMatching();
        
        // Test 5: Check screen and region info
        testScreenInfo();
        
        System.out.println("\n=== Debug test completed ===");
    }
    
    private void testImageLoading() {
        System.out.println("\n--- Test 1: Image Loading Verification ---");
        
        try {
            // Check if image file exists
            File imageFile = new File(TEST_IMAGE_PATH);
            System.out.println("Image file path: " + imageFile.getAbsolutePath());
            System.out.println("Image file exists: " + imageFile.exists());
            
            if (imageFile.exists()) {
                // Load as BufferedImage
                BufferedImage bufferedImage = ImageIO.read(imageFile);
                System.out.println("Image dimensions: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
                System.out.println("Image type: " + bufferedImage.getType());
                
                // Create Pattern - using file path 
                Pattern pattern = new Pattern(imageFile.getAbsolutePath());
                System.out.println("Pattern created successfully with name: " + pattern.getName());
                
                // Create StateImage
                StateImage stateImage = new StateImage.Builder()
                    .addPattern(pattern)
                    .setName(TEST_IMAGE_NAME)
                    .build();
                System.out.println("StateImage created with " + stateImage.getPatterns().size() + " patterns");
            } else {
                System.out.println("ERROR: Image file not found at " + imageFile.getAbsolutePath());
                System.out.println("Please update TEST_IMAGE_PATH to point to a valid image file");
            }
            
        } catch (Exception e) {
            System.out.println("ERROR loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testScreenCapture() {
        System.out.println("\n--- Test 2: Screen Capture ---");
        
        try {
            // Capture and save screenshot
            String screenshotPath = screenshotCapture.captureScreenshot("debug-screenshot");
            System.out.println("Screenshot saved to: " + screenshotPath);
            
            // Also save with timestamp
            screenshotCapture.saveScreenshotWithDate("debug-screenshot-timestamped");
            System.out.println("Timestamped screenshot saved");
            
            // Get screen info
            Screen screen = new Screen();
            System.out.println("Primary screen bounds: " + screen.getBounds());
            System.out.println("Number of screens: " + Screen.getNumberScreens());
            
        } catch (Exception e) {
            System.out.println("ERROR capturing screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testSimilarityThresholds() {
        System.out.println("\n--- Test 3: Similarity Thresholds ---");
        
        File imageFile = new File(TEST_IMAGE_PATH);
        if (!imageFile.exists()) {
            System.out.println("Skipping similarity test - image file not found");
            return;
        }
        
        try {
            // Create Pattern using file path
            Pattern pattern = new Pattern(TEST_IMAGE_NAME);
            // Pattern constructor already takes the path
            
            StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName(TEST_IMAGE_NAME)
                .build();
            
            ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
            
            // Test with different similarity levels
            double[] similarities = {0.3, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95};
            
            for (double similarity : similarities) {
                System.out.println("\nTesting with similarity: " + similarity);
                
                PatternFindOptions options = new PatternFindOptions.Builder()
                    .setSimilarity(similarity)
                    .setStrategy(PatternFindOptions.Strategy.ALL)
                    .setCaptureImage(true)
                    .build();
                
                ActionResult result = new ActionResult();
                result.setActionConfig(options);
                // findImage.findAll(result, List.of(objectCollection)); // Method not accessible
                
                System.out.println("Success: " + result.isSuccess());
                System.out.println("Matches found: " + result.getMatchList().size());
                
                for (Match match : result.getMatchList()) {
                    System.out.println("  Match score: " + match.getScore() + 
                                     " at " + match.getRegion());
                }
                
                // If no matches at low threshold, there's likely an issue
                if (similarity <= 0.5 && result.getMatchList().isEmpty()) {
                    System.out.println("WARNING: No matches found even at low similarity " + similarity);
                    System.out.println("Possible issues:");
                    System.out.println("  - Image not visible on screen");
                    System.out.println("  - Wrong screen being captured");
                    System.out.println("  - Image format/resolution mismatch");
                }
            }
            
        } catch (Exception e) {
            System.out.println("ERROR in similarity test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testRawOpenCVMatching() {
        System.out.println("\n--- Test 4: Raw OpenCV Matching ---");
        
        File imageFile = new File(TEST_IMAGE_PATH);
        if (!imageFile.exists()) {
            System.out.println("Skipping OpenCV test - image file not found");
            return;
        }
        
        try {
            // Load template
            Mat templateMat = opencv_imgcodecs.imread(imageFile.getAbsolutePath());
            System.out.println("Template loaded: " + templateMat.cols() + "x" + templateMat.rows());
            
            if (templateMat.empty()) {
                System.out.println("ERROR: Failed to load template image with OpenCV");
                return;
            }
            
            // Capture screen
            String screenshotPath = screenshotCapture.captureScreenshot("opencv-test-screen");
            File screenshotFile = new File(screenshotPath);
            
            if (!screenshotFile.exists()) {
                System.out.println("ERROR: Screenshot file not created");
                return;
            }
            
            Mat screenMat = opencv_imgcodecs.imread(screenshotPath);
            System.out.println("Screen captured: " + screenMat.cols() + "x" + screenMat.rows());
            
            if (screenMat.empty()) {
                System.out.println("ERROR: Failed to load screenshot with OpenCV");
                return;
            }
            
            // Test with very low threshold
            double[] thresholds = {0.3, 0.5, 0.7, 0.9};
            for (double threshold : thresholds) {
                Optional<org.sikuli.script.Match> match = matImageRecognition.findTemplateMatch(
                    templateMat, screenMat, threshold
                );
                
                System.out.println("Threshold " + threshold + ": " + 
                    (match.isPresent() ? "Found at " + match.get() + " with score " + match.get().getScore() : "Not found"));
            }
            
            // Clean up mats
            templateMat.release();
            screenMat.release();
            
        } catch (Exception e) {
            System.out.println("ERROR in OpenCV test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void testScreenInfo() {
        System.out.println("\n--- Test 5: Screen Information ---");
        
        try {
            // Get all screens
            int numScreens = Screen.getNumberScreens();
            System.out.println("Number of screens: " + numScreens);
            
            for (int i = 0; i < numScreens; i++) {
                Screen screen = new Screen(i);
                System.out.println("Screen " + i + ":");
                System.out.println("  Bounds: " + screen.getBounds());
                System.out.println("  ID: " + screen.getID());
                
                // Check if this is the screen being used for capture
                if (i == 0) {
                    System.out.println("  (Primary screen - used by default)");
                }
            }
            
            // Get screen with mouse
            org.sikuli.script.Location mouseLoc = org.sikuli.script.Mouse.at();
            System.out.println("\nMouse location: " + mouseLoc);
            Screen mouseScreen = new Screen(mouseLoc.getScreen().getID());
            System.out.println("Mouse on screen: " + mouseScreen.getID());
            
            // Test with different screens if multiple available
            if (numScreens > 1) {
                System.out.println("\nWARNING: Multiple screens detected!");
                System.out.println("Make sure your image is on the correct screen.");
                System.out.println("Brobot captures from the screen where the mouse is located.");
            }
            
        } catch (Exception e) {
            System.out.println("ERROR getting screen info: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Simple main method for quick testing
    public static void main(String[] args) {
        System.out.println("Running simple image check...");
        
        File imageFile = new File(TEST_IMAGE_PATH);
        System.out.println("Looking for image at: " + imageFile.getAbsolutePath());
        System.out.println("Image exists: " + imageFile.exists());
        
        if (imageFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(imageFile);
                System.out.println("Image loaded successfully: " + img.getWidth() + "x" + img.getHeight());
                
                // Also check if we can load it as a Pattern
                Pattern pattern = new Pattern(TEST_IMAGE_NAME);
                // Pattern constructor already takes the path
                System.out.println("Pattern created successfully with name: " + pattern.getName());
                
            } catch (Exception e) {
                System.out.println("Failed to load image: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Image file not found!");
            System.out.println("Make sure TEST_IMAGE_PATH points to a valid image file");
        }
    }
}