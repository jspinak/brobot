package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

/**
 * Takes care of setting probabilities for States and State objects
 */
@Component
public class MockStateManagement {

    private StateService stateService;

    public MockStateManagement(StateService stateService) {
        this.stateService = stateService;
    }

    public void setStateAndStateImageProbabilities(int probability, StateEnum... stateEnums) {
        for (StateEnum stateEnum : stateEnums) {
            stateService.findByName(stateEnum).ifPresent(state -> {
                state.setProbabilityExists(probability);
                state.setProbabilitiesForAllImages(100);
            });
        }

    }
}
