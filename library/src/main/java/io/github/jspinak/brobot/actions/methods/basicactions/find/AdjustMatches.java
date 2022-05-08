package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

/**
 * Adjust Matches according to the options in ActionOptions.
 */
@Component
public class AdjustMatches {

    public void adjust(Match match, ActionOptions actionOptions) {
        match.x += actionOptions.getAddX();
        match.y += actionOptions.getAddY();
        if (actionOptions.getAbsoluteW() > 0) match.w = actionOptions.getAbsoluteW();
        else match.w += actionOptions.getAddW();
        if (actionOptions.getAbsoluteH() > 0) match.h = actionOptions.getAbsoluteH();
        else match.h += actionOptions.getAddH();
    }

    public void adjustAll(Matches matches, ActionOptions actionOptions) {
        matches.getMatches().forEach(match -> adjust(match, actionOptions));
    }

    public void adjustAll(ObjectCollection objectCollection, ActionOptions actionOptions) {
        objectCollection.getMatches().forEach(matches -> adjustAll(matches, actionOptions));
    }
}
