package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Defines a Region as the smallest rectangle produced by Matches and Locations.
 * Matches can contain Anchors that specify the point to use.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineInsideAnchors implements ActionInterface {

    private final DefineHelper defineHelper;
    private final AnchorRegion anchorRegion;

    private DefinedBorders definedBorders;

    public DefineInsideAnchors(DefineHelper defineHelper, AnchorRegion anchorRegion) {
        this.defineHelper = defineHelper;
        this.anchorRegion = anchorRegion;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        Region region = new Region();
        definedBorders = new DefinedBorders();
        defineHelper.findMatches(matches, objectCollections);
        // The DefinedBorders object keeps track of defined borders as the region is being defined
        anchorRegion.fitRegionToAnchors(definedBorders, region, matches);
        defineHelper.adjust(region, actionOptions);
        if (definedBorders.allBordersDefined()) {
            matches.addDefinedRegion(region);
        } // else return an undefined region instead of a partially defined region
        matches.setOutputText(region.toString());
    }

}
