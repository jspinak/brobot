package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SelectRegions {

    /**
     * Since version 1.7, the SearchRegions set in the ActionOptions variable do not replace fixed and defined search regions.
     * Select regions by the following methods, in this order:
     * 1. If one of the patterns has a defined, fixed search regions, use that.
     * 2. If the ActionOptions has SearchRegions defined, use those Regions.
     * 3. Use the SearchRegions on the patterns.
     *
     * @param actionOptions holds the action configuration, which can contain search regions.
     * @param stateImage can also contain search regions.
     * @return a list of regions to use in an Action.
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
     * Since version 1.7, the SearchRegions set in the ActionOptions variable do not replace the fixed search region
     * of a fixed image. To replace the fixed search region, the Pattern's fixed region can be reset with the reset() method.
     * The ActionOptions SearchRegions replace the Pattern's SearchRegions but not its fixed search region if defined.
     * @param actionOptions holds the action configuration, which can contain search regions.
     * @param pattern can also contain search regions.
     * @return a list of regions to use in an Action.
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
     * If ActionOptions are the only place to find regions, just make sure there's at least one region.
     * @param actionOptions holds the action configuration, which can contain search regions.
     * @return the regions in ActionOptions or the active screen's region
     */
    public List<Region> getRegions(ActionOptions actionOptions) {
        if (actionOptions.getSearchRegions().isEmpty()) return List.of(new Region());
        return actionOptions.getSearchRegions().getAllRegions();
    }

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
