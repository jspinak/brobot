package io.github.jspinak.brobot.tools.testing.mock.time;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.MockStatus;

/**
 * Provides simulated time control for mock testing environments in Brobot.
 *
 * <p>MockTime enables deterministic testing of time-dependent automation scenarios by replacing
 * real-time waits with simulated time advancement. This is crucial for testing timeout behaviors,
 * action durations, and time-based state transitions without the delays of real-time execution.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Virtual Clock</b>: Maintains simulated current time independent of system clock
 *   <li><b>Instant Execution</b>: Wait operations advance mock time without real delays
 *   <li><b>Action-Aware Timing</b>: Respects configured durations for different action types
 *   <li><b>Deterministic Testing</b>: Ensures reproducible test results
 *   <li><b>Performance Benefits</b>: Tests run at maximum speed
 * </ul>
 *
 * <p>Supported operations:
 *
 * <ul>
 *   <li>Wait for specific duration in seconds
 *   <li>Wait for action-specific durations (click, type, etc.)
 *   <li>Wait for find operation durations
 *   <li>Query current mock time
 *   <li>Automatic time advancement for all wait operations
 * </ul>
 *
 * <p>Testing scenarios enabled:
 *
 * <ul>
 *   <li>Timeout behavior verification
 *   <li>Action sequence timing validation
 *   <li>State transition delay testing
 *   <li>Performance degradation simulation
 *   <li>Time-based workflow testing
 * </ul>
 *
 * <p>Example usage in tests:
 *
 * <pre>
 * // Test timeout behavior
 * mockTime.wait(5.0); // Advances mock time by 5 seconds instantly
 * assertTrue(action.hasTimedOut());
 *
 * // Test action duration (using ActionType - modern approach)
 * LocalDateTime before = mockTime.now();
 * mockTime.wait(ActionType.CLICK);
 * LocalDateTime after = mockTime.now();
 * // Verify correct duration was applied
 *
 * // Using ActionType enum
 * mockTime.wait(ActionType.CLICK);
 * </pre>
 *
 * <p>Integration with action system:
 *
 * <ul>
 *   <li>Uses ActionDurations for realistic timing simulation
 *   <li>Different actions have different simulated durations
 *   <li>Find operations include search time simulation
 *   <li>Maintains consistency with real execution timing
 * </ul>
 *
 * <p>Benefits over real-time testing:
 *
 * <ul>
 *   <li>Tests complete in milliseconds instead of seconds/minutes
 *   <li>No flaky tests due to timing variations
 *   <li>Can simulate hours of execution instantly
 *   <li>Precise control over time-based conditions
 * </ul>
 *
 * <p>In the model-based approach, MockTime is essential for comprehensive testing of temporal
 * aspects without the overhead of real-time execution. It enables thorough validation of
 * time-dependent behaviors while maintaining fast test execution and deterministic results.
 *
 * @since 1.0
 * @see ActionDurations
 * @see MockStatus
 * @see ActionType
 */
@Component
public class MockTime {
    private final ActionDurations actionDurations;
    private LocalDateTime now = LocalDateTime.now(); // keeps track of mock time

    public MockTime(ActionDurations actionDurations) {
        this.actionDurations = actionDurations;
    }

    /**
     * LocalDateTime is immutable, so the 'now' variable can be directly referenced for a deep copy.
     *
     * @return the current time, either as the real current time or the mocked current time.
     */
    public LocalDateTime now() {
        return now;
    }

    public void wait(double seconds) {
        if (seconds <= 0) return;
        if (ConsoleReporter.minReportingLevel(ConsoleReporter.OutputLevel.HIGH))
            System.out.format("%s-%.1f ", "wait", seconds);
        long nanoTimeout = (long) (seconds * Math.pow(10, 9));
        now = now.plusNanos(nanoTimeout);
    }

    // Removed deprecated wait methods for ActionType and ActionOptions.Find
    // Use wait(ActionType) and wait(PatternFindOptions.Strategy) instead

    /**
     * Waits for the mock duration of an action using ActionType. This is the modern approach using
     * ActionType instead of ActionType.
     *
     * @param actionType the type of action to wait for
     */
    public void wait(ActionType actionType) {
        wait(actionDurations.getActionDuration(actionType));
    }

    /**
     * Waits for the mock duration of a find strategy. This is the modern approach using
     * PatternFindOptions.Strategy.
     *
     * @param strategy the find strategy to wait for
     */
    public void wait(PatternFindOptions.Strategy strategy) {
        wait(actionDurations.getFindStrategyDuration(strategy));
    }
}
