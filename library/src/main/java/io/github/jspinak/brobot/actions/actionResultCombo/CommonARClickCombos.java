package io.github.jspinak.brobot.actions.actionResultCombo;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.actionOptions.CommonActionOptions;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterCollection;
import io.github.jspinak.brobot.actions.parameterTuning.ParameterThresholds;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.UnknownState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Currently ActionResultCombos are set up for click operations as the first action.
 * This is because the parameters varied are for optimizing clicks.
 * ARCombos have potential to learn other values, too, but are currently not set up for other tasks.
 */
@Component
public class CommonARClickCombos {

    private final RunARCombo runARCombo;
    private final CommonActionOptions commonActionOptions;
    private final CommonResults commonResults;
    private final AllStatesInProjectService allStatesInProjectService;

    public CommonARClickCombos(RunARCombo runARCombo, CommonActionOptions commonActionOptions,
                               CommonResults commonResults, AllStatesInProjectService allStatesInProjectService) {
        this.runARCombo = runARCombo;
        this.commonActionOptions = commonActionOptions;
        this.commonResults = commonResults;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public boolean clickAndFind(StateImage toClick, StateImage toFind) {
        return clickAndAction(toClick, toFind, ActionOptions.Action.FIND);
    }

    public boolean clickAndVanish(StateImage toClick, StateImage toVanish) {
        return clickAndAction(toClick, toVanish, ActionOptions.Action.VANISH);
    }

    public boolean clickAndAction(StateImage toClick, StateImage resultImage, ActionOptions.Action actionType) {
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

    public boolean clickAndFindState(StateImage toClick, String stateName, int numberOfClicks) {
        return clickAndStateAction(toClick, stateName, 1, ActionOptions.Action.FIND, numberOfClicks);
    }

    public boolean clickAndVanishState(StateImage toClick, String stateName, double maxWait, int numberOfClicks) {
        return clickAndStateAction(toClick, stateName, maxWait, ActionOptions.Action.VANISH, numberOfClicks);
    }

    public boolean clickAndStateAction(StateImage toClick, String stateName,
                                       double maxWait, ActionOptions.Action actionType,
                                       int numberOfClicks) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return false; // this could be true but requires additional coding
        Optional<State> state = allStatesInProjectService.getState(stateName);
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
