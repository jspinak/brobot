package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Definitive test to prove that file path vs BufferedImage loading makes no difference, and that
 * Settings.AlwaysResize is the real solution.
 */
@DisabledInCI
public class DefinitivePatternTest extends DebugTestBase {

    @Test
    public void proveLoadingMethodsAreIdentical() throws Exception {
        System.out.println("=== DEFINITIVE PATTERN LOADING TEST ===\n");

        // Ensure no resize is applied initially
        Settings.AlwaysResize = 0;
        Settings.MinSimilarity = 0.7;

        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);

        if (!patternFile.exists()) {
            System.out.println("Pattern file not found: " + patternFile.getAbsolutePath());
            System.out.println("Creating a test pattern...");
            createTestPattern(patternFile);
        }

        // Set ImagePath so SikuliX can find the pattern
        ImagePath.setBundlePath(new File(".").getAbsolutePath());

        System.out.println("Testing with pattern: " + patternPath);
        System.out.println("Pattern exists: " + patternFile.exists());
        System.out.println("Pattern size: " + patternFile.length() + " bytes");

        // Create a screenshot
        Screen screen = new Screen();
        ScreenImage screenshot = screen.capture();
        BufferedImage screenImg = screenshot.getImage();

        System.out.println("\n=== SCREENSHOT INFO ===");
        System.out.println("Resolution: " + screenImg.getWidth() + "x" + screenImg.getHeight());
        System.out.println("Type: " + getImageType(screenImg.getType()));

        // Method 1: Load pattern from file path (like IDE)
        System.out.println("\n=== METHOD 1: FILE PATH (IDE style) ===");
        testWithFilePath(screenshot, patternPath);

        // Method 2: Load pattern from BufferedImage (like Brobot)
        System.out.println("\n=== METHOD 2: BUFFEREDIMAGE (Brobot style) ===");
        testWithBufferedImage(screenshot, patternFile);

        // Method 3: Apply Settings.AlwaysResize
        System.out.println("\n=== METHOD 3: WITH Settings.AlwaysResize = 0.8 ===");
        Settings.AlwaysResize = 0.8f;
        System.out.println("Settings.AlwaysResize set to: " + Settings.AlwaysResize);

        System.out.println("\nFile path with resize:");
        testWithFilePath(screenshot, patternPath);

        System.out.println("\nBufferedImage with resize:");
        testWithBufferedImage(screenshot, patternFile);

        // Reset
        Settings.AlwaysResize = 0;

        // Method 4: Check system DPI
        System.out.println("\n=== DPI DETECTION ===");
        detectAndReportDPI();

        System.out.println("\n=== CONCLUSION ===");
        System.out.println(
                "If both methods (file path and BufferedImage) produce the same results,");
        System.out.println("then the loading method doesn't matter.");
        System.out.println("\nIf Settings.AlwaysResize improves matching for BOTH methods,");
        System.out.println("then it's the correct solution for DPI scaling issues.");
    }

    private void testWithFilePath(ScreenImage screenshot, String patternPath) {
        try {
            // Create Finder with screenshot
            Finder finder = new Finder(screenshot);

            // Find using file path directly (IDE method)
            finder.find(patternPath);

            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  ✓ Found! Score: " + String.format("%.3f", match.getScore()));
            } else {
                System.out.println("  ✗ Not found at " + Settings.MinSimilarity + " threshold");
            }

            finder.destroy();
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void testWithBufferedImage(ScreenImage screenshot, File patternFile) {
        try {
            // Load pattern as BufferedImage
            BufferedImage patternImg = ImageIO.read(patternFile);
            System.out.println(
                    "  Pattern loaded: " + patternImg.getWidth() + "x" + patternImg.getHeight());

            // Create Pattern from BufferedImage (Brobot method)
            Pattern pattern = new Pattern(patternImg);
            pattern.similar(Settings.MinSimilarity);

            // Create Finder with screenshot
            Finder finder = new Finder(screenshot);

            // Find using Pattern object
            finder.find(pattern);

            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  ✓ Found! Score: " + String.format("%.3f", match.getScore()));
            } else {
                System.out.println("  ✗ Not found at " + Settings.MinSimilarity + " threshold");
            }

            finder.destroy();
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void detectAndReportDPI() {
        try {
            // Get physical resolution
            GraphicsDevice device =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            DisplayMode mode = device.getDisplayMode();

            // Get logical resolution
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            System.out.println("Physical resolution: " + mode.getWidth() + "x" + mode.getHeight());
            System.out.println(
                    "Logical resolution: " + screenSize.getWidth() + "x" + screenSize.getHeight());

            if (mode.getWidth() != screenSize.getWidth()) {
                float scale = (float) screenSize.getWidth() / mode.getWidth();
                int scalingPercent = (int) ((1 / scale) * 100);
                System.out.println("DPI Scaling detected: " + scalingPercent + "%");
                System.out.println("Recommended Settings.AlwaysResize: " + scale);
            } else {
                System.out.println("No DPI scaling detected (or unable to detect)");
            }

            // Java version can affect DPI awareness
            System.out.println("Java version: " + System.getProperty("java.version"));
            System.out.println("OS: " + System.getProperty("os.name"));

        } catch (Exception e) {
            System.out.println("Error detecting DPI: " + e.getMessage());
        }
    }

    private void createTestPattern(File patternFile) {
        try {
            // Create a simple test pattern
            BufferedImage testImg = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = testImg.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 100, 50);
            g.setColor(Color.BLACK);
            g.drawString("TEST", 30, 30);
            g.dispose();

            // Ensure directory exists
            patternFile.getParentFile().mkdirs();

            // Save the pattern
            ImageIO.write(testImg, "png", patternFile);
            System.out.println("Created test pattern: " + patternFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("Failed to create test pattern: " + e.getMessage());
        }
    }

    private String getImageType(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR";
            default:
                return "Type " + type;
        }
    }
}
