package actions.methods.sikuliWrappers.mouse;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.reports.Report;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for MouseWheel, works for real or mocked actions.
 * Scrolls the mouse wheel up or down.
 */
@Component
public class MouseWheel {

    private Map<ActionOptions.ScrollDirection, Integer> scrollInt = new HashMap<>();
    {
        scrollInt.put(ActionOptions.ScrollDirection.DOWN, 1);
        scrollInt.put(ActionOptions.ScrollDirection.UP, -1);
    }

    public boolean scroll(ActionOptions actionOptions) {
        if (BrobotSettings.mock) {
            Report.format("%s %d %s", "scroll", actionOptions.getTimesToRepeatIndividualAction(), "times. ");
            return true;
        }
        new Region().wheel(
                scrollInt.get(actionOptions.getScrollDirection()),
                actionOptions.getTimesToRepeatIndividualAction()
        );
        return true;
    }
}
