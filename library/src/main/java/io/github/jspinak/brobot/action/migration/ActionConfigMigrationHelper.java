package io.github.jspinak.brobot.action.migration;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionOptionsAdapter;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import org.springframework.stereotype.Component;

/**
 * Helper class to facilitate migration from ActionOptions to ActionConfig.
 * <p>
 * This utility provides convenience methods for common migration scenarios,
 * helping developers transition their code from the legacy ActionOptions
 * to the new type-safe ActionConfig hierarchy.
 * <p>
 * This is a temporary component that should be removed once the migration
 * is complete and all code has been updated to use ActionConfig directly.
 *
 * @deprecated This class is temporary and will be removed after migration
 */
@Component
@Deprecated
public class ActionConfigMigrationHelper {
    
    private final ActionOptionsAdapter adapter;
    
    public ActionConfigMigrationHelper(ActionOptionsAdapter adapter) {
        this.adapter = adapter;
    }
    
    /**
     * Converts ActionOptions to the appropriate ActionConfig subclass.
     * 
     * @param actionOptions The legacy options to convert
     * @return The corresponding ActionConfig instance
     * @throws IllegalArgumentException if the action type is not supported
     */
    public ActionConfig convert(ActionOptions actionOptions) {
        return adapter.convert(actionOptions);
    }
    
    /**
     * Creates a basic ClickOptions with common defaults.
     * 
     * @return A new ClickOptions instance with default settings
     */
    public static ClickOptions createDefaultClick() {
        return new ClickOptions.Builder().build();
    }
    
    /**
     * Creates a double-click configuration.
     * 
     * @return A new ClickOptions configured for double-click
     */
    public static ClickOptions createDoubleClick() {
        return new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
    }
    
    /**
     * Creates a right-click configuration.
     * 
     * @return A new ClickOptions configured for right-click
     * @deprecated Use setNumberOfClicks() and setPressOptions() instead
     */
    @Deprecated
    public static ClickOptions createRightClick() {
        return new ClickOptions.Builder()
            .setClickType(ClickOptions.Type.RIGHT)
            .build();
    }
    
    /**
     * Creates a basic PatternFindOptions with common defaults.
     * 
     * @param similarity The minimum similarity score (0.0-1.0)
     * @return A new PatternFindOptions instance
     */
    public static PatternFindOptions createFind(double similarity) {
        return new PatternFindOptions.Builder()
            .setSimilarity(similarity)
            .build();
    }
    
    /**
     * Creates a find-all configuration.
     * 
     * @return A new PatternFindOptions configured to find all matches
     */
    public static PatternFindOptions createFindAll() {
        return new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
    }
    
    /**
     * Creates a basic TypeOptions with default settings.
     * Note: Text content should be provided through ObjectCollection, not TypeOptions.
     * 
     * @return A new TypeOptions instance
     */
    public static TypeOptions createType() {
        return new TypeOptions.Builder().build();
    }
    
    /**
     * Creates TypeOptions with modifier keys.
     * Note: The actual keys/text should be provided through ObjectCollection.
     * 
     * @param modifiers The modifier keys as a string (e.g., "ctrl+shift")
     * @return A new TypeOptions instance
     */
    public static TypeOptions createWithModifiers(String modifiers) {
        return new TypeOptions.Builder()
            .setModifiers(modifiers)
            .build();
    }
    
    /**
     * Creates a click action followed by a mouse move.
     * This replaces the deprecated moveMouseAfterAction functionality.
     * Note: Mouse movement targets should be provided through ObjectCollection.
     * 
     * @return A new ClickOptions with chained mouse movement
     */
    public static ClickOptions createClickWithMouseMove() {
        return new ClickOptions.Builder()
            .then(new MouseMoveOptions.Builder()
                .build())
            .build();
    }
    
    /**
     * Creates a highlight configuration.
     * 
     * @param seconds The duration in seconds to highlight
     * @param color The highlight color as a string
     * @return A new HighlightOptions instance
     */
    public static HighlightOptions createHighlight(double seconds, String color) {
        return new HighlightOptions.Builder()
            .setHighlightSeconds(seconds)
            .setHighlightColor(color)
            .build();
    }
    
    /**
     * Checks if an ActionResult needs migration.
     * Note: In the current implementation, ActionResult uses ActionConfig internally.
     * 
     * @param result The ActionResult to check
     * @return true if migration is needed (always false in current implementation)
     */
    public static boolean needsMigration(ActionResult result) {
        // ActionResult now uses ActionConfig internally
        return false;
    }
    
    /**
     * Provides migration guidance for common patterns.
     * 
     * @param pattern The pattern name (e.g., "moveMouseAfterAction", "dragOffset")
     * @return A string explaining how to migrate this pattern
     */
    public static String getMigrationGuide(String pattern) {
        switch (pattern.toLowerCase()) {
            case "movemouseafteraction":
                return "Use action chaining: new ClickOptions.Builder().then(new MouseMoveOptions.Builder().setLocation(target).build()).build()";
            case "dragoffset":
                return "Use addW/addH in the chained move action for the new composite DRAG action";
            case "addx2":
            case "addy2":
                return "Use addX/addY in the chained move action";
            case "findactions":
                return "Chain find actions using .then() method on the builder";
            case "gettextuntil":
                return "Use RepeatUntilConfig for flexible validation conditions including text";
            case "keeplargermatches":
                return "Use ChainingStrategy.CONFIRM in ActionChainOptions";
            case "successcriteria":
                return "Use setSuccessCriteria() on the ActionConfig builder (note: type changed from Predicate<Matches> to Predicate<ActionResult>)";
            default:
                return "No specific migration guide available for pattern: " + pattern;
        }
    }
}