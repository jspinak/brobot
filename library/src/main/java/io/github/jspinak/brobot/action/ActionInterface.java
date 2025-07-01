package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.execution.CompositeActionRegistry;

/**
 * Core interface for all actions in the Brobot model-based GUI automation framework.
 * 
 * <p>ActionInterface defines the contract that all action implementations must follow, 
 * establishing a uniform execution pattern across the entire Action Model (α). This 
 * interface is the foundation of Brobot's action architecture, enabling polymorphic 
 * dispatch of diverse GUI operations through a single, consistent API.</p>
 * 
 * <p>Key design principles:
 * <ul>
 *   <li><b>Uniform Execution</b>: All actions, from simple clicks to complex workflows, 
 *       implement the same perform() method signature</li>
 *   <li><b>Result Accumulation</b>: Actions modify the provided Matches object to record 
 *       their results and maintain execution context</li>
 *   <li><b>Flexible Input</b>: Accepts variable ObjectCollections to support actions 
 *       requiring different numbers of targets</li>
 *   <li><b>Composability</b>: Enables actions to be combined into composite operations</li>
 * </ul>
 * </p>
 * 
 * <p>The perform method contract:
 * <ul>
 *   <li>Receives a Matches object containing ActionOptions and accumulating results</li>
 *   <li>Processes one or more ObjectCollections containing the action targets</li>
 *   <li>Updates the Matches object with results of the action</li>
 *   <li>May throw runtime exceptions for error conditions</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation categories:
 * <ul>
 *   <li><b>Basic Actions</b>: Click, Type, Find, Drag, etc.</li>
 *   <li><b>Composite Actions</b>: Multi-step operations built from basic actions</li>
 *   <li><b>Custom Actions</b>: Application-specific operations</li>
 *   <li><b>Mock Actions</b>: Test implementations for development and testing</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActionInterface enables the framework to treat all 
 * GUI operations uniformly, regardless of their complexity. This abstraction is crucial 
 * for building maintainable automation scripts where actions can be easily substituted, 
 * extended, or composed without changing the calling code.</p>
 * 
 * @since 1.0
 * @see Action
 * @see ActionResult
 * @see ObjectCollection
 * @see BasicActionRegistry
 * @see CompositeActionRegistry
 */
public interface ActionInterface {

    /**
     * Defines the standard types of GUI automation actions available in the framework.
     * This enum serves as an internal identifier for logging and dispatching.
     * <p>
     * Actions are divided into two categories:
     * <ul>
     * <li><strong>Basic Actions:</strong> Atomic operations that directly interact with the GUI</li>
     * <li><strong>Composite Actions:</strong> Complex operations that combine multiple basic actions</li>
     * </ul>
     * <p>
     * <strong>Basic Actions:</strong>
     * <ul>
     * <li>{@code FIND} - Searches for visual patterns on screen</li>
     * <li>{@code CLICK} - Performs mouse click operations</li>
     * <li>{@code DEFINE} - Captures a screen region with specific coordinates</li>
     * <li>{@code TYPE} - Sends keyboard input to the active window</li>
     * <li>{@code MOVE} - Moves the mouse cursor to a location</li>
     * <li>{@code VANISH} - Waits for elements to disappear from screen</li>
     * <li>{@code HIGHLIGHT} - Draws visual indicators on matches or regions</li>
     * <li>{@code SCROLL_MOUSE_WHEEL} - Performs mouse wheel scrolling</li>
     * <li>{@code MOUSE_DOWN} - Presses and holds mouse button</li>
     * <li>{@code MOUSE_UP} - Releases mouse button</li>
     * <li>{@code KEY_DOWN} - Presses and holds keyboard key</li>
     * <li>{@code KEY_UP} - Releases keyboard key</li>
     * <li>{@code CLASSIFY} - Performs color-based classification</li>
     * </ul>
     * <p>
     * <strong>Composite Actions:</strong>
     * <ul>
     * <li>{@code CLICK_UNTIL} - Repeatedly clicks until a condition is met</li>
     * <li>{@code DRAG} - Performs click-and-drag operations</li>
     * </ul>
     */
    enum Type {
        FIND, CLICK, DEFINE, TYPE, MOVE, VANISH, HIGHLIGHT, SCROLL_MOUSE_WHEEL,
        MOUSE_DOWN, MOUSE_UP, KEY_DOWN, KEY_UP, CLASSIFY,
        CLICK_UNTIL, DRAG
    }

    /**
     * Returns the standard type of this action implementation.
     * @return The ActionInterface.Type enum value.
     */
    Type getActionType();

    /**
     * Executes the action with the provided configuration and target objects.
     * <p>
     * This method is the core execution point for all GUI automation actions in Brobot.
     * Implementations should follow these guidelines:
     * <ul>
     * <li>Read action configuration from the ActionOptions within matches</li>
     * <li>Process target objects from the ObjectCollections</li>
     * <li>Execute the GUI operation (click, type, find, etc.)</li>
     * <li>Update the matches object with results (found elements, success status)</li>
     * <li>Handle errors gracefully, updating matches with failure information</li>
     * </ul>
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Modifies the GUI state through mouse/keyboard operations</li>
     * <li>Updates the matches parameter with execution results</li>
     * <li>May capture screenshots or log execution details</li>
     * </ul>
     * <p>
     * <strong>Implementation note:</strong> The matches parameter serves dual purposes -
     * it provides input configuration via ActionOptions and accumulates output results.
     * This design enables action chaining and comprehensive result tracking.
     *
     * @param matches Contains ActionOptions for configuration and accumulates execution results.
     *                This object is modified during execution to record matches found,
     *                success/failure status, and timing information.
     * @param objectCollections Variable number of collections containing target objects
     *                         (StateImages, Regions, Locations, Strings) that the action
     *                         will operate on. Actions may use zero, one, or multiple collections.
     */
    void perform(ActionResult matches, ObjectCollection... objectCollections);
}
