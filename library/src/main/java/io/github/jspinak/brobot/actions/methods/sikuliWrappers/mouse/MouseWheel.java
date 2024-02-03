package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
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
        new Region().sikuli().wheel(
                scrollInt.get(actionOptions.getScrollDirection()),
                actionOptions.getTimesToRepeatIndividualAction()
        );
        return true;
    }
}
