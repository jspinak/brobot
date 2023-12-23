package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.composites.multipleActions.MultipleActions;
import io.github.jspinak.brobot.actions.composites.multipleActions.MultipleActionsObject;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * NOT WORKING! Sikuli appears not to allow mouse movement after a mouse down operation.
 */
@Component
public class MultipleDrags {

    private final MultipleActions multipleActions;
    private ActionOptionsForDrag actionOptionsForDrag;
    private GetDragLocation getDragLocation;

    public MultipleDrags(MultipleActions multipleActions, ActionOptionsForDrag actionOptionsForDrag,
                         GetDragLocation getDragLocation) {
        this.multipleActions = multipleActions;
        this.actionOptionsForDrag = actionOptionsForDrag;
        this.getDragLocation = getDragLocation;
    }

    // not working. mouseMove is somehow frozen after mouseDown
    private void doMultipleDrags(Matches matches, ActionOptions actionOptions, ObjectCollection... objectCollections) {
        ObjectCollection startColl = getStartingPoint(matches, actionOptions, objectCollections);
        if (startColl.isEmpty()) return;
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(actionOptionsForDrag.getMove(actionOptions), startColl);
        mao.addActionOptionsObjectCollectionPair(
                actionOptionsForDrag.getMouseDown(actionOptions), startColl);
        int len = objectCollections.length;
        for (int i = 1; i < len; i++) {
            mao.addActionOptionsObjectCollectionPair(
                    actionOptionsForDrag.getMove(actionOptions), objectCollections[i]);
        }
        mao.addActionOptionsObjectCollectionPair(
                actionOptionsForDrag.getMouseUp(actionOptions), objectCollections[len - 1]);
        //mao.print();
        multipleActions.perform(mao);
    }

    private ObjectCollection getStartingPoint(Matches matches, ActionOptions actionOptions,
                                              ObjectCollection... objectCollections) {
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(matches, actionOptions, objectCollections);
        if (optStartLoc.isEmpty()) return new ObjectCollection.Builder().build();
        return new ObjectCollection.Builder()
                .withLocations(optStartLoc.get())
                .build();
    }
}
