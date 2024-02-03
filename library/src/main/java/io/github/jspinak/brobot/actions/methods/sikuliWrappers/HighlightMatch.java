package io.github.jspinak.brobot.actions.methods.sikuliWrappers;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import io.github.jspinak.brobot.reports.Report;
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
        else match.sikuli().highlightOn(actionOptions.getHighlightColor());
    }

    public void turnOff(Match match) {
        if (!BrobotSettings.mock || !BrobotSettings.screenshots.isEmpty()) match.sikuli().highlightOff();
    }

    // matches don't highlight, only regions (this seems to be a sikuli bug).
    public boolean highlight(Match match, StateObjectData stateObject, ActionOptions actionOptions) {
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            return Report.print(match, stateObject, actionOptions);
        match.sikuli().highlight(1);
        Region highlightReg = match.getRegion().sikuli();
        if (match.w() == 0) highlightReg.w = 10;
        if (match.h() == 0) highlightReg.h = 10;
        Report.println("in HighlightRegion: "+highlightReg);
        highlightReg.highlight(actionOptions.getHighlightSeconds(), actionOptions.getHighlightColor());
        return true;
    }

}
