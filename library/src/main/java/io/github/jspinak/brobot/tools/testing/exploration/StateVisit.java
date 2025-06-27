package io.github.jspinak.brobot.tools.testing.exploration;

import lombok.Data;

import java.util.Set;

/**
 * Records detailed information about individual state visits during test exploration.
 * 
 * <p>StateVisit captures the essential data about each attempt to visit a state during
 * the comprehensive testing process. This granular tracking enables detailed analysis
 * of test execution, identification of problematic states, and debugging of navigation
 * issues.</p>
 * 
 * <h2>Purpose</h2>
 * <p>Each StateVisit instance represents a single attempt to navigate to and verify
 * a specific state. The class tracks:</p>
 * <ul>
 *   <li>Which state was targeted</li>
 *   <li>When the visit occurred</li>
 *   <li>Whether the visit was successful</li>
 * </ul>
 * 
 * <h2>Success Criteria</h2>
 * <p>A visit is considered successful when:</p>
 * <ul>
 *   <li>The state transition completes without errors</li>
 *   <li>The target state becomes active</li>
 *   <li>State verification passes (if implemented)</li>
 * </ul>
 * 
 * <p>Failed visits may indicate:</p>
 * <ul>
 *   <li>Broken transitions</li>
 *   <li>Missing UI elements</li>
 *   <li>Application errors</li>
 *   <li>Timing issues</li>
 * </ul>
 * 
 * <h2>Integration with Test Framework</h2>
 * <p>StateVisit instances are collected by {@link StateTraversalService} during
 * exploration and can be analyzed to:</p>
 * <ul>
 *   <li>Generate test reports showing visit patterns</li>
 *   <li>Identify unreachable or problematic states</li>
 *   <li>Calculate test coverage metrics</li>
 *   <li>Debug navigation failures</li>
 * </ul>
 * 
 * <h2>Timestamp Usage</h2>
 * <p>The timestamp is captured at the moment of visit attempt, enabling:</p>
 * <ul>
 *   <li>Chronological ordering of visits</li>
 *   <li>Performance analysis (time between visits)</li>
 *   <li>Correlation with logs and recordings</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Record a successful state visit
 * StateVisit visit = new StateVisit(
 *     state.getId(),
 *     state.getName(),
 *     true  // transition succeeded
 * );
 * 
 * // Later analysis
 * List<StateVisit> failedVisits = allVisits.stream()
 *     .filter(v -> !v.isSuccessful())
 *     .collect(Collectors.toList());
 * }</pre>
 * 
 * @see StateTraversalService for the main exploration logic that creates these records
 * @see TestRun for the overall test session context
 * @see StateExplorationTracker for tracking which states still need visiting
 * @author jspinak
 */
@Data
public class StateVisit {
    private final Long stateId;
    private final String stateName;
    private final long timestamp;
    private final boolean successful;

    /**
     * Creates a new StateVisit record with automatic timestamp capture.
     * 
     * <p>Constructs a visit record capturing all essential information about
     * a state visit attempt. The timestamp is automatically set to the current
     * system time when the visit occurs.</p>
     * 
     * @param stateId the unique identifier of the state being visited.
     *                Used for database references and state correlation.
     * @param stateName the human-readable name of the state.
     *                  Useful for logging and report generation.
     * @param successful whether the visit attempt succeeded.
     *                   true if the state was successfully reached and verified,
     *                   false if the transition failed or the state couldn't be verified.
     * @throws NullPointerException if stateId or stateName is null
     */
    public StateVisit(Long stateId, String stateName, boolean successful) {
        this.stateId = stateId;
        this.stateName = stateName;
        this.timestamp = System.currentTimeMillis();
        this.successful = successful;
    }

}
