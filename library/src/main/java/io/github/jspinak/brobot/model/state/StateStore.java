package io.github.jspinak.brobot.model.state;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class StateStore {

    private List<State> states = new ArrayList<>();

    public List<State> getAllStates() {
        return states;
    }

    public Optional<State> getState(String name) {
        return states.stream()
                .filter(state -> state.getName().equals(name))
                .findFirst();
    }

    public Optional<State> getState(Long id) {
        return states.stream()
                .filter(state -> Objects.equals(state.getId(), id))
                .findFirst();
    }

    public void save(State state) {
        states.add(state);
        /*
        If already set in the StateEntity, don't change it.
        There is always the Unknown State, which has an id of 0.
         */
        if (state.getId() == null) state.setId((long) states.size()-1);
        state.getStateImages().forEach(image -> {
            image.setOwnerStateId(state.getId());
            image.getAllMatchSnapshots().forEach(snapshot -> snapshot.setStateId(state.getId()));
        });
        state.getStateLocations().forEach(location -> location.setOwnerStateId(state.getId()));
        state.getStateLocations().forEach(location -> {
            location.setOwnerStateId(state.getId());
            location.getMatchHistory().getSnapshots().forEach(snapshot -> snapshot.setStateId(state.getId()));
        });
        state.getStateRegions().forEach(region -> region.setOwnerStateId(state.getId()));
        state.getStateRegions().forEach(region -> {
            region.setOwnerStateId(state.getId());
            region.getMatchHistory().getSnapshots().forEach(snapshot -> snapshot.setStateId(state.getId()));
        });
        state.getStateStrings().forEach(string -> string.setOwnerStateId(state.getId()));
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
