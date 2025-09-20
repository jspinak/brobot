package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Modal dialog state that overlays the main page. This state hides whatever state was active before
 * it.
 */
@State(description = "Modal dialog overlay")
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
    }
}
