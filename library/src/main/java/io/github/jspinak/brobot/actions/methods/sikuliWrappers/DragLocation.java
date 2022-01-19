package io.github.jspinak.brobot.actions.methods.sikuliWrappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.mock.Mock;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for Drag, handles real and mock drags.
 * Drags from one Location to another Location.
 *
 * Typical settings:
 * 0.3 PauseBeforeMouseDown
 * 0.3 PauseAfterMouseDown
 * 0.5 MoveMouseDelay
 * 0.4 PauseBeforeMouseUp
 * 0.0 PauseAfterMouseUp
 */
@Component
public class DragLocation {
    private Mock mock;
    private Wait wait;

    public DragLocation(Mock mock, Wait wait) {
        this.mock = mock;
        this.wait = wait;
    }

    private boolean drag(Location from, Location to) {
        try {
            if (Report.minReportingLevel(Report.OutputLevel.HIGH))
                System.out.format("drag %d.%d to %d.%d| ",
                        from.getX(), from.getY(), to.getX(), to.getY());
            new Region().dragDrop(from.getSikuliLocation(), to.getSikuliLocation());
        } catch (FindFailed findFailed) {
            if (Report.minReportingLevel(Report.OutputLevel.HIGH))
                System.out.print("|drag failed| ");
            return false;
        }
        return true;
    }

    public boolean drag(Location from, Location to, ActionOptions actionOptions) {
        //System.out.println("to.getY, drag offset y:"+to.getY()+" "+actionOptions.getDragToOffsetY());
        if (BrobotSettings.mock) return mock.drag(from, to);
        Settings.DelayBeforeMouseDown = actionOptions.getPauseBeforeMouseDown();
        Settings.DelayBeforeDrag = actionOptions.getPauseAfterMouseDown();
        Settings.MoveMouseDelay = actionOptions.getMoveMouseDelay();
        Settings.DelayBeforeDrop = actionOptions.getPauseBeforeMouseUp();
        if (!drag(from, to)) return false;
        wait.wait(actionOptions.getPauseAfterMouseUp());
        return true;
    }
}
