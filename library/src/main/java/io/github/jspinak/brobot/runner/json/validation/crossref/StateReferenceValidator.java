package io.github.jspinak.brobot.runner.json.validation.crossref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

/**
 * Validates all references related to states in Brobot configurations.
 * 
 * <p>This validator ensures that all state-related references are valid and consistent
 * throughout the configuration. States are central to Brobot's automation model, and
 * invalid state references are a common source of runtime failures. This validator
 * catches these issues early in the configuration loading process.</p>
 * 
 * <h2>State Reference Types:</h2>
 * <ul>
 *   <li><b>State IDs</b> - Integer identifiers used in transitions and functions</li>
 *   <li><b>StateImages</b> - Visual elements that identify states on screen</li>
 *   <li><b>StateRegions</b> - Defined areas within states for targeted actions</li>
 *   <li><b>StateLocations</b> - Specific coordinates within states</li>
 *   <li><b>CanHide References</b> - States that can obscure other states</li>
 * </ul>
 * 
 * <h2>Validation Scope:</h2>
 * <p>This validator performs both internal validation (within the project model)
 * and cross-model validation (state references from DSL functions). It ensures
 * that every state reference points to an actually defined state or state element.</p>
 * 
 * <h2>Common Issues Detected:</h2>
 * <ul>
 *   <li>Transitions referencing non-existent states</li>
 *   <li>Functions using invalid state IDs</li>
 *   <li>Actions targeting missing StateImages or Regions</li>
 *   <li>CanHide lists containing invalid state references</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * StateReferenceValidator validator = new StateReferenceValidator();
 * 
 * // Validate internal references
 * ValidationResult internalResult = validator.validateInternalReferences(project);
 * 
 * // Validate cross-model references
 * ValidationResult crossResult = validator.validateStateReferencesInFunctions(
 *     project, dsl);
 * 
 * // Handle validation results
 * if (internalResult.hasErrors() || crossResult.hasErrors()) {
 *     throw new ConfigValidationException("Invalid state references");
 * }
 * }</pre>
 * 
 * @see ReferenceValidator for the parent validation coordinator
 * @see FunctionReferenceValidator for function-related references
 * @see ValidationResult for interpreting validation outcomes
 * @author jspinak
 */
