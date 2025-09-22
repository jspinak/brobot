package io.github.jspinak.brobot.action.internal.find.pixel;

import io.github.jspinak.brobot.util.image.visualization.ScoringVisualizer;

import static io.github.jspinak.brobot.model.analysis.color.PixelProfile.Analysis.SCORES;
import static org.bytedeco.opencv.global.opencv_core.*;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
// Removed old logging import: import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

// Remove incorrect atanh import - will use custom implementation

/**
 * Calculates pixel-level matching scores from color distance analysis.
 *
 * <p>PixelScoreCalculator transforms raw distance measurements into normalized similarity scores
 * suitable for pattern matching decisions. It implements sophisticated scoring algorithms that
 * balance accuracy with computational efficiency in color-based matching.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Combines multiple distance metrics into unified scores
 *   <li>Applies penalties for out-of-range colors
 *   <li>Converts between PixelAnalysis and configuration score scales
 *   <li>Supports threshold-based filtering
 * </ul>
 *
 * <p>Score conversion approach:
 *
 * <ul>
 *   <li>Uses hyperbolic tangent (tanh) function for non-linear mapping
 *   <li>Preserves SikuliX compatibility (0.7 default similarity)
 *   <li>Maps pixel distances (0-255) to similarity scores (0-1)
 *   <li>Lower pixel scores indicate higher similarity
 * </ul>
 *
 * <p>The tanh conversion provides better discrimination in the high-similarity range while
 * compressing differences at low similarities, matching human perception of visual similarity.
 *
 * @see PixelProfile
 * @see ActionConfig
 * @see DistanceMatrixCalculator
 */
@Component
public class PixelScoreCalculator {

    // private MatrixVisualizer matVisualize;

    private int outsideRangePenalty = 3; // 100

    // for score conversion
    private double bestScore =
            0; // the value is the same as the target, corresponds to ActionConfig score of 100
    private double worstScore = 255; // anything >= this is the same as an ActionConfig score of 0
    private double scoreRange = worstScore - bestScore;
    private double maxMinScoreForTanh = 3;
    private double maxTanh = Math.tanh(maxMinScoreForTanh);
    private double invMaxTanh = 1 - maxTanh;

    public PixelScoreCalculator(Object matVisualize) { // MatrixVisualizer temporarily replaced with Object
        // this.matVisualize = matVisualize;
    }

