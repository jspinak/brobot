package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Defines a Region as the largest rectangle including all Matches and Locations.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineIncludingMatches implements ActionInterface {

    private final DefineHelper defineHelper;

    public DefineIncludingMatches(DefineHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        defineHelper.findMatches(matches, objectCollections);
        Region region = fitRegionToMatches(matches);
        defineHelper.adjust(region, actionOptions);
        matches.addDefinedRegion(region);
    }

    private Region fitRegionToMatches(Matches matches) {
        if (matches.isEmpty()) return new Region();
        Match firstMatch = matches.getMatchList().get(0);
        int x = firstMatch.x();
        int y = firstMatch.y();
        int x2 = firstMatch.getRegion().sikuli().getTopRight().x;
        int y2 = firstMatch.getRegion().sikuli().getBottomLeft().y;
        for (int i = 1; i<matches.getMatchList().size(); i++) {
            Match match = matches.getMatchList().get(i);
            x = Math.min(x, match.x());
            y = Math.min(y, match.y());
            x2 = Math.max(x2, match.getRegion().sikuli().getTopRight().x);
            y2 = Math.max(y2, match.getRegion().sikuli().getBottomLeft().y);
        }
        return new Region(x, y, x2-x+1, y2-y+1);
    }

}
