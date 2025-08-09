package com.example.mocking.config;

import lombok.Builder;
import lombok.Data;
import java.time.Duration;

/**
 * Defines a failure pattern for mocking.
 */
@Data
@Builder
public class FailurePattern {
    
    private String patternName;
    
    // Failure rate (0.0 - 1.0)
    @Builder.Default
    private double failureRate = 0.1;
    
    // Delay before failure  
    private Duration delayBeforeFailure;
    
    // Actions this failure cascades to
    @Builder.Default
    private java.util.Map<io.github.jspinak.brobot.action.ActionType, Double> cascadesToActions = new java.util.HashMap<>();
    
    // Recovery delay
    private Duration recoveryDelay;
    
    // Base probability of failure (0.0 - 1.0)
    private double baseProbability;
    
    // Whether failures cascade (increase probability)
    @Builder.Default
    private boolean cascading = false;
    
    // Multiplier for cascading failures
    @Builder.Default
    private double cascadeMultiplier = 1.5;
    
    // Maximum consecutive failures before forced success
    @Builder.Default
    private int maxConsecutiveFailures = 3;
    
    // Recovery delay after failures (duplicate removed)
    
    // Custom failure message
    private String failureMessage;
    
    // Exception type to throw on failure
    @Builder.Default
    private Class<? extends Exception> exceptionType = RuntimeException.class;
    
    // Probability decay per failure occurrence
    @Builder.Default
    private double probabilityDecay = 0.1;
    
    // Current state (mutable during execution)
    @Builder.Default
    private transient int currentConsecutiveFailures = 0;
    @Builder.Default
    private transient double currentProbability = 0;
    @Builder.Default
    private transient long lastFailureTime = 0;
    
    /**
     * Calculate current failure probability based on pattern
     */
    public double getCurrentFailureProbability() {
        if (currentProbability == 0) {
            currentProbability = baseProbability;
        }
        
        // Check if in recovery period
        if (recoveryDelay != null && lastFailureTime > 0) {
            long timeSinceFailure = System.currentTimeMillis() - lastFailureTime;
            if (timeSinceFailure < recoveryDelay.toMillis()) {
                return 1.0; // Always fail during recovery
            }
        }
        
        // Apply cascading if enabled
        if (cascading && currentConsecutiveFailures > 0) {
            return Math.min(1.0, currentProbability * Math.pow(cascadeMultiplier, currentConsecutiveFailures));
        }
        
        return currentProbability;
    }
    
    /**
     * Record a failure occurrence
     */
    public void recordFailure() {
        currentConsecutiveFailures++;
        lastFailureTime = System.currentTimeMillis();
        
        // Apply probability decay
        currentProbability = Math.max(0, currentProbability - probabilityDecay);
    }
    
    /**
     * Record a success occurrence
     */
    public void recordSuccess() {
        currentConsecutiveFailures = 0;
        // Reset probability towards base
        currentProbability = baseProbability;
    }
    
    /**
     * Check if should force success due to max failures
     */
    public boolean shouldForceSuccess() {
        return maxConsecutiveFailures > 0 && currentConsecutiveFailures >= maxConsecutiveFailures;
    }
}