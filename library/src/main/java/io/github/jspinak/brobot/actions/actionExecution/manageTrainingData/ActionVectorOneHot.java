package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.trainingData.ActionVector;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This translation method uses one hot encoding to encode the type of action performed.
 * A reason to use one hot encoding is that conceptually, different
 * actions can be considered different categories (i.e. CLICK and DRAG) and should not be on the same scale. If they
 * were on the same scale (i.e. CLICK = 0, DRAG = 1), a concept would exist of an action with the value 0.5,
 * which does not make a lot of sense.
 *
 * Only basic actions that directly modify the GUI are included. Composite actions are not included.
 * The following are not included: FIND, DEFINE, VANISH, GET_TEXT, CLASSIFY, CLICK_UNTIL.
 * HIGHLIGHT is included because it will be a good first test of a GUI automation neural network.
 * It should be easier for a neural network to produce correct highlights since all it needs to do is
 * recognize the area highlighted and the size and color of the highlight.
 * Operations should not include FIND operations. For example, clicking on an image requires a FIND operation.
 * This operation would be converted to a vector after the coordinates to click are found.
 *
 * The Matches object is used to pass all information about the action. Matches contains the ActionsObject as
 * well as information about the coordinates acted on and if the action succeeded. Actions that do not succeed
 * won't be converted to vectors.
 *
 * Some operations that include FIND, when performed by a neural net, will no longer need FIND. This includes
 * dragging an image from one location to another. Other operations, especially those that do not change the
 * GUI environment, will require a FIND operation. This can be performed by a separate neural network.
 */
@Component
public class ActionVectorOneHot implements ActionVectorTranslation {

    public ActionVector toVector(Matches matches) {
        ActionVector actionVector = new ActionVector();
        if (matches.isEmpty()) return actionVector; // action failed
        short[] vec = actionVector.getVector();
        ActionOptions actOpt = matches.getActionOptions();
        setCoordinates(vec, matches);
        setAction(vec, actOpt);
        setHighlightOptions(vec, matches);
        return actionVector;
    }

    private void setCoordinates(short[] vec, Matches matches) {
        Optional<Match> optMatch = matches.getBestMatch();
        if (optMatch.isEmpty()) return;
        Match match = optMatch.get();
        vec[6] = (short) match.x();
        vec[7] = (short) match.y();
        vec[8] = (short) match.w();
        vec[9] = (short) match.h();
    }

    private void setHighlightOptions(short[] vec, Matches matches) {
        if (matches.getActionOptions().getAction() != ActionOptions.Action.HIGHLIGHT) return;
        String color = matches.getActionOptions().getHighlightColor();
        if (color.equals("blue")) vec[10] = 0;
        if (color.equals("red")) vec[10] = 1;
        if (color.equals("yellow")) vec[10] = 2;
        if (color.equals("green")) vec[10] = 3;
        if (color.equals("orange")) vec[10] = 4;
        if (color.equals("purple")) vec[10] = 5;
        if (color.equals("white")) vec[10] = 6;
        if (color.equals("black")) vec[10] = 7;
        if (color.equals("grey")) vec[10] = 8;
        if (matches.getActionOptions().isHighlightAllAtOnce()) vec[11] = 1;
    }

    private void setAction(short[] vec, ActionOptions actionOptions) {
        switch (actionOptions.getAction()) {
            case CLICK -> vec[0] = 1;
            case DRAG -> vec[1] = 1;
            case TYPE -> vec[2] = 1;
            case MOVE -> vec[3] = 1;
            case SCROLL_MOUSE_WHEEL -> vec[4] = 1; // ScrollDirection UP DOWN
            case HIGHLIGHT -> vec[5] = 1;
            /*case MOUSE_DOWN -> vec[6] = 1;
            case MOUSE_UP -> vec[7] = 1;
            case KEY_DOWN -> vec[8] = 1;
            case KEY_UP -> vec[9] = 1;
            case CLICK_UNTIL -> vec[10] = 1;
            case FIND: vec[] = 1; break;
            case DEFINE: vec[] = 1; break;
            case VANISH: vec[] = 1; break;
            case GET_TEXT: vec[] = 1; break;
            case CLASSIFY: vec[] = 1; break;*/
        }
    }

    public ActionOptions toActionOptions(ActionVector actionVector) {
        ActionOptions actionOptions = new ActionOptions();
        short[] vec = actionVector.getVector();
        setAction(actionOptions, vec);
        //... TODO
        return actionOptions;
    }

    /**
     * Extract the data from the ActionVector to produce the ObjectCollection accompanying the action.
     * @param actionVector the action and its results as a vector
     * @return an ObjectCollection using data from the action results
     */
    public ObjectCollection toObjectCollection(ActionVector actionVector) {
        return null;
    }
    
    private void setAction(ActionOptions actionOptions, short[] vec) {
        if (vec[0] == 1) actionOptions.setAction(ActionOptions.Action.CLICK);
        else if (vec[1] == 1) actionOptions.setAction(ActionOptions.Action.DRAG);
        else if (vec[2] == 1) actionOptions.setAction(ActionOptions.Action.TYPE);
        else if (vec[3] == 1) actionOptions.setAction(ActionOptions.Action.SCROLL_MOUSE_WHEEL);
        else if (vec[4] == 1) actionOptions.setAction(ActionOptions.Action.MOVE);
        else if (vec[5] == 1) actionOptions.setAction(ActionOptions.Action.HIGHLIGHT);
    }

}