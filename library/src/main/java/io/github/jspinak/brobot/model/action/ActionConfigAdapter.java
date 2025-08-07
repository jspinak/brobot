package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
// import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions.Action;
import io.github.jspinak.brobot.action.internal.options.ActionOptions.Find;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter for bridging ActionConfig and ActionOptions in ActionRecord storage.
 * <p>
 * This adapter enables ActionRecord to work with both the legacy ActionOptions API
 * and the modern ActionConfig API. It provides methods to:
 * <ul>
 *   <li>Convert ActionConfig instances to ActionOptions for storage</li>
 *   <li>Extract action type information from ActionConfig</li>
 *   <li>Create ActionRecord instances that use ActionConfig</li>
 * </ul>
 * 
 * <p>This adapter is necessary because ActionRecord is a model/entity class that
 * stores historical action data. Changing its structure would break existing
 * persisted records and require database migrations. This adapter allows the
 * framework to use ActionConfig everywhere while maintaining backward compatibility
 * for stored records.</p>
 * 
 * <p>Future considerations:
 * <ul>
 *   <li>A future version might add an actionConfig field to ActionRecord</li>
 *   <li>Migration tools could convert stored ActionOptions to ActionConfig</li>
 *   <li>New storage format could support both APIs during transition</li>
 * </ul>
 * 
 * @see ActionRecord
 * @see ActionConfig
 * @see ActionOptions
 * @see ActionOptionsAdapter
 */
@Component
public class ActionConfigAdapter {
    
    /**
     * Cache for ActionConfig class to Action type mappings.
     * Using ConcurrentHashMap for thread-safe caching.
     */
    private final Map<Class<? extends ActionConfig>, Action> actionTypeCache = new ConcurrentHashMap<>();
    
    /**
     * Cache for PatternFindOptions.Strategy to Find enum mappings.
     */
    private final Map<PatternFindOptions.Strategy, Find> findStrategyCache = new ConcurrentHashMap<>();

    /**
     * Creates an ActionRecord with configuration from an ActionConfig.
     * <p>
     * This method converts the ActionConfig to ActionOptions for storage
     * while preserving all relevant settings. The conversion is handled
     * by ActionOptionsAdapter which maintains mapping logic.
     * 
     * @param actionConfig The ActionConfig to store in the record
     * @return A new ActionRecord builder with the configuration set
     */
    public ActionRecord.Builder createRecordBuilder(ActionConfig actionConfig) {
        // Create a minimal ActionOptions that captures the action type
        // In the future, we could store the full ActionConfig in ActionRecord
        Action actionType = getActionType(actionConfig);
        ActionOptions.Builder optionsBuilder = new ActionOptions.Builder()
                .setAction(actionType);
        
        // Set find strategy if applicable
        if (isFindOperation(actionConfig)) {
            optionsBuilder.setFind(getFindStrategy(actionConfig));
        }
        
        // Copy common timing settings
        optionsBuilder.setPauseBeforeBegin(actionConfig.getPauseBeforeBegin());
        optionsBuilder.setPauseAfterEnd(actionConfig.getPauseAfterEnd());
        
        return new ActionRecord.Builder()
                .setActionOptions(optionsBuilder.build());
    }

    /**
     * Extracts the action type from an ActionConfig with caching for performance.
     * <p>
     * This method determines which Action enum value corresponds to the
     * given ActionConfig implementation. Results are cached to avoid repeated
     * instanceof checks for the same config types.
     * 
     * @param actionConfig The ActionConfig to analyze
     * @return The corresponding Action enum value
     */
    public Action getActionType(ActionConfig actionConfig) {
        if (actionConfig == null) {
            return Action.FIND; // Default for null
        }
        
        // Check cache first
        Class<? extends ActionConfig> configClass = actionConfig.getClass();
        return actionTypeCache.computeIfAbsent(configClass, this::computeActionType);
    }
    
    /**
     * Computes the action type for a given ActionConfig class.
     * This method is called only when the type is not in the cache.
     * 
     * @param configClass The ActionConfig class to analyze
     * @return The corresponding Action enum value
     */
    private Action computeActionType(Class<? extends ActionConfig> configClass) {
        if (PatternFindOptions.class.isAssignableFrom(configClass)) {
            return Action.FIND;
        }
        if (ClickOptions.class.isAssignableFrom(configClass)) {
            return Action.CLICK;
        }
        if (TypeOptions.class.isAssignableFrom(configClass)) {
            return Action.TYPE;
        }
        if (VanishOptions.class.isAssignableFrom(configClass)) {
            return Action.VANISH;
        }
        if (MouseMoveOptions.class.isAssignableFrom(configClass)) {
            return Action.MOVE;
        }
        if (MouseDownOptions.class.isAssignableFrom(configClass)) {
            return Action.MOUSE_DOWN;
        }
        if (MouseUpOptions.class.isAssignableFrom(configClass)) {
            return Action.MOUSE_UP;
        }
        // ScrollOptions, KeyDownOptions, KeyUpOptions would go here when available
        if (DefineRegionOptions.class.isAssignableFrom(configClass)) {
            return Action.DEFINE;
        }
        if (HighlightOptions.class.isAssignableFrom(configClass)) {
            return Action.HIGHLIGHT;
        }
        if (DragOptions.class.isAssignableFrom(configClass)) {
            return Action.DRAG;
        }
        
        // Default for unknown types
        return Action.FIND;
    }

