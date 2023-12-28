package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for MouseDown, works for real or mock actions.
 * Presses and holds a mouse button.
 */
@Component
public class MouseDownWrapper {

    private final ClickType clickType;
    private final Wait wait;

    public MouseDownWrapper(ClickType clickType, Wait wait) {
        this.clickType = clickType;
        this.wait = wait;
    }

    public boolean press(double pauseBeforeBegin, double totalPause, ClickType.Type type) {
        if (BrobotSettings.mock) {
            Report.print("<mouse-down>"); // this could be expanded if object clicks are given mock actions
            if (type != ClickType.Type.LEFT) Report.print(type.name());
            Report.print(" ");
            return true;
        }
        wait.wait(pauseBeforeBegin);
        Mouse.down(clickType.getTypeToSikuliButton().get(type));
        wait.wait(totalPause);
        return true;
    }

}
