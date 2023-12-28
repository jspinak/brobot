package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.OffsetOps;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Drags from an Image Match, Region, or Location to an Image Match, Region, or Location
 * Drag is a Composite Action composed of a Basic Action (Find) and a Sikuli Wrapper (Drag Location)
 */
@Component
public class Drag implements ActionInterface {

    private final DragLocation dragLocation;
    private final GetDragLocation getDragLocation;
    private final OffsetOps offsetOps;
    private final MatchesInitializer matchesInitializer;

    public Drag(DragLocation dragLocation, GetDragLocation getDragLocation, OffsetOps offsetOps,
                MatchesInitializer matchesInitializer) {
        this.dragLocation = dragLocation;
        this.getDragLocation = getDragLocation;
        this.offsetOps = offsetOps;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * The two Actions used are Find and Drag.
     * Find is used twice, once for the 'from' Match and once for the 'to' Match.
     * Matches with the 'to' Match. The 'from' Match is not returned. It additionally
     *   returns a DefinedRegion with x,y as the DragFrom Location and x2,y2 as the
     *   DragTo Location.
     *
     * @param matches has mostly options for Drag but also a few options for Find
     * @param objectCollections ObjectCollection #1 for the 'from' Match, and #2 for the 'to' Match.
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        matches = matchesInitializer.init(actionOptions, objectCollections);
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(matches, objectCollections);
        Optional<Location> optEndLoc = getDragLocation.getToLocation(matches, objectCollections);
        offsetOps.addOffset(List.of(objectCollections), matches, actionOptions);
        if (optStartLoc.isEmpty() || optEndLoc.isEmpty()) return;
        dragLocation.drag(optStartLoc.get(), optEndLoc.get(), actionOptions);
        matches.addDefinedRegion(new Region(
                optStartLoc.get().getX(), optStartLoc.get().getY(),
                optEndLoc.get().getX() - optStartLoc.get().getX(),
                optEndLoc.get().getY() - optStartLoc.get().getY()));
    }

}
