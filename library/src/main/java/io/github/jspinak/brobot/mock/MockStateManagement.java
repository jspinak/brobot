package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import org.springframework.stereotype.Component;

/**
 * Takes care of setting probabilities for States and State objects
 */
@Component
public class MockStateManagement {

    private final AllStatesInProjectService allStatesInProjectService;

    public MockStateManagement(AllStatesInProjectService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void setStateProbabilities(int probability, String... stateNames) {
        for (String stateName : stateNames) {
            allStatesInProjectService.getState(stateName).ifPresent(state -> {
                state.setProbabilityExists(probability);
            });
        }

    }
}
