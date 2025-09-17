package io.github.jspinak.brobot.action.internal.find.pixel;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfile.Analysis.DIST_OUTSIDE_RANGE;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfile.Analysis.DIST_TO_TARGET;
import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCENE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.color.GetPixelAnalysisCollectionScores;
import io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator;
import io.github.jspinak.brobot.analysis.color.SceneScoreCalculator;
import io.github.jspinak.brobot.analysis.color.profiles.KmeansProfileBuilder;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.jspinak.brobot.config.core.BrobotProperties;

/**
 * Central orchestrator for pixel-level color analysis in Brobot's vision system.
 *
 * <p>ColorAnalysisOrchestrator coordinates the complete color analysis pipeline, from extracting
 * color profiles to generating pixel-level similarity scores. It serves as the primary entry point
 * for all color-based matching strategies in the framework.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Extracting appropriate color profiles (k-means or mean-based)
 *   <li>Performing pixel-level distance calculations
 *   <li>Generating similarity scores for pattern matching
 *   <li>Managing scene-wide analysis for multiple state images
 * </ul>
 *
 * <p>Analysis workflow:
 *
 * <ol>
 *   <li>Extract color profiles from state images
 *   <li>Calculate pixel distances in BGR and HSV spaces
 *   <li>Generate similarity scores based on distances
 *   <li>Aggregate results for scene-wide classification
 * </ol>
 *
 * <p>This class supports both individual image matching and multi-class classification scenarios,
 * making it versatile for various computer vision tasks in GUI automation.
 *
 * @see PixelProfiles
 * @see SceneAnalysis
 * @see DistanceMatrixCalculator
 * @see GetPixelAnalysisCollectionScores
 */
@Component
public class ColorAnalysisOrchestrator {

    @Autowired
    private BrobotProperties brobotProperties;

    private DistanceMatrixCalculator getDistanceMatrix;
    private GetPixelAnalysisCollectionScores getPixelAnalysisCollectionScores;
    private KmeansProfileBuilder setKMeansProfiles;
    private SceneScoreCalculator getSceneAnalysisScores;

    public ColorAnalysisOrchestrator(
            DistanceMatrixCalculator getDistanceMatrix,
            GetPixelAnalysisCollectionScores getPixelAnalysisCollectionScores,
            KmeansProfileBuilder setKMeansProfiles,
            SceneScoreCalculator getSceneAnalysisScores) {
        this.getDistanceMatrix = getDistanceMatrix;
        this.getPixelAnalysisCollectionScores = getPixelAnalysisCollectionScores;
        this.setKMeansProfiles = setKMeansProfiles;
        this.getSceneAnalysisScores = getSceneAnalysisScores;
    }

    /**
     * Analyzes a scene against a state image's color profiles.
     *
     * <p>Performs comprehensive pixel-level analysis by:
     *
     * <ol>
     *   <li>Extracting appropriate color profiles based on action options
     *   <li>Calculating pixel distances to each color profile
     *   <li>Generating similarity scores for pattern matching
     * </ol>
     *
     * <p>The analysis covers the entire scene without region restrictions, allowing results to be
     * reused for multiple searches with different search regions.
     *
     * @param scene the scene containing pixels to analyze
     * @param stateImage the target state image with color profiles
     * @param actionConfig configuration determining analysis type (k-means or mean)
     * @return PixelAnalysisCollection containing similarity scores for all pixels
     */
    public PixelProfiles getPixelAnalysisCollection(
            Scene scene, StateImage stateImage, ActionConfig actionConfig) {
        List<ColorCluster> colorClusters = getColorProfiles(stateImage, actionConfig);
        PixelProfiles pixelAnalysisCollection = setPixelAnalyses(scene, colorClusters);
        pixelAnalysisCollection.setStateImage(stateImage);
        getPixelAnalysisCollectionScores.setScores(pixelAnalysisCollection, actionConfig);
        return pixelAnalysisCollection;
    }

    /**
     * Extracts color profiles based on the analysis strategy.
     *
     * <p>Selects between two profile extraction methods:
     *
     * <ul>
     *   <li><b>K-means</b>: Returns multiple profiles from k-means clustering
     *   <li><b>Mean</b>: Returns single profile based on overall color statistics
     * </ul>
     *
     * <p>For k-means analysis, profiles contain both BGR and HSV schemas derived from separate
     * clustering operations in each color space.
     *
     * @param img state image containing pre-computed color profiles
     * @param actionConfig determines profile extraction method
     * @return list of color profiles (multiple for k-means, single for mean)
     */
    private List<ColorCluster> getColorProfiles(StateImage img, ActionConfig actionConfig) {
        // Simplified logic - check config type to determine profile method
        String configType = actionConfig.getClass().getSimpleName();
        if (configType.contains("Classify") || configType.contains("Color")) {
            int kMeans = brobotProperties.getAnalysis().getKMeansInProfile(); // Use default k-means value
            return img.getKmeansProfilesAllSchemas().getColorProfiles(kMeans);
        } else { // Default to mean color profile
            return Collections.singletonList(img.getColorCluster());
        }
    }

