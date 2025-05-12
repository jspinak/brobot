package io.github.jspinak.brobot.json.schemaValidation.schema;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Validates project configuration JSON against the project schema.
 */
@Component
public class ProjectSchemaValidator {
    private static final Logger logger = LoggerFactory.getLogger(ProjectSchemaValidator.class);
    private static final String SCHEMA_PATH = "/schemas/project-schema.json";

    private final Schema schema;

    /**
     * Initializes the validator by loading the project schema.
     */
    public ProjectSchemaValidator() {
        try (InputStream inputStream = getClass().getResourceAsStream(SCHEMA_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not find schema at " + SCHEMA_PATH);
            }

            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            this.schema = SchemaLoader.load(rawSchema);
            logger.debug("Successfully loaded project schema");
        } catch (Exception e) {
            logger.error("Failed to load project schema", e);
            throw new IllegalStateException("Failed to load project schema", e);
        }
    }

    /**
     * Validates the provided JSON against the project schema.
     *
     * @param jsonString JSON string to validate
     * @return ValidationResult containing any validation errors
     */
    public ValidationResult validate(String jsonString) {
        ValidationResult result = new ValidationResult();

        try {
            // Parse JSON
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                result.addError(
                        new ValidationError(
                                "Invalid JSON format",
                                "The provided configuration is not valid JSON: " + e.getMessage(),
                                ValidationSeverity.CRITICAL
                        )
                );
                return result;
            }

            // Validate against schema
            try {
                schema.validate(jsonObject);
            } catch (ValidationException e) {
                processValidationException(e, result);
                return result;
            }

            // Perform additional structural validations
            validateStateReferences(jsonObject, result);
            validateTransitionConsistency(jsonObject, result);

        } catch (Exception e) {
            logger.error("Unexpected error during project schema validation", e);
            result.addError(
                    new ValidationError(
                            "Validation failure",
                            "An unexpected error occurred during validation: " + e.getMessage(),
                            ValidationSeverity.CRITICAL
                    )
            );
        }

        return result;
    }

    /**
     * Processes validation exceptions and adds them to the result.
     */
    private void processValidationException(ValidationException e, ValidationResult result) {
        // Add the main error
        result.addError(
                new ValidationError(
                        "Schema validation failed",
                        e.getMessage(),
                        ValidationSeverity.CRITICAL
                )
        );

        // Add nested validation errors
        if (e.getCausingExceptions() != null && !e.getCausingExceptions().isEmpty()) {
            e.getCausingExceptions().forEach(cause -> {
                result.addError(
                        new ValidationError(
                                "Schema validation error",
                                cause.getMessage(),
                                ValidationSeverity.ERROR
                        )
                );
            });
        }
    }

    /**
     * Validates that all state references in transitions actually exist in the states list.
     */
    private void validateStateReferences(JSONObject jsonObject, ValidationResult result) {
        // Skip if states or stateTransitions aren't present
        if (!jsonObject.has("states") || !jsonObject.has("stateTransitions")) {
            return;
        }

        try {
            // Collect all state IDs
            var states = jsonObject.getJSONArray("states");
            var stateIds = new java.util.HashSet<Integer>();

            for (int i = 0; i < states.length(); i++) {
                var state = states.getJSONObject(i);
                if (state.has("id")) {
                    stateIds.add(state.getInt("id"));
                }
            }

            // Check that all referenced state IDs exist
            var transitions = jsonObject.getJSONArray("stateTransitions");
            for (int i = 0; i < transitions.length(); i++) {
                var transition = transitions.getJSONObject(i);

                // Check sourceStateId reference
                if (transition.has("sourceStateId")) {
                    int sourceId = transition.getInt("sourceStateId");
                    if (!stateIds.contains(sourceId)) {
                        result.addError(
                                new ValidationError(
                                        "Invalid state reference",
                                        String.format("Transition #%d references non-existent sourceStateId: %d",
                                                transition.optInt("id", i), sourceId),
                                        ValidationSeverity.ERROR
                                )
                        );
                    }
                }

                // Check statesToEnter references
                if (transition.has("statesToEnter")) {
                    var statesToEnter = transition.getJSONArray("statesToEnter");
                    for (int j = 0; j < statesToEnter.length(); j++) {
                        int stateId = statesToEnter.getInt(j);
                        if (!stateIds.contains(stateId)) {
                            result.addError(
                                    new ValidationError(
                                            "Invalid state reference",
                                            String.format("Transition #%d references non-existent state ID %d in statesToEnter",
                                                    transition.optInt("id", i), stateId),
                                            ValidationSeverity.ERROR
                                    )
                            );
                        }
                    }
                }

                // Check statesToExit references
                if (transition.has("statesToExit")) {
                    var statesToExit = transition.getJSONArray("statesToExit");
                    for (int j = 0; j < statesToExit.length(); j++) {
                        int stateId = statesToExit.getInt(j);
                        if (!stateIds.contains(stateId)) {
                            result.addError(
                                    new ValidationError(
                                            "Invalid state reference",
                                            String.format("Transition #%d references non-existent state ID %d in statesToExit",
                                                    transition.optInt("id", i), stateId),
                                            ValidationSeverity.ERROR
                                    )
                            );
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error during state reference validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Error validating state references: " + e.getMessage(),
                            ValidationSeverity.ERROR
                    )
            );
        }
    }

    /**
     * Validates that transitions have consistent configurations.
     */
    private void validateTransitionConsistency(JSONObject jsonObject, ValidationResult result) {
        // Skip if stateTransitions isn't present
        if (!jsonObject.has("stateTransitions")) {
            return;
        }

        try {
            var transitions = jsonObject.getJSONArray("stateTransitions");

            for (int i = 0; i < transitions.length(); i++) {
                var transition = transitions.getJSONObject(i);

                // Check that each transition has at least one action step
                if (transition.has("actionDefinition")) {
                    var actionDef = transition.getJSONObject("actionDefinition");
                    if (actionDef.has("steps")) {
                        var steps = actionDef.getJSONArray("steps");
                        if (steps.length() == 0) {
                            result.addError(
                                    new ValidationError(
                                            "Empty action steps",
                                            String.format("Transition #%d has an action definition with no steps",
                                                    transition.optInt("id", i)),
                                            ValidationSeverity.WARNING
                                    )
                            );
                        }
                    }
                }

                // Check that transitions identify at least one state to enter or exit
                if ((!transition.has("statesToEnter") || transition.getJSONArray("statesToEnter").length() == 0) &&
                        (!transition.has("statesToExit") || transition.getJSONArray("statesToExit").length() == 0)) {
                    result.addError(
                            new ValidationError(
                                    "No state changes",
                                    String.format("Transition #%d doesn't enter or exit any states",
                                            transition.optInt("id", i)),
                                    ValidationSeverity.WARNING
                            )
                    );
                }
            }

        } catch (Exception e) {
            logger.error("Error during transition consistency validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Error validating transition consistency: " + e.getMessage(),
                            ValidationSeverity.ERROR
                    )
            );
        }
    }
}
