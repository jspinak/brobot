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
            log.info("=== ATTEMPTING STATE REGISTRATION ===");
            log.info("State name: {}", stateName);
            log.info("State ID (before save): {}", state.getId());
            log.info("State images: {}", state.getStateImages().size());
            log.info("State strings: {}", state.getStateStrings().size());

            // Check if state already exists
            if (isStateRegistered(stateName)) {
                log.warn("State '{}' is already registered, skipping", stateName);
                return false;
            }

            // Save the state to the StateService
            log.info("Calling stateService.save() for state: {}", stateName);
            stateService.save(state);

            // Verify registration
            boolean nowRegistered = isStateRegistered(stateName);
            log.info("State '{}' registered successfully: {}", stateName, nowRegistered);

            // Get the state back to check ID
            stateService
                    .getState(stateName)
                    .ifPresent(
                            savedState -> {
                                log.info("Saved state ID: {}", savedState.getId());
                            });

            log.info(
                    "Successfully registered state: {} with {} images, {} strings",
                    stateName,
                    state.getStateImages().size(),
                    state.getStateStrings().size());

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
