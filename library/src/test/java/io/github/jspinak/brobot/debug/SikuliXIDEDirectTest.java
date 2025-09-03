package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.*;
import org.sikuli.basics.Settings;
import org.sikuli.basics.Debug;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Test that directly uses SikuliX IDE's search functionality
 */
public class SikuliXIDEDirectTest extends BrobotTestBase {
    
    @Test
    public void testUsingSikuliXIDEDirectly() {
        System.out.println("=== USING SIKULIX IDE DIRECTLY ===\n");
        
        try {
            // Give user time to switch to the target application
            System.out.println("!!! SWITCH TO YOUR TARGET APPLICATION NOW !!!");
            System.out.println("You have 5 seconds...");
            for (int i = 5; i > 0; i--) {
                System.out.println(i + "...");
                Thread.sleep(1000);
            }
            System.out.println("Starting test...\n");
            
            // Set up IDE-like environment
            System.out.println("1. SETTING UP IDE ENVIRONMENT:");
            
            // Initialize ImagePath like IDE does
            ImagePath.reset();
            ImagePath.setBundlePath("images");
            System.out.println("   Bundle path set to: " + ImagePath.getBundlePath());
            
            // Set IDE settings
            Settings.MinSimilarity = 0.7;
            Settings.AlwaysResize = 1;
            Settings.CheckLastSeen = true;
            Debug.setDebugLevel(3);  // Enable debug output
            
            System.out.println("   MinSimilarity: " + Settings.MinSimilarity);
            System.out.println("   AlwaysResize: " + Settings.AlwaysResize);
            System.out.println("   Debug level: " + Debug.getDebugLevel());
            
            // Test patterns
            String[] patterns = {
                "images/prompt/claude-prompt-1.png",
                "images/working/claude-icon-1.png"
            };
            
            for (String patternPath : patterns) {
                System.out.println("\n2. TESTING PATTERN: " + patternPath);
                
                // Method 1: Use IDE's Finder directly
                System.out.println("\n   METHOD 1: Using IDE's Finder class:");
                testWithIDEFinder(patternPath);
                
                // Method 2: Use IDE's pattern matching with reflection
                System.out.println("\n   METHOD 2: Using IDE's internal pattern matching:");
                testWithIDEInternals(patternPath);
                
                // Method 3: Use Screen with IDE settings
                System.out.println("\n   METHOD 3: Using Screen with IDE configuration:");
                testWithIDEConfiguredScreen(patternPath);
            }
            
            // Check IDE's image loading
            System.out.println("\n3. IDE IMAGE LOADING TEST:");
            testIDEImageLoading();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testWithIDEFinder(String patternPath) {
        try {
            // Load pattern like IDE would
            Pattern pattern = new Pattern(patternPath);
            
            // Capture screen
            Screen screen = new Screen();
            ScreenImage screenImage = screen.capture();
            
            // Create Finder with IDE settings
            Finder finder = new Finder(screenImage);
            
            // Test at different similarities
            double[] similarities = {0.99, 0.95, 0.90, 0.85, 0.80, 0.70, 0.60, 0.50};
            
            for (double sim : similarities) {
                pattern = pattern.similar(sim);
                finder = new Finder(screenImage);
                finder.findAll(pattern);
                
                if (finder.hasNext()) {
                    Match match = finder.next();
                    System.out.println("     Similarity " + sim + ": FOUND");
                    System.out.println("       Score: " + String.format("%.3f", match.getScore()));
                    System.out.println("       Location: (" + match.x + ", " + match.y + ")");
                    
                    // Count total matches
                    int count = 1;
                    while (finder.hasNext()) {
                        finder.next();
                        count++;
                    }
                    System.out.println("       Total matches: " + count);
                    
                    finder.destroy();
                    break;
                } else {
                    System.out.println("     Similarity " + sim + ": Not found");
                }
                finder.destroy();
            }
        } catch (Exception e) {
            System.out.println("     Error: " + e.getMessage());
        }
    }
    
    private void testWithIDEInternals(String patternPath) {
        try {
            // Try to use IDE's internal matching via reflection
            Screen screen = new Screen();
            
            // Load pattern
            Pattern pattern = new Pattern(patternPath);
            
            // Try IDE's matching methods
            System.out.println("     Using Screen.find():");
            try {
                Match match = screen.find(pattern.similar(0.7));
                if (match != null) {
                    System.out.println("       FOUND with score: " + match.getScore());
                    System.out.println("       Location: " + match.getTarget());
                }
            } catch (FindFailed e) {
                System.out.println("       Not found at 0.7 similarity");
            }
            
            // Try with wait
            System.out.println("     Using Screen.wait():");
            try {
                Match match = screen.wait(pattern.similar(0.7), 0.1);  // 0.1 second timeout
                if (match != null) {
                    System.out.println("       FOUND with score: " + match.getScore());
                    System.out.println("       Location: " + match.getTarget());
                }
            } catch (FindFailed e) {
                System.out.println("       Not found with wait");
            }
            
            // Try exists
            System.out.println("     Using Screen.exists():");
            Match existsMatch = screen.exists(pattern.similar(0.7), 0);
            if (existsMatch != null) {
                System.out.println("       FOUND with score: " + existsMatch.getScore());
                System.out.println("       Location: " + existsMatch.getTarget());
            } else {
                System.out.println("       Not found with exists");
            }
            
        } catch (Exception e) {
            System.out.println("     Error: " + e.getMessage());
        }
    }
    
    private void testWithIDEConfiguredScreen(String patternPath) {
        try {
            // Create screen exactly as IDE would
            Screen screen = new Screen();
            
            // Set screen-specific settings
            screen.setAutoWaitTimeout(3.0);
            
            // Load pattern with full path resolution
            File patternFile = new File(patternPath);
            String absolutePath = patternFile.getAbsolutePath();
            System.out.println("     Absolute path: " + absolutePath);
            
            // Try different pattern creation methods
            Pattern pattern1 = new Pattern(patternPath);
            Pattern pattern2 = new Pattern(absolutePath);
            
            // Test both
            System.out.println("     Testing relative path pattern:");
            Match match1 = screen.exists(pattern1.similar(0.7), 0);
            if (match1 != null) {
                System.out.println("       FOUND with score: " + match1.getScore());
            } else {
                System.out.println("       Not found");
            }
            
            System.out.println("     Testing absolute path pattern:");
            Match match2 = screen.exists(pattern2.similar(0.7), 0);
            if (match2 != null) {
                System.out.println("       FOUND with score: " + match2.getScore());
            } else {
                System.out.println("       Not found");
            }
            
            // Try with findBest
            System.out.println("     Using findBest:");
            try {
                Finder finder = new Finder(screen.capture().getImage());
                finder.find(pattern1.similar(0.5));
                if (finder.hasNext()) {
                    Match best = finder.next();
                    System.out.println("       Best match score: " + best.getScore());
                    System.out.println("       Location: (" + best.x + ", " + best.y + ")");
                }
                finder.destroy();
            } catch (Exception e) {
                System.out.println("       Error: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("     Error: " + e.getMessage());
        }
    }
    
    private void testIDEImageLoading() {
        try {
            System.out.println("   Testing IDE's image loading methods:");
            
            String testPattern = "images/prompt/claude-prompt-1.png";
            
            // Method 1: Image.create()
            Image img1 = Image.create(testPattern);
            if (img1 != null && img1.isValid()) {
                System.out.println("     Image.create() successful");
                System.out.println("       Size: " + img1.getSize());
                System.out.println("       URL: " + img1.getURL());
            } else {
                System.out.println("     Image.create() failed");
            }
            
            // Method 2: Pattern with getBImage()
            Pattern p = new Pattern(testPattern);
            if (p.getBImage() != null) {
                System.out.println("     Pattern.getBImage() successful");
                System.out.println("       Size: " + p.getBImage().getWidth() + "x" + p.getBImage().getHeight());
                System.out.println("       Type: " + p.getBImage().getType());
            } else {
                System.out.println("     Pattern.getBImage() returned null");
            }
            
            // Method 3: Try to access IDE's image cache via reflection
            try {
                Class<?> imageClass = Class.forName("org.sikuli.script.Image");
                Field[] fields = imageClass.getDeclaredFields();
                System.out.println("     Image class fields:");
                for (Field field : fields) {
                    if (field.getName().contains("cache") || field.getName().contains("Cache")) {
                        System.out.println("       Found cache field: " + field.getName());
                    }
                }
            } catch (Exception e) {
                System.out.println("     Could not access Image class internals: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("   Error in image loading test: " + e.getMessage());
        }
    }
}