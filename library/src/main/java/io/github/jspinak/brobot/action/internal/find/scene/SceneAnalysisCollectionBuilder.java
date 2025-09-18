package io.github.jspinak.brobot.action.internal.find.scene;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.find.pixel.ColorAnalysisOrchestrator;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;

/**
 * Orchestrates the creation and population of scene analysis collections for color-based matching.
 *
 * <p>SceneAnalysisCollectionBuilder serves as the entry point for comprehensive color analysis
 * workflows in the Brobot framework. It coordinates scene acquisition, target selection, and
 * pixel-level analysis to produce collections ready for matching operations.
 *
 * <p>Primary responsibilities:
 *
 * <ul>
 *   <li>Acquire scenes through screenshots or provided images
 *   <li>Determine target images for matching
 *   <li>Select additional images for classification context
 *   <li>Coordinate pixel analysis across all scenes
 * </ul>
 *
 * <p>ObjectCollection usage pattern:
 *
 * <ul>
 *   <li><b>First collection</b>: Target images to find as matches
 *   <li><b>Second collection</b>: Additional classification context
 *   <li><b>Third+ collections</b>: Scenes to analyze
 * </ul>
 *
 * <p>When scene analysis is not required (non-color matching), scenes are simply wrapped in
 * SceneAnalysis objects without pixel processing.
 *
 * @see SceneAnalyses
 * @see ColorAnalysisOrchestrator
 * @see SceneProvider
 */
@Component
public class SceneAnalysisCollectionBuilder {

    private final BrobotProperties brobotProperties;
    private final SceneProvider getScenes;
    private final ColorAnalysisOrchestrator analyzePixels;
    private final StateService allStates;
    private final StateMemory stateMemory;

    @Autowired
    public SceneAnalysisCollectionBuilder(
            BrobotProperties brobotProperties,
            SceneProvider getScenes,
            ColorAnalysisOrchestrator analyzePixels,
            StateService allStates,
            StateMemory stateMemory) {
        this.brobotProperties = brobotProperties;
        this.getScenes = getScenes;
        this.analyzePixels = analyzePixels;
        this.allStates = allStates;
        this.stateMemory = stateMemory;
    }

    /**
     * Creates a collection of analyzed scenes for color-based matching operations.
     *
     * <p>Orchestrates the complete workflow from scene acquisition to pixel analysis:
     *
     * <ol>
     *   <li>Acquires scenes (screenshots or provided images)
     *   <li>Determines if color analysis is required
     *   <li>Identifies target and context images
     *   <li>Performs pixel analysis on each scene
     * </ol>
     *
     * <p>When color analysis is not required (non-COLOR find operations), scenes are wrapped
     * without pixel processing for efficiency.
     *
     * <p>Side effects: May capture screenshots if no scenes provided
     *
     * @param objectCollections collections containing targets, context, and scenes
     * @param scenesToCapture number of screenshots to take if no scenes provided
     * @param secondsBetweenCaptures delay between multiple screenshots
     * @param actionConfig configuration including find type and thresholds
     * @return SceneAnalysisCollection ready for matching operations
     */
    public SceneAnalyses get(
            List<ObjectCollection> objectCollections,
            int scenesToCapture,
            double secondsBetweenCaptures,
            ActionConfig actionConfig) {
        SceneAnalyses sceneAnalysisCollection = new SceneAnalyses();
        List<Scene> scenes =
                getScenes.getScenes(
                        actionConfig, objectCollections, scenesToCapture, secondsBetweenCaptures);
        if (!isSceneAnalysisRequired(actionConfig)) {
            for (Scene scene : scenes) {
                SceneAnalysis sceneAnalysis = new SceneAnalysis(scene);
                sceneAnalysisCollection.add(sceneAnalysis);
            }
            return sceneAnalysisCollection;
        }
        Set<StateImage> targetImages = getTargetImages(objectCollections);
        Set<StateImage> additionalImagesForClassification =
                getAdditionalImagesForClassification(objectCollections);
        Set<StateImage> allImages = new HashSet<>();
        allImages.addAll(targetImages);
        allImages.addAll(additionalImagesForClassification);
        for (Scene scene : scenes) {
            SceneAnalysis sceneAnalysis =
                    analyzePixels.getAnalysisForOneScene(
                            scene, targetImages, allImages, actionConfig);
            sceneAnalysisCollection.add(sceneAnalysis);
        }
        return sceneAnalysisCollection;
    }

    /**
     * Identifies context images to improve classification accuracy.
     *
     * <p>These images are used for classification but not for matching. Including context images
     * that appear in the scene improves the accuracy of target image matching by providing better
     * pixel classification boundaries.
     *
     * <p>Sources of context images:
     *
     * <ul>
     *   <li>All state images from currently active states (if configured)
     *   <li>Images from the second and subsequent ObjectCollections
     * </ul>
     *
     * @param objColls all object collections for this action
     * @return set of additional images for classification context
     */
    private Set<StateImage> getAdditionalImagesForClassification(List<ObjectCollection> objColls) {
        Set<StateImage> toClassify = new HashSet<>();
        if (brobotProperties.getAnalysis().isIncludeStateObjects()) {
            allStates
                    .findSetById(stateMemory.getActiveStates())
                    .forEach(state -> toClassify.addAll(state.getStateImages()));
        }
        for (int i = 1; i < objColls.size(); i++) {
            toClassify.addAll(objColls.get(i).getStateImages());
        }
        return toClassify;
    }

    /**
     * Extracts target images from the first ObjectCollection.
     *
     * <p>Target images are the primary patterns to find in the scene. These images will be both
     * classified and matched, generating concrete Match objects when found.
     *
     * <p>The first ObjectCollection by convention contains targets. An empty first collection
     * indicates classify-only mode with no specific matching targets.
     *
     * @param images list of ObjectCollections
     * @return set of target images to find, may be empty
     */
    public Set<StateImage> getTargetImages(List<ObjectCollection> images) {
        Set<StateImage> toClassify = new HashSet<>();
        if (!images.isEmpty() && !images.get(0).isEmpty()) {
            toClassify.addAll(images.get(0).getStateImages());
        }
        return toClassify;
    }

    /**
     * Determines if pixel-level color analysis is needed.
     *
     * <p>Scene analysis is only required when using COLOR-based find operations. Other find types
     * bypass pixel analysis for performance.
     *
     * @param actionConfig contains the find type
     * @return true if color analysis should be performed
     */
    private boolean isSceneAnalysisRequired(ActionConfig actionConfig) {
        // Check if this is a color-based find operation
        if (actionConfig instanceof PatternFindOptions) {
            // Could check for color-specific strategy here
            return false; // For now, simplified logic
        }
        return false;
    }
}
