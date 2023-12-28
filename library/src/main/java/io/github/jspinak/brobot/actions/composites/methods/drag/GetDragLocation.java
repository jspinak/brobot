package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.bytedeco.opencv.opencv_core.Mat;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * Returns the 'from' and 'to' Locations to be used in Drag.
 */
@Component
public class GetDragLocation {

    private final Find find;
    private final ActionOptionsForDrag actionOptionsForDrag;

    public GetDragLocation(Find find, ActionOptionsForDrag actionOptionsForDrag) {
        this.find = find;
        this.actionOptionsForDrag = actionOptionsForDrag;
    }

    public Optional<Location> getFromLocation(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions dragActionOptions = actionOptionsForDrag.getFindFrom(matches.getActionOptions());
        Matches matchesForDrag = new Matches(dragActionOptions);
        find.perform(matchesForDrag, getDragFromObjColl(objectCollections));
        return matches.getBestLocation();
    }

    public Optional<Location> getToLocation(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions dragActionOptions = actionOptionsForDrag.getFindTo(matches.getActionOptions());
        Matches matchesForDrag = new Matches(dragActionOptions);
        find.perform(matchesForDrag, getDragToObjColl(objectCollections));
        return matches.getBestLocation();
    }

    /**
     * Use...
     * ObjectCollection #2 when there are at least 2 ObjectCollections (only 1 and 2 are used)
     * ObjectCollection #1 when there is only 1 ObjectCollection
     * When there are no ObjectCollections, use...
     * The current mouse position when the 'to' offsets do not equal the 'from' offsets
     * An empty ObjectCollection when there are no ObjectCollections and the offsets are the same.
     *   In this case nothing will happen because the 'from' Location is the same as the 'to' Location.
     *
     * @param objectCollections contain Images, Regions, and Locations used to find the Drag Locations
     * @return an ObjectCollection to be used in Find operations
     */
    private ObjectCollection getDragToObjColl(ObjectCollection... objectCollections) {
        if (objectCollections.length >= 2) return objectCollections[1];
        if (objectCollections.length == 1) return objectCollections[0];
        return new ObjectCollection.Builder().build();
    }

    /**
     * The 'from' Location is either in the first ObjectCollection,
     * or when there are no ObjectCollections, the current mouse position.
     */
    private ObjectCollection getDragFromObjColl(ObjectCollection... objectCollections) {
        if (objectCollections.length >= 1) return objectCollections[0];
        return new ObjectCollection.Builder()
                .withLocations(new Location(Mouse.at()))
                .build();
    }

}
