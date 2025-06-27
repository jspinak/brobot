package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;

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
     * As of version 1.7, ActionOptions search regions do not override fixed/defined regions
     * on the StateImage. This method implements the following priority order:
     * <ol>
     * <li>Fixed/defined regions from the StateImage (highest priority)</li>
     * <li>Search regions from ActionOptions</li>
     * <li>Search regions from the StateImage</li>
     * <li>Default full-screen region if no regions are found</li>
     * </ol>
     *
     * @param actionOptions The action configuration containing optional search regions
     * @param stateImage The image that may contain its own search regions
     * @return A non-empty list of regions to search within. Always contains at least
     *         one region (full screen) if no specific regions are defined.
     */
    public List<Region> getRegions(ActionOptions actionOptions, StateImage stateImage) {
        List<Region> definedFixed = stateImage.getDefinedFixedRegions();
        if (!definedFixed.isEmpty()) return definedFixed;
        List<Region> regions;
        if (!actionOptions.getSearchRegions().isEmpty())
            regions = actionOptions.getSearchRegions().getAllRegions();
        else regions = stateImage.getAllSearchRegions();
        if (regions.isEmpty()) regions.add(new Region());
        return regions;
    }

    /**
     * Selects search regions for a Pattern-based find operation.
     * <p>
     * As of version 1.7, ActionOptions search regions do not override a Pattern's fixed
     * search region. To modify a fixed region, use the Pattern's {@code reset()} method.
     * ActionOptions regions will replace standard search regions but not fixed regions.
     * 
     * @param actionOptions The action configuration containing optional search regions
     * @param pattern The pattern that may contain its own search regions (fixed or standard)
     * @return A non-empty list of regions to search within. Returns the Pattern's fixed
     *         region if set, otherwise follows the standard priority order.
     */
    public List<Region> getRegions(ActionOptions actionOptions, Pattern pattern) {
        List<Region> regions;
        if (pattern.getSearchRegions().isFixedRegionSet()) return pattern.getRegions();
        if (!actionOptions.getSearchRegions().isEmpty())
            regions = actionOptions.getSearchRegions().getAllRegions();
        else regions = pattern.getRegions();
        if (regions.isEmpty()) regions.add(new Region());
        return regions;
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
        if (actionOptions.getSearchRegions().isEmpty()) return List.of(new Region());
        return actionOptions.getSearchRegions().getAllRegions();
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
            return actionOptions.getSearchRegions().getAllRegions();
        for (ObjectCollection objColl : objectCollections) {
            for (StateImage stateImage : objColl.getStateImages()) {
                regions.addAll(stateImage.getAllSearchRegions());
            }
        }
        return regions;
    }
}
