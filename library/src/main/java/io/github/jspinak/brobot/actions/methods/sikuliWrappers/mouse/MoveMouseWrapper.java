package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.FindFailed;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for MoveMouse, works for real or mocked actions.
 * Moves the mouse to a given Location.
 */
@Component
public class MoveMouseWrapper {

    private boolean sikuliMove(Location location) {
        try {
            return new Region().mouseMove(location.getSikuliLocation()) != 0;
        } catch (FindFailed findFailed) {
            findFailed.printStackTrace();
            return false;
        }
    }

    public boolean move(Location location) {
        if (BrobotSettings.mock) {
            Report.format(Report.OutputLevel.HIGH, "%s: %d.%d| ", "mouseMove to",
                    location.getX(), location.getY());
            return true;
        }
        return sikuliMove(location);
    }

}
