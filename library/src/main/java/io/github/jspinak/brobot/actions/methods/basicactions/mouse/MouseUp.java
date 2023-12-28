package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseUpWrapper;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Releases a mouse button.
 */
@Component
public class MouseUp implements ActionInterface {

    private final MouseUpWrapper mouseUpWrapper;

    public MouseUp(MouseUpWrapper mouseUpWrapper) {
        this.mouseUpWrapper = mouseUpWrapper;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseUpWrapper.press(
                actionOptions.getPauseBeforeMouseUp(),
                actionOptions.getPauseAfterMouseUp(),
                actionOptions.getClickType());
    }

}
