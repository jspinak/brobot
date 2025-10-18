package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;

import com.example.mrdoob.states.About;
import com.example.mrdoob.states.Harmony;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transitions for the Harmony state. Demonstrates navigation from the Harmony drawing application
 * to the About page.
 */
@TransitionSet(state = Harmony.class, description = "Harmony state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HarmonyTransitions {

    private final Harmony harmony;
    private final Action action;

    /**
     * Verify that we have successfully arrived at the Harmony page. Checks for the presence of the
     * about button.
     */
    @IncomingTransition(description = "Verify arrival at Harmony")
    public boolean verifyArrival() {
        log.info("Verifying arrival at Harmony");
        // Check for presence of about button
        boolean foundAbout = action.find(harmony.getAbout()).isSuccess();

        if (foundAbout) {
            log.info("Successfully confirmed Harmony is active");
            return true;
        } else {
            log.error("Failed to confirm Harmony - about button not found");
            return false;
        }
    }

    /**
     * Navigate from Harmony to About by clicking the about button.
     */
    @OutgoingTransition(
            activate = {About.class},
            pathCost = 1,
            description = "Navigate from Harmony to About")
    public boolean toAbout() {
        log.info("Navigating from Harmony to About");
        // Click the about button
        return action.click(harmony.getAbout()).isSuccess();
    }
}
