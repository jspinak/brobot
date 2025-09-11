package io.github.jspinak.brobot.runner.json.validation.schema;

import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

/**
 * Validates project configuration JSON against the Brobot project schema.
 *
 * <p>This validator ensures that project configuration files conform to the expected schema
 * structure before attempting to parse them into domain objects. It uses the Everit JSON Schema
 * library for schema validation and adds additional semantic checks specific to Brobot project
 * configurations.
 *
 * <h2>Schema Location:</h2>
 *
 * <p>The project schema is loaded from the classpath at <code>/schemas/project-schema.json</code>.
 * This schema defines the structure for states, transitions, images, regions, and other project
 * configuration elements.
 *
 * <h2>Validation Layers:</h2>
 *
 * <ol>
 *   <li><b>JSON Parsing</b> - Ensures the input is valid JSON
 *   <li><b>Schema Validation</b> - Checks conformance to the JSON schema
 *   <li><b>Semantic Validation</b> - Additional Brobot-specific rules
 * </ol>
 *
 * <h2>Semantic Validations:</h2>
 *
 * <ul>
 *   <li>State reference integrity in transitions
 *   <li>Transition consistency checks
 *   <li>Logical constraints not expressible in JSON Schema
 * </ul>
 *
 * <h2>Error Handling:</h2>
 *
 * <p>Validation errors are categorized by severity:
 *
 * <ul>
 *   <li>CRITICAL - JSON parsing failures or schema loading errors
 *   <li>ERROR - Schema violations or invalid references
 *   <li>WARNING - Best practice violations or suspicious patterns
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * ProjectSchemaValidator validator = new ProjectSchemaValidator();
 *
 * String projectJson = Files.readString(Path.of("project.json"));
 * ValidationResult result = validator.validate(projectJson);
 *
 * if (result.hasCriticalErrors()) {
 *     // Cannot proceed - JSON is malformed or schema is violated
 *     throw new ConfigValidationException(result);
 * } else if (result.hasErrors()) {
 *     // Fix reference errors before loading
 *     result.getErrors().forEach(System.err::println);
 * }
 * }</pre>
 *
 * @see SchemaValidator for the parent validation coordinator
 * @see AutomationDSLValidator for DSL schema validation
 * @author jspinak
 */
@Component
public class ProjectSchemaValidator {
    private static final Logger logger = LoggerFactory.getLogger(ProjectSchemaValidator.class);
    private static final String SCHEMA_PATH = "/schemas/project-schema.json";

    private final Schema schema;

    /**
     * Initializes the validator by loading the project schema from classpath.
     *
     * <p>This constructor loads the JSON schema file and prepares it for validation. If the schema
     * cannot be loaded, the validator will fail fast with an IllegalStateException, preventing
     * invalid validators from being created.
     *
     * @throws IllegalStateException if the schema file cannot be found or loaded
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
     * Validates the provided JSON string against the project schema.
     *
     * <p>This method performs comprehensive validation including JSON parsing, schema conformance
     * checking, and semantic validation specific to Brobot project configurations. All errors are
     * collected in a single pass for efficient error reporting.
     *
     * <h3>Validation Steps:</h3>
     *
     * <ol>
     *   <li><b>JSON Parsing</b> - Attempts to parse the string as valid JSON
     *   <li><b>Schema Validation</b> - Checks against the project JSON schema
     *   <li><b>State Reference Validation</b> - Ensures transition state IDs exist
     *   <li><b>Transition Consistency</b> - Validates transition logic
     * </ol>
     *
     * <h3>Early Exit Conditions:</h3>
     *
     * <p>Validation stops early if:
     *
     * <ul>
     *   <li>JSON parsing fails (returns CRITICAL error)
     *   <li>Schema validation fails (returns schema errors)
     * </ul>
     *
     * @param jsonString The project configuration JSON string to validate
     * @return ValidationResult containing all discovered validation errors, warnings, and
     *     informational messages
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
                                ValidationSeverity.CRITICAL));
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
                            ValidationSeverity.CRITICAL));
        }

        return result;
    }

    /** Processes validation exceptions and adds them to the result. */
    private void processValidationException(ValidationException e, ValidationResult result) {
        // Add the main error
        result.addError(
                new ValidationError(
                        "Schema validation failed", e.getMessage(), ValidationSeverity.CRITICAL));

        // Add nested validation errors
        if (e.getCausingExceptions() != null && !e.getCausingExceptions().isEmpty()) {
            e.getCausingExceptions()
                    .forEach(
                            cause -> {
                                result.addError(
                                        new ValidationError(
                                                "Schema validation error",
                                                cause.getMessage(),
                                                ValidationSeverity.ERROR));
                            });
        }
    }

