package io.github.jspinak.brobot.action.internal.find.pixel;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.model.analysis.color.PixelProfile.Analysis.SCORES;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.python.modules.math.atanh;

/**
 * Calculates pixel-level matching scores from color distance analysis.
 * 
 * <p>PixelScoreCalculator transforms raw distance measurements into normalized
 * similarity scores suitable for pattern matching decisions. It implements
 * sophisticated scoring algorithms that balance accuracy with computational
 * efficiency in color-based matching.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Combines multiple distance metrics into unified scores</li>
 *   <li>Applies penalties for out-of-range colors</li>
 *   <li>Converts between PixelAnalysis and ActionOptions score scales</li>
 *   <li>Supports threshold-based filtering</li>
 * </ul>
 * </p>
 * 
 * <p>Score conversion approach:
 * <ul>
 *   <li>Uses hyperbolic tangent (tanh) function for non-linear mapping</li>
 *   <li>Preserves SikuliX compatibility (0.7 default similarity)</li>
 *   <li>Maps pixel distances (0-255) to similarity scores (0-1)</li>
 *   <li>Lower pixel scores indicate higher similarity</li>
 * </ul>
 * </p>
 * 
 * <p>The tanh conversion provides better discrimination in the high-similarity
 * range while compressing differences at low similarities, matching human
 * perception of visual similarity.</p>
 * 
 * @see PixelProfile
 * @see ActionOptions
 * @see DistanceMatrixCalculator
 */
@Component
public class PixelScoreCalculator {

    private MatrixVisualizer matVisualize;

    private int outsideRangePenalty = 3; //100

    // for score conversion
    private double bestScore = 0; // the value is the same as the target, corresponds to ActionOptions score of 100
    private double worstScore = 255; // anything >= this is the same as an ActionOptions score of 0
    private double scoreRange = worstScore - bestScore;
    private double maxMinScoreForTanh = 3;
    private double maxTanh = Math.tanh(maxMinScoreForTanh);
    private double invMaxTanh = 1 - maxTanh;

    public PixelScoreCalculator(MatrixVisualizer matVisualize) {
        this.matVisualize = matVisualize;
    }

