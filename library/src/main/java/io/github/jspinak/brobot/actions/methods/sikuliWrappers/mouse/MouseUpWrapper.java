package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for MouseUp, works for real or mocked actions.
 * Releases a mouse button.
 */
@Component
public class MouseUpWrapper {

    private final ClickType clickType;
    private final Time time;

    public MouseUpWrapper(ClickType clickType, Time time) {
        this.clickType = clickType;
        this.time = time;
    }

    public boolean press(double pauseBefore, double pauseAfter, ClickType.Type type) {
        if (BrobotSettings.mock) {
            Report.print("<mouse-up>"); // this could be expanded if object clicks are given mock actions
            if (type != ClickType.Type.LEFT) System.out.print(type);
            System.out.print(" ");
            return true;
        }
        time.wait(pauseBefore);
        Mouse.up(clickType.getTypeToSikuliButton().get(type));
        time.wait(pauseAfter);
        return true;
    }

}
