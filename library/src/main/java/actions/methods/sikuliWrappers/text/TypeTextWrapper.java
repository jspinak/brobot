package actions.methods.sikuliWrappers.text;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateString;
import com.brobot.multimodule.reports.Report;
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
            return new Region().type(stateString.getString()) != 0;
        else return new Region().type(stateString.getString(), actionOptions.getModifiers()) != 0;
    }

    private boolean mockType(StateString stateString, ActionOptions actionOptions) {
        Report.print(actionOptions.getModifiers());
        Report.print(stateString.getString());
        return true;
    }
}
