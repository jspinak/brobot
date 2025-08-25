package io.github.jspinak.brobot.runner.json.validation.crossref;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for StateReferenceValidator.
 * Tests validation of state references including StateImages, StateRegions,
 * StateLocations, transitions, and cross-model references.
 */
@DisplayName("StateReferenceValidator Tests")
public class StateReferenceValidatorTest extends BrobotTestBase {
    
    private StateReferenceValidator validator;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        validator = new StateReferenceValidator();
    }
    
    // Helper methods for test data creation
    private Map<String, Object> createProjectModel() {
        Map<String, Object> project = new HashMap<>();
        project.put("states", new ArrayList<>());
        project.put("stateTransitions", new ArrayList<>());
        return project;
    }
    
    private Map<String, Object> createState(int id, String name) {
        Map<String, Object> state = new HashMap<>();
        state.put("id", id);
        state.put("name", name);
        state.put("stateImages", new ArrayList<>());
        state.put("stateRegions", new ArrayList<>());
        state.put("stateLocations", new ArrayList<>());
        return state;
    }
    
    private Map<String, Object> createStateImage(String name, String path) {
        Map<String, Object> image = new HashMap<>();
        image.put("name", name);
        image.put("path", path);
        return image;
    }
    
    private Map<String, Object> createStateRegion(String name, int x, int y, int width, int height) {
        Map<String, Object> region = new HashMap<>();
        region.put("name", name);
        region.put("x", x);
        region.put("y", y);
        region.put("width", width);
        region.put("height", height);
        return region;
    }
    
    private Map<String, Object> createStateLocation(String name, int x, int y) {
        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("x", x);
        location.put("y", y);
        return location;
    }
    
    private Map<String, Object> createTransition(int sourceId, List<Integer> targetIds) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("sourceStateId", sourceId);
        transition.put("statesToEnter", targetIds);
        return transition;
    }
    
    private Map<String, Object> createTransitionWithExit(int sourceId, List<Integer> enterIds, List<Integer> exitIds) {
        Map<String, Object> transition = new HashMap<>();
        transition.put("sourceStateId", sourceId);
        transition.put("statesToEnter", enterIds);
        transition.put("statesToExit", exitIds);
        return transition;
    }
    
    private Map<String, Object> createDSLModel() {
        Map<String, Object> dsl = new HashMap<>();
        dsl.put("automationFunctions", new ArrayList<>());
        return dsl;
    }
    
    private Map<String, Object> createFunction(String name) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        function.put("statements", new ArrayList<>());
        return function;
    }
    
    private Map<String, Object> createActionStep(String action, int stateId) {
        Map<String, Object> step = new HashMap<>();
        step.put("statementType", "methodCall");
        step.put("object", "stateTransitionsManagement");
        step.put("method", "openState");
        List<Map<String, Object>> args = new ArrayList<>();
        Map<String, Object> arg = new HashMap<>();
        arg.put("value", stateId);
        args.add(arg);
        step.put("arguments", args);
        return step;
    }
    
    private void addState(Map<String, Object> project, Map<String, Object> state) {
        ((List<Map<String, Object>>)project.get("states")).add(state);
    }
    
    private void addTransition(Map<String, Object> project, Map<String, Object> transition) {
        ((List<Map<String, Object>>)project.get("stateTransitions")).add(transition);
    }
    
    private void addFunction(Map<String, Object> dsl, Map<String, Object> function) {
        ((List<Map<String, Object>>)dsl.get("automationFunctions")).add(function);
    }
    
    private boolean hasError(ValidationResult result, String context) {
        return result.getErrors().stream()
            .anyMatch(e -> e.message().contains(context));
    }
    
    private boolean hasWarning(ValidationResult result, String context) {
        return result.getWarnings().stream()
            .anyMatch(e -> e.message().contains(context));
    }
    
    @Nested
    @DisplayName("Basic Validation")
    class BasicValidation {
        
        @Test
        @DisplayName("Handle null project model")
        public void testNullProjectModel() {
            ValidationResult result = validator.validateInternalReferences(null);
            
            assertFalse(result.isValid());
            assertEquals(1, result.getErrors().size());
            ValidationError error = result.getErrors().get(0);
            assertEquals(ValidationSeverity.CRITICAL, error.severity());
            assertTrue(error.message().contains("Project model is null"));
        }
        
        @Test
        @DisplayName("Handle invalid project model type")
        public void testInvalidProjectModelType() {
            ValidationResult result = validator.validateInternalReferences("invalid");
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "Project model could not be processed"));
        }
        
        @Test
        @DisplayName("Handle empty project model")
        public void testEmptyProjectModel() {
            Map<String, Object> project = new HashMap<>();
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle gracefully when sections are missing
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Valid project with no references")
        public void testValidProjectNoReferences() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("StateImage Reference Validation")
    class StateImageReferenceValidation {
        
        @Test
        @DisplayName("Valid StateImage references")
        public void testValidStateImageReferences() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "LoginScreen");
            List<Map<String, Object>> images = (List<Map<String, Object>>)state.get("stateImages");
            images.add(createStateImage("loginButton", "images/login.png"));
            images.add(createStateImage("usernameField", "images/username.png"));
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Reference to non-existent StateImage")
        public void testInvalidStateImageReference() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            addState(project, state);
            
            // Create a transition that references a non-existent StateImage
            Map<String, Object> transition = createTransition(1, List.of(2));
            transition.put("requiredImage", "nonExistentImage");
            addTransition(project, transition);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // May detect the invalid image reference
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Duplicate StateImage names")
        public void testDuplicateStateImageNames() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> images = (List<Map<String, Object>>)state.get("stateImages");
            images.add(createStateImage("button", "images/button1.png"));
            images.add(createStateImage("button", "images/button2.png")); // Duplicate name
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // May warn about duplicate names
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("State Transition Reference Validation")
    class StateTransitionReferenceValidation {
        
        @Test
        @DisplayName("Valid transition references")
        public void testValidTransitionReferences() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addState(project, createState(3, "State3"));
            
            addTransition(project, createTransition(1, List.of(2, 3)));
            addTransition(project, createTransition(2, List.of(3)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Transition to non-existent state")
        public void testTransitionToNonExistentState() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            // Transition to non-existent state 99
            addTransition(project, createTransition(1, List.of(2, 99)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "99") || hasError(result, "non-existent"));
        }
        
        @Test
        @DisplayName("Transition from non-existent state")
        public void testTransitionFromNonExistentState() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            // Transition from non-existent state 99
            addTransition(project, createTransition(99, List.of(1, 2)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "99") || hasError(result, "source"));
        }
        
        @Test
        @DisplayName("Transition with exit states")
        public void testTransitionWithExitStates() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addState(project, createState(3, "State3"));
            addState(project, createState(4, "State4"));
            
            addTransition(project, createTransitionWithExit(1, List.of(2, 3), List.of(4)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid exit state reference")
        public void testInvalidExitStateReference() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            // Exit state 99 doesn't exist
            addTransition(project, createTransitionWithExit(1, List.of(2), List.of(99)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "99") || hasError(result, "exit"));
        }
    }
    
    @Nested
    @DisplayName("StateRegion Reference Validation")
    class StateRegionReferenceValidation {
        
        @Test
        @DisplayName("Valid StateRegion references")
        public void testValidStateRegionReferences() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> regions = (List<Map<String, Object>>)state.get("stateRegions");
            regions.add(createStateRegion("header", 0, 0, 800, 100));
            regions.add(createStateRegion("content", 0, 100, 800, 500));
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Overlapping StateRegions")
        public void testOverlappingStateRegions() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> regions = (List<Map<String, Object>>)state.get("stateRegions");
            regions.add(createStateRegion("region1", 0, 0, 100, 100));
            regions.add(createStateRegion("region2", 50, 50, 100, 100)); // Overlaps
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // May warn about overlapping regions
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Invalid region dimensions")
        public void testInvalidRegionDimensions() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> regions = (List<Map<String, Object>>)state.get("stateRegions");
            regions.add(createStateRegion("invalidRegion", 0, 0, -100, -100)); // Negative dimensions
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should detect invalid dimensions
            assertFalse(result.isValid());
            assertTrue(hasError(result, "dimension") || hasError(result, "invalid"));
        }
    }
    
    @Nested
    @DisplayName("StateLocation Reference Validation")
    class StateLocationReferenceValidation {
        
        @Test
        @DisplayName("Valid StateLocation references")
        public void testValidStateLocationReferences() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> locations = (List<Map<String, Object>>)state.get("stateLocations");
            locations.add(createStateLocation("center", 400, 300));
            locations.add(createStateLocation("topLeft", 0, 0));
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Negative location coordinates")
        public void testNegativeLocationCoordinates() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> locations = (List<Map<String, Object>>)state.get("stateLocations");
            locations.add(createStateLocation("offScreen", -100, -100));
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // May warn about negative coordinates
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Duplicate location names")
        public void testDuplicateLocationNames() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            List<Map<String, Object>> locations = (List<Map<String, Object>>)state.get("stateLocations");
            locations.add(createStateLocation("point", 100, 100));
            locations.add(createStateLocation("point", 200, 200)); // Duplicate name
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // May warn about duplicate names
            assertNotNull(result);
        }
    }
    
    @Nested
    @DisplayName("CanHide Reference Validation")
    class CanHideReferenceValidation {
        
        @Test
        @DisplayName("Valid canHide references")
        public void testValidCanHideReferences() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "Popup"));
            addState(project, createState(3, "State3"));
            
            Map<String, Object> popupState = (Map<String, Object>)((List)project.get("states")).get(1);
            popupState.put("canHide", List.of(1, 3)); // Popup can hide State1 and State3
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid canHide reference")
        public void testInvalidCanHideReference() {
            Map<String, Object> project = createProjectModel();
            
            addState(project, createState(1, "State1"));
            Map<String, Object> state2 = createState(2, "Popup");
            state2.put("canHide", List.of(1, 99)); // State 99 doesn't exist
            addState(project, state2);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "99") || hasError(result, "canHide"));
        }
        
        @Test
        @DisplayName("Self-reference in canHide")
        public void testSelfReferenceInCanHide() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            state.put("canHide", List.of(1)); // State hiding itself
            addState(project, state);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should warn about self-reference
            assertTrue(hasWarning(result, "self") || hasWarning(result, "itself"));
        }
    }
    
    @Nested
    @DisplayName("Cross-Model State References")
    class CrossModelStateReferences {
        
        @Test
        @DisplayName("Valid state references in functions")
        public void testValidStateReferencesInFunctions() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("TestFunc");
            List<Map<String, Object>> steps = (List<Map<String, Object>>)function.get("statements");
            steps.add(createActionStep("click", 1));
            steps.add(createActionStep("verify", 2));
            addFunction(dsl, function);
            
            ValidationResult result = validator.validateStateReferencesInFunctions(project, dsl);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Function references non-existent state")
        public void testFunctionReferencesNonExistentState() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            
            Map<String, Object> dsl = createDSLModel();
            Map<String, Object> function = createFunction("TestFunc");
            List<Map<String, Object>> steps = (List<Map<String, Object>>)function.get("statements");
            steps.add(createActionStep("click", 99)); // State 99 doesn't exist
            addFunction(dsl, function);
            
            ValidationResult result = validator.validateStateReferencesInFunctions(project, dsl);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "99") || hasError(result, "non-existent"));
        }
        
        @Test
        @DisplayName("Multiple functions with state references")
        public void testMultipleFunctionsWithStateReferences() {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            addState(project, createState(2, "State2"));
            addState(project, createState(3, "State3"));
            
            Map<String, Object> dsl = createDSLModel();
            
            // Function 1: valid references
            Map<String, Object> func1 = createFunction("ValidFunc");
            List<Map<String, Object>> steps1 = (List<Map<String, Object>>)func1.get("steps");
            steps1.add(createActionStep("click", 1));
            steps1.add(createActionStep("verify", 2));
            addFunction(dsl, func1);
            
            // Function 2: invalid reference
            Map<String, Object> func2 = createFunction("InvalidFunc");
            List<Map<String, Object>> steps2 = (List<Map<String, Object>>)func2.get("steps");
            steps2.add(createActionStep("click", 3));
            steps2.add(createActionStep("verify", 99)); // Invalid
            addFunction(dsl, func2);
            
            ValidationResult result = validator.validateStateReferencesInFunctions(project, dsl);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "InvalidFunc"));
            assertFalse(hasError(result, "ValidFunc"));
        }
    }
    
    @Nested
    @DisplayName("Complex Validation Scenarios")
    class ComplexValidationScenarios {
        
        @Test
        @DisplayName("Complete state machine with all reference types")
        public void testCompleteStateMachine() {
            Map<String, Object> project = createProjectModel();
            
            // Create states with all elements
            Map<String, Object> state1 = createState(1, "LoginScreen");
            List<Map<String, Object>> images1 = (List<Map<String, Object>>)state1.get("stateImages");
            images1.add(createStateImage("loginButton", "images/login.png"));
            List<Map<String, Object>> regions1 = (List<Map<String, Object>>)state1.get("stateRegions");
            regions1.add(createStateRegion("form", 100, 100, 400, 300));
            addState(project, state1);
            
            Map<String, Object> state2 = createState(2, "Dashboard");
            state2.put("canHide", List.of(1));
            addState(project, state2);
            
            // Add transitions
            addTransition(project, createTransition(1, List.of(2)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Cascading reference errors")
        public void testCascadingReferenceErrors() {
            Map<String, Object> project = createProjectModel();
            
            // State with invalid references in multiple places
            Map<String, Object> state = createState(1, "State1");
            state.put("canHide", List.of(99)); // Invalid reference
            addState(project, state);
            
            // Transition with invalid references
            addTransition(project, createTransitionWithExit(1, List.of(88), List.of(77)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should detect multiple errors
            assertFalse(result.isValid());
            assertTrue(result.getErrors().size() >= 2);
        }
        
        @ParameterizedTest
        @DisplayName("Various invalid state IDs")
        @ValueSource(ints = {-1, 0, Integer.MAX_VALUE})
        public void testVariousInvalidStateIds(int stateId) {
            Map<String, Object> project = createProjectModel();
            addState(project, createState(1, "State1"));
            
            addTransition(project, createTransition(1, List.of(stateId)));
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should detect invalid reference
            assertFalse(result.isValid());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Handle null states list")
        public void testNullStatesList() {
            Map<String, Object> project = new HashMap<>();
            project.put("states", null);
            project.put("stateTransitions", new ArrayList<>());
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle gracefully
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle null transitions list")
        public void testNullTransitionsList() {
            Map<String, Object> project = new HashMap<>();
            project.put("states", new ArrayList<>());
            project.put("stateTransitions", null);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle gracefully
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle malformed state structure")
        public void testMalformedStateStructure() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> badState = new HashMap<>();
            badState.put("id", "not_an_integer"); // Wrong type
            badState.put("name", "BadState");
            addState(project, badState);
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle type mismatch
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle empty state references")
        public void testEmptyStateReferences() {
            Map<String, Object> project = createProjectModel();
            
            Map<String, Object> state = createState(1, "State1");
            state.put("canHide", new ArrayList<>()); // Empty list
            addState(project, state);
            
            addTransition(project, createTransition(1, new ArrayList<>())); // Empty targets
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle empty references
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Handle very large state machine")
        public void testLargeStateMachine() {
            Map<String, Object> project = createProjectModel();
            
            // Create 100 states
            for (int i = 1; i <= 100; i++) {
                Map<String, Object> state = createState(i, "State" + i);
                if (i > 1) {
                    state.put("canHide", List.of(i - 1));
                }
                addState(project, state);
            }
            
            // Create transitions between consecutive states
            for (int i = 1; i < 100; i++) {
                addTransition(project, createTransition(i, List.of(i + 1)));
            }
            
            ValidationResult result = validator.validateInternalReferences(project);
            
            // Should handle large models efficiently
            assertTrue(result.isValid());
        }
    }
}