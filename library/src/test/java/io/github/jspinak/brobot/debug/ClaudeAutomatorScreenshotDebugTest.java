package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.tools.history.VisualizationOrchestrator;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.sikuli.basics.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to understand why claude-automator produces black screenshots
 * while library tests produce correct screenshots.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.test.TestConfiguration.class, classes = io.github.jspinak.brobot.test.TestConfiguration.class)
@TestPropertySource(properties = {
    "brobot.screenshot.save-history=true",
    "brobot.screenshot.history-path=test-history/",
    "brobot.screenshot.save-snapshots=true",
    "brobot.highlight.enabled=true",
    "brobot.aspects.visual-feedback.enabled=true",
    "brobot.illustration.draw-find=true",
    "brobot.illustration.draw-highlight=true",
    "brobot.framework.mock=false",
    "brobot.core.headless=false"
})
public class ClaudeAutomatorScreenshotDebugTest {

    @Autowired
    private Find find;
    
    @Autowired
    private StateStore stateStore;
    
    @Autowired
    private BrobotProperties brobotProperties;
    
    @Autowired
    private VisualizationOrchestrator visualizationOrchestrator;
    
    @Autowired
    private ImageFileUtilities imageFileUtilities;
    
    private Path historyPath;
    
    @BeforeEach
    void setUp() throws IOException {
        historyPath = Paths.get("test-history");
        if (!Files.exists(historyPath)) {
            Files.createDirectories(historyPath);
        }
        
        // Clear any existing test files
        try (Stream<Path> files = Files.list(historyPath)) {
            files.filter(path -> path.toString().endsWith(".png"))
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         // Ignore
                     }
                 });
        }
        
        System.out.println("=== SCREENSHOT DEBUG TEST SETUP ===");
        System.out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());
        System.out.println("ExecutionEnvironment.hasDisplay: " + ExecutionEnvironment.getInstance().hasDisplay());
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + ExecutionEnvironment.getInstance().canCaptureScreen());
        System.out.println("History path: " + historyPath.toAbsolutePath());
        System.out.println("saveHistory: " + brobotProperties.getScreenshot().isSaveHistory());
        // TODO: Fix BrobotProperties method names - may have changed
        // System.out.println("visual feedback enabled: " + brobotProperties.getAspects().getVisualFeedback().isEnabled());
        // System.out.println("highlight enabled: " + brobotProperties.getHighlight().isEnabled());
    }
    
    @Test
    void testScreenshotGenerationWithLimitedOutput() throws Exception {
        System.out.println("\n=== TESTING SCREENSHOT GENERATION ===");
        
        // Create a simple test state with a dummy image
        State testState = new State.Builder("TestState").build();
        StateImage stateImage = new StateImage.Builder()
            .setName("TestImage")
            .addPattern("library/images/TopLeft.png") // Use existing test image
            .setSearchRegionForAllPatterns(new Region(0, 0, 200, 200)) // Limit search area
            .build();
        testState.addStateImage(stateImage);
        stateStore.save(testState);
        
        // Create ActionResult for the find operation
        ActionResult result = new ActionResult();
        
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(stateImage)
            .build();
        
        System.out.println("Performing find action with forced illustration...");
        
        // Perform the action - this should trigger screenshot saving
        find.perform(result, objects);
        
        // Wait a moment for files to be written
        Thread.sleep(500);
        
        // Check what files were created
        System.out.println("\n=== CHECKING GENERATED FILES ===");
        try (Stream<Path> files = Files.list(historyPath)) {
            List<Path> pngFiles = files.filter(path -> path.toString().endsWith(".png"))
                                      .toList();
            
            System.out.println("Found " + pngFiles.size() + " PNG files");
            
            for (Path pngFile : pngFiles) {
                System.out.println("\nAnalyzing: " + pngFile.getFileName());
                
                // Load and analyze the image
                BufferedImage img = ImageIO.read(pngFile.toFile());
                assertNotNull(img, "Failed to load image: " + pngFile);
                
                int width = img.getWidth();
                int height = img.getHeight();
                System.out.println("Dimensions: " + width + "x" + height);
                
                // Check if image is all black
                boolean hasNonBlackPixels = false;
                int blackPixelCount = 0;
                int sampleSize = Math.min(1000, width * height);
                
                for (int i = 0; i < sampleSize; i++) {
                    int x = (int)(Math.random() * width);
                    int y = (int)(Math.random() * height);
                    int rgb = img.getRGB(x, y);
                    
                    if (rgb == 0xFF000000) {
                        blackPixelCount++;
                    } else {
                        hasNonBlackPixels = true;
                    }
                }
                
                double blackPercentage = (blackPixelCount * 100.0) / sampleSize;
                System.out.println("Black pixels: " + blackPercentage + "%");
                System.out.println("Has non-black content: " + hasNonBlackPixels);
                
                // Also check file size as indicator
                long fileSize = Files.size(pngFile);
                System.out.println("File size: " + fileSize + " bytes");
                
                // A completely black image would have a very small file size
                if (fileSize < 5000 && blackPercentage > 95) {
                    System.out.println("WARNING: This appears to be a black screenshot!");
                }
            }
        }
        
        // Test direct screen capture to compare
        System.out.println("\n=== TESTING DIRECT SCREEN CAPTURE ===");
        Robot robot = new Robot();
        BufferedImage directCapture = robot.createScreenCapture(new Rectangle(0, 0, 200, 200));
        
        // Save direct capture for comparison
        File directFile = new File(historyPath.toFile(), "direct-capture.png");
        ImageIO.write(directCapture, "png", directFile);
        System.out.println("Saved direct capture to: " + directFile.getPath());
        
        // Analyze direct capture
        boolean directHasContent = false;
        for (int i = 0; i < 100; i++) {
            int x = (int)(Math.random() * directCapture.getWidth());
            int y = (int)(Math.random() * directCapture.getHeight());
            if (directCapture.getRGB(x, y) != 0xFF000000) {
                directHasContent = true;
                break;
            }
        }
        System.out.println("Direct capture has content: " + directHasContent);
        
        // Test the visualization orchestrator directly
        System.out.println("\n=== TESTING VISUALIZATION ORCHESTRATOR ===");
        try {
            // Create a test scene
            // TODO: Fix convertToMat method - it may have been renamed or removed
            // Mat testScene = imageFileUtilities.convertToMat(directCapture);
            // System.out.println("Test scene created: " + testScene.cols() + "x" + testScene.rows());
            
            // Try to save it
            // imageFileUtilities.writeAllWithUniqueFilename(historyPath.toString(), "test-scene", List.of(testScene));
            // System.out.println("Test scene saved successfully");
        } catch (Exception e) {
            System.err.println("Error testing visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    void testFrameworkSettingsDirectly() {
        System.out.println("\n=== CHECKING FRAMEWORK SETTINGS ===");
        System.out.println("Settings.highlightDuration: " + Settings.Highlight);
        System.out.println("Settings.WaitScanRate: " + Settings.WaitScanRate);
        
        // Check Sikuli settings that might affect screenshots
        System.out.println("\n=== SIKULI SETTINGS ===");
        System.out.println("Settings.AutoDetectKeyboardLayout: " + Settings.AutoDetectKeyboardLayout);
        System.out.println("Settings.OcrDataPath: " + Settings.OcrDataPath);
    }
}