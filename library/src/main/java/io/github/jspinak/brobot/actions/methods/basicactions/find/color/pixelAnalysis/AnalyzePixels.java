package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysis.Analysis.DIST_OUTSIDE_RANGE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysis.Analysis.DIST_TO_TARGET;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCENE;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.MEAN;

/**
 * This class is a central location for pixel analysis, for every color-based match strategy.
 */
@Component
public class AnalyzePixels {

    private GetDistanceMatrix getDistanceMatrix;
    private GetPixelAnalysisCollectionScores getPixelAnalysisCollectionScores;
    private SetKMeansProfiles setKMeansProfiles;
    private GetSceneAnalysisScores getSceneAnalysisScores;

    public AnalyzePixels(GetDistanceMatrix getDistanceMatrix, GetPixelAnalysisCollectionScores getPixelAnalysisCollectionScores,
                         SetKMeansProfiles setKMeansProfiles, GetSceneAnalysisScores getSceneAnalysisScores) {
        this.getDistanceMatrix = getDistanceMatrix;
        this.getPixelAnalysisCollectionScores = getPixelAnalysisCollectionScores;
        this.setKMeansProfiles = setKMeansProfiles;
        this.getSceneAnalysisScores = getSceneAnalysisScores;
    }

    /**
     * First, PixelAnalysis is performed on each scene with the ColorProfiles for the StateImageObject.
     * This gives us the similarity of each pixel to the color profiles. Using this information, scores are
     * calculated that tell us how likely the pixel is to belong to this StateImageObject.
     *
     * Pixel analysis is done on the entire scene first, without accounting for the search regions.
     * It can then be reused for subsequent searches.
     *
     * @param scene The scene to analyze.
     * @param stateImageObject The state image object to analyze.
     * @param actionOptions The action configuration.
     * @return The pixel scores for the scene.
     */
    public PixelAnalysisCollection getPixelAnalysisCollection(Scene scene, StateImageObject stateImageObject,
                                                    ActionOptions actionOptions) {
        List<ColorCluster> colorClusters = getColorProfiles(stateImageObject, actionOptions);
        PixelAnalysisCollection pixelAnalysisCollection = setPixelAnalyses(scene, colorClusters);
        pixelAnalysisCollection.setStateImageObject(stateImageObject);
        getPixelAnalysisCollectionScores.setScores(pixelAnalysisCollection, actionOptions);
        return pixelAnalysisCollection;
    }

    /**
     * Create ColorProfiles for a single k-means analysis (a specific # of clusters).
     * ColorProfiles comprising both BGR and HSV ColorSchemas are created from separate BGR and HSV k-means analyses,
     * which respectively contain only BGR and HSV Schemas.
     * @param img the StateImageObject with the k-means profiles
     * @param actionOptions the action configuration
     * @return a list of color profiles corresponding to a specific number of means
     */
    private List<ColorCluster> getColorProfiles(StateImageObject img, ActionOptions actionOptions) {
        if (actionOptions.getColor() == ActionOptions.Color.KMEANS ||
                actionOptions.getAction() == ActionOptions.Action.CLASSIFY) {
            int kMeans = actionOptions.getKmeans();
            return img.getDynamicImage().getInsideKmeansProfiles().getColorProfiles(kMeans);
        }
        else { // ActionOptions.Color.MU, or some other operation
            return Collections.singletonList(img.getColorCluster());
        }
    }

    /**
     * Create a PixelAnalysis for each ColorProfile.
     * PixelAnalysis records the similarity between pixels in the scene and a ColorProfile.
     * @param scene the scene to analyze
     * @param colorClusters the color profiles to use
     * @return a PixelAnalysisCollection containing the PixelAnalyses
     */
    public PixelAnalysisCollection setPixelAnalyses(Scene scene, List<ColorCluster> colorClusters) {
        PixelAnalysisCollection pixelAnalysisCollection = new PixelAnalysisCollection(scene);
        colorClusters.forEach(colorProfile ->
                setPixelAnalysisForOneColorProfile(pixelAnalysisCollection, colorProfile));
        return pixelAnalysisCollection;
    }

    private void setPixelAnalysisForOneColorProfile(PixelAnalysisCollection pixelAnalysisCollection,
                                                    ColorCluster colorCluster) {
        Mat sceneBGR = pixelAnalysisCollection.getAnalysis(SCENE, BGR);
        Mat sceneHSV = pixelAnalysisCollection.getAnalysis(SCENE, HSV);
        PixelAnalysis pixelAnalysis = new PixelAnalysis();
        Mat absDistBGR = getDistanceMatrix.getAbsDist(sceneBGR, BGR, colorCluster, MEAN);
        Mat absDistHSV = getDistanceMatrix.getAbsDist(sceneHSV, HSV, colorCluster, MEAN);
        pixelAnalysis.setAnalyses(DIST_TO_TARGET, BGR, absDistBGR);
        pixelAnalysis.setAnalyses(DIST_TO_TARGET, HSV, absDistHSV);
        Mat distOutsideBGR = getDistanceMatrix.getDistanceBelowMinAndAboveMax(sceneBGR, BGR, colorCluster);
        Mat distOutsideHSV = getDistanceMatrix.getDistanceBelowMinAndAboveMax(sceneHSV, HSV, colorCluster);
        pixelAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, BGR, distOutsideBGR);
        pixelAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, HSV, distOutsideHSV);
        pixelAnalysisCollection.add(pixelAnalysis);
    }

    /**
     * A SceneAnalysis comprises all analysis of a scene, for which there may be multiple StateImageObjects.
     * A PixelAnalysisCollection comprises all analysis of a {scene, StateImageObject} pair.
     * A SceneAnalysis therefore can hold a collection of PixelAnalysisCollections. There is a one-to-many
     * relationship between scenes and StateImageObjects.
     * Having a PixelAnalysisCollection for each StateImageObject, we can then compare the scores
     * for each pixel to determine which StateImageObject the pixel belongs to.
     * @param scene the scene to analyze
     * @param allImgs the images to match with the pixels in the scene
     * @param actionOptions the action configuration
     * @return a SceneAnalysis, containing a PixelsAnalysisCollection for each StateImageObject
     */
    public SceneAnalysis getAnalysisForOneScene(Scene scene, Set<StateImageObject> targetImgs, Set<StateImageObject> allImgs,
                                                ActionOptions actionOptions) {
        setKMeansProfiles.addKMeansIfNeeded(allImgs, actionOptions.getKmeans());
        List<PixelAnalysisCollection> pixelAnalysisCollections = new ArrayList<>();
        allImgs.forEach(img -> pixelAnalysisCollections.add(getPixelAnalysisCollection(scene, img, actionOptions)));
        SceneAnalysis sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, scene);
        getSceneAnalysisScores.setSceneAnalysisIndices(sceneAnalysis);
        getSceneAnalysisScores.setSceneAnalysisIndicesTargetsOnly(sceneAnalysis, targetImgs);
        getSceneAnalysisScores.setBGRVisualizationMats(sceneAnalysis);
        return sceneAnalysis;
    }

}
