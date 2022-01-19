package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.FindWrapperMethods;
import io.github.jspinak.brobot.database.primitives.image.Image;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Find.FIRST and Find.ALL methods for Images.
 * Find.BEST and Find.EACH are meant for ObjectCollections and not for individual Images or RIPs.
 * This class and FindRIP comprise the layer of Find classes that work with specific Images. Since
 *   we use specific Images here, we also can set the MatchSnapshot. This is the right level of
 *   granularity to search for and add Strings, so GetTextWrapper is included here as a dependency.
 */
@Component
public class FindImage implements FindImageObject {

    private FindWrapperMethods findWrapperMethods;
    private AdjustMatches adjustMatches;

    public FindImage(FindWrapperMethods findWrapperMethods, AdjustMatches adjustMatches) {
        this.findWrapperMethods = findWrapperMethods;
        this.adjustMatches = adjustMatches;
    }

    public boolean stopAfterFound(ActionOptions actionOptions, Matches matches) {
        if (actionOptions.getFind() == ActionOptions.Find.ALL) return false;
        if (actionOptions.getDoOnEach() == ActionOptions.DoOnEach.BEST) return false;
        return !matches.isEmpty();
    }

    @Override
    public Matches find(ActionOptions actionOptions, StateImageObject stateImageObject) {
        return find(actionOptions, stateImageObject, stateImageObject.getImage());
    }

    /**
     * Creates MatchObjects and sets a new Snapshot for a single Image
     * ripRegion can be left out if not using an RIP
     */
    public Matches find(ActionOptions actionOptions, StateImageObject stateImageObject,
                        Image image, Region... ripRegion) {
        Matches matches = new Matches();
        for (Region region : getRegions(actionOptions, stateImageObject, ripRegion)) {
            matches.addAll(
                    findWrapperMethods.get(actionOptions.getFind()).find(
                    region, stateImageObject, image, actionOptions));
            if (stopAfterFound(actionOptions, matches)) break;
        }
        return matches;
    }

    /**
     * If the StateImageObject is using its RegionImagePairs, and the specific Image's Region
     * is defined (meaning it's been found already), then use that Region. Otherwise, in this order:
     * 1. If the ActionOptions has SearchRegions defined, use those Regions.
     * 2. Use the SearchRegions on the StateImageObject.
     */
    private List<Region> getRegions(ActionOptions actionOptions, StateImageObject stateImage,
                                    Region... ripRegion) {
        List<Region> regions = new ArrayList<>();
        if (ripRegion.length > 0 && ripRegion[0].defined()) {
            regions.add(ripRegion[0]);
            return regions; // This is an RIP and it's defined.
        }
        if (actionOptions.getSearchRegions().defined())
            return actionOptions.getSearchRegions().getAllRegions();
        return stateImage.getAllSearchRegions();
    }

}
