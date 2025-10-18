package com.example.mrdoob.transitions;

import org.springframework.stereotype.Component;

import com.example.mrdoob.states.About;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transitions for the About state. This is the terminal state in the Mr.doob tutorial navigation
 * flow.
 */
@TransitionSet(state = About.class, description = "About state transitions")
@Component
@RequiredArgsConstructor
@Slf4j
public class AboutTransitions {

    private final About about;
    private final Action action;

    /**
     * Verify that we have successfully arrived at the About page. Checks for the presence of the
     * about text.
     */
    @IncomingTransition(description = "Verify arrival at About")
    public boolean verifyArrival() {
        log.info("Verifying arrival at About");
        // Check for presence of about text
        boolean foundAboutText = action.find(about.getAboutText()).isSuccess();

        if (foundAboutText) {
            log.info("Successfully confirmed About page is active");
            return true;
        } else {
            log.error("Failed to confirm About page - about text not found");
            return false;
        }
    }
}
