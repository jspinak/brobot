package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for defining complex action sequences in the Brobot automation framework.
 * <p>
 * This class serves as a builder and container for composite actions, allowing users
 * to construct sequences of different actions on different targets. It maintains an
 * ordered list of action-target pairs and supports repeating the entire sequence
 * multiple times. This is the primary data structure used by {@link MultipleActions}
 * to execute complex automation workflows.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Maintains ordered sequence of action-target pairs</li>
 *   <li>Supports repeating entire sequences</li>
 *   <li>Provides state management for acted-upon objects</li>
 *   <li>Includes debugging support via print method</li>
 * </ul>
 * 
 * <p>Common patterns:</p>
 * <ul>
 *   <li><b>Form automation:</b> Click field 1, type text, click field 2, type text</li>
 *   <li><b>Navigation:</b> Click menu, wait for submenu, click item</li>
 *   <li><b>Drag sequences:</b> Mouse down, move to waypoint 1, move to waypoint 2, mouse up</li>
 *   <li><b>Validation:</b> Type text, click submit, find success message</li>
 * </ul>
 * 
 * <p>The timesToRepeat feature is useful for stress testing, handling flaky UI
 * elements, or performing repetitive tasks like clearing multiple items.</p>
 * 
 * <p>TODO: This class should be updated to support ActionConfig in addition to ActionOptions.
 * The migration would involve:
 * <ul>
 *   <li>Update ActionParameters to accept ActionConfig</li>
 *   <li>Add overloaded addActionConfigObjectCollectionPair method</li>
 *   <li>Update MultipleActions to handle ActionConfig execution</li>
 *   <li>Consider using ActionChainOptions as a modern replacement pattern</li>
 * </ul>
 * </p>
 * 
 * @see MultipleActions
 * @see ActionParameters
 * @see ActionOptions
 * @see ObjectCollection
 */
@Getter
public class MultipleActionsObject {

    private List<ActionParameters> aoocs = new ArrayList<>();
    private int timesToRepeat = 1;

    /**
     * Adds a new action-target pair to the sequence.
     * <p>
     * This method appends a new action configuration and its target collection to the
     * end of the action sequence. The order of addition determines the execution order
     * when the sequence is performed. Each call creates a new {@link ActionParameters}
     * internally.
     * 
     * <p>The method modifies the internal list by adding to it, making this object
     * mutable. Consider the order carefully as it directly affects execution behavior.</p>
     * 
     * @param actionOptions The configuration for the action, including type, timing,
     *                      and behavioral parameters. Must not be null.
     * @param objectCollection The targets for this action (images, regions, locations,
     *                         or strings). Must not be null.
     */
    public void addActionOptionsObjectCollectionPair(ActionOptions actionOptions, ObjectCollection objectCollection) {
        aoocs.add(new ActionParameters(actionOptions, objectCollection));
    }

    /**
     * Resets the action counters for all ObjectCollections in the sequence.
     * <p>
     * This method iterates through all action-target pairs and resets the internal
     * counters that track how many times each object has been acted upon. This is
     * important for actions that have limits on how many times they can act on the
     * same object (controlled by ActionOptions.maxMatchesToActOn).
     * 
     * <p>Common use cases:</p>
     * <ul>
     *   <li>Preparing for a fresh execution of a previously-run sequence</li>
     *   <li>Resetting state between test iterations</li>
     *   <li>Clearing counters after error recovery</li>
     * </ul>
     * 
     * <p>This method modifies the state of all contained ObjectCollections.</p>
     */
    public void resetTimesActedOn() {
        aoocs.forEach(pair -> pair.getObjectCollection().resetTimesActedOn());
    }

    /**
     * Prints a debug representation of the action sequence to the console.
     * <p>
     * This utility method outputs a human-readable representation of all action-target
     * pairs in the sequence, showing the action type and a string representation of
     * the target collection. This is useful for debugging complex action sequences
     * and verifying that actions are configured as intended.
     * 
     * <p>Output format:</p>
     * <pre>
     * __MultipleActionsObject__
     * CLICK->ObjectCollection[images=1, regions=0]
     * TYPE->ObjectCollection[strings=1]
     * FIND->ObjectCollection[images=2, regions=1]
     * </pre>
     * 
     * <p>Note: This method writes directly to System.out and is intended for
     * debugging purposes only.</p>
     */
    public void print() {
        System.out.println("__MultipleActionsObject__");
        aoocs.forEach(aooc -> System.out.println(
                aooc.getActionOptions().getAction()+"->"+aooc.getObjectCollection().toString()));
        System.out.println();
    }
}
