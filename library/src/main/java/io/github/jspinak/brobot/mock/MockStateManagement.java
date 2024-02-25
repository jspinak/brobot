package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.database.services.StateService;
import org.springframework.stereotype.Component;

/**
 * Takes care of setting probabilities for States and State objects
 */
@Component
public class MockStateManagement {

    private final StateService stateService;

    public MockStateManagement(StateService stateService) {
        this.stateService = stateService;
    }

    public void setStateProbabilities(int probability, String... stateNames) {
        for (String stateName : stateNames) {
            stateService.getState(stateName).ifPresent(state -> {
                state.setProbabilityExists(probability);
            });
        }

    }
}
