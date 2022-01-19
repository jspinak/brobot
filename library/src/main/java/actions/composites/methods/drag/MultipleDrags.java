package actions.composites.methods.drag;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActionsObject;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
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
    private Matches doMultipleDrags(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        ObjectCollection startColl = getStartingPoint(actionOptions, objectCollections);
        if (startColl.empty()) return matches;
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
        return multipleActions.perform(mao);
    }

    private ObjectCollection getStartingPoint(ActionOptions actionOptions,
                                              ObjectCollection... objectCollections) {
        Optional<Location> optStartLoc = getDragLocation.getFromLocation(actionOptions, objectCollections);
        if (optStartLoc.isEmpty()) return new ObjectCollection.Builder().build();
        return new ObjectCollection.Builder()
                .withLocations(optStartLoc.get())
                .build();
    }
}
