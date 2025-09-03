package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Pattern;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Direct test to find the pattern in the Windows screenshot using SikuliX Finder.
 * This should work in WSL2 as it's just image processing, not screen capture.
 */
public class DirectPatternMatchTest extends BrobotTestBase {
    
    @Test
    public void testPatternMatching() throws IOException {
        System.out.println("=== DIRECT PATTERN MATCHING TEST ===");
        System.out.println("Testing pattern matching with Windows images from WSL2\n");
        
        // Access Windows files through WSL2 mount point
        String patternPath = "/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/debug_images/pattern_claude-prompt-3.png";
        String scenePath = "/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/debug_images/scene_current.png";
        
        File patternFile = new File(patternPath);
        File sceneFile = new File(scenePath);
        
        // Check if files exist
        System.out.println("Pattern file exists: " + patternFile.exists() + " - " + patternPath);
        System.out.println("Scene file exists: " + sceneFile.exists() + " - " + scenePath);
        
        if (!patternFile.exists() || !sceneFile.exists()) {
            System.err.println("\nERROR: One or both image files not found!");
            System.err.println("Make sure the Windows paths are accessible from WSL2");
            return;
        }
        
        // Load images
        BufferedImage patternImg = ImageIO.read(patternFile);
        BufferedImage sceneImg = ImageIO.read(sceneFile);
        
        System.out.println("\n=== IMAGE PROPERTIES ===");
        System.out.println("Pattern: " + patternImg.getWidth() + "x" + patternImg.getHeight() + 
                         " type=" + getImageTypeName(patternImg.getType()));
        System.out.println("Scene: " + sceneImg.getWidth() + "x" + sceneImg.getHeight() + 
                         " type=" + getImageTypeName(sceneImg.getType()));
        
        // Analyze if scene is black
        boolean sceneIsBlack = isImageBlack(sceneImg);
        if (sceneIsBlack) {
            System.out.println("\n⚠️ WARNING: Scene image appears to be all black!");
            System.out.println("This suggests the screenshot was taken in WSL2, which cannot capture Windows screens.");
            System.out.println("However, we can still test pattern matching if you provide a valid screenshot.\n");
        }
        
        System.out.println("\n=== TESTING PATTERN MATCHING AT DIFFERENT THRESHOLDS ===");
        
        // Test at different similarity thresholds
        double[] thresholds = {0.99, 0.95, 0.90, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30};
        
        for (double threshold : thresholds) {
            System.out.println("\n--- Testing at similarity: " + threshold + " ---");
            
            // Create Pattern with similarity threshold
            Pattern sikuliPattern = new Pattern(patternImg).similar(threshold);
            
            // Create Finder and search
            Finder finder = new Finder(sceneImg);
            finder.findAll(sikuliPattern);
            
            int count = 0;
            double bestScore = 0;
            Match bestMatch = null;
            
            while (finder.hasNext()) {
                Match match = finder.next();
                count++;
                
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                    bestMatch = match;
                }
                
                // Print first 3 matches
                if (count <= 3) {
                    System.out.printf("  Match %d: score=%.4f at (%d,%d) size=%dx%d\n",
                        count, match.getScore(), match.x, match.y, match.w, match.h);
                }
            }
            
            if (count == 0) {
                System.out.println("  No matches found at this threshold");
            } else {
                System.out.println("  Total matches: " + count);
                if (count > 3 && bestMatch != null) {
                    System.out.printf("  Best match: score=%.4f at (%d,%d)\n",
                        bestScore, bestMatch.x, bestMatch.y);
                }
            }
            
            finder.destroy();
            
            // If we found matches, we can stop
            if (count > 0) {
                System.out.println("\n✅ SUCCESS: Pattern found at similarity threshold " + threshold);
                System.out.println("Best match score: " + String.format("%.4f", bestScore));
                break;
            }
        }
        
        System.out.println("\n=== ADDITIONAL ANALYSIS ===");
        
        // Test with converted images (RGB conversion)
        System.out.println("\nTesting with RGB conversion (simulating Brobot's image processing):");
        BufferedImage patternRGB = convertToRGB(patternImg);
        BufferedImage sceneRGB = convertToRGB(sceneImg);
        
        Pattern rgbPattern = new Pattern(patternRGB).similar(0.7);
        Finder rgbFinder = new Finder(sceneRGB);
        rgbFinder.findAll(rgbPattern);
        
        int rgbCount = 0;
        double rgbBestScore = 0;
        while (rgbFinder.hasNext()) {
            Match match = rgbFinder.next();
            rgbCount++;
            if (match.getScore() > rgbBestScore) {
                rgbBestScore = match.getScore();
            }
        }
        
        System.out.println("RGB conversion results at 0.7 threshold:");
        System.out.println("  Matches found: " + rgbCount);
        if (rgbCount > 0) {
            System.out.println("  Best score: " + String.format("%.4f", rgbBestScore));
        }
        
        rgbFinder.destroy();
        
        System.out.println("\n=== SUMMARY ===");
        if (sceneIsBlack) {
            System.out.println("❌ Scene is all black - need a valid Windows screenshot");
            System.out.println("   Run the application on Windows and capture a new screenshot");
        } else {
            System.out.println("Scene appears valid - check matching results above");
        }
    }
    
    private static boolean isImageBlack(BufferedImage img) {
        int blackPixels = 0;
        int totalSamples = 100;
        
        for (int i = 0; i < totalSamples; i++) {
            int x = (img.getWidth() * i / totalSamples) % img.getWidth();
            int y = (img.getHeight() * i / totalSamples) % img.getHeight();
            
            int rgb = img.getRGB(x, y);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            if (r < 10 && g < 10 && b < 10) {
                blackPixels++;
            }
        }
        
        return blackPixels > totalSamples * 0.95; // 95% black pixels
    }
    
    private static BufferedImage convertToRGB(BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_INT_RGB) {
            return img;
        }
        
        BufferedImage rgbImage = new BufferedImage(
            img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        java.awt.Graphics2D g = rgbImage.createGraphics();
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();
        
        return rgbImage;
    }
    
    private static String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB (1)";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB (2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE (3)";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR (4)";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR (5)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR (6)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE (7)";
            case BufferedImage.TYPE_BYTE_GRAY: return "TYPE_BYTE_GRAY (10)";
            case BufferedImage.TYPE_BYTE_BINARY: return "TYPE_BYTE_BINARY (12)";
            case BufferedImage.TYPE_BYTE_INDEXED: return "TYPE_BYTE_INDEXED (13)";
            case BufferedImage.TYPE_USHORT_GRAY: return "TYPE_USHORT_GRAY (11)";
            case BufferedImage.TYPE_USHORT_565_RGB: return "TYPE_USHORT_565_RGB (8)";
            case BufferedImage.TYPE_USHORT_555_RGB: return "TYPE_USHORT_555_RGB (9)";
            case BufferedImage.TYPE_CUSTOM: return "TYPE_CUSTOM (0)";
            default: return "Unknown type: " + type;
        }
    }
}