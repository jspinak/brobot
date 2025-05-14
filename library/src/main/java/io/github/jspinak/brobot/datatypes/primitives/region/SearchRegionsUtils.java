package io.github.jspinak.brobot.datatypes.primitives.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility methods for working with SearchRegions objects.
 * This class contains operations that are useful but not essential for the core functionality.
 * Also provides alternative implementations for methods that might cause issues with JSON serialization.
 */
public class SearchRegionsUtils {

    private static final Random random = new Random();

    /**
     * Creates a new SearchRegions from a list of regions.
     * Safe to use with potentially immutable lists.
     *
     * @param regions The list of regions to use
     * @return A new SearchRegions containing the regions
     */
    public static SearchRegions fromRegionList(List<Region> regions) {
        SearchRegions searchRegions = new SearchRegions();
        if (regions != null) {
            for (Region region : regions) {
                searchRegions.addSearchRegions(region);
            }
        }
        return searchRegions;
    }

    /**
     * Gets a mutable copy of all regions.
     * This is useful for operations that need to modify the list.
     *
     * @param searchRegions The SearchRegions to get regions from
     * @return A mutable ArrayList containing the regions
     */
    public static List<Region> getMutableRegionsCopy(SearchRegions searchRegions) {
        if (searchRegions.getRegions().isEmpty()) {
            List<Region> fallback = new ArrayList<>();
            fallback.add(new Region());
            return fallback;
        }
        return new ArrayList<>(searchRegions.getRegions());
    }

    /**
     * Gets all regions as a mutable list.
     * This is a safe alternative to getAllRegions() which might return an immutable list.
     *
     * @param searchRegions The SearchRegions to get regions from
     * @return A mutable list of all regions
     */
    public static List<Region> getAllRegionsMutable(SearchRegions searchRegions) {
        if (!searchRegions.getRegions().isEmpty()) {
            return new ArrayList<>(searchRegions.getRegions());
        }
        List<Region> fallback = new ArrayList<>();
        fallback.add(new Region());
        return fallback;
    }

    /**
     * Gets a random region from the list
     *
     * @param regions List of regions to choose from
     * @return A randomly selected region
     */
    public static Region getRandomRegion(List<Region> regions) {
        if (regions == null || regions.isEmpty()) {
            return new Region();
        }
        return regions.get(random.nextInt(regions.size()));
    }

    /**
     * Merges two SearchRegions objects.
     *
     * @param first The first SearchRegions
     * @param second The second SearchRegions
     * @return A new SearchRegions containing regions from both inputs
     */
    public static SearchRegions merge(SearchRegions first, SearchRegions second) {
        SearchRegions result = new SearchRegions();

        // Add regions from first
        for (Region region : first.getRegions()) {
            result.addSearchRegions(region);
        }

        // Add regions from second
        for (Region region : second.getRegions()) {
            result.addSearchRegions(region);
        }

        // Handle fixed region - prefer the first if defined
        if (first.isFixedRegionSet()) {
            result.setFixedRegion(first.getFixedRegion());
        } else if (second.isFixedRegionSet()) {
            result.setFixedRegion(second.getFixedRegion());
        }

        return result;
    }

    /**
     * Creates a string representation of the SearchRegions for debugging.
     *
     * @param searchRegions The SearchRegions to convert
     * @return A string representation of the regions
     */
    public static String toString(SearchRegions searchRegions) {
        StringBuilder sb = new StringBuilder("SearchRegions{");

        // Add regions
        List<Region> regions = searchRegions.getRegions();
        sb.append("regions=");
        if (regions.isEmpty()) {
            sb.append("[]");
        } else {
            sb.append("[");
            for (int i = 0; i < regions.size(); i++) {
                sb.append(regions.get(i));
                if (i < regions.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }

        // Add fixed region if defined
        sb.append(", fixedRegion=");
        if (searchRegions.isFixedRegionSet()) {
            sb.append(searchRegions.getFixedRegion());
        } else {
            sb.append("undefined");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Creates a deep copy of a SearchRegions object
     *
     * @param searchRegions The SearchRegions to copy
     * @return A new SearchRegions with the same regions
     */
    public static SearchRegions deepCopy(SearchRegions searchRegions) {
        SearchRegions copy = new SearchRegions();

        // Copy regular regions
        for (Region region : searchRegions.getRegions()) {
            copy.addSearchRegions(new Region(region.x(), region.y(), region.w(), region.h()));
        }

        // Copy fixed region if defined
        if (searchRegions.isFixedRegionSet()) {
            Region fixed = searchRegions.getFixedRegion();
            copy.setFixedRegion(new Region(fixed.x(), fixed.y(), fixed.w(), fixed.h()));
        }

        return copy;
    }
}