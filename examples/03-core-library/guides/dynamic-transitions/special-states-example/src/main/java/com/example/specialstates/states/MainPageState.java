package com.example.specialstates.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Main page state that serves as the base state of the application. This state can be hidden by
 * modal states.
 */
@State(initial = true, description = "Main application page")
@Component
@Getter
@Slf4j
public class MainPageState {

    private final StateImage logo;
    private final StateImage menuButton;
    private final StateImage settingsButton;
    private final StateImage refreshButton;
    private final StateImage nextPageButton;

    public MainPageState() {
        log.info("Initializing MainPageState");

        logo = new StateImage.Builder().addPatterns("mainLogo").setName("logo").build();

        menuButton = new StateImage.Builder().addPatterns("menuBtn").setName("menuButton").build();

        settingsButton =
                new StateImage.Builder()
                        .addPatterns("settingsBtn")
                        .setName("settingsButton")
                        .build();

        refreshButton =
                new StateImage.Builder().addPatterns("refreshBtn").setName("refreshButton").build();

        nextPageButton =
                new StateImage.Builder()
                        .addPatterns("nextPageBtn")
                        .setName("nextPageButton")
                        .build();
    }
}
