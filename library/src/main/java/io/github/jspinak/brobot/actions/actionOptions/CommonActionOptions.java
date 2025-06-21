package io.github.jspinak.brobot.actions.actionOptions;

import org.springframework.stereotype.Component;

/**
 * Provides factory methods for commonly used ActionOptions configurations.
 * 
 * <p>CommonActionOptions simplifies the creation of frequently needed ActionOptions patterns, 
 * reducing boilerplate code and promoting consistency across automation scripts. It encapsulates 
 * best practices for common action configurations, making the framework more accessible to 
 * users who may not be familiar with all available options.</p>
 * 
 * <p>Predefined configurations:
 * <ul>
 *   <li><b>Standard</b>: Basic action configuration with specified action type and wait time</li>
 *   <li><b>Find and Multiple Clicks</b>: Optimized for scenarios requiring multiple clicks 
 *       on the same element</li>
 *   <li><b>Type</b>: Keyboard input configurations with optional modifier keys</li>
 * </ul>
 * </p>
 * 
 * <p>Benefits:
 * <ul>
 *   <li>Reduces code duplication for common action patterns</li>
 *   <li>Ensures consistent configuration across the application</li>
 *   <li>Provides semantic method names that clarify intent</li>
 *   <li>Simplifies the learning curve for new framework users</li>
 * </ul>
 * </p>
 * 
 * <p>Usage examples:
 * <ul>
 *   <li>Standard click with 5-second wait: {@code standard(Action.CLICK, 5.0)}</li>
 *   <li>Double-click: {@code findAndMultipleClicks(3.0, 2)}</li>
 *   <li>Type with Ctrl modifier: {@code type("CTRL")}</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, CommonActionOptions promotes standardization of action 
 * configurations, which is essential for maintaining predictable automation behavior across 
 * different parts of the application. This standardization helps in debugging and ensures 
 * consistent timing and behavior patterns.</p>
 * 
 * @since 1.0
 * @see ActionOptions
 * @see ActionOptions.Builder
 * @see ActionOptions.Action
 */
@Component
public class CommonActionOptions {

    public ActionOptions standard(ActionOptions.Action action, double maxWait) {
        return new ActionOptions.Builder()
                .setAction(action)
                .setMaxWait(maxWait)
                .build();
    }

    public ActionOptions findAndMultipleClicks(double maxWait, int clicks) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setMaxWait(maxWait)
                .setTimesToRepeatIndividualAction(clicks)
                .build();
    }

    public ActionOptions type(String modifier) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .setModifiers(modifier)
                .build();
    }

    public ActionOptions type() {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .build();
    }
}
