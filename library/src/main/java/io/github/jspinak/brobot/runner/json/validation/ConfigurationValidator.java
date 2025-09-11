package io.github.jspinak.brobot.runner.json.validation;

import java.nio.file.Path;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.business.BusinessRuleValidator;
import io.github.jspinak.brobot.runner.json.validation.crossref.ReferenceValidator;
import io.github.jspinak.brobot.runner.json.validation.exception.ConfigValidationException;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.resource.ImageResourceValidator;
import io.github.jspinak.brobot.runner.json.validation.schema.SchemaValidator;

/**
 * Main validator class for Brobot configurations.
 *
 * <p>This class serves as the primary entry point for validating Brobot configuration files,
 * orchestrating multiple validation strategies to ensure configurations are both syntactically
 * correct and semantically valid. It combines schema validation, cross-reference checking, business
 * rule enforcement, and resource verification into a comprehensive validation pipeline.
 *
 * <h2>Key Features:</h2>
 *
 * <ul>
 *   <li>Facade pattern implementation for coordinating multiple validators
 *   <li>Schema validation for both project and DSL configurations
 *   <li>Cross-reference validation between configuration entities
 *   <li>Business rule enforcement for transitions and functions
 *   <li>Image resource verification to ensure referenced files exist
 *   <li>Flexible validation options for partial or complete validation
 * </ul>
 *
 * <h2>Validation Pipeline:</h2>
 *
 * <ol>
 *   <li>Schema validation - Ensures JSON conforms to defined schemas
 *   <li>Reference validation - Verifies all references point to valid entities
 *   <li>Business rule validation - Enforces application-specific constraints
 *   <li>Resource validation - Confirms external resources (images) are available
 * </ol>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * ConfigurationValidator validator = new ConfigurationValidator(schemaValidator,
 *     referenceValidator, businessRuleValidator, imageResourceValidator);
 *
 * try {
 *     ValidationResult result = validator.validateConfiguration(
 *         projectJson, dslJson, imagePath);
 *
 *     if (result.isValid()) {
 *         // Configuration is valid, proceed with loading
 *     } else {
 *         // Handle validation errors
 *         logger.error("Validation failed: {}", result.getFormattedErrors());
 *     }
 * } catch (ConfigValidationException e) {
 *     // Handle critical validation failures
 *     logger.error("Critical validation error", e);
 * }
 * }</pre>
 *
 * @see SchemaValidator for schema validation details
 * @see ReferenceValidator for cross-reference validation
 * @see BusinessRuleValidator for business rule enforcement
 * @see ImageResourceValidator for resource validation
 * @see ValidationResult for understanding validation outcomes
 * @author jspinak
 */