    /**
     * Creates pixel-level analysis for multiple color profiles.
     *
     * <p>Generates a {@link PixelProfile} object for each color profile, recording pixel-by-pixel
     * similarity measurements. This enables multi-profile matching strategies like k-means
     * clustering.
     *
     * @param scene the scene containing pixels to analyze
     * @param colorClusters list of color profiles to match against
     * @return PixelAnalysisCollection aggregating all individual analyses
     */
    public PixelProfiles setPixelAnalyses(Scene scene, List<ColorCluster> colorClusters) {
        PixelProfiles pixelAnalysisCollection = new PixelProfiles(scene);
        colorClusters.forEach(
                colorProfile ->
                        setPixelAnalysisForOneColorProfile(pixelAnalysisCollection, colorProfile));
        return pixelAnalysisCollection;
    }

    private void setPixelAnalysisForOneColorProfile(
            PixelProfiles pixelAnalysisCollection, ColorCluster colorCluster) {
        Mat sceneBGR = pixelAnalysisCollection.getAnalysis(SCENE, BGR);
        Mat sceneHSV = pixelAnalysisCollection.getAnalysis(SCENE, HSV);
        PixelProfile pixelAnalysis = new PixelProfile();
        Mat absDistBGR = getDistanceMatrix.getAbsDist(sceneBGR, BGR, colorCluster, MEAN);
        Mat absDistHSV = getDistanceMatrix.getAbsDist(sceneHSV, HSV, colorCluster, MEAN);
        pixelAnalysis.setAnalyses(DIST_TO_TARGET, BGR, absDistBGR);
        pixelAnalysis.setAnalyses(DIST_TO_TARGET, HSV, absDistHSV);
        Mat distOutsideBGR =
                getDistanceMatrix.getDistanceBelowMinAndAboveMax(sceneBGR, BGR, colorCluster);
        Mat distOutsideHSV =
                getDistanceMatrix.getDistanceBelowMinAndAboveMax(sceneHSV, HSV, colorCluster);
        pixelAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, BGR, distOutsideBGR);
        pixelAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, HSV, distOutsideHSV);
        pixelAnalysisCollection.add(pixelAnalysis);
    }

    /**
     * Performs comprehensive scene analysis for multi-class classification.
     *
     * <p>Creates a complete analysis of a scene against multiple state images, enabling pixel-level
     * classification. Each pixel can be assigned to the state image it most closely matches based
     * on color similarity.
     *
     * <p>The analysis process:
     *
     * <ol>
     *   <li>Ensures required k-means profiles exist for all images
     *   <li>Creates PixelAnalysisCollection for each state image
     *   <li>Calculates classification indices for pixel assignment
     *   <li>Generates visualization matrices for debugging
     * </ol>
     *
     * <p>Side effects: May trigger k-means profile generation if needed
     *
     * @param scene the scene to analyze
     * @param targetImgs primary images for focused analysis
     * @param allImgs complete set of images for classification
     * @param actionConfig configuration including k-means settings
     * @return SceneAnalysis containing analyses for all state images
     */
    public SceneAnalysis getAnalysisForOneScene(
            Scene scene,
            Set<StateImage> targetImgs,
            Set<StateImage> allImgs,
            ActionConfig actionConfig) {
        setKMeansProfiles.addKMeansIfNeeded(allImgs, brobotProperties.getAnalysis().getKMeansInProfile());
        List<PixelProfiles> pixelAnalysisCollections = new ArrayList<>();
        allImgs.forEach(
                img ->
                        pixelAnalysisCollections.add(
                                getPixelAnalysisCollection(scene, img, actionConfig)));
        SceneAnalysis sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, scene);
        getSceneAnalysisScores.setSceneAnalysisIndices(sceneAnalysis);
        if (!targetImgs.isEmpty())
            getSceneAnalysisScores.setSceneAnalysisIndicesTargetsOnly(sceneAnalysis, targetImgs);
        getSceneAnalysisScores.setBGRVisualizationMats(sceneAnalysis);
        return sceneAnalysis;
    }
}
