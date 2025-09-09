package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test with proper ImagePath configuration to match how SikuliX IDE loads images
 */
@DisabledInCI
public class ProperImagePathTest extends BrobotTestBase {
    
    @Test
    public void testWithProperImagePath() {
        System.out.println("=== PROPER IMAGEPATH CONFIGURATION TEST ===\n");
        
        try {
            // Give user time to switch to target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");
            
            // Set up ImagePath properly
            System.out.println("1. CONFIGURING IMAGEPATH:");
            
            // Reset and configure
            ImagePath.reset();
            
            // Add the images directory itself
            String imagesDir = new File("images").getAbsolutePath();
            ImagePath.add(imagesDir);
            System.out.println("   Added to ImagePath: " + imagesDir);
            
            // Add subdirectories
            String promptDir = new File("images/prompt").getAbsolutePath();
            ImagePath.add(promptDir);
            System.out.println("   Added to ImagePath: " + promptDir);
            
            String workingDir = new File("images/working").getAbsolutePath();
            ImagePath.add(workingDir);
            System.out.println("   Added to ImagePath: " + workingDir);
            
            // Set bundle path
            ImagePath.setBundlePath(imagesDir);
            System.out.println("   Bundle path set to: " + ImagePath.getBundlePath());
            
            // Verify paths
            System.out.println("\n   Current ImagePath entries:");
            for (ImagePath.PathEntry entry : ImagePath.getPaths()) {
                System.out.println("     - " + entry.getPath());
            }
            
            // Set IDE-like settings
            Settings.MinSimilarity = 0.7;
            Settings.AlwaysResize = 1;
            Settings.CheckLastSeen = true;
            
            System.out.println("\n2. TESTING WITH JUST FILENAME:");
            
            // Test with just the filename (how IDE would reference it)
            String[] simpleNames = {
                "claude-prompt-1.png",
                "claude-prompt-1",  // Without extension
                "claude-icon-1.png",
                "claude-icon-1"     // Without extension
            };
            
            Screen screen = new Screen();
            
            for (String simpleName : simpleNames) {
                System.out.println("\n   Testing: " + simpleName);
                
                // Try to create pattern with simple name
                try {
                    Pattern pattern = new Pattern(simpleName);
                    
                    // Check if pattern loaded correctly
                    BufferedImage img = pattern.getBImage();
                    if (img != null) {
                        System.out.println("     ✓ Pattern loaded! Size: " + 
                            img.getWidth() + "x" + img.getHeight());
                        
                        // Now try to find it
                        pattern = pattern.similar(0.99);
                        Match match = screen.exists(pattern, 0);
                        
                        if (match != null) {
                            System.out.println("     ✓ FOUND with score: " + match.getScore());
                            System.out.println("       Location: " + match.getTarget());
                            match.highlight(1);
                        } else {
                            System.out.println("     ✗ Not found at 0.99 similarity");
                            
                            // Try lower thresholds
                            for (double sim : new double[]{0.90, 0.80, 0.70, 0.60, 0.50}) {
                                pattern = new Pattern(simpleName).similar(sim);
                                match = screen.exists(pattern, 0);
                                if (match != null) {
                                    System.out.println("     ✓ Found at " + sim + 
                                        " with score: " + match.getScore());
                                    break;
                                }
                            }
                        }
                    } else {
                        System.out.println("     ✗ Pattern failed to load");
                    }
                } catch (Exception e) {
                    System.out.println("     ✗ Error: " + e.getMessage());
                }
            }
            
            System.out.println("\n3. TESTING WITH IMAGE CLASS:");
            
            // Try using Image class directly
            for (String simpleName : new String[]{"claude-prompt-1.png", "claude-icon-1.png"}) {
                System.out.println("\n   Testing with Image: " + simpleName);
                
                Image img = Image.create(simpleName);
                if (img != null && img.isValid()) {
                    System.out.println("     ✓ Image loaded via Image.create()");
                    System.out.println("       URL: " + img.getURL());
                    System.out.println("       Size: " + img.getSize());
                    
                    // Create pattern from Image
                    Pattern pattern = new Pattern(img).similar(0.99);
                    Match match = screen.exists(pattern, 0);
                    
                    if (match != null) {
                        System.out.println("     ✓ FOUND with score: " + match.getScore());
                    } else {
                        System.out.println("     ✗ Not found");
                    }
                } else {
                    System.out.println("     ✗ Image.create() failed");
                }
            }
            
            System.out.println("\n4. DIRECT BUFFEREDIMAGE LOAD TEST:");
            
            // Load images directly as BufferedImages
            String[] fullPaths = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String path : fullPaths) {
                System.out.println("\n   Loading: " + path);
                File file = new File(path);
                
                if (file.exists()) {
                    BufferedImage buffImg = ImageIO.read(file);
                    System.out.println("     Loaded: " + buffImg.getWidth() + "x" + 
                        buffImg.getHeight() + " type=" + buffImg.getType());
                    
                    // Create pattern from BufferedImage
                    Pattern pattern = new Pattern(buffImg).similar(0.99);
                    
                    // Try finding
                    Match match = screen.exists(pattern, 0);
                    if (match != null) {
                        System.out.println("     ✓ FOUND with BufferedImage! Score: " + match.getScore());
                        match.highlight(2);
                    } else {
                        System.out.println("     ✗ Not found with BufferedImage at 0.99");
                        
                        // Try lower threshold
                        pattern = new Pattern(buffImg).similar(0.50);
                        match = screen.exists(pattern, 0);
                        if (match != null) {
                            System.out.println("     ✓ Found at 0.50 with score: " + match.getScore());
                        }
                    }
                } else {
                    System.out.println("     ✗ File not found");
                }
            }
            
            System.out.println("\n5. CHECKING WHAT SIKULIX SEES:");
            
            // Capture screen and save for inspection
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImg = screenCapture.getImage();
            System.out.println("   Screen captured: " + screenImg.getWidth() + "x" + 
                screenImg.getHeight() + " type=" + getImageTypeName(screenImg.getType()));
            
            // Save it
            File debugDir = new File("debug-captures");
            if (!debugDir.exists()) debugDir.mkdirs();
            
            File screenFile = new File(debugDir, "sikulix_screen_capture.png");
            ImageIO.write(screenImg, "png", screenFile);
            System.out.println("   Saved screen to: " + screenFile.getPath());
            
            // Also save what SikuliX thinks the patterns are
            for (String simpleName : new String[]{"claude-prompt-1.png", "claude-icon-1.png"}) {
                Pattern p = new Pattern(simpleName);
                BufferedImage pImg = p.getBImage();
                if (pImg != null) {
                    String outputName = simpleName.replace(".png", "_as_loaded.png");
                    File pFile = new File(debugDir, outputName);
                    ImageIO.write(pImg, "png", pFile);
                    System.out.println("   Saved pattern '" + simpleName + "' as loaded to: " + pFile.getName());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_4BYTE_ABGR: return "4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "4BYTE_ABGR_PRE";
            case BufferedImage.TYPE_3BYTE_BGR: return "3BYTE_BGR";
            default: return "Type " + type;
        }
    }
}