@Component
public class ConfigurationValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    private final SchemaValidator schemaValidator;
    private final ReferenceValidator referenceValidator;
    private final BusinessRuleValidator businessRuleValidator;
    private final ImageResourceValidator imageResourceValidator;

    public ConfigurationValidator(
            SchemaValidator schemaValidator,
            ReferenceValidator referenceValidator,
            BusinessRuleValidator businessRuleValidator,
            ImageResourceValidator imageResourceValidator) {
        this.schemaValidator = schemaValidator;
        this.referenceValidator = referenceValidator;
        this.businessRuleValidator = businessRuleValidator;
        this.imageResourceValidator = imageResourceValidator;
    }

    /**
     * Validates a complete Brobot configuration including project and DSL files.
     *
     * <p>This method performs comprehensive validation by executing all validation strategies in
     * sequence. The validation process stops early if critical errors are encountered during schema
     * validation, as subsequent validations would be unreliable.
     *
     * <h3>Validation Sequence:</h3>
     *
     * <ol>
     *   <li>Project schema validation - Validates structure of project configuration
     *   <li>DSL schema validation - Validates structure of automation functions
     *   <li>Cross-reference validation - Ensures all references are valid
     *   <li>Business rule validation - Checks application-specific constraints
     *   <li>Resource validation - Verifies image files exist and are accessible
     * </ol>
     *
     * @param projectJson The project configuration JSON string containing states, transitions, and
     *     UI elements
     * @param dslJson The automation DSL JSON string containing function definitions
     * @param imageBasePath Base directory path where referenced image files are located. All
     *     relative image paths in the configuration will be resolved against this base path
     * @return ValidationResult containing all errors, warnings, and info messages discovered during
     *     validation. Check {@link ValidationResult#isValid()} to determine if the configuration
     *     can be used
     * @throws ConfigValidationException if validation encounters critical errors that prevent
     *     further processing, such as malformed JSON or schema violations
     * @throws IllegalArgumentException if any parameter is null
     */
    public ValidationResult validateConfiguration(
            String projectJson, String dslJson, Path imageBasePath)
            throws ConfigValidationException {
        ValidationResult result = new ValidationResult();

        // First validate project schema
        result.merge(schemaValidator.validateProjectSchema(projectJson));
        // If schema validation failed with critical errors, stop here
        if (result.hasCriticalErrors()) {
            throw new ConfigValidationException(
                    "Schema validation failed with critical errors", result);
        }

        // Then validate DSL schema
        result.merge(schemaValidator.validateDSLSchema(dslJson));
        // If schema validation failed with critical errors, stop here
        if (result.hasCriticalErrors()) {
            throw new ConfigValidationException(
                    "Schema validation failed with critical errors", result);
        }

        try {
            // Parse the JSON into our model objects
            JSONObject project = new JSONObject(projectJson);
            JSONObject dsl = new JSONObject(dslJson);

            // Cross-reference validation
            result.merge(referenceValidator.validateReferences(project, dsl));

            // Business rule validation
            result.merge(businessRuleValidator.validateRules(project, dsl));

            // Resource validation
            result.merge(imageResourceValidator.validateImageResources(project, imageBasePath));

            // If any validation steps produced critical errors, throw an exception
            if (result.hasCriticalErrors()) {
                throw new ConfigValidationException(
                        "Validation failed with critical errors", result);
            }

            return result;

        } catch (JSONException e) {
            logger.error("Failed to parse JSON", e);
            result.addError(
                    new ValidationError(
                            "JSON parsing error",
                            "Failed to parse JSON: " + e.getMessage(),
                            ValidationSeverity.CRITICAL));
            throw new ConfigValidationException("Failed to parse JSON configuration", e, result);
        } catch (Exception e) {
            logger.error("Unexpected error during validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Unexpected error during validation: " + e.getMessage(),
                            ValidationSeverity.CRITICAL));
            throw new ConfigValidationException(
                    "Validation failed due to an unexpected error", e, result);
        }
    }

    /**
     * Validates only the project schema without performing full validation.
     *
     * <p>This method provides a quick way to check if a project configuration conforms to the
     * expected schema structure. It's useful for early validation during configuration editing or
     * for validating partial configurations.
     *
     * <h3>When to Use:</h3>
     *
     * <ul>
     *   <li>During interactive configuration editing for immediate feedback
     *   <li>When only the project structure needs validation
     *   <li>For performance-critical scenarios where full validation is too slow
     *   <li>When DSL configuration is not yet available
     * </ul>
     *
     * @param projectJson The project configuration JSON string to validate
     * @return ValidationResult containing any schema validation errors. Note that passing schema
     *     validation does not guarantee the configuration is fully valid - cross-references and
     *     business rules are not checked
     * @see #validateConfiguration for complete validation
     */
    public ValidationResult validateProjectSchemaOnly(String projectJson) {
        return schemaValidator.validateProjectSchema(projectJson);
    }

    /**
     * Validates only the DSL schema without performing full validation.
     *
     * <p>This method provides a quick way to check if automation function definitions conform to
     * the expected DSL schema. It validates the structure of function declarations, parameters, and
     * statements without checking references or business rules.
     *
     * <h3>When to Use:</h3>
     *
     * <ul>
     *   <li>During function authoring for syntax validation
     *   <li>When testing DSL changes independently
     *   <li>For validating function libraries before integration
     *   <li>When project configuration is not yet available
     * </ul>
     *
     * @param dslJson The automation DSL JSON string containing function definitions
     * @return ValidationResult containing any schema validation errors. Note that passing schema
     *     validation does not guarantee functions will execute correctly - variable references and
     *     API calls are not validated
     * @see #validateConfiguration for complete validation
     */
    public ValidationResult validateDslSchemaOnly(String dslJson) {
        return schemaValidator.validateDSLSchema(dslJson);
    }

    /**
     * Validates only the images referenced in a project configuration.
     *
     * <p>This method checks that all image files referenced in the project configuration actually
     * exist and are valid image files. It's useful for verifying resource availability before
     * deployment or after moving configuration files.
     *
     * <h3>Validation Checks:</h3>
     *
     * <ul>
     *   <li>Image file existence at the specified path
     *   <li>File is readable and is actually an image
     *   <li>Image format is supported (PNG, JPG, GIF, BMP)
     *   <li>Image dimensions are reasonable (not 0x0)
     * </ul>
     *
     * <h3>Path Resolution:</h3>
     *
     * <p>Image paths in the configuration can be either absolute or relative. Relative paths are
     * resolved against the provided imageBasePath parameter.
     *
     * @param projectJson The project configuration JSON string containing image references
     * @param imageBasePath Base directory path for resolving relative image paths. Must be an
     *     existing directory
     * @return ValidationResult containing errors for missing or invalid images, and warnings for
     *     suspicious image files (e.g., very small dimensions)
     * @see ImageResourceValidator for detailed validation logic
     */
    public ValidationResult validateImageResourcesOnly(String projectJson, Path imageBasePath) {
        ValidationResult result = new ValidationResult();

        try {
            JSONObject project = new JSONObject(projectJson);
            result.merge(imageResourceValidator.validateImageResources(project, imageBasePath));
            return result;
        } catch (JSONException e) {
            logger.error("Failed to parse JSON", e);
            result.addError(
                    new ValidationError(
                            "JSON parsing error",
                            "Failed to parse JSON: " + e.getMessage(),
                            ValidationSeverity.CRITICAL));
            return result;
        }
    }
}
