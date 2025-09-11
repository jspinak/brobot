package io.github.jspinak.brobot.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/** Analyze the saved comparison images from previous tests */
@DisabledInCI
public class AnalyzeSavedComparisonTest extends BrobotTestBase {

    @Test
    public void analyzeSavedImages() {
        System.out.println("=== ANALYZING SAVED COMPARISON IMAGES ===\n");

        // Check multiple possible locations
        String[] possibleDirs = {
            "matched-regions-comparison",
            "C:\\Users\\jspin\\Documents\\brobot_parent\\claude-automator\\matched-regions-comparison",
            "pattern-diagnostic",
            "screen-capture-comparison",
            "claude-prompt-analysis",
            "scaling-analysis"
        };

        for (String dirPath : possibleDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                System.out.println("Found directory: " + dir.getAbsolutePath());
                analyzeDirectory(dir);
            }
        }
    }

    private void analyzeDirectory(File dir) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Analyzing: " + dir.getName());
        System.out.println("=".repeat(60));

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0) {
            System.out.println("No PNG files found in directory");
            return;
        }

        // Sort files by name
        Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));

        // Group files by type
        File originalPattern = null;
        File matchedRegion = null;
        File diffImage = null;

        for (File file : files) {
            String name = file.getName().toLowerCase();

            if (name.contains("original") && name.contains("claude-prompt-1")) {
                originalPattern = file;
            } else if (name.contains("matched") && name.contains("claude-prompt-1")) {
                matchedRegion = file;
            } else if (name.contains("diff") && name.contains("claude-prompt-1")) {
                diffImage = file;
            }

            // Analyze each file
            System.out.println("\nFile: " + file.getName());
            System.out.println("  Size: " + file.length() + " bytes");

            try {
                BufferedImage img = ImageIO.read(file);
                System.out.println("  Dimensions: " + img.getWidth() + "x" + img.getHeight());
                System.out.println("  Type: " + getImageTypeName(img.getType()));

                // Sample some pixels for patterns
                if (name.contains("claude-prompt")) {
                    samplePixels(img, "  ");
                }

            } catch (Exception e) {
                System.out.println("  Error reading image: " + e.getMessage());
            }
        }

        // If we have both original and matched, compare them
        if (originalPattern != null && matchedRegion != null) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("DETAILED COMPARISON: Original vs Matched");
            System.out.println("-".repeat(60));

            try {
                BufferedImage original = ImageIO.read(originalPattern);
                BufferedImage matched = ImageIO.read(matchedRegion);

                System.out.println("\nOriginal Pattern: " + originalPattern.getName());
                System.out.println("  Size: " + original.getWidth() + "x" + original.getHeight());
                System.out.println("  File size: " + originalPattern.length() + " bytes");

                System.out.println("\nMatched Region: " + matchedRegion.getName());
                System.out.println("  Size: " + matched.getWidth() + "x" + matched.getHeight());
                System.out.println("  File size: " + matchedRegion.length() + " bytes");

                // Check if dimensions match
                if (original.getWidth() == matched.getWidth()
                        && original.getHeight() == matched.getHeight()) {

                    System.out.println("\n✓ Dimensions match!");

                    // Detailed pixel comparison
                    compareImages(original, matched);

                    // Check for scaling artifacts
                    checkForScalingArtifacts(original, matched);

                } else {
                    System.out.println("\n✗ Dimensions DO NOT match!");
                    System.out.println("  This indicates a scaling or cropping issue");

                    // Calculate scale factor
                    double scaleX = (double) matched.getWidth() / original.getWidth();
                    double scaleY = (double) matched.getHeight() / original.getHeight();
                    System.out.printf("  Scale factor: X=%.3f, Y=%.3f\n", scaleX, scaleY);
                }

            } catch (Exception e) {
                System.out.println("Error comparing images: " + e.getMessage());
            }
        }

        // Analyze difference image if available
        if (diffImage != null) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("DIFFERENCE IMAGE ANALYSIS");
            System.out.println("-".repeat(60));

            try {
                BufferedImage diff = ImageIO.read(diffImage);
                analyzeDifferenceImage(diff);
            } catch (Exception e) {
                System.out.println("Error analyzing difference image: " + e.getMessage());
            }
        }
    }

    private void compareImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int totalPixels = width * height;

        int identicalPixels = 0;
        int slightlyDifferent = 0; // < 10 total difference
        int moderatelyDifferent = 0; // 10-50 total difference
        int veryDifferent = 0; // > 50 total difference

        long totalDiff = 0;
        int maxDiff = 0;

        // Track specific issues
        int edgePixelsDifferent = 0;
        int centerPixelsDifferent = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                if (rgb1 == rgb2) {
                    identicalPixels++;
                } else {
                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;

                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;

                    int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                    totalDiff += diff;
                    maxDiff = Math.max(maxDiff, diff);

                    if (diff < 10) {
                        slightlyDifferent++;
                    } else if (diff < 50) {
                        moderatelyDifferent++;
                    } else {
                        veryDifferent++;
                    }

                    // Check if edge or center
                    boolean isEdge = (x < 2 || x >= width - 2 || y < 2 || y >= height - 2);
                    if (isEdge) {
                        edgePixelsDifferent++;
                    } else {
                        centerPixelsDifferent++;
                    }
                }
            }
        }

        System.out.println("\nPixel Comparison Results:");
        System.out.println("  Total pixels: " + totalPixels);
        System.out.println(
                "  Identical: "
                        + identicalPixels
                        + " ("
                        + String.format("%.1f%%", (identicalPixels * 100.0 / totalPixels))
                        + ")");
        System.out.println(
                "  Slightly different (diff < 10): "
                        + slightlyDifferent
                        + " ("
                        + String.format("%.1f%%", (slightlyDifferent * 100.0 / totalPixels))
                        + ")");
        System.out.println(
                "  Moderately different (10-50): "
                        + moderatelyDifferent
                        + " ("
                        + String.format("%.1f%%", (moderatelyDifferent * 100.0 / totalPixels))
                        + ")");
        System.out.println(
                "  Very different (> 50): "
                        + veryDifferent
                        + " ("
                        + String.format("%.1f%%", (veryDifferent * 100.0 / totalPixels))
                        + ")");

        System.out.println("\nDifference Statistics:");
        System.out.println("  Max pixel difference: " + maxDiff + "/765");
        if (totalPixels > identicalPixels) {
            System.out.println(
                    "  Average difference: "
                            + (totalDiff / (totalPixels - identicalPixels))
                            + "/765");
        }

        System.out.println("\nDifference Distribution:");
        System.out.println("  Edge pixels different: " + edgePixelsDifferent);
        System.out.println("  Center pixels different: " + centerPixelsDifferent);

        // Calculate similarity score estimate
        double similarity =
                (identicalPixels + slightlyDifferent * 0.9 + moderatelyDifferent * 0.5)
                        / totalPixels;
        System.out.printf("\nEstimated similarity: %.3f\n", similarity);

        if (Math.abs(similarity - 0.692) < 0.05) {
            System.out.println("  ✓ This matches the reported 0.692 similarity!");
        }
    }

    private void checkForScalingArtifacts(BufferedImage original, BufferedImage matched) {
        System.out.println("\nChecking for Scaling Artifacts:");

        // Check if the matched image looks like a scaled version
        // by examining edge transitions

        int sharpEdges = 0;
        int blurryEdges = 0;

        for (int y = 1; y < matched.getHeight() - 1; y++) {
            for (int x = 1; x < matched.getWidth() - 1; x++) {
                int center = matched.getRGB(x, y);
                int left = matched.getRGB(x - 1, y);
                int right = matched.getRGB(x + 1, y);
                int top = matched.getRGB(x, y - 1);
                int bottom = matched.getRGB(x, y + 1);

                // Check for sharp transitions
                int maxDiff = 0;
                maxDiff = Math.max(maxDiff, colorDiff(center, left));
                maxDiff = Math.max(maxDiff, colorDiff(center, right));
                maxDiff = Math.max(maxDiff, colorDiff(center, top));
                maxDiff = Math.max(maxDiff, colorDiff(center, bottom));

                if (maxDiff > 100) {
                    sharpEdges++;
                } else if (maxDiff > 30 && maxDiff < 60) {
                    blurryEdges++;
                }
            }
        }

        int totalEdgePixels = sharpEdges + blurryEdges;
        if (totalEdgePixels > 0) {
            double blurryRatio = (double) blurryEdges / totalEdgePixels;
            System.out.println("  Sharp edges: " + sharpEdges);
            System.out.println("  Blurry edges: " + blurryEdges);
            System.out.printf("  Blurry edge ratio: %.2f%%\n", blurryRatio * 100);

            if (blurryRatio > 0.3) {
                System.out.println("  ⚠ High blur ratio suggests image has been scaled!");
            }
        }
    }

    private void analyzeDifferenceImage(BufferedImage diff) {
        int width = diff.getWidth();
        int height = diff.getHeight();

        int grayPixels = 0;
        int coloredPixels = 0;
        int maxIntensity = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = diff.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (r == g && g == b) {
                    grayPixels++;
                } else {
                    coloredPixels++;
                    maxIntensity = Math.max(maxIntensity, Math.max(r, Math.max(g, b)));
                }
            }
        }

        System.out.println("\nDifference Image Analysis:");
        System.out.println("  Gray pixels (identical): " + grayPixels);
        System.out.println("  Colored pixels (different): " + coloredPixels);
        System.out.println("  Max difference intensity: " + maxIntensity);

        double diffRatio = (double) coloredPixels / (width * height);
        System.out.printf("  Difference ratio: %.1f%%\n", diffRatio * 100);
    }

    private void samplePixels(BufferedImage img, String indent) {
        int w = img.getWidth();
        int h = img.getHeight();

        System.out.println(indent + "Pixel samples:");

        // Sample center and corners
        int[][] points = {{0, 0}, {w - 1, 0}, {w / 2, h / 2}, {0, h - 1}, {w - 1, h - 1}};
        String[] labels = {"TL", "TR", "Center", "BL", "BR"};

        for (int i = 0; i < points.length; i++) {
            int x = points[i][0];
            int y = points[i][1];
            int rgb = img.getRGB(x, y);

            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            System.out.printf("%s    %s: RGB(%d,%d,%d)\n", indent, labels[i], r, g, b);
        }
    }

    private int colorDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }

    private String getImageTypeName(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB:
                return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB:
                return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_3BYTE_BGR:
                return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR:
                return "TYPE_4BYTE_ABGR";
            default:
                return "Type " + type;
        }
    }
}
