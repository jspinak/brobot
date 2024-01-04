package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Match;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class MatchProoferEdges implements MatchProofer {

    private final SelectRegions selectRegions;

    public MatchProoferEdges(SelectRegions selectRegions) {
        this.selectRegions = selectRegions;
    }

    public boolean isInSearchRegions(Match match, ActionOptions actionOptions, Pattern pattern) {
        // these are unique regions so there won't be any duplicate matches
        List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
        return isInSearchRegions(match, regions);
    }

    /**
     * Check if each of the 4 edges are contained within search regions. The downside is that there may be spaces
     * between search regions and some matches may be incorrectly accepted.
     * @param match the match to proof
     * @param regions the search regions
     * @return true if the match is accepted
     */
    public boolean isInSearchRegions(Match match, List<Region> regions) {
        boolean topLeft = false, topRight = false, bottomLeft = false, bottomRight = false;
        Location tL = new Location(match.x, match.y);
        Location tR = new Location(match.x+match.w, match.y);
        Location bL = new Location(match.x, match.y + match.h);
        Location bR = new Location(match.x+match.w, match.y+match.h);
        for (Region region : regions) {
            if (region.contains(tL)) topLeft = true;
            if (region.contains(tR)) topRight = true;
            if (region.contains(bL)) bottomLeft = true;
            if (region.contains(bR)) bottomRight = true;
            if (topLeft && topRight && bottomLeft && bottomRight) return true;
        }
        return false;
    }
}
