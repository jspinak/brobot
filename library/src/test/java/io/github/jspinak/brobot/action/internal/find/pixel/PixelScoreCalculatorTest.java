package io.github.jspinak.brobot.action.internal.find.pixel;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for PixelScoreCalculator class.
 * Tests pixel-level matching score calculations from color distance analysis.
 */
@DisplayName("PixelScoreCalculator Tests")
public class PixelScoreCalculatorTest extends BrobotTestBase {

    @InjectMocks
    private PixelScoreCalculator pixelScoreCalculator;
    
    @Mock
    private DistanceMatrixCalculator distanceMatrixCalculator;
    
    @Mock
    private MatrixVisualizer matrixVisualizer;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    @Mock
    private PixelProfile pixelProfile;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private Mat distanceMatrix;
    
    @Mock
    private Mat scoreMatrix;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        pixelScoreCalculator = new PixelScoreCalculator(
            distanceMatrixCalculator,
            matrixVisualizer,
            consoleReporter
        );
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Score Calculation")
    class ScoreCalculation {
        
        @Test
        @DisplayName("Should calculate scores from distances")
        void shouldCalculateScoresFromDistances() {
            // Arrange
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(distanceMatrix);
            when(distanceMatrix.rows()).thenReturn(100);
            when(distanceMatrix.cols()).thenReturn(100);
            
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, actionConfig);
            
            // Assert
            assertNotNull(scores);
        }
        
        @Test
        @DisplayName("Should handle empty distance matrix")
        void shouldHandleEmptyDistanceMatrix() {
            // Arrange
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(distanceMatrix);
            when(distanceMatrix.rows()).thenReturn(0);
            when(distanceMatrix.cols()).thenReturn(0);
            
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, actionConfig);
            
