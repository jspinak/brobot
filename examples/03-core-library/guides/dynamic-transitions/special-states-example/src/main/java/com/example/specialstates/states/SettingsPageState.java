package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Settings page state that can also be hidden by the modal. Used to test that PreviousState
 * correctly returns to different hidden states.
 */
@State(description = "Settings page")
@Component
@Getter
@Slf4j
public class SettingsPageState {

    private final StateImage settingsHeader;
    private final StateImage backButton;
    private final StateImage saveButton;

    public SettingsPageState() {
        log.info("Initializing SettingsPageState");

        settingsHeader =
                new StateImage.Builder()
                        .addPatterns("settingsHeader")
                        .setName("settingsHeader")
                        .build();

        backButton = new StateImage.Builder().addPatterns("backBtn").setName("backButton").build();

        saveButton = new StateImage.Builder().addPatterns("saveBtn").setName("saveButton").build();
    }
}
