package io.github.jspinak.brobot.debug;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Replicates EXACTLY how the IDE takes screenshots to compare with Brobot. This mimics the code
 * from PatternWindow.takeScreenshot() in the IDE.
 */
@DisabledInCI
public class IDEScreenshotReplicationTest extends DebugTestBase {

    @Test
    public void captureIDEStyleScreenshot() throws Exception {
        System.out.println("=== IDE SCREENSHOT REPLICATION TEST ===\n");
        System.out.println("This test captures screenshots EXACTLY like the SikuliX IDE does.\n");

        // 1. Capture using ScreenUnion (IDE method)
        System.out.println("1. IDE METHOD (ScreenUnion):");
        ScreenImage ideScreenshot = captureIDEStyle();
        BufferedImage ideImage = ideScreenshot.getImage();
        saveAndAnalyze(ideImage, "IDE-style-screenshot.png");

        // 2. Capture using regular Screen (Brobot method)
        System.out.println("\n2. BROBOT METHOD (Screen):");
        Screen screen = new Screen();
        ScreenImage brobotScreenshot = screen.capture();
        BufferedImage brobotImage = brobotScreenshot.getImage();
        saveAndAnalyze(brobotImage, "Brobot-style-screenshot.png");

        // 3. Compare the two images
        System.out.println("\n3. COMPARISON:");
        compareImages(ideImage, brobotImage);

        // 4. Test pattern matching on both
        System.out.println("\n4. PATTERN MATCHING TEST:");
        testPatternMatching(ideScreenshot, brobotScreenshot);

        System.out.println("\n=== RESULTS ===");
        System.out.println("Check the saved files:");
        System.out.println("  - IDE-style-screenshot.png");
        System.out.println("  - Brobot-style-screenshot.png");
        System.out.println("  - screenshot-difference.png (if different)");
    }

