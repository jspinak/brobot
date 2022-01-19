package actions.actionExecution;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.Highlight;
import com.brobot.multimodule.actions.methods.basicactions.WaitVanish;
import com.brobot.multimodule.actions.methods.basicactions.click.Click;
import com.brobot.multimodule.actions.methods.basicactions.define.DefineRegion;
import com.brobot.multimodule.actions.methods.basicactions.find.Find;
import com.brobot.multimodule.actions.methods.basicactions.mouse.MouseDown;
import com.brobot.multimodule.actions.methods.basicactions.mouse.MouseUp;
import com.brobot.multimodule.actions.methods.basicactions.mouse.MoveMouse;
import com.brobot.multimodule.actions.methods.basicactions.mouse.ScrollMouseWheel;
import com.brobot.multimodule.actions.methods.basicactions.textOps.GetText;
import com.brobot.multimodule.actions.methods.basicactions.textOps.KeyDown;
import com.brobot.multimodule.actions.methods.basicactions.textOps.KeyUp;
import com.brobot.multimodule.actions.methods.basicactions.textOps.TypeText;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.Action.*;

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
        actions.put(FIND, find);
        actions.put(CLICK, click);
        actions.put(MOUSE_DOWN, mouseDown);
        actions.put(MOUSE_UP, mouseUp);
        actions.put(DEFINE, defineRegion);
        actions.put(TYPE, typeText);
        actions.put(MOVE, moveMouse);
        actions.put(VANISH, waitVanish);
        actions.put(GET_TEXT, getText);
        actions.put(HIGHLIGHT, highlight);
        actions.put(SCROLL_MOUSE_WHEEL, scrollMouseWheel);
        //actions.put(KEY_DOWN, keyDown);
        //actions.put(KEY_UP, keyUp);
    }

    public Optional<ActionInterface> getAction(ActionOptions.Action action) {
        return Optional.ofNullable(actions.get(action));
    }

}
