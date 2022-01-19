package actions.methods.sikuliWrappers;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.state.stateObject.StateObject;
import com.brobot.multimodule.reports.Report;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for Highlight, performs real or mock highlights.
 */
@Component
public class HighlightMatch {

    public void turnOn(Match match, StateObject stateObject, ActionOptions actionOptions) {
        if (BrobotSettings.mock) Report.print(match, stateObject, actionOptions);
        else match.highlightOn(actionOptions.getHighlightColor());
    }

    public void turnOff(Match match) {
        if (!BrobotSettings.mock) match.highlightOff();
    }

    // matches don't highlight, only regions (this seems to be a sikuli bug).
    public boolean highlight(Match match, StateObject stateObject, ActionOptions actionOptions) {
        if (BrobotSettings.mock) return Report.print(match, stateObject, actionOptions);
        match.highlight(1);
        Region highlightReg = new Region(match.x,match.y,match.w,match.h);
        if (match.w == 0) highlightReg.w = 10;
        if (match.h == 0) highlightReg.h = 10;
        Report.println("in HighlightRegion: "+highlightReg);
        highlightReg.highlight(actionOptions.getHighlightSeconds(), actionOptions.getHighlightColor());
        return true;
    }

}
