package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Compare EXACTLY what SikuliX IDE does vs what Brobot does
 * Both running on Windows, searching VS Code in WSL2
 */
@DisabledInCI
public class SikuliXIDEComparisonTest extends BrobotTestBase {
    
    @Test
    public void compareIDEVsBrobot() {
        System.out.println("=== SIKULIX IDE VS BROBOT COMPARISON ===\n");
        System.out.println("IMPORTANT: Both SikuliX IDE and Brobot are running on Windows");
        System.out.println("Both are searching the same VS Code window (WSL2/Debian)");
        System.out.println("Using the SAME pattern image from images.sikuli\n");
        
        try {
            // Give time to position VS Code window
            System.out.println("Position VS Code window exactly as when using SikuliX IDE");
            System.out.println("You have 5 seconds...");
            Thread.sleep(5000);
            
            // The pattern from SikuliX IDE
            String patternPath = "images.sikuli/1755024811085.png";
            File patternFile = new File(patternPath);
            
            if (!patternFile.exists()) {
                // Try other locations
                patternPath = "images/prompt/claude-prompt-1.png";
                patternFile = new File(patternPath);
            }
            
            System.out.println("Pattern: " + patternPath);
            System.out.println("Exists: " + patternFile.exists());
            
            if (!patternFile.exists()) {
                System.out.println("Pattern file not found!");
                return;
            }
            
            // Load pattern
            BufferedImage patternImage = ImageIO.read(patternFile);
            System.out.println("Pattern size: " + patternImage.getWidth() + "x" + patternImage.getHeight());
            System.out.println("Pattern type: " + getImageType(patternImage.getType()));
            
            // Test different SikuliX configurations
            System.out.println("\n=== TEST 1: Default Settings (What Brobot uses) ===");
            resetToDefaults();
            testPatternMatching(patternImage, "Default");
            
            System.out.println("\n=== TEST 2: IDE-like Settings ===");
            // SikuliX IDE might use different defaults
            Settings.MinSimilarity = 0.7;
            Settings.AlwaysResize = 1.0f;  // IDE default
            Settings.CheckLastSeen = true;
            Settings.WaitScanRate = 3.0f;
            testPatternMatching(patternImage, "IDE-like");
            
            System.out.println("\n=== TEST 3: With AlwaysResize = 0.8 (Your fix) ===");
            Settings.AlwaysResize = 0.8f;
            testPatternMatching(patternImage, "Resize 0.8");
            
            System.out.println("\n=== TEST 4: Try Different Image Loading ===");
            Settings.AlwaysResize = 1.0f;
            
            // Method A: Load as BufferedImage (what Brobot does)
            System.out.println("\nMethod A: BufferedImage (Brobot style):");
            Pattern patternA = new Pattern(patternImage);
            testPattern(patternA);
            
            // Method B: Load from file path (what IDE might do)
            System.out.println("\nMethod B: File path (IDE style):");
            Pattern patternB = new Pattern(patternPath);
            testPattern(patternB);
            
            // Method C: Using Image.create
            System.out.println("\nMethod C: Image.create:");
            Image img = Image.create(patternPath);
            if (img != null && img.isValid()) {
                Pattern patternC = new Pattern(img);
                testPattern(patternC);
            } else {
                System.out.println("  Failed to load with Image.create");
            }
            
            System.out.println("\n=== TEST 5: Check OpenCV Version ===");
            checkOpenCVVersion();
            
            System.out.println("\n=== TEST 6: Pattern Preprocessing ===");
            testPreprocessing(patternImage);
            
            System.out.println("\n=== TEST 7: Different Matching Algorithms ===");
            testMatchingAlgorithms(patternImage);
            
            System.out.println("\n=== HYPOTHESIS ===");
            System.out.println("The difference between IDE (0.99) and Brobot (0.70) might be:");
            System.out.println("1. IDE uses file path loading, Brobot uses BufferedImage");
            System.out.println("2. IDE has internal preprocessing we're not aware of");
            System.out.println("3. IDE uses different OpenCV template matching settings");
            System.out.println("4. IDE caches or optimizes patterns differently");
            System.out.println("5. The 0.8x scaling is the key (as your tests showed)");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void resetToDefaults() {
        Settings.MinSimilarity = 0.7;
        Settings.AlwaysResize = 0;
        Settings.CheckLastSeen = true;
        Settings.WaitScanRate = 3.0f;
        Settings.ObserveScanRate = 3.0f;
    }
    
    private void testPatternMatching(BufferedImage patternImage, String label) {
        try {
            Screen screen = new Screen();
            Pattern pattern = new Pattern(patternImage);
            
            // Test at different similarities
            double[] thresholds = {0.99, 0.95, 0.90, 0.85, 0.80, 0.75, 0.70, 0.65, 0.60};
            
            for (double threshold : thresholds) {
                Pattern p = pattern.similar(threshold);
                Match match = screen.exists(p, 0);
                
                if (match != null) {
                    System.out.printf("%s: Found at %.2f with score %.3f\n", 
                                    label, threshold, match.getScore());
                    return;
                }
            }
            
            System.out.println(label + ": Not found at any threshold");
            
        } catch (Exception e) {
            System.out.println(label + ": Error - " + e.getMessage());
        }
    }
    
    private void testPattern(Pattern pattern) {
        try {
            Screen screen = new Screen();
            
            // Test at 0.99 (what IDE shows)
            pattern = pattern.similar(0.99);
            Match match = screen.exists(pattern, 0);
            
            if (match != null) {
                System.out.println("  Found at 0.99! Score: " + match.getScore());
            } else {
                // Try lower thresholds
                for (double sim = 0.90; sim >= 0.50; sim -= 0.10) {
                    pattern = pattern.similar(sim);
                    match = screen.exists(pattern, 0);
                    
                    if (match != null) {
                        System.out.printf("  Found at %.2f with score %.3f\n", sim, match.getScore());
                        break;
                    }
                }
                
                if (match == null) {
                    System.out.println("  Not found at any threshold");
                }
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void checkOpenCVVersion() {
        try {
            // Check OpenCV version used by SikuliX
            System.out.println("Checking OpenCV configuration...");
            
            // SikuliX uses OpenCV for template matching
            // Different versions might give different results
            
            // Try to get version info
            System.out.println("  Java version: " + System.getProperty("java.version"));
            System.out.println("  OS: " + System.getProperty("os.name"));
            
            // Check if there are any OpenCV settings
            System.out.println("  Settings.AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("  Settings.MinSimilarity: " + Settings.MinSimilarity);
            
        } catch (Exception e) {
            System.out.println("  Could not check OpenCV version: " + e.getMessage());
        }
    }
    
    private void testPreprocessing(BufferedImage pattern) {
        System.out.println("Testing if preprocessing affects matching...");
        
        try {
            Screen screen = new Screen();
            
            // Test 1: Original pattern
            Pattern p1 = new Pattern(pattern).similar(0.70);
            Match m1 = screen.exists(p1, 0);
            System.out.println("  Original: " + (m1 != null ? "Score " + m1.getScore() : "Not found"));
            
            // Test 2: Convert to RGB (remove alpha if present)
            BufferedImage rgbPattern = new BufferedImage(
                pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbPattern.getGraphics().drawImage(pattern, 0, 0, null);
            
            Pattern p2 = new Pattern(rgbPattern).similar(0.70);
            Match m2 = screen.exists(p2, 0);
            System.out.println("  RGB converted: " + (m2 != null ? "Score " + m2.getScore() : "Not found"));
            
            // Test 3: Convert to BGR
            BufferedImage bgrPattern = new BufferedImage(
                pattern.getWidth(), pattern.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            bgrPattern.getGraphics().drawImage(pattern, 0, 0, null);
            
            Pattern p3 = new Pattern(bgrPattern).similar(0.70);
            Match m3 = screen.exists(p3, 0);
            System.out.println("  BGR converted: " + (m3 != null ? "Score " + m3.getScore() : "Not found"));
            
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private void testMatchingAlgorithms(BufferedImage pattern) {
        System.out.println("Testing different matching approaches...");
        
        try {
            Screen screen = new Screen();
            
            // Capture screen once
            ScreenImage screenCapture = screen.capture();
            BufferedImage screenImage = screenCapture.getImage();
            
            // Test with Finder (what might be used internally)
            Finder finder = new Finder(screenImage);
            Pattern p = new Pattern(pattern).similar(0.70);
            
            finder.findAll(p);
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  Finder.findAll: Score " + match.getScore());
            } else {
                System.out.println("  Finder.findAll: Not found");
            }
            
            finder.destroy();
            
            // Test with different Finder settings
            finder = new Finder(screenImage);
            finder.find(p);  // find() vs findAll()
            
            if (finder.hasNext()) {
                Match match = finder.next();
                System.out.println("  Finder.find: Score " + match.getScore());
            } else {
                System.out.println("  Finder.find: Not found");
            }
            
            finder.destroy();
            
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }
    
    private String getImageType(int type) {
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