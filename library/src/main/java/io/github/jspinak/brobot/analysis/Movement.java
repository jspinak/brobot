package io.github.jspinak.brobot.analysis;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toMap;

@Component
public class Movement {

    /**
     * For a scenario when we wish to determine the direction and magnitude of
     * movement with respect to images on the screen. For example, an image appears
     * multiple time on a map, and after we move, the locations of the images also
     * change. We can figure out the direction and magnitude of our move by comparing
     * the image locations before and after the move.
     *
     * @param measurementAccuracy Allows for similar measurements to be counted as the same movement.
     *                            A value of 0 means the measurement must be exact.
     *                            A value of 1 means that a movement of 5 is treated equal to a move of 6.
     * @param firstMatches Images before the move
     * @param secondMatches Images after the move
     * @return A Location giving the direction and magnitude of the move
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
        // find the locations between each match in firstMatches and each match in secondMatches
        List<List<Location>> locations = new ArrayList<>();
        firstMatches.forEach(m1 -> {
            List<Location> loc = new ArrayList<>();
            secondMatches.forEach(m2 -> loc.add(new Location(m1.x - m2.x, m1.y - m2.y)));
            locations.add(loc);
        });
        // get the most frequent locations in terms of distance and magnitude
        Map<Location, Integer> timesAppearing = new HashMap<>();
        locations.forEach(locList -> locList.forEach(loc -> {
            boolean found = false;
            for (Location locInMap : timesAppearing.keySet()) {
                if (Math.abs(loc.getX() - locInMap.getX()) <= measurementAccuracy &&
                        Math.abs(loc.getY() - locInMap.getY()) <= measurementAccuracy)
                {
                    timesAppearing.put(locInMap, timesAppearing.get(locInMap) + 1);
                    found = true;
                }
            }
            if (!found) timesAppearing.put(loc, 1);
        }));
        Map<Location, Integer> sortedDescending = timesAppearing.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        if (sortedDescending.isEmpty()) return new ArrayList<>();
        /*
        Report.print("frequency~location: ");
        sortedDescending.forEach((key, value) -> Report.print(value + "~" + key.getX() + "." + key.getY() + " "));
        Report.println();
         */
        // return the Locations that are most frequent
        Iterator<Map.Entry<Location, Integer>> iterator = sortedDescending.entrySet().iterator();
        Map.Entry<Location, Integer> entry = iterator.next();
        int highestFrequency = entry.getValue();
        List<Location> mostFreqLoc = new ArrayList<>();
        while (entry.getValue() == highestFrequency) {
            mostFreqLoc.add(entry.getKey());
            if (!iterator.hasNext()) break;
            entry = iterator.next();
        }
        return mostFreqLoc;
    }

}
