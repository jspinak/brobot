package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.KeyUpWrapper;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.springframework.stereotype.Component;

/**
 * Uses only ObjectCollection #1
 * An ObjectCollection can have multiple keys
 * ActionObjects hold special keys such as CTRL that are released last
 */
@Component
public class KeyUp implements ActionInterface {

    private final KeyUpWrapper keyUpWrapper;

    public KeyUp(KeyUpWrapper keyUpWrapper) {
        this.keyUpWrapper = keyUpWrapper;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (nothingToRelease(actionOptions, objectCollections)) keyUpWrapper.release(); // releases all keys
        else {
            for (StateString stateString : objectCollections[0].getStateStrings()) {
                keyUpWrapper.release(stateString.getString());
            }
            if (!actionOptions.getModifiers().isEmpty()) keyUpWrapper.release(actionOptions.getModifiers());
        }
    }

    private boolean nothingToRelease(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections == null) return true;
        return objectCollections[0].getStateStrings().isEmpty() &&
                actionOptions.getModifiers().isEmpty();
    }

}
