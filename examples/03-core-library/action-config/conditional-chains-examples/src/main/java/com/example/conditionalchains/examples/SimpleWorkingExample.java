package com.example.conditionalchains.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Examples demonstrating ConditionalActionChain usage.
 *
 * <p>This class shows how to use ConditionalActionChain for elegant conditional execution patterns,
 * including convenience methods and advanced chaining techniques.
 */
@Component
public class SimpleWorkingExample {
    private static final Logger log = LoggerFactory.getLogger(SimpleWorkingExample.class);

    @Autowired private Action action;

    private StateImage buttonImage;
    private StateImage loginButton;
    private StateImage usernameField;
    private StateImage passwordField;
    private StateImage submitButton;

    public SimpleWorkingExample() {
        initializeImages();
    }

    private void initializeImages() {
        buttonImage =
                new StateImage.Builder().setName("ButtonImage").addPattern("button.png").build();

        loginButton =
                new StateImage.Builder()
                        .setName("LoginButton")
                        .addPattern("login_button.png")
                        .build();

        usernameField =
                new StateImage.Builder()
                        .setName("UsernameField")
                        .addPattern("username_field.png")
                        .build();

        passwordField =
                new StateImage.Builder()
                        .setName("PasswordField")
                        .addPattern("password_field.png")
                        .build();

        submitButton =
                new StateImage.Builder()
                        .setName("SubmitButton")
                        .addPattern("submit_button.png")
                        .build();
    }

    /** Simple find and click using ConditionalActionChain with convenience methods. */
    public void simpleFindAndClickWithChain() {
        log.info("=== Simple Find and Click with ConditionalActionChain ===");

        ObjectCollection objects = new ObjectCollection.Builder().withImages(buttonImage).build();

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setSimilarity(0.8).build();

        ClickOptions clickOptions = new ClickOptions.Builder().setPauseAfterEnd(0.5).build();

        // Using ConditionalActionChain with convenience methods
        ActionResult result =
                ConditionalActionChain.find(objects, findOptions)
                        .ifFoundClick(clickOptions)
                        .ifFoundLog("Successfully clicked button")
                        .ifNotFoundLog("Button not found")
                        .perform(action);

        log.info("Chain completed with success: {}", result.isSuccess());
    }

    /** Login flow using ConditionalActionChain with chaining and convenience methods. */
    public void loginFlowWithChain() {
        log.info("=== Login Flow with ConditionalActionChain ===");

        // Step 1: Click login button and enter credentials
        ObjectCollection loginObjects =
                new ObjectCollection.Builder().withImages(loginButton).build();

        ObjectCollection usernameObjects =
                new ObjectCollection.Builder().withImages(usernameField).build();

        ObjectCollection passwordObjects =
                new ObjectCollection.Builder().withImages(passwordField).build();

        ObjectCollection submitObjects =
                new ObjectCollection.Builder().withImages(submitButton).build();

        // Complete login flow using chaining
        ActionResult result =
                ConditionalActionChain.find(
                                loginObjects,
                                new PatternFindOptions.Builder().setSimilarity(0.8).build())
                        .ifFoundClick() // Uses default click options
                        .ifFoundLog("Clicked login button")
                        .ifNotFoundLog("Login button not found - aborting")
                        .ifNotFoundStop() // Stop chain execution if login button not found
                        .then()
                        .find(usernameObjects, new PatternFindOptions.Builder().build())
                        .ifFoundClick()
                        .ifFoundType("testuser") // Convenience method for typing
                        .ifFoundLog("Entered username")
                        .then()
                        .find(passwordObjects, new PatternFindOptions.Builder().build())
                        .ifFoundClick()
                        .ifFoundType("password123")
                        .ifFoundLog("Entered password")
                        .then()
                        .find(submitObjects, new PatternFindOptions.Builder().build())
                        .ifFoundClick()
                        .ifFoundLog("Clicked submit button")
                        .ifNotFoundLog("Submit button not found")
                        .perform(action);

        log.info("Login flow completed with success: {}", result.isSuccess());
    }

    /** Advanced pattern with custom logic using lambda expressions. */
    public void advancedPatternWithLambdas() {
        log.info("=== Advanced Pattern with Lambda Functions ===");

        ObjectCollection objects = new ObjectCollection.Builder().withImages(buttonImage).build();

        ActionResult result =
                ConditionalActionChain.find(
                                objects,
                                new PatternFindOptions.Builder().setSimilarity(0.9).build())
                        .ifFoundDo(
                                actionResult -> {
                                    log.info(
                                            "Button found at coordinates: {}",
                                            actionResult.getBestMatch().getMatch().getCenter());
                                    // Could perform additional validation here
                                })
                        .ifFoundClick(new ClickOptions.Builder().setPauseAfterEnd(1.0).build())
                        .ifNotFoundDo(
                                () -> {
                                    log.warn("Button not found - taking screenshot for debugging");
                                    // Could take screenshot or perform other debugging actions
                                })
                        .perform(action);

        log.info("Advanced pattern completed: {}", result.isSuccess());
    }

    /** Retry pattern using ConditionalActionChain. */
    public ActionResult clickWithRetryChain(StateImage target, int maxRetries) {
        log.info("=== Click with Retry using Chain Pattern ===");

        ObjectCollection objects = new ObjectCollection.Builder().withImages(target).build();

        ActionResult finalResult = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} of {}", attempt, maxRetries);

            finalResult =
                    ConditionalActionChain.find(
                                    objects,
                                    new PatternFindOptions.Builder().setSimilarity(0.7).build())
                            .ifFoundClick(new ClickOptions.Builder().setPauseAfterEnd(0.5).build())
                            .ifFoundLog("Successfully clicked on attempt " + attempt)
                            .ifNotFoundLog("Element not found on attempt " + attempt)
                            .perform(action);

            if (finalResult.isSuccess()) {
                log.info("Click succeeded on attempt {}", attempt);
                return finalResult;
            }

            // Wait before retry
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.error("Failed after {} attempts", maxRetries);
        return finalResult != null ? finalResult : new ActionResult();
    }

    /** Demonstrates all ConditionalActionChain examples. */
    public void runAllExamples() {
        log.info("Running ConditionalActionChain examples\n");

        simpleFindAndClickWithChain();
        log.info("");

        loginFlowWithChain();
        log.info("");

        advancedPatternWithLambdas();
        log.info("");

        clickWithRetryChain(buttonImage, 3);
        log.info("");

        log.info("All ConditionalActionChain examples completed!");
    }

    /** Example showing comparison between old manual approach and new chain approach. */
    public void comparisonExample() {
        log.info("=== Comparison: Manual vs ConditionalActionChain ===");

        ObjectCollection objects = new ObjectCollection.Builder().withImages(buttonImage).build();

        // OLD WAY (manual):
        log.info("Manual approach:");
        ActionResult findResult = action.perform(new PatternFindOptions.Builder().build(), objects);
        if (findResult.isSuccess()) {
            log.info("Found button, clicking...");
            action.perform(new ClickOptions.Builder().build(), objects);
            log.info("Clicked successfully");
        } else {
            log.warn("Button not found");
        }

        // NEW WAY (with ConditionalActionChain):
        log.info("ConditionalActionChain approach:");
        ConditionalActionChain.find(objects, new PatternFindOptions.Builder().build())
                .ifFoundClick()
                .ifFoundLog("Clicked successfully")
                .ifNotFoundLog("Button not found")
                .perform(action);
    }
}
