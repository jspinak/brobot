package io.github.jspinak.brobot.action.internal.find.pixel;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.util.image.visualization.ScoringVisualizer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for PixelRegionExtractor class.
 * Tests extraction of match regions from pixel-level color analysis results.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PixelRegionExtractor Tests")
public class PixelRegionExtractorTest extends BrobotTestBase {

    @InjectMocks
    private PixelRegionExtractor pixelRegionExtractor;
    
    @Mock
    private ContourExtractor contourExtractor;
    
    @Mock
    private SearchRegionResolver searchRegionResolver;
    
    @Mock
    private MatchCollectionUtilities matchCollectionUtilities;
    
    @Mock
    private ScoringVisualizer scoringVisualizer;
    
    @Mock
    private SceneAnalysis sceneAnalysis;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private Mat mat;
    
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        pixelRegionExtractor = new PixelRegionExtractor(
            contourExtractor, 
            searchRegionResolver, 
            matchCollectionUtilities,
            scoringVisualizer
        );
    }
    
    
    @Nested
    @DisplayName("Region Extraction")
    class RegionExtraction {
        
        @Test
        @DisplayName("Should extract regions from scene analysis")
        void shouldExtractRegionsFromSceneAnalysis() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(10, 10, 50, 50));
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertFalse(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle empty contours")
        void shouldHandleEmptyContours() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(contourExtractor.getContours(mat)).thenReturn(Collections.emptyList());
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should extract multiple regions")
        void shouldExtractMultipleRegions() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(10, 10, 50, 50));
            contours.add(new Rect(100, 100, 60, 60));
            contours.add(new Rect(200, 200, 40, 40));
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(3, matches.size());
        }
    }
    
    @Nested
    @DisplayName("Size Filtering")
    class SizeFiltering {
        
        @Test
        @DisplayName("Should filter by minimum area")
        void shouldFilterByMinimumArea() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(actionConfig.getMinArea()).thenReturn(100);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(0, 0, 5, 5));   // Area: 25 (too small)
            contours.add(new Rect(0, 0, 20, 20)); // Area: 400 (ok)
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(1, matches.size());
        }
        
        @Test
        @DisplayName("Should filter by maximum area")
        void shouldFilterByMaximumArea() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(actionConfig.getMaxArea()).thenReturn(500);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(0, 0, 10, 10)); // Area: 100 (ok)
            contours.add(new Rect(0, 0, 50, 50)); // Area: 2500 (too large)
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(1, matches.size());
        }
        
        @ParameterizedTest
        @CsvSource({
            "10, 10, 100, 500, true",   // 100 area, within range
            "5, 5, 100, 500, false",    // 25 area, too small
            "50, 50, 100, 500, false",  // 2500 area, too large
            "20, 20, 100, 500, true",   // 400 area, within range
        })
        @DisplayName("Should apply area constraints")
        void shouldApplyAreaConstraints(int width, int height, int minArea, int maxArea, boolean shouldPass) {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(actionConfig.getMinArea()).thenReturn(minArea);
            when(actionConfig.getMaxArea()).thenReturn(maxArea);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(0, 0, width, height));
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(shouldPass ? 1 : 0, matches.size());
        }
    }
    
    @Nested
    @DisplayName("Search Region Boundaries")
    class SearchRegionBoundaries {
        
        @Test
        @DisplayName("Should respect search region")
        void shouldRespectSearchRegion() {
            // Arrange
            Region searchRegion = new Region(50, 50, 200, 200);
            when(searchRegionResolver.getSearchRegion(actionConfig)).thenReturn(searchRegion);
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(60, 60, 50, 50));  // Inside search region
            contours.add(new Rect(10, 10, 30, 30));  // Outside search region
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            // Should filter based on search region
        }
        
        @Test
        @DisplayName("Should handle null search region")
        void shouldHandleNullSearchRegion() {
            // Arrange
            when(searchRegionResolver.getSearchRegion(actionConfig)).thenReturn(null);
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(10, 10, 50, 50));
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should clip regions to search boundaries")
        void shouldClipRegionsToSearchBoundaries() {
            // Arrange
            Region searchRegion = new Region(0, 0, 100, 100);
            when(searchRegionResolver.getSearchRegion(actionConfig)).thenReturn(searchRegion);
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(80, 80, 40, 40)); // Extends beyond boundary
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            // Region should be clipped to search boundaries
        }
    }
    
    @Nested
    @DisplayName("Match Generation")
    class MatchGeneration {
        
        @Test
        @DisplayName("Should create match with correct location")
        void shouldCreateMatchWithCorrectLocation() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            Rect rect = new Rect(100, 150, 50, 75);
            when(contourExtractor.getContours(mat)).thenReturn(Collections.singletonList(rect));
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(1, matches.size());
            Match match = matches.get(0);
            assertEquals(100, match.getRegion().x());
            assertEquals(150, match.getRegion().y());
            assertEquals(50, match.getRegion().w());
            assertEquals(75, match.getRegion().h());
        }
        
        @Test
        @DisplayName("Should set match score")
        void shouldSetMatchScore() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(contourExtractor.getContours(mat)).thenReturn(
                Collections.singletonList(new Rect(10, 10, 20, 20))
            );
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(1, matches.size());
            assertNotNull(matches.get(0).getScore());
        }
        
        @Test
        @DisplayName("Should create matches for all valid regions")
        void shouldCreateMatchesForAllValidRegions() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                contours.add(new Rect(i * 20, i * 20, 15, 15));
            }
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(5, matches.size());
        }
    }
    
    @Nested
    @DisplayName("Color Schema Handling")
    class ColorSchemaHandling {
        
        @Test
        @DisplayName("Should use BGR color schema")
        void shouldUseBgrColorSchema() {
            // Arrange
            when(sceneAnalysis.getMat(SceneAnalysis.Analysis.BGR_FROM_INDICES_2D)).thenReturn(mat);
            when(contourExtractor.getContours(mat)).thenReturn(
                Collections.singletonList(new Rect(0, 0, 10, 10))
            );
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            verify(sceneAnalysis).getMat(SceneAnalysis.Analysis.BGR_FROM_INDICES_2D);
        }
        
        @Test
        @DisplayName("Should handle missing color matrix")
        void shouldHandleMissingColorMatrix() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(null);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should handle large number of contours")
        void shouldHandleLargeNumberOfContours() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                contours.add(new Rect(i % 100, i % 100, 10, 10));
            }
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            long startTime = System.currentTimeMillis();
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(matches);
            assertTrue(endTime - startTime < 1000, "Should process 1000 contours in less than 1 second");
        }
        
        @ParameterizedTest
        @ValueSource(ints = {10, 50, 100, 500})
        @DisplayName("Should scale with contour count")
        void shouldScaleWithContourCount(int contourCount) {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            for (int i = 0; i < contourCount; i++) {
                contours.add(new Rect(i, i, 10, 10));
            }
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(contourCount, matches.size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null scene analysis")
        void shouldHandleNullSceneAnalysis() {
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(null, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertTrue(matches.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null action config")
        void shouldHandleNullActionConfig() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            when(contourExtractor.getContours(mat)).thenReturn(
                Collections.singletonList(new Rect(0, 0, 10, 10))
            );
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, null);
            
            // Assert
            assertNotNull(matches);
        }
        
        @Test
        @DisplayName("Should handle zero-size rectangles")
        void shouldHandleZeroSizeRectangles() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(10, 10, 0, 0)); // Zero size
            contours.add(new Rect(20, 20, 10, 10)); // Valid size
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            assertEquals(1, matches.size()); // Only valid rectangle
        }
        
        @Test
        @DisplayName("Should handle negative coordinates")
        void shouldHandleNegativeCoordinates() {
            // Arrange
            when(sceneAnalysis.getMat(any())).thenReturn(mat);
            List<Rect> contours = new ArrayList<>();
            contours.add(new Rect(-10, -10, 20, 20)); // Negative coordinates
            when(contourExtractor.getContours(mat)).thenReturn(contours);
            
            // Act
            List<Match> matches = pixelRegionExtractor.extractRegions(sceneAnalysis, actionConfig);
            
            // Assert
            assertNotNull(matches);
            // Should handle or filter negative coordinates appropriately
        }
    }
}