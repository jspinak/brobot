package io.github.jspinak.brobot.actions.composites.methods;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CopyActionOptions;
import io.github.jspinak.brobot.actions.composites.multipleActions.MultipleActionsObject;
import io.github.jspinak.brobot.actions.composites.multipleActions.MultipleBasicActions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.*;

/**
 * ClickUntil clicks Matches until a condition occurs or the operation times out.
 * The two conditions available are OBJECTS_VANISH and OBJECTS_APPEAR.
 *
 * The actionOptions variable is used for both the CLICK and the 'until' Action.
 * To use separate ActionOptions for the CLICK and 'until' Actions, you can set up a custom
 * DoUntilActionObject.
 *
 * ObjectCollection #1 is for CLICK
 * ObjectCollection #2 is for the 'until' method
 *
 * The Objects in the 1st ObjectCollection are acted on by the CLICK method.
 * If there is a 2nd ObjectCollection, it is acted on by the FIND method.
 * If there is only 1 ObjectCollection, the FIND method also uses these objects.
 * 1 ObjectCollection: Click this until it disappears.
 * 2 ObjectCollections: Click #1 until #2 appears or disappears.
 */
@Component
public class ClickUntil implements ActionInterface {

    private final MultipleBasicActions multipleBasicActions;

    private Map<ActionOptions.ClickUntil, ActionOptions.Action> clickUntilConversion = new HashMap<>();
    {
        clickUntilConversion.put(ActionOptions.ClickUntil.OBJECTS_VANISH, VANISH);
        clickUntilConversion.put(ActionOptions.ClickUntil.OBJECTS_APPEAR, FIND);
    }

    public ClickUntil(MultipleBasicActions multipleBasicActions) {
        this.multipleBasicActions = multipleBasicActions;
    }

    /**
     * Performs a Click and a Find operation.
     *
     * @param matches holds the action configuration
     * @param objectCollections holds the objects to act on
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        ObjectCollection coll1 = objectCollections[0]; //the 'click' collection
        ObjectCollection coll2 = objectCollections[0]; //the 'until' collection
        if (objectCollections.length > 1) coll2 = objectCollections[1];
        ActionOptions click = CopyActionOptions.copyImmutableOptions(actionOptions);
        click.setAction(CLICK);
        ActionOptions untilAction = CopyActionOptions.copyImmutableOptions(actionOptions);
        untilAction.setAction(clickUntilConversion.get(actionOptions.getClickUntil()));
        System.out.print(", until action is "+untilAction.getAction());
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(click, coll1);
        mao.addActionOptionsObjectCollectionPair(untilAction, coll2);
        multipleBasicActions.perform(mao);
    }

    /**
     * Success criteria is set in the Success class, but this is another way of setting
     * it. Setting the success criteria inside the method gives more flexibility,
     * and could be useful for more complex Actions.
     * @param actionOptions holds the action configuration
     * @param objectCollections holds the objects to act on
     */
    private void setSuccessEvaluation(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections.length < 1) {
            actionOptions.setSuccessCriteria(matches -> false);
        }
        if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_APPEAR)
            actionOptions.setSuccessCriteria(matches -> !matches.isEmpty());
        if (actionOptions.getClickUntil() == ActionOptions.ClickUntil.OBJECTS_VANISH)
            actionOptions.setSuccessCriteria(Matches::isEmpty);
    }
}
