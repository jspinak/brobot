package io.github.jspinak.brobot.util.geometry;

import static java.util.stream.Collectors.toMap;

import java.util.*;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Grid;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.OverlappingGrids;
import io.github.jspinak.brobot.model.element.Region;

/**
 * Fast spatial clustering using overlapping grid-based partitioning.
 *
 * <p>This utility performs efficient clustering of 2D points by dividing space into grid cells and
 * counting point frequencies. It uses overlapping grids to avoid edge effects where clusters might
 * be split across grid boundaries. The algorithm is particularly effective for finding dense
 * regions of points without the computational overhead of traditional clustering algorithms.
 *
 * <p>Algorithm overview:
 *
 * <ol>
 *   <li>Divide region into a 3x3 grid
 *   <li>Create overlapping grid offset by half cell size
 *   <li>Count points in each cell of both grids
 *   <li>Sort cells by point frequency
 *   <li>Return top N densest regions
 * </ol>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>O(n) time complexity for n points
 *   <li>Overlapping grids prevent cluster splitting
 *   <li>No need to specify cluster centers
 *   <li>Works well for rectangular regions
 *   <li>Handles variable cluster sizes
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Finding dense UI element groups
 *   <li>Identifying click/interaction hotspots
 *   <li>Spatial data preprocessing
 *   <li>Quick density estimation
 *   <li>Region of interest detection
 * </ul>
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>Fixed 3x3 grid may miss fine details
 *   <li>Rectangular cells don't match all cluster shapes
 *   <li>No iterative refinement in current implementation
 *   <li>Equal weight to all points
 * </ul>
 *
 * <p>For more accuracy, the process can be run iteratively, with each iteration focusing on one of
 * the high-density cells from the previous iteration.
 *
 * @see Grid
 * @see OverlappingGrids
 * @see Region#getGridRegion(int)
 * @see Region#getGridNumber(org.sikuli.script.Location)
 */
@Component
public class GridBasedClusterer {

    /**
     * Identifies regions with highest point density using overlapping grid analysis.
     *
     * <p>Creates two overlapping 3x3 grids over the region and counts points in each cell. The
     * overlapping approach ensures clusters near cell boundaries are properly detected. Returns up
     * to maxClusters regions ordered by point density.
     *
     * <p>Grid configuration:
     *
     * <ul>
     *   <li>Primary grid: 3x3 cells covering the entire region
     *   <li>Overlapping grid: Offset by half cell size
     *   <li>Both grids analyzed independently
     *   <li>Results merged and sorted by density
     * </ul>
     *
     * <p>Return value:
     *
     * <ul>
     *   <li>Key: Region representing a grid cell
     *   <li>Value: Number of points in that cell
     *   <li>Ordered by descending point count
     *   <li>Limited to maxClusters entries
     * </ul>
     *
     * @param region bounding area to analyze for clusters
     * @param points list of locations to cluster
     * @param maxClusters maximum number of cluster regions to return
     * @return map of high-density regions to their point counts, ordered by density
     */
    public Map<Region, Integer> getClusterRegions(
            Region region, List<Location> points, int maxClusters) {
        Grid grid = new Grid.Builder().setRegion(region).setRows(3).setColumns(3).build();
        OverlappingGrids grids = new OverlappingGrids(grid);
        // populate maps showing the frequency of points in regions for both the region and the
        // overlapReg
        Map<Integer, Integer> pointFreq = new HashMap<>();
        Map<Integer, Integer> pointFreqOverlap = new HashMap<>();
        for (Location location : points) {
            putLocationInFrequencyMap(location, region, pointFreq);
            putLocationInFrequencyMap(location, grids.getInnerGrid().getRegion(), pointFreqOverlap);
        }
        return getTopRegions(
                maxClusters, pointFreq, pointFreqOverlap, region, grids.getInnerGrid().getRegion());
    }

