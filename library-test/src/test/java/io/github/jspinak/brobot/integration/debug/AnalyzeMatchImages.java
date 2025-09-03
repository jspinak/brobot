package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
@Disabled("Missing ImageNormalizer utility class")
public class AnalyzeMatchImages {
    
    public static void main(String[] args) {
        SpringApplication.run(AnalyzeMatchImages.class, args);
    }
    
    @Bean
    CommandLineRunner runner(Object imageNormalizer // TODO: ImageNormalizer not available) {
        return args -> {
            System.out.println("\n=== Analyzing Claude Prompt Match Images ===\n");
            
            // Load the images from Windows path (via WSL mount)
            File matchFile = new File("/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/history/best-matches/20250810-190508_claude-prompt-3_sim0,387_match.png");
            File patternFile = new File("/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/history/best-matches/20250810-190508_claude-prompt-3_sim0,387_pattern.png");
            
            if (!matchFile.exists() || !patternFile.exists()) {
                System.err.println("Files not found!");
                System.err.println("Match exists: " + matchFile.exists());
                System.err.println("Pattern exists: " + patternFile.exists());
                return;
            }
            
            BufferedImage matchImg = ImageIO.read(matchFile);
            BufferedImage patternImg = ImageIO.read(patternFile);
            
            System.out.println("Match image dimensions: " + matchImg.getWidth() + "x" + matchImg.getHeight());
            System.out.println("Pattern image dimensions: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            
            // Analyze bit depth
            System.out.println("\nMatch image:");
            imageNormalizer.diagnoseImage(matchImg, "Match");
            
            System.out.println("\nPattern image:");
            imageNormalizer.diagnoseImage(patternImg, "Pattern");
            
            // Check if images have same dimensions
            if (matchImg.getWidth() == patternImg.getWidth() && 
                matchImg.getHeight() == patternImg.getHeight()) {
                System.out.println("\n✓ Images have the same dimensions");
                
                // Analyze pixel differences
                analyzePixelDifferences(matchImg, patternImg);
            } else {
                System.out.println("\n✗ Images have DIFFERENT dimensions!");
                
                // Calculate scale factor
                double scaleX = (double) matchImg.getWidth() / patternImg.getWidth();
                double scaleY = (double) matchImg.getHeight() / patternImg.getHeight();
                System.out.println("Scale factor X: " + String.format("%.3f", scaleX));
                System.out.println("Scale factor Y: " + String.format("%.3f", scaleY));
            }
            
            // Measure UI element sizes
            measureUIElements(matchImg, "Match");
            measureUIElements(patternImg, "Pattern");
            
            // Try to detect if there's a rendering scale difference
            detectRenderingScale(matchImg, patternImg);
            
            System.out.println("\n=== Analysis Complete ===\n");
        };
    }
    
    private static void analyzePixelDifferences(BufferedImage img1, BufferedImage img2) {
        System.out.println("\n--- Pixel Analysis ---");
        
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        
        int totalDiff = 0;
        int maxDiff = 0;
        int significantDiffs = 0; // Pixels with >10% difference
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
                maxDiff = Math.max(maxDiff, diff);
                
                if (diff > 76) { // 76 = 10% of max possible diff (765)
                    significantDiffs++;
                }
            }
        }
        
        double avgDiff = (double) totalDiff / (width * height * 3);
        double diffPercent = (double) significantDiffs * 100 / (width * height);
        
        System.out.println("Average pixel difference: " + String.format("%.2f", avgDiff));
        System.out.println("Maximum pixel difference: " + maxDiff + "/765");
        System.out.println("Pixels with significant difference: " + 
            String.format("%.1f%%", diffPercent));
    }
    
    private static void measureUIElements(BufferedImage img, String label) {
        System.out.println("\n--- " + label + " UI Element Measurements ---");
        
        // Find the prompt box (bright rectangle)
        int boxTop = -1, boxBottom = -1, boxLeft = -1, boxRight = -1;
        
        // Scan for bright pixels to find the prompt box
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int brightness = ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
                
                // Look for bright pixels (prompt box border)
                if (brightness > 300) { // Threshold for bright pixels
                    if (boxTop == -1) boxTop = y;
                    boxBottom = y;
                    if (boxLeft == -1 || x < boxLeft) boxLeft = x;
                    if (x > boxRight) boxRight = x;
                }
            }
        }
        
        if (boxTop != -1) {
            int boxWidth = boxRight - boxLeft + 1;
            int boxHeight = boxBottom - boxTop + 1;
            System.out.println("Prompt box dimensions: " + boxWidth + "x" + boxHeight);
            System.out.println("Prompt box position: (" + boxLeft + "," + boxTop + ")");
            
            // Check aspect ratio
            double aspectRatio = (double) boxWidth / boxHeight;
            System.out.println("Aspect ratio: " + String.format("%.2f", aspectRatio));
        } else {
            System.out.println("Could not detect prompt box boundaries");
        }
        
        // Count dark vs bright pixels to understand content
        int darkPixels = 0;
        int brightPixels = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int brightness = ((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF);
                if (brightness < 100) darkPixels++;
                else if (brightness > 400) brightPixels++;
            }
        }
        
        System.out.println("Dark pixels: " + darkPixels);
        System.out.println("Bright pixels: " + brightPixels);
        double darkRatio = (double) darkPixels * 100 / (img.getWidth() * img.getHeight());
        System.out.println("Dark pixel ratio: " + String.format("%.1f%%", darkRatio));
    }
    
    private static void detectRenderingScale(BufferedImage match, BufferedImage pattern) {
        System.out.println("\n--- Rendering Scale Detection ---");
        
        // The images have the same pixel dimensions but different content size
        // This suggests a rendering scale issue
        
        // Check if one image appears to be a scaled version of the other
        // by looking at edge sharpness (scaled images often have softer edges)
        
        int matchEdges = countEdgePixels(match);
        int patternEdges = countEdgePixels(pattern);
        
        System.out.println("Edge pixels in match: " + matchEdges);
        System.out.println("Edge pixels in pattern: " + patternEdges);
        
        double edgeRatio = (double) matchEdges / patternEdges;
        System.out.println("Edge ratio (match/pattern): " + String.format("%.2f", edgeRatio));
        
        if (Math.abs(edgeRatio - 1.0) > 0.2) {
            System.out.println("\n⚠ WARNING: Significant edge difference detected!");
            System.out.println("This suggests the images were captured at different rendering scales.");
            System.out.println("The UI elements appear to be rendered at different sizes.");
            
            if (edgeRatio < 0.8) {
                System.out.println("The match image has fewer edges, suggesting it may be upscaled.");
            } else if (edgeRatio > 1.2) {
                System.out.println("The pattern image has fewer edges, suggesting it may be upscaled.");
            }
        }
    }
    
    private static int countEdgePixels(BufferedImage img) {
        int edges = 0;
        for (int y = 1; y < img.getHeight() - 1; y++) {
            for (int x = 1; x < img.getWidth() - 1; x++) {
                int center = img.getRGB(x, y) & 0xFF;
                int left = img.getRGB(x - 1, y) & 0xFF;
                int right = img.getRGB(x + 1, y) & 0xFF;
                int top = img.getRGB(x, y - 1) & 0xFF;
                int bottom = img.getRGB(x, y + 1) & 0xFF;
                
                int maxDiff = Math.max(
                    Math.max(Math.abs(center - left), Math.abs(center - right)),
                    Math.max(Math.abs(center - top), Math.abs(center - bottom))
                );
                
                if (maxDiff > 30) { // Edge threshold
                    edges++;
                }
            }
        }
        return edges;
    }
}