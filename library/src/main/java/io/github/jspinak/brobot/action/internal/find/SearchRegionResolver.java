package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects appropriate search regions for find operations based on a priority hierarchy.
 * <p>
 * This component implements a sophisticated region selection strategy that respects
 * the precedence of different region sources. It ensures that the most specific and
 * intentional region definitions take priority over more general ones, while always
 * guaranteeing that at least one search region is available for the operation.
 * 
 * <p><b>Region Selection Priority (highest to lowest):</b></p>
 * <ol>
 * <li>Fixed/defined regions on the pattern or image</li>
 * <li>Search regions specified in ActionOptions</li>
 * <li>Search regions defined on the pattern or image</li>
 * <li>Default full-screen region (fallback)</li>
 * </ol>
 * 
 * @see ActionOptions
 * @see StateImage
 * @see Pattern
 * @see Region
 */
@Component
public class SearchRegionResolver {

    /**
     * Selects search regions for a StateImage-based find operation.
     * <p>
     * This method implements the following priority order:
     * <ol>
     * <li>Search regions from ActionOptions (highest priority)</li>
     * <li>Fixed regions from individual Patterns within the StateImage</li>
     * <li>Search regions from individual Patterns within the StateImage</li>
     * <li>Default full-screen region if no regions are found</li>
     * </ol>
     *
     * @param actionOptions The action configuration containing optional search regions
     * @param stateImage The image that may contain patterns with their own search regions
     * @return A non-empty list of regions to search within. Always contains at least
     *         one region (full screen) if no specific regions are defined.
     */
    public List<Region> getRegions(ActionOptions actionOptions, StateImage stateImage) {
        // Priority 1: Use ActionOptions search regions if specified
        if (!actionOptions.getSearchRegions().isEmpty()) {
            List<Region> regions = actionOptions.getSearchRegions().getRegionsForSearch();
            if (regions.size() == 1 && regions.get(0).w() == 1536 && regions.get(0).h() == 864) {
                // Full screen search - don't log unless it's interesting
            } else {
                ConsoleReporter.println("[REGIONS] Using " + regions.size() + " custom region(s) for '" + 
                    stateImage.getName() + "'");
            }
            return regions;
        }
        
        // Priority 2: Collect search regions from all patterns
        // Pattern.getRegionsForSearch() handles fixed regions and defaults internally
        List<Region> regions = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            regions.addAll(pattern.getRegionsForSearch());
        }
        
        // Only log if not full screen or if there are multiple regions
        if (regions.size() > 1 || (regions.size() == 1 && 
            (regions.get(0).w() != 1536 || regions.get(0).h() != 864))) {
            ConsoleReporter.println("[REGIONS] Using " + regions.size() + " region(s) for '" + 
                stateImage.getName() + "'");
        }
        
        return regions;
    }

    /**
     * Selects search regions for a Pattern-based find operation.
     * <p>
     * This method implements the following priority order:
     * <ol>
     * <li>Search regions from ActionOptions (highest priority)</li>
     * <li>Fixed regions from the Pattern</li>
     * <li>Search regions from the Pattern</li>
     * <li>Default full-screen region if no regions are found</li>
     * </ol>
     * 
     * @param actionOptions The action configuration containing optional search regions
     * @param pattern The pattern that may contain its own search regions (fixed or standard)
     * @return A non-empty list of regions to search within.
     */
    public List<Region> getRegions(ActionOptions actionOptions, Pattern pattern) {
        // Priority 1: Use ActionOptions search regions if specified
        if (!actionOptions.getSearchRegions().isEmpty()) {
            return actionOptions.getSearchRegions().getRegionsForSearch();
        }
        
        // Priority 2: Use Pattern's search regions (handles fixed regions and defaults internally)
        return pattern.getRegionsForSearch();
    }

    /**
     * Selects search regions when only ActionOptions are available.
     * <p>
     * This method is used when there are no pattern or image-specific regions to consider.
     * It ensures that at least one region is always returned for the search operation.
     * 
     * @param actionOptions The action configuration containing optional search regions
     * @return The regions specified in ActionOptions, or a default full-screen region
     *         if no regions are defined
     */
    public List<Region> getRegions(ActionOptions actionOptions) {
        return actionOptions.getSearchRegions().getRegionsForSearch();
    }

    /**
     * Collects search regions from multiple ObjectCollections.
     * <p>
     * This method aggregates all search regions from the provided collections of images.
     * If ActionOptions contains any defined regions, those take precedence over the
     * regions defined in the individual images.
     * 
     * @param actionOptions The action configuration that may override image regions
     * @param objectCollections Variable number of collections containing StateImages
     *                         with their own search regions
     * @return A list of all applicable search regions. If ActionOptions has defined regions,
     *         returns those; otherwise returns the union of all regions from all images
     *         in all collections.
     */
    public List<Region> getRegionsForAllImages(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        List<Region> regions = new ArrayList<>();
        if (actionOptions.getSearchRegions().isAnyRegionDefined())
            return actionOptions.getSearchRegions().getRegionsForSearch();
        for (ObjectCollection objColl : objectCollections) {
            for (StateImage stateImage : objColl.getStateImages()) {
                regions.addAll(stateImage.getAllSearchRegions());
            }
        }
        // Ensure at least one region is returned
        if (regions.isEmpty()) {
            regions.add(new Region());
        }
        return regions;
    }

    /**
     * Collects search regions for ActionConfig-based actions.
     * <p>
     * This method handles the new ActionConfig approach. For find-based configurations
     * that extend BaseFindOptions, it extracts search regions. For other action types,
     * it returns regions from the object collections.
     * 
     * @param actionConfig The action configuration
     * @param objectCollections Variable number of collections containing StateImages
     * @return A list of all applicable search regions
     */
    public List<Region> getRegionsForAllImages(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        List<Region> regions = new ArrayList<>();
        
        // Check if this is a find-based config with search regions
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null && findOptions.getSearchRegions().isAnyRegionDefined()) {
                return findOptions.getSearchRegions().getRegionsForSearch();
            }
        }
        
        // Otherwise, collect regions from object collections
        for (ObjectCollection objColl : objectCollections) {
            for (StateImage stateImage : objColl.getStateImages()) {
                regions.addAll(stateImage.getAllSearchRegions());
            }
        }
        
        // Ensure at least one region is returned
        if (regions.isEmpty()) {
            regions.add(new Region());
        }
        
        return regions;
    }
    
    /**
     * Selects search regions for ActionConfig-based find operations.
     * <p>
     * This method implements the following priority order:
     * <ol>
     * <li>Search regions from BaseFindOptions (highest priority)</li>
     * <li>Fixed regions from individual Patterns within the StateImage</li>
     * <li>Search regions from individual Patterns within the StateImage</li>
     * <li>Default full-screen region if no regions are found</li>
     * </ol>
     * 
     * @param actionConfig The action configuration (e.g., PatternFindOptions)
     * @param stateImage The image that may contain patterns with their own search regions
     * @return A non-empty list of regions to search within
     */
    public List<Region> getRegions(ActionConfig actionConfig, StateImage stateImage) {
        // Priority 1: Use BaseFindOptions search regions if specified
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null && !findOptions.getSearchRegions().isEmpty()) {
                return findOptions.getSearchRegions().getRegionsForSearch();
            }
        }
        
        // Priority 2: Collect search regions from all patterns
        // Pattern.getRegionsForSearch() handles fixed regions and defaults internally
        List<Region> regions = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            regions.addAll(pattern.getRegionsForSearch());
        }
        
        return regions;
    }
}
