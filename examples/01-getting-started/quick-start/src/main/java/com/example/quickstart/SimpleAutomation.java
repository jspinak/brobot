package com.example.quickstart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/** Demonstrates different approaches to GUI automation with Brobot */
@Component
@Slf4j // Add logging support
public class SimpleAutomation {

    @Autowired private Action action;

    /**
     * Full version showing all the steps explicitly Code from:
     * docs/docs/01-getting-started/quick-start.md lines 50-81
     */
    public void clickButton() {
        // 1. Define what to look for
        StateImage buttonImage =
                new StateImage.Builder()
                        .setName("submit-button")
                        .addPatterns("submit-button")
                        .build();

        // 2. Configure how to find it
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.FIRST)
                        .setSimilarity(0.9)
                        .build();

        // 3. Add the button to the objects to find
        ObjectCollection objects = new ObjectCollection.Builder().withImages(buttonImage).build();

        // 4. Find the button
        ActionResult findResult = action.perform(findOptions, objects);

        // 4. Click the found button (Note: Step numbering from documentation)
        if (findResult.isSuccess()) {
            ClickOptions clickOptions =
                    new ClickOptions.Builder().setClickType(ClickOptions.Type.LEFT).build();

            // Click on the same objects we found
            ActionResult clickResult = action.perform(clickOptions, objects);
            log.info("Click result: {}", clickResult.isSuccess());
        }
    }

    /**
     * Simplified version using convenience methods Code from:
     * docs/docs/01-getting-started/quick-start.md lines 96-108
     *
     * <p>What happens behind the scenes: - Uses default similarity of 0.7 (70% match) -
     * Automatically finds the image first, then clicks if found - Uses
     * PatternFindOptions.Strategy.FIRST (clicks first match) - Uses standard left click with no
     * delays - No need to create ObjectCollections manually
     */
    public void clickButtonSimplified() {
        // 1. Define the button image
        StateImage buttonImage =
                new StateImage.Builder()
                        .setName("submit-button")
                        .addPatterns("submit-button")
                        .build();

        // 2. Find and click in one line
        action.click(buttonImage);

        // That's it! 🎉
    }

    /**
     * Demonstrates various convenience methods Code from:
     * docs/docs/01-getting-started/quick-start.md lines 123-136
     *
     * <p>Default values used: - Similarity: 0.7 (defined in Sikuli's Settings.MinSimilarity) -
     * Search Strategy: FIRST (find first match) - Search Duration: 3 seconds timeout - Click Type:
     * Single left click - Search Region: Entire screen
     */
    public void demonstrateConvenienceMethods() {
        StateImage submitButton = new StateImage.Builder().addPatterns("submit-button").build();

        // Find an image on screen
        ActionResult found = action.find(submitButton);
        log.info("Found submit button: {}", found.isSuccess());

        // Click an image (finds it first automatically)
        action.click(submitButton);

        // Type text (with automatic focus)
        action.type(new ObjectCollection.Builder().withStrings("Hello World").build());

        // Chain find and click with fluent API
        PatternFindOptions findAndClick =
                new PatternFindOptions.Builder().then(new ClickOptions.Builder().build()).build();

        action.perform(
                findAndClick, new ObjectCollection.Builder().withImages(submitButton).build());
    }

    /** Production-ready example with proper error handling and logging */
    public boolean submitForm(String username, String password) {
        log.info("Starting form submission for user: {}", username);

        // Define all UI elements
        StateImage usernameField =
                new StateImage.Builder()
                        .setName("username-field")
                        .addPatterns("form/username-field")
                        .build();

        StateImage passwordField =
                new StateImage.Builder()
                        .setName("password-field")
                        .addPatterns("form/password-field")
                        .build();

        StateImage submitButton =
                new StateImage.Builder()
                        .setName("submit-button")
                        .addPatterns("form/submit-button")
                        .build();

        try {
            // Click username field and type
            ActionResult userResult = action.click(usernameField);
            if (!userResult.isSuccess()) {
                log.error("Failed to find username field");
                return false;
            }

            action.type(new ObjectCollection.Builder().withStrings(username).build());

            // Click password field and type
            ActionResult passResult = action.click(passwordField);
            if (!passResult.isSuccess()) {
                log.error("Failed to find password field");
                return false;
            }

            action.type(new ObjectCollection.Builder().withStrings(password).build());

            // Submit the form
            ActionResult submitResult = action.click(submitButton);
            if (submitResult.isSuccess()) {
                log.info("Form submitted successfully");
                return true;
            } else {
                log.error("Failed to click submit button");
                return false;
            }

        } catch (Exception e) {
            log.error("Error during form submission", e);
            return false;
        }
    }

    /**
     * Demonstrates type-safe configuration examples Code from:
     * docs/docs/01-getting-started/quick-start.md lines 201-217
     */
    public void demonstrateTypeSafeConfiguration() {
        log.info("=== Type-Safe Configuration Examples ===");

        // Find operations
        PatternFindOptions patternFind =
                new PatternFindOptions.Builder()
                        .setStrategy(PatternFindOptions.Strategy.ALL)
                        .setSimilarity(0.85)
                        .build();

        // Click operations
        ClickOptions click =
                new ClickOptions.Builder().setClickType(ClickOptions.Type.DOUBLE_LEFT).build();

        log.info("Created type-safe configuration objects");
    }

    /**
     * Demonstrates common action patterns Code from: docs/docs/01-getting-started/quick-start.md
     * lines 245-273
     */
    public void demonstrateCommonActions() {
        log.info("=== Common Action Patterns ===");

        StateImage targetImage = new StateImage.Builder().addPatterns("target-image").build();

        // Finding Images with precise search
        PatternFindOptions findOptions =
                new PatternFindOptions.Builder()
                        .setSimilarity(0.9) // forPreciseSearch equivalent
                        .build();

        // Clicking with right click and pause
        ClickOptions clickOptions =
                new ClickOptions.Builder()
                        .setClickType(ClickOptions.Type.RIGHT)
                        .setPauseAfterEnd(0.5)
                        .build();

        // Typing with modifier delay and clear field first
        // Note: TypeOptions would be used in real scenario
        log.info("Demonstrated common action configurations");
    }

    /**
     * Demonstrates proper pause usage in Brobot Code from:
     * docs/docs/01-getting-started/quick-start.md lines 375-396
     */
    public void demonstrateProperPauseUsage() {
        log.info("=== Proper Pause Usage ===");

        StateImage button = new StateImage.Builder().addPatterns("button").build();

        // ❌ Don't do this:
        // action.click(button);
        // Thread.sleep(1000);  // Bad practice in Brobot

        // ✅ Do this instead:
        ClickOptions clickWithPause =
                new ClickOptions.Builder()
                        .setPauseAfterEnd(1.0) // 1 second pause after clicking
                        .build();

        ObjectCollection objects = new ObjectCollection.Builder().withImages(button).build();

        action.perform(clickWithPause, objects);

        // Or for pauses before actions:
        PatternFindOptions findWithPause =
                new PatternFindOptions.Builder()
                        .setPauseBeforeBegin(0.5) // 500ms pause before searching
                        .build();

        action.perform(findWithPause, objects);

        log.info("Demonstrated proper pause configuration");
    }
}
