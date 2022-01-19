package actions.methods.basicactions.textOps;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.actions.methods.sikuliWrappers.text.KeyDownWrapper;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateString;
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
