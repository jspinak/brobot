package io.github.jspinak.brobot.actions.actionConfigurations;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Defines the success condition for individual Actions.
 * CompositeActions may have more complex success conditions that cannot
 *   be defined by just using the Matches returned by the Action. For example,
 *   ClickUntil can click until an object appears or an object vanishes. This
 *   option is in the ActionOptions parameter.
 */
@Component
public class Success {

    private Map<ActionOptions.Action, Predicate<Matches>> criteria = new HashMap<>();
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
        criteria.put(ActionOptions.Action.VANISH, Matches::isEmpty);
        criteria.put(ActionOptions.Action.CLASSIFY, matches -> !matches.isEmpty());
    }

    public Predicate<Matches> getCriteria(ActionOptions actionOptions) {
        if (actionOptions.getAction() == ActionOptions.Action.CLICK_UNTIL) {
            if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_APPEAR)
                return criteria.get(ActionOptions.Action.FIND);
            else return criteria.get(ActionOptions.Action.VANISH);
        }
        else return criteria.get(actionOptions.getAction());
    }

    /**
     * If new success criteria has been added to the operation, use it to determine success.
     * If not, use the standard success criteria as defined in this class.
     * @param actionOptions holds any new success criteria
     * @param matches are normally used by success criteria to calculate success
     */
    public void set(ActionOptions actionOptions, Matches matches) {
        if (actionOptions.getSuccessCriteria() != null) // new success criteria has been added to this operation
            matches.setSuccess(actionOptions.getSuccessCriteria().test(matches));
        else matches.setSuccess(getCriteria(actionOptions).test(matches)); // if not, use the above code
    }

}
