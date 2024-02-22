package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.Highlight;
import io.github.jspinak.brobot.actions.methods.basicactions.WaitVanish;
import io.github.jspinak.brobot.actions.methods.basicactions.click.Click;
import io.github.jspinak.brobot.actions.methods.basicactions.define.DefineRegion;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification.Classify;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MouseDown;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MouseUp;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MoveMouse;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.ScrollMouseWheel;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.KeyDown;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.KeyUp;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.TypeText;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.*;

/**
 * BasicActions, which run for 1 iteration, require 1 or no Find operations.
 * They can be used as building blocks for CompositeActions.
 */
@Component
public class BasicAction {

    private final Map<ActionOptions.Action, ActionInterface> actions = new HashMap<>();

    public BasicAction(Find find, Click click,
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

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }

}
