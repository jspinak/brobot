package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.KeyDownWrapper;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Uses only ObjectCollection #1
 * An ObjectCollection can have multiple keys
 * ActionObjects hold special keys such as CTRL that are pressed first
 */
@Component
public class KeyDown implements ActionInterface {

    private KeyDownWrapper keyDownWrapper;
    private Wait wait;

    public KeyDown(KeyDownWrapper keyDownWrapper, Wait wait) {
        this.keyDownWrapper = keyDownWrapper;
        this.wait = wait;
    }

    // uses the first objectCollection, but this can have multiple keys
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        if (objectCollections == null) return new Matches();
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (StateString str : strings) {
            keyDownWrapper.press(str.getString(), actionOptions.getModifiers());
            if (strings.indexOf(str) < strings.size()-1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        return matches;
    }

}
