package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * When building the state structure with automation, Brobot will move around the application in real time.
 * This class provides convenience functions for defining the application boundaries.
 */
@Component
public class GetUsableArea {

    private final Action action;

    public GetUsableArea(Action action) {
        this.action = action;
    }

    /**
     * The DEFINE operation uses a Find.EACH for objects, so objects can be placed in a single
     * ObjectCollection. The bottom-left point of the topleft match and the top-right point of the
     * bottomright match will be used to define the usable area region.
     * Images need to be StateImageObject(s) because only StateObject(s) have anchors. Anchors are added
     * to the StateImageObject(s).
     * @param topleft sets the top left boundary of the usable area.
     * @param bottomright sets the bottom right boundary of the usable area.
     * @return the usable area.
     */
    public Region getBoundariesFromExcludedImages(Image topleft, Image bottomright) {
        ObjectCollection exteriorImages = new ObjectCollection.Builder()
                .withImages(
                        new StateImageObject.Builder()
                                .withImages(topleft)
                                .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                                .build(),
                        new StateImageObject.Builder()
                                .withImages(bottomright)
                                .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
                                .build())
                .build();
        ActionOptions define = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        return action.perform(define, exteriorImages).getDefinedRegion();
    }

}
