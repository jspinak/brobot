package io.github.jspinak.brobot.config.mock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Simplified mock configuration properties for the Brobot framework.
 *
 * <p>This class provides a streamlined approach to mock mode configuration with just two
 * essential properties:
 *
 * <ul>
 *   <li><b>enabled</b>: Master switch for mock mode (replaces brobot.mock, brobot.core.mock,
 *       brobot.framework.mock)
 *   <li><b>actionSuccessProbability</b>: Probability of action success (0.0 to 1.0)
 * </ul>
 *
 * <p>The simplified design eliminates redundancy and provides clearer control over mock behavior.
 * Instead of multiple overlapping boolean flags, there's now a single enable switch and a
 * probability setting for fine-grained control over action success rates.
 *
 * <p>Configuration example:
 *
 * <pre>{@code
 * # application.properties
 * brobot.mock.enabled=true
 * brobot.mock.action.success.probability=0.95
 * }</pre>
 *
 * @since 1.2.0
 */
@Component
@ConfigurationProperties(prefix = "brobot.mock")
@Data
public class MockProperties {

    /**
     * Master switch for enabling mock mode.
     *
     * <p>When true, all actions are simulated rather than performed on the actual GUI. This is
     * useful for:
     *
     * <ul>
     *   <li>Unit testing without GUI dependencies
     *   <li>Development in headless environments
     *   <li>CI/CD pipeline execution
     *   <li>Demonstrations without live applications
     * </ul>
     *
     * <p>Default: false (real execution mode)
     */
    private boolean enabled = false;

    /**
     * Probability of action success in mock mode (0.0 to 1.0).
     *
     * <p>Controls how often simulated actions succeed:
     *
     * <ul>
     *   <li>1.0 = All actions always succeed (default)
     *   <li>0.95 = 95% success rate (realistic simulation)
     *   <li>0.5 = 50% success rate (stress testing)
     *   <li>0.0 = All actions always fail (failure testing)
     * </ul>
     *
     * <p>This applies to actions like click, type, and drag. Find operations still require
     * ActionSnapshots for proper match simulation.
     *
     * <p>Default: 1.0 (100% success)
     */
    private ActionSuccessProbability action = new ActionSuccessProbability();

    /**
     * Nested configuration for action success probability.
     */
    @Data
    public static class ActionSuccessProbability {
        
        /**
         * Success probability for all actions (0.0 to 1.0).
         * Default: 1.0 (100% success rate)
         */
        private double successProbability = 1.0;

        /**
         * Validates and returns the success probability, ensuring it's within valid range.
         *
         * @return clamped probability between 0.0 and 1.0
         */
        public double getSuccessProbability() {
            return Math.max(0.0, Math.min(1.0, successProbability));
        }
    }

    /**
     * Checks if an action should succeed based on the configured probability.
     *
     * @return true if the action should succeed based on random probability
     */
    public boolean shouldActionSucceed() {
        return Math.random() < action.getSuccessProbability();
    }
}