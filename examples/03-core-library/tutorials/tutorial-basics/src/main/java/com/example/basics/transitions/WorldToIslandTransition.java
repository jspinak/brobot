package com.example.basics.transitions;

import com.example.basics.states.WorldState;
import com.example.basics.states.IslandState;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.annotations.Transition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Transition from WORLD to ISLAND state.
 * Demonstrates finding and clicking on dynamic content.
 */
@Transition(from = WorldState.class, to = IslandState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldToIslandTransition {
    
    private final WorldState worldState;
    private final Action action;
    
    /**
     * Execute the transition
     */
    public boolean execute() {
        log.info("Transitioning from WORLD to ISLAND");
        
        // Click the search button to go to an island
        return action.click(worldState.getSearchButton()).isSuccess();
    }
}