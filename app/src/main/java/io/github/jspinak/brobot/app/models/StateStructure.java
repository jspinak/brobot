package io.github.jspinak.brobot.app.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class StateStructure {
    private List<StateAndTransitions> statesAndTransitions;

    public StateStructure(List<StateAndTransitions> statesAndTransitions) {
        this.statesAndTransitions = statesAndTransitions;
    }

    public Map<String, StateAndTransitions> getStateMap() {
        return statesAndTransitions.stream()
                .collect(Collectors.toMap(
                        sat -> sat.getState().getName(),
                        sat -> sat
                ));
    }

    public StateAndTransitions getStateAndTransitions(String stateName) {
        return getStateMap().get(stateName);
    }
}