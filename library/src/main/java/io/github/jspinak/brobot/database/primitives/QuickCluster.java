package io.github.jspinak.brobot.database.primitives;

import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.region.Region;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Meant as a quick and dirty alternative to k-means clustering. It divides a region into
 * grid cells, some overlapping, and counts the points that are within the cells. The
 * median location is returned. For more accuracy, the process can be run more than once,
 * with each successive iteration selecting one of the cells as the area to divide.
 */
@Component
public class QuickCluster {

    public Map<Region, Integer> getClusterRegions(Region region, List<Location> points, int maxClusters) {
        int rows = 3;
        int cols = 3;
        List<Region> gridRegions = region.getGridRegions(rows, cols);
        int cellW = gridRegions.get(0).w;
        int cellH = gridRegions.get(0).h;
        Region overlapReg = new Region(region.x + cellW/2, region.y + cellH/2,
                region.w - cellW, region.h - cellH);
        List<Region> overlapGridRegions = overlapReg.getGridRegions(rows - 1, cols - 1);
        // populate maps showing the frequency of points in regions for both the region and the overlapReg
        Map<Integer, Integer> pointFreq = new HashMap<>();
        Map<Integer, Integer> pointFreqOverlap = new HashMap<>();
        for (Location location : points) {
            putLocationInFrequencyMap(location, region, pointFreq);
            putLocationInFrequencyMap(location, overlapReg, pointFreqOverlap);
        }
        return getTopRegions(maxClusters, pointFreq, pointFreqOverlap, region, overlapReg);
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
        Optional<Integer> gridNumberOpt = reg.getGridNumber(location);
        if (gridNumberOpt.isEmpty() || gridNumberOpt.get() == -1) return;
        freqMap.merge(gridNumberOpt.get(), 1, Integer::sum);
    }

}