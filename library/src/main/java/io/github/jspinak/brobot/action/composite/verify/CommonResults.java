package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionShortcuts;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.tools.tuning.model.TuningConstraints;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Provides common result verification functionality for ActionResultCombo operations.
 * <p>
 * This class centralizes the logic for verifying state-based results after performing
 * an action. It's primarily used by other verification classes to check if a state
 * becomes active or inactive after an action is performed.
 *
 * @see ActionResultCombo
 * @see RunARCombo
 * @see ClickVerify
 * @see TypeVerify
 */
@Component
public class CommonResults {

    private final RunARCombo runARCombo;
    private final ActionShortcuts actionShortcuts;

    public CommonResults(RunARCombo runARCombo, ActionShortcuts actionShortcuts) {
        this.runARCombo = runARCombo;
        this.actionShortcuts = actionShortcuts;
    }

    /**
     * Performs a state-based result verification for an ActionResultCombo.
     * <p>
     * This method sets up the result verification portion of an ActionResultCombo
     * to check for the presence or absence of a state's images. It creates an
     * ObjectCollection containing all images from the specified state and configures
     * the result action accordingly.
     * <p>
     * The method assumes that the action portion of the ActionResultCombo has
     * already been configured by the calling method. It only sets up the result
     * verification options and collection.
     *
     * @param state The state whose images will be used for verification
     * @param arCombo The ActionResultCombo to configure and execute. This object
     *                is modified by adding result options and collections.
     * @param params The parameter collection containing timing configurations
     * @param resultAction The type of verification to perform (e.g., FIND, VANISH)
     * @return true if both the action succeeded and the state verification passed,
     *         false otherwise
     */
    public boolean stateAction(State state, ActionResultCombo arCombo, TuningExperiment params,
                               ActionOptions.Action resultAction) {
        ObjectCollection resultsCollection = new ObjectCollection.Builder()
                .withAllStateImages(state)
                .build();
        arCombo.setResultOptions(actionShortcuts.standard(resultAction, TuningConstraints.maxWait));
        arCombo.addResultCollection(resultsCollection);
        // perform all actions
        List<ActionResult> matches = runARCombo.perform(arCombo, params);
        return matches.size() > 1 && matches.get(1).isSuccess();
    }
}
