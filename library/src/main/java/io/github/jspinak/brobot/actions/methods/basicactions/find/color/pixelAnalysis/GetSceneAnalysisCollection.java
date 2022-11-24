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

    private GetScenes getScenes;
    private AnalyzePixels analyzePixels;
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
        List<StateImageObject> targetImages = getTargetImages(objectCollections);
        List<StateImageObject> additionalImagesForClassification = getAdditionalImagesForClassification(objectCollections);
        Set<StateImageObject> allImages = new HashSet<>();
        allImages.addAll(targetImages);
        allImages.addAll(additionalImagesForClassification);
        for (Scene scene : scenes) {
            SceneAnalysis sceneAnalysis = analyzePixels.getAnalysisForOneScene(scene, allImages, actionOptions);
            sceneAnalysisCollection.add(sceneAnalysis);
        }
        return sceneAnalysisCollection;
    }

    private List<StateImageObject> getAdditionalImagesForClassification(List<ObjectCollection> images) {
        List<StateImageObject> toClassify = new ArrayList<>();
        if (!BrobotSettings.includeStateImageObjectsFromActiveStatesInAnalysis) return toClassify;
        if (images.size() < 2 || // there is no second ObjectCollection
                images.get(1).empty()) { // the second ObjectCollection exists but is empty
            Report.println("Adding all active state images to classification. Active states are " + stateMemory.getActiveStates());
            stateService.findSetByName(stateMemory.getActiveStates()).forEach(
                    state -> toClassify.addAll(state.getStateImages()));
        } else {
            toClassify.addAll(images.get(0).getStateImages());
        }
        return toClassify;
    }

    public List<StateImageObject> getTargetImages(List<ObjectCollection> images) {
        List<StateImageObject> toClassify = new ArrayList<>();
        if (images.size() > 0 && !images.get(0).empty()) {
            toClassify.addAll(images.get(0).getStateImages());
        }
        return toClassify;
    }
}
