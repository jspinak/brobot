package io.github.jspinak.brobot.schemaValidation;

import io.github.jspinak.brobot.schemaValidation.business.BusinessRuleValidator;
import io.github.jspinak.brobot.schemaValidation.crossref.ReferenceValidator;
import io.github.jspinak.brobot.schemaValidation.exception.ConfigValidationException;
import io.github.jspinak.brobot.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.schemaValidation.model.ValidationSeverity;
import io.github.jspinak.brobot.schemaValidation.resource.ImageResourceValidator;
import io.github.jspinak.brobot.schemaValidation.schema.SchemaValidator;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Main validator class for Brobot configurations.
 * This serves as a facade to different validation strategies.
 */
@Component
public class ConfigValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigValidator.class);

    private final SchemaValidator schemaValidator;
    private final ReferenceValidator referenceValidator;
    private final BusinessRuleValidator businessRuleValidator;
    private final ImageResourceValidator imageResourceValidator;

    public ConfigValidator(SchemaValidator schemaValidator,
                            ReferenceValidator referenceValidator,
                            BusinessRuleValidator businessRuleValidator,
                            ImageResourceValidator imageResourceValidator) {
        this.schemaValidator = schemaValidator;
        this.referenceValidator = referenceValidator;
        this.businessRuleValidator = businessRuleValidator;
        this.imageResourceValidator = imageResourceValidator;
    }

    /**
     * Validates a project configuration file.
     * @param projectJson The project configuration JSON
     * @param dslJson The automation DSL JSON
     * @param imageBasePath Base path to look for referenced images
     * @return Validation result with any errors found
     * @throws ConfigValidationException if validation fails with critical errors
     */
    public ValidationResult validateConfiguration(String projectJson, String dslJson, Path imageBasePath)
            throws ConfigValidationException {
        ValidationResult result = new ValidationResult();

        // First validate project schema
        result.merge(schemaValidator.validateProjectSchema(projectJson));
        // If schema validation failed with critical errors, stop here
        if (result.hasCriticalErrors()) {
            throw new ConfigValidationException("Schema validation failed with critical errors", result);
        }

        // Then validate DSL schema
        result.merge(schemaValidator.validateDSLSchema(dslJson));
        // If schema validation failed with critical errors, stop here
        if (result.hasCriticalErrors()) {
            throw new ConfigValidationException("Schema validation failed with critical errors", result);
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
                throw new ConfigValidationException("Validation failed with critical errors", result);
            }

            return result;

        } catch (JSONException e) {
            logger.error("Failed to parse JSON", e);
            result.addError(new ValidationError(
                    "JSON parsing error",
                    "Failed to parse JSON: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
            throw new ConfigValidationException("Failed to parse JSON configuration", e, result);
        } catch (Exception e) {
            logger.error("Unexpected error during validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Unexpected error during validation: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
            throw new ConfigValidationException("Validation failed due to an unexpected error", e, result);
        }
    }

    /**
     * Validates only the project schema without performing full validation.
     * Useful for quick schema checks.
     *
     * @param projectJson The project configuration JSON
     * @return Validation result with any schema errors found
     */
    public ValidationResult validateProjectSchemaOnly(String projectJson) {
        return schemaValidator.validateProjectSchema(projectJson);
    }

    /**
     * Validates only the DSL schema without performing full validation.
     * Useful for quick schema checks.
     *
     * @param dslJson The automation DSL JSON
     * @return Validation result with any schema errors found
     */
    public ValidationResult validateDslSchemaOnly(String dslJson) {
        return schemaValidator.validateDSLSchema(dslJson);
    }

    /**
     * Validates only the images referenced in a project configuration.
     * Useful for verifying resource availability.
     *
     * @param projectJson The project configuration JSON
     * @param imageBasePath Base path to look for referenced images
     * @return Validation result with any image resource errors found
     */
    public ValidationResult validateImageResourcesOnly(String projectJson, Path imageBasePath) {
        ValidationResult result = new ValidationResult();

        try {
            JSONObject project = new JSONObject(projectJson);
            result.merge(imageResourceValidator.validateImageResources(project, imageBasePath));
            return result;
        } catch (JSONException e) {
            logger.error("Failed to parse JSON", e);
            result.addError(new ValidationError(
                    "JSON parsing error",
                    "Failed to parse JSON: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
            return result;
        }
    }
}