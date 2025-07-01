package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

/**
 * Pairs an ActionConfig configuration with its target ObjectCollection.
 * <p>
 * ActionParametersV2 is the modern replacement for ActionParameters, using the new
 * ActionConfig hierarchy instead of ActionOptions. It represents a single action unit 
 * in the composite action framework, binding together the configuration parameters 
 * (ActionConfig) with the target elements (ObjectCollection) on which the action should 
 * be performed. This pairing is fundamental to the MultipleActions system, allowing 
 * different actions with different configurations to be executed on different targets 
 * in sequence.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.action.ActionChainOptions} instead.
 *             With ActionChainOptions, actions are chained using the builder pattern
 *             and ObjectCollections are passed directly to the execute method.
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
 * <p>Example usage:</p>
 * <pre>{@code
 * ActionParametersV2 clickParams = new ActionParametersV2(
 *     new ClickOptions.Builder().build(),
 *     new ObjectCollection.Builder().withImages(buttonImage).build()
 * );
 * 
 * ActionParametersV2 typeParams = new ActionParametersV2(
 *     new TypeOptions.Builder().setTypeDelay(0.1).build(),
 *     new ObjectCollection.Builder().withStrings("Hello World").build()
 * );
 * }</pre>
 * 
 * <p>This class is typically not used directly but through {@link MultipleActionsObjectV2}
 * which manages collections of these pairs.</p>
 * 
 * @see MultipleActionsObjectV2
 * @see MultipleActions
 * @see ActionConfig
 * @see ObjectCollection
 * @see ActionParameters
 */
@Deprecated
@Getter
public class ActionParametersV2 {

    private final ActionConfig actionConfig;
    private final ObjectCollection objectCollection;

    /**
     * Constructs a new action-target pair using ActionConfig.
     * <p>
     * Creates an immutable binding between action configuration and target elements.
     * Both parameters are stored as-is without defensive copying, so callers should
     * ensure the objects are not modified after construction if immutability is desired.
     * 
     * @param actionConfig The configuration for the action to be performed, including
     *                     action type, timing parameters, and behavioral settings.
     *                     Must not be null.
     * @param objectCollection The collection of GUI elements (images, regions, locations,
     *                         strings) that the action will target. Must not be null.
     */
    public ActionParametersV2(ActionConfig actionConfig, ObjectCollection objectCollection) {
        this.actionConfig = actionConfig;
        this.objectCollection = objectCollection;
    }
}