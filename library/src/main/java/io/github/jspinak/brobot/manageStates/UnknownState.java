package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * The Unknown State is usually the start point for any application,
 * and can also be found when Brobot has become lost.
 * It is good to include transitions from the Unknown State that
 * take into account potential errors, so Brobot can find its way
 * back to a known State.
 */
@Getter
@Component
public class UnknownState {

    public enum Enum implements StateEnum {
        UNKNOWN
    }

    private State state;

    public UnknownState(StateService stateService) {
        state = new State.Builder(Enum.UNKNOWN).build();
        stateService.save(state);
    }
}