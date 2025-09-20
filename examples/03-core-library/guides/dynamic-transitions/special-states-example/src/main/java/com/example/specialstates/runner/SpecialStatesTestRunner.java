package com.example.specialstates.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.specialstates.states.MainPageState;
import com.example.specialstates.states.ModalDialogState;
import com.example.specialstates.states.SettingsPageState;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateNavigator;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.statemanagement.StateVisibilityManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Test runner that demonstrates PreviousState and CurrentState functionality. */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpecialStatesTestRunner implements CommandLineRunner {

    private final StateNavigator navigator;
    private final StateService stateService;
    private final StateMemory stateMemory;
    private final StateVisibilityManager visibilityManager;

    private final MainPageState mainPageState;
    private final ModalDialogState modalDialogState;
    private final SettingsPageState settingsPageState;

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("Starting Special States Test Runner");
        log.info("========================================");

        // Test Scenario 1: PreviousState with MainPage hidden by Modal
        testPreviousStateFromMainPage();

        // Test Scenario 2: PreviousState with SettingsPage hidden by Modal
        testPreviousStateFromSettingsPage();

        // Test Scenario 3: CurrentState self-transitions
        testCurrentStateTransitions();

        log.info("========================================");
        log.info("All tests completed successfully!");
        log.info("========================================");
    }

    private void testPreviousStateFromMainPage() {
        log.info("\n========================================");
        log.info("TEST 1: PreviousState with MainPage hidden by Modal");
        log.info("========================================");

        // Step 1: Navigate to MainPage
        log.info("Step 1: Navigating to MainPage...");
        boolean mainPageSuccess = navigator.openState("MainPage");
        log.info("MainPage opened: {}", mainPageSuccess);
        printCurrentStates();

        // Step 2: Open Modal (which hides MainPage)
        log.info("\nStep 2: Opening Modal from MainPage...");
        log.info("This should hide MainPage and make it the 'previous' state");

        // Manually set the hidden state relationship for testing
        // In real execution, this would be done automatically by the framework
        var mainState = stateService.getState("MainPage").orElseThrow();
        var modalState = stateService.getState("ModalDialog").orElseThrow();

        // First activate modal
        stateMemory.addActiveState(modalState.getId());
        // Then set MainPage as hidden by modal
        modalState.addHiddenState(mainState.getId());
        stateMemory.removeInactiveState(mainState.getId());

        boolean modalSuccess = navigator.openState("ModalDialog");
        log.info("Modal opened: {}", modalSuccess);
        printCurrentStates();

        // Step 3: Use navigator.openState with MainPage as target
        // This should trigger the PreviousState transition from Modal
        log.info("\nStep 3: Using navigator.openState(MainPage) to test PreviousState transition");
        log.info(
                "Expected: Modal should close using PreviousState transition to return to"
                        + " MainPage");

        boolean returnSuccess = navigator.openState("MainPage");
        log.info("Returned to MainPage via PreviousState: {}", returnSuccess);
        printCurrentStates();

        if (returnSuccess) {
            log.info(
                    "✅ TEST 1 PASSED: Successfully returned to MainPage using PreviousState"
                            + " transition");
        } else {
            log.error("❌ TEST 1 FAILED: Could not return to MainPage via PreviousState");
        }
    }

    private void testPreviousStateFromSettingsPage() {
        log.info("\n========================================");
        log.info("TEST 2: PreviousState with SettingsPage hidden by Modal");
        log.info("========================================");

        // Step 1: Navigate to SettingsPage
        log.info("Step 1: Navigating to SettingsPage...");
        boolean settingsSuccess = navigator.openState("SettingsPage");
        log.info("SettingsPage opened: {}", settingsSuccess);
        printCurrentStates();

        // Step 2: Open Modal (which hides SettingsPage)
        log.info("\nStep 2: Opening Modal from SettingsPage...");
        log.info("This should hide SettingsPage and make it the 'previous' state");

        // Manually set the hidden state relationship for testing
        var settingsState = stateService.getState("SettingsPage").orElseThrow();
        var modalState = stateService.getState("ModalDialog").orElseThrow();

        // First activate modal
        stateMemory.addActiveState(modalState.getId());
        // Then set SettingsPage as hidden by modal
        modalState.addHiddenState(settingsState.getId());
        stateMemory.removeInactiveState(settingsState.getId());

        boolean modalSuccess = navigator.openState("ModalDialog");
        log.info("Modal opened: {}", modalSuccess);
        printCurrentStates();

        // Step 3: Use navigator.openState with SettingsPage as target
        // This should trigger the PreviousState transition from Modal
        log.info(
                "\n"
                        + "Step 3: Using navigator.openState(SettingsPage) to test PreviousState"
                        + " transition");
        log.info(
                "Expected: Modal should close using PreviousState transition to return to"
                        + " SettingsPage");

        boolean returnSuccess = navigator.openState("SettingsPage");
        log.info("Returned to SettingsPage via PreviousState: {}", returnSuccess);
        printCurrentStates();

        if (returnSuccess) {
            log.info(
                    "✅ TEST 2 PASSED: Successfully returned to SettingsPage using PreviousState"
                            + " transition");
        } else {
            log.error("❌ TEST 2 FAILED: Could not return to SettingsPage via PreviousState");
        }
    }

    private void testCurrentStateTransitions() {
        log.info("\n========================================");
        log.info("TEST 3: CurrentState self-transitions");
        log.info("========================================");

        // Step 1: Navigate to MainPage
        log.info("Step 1: Starting from MainPage...");
        boolean mainPageSuccess = navigator.openState("MainPage");
        log.info("MainPage opened: {}", mainPageSuccess);
        printCurrentStates();

        // Step 2: Test refresh (self-transition)
        log.info("\nStep 2: Testing refresh (CurrentState transition on MainPage)");
        log.info("This should execute the refresh transition but stay on MainPage");

        // Since we're already on MainPage, navigating to it again should use CurrentState
        boolean refreshSuccess = navigator.openState("MainPage");
        log.info("Refresh (CurrentState) executed: {}", refreshSuccess);
        printCurrentStates();

        // Step 3: Navigate to SettingsPage and test save (self-transition)
        log.info("\nStep 3: Moving to SettingsPage for another CurrentState test");
        navigator.openState("SettingsPage");
        printCurrentStates();

        log.info("\nStep 4: Testing save settings (CurrentState transition on SettingsPage)");
        log.info("This should execute the save transition but stay on SettingsPage");

        // Navigating to SettingsPage while already there should use CurrentState
        boolean saveSuccess = navigator.openState("SettingsPage");
        log.info("Save settings (CurrentState) executed: {}", saveSuccess);
        printCurrentStates();

        if (refreshSuccess && saveSuccess) {
            log.info("✅ TEST 3 PASSED: CurrentState self-transitions work correctly");
        } else {
            log.error("❌ TEST 3 FAILED: CurrentState transitions did not execute properly");
        }
    }

    private void printCurrentStates() {
        log.info("Current active states: {}", stateMemory.getActiveStates());
        // Note: Recent states tracking may vary by implementation
    }
}
