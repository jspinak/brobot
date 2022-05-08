package io.github.jspinak.brobot.datatypes.state;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * NullState is used as a generic State for passing simple objects
 * (non State objects) to Actions. Simple objects are usually temporary
 * objects that are not associated with any State. These objects can still
 * be acted on by Actions, but no State will become active when they are found.
 */
@Component
@Getter
public class NullState {

    // convert simple objects to state objects

    public enum Enum implements StateEnum {
        NULL
    }

    private State state = new State.Builder(Enum.NULL)
            .build();

    public NullState(StateService stateService) {
        stateService.save(state);
    }

}
