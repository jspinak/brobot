package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Exact replication of what SikuliX IDE does when finding patterns
 * This test aims to achieve the same 0.99 similarity that IDE shows
 */
public class SikuliXIDEExactReplicationTest extends BrobotTestBase {
    
    @Test
    public void replicateIDEExactly() {
        System.out.println("=== EXACT SIKULIX IDE REPLICATION TEST ===\n");
        
        try {
            // Give user time to set up screen
            System.out.println("!!! MAKE SURE THE SCREEN LOOKS EXACTLY AS IT DOES IN SIKULIX IDE !!!");
            System.out.println("You have 5 seconds...");
            Thread.sleep(5000);
            
            // Set EXACT IDE settings
            System.out.println("Setting IDE defaults:");
            Settings.MinSimilarity = 0.7;  // IDE default
            Settings.AlwaysResize = 1;     // IDE uses resize by default
            Settings.CheckLastSeen = true; // IDE optimization
            System.out.println("  MinSimilarity: " + Settings.MinSimilarity);
            System.out.println("  AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("  CheckLastSeen: " + Settings.CheckLastSeen);
            
            // Initialize screen exactly as IDE does
            Screen screen = new Screen();
            System.out.println("\nScreen info:");
            System.out.println("  Bounds: " + screen.getBounds());
            System.out.println("  Size: " + screen.w + "x" + screen.h);
            
            // Test patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patterns) {
                System.out.println("\n=== TESTING: " + patternPath + " ===");
                
                File patternFile = new File(patternPath);
                if (!patternFile.exists()) {
                    System.out.println("ERROR: Pattern file not found!");
                    continue;
                }
                
                // Method 1: Exactly how IDE loads patterns from .sikuli folder
                System.out.println("\nMethod 1: IDE-style pattern loading");
                testIDEStyleLoading(screen, patternPath);
                
                // Method 2: Using absolute path (how IDE might resolve paths)
                System.out.println("\nMethod 2: Absolute path loading");
                testAbsolutePathLoading(screen, patternFile.getAbsolutePath());
                
                // Method 3: Using Image.create (IDE's internal method)
                System.out.println("\nMethod 3: Image.create loading");
                testImageCreateLoading(screen, patternPath);
                
                // Method 4: Direct BufferedImage but with IDE preprocessing
                System.out.println("\nMethod 4: BufferedImage with IDE preprocessing");
                testWithIDEPreprocessing(screen, patternFile);
                
                // Method 5: Test with different similarity calculation
                System.out.println("\nMethod 5: Alternative similarity calculation");
                testAlternativeSimilarity(screen, patternFile);
            }
            
            // Test with captured screen from IDE's perspective
            System.out.println("\n=== CAPTURING SCREEN AS IDE SEES IT ===");
            
            // Capture using IDE's method
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            System.out.println("Screen captured: " + screenImage.getWidth() + "x" + screenImage.getHeight());
            System.out.println("Image type: " + getImageTypeName(screenImage.getType()));
            
            // Save for inspection
            File outputDir = new File("ide-replication-test");
            outputDir.mkdirs();
            File screenFile = new File(outputDir, "ide_screen_capture.png");
            ImageIO.write(screenImage, "png", screenFile);
            System.out.println("Screen saved to: " + screenFile.getPath());
            
            // Now test with Finder exactly as IDE would
            System.out.println("\n=== USING FINDER AS IDE DOES ===");
            
            for (String patternPath : patterns) {
                File patternFile = new File(patternPath);
                if (!patternFile.exists()) continue;
                
                System.out.println("\nPattern: " + patternPath);
                
                // Load pattern
                BufferedImage patternImage = ImageIO.read(patternFile);
                
                // Create Finder as IDE would
                Finder finder = new Finder(screenImage);
                
                // Test at 0.99 similarity (what IDE shows)
                Pattern pattern = new Pattern(patternImage).similar(0.99);
                finder.find(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("  ✓ FOUND at 0.99!");
                    System.out.println("    Score: " + match.getScore());
                    System.out.println("    Location: (" + match.x + ", " + match.y + ")");
                } else {
                    System.out.println("  ✗ Not found at 0.99");
                    
                    // Try progressively lower similarities
                    finder.destroy();
                    finder = new Finder(screenImage);
                    
                    for (double sim = 0.95; sim >= 0.50; sim -= 0.05) {
                        finder = new Finder(screenImage);
                        pattern = new Pattern(patternImage).similar(sim);
                        finder.find(pattern);
                        
                        if (finder.hasNext()) {
                            Match match = finder.next();
                            System.out.println("  Found at " + String.format("%.2f", sim) + 
                                " with score: " + String.format("%.3f", match.getScore()));
                            finder.destroy();
                            break;
                        }
                        finder.destroy();
                    }
                }
            }
            
            System.out.println("\n=== DIAGNOSTIC INFO ===");
            
            // Check if patterns might be from a different capture
            System.out.println("\nPattern file details:");
            for (String patternPath : patterns) {
                File f = new File(patternPath);
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    System.out.println("  " + f.getName() + ":");
                    System.out.println("    Size: " + img.getWidth() + "x" + img.getHeight());
                    System.out.println("    Type: " + getImageTypeName(img.getType()));
                    System.out.println("    File size: " + f.length() + " bytes");
                    System.out.println("    Last modified: " + new java.util.Date(f.lastModified()));
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testIDEStyleLoading(Screen screen, String patternPath) {
        try {
            // IDE loads patterns with Pattern constructor
            Pattern pattern = new Pattern(patternPath).similar(0.99);
            
            // IDE uses Screen.exists or Screen.find
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.println("  ✓ FOUND with IDE-style loading!");
                System.out.println("    Score: " + match.getScore());
                System.out.println("    Location: " + match.getTarget());
            } else {
                System.out.println("  ✗ Not found with IDE-style loading");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void testAbsolutePathLoading(Screen screen, String absolutePath) {
        try {
            Pattern pattern = new Pattern(absolutePath).similar(0.99);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.println("  ✓ FOUND with absolute path!");
                System.out.println("    Score: " + match.getScore());
            } else {
                System.out.println("  ✗ Not found with absolute path");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void testImageCreateLoading(Screen screen, String patternPath) {
        try {
            Image img = Image.create(patternPath);
            if (img != null && img.isValid()) {
                Pattern pattern = new Pattern(img).similar(0.99);
                Match match = screen.exists(pattern, 0);
                
                if (match != null) {
                    System.out.println("  ✓ FOUND with Image.create!");
                    System.out.println("    Score: " + match.getScore());
                } else {
                    System.out.println("  ✗ Not found with Image.create");
                }
            } else {
                System.out.println("  Failed to load with Image.create");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void testWithIDEPreprocessing(Screen screen, File patternFile) {
        try {
            // Load image
            BufferedImage original = ImageIO.read(patternFile);
            
            // Convert to RGB if needed (IDE might do this)
            BufferedImage processed = original;
            if (original.getType() != BufferedImage.TYPE_INT_RGB) {
                processed = new BufferedImage(
                    original.getWidth(),
                    original.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
                processed.getGraphics().drawImage(original, 0, 0, null);
                System.out.println("  Converted to RGB");
            }
            
            Pattern pattern = new Pattern(processed).similar(0.99);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.println("  ✓ FOUND with preprocessing!");
                System.out.println("    Score: " + match.getScore());
            } else {
                System.out.println("  ✗ Not found with preprocessing");
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void testAlternativeSimilarity(Screen screen, File patternFile) {
        try {
            BufferedImage patternImage = ImageIO.read(patternFile);
            
            // Try with different Settings
            float oldResize = Settings.AlwaysResize;
            
            // Try without resize
            Settings.AlwaysResize = 0;
            Pattern pattern = new Pattern(patternImage).similar(0.99);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.println("  ✓ FOUND with AlwaysResize=0!");
                System.out.println("    Score: " + match.getScore());
            } else {
                // Try with different resize factors
                for (float resize : new float[]{0.5f, 1.0f, 1.5f, 2.0f}) {
                    Settings.AlwaysResize = resize;
                    pattern = new Pattern(patternImage).similar(0.99);
                    match = screen.exists(pattern, 0);
                    
                    if (match != null) {
                        System.out.println("  ✓ FOUND with AlwaysResize=" + resize + "!");
                        System.out.println("    Score: " + match.getScore());
                        break;
                    }
                }
                
                if (match == null) {
                    System.out.println("  ✗ Not found with any resize setting");
                }
            }
            
            Settings.AlwaysResize = oldResize;
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}