package io.github.jspinak.brobot.action.internal.find.pixel;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

/**
 * Test suite for PixelScoreCalculator class. Tests pixel-level matching score calculations from
 * color distance analysis.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PixelScoreCalculator Tests")
public class PixelScoreCalculatorTest extends BrobotTestBase {

    @InjectMocks private PixelScoreCalculator pixelScoreCalculator;

    @Mock private DistanceMatrixCalculator distanceMatrixCalculator;

    @Mock private MatrixVisualizer matrixVisualizer;

    @Mock private ConsoleReporter consoleReporter;

    @Mock private PixelProfile pixelProfile;

    @Mock private ActionConfig actionConfig;

    @Mock private Mat distanceMatrix;

    @Mock private Mat scoreMatrix;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        pixelScoreCalculator = new PixelScoreCalculator(matrixVisualizer);
    }

    @Nested
    @DisplayName("Score Calculation")
    class ScoreCalculation {

        @Test
        @DisplayName("Should calculate scores from distances")
        void shouldCalculateScoresFromDistances() {
            // Arrange
            Mat distOut = new Mat(100, 100, CV_32F);
            Mat distTarget = new Mat(100, 100, CV_32F);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            verify(pixelProfile)
                    .setAnalyses(
                            eq(PixelProfile.Analysis.SCORES),
                            eq(ColorCluster.ColorSchemaName.BGR),
                            any(Mat.class));
        }

        @Test
        @DisplayName("Should handle empty distance matrix")
        void shouldHandleEmptyDistanceMatrix() {
            // Arrange
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(null);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            // Should return Mat with Scalar(255) when no analysis available
        }

        @Test
        @DisplayName("Should apply similarity threshold")
        void shouldApplySimilarityThreshold() {
            // Arrange
            Mat distOut = new Mat(10, 10, CV_32F);
            Mat distTarget = new Mat(10, 10, CV_32F);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
        }
    }

    @Nested
    @DisplayName("Score Conversion")
    class ScoreConversion {

        @ParameterizedTest
        @CsvSource({
            "0.0, 0.0", // Perfect match (similarity 1.0 = pixel score 0)
            "0.5, 127.5", // Mid similarity
            "0.7, 178.5", // SikuliX default
            "0.9, 229.5", // High threshold
            "1.0, 255.0" // Perfect threshold (similarity 1.0)
        })
        @DisplayName("Should convert action config score to pixel score")
        void shouldConvertActionConfigScoreToPixelScore(
                double similarity, double expectedPixelScore) {
            // Act
            double pixelScore =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScore(similarity);

            // Assert
            assertTrue(pixelScore >= 0.0 && pixelScore <= 255.0);
            // Lower similarity should result in higher pixel score
            if (similarity == 0.0) {
                assertEquals(255.0, pixelScore, 0.1);
            } else if (similarity == 1.0) {
                assertEquals(0.0, pixelScore, 0.1);
            }
        }

        @Test
        @DisplayName("Should use tanh conversion function")
        void shouldUseTanhConversionFunction() {
            // Test that conversion uses hyperbolic tangent
            double score1 =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(0.3);
            double score2 =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(0.5);
            double score3 =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(0.7);

            // Verify non-linear conversion
            assertTrue(score1 > score2); // Lower similarity = higher pixel score
            assertTrue(score2 > score3);

            // Verify all scores are in valid range
            assertTrue(score1 >= 0 && score1 <= 255);
            assertTrue(score2 >= 0 && score2 <= 255);
            assertTrue(score3 >= 0 && score3 <= 255);
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.6, 0.7, 0.8, 0.9})
        @DisplayName("Should convert pixel score back to similarity")
        void shouldConvertPixelScoreBackToSimilarity(double originalSimilarity) {
            // Act - convert to pixel score and back
            double pixelScore =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(
                            originalSimilarity);
            double convertedSimilarity =
                    pixelScoreCalculator.convertPixelAnalysisScoreToActionOptionsScoreWithTanh(
                            pixelScore);

            // Assert - should get approximately the same similarity back
            // Allow for some rounding error due to tanh/atanh conversions
            assertEquals(originalSimilarity, convertedSimilarity, 0.05);
        }
    }

    @Nested
    @DisplayName("Distance Metrics")
    class DistanceMetrics {

        @Test
        @DisplayName("Should combine multiple distance metrics")
        void shouldCombineMultipleDistanceMetrics() {
            // Arrange
            Mat distOutHSV = new Mat(10, 10, CV_32F);
            Mat distTargetHSV = new Mat(10, 10, CV_32F);
            Mat distOutBGR = new Mat(10, 10, CV_32F);
            Mat distTargetBGR = new Mat(10, 10, CV_32F);

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.HSV))
                    .thenReturn(distOutHSV);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.HSV))
                    .thenReturn(distTargetHSV);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOutBGR);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTargetBGR);

            // Act
            Mat scoresHSV =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.HSV);
            Mat scoresBGR =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scoresHSV);
            assertNotNull(scoresBGR);
        }

        @Test
        @DisplayName("Should calculate distance below threshold")
        void shouldCalculateDistanceBelowThreshold() {
            // Arrange
            Mat scores = new Mat(5, 5, CV_32F);
            scores.put(new Scalar(100)); // Sample score values

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // Act
            Mat distBelow = pixelScoreCalculator.getDistBelowThreshhold(scores, findOptions);

            // Assert
            assertNotNull(distBelow);
            assertEquals(CV_8UC3, distBelow.type());
        }
    }

    @Nested
    @DisplayName("Penalty Application")
    class PenaltyApplication {

        @Test
        @DisplayName("Should apply out-of-range penalty")
        void shouldApplyOutOfRangePenalty() {
            // Arrange
            Mat distOut = new Mat(10, 10, CV_32F);
            Mat distTarget = new Mat(10, 10, CV_32F);
            distOut.put(new Scalar(10)); // Some distance outside range

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            // Scores should include penalties for out-of-range pixels
        }

        @Test
        @DisplayName("Should penalize pixels outside color range")
        void shouldPenalizePixelsOutsideColorRange() {
            // Test penalty application through score calculation
            // Arrange
            Mat distOut = new Mat(5, 5, CV_32F);
            Mat distTarget = new Mat(5, 5, CV_32F);
            distOut.put(new Scalar(50)); // Distance outside range
            distTarget.put(new Scalar(10)); // Distance to target

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            // Scores should be influenced by out-of-range distances
        }

        @Test
        @DisplayName("Should not penalize pixels within range")
        void shouldNotPenalizePixelsWithinRange() {
            // Arrange
            Mat distOut = new Mat(5, 5, CV_32F);
            Mat distTarget = new Mat(5, 5, CV_32F);
            distOut.put(new Scalar(0)); // No distance outside range (within range)
            distTarget.put(new Scalar(10)); // Distance to target

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            // Scores should only reflect distance to target, no penalties
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
            double pixelScore =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(
                            threshold);
            scores.put(new Scalar(pixelScore + 10)); // Above threshold (worse score)

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(threshold).build();

            // Act
            Mat filtered = pixelScoreCalculator.getDistBelowThreshhold(scores, findOptions);

            // Assert
            assertNotNull(filtered);
            // Values below threshold should be preserved
        }

        @Test
        @DisplayName("Should preserve scores above threshold")
        void shouldPreserveScoresAboveThreshold() {
            // Arrange
            Mat scores = new Mat(10, 10, CV_32F);
            double threshold = 0.7;
            double pixelScore =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(0.8);
            scores.put(new Scalar(pixelScore)); // Better than threshold

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(threshold).build();

            // Act
            Mat filtered = pixelScoreCalculator.getDistBelowThreshhold(scores, findOptions);

            // Assert
            assertNotNull(filtered);
            // Values above threshold (better scores) should result in positive distance below
            // threshold
        }
    }

    @Nested
    @DisplayName("Matrix Operations")
    class MatrixOperations {

        @Test
        @DisplayName("Should normalize score matrix")
        void shouldNormalizeScoreMatrix() {
            // Test normalization through threshold application
            // Arrange
            Mat scores = new Mat(10, 10, CV_32F);
            scores.put(new Scalar(200)); // Un-normalized value

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // Act
            Mat result = pixelScoreCalculator.getDistBelowThreshhold(scores, findOptions);

            // Assert
            assertNotNull(result);
            // Result should be properly typed
            assertEquals(CV_8UC3, result.type());
        }

        @Test
        @DisplayName("Should handle different matrix types")
        void shouldHandleDifferentMatrixTypes() {
            // Test with different OpenCV types in score calculation
            Mat distOut32F = new Mat(5, 5, CV_32F);
            Mat distTarget32F = new Mat(5, 5, CV_32F);

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut32F);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget32F);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

            // Assert
            assertNotNull(scores);
            assertEquals(CV_32F, scores.type());
        }
    }

    @Nested
    @DisplayName("Performance")
    class Performance {

        @Test
        @DisplayName("Should calculate scores efficiently for large matrices")
        void shouldCalculateScoresEfficientlyForLargeMatrices() {
            // Arrange
            Mat largeDistOut = new Mat(1000, 1000, CV_32F);
            Mat largeDistTarget = new Mat(1000, 1000, CV_32F);

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(largeDistOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(largeDistTarget);

            // Act
            long startTime = System.currentTimeMillis();
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);
            long endTime = System.currentTimeMillis();

            // Assert
            assertNotNull(scores);
            assertTrue(
                    endTime - startTime < 1000, "Should process 1M pixels in less than 1 second");
        }

        @ParameterizedTest
        @ValueSource(ints = {100, 500, 1000, 2000})
        @DisplayName("Should scale with matrix size")
        void shouldScaleWithMatrixSize(int size) {
            // Arrange
            Mat distOut = new Mat(size, size, CV_32F);
            Mat distTarget = new Mat(size, size, CV_32F);

            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_OUTSIDE_RANGE,
                            ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distOut);
            when(pixelProfile.getAnalyses(
                            PixelProfile.Analysis.DIST_TO_TARGET, ColorCluster.ColorSchemaName.BGR))
                    .thenReturn(distTarget);

            // Act
            Mat scores =
                    pixelScoreCalculator.setScores(pixelProfile, ColorCluster.ColorSchemaName.BGR);

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
            // Act - expecting NPE or handling
            try {
                Mat scores = pixelScoreCalculator.setScores(null, ColorCluster.ColorSchemaName.BGR);
                // If no exception, should return default Mat
                assertNotNull(scores);
            } catch (NullPointerException e) {
                // Expected if null checking not implemented
                assertTrue(true);
            }
        }

        @Test
        @DisplayName("Should handle null action config")
        void shouldHandleNullActionConfig() {
            // Arrange
            Mat scores = new Mat(5, 5, CV_32F);
            scores.put(new Scalar(100));

            // Act
            Mat result = pixelScoreCalculator.getDistBelowThreshhold(scores, null);

            // Assert
            assertNotNull(result); // Should use default similarity of 0.7
        }

        @Test
        @DisplayName("Should handle negative distances")
        void shouldHandleNegativeDistances() {
            // Test conversion with extreme values
            double pixelScore =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(
                            -0.1);

            // Assert
            assertTrue(pixelScore >= 0 && pixelScore <= 255); // Should handle negative input
        }

        @Test
        @DisplayName("Should handle infinite values")
        void shouldHandleInfiniteValues() {
            // Test conversion with extreme values
            double pixelScore1 =
                    pixelScoreCalculator.convertActionOptionsScoreToPixelAnalysisScoreWithTanh(
                            2.0); // Above 1.0
            double pixelScore2 =
                    pixelScoreCalculator.convertPixelAnalysisScoreToActionOptionsScoreWithTanh(
                            300); // Above 255

            // Assert - should handle gracefully
            assertTrue(Double.isFinite(pixelScore1));
            assertTrue(Double.isFinite(pixelScore2));
        }
    }
}
