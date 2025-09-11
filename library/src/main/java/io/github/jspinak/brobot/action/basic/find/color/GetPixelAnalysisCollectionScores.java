package io.github.jspinak.brobot.action.basic.find.color;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static org.bytedeco.opencv.global.opencv_core.min;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.find.pixel.PixelScoreCalculator;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Aggregates pixel scores across multiple color profiles for comprehensive matching.
 *
 * <p>GetPixelAnalysisCollectionScores combines individual pixel analysis scores to create unified
 * similarity metrics for state image matching. When using k-means clustering or multiple color
 * profiles, this class ensures the best match (minimum score) is selected across all profiles.
 *
 * <p>Score aggregation process:
 *
 * <ul>
 *   <li>Collects scores from all PixelAnalysis objects (one per color profile)
 *   <li>Finds minimum scores at each pixel (best match wins)
 *   <li>Applies similarity thresholds from configuration
 *   <li>Generates distance-below-threshold metrics
 * </ul>
 *
 * <p>The aggregated scores enable:
 *
 * <ul>
 *   <li>Multi-profile matching (e.g., k-means with k>1)
 *   <li>Threshold-based filtering of poor matches
 *   <li>Unified scoring across BGR and HSV color spaces
 * </ul>
 *
 * @see PixelProfiles
 * @see PixelScoreCalculator
 * @see PixelProfile
 */
@Component
public class GetPixelAnalysisCollectionScores {

    private PixelScoreCalculator getPixelScores;

    public GetPixelAnalysisCollectionScores(PixelScoreCalculator getPixelScores) {
        this.getPixelScores = getPixelScores;
    }

    /**
     * Calculates aggregate scores across all color profiles.
     *
     * <p>Finds the minimum score at each pixel position across all PixelAnalysis objects. This
     * implements a "best match wins" strategy where pixels are scored by their closest color
     * profile.
     *
     * <p>Initializes scores to worst case (255) to ensure proper minimum calculation even for
     * pixels with no valid matches.
     *
     * @param pixelAnalysisCollection contains analyses for each color profile
     * @param colorSchemaName the color space to process (BGR or HSV)
     * @return matrix of minimum scores across all profiles
     */
    public Mat setScores(
            PixelProfiles pixelAnalysisCollection, ColorCluster.ColorSchemaName colorSchemaName) {
        if (pixelAnalysisCollection.getPixelAnalyses().isEmpty()) {
            ConsoleReporter.println("No pixel analyses to set scores for.");
            return new Mat();
        }
        List<PixelProfile> pixelAnalyses = pixelAnalysisCollection.getPixelAnalyses();
        PixelProfile firstAnalysis = pixelAnalyses.get(0);
        Size size =
                firstAnalysis
                        .getAnalyses(PixelProfile.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName)
                        .size();
        int type =
                firstAnalysis
                        .getAnalyses(PixelProfile.Analysis.DIST_OUTSIDE_RANGE, colorSchemaName)
                        .type();
        Mat scores =
                new Mat(size, type, new Scalar(255, 255, 255, 255)); // initialize to worst score
        for (PixelProfile pixelAnalysis : pixelAnalysisCollection.getPixelAnalyses()) {
            Mat pixelScores = getPixelScores.setScores(pixelAnalysis, colorSchemaName);
            min(scores, pixelScores, scores);
        }
        return scores;
    }

    /**
     * Computes and stores comprehensive scoring metrics.
     *
     * <p>Calculates scores for both BGR and HSV color spaces, then applies similarity thresholds to
     * identify matching regions. Results are stored in the PixelAnalysisCollection for later use.
     *
     * <p>Side effects: Updates the pixelAnalysisCollection with:
     *
     * <ul>
     *   <li>SCORE matrices for BGR and HSV
     *   <li>SCORE_DIST_BELOW_THRESHHOLD matrices for threshold filtering
     * </ul>
     *
     * @param pixelAnalysisCollection the collection to update with scores
     * @param actionConfig contains minSimilarity threshold configuration
     */
    public void setScores(PixelProfiles pixelAnalysisCollection, ActionConfig actionConfig) {
        Mat scoresBGR = setScores(pixelAnalysisCollection, BGR);
        Mat scoresHSV = setScores(pixelAnalysisCollection, HSV);
        pixelAnalysisCollection.setAnalyses(SCORE, BGR, scoresBGR);
        pixelAnalysisCollection.setAnalyses(SCORE, HSV, scoresHSV);
        Mat scoreDistBelowThresholdBGR =
                getPixelScores.getDistBelowThreshhold(scoresBGR, actionConfig);
        Mat scoreDistBelowThresholdHSV =
                getPixelScores.getDistBelowThreshhold(scoresHSV, actionConfig);
        pixelAnalysisCollection.setAnalyses(
                SCORE_DIST_BELOW_THRESHHOLD, BGR, scoreDistBelowThresholdBGR);
        pixelAnalysisCollection.setAnalyses(
                SCORE_DIST_BELOW_THRESHHOLD, HSV, scoreDistBelowThresholdHSV);
    }
}
