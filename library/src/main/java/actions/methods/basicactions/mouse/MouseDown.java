package actions.methods.basicactions.mouse;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.mouse.MouseDownWrapper;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Presses and holds a mouse button.
 */
@Component
public class MouseDown implements ActionInterface {

    private MouseDownWrapper mouseDownWrapper;

    public MouseDown(MouseDownWrapper mouseDownWrapper) {
        this.mouseDownWrapper = mouseDownWrapper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        mouseDownWrapper.press(
                actionOptions.getPauseBeforeMouseDown(),
                actionOptions.getPauseAfterMouseDown(),
                actionOptions.getClickType());
        return new Matches();
    }

}
