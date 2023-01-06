package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.FindWrapperMethods;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
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

    /**
     * Creates MatchObjects and sets a new Snapshot for a single Image
     * @param actionOptions holds the action configuration.
     * @param stateImageObject the StateImageObject containing the Image in focus
     * @return a Matches object with all matches found.
     */
    @Override
    public Matches find(ActionOptions actionOptions, StateImageObject stateImageObject, Scene scene) {
        Matches matches = new Matches();
        for (Region region : selectRegions.getRegions(actionOptions, stateImageObject)) {
            matches.addAllResults(
                    findWrapperMethods.get(actionOptions.getFind()).find(
                    region, stateImageObject, actionOptions, scene));
            if (stopAfterFound(actionOptions, matches)) break;
        }
        return matches;
    }

}
