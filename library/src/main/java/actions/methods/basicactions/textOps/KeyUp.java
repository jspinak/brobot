package actions.methods.basicactions.textOps;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.text.KeyUpWrapper;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateString;
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
