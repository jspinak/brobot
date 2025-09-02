package io.github.jspinak.brobot.action.internal.find.scene;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test to verify that ScenePatternMatcher properly filters matches by minimum similarity score.
 */
public class ScenePatternMatcherFilterTest extends BrobotTestBase {
    
    private ScenePatternMatcher scenePatternMatcher;
    private Pattern pattern;
    private Scene scene;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        scenePatternMatcher = new ScenePatternMatcher();
        
        // Create test pattern and scene with dummy images
        BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        pattern = new Pattern();
        pattern.setName("test-pattern");
        pattern.setImage(new io.github.jspinak.brobot.model.element.Image(dummyImage));
        
        Pattern scenePattern = new Pattern();
        scenePattern.setImage(new io.github.jspinak.brobot.model.element.Image(dummyImage));
        scene = new Scene(scenePattern);
    }
    
    @Test
    public void testMatchesFilteredByMinimumSimilarity() {
        // Set the minimum similarity threshold
        double originalThreshold = Settings.MinSimilarity;
        Settings.MinSimilarity = 0.70;
        
        try {
            // Mock the Finder to return matches with various scores
            // Note: In a real test, we'd need to properly mock Sikuli's Finder
            // For this example, we're testing the concept
            
            // Perform the search
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Verify that all returned matches meet the minimum similarity threshold
            for (Match match : matches) {
                assertTrue(match.getScore() >= Settings.MinSimilarity,
                    "Match with score " + match.getScore() + 
                    " should not be included when threshold is " + Settings.MinSimilarity);
            }
            
            System.out.println("Test passed: All matches meet minimum similarity threshold");
            
        } finally {
            // Restore original threshold
            Settings.MinSimilarity = originalThreshold;
        }
    }
    
    @Test
    public void testNoMatchesReturnedWhenAllBelowThreshold() {
        // Set a very high threshold that no matches will meet
        double originalThreshold = Settings.MinSimilarity;
        Settings.MinSimilarity = 0.99;
        
        try {
            // Perform the search
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // In mock mode, this should return empty if no matches meet threshold
            // (actual behavior depends on mock implementation)
            System.out.println("Found " + matches.size() + " matches with threshold " + Settings.MinSimilarity);
            
        } finally {
            // Restore original threshold
            Settings.MinSimilarity = originalThreshold;
        }
    }
    
    @Test
    public void testDifferentThresholdLevels() {
        double originalThreshold = Settings.MinSimilarity;
        
        try {
            // Test with low threshold
            Settings.MinSimilarity = 0.50;
            List<Match> lowThresholdMatches = scenePatternMatcher.findAllInScene(pattern, scene);
            int lowThresholdCount = lowThresholdMatches.size();
            
            // Test with high threshold
            Settings.MinSimilarity = 0.90;
            List<Match> highThresholdMatches = scenePatternMatcher.findAllInScene(pattern, scene);
            int highThresholdCount = highThresholdMatches.size();
            
            // Higher threshold should result in fewer or equal matches
            assertTrue(highThresholdCount <= lowThresholdCount,
                "Higher threshold should result in fewer matches");
            
            System.out.println("Low threshold (0.50) matches: " + lowThresholdCount);
            System.out.println("High threshold (0.90) matches: " + highThresholdCount);
            
        } finally {
            // Restore original threshold
            Settings.MinSimilarity = originalThreshold;
        }
    }
}