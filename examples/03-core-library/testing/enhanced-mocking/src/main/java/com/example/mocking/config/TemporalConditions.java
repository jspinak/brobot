package com.example.mocking.config;

import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;

/**
 * Time-based conditions for mock scenarios.
 */
@Data
@Builder
public class TemporalConditions {
    
    private String conditionName;
    private String description;
    
    // When to activate/deactivate
    private Duration activateAfter;
    private Duration deactivateAfter;
    
    // Base delay for actions
    private Duration baseDelay;
    
    // Maximum delay cap
    private Duration maximumDelay;
    
    // Delay progression per action
    private Duration delayProgression;
    
    // Random variation factor (0.0 - 1.0)
    @Builder.Default
    private double randomVariation = 0.1;
    
    // Time range when conditions are active
    private LocalTime activeStartTime;
    private LocalTime activeEndTime;
    
    // Current state
    @Builder.Default
    private transient int actionCount = 0;
    @Builder.Default
    private transient Random random = new Random();
    
    /**
     * Calculate current delay based on conditions
     */
    public Duration getCurrentDelay() {
        // Check if within active time range
        if (activeStartTime != null && activeEndTime != null) {
            LocalTime now = LocalTime.now();
            if (now.isBefore(activeStartTime) || now.isAfter(activeEndTime)) {
                return Duration.ZERO; // No delay outside active hours
            }
        }
        
        // Calculate base delay with progression
        long delayMillis = baseDelay.toMillis();
        if (delayProgression != null) {
            delayMillis += delayProgression.toMillis() * actionCount;
        }
        
        // Apply maximum cap
        if (maximumDelay != null) {
            delayMillis = Math.min(delayMillis, maximumDelay.toMillis());
        }
        
        // Apply random variation
        if (randomVariation > 0) {
            double variation = 1.0 + (random.nextDouble() * 2 - 1) * randomVariation;
            delayMillis = (long) (delayMillis * variation);
        }
        
        actionCount++;
        return Duration.ofMillis(Math.max(0, delayMillis));
    }
    
    /**
     * Reset temporal state
     */
    public void reset() {
        actionCount = 0;
    }
}