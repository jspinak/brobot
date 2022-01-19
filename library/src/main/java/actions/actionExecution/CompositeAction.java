package actions.actionExecution;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.composites.methods.ClickUntil;
import com.brobot.multimodule.actions.composites.methods.drag.Drag;
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

    public CompositeAction(Drag drag, ClickUntil clickUntil) {
        actions.put(ActionOptions.Action.DRAG, drag);
        actions.put(ActionOptions.Action.CLICK_UNTIL, clickUntil);
    }

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }
}
