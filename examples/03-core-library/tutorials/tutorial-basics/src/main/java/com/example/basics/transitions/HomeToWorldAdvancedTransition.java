package com.example.basics.transitions;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Advanced transition from HOME to WORLD with error handling.
 * Demonstrates ConditionalActionChain for robust transitions.
 */
@Transition(from = HomeState.class, to = WorldState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeToWorldAdvancedTransition {
    
    private final HomeState homeState;
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Execute the transition with robust error handling
     */
    public boolean execute() {
        log.info("Starting advanced Home to World transition");
        
        // Simplified version - ConditionalActionChain API has changed
        ActionResult result = action.click(homeState.getToWorldButton());
        
        if (!result.isSuccess()) {
            log.warn("Primary to-world button not found, trying search button");
            result = action.click(homeState.getSearchButton());
        }
        
        if (!result.isSuccess()) {
            log.error("World state not reached");
        }
        
        return result.isSuccess();
    }
}