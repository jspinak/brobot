package io.github.jspinak.brobot.annotations;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registers Brobot State objects with the framework's StateService.
 *
 * <p>This service is responsible solely for registering State objects with the StateService. It
 * does not handle state building or component extraction, following the Single Responsibility
 * Principle.
 *
 * <p>Registration involves:
 *
 * <ul>
 *   <li>Adding the State to the StateService's internal registry
 *   <li>Making the State available for navigation and transitions
 *   <li>Enabling State discovery by name or enum
 * </ul>
 *
 * @since 1.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StateRegistrationService {

    private final StateService stateService;

    /**
     * Registers a State with the StateService.
     *
     * @param state The State to register
     * @return true if registration was successful, false otherwise
     */
    public boolean registerState(State state) {
        if (state == null) {
            log.error("Cannot register null state");
            return false;
        }

        String stateName = state.getName();
        if (stateName == null || stateName.isEmpty()) {
            log.error("Cannot register state with null or empty name");
            return false;
        }

        try {
            log.debug("Attempting to register state: {}", stateName);
            log.debug(
                    "State details - ID: {}, images: {}, strings: {}",
                    state.getId(),
                    state.getStateImages().size(),
                    state.getStateStrings().size());

            // Check if state already exists
            if (isStateRegistered(stateName)) {
                log.warn("State '{}' is already registered, skipping", stateName);
                return false;
            }

            // Save the state to the StateService
            stateService.save(state);

            // Verify registration
            boolean nowRegistered = isStateRegistered(stateName);

            if (nowRegistered) {
                // Get the state back to check ID
                Long savedId = stateService.getState(stateName).map(State::getId).orElse(null);

                // Single concise INFO log for successful registration
                log.info(
                        "Registered state '{}' (ID: {}) with {} images, {} strings",
                        stateName,
                        savedId,
                        state.getStateImages().size(),
                        state.getStateStrings().size());
            } else {
                log.error("Failed to register state: {}", stateName);
            }

            return nowRegistered;

        } catch (Exception e) {
            log.error("Failed to register state: {}", stateName, e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a state is already registered.
     *
     * @param stateName The name of the state to check
     * @return true if the state is registered, false otherwise
     */
    public boolean isStateRegistered(String stateName) {
        return stateService.getAllStates().stream()
                .anyMatch(state -> state.getName().equals(stateName));
    }

    /**
     * Gets the total number of registered states.
     *
     * @return The number of registered states
     */
    public int getRegisteredStateCount() {
        return stateService.getAllStates().size();
    }
}
