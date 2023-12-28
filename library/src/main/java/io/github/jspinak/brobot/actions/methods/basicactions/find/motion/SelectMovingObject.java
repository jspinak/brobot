package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.analysis.Distance;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SelectMovingObject {

    private final Distance distance;

    public SelectMovingObject(Distance distance) {
        this.distance = distance;
    }

    /**
     * Moving objects are found where
     * 1. a Match overlaps between the first and second MatchList
     * 2. there is a Match in MatchList2 in the opposite direction of a Match in MatchList1
     * 3. the Match found in 2. is approximately the same distance from the shared Match in both MatchLists
     * @param matchList1 matches representing movement between scenes 1 and 2
     * @param matchList2 matches representing movement between scenes 2 and 3
     * @param maxDistance the maximum distance between the shared Match and the Match found in 2.
     * @return matches representing the location of moving objects in scene 3
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
