package io.github.jspinak.brobot.action.internal.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;

/**
 * Bridge between legacy ActionConfig and modern region resolution. Provides methods to extract and
 * convert regions from ActionConfig configurations.
 */
@Component
public class ActionConfigRegionProvider {

    /** Get regions from ActionConfig for Find operations */
    public List<Region> getRegionsFromActionConfig(ActionConfig actionConfig) {
        if (actionConfig == null) {
            return Collections.emptyList();
        }

        List<Region> regions = new ArrayList<>();

        // Check if this is a PatternFindOptions which might have search regions
        if (actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            // PatternFindOptions has searchRegions field
            if (findOptions.getSearchRegions() != null) {
                regions.addAll(findOptions.getSearchRegions().getRegions(true));
            }
        }

        return regions;
    }

    /** Convert ActionConfig regions to StateRegion search regions */
    public List<StateRegion> convertToSearchRegions(ActionConfig actionConfig) {
        List<Region> regions = getRegionsFromActionConfig(actionConfig);
        List<StateRegion> stateRegions = new ArrayList<>();

        for (Region region : regions) {
            StateRegion stateRegion = new StateRegion();
            stateRegion.setSearchRegion(region);
            stateRegion.setName("ActionConfig.Find.Region");
            stateRegions.add(stateRegion);
        }

        return stateRegions;
    }

    /** Get regions specifically for pattern matching from ActionConfig */
    public List<Region> getPatternSearchRegions(ActionConfig actionConfig) {
        List<Region> regions = new ArrayList<>();

        if (actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null) {
                regions.addAll(findOptions.getSearchRegions().getRegions(true));
            }
        }

        // If no regions found, return default full-screen region
        if (regions.isEmpty()) {
            regions.add(new Region()); // Default full-screen region
        }

        return regions;
    }

    /** Get regions specifically for StateImage searches from ActionConfig */
    public List<Region> getStateImageSearchRegions(ActionConfig actionConfig) {
        // Always use getPatternSearchRegions which handles null and provides defaults
        return getPatternSearchRegions(actionConfig);
    }

    /** Check if ActionConfig has any search regions defined */
    public boolean hasSearchRegions(ActionConfig actionConfig) {
        if (actionConfig == null) {
            return false;
        }

        if (actionConfig instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) actionConfig;
            return findOptions.getSearchRegions() != null
                    && !findOptions.getSearchRegions().getRegions(true).isEmpty();
        }

        return false;
    }

    /** Merge regions from ActionConfig with existing regions */
    public List<Region> mergeWithExistingRegions(
            ActionConfig actionConfig, List<Region> existingRegions) {
        List<Region> actionConfigRegions = getRegionsFromActionConfig(actionConfig);

        if (actionConfigRegions.isEmpty()) {
            return existingRegions;
        }

        if (existingRegions == null || existingRegions.isEmpty()) {
            return actionConfigRegions;
        }

        // Merge both lists, avoiding duplicates
        List<Region> merged = new ArrayList<>(existingRegions);
        for (Region r : actionConfigRegions) {
            if (!containsRegion(merged, r)) {
                merged.add(r);
            }
        }

        return merged;
    }

    /** Check if a region list contains a specific region */
    private boolean containsRegion(List<Region> regions, Region region) {
        return regions.stream()
                .anyMatch(
                        r ->
                                r.x() == region.x()
                                        && r.y() == region.y()
                                        && r.w() == region.w()
                                        && r.h() == region.h());
    }
}
