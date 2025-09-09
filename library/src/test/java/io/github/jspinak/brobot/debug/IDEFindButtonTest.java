package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.io.File;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Iterator;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Test that replicates exactly what the SikuliX IDE "Find" button does
 */
@DisabledInCI
public class IDEFindButtonTest extends BrobotTestBase {
    
    @Test
    public void replicateIDEFindButton() {
        System.out.println("=== REPLICATE SIKULIX IDE FIND BUTTON ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("Make sure the screen shows EXACTLY what you see when using SikuliX IDE!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");
            
            // Set up EXACTLY like IDE
            System.out.println("1. IDE SETUP:");
            
            // These are the IDE defaults
            Settings.MinSimilarity = 0.7;
            Settings.AutoWaitTimeout = 3.0f;
            Settings.WaitScanRate = 3.0f;
            Settings.ObserveScanRate = 3.0f;
            Settings.AlwaysResize = 1;  // CRITICAL: IDE uses resize!
            Settings.CheckLastSeen = true;
            
            System.out.println("   MinSimilarity: " + Settings.MinSimilarity);
            System.out.println("   AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   CheckLastSeen: " + Settings.CheckLastSeen);
            
            // Enable debug to see what SikuliX is doing
            Debug.setDebugLevel(3);
            
            // Test patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            Screen screen = new Screen();
            System.out.println("   Screen: " + screen.getBounds());
            
            for (String patternPath : patterns) {
                System.out.println("\n2. TESTING: " + patternPath);
                
                // Load the pattern image to check it
                File patternFile = new File(patternPath);
                if (!patternFile.exists()) {
                    System.out.println("   ERROR: Pattern file not found!");
                    continue;
                }
                
                BufferedImage patternImage = ImageIO.read(patternFile);
                System.out.println("   Pattern loaded: " + patternImage.getWidth() + "x" + 
                    patternImage.getHeight() + " type=" + patternImage.getType());
                
                // METHOD 1: Exactly what IDE Find button does
                System.out.println("\n   IDE FIND BUTTON SIMULATION:");
                System.out.println("   (This is what happens when you click Find in the IDE)");
                
                // The IDE uses Screen.find() with the pattern
                Pattern pattern = new Pattern(patternPath);
                
                // Set similarity to what you'd set in the IDE
                pattern = pattern.similar(0.99);  // Start with 0.99 like your IDE screenshot
                
                System.out.println("   Searching with similarity: 0.99");
                
                try {
                    // This is what the IDE Find button calls
                    Match match = screen.find(pattern);
                    
                    System.out.println("   ✓ FOUND!");
                    System.out.println("     Score: " + match.getScore());
                    System.out.println("     Location: " + match.getTarget());
                    System.out.println("     Region: " + match);
                    
                    // Highlight like IDE does
                    match.highlight(2);  // Highlight for 2 seconds
                    
                } catch (FindFailed e) {
                    System.out.println("   ✗ Not found at 0.99");
                    
                    // Try progressively lower similarities
                    double[] fallbacks = {0.95, 0.90, 0.85, 0.80, 0.70};
                    
                    for (double sim : fallbacks) {
                        pattern = pattern.similar(sim);
                        System.out.println("   Trying similarity: " + sim);
                        
                        try {
                            Match match = screen.find(pattern);
                            System.out.println("   ✓ FOUND at " + sim + "!");
                            System.out.println("     Score: " + match.getScore());
                            System.out.println("     Location: " + match.getTarget());
                            match.highlight(2);
                            break;
                        } catch (FindFailed ff) {
                            System.out.println("   ✗ Not found at " + sim);
                        }
                    }
                }
                
                // METHOD 2: What IDE does for "Find All"
                System.out.println("\n   IDE FIND ALL SIMULATION:");
                
                pattern = new Pattern(patternPath).similar(0.7);  // Reset to default
                
                try {
                    Iterator<Match> matches = screen.findAll(pattern);
                    
                    int count = 0;
                    double bestScore = 0;
                    Match bestMatch = null;
                    
                    while (matches.hasNext()) {
                        Match m = matches.next();
                        count++;
                        
                        if (m.getScore() > bestScore) {
                            bestScore = m.getScore();
                            bestMatch = m;
                        }
                        
                        if (count <= 5) {
                            System.out.println("   Match #" + count + ": score=" + 
                                m.getScore() + " at " + m.getTarget());
                        }
                    }
                    
                    if (count > 5) {
                        System.out.println("   ... and " + (count - 5) + " more matches");
                    }
                    
                    System.out.println("   Total: " + count + " matches, best score: " + bestScore);
                    
                    if (bestMatch != null && bestScore >= 0.90) {
                        System.out.println("   Highlighting best match...");
                        bestMatch.highlight(2);
                    }
                    
                } catch (FindFailed e) {
                    System.out.println("   No matches found");
                }
                
                // METHOD 3: Check with exists (non-throwing version)
                System.out.println("\n   IDE EXISTS CHECK:");
                
                for (double sim : new double[]{0.99, 0.95, 0.90, 0.85, 0.80, 0.70}) {
                    pattern = new Pattern(patternPath).similar(sim);
                    Match exists = screen.exists(pattern, 0);  // 0 timeout
                    
                    if (exists != null) {
                        System.out.println("   EXISTS at similarity " + sim + ": YES");
                        System.out.println("     Score: " + exists.getScore());
                        System.out.println("     Location: " + exists.getTarget());
                        break;
                    } else {
                        System.out.println("   EXISTS at similarity " + sim + ": NO");
                    }
                }
            }
            
            System.out.println("\n3. DIAGNOSTICS:");
            
            // Check what ImagePath sees
            System.out.println("   ImagePath.getBundlePath(): " + ImagePath.getBundlePath());
            
            // Check screen info
            System.out.println("   Number of screens: " + Screen.getNumberScreens());
            System.out.println("   Primary screen ID: " + screen.getID());
            System.out.println("   Screen bounds: " + screen.getBounds());
            
            // Check if patterns are being resized
            System.out.println("\n   RESIZE CHECK:");
            Pattern testPattern = new Pattern("images/prompt/claude-prompt-1.png");
            BufferedImage originalImage = testPattern.getBImage();
            if (originalImage != null) {
                System.out.println("   Original pattern size: " + 
                    originalImage.getWidth() + "x" + originalImage.getHeight());
                
                // Check if resize would happen
                if (Settings.AlwaysResize > 0) {
                    System.out.println("   AlwaysResize is ON - patterns may be scaled!");
                    System.out.println("   Resize factor: " + Settings.AlwaysResize);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}