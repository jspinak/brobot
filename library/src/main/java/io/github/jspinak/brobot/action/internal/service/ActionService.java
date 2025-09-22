package io.github.jspinak.brobot.action.internal.service;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.FindStrategyRegistry;
import io.github.jspinak.brobot.action.composite.drag.Drag;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;

/**
 * Central service for resolving action implementations based on configuration.
 *
 * <p>ActionService acts as a factory that examines {@link ActionConfig} and returns the appropriate
 * {@link ActionInterface} implementation. It intelligently routes between basic and composite
 * actions based on the complexity of the requested operation.
 *
 * <p><strong>Resolution logic:</strong>
 *
 * <ul>
 *   <li>Multiple Find operations → Composite FIND action
 *   <li>Single Find operation → Basic FIND action
 *   <li>Other actions → Checks basic registry first, then composite
 * </ul>
 *
 * <p>This service encapsulates the complexity of action selection, allowing the framework to
 * transparently handle both simple and complex operations through a uniform interface.
 *
 * @see BasicActionRegistry
 * @see ActionInterface
 */
@Component
public class ActionService {

    private final BasicActionRegistry basicAction;
    private final FindStrategyRegistry findFunctions;
    private final Drag dragAction;

    /**
     * Constructs the ActionService with required action registries.
     *
     * @param basicAction Registry of atomic GUI operations
     * @param findFunctions Service for custom find implementations
     */
    public ActionService(
            BasicActionRegistry basicAction, FindStrategyRegistry findFunctions, Drag dragAction) {
        this.basicAction = basicAction;
        this.findFunctions = findFunctions;
        this.dragAction = dragAction;
    }

    /**
     * Registers a custom Find implementation for one-time use.
     *
     * <p>Allows runtime registration of specialized find logic without modifying the core
     * framework. The custom implementation will be available for actions configured with custom
     * find strategies.
     *
     * <p>Custom finds are useful for:
     *
     * <ul>
     *   <li>Application-specific pattern matching
     *   <li>Complex multi-stage searches
     *   <li>Integration with external vision systems
     * </ul>
     *
     * @param customFind BiConsumer that accepts ActionResult to populate and ObjectCollections to
     *     search within
     */
    public void setCustomFind(BiConsumer<ActionResult, List<ObjectCollection>> customFind) {
        findFunctions.addCustomFind(customFind);
    }

    /**
     * Resolves the appropriate action implementation for the given ActionConfig.
     *
     * <p>Since ActionConfig is type-specific (e.g., ClickOptions, FindOptions), this method
     * determines the action type based on the config class name and returns the corresponding
     * implementation.
     *
     * @param actionConfig Configuration specifying the desired action
     * @return Optional containing the action implementation, or empty if not found
     */
    public Optional<ActionInterface> getAction(ActionConfig actionConfig) {
        // Map config class names to action types
        String configClassName = actionConfig.getClass().getSimpleName();

        // Pattern-based find operations
        if (configClassName.contains("FindOptions")
                || configClassName.contains("PatternFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Color-based find operations
        else if (configClassName.contains("ColorFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Text-based find operations
        else if (configClassName.contains("TextFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Histogram-based find operations
        else if (configClassName.contains("HistogramFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Motion-based find operations
        else if (configClassName.contains("MotionFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Pixel-based find operations
        else if (configClassName.contains("DynamicPixelsFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        } else if (configClassName.contains("FixedPixelsFindOptions")) {
            return basicAction.getAction(ActionType.FIND);
        }
        // Click operations
        else if (configClassName.contains("ClickOptions")) {
            return basicAction.getAction(ActionType.CLICK);
        }
        // Type operations
        else if (configClassName.contains("TypeOptions")) {
            return basicAction.getAction(ActionType.TYPE);
        }
        // Mouse operations
        else if (configClassName.contains("MouseMoveOptions")) {
            return basicAction.getAction(ActionType.MOVE);
        } else if (configClassName.contains("MouseDownOptions")) {
            return basicAction.getAction(ActionType.MOUSE_DOWN);
        } else if (configClassName.contains("MouseUpOptions")) {
            return basicAction.getAction(ActionType.MOUSE_UP);
        } else if (configClassName.contains("ScrollOptions")) {
            return basicAction.getAction(ActionType.SCROLL_MOUSE_WHEEL);
        }
        // Visual operations
        else if (configClassName.contains("HighlightOptions")) {
            return basicAction.getAction(ActionType.HIGHLIGHT);
        } else if (configClassName.contains("DefineOptions")) {
            return basicAction.getAction(ActionType.DEFINE);
        }
        // Wait operations
        else if (configClassName.contains("VanishOptions")) {
            return basicAction.getAction(ActionType.VANISH);
        }
        // Key operations
        else if (configClassName.contains("KeyDownOptions")) {
            return basicAction.getAction(ActionType.KEY_DOWN);
        } else if (configClassName.contains("KeyUpOptions")) {
            return basicAction.getAction(ActionType.KEY_UP);
        }
        // Composite operations
        else if (configClassName.contains("DragOptions")) {
            return Optional.of(dragAction);
        }
        // ClickUntilOptions no longer exists - handled by ClickOptions with success criteria
        else if (configClassName.contains("ClassifyOptions")) {
            return basicAction.getAction(ActionType.CLASSIFY);
        }

        return Optional.empty();
    }
}
