package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;

import com.example.mrdoob.states.Harmony;
import com.example.mrdoob.states.Homepage;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transitions for the Homepage state. Demonstrates navigation from the Mr.doob homepage to the
 * Harmony drawing application.
 */
@TransitionSet(state = Homepage.class, description = "Homepage state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class HomepageTransitions {

    private final Homepage homepage;
    private final Action action;

    /**
     * Verify that we have successfully arrived at the Homepage. Checks for the presence of the
     * harmony icon.
     */
    @IncomingTransition(description = "Verify arrival at Homepage")
    public boolean verifyArrival() {
        log.info("Verifying arrival at Homepage");
        // Check for presence of harmony icon
        boolean foundHarmony = action.find(homepage.getHarmony()).isSuccess();

        if (foundHarmony) {
            log.info("Successfully confirmed Homepage is active");
            return true;
        } else {
            log.error("Failed to confirm Homepage - harmony icon not found");
            return false;
        }
    }

    /**
     * Navigate from Homepage to Harmony by clicking the harmony icon.
     */
    @OutgoingTransition(
            activate = {Harmony.class},
            pathCost = 1,
            description = "Navigate from Homepage to Harmony")
    public boolean toHarmony() {
        log.info("Navigating from Homepage to Harmony");
        // Click the harmony icon
        return action.click(homepage.getHarmony()).isSuccess();
    }
}
