package io.github.jspinak.brobot.action.internal.find;

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
 * @see ActionConfig
 * @see StateImage
 * @see Pattern
 * @see Region
 */
@Component
public class SearchRegionResolver {
    
    // Track last logged message to prevent consecutive duplicates
    private String lastLoggedMessage = "";
    private int suppressedCount = 0;
    
    /**
     * Logs a message while preventing consecutive duplicates.
     * If the same message is repeated, it increments a counter instead of logging again.
     * When a different message comes in, it logs any suppressed count and the new message.
     */
    private synchronized void logWithDeduplication(String message) {
        if (message.equals(lastLoggedMessage)) {
            suppressedCount++;
            return;
        }
        
        // If we had suppressed messages, log the count
        if (suppressedCount > 0) {
            ConsoleReporter.println("    [Previous message repeated " + suppressedCount + " more time(s)]");
            suppressedCount = 0;
        }
        
        // Log the new message
        ConsoleReporter.println(message);
        lastLoggedMessage = message;
    }

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
     * @param actionConfig The action configuration containing optional search regions
     * @param stateImage The image that may contain patterns with their own search regions
     * @return A non-empty list of regions to search within. Always contains at least
     *         one region (full screen) if no specific regions are defined.
     */
    public List<Region> getRegions(ActionConfig actionConfig, StateImage stateImage) {
        // Priority 1: Use BaseFindOptions search regions if specified
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null && !findOptions.getSearchRegions().isEmpty()) {
                List<Region> regions = findOptions.getSearchRegions().getRegionsForSearch();
                if (regions.size() == 1 && regions.get(0).w() == 1536 && regions.get(0).h() == 864) {
                    // Full screen search - don't log unless it's interesting
                } else {
                    logWithDeduplication("[REGIONS] Using " + regions.size() + " custom region(s) for '" + 
                        stateImage.getName() + "'");
                }
                return regions;
            }
        }
        
        // Priority 2: Collect search regions from all patterns
        // Pattern.getRegionsForSearch() handles fixed regions and defaults internally
        List<Region> regions = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            regions.addAll(pattern.getRegionsForSearch());
        }
        
        // If no regions found, use default full-screen region
        if (regions.isEmpty()) {
            regions.add(new Region());
        }
        
        // Only log if not full screen or if there are multiple regions
        if (regions.size() > 1 || (regions.size() == 1 && 
            (regions.get(0).w() != 1536 || regions.get(0).h() != 864))) {
            logWithDeduplication("[REGIONS] Using " + regions.size() + " region(s) for '" + 
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
     * @param actionConfig The action configuration containing optional search regions
     * @param pattern The pattern that may contain its own search regions (fixed or standard)
     * @return A non-empty list of regions to search within.
     */
    public List<Region> getRegions(ActionConfig actionConfig, Pattern pattern) {
        // Priority 1: Use BaseFindOptions search regions if specified
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null && !findOptions.getSearchRegions().isEmpty()) {
                return findOptions.getSearchRegions().getRegionsForSearch();
            }
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
     * @param actionConfig The action configuration containing optional search regions
     * @return The regions specified in ActionOptions, or a default full-screen region
     *         if no regions are defined
     */
    public List<Region> getRegions(ActionConfig actionConfig) {
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null) {
                return findOptions.getSearchRegions().getRegionsForSearch();
            }
        }
        return List.of(new Region());
    }

    /**
     * Collects search regions from multiple ObjectCollections.
     * <p>
     * This method aggregates all search regions from the provided collections of images.
     * If ActionOptions contains any defined regions, those take precedence over the
     * regions defined in the individual images.
     * 
     * @param actionConfig The action configuration that may override image regions
     * @param objectCollections Variable number of collections containing StateImages
     *                         with their own search regions
     * @return A list of all applicable search regions. If ActionOptions has defined regions,
     *         returns those; otherwise returns the union of all regions from all images
     *         in all collections.
     */
    public List<Region> getRegionsForAllImages(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        List<Region> regions = new ArrayList<>();
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            if (findOptions.getSearchRegions() != null && findOptions.getSearchRegions().isAnyRegionDefined()) {
                return findOptions.getSearchRegions().getRegionsForSearch();
            }
        }
        for (ObjectCollection objColl : objectCollections) {
            // Add regions from StateImages
            for (StateImage stateImage : objColl.getStateImages()) {
                regions.addAll(stateImage.getAllSearchRegions());
            }
            // Also add direct regions from the ObjectCollection
            if (objColl.getStateRegions() != null) {
                for (var stateRegion : objColl.getStateRegions()) {
                    regions.add(stateRegion.getSearchRegion());
                }
            }
        }
        // Ensure at least one region is returned
        if (regions.isEmpty()) {
            regions.add(new Region());
        }
        return regions;
    }

}
