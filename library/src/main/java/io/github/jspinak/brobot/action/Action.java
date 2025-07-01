package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Entry point for executing GUI automation actions in the Brobot model-based framework.
 * 
 * <p>The Action class serves as the central dispatcher for all GUI operations, implementing 
 * the Action Model (α) described in the theoretical foundations. It processes 
 * ActionOptions to determine what operation to perform and delegates execution to the 
 * appropriate action implementation using the action function f_α.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Parse ActionOptions to identify the requested action type</li>
 *   <li>Route execution to Basic or Composite action implementations</li>
 *   <li>Manage the action lifecycle and error handling</li>
 *   <li>Return comprehensive results via Matches objects</li>
 * </ul>
 * </p>
 * 
 * <p>Action types supported:
 * <ul>
 *   <li><b>Basic Actions</b>: Atomic operations like Find, Click, Type, Drag</li>
 *   <li><b>Composite Actions</b>: Complex operations that combine multiple basic actions</li>
 * </ul>
 * </p>
 * 
 * <p>This class abstracts the complexity of GUI interaction, allowing automation code to 
 * focus on what to do rather than how to do it. The model-based approach ensures actions 
 * are executed in the context of the current State, making them more reliable and robust.</p>
 * 
 * @since 1.0
 * @see ActionOptions
 * @see ActionInterface
 * @see BasicActionRegistry
 * @see CompositeActionRegistry
 * @see ActionResult
 * @author Joshua Spinak
 */
@Component
public class Action {

    private final ActionExecution actionExecution;
    private final ActionService actionService;
    private final ActionChainExecutor actionChainExecutor;

    /**
     * Constructs an Action instance with required dependencies.
     * <p>
     * Uses dependency injection to wire the action execution engine, service
     * layer, and chain executor. The ActionExecution handles the lifecycle management,
     * the ActionService provides the registry of available actions, and the
     * ActionChainExecutor handles chained action sequences.
     *
     * @param actionExecution handles action lifecycle, timing, and cross-cutting concerns
     * @param actionService provides access to registered action implementations
     * @param actionChainExecutor handles execution of chained action sequences
     */
    public Action(ActionExecution actionExecution, ActionService actionService, ActionChainExecutor actionChainExecutor) {
        this.actionExecution = actionExecution;
        this.actionService = actionService;
        this.actionChainExecutor = actionChainExecutor;
    }

