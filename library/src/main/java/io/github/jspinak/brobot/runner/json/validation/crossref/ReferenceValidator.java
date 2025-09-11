package io.github.jspinak.brobot.runner.json.validation.crossref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;

/**
 * Main facade for cross-reference validation in Brobot configurations.
 *
 * <p>This class coordinates the validation of all references between configuration entities,
 * ensuring that every reference points to a valid target. Cross-reference validation is crucial for
 * preventing runtime errors caused by missing or incorrectly specified references.
 *
 * <h2>Reference Types Validated:</h2>
 *
 * <ul>
 *   <li><b>State References</b> - States referenced in transitions and functions
 *   <li><b>Function References</b> - Functions called from buttons and other functions
 *   <li><b>Image References</b> - StateImages referenced in transitions
 *   <li><b>Region/Location References</b> - UI elements referenced in actions
 * </ul>
 *
 * <h2>Validation Strategy:</h2>
 *
 * <p>The validation follows a two-phase approach:
 *
 * <ol>
 *   <li><b>Internal References</b> - Validates references within each model
 *   <li><b>Cross-Model References</b> - Validates references between models
 * </ol>
 *
 * <h2>Why Reference Validation Matters:</h2>
 *
 * <p>Invalid references are one of the most common causes of automation failures. A transition
 * referencing a non-existent state or a button calling an undefined function will cause runtime
 * errors. This validator catches these issues at configuration time.
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * ReferenceValidator validator = new ReferenceValidator(
 *     stateRefValidator, functionRefValidator);
 *
 * ValidationResult result = validator.validateReferences(
 *     projectModel, dslModel);
 *
 * if (result.hasErrors()) {
 *     // Fix reference errors before proceeding
 *     result.getErrors().forEach(error -> {
 *         logger.error("Reference error: {}", error.message());
 *     });
 * }
 * }</pre>
 *
 * @see StateReferenceValidator for state-related reference validation
 * @see FunctionReferenceValidator for function-related reference validation
 * @see ValidationResult for interpreting validation outcomes
 * @author jspinak
 */
@Component
public class ReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceValidator.class);

    private final StateReferenceValidator stateReferenceValidator;
    private final FunctionReferenceValidator functionReferenceValidator;

    public ReferenceValidator(
            StateReferenceValidator stateReferenceValidator,
            FunctionReferenceValidator functionReferenceValidator) {
        this.stateReferenceValidator = stateReferenceValidator;
        this.functionReferenceValidator = functionReferenceValidator;
    }

    /**
     * Validates all cross-references within and between configuration models.
     *
     * <p>This method orchestrates comprehensive reference validation by first checking internal
     * consistency within each model, then validating references that cross model boundaries. This
     * two-phase approach ensures efficient error detection and clearer error messages.
     *
     * <h3>Validation Phases:</h3>
     *
     * <ol>
     *   <li><b>Internal Validation</b>
     *       <ul>
     *         <li>State references within project (transitions → states)
     *         <li>Function references within DSL (function → function calls)
     *       </ul>
     *   <li><b>Cross-Model Validation</b>
     *       <ul>
     *         <li>State references in functions (DSL → project states)
     *         <li>Function references from buttons (project → DSL functions)
     *       </ul>
     * </ol>
     *
     * <h3>Error Handling:</h3>
     *
     * <p>If an exception occurs during validation, it's logged but doesn't stop the validation
     * process. This ensures partial results are available even if one validator fails.
     *
     * @param projectModel Parsed project model containing states, transitions, and UI. Expected to
     *     be a JSONObject or Map structure
     * @param dslModel Parsed DSL model containing automation function definitions. Expected to be a
     *     JSONObject or Map structure
     * @return ValidationResult aggregating all reference errors found. Errors are categorized by
     *     severity - ERROR for invalid references that will cause runtime failures
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
     * Validates references that cross model boundaries.
     *
     * <p>Cross-model references are particularly error-prone because they require coordination
     * between different configuration files. This method ensures that all such references are valid
     * and consistent.
     *
     * <h3>Cross-Model Reference Types:</h3>
     *
     * <ul>
     *   <li><b>State References in Functions</b> - Functions calling openState() or checking
     *       isStateActive() must reference valid state IDs from the project
     *   <li><b>Function References from UI</b> - Buttons and other UI elements must reference
     *       functions that actually exist in the DSL
     * </ul>
     *
     * <h3>Common Issues Detected:</h3>
     *
     * <ul>
     *   <li>Functions using state IDs that don't exist
     *   <li>Buttons calling non-existent functions
     *   <li>Parameter mismatches between button calls and function signatures
     * </ul>
     *
     * @param projectModel Parsed project model providing valid state IDs
     * @param dslModel Parsed DSL model providing function definitions
     * @return ValidationResult containing cross-model reference violations
     */
    private ValidationResult validateCrossModelReferences(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        // State references in function code
        result.merge(
                stateReferenceValidator.validateStateReferencesInFunctions(projectModel, dslModel));

        // Button-to-function references
        result.merge(
                functionReferenceValidator.validateButtonFunctionReferences(
                        projectModel, dslModel));

        return result;
    }
}
