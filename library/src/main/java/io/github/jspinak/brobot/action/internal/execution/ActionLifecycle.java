package io.github.jspinak.brobot.action.internal.execution;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks the temporal execution state of a GUI automation action.
 * <p>
 * This class maintains timing information and execution counters throughout an action's
 * lifecycle. It serves as a temporal record from action initiation through completion,
 * supporting the framework's ability to enforce time limits, track repetitions, and
 * manage complex action sequences.
 * <p>
 * <strong>Key temporal concepts:</strong>
 * <ul>
 * <li><strong>Start time:</strong> When the action began execution</li>
 * <li><strong>End time:</strong> When the action actually completed (may be before allowed time)</li>
 * <li><strong>Allowed end time:</strong> Maximum time permitted for action completion</li>
 * <li><strong>Repetitions:</strong> Individual action executions within a sequence</li>
 * <li><strong>Sequences:</strong> Complete sets of repetitions</li>
 * </ul>
 * <p>
 * This class supports the model-based GUI automation approach by providing precise
 * temporal tracking needed for robust action execution and failure detection.
 *
 * @see ActionLifecycleManagement
 * @see ActionExecution
 */
@Getter
@Setter
public class ActionLifecycle {

    /**
     * The timestamp when this action began execution.
     * Used to calculate duration and enforce time limits.
     */
    private LocalDateTime startTime;
    /**
     * The actual completion timestamp of the action.
     * <p>
     * This records when the action finished execution, which may be before
     * the {@link #allowedEndTime} if the action completed successfully or
     * failed early. Remains null until the action completes.
     */
    private LocalDateTime endTime;
    /**
     * The maximum allowed timestamp for action completion.
     * <p>
     * Calculated as {@link #startTime} plus the configured maximum wait time.
     * Actions exceeding this time are terminated to prevent infinite loops
     * or system hangs.
     */
    private LocalDateTime allowedEndTime;
    /**
     * Count of individual action executions completed.
     * <p>
     * Tracks how many times the core action has been performed within
     * the current sequence. Used to enforce repetition limits and
     * determine when a sequence is complete.
     */
    private int completedRepetitions = 0;
    /**
     * Count of complete action sequences executed.
     * <p>
     * A sequence consists of one or more repetitions. This counter tracks
     * how many full sequences have been completed, supporting complex
     * multi-sequence operations.
     */
    private int completedSequences = 0;
    /**
     * Flag indicating whether action details have been logged.
     * <p>
     * Prevents duplicate logging of the same action information,
     * ensuring clean output in reports and logs.
     */
    private boolean printed = false;

    /**
     * Constructs a new ActionLifecycle with start time and duration limit.
     * <p>
     * Initializes the lifecycle with the current time as the start point and
     * calculates the maximum allowed end time based on the provided wait duration.
     * The wait time is converted from seconds to nanoseconds for precision.
     *
     * @param now The timestamp marking the start of action execution
     * @param maxWait Maximum duration in seconds the action is allowed to run.
     *                Must be non-negative. A value of 0 means no time limit.
     */
    public ActionLifecycle(LocalDateTime now, double maxWait) {
        this.startTime = now;
        long nanos = (long) (maxWait * Math.pow(10, 9));
        this.allowedEndTime = startTime.plusNanos(nanos);
    }

    /**
     * Increments the repetition counter by one.
     * <p>
     * Called after each successful execution of the action within a sequence.
     * This method is typically invoked by {@link ActionLifecycleManagement}
     * to track progress through configured repetition limits.
     */
    public void incrementCompletedRepetitions() {
        completedRepetitions++;
    }

    /**
     * Increments the sequence counter by one.
     * <p>
     * Called when a complete sequence of repetitions has finished.
     * This method is typically invoked by {@link ActionExecution} after
     * all repetitions in a sequence are complete.
     */
    public void incrementCompletedSequences() { completedSequences++; }

}
