package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.manageStates.StateMemory;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GetSceneAnalysisCollection {

    private final GetScenes getScenes;
    private final AnalyzePixels analyzePixels;
    private final StateService stateService;
    private final StateMemory stateMemory;

    public GetSceneAnalysisCollection(GetScenes getScenes, AnalyzePixels analyzePixels,
                                      StateService stateService, StateMemory stateMemory) {
        this.getScenes = getScenes;
        this.analyzePixels = analyzePixels;
        this.stateService = stateService;
        this.stateMemory = stateMemory;
    }

    public SceneAnalysisCollection get(List<ObjectCollection> objectCollections, ActionOptions actionOptions) {
        return get(objectCollections, 1, 0, actionOptions);
    }

    public SceneAnalysisCollection get(List<ObjectCollection> objectCollections,
                                       int scenesToCapture, double secondsBetweenCaptures,
                                       ActionOptions actionOptions) {
        SceneAnalysisCollection sceneAnalysisCollection = new SceneAnalysisCollection();
        List<Scene> scenes = getScenes.getScenes(objectCollections, scenesToCapture, secondsBetweenCaptures);
        Set<StateImageObject> targetImages = getTargetImages(objectCollections);
        Set<StateImageObject> additionalImagesForClassification = getAdditionalImagesForClassification(objectCollections);
        Set<StateImageObject> allImages = new HashSet<>();
        allImages.addAll(targetImages);
        allImages.addAll(additionalImagesForClassification);
        for (Scene scene : scenes) {
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
    private Set<StateImageObject> getAdditionalImagesForClassification(List<ObjectCollection> objColls) {
        Set<StateImageObject> toClassify = new HashSet<>();
        if (BrobotSettings.includeStateImageObjectsFromActiveStatesInAnalysis) {
            stateService.findSetByName(stateMemory.getActiveStates()).forEach(
                    state -> toClassify.addAll(state.getStateImages()));
        }
        for (int i=1; i<objColls.size(); i++) {
            toClassify.addAll(objColls.get(i).getStateImages());
        }
        return toClassify;
    }

    public Set<StateImageObject> getTargetImages(List<ObjectCollection> images) {
        Set<StateImageObject> toClassify = new HashSet<>();
        if (images.size() > 0 && !images.get(0).empty()) {
            toClassify.addAll(images.get(0).getStateImages());
        }
        return toClassify;
    }
}
