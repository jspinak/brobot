package actions.methods.sikuliWrappers.mouse;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.reports.Report;
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
