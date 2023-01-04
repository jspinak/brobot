package io.github.jspinak.brobot.actions.methods.basicactions.capture;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
public class CaptureConversions {

    private Map<Integer, ActionOptions.Action> actionMap = Map.of(
            4, ActionOptions.Action.CLICK,
            7, ActionOptions.Action.MOVE,
            3, ActionOptions.Action.TYPE,
            8, ActionOptions.Action.DRAG,
            5, ActionOptions.Action.MOUSE_DOWN,
            6, ActionOptions.Action.MOUSE_UP
    );

}
