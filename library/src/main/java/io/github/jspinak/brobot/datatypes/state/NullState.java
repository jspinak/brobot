package io.github.jspinak.brobot.datatypes.state;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * NullState is used as a generic State for passing simple objects
 * (non State objects) to Actions. Simple objects are usually temporary
 * objects that are not associated with any State. These objects can still
 * be acted on by Actions, but no State will become active when they are found.
 * This state should not be in the state repository.
 */
@Getter
public class NullState {

    // convert simple objects to state objects

    public enum Name implements StateEnum {
        NULL
    }

    private final State state = new State.Builder("null")
            .build();

}
