package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Focused similarity test using only claude-prompt-1.png
 * Compare with SikuliX IDE's 0.99 similarity score
 */
@DisabledInCI
public class ClaudePromptSimilarityTest extends BrobotTestBase {
    
    @Test
    public void analyzeClaudePromptSimilarity() {
        System.out.println("=== CLAUDE-PROMPT-1.PNG SIMILARITY ANALYSIS ===\n");
        
        try {
            // Give user time to set up screen
            System.out.println("!!! MAKE SURE THE CLAUDE PROMPT IS VISIBLE ON SCREEN !!!");
            System.out.println("Position your screen exactly as you do for SikuliX IDE testing");
            System.out.println("You have 5 seconds...");
            Thread.sleep(5000);
            
            // Pattern file
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                System.out.println("ERROR: Pattern file not found at: " + patternPath);
                return;
            }
            
            // Load pattern
            BufferedImage patternImage = ImageIO.read(patternFile);
            System.out.println("PATTERN INFO:");
            System.out.println("  File: " + patternPath);
            System.out.println("  Size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
            System.out.println("  Type: " + getImageTypeName(patternImage.getType()));
            System.out.println("  File size: " + patternFile.length() + " bytes");
            System.out.println("  MD5: " + calculateMD5(patternFile));
            
            // Initialize screen
            Screen screen = new Screen();
            System.out.println("\nSCREEN INFO:");
            System.out.println("  Bounds: " + screen.getBounds());
            System.out.println("  Size: " + screen.w + "x" + screen.h);
            
            // Capture screen
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            System.out.println("\nSCREEN CAPTURE:");
            System.out.println("  Size: " + screenImage.getWidth() + "x" + screenImage.getHeight());
            System.out.println("  Type: " + getImageTypeName(screenImage.getType()));
            
            // Create output directory
            File outputDir = new File("claude-prompt-analysis");
            outputDir.mkdirs();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            
            // Save screen capture
            File screenFile = new File(outputDir, "screen_" + timestamp + ".png");
            ImageIO.write(screenImage, "png", screenFile);
            System.out.println("  Saved to: " + screenFile.getName());
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("SIMILARITY MATCHING RESULTS:");
            System.out.println("=".repeat(60));
            
            // Test 1: Direct Finder with various thresholds
            System.out.println("\nTEST 1: Direct Finder Matching");
            System.out.println("-".repeat(40));
            testWithFinder(screenImage, patternImage);
            
            // Test 2: Screen.exists with various thresholds
            System.out.println("\nTEST 2: Screen.exists Matching");
            System.out.println("-".repeat(40));
            testWithScreenExists(screen, patternImage);
            
            // Test 3: With different Settings.AlwaysResize values
            System.out.println("\nTEST 3: Different AlwaysResize Settings");
            System.out.println("-".repeat(40));
            testWithDifferentResize(screen, patternImage);
            
            // Test 4: Find best match and extract region
            System.out.println("\nTEST 4: Best Match Analysis");
            System.out.println("-".repeat(40));
            Match bestMatch = findBestMatch(screenImage, patternImage);
            
            if (bestMatch != null) {
                System.out.println("Best match found:");
                System.out.println("  Score: " + String.format("%.3f", bestMatch.getScore()));
                System.out.println("  Location: (" + bestMatch.x + ", " + bestMatch.y + ")");
                System.out.println("  Size: " + bestMatch.w + "x" + bestMatch.h);
                
                // Extract matched region
                BufferedImage matchedRegion = screenImage.getSubimage(
                    bestMatch.x, bestMatch.y, bestMatch.w, bestMatch.h);
                
                // Save matched region
                File matchedFile = new File(outputDir, "matched_region_" + timestamp + ".png");
                ImageIO.write(matchedRegion, "png", matchedFile);
                System.out.println("  Matched region saved to: " + matchedFile.getName());
                
                // Detailed pixel comparison
                System.out.println("\nPIXEL-BY-PIXEL COMPARISON:");
                comparePixels(patternImage, matchedRegion);
                
                // Create visual comparison
                createVisualComparison(patternImage, matchedRegion, outputDir, timestamp);
            }
            
            // Test 5: Check if pattern needs to be scaled
            System.out.println("\nTEST 5: Scaled Pattern Matching");
            System.out.println("-".repeat(40));
            testScaledPatterns(screenImage, patternImage, outputDir);
            
            // Test 6: Compare with SikuliX IDE settings exactly
            System.out.println("\nTEST 6: Exact SikuliX IDE Settings");
            System.out.println("-".repeat(40));
            testWithIDESettings(screen, patternImage);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("ANALYSIS COMPLETE");
            System.out.println("=".repeat(60));
            System.out.println("\nSUMMARY:");
            System.out.println("  SikuliX IDE reports: 0.99 similarity");
            System.out.println("  Your best match: See results above");
            System.out.println("  Check folder: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testWithFinder(BufferedImage screenImage, BufferedImage patternImage) {
        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50};
        
        for (double threshold : thresholds) {
            Finder finder = new Finder(screenImage);
            Pattern pattern = new Pattern(patternImage).similar(threshold);
            finder.findAll(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("  Threshold %.2f: FOUND - Score: %.3f at (%d, %d)\n",
                    threshold, match.getScore(), match.x, match.y);
                
                // Count additional matches
                int count = 1;
                while (finder.hasNext()) {
                    finder.next();
                    count++;
                }
                if (count > 1) {
                    System.out.println("    (Total " + count + " matches found)");
                }
                
                finder.destroy();
                return; // Stop at first successful threshold
            }
            finder.destroy();
        }
        
        System.out.println("  No match found at any threshold!");
    }
    
    private void testWithScreenExists(Screen screen, BufferedImage patternImage) {
        double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60, 0.55, 0.50};
        
        for (double threshold : thresholds) {
            Pattern pattern = new Pattern(patternImage).similar(threshold);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.printf("  Threshold %.2f: FOUND - Score: %.3f at %s\n",
                    threshold, match.getScore(), match.getTarget());
                return; // Stop at first successful threshold
            }
        }
        
