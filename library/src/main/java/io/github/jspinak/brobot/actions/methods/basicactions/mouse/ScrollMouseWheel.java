package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MouseWheel;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Scrolls the mouse wheel up or down.
 */
@Component
public class ScrollMouseWheel implements ActionInterface {

    private final MouseWheel mouseWheel;

    public ScrollMouseWheel(MouseWheel mouseWheel) {
        this.mouseWheel = mouseWheel;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        mouseWheel.scroll(actionOptions);
    }
}
