package io.github.jspinak.brobot.analysis.motion;

import io.github.jspinak.brobot.util.geometry.DistanceCalculator;
import io.github.jspinak.brobot.action.basic.find.motion.FindMotion;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Identifies moving objects by analyzing motion patterns across three consecutive scenes.
 * This class implements an algorithm that distinguishes true moving objects from
 * background changes by tracking consistent motion trajectories.
 * 
 * <p>The algorithm works by:
 * <ul>
 * <li>Finding overlapping regions between scene transitions</li>
 * <li>Identifying opposite-direction movements that indicate object motion</li>
 * <li>Verifying consistent distances to confirm the same object is moving</li>
 * </ul></p>
 * 
 * <p>A moving object is identified when:
 * <ol>
 * <li>A match region overlaps between two scene transitions</li>
 * <li>There's a match in the opposite direction (>150° difference)</li>
 * <li>The distances are consistent, indicating the same object</li>
 * </ol></p>
 * 
 * <p>This approach effectively filters out camera movements and background
 * changes, focusing on objects that move independently within the scene.</p>
 * 
 * @see DistanceCalculator
 * @see FindMotion
 */
@Component
public class MovingObjectSelector {

    private final DistanceCalculator distance;

    /**
     * Constructs a MovingObjectSelector instance with distance calculation utility.
     * 
     * @param distance utility for calculating distances and angles between matches
     */
    public MovingObjectSelector(DistanceCalculator distance) {
        this.distance = distance;
    }

    /**
     * Selects moving objects by analyzing motion patterns across three scenes.
     * The algorithm identifies objects that move in opposite directions between
     * scene transitions, which indicates independent object motion rather than
     * camera movement or background changes.
     * 
     * <p>The returned list contains three sublists:
     * <ul>
     * <li>Index 0: Matches from scene 1 (starting positions)</li>
     * <li>Index 1: Overlapping matches in scene 2 (intermediate positions)</li>
     * <li>Index 2: Matches from scene 3 (final positions)</li>
     * </ul></p>
     * 
     * @param matchList1 matches representing changes between scenes 1 and 2
     * @param matchList2 matches representing changes between scenes 2 and 3
     * @param maxDistance maximum allowed distance for related matches
     * @return list of three lists containing matches for each scene's moving objects
     */
    public List<List<Match>> select(List<Match> matchList1, List<Match> matchList2, int maxDistance) {
        List<Match> overlappingMatches = getOverlappingMatches(matchList1, matchList2);
        List<List<Match>> movingMatches = new ArrayList<>();
        List<Match> movingMatches0 = new ArrayList<>();
        List<Match> movingMatches1 = new ArrayList<>();
        List<Match> movingMatches2 = new ArrayList<>();
        for (Match overlappingMatch : overlappingMatches) {
            List<Match> matchesWithinDistance1 = getMatchesWithinDistance(matchList1, overlappingMatch, maxDistance);
            List<Match> matchesWithinDistance2 = getMatchesWithinDistance(matchList2, overlappingMatch, maxDistance);
            for (Match match2 : matchesWithinDistance2) {
                for (Match match1 : matchesWithinDistance1) {
                    double degreesBetweenMatches = distance.getDegreesBetween(overlappingMatch, match1, match2);
                    if (degreesBetweenMatches > 150 || degreesBetweenMatches < -150) {
                        movingMatches2.add(match2);
                        movingMatches0.add(match1);
                        movingMatches1.add(overlappingMatch);
                        break;
                    }
                }
            }
        }
        movingMatches.add(movingMatches0);
        movingMatches.add(movingMatches1);
        movingMatches.add(movingMatches2);
        return movingMatches;
    }

    /**
     * Finds matches that overlap between two match lists.
     * Overlapping matches indicate regions that changed in both scene transitions,
     * which are candidates for containing moving objects.
     * 
     * @param matchList1 matches from the first scene transition
     * @param matchList2 matches from the second scene transition
     * @return list of matches from matchList1 that overlap with any match in matchList2
     */
    private List<Match> getOverlappingMatches(List<Match> matchList1, List<Match> matchList2) {
        List<Match> overlappingMatches = new ArrayList<>();
        for (Match match1 : matchList1) {
            Region reg1 = new Region(match1);
            for (Match match2 : matchList2) {
                if (reg1.overlaps(new Region(match2))) {
                    overlappingMatches.add(match1);
                    break;
                }
            }
        }
        return overlappingMatches;
    }

    /**
     * Filters matches to find those within a specified distance of a center match.
     * This helps identify related motion regions that could represent the same
     * moving object at different positions.
     * 
     * @param matchList list of matches to filter
     * @param centerMatch the reference match to measure distances from
     * @param maxDist maximum allowed distance from the center match
     * @return list of matches within the specified distance (excluding the center match itself)
     */
    private List<Match> getMatchesWithinDistance(List<Match> matchList, Match centerMatch, int maxDist) {
        List<Match> matchesWithinDistance = new ArrayList<>();
        for (Match match : matchList) {
            double dist = distance.getDistance(centerMatch, match);
            if (dist <= maxDist && dist > 0) {
                matchesWithinDistance.add(match);
            }
        }
        return matchesWithinDistance;
    }


}
