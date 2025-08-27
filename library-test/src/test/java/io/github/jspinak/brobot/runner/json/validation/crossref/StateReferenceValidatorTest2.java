package io.github.jspinak.brobot.runner.json.validation.crossref;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.json.validation.crossref.StateReferenceValidator;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StateReferenceValidatorTest2 {

    private StateReferenceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StateReferenceValidator();
    }

    @Test
    void validateInternalReferences_withNullProject_shouldReturnCriticalError() {
        ValidationResult result = validator.validateInternalReferences(null);

        assertTrue(result.hasCriticalErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("Invalid project model", result.getErrors().get(0).errorCode());
        assertEquals(ValidationSeverity.CRITICAL, result.getErrors().get(0).severity());
    }

    @Test
    void validateInternalReferences_withValidProject_shouldReturnNoErrors() {
        Map<String, Object> project = createValidProject();

        ValidationResult result = validator.validateInternalReferences(project);

        // Debug output to understand what errors are being generated
        if (result.hasErrors()) {
            System.out.println("Unexpected errors found:");
            result.getErrors().forEach(error -> 
                System.out.println("  - " + error.errorCode() + ": " + error.message()));
        }
        if (result.hasWarnings()) {
            System.out.println("Warnings found:");
            result.getWarnings().forEach(warning -> 
                System.out.println("  - " + warning.errorCode() + ": " + warning.message()));
        }

        assertFalse(result.hasErrors());
    }

    @Test
    public void validateInternalReferences_withInvalidStateTransition_shouldReturnError() {
        // Create a minimal project with a state transition that references a non-existent state ID (999)
        Map<String, Object> project = new HashMap<>();

        // Valid State with ID 1
        Map<String, Object> state = new HashMap<>();
        state.put("id", 1);
        state.put("name", "Valid State");
        List<Map<String, Object>> states = new ArrayList<>();
        states.add(state);
        project.put("states", states);

        // Transition with invalid state ID
        Map<String, Object> transition = new HashMap<>();
        transition.put("id", 1);
        transition.put("sourceStateId", 1);
        List<Integer> statesToEnter = new ArrayList<>();
        statesToEnter.add(999); // invalid ID
        transition.put("statesToEnter", statesToEnter);

        List<Map<String, Object>> transitions = new ArrayList<>();
        transitions.add(transition);
        project.put("stateTransitions", transitions);

        // perform validation
        ValidationResult result = validator.validateInternalReferences(project);

        // check if the error is detected
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.message().contains("references non-existent state ID 999")));
    }

    @Test
    void validateInternalReferences_withInvalidStateImageId_shouldReturnError() {
        Map<String, Object> project = createValidProject();

        // reference to a non-existent stateImageId (999)
        List<Map<String, Object>> transitions = (List<Map<String, Object>>) project.get("stateTransitions");
        transitions.get(0).put("stateImageId", 999); // non-existent ID

        ValidationResult result = validator.validateInternalReferences(project);

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid stateImage reference")));
    }

    @Test
    void validateInternalReferences_withInvalidCanHideReference_shouldReturnError() {
        Map<String, Object> project = createValidProject();

        // reference to a non-existent canHide ID (999)
        List<Map<String, Object>> states = (List<Map<String, Object>>) project.get("states");
        states.get(0).put("canHide", List.of(999)); // non-existent ID

        ValidationResult result = validator.validateInternalReferences(project);

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.errorCode().equals("Invalid canHide reference")));
    }

    @Test
    void validateStateReferencesInFunctions_withNullProjectOrDsl_shouldReturnCriticalError() {
        ValidationResult result1 = validator.validateStateReferencesInFunctions(null, createValidDsl());
        ValidationResult result2 = validator.validateStateReferencesInFunctions(createValidProject(), null);

        assertTrue(result1.hasCriticalErrors());
        assertTrue(result2.hasCriticalErrors());
    }

    @Test
    void validateStateReferencesInFunctions_withValidReferences_shouldReturnNoErrors() {
        Map<String, Object> project = createValidProject();
        Map<String, Object> dsl = createValidDsl();

        ValidationResult result = validator.validateStateReferencesInFunctions(project, dsl);

        assertFalse(result.hasErrors());
    }

    @Test
    void validateStateReferencesInFunctions_withInvalidStateId_shouldReturnError() {
        Map<String, Object> project = createValidProject();
        Map<String, Object> dsl = createValidDsl(); // Now creates state.openState(1)

        // Set an invalid state ID (999) in the DSL
        List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");
        List<Map<String, Object>> statements = (List<Map<String, Object>>) functions.get(0).get("statements");
        Map<String, Object> methodCall = statements.getFirst();
        List<Map<String, Object>> args = (List<Map<String, Object>>) methodCall.get("arguments");
        args.getFirst().put("value", 999); // non-existent state ID for openState

        ValidationResult result = validator.validateStateReferencesInFunctions(project, dsl);

        // Debugging output from the test
        if (result.getErrors().stream().noneMatch(e -> e.severity() == ValidationSeverity.ERROR)) { // Assuming hasErrors means at least one error of any type
            System.out.println("Test validateStateReferencesInFunctions_withInvalidStateId_shouldReturnError: No errors found. Result: " + result.getErrors());
        } else {
            System.out.println("Test validateStateReferencesInFunctions_withInvalidStateId_shouldReturnError: Errors found:");
            result.getErrors().forEach(error -> System.out.println("  - Message: " + error.message() + ", Code: " + error.errorCode()));
        }

        // Corrected assertion based on the ValidationResult model
        assertFalse(result.isValid(), "Validation result should not be valid when there are errors.");
        assertTrue(result.getErrors().stream()
                        .anyMatch(e -> e.message().contains("999") &&
                                e.errorCode().equals("Invalid state reference in function")),
                "Should contain an error about referencing non-existent state ID 999.");
    }

    // Helper methods to create valid project and DSL data

    private Map<String, Object> createValidProject() {
        Map<String, Object> project = new HashMap<>();

        // States
        List<Map<String, Object>> states = new ArrayList<>();

        Map<String, Object> state1 = new HashMap<>();
        state1.put("id", 1);
        state1.put("name", "State 1");

        // StateImage for State 1
        List<Map<String, Object>> stateImages1 = new ArrayList<>();
        Map<String, Object> stateImage1 = new HashMap<>();
        stateImage1.put("id", 101);
        stateImage1.put("name", "Image 1");
        stateImages1.add(stateImage1);
        state1.put("stateImages", stateImages1);

        // StateRegion for State 1
        List<Map<String, Object>> stateRegions1 = new ArrayList<>();
        Map<String, Object> stateRegion1 = new HashMap<>();
        stateRegion1.put("id", 201);
        stateRegion1.put("name", "Region 1");
        stateRegion1.put("x", 0);
        stateRegion1.put("y", 0);
        stateRegion1.put("w", 100);
        stateRegion1.put("h", 100);
        stateRegions1.add(stateRegion1);
        state1.put("stateRegions", stateRegions1);

        // StateLocation for State 1
        List<Map<String, Object>> stateLocations1 = new ArrayList<>();
        Map<String, Object> stateLocation1 = new HashMap<>();
        stateLocation1.put("id", 301);
        stateLocation1.put("name", "Location 1");
        stateLocation1.put("location", Map.of("x", 50, "y", 50));
        stateLocations1.add(stateLocation1);
        state1.put("stateLocations", stateLocations1);

        // State 2
        Map<String, Object> state2 = new HashMap<>();
        state2.put("id", 2);
        state2.put("name", "State 2");

        states.add(state1);
        states.add(state2);
        project.put("states", states);

        // State Transitions
        List<Map<String, Object>> stateTransitions = new ArrayList<>();

        Map<String, Object> transition1 = new HashMap<>();
        transition1.put("id", 1);
        transition1.put("sourceStateId", 1);
        transition1.put("stateImageId", 101);
        transition1.put("statesToEnter", List.of(2));

        // Action-Definition for Transition
        Map<String, Object> actionDef = new HashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();

        Map<String, Object> step1 = new HashMap<>();
        Map<String, Object> actionOptions = new HashMap<>();
        actionOptions.put("action", "CLICK");

        Map<String, Object> objectCollection = new HashMap<>();
        objectCollection.put("stateImages", List.of(101));
        objectCollection.put("stateRegions", List.of(201));
        objectCollection.put("stateLocations", List.of(301));

        step1.put("actionOptions", actionOptions);
        step1.put("objectCollection", objectCollection);
        steps.add(step1);

        actionDef.put("steps", steps);
        transition1.put("actionDefinition", actionDef);

        stateTransitions.add(transition1);
        project.put("stateTransitions", stateTransitions);

        return project;
    }

    private Map<String, Object> createValidDsl() {
        Map<String, Object> dsl = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();
        Map<String, Object> function1 = new HashMap<>();
        function1.put("name", "testFunction");
        List<Map<String, Object>> statements = new ArrayList<>();

        Map<String, Object> statement1 = new HashMap<>();
        statement1.put("statementType", "methodCall");
        statement1.put("object", "state");
        statement1.put("method", "openState"); // Using a method the validator checks

        List<Map<String, Object>> args = new ArrayList<>();
        Map<String, Object> arg1 = new HashMap<>();
        arg1.put("expressionType", "literal");
        arg1.put("valueType", "integer");
        arg1.put("value", 1); // Valid state ID initially
        args.add(arg1);

        statement1.put("arguments", args);
        statements.add(statement1);
        function1.put("statements", statements);
        functions.add(function1);
        dsl.put("automationFunctions", functions);
        return dsl;
    }
}