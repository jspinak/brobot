package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateString;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for TypeText, works with real or mocked actions.
 * Types a String to the focused window.
 */
@Component
public class TypeTextWrapper {

    public boolean type(StateString stateString, ActionOptions actionOptions) {
        if (BrobotSettings.mock) return mockType(stateString, actionOptions);
        if (actionOptions.getModifiers().equals(""))
            return new Region().sikuli().type(stateString.getString()) != 0;
        else return new Region().sikuli().type(stateString.getString(), actionOptions.getModifiers()) != 0;
    }

    private boolean mockType(StateString stateString, ActionOptions actionOptions) {
        Report.print(actionOptions.getModifiers());
        Report.print(stateString.getString());
        return true;
    }
}
