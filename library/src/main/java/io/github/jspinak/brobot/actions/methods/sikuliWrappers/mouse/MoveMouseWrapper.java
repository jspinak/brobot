package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for MoveMouse, works for real or mocked actions.
 * Moves the mouse to a given Location.
 */
@Component
public class MoveMouseWrapper {

    private boolean sikuliMove(Location location) {
        org.sikuli.script.Location sikuliLocation = location.sikuli();
        Report.print("move mouse to "+sikuliLocation+" ");
        //return new Region().mouseMove(location.getSikuliLocation()) != 0; // this can cause the script to freeze for unknown reasons
        return location.sikuli().hover() != null;
    }

    public boolean move(Location location) {
        if (BrobotSettings.mock) {
            Report.format(Report.OutputLevel.HIGH, "%s: %d.%d| ", "mouseMove to",
                    location.getX(), location.getY());
            return true;
        }
        boolean success = sikuliMove(location);
        if (!success) Report.print("move failed. ");
        //else Report.print("move succeeded. ");
        return success;
    }

}
