package io.github.jspinak.brobot.tools.testing.mock.scenario;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Defines failure patterns for mock actions to simulate real-world error conditions.
 * <p>
 * This class enables sophisticated failure simulation including:
 * <ul>
 * <li>Base failure probability with decay over time</li>
 * <li>Maximum consecutive failures before forced success</li>
 * <li>Recovery patterns after failure sequences</li>
 * <li>Time-based failure clustering</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * FailurePattern networkIssues = FailurePattern.builder()
 *     .baseProbability(0.3)              // 30% base failure rate
 *     .probabilityDecay(0.05)            // Decrease by 5% each failure
 *     .maxConsecutiveFailures(3)         // Force success after 3 failures
 *     .recoveryDelay(Duration.ofSeconds(1))  // 1 second recovery time
 *     .build();
 * }</pre>
 *
 * @see MockScenarioConfig
 * @see MockScenarioManager
 */
@Data
@Builder
public class FailurePattern {
    
    /**
     * Base probability (0.0-1.0) of failure for this action type.
     */
    @Builder.Default
    private final double baseProbability = 0.0;
    
    /**
     * Amount to decrease failure probability after each failure.
     * Simulates transient issues that resolve over time.
     */
    @Builder.Default
    private final double probabilityDecay = 0.0;
    
    /**
     * Maximum number of consecutive failures before forcing success.
     * Prevents infinite failure loops in testing scenarios.
     */
    @Builder.Default
    private final int maxConsecutiveFailures = Integer.MAX_VALUE;
    
    /**
     * Minimum time between failure occurrences.
     * Useful for simulating intermittent issues with timing patterns.
     */
    private final Duration minimumFailureInterval;
    
    /**
     * Time to wait after a failure sequence before returning to normal probability.
     * Simulates system recovery periods.
     */
    private final Duration recoveryDelay;
    
    /**
     * Whether failures should increase in probability (cascading failures).
     */
    @Builder.Default
    private final boolean cascading = false;
    
    /**
     * Multiplier for probability increase in cascading failures.
     */
    @Builder.Default
    private final double cascadeMultiplier = 1.5;
    
    /**
     * Custom message to include in failure exceptions.
     */
    private final String failureMessage;
    
    /**
     * Type of exception to throw on failure.
     */
    @Builder.Default
    private final Class<? extends Exception> exceptionType = RuntimeException.class;
    
    /**
     * Calculates current failure probability based on failure history.
     *
     * @param consecutiveFailures number of recent consecutive failures
     * @param timeSinceLastFailure time since the last failure occurred
     * @return current failure probability (0.0-1.0)
     */
    public double getCurrentProbability(int consecutiveFailures, Duration timeSinceLastFailure) {
        if (consecutiveFailures >= maxConsecutiveFailures) {
            return 0.0; // Force success
        }
        
        double currentProbability = baseProbability;
        
        // Apply decay based on consecutive failures
        if (probabilityDecay > 0) {
            currentProbability = Math.max(0.0, baseProbability - (consecutiveFailures * probabilityDecay));
        }
        
        // Apply cascading if enabled
        if (cascading && consecutiveFailures > 0) {
            currentProbability *= Math.pow(cascadeMultiplier, consecutiveFailures);
            currentProbability = Math.min(1.0, currentProbability);
        }
        
        // Check recovery delay
        if (recoveryDelay != null && timeSinceLastFailure != null && 
            timeSinceLastFailure.compareTo(recoveryDelay) < 0) {
            currentProbability *= 2.0; // Double probability during recovery period
            currentProbability = Math.min(1.0, currentProbability);
        }
        
        return currentProbability;
    }
    
    /**
     * Checks if enough time has passed since the last failure.
     *
     * @param timeSinceLastFailure duration since last failure
     * @return true if failure is allowed based on timing
     */
    public boolean isFailureAllowedByTiming(Duration timeSinceLastFailure) {
        if (minimumFailureInterval == null || timeSinceLastFailure == null) {
            return true;
        }
        return timeSinceLastFailure.compareTo(minimumFailureInterval) >= 0;
    }
    
    /**
     * Creates an exception for this failure pattern.
     *
     * @return configured exception instance
     */
    public Exception createException() {
        try {
            String message = failureMessage != null ? failureMessage : "Mock failure from pattern";
            return exceptionType.getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            return new RuntimeException("Mock failure from pattern", e);
        }
    }
}