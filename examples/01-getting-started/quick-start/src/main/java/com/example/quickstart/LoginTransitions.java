package com.example.quickstart;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.annotations.IncomingTransition;
import io.github.jspinak.brobot.annotations.OutgoingTransition;
import io.github.jspinak.brobot.annotations.TransitionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@TransitionSet(state = LoginState.class)
@RequiredArgsConstructor
@Slf4j
public class LoginTransitions {

    private final Action action;
    private final LoginState loginState;

    @IncomingTransition(description = "Verify login screen is visible")
    public boolean verifyLoginScreen() {
        log.info("Verifying login screen visibility");
        boolean found = action.find(loginState.getLoginButton()).isSuccess();
        log.info("Login screen verification: {}", found ? "VISIBLE" : "NOT FOUND");
        return found;
    }

    @OutgoingTransition(activate = {DashboardState.class})
    public boolean login() {
        log.info("Attempting to log in");

        // Click username field and type
        boolean usernameSuccess = action.click(loginState.getUsernameField()).isSuccess();
        if (!usernameSuccess) {
            log.error("Failed to find username field");
            return false;
        }

        action.type(new ObjectCollection.Builder().withStrings("user@example.com").build());

        // Click password field and type
        boolean passwordSuccess = action.click(loginState.getPasswordField()).isSuccess();
        if (!passwordSuccess) {
            log.error("Failed to find password field");
            return false;
        }

        action.type(new ObjectCollection.Builder().withStrings("password123").build());

        // Click login button
        boolean loginSuccess = action.click(loginState.getLoginButton()).isSuccess();
        if (loginSuccess) {
            log.info("Login successful");
        } else {
            log.error("Failed to click login button");
        }

        return loginSuccess;
    }
}
