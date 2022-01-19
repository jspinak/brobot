package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.Highlight;
import io.github.jspinak.brobot.actions.methods.basicactions.WaitVanish;
import io.github.jspinak.brobot.actions.methods.basicactions.click.Click;
import io.github.jspinak.brobot.actions.methods.basicactions.define.DefineRegion;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MouseDown;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MouseUp;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.MoveMouse;
import io.github.jspinak.brobot.actions.methods.basicactions.mouse.ScrollMouseWheel;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.GetText;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.KeyDown;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.KeyUp;
import io.github.jspinak.brobot.actions.methods.basicactions.textOps.TypeText;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * BasicActions, which run for 1 iteration, require <=1 Find operation.
 * They can be used as building blocks for CompositeActions.
 */
@Component
public class BasicAction {

    private Map<ActionOptions.Action, ActionInterface> actions = new HashMap<>();

    public BasicAction(Find find, Click click,
                       MouseDown mouseDown, MouseUp mouseUp, DefineRegion defineRegion, TypeText typeText,
                       MoveMouse moveMouse, WaitVanish waitVanish, GetText getText, Highlight highlight,
                       ScrollMouseWheel scrollMouseWheel, KeyDown keyDown, KeyUp keyUp) {
        actions.put(ActionOptions.Action.FIND, find);
        actions.put(ActionOptions.Action.CLICK, click);
        actions.put(ActionOptions.Action.MOUSE_DOWN, mouseDown);
        actions.put(ActionOptions.Action.MOUSE_UP, mouseUp);
        actions.put(ActionOptions.Action.DEFINE, defineRegion);
        actions.put(ActionOptions.Action.TYPE, typeText);
        actions.put(ActionOptions.Action.MOVE, moveMouse);
        actions.put(ActionOptions.Action.VANISH, waitVanish);
        actions.put(ActionOptions.Action.GET_TEXT, getText);
        actions.put(ActionOptions.Action.HIGHLIGHT, highlight);
        actions.put(ActionOptions.Action.SCROLL_MOUSE_WHEEL, scrollMouseWheel);
        //actions.put(KEY_DOWN, keyDown);
        //actions.put(KEY_UP, keyUp);
    }

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }

}
