package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.composites.methods.ClickUntil;
import io.github.jspinak.brobot.actions.composites.methods.drag.Drag;
import io.github.jspinak.brobot.actions.composites.methods.drag.DragSimple;
import io.github.jspinak.brobot.actions.composites.methods.multipleFinds.MultipleFinds;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CompositeActions are built from BasicActions. They typically involve more
 * than 1 Find operation.
 */
@Component
public class CompositeAction {

    private Map<ActionOptions.Action, ActionInterface> actions = new HashMap<>();

    public CompositeAction(DragSimple drag, ClickUntil clickUntil, MultipleFinds multipleFinds) {
        actions.put(ActionOptions.Action.DRAG, drag);
        actions.put(ActionOptions.Action.CLICK_UNTIL, clickUntil);
        actions.put(ActionOptions.Action.FIND, multipleFinds);
    }

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }
}