@Component
public class StateReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(StateReferenceValidator.class);

    /**
     * Validates all internal state references within the project model.
     * 
     * <p>This method performs comprehensive validation of state-related references
     * that exist entirely within the project configuration. It ensures that all
     * state elements referenced in various contexts actually exist and are properly
     * defined.</p>
     * 
     * <h3>Validation Checks Performed:</h3>
     * <ol>
     *   <li><b>StateImage References</b> - Validates images referenced in transitions</li>
     *   <li><b>Transition State References</b> - Checks sourceStateId, statesToEnter, statesToExit</li>
     *   <li><b>StateRegion References</b> - Validates regions used in action steps</li>
     *   <li><b>StateLocation References</b> - Checks location references in actions</li>
     *   <li><b>CanHide References</b> - Ensures canHide lists contain valid state IDs</li>
     * </ol>
     * 
     * <h3>Error Severity:</h3>
     * <ul>
     *   <li>CRITICAL - Null or invalid model structure</li>
     *   <li>ERROR - Invalid references that will cause runtime failures</li>
     * </ul>
     * 
     * @param projectModel Parsed project model containing state definitions.
     *                     Expected to be a Map with "states" and "stateTransitions" arrays
     * @return ValidationResult containing all discovered reference errors
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
     * Validates state references used in automation functions.
     * 
     * <p>This method checks that all state IDs referenced in DSL functions
     * correspond to actual states defined in the project model. This cross-model
     * validation is crucial because functions and states are defined separately
     * but must work together at runtime.</p>
     * 
     * <h3>State References in Functions:</h3>
     * <ul>
     *   <li><b>openState(stateId)</b> - Opens/activates a specific state</li>
     *   <li><b>isStateActive(stateId)</b> - Checks if a state is currently active</li>
     *   <li>Direct state ID usage in conditions and expressions</li>
     * </ul>
     * 
     * <h3>Validation Process:</h3>
     * <ol>
     *   <li>Extract all valid state IDs from the project model</li>
     *   <li>Recursively scan function statements for state references</li>
     *   <li>Validate each reference against the valid state ID set</li>
     *   <li>Report errors with function name and invalid state ID</li>
     * </ol>
     * 
     * @param projectModel Project model providing the authoritative list of valid states
     * @param dslModel DSL model containing functions that may reference states
     * @return ValidationResult containing errors for each invalid state reference found
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
                
                // Use the improved validation that handles multiple functions
                validateMultipleFunctionReferences(functions, validStateIds, result);
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
     * Validates StateImage references throughout the project configuration.
     * 
     * <p>StateImages are visual patterns used to identify states on screen. This
     * method ensures that all StateImage IDs referenced in transitions and actions
     * correspond to actually defined StateImages within their parent states.</p>
     * 
     * <h3>StateImage Reference Contexts:</h3>
     * <ul>
     *   <li><b>Transitions</b> - May specify a stateImageId to click</li>
     *   <li><b>Action Steps</b> - ObjectCollections can reference multiple StateImages</li>
     * </ul>
     * 
     * <h3>Validation Logic:</h3>
     * <ol>
     *   <li>Build a map of all StateImage IDs to their names</li>
     *   <li>Check transition.stateImageId references</li>
     *   <li>Check action step ObjectCollection.stateImages arrays</li>
     *   <li>Report errors with transition ID and invalid image ID</li>
     * </ol>
     * 
     * @param project Project model containing state and transition definitions
     * @param result ValidationResult to accumulate StateImage reference errors
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
     * Validates state ID references in transition definitions.
     * 
     * <p>Transitions define how the automation moves between states. This method
     * ensures that all state IDs referenced in transition definitions correspond
     * to actual states defined in the project. Invalid state references in
     * transitions will cause runtime failures.</p>
     * 
     * <h3>State References in Transitions:</h3>
     * <ul>
     *   <li><b>sourceStateId</b> - The state that must be active for transition to execute</li>
     *   <li><b>statesToEnter</b> - States to activate when transition completes</li>
     *   <li><b>statesToExit</b> - States to deactivate when transition completes</li>
     * </ul>
     * 
     * <h3>Common Issues:</h3>
     * <ul>
     *   <li>Referencing states that were deleted or renamed</li>
     *   <li>Copy-paste errors with state IDs from other projects</li>
     *   <li>Typos in manually edited configuration files</li>
     * </ul>
     * 
     * @param project Project model containing authoritative state definitions
     * @param result ValidationResult to accumulate state reference errors
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
            String stateName = (String) state.getOrDefault("name", "unknown");
            
            if (state.containsKey("stateRegions")) {
                List<Map<String, Object>> regions = (List<Map<String, Object>>) state.get("stateRegions");

                for (Map<String, Object> region : regions) {
                    // Always validate dimensions for all regions
                    String regionName = (String) region.getOrDefault("name", "unnamed");
                    validateRegionDimensions(region, stateName, regionName, result);
                    
                    // Track regions with IDs for reference validation
                    if (region.containsKey("id") && region.containsKey("name")) {
                        regionNames.put(
                                (Integer) region.get("id"),
                                (String) region.get("name")
                        );
                    }
                }
                
                // Check for overlapping regions
                checkOverlappingRegions(regions, stateName, result);
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
     * Validates canHide state references for proper occlusion handling.
     * 
     * <p>The canHide property lists states that can potentially obscure or hide
     * the current state. This is crucial for robust automation that can handle
     * overlapping UI elements like popups, dialogs, or tooltips. This method
     * ensures all states in canHide lists actually exist.</p>
     * 
     * <h3>CanHide Usage:</h3>
     * <p>When Brobot searches for a state, it first checks if any states in the
     * canHide list are present. If found, it attempts to close them before
     * searching for the target state. This prevents false negatives caused by
     * UI occlusion.</p>
     * 
     * <h3>Common CanHide Scenarios:</h3>
     * <ul>
     *   <li>Modal dialogs that block underlying content</li>
     *   <li>Tooltips that appear over buttons</li>
     *   <li>Loading screens that temporarily hide the UI</li>
     *   <li>Error messages that overlay form fields</li>
     * </ul>
     * 
     * @param project Project model containing state definitions with canHide properties
     * @param result ValidationResult to accumulate canHide reference errors
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
                    
                    // Check for self-reference in canHide
                    if (stateId.equals(state.get("id"))) {
                        result.addError(new ValidationError(
                                "Self-reference in canHide",
                                String.format("State '%s' references itself in canHide list", stateName),
                                ValidationSeverity.WARNING
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Validates StateRegion dimensions.
     */
    private void validateRegionDimensions(Map<String, Object> region, String stateName, 
                                         String regionName, ValidationResult result) {
        // Check for both "w"/"h" and "width"/"height" field names
        boolean hasShortNames = region.containsKey("w") && region.containsKey("h");
        boolean hasLongNames = region.containsKey("width") && region.containsKey("height");
        
        if (!hasShortNames && !hasLongNames) {
            result.addError(new ValidationError(
                    "Missing region dimensions",
                    String.format("StateRegion '%s' in state '%s' is missing width or height",
                            regionName, stateName),
                    ValidationSeverity.ERROR
            ));
            return;
        }
        
        try {
            int width = hasShortNames ? 
                ((Number) region.get("w")).intValue() : 
                ((Number) region.get("width")).intValue();
            int height = hasShortNames ? 
                ((Number) region.get("h")).intValue() :
                ((Number) region.get("height")).intValue();
            
            // Check for invalid dimensions
            if (width <= 0 || height <= 0) {
                result.addError(new ValidationError(
                        "Invalid region dimensions",
                        String.format("StateRegion '%s' in state '%s' has invalid dimensions: %dx%d",
                                regionName, stateName, width, height),
                        ValidationSeverity.ERROR
                ));
            }
            
            // Check for unreasonably large dimensions
            if (width > 10000 || height > 10000) {
                result.addError(new ValidationError(
                        "Unreasonable region dimensions",
                        String.format("StateRegion '%s' in state '%s' has unreasonably large dimensions: %dx%d",
                                regionName, stateName, width, height),
                        ValidationSeverity.WARNING
                ));
            }
            
            // Check extreme aspect ratio
            double aspectRatio = (double) width / height;
            if (aspectRatio < 0.01 || aspectRatio > 100) {
                result.addError(new ValidationError(
                        "Extreme aspect ratio",
                        String.format("StateRegion '%s' in state '%s' has extreme aspect ratio: %.2f",
                                regionName, stateName, aspectRatio),
                        ValidationSeverity.WARNING
                ));
            }
        } catch (Exception e) {
            result.addError(new ValidationError(
                    "Invalid region data",
                    String.format("StateRegion '%s' in state '%s' has invalid dimension data",
                            regionName, stateName),
                    ValidationSeverity.ERROR
            ));
        }
    }
    
    /**
     * Validates state references in multiple functions.
     */
    private void validateMultipleFunctionReferences(List<Map<String, Object>> functions, 
                                                   Set<Integer> validStateIds, 
                                                   ValidationResult result) {
        for (Map<String, Object> function : functions) {
            String functionName = (String) function.getOrDefault("name", "unknown");
            
            // Check statements for state references
            if (function.containsKey("statements")) {
                List<Map<String, Object>> statements = 
                    (List<Map<String, Object>>) function.get("statements");
                
                Set<Integer> referencedStates = extractStateReferences(statements);
                
                for (Integer stateId : referencedStates) {
                    if (!validStateIds.contains(stateId)) {
                        result.addError(new ValidationError(
                                "Invalid state reference in function",
                                String.format("Function '%s' references invalid state ID: %d", 
                                            functionName, stateId),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
            }
        }
    }
    
    /**
     * Extracts all state references from function statements.
     */
    private Set<Integer> extractStateReferences(List<Map<String, Object>> statements) {
        Set<Integer> references = new HashSet<>();
        
        for (Map<String, Object> statement : statements) {
            extractStateReferencesFromStatement(statement, references);
        }
        
        return references;
    }
    
    /**
     * Recursively extracts state references from a statement.
     */
    private void extractStateReferencesFromStatement(Map<String, Object> statement, 
                                                    Set<Integer> references) {
        if (statement == null) return;
        
        // Check for direct state references
        if (statement.containsKey("stateId")) {
            try {
                Integer stateId = (Integer) statement.get("stateId");
                if (stateId != null) {
                    references.add(stateId);
                }
            } catch (ClassCastException e) {
                // Invalid type, skip
            }
        }
        
        // Check for state references in method calls
        if (statement.containsKey("statementType") && 
            "methodCall".equals(statement.get("statementType"))) {
            
            // Check if this is a state management method call
            String object = (String) statement.getOrDefault("object", "");
            String method = (String) statement.getOrDefault("method", "");
            
            if ((object.equals("stateTransitionsManagement") || object.equals("state")) &&
                (method.equals("openState") || method.equals("isStateActive"))) {
                
                if (statement.containsKey("arguments")) {
                    List<Object> args = (List<Object>) statement.get("arguments");
                    for (Object arg : args) {
                        if (arg instanceof Integer) {
                            // Direct state ID
                            references.add((Integer) arg);
                        } else if (arg instanceof Map) {
                            // Argument object with value field
                            Map<String, Object> argMap = (Map<String, Object>) arg;
                            if (argMap.containsKey("value")) {
                                Object value = argMap.get("value");
                                if (value instanceof Integer) {
                                    references.add((Integer) value);
                                }
                            }
                            // Also check recursively for nested references
                            extractStateReferencesFromStatement(argMap, references);
                        }
                    }
                }
            }
        }
        
        // Check nested statements (if, forEach, etc.)
        if (statement.containsKey("thenStatements")) {
            List<Map<String, Object>> thenStatements = 
                (List<Map<String, Object>>) statement.get("thenStatements");
            for (Map<String, Object> nested : thenStatements) {
                extractStateReferencesFromStatement(nested, references);
            }
        }
        
        if (statement.containsKey("elseStatements")) {
            List<Map<String, Object>> elseStatements = 
                (List<Map<String, Object>>) statement.get("elseStatements");
            for (Map<String, Object> nested : elseStatements) {
                extractStateReferencesFromStatement(nested, references);
            }
        }
        
        if (statement.containsKey("statements")) {
            List<Map<String, Object>> nestedStatements = 
                (List<Map<String, Object>>) statement.get("statements");
            for (Map<String, Object> nested : nestedStatements) {
                extractStateReferencesFromStatement(nested, references);
            }
        }
    }
    
    /**
     * Checks if state regions overlap inappropriately.
     */
    private void checkOverlappingRegions(List<Map<String, Object>> regions, String stateName, 
                                        ValidationResult result) {
        for (int i = 0; i < regions.size(); i++) {
            for (int j = i + 1; j < regions.size(); j++) {
                Map<String, Object> region1 = regions.get(i);
                Map<String, Object> region2 = regions.get(j);
                
                if (regionsOverlap(region1, region2)) {
                    String name1 = (String) region1.getOrDefault("name", "Region" + i);
                    String name2 = (String) region2.getOrDefault("name", "Region" + j);
                    result.addError(new ValidationError(
                            "Overlapping regions",
                            String.format("In state '%s': %s overlaps with %s", 
                                        stateName, name1, name2),
                            ValidationSeverity.WARNING
                    ));
                }
            }
        }
    }
    
    /**
     * Check if two regions overlap.
     */
    private boolean regionsOverlap(Map<String, Object> r1, Map<String, Object> r2) {
        try {
            int x1 = ((Number) r1.get("x")).intValue();
            int y1 = ((Number) r1.get("y")).intValue();
            int w1 = ((Number) r1.get("w")).intValue();
            int h1 = ((Number) r1.get("h")).intValue();
            
            int x2 = ((Number) r2.get("x")).intValue();
            int y2 = ((Number) r2.get("y")).intValue();
            int w2 = ((Number) r2.get("w")).intValue();
            int h2 = ((Number) r2.get("h")).intValue();
            
            // Check if rectangles overlap
            return !(x1 + w1 <= x2 || x2 + w2 <= x1 || 
                    y1 + h1 <= y2 || y2 + h2 <= y1);
        } catch (Exception e) {
            return false;
        }
    }
}