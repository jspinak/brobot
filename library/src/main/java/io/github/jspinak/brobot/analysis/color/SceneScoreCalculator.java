package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.model.analysis.color.ColorStatistics;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.*;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;

/**
 * Processes pixel scores to generate scene-wide classification indices and visualizations.
 * 
 * <p>SceneScoreCalculator transforms pixel-level scoring data into comprehensive
 * scene classification results. It determines which state image best matches each
 * pixel position and creates visualization matrices for debugging and display.</p>
 * 
 * <p>Key processing steps:</p>
 * <ul>
 *   <li>Aggregates scores below threshold across all state images</li>
 *   <li>Assigns pixel indices based on best scoring state image</li>
 *   <li>Creates separate indices for all images vs. targets only</li>
 *   <li>Generates BGR visualizations colored by state image hue</li>
 * </ul>
 * 
 * <p>The classification process uses a "winner-takes-all" approach where
 * each pixel is assigned to the state image with the highest score below
 * the similarity threshold. Pixels with no matches remain unclassified
 * (index 0).</p>
 * 
 * @see SceneAnalysis
 * @see PixelProfiles
 * @see ColorMatrixUtilities
 * @see MatrixVisualizer
 */
@Component
public class SceneScoreCalculator {

    private ColorMatrixUtilities matOps3d;
    private MatrixVisualizer matVisualize;

    public SceneScoreCalculator(ColorMatrixUtilities matOps3d, MatrixVisualizer matVisualize) {
        this.matOps3d = matOps3d;
        this.matVisualize = matVisualize;
    }

