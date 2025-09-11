package com.example.logging.examples;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/** Demonstrates basic logging patterns for Brobot actions. */
@Slf4j
@Component
public class BasicLoggingExample {

    private final Action action;

    // Example objects
    private StateImage submitButton;
    private StateImage cancelButton;
    private StateImage searchField;

    public BasicLoggingExample(Action action) {
        this.action = action;
        initializeObjects();
    }

    private void initializeObjects() {
        submitButton =
                new StateImage.Builder()
                        .setName("SubmitButton")
                        .addPatterns("submit_button.png")
                        .build();

        cancelButton =
                new StateImage.Builder()
                        .setName("CancelButton")
                        .addPatterns("cancel_button.png")
                        .build();

        searchField =
                new StateImage.Builder()
                        .setName("SearchField")
                        .addPatterns("search_field.png")
                        .build();
    }

    /** Demonstrates basic action logging with levels */
    public void basicActionLogging() {
        log.info("=== Basic Action Logging Example ===");

        // Log at INFO level for normal operations
        log.info("Searching for submit button...");

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setSimilarity(0.8).build();

        ObjectCollection submitCollection =
                new ObjectCollection.Builder().withImages(submitButton).build();

        long startTime = System.currentTimeMillis();
        ActionResult result = action.perform(findOptions, submitCollection);
        long duration = System.currentTimeMillis() - startTime;

        if (result.isSuccess()) {
            Optional<Match> bestMatch = result.getBestMatch();
            if (bestMatch.isPresent()) {
                log.info(
                        "Submit button found at {} (took {}ms)",
                        bestMatch.get().getRegion(),
                        duration);
            }
        } else {
            log.warn("Submit button not found after {}ms", duration);
        }
    }

    /** Demonstrates debug-level logging for detailed flow */
    public void detailedDebugLogging() {
        log.info("=== Detailed Debug Logging Example ===");

        log.debug("Entering detailedDebugLogging method");
        log.debug("Configuration: similarity=0.9, searchDuration=5s");

        PatternFindOptions findOptions =
                new PatternFindOptions.Builder().setSimilarity(0.9).setSearchDuration(5.0).build();

        log.debug("Created find options: {}", findOptions);
        log.debug(
                "Target object: name={}, patterns={}",
                searchField.getName(),
                searchField.getPatterns().size());

        ObjectCollection searchCollection =
                new ObjectCollection.Builder().withImages(searchField).build();

        ActionResult result = action.perform(findOptions, searchCollection);

        log.debug(
                "Action result: success={}, matches={}",
                result.isSuccess(),
                result.getMatchList().size());

        if (log.isTraceEnabled()) {
            result.getMatchList()
                    .forEach(
                            match ->
                                    log.trace(
                                            "Match detail: region={}, score={}",
                                            match.getRegion(),
                                            match.getScore()));
        }
    }

    /** Demonstrates error logging with context */
    public void errorHandlingWithLogging() {
        log.info("=== Error Handling with Logging Example ===");

        try {
            log.info("Attempting critical action: submit form");

            ClickOptions clickOptions = new ClickOptions.Builder().setPauseAfterEnd(1.0).build();

            ObjectCollection submitCollection =
                    new ObjectCollection.Builder().withImages(submitButton).build();

            ActionResult result = action.perform(clickOptions, submitCollection);

            if (!result.isSuccess()) {
                log.error("Failed to click submit button - form submission failed");

                // Log recovery attempt
                log.warn("Attempting recovery: trying cancel button");

                ObjectCollection cancelCollection =
                        new ObjectCollection.Builder().withImages(cancelButton).build();

                ActionResult recoveryResult = action.perform(clickOptions, cancelCollection);

                if (recoveryResult.isSuccess()) {
                    log.info("Recovery successful - clicked cancel button");
                } else {
                    log.error("Recovery failed - both submit and cancel buttons unavailable");
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error during form submission", e);
            log.error("Stack trace:", e);
        }
    }

    /** Demonstrates structured logging for better parsing */
    public void structuredLogging() {
        log.info("=== Structured Logging Example ===");

        // Use structured logging for machine-readable logs
        log.info(
                "action_start action={} target={} similarity={} searchDuration={}",
                "find",
                searchField.getName(),
                0.85,
                3);

        ObjectCollection searchCollection =
                new ObjectCollection.Builder().withImages(searchField).build();

        ActionResult result =
                action.perform(
                        new PatternFindOptions.Builder()
                                .setSimilarity(0.85)
                                .setSearchDuration(3.0)
                                .build(),
                        searchCollection);

        // Log result with structured data
        log.info(
                "action_complete action={} target={} success={} matches={}",
                "find",
                searchField.getName(),
                result.isSuccess(),
                result.getMatchList().size());

        if (result.isSuccess()) {
            Optional<Match> bestMatch = result.getBestMatch();
            if (bestMatch.isPresent()) {
                Match match = bestMatch.get();
                log.info(
                        "match_details best_score={} region_x={} region_y={} region_w={}"
                                + " region_h={}",
                        match.getScore(),
                        match.getRegion().x(),
                        match.getRegion().y(),
                        match.getRegion().w(),
                        match.getRegion().h());
            }
        }
    }

    /** Run all basic logging examples */
    public void runAllExamples() {
        basicActionLogging();
        log.info("");

        detailedDebugLogging();
        log.info("");

        errorHandlingWithLogging();
        log.info("");

        structuredLogging();
    }
}
