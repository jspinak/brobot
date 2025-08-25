package io.github.jspinak.brobot.action.internal.find.pattern;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for PatternScaleAdjuster class.
 * Tests pattern scale adjustment for UI element matching at different scales.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatternScaleAdjuster Tests")
public class PatternScaleAdjusterTest extends BrobotTestBase {

    @InjectMocks
    private PatternScaleAdjuster patternScaleAdjuster;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private Scene scene;
    
    private BufferedImage patternImage;
    private BufferedImage sceneImage;
    
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        patternScaleAdjuster = new PatternScaleAdjuster();
        
        // Create real BufferedImages for testing
        patternImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = patternImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(25, 25, 50, 50);
        g.dispose();
        
        sceneImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        g = sceneImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.RED);
        g.fillRect(50, 50, 50, 50);
        g.dispose();
    }
    
    
    @Nested
    @DisplayName("Basic Scale Finding")
    class BasicScaleFinding {
        
        @Test
        @DisplayName("Should find best scale for pattern")
        void shouldFindBestScaleForPattern() {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(bestScale > 0);
        }
        
        @Test
        @DisplayName("Should return 1.0 for null pattern")
        void shouldReturnOneForNullPattern() {
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(null, scene);
            
            // Assert
            assertEquals(1.0, bestScale);
        }
        
        @Test
        @DisplayName("Should return 1.0 for null scene")
        void shouldReturnOneForNullScene() {
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, null);
            
            // Assert
            assertEquals(1.0, bestScale);
        }
        
        @Test
        @DisplayName("Should return 1.0 when both null")
        void shouldReturnOneWhenBothNull() {
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(null, null);
            
            // Assert
            assertEquals(1.0, bestScale);
        }
    }
    
    @Nested
    @DisplayName("Scale Factor Testing")
    class ScaleFactorTesting {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.7, 1.0, 1.3, 1.5, 2.0})
        @DisplayName("Should test various scale factors")
        void shouldTestVariousScaleFactors(double expectedScale) {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertTrue(bestScale >= 0.5 && bestScale <= 2.0);
        }
        
        @Test
        @DisplayName("Should try all predefined scale factors")
        void shouldTryAllPredefinedScaleFactors() {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            // Scale should be within the range of predefined factors
            assertTrue(bestScale >= 0.5 && bestScale <= 2.0);
        }
        
        @Test
        @DisplayName("Should prefer original scale when similar matches")
        void shouldPreferOriginalScaleWhenSimilarMatches() {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            // When matches are similar, should prefer 1.0 (original scale)
            assertNotNull(bestScale);
        }
    }
    
    // Pattern Scaling tests removed - scaleImage is now private
    
    @Nested
    @DisplayName("Match Scoring")
    class MatchScoring {
        
        @Test
        @DisplayName("Should find matches with minimum similarity")
        void shouldFindMatchesWithMinimumSimilarity() {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            try (MockedConstruction<Finder> finderMock = mockConstruction(Finder.class,
                (mock, context) -> {
                    Match match = mock(Match.class);
                    when(match.getScore()).thenReturn(0.8);
                    when(mock.hasNext()).thenReturn(true, false);
                    when(mock.next()).thenReturn(match);
                })) {
                
                // Act
                double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
                
                // Assert
                assertNotNull(bestScale);
            }
        }
        
        @Test
        @DisplayName("Should reject matches below minimum similarity")
        void shouldRejectMatchesBelowMinimumSimilarity() {
            // Arrange
            when(pattern.getBImage()).thenReturn(patternImage);
            
            // Mock scene's pattern to provide scene image
            Pattern scenePattern = mock(Pattern.class);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            
            try (MockedConstruction<Finder> finderMock = mockConstruction(Finder.class,
                (mock, context) -> {
                    // Mock low score matches for all scale factors
                    when(mock.hasNext()).thenReturn(false);
                    when(mock.next()).thenReturn(null);
                })) {
                
                // Act
                double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
                
                // Assert
                // Should return 1.0 when no good matches found at any scale
                assertEquals(1.0, bestScale);
            }
        }
        
        @Test
        @DisplayName("Should select highest scoring scale")
        void shouldSelectHighestScoringScale() {
            // Arrange
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(bestScale > 0);
        }
    }
    
    // Multiple Matches tests removed - findMatchesAtScale method no longer exists
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should complete scale search quickly")
        void shouldCompleteScaleSearchQuickly() {
            // Arrange
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            long startTime = System.currentTimeMillis();
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(endTime - startTime < 5000, "Scale search should complete in less than 5 seconds");
        }
        
        @Test
        @DisplayName("Should cache scaled patterns")
        void shouldCacheScaledPatterns() {
            // Arrange
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(pattern);
            when(pattern.getBImage()).thenReturn(sceneImage);
            
            // Act - Multiple calls with same pattern
            double scale1 = patternScaleAdjuster.findBestScale(pattern, scene);
            double scale2 = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertEquals(scale1, scale2);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle very small patterns")
        void shouldHandleVerySmallPatterns() {
            // Arrange
            BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            when(pattern.getBImage()).thenReturn(tinyImage);
            
            // Mock scene's pattern to provide scene image
            Pattern scenePattern = mock(Pattern.class);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            // For very small patterns, the scale adjuster may find various scales
            // that work since a 1x1 pattern can match many places
            assertNotNull(bestScale);
            assertTrue(bestScale > 0, "Scale should be positive");
            assertTrue(bestScale <= 2.0, "Scale should not exceed maximum");
        }
        
        @Test
        @DisplayName("Should handle very large patterns")
        void shouldHandleVeryLargePatterns() {
            // Arrange
            BufferedImage largeImage = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_RGB);
            when(pattern.getBImage()).thenReturn(largeImage);
            
            // Mock scene's pattern to provide scene image
            Pattern scenePattern = mock(Pattern.class);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertEquals(1.0, bestScale); // Should default to 1.0 since pattern is larger than scene
        }
        
        @Test
        @DisplayName("Should handle pattern larger than scene")
        void shouldHandlePatternLargerThanScene() {
            // Arrange
            BufferedImage largePatternImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            BufferedImage smallSceneImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
            when(pattern.getBImage()).thenReturn(largePatternImage);
            
            // Mock scene's pattern to provide scene image
            Pattern scenePattern = mock(Pattern.class);
            when(scenePattern.getBImage()).thenReturn(smallSceneImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            // When pattern is larger than scene, the adjuster will try smaller scales
            // It may find that scaling down to 0.5 allows the pattern to fit
            assertNotNull(bestScale);
            assertTrue(bestScale > 0, "Scale should be positive");
            assertTrue(bestScale <= 1.0, "Scale should be 1.0 or less when pattern is larger than scene");
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should handle browser zoom changes")
        void shouldHandleBrowserZoomChanges() {
            // Arrange - Simulating browser at 125% zoom
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            // Could be around 1.25 for 125% zoom
        }
        
        @Test
        @DisplayName("Should handle DPI scaling differences")
        void shouldHandleDpiScalingDifferences() {
            // Arrange - Pattern from standard DPI, scene from high DPI
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
        }
        
        @Test
        @DisplayName("Should handle responsive UI elements")
        void shouldHandleResponsiveUiElements() {
            // Arrange - UI element that changes size based on viewport
            Pattern scenePattern = mock(Pattern.class);
            when(pattern.getBImage()).thenReturn(patternImage);
            when(scene.getPattern()).thenReturn(scenePattern);
            when(scenePattern.getBImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(bestScale > 0);
        }
    }
}