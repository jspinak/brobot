package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;

import java.util.List;

public interface MatchProofer {

    /**
     * To check if a match is within the search regions, there are multiple methods that are satisfactory but with faults:
     * 1. Check each region to see if a match falls within the region. The downside is that you may have matches
     *    that span adjacent regions and do not get included because they are not contained entirely within either region.
     * 2. Check if each of the 4 edges are contained within search regions. The downside is that there may be spaces
     *    between search regions and some matches may be incorrectly accepted.
     * @param match the match to proof
     * @param regions the search regions
     * @return true if in the search regions
     */
    boolean isInSearchRegions(Match match, List<Region> regions);
    boolean isInSearchRegions(Match match, ActionOptions actionOptions, Pattern pattern);
}
