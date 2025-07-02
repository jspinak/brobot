package io.github.jspinak.brobot.action;

import org.springframework.stereotype.Component;

/**
 * Provides factory methods for commonly used ActionOptions configurations.
 * 
 * @deprecated Use {@link ActionConfigShortcuts} instead. This class uses the deprecated
 *             ActionOptions API. The new ActionConfigShortcuts provides the same functionality
 *             with the modern ActionConfig architecture.
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
@Deprecated
public class ActionShortcuts {

    /**
     * Creates a standard action configuration with specified type and timeout.
     * <p>
     * This is the most basic configuration pattern, suitable for simple
     * operations that need only an action type and maximum wait time.
     * All other options use framework defaults.
     *
     * @param action The type of action to perform (CLICK, FIND, TYPE, etc.)
     * @param maxWait Maximum time in seconds to attempt the action
     * @return ActionOptions configured with the specified parameters
     */
    public ActionOptions standard(ActionOptions.Action action, double maxWait) {
        return new ActionOptions.Builder()
                .setAction(action)
                .setMaxWait(maxWait)
                .build();
    }

    /**
     * Creates a click action configured for multiple clicks on the same target.
     * <p>
     * Optimized for scenarios like double-clicking or repeated clicking
     * on stubborn UI elements. The action will first find the target,
     * then click it the specified number of times.
     * <p>
     * Common uses:
     * <ul>
     * <li>Double-click: {@code findAndMultipleClicks(3.0, 2)}</li>
     * <li>Triple-click to select all: {@code findAndMultipleClicks(3.0, 3)}</li>
     * <li>Repeated clicks for stubborn buttons: {@code findAndMultipleClicks(5.0, 5)}</li>
     * </ul>
     *
     * @param maxWait Maximum time in seconds to find the target
     * @param clicks Number of times to click once target is found
     * @return ActionOptions configured for multiple clicks
     */
    public ActionOptions findAndMultipleClicks(double maxWait, int clicks) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setMaxWait(maxWait)
                .setTimesToRepeatIndividualAction(clicks)
                .build();
    }

    /**
     * Creates a type action with keyboard modifiers.
     * <p>
     * Used for keyboard shortcuts and modified key presses. The modifier
     * string follows Sikuli conventions: "CTRL", "ALT", "SHIFT", "CMD".
     * Multiple modifiers can be combined: "CTRL+SHIFT".
     * <p>
     * Examples:
     * <ul>
     * <li>Ctrl+C: {@code type("CTRL").perform(Action.TYPE, "c")}</li>
     * <li>Alt+Tab: {@code type("ALT").perform(Action.TYPE, Key.TAB)}</li>
     * <li>Ctrl+Shift+T: {@code type("CTRL+SHIFT").perform(Action.TYPE, "t")}</li>
     * </ul>
     *
     * @param modifier Keyboard modifier keys (CTRL, ALT, SHIFT, CMD)
     * @return ActionOptions configured for typing with modifiers
     */
    public ActionOptions type(String modifier) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .setModifiers(modifier)
                .build();
    }

    /**
     * Creates a basic type action without modifiers.
     * <p>
     * Used for simple text input without special keys. The actual text
     * to type is provided through the ObjectCollection when performing
     * the action.
     * <p>
     * Example: {@code type().perform(Action.TYPE, "Hello World")}
     *
     * @return ActionOptions configured for basic typing
     */
    public ActionOptions type() {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .build();
    }
}
