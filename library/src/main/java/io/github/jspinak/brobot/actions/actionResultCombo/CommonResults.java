package io.github.jspinak.brobot.actions.actionResultCombo;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CommonActionOptions;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollection;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterThresholds;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommonResults {

    private final RunARCombo runARCombo;
    private final CommonActionOptions commonActionOptions;

    public CommonResults(RunARCombo runARCombo, CommonActionOptions commonActionOptions) {
        this.runARCombo = runARCombo;
        this.commonActionOptions = commonActionOptions;
    }

    public boolean stateAction(State state, ActionResultCombo arCombo, ParameterCollection params,
                               ActionOptions.Action resultAction) {
        ObjectCollection resultsCollection = new ObjectCollection.Builder()
                .withAllStateImages(state)
                .build();
        arCombo.setResultOptions(commonActionOptions.standard(resultAction, ParameterThresholds.maxWait));
        arCombo.addResultCollection(resultsCollection);
        // perform all actions
        List<Matches> matches = runARCombo.perform(arCombo, params);
        return matches.size() > 1 && matches.get(1).isSuccess();
    }
}