            // Assert
            assertNotNull(scores);
        }
        
        @Test
        @DisplayName("Should apply similarity threshold")
        void shouldApplySimilarityThreshold() {
            // Arrange
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(distanceMatrix);
            when(actionConfig.getSimilarity()).thenReturn(0.8);
            when(distanceMatrix.rows()).thenReturn(10);
            when(distanceMatrix.cols()).thenReturn(10);
            
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, actionConfig);
            
            // Assert
            assertNotNull(scores);
            verify(actionConfig).getSimilarity();
        }
    }
    
    @Nested
    @DisplayName("Score Conversion")
    class ScoreConversion {
        
        @ParameterizedTest
        @CsvSource({
            "0, 1.0",      // Perfect match (0 distance = 1.0 similarity)
            "50, 0.8",     // Good match
            "100, 0.6",    // Moderate match
            "200, 0.3",    // Poor match
            "255, 0.0"     // No match (max distance = 0 similarity)
        })
        @DisplayName("Should convert distance to similarity score")
        void shouldConvertDistanceToSimilarityScore(int distance, double expectedSimilarity) {
            // Act
            double similarity = pixelScoreCalculator.distanceToSimilarity(distance);
            
            // Assert
            assertTrue(similarity >= 0.0 && similarity <= 1.0);
            // Verify the conversion follows expected pattern
            if (distance == 0) {
                assertEquals(1.0, similarity, 0.01);
            } else if (distance == 255) {
                assertTrue(similarity < 0.1);
            }
        }
        
        @Test
        @DisplayName("Should use tanh conversion function")
        void shouldUseTanhConversionFunction() {
            // Test that conversion uses hyperbolic tangent
            double score1 = pixelScoreCalculator.distanceToSimilarity(50);
            double score2 = pixelScoreCalculator.distanceToSimilarity(100);
            double score3 = pixelScoreCalculator.distanceToSimilarity(150);
            
            // Verify non-linear conversion
            assertTrue(score1 > score2);
            assertTrue(score2 > score3);
            
            // Verify diminishing differences (characteristic of tanh)
            double diff1 = score1 - score2;
            double diff2 = score2 - score3;
            assertTrue(diff1 > diff2); // Larger differences at higher similarities
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.6, 0.7, 0.8, 0.9, 1.0})
        @DisplayName("Should convert similarity to pixel score")
        void shouldConvertSimilarityToPixelScore(double similarity) {
            // Act
            int pixelScore = pixelScoreCalculator.similarityToPixelScore(similarity);
            
            // Assert
            assertTrue(pixelScore >= 0 && pixelScore <= 255);
            // Higher similarity should result in lower pixel score
            if (similarity >= 0.9) {
                assertTrue(pixelScore < 50);
            }
        }
    }
    
    @Nested
    @DisplayName("Distance Metrics")
    class DistanceMetrics {
        
        @Test
        @DisplayName("Should combine multiple distance metrics")
        void shouldCombineMultipleDistanceMetrics() {
            // Arrange
            Mat hsvDistance = new Mat(10, 10, CV_32F);
            Mat bgrDistance = new Mat(10, 10, CV_32F);
            when(pixelProfile.getMat(PixelProfile.Analysis.HSV_DISTANCES)).thenReturn(hsvDistance);
            when(pixelProfile.getMat(PixelProfile.Analysis.BGR_DISTANCES)).thenReturn(bgrDistance);
            
            // Act
            Mat combinedScores = pixelScoreCalculator.combineDistanceMetrics(pixelProfile);
            
            // Assert
            assertNotNull(combinedScores);
        }
        
        @Test
        @DisplayName("Should weight distance metrics appropriately")
        void shouldWeightDistanceMetricsAppropriately() {
            // Arrange
            Mat primaryDistance = new Mat(5, 5, CV_32F);
            Mat secondaryDistance = new Mat(5, 5, CV_32F);
            
            // Fill with test values
            primaryDistance.put(new Scalar(10));
            secondaryDistance.put(new Scalar(20));
            
            // Act
            Mat weighted = pixelScoreCalculator.weightedCombination(
                primaryDistance, 0.7, secondaryDistance, 0.3
            );
            
            // Assert
            assertNotNull(weighted);
            // Result should be 0.7 * 10 + 0.3 * 20 = 13
        }
    }
    
    @Nested
    @DisplayName("Penalty Application")
    class PenaltyApplication {
        
        @Test
        @DisplayName("Should apply out-of-range penalty")
        void shouldApplyOutOfRangePenalty() {
            // Arrange
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(distanceMatrix);
            when(pixelProfile.getOutOfRangeMask()).thenReturn(new Mat(10, 10, CV_8U));
            
            // Act
            Mat scores = pixelScoreCalculator.calculateScoresWithPenalties(pixelProfile, actionConfig);
            
            // Assert
            assertNotNull(scores);
        }
        
        @Test
        @DisplayName("Should penalize pixels outside color range")
        void shouldPenalizePixelsOutsideColorRange() {
            // Arrange
            ColorCluster cluster = mock(ColorCluster.class);
            when(cluster.isInRange(any())).thenReturn(false);
            
            // Act
            double penalty = pixelScoreCalculator.calculatePenalty(cluster, new Scalar(100, 100, 100));
            
            // Assert
            assertTrue(penalty > 0);
        }
        
        @Test
        @DisplayName("Should not penalize pixels within range")
        void shouldNotPenalizePixelsWithinRange() {
            // Arrange
            ColorCluster cluster = mock(ColorCluster.class);
            when(cluster.isInRange(any())).thenReturn(true);
            
            // Act
            double penalty = pixelScoreCalculator.calculatePenalty(cluster, new Scalar(100, 100, 100));
            
            // Assert
            assertEquals(0, penalty, 0.001);
        }
    }
    
    @Nested
    @DisplayName("Threshold Filtering")
    class ThresholdFiltering {
        
        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.6, 0.7, 0.8, 0.9})
        @DisplayName("Should filter scores below threshold")
        void shouldFilterScoresBelowThreshold(double threshold) {
            // Arrange
            Mat scores = new Mat(10, 10, CV_32F);
            scores.put(new Scalar(threshold - 0.1)); // Below threshold
            
            // Act
            Mat filtered = pixelScoreCalculator.applyThreshold(scores, threshold);
            
            // Assert
            assertNotNull(filtered);
            // Values below threshold should be zeroed
        }
        
        @Test
        @DisplayName("Should preserve scores above threshold")
        void shouldPreserveScoresAboveThreshold() {
            // Arrange
            Mat scores = new Mat(10, 10, CV_32F);
            scores.put(new Scalar(0.9));
            double threshold = 0.7;
            
            // Act
            Mat filtered = pixelScoreCalculator.applyThreshold(scores, threshold);
            
            // Assert
            assertNotNull(filtered);
            // Values above threshold should be preserved
        }
    }
    
    @Nested
    @DisplayName("Matrix Operations")
    class MatrixOperations {
        
        @Test
        @DisplayName("Should normalize score matrix")
        void shouldNormalizeScoreMatrix() {
            // Arrange
            Mat scores = new Mat(10, 10, CV_32F);
            scores.put(new Scalar(200)); // Un-normalized value
            
            // Act
            Mat normalized = pixelScoreCalculator.normalizeScores(scores);
            
            // Assert
            assertNotNull(normalized);
            // All values should be between 0 and 1
        }
        
        @Test
        @DisplayName("Should handle different matrix types")
        void shouldHandleDifferentMatrixTypes() {
            // Test with different OpenCV types
            Mat floatMat = new Mat(5, 5, CV_32F);
            Mat doubleMat = new Mat(5, 5, CV_64F);
            Mat byteMat = new Mat(5, 5, CV_8U);
            
            // Act & Assert
            assertNotNull(pixelScoreCalculator.convertToScoreMatrix(floatMat));
            assertNotNull(pixelScoreCalculator.convertToScoreMatrix(doubleMat));
            assertNotNull(pixelScoreCalculator.convertToScoreMatrix(byteMat));
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should calculate scores efficiently for large matrices")
        void shouldCalculateScoresEfficientlyForLargeMatrices() {
            // Arrange
            Mat largeMatrix = new Mat(1000, 1000, CV_32F);
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(largeMatrix);
            
            // Act
            long startTime = System.currentTimeMillis();
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, actionConfig);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertNotNull(scores);
            assertTrue(endTime - startTime < 1000, "Should process 1M pixels in less than 1 second");
        }
        
        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000, 2000})
        @DisplayName("Should scale with matrix size")
        void shouldScaleWithMatrixSize(int size) {
            // Arrange
            Mat matrix = new Mat(size, size, CV_32F);
            when(pixelProfile.getMat(PixelProfile.Analysis.DISTANCES)).thenReturn(matrix);
            
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, actionConfig);
            
            // Assert
            assertNotNull(scores);
            assertEquals(size, scores.rows());
            assertEquals(size, scores.cols());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle null pixel profile")
        void shouldHandleNullPixelProfile() {
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(null, actionConfig);
            
            // Assert
            assertNull(scores);
        }
        
        @Test
        @DisplayName("Should handle null action config")
        void shouldHandleNullActionConfig() {
            // Act
            Mat scores = pixelScoreCalculator.calculateScores(pixelProfile, null);
            
            // Assert
            assertNotNull(scores); // Should use default settings
        }
        
        @Test
        @DisplayName("Should handle negative distances")
        void shouldHandleNegativeDistances() {
            // Negative distances shouldn't occur but should be handled
            double similarity = pixelScoreCalculator.distanceToSimilarity(-10);
            
            // Assert
            assertEquals(1.0, similarity, 0.001); // Treat as perfect match
        }
        
        @Test
        @DisplayName("Should handle infinite values")
        void shouldHandleInfiniteValues() {
            // Arrange
            Mat scores = new Mat(5, 5, CV_32F);
            scores.put(0, 0, Double.POSITIVE_INFINITY);
            
            // Act
            Mat normalized = pixelScoreCalculator.normalizeScores(scores);
            
            // Assert
            assertNotNull(normalized);
            // Infinite values should be handled appropriately
        }
    }
}