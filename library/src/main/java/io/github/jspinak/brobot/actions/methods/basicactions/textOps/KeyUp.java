package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.KeyUpWrapper;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import io.github.jspinak.brobot.database.state.stateObject.otherStateObjects.StateString;
import org.springframework.stereotype.Component;

/**
 * Uses only ObjectCollection #1
 * An ObjectCollection can have multiple keys
 * ActionObjects hold special keys such as CTRL that are released last
 */
@Component
public class KeyUp implements ActionInterface {

    private KeyUpWrapper keyUpWrapper;

    public KeyUp(KeyUpWrapper keyUpWrapper) {
        this.keyUpWrapper = keyUpWrapper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        if (objectCollections == null) keyUpWrapper.release(); // releases all keys
        else for (StateString stateString : objectCollections[0].getStateStrings()) {
            keyUpWrapper.release(stateString.getString());
        }
        keyUpWrapper.release(actionOptions.getModifiers());
        return matches;
    }

}
