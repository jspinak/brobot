package com.example.actionhierarchy;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Demonstrates various approaches to implementing complex actions in Brobot, specifically showing
 * different ways to implement "click until found" behavior. All code examples are taken verbatim
 * from the action-hierarchy.md documentation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ComplexActionExamples {

    private final Action action;

    /** Method 1: Traditional Loop Approach From action-hierarchy.md lines 50-71 */
    // Using individual actions with retry logic
    public boolean clickUntilFound(StateImage clickTarget, StateImage findTarget, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            // Click on target with pause after action
            ClickOptions click =
                    new ClickOptions.Builder()
                            .setPauseAfterEnd(1.0) // 1 second pause after click
                            .build();
            action.perform(click, new ObjectCollection.Builder().withImages(clickTarget).build());

            // Check if pattern appeared
            PatternFindOptions find = PatternFindOptions.forQuickSearch();
            ActionResult result =
                    action.perform(
                            find, new ObjectCollection.Builder().withImages(findTarget).build());

            if (result.isSuccess()) {
                return true;
            }
        }
        return false;
    }

    /** Method 2: Fluent API with Action Chaining From action-hierarchy.md lines 74-101 */
    // Using the fluent API to chain click and find operations
    public boolean clickUntilFoundFluent(StateImage clickTarget, StateImage findTarget) {
        // Create a chain that clicks and then looks for the target
        ClickOptions clickAndCheck =
                new ClickOptions.Builder()
                        .withBeforeActionLog("Clicking on " + clickTarget.getName() + "...")
                        .withSuccessLog("Click executed")
                        .setPauseAfterEnd(1.0) // Wait after click
                        .then(
                                new PatternFindOptions.Builder()
                                        .withBeforeActionLog(
                                                "Checking if "
                                                        + findTarget.getName()
                                                        + " appeared...")
                                        .withSuccessLog(findTarget.getName() + " found!")
                                        .withFailureLog(findTarget.getName() + " not yet visible")
                                        .build())
                        // Note: setRepetition method doesn't exist in current version
                        // This would handle repetition in the documentation version
                        // .setRepetition(new RepetitionOptions.Builder()
                        //         .setMaxTimesToRepeatActionSequence(10)  // Try up to 10 times
                        //         .setPauseBetweenActionSequences(0.5)    // Brief pause between
                        // attempts
                        //         .build())
                        .build();

        // Execute the chained action with both images
        ObjectCollection targets =
                new ObjectCollection.Builder().withImages(clickTarget, findTarget).build();

        ActionResult result = action.perform(clickAndCheck, targets);
        return result.isSuccess();
    }

    /**
     * Method 3: Using the Built-in ClickUntilOptions (Deprecated but Available) From
     * action-hierarchy.md lines 104-133
     */
    // Using Brobot's built-in ClickUntil composite action
    public boolean clickUntilFoundBuiltIn(StateImage clickTarget, StateImage findTarget) {
        // Create ClickUntilOptions configured to click until objects appear
        ClickUntilOptions clickUntil =
                new ClickUntilOptions.Builder()
                        .setCondition(ClickUntilOptions.Condition.OBJECTS_APPEAR)
                        .withBeforeActionLog(
                                "Clicking until " + findTarget.getName() + " appears...")
                        .withSuccessLog(findTarget.getName() + " appeared!")
                        .withFailureLog("Timeout - " + findTarget.getName() + " did not appear")
                        // Note: setRepetition method doesn't exist in current version
                        // This would handle repetition in the documentation version
                        // .setRepetition(new RepetitionOptions.Builder()
                        //         .setMaxTimesToRepeatActionSequence(10)
                        //         .setPauseBetweenActionSequences(1.0)
                        //         .build())
                        .build();

        // Create ObjectCollections
        // If using 1 collection: clicks objects until they appear
        // If using 2 collections: clicks collection 1 until collection 2 appears
        ObjectCollection clickCollection =
                new ObjectCollection.Builder().withImages(clickTarget).build();
        ObjectCollection appearCollection =
                new ObjectCollection.Builder().withImages(findTarget).build();

        // Execute with two collections - click first until second appears
        ActionResult result = action.perform(clickUntil, clickCollection, appearCollection);
        return result.isSuccess();
    }

    /**
     * Method 4: Creating a Reusable Click-Until-Found Function From action-hierarchy.md lines
     * 136-175
     */
    // Creating a clean, reusable function that combines the best approaches
    public boolean clickUntilFound(
            StateImage clickTarget, StateImage findTarget, int maxAttempts, double pauseBetween) {
        // Use fluent chaining with automatic logging
        PatternFindOptions clickAndCheck =
                new PatternFindOptions.Builder()
                        .withBeforeActionLog("Looking for click target...")
                        .withSuccessLog("Click target found")
                        .then(
                                new ClickOptions.Builder()
                                        .withBeforeActionLog("Clicking...")
                                        .withSuccessLog("Clicked successfully")
                                        .setPauseAfterEnd(pauseBetween)
                                        .build())
                        .then(
                                new PatternFindOptions.Builder()
                                        .withBeforeActionLog("Checking if target appeared...")
                                        .withSuccessLog("Target appeared!")
                                        .withFailureLog("Target not yet visible")
                                        .setSearchDuration(0.5) // Quick check
                                        .build())
                        // Note: setRepetition method doesn't exist in current version
                        // This would handle repetition in the documentation version
                        // .setRepetition(new RepetitionOptions.Builder()
                        //         .setMaxTimesToRepeatActionSequence(maxAttempts)
                        //         .setPauseBetweenActionSequences(0.5)
                        //         .build())
                        .build();

        // Combine both images in one collection
        ObjectCollection targets =
                new ObjectCollection.Builder().withImages(clickTarget, findTarget).build();

        // Execute and check the final result
        ActionResult result = action.perform(clickAndCheck, targets);

        // The chain succeeds if the final find action succeeded
        // The chain succeeds if the final find action succeeded
        // Note: getLastActionResult() doesn't exist in current version
        return result.isSuccess(); // && result.getLastActionResult().isSuccess();
    }

    /** Usage example from documentation line 174 */
    public void usageExample(StateImage nextButton, StateImage finishButton) {
        // Usage example:
        boolean success = clickUntilFound(nextButton, finishButton, 10, 1.0);
    }
}
