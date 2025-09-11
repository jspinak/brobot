package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.Transition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Transition from WORLD back to HOME state. Completes the navigation cycle. */
@Transition(from = WorldState.class, to = HomeState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class WorldToHomeTransition {

    private final WorldState worldState;
    private final Action action;

    /** Execute the transition */
    public boolean execute() {
        log.info("Transitioning from WORLD back to HOME");

        // Click the search button to go home (from the documentation pattern)
        return action.click(worldState.getSearchButton()).isSuccess();
    }
}
