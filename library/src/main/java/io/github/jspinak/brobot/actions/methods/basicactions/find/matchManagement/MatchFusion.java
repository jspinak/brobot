package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatchFusion {

    private final MatchFusionDecider matchFusionDecider;

    public MatchFusion(MatchFusionDecider matchFusionDecider) {
        this.matchFusionDecider = matchFusionDecider;
    }

    public Matches fuseMatchObjects(Matches matches, int minXDistance, int minYDistance) {
        Matches fusedMatches = new Matches();
        if (matches.isEmpty()) return fusedMatches;
        List<Match> originalMatches = matches.getMatchList();
        List<Integer> toCheck = new ArrayList<>();
        for (int i=0; i<originalMatches.size(); i++) toCheck.add(i);
        while (!toCheck.isEmpty()) {
            /*
            Get the next unallocated Match
             */
            Match m = new Match(originalMatches.get(toCheck.get(0)));
            toCheck.remove(0);
            /*
            Keep looping and checking until no more Match objects can be fused.
             */
            boolean fused = true;
            while (fused) {
                fused = false;
                /*
                Check all other Match objects that haven't already been fused.
                If a Match should be fused, expand the current combined Match and remove the Match from the list to check
                 */
                for (int i : new ArrayList<>(toCheck)) {
                    if (matchFusionDecider.isSameMatchGroup(m, originalMatches.get(i), minXDistance, minYDistance)) {
                        Region r = new Region(m).getUnion(new Region(originalMatches.get(i)));
                        m = new Match(r);
                        if (toCheck.contains(i)) toCheck.remove(i);
                        fused = true;
                    }
                }
            }
            fusedMatches.add(m);
        }
        return fusedMatches;
    }

}
