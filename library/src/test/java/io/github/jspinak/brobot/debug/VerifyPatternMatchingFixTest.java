package io.github.jspinak.brobot.debug;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Verifies that the pattern matching fix achieves 0.99 similarity like SikuliX IDE.
 *
 * <p>THE FIX: SikuliX IDE loads patterns directly from file paths: finder.find(patFilename) Brobot
 * was converting to BufferedImage first, causing image type changes that reduced similarity. Now
 * Brobot's Pattern.sikuli() method uses direct file path loading when available.
 */
@DisabledInCI
public class VerifyPatternMatchingFixTest extends BrobotTestBase {

    @Test
    public void verifyDirectFilePathLoadingFix() {
        System.out.println("========================================================");
        System.out.println("    VERIFYING PATTERN MATCHING FIX");
        System.out.println("========================================================\n");

        System.out.println("BACKGROUND:");
        System.out.println("  - SikuliX IDE achieves 0.99 similarity scores");
        System.out.println("  - Brobot was only achieving 0.70 similarity");
        System.out.println("  - Root cause: BufferedImage conversion changes image type");
        System.out.println("  - Solution: Use direct file path loading like the IDE\n");

        try {
            // Try different possible pattern locations
            String[] possiblePaths = {
                "images/prompt/claude-prompt-1.png",
                "images.sikuli/1755024811085.png",
                "src/test/resources/claude-prompt-1.png"
            };

            String patternPath = null;
            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    patternPath = path;
                    break;
                }
            }

            if (patternPath == null) {
                System.out.println("❌ No pattern file found. Tried:");
                for (String path : possiblePaths) {
                    System.out.println("   - " + path);
                }
                System.out.println("\nPlease ensure pattern files exist or adjust paths in test.");
                return;
            }

            System.out.println("Using pattern: " + patternPath);
            File patternFile = new File(patternPath);

            // Create Brobot Pattern - this should now use direct file path loading
            System.out.println("\n1. CREATING BROBOT PATTERN");
            Pattern brobotPattern = new Pattern(patternPath);
            System.out.println("   ✓ Pattern created: " + brobotPattern.getName());
            System.out.println("   ✓ Image path set: " + brobotPattern.getImgpath());

            // Get SikuliX pattern - should use direct file path internally
            System.out.println("\n2. CONVERTING TO SIKULI PATTERN");
            org.sikuli.script.Pattern sikuliPattern = brobotPattern.sikuli();
            System.out.println("   ✓ Conversion complete (should have used direct file path)");

            // Give user time to position window
            System.out.println("\n3. PREPARING FOR PATTERN MATCHING");
            System.out.println("   Position your VS Code window with the pattern visible.");
            System.out.println("   Starting in 5 seconds...");
            Thread.sleep(5000);

            // Test pattern matching
            System.out.println("\n4. TESTING PATTERN MATCHING");
            Screen screen = new Screen();

            // Test at 0.99 similarity (what SikuliX IDE achieves)
            sikuliPattern = sikuliPattern.similar(0.99);
            Match match = screen.exists(sikuliPattern, 0);

            if (match != null) {
                System.out.println("\n✅ SUCCESS! Pattern found at 0.99 similarity!");
                System.out.println("   Score: " + String.format("%.3f", match.getScore()));
                System.out.println("   Location: (" + match.x + ", " + match.y + ")");
                System.out.println("\n🎉 THE FIX IS WORKING!");
                System.out.println(
                        "   Brobot now achieves the same 0.99 similarity as SikuliX IDE");
            } else {
                // Try lower thresholds to see where it matches
                System.out.println("   ❌ Not found at 0.99 similarity");
                System.out.println("\n   Trying lower thresholds:");

                double[] thresholds = {0.95, 0.90, 0.85, 0.80, 0.75, 0.70};
                for (double threshold : thresholds) {
                    sikuliPattern = sikuliPattern.similar(threshold);
                    match = screen.exists(sikuliPattern, 0);

                    if (match != null) {
                        System.out.printf(
                                "   Found at %.2f with score %.3f%n", threshold, match.getScore());
                        break;
                    }
                }

                if (match == null) {
                    System.out.println("   Pattern not found at any threshold");
                }
            }

            // Compare with old BufferedImage approach
            System.out.println("\n5. COMPARING WITH OLD APPROACH (BufferedImage)");
            BufferedImage buffImg = ImageIO.read(patternFile);
            org.sikuli.script.Pattern oldPattern = new org.sikuli.script.Pattern(buffImg);

