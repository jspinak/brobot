package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class MatchFusionDeciderRelativeSize implements MatchFusionDecider {

    private final MatchFusionDeciderAbsoluteSize matchFusionDeciderAbsoluteSize;

    public MatchFusionDeciderRelativeSize(MatchFusionDeciderAbsoluteSize matchFusionDeciderAbsoluteSize) {
        this.matchFusionDeciderAbsoluteSize = matchFusionDeciderAbsoluteSize;
    }

    /**
     * Evaluates the new match with respect to the current combined match region.
     * @param match the new match
     * @param xDistAsPercentOfMatchHeight the minimum x-distance to combine the match is influenced by the match height
     * @param yDistAsPercentOfMatchHeight the minimum y-distance to combine the match is influenced by the match height
     * @return true if the new match should be part of the current combined match region.
     */
    public boolean isSameMatchGroup(Match match, Match match2, int xDistAsPercentOfMatchHeight, int yDistAsPercentOfMatchHeight) {
        int averageHeight = (match.h() + match2.h()) / 2;
        int maxHeight = Math.max(match.h(), match2.h());
        int minHeight = Math.min(match.h(), match2.h());
        int minXDist = minHeight * xDistAsPercentOfMatchHeight / 100;
        int minYDist = minHeight * yDistAsPercentOfMatchHeight / 100;
        return matchFusionDeciderAbsoluteSize.isSameMatchGroup(match, match2, minXDist, minYDist);
    }
}
