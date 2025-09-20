package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.MainPageState;
import com.example.specialstates.states.ModalDialogState;
import com.example.specialstates.states.SettingsPageState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.special.CurrentState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Transitions for the MainPage state. Includes self-transitions using CurrentState. */
@TransitionSet(state = MainPageState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class MainPageTransitions {

    private final MainPageState mainPageState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at MainPage");
        // In mock mode, always return true
        return true;
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // MainPage stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over main page")
    public boolean openModal() {
        log.info("Opening modal dialog from MainPage");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {SettingsPageState.class},
            pathCost = 1,
            description = "Navigate to settings page")
    public boolean toSettings() {
        log.info("Navigating from MainPage to Settings");
        return true;
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Refresh main page")
    public boolean refresh() {
        log.info("Refreshing MainPage (self-transition using CurrentState)");
        // This demonstrates a self-transition
        return true;
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 3,
            description = "Load next page of results")
    public boolean nextPage() {
        log.info("Loading next page of results (self-transition using CurrentState)");
        // Another self-transition example
        return true;
    }
}
