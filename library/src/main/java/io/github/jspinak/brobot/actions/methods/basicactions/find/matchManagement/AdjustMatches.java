package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Adjust Matches according to the options in ActionOptions.
 */
@Component
public class AdjustMatches {

    public void adjust(Match match, ActionOptions actionOptions) {
        match.getRegion().setX(match.x() + actionOptions.getAddX());
        match.getRegion().setY(match.y() + actionOptions.getAddY());
        if (actionOptions.getAbsoluteW() > 0) match.getRegion().setW(actionOptions.getAbsoluteW());
        else match.getRegion().setW(match.w() + actionOptions.getAddW());
        if (actionOptions.getAbsoluteH() > 0) match.getRegion().setH(actionOptions.getAbsoluteH());
        else match.getRegion().setH(match.h() + actionOptions.getAddH());
    }

    public void adjustAll(Matches matches, ActionOptions actionOptions) {
        matches.getMatchList().forEach(match -> adjust(match, actionOptions));
    }

    public void adjustAll(ObjectCollection objectCollection, ActionOptions actionOptions) {
        objectCollection.getMatches().forEach(matches -> adjustAll(matches, actionOptions));
    }
}
