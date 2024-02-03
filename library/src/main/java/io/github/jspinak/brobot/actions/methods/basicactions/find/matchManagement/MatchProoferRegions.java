package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchProoferRegions implements MatchProofer {

    private final SelectRegions selectRegions;

    public MatchProoferRegions(SelectRegions selectRegions) {
        this.selectRegions = selectRegions;
    }

    public boolean isInSearchRegions(Match match, ActionOptions actionOptions, Pattern pattern) {
        // these are unique regions so there won't be any duplicate matches
        List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
        return isInSearchRegions(match, regions);
    }

    /**
     * Check each region to see if a match falls within the region. The downside is that there may be matches
     * that span adjacent regions and do not get included because they are not contained entirely within either region.
     * @param match the match to proof
     * @param regions the search regions
     * @return true if in the search regions
     */
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        for (Region r : regions) {
            if (r.contains(match.getRegion())) return true;
        }
        return false;
    }
}
