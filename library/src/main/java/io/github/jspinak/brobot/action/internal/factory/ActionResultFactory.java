package io.github.jspinak.brobot.action.internal.factory;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Initializes and manages ActionResult objects throughout the action lifecycle.
 * <p>
 * MatchesInitializer serves as a factory for creating properly configured ActionResult
 * instances at the start of action execution. It ensures that all necessary components
 * are initialized, including:
 * <ul>
 * <li>Action lifecycle tracking with start time and max duration</li>
 * <li>Scene analysis for color-based operations</li>
 * <li>Action configuration and description</li>
 * </ul>
 * <p>
 * This centralized initialization approach solves the complexity of ActionResult objects
 * being passed between multiple classes during execution. By establishing a consistent
 * initialization point, it ensures all actions start with properly configured result
 * containers.
 * <p>
 * <strong>Design rationale:</strong> ActionResult objects accumulate data throughout
 * action execution. Having a dedicated initializer ensures consistency and prevents
 * missing initialization steps that could cause runtime errors.
 *
 * @see ActionResult
 * @see ActionLifecycle
 * @see SceneAnalyses
 */
@Component
@Getter
@Setter
public class ActionResultFactory {

    private final SceneAnalysisCollectionBuilder getSceneAnalysisCollection;
    private final TimeProvider time;

    /**
     * Constructs a MatchesInitializer with required dependencies.
     *
     * @param getSceneAnalysisCollection Service for creating scene analysis data
     * @param time Time service for lifecycle tracking
     */
    public ActionResultFactory(SceneAnalysisCollectionBuilder getSceneAnalysisCollection, TimeProvider time) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.time = time;
    }

    /**
     * Creates a fully initialized ActionResult for a new action execution.
     * <p>
     * This method sets up all required components for action execution:
     * <ul>
     * <li>Creates a new ActionLifecycle with current time and max wait duration</li>
     * <li>Stores the action configuration and description</li>
     * <li>For COLOR find operations, pre-generates scene analysis data</li>
     * </ul>
     * <p>
     * The initialized ActionResult serves as the accumulator for all data
     * generated during action execution, from matches found to timing information.
     *
     * @param actionOptions Configuration controlling action behavior
     * @param actionDescription Human-readable description for logging
     * @param objectCollections Target objects for the action (images, regions, etc.)
     * @return Fully initialized ActionResult ready for action execution
     */
    public ActionResult init(ActionOptions actionOptions, String actionDescription, ObjectCollection... objectCollections) {
        ActionResult matches = new ActionResult();
        matches.setActionLifecycle(new ActionLifecycle(time.now(), actionOptions.getMaxWait()));
        // ActionResult no longer uses ActionOptions, it uses ActionConfig
        // matches.setActionOptions(actionOptions);
        matches.setActionDescription(actionDescription);
        SceneAnalyses sceneAnalysisCollection = new SceneAnalyses();
        if (actionOptions.getFind() == ActionOptions.Find.COLOR) sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections), 1, 0, actionOptions);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        return matches;
    }

    /**
     * Creates a fully initialized ActionResult for a new action execution using ActionConfig.
     * <p>
     * This method sets up all required components for action execution with the new ActionConfig approach.
     *
     * @param actionConfig Configuration controlling action behavior
     * @param actionDescription Human-readable description for logging
     * @param objectCollections Target objects for the action (images, regions, etc.)
     * @return Fully initialized ActionResult ready for action execution
     */
    public ActionResult init(ActionConfig actionConfig, String actionDescription, ObjectCollection... objectCollections) {
        ActionResult matches = new ActionResult();
        // For now, use a default max wait time until ActionConfig includes this
        matches.setActionLifecycle(new ActionLifecycle(time.now(), 10.0));
        matches.setActionConfig(actionConfig);
        matches.setActionDescription(actionDescription);
        SceneAnalyses sceneAnalysisCollection = new SceneAnalyses();
        // Scene analysis collection will be handled differently with ActionConfig
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        return matches;
    }

    /**
     * Initializes ActionResult with a list of object collections.
     * <p>
     * Convenience method that converts the list to varargs format.
     * Uses an empty action description.
     *
     * @param actionOptions Configuration for the action
     * @param objectCollections List of target object collections
     * @return Initialized ActionResult
     */
    public ActionResult init(ActionOptions actionOptions, List<ObjectCollection> objectCollections) {
        return init(actionOptions, "", objectCollections.toArray(new ObjectCollection[0]));
    }

    /**
     * Initializes ActionResult without a description.
     * <p>
     * Simplified initialization for cases where action description
     * is not needed for logging or reporting.
     *
     * @param actionOptions Configuration for the action
     * @param objectCollections Target objects for the action
     * @return Initialized ActionResult
     */
    public ActionResult init(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return init(actionOptions, "", objectCollections);
    }
    
    /**
     * Initializes ActionResult with ActionConfig and a list of object collections.
     * <p>
     * Convenience method that converts the list to varargs format.
     *
     * @param actionConfig Configuration for the action
     * @param objectCollections List of target object collections
     * @return Initialized ActionResult
     */
    public ActionResult init(ActionConfig actionConfig, List<ObjectCollection> objectCollections) {
        return init(actionConfig, "", objectCollections.toArray(new ObjectCollection[0]));
    }

    /*
     * Implementation notes for action developers:
     * 
     * Each action iteration produces matches that must be properly merged into the
     * global ActionResult. Different actions have different merging strategies:
     * 
     * - VANISH: Records images that were previously found but now missing
     * - FIND: Adds all newly discovered match regions
     * - Others: May selectively merge specific data types
     * 
     * SceneAnalysisCollection starts with one SceneAnalysis object containing:
     * - Current screen capture (scene)
     * - Match illustrations
     * - Match list and contours
     * - Color/histogram analysis data
     * 
     * Most actions should modify the existing SceneAnalysis rather than creating
     * new ones, maintaining consistency in the analysis data.
     */

}
