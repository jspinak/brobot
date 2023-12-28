package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchProoferRegions implements MatchProofer {

    /**
     * Check each region to see if a match falls within the region. The downside is that there may be matches
     * that span adjacent regions and do not get included because they are not contained entirely within either region.
     * @param match the match to proof
     * @param regions the search regions
     * @return true if in the search regions
     */
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        for (Region r : regions) {
            if (r.contains(match)) return true;
        }
        return false;
    }
}
