package io.github.jspinak.brobot.automationScripts;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.IStateTransition;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultStateHandler implements StateHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultStateHandler.class);
    private final StateTransitionsManagement stateTransitionsManagement;

    public DefaultStateHandler(StateTransitionsManagement stateTransitionsManagement) {
        this.stateTransitionsManagement = stateTransitionsManagement;
    }

    @Override
    public boolean handleState(State currentState, StateTransitions stateTransitions) {
        if (stateTransitions == null || stateTransitions.getTransitions().isEmpty()) {
            logger.info("No transitions available for state: {}", currentState.getName());
            return false;
        }

        // Get first available transition
        Optional<IStateTransition> transition = stateTransitions.getTransitions().stream()
                .findFirst();

        IStateTransition stateTransition = transition.get();
        try {
            // Execute the transition
            if (!stateTransition.getActivate().isEmpty())
                return stateTransitionsManagement.openState(stateTransition.getActivate().iterator().next());
        } catch (Exception e) {
            logger.error("No transitions for state: {}", currentState.getName(), e);
            return false;
        }
        return false;
    }

    @Override
    public void onNoTransitionFound() {
        logger.debug("No active state found, continuing search...");
        // Add any additional logic for handling no transition case
    }
}