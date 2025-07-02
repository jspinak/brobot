package io.github.jspinak.brobot.action.internal.capture;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.region.DefineRegionOptions;
import io.github.jspinak.brobot.action.basic.region.DefineIncludingMatches;
import io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineOutsideAnchors;
import io.github.jspinak.brobot.action.basic.region.DefineRegion;
import io.github.jspinak.brobot.action.basic.region.DefineWithMatch;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

/**
 * Provides reusable helper methods for the various Define actions in the capture package.
 * 
 * <p>This class centralizes common logic required by different 'Define' operations,
 * primarily for adjusting a {@link Region} based on {@link ActionOptions} and for
 * finding initial matches that define the boundaries of a new region. It ensures
 * consistent behavior across all region definition strategies.</p>
 * 
 * <p>The class serves two main purposes:
 * <ul>
 *   <li>Finding matches while isolating Define-specific options from Find operations</li>
 *   <li>Applying consistent adjustments to regions based on ActionOptions</li>
 * </ul>
 * </p>
 * 
 * @see DefineRegion
 * @see DefineWithMatch
 * @see DefineInsideAnchors
 * @see DefineOutsideAnchors
 * @see DefineIncludingMatches
 */
@Component
public class RegionDefinitionHelper {

    private Find find;
    private final ActionResultFactory matchesInitializer;

