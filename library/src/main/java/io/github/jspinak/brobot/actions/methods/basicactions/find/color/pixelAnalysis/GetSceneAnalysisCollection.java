package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.manageStates.StateMemory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is primarily for color analysis. Other actions create Scenes during execution.
 */
@Component
public class GetSceneAnalysisCollection {

    private final GetScenes getScenes;
    private final AnalyzePixels analyzePixels;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;

    public GetSceneAnalysisCollection(GetScenes getScenes, AnalyzePixels analyzePixels,
                                      AllStatesInProjectService allStatesInProjectService, StateMemory stateMemory) {
        this.getScenes = getScenes;
        this.analyzePixels = analyzePixels;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
    }

    /**
     * Sets up a SceneAnalysisCollection with SceneAnalysis objects. Scenes can be
     * either a screenshot taken now or an image from the ObjectCollection.
     * @param objectCollections can contain scenes
     * @param actionOptions has options relevant for screen capture
     * @return the new SceneAnalysisCollection
     */
    public SceneAnalysisCollection get(List<ObjectCollection> objectCollections,
                                       int scenesToCapture, double secondsBetweenCaptures,
                                       ActionOptions actionOptions) {
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        List<Image> scenes = getScenes.getScenes(actionOptions, objectCollections, scenesToCapture, secondsBetweenCaptures);
        if (!isSceneAnalysisRequired(actionOptions)) {
            for (Image scene : scenes) {
                SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
                sceneAnalysisCollection.add(sceneAnalysis);
            }
            return sceneAnalysisCollection;
        }
        Set<StateImage> targetImages = getTargetImages(objectCollections);
        Set<StateImage> additionalImagesForClassification = getAdditionalImagesForClassification(objectCollections);
        Set<StateImage> allImages = new HashSet<>();
        allImages.addAll(targetImages);
        allImages.addAll(additionalImagesForClassification);
        for (Image scene : scenes) {
            SceneAnalysis sceneAnalysis = analyzePixels.getAnalysisForOneScene(scene, targetImages, allImages, actionOptions);
            sceneAnalysisCollection.add(sceneAnalysis);
        }
        return sceneAnalysisCollection;
    }

    /**
     * These images will be used for classification but not for matching. Only the target images
     * will be used to find matches. Having additional images for classification, when they are in
     * the scene, can make matching the target images more accurate.
     * @param objColls all object collection for this action
     * @return a set of additional images that will be used for classification
     */
    private Set<StateImage> getAdditionalImagesForClassification(List<ObjectCollection> objColls) {
        Set<StateImage> toClassify = new HashSet<>();
        if (BrobotSettings.includeStateImageObjectsFromActiveStatesInAnalysis) {
            allStatesInProjectService.findSetByName(stateMemory.getActiveStates()).forEach(
                    state -> toClassify.addAll(state.getStateImages()));
        }
        for (int i=1; i<objColls.size(); i++) {
            toClassify.addAll(objColls.get(i).getStateImages());
        }
        return toClassify;
    }

    public Set<StateImage> getTargetImages(List<ObjectCollection> images) {
        Set<StateImage> toClassify = new HashSet<>();
        if (!images.isEmpty() && !images.get(0).isEmpty()) {
            toClassify.addAll(images.get(0).getStateImages());
        }
        return toClassify;
    }

    private boolean isSceneAnalysisRequired(ActionOptions actionOptions) {
        if (actionOptions.getFind() == ActionOptions.Find.COLOR) return true;
        return false;
    }
}
