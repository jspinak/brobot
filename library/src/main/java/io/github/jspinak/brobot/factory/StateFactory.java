package io.github.jspinak.brobot.factory;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating Brobot states after framework initialization. This factory ensures states
 * are only created after the Brobot framework is ready.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateFactory {

    /**
     * Creates a new State with the given name and optional initialization logic. This method can be
     * safely called at any time as it doesn't perform any framework-dependent operations until the
     * state is actually used.
     *
     * @param stateName The state enum representing this state
     * @param initializer Optional consumer to initialize the state with components
     * @return A new State instance
     */
    public State createState(StateEnum stateName, Consumer<State.Builder> initializer) {
        log.debug("Creating state: {}", stateName);

        State.Builder builder = new State.Builder(stateName);

        if (initializer != null) {
            initializer.accept(builder);
        }

        return builder.build();
    }

    /**
     * Creates a StateImage with the given names. This is a lightweight operation that doesn't load
     * the actual image.
     *
     * @param imageNames Names of the images to include
     * @return A new StateImage instance
     */
    public StateImage createStateImage(String... imageNames) {
        StateImage.Builder builder = new StateImage.Builder();
        for (String imageName : imageNames) {
            builder.addPattern(imageName);
        }
        return builder.build();
    }

    /**
     * Creates a StateString with the given string value.
     *
     * @param value The string value
     * @return A new StateString instance
     */
    public StateString createStateString(String value) {
        return new StateString.Builder().setString(value).build();
    }

    /**
     * Creates a StateString with a name and owner state.
     *
     * @param name The name of the StateString
     * @param value The string value
     * @param ownerStateName The name of the owner state
     * @return A new StateString instance
     */
    public StateString createStateString(String name, String value, String ownerStateName) {
        return new StateString.Builder()
                .setName(name)
                .setString(value)
                .setOwnerStateName(ownerStateName)
                .build();
    }
}
