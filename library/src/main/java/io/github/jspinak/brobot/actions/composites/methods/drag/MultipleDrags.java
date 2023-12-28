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
    private void doMultipleDrags(Matches matches, ObjectCollection... objectCollections) {
        ObjectCollection startColl = getStartingPoint(matches, objectCollections);
        if (startColl.isEmpty()) return;
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(actionOptionsForDrag.getMove(matches.getActionOptions()), startColl);
        mao.addActionOptionsObjectCollectionPair(
                actionOptionsForDrag.getMouseDown(matches.getActionOptions()), startColl);
        int len = objectCollections.length;
        for (int i = 1; i < len; i++) {
            mao.addActionOptionsObjectCollectionPair(
                    actionOptionsForDrag.getMove(matches.getActionOptions()), objectCollections[i]);
        }
        mao.addActionOptionsObjectCollectionPair(
                actionOptionsForDrag.getMouseUp(matches.getActionOptions()), objectCollections[len - 1]);
        //mao.print();
        multipleActions.perform(mao);
    }

    private ObjectCollection getStartingPoint(Matches matches, ObjectCollection... objectCollections) {
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(matches, objectCollections);
        if (optStartLoc.isEmpty()) return new ObjectCollection.Builder().build();
        return new ObjectCollection.Builder()
                .withLocations(optStartLoc.get())
                .build();
    }
}
