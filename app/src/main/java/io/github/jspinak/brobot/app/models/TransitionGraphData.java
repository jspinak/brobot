package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.app.web.responses.StateResponse;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransitionGraphData {
    private List<StateResponse> states;
    private List<TransitionResponse> transitions;

    public TransitionGraphData(List<StateResponse> states, List<TransitionResponse> transitions) {
        this.states = states;
        this.transitions = transitions;
    }

    @Override
    public String toString() {
        return "TransitionGraphData{" +
                "states=" + states +
                ", transitions=" + transitions +
                '}';
    }

}
