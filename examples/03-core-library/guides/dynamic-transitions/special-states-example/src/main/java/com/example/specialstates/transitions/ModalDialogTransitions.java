package com.example.specialstates.transitions;

import org.springframework.stereotype.Component;

import com.example.specialstates.states.MainPageState;
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
 * by the modal, with fallback transitions to MainPage if no hidden states exist.
 *
 * <p>Best Practices Demonstrated:
 *
 * <ul>
 *   <li>PreviousState transitions with low pathCost (0) for primary navigation
 *   <li>Fallback transitions with higher pathCost (10) for edge cases
 *   <li>Error handling with logging to track transition failures
 *   <li>Descriptive comments explaining the transition logic
 * </ul>
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
        try {
            // In mock mode, always return true
            // In real mode, would check for dialog visibility
            return true;
        } catch (Exception e) {
            log.error("Error verifying arrival at ModalDialog", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Confirm and close modal, returning to previous state")
    public boolean confirmAndClose() {
        log.info("Confirming and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        try {
            // In real implementation, would click confirm button:
            // return action.click(modalDialogState.getConfirmButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error confirming and closing modal", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Cancel and close modal, returning to previous state")
    public boolean cancelAndClose() {
        log.info("Cancelling and closing modal - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        try {
            // In real implementation, would click cancel button:
            // return action.click(modalDialogState.getCancelButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error cancelling and closing modal", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {PreviousState.class}, // Return to whatever state was hidden
            staysVisible = false, // Modal closes completely
            pathCost = 0,
            description = "Close modal with X button, returning to previous state")
    public boolean closeModal() {
        log.info("Closing modal with X button - returning to PreviousState");
        log.info("This should return to whatever state was hidden (MainPage or SettingsPage)");
        try {
            // In real implementation, would click close button:
            // return action.click(modalDialogState.getCloseButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error closing modal with X button", e);
            return false;
        }
    }

    // FALLBACK TRANSITIONS - These provide safety when PreviousState is unavailable

    @OutgoingTransition(
            activate = {MainPageState.class}, // Explicit fallback to MainPage
            staysVisible = false,
            pathCost = 10, // Higher cost - only used if PreviousState fails
            description =
                    "Fallback: Close modal and navigate to MainPage if no hidden state exists")
    public boolean closeToMainPage() {
        log.warn("FALLBACK: No hidden states found, navigating to MainPage as fallback");
        log.info("This fallback ensures modal can always be closed, even without PreviousState");
        try {
            // In real implementation, would press ESC or click close:
            // action.type("{ESC}");
            // return action.click(mainPageButton).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error in fallback transition to MainPage", e);
            return false;
        }
    }
}
