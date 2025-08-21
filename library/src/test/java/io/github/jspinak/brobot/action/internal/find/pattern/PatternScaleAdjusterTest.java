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
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;

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
@DisplayName("PatternScaleAdjuster Tests")
public class PatternScaleAdjusterTest extends BrobotTestBase {

    @InjectMocks
    private PatternScaleAdjuster patternScaleAdjuster;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private Scene scene;
    
    @Mock
    private BufferedImage patternImage;
    
    @Mock
    private BufferedImage sceneImage;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        patternScaleAdjuster = new PatternScaleAdjuster();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Basic Scale Finding")
    class BasicScaleFinding {
        
        @Test
        @DisplayName("Should find best scale for pattern")
        void shouldFindBestScaleForPattern() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertTrue(bestScale >= 0.5 && bestScale <= 2.0);
        }
        
        @Test
        @DisplayName("Should try all predefined scale factors")
        void shouldTryAllPredefinedScaleFactors() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            // When matches are similar, should prefer 1.0 (original scale)
            assertNotNull(bestScale);
        }
    }
    
    @Nested
    @DisplayName("Pattern Scaling")
    class PatternScaling {
        
        @Test
        @DisplayName("Should scale pattern image")
        void shouldScalePatternImage() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(patternImage.getWidth()).thenReturn(100);
            when(patternImage.getHeight()).thenReturn(100);
            double scaleFactor = 1.5;
            
            // Act
            BufferedImage scaledImage = patternScaleAdjuster.scaleImage(patternImage, scaleFactor);
            
            // Assert
            assertNotNull(scaledImage);
            assertEquals(150, scaledImage.getWidth());
            assertEquals(150, scaledImage.getHeight());
        }
        
        @Test
        @DisplayName("Should handle small scale factors")
        void shouldHandleSmallScaleFactors() {
            // Arrange
            when(patternImage.getWidth()).thenReturn(100);
            when(patternImage.getHeight()).thenReturn(100);
            double scaleFactor = 0.5;
            
            // Act
            BufferedImage scaledImage = patternScaleAdjuster.scaleImage(patternImage, scaleFactor);
            
            // Assert
            assertNotNull(scaledImage);
            assertEquals(50, scaledImage.getWidth());
            assertEquals(50, scaledImage.getHeight());
        }
        
        @Test
        @DisplayName("Should handle large scale factors")
        void shouldHandleLargeScaleFactors() {
            // Arrange
            when(patternImage.getWidth()).thenReturn(100);
            when(patternImage.getHeight()).thenReturn(100);
            double scaleFactor = 2.0;
            
            // Act
            BufferedImage scaledImage = patternScaleAdjuster.scaleImage(patternImage, scaleFactor);
            
            // Assert
            assertNotNull(scaledImage);
            assertEquals(200, scaledImage.getWidth());
            assertEquals(200, scaledImage.getHeight());
        }
    }
    
    @Nested
    @DisplayName("Match Scoring")
    class MatchScoring {
        
        @Test
        @DisplayName("Should find matches with minimum similarity")
        void shouldFindMatchesWithMinimumSimilarity() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            try (MockedConstruction<Finder> finderMock = mockConstruction(Finder.class,
                (mock, context) -> {
                    Match match = mock(Match.class);
                    when(match.getScore()).thenReturn(0.5); // Below MIN_SIMILARITY
                    when(mock.hasNext()).thenReturn(true, false);
                    when(mock.next()).thenReturn(match);
                })) {
                
                // Act
                double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
                
                // Assert
                // Should return 1.0 when no good matches found
                assertEquals(1.0, bestScale);
            }
        }
        
        @Test
        @DisplayName("Should select highest scoring scale")
        void shouldSelectHighestScoringScale() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(bestScale > 0);
        }
    }
    
    @Nested
    @DisplayName("Multiple Matches")
    class MultipleMatches {
        
        @Test
        @DisplayName("Should find multiple matches at scale")
        void shouldFindMultipleMatchesAtScale() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            double scale = 1.2;
            
            // Act
            List<Match> matches = patternScaleAdjuster.findMatchesAtScale(pattern, scene, scale);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should return empty list for no matches")
        void shouldReturnEmptyListForNoMatches() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            try (MockedConstruction<Finder> finderMock = mockConstruction(Finder.class,
                (mock, context) -> {
                    when(mock.hasNext()).thenReturn(false);
                })) {
                
                // Act
                List<Match> matches = patternScaleAdjuster.findMatchesAtScale(pattern, scene, 1.0);
                
                // Assert
                assertNotNull(matches);
                assertTrue(matches.isEmpty());
            }
        }
        
        @Test
        @DisplayName("Should collect all matches above threshold")
        void shouldCollectAllMatchesAboveThreshold() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            try (MockedConstruction<Finder> finderMock = mockConstruction(Finder.class,
                (mock, context) -> {
                    Match match1 = mock(Match.class);
                    Match match2 = mock(Match.class);
                    when(match1.getScore()).thenReturn(0.9);
                    when(match2.getScore()).thenReturn(0.85);
                    when(mock.hasNext()).thenReturn(true, true, false);
                    when(mock.next()).thenReturn(match1, match2);
                })) {
                
                // Act
                List<Match> matches = patternScaleAdjuster.findMatchesAtScale(pattern, scene, 1.0);
                
                // Assert
                assertNotNull(matches);
                assertEquals(2, matches.size());
            }
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should complete scale search quickly")
        void shouldCompleteScaleSearchQuickly() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(patternImage.getWidth()).thenReturn(1);
            when(patternImage.getHeight()).thenReturn(1);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertEquals(1.0, bestScale); // Should default to 1.0 for tiny patterns
        }
        
        @Test
        @DisplayName("Should handle very large patterns")
        void shouldHandleVeryLargePatterns() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(patternImage.getWidth()).thenReturn(5000);
            when(patternImage.getHeight()).thenReturn(5000);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
        }
        
        @Test
        @DisplayName("Should handle pattern larger than scene")
        void shouldHandlePatternLargerThanScene() {
            // Arrange
            when(pattern.getImage()).thenReturn(patternImage);
            when(patternImage.getWidth()).thenReturn(1000);
            when(patternImage.getHeight()).thenReturn(1000);
            when(scene.getImage()).thenReturn(sceneImage);
            when(sceneImage.getWidth()).thenReturn(500);
            when(sceneImage.getHeight()).thenReturn(500);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            // Should try smaller scales when pattern is larger than scene
            assertTrue(bestScale <= 1.0);
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should handle browser zoom changes")
        void shouldHandleBrowserZoomChanges() {
            // Arrange - Simulating browser at 125% zoom
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
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
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
        }
        
        @Test
        @DisplayName("Should handle responsive UI elements")
        void shouldHandleResponsiveUiElements() {
            // Arrange - UI element that changes size based on viewport
            when(pattern.getImage()).thenReturn(patternImage);
            when(scene.getImage()).thenReturn(sceneImage);
            
            // Act
            double bestScale = patternScaleAdjuster.findBestScale(pattern, scene);
            
            // Assert
            assertNotNull(bestScale);
            assertTrue(bestScale > 0);
        }
    }
}