package io.github.jspinak.brobot.analysis;

import io.github.jspinak.brobot.datatypes.primitives.grid.Grid;
import io.github.jspinak.brobot.datatypes.primitives.grid.OverlappingGrids;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * It divides a region into
 * grid cells, some overlapping, and counts the points that are within the cells. The
 * median location is returned. For more accuracy, the process can be run more than once,
 * with each successive iteration selecting one of the cells as the area to divide.
 */
@Component
public class QuickCluster {

    public Map<Region, Integer> getClusterRegions(Region region, List<Location> points, int maxClusters) {
        Grid grid = new Grid.Builder()
                .setRegion(region)
                .setRows(3)
                .setColumns(3)
                .build();
        OverlappingGrids grids = new OverlappingGrids(grid);
        // populate maps showing the frequency of points in regions for both the region and the overlapReg
        Map<Integer, Integer> pointFreq = new HashMap<>();
        Map<Integer, Integer> pointFreqOverlap = new HashMap<>();
        for (Location location : points) {
            putLocationInFrequencyMap(location, region, pointFreq);
            putLocationInFrequencyMap(location, grids.getInnerGrid().getRegion(), pointFreqOverlap);
        }
        return getTopRegions(maxClusters, pointFreq, pointFreqOverlap, region, grids.getInnerGrid().getRegion());
    }

    private Map<Region, Integer> getTopRegions(int maxClusters, Map<Integer, Integer> pointFreq,
                                       Map<Integer, Integer> pointFreqOverlap,
                                       Region region, Region regionOverlap) {
        Map<Region, Integer> topRegions = new HashMap<>();
        // sort maps by value
        Map<Integer, Integer> sortedPointFreq = pointFreq.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        Map<Integer, Integer> sortedPointFreqOverlap = pointFreqOverlap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        for (int i=0; i<maxClusters; i++) {
            if (sortedPointFreq.size() > i) {
                Integer gridNumber = sortedPointFreq.get(i);
                Integer freq = sortedPointFreq.get(i);
                if (gridNumber != null && freq != null)
                    topRegions.put(region.getGridRegion(gridNumber), freq);
            }
            if (sortedPointFreqOverlap.size() > i) {
                Integer gridNumber = sortedPointFreqOverlap.get(i);
                Integer freq = sortedPointFreqOverlap.get(i);
                if (gridNumber != null && freq != null)
                    topRegions.put(regionOverlap.getGridRegion(gridNumber), freq);
            }
        }
        // sort topRegions
        Map<Region, Integer> sortedTopRegs = topRegions.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        Map<Region, Integer> topRegsMaxClusters = new HashMap<>();
        Iterator<Map.Entry<Region, Integer>> entries = sortedTopRegs.entrySet().stream().iterator();
        for (int i=0; i<maxClusters; i++) {
            if (!entries.hasNext()) break;
            Map.Entry<Region, Integer> entry = entries.next();
            topRegsMaxClusters.put(entry.getKey(), entry.getValue());
        }
        return topRegsMaxClusters;
    }

    private void putLocationInFrequencyMap(Location location, Region reg, Map<Integer, Integer> freqMap) {
        Optional<Integer> gridNumberOpt = reg.getGridNumber(location.sikuli());
        if (gridNumberOpt.isEmpty() || gridNumberOpt.get() == -1) return;
        freqMap.merge(gridNumberOpt.get(), 1, Integer::sum);
    }

}
