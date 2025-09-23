package io.github.jspinak.brobot.action.internal.execution;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Manages the execution lifecycle logic for GUI automation actions.
 *
 * <p>This class implements the control flow logic that determines when actions should continue,
 * repeat, or terminate. It encapsulates the complex decision-making process for action execution,
 * supporting the model-based GUI automation framework's ability to handle dynamic, state-based
 * interactions.
 *
 * <p><strong>Key responsibilities:</strong>
 *
 * <ul>
 *   <li>Tracking and incrementing repetition and sequence counters
 *   <li>Evaluating time constraints and repetition limits
 *   <li>Determining when text conditions are met (appearance/vanishing)
 *   <li>Managing find strategies (FIRST, EACH, ALL)
 *   <li>Calculating action duration from start to current time
 *   <li>Controlling action output for clean reporting
 * </ul>
 *
 * <p>The lifecycle management supports multiple termination conditions:
 *
 * <ul>
 *   <li><strong>Time-based:</strong> Actions terminate when max wait time is exceeded
 *   <li><strong>Repetition-based:</strong> Actions stop after configured repetitions
 *   <li><strong>Match-based:</strong> Actions complete when find criteria are satisfied
 *   <li><strong>Text-based:</strong> Actions end when text appears or vanishes as configured
 * </ul>
 *
 * <p>This class works closely with {@link ActionLifecycle} to track state and {@link
 * ActionExecution} to control the execution flow.
 *
 * @see ActionLifecycle
 * @see ActionExecution
 * @see ActionConfig
 */
@Component
public class ActionLifecycleManagement {

    private final BrobotProperties brobotProperties;
    private final TimeWrapper timeWrapper;

    @Autowired
    public ActionLifecycleManagement(BrobotProperties brobotProperties, TimeWrapper timeWrapper) {
        this.brobotProperties = brobotProperties;
        this.timeWrapper = timeWrapper;
    }

    /**
     * Increments the completed repetition counter for the current action.
     *
     * <p>Called after each successful execution of the action within a sequence. This method
     * updates the counter in the {@link ActionLifecycle} contained within the ActionResult.
     *
     * @param matches The ActionResult containing the lifecycle to update
     */
    public void incrementCompletedRepetitions(ActionResult matches) {
        if (matches.getActionLifecycle() != null) {
            matches.getActionLifecycle().incrementCompletedRepetitions();
        }
    }

    /**
     * Increments the completed sequence counter for the current action.
     *
     * <p>Called when a complete sequence of repetitions has finished. A sequence may contain one or
     * more repetitions as configured in {@link ActionConfig}.
     *
     * @param matches The ActionResult containing the lifecycle to update
     */
    public void incrementCompletedSequences(ActionResult matches) {
        if (matches.getActionLifecycle() != null) {
            matches.getActionLifecycle().incrementCompletedSequences();
        }
    }

    /**
     * Calculates the elapsed time since the action started.
     *
     * <p>Computes the duration between the action's start time and the current moment. This is used
     * to enforce time limits and track performance.
     *
     * @param matches The ActionResult containing the start time
     * @return Duration representing elapsed time since action start
     */
    public Duration getCurrentDuration(ActionResult matches) {
        if (matches.getActionLifecycle() == null) {
            // Return a large duration to trigger timeout if lifecycle not initialized
            return Duration.ofSeconds(999999);
        }
        LocalDateTime start = matches.getActionLifecycle().getStartTime();
        LocalDateTime end = timeWrapper.now();
        return Duration.between(start, end);
    }

    /**
     * Retrieves the current count of completed repetitions.
     *
     * @param matches The ActionResult containing the lifecycle data
     * @return Number of repetitions completed so far
     */
    public int getCompletedRepetitions(ActionResult matches) {
        if (matches.getActionLifecycle() == null) {
            // Return 0 if lifecycle not initialized
            return 0;
        }
        return matches.getActionLifecycle().getCompletedRepetitions();
    }

    /**
     * Determines if additional sequences are permitted for this action.
     *
     * <p>Compares the completed sequence count against the maximum allowed sequences configured in
     * {@link RepetitionOptions#getMaxTimesToRepeatActionSequence()}.
     *
     * @param matches The ActionResult containing options and lifecycle data
     * @return true if more sequences can be executed, false if limit reached
     */
    public boolean isMoreSequencesAllowed(ActionResult matches) {
        // With ActionConfig, repetition is handled differently
        // Default to allowing one sequence unless specifically configured
        if (matches.getActionLifecycle() == null) return false;
        return matches.getActionLifecycle().getCompletedSequences() < 1;
    }

