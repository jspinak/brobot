package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Test to replicate exactly what SikuliX IDE does when matching patterns
 */
public class SikuliXIDEReplicationTest extends BrobotTestBase {
    
    @Test
    public void replicateSikuliXIDEMatching() {
        System.out.println("=== REPLICATE SIKULIX IDE MATCHING ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("Make sure the screen looks EXACTLY like when you use SikuliX IDE!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Capturing screen now!\n");
            
            // Set up exactly like SikuliX IDE would
            System.out.println("1. SIKULIX SETTINGS (matching IDE defaults):");
            Settings.MinSimilarity = 0.7;  // SikuliX IDE default
            Settings.AutoWaitTimeout = 3.0f;
            Settings.WaitScanRate = 3.0f;
            Settings.ObserveScanRate = 3.0f;
            Settings.AlwaysResize = 1;  // This is important!
            Settings.CheckLastSeen = true;
            
            System.out.println("   MinSimilarity: " + Settings.MinSimilarity);
            System.out.println("   AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   CheckLastSeen: " + Settings.CheckLastSeen);
            
            // Test both patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patterns) {
                System.out.println("\n2. TESTING: " + patternPath);
                
                // Method 1: Use Screen.exists() like IDE would
                System.out.println("\n   METHOD 1: Screen.exists() (like IDE Find button):");
                Screen screen = new Screen();
                
                // Try with file path directly (like IDE)
                Pattern pattern = new Pattern(patternPath);
                
                // Test at different similarities
                double[] similarities = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70};
                
                for (double sim : similarities) {
                    pattern = pattern.similar(sim);
                    Match match = screen.exists(pattern, 0);  // 0 timeout for immediate result
                    
                    if (match != null) {
                        System.out.println("     Similarity " + sim + ": FOUND!");
                        System.out.println("       Score: " + match.getScore());
                        System.out.println("       Location: " + match.getTarget());
                        System.out.println("       Region: " + match.toString());
                        break;
                    } else {
                        System.out.println("     Similarity " + sim + ": Not found");
                    }
                }
                
                // Method 2: Use Screen.findAll() like IDE pattern matching
                System.out.println("\n   METHOD 2: Screen.findAll() (comprehensive search):");
                
                pattern = new Pattern(patternPath);
                pattern = pattern.similar(0.7);  // IDE default
                
                List<Match> matches = screen.findAllList(pattern);
                
                if (matches != null && !matches.isEmpty()) {
                    System.out.println("     Found " + matches.size() + " matches:");
                    int count = 0;
                    for (Match m : matches) {
                        count++;
                        if (count <= 5) {  // Show first 5
                            System.out.println("       Match #" + count + 
                                ": score=" + m.getScore() + 
                                " at " + m.getTarget());
                        }
                    }
                    if (count > 5) {
                        System.out.println("       ... and " + (count - 5) + " more");
                    }
                } else {
                    System.out.println("     No matches found");
                }
                
                // Method 3: Direct region search (if pattern specifies a region)
                System.out.println("\n   METHOD 3: Region-specific search:");
                
                // For prompt patterns, search in lower-left quarter
                if (patternPath.contains("prompt")) {
                    int screenWidth = screen.w;
                    int screenHeight = screen.h;
                    Region lowerLeft = new Region(0, screenHeight/2, screenWidth/2, screenHeight/2);
                    
                    System.out.println("     Searching in lower-left region: " + lowerLeft);
                    
                    pattern = new Pattern(patternPath).similar(0.7);
                    Match regionMatch = lowerLeft.exists(pattern, 0);
                    
                    if (regionMatch != null) {
                        System.out.println("     FOUND in lower-left!");
                        System.out.println("       Score: " + regionMatch.getScore());
                        System.out.println("       Location: " + regionMatch.getTarget());
                    } else {
                        System.out.println("     Not found in lower-left region");
                        
                        // Try with lower threshold
                        pattern = pattern.similar(0.5);
                        regionMatch = lowerLeft.exists(pattern, 0);
                        if (regionMatch != null) {
                            System.out.println("     FOUND at 0.5 threshold!");
                            System.out.println("       Score: " + regionMatch.getScore());
                        }
                    }
                }
                
                // Method 4: Load image the way IDE does internally
                System.out.println("\n   METHOD 4: Using Image class (IDE internal method):");
                
                org.sikuli.script.Image img = org.sikuli.script.Image.create(patternPath);
                if (img != null && img.isValid()) {
                    System.out.println("     Image loaded successfully");
                    System.out.println("     Size: " + img.getSize());
                    
                    // Create pattern from Image
                    Pattern imgPattern = new Pattern(img).similar(0.7);
                    Match imgMatch = screen.exists(imgPattern, 0);
                    
                    if (imgMatch != null) {
                        System.out.println("     FOUND using Image class!");
                        System.out.println("       Score: " + imgMatch.getScore());
                    } else {
                        System.out.println("     Not found using Image class");
                    }
                } else {
                    System.out.println("     Failed to load image");
                }
            }
            
            // Final test: Check what the IDE sees
            System.out.println("\n3. SCREEN INFORMATION (what SikuliX sees):");
            Screen s = new Screen();
            System.out.println("   Number of screens: " + Screen.getNumberScreens());
            System.out.println("   Primary screen: " + s.getID());
            System.out.println("   Screen bounds: " + s.getBounds());
            System.out.println("   Screen size: " + s.w + "x" + s.h);
            
            // Check if patterns are in ImagePath
            System.out.println("\n4. IMAGEPATH CHECK:");
            List<String> pathList = new ArrayList<>();
            for (ImagePath.PathEntry entry : ImagePath.getPaths()) {
                pathList.add(entry.getPath());
            }
            System.out.println("   ImagePath has " + pathList.size() + " paths:");
            for (String path : pathList) {
                System.out.println("     - " + path);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}