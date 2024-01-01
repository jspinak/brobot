package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.*;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.MatchFusionMethod.NONE;

/**
 * Match objects do not retain data on Pattern, StateObject, etc. after being fused.
 * If this data is needed, you should fuse matches after the action with a direct call to this method.
 *   This way, you have the original Match objects in the Matches variable, and can produce an additional
 *   variable holding a list of the fused Match objects.
 */
@Component
public class MatchFusion {
    private final Map<ActionOptions.MatchFusionMethod, MatchFusionDecider> fusionMethods = new HashMap<>();

    public MatchFusion(MatchFusionDeciderWords matchFusionDeciderWords) {
        fusionMethods.put(ActionOptions.MatchFusionMethod.WORDS, matchFusionDeciderWords);
    }

    public void setFusedMatches(Matches matches) {
        if (matches.getActionOptions().getFusionMethod() == NONE) return;
        matches.setMatchList(getFusedMatchObjects(matches));
    }

    /**
     * Match objects are fused, and the image and text are captured from the scene(s).
     * @param matches holds the match objects to fuse, the scene(s), and the action configuration.
     * @return the new Match list.
     */
    public List<Match> getFusedMatchObjects(Matches matches) {
        MatchFusionDecider decider = fusionMethods.get(matches.getActionOptions().getFusionMethod());
        int maxXDistance = matches.getActionOptions().getMaxFusionDistanceX();
        int maxYDistance = matches.getActionOptions().getMaxFusionDistanceY();
        List<Match> fusedMatches = new ArrayList<>();
        if (matches.isEmpty()) return fusedMatches;
        List<Match> originalMatches = matches.getMatchList();
        List<Integer> toCheck = new ArrayList<>();
        for (int i=0; i<originalMatches.size(); i++) toCheck.add(i);
        while (!toCheck.isEmpty()) {
            /*
            Get the next unallocated Match
             */
            int nextUnallocatedIndex = toCheck.get(0);
            Match m = new Match(originalMatches.get(nextUnallocatedIndex));
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
                    if (decider.isSameMatchGroup(m, originalMatches.get(i), maxXDistance, maxYDistance)) {
                        Region r = new Region(m).getUnion(new Region(originalMatches.get(i)));
                        m = new Match(r);
                        if (toCheck.contains(i)) toCheck.remove(Integer.valueOf(i));
                        fused = true;
                    }
                }
            }
            fusedMatches.add(m);
        }
        return fusedMatches;
    }

}
