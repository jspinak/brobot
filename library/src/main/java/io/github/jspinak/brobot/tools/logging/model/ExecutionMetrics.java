package io.github.jspinak.brobot.tools.logging.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Captures performance metrics for automated testing operations in the Brobot framework. This model
 * provides detailed timing information that helps identify performance bottlenecks, optimize test
 * execution, and monitor application responsiveness.
 *
 * <p>All time measurements are in milliseconds, providing sufficient precision for typical UI
 * automation scenarios while maintaining compatibility with standard Java time APIs.
 *
 * <p>Performance metrics are optional in {@link LogData} and are typically populated for operations
 * where performance monitoring is explicitly enabled or for log types that focus on performance
 * analysis (e.g., METRICS type logs).
 *
 * @see LogData for the parent log entry that may contain these metrics
 * @see LogEventType#METRICS for performance-focused log entries
 * @see io.github.jspinak.brobot.tools.logging.dto.ExecutionMetricsDTO for the corresponding DTO
 */
@Getter
@Setter
public class ExecutionMetrics {
    /**
     * Duration of the specific action performed (e.g., click, type, find). Measures the time from
     * action initiation to completion, excluding any wait times or retries.
     */
    private long actionDuration;

    /**
     * Time taken for a page or screen to fully load after navigation. Useful for web applications
     * and applications with dynamic content loading. A value of 0 may indicate the metric is not
     * applicable for the operation.
     */
    private long pageLoadTime;

    /**
     * Time taken to complete a state transition. Measures from the initiation of the transition to
     * the confirmation that the target state is active. Includes any necessary waits and state
     * detection attempts.
     */
    private long transitionTime;

    /**
     * Total duration of the entire test or operation sequence. For individual operations, this
     * might equal actionDuration. For composite operations or test suites, this captures the
     * end-to-end time.
     */
    private long totalTestDuration;

    @Override
    public String toString() {
        return "ExecutionMetrics{"
                + "actionDuration="
                + actionDuration
                + ", pageLoadTime="
                + pageLoadTime
                + ", transitionTime="
                + transitionTime
                + ", totalTestDuration="
                + totalTestDuration
                + '}';
    }
}
