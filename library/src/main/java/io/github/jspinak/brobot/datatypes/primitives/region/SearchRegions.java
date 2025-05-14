package io.github.jspinak.brobot.datatypes.primitives.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;

/**
 * SearchRegions allows for multiple Regions to be associated with an Image. This
 * could be useful when you are not sure where an Image may be but want to exclude
 * an area, or when the desired search area cannot be described by one rectangle.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRegions {

    private List<Region> regions = new ArrayList<>();
    /**
     * The fixed region is usually defined when an image with a fixed location is found for the first time.
     * This region is then used in future FIND operations with the associated image.
     */
    private Region fixedRegion = new Region();

    /**
     * If the fixed region has been set.
     * @return true if set.
     */
    @JsonIgnore
    public boolean isFixedRegionSet() {
        return fixedRegion.isDefined();
    }

    public void resetFixedRegion() {
        fixedRegion = new Region();
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
        if (fixedRegion.isDefined()) return List.of(fixedRegion);
        return regions;
    }

    @JsonIgnore
    public void setSearchRegion(Region searchRegion) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegion);
    }

    /**
     * Returns all regions, or a list with one empty region if no regions exist.
     * Using a mutable ArrayList instead of Collections.singletonList to avoid
     * serialization issues.
     *
     * @return a list of all regions, or a list with one empty region
     */
    @JsonIgnore
    public List<Region> getAllRegions() {
        if (!regions.isEmpty()) return regions;
        List<Region> list = new ArrayList<>();
        list.add(new Region());
        return list;
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
        if (fixedRegion.isDefined()) return true;
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
        if (fixedRegion.isDefined()) return true;
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