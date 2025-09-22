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
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
// Removed old logging import: 
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
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Simple click at location
 * action.click(new Location(100, 200));
 *
 * // Find and click an image
 * StateImage submitButton = new StateImage.Builder()
 *     .addPatterns("submit-button")
 *     .build();
 * action.click(submitButton);
 *
 * // Type text at current location
 * action.type("Hello World");
 *
 * // Find pattern with custom options
 * PatternFindOptions findOptions = new PatternFindOptions.Builder()
 *     .setSimilarity(0.85)
 *     .build();
 * ActionResult result = action.perform(findOptions, submitButton.asObjectCollection());
 *
 * // Using conditional chains (recommended approach)
 * ConditionalActionChain
 *     .find(new PatternFindOptions.Builder().build())
 *     .ifFoundClick()
 *     .ifNotFoundLog("Button not found")
 *     .perform(action, new ObjectCollection.Builder()
 *         .withImages(submitButton)
 *         .build());
 * }</pre>
 *
 * @since 1.0
 * @see ActionConfig
 * @see ActionInterface
 * @see BasicActionRegistry
 * @see ActionResult
 * @see ConditionalActionChain
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
        // First, perform a Find operation
        ActionResult findResult = find(stateImages);

        // If Find succeeded and we have matches, click on them
        if (findResult.isSuccess() && !findResult.getMatchList().isEmpty()) {
            // Create ObjectCollection with the matches from Find
            // Convert matches to clickable regions
            ObjectCollection matchCollection =
                    new ObjectCollection.Builder()
                            .withMatchObjectsAsRegions(
                                    findResult.getMatchList().toArray(new Match[0]))
                            .build();

            // Perform the Click with the matches
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            return perform(clickOptions, matchCollection);
        }

        // Find failed or no matches found
        return findResult;
    }

    /**
     * Performs a Click action with default options on the specified object collections.
     *
     * <p>This method handles clicking on various object types. For StateImages, it performs a Find
     * operation first, then clicks on the found matches. For Locations and Regions, it clicks
     * directly without finding.
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
            // If there are images, perform find first
            ActionResult findResult = find(objectCollections);

            // If Find succeeded and we have matches, click on them
            if (findResult.isSuccess() && !findResult.getMatchList().isEmpty()) {
                // Create ObjectCollection with the matches from Find
                ObjectCollection matchCollection =
                        new ObjectCollection.Builder()
                                .withMatchObjectsAsRegions(
                                        findResult.getMatchList().toArray(new Match[0]))
                                .build();

                // Perform the Click with the matches
                ClickOptions clickOptions = new ClickOptions.Builder().build();
                return perform(clickOptions, matchCollection);
            }

            // Find failed or no matches found
            return findResult;
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

    // ========== NEW CONVENIENCE METHODS ==========

    /**
     * Clicks on the specified region.
     *
     * <p>This is a convenience method equivalent to: {@code
     * click(ObjectCollection.builder().withRegions(region).build())}
     *
     * <p>For complex operations involving multiple objects or conditions, use the ObjectCollection
     * variant or ConditionalActionChain.
     *
     * @param region The region to click
     * @return ActionResult containing the operation outcome
     * @see #click(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult click(Region region) {
        return click(new ObjectCollection.Builder().withRegions(region).build());
    }

    /**
     * Clicks on the specified location.
     *
     * <p>This is a convenience method that clicks at a specific screen coordinate. The location is
     * automatically wrapped in an ObjectCollection for processing.
     *
     * @param location The location to click
     * @return ActionResult containing the operation outcome
     * @see #click(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult click(Location location) {
        return click(new ObjectCollection.Builder().withLocations(location).build());
    }

    /**
     * Clicks on the region of the specified match.
     *
     * <p>This convenience method extracts the region from a match result and clicks on it. Useful
     * for clicking on previously found elements without manual region extraction.
     *
     * @param match The match whose region should be clicked
     * @return ActionResult containing the operation outcome
     * @see #click(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult click(Match match) {
        return click(new ObjectCollection.Builder().withRegions(match.getRegion()).build());
    }

    /**
     * Types the specified text string.
     *
     * <p>This is a convenience method for typing plain text without needing to wrap it in an
     * ObjectCollection. The text is typed at the current cursor position or active input field.
     *
     * @param text The text to type
     * @return ActionResult containing the operation outcome
     * @see #type(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult type(String text) {
        return type(new ObjectCollection.Builder().withStrings(text).build());
    }

    /**
     * Types the text from the specified StateString.
     *
     * <p>This convenience method enables typing text that has state context without needing to wrap
     * it in an ObjectCollection. The StateString includes state ownership and optional spatial
     * context for where to click before typing.
     *
     * @param stateString The StateString containing text and optional context
     * @return ActionResult containing the operation outcome
     * @see #type(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult type(StateString stateString) {
        return type(new ObjectCollection.Builder().withStrings(stateString).build());
    }

    /**
     * Finds the specified pattern on screen.
     *
     * <p>This convenience method searches for a single pattern without needing to wrap it in a
     * StateImage and ObjectCollection. Uses default find options.
     *
     * @param pattern The pattern to search for
     * @return ActionResult containing found matches
     * @see #find(ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult find(Pattern pattern) {
        StateImage stateImage = new StateImage.Builder().addPattern(pattern).build();
        return find(new ObjectCollection.Builder().withImages(stateImage).build());
    }

    /**
     * Moves the mouse to the specified location.
     *
     * <p>This convenience method moves the mouse cursor to a specific screen coordinate. The
     * movement uses default speed and path settings.
     *
     * @param location The location to move to
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult move(Location location) {
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder().build();
        return perform(moveOptions, new ObjectCollection.Builder().withLocations(location).build());
    }

    /**
     * Moves the mouse to the center of the specified region.
     *
     * <p>This convenience method calculates the center point of a region and moves the mouse cursor
     * there. Useful for hovering over UI elements.
     *
     * @param region The region to move to (cursor moves to center)
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult move(Region region) {
        MouseMoveOptions moveOptions = new MouseMoveOptions.Builder().build();
        return perform(moveOptions, new ObjectCollection.Builder().withRegions(region).build());
    }

    /**
     * Moves the mouse to the center of the specified match.
     *
     * <p>This convenience method extracts the region from a match and moves the mouse to its
     * center. Useful for hovering over previously found elements.
     *
     * @param match The match whose center should be moved to
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult move(Match match) {
        return move(match.getRegion());
    }

    /**
     * Highlights the specified region on screen.
     *
     * <p>This convenience method draws a visual highlight around a region for debugging or user
     * feedback purposes. The highlight duration uses default settings.
     *
     * @param region The region to highlight
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult highlight(Region region) {
        HighlightOptions highlightOptions = new HighlightOptions.Builder().build();
        return perform(
                highlightOptions, new ObjectCollection.Builder().withRegions(region).build());
    }

    /**
     * Highlights the region of the specified match.
     *
     * <p>This convenience method highlights a previously found match, useful for visual debugging
     * of pattern matching results.
     *
     * @param match The match to highlight
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult highlight(Match match) {
        return highlight(match.getRegion());
    }

    /**
     * Drags from one location to another.
     *
     * <p>This convenience method performs a drag operation between two screen coordinates. Uses
     * default mouse button (left) and drag speed settings.
     *
     * @param from The starting location
     * @param to The ending location
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult drag(Location from, Location to) {
        DragOptions dragOptions = new DragOptions.Builder().build();
        ObjectCollection fromCollection =
                new ObjectCollection.Builder().withLocations(from).build();
        ObjectCollection toCollection = new ObjectCollection.Builder().withLocations(to).build();
        return perform(dragOptions, fromCollection, toCollection);
    }

    /**
     * Drags from one region's center to another region's center.
     *
     * <p>This convenience method performs a drag operation between the centers of two regions.
     * Useful for dragging UI elements from one area to another.
     *
     * @param from The starting region (drag starts from center)
     * @param to The ending region (drag ends at center)
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult drag(Region from, Region to) {
        DragOptions dragOptions = new DragOptions.Builder().build();
        ObjectCollection fromCollection = new ObjectCollection.Builder().withRegions(from).build();
        ObjectCollection toCollection = new ObjectCollection.Builder().withRegions(to).build();
        return perform(dragOptions, fromCollection, toCollection);
    }

    /**
     * Scrolls the mouse wheel at the current cursor position.
     *
     * <p>This convenience method performs mouse wheel scrolling. Positive values scroll down,
     * negative values scroll up. The amount represents the number of "notches" to scroll.
     *
     * @param direction The scroll direction (UP or DOWN)
     * @param steps The number of scroll steps
     * @return ActionResult containing the operation outcome
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult scroll(ScrollOptions.Direction direction, int steps) {
        ScrollOptions scrollOptions =
                new ScrollOptions.Builder().setDirection(direction).setScrollSteps(steps).build();
        return perform(scrollOptions, new ObjectCollection.Builder().build());
    }

    /**
     * Waits for the specified image to vanish from the screen.
     *
     * <p>This convenience method repeatedly checks if an image is no longer visible. Uses default
     * timeout and check interval settings.
     *
     * @param stateImage The image to wait for vanishing
     * @return ActionResult indicating success when image vanishes
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult vanish(StateImage stateImage) {
        VanishOptions vanishOptions = new VanishOptions.Builder().build();
        return perform(
                vanishOptions, new ObjectCollection.Builder().withImages(stateImage).build());
    }

    /**
     * Waits for the specified pattern to vanish from the screen.
     *
     * <p>This convenience method checks if a pattern is no longer visible. The pattern is
     * automatically wrapped in a StateImage for processing.
     *
     * @param pattern The pattern to wait for vanishing
     * @return ActionResult indicating success when pattern vanishes
     * @see #perform(ActionConfig, ObjectCollection...) for the canonical implementation
     * @since 2.1
     */
    public ActionResult vanish(Pattern pattern) {
        StateImage stateImage = new StateImage.Builder().addPattern(pattern).build();
        return vanish(stateImage);
    }

    // ========== END NEW CONVENIENCE METHODS ==========

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

    // ===== Convenience Methods for ActionType enum =====

    /**
     * Performs an action with default configuration on a location.
     *
     * <p>This is a convenience method for simple operations. For actions requiring specific
     * configuration (like custom delays, similarity thresholds, etc.), use with the appropriate
     * options class.
     *
     * <h3>When to use this method:</h3>
     *
     * <ul>
     *   <li>Simple click/move/hover operations with default settings
     *   <li>Quick prototyping and testing
     *   <li>When you don't need custom delays or configurations
     * </ul>
     *
     * <h3>When to use the full API:</h3>
     *
     * <ul>
     *   <li>Custom click delays or multiple clicks
     *   <li>Specific mouse buttons (right-click, middle-click)
     *   <li>Custom move speeds or trajectories
     *   <li>Any operation requiring fine control
     * </ul>
     *
     * <h3>Examples:</h3>
     *
     * <pre>{@code
     * // Simple click with defaults - perfect for this method
     * action.perform(ActionType.CLICK, location);
     *
     * // Simple move - also great for this method
     * action.perform(ActionType.MOVE, centerLocation);
     *
     * // Complex click with configuration - use the full API
     * ClickOptions options = new ClickOptions.Builder()
     *     .setClickCount(2)
     *     .setPauseAfter(1.0)
     *     .setButton(MouseButton.RIGHT)
     *     .build();
     * ObjectCollection collection = ObjectCollection.withLocations(location);
     * action.perform(options, collection);
     * }</pre>
     *
     * @param type the type of action to perform
     * @param location the target location
     * @return ActionResult containing the operation results
     * @since 2.0
     * @see ClickOptions for click-specific configuration
     * @see MouseMoveOptions for move-specific configuration
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
        return switch (type) {
            case CLICK -> new ClickOptions.Builder().build();
            case DOUBLE_CLICK -> new ClickOptions.Builder().setNumberOfClicks(2).build();
            case RIGHT_CLICK ->
                    new ClickOptions.Builder()
                            .setPressOptions(
                                    MousePressOptions.builder()
                                            .setButton(
                                                    io.github.jspinak.brobot.model.action
                                                            .MouseButton.RIGHT)
                                            .build())
                            .build();
            case MIDDLE_CLICK ->
                    new ClickOptions.Builder()
                            .setPressOptions(
                                    MousePressOptions.builder()
                                            .setButton(
                                                    io.github.jspinak.brobot.model.action
                                                            .MouseButton.MIDDLE)
                                            .build())
                            .build();
            case HIGHLIGHT -> new HighlightOptions.Builder().build();
            case TYPE -> new TypeOptions.Builder().build();
            case HOVER -> new MouseMoveOptions.Builder().build();
            case MOVE -> new MouseMoveOptions.Builder().build();
            case DRAG -> new DragOptions.Builder().build();
            case FIND -> new PatternFindOptions.Builder().build();
            case WAIT_VANISH -> new VanishOptions.Builder().build();
            case SCROLL_UP ->
                    new ScrollOptions.Builder().setDirection(ScrollOptions.Direction.UP).build();
            case SCROLL_DOWN ->
                    new ScrollOptions.Builder().setDirection(ScrollOptions.Direction.DOWN).build();
            case KEY_DOWN -> new KeyDownOptions.Builder().build();
            case KEY_UP -> new KeyUpOptions.Builder().build();
            case MOUSE_DOWN -> new MouseDownOptions.Builder().build();
            case MOUSE_UP -> new MouseUpOptions.Builder().build();
            default ->
                    throw new IllegalArgumentException(
                            "ActionType " + type + " is not yet supported in convenience methods");
        };
    }
}
