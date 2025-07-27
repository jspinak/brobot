package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.execution.actions.find.FindActions;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.action.ActionConfigBuilder;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.region.Region;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.model.state.stateObject.otherStateObjects.StateRegion;
import io.github.jspinak.brobot.model.state.ObjectCollections;
import io.github.jspinak.brobot.services.registry.StateRegistryService;
import io.github.jspinak.brobot.sikuli.interfaces.FrameworkSettings;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;
import io.github.jspinak.brobot.util.loggers.TestLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Debug test to understand why claude-automator produces black screenshots.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "brobot.screenshot.save-history=true",
    "brobot.screenshot.history-path=test-history/",
    "brobot.screenshot.save-snapshots=true",
    "brobot.highlight.enabled=true",
    "brobot.aspects.visual-feedback.enabled=true",
    "brobot.illustration.draw-find=true",
    "brobot.illustration.draw-highlight=true",
    "brobot.framework.mock=false",
    "brobot.core.headless=false",
    "brobot.aspects.action-lifecycle.enabled=true"
})
public class BlackScreenshotDebugTest {

    @Autowired
    private FindActions findActions;
    
    @Autowired
    private StateRegistryService stateRegistryService;
    
    @Autowired
    private BrobotProperties brobotProperties;
    
    @Autowired
    private ImageFileUtilities imageFileUtilities;
    
    @Autowired
    private TestLogger testLogger;
    
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
        
        System.out.println("=== BLACK SCREENSHOT DEBUG TEST SETUP ===");
        System.out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
        System.out.println("GraphicsEnvironment.isHeadless: " + GraphicsEnvironment.isHeadless());
        System.out.println("ExecutionEnvironment.hasDisplay: " + ExecutionEnvironment.getInstance().hasDisplay());
        System.out.println("ExecutionEnvironment.canCaptureScreen: " + ExecutionEnvironment.getInstance().canCaptureScreen());
        System.out.println("History path: " + historyPath.toAbsolutePath());
        
        // Check FrameworkSettings directly
        System.out.println("\n=== FRAMEWORK SETTINGS ===");
        System.out.println("FrameworkSettings.saveHistory: " + FrameworkSettings.saveHistory);
        System.out.println("FrameworkSettings.historyPath: " + FrameworkSettings.historyPath);
        System.out.println("FrameworkSettings.drawFind: " + FrameworkSettings.drawFind);
        System.out.println("FrameworkSettings.drawHighlight: " + FrameworkSettings.drawHighlight);
        
        // Force enable history
        FrameworkSettings.saveHistory = true;
        FrameworkSettings.historyPath = historyPath.toString() + "/";
        FrameworkSettings.drawFind = true;
        FrameworkSettings.drawHighlight = true;
        
