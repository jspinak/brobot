package io.github.jspinak.brobot.action.basic.region;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.capture.RegionDefinitionHelper;
import io.github.jspinak.brobot.action.internal.capture.RegionDefinitionHelperV2;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

/**
 * Defines regions based on the position and dimensions of found matches.
 * 
 * <p>This class implements multiple region definition strategies relative to matches,
 * supporting the creation of regions that are positioned around, above, below, or
 * to the sides of found GUI elements. It's a key component in the Brobot framework's
 * model-based automation approach, allowing dynamic region creation based on the
 * current state of the GUI.</p>
 * 
 * <p>The class handles five different positioning strategies:
 * <ul>
 *   <li>{@link DefineRegionOptions.DefineAs#MATCH} - Region identical to the match bounds</li>
 *   <li>{@link DefineRegionOptions.DefineAs#BELOW_MATCH} - Region of same size positioned below</li>
 *   <li>{@link DefineRegionOptions.DefineAs#ABOVE_MATCH} - Region of same size positioned above</li>
 *   <li>{@link DefineRegionOptions.DefineAs#LEFT_OF_MATCH} - Region of same size positioned to the left</li>
 *   <li>{@link DefineRegionOptions.DefineAs#RIGHT_OF_MATCH} - Region of same size positioned to the right</li>
 * </ul>
 * </p>
 * 
 * <p>For directional positioning (above, below, left, right), the new region maintains
 * the same dimensions as the original match but is offset by the match's width or height
 * in the specified direction.</p>
 * 
 * @see DefineRegion
 * @see RegionDefinitionHelper
 * @see DefineRegionOptions.DefineAs
 */
@Component
public class DefineWithMatch implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.DEFINE;
    }

    private final RegionDefinitionHelper defineHelper;
    private final RegionDefinitionHelperV2 defineHelperV2;

    public DefineWithMatch(RegionDefinitionHelper defineHelper, RegionDefinitionHelperV2 defineHelperV2) {
        this.defineHelper = defineHelper;
        this.defineHelperV2 = defineHelperV2;
    }

    /**
     * Creates a region based on the best match found and the specified positioning strategy.
     * 
     * <p>This method follows these steps:
     * <ol>
     *   <li>Uses DefineHelper to find matches in the provided object collections</li>
     *   <li>Returns early if no matches are found</li>
     *   <li>Creates a region from the best match</li>
     *   <li>Adjusts the region position based on the DefineAs strategy</li>
     *   <li>Applies any additional adjustments from DefineRegionOptions</li>
     *   <li>Adds the defined region to the ActionResult</li>
     * </ol>
     * </p>
     * 
     * <p>The positioning adjustments work as follows:
     * <ul>
     *   <li>BELOW_MATCH: Moves the region down by its height</li>
     *   <li>ABOVE_MATCH: Moves the region up by its height</li>
     *   <li>LEFT_OF_MATCH: Moves the region left by its width</li>
     *   <li>RIGHT_OF_MATCH: Moves the region right by its width</li>
     *   <li>MATCH: No position adjustment (default)</li>
     * </ul>
     * </p>
     * 
     * @param matches The ActionResult that contains the DefineRegionOptions and to which
     *                the defined region will be added. This object is mutated by
     *                the method. If no match is found, no region is added.
     * @param objectCollections The collections containing the objects to find. The best
     *                          match from these collections determines the base position
     *                          and size of the defined region.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration
        ActionConfig config = matches.getActionConfig();
        DefineRegionOptions defineOptions = null;
        if (config instanceof DefineRegionOptions) {
            defineOptions = (DefineRegionOptions) config;
        }
        
        defineHelper.findMatches(matches, objectCollections);
        if (matches.getBestMatch().isEmpty()) return;
        
        Region region = new Region(matches.getBestMatch().get());
        
        // Apply positioning based on DefineAs strategy
        if (defineOptions != null) {
            DefineRegionOptions.DefineAs defineAs = defineOptions.getDefineAs();
            if (defineAs == DefineRegionOptions.DefineAs.BELOW_MATCH) region.setY(region.y() + region.h());
            if (defineAs == DefineRegionOptions.DefineAs.ABOVE_MATCH) region.setY(region.y() - region.h());
            if (defineAs == DefineRegionOptions.DefineAs.LEFT_OF_MATCH) region.setX(region.x() - region.w());
            if (defineAs == DefineRegionOptions.DefineAs.RIGHT_OF_MATCH) region.setX(region.x() + region.w());
        }
        
        // Apply adjustments using V2 helper if we have DefineRegionOptions
        if (defineOptions != null) {
            defineHelperV2.adjust(region, defineOptions);
        }
        
        matches.addDefinedRegion(region);
    }
}
