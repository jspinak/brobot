/**
 * Business rule validators for semantic correctness.
 *
 * <p>This package contains validators that enforce domain-specific business rules and semantic
 * constraints on Brobot configurations. These validators go beyond structural validation to ensure
 * that configurations make logical sense within the context of GUI automation.
 *
 * <h2>Validators</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.business.BusinessRuleValidator} -
 *       Orchestrates all business rule validations
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.business.FunctionRuleValidator} -
 *       Validates automation function definitions
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.business.TransitionRuleValidator} -
 *       Validates state transition logic
 * </ul>
 *
 * <h2>Business Rules</h2>
 *
 * <h3>Function Rules</h3>
 *
 * <ul>
 *   <li>Function names must be unique
 *   <li>Parameters must have valid types
 *   <li>Return statements match function signature
 *   <li>No unreachable code after return
 *   <li>Variable usage before declaration
 * </ul>
 *
 * <h3>Transition Rules</h3>
 *
 * <ul>
 *   <li>Source and target states must exist
 *   <li>No duplicate transitions between states
 *   <li>Transition actions are valid
 *   <li>Score thresholds are reasonable (0-1)
 *   <li>At least one transition from initial states
 * </ul>
 *
 * <h3>State Rules</h3>
 *
 * <ul>
 *   <li>State names are unique
 *   <li>Initial states are reachable
 *   <li>No orphaned states
 *   <li>Hidden states properly configured
 * </ul>
 *
 * <h2>Validation Examples</h2>
 *
 * <h3>Function Validation</h3>
 *
 * <pre>{@code
 * // Validates parameter types
 * function login(username: string, attempts: number) {
 *     // OK: parameters have valid types
 * }
 *
 * // Error: invalid parameter type
 * function invalid(data: complexType) {
 *     // ERROR: 'complexType' is not a valid type
 * }
 * }</pre>
 *
 * <h3>Transition Validation</h3>
 *
 * <pre>{@code
 * // Valid transition
 * {
 *   "from": "loginPage",
 *   "to": "homePage",
 *   "action": "clickLogin",
 *   "scoreThreshold": 0.9
 * }
 *
 * // Invalid: score out of range
 * {
 *   "from": "page1",
 *   "to": "page2",
 *   "scoreThreshold": 1.5  // ERROR: > 1.0
 * }
 * }</pre>
 *
 * <h2>Complex Rules</h2>
 *
 * <h3>Reachability Analysis</h3>
 *
 * <pre>{@code
 * // Ensures all states can be reached
 * validator.validateReachability(states, transitions);
 *
 * // Detects cycles
 * validator.detectCycles(transitions);
 *
 * // Finds dead ends
 * validator.findDeadEnds(states, transitions);
 * }</pre>
 *
 * <h3>Action Sequence Validation</h3>
 *
 * <pre>{@code
 * // Validates action sequences make sense
 * validator.validateActionSequence(Arrays.asList(
 *     new Action(FIND),
 *     new Action(CLICK),  // OK: click after find
 *     new Action(TYPE)    // OK: type after click
 * ));
 * }</pre>
 *
 * <h2>Custom Business Rules</h2>
 *
 * <p>Adding custom rules:
 *
 * <pre>{@code
 * @Component
 * public class CustomRuleValidator {
 *     public void validateCustomRules(Project project,
 *                                   ValidationResult result) {
 *         // Check project-specific constraints
 *         if (project.getStates().size() > 100) {
 *             result.addWarning(new ValidationError(
 *                 ValidationSeverity.WARNING,
 *                 "Large number of states may impact performance",
 *                 "$.states"
 *             ));
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Validate logical consistency, not just syntax
 *   <li>Provide suggestions for fixing violations
 *   <li>Use warnings for non-critical issues
 *   <li>Consider performance implications
 *   <li>Validate incrementally when possible
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.validation
 * @see io.github.jspinak.brobot.model.state
 */
package io.github.jspinak.brobot.runner.json.validation.business;
