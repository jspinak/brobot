package io.github.jspinak.brobot.tools.testing.mock.verification;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verification for action execution patterns and retry behavior.
 * <p>
 * This class validates that actions are executed according to expected patterns,
 * including:
 * <ul>
 * <li>Maximum retry attempts for failed actions</li>
 * <li>Backoff timing between retries</li>
 * <li>Success rate expectations</li>
 * <li>Action sequence dependencies</li>
 * </ul>
 *
 * @see MockBehaviorVerifier
 * @see VerificationResult
 */
@Data
public class ActionPatternVerification {
    
    private final String verificationId;
    private final ActionType targetAction;
    private final int maxAttempts;
    private final Duration backoffDuration;
    private final double expectedSuccessRate;
    private final Duration verificationWindow;
    private final LocalDateTime startTime;
    
    private final List<ActionAttempt> attempts = new ArrayList<>();
    private VerificationResult result = VerificationResult.IN_PROGRESS;
    private final List<String> errors = new ArrayList<>();
    private LocalDateTime firstEventTime = null;
    
    private ActionPatternVerification(String verificationId, ActionType targetAction,
                                     int maxAttempts, Duration backoffDuration, 
                                     double expectedSuccessRate, Duration verificationWindow) {
        this.verificationId = verificationId;
        this.targetAction = targetAction;
        this.maxAttempts = maxAttempts;
        this.backoffDuration = backoffDuration;
        this.expectedSuccessRate = expectedSuccessRate;
        this.verificationWindow = verificationWindow;
        this.startTime = LocalDateTime.now();
    }
    
    /**
     * Processes a new execution event to check against expected patterns.
     *
     * @param event the execution event to check
     */
    public void checkEvent(MockBehaviorVerifier.ExecutionEvent event) {
        if (result != VerificationResult.IN_PROGRESS) {
            return; // Verification already completed
        }
        
        // Skip non-matching events before checking window
        if (!event.isActionEvent() || !event.getAction().equals(targetAction)) {
            return; // Not the action we're monitoring
        }
        
        // Track first event time for window calculation
        if (firstEventTime == null) {
            firstEventTime = event.getTimestamp();
            // If this is the first event and we're already expired, complete immediately
            if (isExpired()) {
                completeVerification();
                return;
            }
        } else {
            // Check if verification window has expired based on event timestamps
            if (verificationWindow != null) {
                Duration elapsed = Duration.between(firstEventTime, event.getTimestamp());
                if (elapsed.compareTo(verificationWindow) > 0) {
                    completeVerification();
                    return;
                }
            }
        }
        
        // Record this attempt
        boolean success = event.getResult() != null && event.getResult().isSuccess();
        ActionAttempt attempt = new ActionAttempt(event.getTimestamp(), success);
        attempts.add(attempt);
        
        // Check retry pattern
        if (attempts.size() > 1) {
            checkRetryTiming();
        }
        
        // Check maximum attempts
        if (attempts.size() > maxAttempts) {
            errors.add(String.format("Action %s exceeded maximum attempts: %d (max: %d)", 
                targetAction, attempts.size(), maxAttempts));
            result = VerificationResult.FAILED;
            return;
        }
        
        // Check if we can complete verification early
        if (success && expectedSuccessRate > 0) {
            // Successful action might indicate completion of retry sequence
            completeVerification();
        }
    }
    
    /**
     * Checks if retry timing follows the expected backoff pattern.
     */
    private void checkRetryTiming() {
        if (backoffDuration == null || attempts.size() < 2) {
            return;
        }
        
        ActionAttempt previous = attempts.get(attempts.size() - 2);
        ActionAttempt current = attempts.get(attempts.size() - 1);
        
        Duration actualDelay = Duration.between(previous.getTimestamp(), current.getTimestamp());
        
        // Allow some tolerance (±10%) for timing variations
        Duration minExpected = backoffDuration.multipliedBy(9).dividedBy(10);
        Duration maxExpected = backoffDuration.multipliedBy(11).dividedBy(10);
        
        if (actualDelay.compareTo(minExpected) < 0) {
            errors.add(String.format("Retry delay too short: %s (expected: ~%s)", 
                actualDelay, backoffDuration));
        }
        
        if (actualDelay.compareTo(maxExpected) > 0) {
            errors.add(String.format("Retry delay too long: %s (expected: ~%s)", 
                actualDelay, backoffDuration));
        }
    }
    
