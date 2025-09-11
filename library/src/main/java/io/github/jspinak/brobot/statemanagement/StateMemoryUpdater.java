package io.github.jspinak.brobot.statemanagement;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Updates StateMemory based on matches found during action execution.
 *
 * <p>This component bridges the gap between action execution and state management by automatically
 * updating the StateMemory when images belonging to states are found. This ensures that the
 * framework maintains an accurate understanding of which states are currently active based on
 * visual evidence.
 *
 * @since 1.1
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateMemoryUpdater {

    private final StateMemory stateMemory;
    private final StateService stateService;

    /**
     * Updates StateMemory based on matches found in an ActionResult. When an image is found, the
     * state it belongs to is set as active.
     *
     * @param actionResult The result containing matches to process
     */
    public void updateFromActionResult(ActionResult actionResult) {
        if (actionResult == null || actionResult.getMatchList().isEmpty()) {
            return;
        }

        Set<Long> stateIdsToActivate = new HashSet<>();
        Set<String> stateNamesToActivate = new HashSet<>();

        for (Match match : actionResult.getMatchList()) {
            if (match != null && match.getStateObjectData() != null) {
                String ownerStateName = match.getStateObjectData().getOwnerStateName();
                if (ownerStateName != null && !ownerStateName.isEmpty()) {
                    stateNamesToActivate.add(ownerStateName);

                    // Get the state ID for this state name
                    stateService
                            .getState(ownerStateName)
                            .ifPresent(
                                    state -> {
                                        Long stateId = state.getId();
                                        if (stateId != null && stateId > 0) {
                                            stateIdsToActivate.add(stateId);
                                        }
                                    });
                }
            }
        }

        // Activate the states
        for (Long stateId : stateIdsToActivate) {
            if (!stateMemory.getActiveStates().contains(stateId)) {
                log.debug(
                        "Activating state {} based on found match",
                        stateService.getStateName(stateId));
                stateMemory.addActiveState(stateId);
            }
        }

        if (!stateIdsToActivate.isEmpty()) {
            log.info(
                    "Updated active states based on {} found matches. Active states: {}",
                    actionResult.getMatchList().size(),
                    stateMemory.getActiveStateNames());
        }
    }

    /**
     * Updates StateMemory from a single match.
     *
     * @param match The match to process
     */
    public void updateFromMatch(Match match) {
        if (match == null || match.getStateObjectData() == null) {
            return;
        }

        String ownerStateName = match.getStateObjectData().getOwnerStateName();
        if (ownerStateName == null || ownerStateName.isEmpty()) {
            return;
        }

        stateService
                .getState(ownerStateName)
                .ifPresent(
                        state -> {
                            Long stateId = state.getId();
                            if (stateId != null
                                    && stateId > 0
                                    && !stateMemory.getActiveStates().contains(stateId)) {
                                log.debug(
                                        "Activating state {} ({}) based on found match",
                                        ownerStateName,
                                        stateId);
                                stateMemory.addActiveState(stateId);
                            }
                        });
    }
}
