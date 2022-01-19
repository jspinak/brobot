package actions.methods.sikuliWrappers.mouse;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import static com.brobot.multimodule.actions.methods.sikuliWrappers.mouse.ClickType.Type.LEFT;

/**
 * Wrapper class for MouseUp, works for real or mocked actions.
 * Releases a mouse button.
 */
@Component
public class MouseUpWrapper {

    private ClickType clickType;
    private final Wait wait;

    public MouseUpWrapper(ClickType clickType, Wait wait) {
        this.clickType = clickType;
        this.wait = wait;
    }

    public boolean press(double pauseBefore, double pauseAfter, ClickType.Type type) {
        if (BrobotSettings.mock) {
            Report.print("<mouse-up>"); // this could be expanded if object clicks are given mock actions
            if (type != LEFT) System.out.print(type);
            System.out.print(" ");
            return true;
        }
        wait.wait(pauseBefore);
        Mouse.up(clickType.getTypeToSikuliButton().get(type));
        wait.wait(pauseAfter);
        return true;
    }

}
