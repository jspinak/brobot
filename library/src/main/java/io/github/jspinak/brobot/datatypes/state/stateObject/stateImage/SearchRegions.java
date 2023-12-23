package io.github.jspinak.brobot.datatypes.state.stateObject.stateImage;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;

import java.util.*;

/**
 * SearchRegions allows for multiple Regions to be associated with an Image. This
 * could be useful when you are not sure where an Image may be but want to exclude
 * an area, or when the desired search area cannot be described by one rectangle.
 */
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
    public boolean isFixedRegionSet() {
        return fixedRegion.defined();
    }

    public void setFixedRegion(Region region) {
        fixedRegion = region;
    }

    public void resetFixedRegion() {
        fixedRegion = new Region();
    }

    public Region getSearchRegion() {
        for (Region region : regions) {
            if (region.defined()) return region;
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
    public Region getRegion(boolean fixed) {
        List<Region> regions = getRegions(fixed);
        Random rand = new Random(regions.size());
        return regions.get(rand.nextInt());
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
        if (fixedRegion.defined()) return List.of(fixedRegion);
        return regions;
    }

    public void setSearchRegions(List<Region> searchRegions) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegions);
    }

    public void setSearchRegion(Region searchRegion) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegion);
    }

    public List<Region> getAllRegions() {
        if (!regions.isEmpty()) return regions;
        return Collections.singletonList(new Region());
    }

    public void addSearchRegions(Region... searchRegions) {
        addSearchRegions(List.of(searchRegions));
    }

    public void addSearchRegions(List<Region> searchRegions) {
        for (Region region : searchRegions) {
            regions.addAll(trimRegion(region));
        }
        regions = Region.mergeAdjacent(regions);
    }

    public boolean defined() {
        for (Region region : regions) {
            if (region.defined()) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return regions.isEmpty();
    }

    public SearchRegions getDeepCopy() {
        SearchRegions searchRegions = new SearchRegions();
        regions.forEach(reg -> searchRegions.addSearchRegions(
                new Region(reg.getX(), reg.getY(), reg.getW(), reg.getH())));
        return searchRegions;
    }

    /**
     * Newly added regions may overlap with existing regions. To make sure duplicated matches are not returned
     * due to overlapping regions, a newly added region should be trimmed if it overlaps with an existing region.
     */
    private List<Region> trimRegion(Region newRegion) {
        for (Region region : regions) {
            if (region.overlaps(newRegion)) return newRegion.minus(region);
        }
        return List.of(newRegion);
    }
}
