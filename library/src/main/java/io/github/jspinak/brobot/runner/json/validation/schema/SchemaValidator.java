package io.github.jspinak.brobot.runner.json.validation.schema;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;

/**
 * Entry point for schema validation operations in Brobot configurations.
 *
 * <p>This class provides a unified interface for validating JSON configurations against their
 * respective schemas. It delegates to specialized validators for project and DSL schemas, making it
 * easy to validate either or both types of configuration files.
 *
 * <h2>Schema Validation Purpose:</h2>
 *
 * <p>Schema validation ensures that configuration files have the correct structure and data types
 * before attempting to parse them into domain objects. This catches syntax errors, missing required
 * fields, and type mismatches early in the loading process.
 *
 * <h2>Validation Types:</h2>
 *
 * <ul>
 *   <li><b>Project Schema</b> - Validates state definitions, transitions, and UI elements
 *   <li><b>DSL Schema</b> - Validates automation function syntax and structure
 *   <li><b>Combined Validation</b> - Validates both schemas in one operation
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * SchemaValidator validator = new SchemaValidator(
 *     projectValidator, dslValidator);
 *
 * // Validate individual schemas
 * ValidationResult projectResult = validator.validateProjectSchema(projectJson);
 * ValidationResult dslResult = validator.validateDSLSchema(dslJson);
 *
 * // Or validate both at once
 * ValidationResult combined = validator.validateAll(projectJson, dslJson);
 *
 * if (combined.hasCriticalErrors()) {
 *     throw new ConfigValidationException(
 *         "Schema validation failed", combined);
 * }
 * }</pre>
 *
 * @see ProjectSchemaValidator for project-specific schema validation
 * @see AutomationDSLValidator for DSL-specific schema validation
 * @see ConfigurationValidator for complete validation including schemas
 * @author jspinak
 */
@Component
public class SchemaValidator {
    private final ProjectSchemaValidator projectValidator;
    private final AutomationDSLValidator dslValidator;

    public SchemaValidator(
            ProjectSchemaValidator projectValidator, AutomationDSLValidator dslValidator) {
        this.projectValidator = projectValidator;
        this.dslValidator = dslValidator;
    }

    /**
     * Validates a project configuration against the project schema.
     *
     * <p>This method delegates to the ProjectSchemaValidator to check that the provided JSON
     * conforms to the expected project configuration structure. It validates required fields, data
     * types, and structural constraints.
     *
     * @param jsonString Project configuration JSON string to validate
     * @return ValidationResult containing any schema violations found
     * @see ProjectSchemaValidator#validate for detailed validation logic
     */
    public ValidationResult validateProjectSchema(String jsonString) {
        return projectValidator.validate(jsonString);
    }

    /**
     * Validates an automation DSL configuration against the DSL schema.
     *
     * <p>This method delegates to the AutomationDSLValidator to verify that automation functions
     * are properly structured with valid syntax for statements, expressions, and function
     * declarations.
     *
     * @param jsonString DSL configuration JSON string to validate
     * @return ValidationResult containing any schema violations found
     * @see AutomationDSLValidator#validate for detailed validation logic
     */
    public ValidationResult validateDSLSchema(String jsonString) {
        return dslValidator.validate(jsonString);
    }

    /**
     * Validates both project and DSL configurations in a single operation.
     *
     * <p>This convenience method validates both configuration files and merges the results. It's
     * useful when loading a complete Brobot configuration where both files must be valid for the
     * system to function.
     *
     * <h3>Validation Order:</h3>
     *
     * <ol>
     *   <li>Project schema validation
     *   <li>DSL schema validation
     *   <li>Results are merged into a single ValidationResult
     * </ol>
     *
     * <p>Note that this only performs schema validation. For complete validation including
     * cross-references and business rules, use ConfigValidator.
     *
     * @param projectJson Project configuration JSON string containing states and transitions
     * @param dslJson DSL configuration JSON string containing automation functions
     * @return Combined ValidationResult containing all schema violations from both configurations
     */
    public ValidationResult validateAll(String projectJson, String dslJson) {
        ValidationResult result = new ValidationResult();
        result.merge(validateProjectSchema(projectJson));
        result.merge(validateDSLSchema(dslJson));
        return result;
    }
}
