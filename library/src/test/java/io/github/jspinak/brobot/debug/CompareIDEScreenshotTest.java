package io.github.jspinak.brobot.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;

/**
 * Test that compares different screenshot capture methods to understand why SikuliX IDE achieves
 * 0.99 similarity while Brobot gets 0.70.
 */
@DisabledInHeadlessEnvironment(
        "Screenshot comparison requires real display for capturing and comparing screenshots")
public class CompareIDEScreenshotTest extends BrobotTestBase {

    @Test
    public void compareScreenshotMethods() throws Exception {
        System.out.println("=== SCREENSHOT COMPARISON TEST ===\n");
        System.out.println(
                "Testing different screenshot capture methods to find the sharp one...\n");

        // Test 1: Basic Screen capture (what Brobot uses)
        System.out.println("1. BASIC SCREEN CAPTURE (Brobot method):");
        Screen screen = new Screen();
        ScreenImage screenImage = screen.capture();
        BufferedImage brobotCapture = screenImage.getImage();
        System.out.println(
                "   Resolution: " + brobotCapture.getWidth() + "x" + brobotCapture.getHeight());
        System.out.println("   Image type: " + getImageType(brobotCapture.getType()));
        saveAndAnalyze(brobotCapture, "brobot-capture.png");

        // Test 2: Primary Screen capture (IDE might use this)
        System.out.println("\n2. PRIMARY SCREEN CAPTURE:");
        Screen primary = Screen.getPrimaryScreen();
        ScreenImage primaryImage = primary.capture();
        BufferedImage primaryCapture = primaryImage.getImage();
        System.out.println(
                "   Resolution: " + primaryCapture.getWidth() + "x" + primaryCapture.getHeight());
        System.out.println("   Image type: " + getImageType(primaryCapture.getType()));
        saveAndAnalyze(primaryCapture, "primary-capture.png");

        // Test 3: Screen.all() capture
        System.out.println("\n3. SCREEN.ALL() CAPTURE:");
        Region all = Screen.all();
        ScreenImage allImage = all.getScreen().capture();
        BufferedImage allCapture = allImage.getImage();
        System.out.println(
                "   Resolution: " + allCapture.getWidth() + "x" + allCapture.getHeight());
        System.out.println("   Image type: " + getImageType(allCapture.getType()));
        saveAndAnalyze(allCapture, "all-capture.png");

        // Test 4: Check Settings.AlwaysResize
        System.out.println("\n4. SETTINGS CHECK:");
        System.out.println("   Settings.AlwaysResize = " + Settings.AlwaysResize);

        // Test 5: Pattern matching comparison
        System.out.println("\n5. PATTERN MATCHING COMPARISON:");
        testPatternOnScreenshots();

        System.out.println("\n=== RESULTS ===");
        System.out.println("Check the saved PNG files:");
        System.out.println("  - brobot-capture.png");
        System.out.println("  - primary-capture.png");
        System.out.println("  - all-capture.png");
        System.out.println("\nLook for differences in:");
        System.out.println("  1. Resolution (1920x1080 vs 1536x864)");
        System.out.println("  2. Sharpness (zoom in to check pixel clarity)");
        System.out.println("  3. File size (larger = more detail)");
    }

    private void testPatternOnScreenshots() {
        try {
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);

            if (!patternFile.exists()) {
                System.out.println("   Pattern not found: " + patternPath);
                return;
            }

            BufferedImage pattern = ImageIO.read(patternFile);
            System.out.println(
                    "   Pattern size: " + pattern.getWidth() + "x" + pattern.getHeight());

            // Test on each capture type
            Screen screen = new Screen();
            ScreenImage screenImage = screen.capture();
            testFinder(screenImage, patternPath, "Basic Screen");

            Screen primary = Screen.getPrimaryScreen();
            ScreenImage primaryImage = primary.capture();
            testFinder(primaryImage, patternPath, "Primary Screen");

            Region all = Screen.all();
            ScreenImage allImage = all.getScreen().capture();
            testFinder(allImage, patternPath, "Screen.all()");

        } catch (Exception e) {
            System.out.println("   Error: " + e.getMessage());
        }
    }

    private void testFinder(ScreenImage searchImage, String patternPath, String method) {
        try {
            System.out.println("\n   Testing with " + method + ":");

            // Test with default similarity (0.7)
            Region region = Region.create(0, 0, 1, 1);
            Finder finder = new Finder(searchImage, region);
            finder.find(patternPath);

            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("     ✓ Found at default similarity");
                System.out.println("       Score: " + match.getScore());
            } else {
                System.out.println("     ✗ Not found at default similarity");

                // Try with lower similarity
                finder = new Finder(searchImage, region);
                Pattern pattern = new Pattern(patternPath).similar(0.5);
                finder.find(pattern);

                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("     ✓ Found at 0.5 similarity");
                    System.out.println("       Score: " + match.getScore());
                } else {
                    System.out.println("     ✗ Not found even at 0.5 similarity");
                }
            }

            finder.destroy();

        } catch (Exception e) {
            System.out.println("     Error: " + e.getMessage());
        }
    }

    private void saveAndAnalyze(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);

            long fileSize = file.length();
            System.out.println("   Saved: " + filename);
            System.out.println("   File size: " + (fileSize / 1024) + " KB");

            // Check a small portion for blur
            checkSharpness(image);

        } catch (IOException e) {
            System.out.println("   Failed to save: " + e.getMessage());
        }
    }

    private void checkSharpness(BufferedImage image) {
        // Simple edge detection to check sharpness
        int edgeCount = 0;
        int sampleSize = Math.min(100, image.getWidth());

        for (int x = 1; x < sampleSize - 1; x++) {
            for (int y = 1; y < sampleSize - 1; y++) {
                int center = image.getRGB(x, y) & 0xFF;
                int left = image.getRGB(x - 1, y) & 0xFF;
                int right = image.getRGB(x + 1, y) & 0xFF;
                int top = image.getRGB(x, y - 1) & 0xFF;
                int bottom = image.getRGB(x, y + 1) & 0xFF;

                int maxDiff =
                        Math.max(
                                Math.max(Math.abs(center - left), Math.abs(center - right)),
                                Math.max(Math.abs(center - top), Math.abs(center - bottom)));

                if (maxDiff > 30) {
                    edgeCount++;
                }
            }
        }

        double sharpness = (double) edgeCount / (sampleSize * sampleSize) * 100;
        System.out.println(
                "   Sharpness indicator: "
                        + String.format("%.1f%%", sharpness)
                        + " (higher = sharper)");
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
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE";
            default:
                return "Type " + type;
        }
    }

    @Test
    public void testWithAlwaysResize() {
        System.out.println("=== TESTING WITH Settings.AlwaysResize ===\n");

        // Save original value
        float originalResize = Settings.AlwaysResize;

        try {
            // Test with different resize values
            double[] resizeValues = {1.0, 0.8, 1.25};

            for (double resize : resizeValues) {
                Settings.AlwaysResize = (float) resize;
                System.out.println("Testing with AlwaysResize = " + resize);

                Screen screen = new Screen();
                String patternPath = "images/prompt/claude-prompt-1.png";

                try {
                    Match match = screen.find(new Pattern(patternPath).similar(0.6));
                    System.out.println("  ✓ Found! Score: " + match.getScore());
                } catch (FindFailed e) {
                    System.out.println("  ✗ Not found");
                }
            }

        } finally {
            // Restore original value
            Settings.AlwaysResize = originalResize;
        }
    }
}
