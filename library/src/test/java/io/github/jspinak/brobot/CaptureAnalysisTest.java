package io.github.jspinak.brobot;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Analyzes captured images and patterns to debug matching issues.
 * This test doesn't require a display - it analyzes existing files.
 */
public class CaptureAnalysisTest extends BrobotTestBase {
    
    @Test
    public void analyzePatternMatching() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PATTERN MATCHING ANALYSIS");
        System.out.println("=".repeat(80));
        
        // Load the pattern image
        String patternPath = "images/claude-prompt.png";
        File patternFile = new File(patternPath);
        
        if (!patternFile.exists()) {
            System.out.println("Pattern file not found: " + patternPath);
            return;
        }
        
        try {
            // Load pattern
            BufferedImage patternImg = ImageIO.read(patternFile);
            System.out.println("\nPattern Image Analysis:");
            System.out.println("  Path: " + patternPath);
            System.out.println("  Size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
            System.out.println("  Type: " + getImageTypeName(patternImg.getType()));
            System.out.println("  Has Alpha: " + patternImg.getColorModel().hasAlpha());
            
            // Check for scaling artifacts
            analyzeImageContent(patternImg, "PATTERN");
            
            // Load a saved match or searched image if available
            File historyDir = new File("history/best-matches");
            if (historyDir.exists() && historyDir.isDirectory()) {
                File[] searchedImages = historyDir.listFiles((dir, name) -> 
                    name.contains("claude-prompt") && name.contains("searched") && name.endsWith(".png"));
                
                if (searchedImages != null && searchedImages.length > 0) {
                    File searchedFile = searchedImages[searchedImages.length - 1]; // Get most recent
                    BufferedImage searchedImg = ImageIO.read(searchedFile);
                    
                    System.out.println("\nSearched Image Analysis:");
                    System.out.println("  Path: " + searchedFile.getName());
                    System.out.println("  Size: " + searchedImg.getWidth() + "x" + searchedImg.getHeight());
                    System.out.println("  Type: " + getImageTypeName(searchedImg.getType()));
                    System.out.println("  Has Alpha: " + searchedImg.getColorModel().hasAlpha());
                    
                    analyzeImageContent(searchedImg, "SEARCHED");
                    
                    // Test pattern matching
                    testPatternMatch(searchedImg, patternImg);
                }
                
                // Also check for match images
                File[] matchImages = historyDir.listFiles((dir, name) -> 
                    name.contains("claude-prompt") && name.contains("match") && !name.contains("searched") && name.endsWith(".png"));
                
                if (matchImages != null && matchImages.length > 0) {
                    File matchFile = matchImages[matchImages.length - 1];
                    BufferedImage matchImg = ImageIO.read(matchFile);
                    
                    System.out.println("\nCaptured Match Analysis:");
                    System.out.println("  Path: " + matchFile.getName());
                    System.out.println("  Size: " + matchImg.getWidth() + "x" + matchImg.getHeight());
                    System.out.println("  Type: " + getImageTypeName(matchImg.getType()));
                    
                    analyzeImageContent(matchImg, "MATCH");
                    
                    // Compare pattern and match sizes
                    double widthRatio = (double)matchImg.getWidth() / patternImg.getWidth();
                    double heightRatio = (double)matchImg.getHeight() / patternImg.getHeight();
                    System.out.println("\nSize Comparison:");
                    System.out.println("  Width ratio (match/pattern): " + String.format("%.3f", widthRatio));
                    System.out.println("  Height ratio (match/pattern): " + String.format("%.3f", heightRatio));
                    
                    if (Math.abs(widthRatio - 1.0) > 0.05 || Math.abs(heightRatio - 1.0) > 0.05) {
                        System.out.println("  WARNING: Significant size difference detected!");
                        System.out.println("  This indicates a scaling issue during capture.");
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    private void testPatternMatch(BufferedImage scene, BufferedImage pattern) {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("TESTING PATTERN MATCHING");
        System.out.println("-".repeat(40));
        
        try {
            // Create SikuliX Pattern
            org.sikuli.script.Pattern sikuliPattern = new org.sikuli.script.Pattern(pattern);
            
            // Create Finder
            Finder finder = new Finder(scene);
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
            }
            
            System.out.println("Matches found: " + count);
            System.out.println("Best score: " + String.format("%.3f", bestScore));
            
            if (bestMatch != null) {
                System.out.println("Best match location: " + bestMatch.x + ", " + bestMatch.y);
                System.out.println("Best match size: " + bestMatch.w + "x" + bestMatch.h);
            }
            
            if (bestScore < 0.9) {
                System.out.println("\nWARNING: Low similarity score!");
                System.out.println("This confirms the pattern matching issue.");
            }
            
            finder.destroy();
            
        } catch (Exception e) {
            System.err.println("Error in pattern matching: " + e.getMessage());
        }
    }
    
    private void analyzeImageContent(BufferedImage img, String label) {
        System.out.println("\nContent Analysis - " + label + ":");
        
        // Sample pixels for analysis
        int samples = 100;
        int blackCount = 0, whiteCount = 0;
        long totalR = 0, totalG = 0, totalB = 0;
        
        for (int i = 0; i < samples; i++) {
            int x = (img.getWidth() * i) / samples;
            int y = img.getHeight() / 2;
            
            if (x < img.getWidth()) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                totalR += r;
                totalG += g;
                totalB += b;
                
                if (r < 10 && g < 10 && b < 10) blackCount++;
                if (r > 245 && g > 245 && b > 245) whiteCount++;
            }
        }
        
        System.out.println("  Black pixels: " + blackCount + "/" + samples);
        System.out.println("  White pixels: " + whiteCount + "/" + samples);
        System.out.println("  Avg RGB: (" + (totalR/samples) + ", " + (totalG/samples) + ", " + (totalB/samples) + ")");
        
        // Check edges for interpolation artifacts
        checkForScalingArtifacts(img);
    }
    
    private void checkForScalingArtifacts(BufferedImage img) {
        int edgeChanges = 0;
        int checks = Math.min(20, img.getWidth() - 1);
        
        for (int x = 0; x < checks; x++) {
            if (img.getHeight() > 1) {
                int rgb1 = img.getRGB(x, 0);
                int rgb2 = img.getRGB(x, 1);
                
                // Check for significant color changes at edges (scaling artifacts)
                int diff = Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
                if (diff > 30 && diff < 200) { // Not pure black/white edge
                    edgeChanges++;
                }
            }
        }
        
        if (edgeChanges > checks / 3) {
            System.out.println("  WARNING: Possible scaling artifacts detected at edges!");
        }
    }
    
    private String getImageTypeName(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            case BufferedImage.TYPE_BYTE_GRAY: return "GRAY";
            default: return "Type" + type;
        }
    }
}