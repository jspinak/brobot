package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import io.github.jspinak.brobot.manageStates.ActionDefinitionStateTransition;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class Model {

    private final List<State> states;
    private final List<StateTransitions> stateTransitions;

    public Model(List<State> states, List<StateTransitions> stateTransitions) {
        this.states = states;
        this.stateTransitions = stateTransitions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String indentation = "";
        sb.append("__MODEL__\n");

        sb.append("_STATES_\n");
        for (State state : states) {
            sb.append("State: ").append(state.getName()).append(" (id=").append(state.getId()).append(")\n");
            indentation = "  ";
            sb.append(indentation).append("Images: ").append(state.getStateImages().size()).append("\n");
            for (StateImage stateImage : state.getStateImages()) {
                indentation = "    ";
                sb.append(indentation).append(stateImage.getName())
                        .append(" (id=").append(stateImage.getIdAsString())
                        .append(", patterns=").append(stateImage.getPatterns().size())
                        .append(")\n");
            }

            StateTransitions stTrs = getStateTransitions(state);
            indentation = "  ";
            sb.append(indentation).append("StateTransitions{\n");
            indentation = "    ";
            sb.append(indentation).append("stateName='").append(state.getName()).append("',\n");
            sb.append(indentation).append("stateId=").append(state.getId()).append(",\n");

            assert stTrs != null;
            IStateTransition transitionFinish = stTrs.getTransitionFinish();
            sb.append(indentation).append("transitionFinish=");
            if (transitionFinish != null) {
                sb.append(transitionToString(transitionFinish, indentation));
            } else {
                sb.append("null");
            }
            sb.append(",\n");

            indentation = "  ";
            sb.append(indentation).append("transitions={\n");
            indentation = "    ";
            for (Map.Entry<Long, IStateTransition> entry : stTrs.getTransitions().entrySet()) {
                sb.append(indentation).append(entry.getKey()).append(" -> ")
                        .append(transitionToString(entry.getValue(), "  ")).append(",\n");
            }
            indentation = "  ";
            sb.append(indentation).append("},\n");

            sb.append(indentation).append("staysVisibleAfterTransition=").append(stTrs.isStaysVisibleAfterTransition()).append("\n");
            indentation = "";
            sb.append(indentation).append("}").append("\n");
        }

        return sb.toString();
    }

    private String transitionToString(IStateTransition transition, String indentation) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentation).append("IStateTransition{");
        sb.append(indentation).append("staysVisibleAfterTransition=").append(transition.getStaysVisibleAfterTransition()).append(", ");
        sb.append(indentation).append("activate=").append(transition.getActivate()).append(", ");
        sb.append(indentation).append("exit=").append(transition.getExit()).append(", ");
        sb.append(indentation).append("score=").append(transition.getScore()).append(", ");
        sb.append(indentation).append("timesSuccessful=").append(transition.getTimesSuccessful());

        if (transition instanceof ActionDefinitionStateTransition) {
            ActionDefinitionStateTransition adst = (ActionDefinitionStateTransition) transition;
            adst.getActionDefinition().ifPresent(actionDef ->
                    sb.append(", actionDefinition=").append(actionDefinitionToString(actionDef, "  ")));
        }

        sb.append("}");
        return sb.toString();
    }

    private String actionDefinitionToString(ActionDefinition actionDef, String indentation) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentation).append("ActionDefinition{");
        sb.append(indentation).append("steps=[");
        for (ActionStep step : actionDef.getSteps()) {
            sb.append("Step{");
            sb.append("options=").append(step.getOptions()).append(", ");
            sb.append("objects=").append(objectCollectionToString(step.getObjects(), "  "));
            sb.append("}, ");
        }
        if (!actionDef.getSteps().isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String objectCollectionToString(ObjectCollection objects, String indentation) {
        StringBuilder sb = new StringBuilder();
        sb.append(indentation).append("ObjectCollection{");
        sb.append(indentation).append("stateImages=[");
        for (StateImage stateImage : objects.getStateImages()) {
            sb.append("StateImage{name='").append(stateImage.getName()).append("', ");
            sb.append("patterns=").append(stateImage.getPatterns().size()).append("}, ");
        }
        if (!objects.getStateImages().isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private StateTransitions getStateTransitions(State state) {
        for (StateTransitions sT : stateTransitions) {
            if (Objects.equals(sT.getStateId(), state.getId())) return sT;
        }
        return null;
    }
}
