package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class GetSceneAnalysis {

    private AnalyzePixels analyzePixels;
    private GetScenes getScenes;

    public GetSceneAnalysis(AnalyzePixels analyzePixels, GetScenes getScenes) {
        this.analyzePixels = analyzePixels;
        this.getScenes = getScenes;
    }

    /**
     * Gets the images, scenes, and timing information for the analysis.
     * With the necessary parameters, the method to perform scene analysis is called.
     * Depending on tbe parameters, multiple scenes can be analyzed with one method call.
     * @param actionOptions the action configuration
     * @param objectCollections the images to search are contained in the first collection
     * @param scenesToCapture the number of scenes to capture
     * @return the results of the analysis
     */
    public SceneAnalysisCollection getSceneAnalysisCollection(ActionOptions actionOptions,
                                                              List<ObjectCollection> objectCollections,
                                                              int scenesToCapture) {
        List<StateImageObject> images = objectCollections.get(0).getStateImages();
        double secsBtwCapture = actionOptions.getPauseBetweenIndividualActions();
        List<Scene> scenes = getScenes.getScenes(
                objectCollections, scenesToCapture, secsBtwCapture);
        return getSceneAnalysisCollection(scenes, images, actionOptions, secsBtwCapture);
    }

    /**
     * A SceneAnalysisCollection gives us a SceneAnalysis for each scene. A SceneAnalysis
     * tells us which pixels in a scene are likely to belong to one of the StateImageObjects.
     * @param scenes the scenes to analyze
     * @param stateImageObjects the images to use for classification
     * @param actionOptions the action configuration
     * @param secondsBetweenCaptures used for motion detection analysis
     * @return the results of the analysis
     */
    public SceneAnalysisCollection getSceneAnalysisCollection(List<Scene> scenes, List<StateImageObject> stateImageObjects,
                                                              ActionOptions actionOptions, double secondsBetweenCaptures) {
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        sceneAnalysisCollection.setSecondsBetweenScenes(secondsBetweenCaptures);
        for (Scene scene : scenes) {
            SceneAnalysis sceneAnalysis = analyzePixels.getAnalysisForOneScene(
                    scene, new HashSet<>(stateImageObjects), actionOptions);
            sceneAnalysisCollection.add(sceneAnalysis);
        }
        return sceneAnalysisCollection;
    }

}
