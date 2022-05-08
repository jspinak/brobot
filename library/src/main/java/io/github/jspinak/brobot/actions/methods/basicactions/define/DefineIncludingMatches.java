package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

/**
 * Defines a Region as the largest rectangle including all Matches and Locations.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineIncludingMatches implements ActionInterface {

    private DefineHelper defineHelper;

    public DefineIncludingMatches(DefineHelper defineHelper) {
        this.defineHelper = defineHelper;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = defineHelper.findMatches(actionOptions, objectCollections);
        Region region = fitRegionToMatches(matches);
        defineHelper.adjust(region, actionOptions);
        matches.addDefinedRegion(region);
        return matches;
    }

    private Region fitRegionToMatches(Matches matches) {
        if (matches.isEmpty()) return new Region();
        Match firstMatch = matches.getMatches().get(0);
        int x = firstMatch.x;
        int y = firstMatch.y;
        int x2 = firstMatch.getTopRight().x;
        int y2 = firstMatch.getBottomLeft().y;
        for (int i=1; i<matches.getMatches().size(); i++) {
            Match match = matches.getMatches().get(i);
            x = Math.min(x, match.x);
            y = Math.min(y, match.y);
            x2 = Math.max(x2, match.getTopRight().x);
            y2 = Math.max(y2, match.getBottomLeft().y);
        }
        return new Region(x, y, x2-x+1, y2-y+1);
    }

}
