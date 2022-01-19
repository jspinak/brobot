package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

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
            if (type != ClickType.Type.LEFT) System.out.print(type);
            System.out.print(" ");
            return true;
        }
        wait.wait(pauseBefore);
        Mouse.up(clickType.getTypeToSikuliButton().get(type));
        wait.wait(pauseAfter);
        return true;
    }

}