    /**
     * Completes the verification and calculates final result.
     */
    private void completeVerification() {
        if (result != VerificationResult.IN_PROGRESS) {
            return;
        }
        
        // Check success rate
        if (expectedSuccessRate > 0 && !attempts.isEmpty()) {
            long successCount = attempts.stream()
                .mapToLong(attempt -> attempt.isSuccess() ? 1 : 0)
                .sum();
            
            double actualSuccessRate = (double) successCount / attempts.size();
            
            if (actualSuccessRate < expectedSuccessRate) {
                errors.add(String.format("Success rate too low: %.2f (expected: %.2f)", 
                    actualSuccessRate, expectedSuccessRate));
            }
        }
        
        result = errors.isEmpty() ? VerificationResult.PASSED : VerificationResult.FAILED;
    }
    
    /**
     * Gets the current verification result.
     *
     * @return current result status
     */
    public VerificationResult getResult() {
        return result;
    }
    
    /**
     * Gets any errors that occurred during verification.
     *
     * @return list of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * Gets the number of recorded attempts.
     *
     * @return attempt count
     */
    public int getAttemptCount() {
        return attempts.size();
    }
    
    /**
     * Checks if the verification window has expired based on real time.
     * This is used for timeout detection when no events are being received.
     *
     * @return true if the verification window has expired
     */
    public boolean isExpired() {
        if (verificationWindow == null || result != VerificationResult.IN_PROGRESS) {
            return false;
        }
        
        Duration realTimeElapsed = Duration.between(startTime, LocalDateTime.now());
        return realTimeElapsed.compareTo(verificationWindow) > 0;
    }
    
    /**
     * Represents a single action attempt.
     */
    @Data
    public static class ActionAttempt {
        private final LocalDateTime timestamp;
        private final boolean success;
    }
    
    /**
     * Builder for creating action pattern verifications.
     */
    public static class Builder {
        private final String verificationId;
        private final MockBehaviorVerifier verifier;
        private ActionType targetAction;
        private int maxAttempts = Integer.MAX_VALUE;
        private Duration backoffDuration;
        private double expectedSuccessRate = 0.0;
        private Duration verificationWindow;
        
        public Builder(String verificationId, MockBehaviorVerifier verifier) {
            this.verificationId = verificationId;
            this.verifier = verifier;
        }
        
        /**
         * Sets the action type to monitor.
         *
         * @param action the action type
         * @return this builder
         */
        public Builder action(ActionType action) {
            this.targetAction = action;
            return this;
        }
        
        /**
         * Sets the maximum number of attempts allowed.
         *
         * @param maxAttempts maximum attempts
         * @return this builder
         */
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }
        
        /**
         * Sets the expected backoff duration between retries.
         *
         * @param backoff backoff duration
         * @return this builder
         */
        public Builder withBackoff(Duration backoff) {
            this.backoffDuration = backoff;
            return this;
        }
        
        /**
         * Sets the expected success rate.
         *
         * @param successRate expected success rate (0.0-1.0)
         * @return this builder
         */
        public Builder expectedSuccessRate(double successRate) {
            this.expectedSuccessRate = successRate;
            return this;
        }
        
        /**
         * Sets the verification window duration.
         *
         * @param window verification window
         * @return this builder
         */
        public Builder within(Duration window) {
            this.verificationWindow = window;
            return this;
        }
        
        /**
         * Starts the verification and returns the verification object.
         *
         * @return the created verification
         */
        public ActionPatternVerification verify() {
            if (targetAction == null) {
                throw new IllegalStateException("Target action must be specified");
            }
            
            ActionPatternVerification verification = 
                new ActionPatternVerification(verificationId, targetAction, maxAttempts, 
                    backoffDuration, expectedSuccessRate, verificationWindow);
            verifier.addPatternVerification(verificationId, verification);
            return verification;
        }
    }
}