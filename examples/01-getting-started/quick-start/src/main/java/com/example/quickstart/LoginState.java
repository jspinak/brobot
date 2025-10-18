package com.example.quickstart;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

@State  // Includes @Component, registers as Brobot state
@Getter
public class LoginState {
    private final StateImage loginButton = new StateImage.Builder()
            .setName("login-button")
            .addPatterns("login-button")
            .build();

    private final StateImage usernameField = new StateImage.Builder()
            .setName("username-field")
            .addPatterns("username-field")
            .build();

    private final StateImage passwordField = new StateImage.Builder()
            .setName("password-field")
            .addPatterns("password-field")
            .build();
}
