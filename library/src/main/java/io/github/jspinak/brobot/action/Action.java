package io.github.jspinak.brobot.action;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.type.KeyDownOptions;
import io.github.jspinak.brobot.action.basic.type.KeyUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.action.internal.execution.ActionExecution;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Entry point for executing GUI automation actions in the Brobot model-based framework.
 *
 * <p>The Action class serves as the central dispatcher for all GUI operations, implementing the
 * Action Model (α) described in the theoretical foundations. It processes ActionConfig to determine
 * what operation to perform and delegates execution to the appropriate action implementation using
 * the action function f_α.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Parse ActionConfig to identify the requested action type
 *   <li>Route execution to Basic or Composite action implementations
 *   <li>Manage the action lifecycle and error handling
 *   <li>Return comprehensive results via Matches objects
 * </ul>
 *
 * <p>Action types supported:
 *
 * <ul>
 *   <li><b>Basic Actions</b>: Atomic operations like Find, Click, Type, Drag
 *   <li><b>Composite Actions</b>: Complex operations that combine multiple basic actions
 * </ul>
 *
 * <p>This class abstracts the complexity of GUI interaction, allowing automation code to focus on
 * what to do rather than how to do it. The model-based approach ensures actions are executed in the
 * context of the current State, making them more reliable and robust.
 *
 * @since 1.0
 * @see ActionConfig
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
     *
     * <p>Uses dependency injection to wire the action execution engine, service layer, and chain
     * executor. The ActionExecution handles the lifecycle management, the ActionService provides
     * the registry of available actions, and the ActionChainExecutor handles chained action
     * sequences.
     *
     * @param actionExecution handles action lifecycle, timing, and cross-cutting concerns
     * @param actionService provides access to registered action implementations
     * @param actionChainExecutor handles execution of chained action sequences
     */
    public Action(
            ActionExecution actionExecution,
            ActionService actionService,
            ActionChainExecutor actionChainExecutor) {
        this.actionExecution = actionExecution;
        this.actionService = actionService;
        this.actionChainExecutor = actionChainExecutor;
    }

    // Removed ActionOptions-based perform method - use ActionConfig instead

    /**
     * Executes a GUI automation action with the specified configuration and target objects.
     *
     * <p>This method uses the new ActionConfig approach for more type-safe action configuration.
     *
     * @param actionConfig configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon (images, regions, locations, etc.)
     * @return an ActionResult containing all results from the action execution
     */
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        return perform("", actionConfig, objectCollections);
    }

    // Removed ActionOptions-based perform method with description - use ActionConfig instead

    /**
     * Executes a GUI automation action with a descriptive label using ActionConfig.
     *
     * <p>This method uses the new ActionConfig approach for more type-safe action configuration,
     * while still providing human-readable descriptions for debugging and logging.
     *
     * @param actionDescription human-readable description of what this action accomplishes
     * @param actionConfig configuration specifying the action type and parameters
     * @param objectCollections target GUI elements to act upon
     * @return an ActionResult containing all results from the action execution
     */
    public ActionResult perform(
            String actionDescription,
            ActionConfig actionConfig,
            ObjectCollection... objectCollections) {
        // Handle null parameters gracefully
        if (actionConfig == null) {
            ActionResult result = new ActionResult();
            result.setSuccess(false);
            result.setActionDescription("Failed: ActionConfig is null");
            return result;
        }

        if (objectCollections == null) {
            objectCollections = new ObjectCollection[0];
        }

        for (ObjectCollection objColl : objectCollections) {
            if (objColl != null) {
                objColl.resetTimesActedOn();
            }
        }

        // Check if this config has subsequent actions chained
        if (!actionConfig.getSubsequentActions().isEmpty()) {
            // Build an ActionChainOptions from the config and its subsequent actions
            ActionChainOptions.Builder chainBuilder = new ActionChainOptions.Builder(actionConfig);
            for (ActionConfig nextConfig : actionConfig.getSubsequentActions()) {
                chainBuilder.then(nextConfig);
            }
            ActionChainOptions chainOptions = chainBuilder.build();

            // Execute the chain
            return actionChainExecutor.executeChain(
                    chainOptions, new ActionResult(), objectCollections);
        }

        // Single action execution
        Optional<ActionInterface> action = actionService.getAction(actionConfig);
        if (action.isEmpty()) {
            ConsoleReporter.println(
                    "Not a valid Action for " + actionConfig.getClass().getSimpleName());
            return new ActionResult();
        }
        return actionExecution.perform(
                action.get(), actionDescription, actionConfig, objectCollections);
    }

    /**
     * Performs a Find action with default options on the specified images.
     *
     * <p>This convenience method simplifies the common case of searching for images on screen. The
     * images are automatically wrapped in an ObjectCollection and searched using default Find
     * parameters.
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
     *
     * <p>This method allows finding multiple types of objects (images, regions, text) in a single
     * operation. Each ObjectCollection can contain different object types that will be searched
     * according to their specific matching logic.
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
     *
     * <p>This method is useful when you need to wait for UI elements to appear or stabilize before
     * attempting to find them. The timeout is applied as a pause before the find operation begins,
     * giving the application time to render or update the UI.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * // Wait 2.5 seconds before searching for the save button
     * ActionResult result = action.findWithTimeout(2.5, saveButton);
     *
     * // Wait 1 second before searching for multiple images
     * ActionResult results = action.findWithTimeout(1.0, loginButton, submitButton);
     * }</pre>
     *
     * @param timeoutSeconds the number of seconds to wait before beginning the find operation
     * @param stateImages the images to search for on screen after the timeout
     * @return ActionResult containing found matches and execution details
     * @see #find(StateImage...)
     * @see PatternFindOptions.Builder#setPauseBeforeBegin(double)
     */
    public ActionResult findWithTimeout(double timeoutSeconds, StateImage... stateImages) {
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setPauseBeforeBegin(timeoutSeconds).build();
        return perform(findOptions, stateImages);
    }

    /**
     * Performs a Find action with a specified timeout on the given object collections.
     *
     * <p>This method extends the timeout functionality to work with ObjectCollections, allowing you
     * to search for mixed object types (images, regions, text) after waiting for a specified
     * duration. This is particularly useful when dealing with dynamic UIs or slow-loading content.
     *
     * <p>The timeout helps in scenarios such as:
     *
     * <ul>
     *   <li>Waiting for animations to complete
     *   <li>Allowing time for AJAX requests to populate UI elements
     *   <li>Ensuring dialogs or popups have fully rendered
     *   <li>Synchronizing with application state changes
     * </ul>
     *
     * @param timeoutSeconds the number of seconds to wait before beginning the find operation
     * @param objectCollections collections of objects to search for after the timeout
     * @return ActionResult containing all found matches across collections
     * @see #find(ObjectCollection...)
     * @see PatternFindOptions.Builder#setPauseBeforeBegin(double)
     */
    public ActionResult findWithTimeout(
            double timeoutSeconds, ObjectCollection... objectCollections) {
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setPauseBeforeBegin(timeoutSeconds).build();
        return perform(findOptions, objectCollections);
    }

    /**
     * Performs a Click action with default options on the specified state images.
     *
     * <p>This convenience method simplifies the common pattern of clicking on images, automatically
     * finding and clicking on the first match found. When StateImages are provided, this method
     * performs a Find operation first, then clicks on the found matches.
     *
     * @param stateImages the images to find and click
     * @return ActionResult containing the click operation results
     */
    public ActionResult click(StateImage... stateImages) {
        // When clicking on StateImages, we need to find them first, then click
        // Use ConditionalActionChain for proper find-then-click behavior
        if (stateImages.length == 1) {
            // Use the convenience method for single image
            return ConditionalActionChain
                    .find(stateImages[0])
                    .ifFoundClick()
                    .perform(this);
        } else {
            // For multiple images, create a chain with ObjectCollection
            ConditionalActionChain chain = ConditionalActionChain
                    .find(new PatternFindOptions.Builder().build());
            // Add the ObjectCollection as the first action's target
            ObjectCollection collection = new ObjectCollection.Builder()
                    .withImages(stateImages)
                    .build();
            return chain.ifFoundClick()
                    .perform(this, collection);
        }
    }

    /**
     * Performs a Click action with default options on the specified object collections.
     *
     * <p>This method handles clicking on various object types. For StateImages, it performs
     * a Find operation first, then clicks on the found matches. For Locations and Regions,
     * it clicks directly without finding.
     *
     * @param objectCollections collections containing objects to click
     * @return ActionResult containing the click operation results
     */
    public ActionResult click(ObjectCollection... objectCollections) {
        // Check if collections contain StateImages that need to be found first
        boolean hasImages = false;
        if (objectCollections != null) {
            for (ObjectCollection collection : objectCollections) {
                if (collection != null && !collection.getStateImages().isEmpty()) {
                    hasImages = true;
                    break;
                }
            }
        }

        if (hasImages) {
            // If there are images, use find-then-click pattern
            ConditionalActionChain chain = ConditionalActionChain
                    .find(new PatternFindOptions.Builder().build());
            return chain.ifFoundClick()
                    .perform(this, objectCollections);
        } else {
            // For locations/regions, click directly without finding
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            return perform(clickOptions, objectCollections);
        }
    }

    /**
     * Performs a Type action with default options using the specified object collections.
     *
     * <p>This method types text from StateString objects in the collections. If the collections
     * also contain images, it will first find and click on them before typing.
     *
     * @param objectCollections collections containing strings to type
     * @return ActionResult containing the type operation results
     */
    public ActionResult type(ObjectCollection... objectCollections) {
        TypeOptions typeOptions = new TypeOptions.Builder().build();
        return perform(typeOptions, objectCollections);
    }

    // Removed ActionOptions-based perform method for StateImages - use ActionConfig instead

    /**
     * Performs an action on state images with specified configuration.
     *
     * <p>This convenience method automatically wraps the provided StateImages into an
     * ObjectCollection, using the new ActionConfig approach.
     *
     * @param actionConfig configuration specifying action type and parameters
     * @param stateImages target images to act upon
     * @return ActionResult containing operation results and execution details
     */
    public ActionResult perform(ActionConfig actionConfig, StateImage... stateImages) {
        return perform(
                actionConfig, new ObjectCollection.Builder().withImages(stateImages).build());
    }

    // Removed ActionOptions-based perform method without objects - use ActionConfig instead

    /**
     * Performs the specified action type with default configuration.
     *
     * <p>Creates an appropriate ActionConfig implementation based on the action type. This
     * simplifies common operations where default behavior is sufficient.
     *
     * @param action the type of action to perform (CLICK, TYPE, etc.)
     * @param objectCollections target objects for the action
     * @return ActionResult containing matches and execution details
     * @deprecated Use specific ActionConfig implementations instead (e.g., ClickOptions,
     *     PatternFindOptions)
     */
    // Removed deprecated ActionType methods - use ActionType instead

    /**
     * Performs the specified action type on images with default configuration.
     *
     * <p>Combines action type specification with automatic ObjectCollection creation for image
     * targets. Useful for simple image-based operations like clicking on buttons or icons.
     *
     * @param action the type of action to perform
     * @param stateImages target images for the action
     * @return ActionResult containing matches found and action outcomes
     * @deprecated Use specific ActionConfig implementations instead (e.g., ClickOptions,
     *     PatternFindOptions)
     */
    // Removed deprecated ActionType methods - use ActionType instead

    /**
     * Performs the specified action type with no targets and default options.
     *
     * <p>Handles actions that operate globally or use coordinates/text specified in ActionConfig
     * rather than target objects. Examples include typing text, pressing keyboard shortcuts, or
     * scrolling.
     *
     * @param action the type of action to perform
     * @return ActionResult with execution details but no match data
     * @deprecated Use specific ActionConfig implementations instead (e.g., TypeOptions,
     *     MouseMoveOptions)
     */
    // Removed deprecated ActionType methods - use ActionType instead

    /**
     * Performs the specified action using text strings as targets.
     *
     * <p>Automatically creates an ObjectCollection containing the provided strings. Useful for
     * text-based actions like finding text on screen or typing into fields identified by their text
     * labels.
     *
     * @param action the type of action to perform
     * @param strings text strings to use as action targets
     * @return ActionResult containing text matches and action outcomes
     * @deprecated Use specific ActionConfig implementations instead
     */
    @Deprecated
    public ActionResult perform(ActionType action, String... strings) {
        ObjectCollection strColl = new ObjectCollection.Builder().withStrings(strings).build();
        return perform(action, strColl);
    }

    /**
     * Performs an action with custom options on text string targets.
     *
     * <p>Provides full control over action configuration while working with text targets. The
     * strings are automatically wrapped in an ObjectCollection.
     *
     * @param actionConfig detailed configuration for the action
     * @param strings text strings to use as action targets
     * @return ActionResult containing matches and execution details
     */
    // Removed ActionOptions-based perform method for strings - use ActionConfig instead

    /**
     * Performs the specified action on screen regions with default options.
     *
     * <p>Regions define specific areas of the screen to constrain the action. This is useful for
     * limiting searches to specific UI areas or performing actions at predetermined screen
     * locations.
     *
     * @param action the type of action to perform
     * @param regions screen areas to target or search within
     * @return ActionResult containing regional matches and outcomes
     * @deprecated Use specific ActionConfig implementations instead
     */
    @Deprecated
    public ActionResult perform(ActionType action, Region... regions) {
        ObjectCollection strColl = new ObjectCollection.Builder().withRegions(regions).build();
        return perform(action, strColl);
    }

    // ===== New Convenience Methods for ActionType enum =====

    /**
     * Performs the specified action type on a location with default configuration.
     *
     * <p>This convenience method enables simple one-line calls like: {@code action.perform(CLICK,
     * location)}
     *
     * <p>The method automatically creates the appropriate ActionConfig based on the ActionType and
     * wraps the location in an ObjectCollection.
     *
     * @param type the type of action to perform
     * @param location the location to act upon
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, Location location) {
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection =
                new ObjectCollection.Builder().withLocations(location).build();
        return perform(config, collection);
    }

    /**
     * Performs the specified action type on a region with default configuration.
     *
     * <p>This convenience method enables simple one-line calls like: {@code
     * action.perform(HIGHLIGHT, region)}
     *
     * @param type the type of action to perform
     * @param region the region to act upon
     * @return ActionResult containing the operation results
     * @since 2.0
     */
    public ActionResult perform(ActionType type, Region region) {
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection = new ObjectCollection.Builder().withRegions(region).build();
        return perform(config, collection);
    }

    /**
     * Performs the specified action type with text input.
     *
     * <p>This convenience method enables simple one-line calls like: {@code action.perform(TYPE,
     * "Hello World")}
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
            ObjectCollection collection = new ObjectCollection.Builder().withStrings(text).build();
            return perform(typeOptions, collection);
        }

        // For other actions, wrap text in ObjectCollection
        ActionConfig config = createDefaultConfig(type);
        ObjectCollection collection = new ObjectCollection.Builder().withStrings(text).build();
        return perform(config, collection);
    }

    /**
     * Performs the specified action type on multiple objects with default configuration.
     *
     * <p>This is the most flexible convenience method, accepting any objects that can be converted
     * to the appropriate type for the action. The method will attempt to extract locations,
     * regions, or other required data from the provided objects.
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
     *
     * <p>Maps ActionType enum values to their corresponding ActionConfig implementations with
     * sensible defaults.
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
                return new ClickOptions.Builder().setNumberOfClicks(2).build();
            case RIGHT_CLICK:
                return new ClickOptions.Builder()
                        .setPressOptions(
                                MousePressOptions.builder()
                                        .setButton(
                                                io.github.jspinak.brobot.model.action.MouseButton
                                                        .RIGHT)
                                        .build())
                        .build();
            case MIDDLE_CLICK:
                return new ClickOptions.Builder()
                        .setPressOptions(
                                MousePressOptions.builder()
                                        .setButton(
                                                io.github.jspinak.brobot.model.action.MouseButton
                                                        .MIDDLE)
                                        .build())
                        .build();
            case HIGHLIGHT:
                return new HighlightOptions.Builder().build();
            case TYPE:
                return new TypeOptions.Builder().build();
            case HOVER:
                return new MouseMoveOptions.Builder().build();
            case MOVE:
                return new MouseMoveOptions.Builder().build();
            case DRAG:
                return new DragOptions.Builder().build();
            case FIND:
                return new PatternFindOptions.Builder().build();
            case WAIT_VANISH:
                return new VanishOptions.Builder().build();
            case SCROLL_UP:
                return new ScrollOptions.Builder().setDirection(ScrollOptions.Direction.UP).build();
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

    // Duplicate createDefaultConfig method removed - using the one above

}
