package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import org.springframework.stereotype.Component;

/**
 * Provides factory methods for commonly used ActionConfig configurations.
 * 
 * <p>ActionConfigShortcuts simplifies the creation of frequently needed ActionConfig patterns, 
 * reducing boilerplate code and promoting consistency across automation scripts. It encapsulates 
 * best practices for common action configurations, making the framework more accessible to 
 * users who may not be familiar with all available options.</p>
 * 
 * <p>This is the modern replacement for {@link ActionShortcuts}, using the new ActionConfig
 * architecture instead of the deprecated ActionOptions.</p>
 * 
 * <p>Predefined configurations:
 * <ul>
 *   <li><b>Click</b>: Various click configurations (single, double, right-click)</li>
 *   <li><b>Find</b>: Pattern finding with configurable similarity and strategy</li>
 *   <li><b>Type</b>: Keyboard input configurations with optional modifier keys</li>
 *   <li><b>Vanish</b>: Wait for elements to disappear</li>
 * </ul>
 * </p>
 * 
 * <p>Benefits:
 * <ul>
 *   <li>Type-safe configuration with specific Options classes</li>
 *   <li>Clearer API with method names that match intent</li>
 *   <li>Better IDE support with auto-completion</li>
 *   <li>Easier to extend with new configuration types</li>
 * </ul>
 * </p>
 * 
 * <p>Usage examples:
 * <pre>{@code
 * // Simple left click
 * ClickOptions click = shortcuts.click();
 * 
 * // Double-click
 * ClickOptions doubleClick = shortcuts.doubleClick();
 * 
 * // Right-click with custom timing
 * ClickOptions rightClick = shortcuts.rightClick(0.5, 0.5);
 * 
 * // Find with 90% similarity
 * PatternFindOptions find = shortcuts.find(0.9);
 * 
 * // Type with Ctrl modifier
 * TypeOptions ctrlType = shortcuts.type("CTRL");
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 * @see ActionConfig
 * @see ClickOptions
 * @see PatternFindOptions
 * @see TypeOptions
 */
@Component
public class ActionConfigShortcuts {

    /**
     * Creates a basic single left-click configuration.
     * 
     * @return ClickOptions for a simple left click
     */
    public ClickOptions click() {
        return new ClickOptions.Builder().build();
    }
    
    /**
     * Creates a single left-click with custom timing.
     * 
     * @param pauseBeforeBegin Pause in seconds before starting the click
     * @param pauseAfterEnd Pause in seconds after completing the click
     * @return ClickOptions with custom timing
     */
    public ClickOptions click(double pauseBeforeBegin, double pauseAfterEnd) {
        return new ClickOptions.Builder()
                .setPauseBeforeBegin(pauseBeforeBegin)
                .setPauseAfterEnd(pauseAfterEnd)
                .build();
    }

    /**
     * Creates a double-click configuration.
     * 
     * @return ClickOptions for a double-click
     */
    public ClickOptions doubleClick() {
        return new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
    }
    
    /**
     * Creates a double-click with custom timing between clicks.
     * 
     * @param pauseBetweenClicks Pause in seconds between the two clicks
     * @return ClickOptions for a double-click with custom timing
     */
    public ClickOptions doubleClick(double pauseBetweenClicks) {
        return new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPressOptions(new MousePressOptions.Builder()
                        .setPauseAfterMouseUp(pauseBetweenClicks))
                .build();
    }

    /**
     * Creates a right-click configuration.
     * 
     * @return ClickOptions for a right-click
     */
    public ClickOptions rightClick() {
        return new ClickOptions.Builder()
                .setPressOptions(new MousePressOptions.Builder()
                        .setButton(MouseButton.RIGHT))
                .build();
    }
    