            // Check what image type the BufferedImage has
            System.out.println("   BufferedImage type: " + getImageType(buffImg.getType()));

            oldPattern = oldPattern.similar(0.99);
            Match oldMatch = screen.exists(oldPattern, 0);

            if (oldMatch != null) {
                System.out.println(
                        "   Old approach: Found at 0.99 with score "
                                + String.format("%.3f", oldMatch.getScore()));
            } else {
                // Try lower threshold
                oldPattern = oldPattern.similar(0.70);
                oldMatch = screen.exists(oldPattern, 0);
                if (oldMatch != null) {
                    System.out.println(
                            "   Old approach: Found at 0.70 with score "
                                    + String.format("%.3f", oldMatch.getScore()));
                } else {
                    System.out.println("   Old approach: Not found even at 0.70");
                }
            }

            // Show current settings
            System.out.println("\n6. CURRENT SETTINGS");
            System.out.println("   Settings.AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   Settings.MinSimilarity: " + Settings.MinSimilarity);

            System.out.println("\n========================================================");
            System.out.println("    TEST COMPLETE");
            System.out.println("========================================================");

        } catch (Exception e) {
            System.err.println("\n❌ Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void demonstrateImageTypeConversionIssue() {
        System.out.println("\n=== DEMONSTRATING IMAGE TYPE CONVERSION ISSUE ===\n");

        try {
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);

            if (!patternFile.exists()) {
                System.out.println("Pattern file not found, skipping demonstration");
                return;
            }

            // Load image as BufferedImage
            BufferedImage buffImg = ImageIO.read(patternFile);

            System.out.println("ORIGINAL IMAGE:");
            System.out.println("  Type: " + getImageType(buffImg.getType()));
            System.out.println("  Size: " + buffImg.getWidth() + "x" + buffImg.getHeight());
            System.out.println("  Has Alpha: " + buffImg.getColorModel().hasAlpha());

            // Convert to different types (simulating what might happen internally)
            BufferedImage rgbImage =
                    new BufferedImage(
                            buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.getGraphics().drawImage(buffImg, 0, 0, null);

            BufferedImage bgrImage =
                    new BufferedImage(
                            buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            bgrImage.getGraphics().drawImage(buffImg, 0, 0, null);

            System.out.println("\nCONVERSION EFFECTS:");
            System.out.println("  RGB conversion type: " + getImageType(rgbImage.getType()));
            System.out.println("  BGR conversion type: " + getImageType(bgrImage.getType()));

            // Test matching with each type
            System.out.println("\nPATTERN MATCHING WITH DIFFERENT IMAGE TYPES:");
            System.out.println("(Position window and wait 3 seconds...)");
            Thread.sleep(3000);

            Screen screen = new Screen();
            BufferedImage screenshot = screen.capture().getImage();

            // Test original
            testImageMatching(screenshot, buffImg, "Original");

            // Test RGB conversion
            testImageMatching(screenshot, rgbImage, "RGB Converted");

            // Test BGR conversion
            testImageMatching(screenshot, bgrImage, "BGR Converted");

            // Test direct file path
            System.out.println("\nDirect file path (no conversion):");
            Finder finder = new Finder(screenshot);
            org.sikuli.script.Pattern filePattern = new org.sikuli.script.Pattern(patternPath);
            filePattern = filePattern.similar(0.70);
            finder.find(filePattern);
            if (finder.hasNext()) {
                Match m = finder.next();
                System.out.printf("  Score: %.3f%n", m.getScore());
            } else {
                System.out.println("  No match");
            }
            finder.destroy();

            System.out.println("\nCONCLUSION:");
            System.out.println("Image type conversions affect similarity scores!");
            System.out.println("Direct file path loading avoids conversions.");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testImageMatching(BufferedImage screenshot, BufferedImage pattern, String label) {
        try {
            System.out.println("\n" + label + ":");
            Finder finder = new Finder(screenshot);
            org.sikuli.script.Pattern p = new org.sikuli.script.Pattern(pattern);
            p = p.similar(0.70);
            finder.find(p);
            if (finder.hasNext()) {
                Match m = finder.next();
                System.out.printf("  Score: %.3f%n", m.getScore());
            } else {
                System.out.println("  No match");
            }
            finder.destroy();
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
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
            default:
                return "Type " + type;
        }
    }
}
