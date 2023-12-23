package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;

/**
 * Holds the results of all per-pixel color analysis for a {scene, ColorProfile} pair.
 * A single image can have multiple color profiles, each corresponding to a k-Means center and a PixelAnalysis object.
 * For example, a k-Means of 3 will produce 3 color profiles for one image, and each of these
 * profiles will engender a separate PixelAnalysis object.
 * Scores can also correspond to histogram analysis, where each pixel is treated as the top left point of the
 * region used for the histogram and given a score based on its similarity to the histogram of the StateImage.
 */
@Getter
public class PixelAnalysis {

    public enum Analysis {
        DIST_TO_TARGET, DIST_OUTSIDE_RANGE, DIST_TO_BOUNDARY, SCORES, SCORE_DISTANCE
    }
    /**
     All Mats in this class are 3d and hold independent values for the 3 channels of the color schema.

     DIST_TO_TARGET: Contains the per-cell distance to a target color (color mean or k-means center).
     The matrix is 3d and each channel is compared to the corresponding channel of the target color.
     DIST_OUTSIDE_RANGE: The distance below the min value or above the max value.
     DIST_TO_BOUNDARY: Has the per-cell distance to the nearest boundary of the target color range.
     Distances are calculated with respect to the per-channel target and can be positive or negative numbers.
     This matrix is user-defined and depends on the boundaries used. It is not provided at the start of an app.
     It can be defined with getDistanceMatrix.getDistToNearest
     SCORES: Scores are subjective in that there are different methods that can produce the pixelScores matrix.
     Calculating scores usually involves referencing the above, objective Mats in this class.
     SCORE_DISTANCE: The distance below a threshold.
     */
    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analyses = new HashMap<>();
    {
        analyses.put(BGR, new HashMap<>());
        analyses.put(HSV, new HashMap<>());
    }

    public Mat getAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName) {
        return this.analyses.get(colorSchemaName).get(analysis);
    }

    public void setAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName, Mat mat) {
        this.analyses.get(colorSchemaName).put(analysis, mat);
    }

    public void print() {
        Report.println("PixelAnalysis");
        MatOps.printDimensions(getAnalyses(Analysis.DIST_TO_TARGET, BGR), "distanceToTargetColorBGR: ");
        MatOps.printDimensions(getAnalyses(Analysis.DIST_TO_TARGET, HSV), "distanceToTargetColorHSV: ");
        MatOps.printDimensions(getAnalyses(Analysis.DIST_OUTSIDE_RANGE, BGR), "distanceOutsideOfRangeBGR: ");
        MatOps.printDimensions(getAnalyses(Analysis.DIST_OUTSIDE_RANGE, HSV), "distanceOutsideOfRangeHSV: ");
        MatOps.printDimensions(getAnalyses(Analysis.SCORES, BGR), "pixelScoresBGR: ");
        MatOps.printDimensions(getAnalyses(Analysis.SCORES, HSV), "pixelScoresHSV: ");
        MatOps.printDimensions(getAnalyses(Analysis.SCORE_DISTANCE, BGR), "scoreDistanceBelowThresholdBGR: ");
        MatOps.printDimensions(getAnalyses(Analysis.SCORE_DISTANCE, HSV), "scoreDistanceBelowThresholdHSV: ");
    }
}
