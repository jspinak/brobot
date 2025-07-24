package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
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
     * Extracts the action type from an ActionConfig.
     * <p>
     * This method determines which Action enum value corresponds to the
     * given ActionConfig implementation. This is useful for logging,
     * debugging, and analytics.
     * 
     * @param actionConfig The ActionConfig to analyze
     * @return The corresponding Action enum value
     */
    public Action getActionType(ActionConfig actionConfig) {
        if (actionConfig instanceof PatternFindOptions || 
            actionConfig instanceof ColorFindOptions) {
            return Action.FIND;
        }
        if (actionConfig instanceof ClickOptions) {
            return Action.CLICK;
        }
        if (actionConfig instanceof TypeOptions) {
            return Action.TYPE;
        }
        if (actionConfig instanceof VanishOptions) {
            return Action.VANISH;
        }
        if (actionConfig instanceof MouseMoveOptions) {
            return Action.MOVE;
        }
        if (actionConfig instanceof MouseDownOptions) {
            return Action.MOUSE_DOWN;
        }
        if (actionConfig instanceof MouseUpOptions) {
            return Action.MOUSE_UP;
        }
        // ScrollOptions, KeyDownOptions, KeyUpOptions would go here when available
        if (actionConfig instanceof DefineRegionOptions) {
            return Action.DEFINE;
        }
        if (actionConfig instanceof HighlightOptions) {
            return Action.HIGHLIGHT;
        }
        if (actionConfig instanceof DragOptions) {
            return Action.DRAG;
        }
        
        // Default for unknown types
        return Action.FIND;
    }

    /**
     * Extracts the find strategy from a find-related ActionConfig.
     * <p>
     * For PatternFindOptions, this maps the Strategy enum to the legacy
     * Find enum. For other action types, returns UNIVERSAL as default.
     * 
     * @param actionConfig The ActionConfig to analyze
     * @return The corresponding Find enum value
     */
    public Find getFindStrategy(ActionConfig actionConfig) {
        if (actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            switch (findOptions.getStrategy()) {
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
        if (actionConfig instanceof ColorFindOptions) {
            ColorFindOptions colorOptions = (ColorFindOptions) actionConfig;
            if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
                return Find.COLOR;
            }
            return Find.HISTOGRAM;
        }
        
        return Find.UNIVERSAL;
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
        return actionConfig instanceof PatternFindOptions || 
               actionConfig instanceof ColorFindOptions;
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