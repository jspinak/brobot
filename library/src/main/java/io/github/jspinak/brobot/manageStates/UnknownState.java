package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
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

    private State state = new State.Builder("unknown").build();

    public UnknownState(AllStatesInProjectService allStatesInProjectService) {
        allStatesInProjectService.save(state);
    }
}
