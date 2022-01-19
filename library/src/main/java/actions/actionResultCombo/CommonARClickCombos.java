package actions.actionResultCombo;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.actionOptions.CommonActionOptions;
import com.brobot.multimodule.actions.parameterTuning.ParameterCollection;
import com.brobot.multimodule.actions.parameterTuning.ParameterThresholds;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.state.State;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.services.StateService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.brobot.multimodule.manageStates.UnknownState.Enum.UNKNOWN;

/**
 * Currently ActionResultCombos are set up for click operations as the first action.
 * This is because the parameters varied are for optimizing clicks.
 * ARCombos have potential to learn other values, too, but are currently not set up for other tasks.
 */
@Component
public class CommonARClickCombos {

    private RunARCombo runARCombo;
    private CommonActionOptions commonActionOptions;
    private CommonResults commonResults;
    private StateService stateService;

    public CommonARClickCombos(RunARCombo runARCombo, CommonActionOptions commonActionOptions,
                               CommonResults commonResults, StateService stateService) {
        this.runARCombo = runARCombo;
        this.commonActionOptions = commonActionOptions;
        this.commonResults = commonResults;
        this.stateService = stateService;
    }

    public boolean clickAndFind(StateImageObject toClick, StateImageObject toFind) {
        return clickAndAction(toClick, toFind, ActionOptions.Action.FIND);
    }

    public boolean clickAndVanish(StateImageObject toClick, StateImageObject toVanish) {
        return clickAndAction(toClick, toVanish, ActionOptions.Action.VANISH);
    }

    public boolean clickAndAction(StateImageObject toClick, StateImageObject resultImage, ActionOptions.Action actionType) {
        ActionResultCombo arCombo = new ActionResultCombo();
        ActionOptions actionOptions1 = commonActionOptions.standard(ActionOptions.Action.CLICK, 1);
        arCombo.setActionOptions(actionOptions1);
        ParameterCollection params = new ParameterCollection(actionOptions1);
        params.setMaxWait(ParameterThresholds.maxWait);
        arCombo.addActionCollection(toClick.asObjectCollection());
        arCombo.setResultOptions(commonActionOptions.standard(actionType, 1));
        arCombo.addResultCollection(resultImage.asObjectCollection());
        List<Matches> matches = runARCombo.perform(arCombo, params);
        return matches.size() > 1 && matches.get(1).isSuccess();
    }

    public boolean clickAndFindState(StateImageObject toClick, StateEnum stateEnum, int numberOfClicks) {
        return clickAndStateAction(toClick, stateEnum, 1, ActionOptions.Action.FIND, numberOfClicks);
    }

    public boolean clickAndVanishState(StateImageObject toClick, StateEnum stateEnum, double maxWait, int numberOfClicks) {
        return clickAndStateAction(toClick, stateEnum, maxWait, ActionOptions.Action.VANISH, numberOfClicks);
    }

    public boolean clickAndStateAction(StateImageObject toClick, StateEnum stateEnum,
                                       double maxWait, ActionOptions.Action actionType,
                                       int numberOfClicks) {
        if (stateEnum == UNKNOWN) return false; // this could be true but requires additional coding
        Optional<State> state = stateService.findByName(stateEnum);
        if (state.isEmpty()) return false;
        ActionResultCombo arCombo = new ActionResultCombo();
        ActionOptions actionOptions1 = commonActionOptions.findAndMultipleClicks(maxWait, numberOfClicks);
        ParameterCollection params = new ParameterCollection(actionOptions1);
        params.setMaxWait(ParameterThresholds.maxWait);
        arCombo.setActionOptions(actionOptions1);
        arCombo.addActionCollection(toClick.asObjectCollection());
        return commonResults.stateAction(state.get(), arCombo, params, actionType);
    }

}
