package io.github.jspinak.brobot.tools.testing.mock.verification;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.tools.testing.mock.scenario.MockTestContext;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Advanced verification system for mock behavior and state transitions.
 * <p>
 * This component provides comprehensive verification capabilities beyond simple
 * operation counting, enabling validation of:
 * <ul>
 * <li>State transition sequences and timing</li>
 * <li>Action execution patterns and dependencies</li>
 * <li>Performance characteristics under different conditions</li>
 * <li>Error recovery and resilience behavior</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Verify state transition sequence
 * verifier.expectTransitionSequence("login_flow")
 *     .fromState("LOGIN_PAGE")
 *     .toState("DASHBOARD")
 *     .withinTime(Duration.ofSeconds(5))
 *     .verify();
 *     
 * // Verify action execution patterns
 * verifier.expectActionPattern("retry_pattern")
 *     .action(ActionOptions.Action.FIND)
 *     .maxAttempts(3)
 *     .withBackoff(Duration.ofMillis(500))
 *     .verify();
 * }</pre>
 *
 * @see MockTestContext
 * @see StateTransitionVerification
 * @see ActionPatternVerification
 */
@Component
public class MockBehaviorVerifier {
    
    private final Map<String, StateTransitionVerification> activeTransitionVerifications = new ConcurrentHashMap<>();
    private final Map<String, ActionPatternVerification> activePatternVerifications = new ConcurrentHashMap<>();
    private final List<ExecutionEvent> executionHistory = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Starts verification of a state transition sequence.
     *
     * @param verificationId unique identifier for this verification
     * @return transition verification builder
     */
    public StateTransitionVerification.Builder expectTransitionSequence(String verificationId) {
        return new StateTransitionVerification.Builder(verificationId, this);
    }
    
    /**
     * Starts verification of an action execution pattern.
     *
     * @param verificationId unique identifier for this verification
     * @return pattern verification builder
     */
    public ActionPatternVerification.Builder expectActionPattern(String verificationId) {
        return new ActionPatternVerification.Builder(verificationId, this);
    }
    
    /**
     * Records an action execution for verification purposes.
     *
     * @param action the action that was executed
     * @param result the result of the action
     * @param context current test context
     */
    public void recordAction(ActionOptions.Action action, ActionResult result, MockTestContext context) {
        ExecutionEvent event = ExecutionEvent.builder()
            .timestamp(LocalDateTime.now())
            .action(action)
            .result(result)
            .context(context)
            .build();
            
        executionHistory.add(event);
        
        // Check active verifications
        checkActiveVerifications(event);
    }
    
    /**
     * Records a state transition for verification purposes.
     *
     * @param fromState the starting state
     * @param toState the destination state
     * @param context current test context
     */
    public void recordStateTransition(String fromState, String toState, MockTestContext context) {
        ExecutionEvent event = ExecutionEvent.builder()
            .timestamp(LocalDateTime.now())
            .fromState(fromState)
            .toState(toState)
            .context(context)
            .build();
            
        executionHistory.add(event);
        
        // Check active verifications
        checkActiveVerifications(event);
    }
    
    /**
     * Checks all active verifications against the new event.
     *
     * @param event the execution event to check
     */
    private void checkActiveVerifications(ExecutionEvent event) {
        activeTransitionVerifications.values().forEach(verification -> 
            verification.checkEvent(event));
        activePatternVerifications.values().forEach(verification -> 
            verification.checkEvent(event));
    }
    
    /**
     * Gets the complete execution history.
     *
     * @return list of all recorded events
     */
    public List<ExecutionEvent> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }
    
    /**
     * Gets events matching the specified criteria.
     *
     * @param filter predicate to filter events
     * @return filtered list of events
     */
    public List<ExecutionEvent> getEvents(Predicate<ExecutionEvent> filter) {
        return executionHistory.stream()
            .filter(filter)
            .toList();
    }
    
    /**
     * Clears all verification history and active verifications.
     */
    public void reset() {
        executionHistory.clear();
        activeTransitionVerifications.clear();
        activePatternVerifications.clear();
    }
    
    /**
     * Gets verification results for all completed verifications.
     *
     * @return map of verification IDs to their results
     */
    public Map<String, VerificationResult> getVerificationResults() {
        Map<String, VerificationResult> results = new HashMap<>();
        
        activeTransitionVerifications.forEach((id, verification) -> 
            results.put(id, verification.getResult()));
        activePatternVerifications.forEach((id, verification) -> 
            results.put(id, verification.getResult()));
            
        return results;
    }
    
    /**
     * Adds a transition verification to the active set.
     *
     * @param verificationId unique identifier
     * @param verification the verification to add
     */
    void addTransitionVerification(String verificationId, StateTransitionVerification verification) {
        activeTransitionVerifications.put(verificationId, verification);
    }
    
    /**
     * Adds a pattern verification to the active set.
     *
     * @param verificationId unique identifier
     * @param verification the verification to add
     */
    void addPatternVerification(String verificationId, ActionPatternVerification verification) {
        activePatternVerifications.put(verificationId, verification);
    }
    
    /**
     * Represents a single execution event for verification.
     */
    @Data
    @Builder
    public static class ExecutionEvent {
        private final LocalDateTime timestamp;
        private final ActionOptions.Action action;
        private final ActionResult result;
        private final String fromState;
        private final String toState;
        private final MockTestContext context;
        private final Map<String, Object> metadata;
        
        /**
         * Checks if this is an action event.
         *
         * @return true if this event represents an action execution
         */
        public boolean isActionEvent() {
            return action != null;
        }
        
        /**
         * Checks if this is a state transition event.
         *
         * @return true if this event represents a state transition
         */
        public boolean isStateTransitionEvent() {
            return fromState != null && toState != null;
        }
        
        /**
         * Gets the duration since this event occurred.
         *
         * @return duration from event timestamp to now
         */
        public Duration getAge() {
            return Duration.between(timestamp, LocalDateTime.now());
        }
    }
}