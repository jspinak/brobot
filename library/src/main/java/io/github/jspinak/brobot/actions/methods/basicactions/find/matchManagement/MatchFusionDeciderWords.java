package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class MatchFusionDeciderWords implements MatchFusionDecider {

    /**
     * Evaluates the new match with respect to the current combined match region.
     * @param match the new match
     * @param minXDist the minimum x-distance to combine the match
     * @param minYDist the minimum y-distance to combine the match
     * @return true if the new match should be part of the current combined match region.
     */
    public boolean isSameMatchGroup(Match match, Match match2, int minXDist, int minYDist) {
        int xDistEndBegin = Math.abs(match.x + match.w - match2.x); // x-distance between end of the current region and the start of the new word match
        int xDist = Math.abs(match.x - match2.x);
        int yDist = Math.abs(match.y - match2.y); // y-distance between the beginning of both region and match
        boolean isOkDistBetweenEndOfMatch1AndBeginOfMatch2 = xDistEndBegin <= minXDist || xDist <= minXDist;
        boolean isOkDistBetweenBeginOfMatch1AndBeginOfMatch2 = yDist <= minYDist;
        Region r = new Region(match);
        Region r2 = new Region(match2);
        Location r2xy = new Location(r2,0,0);
        boolean overlapsMatchX = r.containsX(r2xy);
        return isOkDistBetweenBeginOfMatch1AndBeginOfMatch2 &&
                (isOkDistBetweenEndOfMatch1AndBeginOfMatch2 || overlapsMatchX);
    }
}