    /**
     * Generates classification indices by finding the best-scoring state image for each pixel.
     * 
     * <p>Processes score matrices from all PixelAnalysisCollections to determine
     * pixel classification. For each pixel position, the state image with the
     * highest score below threshold is selected as the classification result.</p>
     * 
     * <p>The method operates on both BGR and HSV color spaces independently,
     * allowing for dual-space classification. Pixels with no scores below
     * threshold remain unclassified (index 0).</p>
     * 
     * <p>Side effects: Updates sceneAnalysis with:</p>
     * <ul>
     *   <li>INDICES_3D matrices for HSV and BGR</li>
     *   <li>INDICES_2D matrix (HSV first channel only)</li>
     * </ul>
     *
     * @param sceneAnalysis the scene containing state images and score data
     */
    public void setSceneAnalysisIndices(SceneAnalysis sceneAnalysis) {
        List<PixelProfiles> pixelAnalysisCollections = sceneAnalysis.getPixelAnalysisCollections();
        if (pixelAnalysisCollections.size() == 0) return;
        Mat scoresBelowThresholdHSV0 = pixelAnalysisCollections.get(0).getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, HSV);
        Mat scoreBelowThresholdBGR0 = pixelAnalysisCollections.get(0).getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR);
        Mat indicesHSV = new Mat(scoresBelowThresholdHSV0.size(), scoresBelowThresholdHSV0.type(), new Scalar(0, 0, 0, 0));
        Mat indicesBGR = new Mat(scoreBelowThresholdBGR0.size(), scoreBelowThresholdBGR0.type(), new Scalar(0, 0, 0, 0));
        Mat bestScoresHSV = new Mat(scoresBelowThresholdHSV0.size(), scoresBelowThresholdHSV0.type(), new Scalar(0, 0, 0, 0));
        Mat bestScoresBGR = new Mat(scoreBelowThresholdBGR0.size(), scoreBelowThresholdBGR0.type(), new Scalar(0, 0, 0, 0));
        for (int i = 0; i < pixelAnalysisCollections.size(); i++) {
            PixelProfiles pixelAnalysisCollection = pixelAnalysisCollections.get(i);
            Mat scoresBelowThresholdHSV = pixelAnalysisCollection.getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, HSV);
            Mat scoreBelowThresholdBGR = pixelAnalysisCollection.getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR);
            int index = pixelAnalysisCollection.getStateImage().getIndex();
            matOps3d.getIndicesOfMax(bestScoresHSV, scoresBelowThresholdHSV, indicesHSV, index);
            matOps3d.getIndicesOfMax(bestScoresBGR, scoreBelowThresholdBGR, indicesBGR, index);
        }
        sceneAnalysis.addAnalysis(HSV, INDICES_3D, indicesHSV);
        sceneAnalysis.addAnalysis(BGR, INDICES_3D, indicesBGR);
        Mat indices2D = MatrixUtilities.getFirstChannel(indicesHSV); // just set the 2D indices to the Hue indices of the 3D matrix
        sceneAnalysis.addAnalysis(HSV, INDICES_2D, indices2D);
    }

    /**
     * Creates filtered classification indices containing only target state images.
     * 
     * <p>Generates a subset of the full classification results that includes
     * only pixels classified as target images. Non-target classifications
     * are set to 0, effectively creating a mask of target matches.</p>
     * 
     * <p>This filtering enables focused matching operations that ignore
     * context images used only for classification accuracy.</p>
     * 
     * <p>Side effects: Updates sceneAnalysis with INDICES_3D_TARGETS
     * matrices for both HSV and BGR</p>
     * 
     * @param sceneAnalysis the scene with full classification indices
     * @param targets set of state images to include in filtered results
     */
    public void setSceneAnalysisIndicesTargetsOnly(SceneAnalysis sceneAnalysis, Set<StateImage> targets) {
        Set<Integer> targetIndices = targets.stream().map(StateImage::getIndex).collect(Collectors.toSet());
        Mat indicesHSV3D = sceneAnalysis.getAnalysis(HSV, INDICES_3D);
        Mat indicesBGR3D = sceneAnalysis.getAnalysis(BGR, INDICES_3D);
        Mat indicesHSV3Dtargets = matOps3d.getMatWithOnlyTheseIndices(indicesHSV3D, targetIndices);
        Mat indicesBGR3Dtargets = matOps3d.getMatWithOnlyTheseIndices(indicesBGR3D, targetIndices);
        sceneAnalysis.addAnalysis(HSV, INDICES_3D_TARGETS, indicesHSV3Dtargets);
        sceneAnalysis.addAnalysis(BGR, INDICES_3D_TARGETS, indicesBGR3Dtargets);
    }

    /**
     * Generates BGR color visualizations of classification results.
     * 
     * <p>Creates visual representations where each pixel is colored according
     * to the mean hue of its classified state image. This provides an intuitive
     * visualization of the classification results for debugging and display.</p>
     * 
     * <p>Generates two visualization types:</p>
     * <ul>
     *   <li>Full classification visualization (all state images)</li>
     *   <li>Target-only visualization (filtered to target images)</li>
     * </ul>
     * 
     * <p>Side effects: Updates sceneAnalysis with BGR_FROM_INDICES_2D
     * and BGR_FROM_INDICES_2D_TARGETS visualization matrices</p>
     * 
     * @param sceneAnalysis the scene with classification indices to visualize
     */
    public void setBGRVisualizationMats(SceneAnalysis sceneAnalysis) {
        Map<Integer, Scalar> hueList = getHueMap(sceneAnalysis);
        Mat hsv3D = sceneAnalysis.getAnalysis(HSV, INDICES_3D);
        if (hsv3D == null) return; // no indices to visualize
        Mat bgrColorMatFromHSV2dIndexMat = matVisualize.getBGRColorMatFromHSV2dIndexMat(hsv3D, hueList);
        sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D, bgrColorMatFromHSV2dIndexMat);
        Mat hsv3Dtargets = sceneAnalysis.getAnalysis(HSV, INDICES_3D_TARGETS);
        if (hsv3Dtargets == null) return; // no targets
        Mat hsvTargetsColorMat = matVisualize.getBGRColorMatFromHSV2dIndexMat(hsv3Dtargets, hueList);
        sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D_TARGETS, hsvTargetsColorMat);
    }

    /**
     * Creates a mapping from state image indices to their mean HSV colors.
     * 
     * <p>Builds a lookup table for visualization purposes, associating each
     * state image index with its characteristic hue. Only includes state
     * images that actually appear in the classification results.</p>
     * 
     * @param sceneAnalysis the scene containing state images and classification data
     * @return map from state image index to mean HSV color scalar
     */
    private Map<Integer, Scalar> getHueMap(SceneAnalysis sceneAnalysis) {
        Map<Integer, Scalar> hueList = new HashMap<>();
        Mat indicesHSV = sceneAnalysis.getAnalysis(HSV, INDICES_2D);
        for (StateImage img : sceneAnalysis.getStateImageObjects()) {
            if (MatrixUtilities.firstChannelContains(indicesHSV, img.getIndex())) {
                ColorStatistics colorInfo = img.getColorCluster().getSchema(HSV).getColorStatistics(MEAN);
                Scalar meanHSV = colorInfo.getMeanScalarHSV();
                hueList.put(img.getIndex(), meanHSV);
            }
        }
        return hueList;
    }
}