    /**
     * Calculates and stores pixel scores based on color analysis results.
     * 
     * <p>Combines distance-to-target measurements with out-of-range penalties
     * to produce comprehensive similarity scores. Lower scores indicate higher
     * similarity to the target color profile.</p>
     * 
     * <p>Side effects: Updates the SCORES analysis in the pixelAnalysis object</p>
     *
     * @param pixelAnalysis the analysis containing distance measurements
     * @param colorSchemaName the color space to process (BGR or HSV)
     * @return Mat containing pixel scores, or Mat(255) if analysis is missing
     */
    public Mat setScores(PixelProfile pixelAnalysis, ColorCluster.ColorSchemaName colorSchemaName) {
        Mat distOut = pixelAnalysis.getAnalyses(PixelProfile.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName);
        if (distOut == null) {
            ConsoleReporter.println("No scores Mat for "+colorSchemaName+" pixel analysis.");
            return new Mat(new Scalar(255));
        }
        Mat pixelScores = new Mat(distOut.size(), distOut.type());
        Mat distTarget = pixelAnalysis.getAnalyses(PixelProfile.Analysis.DIST_TO_TARGET, colorSchemaName);
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
     * <p>Side effects: Modifies the scores matrix in-place</p>
     * 
     * @param analysis the analysis matrix to add
     * @param scores the score matrix to update
     */
    private void addAnalysisToScores(Mat analysis, Mat scores) {
        analysis.convertTo(analysis, scores.type());
        add(analysis, scores, scores);
    }

    /**
     * Converts ActionOptions similarity score to PixelAnalysis distance threshold.
     * 
     * <p>Uses hyperbolic tangent function to create non-linear mapping that
     * provides better discrimination in the high-similarity range. The conversion
     * ensures that ActionOptions score of 0.7 (SikuliX default) maps appropriately.</p>
     * 
     * <p>Mathematical basis:
     * <ul>
     *   <li>tanh(3) ≈ 0.995 (maps to pixel score ~255)</li>
     *   <li>tanh(0.83) ≈ 0.70 (preserves SikuliX default)</li>
     *   <li>tanh(0) = 0 (maps to pixel score 0)</li>
     * </ul>
     * </p>
     *
     * @param actionOptionsScore similarity score (0-1, higher is better match)
     * @return pixel distance threshold (0-255, lower is better match)
     */
    public double convertActionOptionsScoreToPixelAnalysisScoreWithTanh(double actionOptionsScore) {
        double adjustedMinScore = actionOptionsScore * maxMinScoreForTanh; // minScore is now between 0 and maxMinScoreForTanh
        double tanh = Math.tanh(adjustedMinScore); // tanh(1) = 0.7615941559557649, tanh(0) = 0
        double invertedTanh = 1 - tanh; // (1 - 0.7615941559557649) = 0.2384058440442351, 1 - 0 = 1
        double normalizedTanh = (invertedTanh - invMaxTanh) / (1 - invMaxTanh); // values between 0 and 1
        return normalizedTanh * scoreRange + bestScore; // values between bestScore and worstScore (0 and 255)
    }

    /**
     * Linear conversion from ActionOptions to PixelAnalysis scores.
     * 
     * <p>Simple linear mapping without tanh transformation. Provided for
     * comparison and backwards compatibility.</p>
     * 
     * <p>Side effects: Outputs debug information to Report</p>
     * 
     * @param actionOptionsScore similarity score (0-1)
     * @return pixel distance score (0-255)
     */
    public double convertActionOptionsScoreToPixelAnalysisScore(double actionOptionsScore) {
        ConsoleReporter.println("scoreRange: "+scoreRange);
        ConsoleReporter.println("score" + actionOptionsScore);
        ConsoleReporter.println("bestScore" + bestScore);
        return (1 - actionOptionsScore) * scoreRange + bestScore;
    }

    /**
     * Converts PixelAnalysis distance scores to ActionOptions similarity scores.
     * 
     * <p>Inverse of the tanh-based conversion, mapping pixel distances back to
     * similarity scores for use in match evaluation. Uses atanh (inverse hyperbolic
     * tangent) to maintain the non-linear characteristics of the forward conversion.</p>
     * 
     * <p>This conversion is used by ContourOps to evaluate match quality based on
     * pixel-level color analysis results.</p>
     *
     * @param pixelAnalysisScore pixel distance score (0-255, lower is better)
     * @return similarity score (0-1, higher is better)
     */
    public double convertPixelAnalysisScoreToActionOptionsScoreWithTanh(double pixelAnalysisScore) {
        double normalized = (pixelAnalysisScore - worstScore) / scoreRange; // values between 0 and 1
        double invertedTanh = normalized * (1 - invMaxTanh) + invMaxTanh; // values between invMaxTanh and 1
        double tanh = 1 - invertedTanh; // [0, 1-invMaxTanh] lower pixel scores are better, higher similarity is better
        double adjustedMinScore = atanh(tanh); // values between 0 and maxMinScoreForTanh
        return adjustedMinScore / maxMinScoreForTanh; // values between 0 and 1
    }

    /**
     * Linear conversion from PixelAnalysis to ActionOptions scores.
     * 
     * <p>Simple inverse linear mapping for backwards compatibility.</p>
     * 
     * @param pixelAnalysisScore pixel distance score (0-255)
     * @return similarity score (0-1)
     */
    public double convertPixelAnalysisScoreToActionOptionsScore(double pixelAnalysisScore) {
        double scoreRange = worstScore - bestScore;
        return - ((pixelAnalysisScore - bestScore) / (scoreRange)) + 1;
    }

    /**
     * Calculates distance below similarity threshold for filtering.
     * 
     * <p>Creates a matrix where each pixel value represents how far below
     * the similarity threshold it falls. Pixels at or above threshold are
     * set to 0. This can be used as a mask to identify matching regions.</p>
     * 
     * <p>Lower pixel scores indicate higher similarity, so positive values
     * in the result indicate pixels that meet the similarity criteria.</p>
     *
     * @param scores the pixel score matrix
     * @param actionOptions contains minScore threshold
     * @return distance below threshold matrix (0 for non-matching pixels)
     */
    public Mat getDistBelowThreshhold(Mat scores, ActionOptions actionOptions) {
        double threshold = convertActionOptionsScoreToPixelAnalysisScoreWithTanh(actionOptions.getMinScore());
        Mat binaryScores = new Mat(scores.size(), CV_8UC3);
        Mat thresholdMat = new Mat(scores.size(), CV_8UC3, Scalar.all(threshold));
        subtract(thresholdMat, scores, binaryScores, null, scores.type());
        return binaryScores;
    }
}
