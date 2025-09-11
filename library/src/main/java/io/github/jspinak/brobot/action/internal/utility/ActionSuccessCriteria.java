package io.github.jspinak.brobot.action.internal.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;

/**
 * Evaluates and determines success conditions for all action types.
 *
 * <p>ActionSuccessCriteria provides a centralized definition of what constitutes a successful
 * action execution for each action type in the framework. It encapsulates the logic that determines
 * whether an action achieved its intended goal based on the results obtained.
 *
 * <p><strong>Success criteria by action type:</strong>
 *
 * <ul>
 *   <li><strong>Pattern-based:</strong> FIND, CLICK, MOVE, HIGHLIGHT, CLASSIFY - Success when
 *       matches are found
 *   <li><strong>Always succeed:</strong> TYPE, SCROLL_MOUSE_WHEEL, MOUSE_DOWN/UP, KEY_DOWN/UP -
 *       Input actions always succeed
 *   <li><strong>Special conditions:</strong>
 *       <ul>
 *         <li>DEFINE - Success when a region is successfully defined
 *         <li>VANISH - Success when no matches are found (inverse of FIND)
 *         <li>DRAG - Success when exactly 2 points are identified (from and to)
 *         <li>CLICK_UNTIL - Success depends on ClickUntil option (appear/vanish)
 *       </ul>
 * </ul>
 *
 * <p>The class supports custom success criteria through ActionOptions, allowing actions to override
 * default behavior when needed. This flexibility is crucial for complex automation scenarios where
 * success depends on application-specific conditions.
 *
 * @see ActionOptions#getSuccessCriteria()
 * @see ActionResult#setSuccess(boolean)
 */
@Component
public class ActionSuccessCriteria {

    /**
     * Registry of default success criteria for each action type. Maps action types to predicates
     * that evaluate ActionResult for success.
     */
    private Map<ActionType, Predicate<ActionResult>> criteria = new HashMap<>();

    {
        criteria.put(ActionType.FIND, matches -> !matches.isEmpty());
        criteria.put(ActionType.CLICK, matches -> !matches.isEmpty());
        criteria.put(ActionType.DEFINE, matches -> matches.getDefinedRegion().isDefined());
        criteria.put(ActionType.TYPE, matches -> true);
        criteria.put(ActionType.MOVE, matches -> !matches.isEmpty());
        criteria.put(ActionType.HIGHLIGHT, matches -> !matches.isEmpty());
        criteria.put(ActionType.SCROLL_MOUSE_WHEEL, matches -> true);
        criteria.put(ActionType.MOUSE_DOWN, matches -> true);
        criteria.put(ActionType.MOUSE_UP, matches -> true);
        criteria.put(ActionType.KEY_DOWN, matches -> true);
        criteria.put(ActionType.KEY_UP, matches -> true);
        criteria.put(
                ActionType.DRAG, matches -> matches.size() == 2); // <- for DragSimple. for Drag,
        // matches.getDefinedRegion().defined());
        criteria.put(ActionType.VANISH, ActionResult::isEmpty);
        criteria.put(ActionType.CLASSIFY, matches -> !matches.isEmpty());
    }

    /**
     * Retrieves the appropriate success criteria for an action.
     *
     * <p>Handles special cases like CLICK_UNTIL where the success criteria depends on additional
     * configuration (whether waiting for objects to appear or vanish).
     *
     * @param actionConfig Configuration containing action type and parameters
     * @return Predicate that evaluates ActionResult for success
     */
    public Predicate<ActionResult> getCriteria(ActionConfig actionConfig) {
        if (actionConfig == null) {
            // Return a default criteria that always returns false
            return result -> false;
        }

        // Get action type from the config - implementation depends on config type
        ActionType actionType = getActionTypeFromConfig(actionConfig);

        // Handle special case for CLICK_UNTIL
        if (actionType == ActionType.CLICK_UNTIL) {
            // For CLICK_UNTIL, determine if we're waiting for objects to appear or vanish
            // Default to FIND criteria for now
            return criteria.get(ActionType.FIND);
        }

        // Return criteria or default if not found
        return criteria.getOrDefault(actionType, result -> false);
    }

    /**
     * Evaluates and sets the success status of an action execution.
     *
     * <p>This method implements a two-tier evaluation system:
     *
     * <ol>
     *   <li>Custom criteria: If ActionOptions contains custom success criteria, that takes
     *       precedence
     *   <li>Default criteria: Otherwise, uses the standard criteria defined for the action type
     * </ol>
     *
     * <p>The evaluated success status is stored directly in the ActionResult, making it available
     * for subsequent processing and reporting.
     *
     * <p><strong>Side effects:</strong> Modifies the success field of the matches parameter.
     *
     * @param actionConfig Contains action type and optional custom success criteria
     * @param matches The action results to evaluate and update with success status
     */
    public void set(ActionConfig actionConfig, ActionResult matches) {
        if (actionConfig == null || matches == null) {
            return; // Handle null gracefully
        }

        // Check if the config has custom success criteria
        if (actionConfig.getSuccessCriteria() != null) {
            matches.setSuccess(actionConfig.getSuccessCriteria().test(matches));
        } else {
            // Use the default criteria based on action type
            matches.setSuccess(getCriteria(actionConfig).test(matches));
        }
    }

    /**
     * Helper method to determine the ActionType from an ActionConfig. Since ActionConfig is now a
     * base class, we need to infer the action type from the specific config class.
     *
     * @param actionConfig The configuration to extract action type from
     * @return The corresponding ActionType
     */
    private ActionType getActionTypeFromConfig(ActionConfig actionConfig) {
        String className = actionConfig.getClass().getSimpleName();

        if (className.contains("Click")) return ActionType.CLICK;
        if (className.contains("Find") || className.contains("Pattern")) return ActionType.FIND;
        if (className.contains("Type")) return ActionType.TYPE;
        if (className.contains("Drag")) return ActionType.DRAG;
        if (className.contains("Move")) return ActionType.MOVE;
        if (className.contains("Define")) return ActionType.DEFINE;
        if (className.contains("Vanish")) return ActionType.VANISH;
        if (className.contains("Scroll")) return ActionType.SCROLL_MOUSE_WHEEL;
        if (className.contains("Highlight")) return ActionType.HIGHLIGHT;
        if (className.contains("KeyDown")) return ActionType.KEY_DOWN;
        if (className.contains("KeyUp")) return ActionType.KEY_UP;
        if (className.contains("MouseDown")) return ActionType.MOUSE_DOWN;
        if (className.contains("MouseUp")) return ActionType.MOUSE_UP;

        // Default to FIND for unknown config types
        return ActionType.FIND;
    }
}
