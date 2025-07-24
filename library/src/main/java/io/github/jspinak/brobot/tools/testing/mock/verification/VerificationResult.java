package io.github.jspinak.brobot.tools.testing.mock.verification;

/**
 * Enumeration of possible verification result states.
 * 
 * @see MockBehaviorVerifier
 * @see StateTransitionVerification
 * @see ActionPatternVerification
 */
public enum VerificationResult {
    /**
     * Verification is still in progress.
     */
    IN_PROGRESS,
    
    /**
     * Verification completed successfully with all conditions met.
     */
    PASSED,
    
    /**
     * Verification failed due to unmet conditions or errors.
     */
    FAILED,
    
    /**
     * Verification timed out before completion.
     */
    TIMED_OUT
}