    /**
     * Validates referential integrity of state IDs in transitions.
     *
     * <p>This semantic validation ensures that all state IDs referenced in transitions
     * (sourceStateId, statesToEnter, statesToExit) correspond to actual states defined in the
     * configuration. This check cannot be expressed in JSON Schema but is crucial for preventing
     * runtime errors.
     *
     * <h3>Reference Types Checked:</h3>
     *
     * <ul>
     *   <li><b>sourceStateId</b> - The state that must be active for transition
     *   <li><b>statesToEnter</b> - States to activate after transition
     *   <li><b>statesToExit</b> - States to deactivate after transition
     * </ul>
     *
     * @param jsonObject The parsed project configuration
     * @param result ValidationResult to accumulate reference errors
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
                                        String.format(
                                                "Transition #%d references non-existent"
                                                        + " sourceStateId: %d",
                                                transition.optInt("id", i), sourceId),
                                        ValidationSeverity.ERROR));
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
                                            String.format(
                                                    "Transition #%d references non-existent state"
                                                            + " ID %d in statesToEnter",
                                                    transition.optInt("id", i), stateId),
                                            ValidationSeverity.ERROR));
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
                                            String.format(
                                                    "Transition #%d references non-existent state"
                                                            + " ID %d in statesToExit",
                                                    transition.optInt("id", i), stateId),
                                            ValidationSeverity.ERROR));
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
                            ValidationSeverity.ERROR));
        }
    }

    /**
     * Validates logical consistency of transition configurations.
     *
     * <p>This method checks for transition patterns that, while schema-valid, represent logical
     * issues or best practice violations. These semantic checks help ensure transitions will behave
     * as expected at runtime.
     *
     * <h3>Consistency Checks:</h3>
     *
     * <ul>
     *   <li><b>Empty Action Steps</b> - Warns if a transition has no actions
     *   <li><b>No State Changes</b> - Warns if transition doesn't enter/exit states
     *   <li><b>Future:</b> Could check for duplicate transitions, circular dependencies
     * </ul>
     *
     * @param jsonObject The parsed project configuration
     * @param result ValidationResult to accumulate consistency warnings
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
                                            String.format(
                                                    "Transition #%d has an action definition with"
                                                            + " no steps",
                                                    transition.optInt("id", i)),
                                            ValidationSeverity.WARNING));
                        }
                    }
                }

                // Check that transitions identify at least one state to enter or exit
                if ((!transition.has("statesToEnter")
                                || transition.getJSONArray("statesToEnter").length() == 0)
                        && (!transition.has("statesToExit")
                                || transition.getJSONArray("statesToExit").length() == 0)) {
                    result.addError(
                            new ValidationError(
                                    "No state changes",
                                    String.format(
                                            "Transition #%d doesn't enter or exit any states",
                                            transition.optInt("id", i)),
                                    ValidationSeverity.WARNING));
                }
            }

        } catch (Exception e) {
            logger.error("Error during transition consistency validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Error validating transition consistency: " + e.getMessage(),
                            ValidationSeverity.ERROR));
        }
    }
}
