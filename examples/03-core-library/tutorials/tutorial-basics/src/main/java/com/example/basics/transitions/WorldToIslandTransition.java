package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.IslandState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.Transition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Transition from WORLD to ISLAND state. Demonstrates finding and clicking on dynamic content. */
@Transition(from = WorldState.class, to = IslandState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldToIslandTransition {

    private final WorldState worldState;
    private final Action action;

    /** Execute the transition */
    public boolean execute() {
        log.info("Transitioning from WORLD to ISLAND");

        // Click the search button to go to an island
        return action.click(worldState.getSearchButton()).isSuccess();
    }
}
