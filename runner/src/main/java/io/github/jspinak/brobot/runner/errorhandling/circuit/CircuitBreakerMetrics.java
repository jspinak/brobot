package io.github.jspinak.brobot.runner.errorhandling.circuit;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Metrics snapshot for a circuit breaker.
 */
@Data
@Builder
public class CircuitBreakerMetrics {
    private final String name;
    private final CircuitBreaker.State state;
    private final int totalCalls;
    private final int blockedCalls;
    private final int failedCalls;
    private final int consecutiveFailures;
    private final Instant lastStateTransition;
    
    /**
     * Gets the success rate.
     * 
     * @return success rate as percentage (0-100)
     */
    public double getSuccessRate() {
        if (totalCalls == 0) {
            return 100.0;
        }
        int successfulCalls = totalCalls - failedCalls - blockedCalls;
        return (double) successfulCalls / totalCalls * 100;
    }
    
    /**
     * Gets the block rate.
     * 
     * @return block rate as percentage (0-100)
     */
    public double getBlockRate() {
        if (totalCalls == 0) {
            return 0.0;
        }
        return (double) blockedCalls / totalCalls * 100;
    }
}