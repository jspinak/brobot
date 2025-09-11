package com.example.logging.examples;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates how to add logging to action chains for better debugging and monitoring.
 *
 * <p>Key features shown: - Step-by-step logging in action chains - Conditional logging based on
 * action results - MDC (Mapped Diagnostic Context) for structured logging - Performance timing for
 * chains
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionChainLoggingExample {

    private final Action action;

    // UI elements
    private final StateImage loginButton =
            new StateImage.Builder().setName("LoginButton").addPatterns("login_button.png").build();

    private final StateImage usernameField =
            new StateImage.Builder()
                    .setName("UsernameField")
                    .addPatterns("username_field.png")
                    .build();

    private final StateImage passwordField =
            new StateImage.Builder()
                    .setName("PasswordField")
                    .addPatterns("password_field.png")
                    .build();

    private final StateImage submitButton =
            new StateImage.Builder()
                    .setName("SubmitButton")
                    .addPatterns("submit_button.png")
                    .build();

    private final StateImage errorMessage =
            new StateImage.Builder()
                    .setName("ErrorMessage")
                    .addPatterns("error_message.png")
                    .build();

    /**
     * Demonstrates using separate action chains for each step with logging
     *
     * <p>Note: This example shows manual sequential execution for educational purposes. For
     * production code, consider using ConditionalActionChain:
     *
     * <p>ConditionalActionChain.find(loginButtonCollection, findOptions)
     * .ifFoundClick(clickOptions) .ifNotFoundLog("Login button not found") .then()
     * .find(usernameCollection, findOptions) .ifFoundClick() .ifFoundType("testuser")
     * .perform(action);
     */
    public void loginChainWithLogging() {
        log.info("=== Login Chain with Logging Example ===");

        // In v1.1.0, we need to execute actions sequentially instead of chaining
        boolean success = true;

        // Step 1: Find and click login button
        ObjectCollection loginButtonCollection =
                new ObjectCollection.Builder().withImages(loginButton).build();

        log.info("Looking for login button...");
        ActionResult loginFindResult =
                action.perform(
                        new PatternFindOptions.Builder().setSimilarity(0.9).build(),
                        loginButtonCollection);

        if (loginFindResult.isSuccess()) {
            log.info("✓ Found login button");
            ActionResult loginClickResult =
                    action.perform(new ClickOptions.Builder().build(), loginButtonCollection);
            if (loginClickResult.isSuccess()) {
                log.info("✓ Clicked login button");
            } else {
                log.error("✗ Failed to click login button");
                success = false;
            }
        } else {
            log.error("✗ Login button not found - aborting");
            success = false;
        }

        // Step 2: Username field
        if (success) {
            ObjectCollection usernameCollection =
                    new ObjectCollection.Builder().withImages(usernameField).build();

            ActionResult usernameFindResult =
                    action.perform(new PatternFindOptions.Builder().build(), usernameCollection);

            if (usernameFindResult.isSuccess()) {
                log.info("✓ Username field visible");
                action.perform(new ClickOptions.Builder().build(), usernameCollection);

                // Type username
                ObjectCollection usernameText =
                        new ObjectCollection.Builder().withStrings("testuser").build();
                action.perform(new TypeOptions.Builder().setTypeDelay(0.1).build(), usernameText);
                log.info("✓ Username entered");
            } else {
                log.error("✗ Username field not found");
                success = false;
            }
        }

        // Step 3: Password field
        if (success) {
            ObjectCollection passwordCollection =
                    new ObjectCollection.Builder().withImages(passwordField).build();

            ActionResult passwordFindResult =
                    action.perform(new PatternFindOptions.Builder().build(), passwordCollection);

            if (passwordFindResult.isSuccess()) {
                action.perform(new ClickOptions.Builder().build(), passwordCollection);

                // Type password
                ObjectCollection passwordText =
                        new ObjectCollection.Builder().withStrings("password123").build();
                action.perform(new TypeOptions.Builder().setTypeDelay(0.1).build(), passwordText);
                log.info("✓ Password entered");
            } else {
                log.error("✗ Password field not found");
                success = false;
            }
        }

        // Step 4: Submit
        if (success) {
            ObjectCollection submitCollection =
                    new ObjectCollection.Builder().withImages(submitButton).build();

            ActionResult submitResult =
                    action.perform(new ClickOptions.Builder().build(), submitCollection);

            if (submitResult.isSuccess()) {
                log.info("✓ Submit button clicked");
            } else {
                log.error("✗ Submit button not found");
                success = false;
            }
        }

        if (success) {
            log.info("Login chain completed successfully");
        } else {
            log.error("Login chain failed");
        }
    }

    /** Demonstrates error detection in chains */
    public void errorDetectionChain() {
        log.info("=== Error Detection Chain Example ===");

        // Check for error message after action
        ObjectCollection errorCollection =
                new ObjectCollection.Builder().withImages(errorMessage).build();

        ActionResult errorCheck =
                action.perform(new PatternFindOptions.Builder().build(), errorCollection);

        if (errorCheck.isSuccess()) {
            log.warn("⚠️ Error message detected!");
        } else {
            log.info("✓ No error message found");
        }
    }

    /** Demonstrates performance logging for chains */
    public void performanceLoggingExample() {
        log.info("=== Performance Logging Example ===");

        long startTime = System.currentTimeMillis();
        MDC.put("chain", "performance-test");

        try {
            // Execute a series of actions with timing
            for (int i = 0; i < 3; i++) {
                long actionStart = System.currentTimeMillis();

                ObjectCollection testCollection =
                        new ObjectCollection.Builder().withImages(loginButton).build();

                ActionResult result =
                        action.perform(new PatternFindOptions.Builder().build(), testCollection);

                long actionTime = System.currentTimeMillis() - actionStart;
                log.info("Action {} completed in {}ms", i + 1, actionTime);
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Chain execution completed in {}ms", totalTime);

        } finally {
            MDC.remove("chain");
        }
    }

    /** Demonstrates MDC usage for structured logging */
    public void structuredLoggingExample() {
        log.info("=== Structured Logging Example ===");

        MDC.put("user", "testuser");
        MDC.put("session", "12345");
        MDC.put("action", "login");

        try {
            log.info("Starting login process");

            // Simulate login steps
            MDC.put("step", "find-button");
            log.info("Looking for login button");

            MDC.put("step", "click-button");
            log.info("Clicking login button");

            MDC.put("step", "enter-credentials");
            log.info("Entering credentials");

            MDC.put("step", "submit");
            log.info("Submitting form");

            log.info("Login process completed");

        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    /** Run all examples */
    public void runExample() {
        log.info("Starting Action Chain Logging Examples");

        loginChainWithLogging();
        log.info("");

        errorDetectionChain();
        log.info("");

        performanceLoggingExample();
        log.info("");

        structuredLoggingExample();

        log.info("Action Chain Logging Examples completed");
    }
}