    /** Captures screenshot EXACTLY like the IDE does in PatternWindow.takeScreenshot() */
    private ScreenImage captureIDEStyle() {
        // This is the EXACT code from PatternWindow.takeScreenshot()
        // minus the window hiding part

        try {
            // IDE waits 500ms after hiding windows
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // IDE uses: (new ScreenUnion()).getScreen().capture()
        // Since ScreenUnion is not in API, we'll use what it does internally:
        // ScreenUnion just calls Screen.getPrimaryScreen().capture()
        ScreenImage img = Screen.getPrimaryScreen().capture();

        return img;
    }

    private void saveAndAnalyze(BufferedImage image, String filename) {
        try {
            File file = new File(filename);
            ImageIO.write(image, "png", file);

            System.out.println("  Saved: " + filename);
            System.out.println("  Resolution: " + image.getWidth() + "x" + image.getHeight());
            System.out.println("  Type: " + getImageType(image.getType()));
            System.out.println("  File size: " + (file.length() / 1024) + " KB");

            // Check color depth
            int bitDepth = image.getColorModel().getPixelSize();
            System.out.println("  Bit depth: " + bitDepth);

            // Sample a few pixels to check quality
            checkPixelQuality(image);

        } catch (IOException e) {
            System.out.println("  Failed to save: " + e.getMessage());
        }
    }

    private void checkPixelQuality(BufferedImage image) {
        // Sample center and corners to check for compression artifacts
        int centerX = image.getWidth() / 2;
        int centerY = image.getHeight() / 2;

        // Check if we have smooth gradients or sharp pixels
        int variations = 0;
        int lastPixel = image.getRGB(centerX, centerY);

        for (int x = centerX - 10; x < centerX + 10; x++) {
            int pixel = image.getRGB(x, centerY);
            if (pixel != lastPixel) {
                variations++;
                lastPixel = pixel;
            }
        }

        System.out.println(
                "  Pixel variations in 20px line: "
                        + variations
                        + (variations > 15 ? " (sharp/detailed)" : " (smooth/compressed)"));
    }

    private void compareImages(BufferedImage img1, BufferedImage img2) {
        System.out.println("Comparing images...");

        // Check dimensions
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            System.out.println("  ✗ DIFFERENT DIMENSIONS!");
            System.out.println("    IDE: " + img1.getWidth() + "x" + img1.getHeight());
            System.out.println("    Brobot: " + img2.getWidth() + "x" + img2.getHeight());
            return;
        }

        System.out.println("  ✓ Same dimensions: " + img1.getWidth() + "x" + img1.getHeight());

        // Check image types
        if (img1.getType() != img2.getType()) {
            System.out.println("  ⚠ Different image types:");
            System.out.println("    IDE: " + getImageType(img1.getType()));
            System.out.println("    Brobot: " + getImageType(img2.getType()));
        }

        // Compare pixels
        int differences = 0;
        int maxDiff = 0;
        BufferedImage diffImage =
                new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                if (rgb1 != rgb2) {
                    differences++;

                    // Calculate difference magnitude
                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;

                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;

                    int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                    maxDiff = Math.max(maxDiff, diff);

                    // Highlight differences in red
                    diffImage.setRGB(x, y, 0xFF0000);
                } else {
                    diffImage.setRGB(x, y, 0x000000);
                }
            }
        }

        if (differences == 0) {
            System.out.println("  ✓ Images are IDENTICAL!");
        } else {
            double percentage = (100.0 * differences) / (img1.getWidth() * img1.getHeight());
            System.out.println(
                    "  ✗ Images differ at "
                            + differences
                            + " pixels ("
                            + String.format("%.2f%%", percentage)
                            + ")");
            System.out.println("  Maximum pixel difference: " + maxDiff + "/765");

            // Save difference image
            try {
                File diffFile = new File("screenshot-difference.png");
                ImageIO.write(diffImage, "png", diffFile);
                System.out.println("  Difference map saved to: " + diffFile.getName());
            } catch (IOException e) {
                System.out.println("  Failed to save difference map");
            }
        }
    }

    private void testPatternMatching(ScreenImage ideScreenshot, ScreenImage brobotScreenshot) {
        String patternPath = "images/prompt/claude-prompt-1.png";
        File patternFile = new File(patternPath);

        if (!patternFile.exists()) {
            System.out.println("Pattern file not found: " + patternPath);
            return;
        }

        try {
            // Test on IDE screenshot
            System.out.println("Testing pattern on IDE screenshot:");
            Finder ideFinder = new Finder(ideScreenshot);
            ideFinder.find(patternPath);

            if (ideFinder.hasNext()) {
                Match match = ideFinder.next();
                System.out.println("  ✓ Found! Score: " + String.format("%.3f", match.getScore()));
            } else {
                System.out.println("  ✗ Not found");
            }
            ideFinder.destroy();

            // Test on Brobot screenshot
            System.out.println("Testing pattern on Brobot screenshot:");
            Finder brobotFinder = new Finder(brobotScreenshot);
            brobotFinder.find(patternPath);

            if (brobotFinder.hasNext()) {
                Match match = brobotFinder.next();
                System.out.println("  ✓ Found! Score: " + String.format("%.3f", match.getScore()));
            } else {
                System.out.println("  ✗ Not found");
            }
            brobotFinder.destroy();

        } catch (Exception e) {
            System.out.println("Error in pattern matching: " + e.getMessage());
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
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_BYTE_BINARY:
                return "TYPE_BYTE_BINARY";
            case BufferedImage.TYPE_BYTE_GRAY:
                return "TYPE_BYTE_GRAY";
            case BufferedImage.TYPE_BYTE_INDEXED:
                return "TYPE_BYTE_INDEXED";
            default:
                return "Type " + type;
        }
    }

    @Test
    public void captureMultipleScreenshotsForComparison() throws Exception {
        System.out.println("=== MULTIPLE SCREENSHOT COMPARISON ===\n");

        for (int i = 0; i < 3; i++) {
            System.out.println("Capture " + (i + 1) + ":");

            // IDE style
            ScreenImage ideShot = captureIDEStyle();
            File ideFile = new File("IDE-screenshot-" + i + ".png");
            ImageIO.write(ideShot.getImage(), "png", ideFile);
            System.out.println(
                    "  IDE saved: "
                            + ideFile.getName()
                            + " ("
                            + ideShot.getImage().getWidth()
                            + "x"
                            + ideShot.getImage().getHeight()
                            + ")");

            // Brobot style
            Screen screen = new Screen();
            ScreenImage brobotShot = screen.capture();
            File brobotFile = new File("Brobot-screenshot-" + i + ".png");
            ImageIO.write(brobotShot.getImage(), "png", brobotFile);
            System.out.println(
                    "  Brobot saved: "
                            + brobotFile.getName()
                            + " ("
                            + brobotShot.getImage().getWidth()
                            + "x"
                            + brobotShot.getImage().getHeight()
                            + ")");

            Thread.sleep(1000); // Wait between captures
        }

        System.out.println("\nCompare the saved screenshots to see if there are any differences!");
    }
}
