package io.github.jspinak.brobot.actions.actionResultCombo;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CommonActionOptions;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollection;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterThresholds;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.services.StateService;
import io.github.jspinak.brobot.manageStates.UnknownState;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class CommonARTypeCombos {

    private RunARCombo runARCombo;
    private CommonActionOptions commonActionOptions;
    private CommonResults commonResults;
    private StateService stateService;

    public CommonARTypeCombos(RunARCombo runARCombo, CommonActionOptions commonActionOptions,
                              CommonResults commonResults, StateService stateService) {
        this.runARCombo = runARCombo;
        this.commonActionOptions = commonActionOptions;
        this.commonResults = commonResults;
        this.stateService = stateService;
    }

    public boolean typeAndFindState(String stateName, String string) {
        return typeAndFindState(stateName, string, "");
    }

    public boolean typeAndFindState(String stateName, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.FIND, stateName, string, modifier);
    }

    public boolean typeAndVanishState(String stateName, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.VANISH, stateName, string, modifier);
    }

    public boolean typeAndStateAction(ActionOptions.Action resultAction, String stateName,
                                      String string, String modifier) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return false; // this could be true but requires additional coding
        Optional<State> state = stateService.findByName(stateName);
        if (state.isEmpty()) return false;
        ActionResultCombo arCombo = new ActionResultCombo();
        // add the type action
        ActionOptions actionOptions1 = commonActionOptions.type(modifier);
        ParameterCollection params = new ParameterCollection(actionOptions1);
        params.setMaxWait(ParameterThresholds.maxWait);
        arCombo.setActionOptions(actionOptions1);
        ObjectCollection strColl = new ObjectCollection.Builder()
                .withStrings(string)
                .build();
        arCombo.addActionCollection(strColl);
        return commonResults.stateAction(state.get(), arCombo, params, resultAction);
    }

}