    /**
     * Extracts the find strategy from a find-related ActionConfig with caching.
     * <p>
     * For PatternFindOptions, this maps the Strategy enum to the legacy
     * Find enum. Results are cached to improve performance. For other action 
     * types, returns UNIVERSAL as default.
     * 
     * @param actionConfig The ActionConfig to analyze
     * @return The corresponding Find enum value
     */
    public Find getFindStrategy(ActionConfig actionConfig) {
        if (actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            PatternFindOptions.Strategy strategy = findOptions.getStrategy();
            
            // Use cache for strategy mapping
            return findStrategyCache.computeIfAbsent(strategy, this::computeFindStrategy);
        }
        // ColorFindOptions support commented out - class not available
        // if (actionConfig instanceof ColorFindOptions) {
        //     ColorFindOptions colorOptions = (ColorFindOptions) actionConfig;
        //     if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
        //         return Find.COLOR;
        //     }
        //     return Find.HISTOGRAM;
        // }
        
        return Find.UNIVERSAL;
    }
    
    /**
     * Computes the Find enum value for a given PatternFindOptions.Strategy.
     * This method is called only when the strategy is not in the cache.
     * 
     * @param strategy The strategy to convert
     * @return The corresponding Find enum value
     */
    private Find computeFindStrategy(PatternFindOptions.Strategy strategy) {
        switch (strategy) {
            case FIRST:
                return Find.FIRST;
            case BEST:
                return Find.BEST;
            case ALL:
                return Find.ALL;
            case EACH:
                return Find.EACH;
            default:
                return Find.UNIVERSAL;
        }
    }

    /**
     * Creates an ActionRecord that captures both ActionConfig and ActionOptions.
     * <p>
     * This is a convenience method that creates a complete ActionRecord with
     * the ActionConfig converted to ActionOptions for storage. Additional
     * record properties can be set using the returned builder.
     * 
     * @param actionConfig The ActionConfig used in the action
     * @param actionSuccess Whether the action was successfully performed
     * @param resultSuccess Whether the result met expectations
     * @return A new ActionRecord configured with the action details
     */
    public ActionRecord createRecord(ActionConfig actionConfig, 
                                   boolean actionSuccess, 
                                   boolean resultSuccess) {
        return createRecordBuilder(actionConfig)
                .setActionSuccess(actionSuccess)
                .setResultSuccess(resultSuccess)
                .build();
    }

    /**
     * Checks if an ActionConfig represents a find operation.
     * <p>
     * This is useful for determining whether match data should be
     * expected in the ActionRecord.
     * 
     * @param actionConfig The ActionConfig to check
     * @return true if this is a find operation
     */
    public boolean isFindOperation(ActionConfig actionConfig) {
        return actionConfig instanceof PatternFindOptions;
        // ColorFindOptions support commented out - class not available
        // || actionConfig instanceof ColorFindOptions;
    }

    /**
     * Checks if an ActionConfig represents a text operation.
     * <p>
     * This is useful for determining whether text data should be
     * expected in the ActionRecord.
     * 
     * @param actionConfig The ActionConfig to check
     * @return true if this is a text-related operation
     */
    public boolean isTextOperation(ActionConfig actionConfig) {
        Action action = getActionType(actionConfig);
        return action == Action.TYPE;
    }

    /**
     * Creates a simple ActionRecord for a find operation.
     * <p>
     * This is a convenience method for the common case of recording
     * find results with ActionConfig.
     * 
     * @param actionConfig The find configuration used
     * @param matchCount The number of matches found
     * @param duration The time taken for the operation
     * @return A new ActionRecord with the find results
     */
    public ActionRecord createFindRecord(ActionConfig actionConfig, 
                                       int matchCount, 
                                       double duration) {
        return createRecordBuilder(actionConfig)
                .setActionSuccess(matchCount > 0)
                .setDuration(duration)
                .addMatches(matchCount, 100, 50) // Default size for mock matches
                .build();
    }
}