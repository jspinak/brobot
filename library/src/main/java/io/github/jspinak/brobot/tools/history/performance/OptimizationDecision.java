package io.github.jspinak.brobot.tools.history.performance;

import lombok.Data;

/**
 * Represents a decision made by the illustration performance optimizer.
 * <p>
 * This class encapsulates the optimizer's decision about whether and how
 * to proceed with illustration generation, including the reasoning behind
 * the decision for debugging and analysis purposes.
 *
 * @see IllustrationPerformanceOptimizer
 */
@Data
public class OptimizationDecision {
    
    /**
     * The type of decision made.
     */
    private final DecisionType decision;
    
    /**
     * Human-readable reason for the decision.
     */
    private final String reason;
    
    /**
     * Additional metadata about the decision.
     */
    private final Object metadata;
    
    private OptimizationDecision(DecisionType decision, String reason, Object metadata) {
        this.decision = decision;
        this.reason = reason;
        this.metadata = metadata;
    }
    
    /**
     * Creates a decision to proceed with illustration generation.
     *
     * @param reason explanation for the decision
     * @return optimization decision
     */
    public static OptimizationDecision proceed(String reason) {
        return new OptimizationDecision(DecisionType.PROCEED, reason, null);
    }
    
    /**
     * Creates a decision to skip illustration generation.
     *
     * @param reason explanation for the decision
     * @return optimization decision
     */
    public static OptimizationDecision skip(String reason) {
        return new OptimizationDecision(DecisionType.SKIP, reason, null);
    }
    
    /**
     * Creates a decision to batch the illustration for later processing.
     *
     * @param reason explanation for the decision
     * @return optimization decision
     */
    public static OptimizationDecision batch(String reason) {
        return new OptimizationDecision(DecisionType.BATCH, reason, null);
    }
    
    /**
     * Creates a decision to defer illustration to a later time.
     *
     * @param reason explanation for the decision
     * @param retryAfter suggested retry delay
     * @return optimization decision
     */
    public static OptimizationDecision defer(String reason, java.time.Duration retryAfter) {
        return new OptimizationDecision(DecisionType.DEFER, reason, retryAfter);
    }
    
    /**
     * Checks if the decision is to proceed with illustration.
     *
     * @return true if should proceed
     */
    public boolean shouldProceed() {
        return decision == DecisionType.PROCEED;
    }
    
    /**
     * Checks if the decision is to skip illustration.
     *
     * @return true if should skip
     */
    public boolean shouldSkip() {
        return decision == DecisionType.SKIP;
    }
    
    /**
     * Checks if the decision is to batch illustration.
     *
     * @return true if should batch
     */
    public boolean shouldBatch() {
        return decision == DecisionType.BATCH;
    }
    
    /**
     * Checks if the decision is to defer illustration.
     *
     * @return true if should defer
     */
    public boolean shouldDefer() {
        return decision == DecisionType.DEFER;
    }
    
    /**
     * Gets retry delay if decision is to defer.
     *
     * @return retry delay or null if not applicable
     */
    public java.time.Duration getRetryAfter() {
        if (metadata instanceof java.time.Duration) {
            return (java.time.Duration) metadata;
        }
        return null;
    }
    
    /**
     * Types of optimization decisions.
     */
    public enum DecisionType {
        /**
         * Proceed with illustration generation immediately.
         */
        PROCEED,
        
        /**
         * Skip illustration generation entirely.
         */
        SKIP,
        
        /**
         * Add to batch for later processing.
         */
        BATCH,
        
        /**
         * Defer illustration to a later time.
         */
        DEFER
    }
}