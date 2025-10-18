package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal dialog state that overlays other pages. This state specifies which states it can hide using
 * the canHide parameter, which is REQUIRED for PreviousState transitions to work correctly.
 */
@State(
        description = "Modal dialog overlay",
        canHide = {
            "MainPage",
            "SettingsPage"
        } // CRITICAL: List all states that can be hidden by this modal
        )
@Component
@Getter
@Slf4j
public class ModalDialogState {

    private final StateImage dialogTitle;
    private final StateImage confirmButton;
    private final StateImage cancelButton;
    private final StateImage closeButton;

    public ModalDialogState() {
        log.info("Initializing ModalDialogState");

        dialogTitle =
                new StateImage.Builder().addPatterns("dialogTitle").setName("dialogTitle").build();

        confirmButton =
                new StateImage.Builder().addPatterns("confirmBtn").setName("confirmButton").build();

        cancelButton =
                new StateImage.Builder().addPatterns("cancelBtn").setName("cancelButton").build();

        closeButton =
                new StateImage.Builder()
                        .addPatterns("closeBtn", "xButton")
                        .setName("closeButton")
                        .build();

        log.info("ModalDialog configured to hide: MainPage, SettingsPage");
    }
}
