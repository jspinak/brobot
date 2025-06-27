package io.github.jspinak.brobot.util.geometry;

import io.github.jspinak.brobot.model.element.Location;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Analyzes movement patterns by comparing object positions before and after actions.
 * <p>
 * This utility helps determine camera or viewport movement by tracking how multiple
 * instances of the same visual element shift position. It's particularly useful for
 * navigation in games, maps, or scrollable interfaces where movement can be inferred
 * from the displacement of recognizable landmarks.
 * <p>
 * Key concepts:
 * <ul>
 * <li>Movement detection: Compares positions of matches before and after an action</li>
 * <li>Voting algorithm: Most common displacement vector wins</li>
 * <li>Measurement tolerance: Accounts for slight variations in detection</li>
 * <li>Multi-match analysis: Uses multiple landmarks for accuracy</li>
 * </ul>
 * <p>
 * Algorithm overview:
 * <ol>
 * <li>Calculate displacement vectors between all match pairs</li>
 * <li>Group similar vectors within measurement accuracy</li>
 * <li>Count frequency of each displacement group</li>
 * <li>Return the most frequent displacement(s)</li>
 * </ol>
 * <p>
 * Use cases:
 * <ul>
 * <li>Game map navigation tracking</li>
 * <li>Scrolling distance measurement</li>
 * <li>Camera movement detection</li>
 * <li>Parallax effect analysis</li>
 * <li>Multi-layer UI navigation</li>
 * </ul>
 * <p>
 * Important notes:
 * <ul>
 * <li>Displacement is from second to first (opposite of movement direction)</li>
 * <li>Multiple results indicate ambiguous movement</li>
 * <li>Empty result means no consistent movement detected</li>
 * <li>Accuracy parameter affects grouping sensitivity</li>
 * </ul>
 *
 * @see Location
 * @see Match
 * @see DistanceCalculator
 */
@Component
public class MovementAnalyzer {

    /**
     * Determines movement vector by analyzing position changes of visual landmarks.
     * <p>
     * Calculates the most likely movement by comparing positions of the same
     * visual elements before and after an action. The algorithm uses a voting
     * system where the most frequent displacement vector is considered the
     * true movement.
     * <p>
     * Movement calculation:
     * <ul>
     * <li>Displacement = firstMatch position - secondMatch position</li>
     * <li>Positive X = movement to the right</li>
     * <li>Positive Y = movement down (screen coordinates)</li>
     * </ul>
     * <p>
     * Accuracy parameter effects:
     * <ul>
     * <li>0: Exact match required (pixel-perfect)</li>
     * <li>1: ±1 pixel tolerance in each dimension</li>
     * <li>5: ±5 pixel tolerance (handles detection noise)</li>
     * </ul>
     * <p>
     * Return value interpretation:
     * <ul>
     * <li>Single Location: Clear, unambiguous movement detected</li>
     * <li>Multiple Locations: Tie between equally likely movements</li>
     * <li>Empty list: No consistent movement pattern found</li>
     * </ul>
     * <p>
     * Example scenario: Tracking map movement in a game by monitoring
     * multiple landmark buildings that appear both before and after
     * scrolling.
     *
     * @param measurementAccuracy pixel tolerance for grouping similar movements (0 = exact)
     * @param firstMatches landmark positions before movement
     * @param secondMatches landmark positions after movement
     * @return list of most frequent movement vectors (usually one)
     */
    public List<Location> getMovement(int measurementAccuracy,
                                      List<Match> firstMatches, List<Match> secondMatches) {
        /*
        Report.print("first-matches: ");
        firstMatches.forEach(m -> Report.print(m.x+"."+m.y+" "));
        Report.println();
        Report.print("second-matches: ");
        secondMatches.forEach(m -> Report.print(m.x+"."+m.y+" "));
        Report.println();
         */
        // Calculate displacement vectors between all pairs of matches
        // Each inner list contains displacements from one first match to all second matches
        List<List<Location>> locations = new ArrayList<>();
        firstMatches.forEach(m1 -> {
            List<Location> loc = new ArrayList<>();
            // Calculate displacement: first position minus second position
            // This gives the movement vector from second to first
            secondMatches.forEach(m2 -> loc.add(new Location(m1.x - m2.x, m1.y - m2.y)));
            locations.add(loc);
        });
        // Count frequency of each displacement vector, grouping similar ones
        // within the measurement accuracy threshold
        Map<Location, Integer> timesAppearing = new HashMap<>();
        locations.forEach(locList -> locList.forEach(loc -> {
            boolean found = false;
            // Check if this displacement is similar to any already recorded
            for (Location locInMap : timesAppearing.keySet()) {
                if (Math.abs(loc.getCalculatedX() - locInMap.getCalculatedX()) <= measurementAccuracy &&
                        Math.abs(loc.getCalculatedY() - locInMap.getCalculatedY()) <= measurementAccuracy)
                {
                    // Similar displacement found, increment its count
                    timesAppearing.put(locInMap, timesAppearing.get(locInMap) + 1);
                    found = true;
                }
            }
            // New unique displacement, start counting at 1
            if (!found) timesAppearing.put(loc, 1);
        }));
        // Sort displacements by frequency in descending order
        Map<Location, Integer> sortedDescending = timesAppearing.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        if (sortedDescending.isEmpty()) return new ArrayList<>();
        /*
        Report.print("frequency~location: ");
        sortedDescending.forEach((key, value) -> Report.print(value + "~" + key.getX() + "." + key.getY() + " "));
        Report.println();
         */
        // Extract all displacement vectors with the highest frequency
        // Multiple results indicate ambiguous movement detection
        Iterator<Map.Entry<Location, Integer>> iterator = sortedDescending.entrySet().iterator();
        Map.Entry<Location, Integer> entry = iterator.next();
        int highestFrequency = entry.getValue();
        List<Location> mostFreqLoc = new ArrayList<>();
        // Collect all displacements with the highest frequency
        while (entry.getValue() == highestFrequency) {
            mostFreqLoc.add(entry.getKey());
            if (!iterator.hasNext()) break;
            entry = iterator.next();
        }
        return mostFreqLoc;
    }

}