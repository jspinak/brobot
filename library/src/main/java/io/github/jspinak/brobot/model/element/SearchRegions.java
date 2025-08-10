package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.util.region.RegionUtils;
import io.github.jspinak.brobot.util.region.SearchRegionsUtils;
import lombok.Data;

import java.util.*;

/**
 * Manages multiple search areas for pattern matching in the Brobot framework.
 * 
 * <p>SearchRegions provides sophisticated control over where patterns are searched for 
 * on the screen, supporting both dynamic and fixed location patterns. This flexibility 
 * is crucial for handling various GUI scenarios where elements may appear in multiple 
 * locations or have complex spatial constraints.</p>
 * 
 * <p>Key concepts:
 * <ul>
 *   <li><b>Multiple Regions</b>: Supports searching in multiple rectangular areas, 
 *       useful for complex layouts or when excluding certain screen areas</li>
 *   <li><b>Fixed Region</b>: Special handling for patterns that always appear in the 
 *       same location, optimizing search performance</li>
 *   <li><b>Dynamic Selection</b>: Can randomly select from available regions for 
 *       load distribution or testing variability</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases in model-based automation:
 * <ul>
 *   <li>Defining search areas for patterns that may appear in multiple locations 
 *       (e.g., pop-ups, floating windows)</li>
 *   <li>Excluding areas known to contain similar but incorrect patterns</li>
 *   <li>Optimizing performance by constraining searches to relevant screen areas</li>
 *   <li>Handling fixed UI elements that always appear in the same position</li>
 * </ul>
 * </p>
 * 
 * <p>The fixed region feature is particularly powerful for improving performance. When 
 * a pattern is found for the first time, its location can be recorded as fixed, and 
 * subsequent searches will only look in that specific area, dramatically reducing 
 * search time.</p>
 * 
 * @since 1.0
 * @see Region
 * @see Pattern
 * @see Find
 * @see SearchRegionsUtils
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRegions {

    public SearchRegions() {
        // Default constructor for serialization/deserialization
    }

    private List<Region> regions = new ArrayList<>();
    /**
     * The fixed region is usually defined when an image with a fixed location is found for the first time.
     * This region is then used in future FIND operations with the associated image.
     * Initialized to null to distinguish between "not set" and "explicitly set to a region".
     */
    private Region fixedRegion = null;

    /**
     * If the fixed region has been set.
     * @return true if set.
     */
    @JsonIgnore
    public boolean isFixedRegionSet() {
        return fixedRegion != null && fixedRegion.isDefined();
    }

    public void resetFixedRegion() {
        fixedRegion = null;
    }

    /**
     * Return the fixed region if defined.
     * Otherwise, return the first defined region.
     * If no regions are defined, return an undefined region.
     * @return one region.
     */
    @JsonIgnore
    public Region getOneRegion() {
        List<Region> regionList = getRegions(true);
        if (regionList.size() == 1) return regionList.getFirst();
        for (Region region : regionList) {
            if (region.isDefined()) return region;
        }
        return new Region();
    }

    /**
     * When the image has a fixed location, if
     * - a region is defined, it is returned
     * - no region is defined, a random search region is returned
     * @param fixed if the image has a fixed location
     * @return the search region
     */
    @JsonIgnore
    public Region getFixedIfDefinedOrRandomRegion(boolean fixed) {
        List<Region> regionList = getRegions(fixed);
        if (regionList.isEmpty()) return new Region();
        return SearchRegionsUtils.getRandomRegion(regionList);
    }

    /**
     * When the image has a fixed location, if
     * - a region is defined, it is returned
     * - no region is defined, all search regions are returned
     * @param fixed if the image has a fixed location
     * @return the search regions
     */
    public List<Region> getRegions(boolean fixed) {
        if (!fixed) return regions;
        // Only return fixed region if it's been explicitly set (not just default initialized)
        // fixedRegion is null when not set (to distinguish from default full-screen Region)
        if (fixedRegion != null && fixedRegion.isDefined()) return List.of(fixedRegion);
        return regions;
    }

    @JsonIgnore
    public void setSearchRegion(Region searchRegion) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegion);
    }

    /**
     * Returns all configured regions without adding any defaults.
     * This method returns exactly what has been configured, with no surprises.
     *
     * @return a list of all configured regions (may be empty)
     */
    @JsonIgnore
    public List<Region> getAllRegions() {
        return new ArrayList<>(regions);
    }
    
    /**
     * Returns regions for searching, adding a full-screen default if no regions are configured.
     * This is the method to use when actually performing searches.
     * 
     * @return a list of search regions, with full-screen default if empty
     */
    @JsonIgnore
    public List<Region> getRegionsForSearch() {
        // If fixed region is set, use only that
        if (fixedRegion != null && fixedRegion.isDefined()) {
            return List.of(fixedRegion);
        }
        // If regions are configured, use them
        if (!regions.isEmpty()) {
            return new ArrayList<>(regions);
        }
        // Only default to full screen when no regions are configured
        return List.of(new Region()); // full screen default
    }

    /**
     * Access regions in a way that's safe for serialization/deserialization.
     * @return A mutable list containing all regions
     */
    @JsonIgnore
    public List<Region> getRegionsMutable() {
        return SearchRegionsUtils.getMutableRegionsCopy(this);
    }

    public void addSearchRegions(Region... searchRegions) {
        addSearchRegions(Arrays.asList(searchRegions));
    }

    public void addSearchRegions(List<Region> searchRegions) {
        for (Region region : searchRegions) {
            if (region != null) {
                regions.addAll(trimRegion(region));
            }
        }
        regions = RegionUtils.mergeAdjacent(regions);
    }

    /**
     * @return true if fixed region or any search region is defined.
     */
    @JsonIgnore
    public boolean isAnyRegionDefined() {
        if (fixedRegion != null && fixedRegion.isDefined()) return true;
        for (Region region : regions) {
            if (region.isDefined()) return true;
        }
        return false;
    }

    /**
     * @param fixed does the pattern have a fixed position
     * @return true if fixed and has been found, or if not fixed and one of the search regions is defined
     */
    @JsonIgnore
    public boolean isDefined(boolean fixed) {
        if (fixedRegion != null && fixedRegion.isDefined()) return true;
        if (fixed) return false; // should be fixed but the region is not defined
        return isAnyRegionDefined();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return regions.isEmpty();
    }

    /**
     * Create a deep copy by manually copying each field.
     * Avoid calling SearchRegionsUtils to prevent circular reference.
     */
    @JsonIgnore
    public SearchRegions getDeepCopy() {
        SearchRegions copy = new SearchRegions();

        // Copy regular regions
        for (Region region : this.regions) {
            if (region != null) {
                copy.regions.add(new Region(region.x(), region.y(), region.w(), region.h()));
            }
        }

        // Copy fixed region if defined
        if (this.isFixedRegionSet()) {
            Region fixed = this.fixedRegion;
            copy.fixedRegion = new Region(fixed.x(), fixed.y(), fixed.w(), fixed.h());
        }

        return copy;
    }

    /**
     * Copy constructor for deep copying SearchRegions.
     * @param other the SearchRegions instance to copy
     */
    public SearchRegions(SearchRegions other) {
        this.regions = new ArrayList<>();
        for (Region region : other.regions) {
            this.regions.add(region != null ? new Region(region) : null);
        }
        this.fixedRegion = other.fixedRegion != null ? new Region(other.fixedRegion) : null;
    }

    /**
     * Newly added regions may overlap with existing regions. To make sure duplicated matches are not returned
     * due to overlapping regions, a newly added region should be trimmed if it overlaps with an existing region.
     */
    private List<Region> trimRegion(Region newRegion) {
        for (Region region : regions) {
            if (region.overlaps(newRegion)) return newRegion.minus(region);
        }
        // Use ArrayList instead of List.of for better serialization compatibility
        List<Region> result = new ArrayList<>();
        result.add(newRegion);
        return result;
    }

    /**
     * Use utility class to create a string representation
     */
    @Override
    public String toString() {
        return SearchRegionsUtils.toString(this);
    }

    /**
     * Merges this SearchRegions with another one
     * @param other The other SearchRegions to merge with
     * @return A new SearchRegions containing regions from both
     */
    public SearchRegions merge(SearchRegions other) {
        return SearchRegionsUtils.merge(this, other);
    }
}