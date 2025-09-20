package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.ModalDialogState;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import io.github.jspinak.brobot.model.state.special.PreviousState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Transitions for the ModalDialog state. Uses PreviousState to return to whatever state was hidden
 * by the modal.
 */
@TransitionSet(state = ModalDialogState.class)
@Component
@RequiredArgsConstructor
@Slf4j
public class ModalDialogTransitions {

    private final ModalDialogState modalDialogState;
    private final Action action;

    @IncomingTransition
    public boolean verifyArrival() {
        log.info("Verifying arrival at ModalDialog");
        // In mock mode, always return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Confirm and close modal, returning to previous state")
    public boolean confirmAndClose() {
        log.info("Confirming and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Cancel and close modal, returning to previous state")
    public boolean cancelAndClose() {
        log.info("Cancelling and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Close modal with X button, returning to previous state")
    public boolean closeModal() {
        log.info("Closing modal with X button - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        // In mock mode, just return true
        return true;
    }
}