    public RegionDefinitionHelper(Find find, ActionResultFactory matchesInitializer) {
        this.find = find;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Adjusts the position and dimensions of a Region object in-place.
     * 
     * <p>The adjustments are based on the additive (addX, addY, addW, addH) and absolute
     * (absoluteW, absoluteH) values specified in the ActionOptions. Absolute dimensions
     * take precedence over additive adjustments when specified (value >= 0).</p>
     * 
     * <p>This method modifies the region parameter directly. The adjustments are applied
     * in the following order:
     * <ol>
     *   <li>Position adjustments (addX, addY) are always applied</li>
     *   <li>Width is set to absoluteW if >= 0, otherwise increased by addW</li>
     *   <li>Height is set to absoluteH if >= 0, otherwise increased by addH</li>
     * </ol>
     * </p>
     * 
     * @param region The Region object to modify. Its state will be changed by this method.
     * @param actionOptions The configuration object containing the adjustment values:
     *                      addX, addY (position offsets),
     *                      addW, addH (dimension increases),
     *                      absoluteW, absoluteH (absolute dimension overrides).
     * @deprecated Use {@link #adjust(Region, DefineRegionOptions)} instead
     */
    @Deprecated
    public void adjust(Region region, ActionOptions actionOptions) {
        region.setX(region.x() + actionOptions.getAddX());
        region.setY(region.y() + actionOptions.getAddY());
        if (actionOptions.getAbsoluteW() >= 0) region.setW(actionOptions.getAbsoluteW());
        else region.setW(region.w() + actionOptions.getAddW());
        if (actionOptions.getAbsoluteH() >= 0) region.setH(actionOptions.getAbsoluteH());
        else region.setH(region.h() + actionOptions.getAddH());
    }
    
    /**
     * Adjusts the position and dimensions of a Region object in-place using DefineRegionOptions.
     * 
     * <p>The adjustments are based on the match adjustment options specified in the
     * DefineRegionOptions. Absolute dimensions take precedence over additive adjustments
     * when specified (value >= 0).</p>
     * 
     * <p>This method modifies the region parameter directly. The adjustments are applied
     * in the following order:
     * <ol>
     *   <li>Position adjustments (addX, addY) are always applied</li>
     *   <li>Width is set to absoluteW if >= 0, otherwise increased by addW</li>
     *   <li>Height is set to absoluteH if >= 0, otherwise increased by addH</li>
     * </ol>
     * </p>
     * 
     * @param region The Region object to modify. Its state will be changed by this method.
     * @param defineOptions The configuration object containing the adjustment values.
     */
    public void adjust(Region region, DefineRegionOptions defineOptions) {
        if (defineOptions.getMatchAdjustmentOptions() == null) return;
        
        region.setX(region.x() + defineOptions.getMatchAdjustmentOptions().getAddX());
        region.setY(region.y() + defineOptions.getMatchAdjustmentOptions().getAddY());
        
        if (defineOptions.getMatchAdjustmentOptions().getAbsoluteW() >= 0) {
            region.setW(defineOptions.getMatchAdjustmentOptions().getAbsoluteW());
        } else {
            region.setW(region.w() + defineOptions.getMatchAdjustmentOptions().getAddW());
        }
        
        if (defineOptions.getMatchAdjustmentOptions().getAbsoluteH() >= 0) {
            region.setH(defineOptions.getMatchAdjustmentOptions().getAbsoluteH());
        } else {
            region.setH(region.h() + defineOptions.getMatchAdjustmentOptions().getAddH());
        }
    }

    /**
     * Finds all matches for the given ObjectCollections and adds them to the ActionResult.
     * 
     * <p>This method creates a temporary, modified copy of the initial {@link ActionOptions}
     * to ensure that settings specific to the 'Define' operation (like addX, addW, or absoluteW)
     * do not improperly affect the 'Find' operation used here. It resets these Define-specific
     * values while preserving other find-related options like similarity settings.</p>
     * 
     * <p>The isolation of Define-specific options is crucial because:
     * <ul>
     *   <li>Define adjustments (addX, addW, etc.) are meant for the final region, not for finding matches</li>
     *   <li>Applying these adjustments during Find would offset all matches incorrectly</li>
     *   <li>Find-specific options (minSimilarity, searchRegion, etc.) must be preserved</li>
     * </ul>
     * </p>
     * 
     * <p>This method is configured to use {@link ActionOptions.Find#EACH}, which returns one Match
     * per object in the provided collections, ensuring all potential anchors are found.</p>
     * 
     * @param matches The ActionResult object that contains the initial ActionOptions and
     *                to which the resulting Match objects will be added. This object is
     *                mutated by the method.
     * @param objectCollections The collections of objects to find on the screen. Only objects
     *                          from the first collection are searched when using Find.EACH.
     */
    public void findMatches(ActionResult matches, ObjectCollection... objectCollections) {
        // Check if we have ActionConfig (new) or ActionOptions (legacy)
        ActionConfig config = matches.getActionConfig();
        if (config instanceof DefineRegionOptions) {
            findMatchesWithConfig(matches, (DefineRegionOptions) config, objectCollections);
        } else {
            // Legacy path
            findMatchesWithActionOptions(matches, objectCollections);
        }
    }
    
    private void findMatchesWithConfig(ActionResult matches, DefineRegionOptions defineOptions, ObjectCollection... objectCollections) {
        // Create PatternFindOptions for finding matches
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .build();
                
        ActionResult findMatches = matchesInitializer.init(findOptions, "findMatches", objectCollections);
        find.perform(findMatches, objectCollections);
        matches.addMatchObjects(findMatches);
    }
    
    private void findMatchesWithActionOptions(ActionResult matches, ObjectCollection... objectCollections) {
        // For legacy support, create default ActionOptions
        ActionOptions baseOptions = new ActionOptions();
        ActionOptions findOptions = new ActionOptions.Builder(baseOptions).build();
        findOptions.setFind(ActionOptions.Find.EACH);
        findOptions.setAddH(0);
        findOptions.setAddW(0);
        findOptions.setAddY(0);
        findOptions.setAddX(0);
        findOptions.setAbsoluteH(-1);
        findOptions.setAbsoluteW(-1);
        ActionResult findMatches = matchesInitializer.init(findOptions, objectCollections);
        find.perform(findMatches, objectCollections);
        matches.addMatchObjects(findMatches);
    }

}
