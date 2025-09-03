package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.sikuli.script.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Compare screen captures from SikuliX vs Brobot to see if there's a difference
 */
public class ScreenCaptureComparisonTest extends BrobotTestBase {
    
    @Test
    public void compareScreenCaptures() {
        System.out.println("=== SCREEN CAPTURE COMPARISON: SIKULIX VS BROBOT ===\n");
        
        try {
            // Give user time to switch to target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("Make sure the screen shows what you want to match!");
            System.out.println("You have 3 seconds...");
            for (int i = 3; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Capturing screens now!\n");
            
            // Create output directory
            File outputDir = new File("screen-capture-comparison");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
            
            // 1. SIKULIX SCREEN CAPTURE
            System.out.println("1. SIKULIX SCREEN CAPTURE:");
            Screen sikuliScreen = new Screen();
            System.out.println("   SikuliX Screen bounds: " + sikuliScreen.getBounds());
            
            // Method 1a: SikuliX Screen.capture()
            ScreenImage sikuliCapture = sikuliScreen.capture();
            BufferedImage sikuliImage = sikuliCapture.getImage();
            System.out.println("   SikuliX captured: " + sikuliImage.getWidth() + "x" + sikuliImage.getHeight());
            System.out.println("   Image type: " + getImageTypeDetails(sikuliImage.getType()));
            System.out.println("   Has alpha: " + sikuliImage.getColorModel().hasAlpha());
            
            File sikuliFile = new File(outputDir, "sikulix_capture_" + timestamp + ".png");
            ImageIO.write(sikuliImage, "png", sikuliFile);
            System.out.println("   Saved to: " + sikuliFile.getName());
            
            // Method 1b: SikuliX Screen.capture(Rectangle)
            Rectangle fullScreen = new Rectangle(0, 0, sikuliScreen.w, sikuliScreen.h);
            ScreenImage sikuliCapture2 = sikuliScreen.capture(fullScreen);
            BufferedImage sikuliImage2 = sikuliCapture2.getImage();
            System.out.println("\n   SikuliX capture(Rectangle): " + sikuliImage2.getWidth() + "x" + sikuliImage2.getHeight());
            
            // 2. BROBOT SCREEN CAPTURE
            System.out.println("\n2. BROBOT SCREEN CAPTURE:");
            
            // Method 2a: Brobot BufferedImageUtilities static method
            Region fullRegion = new Region(0, 0, sikuliScreen.w, sikuliScreen.h);
            BufferedImage brobotStatic = BufferedImageUtilities.getBufferedImageFromScreen(fullRegion);
            System.out.println("   Brobot static capture: " + brobotStatic.getWidth() + "x" + brobotStatic.getHeight());
            System.out.println("   Image type: " + getImageTypeDetails(brobotStatic.getType()));
            System.out.println("   Has alpha: " + brobotStatic.getColorModel().hasAlpha());
            
            File brobotStaticFile = new File(outputDir, "brobot_static_" + timestamp + ".png");
            ImageIO.write(brobotStatic, "png", brobotStaticFile);
            System.out.println("   Saved to: " + brobotStaticFile.getName());
            
            // Method 2b: Brobot instance method (if available)
            BufferedImageUtilities brobotUtils = new BufferedImageUtilities();
            BufferedImage brobotInstance = brobotUtils.getBuffImgFromScreen(fullRegion);
            System.out.println("\n   Brobot instance capture: " + brobotInstance.getWidth() + "x" + brobotInstance.getHeight());
            System.out.println("   Image type: " + getImageTypeDetails(brobotInstance.getType()));
            
            File brobotInstanceFile = new File(outputDir, "brobot_instance_" + timestamp + ".png");
            ImageIO.write(brobotInstance, "png", brobotInstanceFile);
            System.out.println("   Saved to: " + brobotInstanceFile.getName());
            
            // 3. JAVA ROBOT CAPTURE (for reference)
            System.out.println("\n3. JAVA ROBOT CAPTURE:");
            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            BufferedImage robotImage = robot.createScreenCapture(screenRect);
            System.out.println("   Robot captured: " + robotImage.getWidth() + "x" + robotImage.getHeight());
            System.out.println("   Image type: " + getImageTypeDetails(robotImage.getType()));
            
            File robotFile = new File(outputDir, "robot_capture_" + timestamp + ".png");
            ImageIO.write(robotImage, "png", robotFile);
            System.out.println("   Saved to: " + robotFile.getName());
            
            // 4. PIXEL-BY-PIXEL COMPARISON
            System.out.println("\n4. PIXEL COMPARISON:");
            compareImages(sikuliImage, brobotStatic, "SikuliX vs Brobot Static");
            compareImages(sikuliImage, robotImage, "SikuliX vs Robot");
            compareImages(brobotStatic, robotImage, "Brobot vs Robot");
            
            // 5. TEST PATTERN MATCHING ON EACH CAPTURE
            System.out.println("\n5. PATTERN MATCHING TEST:");
            
            // Load a test pattern
            String patternPath = "images/prompt/claude-prompt-1.png";
            File patternFile = new File(patternPath);
            if (patternFile.exists()) {
                BufferedImage patternImg = ImageIO.read(patternFile);
                Pattern pattern = new Pattern(patternImg);
                
                System.out.println("\n   Pattern: " + patternPath);
                System.out.println("   Pattern size: " + patternImg.getWidth() + "x" + patternImg.getHeight());
                System.out.println("   Pattern type: " + getImageTypeDetails(patternImg.getType()));
                
                // Save pattern for reference
                File patternOutFile = new File(outputDir, "pattern_used_" + timestamp + ".png");
                ImageIO.write(patternImg, "png", patternOutFile);
                
                // Test on SikuliX capture
                System.out.println("\n   5a. Finding pattern in SikuliX capture:");
                testPatternOnImage(pattern, sikuliImage, "SikuliX");
                
                // Test on Brobot capture
                System.out.println("\n   5b. Finding pattern in Brobot capture:");
                testPatternOnImage(pattern, brobotStatic, "Brobot");
                
                // Test on Robot capture
                System.out.println("\n   5c. Finding pattern in Robot capture:");
                testPatternOnImage(pattern, robotImage, "Robot");
            }
            
            // 6. CAPTURE LOWER-LEFT REGIONS
            System.out.println("\n6. LOWER-LEFT REGION CAPTURES:");
            
            int llX = 0;
            int llY = sikuliScreen.h / 2;
            int llW = sikuliScreen.w / 2;
            int llH = sikuliScreen.h / 2;
            
            // SikuliX lower-left
            Rectangle llRect = new Rectangle(llX, llY, llW, llH);
            ScreenImage sikuliLL = sikuliScreen.capture(llRect);
            BufferedImage sikuliLLImage = sikuliLL.getImage();
            File sikuliLLFile = new File(outputDir, "sikulix_lowerleft_" + timestamp + ".png");
            ImageIO.write(sikuliLLImage, "png", sikuliLLFile);
            System.out.println("   SikuliX lower-left saved to: " + sikuliLLFile.getName());
            
            // Brobot lower-left
            Region llRegion = new Region(llX, llY, llW, llH);
            BufferedImage brobotLL = BufferedImageUtilities.getBufferedImageFromScreen(llRegion);
            File brobotLLFile = new File(outputDir, "brobot_lowerleft_" + timestamp + ".png");
            ImageIO.write(brobotLL, "png", brobotLLFile);
            System.out.println("   Brobot lower-left saved to: " + brobotLLFile.getName());
            
            // Compare lower-left regions
            System.out.println("\n   Lower-left comparison:");
            compareImages(sikuliLLImage, brobotLL, "SikuliX LL vs Brobot LL");
            
            System.out.println("\n=== All captures saved to: " + outputDir.getAbsolutePath() + " ===");
            System.out.println("Please visually compare these images to see if there are any differences!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testPatternOnImage(Pattern pattern, BufferedImage searchImage, String label) {
        try {
            Finder finder = new Finder(searchImage);
            
            // Test at different thresholds
            double[] thresholds = {0.99, 0.90, 0.70, 0.50};
            
            for (double threshold : thresholds) {
                Pattern p = pattern.similar(threshold);
                finder = new Finder(searchImage);
                finder.findAll(p);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("      " + label + " at " + threshold + 
                        ": FOUND with score " + String.format("%.3f", match.getScore()) + 
                        " at (" + match.x + ", " + match.y + ")");
                    finder.destroy();
                    break;
                } else {
                    System.out.println("      " + label + " at " + threshold + ": Not found");
                }
                finder.destroy();
            }
        } catch (Exception e) {
            System.out.println("      Error testing " + label + ": " + e.getMessage());
        }
    }
    
    private void compareImages(BufferedImage img1, BufferedImage img2, String comparison) {
        System.out.println("\n   " + comparison + ":");
        
        // Check dimensions
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            System.out.println("     DIFFERENT DIMENSIONS!");
            System.out.println("     Image 1: " + img1.getWidth() + "x" + img1.getHeight());
            System.out.println("     Image 2: " + img2.getWidth() + "x" + img2.getHeight());
            return;
        }
        
        // Check image types
        if (img1.getType() != img2.getType()) {
            System.out.println("     Different image types:");
            System.out.println("     Image 1: " + getImageTypeDetails(img1.getType()));
            System.out.println("     Image 2: " + getImageTypeDetails(img2.getType()));
        }
        
        // Sample pixels for comparison
        int w = img1.getWidth();
        int h = img1.getHeight();
        int differences = 0;
        int samples = 0;
        int maxDiff = 0;
        
        // Sample every 100th pixel
        for (int y = 0; y < h; y += 100) {
            for (int x = 0; x < w; x += 100) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                if (rgb1 != rgb2) {
                    differences++;
                    
                    // Calculate color difference
                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;
                    
                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;
                    
                    int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                    maxDiff = Math.max(maxDiff, diff);
                    
                    if (differences <= 3) {  // Show first few differences
                        System.out.printf("     Diff at (%d,%d): RGB1(%d,%d,%d) vs RGB2(%d,%d,%d)\n",
                            x, y, r1, g1, b1, r2, g2, b2);
                    }
                }
                samples++;
            }
        }
        
        if (differences == 0) {
            System.out.println("     ✓ IDENTICAL (sampled " + samples + " pixels)");
        } else {
            System.out.println("     ✗ DIFFERENT: " + differences + "/" + samples + 
                " pixels differ (max diff: " + maxDiff + ")");
        }
    }
    
    private String getImageTypeDetails(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: 
                return "TYPE_INT_RGB (1)";
            case BufferedImage.TYPE_INT_ARGB: 
                return "TYPE_INT_ARGB (2)";
            case BufferedImage.TYPE_INT_ARGB_PRE: 
                return "TYPE_INT_ARGB_PRE (3)";
            case BufferedImage.TYPE_INT_BGR: 
                return "TYPE_INT_BGR (4)";
            case BufferedImage.TYPE_3BYTE_BGR: 
                return "TYPE_3BYTE_BGR (5)";
            case BufferedImage.TYPE_4BYTE_ABGR: 
                return "TYPE_4BYTE_ABGR (6)";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: 
                return "TYPE_4BYTE_ABGR_PRE (7)";
            default: 
                return "Type " + type;
        }
    }
}