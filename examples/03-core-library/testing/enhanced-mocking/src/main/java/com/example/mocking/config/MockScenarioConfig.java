package com.example.mocking.config;

import io.github.jspinak.brobot.action.ActionType;
import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a mock scenario.
 */
@Data
@Builder
public class MockScenarioConfig {
    
    private String scenarioName;
    private String description;
    
    // State appearance probabilities (0.0 - 1.0)
    @Builder.Default
    private Map<String, Double> stateAppearanceProbabilities = new HashMap<>();
    
    // Action failure patterns
    @Builder.Default
    private Map<ActionType, FailurePattern> actionFailurePatterns = new HashMap<>();
    
    // Temporal conditions
    @Builder.Default
    private Map<String, TemporalConditions> temporalConditions = new HashMap<>();
    
    // Maximum scenario duration
    private Duration maxDuration;
    
    // Enable cascading failures
    @Builder.Default
    private boolean cascadingFailures = false;
    
    // Enable performance tracking
    @Builder.Default
    private boolean trackPerformance = true;
    
    // Custom success criteria (removed for simplicity)
    
    /**
     * Convenience methods for configuration
     */
    public MockScenarioConfig stateAppearanceProbability(String stateName, double probability) {
        this.stateAppearanceProbabilities.put(stateName, probability);
        return this;
    }
    
    public MockScenarioConfig actionFailurePattern(ActionType action, FailurePattern pattern) {
        this.actionFailurePatterns.put(action, pattern);
        return this;
    }
    
    public MockScenarioConfig temporalCondition(String name, TemporalConditions condition) {
        this.temporalConditions.put(name, condition);
        return this;
    }
}