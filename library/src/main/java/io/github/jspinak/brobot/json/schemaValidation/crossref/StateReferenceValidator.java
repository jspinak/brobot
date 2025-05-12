package io.github.jspinak.brobot.json.schemaValidation.crossref;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates references related to states, including state elements, transitions,
 * and references to states from other components.
 */
@Component
public class StateReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(StateReferenceValidator.class);

    /**
     * Validates internal references within the project model that are related to states.
     * This includes state element references, transition references, etc.
     *
     * @param projectModel Parsed project model
     * @return Validation result
     */
    public ValidationResult validateInternalReferences(Object projectModel) {
        ValidationResult result = new ValidationResult();

        if (projectModel == null) {
            result.addError(new ValidationError(
                    "Invalid project model",
                    "Project model is null",
                    ValidationSeverity.CRITICAL
            ));
            return result;
        }

        // For type safety, we need to cast the model to our actual model class
        // For this example, we'll use a map-based approach for simplicity
        try {
            Map<String, Object> project = (Map<String, Object>) projectModel;

            // Validate state image references
            validateStateImageReferences(project, result);

            // Validate state transition references
            validateStateTransitionReferences(project, result);

            // Validate state region references
            validateStateRegionReferences(project, result);

            // Validate state location references
            validateStateLocationReferences(project, result);

            // Validate state canHide references
            validateCanHideReferences(project, result);

        } catch (ClassCastException e) {
            logger.error("Project model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid project model type",
                    "Project model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during state reference validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating state references: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Validates references to states in automation functions.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result
     */
    public ValidationResult validateStateReferencesInFunctions(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        if (projectModel == null || dslModel == null) {
            if (projectModel == null) {
                result.addError(new ValidationError(
                        "Invalid project model",
                        "Project model is null",
                        ValidationSeverity.CRITICAL
                ));
            }
            if (dslModel == null) {
                result.addError(new ValidationError(
                        "Invalid DSL model",
                        "DSL model is null",
                        ValidationSeverity.CRITICAL
                ));
            }
            return result;
        }

        try {
            Map<String, Object> project = (Map<String, Object>) projectModel;
            Map<String, Object> dsl = (Map<String, Object>) dslModel;

            // Extract valid state IDs from project
            Set<Integer> validStateIds = extractValidStateIds(project);

            // Check state references in function statements
            if (dsl.containsKey("automationFunctions")) {
                List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

                for (Map<String, Object> function : functions) {
                    String functionName = function.containsKey("name")
                            ? (String) function.get("name")
                            : "unknown";

                    if (function.containsKey("statements")) {
                        validateStatementsForStateReferences(
                                (List<Map<String, Object>>) function.get("statements"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }
                }
            }

        } catch (ClassCastException e) {
            logger.error("Model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid model type",
                    "Model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during state reference validation in functions", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating state references in functions: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Extracts all valid state IDs from the project model.
     *
     * @param project Project model as a map
     * @return Set of valid state IDs
     */
    private Set<Integer> extractValidStateIds(Map<String, Object> project) {
        Set<Integer> stateIds = new HashSet<>();

        if (project.containsKey("states")) {
            List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

            for (Map<String, Object> state : states) {
                if (state.containsKey("id")) {
                    stateIds.add((Integer) state.get("id"));
                }
            }
        }

        return stateIds;
    }

    /**
     * Recursively validates statements for references to state IDs.
     *
     * @param statements List of statement objects
     * @param validStateIds Set of valid state IDs
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateStatementsForStateReferences(
            List<Map<String, Object>> statements,
            Set<Integer> validStateIds,
            String functionName,
            ValidationResult result) {

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType")
                    ? (String) statement.get("statementType")
                    : "";

            switch (statementType) {
                case "methodCall":
                    // Check if it's a call to openState or similar
                    String object = statement.containsKey("object")
                            ? (String) statement.get("object")
                            : "";
                    String method = statement.containsKey("method")
                            ? (String) statement.get("method")
                            : "";

                    if ((object.equals("stateTransitionsManagement") || object.equals("state")) &&
                            (method.equals("openState") || method.equals("isStateActive"))) {

                        if (statement.containsKey("arguments")) {
                            List<Map<String, Object>> args = (List<Map<String, Object>>) statement.get("arguments");

                            for (Map<String, Object> arg : args) {
                                validateExpressionForStateIds(arg, validStateIds, functionName, result);
                            }
                        }
                    }
                    break;

                case "if":
                    // Check condition
                    if (statement.containsKey("condition")) {
                        validateExpressionForStateIds(
                                (Map<String, Object>) statement.get("condition"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }

                    // Check then and else branches
                    if (statement.containsKey("thenStatements")) {
                        validateStatementsForStateReferences(
                                (List<Map<String, Object>>) statement.get("thenStatements"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }

                    if (statement.containsKey("elseStatements")) {
                        validateStatementsForStateReferences(
                                (List<Map<String, Object>>) statement.get("elseStatements"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }
                    break;

                case "forEach":
                    // Check collection
                    if (statement.containsKey("collection")) {
                        validateExpressionForStateIds(
                                (Map<String, Object>) statement.get("collection"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }

                    // Check body statements
                    if (statement.containsKey("statements")) {
                        validateStatementsForStateReferences(
                                (List<Map<String, Object>>) statement.get("statements"),
                                validStateIds,
                                functionName,
                                result
                        );
                    }
                    break;
            }
        }
    }

    /**
     * Validates an expression for references to state IDs.
     *
     * @param expression Expression object
     * @param validStateIds Set of valid state IDs
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateExpressionForStateIds(
            Map<String, Object> expression,
            Set<Integer> validStateIds,
            String functionName,
            ValidationResult result) {

        String expressionType = expression.containsKey("expressionType")
                ? (String) expression.get("expressionType")
                : "";

        switch (expressionType) {
            case "literal":
                String valueType = expression.containsKey("valueType")
                        ? (String) expression.get("valueType")
                        : "";

                if (valueType.equals("integer") && expression.containsKey("value")) {
                    Integer stateId = (Integer) expression.get("value");

                    if (!validStateIds.contains(stateId)) {
                        result.addError(new ValidationError(
                                "Invalid state reference",
                                String.format("Function '%s' references non-existent state ID %d",
                                        functionName, stateId),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
                break;

            case "methodCall":
                if (expression.containsKey("arguments")) {
                    List<Map<String, Object>> args = (List<Map<String, Object>>) expression.get("arguments");

                    for (Map<String, Object> arg : args) {
                        validateExpressionForStateIds(arg, validStateIds, functionName, result);
                    }
                }
                break;

            case "binaryOperation":
                if (expression.containsKey("left")) {
                    validateExpressionForStateIds(
                            (Map<String, Object>) expression.get("left"),
                            validStateIds,
                            functionName,
                            result
                    );
                }

                if (expression.containsKey("right")) {
                    validateExpressionForStateIds(
                            (Map<String, Object>) expression.get("right"),
                            validStateIds,
                            functionName,
                            result
                    );
                }
                break;
        }
    }

    /**
     * Validates that all stateImages referenced in states actually exist.
     *
     * @param project Project model as a map
     * @param result Validation result to update
     */
    private void validateStateImageReferences(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        // Extract all stateImage IDs
        Map<Integer, String> stateImageNames = new HashMap<>();
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

        for (Map<String, Object> state : states) {
            if (state.containsKey("stateImages")) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) state.get("stateImages");

                for (Map<String, Object> image : images) {
                    if (image.containsKey("id") && image.containsKey("name")) {
                        stateImageNames.put(
                                (Integer) image.get("id"),
                                (String) image.get("name")
                        );
                    }
                }
            }
        }

        // Check transitions for references to stateImages
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

        for (Map<String, Object> transition : transitions) {
            if (transition.containsKey("stateImageId")) {
                Integer imageId = (Integer) transition.get("stateImageId");

                if (!stateImageNames.containsKey(imageId)) {
                    result.addError(new ValidationError(
                            "Invalid stateImage reference",
                            String.format("Transition #%s references non-existent stateImage ID: %d",
                                    transition.containsKey("id") ? transition.get("id").toString() : "unknown",
                                    imageId),
                            ValidationSeverity.ERROR
                    ));
                }
            }

            // Check action steps for stateImage references
            if (transition.containsKey("actionDefinition")) {
                Map<String, Object> actionDef = (Map<String, Object>) transition.get("actionDefinition");

                if (actionDef.containsKey("steps")) {
                    List<Map<String, Object>> steps = (List<Map<String, Object>>) actionDef.get("steps");

                    for (Map<String, Object> step : steps) {
                        if (step.containsKey("objectCollection")) {
                            Map<String, Object> objColl = (Map<String, Object>) step.get("objectCollection");

                            if (objColl.containsKey("stateImages")) {
                                List<Integer> imageIds = (List<Integer>) objColl.get("stateImages");

                                for (Integer imageId : imageIds) {
                                    if (!stateImageNames.containsKey(imageId)) {
                                        result.addError(new ValidationError(
                                                "Invalid stateImage reference",
                                                String.format("Transition #%s action step references non-existent stateImage ID: %d",
                                                        transition.containsKey("id") ? transition.get("id").toString() : "unknown",
                                                        imageId),
                                                ValidationSeverity.ERROR
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates references in state transitions.
     *
     * @param project Project model as a map
     * @param result Validation result to update
     */
    private void validateStateTransitionReferences(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        // Extract all state IDs
        Set<Integer> stateIds = new HashSet<>();
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

        for (Map<String, Object> state : states) {
            if (state.containsKey("id")) {
                stateIds.add((Integer) state.get("id"));
            }
        }

        // Check state transitions for references to state IDs
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

        for (Map<String, Object> transition : transitions) {
            String transitionId = transition.containsKey("id")
                    ? transition.get("id").toString()
                    : "unknown";

            // Check sourceStateId
            if (transition.containsKey("sourceStateId")) {
                Integer sourceId = (Integer) transition.get("sourceStateId");

                if (!stateIds.contains(sourceId)) {
                    result.addError(new ValidationError(
                            "Invalid state reference",
                            String.format("Transition #%s references non-existent state ID %d in sourceStateId",
                                    transitionId, sourceId),
                            ValidationSeverity.ERROR
                    ));
                }
            }

            // Check statesToEnter
            if (transition.containsKey("statesToEnter")) {
                List<Integer> enterIds = (List<Integer>) transition.get("statesToEnter");

                for (Integer stateId : enterIds) {
                    if (!stateIds.contains(stateId)) {
                        result.addError(new ValidationError(
                                "Invalid state reference",
                                String.format("Transition #%s references non-existent state ID %d in statesToEnter",
                                        transitionId, stateId),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
            }

            // Check statesToExit
            if (transition.containsKey("statesToExit")) {
                List<Integer> exitIds = (List<Integer>) transition.get("statesToExit");

                for (Integer stateId : exitIds) {
                    if (!stateIds.contains(stateId)) {
                        result.addError(new ValidationError(
                                "Invalid state reference",
                                String.format("Transition #%s references non-existent state ID %d in statesToExit",
                                        transitionId, stateId),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
            }
        }
    }

    /**
     * Validates state region references in action steps.
     *
     * @param project Project model as a map
     * @param result Validation result to update
     */
    private void validateStateRegionReferences(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        // Extract all stateRegion IDs
        Map<Integer, String> regionNames = new HashMap<>();
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

        for (Map<String, Object> state : states) {
            if (state.containsKey("stateRegions")) {
                List<Map<String, Object>> regions = (List<Map<String, Object>>) state.get("stateRegions");

                for (Map<String, Object> region : regions) {
                    if (region.containsKey("id") && region.containsKey("name")) {
                        regionNames.put(
                                (Integer) region.get("id"),
                                (String) region.get("name")
                        );
                    }
                }
            }
        }

        // Check action steps for stateRegion references
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

        for (Map<String, Object> transition : transitions) {
            if (transition.containsKey("actionDefinition")) {
                Map<String, Object> actionDef = (Map<String, Object>) transition.get("actionDefinition");

                if (actionDef.containsKey("steps")) {
                    List<Map<String, Object>> steps = (List<Map<String, Object>>) actionDef.get("steps");

                    for (Map<String, Object> step : steps) {
                        if (step.containsKey("objectCollection")) {
                            Map<String, Object> objColl = (Map<String, Object>) step.get("objectCollection");

                            if (objColl.containsKey("stateRegions")) {
                                List<Integer> regionIds = (List<Integer>) objColl.get("stateRegions");

                                for (Integer regionId : regionIds) {
                                    if (!regionNames.containsKey(regionId)) {
                                        result.addError(new ValidationError(
                                                "Invalid stateRegion reference",
                                                String.format("Transition #%s action step references non-existent stateRegion ID: %d",
                                                        transition.containsKey("id") ? transition.get("id").toString() : "unknown",
                                                        regionId),
                                                ValidationSeverity.ERROR
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates state location references in action steps.
     *
     * @param project Project model as a map
     * @param result Validation result to update
     */
    private void validateStateLocationReferences(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states") || !project.containsKey("stateTransitions")) {
            return;
        }

        // Extract all stateLocation IDs
        Map<Integer, String> locationNames = new HashMap<>();
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

        for (Map<String, Object> state : states) {
            if (state.containsKey("stateLocations")) {
                List<Map<String, Object>> locations = (List<Map<String, Object>>) state.get("stateLocations");

                for (Map<String, Object> location : locations) {
                    if (location.containsKey("id") && location.containsKey("name")) {
                        locationNames.put(
                                (Integer) location.get("id"),
                                (String) location.get("name")
                        );
                    }
                }
            }
        }

        // Check action steps for stateLocation references
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");

        for (Map<String, Object> transition : transitions) {
            if (transition.containsKey("actionDefinition")) {
                Map<String, Object> actionDef = (Map<String, Object>) transition.get("actionDefinition");

                if (actionDef.containsKey("steps")) {
                    List<Map<String, Object>> steps = (List<Map<String, Object>>) actionDef.get("steps");

                    for (Map<String, Object> step : steps) {
                        if (step.containsKey("objectCollection")) {
                            Map<String, Object> objColl = (Map<String, Object>) step.get("objectCollection");

                            if (objColl.containsKey("stateLocations")) {
                                List<Integer> locationIds = (List<Integer>) objColl.get("stateLocations");

                                for (Integer locationId : locationIds) {
                                    if (!locationNames.containsKey(locationId)) {
                                        result.addError(new ValidationError(
                                                "Invalid stateLocation reference",
                                                String.format("Transition #%s action step references non-existent stateLocation ID: %d",
                                                        transition.containsKey("id") ? transition.get("id").toString() : "unknown",
                                                        locationId),
                                                ValidationSeverity.ERROR
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Validates state canHide references.
     *
     * @param project Project model as a map
     * @param result Validation result to update
     */
    private void validateCanHideReferences(Map<String, Object> project, ValidationResult result) {
        if (!project.containsKey("states")) {
            return;
        }

        // Extract all state IDs
        Set<Integer> stateIds = new HashSet<>();
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");

        for (Map<String, Object> state : states) {
            if (state.containsKey("id")) {
                stateIds.add((Integer) state.get("id"));
            }
        }

        // Check canHide references
        for (Map<String, Object> state : states) {
            String stateName = state.containsKey("name")
                    ? (String) state.get("name")
                    : "unknown";

            if (state.containsKey("canHide")) {
                List<Integer> canHideIds = (List<Integer>) state.get("canHide");

                for (Integer stateId : canHideIds) {
                    if (!stateIds.contains(stateId)) {
                        result.addError(new ValidationError(
                                "Invalid canHide reference",
                                String.format("State '%s' references non-existent state ID %d in canHide",
                                        stateName, stateId),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
            }
        }
    }
}