package io.github.jspinak.brobot.action.internal.capture;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.region.DefineIncludingMatches;
import io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineOutsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineRegion;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.region.DefineWithMatch;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.model.element.Region;

/**
 * V2 version that provides reusable helper methods for the various Define actions.
 *
 * <p>This V2 class works with ActionConfig instead of ActionConfig. It centralizes common logic
 * required by different 'Define' operations, primarily for adjusting a {@link Region} based on
 * {@link MatchAdjustmentOptions} and for finding initial matches that define the boundaries of a
 * new region. It ensures consistent behavior across all region definition strategies.
 *
 * <p>The class serves two main purposes:
 *
 * <ul>
 *   <li>Finding matches while isolating Define-specific options from Find operations
 *   <li>Applying consistent adjustments to regions based on MatchAdjustmentOptions
 * </ul>
 *
 * @see DefineRegion
 * @see DefineWithMatch
 * @see DefineInsideAnchors
 * @see DefineOutsideAnchors
 * @see DefineIncludingMatches
 * @see DefineRegionOptions
 * @see MatchAdjustmentOptions
 */
@Component
public class RegionDefiner {

    private final Find find;
    private final ActionResultFactory matchesInitializer;

    // No longer needs legacy dependency - merged implementation

    public RegionDefiner(Find find, ActionResultFactory matchesInitializer) {
        this.find = find;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Adjusts the position and dimensions of a Region object in-place using MatchAdjustmentOptions.
     *
     * <p>The adjustments are based on the additive (addX, addY, addW, addH) and absolute
     * (absoluteW, absoluteH) values specified in the MatchAdjustmentOptions. Absolute dimensions
     * take precedence over additive adjustments when specified (value >= 0).
     *
     * <p>This method modifies the region parameter directly. The adjustments are applied in the
     * following order:
     *
     * <ol>
     *   <li>Position adjustments (addX, addY) are always applied
     *   <li>Width is set to absoluteW if >= 0, otherwise increased by addW
     *   <li>Height is set to absoluteH if >= 0, otherwise increased by addH
     * </ol>
     *
     * @param region The Region object to modify. Its state will be changed by this method.
     * @param adjustmentOptions The configuration object containing the adjustment values: addX,
     *     addY (position offsets), addW, addH (dimension increases), absoluteW, absoluteH (absolute
     *     dimension overrides).
     */
    public void adjust(Region region, MatchAdjustmentOptions adjustmentOptions) {
        if (adjustmentOptions == null) {
            return;
        }

        // Apply position adjustments
        region.setX(region.x() + adjustmentOptions.getAddX());
        region.setY(region.y() + adjustmentOptions.getAddY());

        // Apply width adjustments
        if (adjustmentOptions.getAbsoluteW() >= 0) {
            region.setW(adjustmentOptions.getAbsoluteW());
        } else {
            region.setW(region.w() + adjustmentOptions.getAddW());
        }

        // Apply height adjustments
        if (adjustmentOptions.getAbsoluteH() >= 0) {
            region.setH(adjustmentOptions.getAbsoluteH());
        } else {
            region.setH(region.h() + adjustmentOptions.getAddH());
        }
    }

    /**
     * Adjusts the position and dimensions of a Region object using DefineRegionOptions.
     *
     * <p>This is a convenience method that extracts MatchAdjustmentOptions from DefineRegionOptions
     * and delegates to the main adjust method.
     *
     * @param region The Region object to modify.
     * @param defineOptions The DefineRegionOptions containing adjustment settings.
     */
    public void adjust(Region region, DefineRegionOptions defineOptions) {
        if (defineOptions != null && defineOptions.getMatchAdjustmentOptions() != null) {
            adjust(region, defineOptions.getMatchAdjustmentOptions());
        }
    }

    /**
     * Finds all matches for the given ObjectCollections and adds them to the ActionResult.
     *
     * <p>This method ensures that settings specific to the 'Define' operation do not improperly
     * affect the 'Find' operation used here. It preserves find-related options while isolating
     * define-specific adjustments.
     *
     * <p>For now, this delegates to the legacy implementation as ActionResult still uses
     * ActionConfig internally.
     *
     * @param matches The ActionResult object that contains the configuration and to which the
     *     resulting Match objects will be added.
     * @param objectCollections The collections of objects to find on the screen.
     */
    public void findMatches(ActionResult matches, ObjectCollection... objectCollections) {
        // Check if we have DefineRegionOptions or generic ActionConfig
        ActionConfig config = matches.getActionConfig();

        // Create PatternFindOptions for finding matches
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.EACH)
                        .build();

        ActionResult findMatches =
                matchesInitializer.init(findOptions, "findMatches", objectCollections);
        find.perform(findMatches, objectCollections);
        matches.addMatchObjects(findMatches);
    }
}
