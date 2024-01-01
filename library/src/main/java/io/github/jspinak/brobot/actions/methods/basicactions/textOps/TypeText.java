package io.github.jspinak.brobot.actions.methods.basicactions.textOps;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.TypeTextWrapper;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Types text to the window in focus.
 */
@Component
public class TypeText implements ActionInterface {

    private final TypeTextWrapper typeTextWrapper;
    private final Time time;

    public TypeText(TypeTextWrapper typeTextWrapper, Time time) {
        this.typeTextWrapper = typeTextWrapper;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        double defaultTypeDelay = Settings.TypeDelay;
        Settings.TypeDelay = actionOptions.getTypeDelay();
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (StateString str : strings) {
            typeTextWrapper.type(str, actionOptions);
            if (strings.indexOf(str) < strings.size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        Settings.TypeDelay = defaultTypeDelay;
    }

}
