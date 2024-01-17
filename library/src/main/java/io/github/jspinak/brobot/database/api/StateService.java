package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.database.data.StateRepo;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateService {

    private final StateRepo StateRepo;

    public StateService(StateRepo StateRepo) {
        this.StateRepo = StateRepo;
    }

    public State getState(String name) {
        return StateRepo.findByName(name).orElse(null);
    }

    public List<State> getAllStates() {
        return StateRepo.findAllAsList();
    }

    public void saveState(State State) {
        StateRepo.save(State);
    }
}
