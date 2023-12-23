package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines a Region as the smallest rectangle produced by Matches and Locations.
 * Matches can contain Anchors that specify the point to use.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class DefineInsideAnchors implements ActionInterface {

    private DefineHelper defineHelper;
    private final AnchorRegion anchorRegion;

    private DefinedBorders definedBorders;

    public DefineInsideAnchors(DefineHelper defineHelper, AnchorRegion anchorRegion) {
        this.defineHelper = defineHelper;
        this.anchorRegion = anchorRegion;
    }

    public void perform(Matches matches, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Region region = new Region();
        definedBorders = new DefinedBorders();
        defineHelper.findMatches(matches, actionOptions, objectCollections);
        // The DefinedBorders object keeps track of defined borders as the region is being defined
        anchorRegion.fitRegionToAnchors(definedBorders, region, matches);
        //fitRegionToLocations(region, objectCollections[0].getStateLocations()); // locations are converted to Match objects in a Find operation
        defineHelper.adjust(region, actionOptions);
        if (definedBorders.allBordersDefined()) {
            matches.addDefinedRegion(region);
        } // else return an undefined region instead of a partially defined region
        matches.setOutputText(region.toString());
    }

}
