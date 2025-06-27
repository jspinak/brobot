package io.github.jspinak.brobot.analysis.match;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.util.string.StringFusion;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.action.ActionOptions.MatchFusionMethod.NONE;

import java.util.*;


/**
 * Combines overlapping or closely positioned matches into single unified matches.
 * <p>
 * Match fusion is a critical post-processing step that prevents duplicate detections
 * and improves result quality by merging matches that likely represent the same UI
 * element. The fusion process iteratively combines matches based on configurable
 * distance thresholds and fusion strategies.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Supports multiple fusion strategies (absolute size, relative size)</li>
 *   <li>Iterative fusion until no more matches can be combined</li>
 *   <li>Preserves match names through string fusion</li>
 *   <li>Configurable distance thresholds for X and Y axes</li>
 * </ul>
 * 
 * <p><b>Important:</b> Fused matches lose their original pattern, state object, and
 * other metadata. If this information is needed, perform fusion on a copy of the
 * matches or retain the original matches separately.
 * 
 * @see MatchFusionDecider
 * @see ActionOptions.MatchFusionMethod
 * @see Match
 */
@Component
public class MatchFusion {
    private final Map<ActionOptions.MatchFusionMethod, MatchFusionDecider> fusionMethods = new HashMap<>();

    public MatchFusion(AbsoluteSizeFusionDecider matchFusionDeciderAbsoluteSize,
                       RelativeSizeFusionDecider matchFusionDeciderRelativeSize) {
        fusionMethods.put(ActionOptions.MatchFusionMethod.ABSOLUTE, matchFusionDeciderAbsoluteSize);
        fusionMethods.put(ActionOptions.MatchFusionMethod.RELATIVE, matchFusionDeciderRelativeSize);
    }

    /**
     * Applies match fusion to the provided ActionResult if fusion is enabled.
     * <p>
     * This method modifies the match list in-place by replacing all matches with
     * their fused equivalents. The fusion process continues iteratively until no
     * more matches can be combined. If fusion method is set to NONE, no changes
     * are made.
     * 
     * @param matches The ActionResult containing matches to fuse. The match list
     *                within this object is modified in-place with fused results.
     */
    public void setFusedMatches(ActionResult matches) {
        if (matches.getActionOptions().getFusionMethod() == NONE) return;
        matches.setMatchList(getFinalFusedMatchObjects(matches));
    }

    /**
     * Performs complete iterative fusion of matches until no more can be combined.
     * <p>
     * This method repeatedly applies the fusion algorithm until a stable state is
     * reached where no additional matches can be fused. This ensures that all
     * possible combinations are found, even when initial fusion creates new
     * opportunities for further fusion.
     * 
     * @param matches The ActionResult containing the matches to fuse and the
     *                fusion configuration in its ActionOptions
     * @return A new list containing the fully fused matches. The original matches
     *         in the ActionResult are not modified.
     */
    public List<Match> getFinalFusedMatchObjects(ActionResult matches) {
        List<Match> fusedMatches = new ArrayList<>(matches.getMatchList());
        int size = 0;
        while (fusedMatches.size() != size) {
            size = fusedMatches.size();
            fusedMatches = getFusedMatchObjects(fusedMatches, matches.getActionOptions());
        }
        return fusedMatches;
    }

    /**
     * Performs one iteration of match fusion on the provided match list.
     * <p>
     * This method implements the core fusion algorithm, examining each match to find
     * others that should be combined with it based on the fusion strategy and distance
     * thresholds. The algorithm:
     * <ol>
     *   <li>Processes each unallocated match as a potential fusion seed</li>
     *   <li>Iteratively expands the seed by fusing nearby matches</li>
     *   <li>Creates a new combined match with merged regions and concatenated names</li>
     *   <li>Continues until all matches have been allocated to fusion groups</li>
     * </ol>
     * 
     * <p>The fusion decision is delegated to the configured {@link MatchFusionDecider},
     * allowing for different fusion strategies. Match names are preserved and combined
     * using string fusion to maintain traceability.
     * 
     * @param matchList The list of matches to fuse. This list is not modified.
     * @param actionOptions Configuration containing the fusion method and distance
     *                      thresholds (maxFusionDistanceX, maxFusionDistanceY)
     * @return A new list containing the fused matches. May be smaller than the input
     *         list if matches were combined.
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
            Match originalMatch = originalMatches.get(nextUnallocatedIndex);
            String name = originalMatch.getName();
            Match m = new Match.Builder()
                    .setRegion(originalMatch.getRegion())
                    .setName(name)
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
                        name = StringFusion.fuse(name, originalMatches.get(i).getName());
                        m = new Match.Builder()
                                .setRegion(r)
                                .setName(name)
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
