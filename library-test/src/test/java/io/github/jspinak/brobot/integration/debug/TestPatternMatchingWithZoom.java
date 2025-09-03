package io.github.jspinak.brobot.integration.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.Test;
import org.sikuli.script.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Test pattern matching with different zoom levels.
 */
@SpringBootTest(classes = ClaudeAutomatorApplication.class)
public class TestPatternMatchingWithZoom {
    
    @Autowired
    private ScenePatternMatcher scenePatternMatcher;
    
    @Test
    public void testMatchingWithSavedImages() throws Exception {
        System.out.println("\n=== Testing Pattern Matching with Saved Images ===\n");
        
        // Load the saved pattern and match
        File patternFile = new File("/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/history/best-matches/20250810-190508_claude-prompt-3_sim0,387_pattern.png");
        File matchFile = new File("/mnt/c/Users/jspin/Documents/brobot_parent/claude-automator/history/best-matches/20250810-190508_claude-prompt-3_sim0,387_match.png");
        
        BufferedImage patternImg = ImageIO.read(patternFile);
        BufferedImage sceneImg = ImageIO.read(matchFile);
        
        // Create Pattern and Scene objects as Brobot would
        Pattern pattern = new Pattern(patternImg);
        pattern.setName("claude-prompt-test");
        
        Pattern scenePattern = new Pattern(sceneImg);
        scenePattern.setName("test-scene");
        Scene scene = new Scene(scenePattern);
        
        System.out.println("Pattern: " + patternImg.getWidth() + "x" + patternImg.getHeight());
        System.out.println("Scene: " + sceneImg.getWidth() + "x" + sceneImg.getHeight());
        
        // Test 1: Direct Sikuli matching at different thresholds
        System.out.println("\n--- Direct Sikuli Matching Test ---");
        testDirectSikuliMatching(patternImg, sceneImg);
        
        // Test 2: Brobot's matching
        System.out.println("\n--- Brobot Pattern Matching ---");
        List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
        System.out.println("Matches found: " + matches.size());
        for (Match match : matches) {
            System.out.println("  Match: " + match.getName() + " at " + match.x() + "," + match.y() + 
                " with score " + match.getScore());
        }
        
        // Test 3: Try matching with the images swapped
        System.out.println("\n--- Reverse Test (Scene as Pattern) ---");
        testDirectSikuliMatching(sceneImg, patternImg);
        
        System.out.println("\n=== Test Complete ===\n");
    }
    
    private void testDirectSikuliMatching(BufferedImage pattern, BufferedImage scene) {
        // Test at different similarity thresholds
        double[] thresholds = {0.99, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
        
        for (double threshold : thresholds) {
            org.sikuli.basics.Settings.MinSimilarity = threshold;
            
            Finder finder = new Finder(scene);
            org.sikuli.script.Pattern sikuliPattern = new org.sikuli.script.Pattern(pattern).similar(threshold);
            finder.findAll(sikuliPattern);
            
            double bestScore = 0;
            boolean found = false;
            while (finder.hasNext()) {
                org.sikuli.script.Match match = finder.next();
                if (match.getScore() > bestScore) {
                    bestScore = match.getScore();
                }
                found = true;
            }
            
            finder.destroy();
            
            if (found) {
                System.out.println("Threshold " + String.format("%.2f", threshold) + 
                    ": FOUND with best score " + String.format("%.3f", bestScore));
            } else {
                System.out.println("Threshold " + String.format("%.2f", threshold) + ": NOT FOUND");
            }
        }
    }
    
    @Test
    public void testCurrentScreenCapture() throws Exception {
        System.out.println("\n=== Testing Current Screen Capture ===\n");
        
        // Load the pattern
        Pattern pattern = new Pattern("claude-prompt");
        System.out.println("Pattern loaded: " + pattern.getName());
        System.out.println("Pattern dimensions: " + pattern.w() + "x" + pattern.h());
        
        // Capture current screen
        Region fullScreen = new Region();
        BufferedImage currentScreen = BufferedImageUtilities.getBufferedImageFromScreen(fullScreen);
        System.out.println("Screen captured: " + currentScreen.getWidth() + "x" + currentScreen.getHeight());
        
        // Create scene from current screen
        Pattern scenePattern = new Pattern(currentScreen);
        scenePattern.setName("current-screen");
        Scene scene = new Scene(scenePattern);
        
        // Try to find the pattern
        System.out.println("\n--- Searching for pattern in current screen ---");
        List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
        System.out.println("Matches found: " + matches.size());
        
        if (matches.isEmpty()) {
            System.out.println("\n⚠ Pattern not found in current screen!");
            System.out.println("Possible reasons:");
            System.out.println("1. Browser zoom level has changed since pattern was captured");
            System.out.println("2. UI scale/DPI settings have changed");
            System.out.println("3. The Claude UI has changed");
            System.out.println("4. The prompt area is not visible on screen");
            
            // Save current screen for inspection
            File debugDir = new File("diagnostics/current-screen");
            debugDir.mkdirs();
            File screenFile = new File(debugDir, "current-screen.png");
            ImageIO.write(currentScreen, "png", screenFile);
            System.out.println("\nSaved current screen to: " + screenFile.getPath());
        } else {
            for (Match match : matches) {
                System.out.println("Found at: " + match.x() + "," + match.y() + 
                    " with score: " + match.getScore());
            }
        }
        
        System.out.println("\n=== Test Complete ===\n");
    }
}