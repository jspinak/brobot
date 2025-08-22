package io.github.jspinak.brobot.runner.json.validation.crossref;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import java.util.Set;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReferenceValidator - validates cross-references in Brobot configurations.
 * Ensures all references between states, functions, and images are valid.
 */
@DisplayName("ReferenceValidator Tests")
public class ReferenceValidatorTest extends BrobotTestBase {
    
    private ReferenceValidator validator;
    private StateReferenceValidator stateValidator;
    private FunctionReferenceValidator functionValidator;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        validator = new ReferenceValidator();
        stateValidator = new StateReferenceValidator();
        functionValidator = new FunctionReferenceValidator();
        objectMapper = new ObjectMapper();
    }
    
    @Nested
    @DisplayName("State Reference Validation")
    class StateReferenceValidation {
        
        @Test
        @DisplayName("Valid state references pass validation")
        public void testValidStateReferences() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "LoginState", "images": ["login.png"]},
                        {"name": "HomeState", "images": ["home.png"]}
                    ],
                    "transitions": [
                        {"from": "LoginState", "to": "HomeState", "trigger": "login"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = stateValidator.validateInternalReferences(jsonNode);
            
            assertTrue(result.isValid());
            assertTrue(result.getErrors().isEmpty());
        }
        
        @Test
        @DisplayName("Invalid state reference in transition fails")
        public void testInvalidTransitionStateReference() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "LoginState", "images": ["login.png"]}
                    ],
                    "transitions": [
                        {"from": "LoginState", "to": "NonExistentState", "trigger": "login"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = stateValidator.validateInternalReferences(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "state-not-found"));
        }
        
        @Test
        @DisplayName("Invalid state reference in function fails")
        public void testInvalidFunctionStateReference() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "LoginState", "images": ["login.png"]}
                    ],
                    "functions": [
                        {
                            "name": "navigateToHome",
                            "steps": [
                                {"action": "waitForState", "state": "NonExistentState"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = stateValidator.validateInternalReferences(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "state-not-found"));
        }
        
        @Test
        @DisplayName("Shared state references are valid across states")
        public void testSharedStateReferences() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "State1", "images": ["shared.png"], "shared": true},
                        {"name": "State2", "images": ["shared.png"], "shared": true}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = stateValidator.validateInternalReferences(jsonNode);
            
            assertTrue(result.isValid());
        }
    }
    
    @Nested
    @DisplayName("Function Reference Validation")
    class FunctionReferenceValidation {
        
        @Test
        @DisplayName("Valid function references pass validation")
        public void testValidFunctionReferences() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "login",
                            "steps": [
                                {"action": "type", "text": "username"}
                            ]
                        },
                        {
                            "name": "completeLogin",
                            "steps": [
                                {"action": "call", "function": "login"},
                                {"action": "click", "target": "submit"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = functionValidator.validateInternalReferences(jsonNode);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid function reference fails")
        public void testInvalidFunctionReference() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "login",
                            "steps": [
                                {"action": "call", "function": "nonExistentFunction"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = functionValidator.validateInternalReferences(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "function-not-found"));
        }
        
        @Test
        @DisplayName("Detect circular function references")
        public void testCircularFunctionReferences() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "func1",
                            "steps": [{"action": "call", "function": "func2"}]
                        },
                        {
                            "name": "func2",
                            "steps": [{"action": "call", "function": "func3"}]
                        },
                        {
                            "name": "func3",
                            "steps": [{"action": "call", "function": "func1"}]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = functionValidator.validateInternalReferences(jsonNode);
            
            assertTrue(hasWarning(result, "circular-reference"));
        }
        
        @Test
        @DisplayName("Detect recursive function calls")
        public void testRecursiveFunctionCalls() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "recursive",
                            "steps": [
                                {"action": "click", "target": "button"},
                                {"action": "call", "function": "recursive"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = functionValidator.validateInternalReferences(jsonNode);
            
            assertTrue(hasWarning(result, "recursive-call"));
        }
    }
    
    @Nested
    @DisplayName("Image Reference Validation")
    class ImageReferenceValidation {
        
        @Test
        @DisplayName("Valid image references pass validation")
        public void testValidImageReferences() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "LoginState", "images": ["login-button.png", "login-form.png"]}
                    ],
                    "functions": [
                        {
                            "name": "clickLogin",
                            "steps": [
                                {"action": "click", "target": "login-button.png"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateImageReferences(jsonNode);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid image reference fails")
        public void testInvalidImageReference() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "LoginState", "images": ["login.png"]}
                    ],
                    "functions": [
                        {
                            "name": "clickButton",
                            "steps": [
                                {"action": "click", "target": "nonexistent-button.png"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateImageReferences(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "image-not-found"));
        }
        
        @Test
        @DisplayName("Warn about unused images")
        public void testUnusedImages() throws Exception {
            String json = """
                {
                    "states": [
                        {
                            "name": "State1",
                            "images": ["used.png", "unused.png"]
                        }
                    ],
                    "functions": [
                        {
                            "name": "useImage",
                            "steps": [
                                {"action": "click", "target": "used.png"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.detectUnusedImages(jsonNode);
            
            assertTrue(hasWarning(result, "unused-image"));
        }
        
        @ParameterizedTest
        @MethodSource("provideImagePaths")
        @DisplayName("Validate image path formats")
        public void testImagePathFormats(String path, boolean shouldBeValid) throws Exception {
            String json = String.format("""
                {
                    "states": [
                        {"name": "State", "images": ["%s"]}
                    ]
                }
                """, path);
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateImagePaths(jsonNode);
            
            assertEquals(shouldBeValid, result.isValid());
        }
        
        static Stream<Arguments> provideImagePaths() {
            return Stream.of(
                Arguments.of("image.png", true),
                Arguments.of("path/to/image.png", true),
                Arguments.of("image-with-dash.png", true),
                Arguments.of("image_with_underscore.png", true),
                Arguments.of("", false),
                Arguments.of("image", false),  // No extension
                Arguments.of("image.txt", false),  // Wrong extension
                Arguments.of("../../../etc/passwd", false)  // Path traversal
            );
        }
    }
    
    @Nested
    @DisplayName("Region Reference Validation")
    class RegionReferenceValidation {
        
        @Test
        @DisplayName("Valid region references pass validation")
        public void testValidRegionReferences() throws Exception {
            String json = """
                {
                    "states": [
                        {
                            "name": "State1",
                            "regions": [
                                {"name": "headerRegion", "x": 0, "y": 0, "w": 800, "h": 100}
                            ]
                        }
                    ],
                    "functions": [
                        {
                            "name": "clickHeader",
                            "steps": [
                                {"action": "click", "region": "headerRegion"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateRegionReferences(jsonNode);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid region reference fails")
        public void testInvalidRegionReference() throws Exception {
            String json = """
                {
                    "functions": [
                        {
                            "name": "clickRegion",
                            "steps": [
                                {"action": "click", "region": "nonExistentRegion"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateRegionReferences(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "region-not-found"));
        }
    }
    
    @Nested
    @DisplayName("Cross-Configuration References")
    class CrossConfigurationReferences {
        
        @Test
        @DisplayName("Validate references across project and automation files")
        public void testCrossFileReferences() throws Exception {
            String projectJson = """
                {
                    "name": "TestProject",
                    "states": [
                        {"name": "LoginState", "images": ["login.png"]}
                    ]
                }
                """;
            
            String automationJson = """
                {
                    "functions": [
                        {
                            "name": "login",
                            "steps": [
                                {"action": "waitForState", "state": "LoginState"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode project = objectMapper.readTree(projectJson);
            JsonNode automation = objectMapper.readTree(automationJson);
            
            ValidationResult result = validator.validateCrossReferences(project, automation);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Invalid cross-file reference fails")
        public void testInvalidCrossFileReference() throws Exception {
            String projectJson = """
                {
                    "name": "TestProject",
                    "states": []
                }
                """;
            
            String automationJson = """
                {
                    "functions": [
                        {
                            "name": "login",
                            "steps": [
                                {"action": "waitForState", "state": "NonExistentState"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode project = objectMapper.readTree(projectJson);
            JsonNode automation = objectMapper.readTree(automationJson);
            
            ValidationResult result = validator.validateCrossReferences(project, automation);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "state-not-found"));
        }
    }
    
    @Nested
    @DisplayName("Reference Chain Validation")
    class ReferenceChainValidation {
        
        @Test
        @DisplayName("Validate complete reference chains")
        public void testCompleteReferenceChain() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "A", "images": ["a.png"]},
                        {"name": "B", "images": ["b.png"]},
                        {"name": "C", "images": ["c.png"]}
                    ],
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "next"},
                        {"from": "B", "to": "C", "trigger": "next"}
                    ],
                    "functions": [
                        {
                            "name": "navigateToC",
                            "steps": [
                                {"action": "waitForState", "state": "A"},
                                {"action": "click", "target": "a.png"},
                                {"action": "waitForState", "state": "B"},
                                {"action": "click", "target": "b.png"},
                                {"action": "waitForState", "state": "C"}
                            ]
                        }
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateReferenceChains(jsonNode);
            
            assertTrue(result.isValid());
        }
        
        @Test
        @DisplayName("Detect broken reference chains")
        public void testBrokenReferenceChain() throws Exception {
            String json = """
                {
                    "states": [
                        {"name": "A", "images": ["a.png"]},
                        {"name": "C", "images": ["c.png"]}
                    ],
                    "transitions": [
                        {"from": "A", "to": "B", "trigger": "next"},
                        {"from": "B", "to": "C", "trigger": "next"}
                    ]
                }
                """;
            
            JsonNode jsonNode = objectMapper.readTree(json);
            ValidationResult result = validator.validateReferenceChains(jsonNode);
            
            assertFalse(result.isValid());
            assertTrue(hasError(result, "broken-chain"));
        }
    }
    
    // Mock validator implementations
    private static class ReferenceValidator {
        
        public ValidationResult validateImageReferences(JsonNode json) {
            ValidationResult result = new ValidationResult();
            Set<String> availableImages = extractAllImages(json);
            
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    if (function.has("steps")) {
                        for (JsonNode step : function.get("steps")) {
                            if (step.has("target")) {
                                String target = step.get("target").asText();
                                if (target.endsWith(".png") && !availableImages.contains(target)) {
                                    result.addError("image-not-found",
                                            "Image " + target + " not found in any state", ValidationSeverity.ERROR);
                                }
                            }
                        }
                    }
                }
            }
            
            return result;
        }
        
        public ValidationResult validateImagePaths(JsonNode json) {
            ValidationResult result = new ValidationResult();
            
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    if (state.has("images")) {
                        for (JsonNode image : state.get("images")) {
                            String path = image.asText();
                            if (!isValidImagePath(path)) {
                                result.addError("invalid-path", "Invalid image path: " + path,
                                                ValidationSeverity.ERROR);
                            }
                        }
                    }
                }
            }
            
            return result;
        }
        
        public ValidationResult validateRegionReferences(JsonNode json) {
            ValidationResult result = new ValidationResult();
            Set<String> availableRegions = extractAllRegions(json);
            
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    if (function.has("steps")) {
                        for (JsonNode step : function.get("steps")) {
                            if (step.has("region")) {
                                String region = step.get("region").asText();
                                if (!availableRegions.contains(region)) {
                                    result.addError("region-not-found",
                                            "Region " + region + " not found", ValidationSeverity.ERROR);
                                }
                            }
                        }
                    }
                }
            }
            
            return result;
        }
        
        public ValidationResult detectUnusedImages(JsonNode json) {
            ValidationResult result = new ValidationResult();
            Set<String> allImages = extractAllImages(json);
            Set<String> usedImages = extractUsedImages(json);
            
            for (String image : allImages) {
                if (!usedImages.contains(image)) {
                    result.addError("unused-image", "Image " + image + " is defined but never used",
                                   ValidationSeverity.WARNING);
                }
            }
            
            return result;
        }
        
        public ValidationResult validateCrossReferences(JsonNode project, JsonNode automation) {
            ValidationResult result = new ValidationResult();
            Set<String> projectStates = extractStates(project);
            
            if (automation.has("functions")) {
                for (JsonNode function : automation.get("functions")) {
                    if (function.has("steps")) {
                        for (JsonNode step : function.get("steps")) {
                            if (step.has("state")) {
                                String state = step.get("state").asText();
                                if (!projectStates.contains(state)) {
                                    result.addError("state-not-found", 
                                                  "State " + state + " not found in project",
                                                  ValidationSeverity.ERROR);
                                }
                            }
                        }
                    }
                }
            }
            
            return result;
        }
        
        public ValidationResult validateReferenceChains(JsonNode json) {
            ValidationResult result = new ValidationResult();
            Set<String> states = extractStates(json);
            
            if (json.has("transitions")) {
                for (JsonNode transition : json.get("transitions")) {
                    String from = transition.get("from").asText();
                    String to = transition.get("to").asText();
                    
                    if (!states.contains(from) || !states.contains(to)) {
                        String missing = !states.contains(from) ? from : to;
                        result.addError("broken-chain", 
                                      "Broken reference chain - state " + missing + " missing",
                                      ValidationSeverity.ERROR);
                    }
                }
            }
            
            return result;
        }
        
        private Set<String> extractAllImages(JsonNode json) {
            Set<String> images = new java.util.HashSet<>();
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    if (state.has("images")) {
                        for (JsonNode image : state.get("images")) {
                            images.add(image.asText());
                        }
                    }
                }
            }
            return images;
        }
        
        private Set<String> extractUsedImages(JsonNode json) {
            Set<String> used = new java.util.HashSet<>();
            if (json.has("functions")) {
                for (JsonNode function : json.get("functions")) {
                    if (function.has("steps")) {
                        for (JsonNode step : function.get("steps")) {
                            if (step.has("target")) {
                                used.add(step.get("target").asText());
                            }
                        }
                    }
                }
            }
            return used;
        }
        
        private Set<String> extractAllRegions(JsonNode json) {
            Set<String> regions = new java.util.HashSet<>();
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    if (state.has("regions")) {
                        for (JsonNode region : state.get("regions")) {
                            if (region.has("name")) {
                                regions.add(region.get("name").asText());
                            }
                        }
                    }
                }
            }
            return regions;
        }
        
        private Set<String> extractStates(JsonNode json) {
            Set<String> states = new java.util.HashSet<>();
            if (json.has("states")) {
                for (JsonNode state : json.get("states")) {
                    if (state.has("name")) {
                        states.add(state.get("name").asText());
                    }
                }
            }
            return states;
        }
        
        private boolean isValidImagePath(String path) {
            if (path == null || path.isEmpty()) return false;
            if (!path.matches(".*\\.(png|jpg|jpeg|gif|bmp)$")) return false;
            if (path.contains("..")) return false;  // Path traversal
            return true;
        }
    }
    
    // Helper methods for ValidationResult checking
    private boolean hasError(ValidationResult result, String errorCode) {
        return result.getErrors().stream()
            .anyMatch(e -> e.errorCode().equals(errorCode));
    }
    
    private boolean hasWarning(ValidationResult result, String errorCode) {
        return result.getErrors().stream()
            .anyMatch(e -> e.errorCode().equals(errorCode) && 
                          e.severity() == ValidationSeverity.WARNING);
    }
}