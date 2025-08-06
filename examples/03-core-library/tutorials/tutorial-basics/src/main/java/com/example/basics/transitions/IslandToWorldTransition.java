package com.example.basics.transitions;

import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transition from ISLAND back to WORLD state.
 * Demonstrates simple navigation transition.
 */
@Transition(from = IslandState.class, to = WorldState.class)
@RequiredArgsConstructor
@Slf4j
public class IslandToWorldTransition {
    
    private final IslandState islandState;
    private final Action action;
    
    /**
     * Execute the transition
     */
    public boolean execute() {
        log.info("Transitioning from ISLAND back to WORLD");
        
        // Click the "Back to World" button
        return action.click(islandState.getBackToWorldButton()).isSuccess();
    }
}