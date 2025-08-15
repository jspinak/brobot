package io.github.jspinak.brobot.tools.testing.mock.scenario;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Configuration for scenario-based mock testing with advanced conditions and patterns.
 * <p>
 * This class enables sophisticated test scenarios by providing configurable:
 * <ul>
 * <li>State appearance probabilities for different GUI states</li>
 * <li>Action failure patterns with temporal and conditional triggers</li>
 * <li>Time-based conditions for simulating real-world timing variations</li>
 * <li>Cascading failure scenarios for robust error handling testing</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * MockScenarioConfig config = MockScenarioConfig.builder()
 *     .scenarioName("login_network_issues")
 *     .stateAppearanceProbability("LOGIN_STATE", 0.8)
 *     .actionFailurePattern(ActionType.FIND, 
 *         FailurePattern.builder()
 *             .probabilityDecay(0.1)
 *             .maxConsecutiveFailures(3)
 *             .build())
 *     .temporalCondition("slow_network", Duration.ofSeconds(2))
 *     .build();
 * }</pre>
 *
 * @see FailurePattern
 * @see TemporalConditions
 * @see MockScenarioManager
 */
@Data
@Builder(toBuilder = true)
public class MockScenarioConfig {
    
    /**
     * Human-readable name for the test scenario.
     */
    private final String scenarioName;
    
    /**
     * Description of what the scenario is testing.
     */
    private final String description;
    
    /**
     * Probability (0.0-1.0) that each state will appear when searched for.
     * States not in this map use default mock behavior.
     */
    @Singular("stateAppearanceProbability")
    private final Map<String, Double> stateAppearanceProbabilities;
    
    /**
     * Failure patterns for different action types, enabling simulation
     * of intermittent failures, cascading errors, and recovery scenarios.
     */
    @Singular("actionFailurePattern")
    private final Map<ActionType, FailurePattern> actionFailurePatterns;
    
    /**
     * Time-based conditions that affect mock behavior, such as
     * network delays, processing time variations, or timeout simulations.
     */
    @Singular("temporalCondition")
    private final Map<String, TemporalConditions> temporalConditions;
    
    /**
     * Global conditions that must be met for this scenario to be active.
     * Useful for environment-specific testing or progressive scenarios.
     */
    @Singular("activationCondition")
    private final Map<String, Predicate<MockTestContext>> activationConditions;
    
    /**
     * Maximum duration this scenario should run before automatically
     * switching to default mock behavior.
     */
    private final Duration maxDuration;
    
    /**
     * Timestamp when this scenario was activated.
     */
    private LocalDateTime activatedAt;
    
    /**
     * Whether this scenario should cascade failures (one failure increases
     * probability of subsequent failures).
     */
    @Builder.Default
    private final boolean cascadingFailures = false;
    
    /**
     * Factor by which to multiply failure probabilities when cascading is enabled.
     */
    @Builder.Default
    private final double cascadeMultiplier = 1.5;
    
    /**
     * Checks if this scenario is currently active based on duration and conditions.
     *
     * @param context current test execution context
     * @return true if scenario should be applied
     */
    public boolean isActive(MockTestContext context) {
        if (activatedAt == null) {
            activatedAt = LocalDateTime.now();
        }
        
        // Check duration limit
        if (maxDuration != null && 
            Duration.between(activatedAt, LocalDateTime.now()).compareTo(maxDuration) > 0) {
            return false;
        }
        
        // Check activation conditions
        return activationConditions.values().stream()
            .allMatch(condition -> condition.test(context));
    }
    
    /**
     * Gets the failure pattern for a specific action type.
     *
     * @param action the action type to check
     * @return failure pattern or null if none configured
     */
    public FailurePattern getFailurePattern(ActionType action) {
        return actionFailurePatterns.get(action);
    }
    
    /**
     * Gets the appearance probability for a state.
     *
     * @param stateName name of the state
     * @return probability (0.0-1.0) or null if not configured
     */
    public Double getStateAppearanceProbability(String stateName) {
        return stateAppearanceProbabilities.get(stateName);
    }
    
    /**
     * Gets temporal conditions by name.
     *
     * @param conditionName name of the temporal condition
     * @return temporal conditions or null if not found
     */
    public TemporalConditions getTemporalCondition(String conditionName) {
        return temporalConditions.get(conditionName);
    }
    
    /**
     * Creates a copy of this config with updated activation timestamp.
     *
     * @return new config instance with current activation time
     */
    public MockScenarioConfig activate() {
        return this.toBuilder()
            .activatedAt(LocalDateTime.now())
            .build();
    }
}