package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;

/**
 * Per-pixel color analysis results for individual color profiles.
 * 
 * <p>PixelProfile stores pixel-level analysis data for a single color profile
 * when matching against a scene. Unlike {@link ColorAnalysis} which may handle
 * multiple analyses, each PixelProfile instance represents the matching results
 * for one specific color profile (e.g., one k-means cluster center).</p>
 * 
 * <p>Analysis types stored:
 * <ul>
 *   <li><b>DIST_TO_TARGET</b>: Per-pixel distance to target color center</li>
 *   <li><b>DIST_OUTSIDE_RANGE</b>: Distance beyond min/max boundaries</li>
 *   <li><b>DIST_TO_BOUNDARY</b>: Distance to nearest color boundary</li>
 *   <li><b>SCORES</b>: Calculated matching scores from distances</li>
 *   <li><b>SCORE_DISTANCE</b>: Distance below score threshold</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>K-means clustering: One instance per cluster center</li>
 *   <li>Histogram analysis: Pixel-wise similarity scoring</li>
 *   <li>Single color profile matching</li>
 * </ul>
 * </p>
 * 
 * <p>All matrices are 3-channel, corresponding to the color space dimensions
 * (BGR or HSV), with each channel analyzed independently.</p>
 * 
 * @see PixelProfiles
 * @see ColorCluster
 * @see DistanceMatrixCalculator
 */
@Getter
public class PixelProfile {

    /**
     * Types of pixel-level analysis metrics.
     * 
     * <ul>
     *   <li><b>DIST_TO_TARGET</b>: Euclidean distance to target color</li>
     *   <li><b>DIST_OUTSIDE_RANGE</b>: How far pixels exceed boundaries</li>
     *   <li><b>DIST_TO_BOUNDARY</b>: Signed distance to nearest boundary</li>
     *   <li><b>SCORES</b>: Normalized similarity scores</li>
     *   <li><b>SCORE_DISTANCE</b>: Distance below threshold</li>
     * </ul>
     */
    public enum Analysis {
        DIST_TO_TARGET, DIST_OUTSIDE_RANGE, DIST_TO_BOUNDARY, SCORES, SCORE_DISTANCE
    }
    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analyses = new HashMap<>();
    {
        analyses.put(BGR, new HashMap<>());
        analyses.put(HSV, new HashMap<>());
    }

    /**
     * Retrieves a specific analysis matrix.
     * 
     * @param analysis the type of analysis to retrieve
     * @param colorSchemaName the color space (BGR or HSV)
     * @return the analysis Mat, or null if not computed
     */
    public Mat getAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName) {
        return this.analyses.get(colorSchemaName).get(analysis);
    }

    /**
     * Stores an analysis matrix.
     * 
     * <p>The Mat should be 3-channel with dimensions matching the analyzed scene.</p>
     * 
     * @param analysis the type of analysis being stored
     * @param colorSchemaName the color space of the analysis
     * @param mat the analysis results to store
     */
    public void setAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName, Mat mat) {
        this.analyses.get(colorSchemaName).put(analysis, mat);
    }

    /**
     * Prints dimensions of all analysis matrices.
     * 
     * <p>Outputs matrix dimensions for each analysis type in both BGR and HSV
     * color spaces. Useful for debugging and verification.</p>
     * 
     * <p>Side effects: Outputs to the Report logging system</p>
     */
    public void print() {
        ConsoleReporter.println("PixelProfile");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.DIST_TO_TARGET, BGR), "distanceToTargetColorBGR: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.DIST_TO_TARGET, HSV), "distanceToTargetColorHSV: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.DIST_OUTSIDE_RANGE, BGR), "distanceOutsideOfRangeBGR: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.DIST_OUTSIDE_RANGE, HSV), "distanceOutsideOfRangeHSV: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.SCORES, BGR), "pixelScoresBGR: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.SCORES, HSV), "pixelScoresHSV: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.SCORE_DISTANCE, BGR), "scoreDistanceBelowThresholdBGR: ");
        MatrixUtilities.printDimensions(getAnalyses(Analysis.SCORE_DISTANCE, HSV), "scoreDistanceBelowThresholdHSV: ");
    }
}
