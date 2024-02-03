package io.github.jspinak.brobot.actions.customActions;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.composites.methods.drag.Drag;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines some typical configurations for Drag and
 * allows for multiple drag operations.
 */
@Component
public class CommonDrag {

    private final Drag drag;
    private final ActionOptions actionOptions = new ActionOptions.Builder()
            .setPauseAfterEnd(.3)
            .setPauseBeforeMouseDown(.3)
            .setPauseAfterMouseDown(.5)
            .build();

    public CommonDrag(Drag drag) {
        this.drag = drag;
    }

    /*
     * Allows for dragging to multiple points by performing multiple Drag Actions.
     * This could be converted into a CompositeAction (Action == DRAGS).
     */
    public void drag(Matches matches, StateImage from, Location... to) {
        for (ObjectCollection[] oC : getObjectCollections(from, to)) {
            drag.perform(matches, oC);
            if (matches.isEmpty()) break;
        }
    }

    private List<ObjectCollection[]> getObjectCollections(StateImage from, Location... to) {
        List<ObjectCollection[]> objectCollectionsList = new ArrayList<>();
        int l = to.length > 0 ? 2 : 1;
        ObjectCollection[] firstColl = new ObjectCollection[l];
        firstColl[0] = new ObjectCollection.Builder().withImages(from).build();
        if (l == 2) firstColl[1] = new ObjectCollection.Builder().withLocations(to[0]).build();
        objectCollectionsList.add(firstColl);
        for (int i = 0; i < to.length / 2; i++) {
            l = i + 1 >= to.length ? 1 : 2;
            ObjectCollection[] objColl = new ObjectCollection[l];
            objColl[0] = new ObjectCollection.Builder().withLocations(to[i]).build();
            if (l == 2) objColl[1] = new ObjectCollection.Builder().withLocations(to[i + 1]).build();
            objectCollectionsList.add(objColl);
        }
        return objectCollectionsList;
    }

    public void dragInScreen(Matches matches, StateImage from, Positions.Name... positions) {
        Location[] locations = new Location[positions.length];
        for (int i=0; i<positions.length; i++)
            locations[i] = new Location(new Region(), positions[i]);
        drag(matches, from, locations);
    }

    public void dragInScreen(Matches matches, StateImage from, int xOff, int yOff) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setDragToOffsetX(xOff)
                .setDragToOffsetY(yOff)
                .build();
        matches.setActionOptions(actionOptions);
        drag.perform(matches, from.asObjectCollection());
    }



}
