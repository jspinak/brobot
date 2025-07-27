package io.github.jspinak.brobot.tools.logging.illustration;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.testutils.TestPaths;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test specifically for illustration functionality.
 * This test helps identify why illustrations are showing black images.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "java.awt.headless=false",
    "brobot.mock.enabled=false",
    "brobot.force.headless=false", 
    "brobot.allow.screen.capture=true",
    "brobot.screenshot.save-history=true",
    "brobot.illustration.enabled=true",
    "brobot.logging.verbosity=DEBUG"
})
public class IllustrationDebugTest {

    @Autowired
    Action action;

    @BeforeAll
    public static void setupEnvironment() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void setUp() {
        FrameworkSettings.mock = false;
    }

    @Test
    void testScreenCaptureDirectly() {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();
        
        // Assert ExecutionEnvironment is properly configured
        assertTrue(env.canCaptureScreen(), 
            "ExecutionEnvironment.canCaptureScreen() must be true - if false, this is why illustrations are black");
        
        // Test direct screen capture
        Region fullScreen = new Region(); // Default constructor creates full screen region
        BufferedImage screenshot = BufferedImageUtilities.getBuffImgFromScreen(fullScreen);
        
        assertNotNull(screenshot, "Screenshot must not be null");
        assertTrue(screenshot.getWidth() > 0, "Screenshot width must be positive");
        assertTrue(screenshot.getHeight() > 0, "Screenshot height must be positive");
        
        // Verify image is not all black
        boolean hasNonBlackPixels = checkForNonBlackPixels(screenshot);
        assertTrue(hasNonBlackPixels, 
            "Screenshot must contain non-black pixels - all black indicates screen capture failure");
    }
    
    private boolean checkForNonBlackPixels(BufferedImage image) {
        // Check first 100x100 pixels
        int maxX = Math.min(100, image.getWidth());
        int maxY = Math.min(100, image.getHeight());
        
        for (int x = 0; x < maxX; x++) {
            for (int y = 0; y < maxY; y++) {
                int rgb = image.getRGB(x, y);
                if (rgb != 0xFF000000) { // Not pure black
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void testFindActionWithIllustrationDebug() {
        // Set up find operation with real images
        Pattern topLeftPattern = new Pattern.Builder()
                .setFilename(TestPaths.getImagePath("topLeft"))
                .setSimilarity(0.8)
                .build();

        StateImage stateImage = new StateImage.Builder()
                .addPattern(topLeftPattern)
                .setName("DebugTopLeft")
                .build();

        Scene scene = new Scene.Builder()
                .setFilename(TestPaths.getScreenshotPath("floranext0"))
                .build();

        ObjectCollection objColl = new ObjectCollection.Builder()
                .withImages(stateImage)
                .withScenes(scene)
                .build();

        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.7)
                .build();
        
        // Execute find operation
        ActionResult result = action.perform(findOptions, objColl);
        
        // Assert basic results
        assertNotNull(result, "ActionResult must not be null");
        
        // Verify scenes were captured for illustration
        assertTrue(result.getSceneAnalysisCollection().getScenes().size() > 0, 
            "Must capture scenes for illustration");
        
        // Verify illustration files were created
        File historyDir = new File("history");
        if (historyDir.exists()) {
            File[] files = historyDir.listFiles((dir, name) -> name.endsWith(".png"));
            assertNotNull(files, "History directory must contain files");
            assertTrue(files.length > 0, "Illustration PNG files must be created");
            
            // Verify files are not empty
            for (File file : files) {
                assertTrue(file.length() > 0, "Illustration file " + file.getName() + " must not be empty");
            }
        }
    }

    @Test
    void testGraphicsEnvironmentDebug() {
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        // Test headless state
        boolean isHeadless = ge.isHeadless();
        
        if (isHeadless) {
            // If GraphicsEnvironment is headless, ExecutionEnvironment should compensate
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            assertTrue(env.hasDisplay(), 
                "When GraphicsEnvironment is headless, ExecutionEnvironment must still report display available");
            assertTrue(env.canCaptureScreen(), 
                "When GraphicsEnvironment is headless, ExecutionEnvironment must still allow screen capture");
        } else {
            // If not headless, verify screen devices
            java.awt.GraphicsDevice[] devices = ge.getScreenDevices();
            assertTrue(devices.length > 0, "Must have at least one screen device");
            
            // Verify at least one device has valid resolution
            boolean hasValidDevice = false;
            for (java.awt.GraphicsDevice device : devices) {
                java.awt.DisplayMode mode = device.getDisplayMode();
                if (mode.getWidth() > 0 && mode.getHeight() > 0) {
                    hasValidDevice = true;
                    break;
                }
            }
            assertTrue(hasValidDevice, "At least one device must have valid display mode");
        }
    }

    @Test
    void testFileSystemPermissions() {
        // Verify test images are accessible
        assertTrue(Files.exists(TestPaths.getImagePathObject("topLeft")), 
            "topLeft.png must exist");
        assertTrue(Files.isReadable(TestPaths.getImagePathObject("topLeft")), 
            "topLeft.png must be readable");
        assertTrue(Files.exists(TestPaths.getScreenshotPathObject("floranext0")), 
            "floranext0.png must exist");
        assertTrue(Files.isReadable(TestPaths.getScreenshotPathObject("floranext0")), 
            "floranext0.png must be readable");
            
        // Verify illustration directory can be created and written to
        File historyDir = new File("history");
        if (!historyDir.exists()) {
            boolean created = historyDir.mkdirs();
            assertTrue(created || historyDir.exists(), "Must be able to create history directory");
        }
        
        assertTrue(historyDir.exists(), "History directory must exist");
        assertTrue(historyDir.canWrite(), "History directory must be writable");
    }
}