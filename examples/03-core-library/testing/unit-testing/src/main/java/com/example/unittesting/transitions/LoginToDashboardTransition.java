package com.example.unittesting.transitions;

import org.springframework.stereotype.Component;

import com.example.unittesting.states.DashboardState;
import com.example.unittesting.states.LoginState;

import io.github.jspinak.brobot.annotations.Transition;
import io.github.jspinak.brobot.annotations.TransitionStep;

import lombok.extern.slf4j.Slf4j;

/** Transition from login to dashboard state. */
@Transition(from = LoginState.class, to = DashboardState.class, priority = 1)
@Component
@Slf4j
public class LoginToDashboardTransition {

    @TransitionStep(order = 1)
    public void enterCredentials() {
        log.info("Step 1: Entering username and password");
        // In real implementation, would use Actions to type in fields
    }

    @TransitionStep(order = 2)
    public void clickLogin() {
        log.info("Step 2: Clicking login button");
        // In real implementation, would use Actions to click button
    }

    @TransitionStep(order = 3)
    public void waitForDashboard() {
        log.info("Step 3: Waiting for dashboard to appear");
        // In real implementation, would wait for dashboard elements
    }

    // Helper methods for testing
    public boolean validateCredentials(String username, String password) {
        return username != null
                && !username.isEmpty()
                && password != null
                && password.length() >= 8;
    }

    public String getTransitionDescription() {
        return "Login to Dashboard transition with credential validation";
    }
}