    /**
     * Evaluates time and repetition constraints to determine if action can continue.
     *
     * <p>Checks two primary conditions:
     *
     * <ul>
     *   <li>Whether the maximum repetition count has been reached (except for FIND actions)
     *   <li>Whether the elapsed time exceeds the configured maximum wait time
     * </ul>
     *
     * <p>The first repetition is always allowed to proceed. FIND actions have special handling and
     * can continue beyond the repetition limit.
     *
     * @param matches The ActionResult containing options and lifecycle data
     * @return true if both time and repetition constraints allow continuation
     */
    private boolean isTimeLeftAndMoreRepsAllowed(ActionResult matches) {
        int completedReps = getCompletedRepetitions(matches);

        // Always check time limit, even on first iteration
        // This prevents infinite loops in mock mode
        ActionConfig config = matches.getActionConfig();
        double maxWait = 10.0; // Default max wait

        if (config instanceof io.github.jspinak.brobot.action.basic.find.BaseFindOptions) {
            io.github.jspinak.brobot.action.basic.find.BaseFindOptions findOptions =
                    (io.github.jspinak.brobot.action.basic.find.BaseFindOptions) config;
            maxWait = findOptions.getSearchDuration();
        }

        Duration duration = getCurrentDuration(matches);
        boolean timeLeft = duration.getSeconds() <= maxWait;

        // Allow first iteration always, but respect time limit
        if (completedReps == 0) return timeLeft;

        // For subsequent iterations, check both time and any repetition limits
        return timeLeft;
    }

    /**
     * Master control method determining if an action should continue executing.
     *
     * <p>Evaluates multiple termination conditions in priority order:
     *
     * <ol>
     *   <li>Time and repetition limits (mandatory check)
     *   <li>FIND FIRST with at least one match found
     *   <li>FIND EACH FIRST with all patterns found
     *   <li>Text appearance or vanishing conditions
     * </ol>
     *
     * <p>Any satisfied termination condition causes the action to stop.
     *
     * @param matches The ActionResult containing execution state and results
     * @param numberOfImages Total number of unique images being searched
     * @return true if action should continue, false if any stop condition is met
     */
    public boolean isOkToContinueAction(ActionResult matches, int numberOfImages) {
        // In mock mode, always stop after first iteration to prevent hanging
        if (brobotProperties.getCore().isMock() && getCompletedRepetitions(matches) > 0) {
            return false;
        }

        boolean timeLeftAndMoreRepsAllowed = isTimeLeftAndMoreRepsAllowed(matches);
        boolean findFirstAndAtLeastOneMatchFound = isFindFirstAndAtLeastOneMatchFound(matches);
        boolean findEachFirstAndEachPatternFound =
                isFindEachFirstAndEachPatternFound(matches, numberOfImages);
        boolean findAllStrategyShouldStop = isFindAllStrategyShouldStop(matches);

        if (!timeLeftAndMoreRepsAllowed) return false;
        if (findFirstAndAtLeastOneMatchFound) return false;
        if (findEachFirstAndEachPatternFound) return false;
        if (findAllStrategyShouldStop) return false;
        if (isTextConditionAchieved(matches)) return false;
        return true;
    }

