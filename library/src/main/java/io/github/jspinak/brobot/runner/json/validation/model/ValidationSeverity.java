package io.github.jspinak.brobot.runner.json.validation.model;

/**
 * Defines severity levels for validation errors in Brobot configurations.
 *
 * <p>This enum categorizes validation issues by their impact on system operation, helping
 * developers and users prioritize which issues to address first. The severity levels follow a clear
 * hierarchy from informational messages to critical failures.
 *
 * <h2>Severity Level Guidelines:</h2>
 *
 * <p>When assigning severity levels during validation:
 *
 * <ul>
 *   <li>Consider the impact on runtime behavior
 *   <li>Think about data integrity and consistency
 *   <li>Evaluate the likelihood of user confusion
 *   <li>Assess the difficulty of debugging if ignored
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * // In a validator
 * if (stateId == null) {
 *     result.addError(new ValidationError(
 *         "Missing state ID",
 *         "Transition must specify a source state",
 *         ValidationSeverity.CRITICAL  // Cannot execute without state
 *     ));
 * } else if (!isEfficient(transition)) {
 *     result.addError(new ValidationError(
 *         "Inefficient transition",
 *         "This transition pattern may cause slow execution",
 *         ValidationSeverity.WARNING   // Works but not optimal
 *     ));
 * }
 *
 * // In error handling
 * if (result.hasErrors()) {
 *     result.getErrors().stream()
 *         .filter(e -> e.severity() == ValidationSeverity.CRITICAL)
 *         .forEach(e -> blockExecution(e));
 * }
 * }</pre>
 *
 * @see ValidationError for usage in error reporting
 * @see ValidationResult for severity-based filtering methods
 * @author jspinak
 */
public enum ValidationSeverity {
    /**
     * Minor issue that should be addressed but doesn't prevent operation.
     *
     * <p>Warnings indicate suboptimal configurations, potential performance issues, or deviations
     * from best practices. The system will function but may not perform optimally or may be harder
     * to maintain.
     *
     * <h3>Common Warning Scenarios:</h3>
     *
     * <ul>
     *   <li>Performance anti-patterns detected
     *   <li>Deprecated feature usage
     *   <li>Missing optional but recommended elements
     *   <li>Suspicious but not definitely wrong patterns
     * </ul>
     */
    WARNING,

    /**
     * Significant issue that will likely cause problems during execution.
     *
     * <p>Errors represent violations that will probably lead to runtime failures, incorrect
     * behavior, or data corruption. While the configuration might load, it's unlikely to work
     * correctly without fixing these issues.
     *
     * <h3>Common Error Scenarios:</h3>
     *
     * <ul>
     *   <li>Invalid references to non-existent elements
     *   <li>Type mismatches in parameters
     *   <li>Missing required configuration elements
     *   <li>Logical inconsistencies in state machines
     * </ul>
     */
    ERROR,

    /**
     * Severe issue that completely prevents the configuration from being used.
     *
     * <p>Critical errors indicate fundamental problems that make it impossible to load or execute
     * the configuration. These must be fixed before any operations can proceed.
     *
     * <h3>Common Critical Scenarios:</h3>
     *
     * <ul>
     *   <li>Malformed JSON that cannot be parsed
     *   <li>Schema violations in required sections
     *   <li>Missing essential configuration files
     *   <li>Security violations or malicious patterns
     * </ul>
     */
    CRITICAL,

    /**
     * Informational message providing helpful context or suggestions.
     *
     * <p>Info messages are not errors but provide useful information about the configuration, such
     * as optimization opportunities, usage tips, or confirmation of automatic corrections.
     *
     * <h3>Common Info Scenarios:</h3>
     *
     * <ul>
     *   <li>Suggestions for improved configuration
     *   <li>Information about default values being used
     *   <li>Tips for better performance or maintainability
     *   <li>Confirmation of automatic fixes or migrations
     * </ul>
     */
    INFO
}
