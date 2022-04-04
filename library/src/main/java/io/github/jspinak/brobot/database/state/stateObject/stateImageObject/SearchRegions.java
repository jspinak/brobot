package io.github.jspinak.brobot.database.state.stateObject.stateImageObject;

import io.github.jspinak.brobot.database.primitives.region.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SearchRegions allows for multiple Regions to be associated with an Image. This
 * could be useful when you are not sure where an Image may be but want to exclude
 * an area, or when the desired search area cannot be described by one rectangle.
 */
public class SearchRegions {

    List<Region> regions = new ArrayList<>();

    public Region getSearchRegion() {
        for (Region region : regions) {
            if (region.defined()) return region;
        }
        return new Region();
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
