package com.example.logging.examples;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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

/** Demonstrates contextual logging with MDC (Mapped Diagnostic Context). */
@Slf4j
@Component
public class ContextualLoggingExample {

    private final Action action;
    private final AtomicInteger sessionCounter = new AtomicInteger(0);

    // Example objects
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;

    public ContextualLoggingExample(Action action) {
        this.action = action;
        initializeObjects();
    }

    private void initializeObjects() {
        loginButton =
                new StateImage.Builder()
                        .setName("LoginButton")
                        .addPatterns("login_button.png")
                        .build();

        usernameField =
                new StateImage.Builder()
                        .setName("UsernameField")
                        .addPatterns("username_field.png")
                        .build();

        passwordField =
                new StateImage.Builder()
                        .setName("PasswordField")
                        .addPatterns("password_field.png")
                        .build();
    }

    /** Demonstrates basic MDC usage for workflow tracking */
    public void workflowContextLogging() {
        log.info("=== Workflow Context Logging Example ===");

        String workflowId = UUID.randomUUID().toString();
        String sessionId = "session-" + sessionCounter.incrementAndGet();

        try {
            // Set context for entire workflow
            MDC.put("workflowId", workflowId);
            MDC.put("sessionId", sessionId);
            MDC.put("workflow", "user-login");

            log.info("Starting user login workflow");

            // Step 1: Find login button
            MDC.put("step", "find-login");
            log.info("Looking for login button");

            ObjectCollection loginCollection =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            ActionResult findResult =
                    action.perform(new PatternFindOptions.Builder().build(), loginCollection);

            if (!findResult.isSuccess()) {
                log.error("Login button not found - aborting workflow");
                return;
            }

            log.info("Login button found, proceeding to credentials");

            // Step 2: Enter credentials
            MDC.put("step", "enter-credentials");
            enterCredentials("testuser", "testpass");

            // Step 3: Submit
            MDC.put("step", "submit-login");
            submitLogin();

            log.info("Login workflow completed successfully");

        } finally {
            // Always clear MDC to prevent context leakage
            MDC.clear();
        }
    }

    private void enterCredentials(String username, String password) {
        log.info("Entering user credentials");

        // Username
        MDC.put("field", "username");
        log.debug("Clicking username field");

        ObjectCollection usernameCollection =
                new ObjectCollection.Builder().withImages(usernameField).build();
        action.perform(new ClickOptions.Builder().build(), usernameCollection);

        log.debug("Typing username");
        action.perform(
                new TypeOptions.Builder().build(),
                new ObjectCollection.Builder().withStrings(username).build());

        // Password
        MDC.put("field", "password");
        log.debug("Clicking password field");

        ObjectCollection passwordCollection =
                new ObjectCollection.Builder().withImages(passwordField).build();
        action.perform(new ClickOptions.Builder().build(), passwordCollection);

        log.debug("Typing password (masked)");
        action.perform(
                new TypeOptions.Builder().build(),
                new ObjectCollection.Builder().withStrings(password).build());

        MDC.remove("field");
    }

    private void submitLogin() {
        log.info("Submitting login form");

        ObjectCollection loginCollection =
                new ObjectCollection.Builder().withImages(loginButton).build();

        ActionResult result =
                action.perform(
                        new ClickOptions.Builder().setPauseAfterEnd(2.0).build(), loginCollection);

        if (result.isSuccess()) {
            log.info("Login form submitted");
        } else {
            log.error("Failed to submit login form");
        }
    }

    /** Demonstrates multi-user context tracking */
    public void multiUserContextLogging() {
        log.info("=== Multi-User Context Logging Example ===");

        String[] users = {"alice", "bob", "charlie"};

        for (String user : users) {
            processUserTask(user);
        }
    }

