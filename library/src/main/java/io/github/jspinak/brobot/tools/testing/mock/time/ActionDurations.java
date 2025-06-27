package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.config.FrameworkSettings;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock durations for different Actions.
 */
@Component
public class ActionDurations {

    private final Map<ActionOptions.Find, Double> findDurations = new HashMap<>();
    {
        findDurations.put(ActionOptions.Find.FIRST, FrameworkSettings.mockTimeFindFirst);
        findDurations.put(ActionOptions.Find.EACH, FrameworkSettings.mockTimeFindFirst);
        findDurations.put(ActionOptions.Find.ALL, FrameworkSettings.mockTimeFindAll);
        findDurations.put(ActionOptions.Find.BEST, FrameworkSettings.mockTimeFindAll);
        findDurations.put(ActionOptions.Find.HISTOGRAM, FrameworkSettings.mockTimeFindHistogram);
        findDurations.put(ActionOptions.Find.COLOR, FrameworkSettings.mockTimeFindColor);
    }

    private final Map<ActionOptions.Action, Double> actionDurations = new HashMap<>();
    {
        actionDurations.put(ActionOptions.Action.CLICK, FrameworkSettings.mockTimeClick);
        actionDurations.put(ActionOptions.Action.DRAG, FrameworkSettings.mockTimeDrag);
        actionDurations.put(ActionOptions.Action.MOVE, FrameworkSettings.mockTimeMove);
        actionDurations.put(ActionOptions.Action.CLASSIFY, FrameworkSettings.mockTimeClassify);
    }

    /**
     * Returns the mock duration of a Find operation.
     *
     * @param find the type of Find operation
     * @return a double, which can be converted later to an int if necessary.
     */
    public double getFindDuration(ActionOptions.Find find) {
        if (!findDurations.containsKey(find)) return 0.0;
        return findDurations.get(find);
    }

    public double getActionDuration(ActionOptions.Action action) {
        if (!actionDurations.containsKey(action)) return 0.0;
        return actionDurations.get(action);
    }

}
