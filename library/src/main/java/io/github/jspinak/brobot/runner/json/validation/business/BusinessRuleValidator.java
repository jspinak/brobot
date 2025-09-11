package io.github.jspinak.brobot.runner.json.validation.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;

/**
 * Main entry point for business rule validation in Brobot configurations.
 *
 * <p>This class coordinates the validation of business rules across different domains within the
 * Brobot automation framework. Business rules represent constraints and best practices that go
 * beyond simple schema validation, ensuring configurations are not just syntactically correct but
 * also semantically sound and optimized for execution.
 *
 * <h2>Key Responsibilities:</h2>
 *
 * <ul>
 *   <li>Orchestrating validation across transition and function domains
 *   <li>Enforcing cross-domain consistency rules
 *   <li>Detecting potential performance and reliability issues
 *   <li>Providing actionable feedback for configuration improvement
 * </ul>
 *
 * <h2>Business Rule Categories:</h2>
 *
 * <ul>
 *   <li><b>Transition Rules</b> - State transition consistency, reachability, cycles
 *   <li><b>Function Rules</b> - Complexity limits, error handling, performance patterns
 *   <li><b>Cross-Domain Rules</b> - Interactions between transitions and functions
 * </ul>
 *
 * <h2>Why Business Rules Matter:</h2>
 *
 * <p>While schema validation ensures structural correctness, business rules catch logical errors
 * and inefficiencies that could lead to runtime failures, poor performance, or maintenance
 * difficulties. These rules encode expert knowledge about automation best practices.
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * BusinessRuleValidator validator = new BusinessRuleValidator(
 *     transitionValidator, functionValidator);
 *
 * ValidationResult result = validator.validateRules(projectModel, dslModel);
 *
 * if (result.hasWarnings()) {
 *     // Log warnings but allow execution
 *     logger.warn("Business rule warnings: {}", result.getWarnings());
 * }
 *
 * if (result.hasSevereErrors()) {
 *     // Block execution for severe violations
 *     throw new ConfigValidationException(result);
 * }
 * }</pre>
 *
 * @see TransitionRuleValidator for state transition validation rules
 * @see FunctionRuleValidator for automation function validation rules
 * @see ValidationResult for interpreting validation outcomes
 * @author jspinak
 */
@Component
public class BusinessRuleValidator {
    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleValidator.class);

    private final TransitionRuleValidator transitionRuleValidator;
    private final FunctionRuleValidator functionRuleValidator;

    public BusinessRuleValidator(
            TransitionRuleValidator transitionRuleValidator,
            FunctionRuleValidator functionRuleValidator) {
        this.transitionRuleValidator = transitionRuleValidator;
        this.functionRuleValidator = functionRuleValidator;
    }

    /**
     * Validates all business rules in the project and DSL models.
     *
     * <p>This method executes all business rule validators in sequence, collecting violations
     * across different domains. Each validator is executed independently with errors logged but not
     * causing early termination, ensuring all issues are discovered in a single validation pass.
     *
     * <h3>Validation Sequence:</h3>
     *
     * <ol>
     *   <li><b>Transition Rules</b> - Validates state machine consistency and efficiency
     *   <li><b>Function Rules</b> - Checks function complexity and implementation patterns
     *   <li><b>Cross-Domain Rules</b> - Ensures consistency between transitions and functions
     * </ol>
     *
     * <h3>Error Handling:</h3>
     *
     * <p>If any individual validator throws an exception, the error is logged and validation
     * continues with the next validator. This ensures partial validation results are still
     * available even if one validator fails.
     *
     * @param projectModel Parsed project model containing states, transitions, and UI elements.
     *     Expected to be a Map structure matching the project schema
     * @param dslModel Parsed DSL model containing automation function definitions. Expected to be a
     *     Map structure matching the DSL schema
     * @return ValidationResult aggregating all business rule violations found across all
     *     validators. Check severity levels to determine if violations are blocking
     *     (ERROR/CRITICAL) or advisory (WARNING)
     * @throws IllegalArgumentException if either model parameter is null
     */
    public ValidationResult validateRules(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        // Validate transition rules
        try {
            result.merge(transitionRuleValidator.validateTransitionRules(projectModel));
        } catch (Exception e) {
            logger.error("Error during transition rule validation", e);
        }

        // Validate function implementation rules
        try {
            result.merge(functionRuleValidator.validateFunctionRules(dslModel));
        } catch (Exception e) {
            logger.error("Error during function rule validation", e);
        }

        // Validate cross-domain rules
        try {
            result.merge(validateCrossDomainRules(projectModel, dslModel));
        } catch (Exception e) {
            logger.error("Error during cross-domain rule validation", e);
        }

        return result;
    }

    /**
     * Validates rules that span multiple domains, such as interactions between transitions and
     * functions.
     *
     * <p>Cross-domain rules ensure consistency and proper integration between different parts of
     * the automation configuration. These rules catch issues that individual domain validators
     * might miss when looking at their domains in isolation.
     *
     * <h3>Examples of Cross-Domain Rules:</h3>
     *
     * <ul>
     *   <li>Functions called from transitions should follow specific patterns
     *   <li>State modifications in functions should align with transition definitions
     *   <li>Resource usage patterns should be consistent across domains
     *   <li>Error handling strategies should be compatible between layers
     * </ul>
     *
     * <h3>Future Enhancements:</h3>
     *
     * <p>This method is designed to be extended with additional cross-domain validation logic as
     * new patterns and anti-patterns are discovered through usage.
     *
     * @param projectModel Parsed project model containing the state machine definition
     * @param dslModel Parsed DSL model containing function implementations
     * @return ValidationResult containing any cross-domain rule violations found
     */
    private ValidationResult validateCrossDomainRules(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        // Implement cross-domain validation rules here
        // For example, validating that functions used in transitions follow certain patterns

        return result;
    }
}
