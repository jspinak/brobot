package actions.customActions;

import com.brobot.multimodule.actions.actionExecution.Action;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActions;
import com.brobot.multimodule.actions.composites.multipleActions.MultipleActionsObject;
import com.brobot.multimodule.actions.methods.sikuliWrappers.mouse.ClickType;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import org.springframework.stereotype.Component;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.Action.*;

/**
 * This is an example of a more complex custom action.
 */
@Component
public class CommonMultipleActions {

    private MultipleActions multipleActions;
    private Action action;
    private CommonActions commonActions;

    public CommonMultipleActions(MultipleActions multipleActions, Action action, CommonActions commonActions) {
        this.multipleActions = multipleActions;
        this.action = action;
        this.commonActions = commonActions;
    }

    /**
     * This method is similar to ClickUntil with the ActionOptions set for right clicks
     * and for moving after clicking.
     * @param timesToClick
     * @param pauseBetweenClicks
     * @param pauseBeforeClick
     * @param pauseAfterMove
     * @param image
     * @param xMove
     * @param yMove
     * @return
     */
    public boolean rightClickAndMoveUntilVanishes(int timesToClick, double pauseBetweenClicks,
                                                  double pauseBeforeClick, double pauseAfterMove,
                                                  StateImageObject image, int xMove, int yMove) {
        ActionOptions moveBeforeClick = new ActionOptions.Builder()
                .setAction(MOVE)
                .setPauseAfterEnd(pauseBeforeClick)
                .build();
        ActionOptions doAction = new ActionOptions.Builder()
                .setAction(CLICK)
                .setClickType(ClickType.Type.RIGHT)
                .setPauseBeforeMouseDown(pauseBeforeClick)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        ActionOptions moveAfterClick = new ActionOptions.Builder()
                .setAction(MOVE)
                .setAddX(xMove)
                .setAddY(yMove)
                .setPauseAfterEnd(pauseAfterMove)
                .build();
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(moveBeforeClick, objectCollection);
        mao.addActionOptionsObjectCollectionPair(doAction, objectCollection);
        ActionOptions vanish = new ActionOptions.Builder()
                .setAction(VANISH)
                .setMaxWait(pauseBetweenClicks)
                .setMinSimilarity(doAction.getSimilarity() - .10)
                .build();
        Matches matches;
        for (int i=0; i<timesToClick; i++) {
            matches = multipleActions.perform(mao);
            if (matches.isSuccess()) {
                action.perform(moveAfterClick);
                matches.getBestMatch().ifPresent(matchObject ->
                        commonActions.highlightRegion(1, new Region(matchObject.getMatch())));
            }
            if (action.perform(vanish, image).isSuccess()) {
                System.out.println("object vanished, min sim = "+vanish.getSimilarity());
                return true;
            }
        }
        return false;
    }
}
