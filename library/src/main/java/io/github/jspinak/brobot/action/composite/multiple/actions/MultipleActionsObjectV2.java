package io.github.jspinak.brobot.action.composite.multiple.actions;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for defining sequences of actions to be executed using ActionConfig.
 * <p>
 * MultipleActionsObjectV2 is the modern replacement for MultipleActionsObject, using
 * ActionParametersV2 which contains ActionConfig instead of ActionOptions. It holds
 * a list of action-target pairs that define a complete workflow to be executed
 * sequentially. Each action can have different configurations and operate on different
 * GUI elements, enabling complex automation scenarios to be built from simple primitives.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.action.ActionChainOptions} instead.
 *             ActionChainOptions provides better action chaining with strategies (NESTED, CONFIRM)
 *             and proper result flow between actions. See the migration guide for examples.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Ordered list of ActionParametersV2 defining the action sequence</li>
 *   <li>Support for repeating the entire sequence multiple times</li>
 *   <li>Flexible action composition - mix any action types</li>
 *   <li>Each action has independent configuration and targets</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Form automation: Click field1 → Type text → Tab → Click field2 → Type</li>
 *   <li>Menu navigation: Click menu → Wait → Click submenu → Find result</li>
 *   <li>Complex interactions: Mouse down → Move → Move → Mouse up (drag)</li>
 *   <li>Repetitive tasks: Repeat a sequence of actions on multiple items</li>
 * </ul>
 * 
 * <p>The class provides methods to:</p>
 * <ul>
 *   <li>Add single action-target pairs</li>
 *   <li>Set the complete list of actions</li>
 *   <li>Configure repetition count</li>
 *   <li>Retrieve the action sequence for execution</li>
 * </ul>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * MultipleActionsObjectV2 workflow = new MultipleActionsObjectV2();
 * 
 * // Add a click action
 * workflow.add(new ActionParametersV2(
 *     new ClickOptions.Builder().build(),
 *     new ObjectCollection.Builder().withImages(button).build()
 * ));
 * 
 * // Add a type action
 * workflow.add(new ActionParametersV2(
 *     new TypeOptions.Builder().setTypeDelay(0.1).build(),
 *     new ObjectCollection.Builder().withStrings("username").build()
 * ));
 * 
 * // Execute the sequence 3 times
 * workflow.setTimesToRepeat(3);
 * }</pre>
 * 
 * @see ActionParametersV2
 * @see MultipleActions
 * @see ActionConfig
 * @see MultipleActionsObject
 */
@Deprecated
@Getter
@Setter
public class MultipleActionsObjectV2 {

    /**
     * The ordered list of action-target pairs to execute.
     * Each element represents one action in the sequence.
     */
    private List<ActionParametersV2> actionParameters = new ArrayList<>();
    
    /**
     * The number of times to repeat the entire action sequence.
     * Default is 1 (execute once). Values > 1 cause the entire
     * sequence to be repeated.
     */
    private int timesToRepeat = 1;

    /**
     * Default constructor creating an empty action sequence.
     * Actions can be added using the add() method or setActionParameters().
     */
    public MultipleActionsObjectV2() {}

    /**
     * Adds a single action-target pair to the sequence.
     * <p>
     * The action is appended to the end of the current sequence.
     * Actions are executed in the order they are added.
     * 
     * @param actionParameter The action configuration and target to add
     */
    public void add(ActionParametersV2 actionParameter) {
        actionParameters.add(actionParameter);
    }

    /**
     * Adds multiple action-target pairs to the sequence.
     * <p>
     * All actions are appended to the end of the current sequence
     * in the order they appear in the list.
     * 
     * @param actionParametersList List of action configurations and targets to add
     */
    public void addAll(List<ActionParametersV2> actionParametersList) {
        actionParameters.addAll(actionParametersList);
    }

    /**
     * Clears all actions from the sequence.
     * <p>
     * After calling this method, the sequence will be empty.
     * The timesToRepeat value is not affected.
     */
    public void clear() {
        actionParameters.clear();
    }

    /**
     * Returns the number of actions in the sequence.
     * 
     * @return The count of action-target pairs
     */
    public int size() {
        return actionParameters.size();
    }

    /**
     * Checks if the action sequence is empty.
     * 
     * @return true if no actions are defined, false otherwise
     */
    public boolean isEmpty() {
        return actionParameters.isEmpty();
    }
}