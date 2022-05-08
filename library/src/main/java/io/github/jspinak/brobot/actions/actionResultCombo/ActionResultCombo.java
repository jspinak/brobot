package io.github.jspinak.brobot.actions.actionResultCombo;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollection;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *      An ActionResultsCombos has both an action and an expected result. It is built from
 *      Composite and Basic Actions. Its purpose is to give feedback on an Action based on an
 *      environmental success (as opposed to an operational success).
 *        Operational Success: The success of a specific operation as determined by its successful
 *          completion. For example, a Find operation is successful when it finds a Match. A Click
 *          operation is successful when it finds a Match and that Match is clickable (on the screen).
 *          Operational success in brobot can be modified; there is a variable in ActionOptions of
 *          type {@code Predicate<Matches>} that allows the user to implement her own success evaluation
 *          method.
 *        Environmental Success: The success of an operation as measured by some result in the
 *          environment. For example, a Click may succeed operationally but not be registered by the
 *          application. In order to determine whether the action has its desired effect in the
 *          application, it is necessary to observe the desired effect. This is typically done with
 *          a Find operation but could be done with any Action that gets data from the environment.
 *
 *      The evaluation of the action based on the result can help the algorithm learn
 *     - one example of this is tuning action hyperparameters such as ClickDelay
 *     - it can also get an estimate of how long it takes for objects to appear and disappear after clicking
 *       and set maxWait times accordingly.
 *
 *      Click-Find is an ActionResultsCombo. For example, we want to click a button that opens
 *        a menu. The Click-Find operation will succeed when the button is clicked and the menu is
 *        found, and will fail when the button is clicked and the menu is not found. If the button
 *        is not clicked, no result will be recorded. The result is recorded in ParameterCollections.
 *      ParameterCollections: The purpose of this class is to fine tune the brobot
 *        hyperparameters (variables found in the BrobotSettings and Sikuli Settings classes).
 *        For example, the user may want to make the automation as fast as possible, and reduce the pauses
 *        in execution of commands to a minimum. At some point, actions will be too quick to
 *        work correctly in the application and there will be errors in the automation.
 *        This point can be determined with the ParameterCollections class.
 *
 *      ActionResultsCombos can be useful for reinforcement learning as they give feedback
 *      on changes to the environment caused by specific actions. The Result operation, whether
 *      it is a Find, Define, Text, or some other operation, will provide detailed information
 *      about the new state of the environment and can be incorporated into the algorithm's
 *      reward function. The existence and position of specific objects does not need to be
 *      guessed by the neural network but can be directly added to the neural network by
 *      deterministic processes (brobot Actions). For Atari games, which are
 *      benchmark applications for reinforcement learning, the game objects are always the same
 *      and easily recognizable with a brobot Find operation.
 */
@Getter
@Setter
public class ActionResultCombo {

    private ActionOptions actionOptions;
    private List<ObjectCollection> actionCollection = new ArrayList<>();
    private ActionOptions resultOptions;
    private List<ObjectCollection> resultCollection = new ArrayList<>();

    public void addActionCollection(ObjectCollection objectCollection) {
        actionCollection.add(objectCollection);
    }

    public void addResultCollection(ObjectCollection objectCollection) {
        resultCollection.add(objectCollection);
    }

    public void setParameters(ParameterCollection params) {
        actionOptions.setPauseBeforeMouseDown(params.getPauseBeforeMouseDown());
        actionOptions.setPauseAfterMouseDown(params.getPauseAfterMouseDown());
        actionOptions.setPauseAfterMouseUp(params.getPauseAfterMouseUp());
        actionOptions.setMoveMouseDelay(params.getMoveMouseDelay());
        resultOptions.setMaxWait(params.getMaxWait());
    }
}
