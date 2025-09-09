package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Extract and save the matched regions to compare with pattern images
 */
@DisabledInCI
public class ExtractMatchedRegionTest extends BrobotTestBase {
    
    @Test
    public void extractMatchedRegions() {
        System.out.println("=== EXTRACT MATCHED REGIONS TEST ===\n");
        
        try {
            // Give user time to set up screen
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 3 seconds...");
            Thread.sleep(3000);
            
            // Create output directory
            File outputDir = new File("matched-regions-comparison");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            
            // Initialize screen
            Screen screen = new Screen();
            System.out.println("Screen: " + screen.getBounds());
            
            // Capture the full screen
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            System.out.println("Screen captured: " + screenImage.getWidth() + "x" + screenImage.getHeight());
            
            // Save full screen
            File screenFile = new File(outputDir, "full_screen_" + timestamp + ".png");
            ImageIO.write(screenImage, "png", screenFile);
            System.out.println("Full screen saved to: " + screenFile.getName());
            
            // Test patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patterns) {
                System.out.println("\n=== TESTING: " + patternPath + " ===");
                
                File patternFile = new File(patternPath);
                if (!patternFile.exists()) {
                    System.out.println("Pattern file not found!");
                    continue;
                }
                
                // Load pattern
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("Pattern size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
                System.out.println("Pattern file size: " + patternFile.length() + " bytes");
                
                // Save original pattern for comparison
                String patternName = patternFile.getName().replace(".png", "");
                File origPatternFile = new File(outputDir, patternName + "_ORIGINAL_" + timestamp + ".png");
                ImageIO.write(patternImage, "png", origPatternFile);
                System.out.println("Original pattern saved to: " + origPatternFile.getName());
                
                // Find pattern using Finder
                Finder finder = new Finder(screenImage);
                Pattern pattern = new Pattern(patternImage);
                
                // Try different thresholds
                double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50};
                
                for (double threshold : thresholds) {
                    finder = new Finder(screenImage);
                    Pattern p = pattern.similar(threshold);
                    finder.findAll(p);
                    
                    if (finder.hasNext()) {
                        Match match = finder.next();
                        System.out.println("\nFound at threshold " + threshold + ":");
                        System.out.println("  Score: " + String.format("%.3f", match.getScore()));
                        System.out.println("  Location: (" + match.x + ", " + match.y + ")");
                        System.out.println("  Size: " + match.w + "x" + match.h);
                        
                        // Extract the matched region from screen
                        BufferedImage matchedRegion = screenImage.getSubimage(
                            match.x, match.y, match.w, match.h);
                        
                        // Save matched region
                        String matchFileName = String.format("%s_MATCHED_score%.3f_%s.png", 
                            patternName, match.getScore(), timestamp);
                        File matchFile = new File(outputDir, matchFileName);
                        ImageIO.write(matchedRegion, "png", matchFile);
                        System.out.println("  Matched region saved to: " + matchFile.getName());
                        System.out.println("  Matched region file size: " + matchFile.length() + " bytes");
                        
                        // Compare pixel-by-pixel
                        compareImages(patternImage, matchedRegion);
                        
                        // Extract and save difference image
                        BufferedImage diffImage = createDifferenceImage(patternImage, matchedRegion);
                        File diffFile = new File(outputDir, patternName + "_DIFF_" + timestamp + ".png");
                        ImageIO.write(diffImage, "png", diffFile);
                        System.out.println("  Difference image saved to: " + diffFile.getName());
                        
                        // Also save with Screen.capture for that specific region
                        Rectangle matchRect = new Rectangle(match.x, match.y, match.w, match.h);
                        ScreenImage regionCapture = screen.capture(matchRect);
                        BufferedImage regionImage = regionCapture.getImage();
                        File regionFile = new File(outputDir, patternName + "_SCREEN_REGION_" + timestamp + ".png");
                        ImageIO.write(regionImage, "png", regionFile);
                        System.out.println("  Screen.capture region saved to: " + regionFile.getName());
                        
                        finder.destroy();
                        break; // Found the best match, no need to continue
                    }
                    finder.destroy();
                }
            }
            
            System.out.println("\n=== ANALYSIS COMPLETE ===");
            System.out.println("All images saved to: " + outputDir.getAbsolutePath());
            System.out.println("\nCompare these files:");
            System.out.println("  *_ORIGINAL_*.png - Your pattern images");
            System.out.println("  *_MATCHED_*.png - What was actually found on screen");
            System.out.println("  *_DIFF_*.png - Pixel differences highlighted");
            System.out.println("  *_SCREEN_REGION_*.png - Direct screen capture of the region");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void compareImages(BufferedImage img1, BufferedImage img2) {
        System.out.println("\n  Pixel comparison:");
        
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            System.out.println("    Images have different dimensions!");
            return;
        }
        
        int width = img1.getWidth();
        int height = img1.getHeight();
        int totalPixels = width * height;
        int differentPixels = 0;
        int maxDiff = 0;
        long totalDiff = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 != rgb2) {
                    differentPixels++;
                    
                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;
                    
                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;
                    
                    int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                    totalDiff += diff;
                    maxDiff = Math.max(maxDiff, diff);
                }
            }
        }
        
        System.out.println("    Total pixels: " + totalPixels);
        System.out.println("    Different pixels: " + differentPixels + " (" + 
            String.format("%.1f%%", (differentPixels * 100.0 / totalPixels)) + ")");
        System.out.println("    Max pixel difference: " + maxDiff + "/765");
        if (differentPixels > 0) {
            System.out.println("    Average difference: " + (totalDiff / differentPixels) + "/765");
        }
    }
    
    private BufferedImage createDifferenceImage(BufferedImage img1, BufferedImage img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        
        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 == rgb2) {
                    // Same pixel - show as gray
                    diff.setRGB(x, y, 0x808080);
                } else {
                    // Different pixel - show the difference amplified
                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;
                    
                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;
                    
                    // Amplify differences for visibility
                    int dr = Math.min(255, Math.abs(r1 - r2) * 3);
                    int dg = Math.min(255, Math.abs(g1 - g2) * 3);
                    int db = Math.min(255, Math.abs(b1 - b2) * 3);
                    
                    diff.setRGB(x, y, (dr << 16) | (dg << 8) | db);
                }
            }
        }
        
        return diff;
    }
}