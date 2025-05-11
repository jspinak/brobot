package io.github.jspinak.brobot.schemaValidation.business;

import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Main entry point for business rule validation.
 * This class coordinates validation of business rules across different
 * domains including transitions and functions.
 */
@Component
public class BusinessRuleValidator {
    private static final Logger logger = LoggerFactory.getLogger(BusinessRuleValidator.class);

    private final TransitionRuleValidator transitionRuleValidator;
    private final FunctionRuleValidator functionRuleValidator;

    public BusinessRuleValidator(TransitionRuleValidator transitionRuleValidator,
                                 FunctionRuleValidator functionRuleValidator) {
        this.transitionRuleValidator = transitionRuleValidator;
        this.functionRuleValidator = functionRuleValidator;
    }

    /**
     * Validates all business rules in the project and DSL models.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result containing any business rule violations
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
     * Validates rules that span multiple domains, such as interactions
     * between transitions and functions.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result
     */
    private ValidationResult validateCrossDomainRules(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        // Implement cross-domain validation rules here
        // For example, validating that functions used in transitions follow certain patterns

        return result;
    }
}