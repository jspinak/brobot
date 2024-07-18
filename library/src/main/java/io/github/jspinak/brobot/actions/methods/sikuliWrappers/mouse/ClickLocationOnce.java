package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Region;
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
        double pauseBeforeUp = actionOptions.getPauseBeforeMouseUp();
        double pauseAfterUp = actionOptions.getPauseAfterMouseUp();

        if (isSimpleLeftDoubleClick(actionOptions)) {
            new Region(location.getX(), location.getY(), 0, 0).doubleClick();
        } else {
            int i = 1;
            if (isTwoClicks(actionOptions)) i = 2;
            for (int j=0; j<i; j++) {
                mouseDownWrapper.press(pauseBeforeDown, pauseAfterDown, actionOptions.getClickType());
                mouseUpWrapper.press(pauseBeforeUp, pauseAfterUp, actionOptions.getClickType());
            }
        }
        return true;
    }

    /**
     * A double-click has a shorter pause between clicks as two separate clicks.
     * If there is a pause, two separate clicks will be used.
     * @param actionOptions the click options
     * @return true if clicked
     */
    private boolean isSimpleLeftDoubleClick(ActionOptions actionOptions) {
        return actionOptions.getClickType() == ClickType.Type.DOUBLE_LEFT
                && actionOptions.getPauseAfterMouseDown() == 0
                && actionOptions.getPauseBeforeMouseUp() == 0
                && actionOptions.getPauseBeforeMouseDown() == 0
                && actionOptions.getPauseAfterMouseUp() == 0;
    }

    /**
     * If there is a pause, two separate clicks will be used.
     * There is no double-click for the middle or right mouse button in Sikuli.
     * @param actionOptions the action's options
     * @return true if a double-click should be used
     */
    private boolean isTwoClicks(ActionOptions actionOptions) {
        if (actionOptions.getClickType() == ClickType.Type.DOUBLE_RIGHT
                || actionOptions.getClickType() == ClickType.Type.DOUBLE_MIDDLE)
            return true;
        if (actionOptions.getClickType() != ClickType.Type.DOUBLE_LEFT) return false;
        return actionOptions.getPauseAfterMouseDown() > 0 ||
                actionOptions.getPauseBeforeMouseUp() > 0 ||
                actionOptions.getPauseBeforeMouseDown() > 0 ||
                actionOptions.getPauseAfterMouseUp() > 0;
    }

}
