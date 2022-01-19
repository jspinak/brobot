package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.springframework.stereotype.Component;

@Component
public class ActionOptionsForDrag {

    public ActionOptions getMouseDown(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_DOWN)
                .setPauseBeforeMouseDown(actionOptions.getPauseBeforeMouseDown())
                .setPauseAfterMouseDown(actionOptions.getPauseAfterMouseDown())
                .setClickType(actionOptions.getClickType())
                .build();
    }

    public ActionOptions getMouseUp(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_UP)
                .setPauseBeforeMouseUp(actionOptions.getPauseBeforeMouseUp())
                .setPauseAfterMouseUp(actionOptions.getPauseAfterMouseUp())
                .build();
    }

    public ActionOptions getFindFrom(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getAddX())
                .setAddY(actionOptions.getAddY())
                .build();
    }

    public ActionOptions getFindTo(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getDragToOffsetX())
                .setAddY(actionOptions.getDragToOffsetY())
                .build();
    }

    public ActionOptions getMove(ActionOptions actionOptions) {
        return new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOVE)
                .setMoveMouseDelay(actionOptions.getMoveMouseDelay())
                .build();
    }

}
