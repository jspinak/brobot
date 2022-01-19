package actions.methods.basicactions.mouse;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.mouse.MouseUpWrapper;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Releases a mouse button.
 */
@Component
public class MouseUp implements ActionInterface {

    private MouseUpWrapper mouseUpWrapper;

    public MouseUp(MouseUpWrapper mouseUpWrapper) {
        this.mouseUpWrapper = mouseUpWrapper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        mouseUpWrapper.press(
                actionOptions.getPauseBeforeMouseUp(),
                actionOptions.getPauseAfterMouseUp(),
                actionOptions.getClickType());
        return new Matches();
    }

}
