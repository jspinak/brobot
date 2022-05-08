package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.FindWrapperMethods;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

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
    private SelectRegions selectRegions;

    public FindImage(FindWrapperMethods findWrapperMethods,
                     SelectRegions selectRegions) {
        this.findWrapperMethods = findWrapperMethods;
        this.selectRegions = selectRegions;
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
        for (Region region : selectRegions.getRegions(actionOptions, stateImageObject, ripRegion)) {
            matches.addAll(
                    findWrapperMethods.get(actionOptions.getFind()).find(
                    region, stateImageObject, image, actionOptions));
            if (stopAfterFound(actionOptions, matches)) break;
        }
        return matches;
    }

}
