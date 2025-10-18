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
 * Transitions for the MainPage state. Includes self-transitions using CurrentState.
 *
 * <p>Best Practices Demonstrated:
 *
 * <ul>
 *   <li>staysVisible=true when opening overlays (modal stays on top)
 *   <li>CurrentState for self-transitions (refresh, pagination)
 *   <li>Error handling with try-catch and logging
 *   <li>Progressive path costs (0 for instant, higher for complex operations)
 * </ul>
 */
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
        try {
            // In mock mode, always return true
            // In real mode, would check for main page visibility:
            // return action.find(mainPageState.getLogo()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error verifying arrival at MainPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {ModalDialogState.class},
            staysVisible = true, // MainPage stays visible behind modal
            pathCost = 0,
            description = "Open modal dialog over main page")
    public boolean openModal() {
        log.info("Opening modal dialog from MainPage");
        log.debug("MainPage will remain visible behind the modal (staysVisible=true)");
        try {
            // In real implementation, would click button to open modal:
            // return action.click(mainPageState.getMenuButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error opening modal from MainPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {SettingsPageState.class},
            pathCost = 1,
            description = "Navigate to settings page")
    public boolean toSettings() {
        log.info("Navigating from MainPage to Settings");
        try {
            // In real implementation, would click settings button:
            // return action.click(mainPageState.getSettingsButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error navigating to Settings", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 2,
            description = "Refresh main page")
    public boolean refresh() {
        log.info("Refreshing MainPage (self-transition using CurrentState)");
        log.debug("This demonstrates a self-transition - stays in MainPage");
        try {
            // In real implementation, would refresh the page:
            // return action.click(mainPageState.getRefreshButton()).isSuccess();
            // or: return action.type("{F5}").isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error refreshing MainPage", e);
            return false;
        }
    }

    @OutgoingTransition(
            activate = {CurrentState.class}, // Self-transition
            pathCost = 3,
            description = "Load next page of results")
    public boolean nextPage() {
        log.info("Loading next page of results (self-transition using CurrentState)");
        log.debug("Another self-transition example - pagination within MainPage");
        try {
            // In real implementation, would click next page button:
            // return action.click(mainPageState.getNextPageButton()).isSuccess();
            return true;
        } catch (Exception e) {
            log.error("Error loading next page", e);
            return false;
        }
    }
}
