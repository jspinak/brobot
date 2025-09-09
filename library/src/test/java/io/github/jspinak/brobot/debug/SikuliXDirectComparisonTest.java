package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test to compare SikuliX pattern matching when using the same approach as the IDE
 */
@DisabledInCI
public class SikuliXDirectComparisonTest extends BrobotTestBase {
    
    @Test
    public void testDirectSikuliXPatternMatching() {
        System.out.println("=== SIKULIX DIRECT PATTERN MATCHING TEST ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds to make the target screen visible...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");
            
            // Set up SikuliX ImagePath
            org.sikuli.script.ImagePath.reset();
            org.sikuli.script.ImagePath.setBundlePath("images");
            org.sikuli.script.ImagePath.add("images");
            org.sikuli.script.ImagePath.add("images/prompt");
            org.sikuli.script.ImagePath.add("images/working");
            System.out.println("Configured ImagePath: " + org.sikuli.script.ImagePath.getPaths());
            
            // Test with different pattern loading methods
            String[] patternPaths = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patternPaths) {
                System.out.println("\n--- Testing pattern: " + patternPath + " ---");
                
                // Method 1: Load pattern using file path (like SikuliX IDE)
                testWithFilePath(patternPath);
                
                // Method 2: Load pattern using BufferedImage (like Brobot currently does)
                testWithBufferedImage(patternPath);
                
                // Method 3: Use SikuliX Pattern constructor directly with path
                testWithSikuliXPattern(patternPath);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testWithFilePath(String patternPath) {
        System.out.println("\n1. USING FILE PATH (like SikuliX IDE):");
        try {
            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            
            // Create pattern from file path
            Pattern pattern = new Pattern(patternPath);
            
            // Check current MinSimilarity
            double currentMinSim = org.sikuli.basics.Settings.MinSimilarity;
            System.out.println("   Current MinSimilarity: " + currentMinSim);
            
            // Create Finder and search
            Finder finder = new Finder(screenCapture.getImage());
            
            // Test with different similarity thresholds
            double[] thresholds = {0.99, 0.95, 0.90, 0.80, 0.70, 0.60, 0.50};
            
            for (double threshold : thresholds) {
                pattern = pattern.similar(threshold);
                finder = new Finder(screenCapture.getImage());
                finder.findAll(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("   Threshold " + threshold + ": FOUND with score " + 
                        String.format("%.3f", match.getScore()));
                    finder.destroy();
                    break;
                } else {
                    System.out.println("   Threshold " + threshold + ": No match");
                }
                finder.destroy();
            }
            
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
        }
    }
    
    private void testWithBufferedImage(String patternPath) {
        System.out.println("\n2. USING BUFFEREDIMAGE (like Brobot):");
        try {
            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            
            // Load BufferedImage
            File file = new File(patternPath);
            BufferedImage patternImage = ImageIO.read(file);
            System.out.println("   Loaded image: " + patternImage.getWidth() + "x" + 
                patternImage.getHeight() + " type=" + getImageTypeName(patternImage.getType()));
            
            // Create pattern from BufferedImage
            Pattern pattern = new Pattern(patternImage);
            
            // Create Finder and search
            Finder finder = new Finder(screenCapture.getImage());
            
            // Test with different similarity thresholds
            double[] thresholds = {0.99, 0.95, 0.90, 0.80, 0.70, 0.60, 0.50};
            
            for (double threshold : thresholds) {
                pattern = pattern.similar(threshold);
                finder = new Finder(screenCapture.getImage());
                finder.findAll(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("   Threshold " + threshold + ": FOUND with score " + 
                        String.format("%.3f", match.getScore()));
                    finder.destroy();
                    break;
                } else {
                    System.out.println("   Threshold " + threshold + ": No match");
                }
                finder.destroy();
            }
            
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
        }
    }
    
    private void testWithSikuliXPattern(String patternPath) {
        System.out.println("\n3. USING SIKULIX PATTERN CLASS:");
        try {
            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenCapture = screen.capture();
            
            // Create SikuliX Pattern directly
            Pattern pattern = new Pattern(patternPath);
            
            // Get the internal BufferedImage to check what SikuliX loaded
            BufferedImage internalImage = pattern.getBImage();
            if (internalImage != null) {
                System.out.println("   SikuliX loaded image: " + internalImage.getWidth() + "x" + 
                    internalImage.getHeight() + " type=" + getImageTypeName(internalImage.getType()));
            }
            
            // Create Finder and search
            Finder finder = new Finder(screenCapture.getImage());
            
            // Test with different similarity thresholds
            double[] thresholds = {0.99, 0.95, 0.90, 0.80, 0.70, 0.60, 0.50};
            
            for (double threshold : thresholds) {
                pattern = pattern.similar(threshold);
                finder = new Finder(screenCapture.getImage());
                finder.findAll(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("   Threshold " + threshold + ": FOUND with score " + 
                        String.format("%.3f", match.getScore()));
                    
                    // Show location for verification
                    System.out.println("   Location: (" + match.x + ", " + match.y + ")");
                    finder.destroy();
                    break;
                } else {
                    System.out.println("   Threshold " + threshold + ": No match");
                }
                finder.destroy();
            }
            
        } catch (Exception e) {
            System.out.println("   ERROR: " + e.getMessage());
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB (Type 1)";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB (Type 2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "ARGB_PRE (Type 3)";
            case BufferedImage.TYPE_INT_BGR: return "BGR (Type 4)";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR (Type 5)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR (Type 6)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE (Type 7)";
            default: return "Type " + type;
        }
    }
    
    @Test 
    public void compareImageLoadingMethods() {
        System.out.println("\n=== COMPARE IMAGE LOADING METHODS ===\n");
        
        String testPattern = "images/prompt/claude-prompt-1.png";
        
        try {
            // Method 1: Direct file read with ImageIO
            File file = new File(testPattern);
            BufferedImage imageIOImage = ImageIO.read(file);
            System.out.println("1. ImageIO.read():");
            System.out.println("   Size: " + imageIOImage.getWidth() + "x" + imageIOImage.getHeight());
            System.out.println("   Type: " + getImageTypeName(imageIOImage.getType()));
            System.out.println("   Has alpha: " + imageIOImage.getColorModel().hasAlpha());
            
            // Method 2: SikuliX Pattern loading
            Pattern pattern = new Pattern(testPattern);
            BufferedImage sikuliImage = pattern.getBImage();
            System.out.println("\n2. SikuliX Pattern(path):");
            System.out.println("   Size: " + sikuliImage.getWidth() + "x" + sikuliImage.getHeight());
            System.out.println("   Type: " + getImageTypeName(sikuliImage.getType()));
            System.out.println("   Has alpha: " + sikuliImage.getColorModel().hasAlpha());
            
            // Method 3: SikuliX Pattern from BufferedImage
            Pattern patternFromBI = new Pattern(imageIOImage);
            BufferedImage sikuliFromBI = patternFromBI.getBImage();
            System.out.println("\n3. SikuliX Pattern(BufferedImage):");
            System.out.println("   Size: " + sikuliFromBI.getWidth() + "x" + sikuliFromBI.getHeight());
            System.out.println("   Type: " + getImageTypeName(sikuliFromBI.getType()));
            System.out.println("   Has alpha: " + sikuliFromBI.getColorModel().hasAlpha());
            
            // Compare pixel values
            System.out.println("\n4. Pixel comparison (center pixel):");
            int centerX = imageIOImage.getWidth() / 2;
            int centerY = imageIOImage.getHeight() / 2;
            
            int pixel1 = imageIOImage.getRGB(centerX, centerY);
            int pixel2 = sikuliImage.getRGB(centerX, centerY);
            int pixel3 = sikuliFromBI.getRGB(centerX, centerY);
            
            System.out.println("   ImageIO pixel: " + String.format("0x%08X", pixel1));
            System.out.println("   SikuliX(path) pixel: " + String.format("0x%08X", pixel2));
            System.out.println("   SikuliX(BufferedImage) pixel: " + String.format("0x%08X", pixel3));
            
            if (pixel1 == pixel2 && pixel2 == pixel3) {
                System.out.println("   ✓ All pixels match!");
            } else {
                System.out.println("   ✗ Pixels differ!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}