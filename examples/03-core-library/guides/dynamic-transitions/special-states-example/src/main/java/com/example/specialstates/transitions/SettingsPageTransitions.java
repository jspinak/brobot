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

/**
 * Transitions for the SettingsPage state. Can also open modal and includes self-transitions.
 *
 * <p>Best Practices Demonstrated:
 *
 * <ul>
 *   <li>staysVisible=true when opening modal (Settings stays behind modal)
 *   <li>CurrentState for in-page actions (save without leaving page)
 *   <li>Error handling with try-catch blocks
 *   <li>Clear logging at different levels (info, debug, error)
 * </ul>
 */
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
        try {
            // In mock mode, always return true
            // In real mode, would check for settings page visibility:
            // return action.find(settingsPageState.getSettingsHeader()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error verifying arrival at SettingsPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {MainPageState.class},
            pathCost = 1,
            description = "Navigate back to main page")
    public boolean backToMain() {
        log.info("Navigating from SettingsPage to MainPage");
        try {
            // In real implementation, would click back button:
            // return action.click(settingsPageState.getBackButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error navigating back to MainPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // Settings stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over settings page")
    public boolean openModal() {
        log.info("Opening modal dialog from SettingsPage");
        log.debug("This will make SettingsPage the hidden state for the modal");
        try {
            // In real implementation, would trigger modal:
            // Could be a button click, keyboard shortcut, etc.
            // return action.click(settingsPageState.getHelpButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error opening modal from SettingsPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Save settings and stay on page")
    public boolean saveSettings() {
        log.info("Saving settings (self-transition using CurrentState)");
        log.debug("Demonstrates CurrentState usage - settings saved but page doesn't change");
        try {
            // In real implementation, would click save button:
            // return action.click(settingsPageState.getSaveButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error saving settings", e);
            return false;
        }
    }
}
