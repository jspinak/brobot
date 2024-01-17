package io.github.jspinak.brobot.actions.methods.sikuliWrappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.sikuli.script.Region;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for Highlight, performs real or mock highlights.
 */
@Component
public class HighlightMatch {

    public void turnOn(Match match, StateObjectData stateObject, ActionOptions actionOptions) {
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            Report.print(match, stateObject, actionOptions);
        else match.highlightOn(actionOptions.getHighlightColor());
    }

    public void turnOff(Match match) {
        if (!BrobotSettings.mock || !BrobotSettings.screenshots.isEmpty()) match.highlightOff();
    }

    // matches don't highlight, only regions (this seems to be a sikuli bug).
    public boolean highlight(Match match, StateObjectData stateObject, ActionOptions actionOptions) {
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            return Report.print(match, stateObject, actionOptions);
        match.highlight(1);
        Region highlightReg = new Region(match.x,match.y,match.w,match.h);
        if (match.w == 0) highlightReg.w = 10;
        if (match.h == 0) highlightReg.h = 10;
        Report.println("in HighlightRegion: "+highlightReg);
        highlightReg.highlight(actionOptions.getHighlightSeconds(), actionOptions.getHighlightColor());
        return true;
    }

}
