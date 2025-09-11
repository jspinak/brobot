package io.github.jspinak.brobot.debug;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive diagnostic test to understand why patterns don't match despite SikuliX IDE showing
 * 0.99 similarity
 */
@DisabledInCI
public class PatternMatchingDiagnosticTest extends DebugTestBase {

    @Test
    public void diagnosePatternMatching() throws Exception {
        System.out.println("=== PATTERN MATCHING DIAGNOSTIC TEST ===\n");

        // Test patterns
        String[] patterns = {
            "images/prompt/claude-prompt-1.png", "images/working/claude-icon-1.png"
        };

        // Give user time to set up screen
        System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
        System.out.println("Make sure the screen shows what SikuliX IDE sees!");
        System.out.println("You have 5 seconds...");
        Thread.sleep(5000);

        // Initialize screen
        Screen screen = new Screen();
        System.out.println("Screen bounds: " + screen.getBounds());
        System.out.println("Screen size: " + screen.w + "x" + screen.h);

        // Capture current screen
        ScreenImage screenCapture = screen.capture();
        BufferedImage screenImage = screenCapture.getImage();
        System.out.println(
                "\nScreen captured: " + screenImage.getWidth() + "x" + screenImage.getHeight());
        System.out.println("Screen image type: " + getImageTypeDetails(screenImage.getType()));

        // Save screen capture for analysis
        File outputDir = new File("pattern-diagnostic");
        outputDir.mkdirs();
        File screenFile = new File(outputDir, "screen_capture.png");
        ImageIO.write(screenImage, "png", screenFile);
        System.out.println("Screen saved to: " + screenFile.getPath());

        // Test each pattern
        for (String patternPath : patterns) {
            System.out.println("\n=== TESTING PATTERN: " + patternPath + " ===");

            // Load pattern image
            File patternFile = new File(patternPath);
            if (!patternFile.exists()) {
                System.out.println("ERROR: Pattern file not found!");
                continue;
            }

            BufferedImage patternImage = ImageIO.read(patternFile);
            System.out.println(
                    "Pattern size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
            System.out.println("Pattern type: " + getImageTypeDetails(patternImage.getType()));

            // Method 1: Direct Finder with BufferedImage
            System.out.println("\nMethod 1: Direct Finder with BufferedImage");
            testWithFinder(screenImage, patternImage, "BufferedImage");

            // Method 2: Convert pattern to RGB (remove alpha)
            System.out.println("\nMethod 2: Pattern converted to RGB (no alpha)");
            BufferedImage patternRGB =
                    new BufferedImage(
                            patternImage.getWidth(),
                            patternImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            patternRGB.getGraphics().drawImage(patternImage, 0, 0, null);
            testWithFinder(screenImage, patternRGB, "RGB Pattern");

            // Method 3: Convert screen to ARGB (add alpha)
            System.out.println("\nMethod 3: Screen converted to ARGB (with alpha)");
            BufferedImage screenARGB =
                    new BufferedImage(
                            screenImage.getWidth(),
                            screenImage.getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
            screenARGB.getGraphics().drawImage(screenImage, 0, 0, null);
            testWithFinder(screenARGB, patternImage, "ARGB Screen");

            // Method 4: Both converted to RGB
            System.out.println("\nMethod 4: Both converted to RGB");
            BufferedImage screenRGB =
                    new BufferedImage(
                            screenImage.getWidth(),
                            screenImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            screenRGB.getGraphics().drawImage(screenImage, 0, 0, null);
            testWithFinder(screenRGB, patternRGB, "Both RGB");

            // Method 5: Using SikuliX Pattern and Screen.find
            System.out.println("\nMethod 5: Using SikuliX Pattern and Screen.find");
            testWithScreenFind(screen, patternImage);

            // Method 6: Using file path directly
            System.out.println("\nMethod 6: Using file path directly");
            testWithFilePath(screen, patternPath);

            // Save pattern for comparison
            String patternName = new File(patternPath).getName().replace(".png", "");
            File patternOutputFile = new File(outputDir, patternName + "_original.png");
            ImageIO.write(patternImage, "png", patternOutputFile);

            File patternRGBFile = new File(outputDir, patternName + "_rgb.png");
            ImageIO.write(patternRGB, "png", patternRGBFile);

            System.out.println("\nPattern files saved to: " + outputDir.getPath());
        }

        // Test with different Settings
        System.out.println("\n=== TESTING WITH DIFFERENT SETTINGS ===");

        // Test with AlwaysResize = 0
        Settings.AlwaysResize = 0;
        System.out.println("\nTesting with AlwaysResize = 0");
        for (String patternPath : patterns) {
            System.out.println("Pattern: " + patternPath);
            BufferedImage patternImage = ImageIO.read(new File(patternPath));
            Pattern pattern = new Pattern(patternImage).similar(0.5);
            Match match = screen.exists(pattern, 0);
            if (match != null) {
                System.out.println("  FOUND with score: " + match.getScore());
            } else {
                System.out.println("  Not found");
            }
        }

        // Test with AlwaysResize = 1
        Settings.AlwaysResize = 1;
        System.out.println("\nTesting with AlwaysResize = 1");
        for (String patternPath : patterns) {
            System.out.println("Pattern: " + patternPath);
            BufferedImage patternImage = ImageIO.read(new File(patternPath));
            Pattern pattern = new Pattern(patternImage).similar(0.5);
            Match match = screen.exists(pattern, 0);
            if (match != null) {
                System.out.println("  FOUND with score: " + match.getScore());
            } else {
                System.out.println("  Not found");
            }
        }

        System.out.println("\n=== DIAGNOSTIC COMPLETE ===");
        System.out.println("Check the 'pattern-diagnostic' folder for saved images");
    }

    private void testWithFinder(
            BufferedImage searchImage, BufferedImage patternImage, String label) {
        try {
            Finder finder = new Finder(searchImage);
            Pattern pattern = new Pattern(patternImage);

            // Test at various thresholds
            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};

            for (double threshold : thresholds) {
                finder = new Finder(searchImage);
                Pattern p = pattern.similar(threshold);
                finder.findAll(p);

                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.printf(
                            "  %s at %.2f: FOUND with score %.3f at (%d, %d)\n",
                            label, threshold, match.getScore(), match.x, match.y);

                    // Check if there are multiple matches
                    int count = 1;
                    while (finder.hasNext()) {
                        finder.next();
                        count++;
                    }
                    if (count > 1) {
                        System.out.println("    (Total " + count + " matches found)");
                    }

                    finder.destroy();
                    return; // Found at this threshold, no need to test lower
                }
                finder.destroy();
            }

            System.out.println("  " + label + ": Not found at any threshold");

        } catch (Exception e) {
            System.out.println("  Error with " + label + ": " + e.getMessage());
        }
    }

    private void testWithScreenFind(Screen screen, BufferedImage patternImage) {
        try {
            Pattern pattern = new Pattern(patternImage);

            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};

            for (double threshold : thresholds) {
                Pattern p = pattern.similar(threshold);
                Match match = screen.exists(p, 0);

                if (match != null) {
                    System.out.printf(
                            "  Screen.exists at %.2f: FOUND with score %.3f at %s\n",
                            threshold, match.getScore(), match.getTarget());
                    return;
                }
            }

            System.out.println("  Screen.exists: Not found at any threshold");

        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void testWithFilePath(Screen screen, String patternPath) {
        try {
            // Try to load with file path directly
            Pattern pattern = new Pattern(patternPath);

            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};

            for (double threshold : thresholds) {
                Pattern p = pattern.similar(threshold);
                Match match = screen.exists(p, 0);

                if (match != null) {
                    System.out.printf(
                            "  File path at %.2f: FOUND with score %.3f at %s\n",
                            threshold, match.getScore(), match.getTarget());
                    return;
                }
            }

            System.out.println("  File path: Not found at any threshold");

        } catch (Exception e) {
            System.out.println("  Error with file path: " + e.getMessage());
        }
    }

    private String getImageTypeDetails(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB (1) - 24-bit RGB, no alpha";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB (2) - 32-bit ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE (3) - 32-bit ARGB, premultiplied";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR (4) - 24-bit BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR (5) - 24-bit BGR, 3 bytes";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR (6) - 32-bit ABGR, 4 bytes";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE (7) - 32-bit ABGR, premultiplied";
            default:
                return "Type " + type;
        }
    }
}
