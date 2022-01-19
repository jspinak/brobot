package database.state.state;

import com.brobot.multimodule.primatives.enums.StateEnum;

import java.util.HashSet;
import java.util.Set;

public class States {

    private Set<StateEnum> activeStates = new HashSet<>();

    public void addState(StateEnum stateEnum) {
        activeStates.add(stateEnum);
    }

    public void addStates(Set<StateEnum> states) {
        activeStates.addAll(states);
    }

    public void addStates(States states) {
        activeStates.addAll(states.getActiveStates());
    }

    public Set<StateEnum> getActiveStates() {
        return activeStates;
    }

}
