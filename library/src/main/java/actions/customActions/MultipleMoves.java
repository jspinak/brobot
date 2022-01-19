package actions.customActions;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActionsObject;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.state.ObjectCollection;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.Action.MOVE;

/**
 * This is an example of how to create an Action using the MultipleActions class.
 * Multiple mouse moves can be performed by the BasicAction MouseMove.
 */
public class MultipleMoves {

    private MultipleActions multipleActions;

    public MultipleMoves(MultipleActions multipleActions) {
        this.multipleActions = multipleActions;
    }

    public boolean multipleMoves(Location... locations) {
        ActionOptions move = new ActionOptions.Builder()
                .setAction(MOVE)
                .setPauseAfterEnd(.5)
                .build();
        MultipleActionsObject mao = new MultipleActionsObject();
        for (Location loc : locations) {
            mao.addActionOptionsObjectCollectionPair(
                    move, new ObjectCollection.Builder().withLocations(loc).build()
            );
        }
        mao.print();
        return multipleActions.perform(mao).isSuccess();
    }
}