    /**
     * Checks if FIND FIRST strategy has achieved its goal.
     *
     * <p>For actions configured with {@link PatternFindOptions.Strategy#FIRST}, this method
     * determines if at least one match has been found, which satisfies the search criteria and
     * allows the action to complete.
     *
     * @param matches The ActionResult containing found matches and options
     * @return true if FIND FIRST is configured and at least one match exists
     */
    public boolean isFindFirstAndAtLeastOneMatchFound(ActionResult matches) {
        // In the new API, check if we're using PatternFindOptions with FIRST strategy
        ActionConfig config = matches.getActionConfig();
        if (config instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) config;
            return !matches.isEmpty()
                    && findOptions.getStrategy() == PatternFindOptions.Strategy.FIRST;
        }
        return !matches.isEmpty();
    }

    /**
     * Tests if a match contains the specified text.
     *
     * <p>When the text parameter is empty, checks if the match contains any text at all. Otherwise,
     * performs a substring search within the match's text content.
     *
     * @param match The Match object containing OCR text results
     * @param text The text to search for, or empty string to check for any text
     * @return true if the condition is satisfied
     */
    private boolean matchContainsText(Match match, String text) {
        if (text.isEmpty()) return !match.getText().isEmpty();
        return match.getText().contains(text);
    }

    /**
     * Evaluates whether text-based termination conditions have been met.
     *
     * <p>Supports three text conditions:
     *
     * <ul>
     *   <li>{@link GetTextUntil#TEXT_APPEARS}: Stop when specified text is found
     *   <li>{@link GetTextUntil#TEXT_VANISHES}: Stop when specified text is no longer found
     *   <li>{@link GetTextUntil#NONE}: Text conditions are not used
     * </ul>
     *
     * <p>For TEXT_APPEARS, returns true (stop) when any match contains the text. For TEXT_VANISHES,
     * returns true (stop) when no matches contain the text.
     *
     * @param matches The ActionResult containing matches with text content
     * @return true if the text condition for stopping has been achieved
     */
    private boolean isTextConditionAchieved(ActionResult matches) {
        // Text conditions are not directly supported in ActionConfig
        // This would need to be implemented in specific action configs if needed
        return false;
    }

    /**
     * Checks if FIND ALL strategy should stop. For ALL strategy, we should stop after one iteration
     * in mock mode or when we've found matches (to avoid infinite loops).
     *
     * @param matches The ActionResult containing execution state
     * @return true if ALL strategy should stop
     */
    private boolean isFindAllStrategyShouldStop(ActionResult matches) {
        ActionConfig config = matches.getActionConfig();
        if (config instanceof io.github.jspinak.brobot.action.basic.find.PatternFindOptions) {
            io.github.jspinak.brobot.action.basic.find.PatternFindOptions findOptions =
                    (io.github.jspinak.brobot.action.basic.find.PatternFindOptions) config;
            if (findOptions.getStrategy()
                    == io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Strategy.ALL) {
                // For ALL strategy, stop after first iteration to prevent infinite loops
                // This is especially important in mock mode
                return getCompletedRepetitions(matches) > 0;
            }
        }
        return false;
    }

    /**
     * Checks if FIND EACH FIRST strategy has found all unique patterns.
     *
     * <p>For actions configured with {@link PatternFindOptions.Strategy#EACH} and {@link
     * PatternFindOptions.DoOnEach#FIRST}, this method verifies that at least one instance of each
     * unique pattern has been found.
     *
     * @param matches The ActionResult containing found matches
     * @param numberOfPatterns Expected number of unique patterns to find
     * @return true if all patterns have been found at least once
     */
    public boolean isFindEachFirstAndEachPatternFound(ActionResult matches, int numberOfPatterns) {
        ActionConfig config = matches.getActionConfig();
        if (config instanceof io.github.jspinak.brobot.action.basic.find.PatternFindOptions) {
            io.github.jspinak.brobot.action.basic.find.PatternFindOptions findOptions =
                    (io.github.jspinak.brobot.action.basic.find.PatternFindOptions) config;
            if (findOptions.getStrategy()
                    == io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Strategy
                            .EACH) {
                return areAllImagesFound(matches, numberOfPatterns);
            }
        }
        return false;
    }

    /**
     * Determines if matches contain all expected unique images.
     *
     * <p>Counts the number of distinct images found in the match results and compares against the
     * expected count. Each unique image is counted only once, regardless of how many matches it
     * produced.
     *
     * @param matches The ActionResult containing match data
     * @param numberOfPatterns Expected number of unique images
     * @return true if the count of unique images equals the expected number
     */
    private boolean areAllImagesFound(ActionResult matches, int numberOfPatterns) {
        int patternsFound = 0;
        List<Image> foundImages = new ArrayList<>();
        for (Match m : matches.getMatchList()) {
            if (!foundImages.contains(m.getSearchImage())) {
                foundImages.add(m.getSearchImage());
                patternsFound++;
            }
        }
        return patternsFound == numberOfPatterns;
    }

    /**
     * Prints action information to the console exactly once per action execution.
     *
     * <p>Uses the printed flag in {@link ActionLifecycle} to ensure action details are output only
     * once, preventing duplicate logging during repeated executions. Output is controlled by the
     * reporting level configuration.
     *
     * <p>For FIND actions, includes the find strategy (FIRST, EACH, ALL) in the output.
     *
     * @param matches The ActionResult containing action options and lifecycle data
     */
    public void printActionOnce(ActionResult matches) {
        ActionLifecycle actionLifecycle = matches.getActionLifecycle();
        if (actionLifecycle.isPrinted()) {
            return;
        }
        actionLifecycle.setPrinted(true);
        // which respects verbosity settings and structured logging
    }
}
