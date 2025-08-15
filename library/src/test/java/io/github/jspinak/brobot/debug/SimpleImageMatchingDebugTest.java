package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.recognition.MatImageRecognition;
import io.github.jspinak.brobot.util.image.capture.ScreenshotCapture;
import io.github.jspinak.brobot.util.file.SaveToFile;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

/**
 * Simple debugging test for image matching issues.
 * 
 * Update TEST_IMAGE_PATH to point to your actual image file.
 */

public class SimpleImageMatchingDebugTest {

    // @Autowired
    private MatImageRecognition matImageRecognition;
    
    // @Autowired
    private ScreenshotCapture screenshotCapture;
    
    // @Autowired
    private SaveToFile saveToFile;
    
    // Update this to your actual image path
    private static final String TEST_IMAGE_PATH = "prompt/claude-prompt-3.png";
    
    @Test
    public void testImageMatching() {
        System.out.println("=== Simple Image Matching Debug Test ===");
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Default SikuliX similarity: " + Settings.MinSimilarity);
        System.out.println();
        
        // Step 1: Check if image exists
        File imageFile = new File(TEST_IMAGE_PATH);
        System.out.println("Looking for image at: " + imageFile.getAbsolutePath());
        System.out.println("Image exists: " + imageFile.exists());
        
        if (!imageFile.exists()) {
            System.out.println("ERROR: Image file not found!");
            return;
        }
        
        try {
            // Step 2: Load and check image
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            System.out.println("Image loaded: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
            
            // Step 3: Create Pattern
            Pattern pattern = new Pattern(TEST_IMAGE_PATH);
            System.out.println("Pattern created with name: " + pattern.getName());
            System.out.println("Pattern has image: " + (pattern.getImage() != null));
            if (pattern.getImage() != null) {
                System.out.println("Pattern image size: " + 
                    pattern.getImage().getBufferedImage().getWidth() + "x" + 
                    pattern.getImage().getBufferedImage().getHeight());
            }
            
            // Step 4: Create StateImage
            StateImage stateImage = new StateImage.Builder()
                .addPattern(pattern)
                .setName("test-image")
                .build();
            System.out.println("StateImage created with " + stateImage.getPatterns().size() + " patterns");
            
            // Step 5: Capture screen
            System.out.println("\n--- Screen Capture Test ---");
            String screenshotPath = screenshotCapture.captureScreenshot("debug-screenshot");
            System.out.println("Screenshot saved to: " + screenshotPath);
            
            Screen screen = new Screen();
            System.out.println("Screen bounds: " + screen.getBounds());
            System.out.println("Number of screens: " + Screen.getNumberScreens());
            
            // Step 6: Test OpenCV matching
            System.out.println("\n--- OpenCV Matching Test ---");
            Mat templateMat = opencv_imgcodecs.imread(imageFile.getAbsolutePath());
            Mat screenMat = opencv_imgcodecs.imread(screenshotPath);
            
            System.out.println("Template Mat: " + templateMat.cols() + "x" + templateMat.rows());
            System.out.println("Screen Mat: " + screenMat.cols() + "x" + screenMat.rows());
            
            // Test with different thresholds
            double[] thresholds = {0.3, 0.5, 0.7, 0.9};
            for (double threshold : thresholds) {
                Optional<org.sikuli.script.Match> match = matImageRecognition.findTemplateMatch(
                    templateMat, screenMat, threshold
                );
                
                if (match.isPresent()) {
                    System.out.println("Threshold " + threshold + ": FOUND at " + match.get() + 
                                     " with score " + match.get().getScore());
                } else {
                    System.out.println("Threshold " + threshold + ": NOT FOUND");
                }
            }
            
            // Clean up
            templateMat.release();
            screenMat.release();
            
            System.out.println("\n--- Debugging Summary ---");
            if (thresholds[0] > 0.3) {
                System.out.println("If no matches found even at 0.3 threshold:");
                System.out.println("1. Make sure the image is visible on screen");
                System.out.println("2. Check if you're on the correct monitor");
                System.out.println("3. Verify image resolution matches screen");
                System.out.println("4. Check for scaling/DPI issues");
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        // Quick check without Spring
        File imageFile = new File(TEST_IMAGE_PATH);
        System.out.println("Image check: " + imageFile.getAbsolutePath());
        System.out.println("Exists: " + imageFile.exists());
        
        if (imageFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(imageFile);
                System.out.println("Size: " + img.getWidth() + "x" + img.getHeight());
                
                Pattern pattern = new Pattern(TEST_IMAGE_PATH);
                System.out.println("Pattern name: " + pattern.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}