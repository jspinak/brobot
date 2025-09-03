package io.github.jspinak.brobot.tools.testing.mock.time;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock durations for different Actions.
 */
@Component
public class ActionDurations {

    // Removed ActionOptions-based durations - use ActionType and Strategy-based
    // maps instead

    // New ActionType-based durations for migration
    private final Map<ActionType, Double> actionTypeDurations = new HashMap<>();
    {
        actionTypeDurations.put(ActionType.CLICK, FrameworkSettings.mockTimeClick);
        actionTypeDurations.put(ActionType.DRAG, FrameworkSettings.mockTimeDrag);
        actionTypeDurations.put(ActionType.MOVE, FrameworkSettings.mockTimeMove);
        actionTypeDurations.put(ActionType.CLASSIFY, FrameworkSettings.mockTimeClassify);
    }

    // Pattern find strategy durations
    private final Map<PatternFindOptions.Strategy, Double> strategyDurations = new HashMap<>();
    {
        strategyDurations.put(PatternFindOptions.Strategy.FIRST, FrameworkSettings.mockTimeFindFirst);
        strategyDurations.put(PatternFindOptions.Strategy.EACH, FrameworkSettings.mockTimeFindFirst);
        strategyDurations.put(PatternFindOptions.Strategy.ALL, FrameworkSettings.mockTimeFindAll);
        strategyDurations.put(PatternFindOptions.Strategy.BEST, FrameworkSettings.mockTimeFindAll);
    }

    // Removed deprecated methods getFindDuration(ActionOptions.Find) and
    // getActionDuration(ActionType). Use the ActionType and Strategy versions
    // instead.

    /**
     * Returns the mock duration of an action using ActionType.
     * This is the modern approach using ActionType instead of ActionType.
     *
     * @param actionType the type of action
     * @return a double representing the mock duration in seconds
     */
    public double getActionDuration(ActionType actionType) {
        if (!actionTypeDurations.containsKey(actionType))
            return 0.0;
        return actionTypeDurations.get(actionType);
    }

    /**
     * Returns the mock duration of a find strategy.
     * This is the modern approach using PatternFindOptions.Strategy.
     *
     * @param strategy the find strategy
     * @return a double representing the mock duration in seconds
     */
    public double getFindStrategyDuration(PatternFindOptions.Strategy strategy) {
        if (!strategyDurations.containsKey(strategy))
            return 0.0;
        return strategyDurations.get(strategy);
    }

}
