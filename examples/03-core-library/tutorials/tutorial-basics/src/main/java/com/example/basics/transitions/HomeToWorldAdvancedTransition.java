package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.annotations.Transition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Advanced transition from HOME to WORLD with error handling. Demonstrates ConditionalActionChain
 * for robust transitions.
 */
@Transition(from = HomeState.class, to = WorldState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeToWorldAdvancedTransition {

    private final HomeState homeState;
    private final WorldState worldState;
    private final Action action;

    /** Execute the transition with robust error handling */
    public boolean execute() {
        log.info("Starting game from main menu");

        try {
            // First try to click the to-world button
            ActionResult clickResult = action.click(homeState.getToWorldButton());

            if (!clickResult.isSuccess()) {
                log.warn("To-world button not found, trying alternative");
                clickResult = action.click(homeState.getSearchButton());

                if (!clickResult.isSuccess()) {
                    log.error("Neither button could be clicked");
                    return false;
                }
            }

            // Wait for world state to load (using findWithTimeout instead of waitFor)
            ActionResult waitResult = action.findWithTimeout(10.0, worldState.getMinimap());

            if (!waitResult.isSuccess()) {
                log.error("World state did not load");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Transition failed with exception: ", e);
            return false;
        }
    }
}
