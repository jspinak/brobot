package io.github.jspinak.brobot.schemaValidation.schema;

import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import org.springframework.stereotype.Component;

/**
 * Entry point for schema validation operations.
 */
@Component
public class SchemaValidator {
    private final ProjectSchemaValidator projectValidator;
    private final AutomationDSLValidator dslValidator;

    public SchemaValidator(ProjectSchemaValidator projectValidator,
                            AutomationDSLValidator dslValidator) {
        this.projectValidator = projectValidator;
        this.dslValidator = dslValidator;
    }

    /**
     * Validates a project configuration against the project schema.
     *
     * @param jsonString Project configuration JSON string
     * @return Validation result
     */
    public ValidationResult validateProjectSchema(String jsonString) {
        return projectValidator.validate(jsonString);
    }

    /**
     * Validates an automation DSL configuration against the DSL schema.
     *
     * @param jsonString DSL configuration JSON string
     * @return Validation result
     */
    public ValidationResult validateDSLSchema(String jsonString) {
        return dslValidator.validate(jsonString);
    }

    /**
     * Validates both project and DSL configurations.
     *
     * @param projectJson Project configuration JSON string
     * @param dslJson DSL configuration JSON string
     * @return Combined validation result
     */
    public ValidationResult validateAll(String projectJson, String dslJson) {
        ValidationResult result = new ValidationResult();
        result.merge(validateProjectSchema(projectJson));
        result.merge(validateDSLSchema(dslJson));
        return result;
    }
}
