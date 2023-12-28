package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;

public interface MatchFusionDecider {

    boolean isSameMatchGroup(Match match, Match match2, int minXDist, int minYDist);
}
