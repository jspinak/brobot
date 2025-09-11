package com.example.unittesting.states;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.annotations.StateObject;
import io.github.jspinak.brobot.annotations.StateString;

import lombok.Getter;

/** Example login state for testing. */
@State(name = "login")
@Component
@Getter
public class LoginState {

    @StateObject(name = "username_field")
    private String usernameField = "username_input.png";

    @StateObject(name = "password_field")
    private String passwordField = "password_input.png";

    @StateObject(name = "login_button")
    private String loginButton = "login_btn.png";

    @StateObject(name = "remember_me_checkbox")
    private String rememberMeCheckbox = "remember_me.png";

    @StateString(name = "login_title")
    private String loginTitle = "Welcome to Brobot";

    @StateString(name = "error_message")
    private String errorMessage = "Invalid username or password";

    // Additional methods for state behavior
    public boolean hasRequiredElements() {
        return usernameField != null && passwordField != null && loginButton != null;
    }

    public String getStateDescription() {
        return "Login state with username, password, and login button";
    }
}
