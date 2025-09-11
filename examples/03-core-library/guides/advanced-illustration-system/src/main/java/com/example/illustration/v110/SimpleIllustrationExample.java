package com.example.illustration.v110;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple illustration example for Brobot v1.1.0.
 *
 * <p>Illustrations are controlled via application properties: -
 * brobot.illustration.draw-find=true/false - brobot.illustration.draw-click=true/false -
 * brobot.screenshot.save-history=true/false
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimpleIllustrationExample {

    private final Action action;

    // StateMemory removed - not used in this example

    @Value("${brobot.illustration.draw-find:false}")
    private boolean drawFind;

    @Value("${brobot.illustration.draw-click:false}")
    private boolean drawClick;

    @Value("${brobot.screenshot.save-history:false}")
    private boolean saveHistory;

    /** Demonstrates basic workflow with illustrations */
    public void demonstrateIllustrations() {
        log.info("=== Simple Illustration Example ===");
        log.info("Illustration settings:");
        log.info("  Draw Find: {}", drawFind);
        log.info("  Draw Click: {}", drawClick);
        log.info("  Save History: {}", saveHistory);

        // Create example state images
        StateImage loginButton =
                new StateImage.Builder()
                        .setName("LoginButton")
                        .addPatterns("login/login_button")
                        .build();

        StateImage usernameField =
                new StateImage.Builder()
                        .setName("UsernameField")
                        .addPatterns("login/username_field")
                        .build();

        StateImage submitButton =
                new StateImage.Builder()
                        .setName("SubmitButton")
                        .addPatterns("login/submit_button")
                        .build();

        // Find operations (illustrated if draw-find=true)
        log.info("\nPerforming FIND operations...");
        ObjectCollection loginCollection =
                new ObjectCollection.Builder().withImages(loginButton).build();

        ActionResult findResult =
                action.perform(
                        new io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Builder()
                                .build(),
                        loginCollection);
        if (findResult.isSuccess()) {
            log.info("Found login button - illustration saved: {}", drawFind);
        }

        // Click operations (illustrated if draw-click=true)
        log.info("\nPerforming CLICK operations...");
        ActionResult clickResult =
                action.perform(
                        new io.github.jspinak.brobot.action.basic.click.ClickOptions.Builder()
                                .build(),
                        loginCollection);
        if (clickResult.isSuccess()) {
            log.info("Clicked login button - illustration saved: {}", drawClick);
        }

        // Multiple actions in sequence
        log.info("\nPerforming login sequence...");
        performLoginSequence(usernameField, submitButton);

        if (saveHistory) {
            log.info("\nHistory saved to: history/ directory");
            log.info("Check for hist_*.png files");
        }
    }

    private void performLoginSequence(StateImage usernameField, StateImage submitButton) {
        // Click username field
        ObjectCollection usernameCollection =
                new ObjectCollection.Builder().withImages(usernameField).build();
        action.perform(
                new io.github.jspinak.brobot.action.basic.click.ClickOptions.Builder().build(),
                usernameCollection);

        // Type username
        ObjectCollection usernameText =
                new ObjectCollection.Builder().withStrings("testuser").build();
        action.perform(
                new io.github.jspinak.brobot.action.basic.type.TypeOptions.Builder().build(),
                usernameText);

        // Click submit
        ObjectCollection submitCollection =
                new ObjectCollection.Builder().withImages(submitButton).build();
        action.perform(
                new io.github.jspinak.brobot.action.basic.click.ClickOptions.Builder().build(),
                submitCollection);

        log.info("Login sequence completed");
    }

    /** Demonstrates different illustration scenarios */
    public void demonstrateScenarios() {
        log.info("\n=== Illustration Scenarios ===");

        // Scenario 1: Development debugging
        log.info("\nScenario 1: Development Debugging");
        log.info("Recommended settings:");
        log.info("  brobot.illustration.draw-find=true");
        log.info("  brobot.illustration.draw-click=true");
        log.info("  brobot.screenshot.save-history=true");
        log.info("Purpose: See all actions for debugging");

        // Scenario 2: Performance testing
        log.info("\nScenario 2: Performance Testing");
        log.info("Recommended settings:");
        log.info("  brobot.illustration.draw-find=false");
        log.info("  brobot.illustration.draw-click=false");
        log.info("  brobot.screenshot.save-history=false");
        log.info("Purpose: Maximum performance, no overhead");

        // Scenario 3: Critical path monitoring
        log.info("\nScenario 3: Critical Path Monitoring");
        log.info("Recommended settings:");
        log.info("  brobot.illustration.draw-find=false");
        log.info("  brobot.illustration.draw-click=true");
        log.info("  brobot.screenshot.save-history=true");
        log.info("Purpose: Track user interactions only");
    }

    /** Shows how to check illustration status at runtime */
    public void checkIllustrationStatus() {
        log.info("\n=== Runtime Illustration Status ===");

        if (drawFind || drawClick) {
            log.info("Illustrations are ENABLED");
            log.info("Performance impact: MODERATE");

            if (saveHistory) {
                log.info("History recording: ACTIVE");
                log.info("Storage impact: HIGH (check disk space)");
            } else {
                log.info("History recording: DISABLED");
                log.info("Storage impact: NONE");
            }
        } else {
            log.info("Illustrations are DISABLED");
            log.info("Performance impact: MINIMAL");
            log.info("No visual feedback will be generated");
        }
    }
}