    private void processUserTask(String username) {
        String taskId = UUID.randomUUID().toString();

        try {
            MDC.put("userId", username);
            MDC.put("taskId", taskId);
            MDC.put("taskType", "data-processing");

            log.info("Starting task for user");

            // Simulate finding user-specific element
            StateImage userElement =
                    new StateImage.Builder()
                            .setName(username + "Element")
                            .addPatterns(username + "_element.png")
                            .build();

            ObjectCollection userCollection =
                    new ObjectCollection.Builder().withImages(userElement).build();

            MDC.put("action", "find-element");
            ActionResult findResult =
                    action.perform(
                            new PatternFindOptions.Builder()
                                    .setSimilarity(0.8)
                                    .setSearchDuration(3.0)
                                    .build(),
                            userCollection);

            if (findResult.isSuccess()) {
                log.info("User element found, processing...");

                MDC.put("action", "process-data");
                // Simulate processing
                Thread.sleep(100);

                log.info("Task completed for user");
            } else {
                log.warn("User element not found, skipping task");
            }

        } catch (InterruptedException e) {
            log.error("Task interrupted for user", e);
            Thread.currentThread().interrupt();
        } finally {
            MDC.clear();
        }
    }

    /** Demonstrates performance tracking with context */
    public void performanceContextLogging() {
        log.info("=== Performance Context Logging Example ===");

        String perfTestId = "perf-" + System.currentTimeMillis();

        try {
            MDC.put("testId", perfTestId);
            MDC.put("testType", "performance");

            log.info("Starting performance test");

            // Test different similarity thresholds
            double[] similarities = {0.7, 0.8, 0.9, 0.95};

            ObjectCollection loginCollection =
                    new ObjectCollection.Builder().withImages(loginButton).build();

            for (double similarity : similarities) {
                MDC.put("similarity", String.valueOf(similarity));

                long startTime = System.nanoTime();

                ActionResult result =
                        action.perform(
                                new PatternFindOptions.Builder().setSimilarity(similarity).build(),
                                loginCollection);

                long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

                MDC.put("duration_ms", String.valueOf(duration));
                MDC.put("success", String.valueOf(result.isSuccess()));

                if (result.isSuccess()) {
                    Optional<Match> bestMatch = result.getBestMatch();
                    if (bestMatch.isPresent()) {
                        MDC.put("match_score", String.valueOf(bestMatch.get().getScore()));
                    }
                }

                log.info("Performance test iteration completed");

                // Clear iteration-specific context
                MDC.remove("similarity");
                MDC.remove("duration_ms");
                MDC.remove("success");
                MDC.remove("match_score");
            }

            log.info("Performance test completed");

        } finally {
            MDC.clear();
        }
    }

    /** Demonstrates error context enrichment */
    public void errorContextLogging() {
        log.info("=== Error Context Logging Example ===");

        String errorTestId = "error-test-" + UUID.randomUUID().toString();

        try {
            MDC.put("testId", errorTestId);
            MDC.put("testType", "error-handling");

            // Simulate an error scenario
            StateImage nonExistentElement =
                    new StateImage.Builder()
                            .setName("NonExistentElement")
                            .addPatterns("non_existent_element.png")
                            .build();

            ObjectCollection errorCollection =
                    new ObjectCollection.Builder().withImages(nonExistentElement).build();

            MDC.put("operation", "critical-action");
            MDC.put("retryAttempt", "0");

            for (int attempt = 1; attempt <= 3; attempt++) {
                MDC.put("retryAttempt", String.valueOf(attempt));

                log.info("Attempting critical action");

                ActionResult result =
                        action.perform(
                                new PatternFindOptions.Builder()
                                        .setSimilarity(0.95)
                                        .setSearchDuration(2.0)
                                        .build(),
                                errorCollection);

                if (result.isSuccess()) {
                    log.info("Critical action succeeded");
                    break;
                } else {
                    if (attempt < 3) {
                        log.warn("Critical action failed, will retry");
                    } else {
                        log.error("Critical action failed after all retries");

                        // Add additional error context
                        MDC.put("errorType", "element-not-found");
                        MDC.put("finalAttempt", "true");
                        log.error("Error details captured for analysis");
                    }
                }
            }

        } finally {
            MDC.clear();
        }
    }

    /** Run all contextual logging examples */
    public void runAllExamples() {
        workflowContextLogging();
        log.info("");

        multiUserContextLogging();
        log.info("");

        performanceContextLogging();
        log.info("");

        errorContextLogging();
    }
}
