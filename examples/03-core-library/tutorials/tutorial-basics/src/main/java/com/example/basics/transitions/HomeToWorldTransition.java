package com.example.basics.transitions;

import org.springframework.stereotype.Component;

import com.example.basics.states.HomeState;
import com.example.basics.states.WorldState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.Transition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transition from HOME to WORLD state. Demonstrates state transition with @Transition annotation in
 * v1.1.0.
 */
@Transition(from = HomeState.class, to = WorldState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class HomeToWorldTransition {

    private final HomeState homeState;
    private final Action action;

    /** Execute the transition */
    public boolean execute() {
        log.info("Transitioning from Home to World");
        return action.click(homeState.getToWorldButton()).isSuccess();
    }
}