    /**
     * Selects and merges top density regions from both grids.
     *
     * <p>This method combines results from overlapping grids to find the overall densest regions.
     * It prevents the same cluster from being split across grid boundaries by considering both grid
     * systems.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Sort both frequency maps by density (descending)
     *   <li>Take top N regions from each grid
     *   <li>Merge results, keeping all unique regions
     *   <li>Re-sort combined results by density
     *   <li>Return top maxClusters regions
     * </ol>
     *
     * <p>Bug note: The current implementation has a bug where it uses index i instead of accessing
     * map entries properly, potentially causing incorrect grid number retrieval.
     *
     * @param maxClusters limit on returned regions
     * @param pointFreq frequency map for primary grid
     * @param pointFreqOverlap frequency map for overlapping grid
     * @param region primary grid region for cell lookup
     * @param regionOverlap overlapping grid region for cell lookup
     * @return top density regions from combined analysis
     */
    private Map<Region, Integer> getTopRegions(
            int maxClusters,
            Map<Integer, Integer> pointFreq,
            Map<Integer, Integer> pointFreqOverlap,
            Region region,
            Region regionOverlap) {
        Map<Region, Integer> topRegions = new HashMap<>();
        // sort maps by value
        Map<Integer, Integer> sortedPointFreq =
                pointFreq.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(
                                toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e2,
                                        LinkedHashMap::new));
        Map<Integer, Integer> sortedPointFreqOverlap =
                pointFreqOverlap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(
                                toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e2,
                                        LinkedHashMap::new));
        // Convert sorted maps to lists of entries to access by index
        List<Map.Entry<Integer, Integer>> sortedEntries =
                new ArrayList<>(sortedPointFreq.entrySet());
        List<Map.Entry<Integer, Integer>> sortedEntriesOverlap =
                new ArrayList<>(sortedPointFreqOverlap.entrySet());

        for (int i = 0; i < maxClusters; i++) {
            if (sortedEntries.size() > i) {
                Map.Entry<Integer, Integer> entry = sortedEntries.get(i);
                Integer gridNumber = entry.getKey();
                Integer freq = entry.getValue();
                if (gridNumber != null && freq != null && gridNumber >= 0 && gridNumber < 9)
                    topRegions.put(region.getGridRegion(gridNumber), freq);
            }
            if (sortedEntriesOverlap.size() > i) {
                Map.Entry<Integer, Integer> entry = sortedEntriesOverlap.get(i);
                Integer gridNumber = entry.getKey();
                Integer freq = entry.getValue();
                if (gridNumber != null && freq != null && gridNumber >= 0 && gridNumber < 9)
                    topRegions.put(regionOverlap.getGridRegion(gridNumber), freq);
            }
        }
        // sort topRegions
        Map<Region, Integer> sortedTopRegs =
                topRegions.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .collect(
                                toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (e1, e2) -> e2,
                                        LinkedHashMap::new));
        Map<Region, Integer> topRegsMaxClusters = new HashMap<>();
        Iterator<Map.Entry<Region, Integer>> entries = sortedTopRegs.entrySet().stream().iterator();
        for (int i = 0; i < maxClusters; i++) {
            if (!entries.hasNext()) break;
            Map.Entry<Region, Integer> entry = entries.next();
            topRegsMaxClusters.put(entry.getKey(), entry.getValue());
        }
        return topRegsMaxClusters;
    }

    /**
     * Increments point count for the grid cell containing a location.
     *
     * <p>Determines which grid cell contains the given location and increments its frequency count.
     * Uses the merge operation for thread-safe counting if this method is called concurrently.
     *
     * <p>Grid numbering is row-major:
     *
     * <pre>
     * 0 1 2
     * 3 4 5
     * 6 7 8
     * </pre>
     *
     * <p>Side effects: Modifies the frequency map by incrementing counts.
     *
     * @param location point to assign to a grid cell
     * @param reg region containing the grid definition
     * @param freqMap frequency map to update; modified in place
     */
    private void putLocationInFrequencyMap(
            Location location, Region reg, Map<Integer, Integer> freqMap) {
        Optional<Integer> gridNumberOpt = reg.getGridNumber(location.sikuli());
        if (gridNumberOpt.isEmpty() || gridNumberOpt.get() == -1) return;
        freqMap.merge(gridNumberOpt.get(), 1, Integer::sum);
    }
}
