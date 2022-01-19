package actions.actionResultCombo;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.actionOptions.CommonActionOptions;
import com.brobot.multimodule.actions.parameterTuning.ParameterCollection;
import com.brobot.multimodule.actions.parameterTuning.ParameterThresholds;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.state.State;
import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.services.StateService;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.brobot.multimodule.manageStates.UnknownState.Enum.UNKNOWN;

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

    public boolean typeAndFindState(StateEnum stateEnum, String string) {
        return typeAndFindState(stateEnum, string, "");
    }

    public boolean typeAndFindState(StateEnum stateEnum, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.FIND, stateEnum, string, modifier);
    }

    public boolean typeAndVanishState(StateEnum stateEnum, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.VANISH, stateEnum, string, modifier);
    }

    public boolean typeAndStateAction(ActionOptions.Action resultAction, StateEnum stateEnum,
                                      String string, String modifier) {
        if (stateEnum == UNKNOWN) return false; // this could be true but requires additional coding
        Optional<State> state = stateService.findByName(stateEnum);
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
