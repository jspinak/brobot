package io.github.jspinak.brobot.actions.customActions.select;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Select is a custom Action built using the custom SelectActionObject. It gives an example of how to build
 * custom Actions that use custom ActionObjects.
 *
 * ActionObjects are classes that contain both ActionOptions and ObjectCollections. They have everything
 * they need to define an operation. All that's needed to run the operation is an Action class such as
 * this class, 'Select'. The Action class determines how and when the ActionOptions and
 * ObjectCollections are used.
 *
 * Select swipes a region until it find an Image. It clicks on the Image and if it finds another
 * confirmation Image, returns true.
 */
@Component
public class Select {

    private Action action;

    public Select(Action action) {
        this.action = action;
    }

    public boolean select(SelectActionObject sao) {
        sao.resetTotalSwipes();
        for (int i=0; i<sao.getMaxSwipes(); i++) {
            sao.setFoundMatches(action.perform(sao.getFindActionOptions(), sao.getFindObjectCollection()));
            if (sao.getFoundMatches().isSuccess()) {
                action.perform(sao.getClickActionOptions(), new ObjectCollection.Builder()
                        .withMatches(sao.getFoundMatches())
                        .build());
                if (sao.getConfirmationObjectCollection() == null) {
                    sao.setSuccess(true);
                    return true;
                }
                else {
                    sao.setFoundConfirmations(action.perform(
                            sao.getConfirmActionOptions(), sao.getConfirmationObjectCollection()));
                    if (sao.getFoundConfirmations().isSuccess()) {
                        sao.setSuccess(true);
                        return true;
                    }
                }
            }
            action.perform(sao.getSwipeActionOptions(), sao.getSwipeFromObjColl(), sao.getSwipeToObjColl());
            sao.addSwipe();
        }
        return false;
    }
}
