package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.sikuli.script.Pattern;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import io.github.jspinak.brobot.test.DisabledInCI;

@DisabledInCI
public class PatternMatchingVerificationTest extends BrobotTestBase {
    
    @Test
    public void verifyPatternMatchingConfiguration() throws IOException {
        System.out.println("=== PATTERN MATCHING CONFIGURATION TEST ===");
        
        // Check system properties
        System.out.println("\nSystem Properties:");
        System.out.println("brobot.pattern.v107: " + System.getProperty("brobot.pattern.v107", "not set"));
        System.out.println("brobot.pattern.forceRGB: " + System.getProperty("brobot.pattern.forceRGB", "not set"));
        
        // Check SikuliX settings
        System.out.println("\nSikuliX Settings:");
        System.out.println("MinSimilarity: " + org.sikuli.basics.Settings.MinSimilarity);
        
        // Test with actual images if they exist
        File patternFile = new File("debug_images/pattern_claude-prompt-3.png");
        File sceneFile = new File("debug_images/scene_current.png");
        
        if (patternFile.exists() && sceneFile.exists()) {
            System.out.println("\n=== TESTING WITH DEBUG IMAGES ===");
            
            BufferedImage pattern = ImageIO.read(patternFile);
            BufferedImage scene = ImageIO.read(sceneFile);
            
            System.out.println("Pattern: " + pattern.getWidth() + "x" + pattern.getHeight() + 
                             " type=" + pattern.getType());
            System.out.println("Scene: " + scene.getWidth() + "x" + scene.getHeight() + 
                           " type=" + scene.getType());
            
            // Test direct SikuliX matching
            Pattern sikuliPattern = new Pattern(pattern);
            sikuliPattern = sikuliPattern.similar(0.5); // Lower threshold for testing
            
            Finder finder = new Finder(scene);
            finder.findAll(sikuliPattern);
            
            int count = 0;
            while (finder.hasNext()) {
                Match match = finder.next();
                count++;
                System.out.println("Match " + count + ": score=" + match.getScore() + 
                                 " at (" + match.x + "," + match.y + ")");
            }
            
            if (count == 0) {
                System.out.println("NO MATCHES FOUND even at 0.5 similarity!");
                System.out.println("\nPossible issues:");
                System.out.println("1. Scene is all black (check if running in WSL2)");
                System.out.println("2. Pattern and scene are from different UI versions");
                System.out.println("3. Scale/DPI mismatch between pattern and scene");
                System.out.println("4. Image type mismatch (ARGB vs RGB)");
            }
            
            finder.destroy();
        } else {
            System.out.println("\nDebug images not found at expected paths");
            System.out.println("Pattern exists: " + patternFile.exists());
            System.out.println("Scene exists: " + sceneFile.exists());
        }
    }
}