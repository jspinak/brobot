package io.github.jspinak.brobot.schemaValidation.crossref;

import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Main facade for cross-reference validation between configuration entities.
 * This class coordinates validation of references between various components
 * of the project and automation DSL models.
 */
@Component
public class ReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceValidator.class);

    private final StateReferenceValidator stateReferenceValidator;
    private final FunctionReferenceValidator functionReferenceValidator;

    public ReferenceValidator(StateReferenceValidator stateReferenceValidator,
                               FunctionReferenceValidator functionReferenceValidator) {
        this.stateReferenceValidator = stateReferenceValidator;
        this.functionReferenceValidator = functionReferenceValidator;
    }

    /**
     * Validates all cross-references within and between the project and DSL models.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result containing any reference errors
     */
    public ValidationResult validateReferences(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        try {
            // First validate internal references within each model
            result.merge(stateReferenceValidator.validateInternalReferences(projectModel));
            result.merge(functionReferenceValidator.validateInternalReferences(dslModel));

            // Then validate cross-model references
            result.merge(validateCrossModelReferences(projectModel, dslModel));

        } catch (Exception e) {
            logger.error("Error during reference validation", e);
            // Add a generic error to the result
        }

        return result;
    }

    /**
     * Validates references between the project and DSL models.
     * For example, ensuring automation functions reference valid state IDs.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result
     */
    private ValidationResult validateCrossModelReferences(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        // State references in function code
        result.merge(stateReferenceValidator.validateStateReferencesInFunctions(projectModel, dslModel));

        // Button-to-function references
        result.merge(functionReferenceValidator.validateButtonFunctionReferences(projectModel, dslModel));

        return result;
    }
}
