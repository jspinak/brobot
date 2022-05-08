package io.github.jspinak.brobot.actions.actionResultCombo;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollection;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollections;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns a List with the first element the Action Matches and the second element the Results Matches
 * ActionResultsCombos are typically:
 * - an Action such as CLICK
 * - a Result from an operation such as FIND
 *
 * The parameters and results only get recorded if the Action is successful.
 * When not successful, a List with only the Action Matches is returned and the Results operation is not run.
 */
@Component
public class RunARCombo {

    private Action action;
    private ParameterCollections parameterCollections;

    public RunARCombo(Action action, ParameterCollections parameterCollections) {
        this.action = action;
        this.parameterCollections = parameterCollections;
    }

    public List<Matches> perform(ActionResultCombo actionResultCombo, ParameterCollection params) {
        List<Matches> matches = new ArrayList<>();
        actionResultCombo.setParameters(params);
        Matches actionMatches = action.perform(actionResultCombo.getActionOptions(),
                actionResultCombo.getActionCollection().toArray(new ObjectCollection[0]));
        matches.add(actionMatches);
        if (!actionMatches.isSuccess()) return matches; //return only action matches when unsuccessful
        Matches resultsMatches = action.perform(actionResultCombo.getResultOptions(),
                actionResultCombo.getResultCollection().toArray(new ObjectCollection[0]));
        matches.add(resultsMatches);
        params.setSuccess(resultsMatches.isSuccess());
        parameterCollections.add(params);
        parameterCollections.printEvery(10);
        return matches;
    }
}
