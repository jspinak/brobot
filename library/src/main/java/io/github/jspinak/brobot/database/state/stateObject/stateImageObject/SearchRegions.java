package io.github.jspinak.brobot.database.state.stateObject.stateImageObject;

import io.github.jspinak.brobot.database.primitives.region.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SearchRegions allows for multiple Regions to be associated with an Image. This
 * could be useful when you are not sure where an Image may be but want to exclude
 * an area, or when the desired search area cannot be described by one rectangle.
 */
public class SearchRegions {

    List<Region> regions = new ArrayList<>();
    {
        regions.add(new Region());  // initialize with an undefined region (representing the screen)
    }

    public Region getSearchRegion() {
        for (Region region : regions) {
            if (region.defined()) return region;
        }
        return regions.get(0);
    }

    public void setSearchRegions(List<Region> searchRegions) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegions);
        if (regions.isEmpty()) regions.add(new Region()); // there should be at least one Region
    }

    public void setSearchRegion(Region searchRegion) {
        regions = new ArrayList<>();
        addSearchRegions(searchRegion);
    }

    public List<Region> getAllRegions() {
        return regions;
    }

    public void addSearchRegions(Region... searchRegions) {
        regions.addAll(Arrays.asList(searchRegions));
    }

    public void addSearchRegions(List<Region> searchRegions) {
        regions.addAll(searchRegions);
    }

    public boolean defined() {
        for (Region region : regions) {
            if (region.defined()) return true;
        }
        return false;
    }

    public SearchRegions getDeepCopy() {
        SearchRegions searchRegions = new SearchRegions();
        regions.forEach(reg -> searchRegions.addSearchRegions(
                new Region(reg.getX(), reg.getY(), reg.getW(), reg.getH())));
        return searchRegions;
    }
}