    /**
     * Executes a GUI automation action with the specified options and target objects.
     * 
     * <p>This is the primary method for performing GUI operations in Brobot. It processes
     * the action configuration and executes it against the provided GUI elements.</p>
     * 
     * @param actionOptions     configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon (images, regions, locations, etc.)
     * @return a Matches object containing all results from the action execution
     */
    public ActionResult perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        return perform("", actionOptions, objectCollections);
    }
    
    /**
     * Executes a GUI automation action with the specified configuration and target objects.
     * 
     * <p>This method uses the new ActionConfig approach for more type-safe action configuration.</p>
     * 
     * @param actionConfig      configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon (images, regions, locations, etc.)
     * @return an ActionResult containing all results from the action execution
     */
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        return perform("", actionConfig, objectCollections);
    }

    /**
     * Executes a GUI automation action with a descriptive label for logging and debugging.
     * 
     * <p>This method adds a human-readable description to the action execution, which is
     * valuable for debugging, logging, and understanding automation flow. The description
     * appears in reports and illustrated histories.</p>
     * 
     * @param actionDescription human-readable description of what this action accomplishes
     * @param actionOptions     configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon
     * @return a Matches object containing all results from the action execution
     */
    public ActionResult perform(String actionDescription, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        for (ObjectCollection objColl : objectCollections) objColl.resetTimesActedOn();
        Optional<ActionInterface> action = actionService.getAction(actionOptions);
        if (action.isEmpty()) {
            ConsoleReporter.println("Not a valid Action.");
            return new ActionResult();
        }
        return actionExecution.perform(action.get(), actionDescription, actionOptions, objectCollections);
    }
    
    /**
     * Executes a GUI automation action with a descriptive label using ActionConfig.
     * 
     * <p>This method uses the new ActionConfig approach for more type-safe action configuration,
     * while still providing human-readable descriptions for debugging and logging.</p>
     * 
     * @param actionDescription human-readable description of what this action accomplishes
     * @param actionConfig      configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon
     * @return an ActionResult containing all results from the action execution
     */
    public ActionResult perform(String actionDescription, ActionConfig actionConfig, ObjectCollection... objectCollections) {
        for (ObjectCollection objColl : objectCollections) objColl.resetTimesActedOn();
        
        // Check if this config has subsequent actions chained
        if (!actionConfig.getSubsequentActions().isEmpty()) {
            // Build an ActionChainOptions from the config and its subsequent actions
            ActionChainOptions.Builder chainBuilder = new ActionChainOptions.Builder(actionConfig);
            for (ActionConfig nextConfig : actionConfig.getSubsequentActions()) {
                chainBuilder.then(nextConfig);
            }
            ActionChainOptions chainOptions = chainBuilder.build();
            
            // Execute the chain
            return actionChainExecutor.executeChain(chainOptions, new ActionResult(), objectCollections);
        }
        
        // Single action execution
        Optional<ActionInterface> action = actionService.getAction(actionConfig);
        if (action.isEmpty()) {
            ConsoleReporter.println("Not a valid Action for " + actionConfig.getClass().getSimpleName());
            return new ActionResult();
        }
        return actionExecution.perform(action.get(), actionDescription, actionConfig, objectCollections);
    }

    /**
     * Performs a Find action with default options on the specified images.
     * <p>
     * This convenience method simplifies the common case of searching for images
     * on screen. The images are automatically wrapped in an ObjectCollection and
     * searched using default Find parameters.
     *
     * @param stateImages the images to search for on screen
     * @return ActionResult containing found matches and execution details
     */
    public ActionResult find(StateImage... stateImages) {
        return perform(new ActionOptions(), stateImages);
    }

    /**
     * Performs a Find action with default options on the specified object collections.
     * <p>
     * This method allows finding multiple types of objects (images, regions, text)
     * in a single operation. Each ObjectCollection can contain different object types
     * that will be searched according to their specific matching logic.
     *
     * @param objectCollections collections of objects to search for
     * @return ActionResult containing all found matches across collections
     */
    public ActionResult find(ObjectCollection... objectCollections) {
        return perform(new ActionOptions(), objectCollections);
    }

    /**
     * Performs an action on state images with specified options.
     * <p>
     * This convenience method automatically wraps the provided StateImages into
     * an ObjectCollection, simplifying the API for image-based actions. All images
     * are grouped into a single collection for processing.
     *
     * @param actionOptions configuration specifying action type and parameters
     * @param stateImages target images to act upon
     * @return ActionResult containing operation results and execution details
     */
    public ActionResult perform(ActionOptions actionOptions, StateImage... stateImages) {
        return perform(actionOptions, new ObjectCollection.Builder().withImages(stateImages).build());
    }
    
    /**
     * Performs an action on state images with specified configuration.
     * <p>
     * This convenience method automatically wraps the provided StateImages into
     * an ObjectCollection, using the new ActionConfig approach.
     *
     * @param actionConfig configuration specifying action type and parameters
     * @param stateImages target images to act upon
     * @return ActionResult containing operation results and execution details
     */
    public ActionResult perform(ActionConfig actionConfig, StateImage... stateImages) {
        return perform(actionConfig, new ObjectCollection.Builder().withImages(stateImages).build());
    }

    /**
     * Performs an action with no target objects.
     * <p>
     * This method handles actions that don't require target objects, such as
     * global keyboard shortcuts, mouse movements to absolute positions, or
     * system-level operations. The empty ObjectCollection ensures proper method
     * resolution given Java's varargs ambiguity.
     *
     * @param actionOptions configuration for the action to perform
     * @return ActionResult containing execution details without match data
     */
    public ActionResult perform(ActionOptions actionOptions) {
        return perform(actionOptions, new ObjectCollection.Builder().build());
    }

    /**
     * Performs the specified action type with default configuration.
     * <p>
     * Creates an ActionOptions with default values for the given action type.
     * This simplifies common operations where default behavior is sufficient.
     *
     * @param action the type of action to perform (CLICK, TYPE, etc.)
     * @param objectCollections target objects for the action
     * @return ActionResult containing matches and execution details
     */
    public ActionResult perform(ActionOptions.Action action, ObjectCollection... objectCollections) {
        return perform(new ActionOptions.Builder().setAction(action).build(), objectCollections);
    }

    /**
     * Performs the specified action type on images with default configuration.
     * <p>
     * Combines action type specification with automatic ObjectCollection creation
     * for image targets. Useful for simple image-based operations like clicking
     * on buttons or icons.
     *
     * @param action the type of action to perform
     * @param stateImages target images for the action
     * @return ActionResult containing matches found and action outcomes
     */
    public ActionResult perform(ActionOptions.Action action, StateImage... stateImages) {
        return perform(new ActionOptions.Builder().setAction(action).build(), stateImages);
    }

    /**
     * Performs the specified action type with no targets and default options.
     * <p>
     * Handles actions that operate globally or use coordinates/text specified
     * in ActionOptions rather than target objects. Examples include typing text,
     * pressing keyboard shortcuts, or scrolling.
     *
     * @param action the type of action to perform
     * @return ActionResult with execution details but no match data
     */
    public ActionResult perform(ActionOptions.Action action) {
        return perform(new ActionOptions.Builder().setAction(action).build(), new ObjectCollection.Builder().build());
    }

    /**
     * Performs the specified action using text strings as targets.
     * <p>
     * Automatically creates an ObjectCollection containing the provided strings.
     * Useful for text-based actions like finding text on screen or typing into
     * fields identified by their text labels.
     *
     * @param action the type of action to perform
     * @param strings text strings to use as action targets
     * @return ActionResult containing text matches and action outcomes
     */
    public ActionResult perform(ActionOptions.Action action, String... strings) {
        ObjectCollection strColl = new ObjectCollection.Builder().withStrings(strings).build();
        return perform(action, strColl);
    }

    /**
     * Performs an action with custom options on text string targets.
     * <p>
     * Provides full control over action configuration while working with text
     * targets. The strings are automatically wrapped in an ObjectCollection.
     *
     * @param actionOptions detailed configuration for the action
     * @param strings text strings to use as action targets
     * @return ActionResult containing matches and execution details
     */
    public ActionResult perform(ActionOptions actionOptions, String... strings) {
        ObjectCollection strColl = new ObjectCollection.Builder().withStrings(strings).build();
        return perform(actionOptions, strColl);
    }

    /**
     * Performs the specified action on screen regions with default options.
     * <p>
     * Regions define specific areas of the screen to constrain the action.
     * This is useful for limiting searches to specific UI areas or performing
     * actions at predetermined screen locations.
     *
     * @param action the type of action to perform
     * @param regions screen areas to target or search within
     * @return ActionResult containing regional matches and outcomes
     */
    public ActionResult perform(ActionOptions.Action action, Region... regions) {
        ObjectCollection strColl = new ObjectCollection.Builder().withRegions(regions).build();
        return perform(action, strColl);
    }

}
