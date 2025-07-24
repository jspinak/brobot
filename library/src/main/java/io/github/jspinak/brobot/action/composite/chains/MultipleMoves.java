package io.github.jspinak.brobot.action.composite.chains;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActionsObject;
import io.github.jspinak.brobot.model.element.Location;

import static io.github.jspinak.brobot.action.internal.options.ActionOptions.Action.MOVE;

/**
 * Demonstrates sequential mouse movement patterns using the composite action framework.
 * <p>
 * This class showcases how to create custom actions by combining multiple basic actions
 * through the {@link MultipleActions} framework. While the basic MouseMove action can
 * handle multiple locations, this implementation demonstrates the composite pattern
 * approach, which offers more flexibility for complex movement sequences with
 * customizable timing between moves.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Sequential execution of mouse movements to multiple locations</li>
 *   <li>Configurable pause after each movement (0.5 seconds default)</li>
 *   <li>Debug output showing the constructed action sequence</li>
 *   <li>Success/failure reporting for the entire movement sequence</li>
 * </ul>
 * 
 * <p>This implementation serves as a template for creating more sophisticated
 * movement patterns, such as:
 * <ul>
 *   <li>Mouse gestures for triggering special UI behaviors</li>
 *   <li>Path-based interactions (drawing, drag paths)</li>
 *   <li>Hover sequences for revealing hidden UI elements</li>
 *   <li>Complex navigation patterns through multiple UI regions</li>
 * </ul>
 * 
 * <p><b>Note:</b> For simple point-to-point movements, consider using the basic
 * MouseMove action directly. This composite approach is most valuable when you need
 * fine-grained control over timing or when movements are part of a larger
 * action sequence.</p>
 * 
 * @see MultipleActions
 * @see MultipleActionsObject
 * @see ActionOptions
 * @see Location
 */
public class MultipleMoves {

    private MultipleActions multipleActions;

    public MultipleMoves(MultipleActions multipleActions) {
        this.multipleActions = multipleActions;
    }

    /**
     * Executes a sequence of mouse movements to the specified locations.
     * <p>
     * This method constructs a composite action that moves the mouse cursor
     * sequentially through each provided location, pausing for 0.5 seconds
     * after each movement. The entire sequence is executed as a single
     * composite action, ensuring atomic success/failure reporting.
     * 
     * <p><b>Implementation details:</b>
     * <ul>
     *   <li>Each location becomes a separate MOVE action in the sequence</li>
     *   <li>All moves use the same timing configuration (0.5s pause after)</li>
     *   <li>The action sequence is printed to console for debugging</li>
     *   <li>Success requires all movements to complete successfully</li>
     * </ul>
     * 
     * <p>Common use cases include:
     * <ul>
     *   <li>Navigating through a series of UI elements</li>
     *   <li>Creating mouse gesture patterns</li>
     *   <li>Hovering over multiple elements to reveal tooltips</li>
     *   <li>Simulating user exploration of an interface</li>
     * </ul>
     * 
     * @param locations Variable number of screen locations to visit in sequence.
     *                  The mouse will move to each location in the order provided.
     * @return true if all movements completed successfully, false if any movement
     *         failed or if the action sequence was interrupted
     */
    public boolean multipleMoves(Location... locations) {
        ActionOptions move = new ActionOptions.Builder()
                .setAction(MOVE)
                .setPauseAfterEnd(.5)
                .build();
        MultipleActionsObject mao = new MultipleActionsObject();
        for (Location loc : locations) {
            mao.addActionOptionsObjectCollectionPair(
                    move, new ObjectCollection.Builder().withLocations(loc).build()
            );
        }
        mao.print();
        return multipleActions.perform(mao).isSuccess();
    }
}
