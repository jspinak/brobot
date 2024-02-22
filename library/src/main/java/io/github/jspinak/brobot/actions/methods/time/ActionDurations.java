package io.github.jspinak.brobot.actions.methods.time;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
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
        findDurations.put(ActionOptions.Find.FIRST, BrobotSettings.mockTimeFindFirst);
        findDurations.put(ActionOptions.Find.EACH, BrobotSettings.mockTimeFindFirst);
        findDurations.put(ActionOptions.Find.ALL, BrobotSettings.mockTimeFindAll);
        findDurations.put(ActionOptions.Find.BEST, BrobotSettings.mockTimeFindAll);
        findDurations.put(ActionOptions.Find.HISTOGRAM, BrobotSettings.mockTimeFindHistogram);
        findDurations.put(ActionOptions.Find.COLOR, BrobotSettings.mockTimeFindColor);
    }

    private final Map<ActionOptions.Action, Double> actionDurations = new HashMap<>();
    {
        actionDurations.put(ActionOptions.Action.CLICK, BrobotSettings.mockTimeClick);
        actionDurations.put(ActionOptions.Action.DRAG, BrobotSettings.mockTimeDrag);
        actionDurations.put(ActionOptions.Action.MOVE, BrobotSettings.mockTimeMove);
        actionDurations.put(ActionOptions.Action.CLASSIFY, BrobotSettings.mockTimeClassify);
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
