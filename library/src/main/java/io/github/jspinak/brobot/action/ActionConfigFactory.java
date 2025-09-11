package io.github.jspinak.brobot.action;

import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.type.KeyDownOptions;
import io.github.jspinak.brobot.action.basic.type.KeyUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

/**
 * Factory for creating ActionConfig instances based on action type.
 *
 * <p>This factory centralizes the creation logic for all ActionConfig subclasses, providing a
 * single point of configuration and reducing coupling between actions and their configuration
 * creation.
 */
@Component
public class ActionConfigFactory {

    // Map of action types to their factory functions
    private final Map<ActionInterface.Type, Function<Map<String, Object>, ActionConfig>> factories;

    public ActionConfigFactory() {
        // Initialize the map of action types to their factory functions
        this.factories =
                Map.ofEntries(
                        Map.entry(ActionInterface.Type.CLICK, this::createClickOptions),
                        Map.entry(ActionInterface.Type.DRAG, this::createDragOptions),
                        Map.entry(ActionInterface.Type.FIND, this::createPatternFindOptions),
                        Map.entry(ActionInterface.Type.TYPE, this::createTypeOptions),
                        Map.entry(ActionInterface.Type.MOVE, this::createMouseMoveOptions),
                        Map.entry(ActionInterface.Type.VANISH, this::createVanishOptions),
                        Map.entry(ActionInterface.Type.HIGHLIGHT, this::createHighlightOptions),
                        Map.entry(
                                ActionInterface.Type.SCROLL_MOUSE_WHEEL, this::createScrollOptions),
                        Map.entry(ActionInterface.Type.MOUSE_DOWN, this::createMouseDownOptions),
                        Map.entry(ActionInterface.Type.MOUSE_UP, this::createMouseUpOptions),
                        Map.entry(ActionInterface.Type.KEY_DOWN, this::createKeyDownOptions),
                        Map.entry(ActionInterface.Type.KEY_UP, this::createKeyUpOptions),
                        Map.entry(ActionInterface.Type.CLASSIFY, this::createColorClassifyOptions),
                        Map.entry(
                                ActionInterface.Type.CLICK_UNTIL,
                                this::createClickUntilOptions), // Deprecated but supported for
                        // backward compatibility
                        Map.entry(ActionInterface.Type.DEFINE, this::createDefineRegionOptions));
    }

    /**
     * Creates an ActionConfig instance for the specified action type with default settings.
     *
     * @param actionType The type of action for which to create a config
     * @return A new ActionConfig instance with default settings
     * @throws IllegalArgumentException if the action type is not supported
     */
    public ActionConfig create(ActionInterface.Type actionType) {
        return create(actionType, null);
    }

    /**
     * Creates an ActionConfig instance for the specified action type, optionally applying
     * user-provided overrides.
     *
     * @param actionType The type of action for which to create a config
     * @param userOverrides Optional map of property names to values for overriding defaults
     * @return A new ActionConfig instance with applied settings
     * @throws IllegalArgumentException if the action type is not supported
     */
    public ActionConfig create(ActionInterface.Type actionType, Map<String, Object> userOverrides) {
        if (actionType == null) {
            throw new IllegalArgumentException("Action type cannot be null");
        }

        Function<Map<String, Object>, ActionConfig> factory = factories.get(actionType);
        if (factory == null) {
            throw new IllegalArgumentException(
                    "No ActionConfig factory available for type: " + actionType);
        }

        return factory.apply(userOverrides);
    }

    /**
     * Creates an ActionConfig instance by copying settings from an existing config and optionally
     * applying overrides.
     *
     * @param existingConfig The config to copy settings from
     * @param userOverrides Optional map of property names to values for overriding
     * @return A new ActionConfig instance with copied and overridden settings
     */
    public ActionConfig createFrom(ActionConfig existingConfig, Map<String, Object> userOverrides) {
        // Determine the action type from the config class
        ActionInterface.Type actionType = getActionTypeFromConfig(existingConfig);

        // Create a new config with overrides, using the existing config as base
        // This is a simplified implementation - in practice, you might want to
        // deep copy the existing config values
        return create(actionType, userOverrides);
    }

    private ActionInterface.Type getActionTypeFromConfig(ActionConfig config) {
        // Map config classes to their action types
        if (config instanceof ClickOptions) return ActionInterface.Type.CLICK;
        if (config instanceof DragOptions) return ActionInterface.Type.DRAG;
        if (config instanceof PatternFindOptions) return ActionInterface.Type.FIND;
        if (config instanceof TypeOptions) return ActionInterface.Type.TYPE;
        if (config instanceof MouseMoveOptions) return ActionInterface.Type.MOVE;
        if (config instanceof VanishOptions) return ActionInterface.Type.VANISH;
        if (config instanceof HighlightOptions) return ActionInterface.Type.HIGHLIGHT;
        if (config instanceof ScrollOptions) return ActionInterface.Type.SCROLL_MOUSE_WHEEL;
        if (config instanceof MouseDownOptions) return ActionInterface.Type.MOUSE_DOWN;
        if (config instanceof MouseUpOptions) return ActionInterface.Type.MOUSE_UP;
        if (config instanceof KeyDownOptions) return ActionInterface.Type.KEY_DOWN;
        if (config instanceof KeyUpOptions) return ActionInterface.Type.KEY_UP;
        if (config instanceof ColorFindOptions) {
            ColorFindOptions colorOptions = (ColorFindOptions) config;
            if (colorOptions.getColor() == ColorFindOptions.Color.CLASSIFICATION) {
                return ActionInterface.Type.CLASSIFY;
            }
            return ActionInterface.Type.FIND; // Other color strategies map to FIND
        }
        // ClickUntilOptions removed - use ClickOptions with success criteria
        if (config instanceof DefineRegionOptions) return ActionInterface.Type.DEFINE;

        throw new IllegalArgumentException("Unknown config type: " + config.getClass().getName());
    }

