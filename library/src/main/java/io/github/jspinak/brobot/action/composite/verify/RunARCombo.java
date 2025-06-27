package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;
import io.github.jspinak.brobot.tools.tuning.store.TuningExperimentStore;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes ActionResultCombo operations and collects parameter performance data.
 * <p>
 * This class is responsible for running the two-phase operations defined in an
 * {@link ActionResultCombo}: first performing an action, then verifying the result.
 * It collects timing and success data to help optimize automation parameters.
 * <p>
 * Execution flow:
 * <ol>
 * <li>Apply timing parameters from the ParameterCollection to the combo</li>
 * <li>Execute the primary action (e.g., CLICK)</li>
 * <li>If successful, execute the verification action (e.g., FIND)</li>
 * <li>Record results in ParameterCollections for analysis</li>
 * </ol>
 * <p>
 * The verification step is only performed if the primary action succeeds. This
 * ensures that parameter data is only collected for valid action-result pairs.
 *
 * @see ActionResultCombo
 * @see TuningExperiment
 * @see TuningExperimentStore
 */
@Component
public class RunARCombo {

    private Action action;
    private TuningExperimentStore parameterCollections;

    public RunARCombo(Action action, TuningExperimentStore parameterCollections) {
        this.action = action;
        this.parameterCollections = parameterCollections;
    }

    /**
     * Executes an ActionResultCombo and returns the results of both phases.
     * <p>
     * This method applies the provided parameters to the combo, executes the primary
     * action, and if successful, executes the verification action. Results are collected
     * for parameter optimization analysis.
     * <p>
     * The returned list contains:
     * <ul>
     * <li>Index 0: ActionResult from the primary action (always present)</li>
     * <li>Index 1: ActionResult from the verification action (only if primary succeeded)</li>
     * </ul>
     * <p>
     * Side effects:
     * <ul>
     * <li>Modifies the actionResultCombo by applying parameters</li>
     * <li>Updates the params success status based on verification results</li>
     * <li>Adds the parameter collection to the tracking system</li>
     * <li>May trigger console output every 10 executions</li>
     * </ul>
     *
     * @param actionResultCombo The combo defining the action and verification to perform
     * @param params The timing parameters to use for this execution
     * @return A list containing 1 or 2 ActionResult objects depending on primary action success
     */
    public List<ActionResult> perform(ActionResultCombo actionResultCombo, TuningExperiment params) {
        List<ActionResult> matches = new ArrayList<>();
        actionResultCombo.setParameters(params);
        ActionResult actionMatches = action.perform(actionResultCombo.getActionOptions(),
                actionResultCombo.getActionCollection().toArray(new ObjectCollection[0]));
        matches.add(actionMatches);
        if (!actionMatches.isSuccess()) return matches; //return only action matches when unsuccessful
        ActionResult resultsMatches = action.perform(actionResultCombo.getResultOptions(),
                actionResultCombo.getResultCollection().toArray(new ObjectCollection[0]));
        matches.add(resultsMatches);
        params.setSuccess(resultsMatches.isSuccess());
        parameterCollections.add(params);
        parameterCollections.printEvery(10);
        return matches;
    }
}