        System.out.println("  No match found at any threshold!");
    }
    
    private void testWithDifferentResize(Screen screen, BufferedImage patternImage) {
        float originalResize = Settings.AlwaysResize;
        float[] resizeValues = {0, 0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f};
        
        for (float resize : resizeValues) {
            Settings.AlwaysResize = resize;
            Pattern pattern = new Pattern(patternImage).similar(0.70);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.printf("  AlwaysResize=%.1f: FOUND - Score: %.3f\n",
                    resize, match.getScore());
            } else {
                System.out.printf("  AlwaysResize=%.1f: Not found\n", resize);
            }
        }
        
        Settings.AlwaysResize = originalResize;
    }
    
    private Match findBestMatch(BufferedImage screenImage, BufferedImage patternImage) {
        Finder finder = new Finder(screenImage);
        Pattern pattern = new Pattern(patternImage).similar(0.50);
        finder.findAll(pattern);
        
        Match bestMatch = null;
        double bestScore = 0;
        
        while (finder.hasNext()) {
            Match match = finder.next();
            if (match.getScore() > bestScore) {
                bestScore = match.getScore();
                bestMatch = match;
            }
        }
        
        finder.destroy();
        return bestMatch;
    }
    
    private void comparePixels(BufferedImage pattern, BufferedImage matched) {
        int width = pattern.getWidth();
        int height = pattern.getHeight();
        int totalPixels = width * height;
        int identicalPixels = 0;
        int similarPixels = 0; // Within 10 RGB values
        int differentPixels = 0;
        
        long totalDiff = 0;
        int maxDiff = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = pattern.getRGB(x, y);
                int rgb2 = matched.getRGB(x, y);
                
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
                    
                    if (diff <= 30) { // Within 10 per channel
                        similarPixels++;
                    } else {
                        differentPixels++;
                    }
                }
            }
        }
        
        System.out.println("  Total pixels: " + totalPixels);
        System.out.println("  Identical: " + identicalPixels + " (" + 
            String.format("%.1f%%", (identicalPixels * 100.0 / totalPixels)) + ")");
        System.out.println("  Similar: " + similarPixels + " (" + 
            String.format("%.1f%%", (similarPixels * 100.0 / totalPixels)) + ")");
        System.out.println("  Different: " + differentPixels + " (" + 
            String.format("%.1f%%", (differentPixels * 100.0 / totalPixels)) + ")");
        System.out.println("  Max pixel difference: " + maxDiff + "/765");
        if (identicalPixels < totalPixels) {
            System.out.println("  Average difference: " + 
                (totalDiff / (totalPixels - identicalPixels)) + "/765");
        }
    }
    
    private void createVisualComparison(BufferedImage pattern, BufferedImage matched, 
                                       File outputDir, String timestamp) {
        try {
            int width = pattern.getWidth();
            int height = pattern.getHeight();
            
            // Create difference image
            BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb1 = pattern.getRGB(x, y);
                    int rgb2 = matched.getRGB(x, y);
                    
                    if (rgb1 == rgb2) {
                        diffImage.setRGB(x, y, 0x808080); // Gray for identical
                    } else {
                        // Highlight differences in red
                        int r1 = (rgb1 >> 16) & 0xFF;
                        int g1 = (rgb1 >> 8) & 0xFF;
                        int b1 = rgb1 & 0xFF;
                        
                        int r2 = (rgb2 >> 16) & 0xFF;
                        int g2 = (rgb2 >> 8) & 0xFF;
                        int b2 = rgb2 & 0xFF;
                        
                        int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                        int intensity = Math.min(255, diff * 2);
                        diffImage.setRGB(x, y, (intensity << 16));
                    }
                }
            }
            
            File diffFile = new File(outputDir, "difference_" + timestamp + ".png");
            ImageIO.write(diffImage, "png", diffFile);
            System.out.println("  Difference image saved to: " + diffFile.getName());
            
            // Create side-by-side comparison
            BufferedImage comparison = new BufferedImage(width * 3, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = comparison.createGraphics();
            g.drawImage(pattern, 0, 0, null);
            g.drawImage(matched, width, 0, null);
            g.drawImage(diffImage, width * 2, 0, null);
            g.dispose();
            
            File compFile = new File(outputDir, "comparison_" + timestamp + ".png");
            ImageIO.write(comparison, "png", compFile);
            System.out.println("  Side-by-side comparison saved to: " + compFile.getName());
            
        } catch (Exception e) {
            System.out.println("  Error creating visual comparison: " + e.getMessage());
        }
    }
    
    private void testScaledPatterns(BufferedImage screenImage, BufferedImage patternImage, File outputDir) {
        double[] scales = {0.8, 0.9, 1.0, 1.1, 1.2, 1.25};
        
        for (double scale : scales) {
            if (scale == 1.0) continue; // Skip original
            
            int newWidth = (int)(patternImage.getWidth() * scale);
            int newHeight = (int)(patternImage.getHeight() * scale);
            
            BufferedImage scaledPattern = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = scaledPattern.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(patternImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            Finder finder = new Finder(screenImage);
            Pattern pattern = new Pattern(scaledPattern).similar(0.70);
            finder.find(pattern);
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.printf("  Scale %.1fx (%dx%d): Score: %.3f\n",
                    scale, newWidth, newHeight, match.getScore());
                
                // Save if significantly better
                if (match.getScore() > 0.90) {
                    try {
                        File scaledFile = new File(outputDir, 
                            String.format("pattern_scaled_%.1fx.png", scale));
                        ImageIO.write(scaledPattern, "png", scaledFile);
                        System.out.println("    High-scoring scaled pattern saved!");
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            } else {
                System.out.printf("  Scale %.1fx (%dx%d): Not found\n",
                    scale, newWidth, newHeight);
            }
            
            finder.destroy();
        }
    }
    
    private void testWithIDESettings(Screen screen, BufferedImage patternImage) {
        // Set exact IDE defaults
        Settings.MinSimilarity = 0.7;
        Settings.AlwaysResize = 1.0f;
        Settings.CheckLastSeen = true;
        
        System.out.println("  Settings: MinSimilarity=0.7, AlwaysResize=1.0, CheckLastSeen=true");
        
        Pattern pattern = new Pattern(patternImage).similar(0.99);
        Match match = screen.exists(pattern, 0);
        
        if (match != null) {
            System.out.println("  ✓ FOUND at 0.99 with IDE settings!");
            System.out.println("    Score: " + match.getScore());
            System.out.println("    Location: " + match.getTarget());
        } else {
            // Try lower thresholds
            for (double sim = 0.95; sim >= 0.50; sim -= 0.05) {
                pattern = new Pattern(patternImage).similar(sim);
                match = screen.exists(pattern, 0);
                
                if (match != null) {
                    System.out.printf("  Found at %.2f: Score: %.3f\n", sim, match.getScore());
                    break;
                }
            }
        }
    }
    
    private String calculateMD5(File file) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                while ((read = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, read);
                }
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB (24-bit)";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB (32-bit)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR (24-bit)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR (32-bit)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}