    // Factory methods for each action type

    private ClickOptions createClickOptions(Map<String, Object> overrides) {
        ClickOptions.Builder builder = new ClickOptions.Builder();
        applyCommonOverrides(builder, overrides);

        if (overrides != null) {
            if (overrides.containsKey("numberOfClicks")) {
                builder.setNumberOfClicks((Integer) overrides.get("numberOfClicks"));
            }
            if (overrides.containsKey("mousePressOptions")) {
                builder.setPressOptions((MousePressOptions) overrides.get("mousePressOptions"));
            }
        }

        return builder.build();
    }

    private DragOptions createDragOptions(Map<String, Object> overrides) {
        DragOptions.Builder builder = new DragOptions.Builder();
        applyCommonOverrides(builder, overrides);

        if (overrides != null) {
            if (overrides.containsKey("delayBetweenMouseDownAndMove")) {
                builder.setDelayBetweenMouseDownAndMove(
                        (Double) overrides.get("delayBetweenMouseDownAndMove"));
            }
            if (overrides.containsKey("delayAfterDrag")) {
                builder.setDelayAfterDrag((Double) overrides.get("delayAfterDrag"));
            }
        }

        return builder.build();
    }

    private PatternFindOptions createPatternFindOptions(Map<String, Object> overrides) {
        PatternFindOptions.Builder builder = new PatternFindOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private TypeOptions createTypeOptions(Map<String, Object> overrides) {
        TypeOptions.Builder builder = new TypeOptions.Builder();
        applyCommonOverrides(builder, overrides);

        if (overrides != null) {
            if (overrides.containsKey("typeDelay")) {
                builder.setTypeDelay((Double) overrides.get("typeDelay"));
            }
            if (overrides.containsKey("modifiers")) {
                builder.setModifiers((String) overrides.get("modifiers"));
            }
        }

        return builder.build();
    }

    private MouseMoveOptions createMouseMoveOptions(Map<String, Object> overrides) {
        MouseMoveOptions.Builder builder = new MouseMoveOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private VanishOptions createVanishOptions(Map<String, Object> overrides) {
        VanishOptions.Builder builder = new VanishOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private HighlightOptions createHighlightOptions(Map<String, Object> overrides) {
        HighlightOptions.Builder builder = new HighlightOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private ScrollOptions createScrollOptions(Map<String, Object> overrides) {
        ScrollOptions.Builder builder = new ScrollOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private MouseDownOptions createMouseDownOptions(Map<String, Object> overrides) {
        MouseDownOptions.Builder builder = new MouseDownOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private MouseUpOptions createMouseUpOptions(Map<String, Object> overrides) {
        MouseUpOptions.Builder builder = new MouseUpOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private KeyDownOptions createKeyDownOptions(Map<String, Object> overrides) {
        KeyDownOptions.Builder builder = new KeyDownOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private KeyUpOptions createKeyUpOptions(Map<String, Object> overrides) {
        KeyUpOptions.Builder builder = new KeyUpOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    /**
     * Creates ClickOptions for CLICK_UNTIL action type.
     *
     * @deprecated Use ClickOptions with success criteria instead. See
     *     action/composite/repeat/CLICK_UNTIL_MIGRATION.md for migration guide.
     */
    @Deprecated
    private ClickOptions createClickUntilOptions(Map<String, Object> overrides) {
        ClickOptions.Builder builder = new ClickOptions.Builder();
        applyCommonOverrides(builder, overrides);

        // Set a default success criteria that mimics old CLICK_UNTIL behavior
        if (overrides == null || !overrides.containsKey("successCriteria")) {
            // Default: stop when target vanishes (common CLICK_UNTIL use case)
            builder.setSuccessCriteria(result -> result.getMatchList().isEmpty());
        }

        // Set a reasonable max clicks to prevent infinite loops
        if (overrides == null || !overrides.containsKey("numberOfClicks")) {
            builder.setNumberOfClicks(10); // Default safety limit
        }

        return builder.build();
    }

    private ColorFindOptions createColorClassifyOptions(Map<String, Object> overrides) {
        ColorFindOptions.Builder builder = new ColorFindOptions.Builder();
        builder.setColorStrategy(ColorFindOptions.Color.CLASSIFICATION);
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    private DefineRegionOptions createDefineRegionOptions(Map<String, Object> overrides) {
        DefineRegionOptions.Builder builder = new DefineRegionOptions.Builder();
        applyCommonOverrides(builder, overrides);
        return builder.build();
    }

    /** Applies common ActionConfig overrides to any builder. */
    private void applyCommonOverrides(
            ActionConfig.Builder<?> builder, Map<String, Object> overrides) {
        if (overrides == null) return;

        if (overrides.containsKey("pauseBeforeBegin")) {
            builder.setPauseBeforeBegin((Double) overrides.get("pauseBeforeBegin"));
        }
        if (overrides.containsKey("pauseAfterEnd")) {
            builder.setPauseAfterEnd((Double) overrides.get("pauseAfterEnd"));
        }
        if (overrides.containsKey("illustrate")) {
            builder.setIllustrate((ActionConfig.Illustrate) overrides.get("illustrate"));
        }
        if (overrides.containsKey("logType")) {
            builder.setLogType((LogEventType) overrides.get("logType"));
        }
        if (overrides.containsKey("successCriteria")) {
            @SuppressWarnings("unchecked")
            var criteria =
                    (java.util.function.Predicate<ActionResult>) overrides.get("successCriteria");
            builder.setSuccessCriteria(criteria);
        }
    }
}
