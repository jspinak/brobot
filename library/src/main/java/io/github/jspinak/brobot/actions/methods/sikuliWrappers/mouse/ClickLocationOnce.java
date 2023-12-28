package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for Click, handles real and mock clicks.
 * Performs a full click once, with pauses, mouse down, and mouse up.
 */
@Component
public class ClickLocationOnce {

    private final MouseDownWrapper mouseDownWrapper;
    private final MouseUpWrapper mouseUpWrapper;
    private final MoveMouseWrapper moveMouseWrapper;

    public ClickLocationOnce(MouseDownWrapper mouseDownWrapper,
                             MouseUpWrapper mouseUpWrapper,
                             MoveMouseWrapper moveMouseWrapper) {
        this.mouseDownWrapper = mouseDownWrapper;
        this.mouseUpWrapper = mouseUpWrapper;
        this.moveMouseWrapper = moveMouseWrapper;
    }

    public boolean click(Location location, ActionOptions actionOptions) {
        if (BrobotSettings.mock) {
            Report.print("<click>");
            if (actionOptions.getClickType() != ClickType.Type.LEFT) Report.print(actionOptions.getClickType().name());
            Report.print(" ");
            return true;
        }
        return doClick(location, actionOptions);
    }

    private boolean doClick(Location location, ActionOptions actionOptions) {
        if (!moveMouseWrapper.move(location)) return false;
        //if (Mouse.move(location.getSikuliLocation()) == 0) return false;
        double pauseBeforeDown = actionOptions.getPauseBeforeMouseDown();
        double pauseAfterDown = actionOptions.getPauseAfterMouseDown();
        mouseDownWrapper.press(pauseBeforeDown, pauseAfterDown, actionOptions.getClickType());
        double pauseBeforeUp = actionOptions.getPauseBeforeMouseUp();
        double pauseAfterUp = actionOptions.getPauseAfterMouseUp();
        mouseUpWrapper.press(pauseBeforeUp, pauseAfterUp, actionOptions.getClickType());
        return true;
    }

}
