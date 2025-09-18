package io.github.jspinak.brobot.action.internal.factory;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.action.internal.find.scene.SceneAnalysisCollectionBuilder;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

import lombok.Getter;
import lombok.Setter;

/**
 * Initializes and manages ActionResult objects throughout the action lifecycle.
 *
 * <p>MatchesInitializer serves as a factory for creating properly configured ActionResult instances
 * at the start of action execution. It ensures that all necessary components are initialized,
 * including:
 *
 * <ul>
 *   <li>Action lifecycle tracking with start time and max duration
 *   <li>Scene analysis for color-based operations
 *   <li>Action configuration and description
 * </ul>
 *
 * <p>This centralized initialization approach solves the complexity of ActionResult objects being
 * passed between multiple classes during execution. By establishing a consistent initialization
 * point, it ensures all actions start with properly configured result containers.
 *
 * <p><strong>Design rationale:</strong> ActionResult objects accumulate data throughout action
 * execution. Having a dedicated initializer ensures consistency and prevents missing initialization
 * steps that could cause runtime errors.
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
    private final TimeWrapper timeWrapper;

    /**
     * Constructs a MatchesInitializer with required dependencies.
     *
     * @param getSceneAnalysisCollection Service for creating scene analysis data
     * @param timeWrapper Time service for lifecycle tracking
     */
    public ActionResultFactory(
            SceneAnalysisCollectionBuilder getSceneAnalysisCollection, TimeWrapper timeWrapper) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.timeWrapper = timeWrapper;
    }

    // Removed ActionConfig-based init method - use ActionConfig version instead

    /**
     * Creates a fully initialized ActionResult for a new action execution using ActionConfig.
     *
     * <p>This method sets up all required components for action execution with the new ActionConfig
     * approach.
     *
     * @param actionConfig Configuration controlling action behavior
     * @param actionDescription Human-readable description for logging
     * @param objectCollections Target objects for the action (images, regions, etc.)
     * @return Fully initialized ActionResult ready for action execution
     */
    public ActionResult init(
            ActionConfig actionConfig,
            String actionDescription,
            ObjectCollection... objectCollections) {
        ActionResult matches = new ActionResult();

        // Get search duration from the config if it's a find-based action
        double maxWait = 10.0; // default
        if (actionConfig instanceof io.github.jspinak.brobot.action.basic.find.BaseFindOptions) {
            io.github.jspinak.brobot.action.basic.find.BaseFindOptions findOptions =
                    (io.github.jspinak.brobot.action.basic.find.BaseFindOptions) actionConfig;
            maxWait = findOptions.getSearchDuration();
        }

        matches.setActionLifecycle(new ActionLifecycle(timeWrapper.now(), maxWait));
        matches.setActionConfig(actionConfig);
        matches.setActionDescription(actionDescription);

        // For color find operations, create scene analysis
        SceneAnalyses sceneAnalysisCollection;
        if (actionConfig
                instanceof io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions) {
            io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions colorOptions =
                    (io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions)
                            actionConfig;
            sceneAnalysisCollection =
                    getSceneAnalysisCollection.get(
                            Arrays.asList(objectCollections), 1, 0.0, colorOptions);
        } else {
            sceneAnalysisCollection = new SceneAnalyses();
        }

        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        return matches;
    }

    // Removed ActionConfig-based init methods - use ActionConfig versions instead

    /**
     * Initializes ActionResult with ActionConfig and a list of object collections.
     *
     * <p>Convenience method that converts the list to varargs format.
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
