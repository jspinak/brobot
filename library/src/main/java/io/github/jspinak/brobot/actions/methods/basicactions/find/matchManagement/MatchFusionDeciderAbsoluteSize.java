package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Component
public class MatchFusionDeciderAbsoluteSize implements MatchFusionDecider {

    /**
     * Evaluates the new match with respect to the current combined match region.
     * @param match1 the new match
     * @param minXDist the minimum x-distance to combine the match
     * @param minYDist the minimum y-distance to combine the match
     * @return true if the new match should be part of the current combined match region.
     */
    public boolean isSameMatchGroup(Match match1, Match match2, int minXDist, int minYDist) {
        Region region1 = new Region(match1.x() - minXDist, match1.y() - minYDist, match1.w() + 2 * minXDist, match1.h() + 2 * minYDist);
        Region region2 = new Region(match2.x() - minXDist, match2.y() - minYDist, match2.w() + 2 * minXDist, match2.h() + 2 * minYDist);
        Optional<Region> overlap = region1.getOverlappingRegion(region2);
        return overlap.isPresent();
    }
}
