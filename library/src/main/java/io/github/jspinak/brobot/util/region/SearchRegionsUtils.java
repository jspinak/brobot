package io.github.jspinak.brobot.util.region;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;

/**
 * Utility methods for manipulating and working with SearchRegions objects.
 *
 * <p>This class provides safe alternatives and extended functionality for SearchRegions operations.
 * It addresses common issues such as immutable list handling from JSON deserialization and provides
 * convenience methods for common operations.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Safe handling of potentially immutable lists from JSON deserialization
 *   <li>Creation of mutable copies for modification operations
 *   <li>Merging and deep copying of SearchRegions
 *   <li>Random region selection for testing or distribution
 *   <li>Debug-friendly string representations
 * </ul>
 *
 * <p>Design rationale:
 *
 * <ul>
 *   <li>Methods return new instances rather than modifying inputs
 *   <li>Defensive copying ensures thread safety and prevents side effects
 *   <li>Fallback to empty regions prevents null pointer exceptions
 *   <li>Mutable list guarantees allow safe modification operations
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Converting immutable JSON-deserialized lists to mutable ones
 *   <li>Combining search regions from multiple sources
 *   <li>Creating test data with random region selection
 *   <li>Debugging region configurations with string representations
 * </ul>
 *
 * <p>Thread safety: All methods are stateless except for the shared Random instance. Methods create
 * new objects rather than modifying inputs.
 *
 * @see SearchRegions
 * @see Region
 */
public class SearchRegionsUtils {

    /**
     * Shared Random instance for region selection. Thread-safe as Random is thread-safe for basic
     * operations.
     */
    private static final Random random = new Random();

    /**
     * Creates a new SearchRegions from a list of regions.
     *
     * <p>This method safely handles potentially immutable lists that may result from JSON
     * deserialization. Each region is individually added to ensure the resulting SearchRegions has
     * a mutable internal list.
     *
     * <p>Null handling:
     *
     * <ul>
     *   <li>Null list: Returns empty SearchRegions
     *   <li>Null regions in list: Skipped (not added)
     * </ul>
     *
     * <p>Use case: Converting deserialized region lists into proper SearchRegions objects when the
     * list mutability is uncertain.
     *
     * @param regions the list of regions to convert; may be null or immutable
     * @return a new SearchRegions containing copies of the input regions
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
     * Gets a mutable copy of all regions with fallback to default region.
     *
     * <p>Creates a new ArrayList containing all regions from the SearchRegions. If no regions
     * exist, returns a list with a single default Region to prevent empty list issues in downstream
     * operations.
     *
     * <p>Fallback behavior:
     *
     * <ul>
     *   <li>Empty regions: Returns list with one default Region()
     *   <li>Non-empty: Returns mutable copy of all regions
     * </ul>
     *
     * <p>Use case: Preparing region lists for modification operations such as sorting, filtering,
     * or transformation without affecting the original.
     *
     * @param searchRegions the SearchRegions to copy regions from
     * @return a mutable ArrayList containing the regions, never empty
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
     * Gets all regions as a guaranteed mutable list.
     *
     * <p>Safe alternative to SearchRegions.getAllRegions() which may return an immutable list after
     * JSON deserialization. This method ensures the returned list can be safely modified.
     *
     * <p>Implementation note: Similar to getMutableRegionsCopy but with slightly different method
     * naming for API consistency. Consider consolidating these methods in future versions.
     *
     * <p>Fallback: Returns list with default Region if source is empty.
     *
     * @param searchRegions the SearchRegions to extract regions from
     * @return a new mutable ArrayList of regions, never empty
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
     * Selects a random region from the provided list.
     *
     * <p>Uses a uniform distribution to select one region from the list. Useful for load
     * distribution, random testing, or when any region from a set is acceptable.
     *
     * <p>Edge cases:
     *
     * <ul>
     *   <li>Null list: Returns default Region()
     *   <li>Empty list: Returns default Region()
     *   <li>Single region: Always returns that region
     * </ul>
     *
     * <p>Thread safety: Uses shared Random instance but is thread-safe for basic random number
     * generation.
     *
     * @param regions list of regions to select from; may be null or empty
     * @return a randomly selected region or default Region() if list is invalid
     */
    public static Region getRandomRegion(List<Region> regions) {
        if (regions == null || regions.isEmpty()) {
            return new Region();
        }
        return regions.get(random.nextInt(regions.size()));
    }

    /**
     * Merges two SearchRegions objects into a new combined instance.
     *
     * <p>Creates a new SearchRegions containing all regions from both inputs. The fixed region
     * handling follows a priority system where the first SearchRegions' fixed region takes
     * precedence if defined.
     *
     * <p>Merge behavior:
     *
     * <ul>
     *   <li>Regular regions: All regions from both inputs are included
     *   <li>Fixed region priority: first > second > undefined
     *   <li>No deduplication: Duplicate regions are preserved
     * </ul>
     *
     * <p>Use cases:
     *
     * <ul>
     *   <li>Combining search areas from multiple sources
     *   <li>Building composite search strategies
     *   <li>Fallback region handling with priority
     * </ul>
     *
     * @param first the primary SearchRegions (higher priority for fixed region)
     * @param second the secondary SearchRegions to merge
     * @return a new SearchRegions containing all regions from both inputs
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
     * Creates a detailed string representation for debugging purposes.
     *
     * <p>Generates a human-readable string showing all regions and the fixed region status. Useful
     * for logging, debugging, and understanding the current state of a SearchRegions object.
     *
     * <p>Output format:
     *
     * <pre>
     * SearchRegions{regions=[Region1, Region2, ...], fixedRegion=Region/undefined}
     * </pre>
     *
     * <p>Implementation note: Uses StringBuilder for efficiency when dealing with potentially large
     * numbers of regions.
     *
     * @param searchRegions the SearchRegions to convert to string
     * @return a formatted string representation showing all regions and fixed region status
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
     * Creates a deep copy of a SearchRegions object.
     *
     * <p>Produces a new SearchRegions instance with new Region objects that have the same
     * coordinates as the originals. This ensures complete independence between the copy and the
     * original.
     *
     * <p>Copy behavior:
     *
     * <ul>
     *   <li>Regular regions: Each region is recreated with same coordinates
     *   <li>Fixed region: Copied if defined, maintaining the same state
     *   <li>Region references: All new objects (true deep copy)
     * </ul>
     *
     * <p>Use cases:
     *
     * <ul>
     *   <li>Creating modified versions without affecting originals
     *   <li>Storing snapshots of search configurations
     *   <li>Thread-safe region manipulation
     * </ul>
     *
     * <p>Performance: O(n) where n is the number of regions.
     *
     * @param searchRegions the SearchRegions to copy
     * @return a new SearchRegions with independent copies of all regions
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
