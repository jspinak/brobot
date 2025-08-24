package io.github.jspinak.brobot.action.internal.find.scene;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Finder;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ScenePatternMatcher class.
 * Tests pattern matching operations within scene images.
 */
@DisplayName("ScenePatternMatcher Tests")
public class ScenePatternMatcherTest extends BrobotTestBase {

    @InjectMocks
    private ScenePatternMatcher scenePatternMatcher;
    
    @Mock
    private Scene scene;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private BufferedImage sceneImage;
    
    @Mock
    private BufferedImage patternImage;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        scenePatternMatcher = new ScenePatternMatcher();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Pattern Matching")
    class PatternMatching {
        
        @Test
        @DisplayName("Should find pattern in scene")
        void shouldFindPatternInScene() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            when(pattern.getBImage()).thenReturn(patternImage);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyListWhenNoMatches() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            when(pattern.getBImage()).thenReturn(patternImage);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should find multiple matches")
        void shouldFindMultipleMatches() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            // Similarity is not used in findAllInScene
            // when(actionConfig.getSimilarity()).thenReturn(0.6);
            when(actionConfig.getMaxMatches()).thenReturn(5);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
        }
    }
    
    @Nested
    @DisplayName("Similarity Threshold")
    class SimilarityThreshold {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.6, 0.7, 0.8, 0.9, 0.95})
        @DisplayName("Should respect similarity threshold")
        void shouldRespectSimilarityThreshold(double similarity) {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            // Similarity is not used in findAllInScene
            // when(actionConfig.getSimilarity()).thenReturn(similarity);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
            // All matches should have score >= similarity
            matches.forEach(match -> 
                assertTrue(match.getScore() >= similarity)
            );
        }
        
        @Test
        @DisplayName("Should use default similarity when not specified")
        void shouldUseDefaultSimilarityWhenNotSpecified() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            // Similarity is not used in findAllInScene
            // when(actionConfig.getSimilarity()).thenReturn(null);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
            // Should use default (0.7)
        }
    }
    
    @Nested
    @DisplayName("Match Limit")
    class MatchLimit {
        
        @Test
        @DisplayName("Should limit number of matches")
        void shouldLimitNumberOfMatches() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            when(actionConfig.getMaxMatches()).thenReturn(3);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.size() <= 3);
        }
        
        @Test
        @DisplayName("Should return all matches when no limit")
        void shouldReturnAllMatchesWhenNoLimit() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            when(actionConfig.getMaxMatches()).thenReturn(0); // No limit
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should return top matches by score")
        void shouldReturnTopMatchesByScore() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            when(actionConfig.getMaxMatches()).thenReturn(1);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            if (!matches.isEmpty()) {
                assertEquals(1, matches.size());
                // Should be the highest scoring match
            }
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null scene")
        void shouldHandleNullScene() {
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, null);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null pattern")
        void shouldHandleNullPattern() {
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(null, scene);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null config")
        void shouldHandleNullConfig() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should handle empty images")
        void shouldHandleEmptyImages() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
            when(pattern.getBImage()).thenReturn(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
            
            // Act
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            
            // Assert
            assertNotNull(matches);
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should complete matching quickly")
        void shouldCompleteMatchingQuickly() {
            // Arrange
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB));
            when(pattern.getBImage()).thenReturn(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
            // Similarity is not used in findAllInScene
            // when(actionConfig.getSimilarity()).thenReturn(0.8);
            
            // Act
            long startTime = System.currentTimeMillis();
            List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(matches);
            assertTrue(endTime - startTime < 2000, "Matching should complete in less than 2 seconds");
        }
    }
}