    /**
     * Inverse hyperbolic tangent function. atanh(x) = 0.5 * ln((1 + x) / (1 - x))
     *
     * @param x value between -1 and 1 (exclusive)
     * @return inverse hyperbolic tangent of x
     */
    private double atanh(double x) {
        if (Math.abs(x) >= 1.0) {
            // Handle edge cases
            return x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return 0.5 * Math.log((1 + x) / (1 - x));
    }

    /**
     * Calculates and stores pixel scores based on color analysis results.
     *
     * <p>Combines distance-to-target measurements with out-of-range penalties to produce
     * comprehensive similarity scores. Lower scores indicate higher similarity to the target color
     * profile.
     *
     * <p>Side effects: Updates the SCORES analysis in the pixelAnalysis object
     *
     * @param pixelAnalysis the analysis containing distance measurements
     * @param colorSchemaName the color space to process (BGR or HSV)
     * @return Mat containing pixel scores, or Mat(255) if analysis is missing
     */
    public Mat setScores(PixelProfile pixelAnalysis, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat distOut =
                pixelAnalysis.getAnalyses(
                        PixelProfile.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName);
        if (distOut == null) {
            return new Mat(new Scalar(255));
        }
        Mat pixelScores = new Mat(distOut.size(), distOut.type());
        Mat distTarget =
                pixelAnalysis.getAnalyses(PixelProfile.Analysis.DIST_TO_TARGET, colorSchemaName);
        addAnalysisToScores(distTarget, pixelScores);
        pixelAnalysis.setAnalyses(SCORES, colorSchemaName, pixelScores);
        return pixelScores;
    }

    /**
     * Calculates penalties for pixels outside the color range.
     *
     * @param distOut distance outside range matrix
     * @return penalty matrix scaled by outsideRangePenalty factor
     */
    private Mat getOutsideRangePenalties(Mat distOut) {
        Mat outsideRangePenalties = new Mat(distOut.size(), distOut.type());
        Mat penaltyMat = new Mat(distOut.size(), distOut.type(), Scalar.all(outsideRangePenalty));
        multiply(distOut, penaltyMat, outsideRangePenalties);
        return outsideRangePenalties;
    }

    /**
     * Adds analysis values to the cumulative score matrix.
     *
     * <p>Side effects: Modifies the scores matrix in-place
     *
     * @param analysis the analysis matrix to add
     * @param scores the score matrix to update
     */
    private void addAnalysisToScores(Mat analysis, Mat scores) {
        analysis.convertTo(analysis, scores.type());
        add(analysis, scores, scores);
    }

    /**
     * Converts ActionConfig similarity score to PixelAnalysis distance threshold.
     *
     * <p>Uses hyperbolic tangent function to create non-linear mapping that provides better
     * discrimination in the high-similarity range. The conversion ensures that ActionConfig score
     * of 0.7 (SikuliX default) maps appropriately.
     *
     * <p>Mathematical basis:
     *
     * <ul>
     *   <li>tanh(3) ≈ 0.995 (maps to pixel score ~255)
     *   <li>tanh(0.83) ≈ 0.70 (preserves SikuliX default)
     *   <li>tanh(0) = 0 (maps to pixel score 0)
     * </ul>
     *
     * @param actionConfigScore similarity score (0-1, higher is better match)
     * @return pixel distance threshold (0-255, lower is better match)
     */
    public double convertActionConfigScoreToPixelAnalysisScoreWithTanh(double actionConfigScore) {
        // Clamp input to valid range [0, 1] to handle edge cases
        actionConfigScore = Math.max(0, Math.min(1, actionConfigScore));

        double adjustedMinScore =
                actionConfigScore * maxMinScoreForTanh; // minSimilarity is now between 0 and
        // maxMinScoreForTanh
        double tanh = Math.tanh(adjustedMinScore); // tanh(1) = 0.7615941559557649, tanh(0) = 0
        double invertedTanh = 1 - tanh; // (1 - 0.7615941559557649) = 0.2384058440442351, 1 - 0 = 1
        double normalizedTanh =
                (invertedTanh - invMaxTanh) / (1 - invMaxTanh); // values between 0 and 1
        return normalizedTanh * scoreRange
                + bestScore; // values between bestScore and worstScore (0 and 255)
    }

    /**
     * Linear conversion from ActionConfig to PixelAnalysis scores.
     *
     * <p>Simple linear mapping without tanh transformation. Provided for comparison and backwards
     * compatibility.
     *
     * <p>Side effects: Outputs debug information to Report
     *
     * @param actionConfigScore similarity score (0-1)
     * @return pixel distance score (0-255)
     */
    public double convertActionConfigScoreToPixelAnalysisScore(double actionConfigScore) {
        return (1 - actionConfigScore) * scoreRange + bestScore;
    }

    /**
     * Converts PixelAnalysis distance scores to ActionConfig similarity scores.
     *
     * <p>Inverse of the tanh-based conversion, mapping pixel distances back to similarity scores
     * for use in match evaluation. Uses atanh (inverse hyperbolic tangent) to maintain the
     * non-linear characteristics of the forward conversion.
     *
     * <p>This conversion is used by ContourOps to evaluate match quality based on pixel-level color
     * analysis results.
     *
     * @param pixelAnalysisScore pixel distance score (0-255, lower is better)
     * @return similarity score (0-1, higher is better)
     */
    public double convertPixelAnalysisScoreToActionConfigScoreWithTanh(double pixelAnalysisScore) {
        // Clamp input to valid range
        pixelAnalysisScore = Math.max(bestScore, Math.min(worstScore, pixelAnalysisScore));

        // Reverse the forward conversion exactly
        // Forward: normalizedTanh * scoreRange + bestScore = pixelAnalysisScore
        // So: normalizedTanh = (pixelAnalysisScore - bestScore) / scoreRange
        double normalizedTanh =
                (pixelAnalysisScore - bestScore) / scoreRange; // values between 0 and 1

        // Forward: normalizedTanh = (invertedTanh - invMaxTanh) / (1 - invMaxTanh)
        // So: invertedTanh = normalizedTanh * (1 - invMaxTanh) + invMaxTanh
        double invertedTanh =
                normalizedTanh * (1 - invMaxTanh) + invMaxTanh; // values between invMaxTanh and 1

        // Forward: invertedTanh = 1 - tanh
        // So: tanh = 1 - invertedTanh
        double tanh = 1 - invertedTanh; // values between 0 and maxTanh

        // Ensure tanh value is within valid domain for atanh (-1, 1)
        // Add small epsilon to avoid domain edges
        double epsilon = 1e-10;
        tanh = Math.max(-1 + epsilon, Math.min(1 - epsilon, tanh));

        // Forward: tanh = Math.tanh(adjustedMinScore)
        // So: adjustedMinScore = atanh(tanh)
        double adjustedMinScore = atanh(tanh); // values between 0 and maxMinScoreForTanh

        // Forward: adjustedMinScore = actionConfigScore * maxMinScoreForTanh
        // So: actionConfigScore = adjustedMinScore / maxMinScoreForTanh
        return Math.max(0, Math.min(1, adjustedMinScore / maxMinScoreForTanh)); // clamp to [0, 1]
    }

    /**
     * Linear conversion from PixelAnalysis to ActionConfig scores.
     *
     * <p>Simple inverse linear mapping for backwards compatibility.
     *
     * @param pixelAnalysisScore pixel distance score (0-255)
     * @return similarity score (0-1)
     */
    public double convertPixelAnalysisScoreToActionConfigScore(double pixelAnalysisScore) {
        double scoreRange = worstScore - bestScore;
        return -((pixelAnalysisScore - bestScore) / (scoreRange)) + 1;
    }

    /**
     * Calculates distance below similarity threshold for filtering.
     *
     * <p>Creates a matrix where each pixel value represents how far below the similarity threshold
     * it falls. Pixels at or above threshold are set to 0. This can be used as a mask to identify
     * matching regions.
     *
     * <p>Lower pixel scores indicate higher similarity, so positive values in the result indicate
     * pixels that meet the similarity criteria.
     *
     * @param scores the pixel score matrix
     * @param actionConfig contains minSimilarity threshold
     * @return distance below threshold matrix (0 for non-matching pixels)
     */
    public Mat getDistBelowThreshhold(Mat scores, ActionConfig actionConfig) {
        double minSimilarity = 0.7; // default
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            minSimilarity = findOptions.getSimilarity();
        }
        double threshold = convertActionConfigScoreToPixelAnalysisScoreWithTanh(minSimilarity);

        // Create matrices with the same type as scores
        Mat thresholdMat = new Mat(scores.size(), scores.type());
        thresholdMat.put(Scalar.all(threshold));

        Mat distBelow = new Mat(scores.size(), scores.type());
        subtract(thresholdMat, scores, distBelow);

        // Convert to 8UC3 for visualization if needed
        Mat result = new Mat(scores.size(), CV_8UC3);
        if (scores.type() == CV_32F) {
            // Convert single channel float to 3-channel byte
            Mat temp = new Mat();
            distBelow.convertTo(temp, CV_8U);
            MatVector channels = new MatVector(3);
            for (int i = 0; i < 3; i++) {
                channels.put(i, temp);
            }
            merge(channels, result);
        } else {
            distBelow.convertTo(result, CV_8UC3);
        }

        return result;
    }
}
