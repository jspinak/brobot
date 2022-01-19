package actions.methods.basicactions.textOps;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.actions.methods.sikuliWrappers.text.TypeTextWrapper;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateString;
import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Types text to the window in focus.
 */
@Component
public class TypeText implements ActionInterface {

    private TypeTextWrapper typeTextWrapper;
    private Wait wait;

    public TypeText(TypeTextWrapper typeTextWrapper, Wait wait) {
        this.typeTextWrapper = typeTextWrapper;
        this.wait = wait;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        double defaultTypeDelay = Settings.TypeDelay;
        Settings.TypeDelay = actionOptions.getTypeDelay();
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (StateString str : strings) {
            typeTextWrapper.type(str, actionOptions);
            if (strings.indexOf(str) < strings.size() - 1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        Settings.TypeDelay = defaultTypeDelay;
        return new Matches();
    }

}
