package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.KeyDownWrapper;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Uses only ObjectCollection #1
 * An ObjectCollection can have multiple keys
 * ActionObjects hold special keys such as CTRL that are pressed first
 */
@Component
public class KeyDown implements ActionInterface {

    private final KeyDownWrapper keyDownWrapper;
    private final Time time;

    public KeyDown(KeyDownWrapper keyDownWrapper, Time time) {
        this.keyDownWrapper = keyDownWrapper;
        this.time = time;
    }

    // uses the first objectCollection, but this can have multiple keys
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (objectCollections == null) return;
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (StateString str : strings) {
            keyDownWrapper.press(str.getString(), actionOptions.getModifiers());
            if (strings.indexOf(str) < strings.size()-1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }

}
