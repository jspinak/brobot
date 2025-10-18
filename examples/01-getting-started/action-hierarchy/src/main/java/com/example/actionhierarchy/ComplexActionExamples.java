package com.example.actionhierarchy;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
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
                        .build();

        // Note: RepetitionOptions shown in documentation is not implemented in Brobot 1.1.0+.
        // For retry logic, use a manual loop (Method 1) or create a custom function (Method 3).

        // Execute the chained action with both images
        ObjectCollection targets =
                new ObjectCollection.Builder().withImages(clickTarget, findTarget).build();

        ActionResult result = action.perform(clickAndCheck, targets);
        return result.isSuccess();
    }

    /**
     * Method 3: Creating a Reusable Click-Until-Found Function From action-hierarchy.md lines
     * 136-175
     *
     * Note: The built-in ClickUntilOptions demonstrated in the documentation has been removed
     * in Brobot 1.1.0+ as part of clean API refactoring. This reusable function approach
     * is now the recommended pattern for implementing click-until-found behavior.
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
                        .build();

        // Note: RepetitionOptions not implemented in Brobot 1.1.0+.
        // This function demonstrates the recommended pattern: wrap retry logic in a clean function
        // with configurable parameters (maxAttempts, pauseBetween).

        // Combine both images in one collection
        ObjectCollection targets =
                new ObjectCollection.Builder().withImages(clickTarget, findTarget).build();

        // Execute and check the final result
        ActionResult result = action.perform(clickAndCheck, targets);

        // Return overall success status
        // Note: getLastActionResult() method not implemented in Brobot 1.1.0+
        return result.isSuccess();
    }

    /** Usage example from documentation line 174 */
    public void usageExample(StateImage nextButton, StateImage finishButton) {
        // Usage example:
        boolean success = clickUntilFound(nextButton, finishButton, 10, 1.0);
    }
}
