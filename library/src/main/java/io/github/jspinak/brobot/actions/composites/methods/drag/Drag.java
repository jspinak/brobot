package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Drags from an Image Match, Region, or Location to an Image Match, Region, or Location
 * Drag is a Composite Action composed of a Basic Action (Find) and a Sikuli Wrapper (Drag Location)
 */
@Component
public class Drag implements ActionInterface {

    private DragLocation dragLocation;
    private GetDragLocation getDragLocation;

    public Drag(DragLocation dragLocation, GetDragLocation getDragLocation) {
        this.dragLocation = dragLocation;
        this.getDragLocation = getDragLocation;
    }

    /**
     * The two Actions used are Find and Drag.
     * Find is used twice, once for the 'from' Match and once for the 'to' Match.
     *
     * @param actionOptions     has mostly options for Drag but also a few options for Find
     * @param objectCollections ObjectCollection #1 for the 'from' Match, and #2 for the 'to' Match.
     * @return Matches with the 'to' Match. The 'from' Match is not returned. It additionally
     * returns a DefinedRegion with x,y as the DragFrom Location and x2,y2 as the
     * DragTo Location.
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches dragToMatches = new Matches();
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(actionOptions, objectCollections);
        Optional<Location> optEndLoc = getDragLocation.getToLocation(actionOptions, objectCollections);
        if (optStartLoc.isEmpty() || optEndLoc.isEmpty()) return dragToMatches;
        dragLocation.drag(optStartLoc.get(), optEndLoc.get(), actionOptions);
        dragToMatches.addDefinedRegion(new Region(
                optStartLoc.get().getX(), optStartLoc.get().getY(),
                optEndLoc.get().getX() - optStartLoc.get().getX() + 1,
                optEndLoc.get().getY() - optStartLoc.get().getY() + 1));
        return dragToMatches;
    }

}
