package io.github.jspinak.brobot.action.composite.multiple.actions;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.service.ActionService;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Alternative execution engine for composite actions using direct BasicAction access.
 * <p>
 * This class provides a lower-level alternative to {@link MultipleActions} for executing
 * action sequences. Instead of using the high-level Action service, it directly accesses
 * BasicAction implementations through the action registry. This approach offers more
 * control over action execution and allows for dynamic action resolution based on the
 * ActionOptions configuration.
 * 
 * <p>Key differences from MultipleActions:</p>
 * <ul>
 *   <li>Uses BasicAction.getAction() for dynamic action resolution</li>
 *   <li>Modifies a single ActionResult object throughout execution</li>
 *   <li>Handles missing actions gracefully with Optional</li>
 *   <li>Updates ActionOptions in the result for each action</li>
 * </ul>
 * 
 * <p>Advantages of this approach:</p>
 * <ul>
 *   <li>More direct control over action execution</li>
 *   <li>Better suited for custom action implementations</li>
 *   <li>Allows for conditional action execution</li>
 *   <li>Maintains action context throughout the sequence</li>
 * </ul>
 * 
 * <p>This class is particularly useful when you need to work with custom actions
 * or require more granular control over the execution process.</p>
 * 
 * @see MultipleActions
 * @see BasicActionRegistry
 * @see ActionInterface
 * @see MultipleActionsObject
 */
@Component
public class MultipleBasicActions {

    private final BasicActionRegistry basicAction;
    private final ActionService actionService;

    public MultipleBasicActions(BasicActionRegistry basicAction, ActionService actionService) {
        this.basicAction = basicAction;
        this.actionService = actionService;
    }

    /**
     * Executes all actions in the sequence using direct BasicAction invocation.
     * <p>
     * This method iterates through the action sequence defined in the MultipleActionsObject,
     * dynamically resolving and executing each action. Unlike {@link MultipleActions#perform},
     * this implementation uses a single ActionResult object that accumulates results across
     * all actions, with each action's options updating the result's configuration.
     * 
     * <p><b>Execution flow:</b>
     * <ol>
     *   <li>Creates a single ActionResult to accumulate results</li>
     *   <li>Outer loop: Repeats sequence based on timesToRepeat</li>
     *   <li>For each action-target pair:
     *       <ul>
     *         <li>Updates ActionResult with current ActionOptions</li>
     *         <li>Dynamically resolves the action implementation</li>
     *         <li>Executes if found (silently skips if not)</li>
     *       </ul>
     *   </li>
     * </ol>
     * 
     * <p><b>Important behaviors:</b>
     * <ul>
     *   <li>Single ActionResult is modified throughout execution</li>
     *   <li>Missing actions are silently skipped (no error thrown)</li>
     *   <li>Each action sees results from previous actions</li>
     *   <li>Final result contains last action's options and accumulated matches</li>
     * </ul>
     * 
     * @param mao The MultipleActionsObject containing the action sequence and repeat count
     * @return The accumulated ActionResult containing results from all executed actions
     *         with the last action's options
     */
    public ActionResult perform(MultipleActionsObject mao) {
        ActionResult matches = new ActionResult();
        for (int i=0; i<mao.getTimesToRepeat(); i++) {
            for (ActionParameters aooc : mao.getAoocs()) {
                matches.setActionOptions(aooc.getActionOptions());
                Optional<ActionInterface> action = basicAction.getAction(aooc.getActionOptions().getAction());
                action.ifPresent(actionInterface -> actionInterface.perform(matches, aooc.getObjectCollection()));
            }
        }
        return matches; // we should return the last Matches object
    }

    /**
     * Executes all actions in the sequence using ActionConfig-based implementation.
     * <p>
     * This method provides support for the new ActionConfig API, allowing execution
     * of action sequences defined with specific action configuration classes
     * (e.g., ClickOptions, PatternFindOptions, TypeOptions).
     * <p>
     * Like the ActionOptions version, this method:
     * <ul>
     *   <li>Creates a single ActionResult to accumulate results</li>
     *   <li>Executes actions in the order they appear in the sequence</li>
     *   <li>Repeats the entire sequence based on timesToRepeat</li>
     *   <li>Returns the accumulated result with the last action's config</li>
     * </ul>
     * 
     * @param mao The MultipleActionsObjectV2 containing ActionConfig-based action sequence
     * @return The accumulated ActionResult containing results from all executed actions
     *         with the last action's configuration
     * @since 2.0
     */
    public ActionResult perform(MultipleActionsObjectV2 mao) {
        ActionResult matches = new ActionResult();
        for (int i = 0; i < mao.getTimesToRepeat(); i++) {
            for (ActionParametersV2 acoc : mao.getActionParameters()) {
                matches.setActionConfig(acoc.getActionConfig());
                Optional<ActionInterface> action = actionService.getAction(acoc.getActionConfig());
                action.ifPresent(actionInterface -> actionInterface.perform(matches, acoc.getObjectCollection()));
            }
        }
        return matches;
    }

}
