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

/** Transitions for the SettingsPage state. Can also open modal and includes self-transitions. */
@TransitionSet(state = SettingsPageState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class SettingsPageTransitions {

    private final SettingsPageState settingsPageState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at SettingsPage");
        // In mock mode, always return true
        return true;
    }

    @OutgoingTransition(
            activate = {MainPageState.class},
            pathCost = 1,
            description = "Navigate back to main page")
    public boolean backToMain() {
        log.info("Navigating from SettingsPage to MainPage");
        return true;
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // Settings stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over settings page")
    public boolean openModal() {
        log.info("Opening modal dialog from SettingsPage");
        // This will make SettingsPage the hidden state for the modal
        return true;
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Save settings and stay on page")
    public boolean saveSettings() {
        log.info("Saving settings (self-transition using CurrentState)");
        // Demonstrates CurrentState usage in settings
        return true;
    }
}
