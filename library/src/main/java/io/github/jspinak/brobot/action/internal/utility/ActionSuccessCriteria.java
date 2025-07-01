package io.github.jspinak.brobot.action.internal.utility;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Evaluates and determines success conditions for all action types.
 * <p>
 * ActionSuccessCriteria provides a centralized definition of what constitutes a successful
 * action execution for each action type in the framework. It encapsulates the
 * logic that determines whether an action achieved its intended goal based on
 * the results obtained.
 * <p>
 * <strong>Success criteria by action type:</strong>
 * <ul>
 * <li><strong>Pattern-based:</strong> FIND, CLICK, MOVE, HIGHLIGHT, CLASSIFY - Success when matches are found</li>
 * <li><strong>Always succeed:</strong> TYPE, SCROLL_MOUSE_WHEEL, MOUSE_DOWN/UP, KEY_DOWN/UP - Input actions always succeed</li>
 * <li><strong>Special conditions:</strong>
 *     <ul>
 *     <li>DEFINE - Success when a region is successfully defined</li>
 *     <li>VANISH - Success when no matches are found (inverse of FIND)</li>
 *     <li>DRAG - Success when exactly 2 points are identified (from and to)</li>
 *     <li>CLICK_UNTIL - Success depends on ClickUntil option (appear/vanish)</li>
 *     </ul>
 * </li>
 * </ul>
 * <p>
 * The class supports custom success criteria through ActionOptions, allowing
 * actions to override default behavior when needed. This flexibility is crucial
 * for complex automation scenarios where success depends on application-specific
 * conditions.
 *
 * @see ActionOptions#getSuccessCriteria()
 * @see ActionResult#setSuccess(boolean)
 */
@Component
public class ActionSuccessCriteria {

    /**
     * Registry of default success criteria for each action type.
     * Maps action types to predicates that evaluate ActionResult for success.
     */
    private Map<ActionOptions.Action, Predicate<ActionResult>> criteria = new HashMap<>();
    {
        criteria.put(ActionOptions.Action.FIND, matches -> !matches.isEmpty());
        criteria.put(ActionOptions.Action.CLICK, matches -> !matches.isEmpty());
        criteria.put(ActionOptions.Action.DEFINE, matches -> matches.getDefinedRegion().isDefined());
        criteria.put(ActionOptions.Action.TYPE, matches -> true);
        criteria.put(ActionOptions.Action.MOVE, matches -> !matches.isEmpty());
        criteria.put(ActionOptions.Action.HIGHLIGHT, matches -> !matches.isEmpty());
        criteria.put(ActionOptions.Action.SCROLL_MOUSE_WHEEL, matches -> true);
        criteria.put(ActionOptions.Action.MOUSE_DOWN, matches -> true);
        criteria.put(ActionOptions.Action.MOUSE_UP, matches -> true);
        criteria.put(ActionOptions.Action.KEY_DOWN, matches -> true);
        criteria.put(ActionOptions.Action.KEY_UP, matches -> true);
        criteria.put(ActionOptions.Action.DRAG, matches -> matches.size() == 2); // <- for DragSimple. for Drag, matches.getDefinedRegion().defined());
        criteria.put(ActionOptions.Action.VANISH, ActionResult::isEmpty);
        criteria.put(ActionOptions.Action.CLASSIFY, matches -> !matches.isEmpty());
    }

    /**
     * Retrieves the appropriate success criteria for an action.
     * <p>
     * Handles special cases like CLICK_UNTIL where the success criteria
     * depends on additional configuration (whether waiting for objects
     * to appear or vanish).
     *
     * @param actionOptions Configuration containing action type and parameters
     * @return Predicate that evaluates ActionResult for success
     */
    public Predicate<ActionResult> getCriteria(ActionOptions actionOptions) {
        if (actionOptions.getAction() == ActionOptions.Action.CLICK_UNTIL) {
            if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_APPEAR)
                return criteria.get(ActionOptions.Action.FIND);
            else return criteria.get(ActionOptions.Action.VANISH);
        }
        else return criteria.get(actionOptions.getAction());
    }

    /**
     * Evaluates and sets the success status of an action execution.
     * <p>
     * This method implements a two-tier evaluation system:
     * <ol>
     * <li>Custom criteria: If ActionOptions contains custom success criteria,
     *     that takes precedence</li>
     * <li>Default criteria: Otherwise, uses the standard criteria defined
     *     for the action type</li>
     * </ol>
     * <p>
     * The evaluated success status is stored directly in the ActionResult,
     * making it available for subsequent processing and reporting.
     * <p>
     * <strong>Side effects:</strong> Modifies the success field of the matches parameter.
     *
     * @param actionOptions Contains action type and optional custom success criteria
     * @param matches The action results to evaluate and update with success status
     */
    public void set(ActionOptions actionOptions, ActionResult matches) {
        if (actionOptions.getSuccessCriteria() != null) // new success criteria has been added to this operation
            matches.setSuccess(actionOptions.getSuccessCriteria().test(matches));
        else matches.setSuccess(getCriteria(actionOptions).test(matches)); // if not, use the above code
    }

    /**
     * Evaluates and sets the success status for ActionConfig-based actions.
     * <p>
     * This method evaluates success based on the ActionConfig's success criteria.
     * Since ActionConfig is type-specific, we derive the success based on the config type
     * and the matches found.
     *
     * @param actionConfig Configuration for the action
     * @param matches The action results to evaluate and update with success status
     */
    public void set(ActionConfig actionConfig, ActionResult matches) {
        // Check if custom success criteria is provided
        if (actionConfig.getSuccessCriteria() != null) {
            matches.setSuccess(actionConfig.getSuccessCriteria().test(matches));
            return;
        }
        
        // Default success criteria based on common patterns
        // Most find-based actions succeed when matches are found
        boolean defaultSuccess = !matches.isEmpty();
        
        // Override for specific action types that always succeed
        String configClassName = actionConfig.getClass().getSimpleName();
        if (configClassName.contains("Type") || 
            configClassName.contains("Scroll") ||
            configClassName.contains("MouseDown") ||
            configClassName.contains("MouseUp") ||
            configClassName.contains("KeyDown") ||
            configClassName.contains("KeyUp") ||
            configClassName.contains("Wait")) {
            defaultSuccess = true;
        } else if (configClassName.contains("Vanish")) {
            defaultSuccess = matches.isEmpty();
        } else if (configClassName.contains("Define")) {
            defaultSuccess = matches.getDefinedRegion() != null && matches.getDefinedRegion().isDefined();
        }
        
        matches.setSuccess(defaultSuccess);
    }

}