        System.out.println("\n=== AFTER FORCING FRAMEWORK SETTINGS ===");
        System.out.println("FrameworkSettings.saveHistory: " + FrameworkSettings.saveHistory);
        System.out.println("FrameworkSettings.historyPath: " + FrameworkSettings.historyPath);
    }
    
    @Test
    void testScreenshotGenerationAndAnalyze() throws Exception {
        System.out.println("\n=== TESTING SCREENSHOT GENERATION ===");
        
        // Create a simple test state
        State testState = new State("TestState");
        
        // Add a search region to limit the area
        StateRegion searchRegion = new StateRegion.Builder()
            .withName("SearchArea")
            .withSearchRegion(new Region(0, 0, 200, 200))
            .build();
        testState.addStateRegion(searchRegion);
        
        // Use a simple StateImage without requiring an actual image file
        StateImage stateImage = new StateImage.Builder()
            .withName("TestImage")
            .withSearchRegion(new Region(0, 0, 200, 200))
            .build();
        testState.addStateImage(stateImage);
        
        stateRegistryService.addState(testState);
        
        // Create ActionConfig to force illustration
        ActionConfig actionConfig = new ActionConfigBuilder()
            .setAction(ActionConfig.Action.FIND)
            .setIllustrate(ActionConfig.Illustrate.YES)
            .setPauseAfterEnd(0.1)
            .build();
        
        ObjectCollections objects = new ObjectCollections.Builder()
            .withRegions(searchRegion)
            .build();
        
        System.out.println("Performing find action with forced illustration...");
        System.out.println("Using search region: " + searchRegion.getSearchRegion());
        
        // Perform the action
        ActionResult result = findActions.perform(actionConfig, objects);
        System.out.println("Action result: " + result);
        
        // Wait for files to be written
        Thread.sleep(1000);
        
        // Check what files were created
        System.out.println("\n=== CHECKING GENERATED FILES ===");
        File historyDir = historyPath.toFile();
        File[] pngFiles = historyDir.listFiles((dir, name) -> name.endsWith(".png"));
        
        if (pngFiles == null || pngFiles.length == 0) {
            System.out.println("NO PNG FILES FOUND IN: " + historyDir.getAbsolutePath());
            
            // Check if directory is writable
            System.out.println("Directory exists: " + historyDir.exists());
            System.out.println("Directory is writable: " + historyDir.canWrite());
            
            // Try direct screen capture as fallback test
            testDirectScreenCapture();
        } else {
            System.out.println("Found " + pngFiles.length + " PNG files");
            
            for (File pngFile : pngFiles) {
                analyzeScreenshot(pngFile);
            }
        }
    }
    
    private void analyzeScreenshot(File pngFile) throws IOException {
        System.out.println("\nAnalyzing: " + pngFile.getName());
        
        BufferedImage img = ImageIO.read(pngFile);
        assertNotNull(img, "Failed to load image: " + pngFile);
        
        int width = img.getWidth();
        int height = img.getHeight();
        System.out.println("Dimensions: " + width + "x" + height);
        
        // Detailed pixel analysis
        int blackPixelCount = 0;
        int nonBlackPixelCount = 0;
        int totalPixels = width * height;
        int sampleSize = Math.min(10000, totalPixels);
        
        // Sample evenly across the image
        for (int i = 0; i < sampleSize; i++) {
            int x = (i * width / sampleSize) % width;
            int y = (i / (width / sampleSize)) % height;
            int rgb = img.getRGB(x, y);
            
            if (rgb == 0xFF000000) {
                blackPixelCount++;
            } else {
                nonBlackPixelCount++;
                // Print first few non-black pixels
                if (nonBlackPixelCount <= 5) {
                    System.out.printf("Non-black pixel at (%d,%d): 0x%08X%n", x, y, rgb);
                }
            }
        }
        
        double blackPercentage = (blackPixelCount * 100.0) / sampleSize;
        System.out.println("Black pixels: " + blackPixelCount + " (" + blackPercentage + "%)");
        System.out.println("Non-black pixels: " + nonBlackPixelCount);
        System.out.println("File size: " + pngFile.length() + " bytes");
        
        if (blackPercentage > 95) {
            System.out.println("⚠️ WARNING: This appears to be a BLACK SCREENSHOT!");
            
            // Additional debugging for black screenshots
            System.out.println("\n=== DEBUGGING BLACK SCREENSHOT ===");
            
            // Check the corners
            System.out.println("Corner pixels:");
            System.out.printf("Top-left (0,0): 0x%08X%n", img.getRGB(0, 0));
            System.out.printf("Top-right (%d,0): 0x%08X%n", width-1, img.getRGB(width-1, 0));
            System.out.printf("Bottom-left (0,%d): 0x%08X%n", height-1, img.getRGB(0, height-1));
            System.out.printf("Bottom-right (%d,%d): 0x%08X%n", width-1, height-1, img.getRGB(width-1, height-1));
        }
    }
    
    private void testDirectScreenCapture() throws Exception {
        System.out.println("\n=== TESTING DIRECT SCREEN CAPTURE ===");
        
        Robot robot = new Robot();
        Rectangle captureRect = new Rectangle(0, 0, 400, 300);
        BufferedImage directCapture = robot.createScreenCapture(captureRect);
        
        // Save it
        File directFile = new File(historyPath.toFile(), "direct-robot-capture.png");
        ImageIO.write(directCapture, "png", directFile);
        System.out.println("Saved direct capture to: " + directFile.getAbsolutePath());
        
        // Analyze it
        analyzeScreenshot(directFile);
        
        // Also test with Brobot's utilities
        System.out.println("\n=== TESTING BROBOT'S IMAGE UTILITIES ===");
        BufferedImage brobotCapture = imageFileUtilities.captureScreen(new Region(0, 0, 400, 300));
        if (brobotCapture != null) {
            File brobotFile = new File(historyPath.toFile(), "brobot-capture.png");
            ImageIO.write(brobotCapture, "png", brobotFile);
            System.out.println("Saved Brobot capture to: " + brobotFile.getAbsolutePath());
            analyzeScreenshot(brobotFile);
        } else {
            System.out.println("Brobot capture returned null!");
        }
    }
}