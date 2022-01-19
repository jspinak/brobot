package actions.actionResultCombo;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.actionOptions.CommonActionOptions;
import com.brobot.multimodule.actions.parameterTuning.ParameterCollection;
import com.brobot.multimodule.actions.parameterTuning.ParameterThresholds;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.state.State;
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
