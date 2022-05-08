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

    private MouseWheel mouseWheel;

    public ScrollMouseWheel(MouseWheel mouseWheel) {
        this.mouseWheel = mouseWheel;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        mouseWheel.scroll(actionOptions);
        return matches;
    }
}
