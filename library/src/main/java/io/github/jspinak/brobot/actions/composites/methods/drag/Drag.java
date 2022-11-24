package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.OffsetOps;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Drags from an Image Match, Region, or Location to an Image Match, Region, or Location
 * Drag is a Composite Action composed of a Basic Action (Find) and a Sikuli Wrapper (Drag Location)
 */
@Component
public class Drag implements ActionInterface {

    private DragLocation dragLocation;
    private GetDragLocation getDragLocation;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;
    private OffsetOps offsetOps;

    public Drag(DragLocation dragLocation, GetDragLocation getDragLocation,
                GetSceneAnalysisCollection getSceneAnalysisCollection, OffsetOps offsetOps) {
        this.dragLocation = dragLocation;
        this.getDragLocation = getDragLocation;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.offsetOps = offsetOps;
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
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections), actionOptions);
        dragToMatches.setSceneAnalysisCollection(sceneAnalysisCollection);
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(actionOptions, objectCollections);
        Optional<Location> optEndLoc = getDragLocation.getToLocation(actionOptions, objectCollections);
        offsetOps.addOffset(List.of(objectCollections), dragToMatches, actionOptions);
        if (optStartLoc.isEmpty() || optEndLoc.isEmpty()) return dragToMatches;
        dragLocation.drag(optStartLoc.get(), optEndLoc.get(), actionOptions);
        dragToMatches.addDefinedRegion(new Region(
                optStartLoc.get().getX(), optStartLoc.get().getY(),
                optEndLoc.get().getX() - optStartLoc.get().getX(),
                optEndLoc.get().getY() - optStartLoc.get().getY()));
        return dragToMatches;
    }

}
