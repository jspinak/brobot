package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AllStatesInProject {

    private List<State> states = new ArrayList<>();

    public List<State> getAllStates() {
        return states;
    }

    public Optional<State> getState(String name) {
        return states.stream()
                .filter(state -> state.getName().equals(name))
                .findFirst();
    }

    public void save(State state) {
        states.add(state);
    }

    public void deleteAll() {
        states = new ArrayList<>();
    }

    public boolean delete(State state) {
        if (!states.contains(state)) return false;
        states.remove(state);
        return true;
    }

}
