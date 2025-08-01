package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.type.KeyDownOptions;
import io.github.jspinak.brobot.action.basic.type.KeyUpOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;

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
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        return perform(findOptions, stateImages);
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
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        return perform(findOptions, objectCollections);
    }

    /**
     * Performs a Find action with a specified timeout before beginning the search.
     * <p>
     * This method is useful when you need to wait for UI elements to appear or
     * stabilize before attempting to find them. The timeout is applied as a pause
     * before the find operation begins, giving the application time to render or
     * update the UI.
     * </p>
     * 
     * <p>Example usage:
     * <pre>{@code
     * // Wait 2.5 seconds before searching for the save button
     * ActionResult result = action.findWithTimeout(2.5, saveButton);
     * 
     * // Wait 1 second before searching for multiple images
     * ActionResult results = action.findWithTimeout(1.0, loginButton, submitButton);
     * }</pre>
     * </p>
     *
     * @param timeoutSeconds the number of seconds to wait before beginning the find operation
     * @param stateImages the images to search for on screen after the timeout
     * @return ActionResult containing found matches and execution details
     * @see #find(StateImage...)
     * @see PatternFindOptions.Builder#setPauseBeforeBegin(double)
     */
    public ActionResult findWithTimeout(double timeoutSeconds, StateImage... stateImages) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(timeoutSeconds)
                .build();
        return perform(findOptions, stateImages);
    }

    /**
     * Performs a Find action with a specified timeout on the given object collections.
     * <p>
     * This method extends the timeout functionality to work with ObjectCollections,
     * allowing you to search for mixed object types (images, regions, text) after
     * waiting for a specified duration. This is particularly useful when dealing
     * with dynamic UIs or slow-loading content.
     * </p>
     * 
     * <p>The timeout helps in scenarios such as:
     * <ul>
     *   <li>Waiting for animations to complete</li>
     *   <li>Allowing time for AJAX requests to populate UI elements</li>
     *   <li>Ensuring dialogs or popups have fully rendered</li>
     *   <li>Synchronizing with application state changes</li>
     * </ul>
     * </p>
     *
     * @param timeoutSeconds the number of seconds to wait before beginning the find operation
     * @param objectCollections collections of objects to search for after the timeout
     * @return ActionResult containing all found matches across collections
     * @see #find(ObjectCollection...)
     * @see PatternFindOptions.Builder#setPauseBeforeBegin(double)
     */
    public ActionResult findWithTimeout(double timeoutSeconds, ObjectCollection... objectCollections) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setPauseBeforeBegin(timeoutSeconds)
                .build();
        return perform(findOptions, objectCollections);
    }

    /**
     * Performs a Click action with default options on the specified state images.
     * <p>
     * This convenience method simplifies the common pattern of clicking on images,
     * automatically finding and clicking on the first match found.
     *
     * @param stateImages the images to find and click
     * @return ActionResult containing the click operation results
     */
    public ActionResult click(StateImage... stateImages) {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        return perform(clickOptions, stateImages);
    }

    /**
     * Performs a Type action with default options using the specified object collections.
     * <p>
     * This method types text from StateString objects in the collections. If the
     * collections also contain images, it will first find and click on them before typing.
     *
     * @param objectCollections collections containing strings to type
     * @return ActionResult containing the type operation results
     */
    public ActionResult type(ObjectCollection... objectCollections) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        return perform(typeOptions, objectCollections);
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
     * Creates an appropriate ActionConfig implementation based on the action type.
     * This simplifies common operations where default behavior is sufficient.
     *
     * @param action the type of action to perform (CLICK, TYPE, etc.)
     * @param objectCollections target objects for the action
     * @return ActionResult containing matches and execution details
     * @deprecated Use specific ActionConfig implementations instead (e.g., ClickOptions, PatternFindOptions)
     */
    @Deprecated
    public ActionResult perform(ActionOptions.Action action, ObjectCollection... objectCollections) {
        ActionConfig config = createDefaultConfig(action);
        if (config != null) {
            return perform(config, objectCollections);
        }
        // Fall back to ActionOptions for actions without ActionConfig yet
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
     * @deprecated Use specific ActionConfig implementations instead (e.g., ClickOptions, PatternFindOptions)
     */
    @Deprecated
    public ActionResult perform(ActionOptions.Action action, StateImage... stateImages) {
        ActionConfig config = createDefaultConfig(action);
        if (config != null) {
            return perform(config, stateImages);
        }
        // Fall back to ActionOptions for actions without ActionConfig yet
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
     * @deprecated Use specific ActionConfig implementations instead (e.g., TypeOptions, MouseMoveOptions)
     */
    @Deprecated
    public ActionResult perform(ActionOptions.Action action) {
        ActionConfig config = createDefaultConfig(action);
        if (config != null) {
            return perform(config, new ObjectCollection.Builder().build());
        }
        // Fall back to ActionOptions for actions without ActionConfig yet
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
     * @deprecated Use specific ActionConfig implementations instead
     */
    @Deprecated
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
     * @deprecated Use specific ActionConfig implementations instead
     */
    @Deprecated
    public ActionResult perform(ActionOptions.Action action, Region... regions) {
        ObjectCollection strColl = new ObjectCollection.Builder().withRegions(regions).build();
        return perform(action, strColl);
    }
    
    // ===== New Convenience Methods for ActionType enum =====
    
    /**
     * Performs the specified action type on a location with default configuration.
     * <p>
     * This convenience method enables simple one-line calls like:
     * {@code action.perform(CLICK, location)}
     * </p>
     * <p>
     * The method automatically creates the appropriate ActionConfig based on
     * the ActionType and wraps the location in an ObjectCollection.
     * </p>
     * 
     * @param type the type of action to perform
     * @param location the location to act upon
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, Location location) {
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection = new ObjectCollection.Builder()
            .withLocations(location)
            .build();
        return perform(config, collection);
    }
    
    /**
     * Performs the specified action type on a region with default configuration.
     * <p>
     * This convenience method enables simple one-line calls like:
     * {@code action.perform(HIGHLIGHT, region)}
     * </p>
     * 
     * @param type the type of action to perform
     * @param region the region to act upon
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, Region region) {
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection = new ObjectCollection.Builder()
            .withRegions(region)
            .build();
        return perform(config, collection);
    }
    
    /**
     * Performs the specified action type with text input.
     * <p>
     * This convenience method enables simple one-line calls like:
     * {@code action.perform(TYPE, "Hello World")}
     * </p>
     * 
     * @param type the type of action to perform (typically TYPE)
     * @param text the text to type or use in the action
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, String text) {
        if (type == ActionType.TYPE) {
            // For typing, create TypeOptions and pass text in ObjectCollection
            TypeOptions typeOptions = new TypeOptions.Builder().build();
            ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings(text)
                .build();
            return perform(typeOptions, collection);
        }
        
        // For other actions, wrap text in ObjectCollection
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection = new ObjectCollection.Builder()
            .withStrings(text)
            .build();
        return perform(config, collection);
    }
    
    /**
     * Performs the specified action type on multiple objects with default configuration.
     * <p>
     * This is the most flexible convenience method, accepting any objects that
     * can be converted to the appropriate type for the action. The method will
     * attempt to extract locations, regions, or other required data from the
     * provided objects.
     * </p>
     * 
     * @param type the type of action to perform
     * @param objects the objects to act upon (Location, Region, StateRegion, Match, etc.)
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, Object... objects) {
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection.Builder builder = new ObjectCollection.Builder();
        
        // Process each object and add to collection
        for (Object obj : objects) {
            if (obj instanceof Location) {
                builder.withLocations((Location) obj);
            } else if (obj instanceof Region) {
                builder.withRegions((Region) obj);
            } else if (obj instanceof StateImage) {
                builder.withImages((StateImage) obj);
            } else if (obj instanceof String) {
                builder.withStrings((String) obj);
            } else if (obj instanceof ObjectCollection) {
                // Direct ObjectCollection - use as is
                return perform(config, (ObjectCollection) obj);
            }
            // Add more type conversions as needed
        }
        
        return perform(config, builder.build());
    }
    
    /**
     * Creates a default ActionConfig instance for the given ActionType.
     * <p>
     * Maps ActionType enum values to their corresponding ActionConfig
     * implementations with sensible defaults.
     * </p>
     * 
     * @param type the action type to create config for
     * @return appropriate ActionConfig instance
     * @throws IllegalArgumentException if action type is not yet supported
     * @since 2.0
     */
    private ActionConfig createDefaultConfig(ActionType type) {
        switch (type) {
            case CLICK:
                return new ClickOptions.Builder().build();
            case DOUBLE_CLICK:
                return new ClickOptions.Builder()
                    .setNumberOfClicks(2)
                    .build();
            case RIGHT_CLICK:
                return new ClickOptions.Builder()
                    .setPressOptions(MousePressOptions.builder()
                        .button(io.github.jspinak.brobot.model.action.MouseButton.RIGHT)
                        .build())
                    .build();
            case MIDDLE_CLICK:
                return new ClickOptions.Builder()
                    .setPressOptions(MousePressOptions.builder()
                        .button(io.github.jspinak.brobot.model.action.MouseButton.MIDDLE)
                        .build())
                    .build();
            case HIGHLIGHT:
                return new HighlightOptions.Builder().build();
            case TYPE:
                return new TypeOptions.Builder().build();
            case HOVER:
                return new MouseMoveOptions.Builder().build();
            case DRAG:
                return new DragOptions.Builder().build();
            case FIND:
                return new PatternFindOptions.Builder().build();
            case WAIT_VANISH:
                return new VanishOptions.Builder().build();
            case SCROLL_UP:
                return new ScrollOptions.Builder()
                    .setDirection(ScrollOptions.Direction.UP)
                    .build();
            case SCROLL_DOWN:
                return new ScrollOptions.Builder()
                    .setDirection(ScrollOptions.Direction.DOWN)
                    .build();
            case KEY_DOWN:
                return new KeyDownOptions.Builder().build();
            case KEY_UP:
                return new KeyUpOptions.Builder().build();
            case MOUSE_DOWN:
                return new MouseDownOptions.Builder().build();
            case MOUSE_UP:
                return new MouseUpOptions.Builder().build();
            default:
                throw new IllegalArgumentException(
                    "ActionType " + type + " is not yet supported in convenience methods");
        }
    }
    
    /**
     * Creates a default ActionConfig instance for the given action type.
     * <p>
     * This helper method maps legacy ActionOptions.Action enum values to their
     * corresponding ActionConfig implementations. Used to support backward
     * compatibility during migration.
     *
     * @param action the action type to create config for
     * @return appropriate ActionConfig instance or null if no mapping exists
     */
    private ActionConfig createDefaultConfig(ActionOptions.Action action) {
        switch (action) {
            case FIND:
                return new PatternFindOptions.Builder().build();
            case CLICK:
                return new ClickOptions.Builder().build();
            case TYPE:
                return new TypeOptions.Builder().build();
            case DRAG:
                return new DragOptions.Builder().build();
            case MOVE:
                return new MouseMoveOptions.Builder().build();
            case HIGHLIGHT:
                return new HighlightOptions.Builder().build();
            case DEFINE:
                return new DefineRegionOptions.Builder().build();
            case VANISH:
                return new VanishOptions.Builder().build();
            case SCROLL_MOUSE_WHEEL:
                return new ScrollOptions.Builder().build();
            case MOUSE_DOWN:
                return new MouseDownOptions.Builder().build();
            case MOUSE_UP:
                return new MouseUpOptions.Builder().build();
            case KEY_DOWN:
                return new KeyDownOptions.Builder().build();
            case KEY_UP:
                return new KeyUpOptions.Builder().build();
            case CLASSIFY:
                return new ColorFindOptions.Builder()
                        .setColorStrategy(ColorFindOptions.Color.CLASSIFICATION)
                        .build();
            // Actions without ActionConfig implementations yet
            case CLICK_UNTIL:
                return null;
            default:
                return null;
        }
    }

}
