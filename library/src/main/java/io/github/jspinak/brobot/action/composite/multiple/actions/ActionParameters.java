package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Pairs an ActionOptions configuration with its target ObjectCollection.
 * <p>
 * This data structure class represents a single action unit in the composite action
 * framework. It binds together the configuration parameters (ActionOptions) with the
 * target elements (ObjectCollection) on which the action should be performed. This
 * pairing is fundamental to the MultipleActions system, allowing different actions
 * with different configurations to be executed on different targets in sequence.
 * 
 * <p>Key characteristics:</p>
 * <ul>
 *   <li>Immutable after construction (fields are final via Lombok)</li>
 *   <li>Represents one atomic action in a composite sequence</li>
 *   <li>Allows each action to have independent configuration</li>
 *   <li>Enables targeting different GUI elements per action</li>
 * </ul>
 * 
 * <p>Common usage patterns:</p>
 * <ul>
 *   <li>Click action on button A, followed by type action on field B</li>
 *   <li>Find action with high similarity, followed by click with offset</li>
 *   <li>Mouse down on source, move to destination, mouse up at destination</li>
 * </ul>
 * 
 * <p>This class is typically not used directly but through {@link MultipleActionsObject}
 * which manages collections of these pairs.</p>
 * 
 * @see MultipleActionsObject
 * @see MultipleActions
 * @see ActionOptions
 * @see ObjectCollection
 * 
 * @deprecated Since version 2.0, use {@link ActionParametersV2} which supports ActionConfig,
 *             or preferably migrate to {@link io.github.jspinak.brobot.action.ActionChainOptions}
 *             for better action chaining capabilities.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
@Getter
public class ActionParameters {

    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    /**
     * Constructs a new action-target pair.
     * <p>
     * Creates an immutable binding between action configuration and target elements.
     * Both parameters are stored as-is without defensive copying, so callers should
     * ensure the objects are not modified after construction if immutability is desired.
     * 
     * @param actionOptions The configuration for the action to be performed, including
     *                      action type, timing parameters, and behavioral settings.
     *                      Must not be null.
     * @param objectCollection The collection of GUI elements (images, regions, locations,
     *                         strings) that the action will target. Must not be null.
     */
    public ActionParameters(ActionOptions actionOptions, ObjectCollection objectCollection) {
        this.actionOptions = actionOptions;
        this.objectCollection = objectCollection;
    }
}
