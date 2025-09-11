package com.example.logging.workflows;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * Real-world login workflow example with comprehensive logging. Based on the documentation example.
 */
@Slf4j
@Component
public class LoginWorkflow {

    private final Action action;

    public LoginWorkflow(Action action) {
        this.action = action;
    }

    /** Performs a complete login workflow with detailed logging */
    public boolean login(String username, String password) {
        log.info("Starting login workflow for user: {}", username);

        // Set workflow context
        MDC.put("workflow", "login");
        MDC.put("username", username);

        StateImage usernameField =
                new StateImage.Builder()
                        .setName("UsernameField")
                        .addPatterns("username_field.png")
                        .build();

        StateImage passwordField =
                new StateImage.Builder()
                        .setName("PasswordField")
                        .addPatterns("password_field.png")
                        .build();

        StateImage loginButton =
                new StateImage.Builder()
                        .setName("LoginButton")
                        .addPatterns("login_button.png")
                        .build();

        try {
            // Find and fill username
            MDC.put("step", "username");
            log.debug("Looking for username field");

            ObjectCollection usernameCollection =
                    new ObjectCollection.Builder().withImages(usernameField).build();

            ActionResult userResult =
                    action.perform(
                            new PatternFindOptions.Builder()
                                    .setSimilarity(0.8)
                                    .setSearchDuration(5.0)
                                    .build(),
                            usernameCollection);

            if (!userResult.isSuccess()) {
                log.error("Username field not found");
                return false;
            }

            Optional<Match> userMatch = userResult.getBestMatch();
            if (userMatch.isPresent()) {
                log.debug("Username field found at {}", userMatch.get().getRegion());
            }

            log.debug("Clicking username field");
            ActionResult clickResult =
                    action.perform(
                            new ClickOptions.Builder().setPauseAfterEnd(0.2).build(),
                            usernameCollection);

            if (!clickResult.isSuccess()) {
                log.error("Failed to click username field");
                return false;
            }

            log.debug("Typing username");
            action.perform(
                    new TypeOptions.Builder().setTypeDelay(0.05).build(),
                    new ObjectCollection.Builder().withStrings(username).build());

            log.info("Username entered successfully");

            // Find and fill password
            MDC.put("step", "password");
            log.debug("Looking for password field");

            ObjectCollection passwordCollection =
                    new ObjectCollection.Builder().withImages(passwordField).build();

            ActionResult passResult =
                    action.perform(
                            new PatternFindOptions.Builder()
                                    .setSimilarity(0.8)
                                    .setSearchDuration(5.0)
                                    .build(),
                            passwordCollection);

            if (!passResult.isSuccess()) {
                log.error("Password field not found");
                return false;
            }

            Optional<Match> passMatch = passResult.getBestMatch();
            if (passMatch.isPresent()) {
                log.debug("Password field found at {}", passMatch.get().getRegion());
            }

            log.debug("Clicking password field");
            action.perform(
                    new ClickOptions.Builder().setPauseAfterEnd(0.2).build(), passwordCollection);

            log.debug("Typing password (masked in logs)");
            action.perform(
                    new TypeOptions.Builder().setTypeDelay(0.05).build(),
                    new ObjectCollection.Builder().withStrings(password).build());

            log.info("Password entered successfully");

            // Submit
            MDC.put("step", "submit");
            log.debug("Looking for login button");

            ObjectCollection loginCollection =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            ActionResult loginButtonResult =
                    action.perform(
                            new PatternFindOptions.Builder()
                                    .setSimilarity(0.9)
                                    .setSearchDuration(5.0)
                                    .build(),
                            loginCollection);

            if (!loginButtonResult.isSuccess()) {
                log.error("Login button not found");
                return false;
            }

            log.info("Clicking login button");
            ActionResult loginResult =
                    action.perform(
                            new ClickOptions.Builder()
                                    .setPauseAfterEnd(2.0) // Wait for login
                                    .build(),
                            loginCollection);

            if (loginResult.isSuccess()) {
                log.info("Login button clicked successfully");

                // Verify login success
                boolean loginSuccessful = verifyLoginSuccess();

                if (loginSuccessful) {
                    log.info("Login successful - user authenticated");
                    return true;
                } else {
                    log.error("Login failed - authentication rejected");
                    return false;
                }
            } else {
                log.error("Failed to click login button");
                return false;
            }

        } catch (Exception e) {
            log.error("Login workflow failed with exception", e);
            return false;
        } finally {
            // Clear context
            MDC.clear();
        }
    }

    /** Verifies that login was successful */
    private boolean verifyLoginSuccess() {
        MDC.put("step", "verify");
        log.debug("Verifying login success");

        // Check for dashboard or success indicator
        StateImage dashboardElement =
                new StateImage.Builder()
                        .setName("DashboardElement")
                        .addPatterns("dashboard_element.png")
                        .build();

        StateImage errorMessage =
                new StateImage.Builder()
                        .setName("LoginError")
                        .addPatterns("login_error.png")
                        .build();

        // First check for error
        ObjectCollection errorCollection =
                new ObjectCollection.Builder().withImages(errorMessage).build();

        ActionResult errorCheck =
                action.perform(
                        new PatternFindOptions.Builder()
                                .setSimilarity(0.8)
                                .setSearchDuration(2.0)
                                .build(),
                        errorCollection);

        if (errorCheck.isSuccess()) {
            log.warn("Login error message detected");
            return false;
        }

        // Then check for success
        ObjectCollection dashboardCollection =
                new ObjectCollection.Builder().withImages(dashboardElement).build();

        ActionResult successCheck =
                action.perform(
                        new PatternFindOptions.Builder()
                                .setSimilarity(0.8)
                                .setSearchDuration(5.0)
                                .build(),
                        dashboardCollection);

        if (successCheck.isSuccess()) {
            log.debug("Dashboard element found - login verified");
            return true;
        } else {
            log.warn("Dashboard element not found - login may have failed");
            return false;
        }
    }

    /** Demonstrates the login workflow */
    public void demonstrateLogin() {
        log.info("=== Login Workflow Demonstration ===");

        // Attempt login with test credentials
        boolean success = login("demo_user", "demo_password");

        if (success) {
            log.info("✓ Login workflow completed successfully");
        } else {
            log.error("✗ Login workflow failed");

            // Log additional diagnostics
            log.info("Common failure reasons:");
            log.info("  - UI elements have changed");
            log.info("  - Similarity threshold too high");
            log.info("  - Timing issues (increase pauses)");
            log.info("  - Invalid credentials");
        }
    }
}
