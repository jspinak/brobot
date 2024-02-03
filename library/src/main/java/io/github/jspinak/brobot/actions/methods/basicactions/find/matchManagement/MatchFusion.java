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

    public MatchFusion(MatchFusionDeciderAbsoluteSize matchFusionDeciderAbsoluteSize,
                       MatchFusionDeciderRelativeSize matchFusionDeciderRelativeSize) {
        fusionMethods.put(ActionOptions.MatchFusionMethod.ABSOLUTE, matchFusionDeciderAbsoluteSize);
        fusionMethods.put(ActionOptions.MatchFusionMethod.RELATIVE, matchFusionDeciderRelativeSize);
    }

    public void setFusedMatches(Matches matches) {
        if (matches.getActionOptions().getFusionMethod() == NONE) return;
        matches.setMatchList(getFinalFusedMatchObjects(matches));
    }

    public List<Match> getFinalFusedMatchObjects(Matches matches) {
        List<Match> fusedMatches = new ArrayList<>(matches.getMatchList());
        int size = 0;
        while (fusedMatches.size() != size) {
            size = fusedMatches.size();
            fusedMatches = getFusedMatchObjects(fusedMatches, matches.getActionOptions());
        }
        return fusedMatches;
    }

    /**
     * Match objects are fused, and the image and text are captured from the scene(s).
     * @param matchList holds the match objects to fuse
     * @param actionOptions the action configuration.
     * @return the new Match list.
     */
    public List<Match> getFusedMatchObjects(List<Match> matchList, ActionOptions actionOptions) {
        MatchFusionDecider decider = fusionMethods.get(actionOptions.getFusionMethod());
        int maxXDistance = actionOptions.getMaxFusionDistanceX();
        int maxYDistance = actionOptions.getMaxFusionDistanceY();
        List<Match> fusedMatches = new ArrayList<>();
        if (matchList.isEmpty()) return fusedMatches;
        List<Match> originalMatches = matchList;
        List<Integer> toCheck = new ArrayList<>();
        for (int i=0; i<originalMatches.size(); i++) toCheck.add(i);
        while (!toCheck.isEmpty()) {
            /*
            Get the next unallocated Match
             */
            int nextUnallocatedIndex = toCheck.get(0);
            Match m = new Match.Builder()
                    .setRegion(originalMatches.get(nextUnallocatedIndex).getRegion())
                    .build();
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
                        m = new Match.Builder()
                                .setRegion(r)
                                .build();
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
