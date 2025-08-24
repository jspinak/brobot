package io.github.jspinak.brobot.action.internal.execution;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionType;
import static io.github.jspinak.brobot.action.ActionType.*;
import io.github.jspinak.brobot.action.basic.classify.Classify;
import io.github.jspinak.brobot.action.basic.wait.WaitVanish;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.highlight.Highlight;
import io.github.jspinak.brobot.action.basic.mouse.MouseDown;
import io.github.jspinak.brobot.action.basic.mouse.MouseUp;
import io.github.jspinak.brobot.action.basic.mouse.MoveMouse;
import io.github.jspinak.brobot.action.basic.mouse.ScrollMouseWheel;
import io.github.jspinak.brobot.action.basic.region.DefineRegion;
import io.github.jspinak.brobot.action.basic.type.KeyDown;
import io.github.jspinak.brobot.action.basic.type.KeyUp;
import io.github.jspinak.brobot.action.basic.type.TypeText;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * BasicActions, which run for 1 iteration, require 1 or no Find operations.
 * They can be used as building blocks for CompositeActions.
 */
@Component
public class BasicActionRegistry {

    /**
     * Registry mapping action types to their implementations.
     * Provides O(1) lookup for action resolution during execution.
     */
    private final Map<ActionType, ActionInterface> actions = new HashMap<>();

    /**
     * Constructs the BasicAction registry with all available action implementations.
     * <p>
     * This constructor uses dependency injection to wire all basic action types into
     * a centralized registry. Each action implementation handles a specific GUI
     * interaction primitive that can be executed independently or composed into
     * more complex operations.
     * <p>
     * The registered actions include:
     * <ul>
     * <li><strong>Input actions:</strong> Click, MouseDown, MouseUp, TypeText, KeyDown, KeyUp</li>
     * <li><strong>Movement actions:</strong> MoveMouse, ScrollMouseWheel</li>
     * <li><strong>Search actions:</strong> Find, WaitVanish</li>
     * <li><strong>Analysis actions:</strong> DefineRegion, Classify</li>
     * <li><strong>Visual actions:</strong> Highlight</li>
     * </ul>
     *
     * @param find Searches for visual patterns on screen
     * @param click Performs mouse click operations
     * @param mouseDown Presses and holds mouse button
     * @param mouseUp Releases mouse button
     * @param defineRegion Captures and defines screen regions
     * @param typeText Types text via keyboard input
     * @param moveMouse Moves mouse cursor to positions
     * @param waitVanish Waits for visual elements to disappear
     * @param highlight Draws visual indicators on screen
     * @param scrollMouseWheel Performs mouse wheel scrolling
     * @param keyDown Presses and holds keyboard keys
     * @param keyUp Releases keyboard keys
     * @param classify Performs color-based classification
     */
    public BasicActionRegistry(Find find, Click click,
                       MouseDown mouseDown, MouseUp mouseUp, DefineRegion defineRegion, TypeText typeText,
                       MoveMouse moveMouse, WaitVanish waitVanish, Highlight highlight,
                       ScrollMouseWheel scrollMouseWheel, KeyDown keyDown, KeyUp keyUp, Classify classify) {
        actions.put(FIND, find);
        actions.put(CLICK, click);
        actions.put(MOUSE_DOWN, mouseDown);
        actions.put(MOUSE_UP, mouseUp);
        actions.put(DEFINE, defineRegion);
        actions.put(TYPE, typeText);
        actions.put(MOVE, moveMouse);
        actions.put(VANISH, waitVanish);
        actions.put(HIGHLIGHT, highlight);
        actions.put(SCROLL_MOUSE_WHEEL, scrollMouseWheel);
        actions.put(KEY_DOWN, keyDown);
        actions.put(KEY_UP, keyUp);
        actions.put(CLASSIFY, classify);
    }

    /**
     * Retrieves the action implementation for the specified action type.
     * <p>
     * This factory method provides type-safe access to action implementations.
     * Returns an empty Optional if the requested action type is not registered,
     * allowing callers to handle missing actions gracefully.
     * <p>
     * This method supports the framework's ability to dynamically resolve and
     * execute actions based on ActionConfig configuration.
     *
     * @param action The action type to retrieve
     * @return Optional containing the action implementation, or empty if not found
     * @see ActionInterface
     * @see ActionType
     */
    public Optional<ActionInterface> getAction(ActionType action) {
        return Optional.ofNullable(actions.get(action));
    }

}
