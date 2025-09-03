package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test to capture the specific regions where patterns should be found
 * and compare them with the pattern images
 */
public class CaptureTargetRegionTest extends BrobotTestBase {
    
    @Test
    public void captureAndCompareTargetRegions() {
        System.out.println("=== CAPTURE TARGET REGIONS TEST ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("Make sure the Claude prompt is visible in the lower-left area!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Capturing screen now!\n");
            
            // Create output directory
            File outputDir = new File("target-region-analysis");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            // Capture full screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            BufferedImage fullScreen = screenCapture.getImage();
            System.out.println("Screen captured: " + fullScreen.getWidth() + "x" + fullScreen.getHeight());
            
            // Save full screen
            File fullScreenFile = new File(outputDir, "full_screen.png");
            ImageIO.write(fullScreen, "png", fullScreenFile);
            System.out.println("Saved full screen to: " + fullScreenFile.getName());
            
            // Define regions based on the PromptState configuration
            // Lower left quarter: x=0, y=432, w=768, h=432 (for 1536x864 screen)
            int screenWidth = fullScreen.getWidth();
            int screenHeight = fullScreen.getHeight();
            
            // Lower left quarter
            int lowerLeftX = 0;
            int lowerLeftY = screenHeight / 2;
            int lowerLeftW = screenWidth / 2;
            int lowerLeftH = screenHeight / 2;
            
            System.out.println("\n1. LOWER LEFT QUARTER (where prompt should be):");
            System.out.println("   Region: x=" + lowerLeftX + ", y=" + lowerLeftY + 
                              ", w=" + lowerLeftW + ", h=" + lowerLeftH);
            
            BufferedImage lowerLeftRegion = fullScreen.getSubimage(
                lowerLeftX, lowerLeftY, lowerLeftW, lowerLeftH);
            
            File lowerLeftFile = new File(outputDir, "lower_left_quarter.png");
            ImageIO.write(lowerLeftRegion, "png", lowerLeftFile);
            System.out.println("   Saved to: " + lowerLeftFile.getName());
            
            // Load and compare with prompt pattern
            File promptPatternFile = new File("images/prompt/claude-prompt-1.png");
            if (promptPatternFile.exists()) {
                BufferedImage promptPattern = ImageIO.read(promptPatternFile);
                System.out.println("\n2. PROMPT PATTERN:");
                System.out.println("   Size: " + promptPattern.getWidth() + "x" + promptPattern.getHeight());
                System.out.println("   Type: " + getImageTypeName(promptPattern.getType()));
                
                // Search for pattern in lower left region
                System.out.println("\n3. SEARCHING IN LOWER LEFT REGION:");
                Pattern pattern = new Pattern(promptPattern);
                Finder finder = new Finder(lowerLeftRegion);
                
                double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50, 0.40, 0.30};
                for (double threshold : thresholds) {
                    pattern = pattern.similar(threshold);
                    finder = new Finder(lowerLeftRegion);
                    finder.findAll(pattern);
                    
                    if (finder.hasNext()) {
                        Match match = finder.next();
                        System.out.println("   Threshold " + String.format("%.2f", threshold) + 
                            ": FOUND with score " + String.format("%.3f", match.getScore()) +
                            " at (" + match.x + ", " + match.y + ") in lower-left region");
                        
                        // Save the matched region
                        BufferedImage matchedRegion = lowerLeftRegion.getSubimage(
                            match.x, match.y, match.w, match.h);
                        File matchedFile = new File(outputDir, 
                            String.format("matched_in_lowerleft_t%.2f_s%.3f.png", 
                                threshold, match.getScore()));
                        ImageIO.write(matchedRegion, "png", matchedFile);
                        System.out.println("   Saved matched region to: " + matchedFile.getName());
                        
                        finder.destroy();
                        break;
                    }
                    finder.destroy();
                }
            }
            
            // Also capture some specific areas where UI elements might be
            System.out.println("\n4. CAPTURING SPECIFIC UI AREAS:");
            
            // Bottom area (taskbar/dock area)
            BufferedImage bottomStrip = fullScreen.getSubimage(
                0, screenHeight - 100, screenWidth, 100);
            File bottomFile = new File(outputDir, "bottom_strip.png");
            ImageIO.write(bottomStrip, "png", bottomFile);
            System.out.println("   Bottom strip saved to: " + bottomFile.getName());
            
            // Sample pixels from different areas
            System.out.println("\n5. PIXEL SAMPLING:");
            samplePixels(fullScreen, "Full screen center", screenWidth/2, screenHeight/2);
            samplePixels(fullScreen, "Lower left corner", 100, screenHeight - 100);
            samplePixels(fullScreen, "Lower left quarter center", lowerLeftW/2, lowerLeftY + lowerLeftH/2);
            
            // Try to find any text input areas
            System.out.println("\n6. SEARCHING FULL SCREEN WITH LOWER THRESHOLDS:");
            Pattern pattern = new Pattern(promptPatternFile.getPath());
            Finder fullScreenFinder = new Finder(fullScreen);
            
            for (double threshold = 0.5; threshold >= 0.1; threshold -= 0.1) {
                pattern = pattern.similar(threshold);
                fullScreenFinder = new Finder(fullScreen);
                fullScreenFinder.findAll(pattern);
                
                int count = 0;
                System.out.println("   Threshold " + String.format("%.1f", threshold) + ":");
                while (fullScreenFinder.hasNext()) {
                    Match match = fullScreenFinder.next();
                    count++;
                    if (count <= 3) {  // Show first 3 matches
                        System.out.println("     Match #" + count + ": score=" + 
                            String.format("%.3f", match.getScore()) + 
                            " at (" + match.x + ", " + match.y + ")");
                        
                        // Determine which quadrant
                        String quadrant = getQuadrant(match.x, match.y, screenWidth, screenHeight);
                        System.out.println("       Location: " + quadrant);
                    }
                }
                if (count > 3) {
                    System.out.println("     ... and " + (count - 3) + " more matches");
                }
                fullScreenFinder.destroy();
                
                if (count > 0) break;  // Stop after finding matches
            }
            
            System.out.println("\n=== Analysis complete. Check folder: " + outputDir.getAbsolutePath() + " ===");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getQuadrant(int x, int y, int screenWidth, int screenHeight) {
        String vertical = y < screenHeight / 2 ? "Upper" : "Lower";
        String horizontal = x < screenWidth / 2 ? "Left" : "Right";
        return vertical + "-" + horizontal + " quadrant";
    }
    
    private void samplePixels(BufferedImage img, String location, int x, int y) {
        int rgb = img.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        System.out.printf("   %s (%d,%d): RGB(%d,%d,%d) = #%02X%02X%02X\n", 
            location, x, y, r, g, b, r, g, b);
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB (Type 1)";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB (Type 2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "ARGB_PRE (Type 3)";
            case BufferedImage.TYPE_INT_BGR: return "BGR (Type 4)";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR (Type 5)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR (Type 6)";
            default: return "Type " + type;
        }
    }
}