package io.github.jspinak.brobot.tools.testing.mock.scenario;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Defines time-based conditions that affect mock behavior during testing.
 * <p>
 * This class enables simulation of time-dependent scenarios such as:
 * <ul>
 * <li>Network delays and latency variations</li>
 * <li>Peak hours with slower system response</li>
 * <li>Time-of-day dependent behavior</li>
 * <li>Progressive delays that worsen over time</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * TemporalConditions networkDelay = TemporalConditions.builder()
 *     .baseDelay(Duration.ofMillis(500))
 *     .maximumDelay(Duration.ofSeconds(3))
 *     .delayProgression(Duration.ofMillis(100))
 *     .activeTimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0))
 *     .build();
 * }</pre>
 *
 * @see MockScenarioConfig
 * @see FailurePattern
 */
@Data
@Builder
public class TemporalConditions {
    
    /**
     * Base delay to apply to operations.
     */
    private final Duration baseDelay;
    
    /**
     * Maximum delay that can be applied (caps progression).
     */
    private final Duration maximumDelay;
    
    /**
     * Amount to increase delay with each operation (progressive delays).
     */
    private final Duration delayProgression;
    
    /**
     * Time ranges when these conditions are active.
     */
    @Singular("activeTimeRange")
    private final List<TimeRange> activeTimeRanges;
    
    /**
     * Whether delays should reset after a period of inactivity.
     */
    @Builder.Default
    private final boolean resetOnInactivity = false;
    
    /**
     * Duration of inactivity required to reset progressive delays.
     */
    private final Duration inactivityThreshold;
    
    /**
     * Multiplier for delays during "peak hours" or high-load simulation.
     */
    @Builder.Default
    private final double peakMultiplier = 1.0;
    
    /**
     * Random variation range for delays (percentage of base delay).
     */
    @Builder.Default
    private final double randomVariation = 0.0;
    
    /**
     * Calculates the current delay based on operation count and time.
     *
     * @param operationCount number of operations performed
     * @param currentTime current time for time-range checks
     * @return calculated delay duration
     */
    public Duration getCurrentDelay(int operationCount, LocalTime currentTime) {
        if (!isActiveAt(currentTime)) {
            return Duration.ZERO;
        }
        
        Duration currentDelay = baseDelay != null ? baseDelay : Duration.ZERO;
        
        // Apply progression
        if (delayProgression != null && operationCount > 0) {
            Duration progressionDelay = delayProgression.multipliedBy(operationCount);
            currentDelay = currentDelay.plus(progressionDelay);
        }
        
        // Apply peak multiplier
        if (peakMultiplier != 1.0) {
            long delayMillis = (long) (currentDelay.toMillis() * peakMultiplier);
            currentDelay = Duration.ofMillis(delayMillis);
        }
        
        // Apply random variation
        if (randomVariation > 0) {
            double variation = (Math.random() - 0.5) * 2 * randomVariation;
            long variationMillis = (long) (currentDelay.toMillis() * variation);
            currentDelay = currentDelay.plusMillis(variationMillis);
        }
        
        // Cap at maximum
        if (maximumDelay != null && currentDelay.compareTo(maximumDelay) > 0) {
            currentDelay = maximumDelay;
        }
        
        return currentDelay.isNegative() ? Duration.ZERO : currentDelay;
    }
    
    /**
     * Checks if these conditions are active at the specified time.
     *
     * @param currentTime time to check
     * @return true if conditions should be applied
     */
    public boolean isActiveAt(LocalTime currentTime) {
        if (activeTimeRanges.isEmpty()) {
            return true; // Always active if no ranges specified
        }
        
        return activeTimeRanges.stream()
            .anyMatch(range -> range.contains(currentTime));
    }
    
    /**
     * Represents a time range for conditional activation.
     */
    @Data
    @Builder
    public static class TimeRange {
        private final LocalTime start;
        private final LocalTime end;
        
        /**
         * Checks if the given time falls within this range.
         *
         * @param time time to check
         * @return true if time is within range
         */
        public boolean contains(LocalTime time) {
            if (start.isBefore(end)) {
                // Normal range (e.g., 9:00 to 17:00)
                return !time.isBefore(start) && !time.isAfter(end);
            } else {
                // Overnight range (e.g., 22:00 to 6:00)
                return !time.isBefore(start) || !time.isAfter(end);
            }
        }
    }
}