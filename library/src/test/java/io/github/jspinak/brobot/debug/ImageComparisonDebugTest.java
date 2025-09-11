package io.github.jspinak.brobot.debug;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/** Deep dive into why patterns don't match even though they're the same files */
@DisabledInCI
public class ImageComparisonDebugTest extends BrobotTestBase {

    @Test
    public void debugImageComparison() {
        System.out.println("=== IMAGE COMPARISON DEBUG TEST ===\n");

        try {
            // Give user time to switch
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");

            // Test the prompt pattern since it should be easier to find
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);

            System.out.println("1. PATTERN ANALYSIS:");
            BufferedImage patternImg = ImageIO.read(patternFile);
            System.out.println("   Size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            System.out.println("   Type: " + getImageTypeDetails(patternImg.getType()));
            System.out.println("   Color Model: " + patternImg.getColorModel());
            System.out.println("   Has Alpha: " + patternImg.getColorModel().hasAlpha());
            System.out.println(
                    "   Num Components: " + patternImg.getColorModel().getNumComponents());
            System.out.println(
                    "   Num Color Components: "
                            + patternImg.getColorModel().getNumColorComponents());

            // Analyze pattern pixels
            analyzeImagePixels(patternImg, "Pattern");

            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImg = screenCapture.getImage();

            System.out.println("\n2. SCREEN ANALYSIS:");
            System.out.println("   Size: " + screenImg.getWidth() + "x" + screenImg.getHeight());
            System.out.println("   Type: " + getImageTypeDetails(screenImg.getType()));
            System.out.println("   Color Model: " + screenImg.getColorModel());
            System.out.println("   Has Alpha: " + screenImg.getColorModel().hasAlpha());

            // Analyze screen pixels in lower-left area where prompt should be
            int searchX = 0;
            int searchY = screenImg.getHeight() / 2;
            int searchW = screenImg.getWidth() / 2;
            int searchH = screenImg.getHeight() / 2;

            System.out.println("\n3. LOWER-LEFT REGION ANALYSIS:");
            System.out.println(
                    "   Region: (" + searchX + ", " + searchY + ") " + searchW + "x" + searchH);
            BufferedImage lowerLeft = screenImg.getSubimage(searchX, searchY, searchW, searchH);
            analyzeImagePixels(lowerLeft, "Lower-Left Region");

            // Try different image type conversions
            System.out.println("\n4. TESTING DIFFERENT IMAGE CONVERSIONS:");

            // Convert pattern to RGB (no alpha)
            System.out.println("\n   4a. Converting pattern to RGB (removing alpha):");
            BufferedImage patternRGB =
                    new BufferedImage(
                            patternImg.getWidth(),
                            patternImg.getHeight(),
                            BufferedImage.TYPE_INT_RGB);
            patternRGB.getGraphics().drawImage(patternImg, 0, 0, null);

            Pattern p1 = new Pattern(patternRGB).similar(0.50);
            Match m1 = screen.exists(p1, 0);
            if (m1 != null) {
                System.out.println("      ✓ FOUND with RGB conversion! Score: " + m1.getScore());
                System.out.println("        Location: " + m1.getTarget());
            } else {
                System.out.println("      ✗ Not found with RGB conversion");
            }

            // Convert to BGR
            System.out.println("\n   4b. Converting pattern to BGR:");
            BufferedImage patternBGR =
                    new BufferedImage(
                            patternImg.getWidth(),
                            patternImg.getHeight(),
                            BufferedImage.TYPE_3BYTE_BGR);
            patternBGR.getGraphics().drawImage(patternImg, 0, 0, null);

            Pattern p2 = new Pattern(patternBGR).similar(0.50);
            Match m2 = screen.exists(p2, 0);
            if (m2 != null) {
                System.out.println("      ✓ FOUND with BGR conversion! Score: " + m2.getScore());
                System.out.println("        Location: " + m2.getTarget());
            } else {
                System.out.println("      ✗ Not found with BGR conversion");
            }

            // Try with Settings.AlwaysResize = 0
            System.out.println("\n   4c. Testing with AlwaysResize = 0:");
            float oldResize = Settings.AlwaysResize;
            Settings.AlwaysResize = 0;

            Pattern p3 = new Pattern(patternImg).similar(0.50);
            Match m3 = screen.exists(p3, 0);
            if (m3 != null) {
                System.out.println("      ✓ FOUND with AlwaysResize=0! Score: " + m3.getScore());
                System.out.println("        Location: " + m3.getTarget());
            } else {
                System.out.println("      ✗ Not found with AlwaysResize=0");
            }

            Settings.AlwaysResize = oldResize;

            // Manual pixel-by-pixel search in lower-left
            System.out.println("\n5. MANUAL SEARCH FOR SIMILAR REGIONS:");
            searchForSimilarRegion(patternImg, lowerLeft);

            // Save debug images
            System.out.println("\n6. SAVING DEBUG IMAGES:");
            File debugDir = new File("image-comparison-debug");
            if (!debugDir.exists()) debugDir.mkdirs();

            // Save original pattern
            File origFile = new File(debugDir, "pattern_original.png");
            ImageIO.write(patternImg, "png", origFile);
            System.out.println("   Saved original pattern to: " + origFile.getName());

            // Save RGB converted pattern
            File rgbFile = new File(debugDir, "pattern_rgb_converted.png");
            ImageIO.write(patternRGB, "png", rgbFile);
            System.out.println("   Saved RGB pattern to: " + rgbFile.getName());

            // Save lower-left region
            File llFile = new File(debugDir, "screen_lower_left.png");
            ImageIO.write(lowerLeft, "png", llFile);
            System.out.println("   Saved lower-left region to: " + llFile.getName());

            // Save full screen
            File screenFile = new File(debugDir, "screen_full.png");
            ImageIO.write(screenImg, "png", screenFile);
            System.out.println("   Saved full screen to: " + screenFile.getName());

            System.out.println("\n7. CHECKING SIKULIX INTERNAL CONVERSION:");

            // See what SikuliX does internally when creating a pattern
            Pattern testPattern = new Pattern(patternPath);
            BufferedImage sikuliLoadedImg = testPattern.getBImage();
            if (sikuliLoadedImg != null) {
                System.out.println("   SikuliX loaded pattern:");
                System.out.println(
                        "     Size: "
                                + sikuliLoadedImg.getWidth()
                                + "x"
                                + sikuliLoadedImg.getHeight());
                System.out.println("     Type: " + getImageTypeDetails(sikuliLoadedImg.getType()));

                // Compare with original
                if (sikuliLoadedImg.getType() != patternImg.getType()) {
                    System.out.println("   ⚠ SikuliX converted the image type!");
                }

                // Save what SikuliX loaded
                File sikuliFile = new File(debugDir, "pattern_sikulix_loaded.png");
                ImageIO.write(sikuliLoadedImg, "png", sikuliFile);
                System.out.println("   Saved SikuliX-loaded pattern to: " + sikuliFile.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void analyzeImagePixels(BufferedImage img, String label) {
        System.out.println("\n   " + label + " Pixel Analysis:");

        // Sample corners and center
        int w = img.getWidth();
        int h = img.getHeight();

        int[][] samples = {{0, 0}, {w - 1, 0}, {0, h - 1}, {w - 1, h - 1}, {w / 2, h / 2}};
        String[] labels = {"Top-left", "Top-right", "Bottom-left", "Bottom-right", "Center"};

        for (int i = 0; i < samples.length; i++) {
            int x = samples[i][0];
            int y = samples[i][1];
            int rgb = img.getRGB(x, y);

            Color c = new Color(rgb, true);
            System.out.printf(
                    "     %s (%d,%d): R=%d G=%d B=%d A=%d (0x%08X)\n",
                    labels[i], x, y, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha(), rgb);
        }

        // Calculate average color
        long totalR = 0, totalG = 0, totalB = 0, totalA = 0;
        int sampleCount = Math.min(1000, w * h);
        int step = Math.max(1, (w * h) / sampleCount);

        for (int i = 0; i < w * h; i += step) {
            int x = i % w;
            int y = i / w;
            Color c = new Color(img.getRGB(x, y), true);
            totalR += c.getRed();
            totalG += c.getGreen();
            totalB += c.getBlue();
            totalA += c.getAlpha();
        }

        int samplesCounted = (w * h) / step;
        System.out.printf(
                "     Average color (sampled): R=%d G=%d B=%d A=%d\n",
                totalR / samplesCounted,
                totalG / samplesCounted,
                totalB / samplesCounted,
                totalA / samplesCounted);
    }

    private void searchForSimilarRegion(BufferedImage pattern, BufferedImage search) {
        System.out.println("   Searching for regions with similar color distribution...");

        int pw = pattern.getWidth();
        int ph = pattern.getHeight();
        int sw = search.getWidth();
        int sh = search.getHeight();

        if (pw > sw || ph > sh) {
            System.out.println("   Pattern larger than search region, skipping");
            return;
        }

        // Calculate pattern's average color
        long pR = 0, pG = 0, pB = 0;
        for (int y = 0; y < ph; y++) {
            for (int x = 0; x < pw; x++) {
                Color c = new Color(pattern.getRGB(x, y), true);
                pR += c.getRed();
                pG += c.getGreen();
                pB += c.getBlue();
            }
        }
        int pPixels = pw * ph;
        int avgPR = (int) (pR / pPixels);
        int avgPG = (int) (pG / pPixels);
        int avgPB = (int) (pB / pPixels);

        System.out.println(
                "   Pattern average color: RGB(" + avgPR + "," + avgPG + "," + avgPB + ")");

        // Search for similar regions (sample every 10 pixels for speed)
        int step = 10;
        int bestX = -1, bestY = -1;
        int minDiff = Integer.MAX_VALUE;

        for (int y = 0; y <= sh - ph; y += step) {
            for (int x = 0; x <= sw - pw; x += step) {
                // Calculate average color of this region
                long rR = 0, rG = 0, rB = 0;
                for (int dy = 0; dy < ph; dy += 2) { // Sample every other pixel for speed
                    for (int dx = 0; dx < pw; dx += 2) {
                        Color c = new Color(search.getRGB(x + dx, y + dy));
                        rR += c.getRed();
                        rG += c.getGreen();
                        rB += c.getBlue();
                    }
                }
                int rPixels = (pw / 2) * (ph / 2);
                int avgRR = (int) (rR / rPixels);
                int avgRG = (int) (rG / rPixels);
                int avgRB = (int) (rB / rPixels);

                // Calculate color difference
                int diff =
                        Math.abs(avgRR - avgPR) + Math.abs(avgRG - avgPG) + Math.abs(avgRB - avgPB);

                if (diff < minDiff) {
                    minDiff = diff;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        if (bestX >= 0) {
            System.out.println(
                    "   Best match at (" + bestX + "," + bestY + ") with color diff: " + minDiff);

            // Get actual color at that location
            long rR = 0, rG = 0, rB = 0;
            for (int dy = 0; dy < Math.min(ph, 5); dy++) {
                for (int dx = 0; dx < Math.min(pw, 5); dx++) {
                    Color c = new Color(search.getRGB(bestX + dx, bestY + dy));
                    rR += c.getRed();
                    rG += c.getGreen();
                    rB += c.getBlue();
                }
            }
            int samples = Math.min(ph, 5) * Math.min(pw, 5);
            System.out.println(
                    "   Region color: RGB("
                            + (rR / samples)
                            + ","
                            + (rG / samples)
                            + ","
                            + (rB / samples)
                            + ")");
        }
    }

    private String getImageTypeDetails(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB (1) - 8-bit RGB, no alpha";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB (2) - 8-bit ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return "TYPE_INT_ARGB_PRE (3) - 8-bit ARGB, premultiplied";
            case BufferedImage.TYPE_INT_BGR:
                return "TYPE_INT_BGR (4) - 8-bit BGR";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR (5) - 8-bit BGR, 3 bytes";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR (6) - 8-bit ABGR, 4 bytes";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return "TYPE_4BYTE_ABGR_PRE (7) - 8-bit ABGR, premultiplied";
            default:
                return "Type " + type;
        }
    }
}
