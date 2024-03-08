package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.primatives.enums.StateEnum;

import java.util.HashSet;
import java.util.Set;

public class ActiveStates {

    private Set<StateEnum> activeStates = new HashSet<>();

    public void addState(StateEnum stateEnum) {
        activeStates.add(stateEnum);
    }

    public void addStates(Set<StateEnum> states) {
        activeStates.addAll(states);
    }

    public void addStates(ActiveStates activeStates) {
        this.activeStates.addAll(activeStates.getActiveStates());
    }

    public Set<StateEnum> getActiveStates() {
        return activeStates;
    }

}
