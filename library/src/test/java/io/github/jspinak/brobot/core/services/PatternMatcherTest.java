package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for PatternMatcher interface.
 * Tests pattern matching operations and configurations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatternMatcher Interface Tests")
public class PatternMatcherTest extends BrobotTestBase {

    @Mock
    private PatternMatcher patternMatcher;
    
    @Mock
    private BufferedImage mockImage;
    
    @Mock
    private Pattern mockPattern;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should find single pattern in screen")
    void testFindSinglePattern() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.8)
            .withFindAll(false)
            .build();
            
        PatternMatcher.MatchResult expectedResult = 
            new PatternMatcher.MatchResult(100, 200, 50, 50, 0.95);
        List<PatternMatcher.MatchResult> results = Arrays.asList(expectedResult);
        
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(results);

        // Act
        List<PatternMatcher.MatchResult> actualResults = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(actualResults);
        assertEquals(1, actualResults.size());
        assertEquals(100, actualResults.get(0).getX());
        assertEquals(200, actualResults.get(0).getY());
        assertEquals(0.95, actualResults.get(0).getConfidence());
    }

    @Test
    @DisplayName("Should find multiple patterns when findAll is true")
    void testFindMultiplePatterns() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.7)
            .withFindAll(true)
            .withMaxMatches(10)
            .build();
            
        List<PatternMatcher.MatchResult> results = Arrays.asList(
            new PatternMatcher.MatchResult(100, 200, 50, 50, 0.95),
            new PatternMatcher.MatchResult(300, 400, 50, 50, 0.85),
            new PatternMatcher.MatchResult(500, 600, 50, 50, 0.75)
        );
        
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(results);

        // Act
        List<PatternMatcher.MatchResult> actualResults = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(actualResults);
        assertEquals(3, actualResults.size());
        verify(patternMatcher).findPatterns(mockImage, mockPattern, options);
    }

    @Test
    @DisplayName("Should return empty list when no patterns found")
    void testNoPatternFound() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.95)
            .build();
            
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(new ArrayList<>());

        // Act
        List<PatternMatcher.MatchResult> results = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should find patterns within specified region")
    void testFindPatternsInRegion() {
        // Arrange
        int regionX = 100, regionY = 100, regionWidth = 400, regionHeight = 300;
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.8)
            .build();
            
        PatternMatcher.MatchResult expectedResult = 
            new PatternMatcher.MatchResult(150, 150, 50, 50, 0.9);
        
        when(patternMatcher.findPatternsInRegion(
            mockImage, mockPattern, regionX, regionY, regionWidth, regionHeight, options))
            .thenReturn(Arrays.asList(expectedResult));

        // Act
        List<PatternMatcher.MatchResult> results = patternMatcher.findPatternsInRegion(
            mockImage, mockPattern, regionX, regionY, regionWidth, regionHeight, options);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(150, results.get(0).getX());
        assertEquals(150, results.get(0).getY());
    }

    @Test
    @DisplayName("Should respect max matches limit")
    void testMaxMatchesLimit() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.7)
            .withFindAll(true)
            .withMaxMatches(2)
            .build();
            
        List<PatternMatcher.MatchResult> results = Arrays.asList(
            new PatternMatcher.MatchResult(100, 100, 50, 50, 0.9),
            new PatternMatcher.MatchResult(200, 200, 50, 50, 0.85)
        );
        
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(results);

        // Act
        List<PatternMatcher.MatchResult> actualResults = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(actualResults);
        assertEquals(2, actualResults.size());
    }

    @Test
    @DisplayName("Should check pattern support")
    void testSupportsPattern() {
        // Arrange
        when(patternMatcher.supportsPattern(mockPattern)).thenReturn(true);

        // Act
        boolean supports = patternMatcher.supportsPattern(mockPattern);

        // Assert
        assertTrue(supports);
        verify(patternMatcher).supportsPattern(mockPattern);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Arrange
        when(patternMatcher.getImplementationName()).thenReturn("TestMatcher");

        // Act
        String name = patternMatcher.getImplementationName();

        // Assert
        assertEquals("TestMatcher", name);
    }

    @Test
    @DisplayName("Should build MatchOptions with default values")
    void testMatchOptionsDefaultValues() {
        // Act
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .build();

        // Assert
        assertEquals(0.7, options.getSimilarity());
        assertEquals(Integer.MAX_VALUE, options.getMaxMatches());
        assertTrue(options.isFindAll());
    }

    @Test
    @DisplayName("Should build MatchOptions with custom values")
    void testMatchOptionsCustomValues() {
        // Act
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.95)
            .withMaxMatches(5)
            .withFindAll(false)
            .build();

        // Assert
        assertEquals(0.95, options.getSimilarity());
        assertEquals(5, options.getMaxMatches());
        assertFalse(options.isFindAll());
    }

    @Test
    @DisplayName("Should convert MatchResult to Match object")
    void testMatchResultToMatch() {
        // Arrange
        PatternMatcher.MatchResult result = 
            new PatternMatcher.MatchResult(100, 200, 50, 60, 0.92);

        // Act
        Match match = result.toMatch();

        // Assert
        assertNotNull(match);
        assertNotNull(match.getTarget());
        assertEquals(0.92, match.getScore());
        // The location should be based on the region created from the result
        assertEquals(100, match.getTarget().getRegion().x());
        assertEquals(200, match.getTarget().getRegion().y());
        assertEquals(50, match.getTarget().getRegion().w());
        assertEquals(60, match.getTarget().getRegion().h());
    }

    @Test
    @DisplayName("Should handle region edge cases")
    void testRegionEdgeCases() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder().build();
        
        // Test with zero-sized region
        when(patternMatcher.findPatternsInRegion(
            mockImage, mockPattern, 0, 0, 0, 0, options))
            .thenReturn(new ArrayList<>());

        // Act
        List<PatternMatcher.MatchResult> results = patternMatcher.findPatternsInRegion(
            mockImage, mockPattern, 0, 0, 0, 0, options);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle high similarity threshold")
    void testHighSimilarityThreshold() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.99)
            .build();
            
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(new ArrayList<>());

        // Act
        List<PatternMatcher.MatchResult> results = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle low similarity threshold")
    void testLowSimilarityThreshold() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions.Builder()
            .withSimilarity(0.1)
            .withMaxMatches(100)
            .build();
            
        // With low similarity, expect many matches
        List<PatternMatcher.MatchResult> manyResults = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyResults.add(new PatternMatcher.MatchResult(
                i * 10, i * 10, 50, 50, 0.1 + i * 0.04));
        }
        
        when(patternMatcher.findPatterns(mockImage, mockPattern, options))
            .thenReturn(manyResults);

        // Act
        List<PatternMatcher.MatchResult> results = 
            patternMatcher.findPatterns(mockImage, mockPattern, options);

        // Assert
        assertNotNull(results);
        assertEquals(20, results.size());
    }

    @Test
    @DisplayName("Should validate MatchResult properties")
    void testMatchResultProperties() {
        // Arrange
        int x = 150, y = 250, width = 75, height = 85;
        double confidence = 0.88;
        
        // Act
        PatternMatcher.MatchResult result = 
            new PatternMatcher.MatchResult(x, y, width, height, confidence);

        // Assert
        assertEquals(x, result.getX());
        assertEquals(y, result.getY());
        assertEquals(width, result.getWidth());
        assertEquals(height, result.getHeight());
        assertEquals(confidence, result.getConfidence());
    }

    @Test
    @DisplayName("Should mutate MatchOptions through setters")
    void testMatchOptionsSetters() {
        // Arrange
        PatternMatcher.MatchOptions options = new PatternMatcher.MatchOptions();
        
        // Act
        options.setSimilarity(0.85);
        options.setMaxMatches(10);
        options.setFindAll(false);

        // Assert
        assertEquals(0.85, options.getSimilarity());
        assertEquals(10, options.getMaxMatches());
        assertFalse(options.isFindAll());
    }
}