    /**
     * Creates a right-click with custom timing.
     * 
     * @param pauseBeforeBegin Pause in seconds before starting the click
     * @param pauseAfterEnd Pause in seconds after completing the click
     * @return ClickOptions for a right-click with custom timing
     */
    public ClickOptions rightClick(double pauseBeforeBegin, double pauseAfterEnd) {
        return new ClickOptions.Builder()
                .setPauseBeforeBegin(pauseBeforeBegin)
                .setPauseAfterEnd(pauseAfterEnd)
                .setPressOptions(new MousePressOptions.Builder()
                        .setButton(MouseButton.RIGHT))
                .build();
    }
    
    /**
     * Creates a click configuration for multiple clicks on the same target.
     * <p>
     * Useful for scenarios like triple-click to select all text, or
     * repeated clicks on stubborn UI elements.
     * 
     * @param numberOfClicks Number of times to click
     * @return ClickOptions configured for multiple clicks
     */
    public ClickOptions multipleClicks(int numberOfClicks) {
        return new ClickOptions.Builder()
                .setNumberOfClicks(numberOfClicks)
                .build();
    }

    /**
     * Creates a basic pattern find configuration with default similarity.
     * 
     * @return PatternFindOptions with default settings
     */
    public PatternFindOptions find() {
        return new PatternFindOptions.Builder().build();
    }
    
    /**
     * Creates a pattern find configuration with custom similarity.
     * 
     * @param similarity Minimum similarity threshold (0.0 to 1.0)
     * @return PatternFindOptions with custom similarity
     */
    public PatternFindOptions find(double similarity) {
        return new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .build();
    }
    
    /**
     * Creates a find configuration for finding the best match.
     * 
     * @param similarity Minimum similarity threshold (0.0 to 1.0)
     * @return PatternFindOptions configured to find the best match
     */
    public PatternFindOptions findBest(double similarity) {
        return new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
    }
    
    /**
     * Creates a find configuration for finding all matches.
     * 
     * @param similarity Minimum similarity threshold (0.0 to 1.0)
     * @return PatternFindOptions configured to find all matches
     */
    public PatternFindOptions findAll(double similarity) {
        return new PatternFindOptions.Builder()
                .setSimilarity(similarity)
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
    }

    /**
     * Creates a type action configuration with keyboard modifiers.
     * <p>
     * Used for keyboard shortcuts and modified key presses. The modifier
     * string follows standard conventions: "CTRL", "ALT", "SHIFT", "CMD".
     * Multiple modifiers can be combined: "CTRL+SHIFT".
     * 
     * @param modifiers Keyboard modifier keys (CTRL, ALT, SHIFT, CMD)
     * @return TypeOptions configured with modifiers
     */
    public TypeOptions type(String modifiers) {
        return new TypeOptions.Builder()
                .setModifiers(modifiers)
                .build();
    }
    
    /**
     * Creates a basic type action configuration without modifiers.
     * 
     * @return TypeOptions with default settings
     */
    public TypeOptions type() {
        return new TypeOptions.Builder().build();
    }
    
    /**
     * Creates a type action with custom delay between keystrokes.
     * <p>
     * Useful for applications that need slower typing to process input correctly.
     * 
     * @param typeDelay Delay in seconds between each keystroke
     * @return TypeOptions with custom type delay
     */
    public TypeOptions typeSlowly(double typeDelay) {
        return new TypeOptions.Builder()
                .setTypeDelay(typeDelay)
                .build();
    }

    /**
     * Creates a vanish action configuration.
     * <p>
     * Used to wait for elements to disappear from the screen.
     * 
     * @return VanishOptions with default settings
     */
    public VanishOptions vanish() {
        return new VanishOptions.Builder().build();
    }
    
    /**
     * Creates a vanish action with custom timeout.
     * 
     * @param timeout Maximum time in seconds to wait for elements to vanish
     * @return VanishOptions with custom timeout
     */
    public VanishOptions vanish(double timeout) {
        return new VanishOptions.Builder()
                .setTimeout(timeout)
                .build();
    }
}