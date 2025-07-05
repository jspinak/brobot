package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import org.springframework.stereotype.Component;

/**
 * Executes sequences of actions as defined by MultipleActionsObject configurations.
 * <p>
 * This class is the core execution engine for composite actions in the Brobot framework.
 * It takes a MultipleActionsObject containing a sequence of action-target pairs and
 * executes them in order, optionally repeating the entire sequence multiple times.
 * This enables complex automation workflows to be built from simple action primitives.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Sequential execution of heterogeneous actions</li>
 *   <li>Support for repeating action sequences</li>
 *   <li>Debug output showing action execution flow</li>
 *   <li>Returns the result of the last executed action</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Multi-step form filling (click field, type, tab to next)</li>
 *   <li>Complex drag operations (mouse down, move, move, up)</li>
 *   <li>Navigation sequences (click menu, wait, click submenu)</li>
 *   <li>Repetitive tasks (click and type in multiple fields)</li>
 * </ul>
 * 
 * <p><b>Note:</b> Currently returns only the last ActionResult. Future versions
 * might aggregate results from all actions in the sequence.</p>
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.action.ActionChainOptions} with
 *             {@link io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor} instead.
 *             ActionChainOptions provides better type safety, proper result flow between actions,
 *             and sophisticated chaining strategies. See {@link io.github.jspinak.brobot.action.examples.ActionChainExamples}
 *             for migration examples.
 * 
 * @see MultipleActionsObject
 * @see ActionParameters
 * @see Action
 * @see io.github.jspinak.brobot.action.ActionChainOptions
 */
@Deprecated
@Component
public class MultipleActions {

    private Action action;

    public MultipleActions(Action action) {
        this.action = action;
    }

    /**
     * Executes all actions defined in the MultipleActionsObject in sequence.
     * <p>
     * This method iterates through the action-target pairs contained in the
     * MultipleActionsObject, executing each action on its corresponding target.
     * The entire sequence can be repeated multiple times based on the
     * timesToRepeat setting in the MultipleActionsObject.
     * 
     * <p><b>Execution flow:</b>
     * <ol>
     *   <li>Outer loop: Repeats the entire sequence timesToRepeat times</li>
     *   <li>Inner loop: Executes each action-target pair in order</li>
     *   <li>Debug output: Prints each action type and target to console</li>
     *   <li>Result tracking: Each action's result overwrites the previous</li>
     * </ol>
     * 
     * <p><b>Important behaviors:</b>
     * <ul>
     *   <li>Actions are executed synchronously in order</li>
     *   <li>No error handling - failures in one action don't stop the sequence</li>
     *   <li>Only the last action's result is returned</li>
     *   <li>Empty sequences return an empty ActionResult</li>
     * </ul>
     * 
     * @param mao The MultipleActionsObject containing the sequence of actions to execute
     *            and the number of times to repeat the sequence
     * @return The ActionResult from the last executed action, or an empty ActionResult
     *         if no actions were executed
     */
    public ActionResult perform(MultipleActionsObject mao) {
        ActionResult matches = new ActionResult();
        for (int i=0; i<mao.getTimesToRepeat(); i++) {
            for (ActionParameters aooc : mao.getAoocs()) {
                System.out.println(aooc.getActionOptions().getAction()+"->"+aooc.getObjectCollection());
                //action.perform(new ActionOptions.Builder().setAction(ActionOptions.Action.HIGHLIGHT).build(), aooc.getObjectCollection());
                matches = action.perform(aooc.getActionOptions(), aooc.getObjectCollection());
            }
        }
        return matches; // we should return the last Matches object
    }

    /**
     * Executes all actions defined in the MultipleActionsObjectV2 in sequence.
     * <p>
     * This overload provides support for the new ActionConfig API, allowing
     * action sequences to be defined using type-specific configuration classes
     * (e.g., ClickOptions, PatternFindOptions, TypeOptions) instead of the
     * generic ActionOptions.
     * <p>
     * The execution behavior is identical to the ActionOptions version:
     * <ul>
     *   <li>Actions execute synchronously in order</li>
     *   <li>The entire sequence can be repeated multiple times</li>
     *   <li>Debug output shows the execution flow</li>
     *   <li>Returns the result of the last executed action</li>
     * </ul>
     * 
     * @param mao The MultipleActionsObjectV2 containing ActionConfig-based action sequence
     * @return The ActionResult from the last executed action, or an empty ActionResult
     *         if no actions were executed
     * @since 2.0
     */
    public ActionResult perform(MultipleActionsObjectV2 mao) {
        ActionResult matches = new ActionResult();
        for (int i = 0; i < mao.getTimesToRepeat(); i++) {
            for (ActionParametersV2 acoc : mao.getActionParameters()) {
                System.out.println(acoc.getActionConfig().getClass().getSimpleName() + "->" + acoc.getObjectCollection());
                matches = action.perform(acoc.getActionConfig(), acoc.getObjectCollection());
            }
        }
        return matches;
    }
}
