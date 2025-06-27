package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;

/**
 * Per-pixel color analysis results for scene-color profile matching.
 * 
 * <p>ColorAnalysis stores comprehensive pixel-level analysis data for evaluating
 * how well an image matches specific color profiles. It maintains separate analysis
 * matrices for both BGR and HSV color spaces, enabling robust color-based matching
 * that can leverage the strengths of each color representation.</p>
 * 
 * <p>Analysis types:
 * <ul>
 *   <li><b>DIST_TO_TARGET</b>: Per-pixel distance to target color (mean or k-means center)</li>
 *   <li><b>DIST_OUTSIDE_RANGE</b>: Distance beyond min/max boundaries</li>
 *   <li><b>DIST_TO_BOUNDARY</b>: Distance to nearest color range boundary</li>
 *   <li><b>SCORES</b>: Calculated matching scores based on distance metrics</li>
 *   <li><b>SCORE_DISTANCE</b>: Distance below scoring threshold</li>
 * </ul>
 * </p>
 * 
 * <p>Matrix structure:
 * <ul>
 *   <li>All matrices are 3-channel, matching the color space structure</li>
 *   <li>Each channel is analyzed independently</li>
 *   <li>Values represent distances or scores for each pixel</li>
 * </ul>
 * </p>
 * 
 * <p>Common workflows:
 * <ul>
 *   <li>K-means clustering: Each cluster center generates a separate analysis</li>
 *   <li>Histogram matching: Pixel-wise similarity to reference histograms</li>
 *   <li>Range-based filtering: Identifying pixels within color tolerances</li>
 *   <li>Score generation: Converting distances to matching confidence</li>
 * </ul>
 * </p>
 * 
 * @see ColorCluster
 * @see PixelProfile
 * @see KmeansProfile
 */
@Getter
public class ColorAnalysis {

    /**
     * Types of color analysis metrics available.
     * 
     * <p>Each analysis type represents a different way of measuring color similarity:</p>
     * <ul>
     *   <li><b>DIST_TO_TARGET</b>: Per-pixel Euclidean distance to target color center</li>
     *   <li><b>DIST_OUTSIDE_RANGE</b>: How far pixels exceed min/max boundaries (0 if within range)</li>
     *   <li><b>DIST_TO_BOUNDARY</b>: Signed distance to nearest range boundary (negative if inside)</li>
     *   <li><b>SCORES</b>: Normalized similarity scores (higher = better match)</li>
     *   <li><b>SCORE_DISTANCE</b>: Distance below score threshold (for filtering)</li>
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
     * Retrieves the analysis matrix for a specific metric and color space.
     * 
     * @param analysis the type of analysis to retrieve
     * @param colorSchemaName the color space (BGR or HSV)
     * @return Mat containing the analysis results, or null if not computed
     */
    public Mat getAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName) {
        return this.analyses.get(colorSchemaName).get(analysis);
    }

    /**
     * Stores an analysis matrix for a specific metric and color space.
     * 
     * <p>The provided Mat should be 3-channel with the same dimensions
     * as the analyzed image.</p>
     * 
     * @param analysis the type of analysis being stored
     * @param colorSchemaName the color space of the analysis
     * @param mat the analysis results matrix
     */
    public void setAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName, Mat mat) {
        this.analyses.get(colorSchemaName).put(analysis, mat);
    }

    /**
     * Prints dimensions of all analysis matrices for debugging.
     * 
     * <p>Outputs the dimensions of each analysis type for both BGR and HSV
     * color spaces. Useful for verifying that analysis has been performed
     * and matrices are properly sized.</p>
     * 
     * <p>Side effects: Outputs to the Report logging system</p>
     */
    public void print() {
        ConsoleReporter.println("PixelAnalysis");
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
