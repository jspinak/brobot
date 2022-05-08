package io.github.jspinak.brobot.actions.actionConfigurations;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Defines the condition necessary to stop repeating sequences for individual Actions.
 * It uses a Predicate because it may be useful to add it to ActionOptions as a variable.
 */
@Component
public class ExitSequences {

    private Map<ActionOptions.Action, Predicate<Matches>> criteria = new HashMap<>();
    {
        criteria.put(ActionOptions.Action.FIND, Matches::isSuccess);
        criteria.put(ActionOptions.Action.CLICK, matches -> false);
        criteria.put(ActionOptions.Action.DEFINE, Matches::isSuccess);
        criteria.put(ActionOptions.Action.TYPE, matches -> false);
        criteria.put(ActionOptions.Action.MOVE, matches -> false);
        criteria.put(ActionOptions.Action.GET_TEXT, Matches::isSuccess);
        criteria.put(ActionOptions.Action.HIGHLIGHT, matches -> false);
        criteria.put(ActionOptions.Action.SCROLL_MOUSE_WHEEL, matches -> false);
        criteria.put(ActionOptions.Action.MOUSE_DOWN, matches -> false);
        criteria.put(ActionOptions.Action.MOUSE_UP, matches -> false);
        criteria.put(ActionOptions.Action.KEY_DOWN, matches -> false);
        criteria.put(ActionOptions.Action.KEY_UP, matches -> false);
        criteria.put(ActionOptions.Action.DRAG, matches -> false);
        criteria.put(ActionOptions.Action.VANISH, Matches::isSuccess);
        criteria.put(ActionOptions.Action.CLICK_UNTIL, Matches::isSuccess);
    }

    public boolean okToExit(ActionOptions actionOptions, Matches matches) {
        return criteria.get(actionOptions.getAction()).test(matches);
    }

}
