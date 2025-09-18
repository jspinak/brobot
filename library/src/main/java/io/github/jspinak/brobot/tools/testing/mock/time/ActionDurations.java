package io.github.jspinak.brobot.tools.testing.mock.time;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;

/** Mock durations for different Actions. */
@Component
public class ActionDurations {

    @Autowired private BrobotProperties brobotProperties;

    // Removed ActionConfig-based durations - use ActionType and Strategy-based
    // maps instead

    // New ActionType-based durations for migration
    private final Map<ActionType, Double> actionTypeDurations = new HashMap<>();

    {
        actionTypeDurations.put(ActionType.CLICK, brobotProperties.getMock().getTimeClick());
        actionTypeDurations.put(ActionType.DRAG, brobotProperties.getMock().getTimeDrag());
        actionTypeDurations.put(ActionType.MOVE, brobotProperties.getMock().getTimeMove());
        actionTypeDurations.put(ActionType.CLASSIFY, brobotProperties.getMock().getTimeClassify());
    }

    // Pattern find strategy durations
    private final Map<PatternFindOptions.Strategy, Double> strategyDurations = new HashMap<>();

    {
        strategyDurations.put(
                PatternFindOptions.Strategy.FIRST, brobotProperties.getMock().getTimeFindFirst());
        strategyDurations.put(
                PatternFindOptions.Strategy.EACH, brobotProperties.getMock().getTimeFindFirst());
        strategyDurations.put(
                PatternFindOptions.Strategy.ALL, brobotProperties.getMock().getTimeFindAll());
        strategyDurations.put(
                PatternFindOptions.Strategy.BEST, brobotProperties.getMock().getTimeFindAll());
    }

    // Removed deprecated methods getFindDuration(ActionConfig.Find) and
    // getActionDuration(ActionType). Use the ActionType and Strategy versions
    // instead.

    /**
     * Returns the mock duration of an action using ActionType. This is the modern approach using
     * ActionType instead of ActionType.
     *
     * @param actionType the type of action
     * @return a double representing the mock duration in seconds
     */
    public double getActionDuration(ActionType actionType) {
        if (!actionTypeDurations.containsKey(actionType)) return 0.0;
        return actionTypeDurations.get(actionType);
    }

    /**
     * Returns the mock duration of a find strategy. This is the modern approach using
     * PatternFindOptions.Strategy.
     *
     * @param strategy the find strategy
     * @return a double representing the mock duration in seconds
     */
    public double getFindStrategyDuration(PatternFindOptions.Strategy strategy) {
        if (!strategyDurations.containsKey(strategy)) return 0.0;
        return strategyDurations.get(strategy);
    }
}
