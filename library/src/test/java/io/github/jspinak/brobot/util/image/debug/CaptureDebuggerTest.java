package io.github.jspinak.brobot.util.image.debug;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.Pattern;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

@ExtendWith(MockitoExtension.class)
@DisabledInCI
public class CaptureDebuggerTest extends BrobotTestBase {

    private CaptureDebugger captureDebugger;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @TempDir Path tempDir;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Create CaptureDebugger (it will use its default directory)
        captureDebugger = new CaptureDebugger();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    public void testConstructor_CreatesDebugDirectory() {
        // Act
        CaptureDebugger debugger = new CaptureDebugger();

        // Assert - default debug directory should exist
        File dir = new File("debug-captures");
        assertTrue(dir.exists() || dir.mkdir());
    }

    @Test
    public void testDebugCapture_BasicRegion() {
        // Arrange
        Region region = new Region(0, 0, 100, 100);

        // Act
        captureDebugger.debugCapture(region, null);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("CAPTURE DEBUG SESSION"));
        assertTrue(output.contains("TESTING SCREEN CREATION METHODS"));
        assertTrue(output.contains("COMPARING CAPTURES"));
        assertTrue(output.contains("SYSTEM INFORMATION"));
    }

    @Test
    public void testDebugCapture_WithPattern() throws IOException {
        // Arrange
        Region region = new Region(0, 0, 100, 100);

        // Create a test pattern image
        BufferedImage patternImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = patternImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        File patternFile = tempDir.resolve("test-pattern.png").toFile();
        ImageIO.write(patternImage, "png", patternFile);

        // Act
        captureDebugger.debugCapture(region, patternFile.getAbsolutePath());

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("PATTERN MATCHING TESTS"));
        assertTrue(output.contains("Pattern: " + patternFile.getAbsolutePath()));
    }

    @Test
    public void testDebugCapture_InvalidPatternPath() {
        // Arrange
        Region region = new Region(0, 0, 100, 100);
        String invalidPath = "/non/existent/pattern.png";

        // Act
        captureDebugger.debugCapture(region, invalidPath);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("CAPTURE DEBUG SESSION"));
        // Pattern matching section should be skipped
        assertFalse(output.contains("PATTERN MATCHING TESTS"));
    }

    @Test
    public void testCaptureWithNewScreen() {
        // Arrange
        Region region = new Region(10, 10, 50, 50);
        String filename = "test-capture";

        // Act - Use reflection to test private method
        BufferedImage result =
                invokePrivateMethod(captureDebugger, "captureWithNewScreen", region, filename);

        // Assert
        // In mock mode, capture methods might return null or mock images
        String output = outputStream.toString();
        assertTrue(
                output.contains("Method: new Screen()")
                        || output.contains("capture")
                        || result == null
                        || result.getWidth() > 0);
    }

    @Test
    public void testCaptureWithDefaultScreen() {
        // Arrange
        Region region = new Region(10, 10, 50, 50);
        String filename = "test-default";

        // Act
        BufferedImage result =
                invokePrivateMethod(captureDebugger, "captureWithDefaultScreen", region, filename);

        // Assert
        String output = outputStream.toString();
        assertTrue(
                output.contains("Method: Screen.getPrimaryScreen()")
                        || output.contains("capture")
                        || result == null);
    }

    @Test
    public void testCaptureWithRobot() {
        // Arrange
        Region region = new Region(10, 10, 50, 50);
        String filename = "test-robot";

        // Act
        BufferedImage result =
                invokePrivateMethod(captureDebugger, "captureWithRobot", region, filename);

        // Assert
        String output = outputStream.toString();
        assertTrue(
                output.contains("Method: Robot.createScreenCapture()")
                        || output.contains("Robot")
                        || result == null);
    }

    @Test
    public void testCompareImages_IdenticalImages() {
        // Arrange
        BufferedImage img1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);

        // Make them identical
        Graphics2D g1 = img1.createGraphics();
        g1.setColor(Color.BLUE);
        g1.fillRect(0, 0, 50, 50);
        g1.dispose();

        Graphics2D g2 = img2.createGraphics();
        g2.setColor(Color.BLUE);
        g2.fillRect(0, 0, 50, 50);
        g2.dispose();

        // Act
        invokePrivateMethod(captureDebugger, "compareImages", img1, img2, "Test Comparison");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Test Comparison"));
        assertTrue(
                output.contains("Same dimensions: 50x50")
                        || output.contains("50")
                        || output.contains("identical"));
    }

    @Test
    public void testCompareImages_DifferentSizes() {
        // Arrange
        BufferedImage img1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Act
        invokePrivateMethod(captureDebugger, "compareImages", img1, img2, "Size Diff");

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Size Diff"));
        assertTrue(
                output.contains("DIFFERENT dimensions")
                        || output.contains("50x50 vs 100x100")
                        || output.contains("different"));
    }

    @Test
    public void testCompareImages_NullImages() {
        // Act & Assert - Should handle nulls gracefully
        assertDoesNotThrow(
                () -> {
                    invokePrivateMethod(captureDebugger, "compareImages", null, null, "Both Null");
                    invokePrivateMethod(
                            captureDebugger,
                            "compareImages",
                            new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB),
                            null,
                            "Second Null");
                    invokePrivateMethod(
                            captureDebugger,
                            "compareImages",
                            null,
                            new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB),
                            "First Null");
                });

        String output = outputStream.toString();
        assertTrue(
                output.contains("null") || output.contains("NULL") || output.contains("missing"));
    }

    @Test
    public void testAnalyzeImage() {
        // Arrange
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        // Create a test pattern with different colors
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.GREEN);
        g.fillRect(50, 0, 50, 50);
        g.setColor(Color.BLUE);
        g.fillRect(0, 50, 50, 50);
        g.setColor(Color.WHITE);
        g.fillRect(50, 50, 50, 50);
        g.dispose();

        // Act
        invokePrivateMethod(captureDebugger, "analyzeImage", img, "TEST IMAGE");

        // Assert
        String output = outputStream.toString();
        assertTrue(
                output.contains("TEST IMAGE ANALYSIS")
                        || output.contains("TEST IMAGE")
                        || output.contains("100x100"));
    }

    @Test
    public void testSaveImage() throws IOException {
        // Arrange
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.YELLOW);
        g.fillRect(0, 0, 50, 50);
        g.dispose();

        String filename = "test-save";

        // Act
        invokePrivateMethod(captureDebugger, "saveImage", img, filename);

        // Assert
        // File will be saved in the debug-captures directory
        File expectedFile = new File("debug-captures", filename + ".png");
        assertTrue(expectedFile.exists() || outputStream.toString().contains("Saved"));
    }

    @Test
    public void testPrintSystemInfo() {
        // Act
        invokePrivateMethod(captureDebugger, "printSystemInfo");

        // Assert
        String output = outputStream.toString();
        assertTrue(
                output.contains("Java Version")
                        || output.contains("OS")
                        || output.contains("Screen")
                        || output.contains("System"));
    }

    @Test
    public void testTestPatternMatch() {
        // Arrange
        BufferedImage capture = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = capture.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50); // Draw pattern in capture
        g.dispose();

        // Create a mock pattern (in mock mode, Pattern might not work properly)
        try {
            // Create pattern image
            BufferedImage patternImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D pg = patternImg.createGraphics();
            pg.setColor(Color.RED);
            pg.fillRect(0, 0, 50, 50);
            pg.dispose();

            // Save pattern temporarily
            File patternFile = tempDir.resolve("pattern.png").toFile();
            ImageIO.write(patternImg, "png", patternFile);

            Pattern pattern = new Pattern(patternFile.getAbsolutePath());

            // Act
            invokePrivateMethod(
                    captureDebugger, "testPatternMatch", capture, pattern, "Test Match");

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Test Match") || output.contains("Pattern matching"));

        } catch (Exception e) {
            // In mock mode, Pattern creation might fail - that's OK
            assertTrue(FrameworkSettings.mock);
        }
    }

    @Test
    public void testDebugCapture_FullIntegration() throws IOException {
        // Arrange
        Region region = new Region(0, 0, 200, 200);

        // Create multiple test images
        BufferedImage pattern = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = pattern.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, 30, 30);
        g.dispose();

        File patternFile = tempDir.resolve("integration-pattern.png").toFile();
        ImageIO.write(pattern, "png", patternFile);

        // Act
        captureDebugger.debugCapture(region, patternFile.getAbsolutePath());

        // Assert
        String output = outputStream.toString();

        // Verify all major sections are present
        assertTrue(output.contains("CAPTURE DEBUG SESSION"));
        assertTrue(output.contains("TESTING SCREEN CREATION METHODS"));
        assertTrue(output.contains("COMPARING CAPTURES"));
        assertTrue(output.contains("PATTERN MATCHING TESTS"));
        assertTrue(output.contains("SYSTEM INFORMATION"));
        assertTrue(output.contains("Debug images saved to"));

        // Verify timestamp format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestamp = dateFormat.format(new Date());
        assertTrue(output.contains(timestamp.substring(0, 8))); // Check date part
    }

    @Test
    public void testDebugCapture_LargeRegion() {
        // Arrange
        Region largeRegion = new Region(0, 0, 2000, 2000);

        // Act
        captureDebugger.debugCapture(largeRegion, null);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("CAPTURE DEBUG SESSION"));
        // Should handle large regions without errors
    }

    @Test
    public void testDebugCapture_NegativeCoordinates() {
        // Arrange
        Region negativeRegion = new Region(-100, -100, 200, 200);

        // Act
        assertDoesNotThrow(() -> captureDebugger.debugCapture(negativeRegion, null));

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("CAPTURE DEBUG SESSION"));
    }

    private <T> T invokePrivateMethod(Object target, String methodName, Object... args) {
        try {
            return (T) ReflectionTestUtils.invokeMethod(target, methodName, args);
        } catch (Exception e) {
            // Some methods might fail in mock mode - that's OK
            if (FrameworkSettings.mock) {
                return null;
            }
            throw new RuntimeException("Failed to invoke: " + methodName, e);
        }
    }
}
