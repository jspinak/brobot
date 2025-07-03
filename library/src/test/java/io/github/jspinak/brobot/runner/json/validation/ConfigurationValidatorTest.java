package io.github.jspinak.brobot.runner.json.validation;

import io.github.jspinak.brobot.runner.json.validation.business.BusinessRuleValidator;
import io.github.jspinak.brobot.runner.json.validation.crossref.ReferenceValidator;
import io.github.jspinak.brobot.runner.json.validation.exception.ConfigValidationException;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.resource.ImageResourceValidator;
import io.github.jspinak.brobot.runner.json.validation.schema.SchemaValidator;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {

    @Mock
    private SchemaValidator schemaValidator;
    
    @Mock
    private ReferenceValidator referenceValidator;
    
    @Mock
    private BusinessRuleValidator businessRuleValidator;
    
    @Mock
    private ImageResourceValidator imageResourceValidator;
    
    private ConfigurationValidator configurationValidator;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        configurationValidator = new ConfigurationValidator(
            schemaValidator,
            referenceValidator,
            businessRuleValidator,
            imageResourceValidator
        );
    }
    
    @Test
    void testValidateConfiguration_Success() throws Exception {
        // Setup
        String projectJson = "{\"states\": []}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult refResult = new ValidationResult();
        ValidationResult businessResult = new ValidationResult();
        ValidationResult imageResult = new ValidationResult();
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(schemaResult);
        when(referenceValidator.validateReferences(any(JSONObject.class), any(JSONObject.class)))
            .thenReturn(refResult);
        when(businessRuleValidator.validateRules(any(JSONObject.class), any(JSONObject.class)))
            .thenReturn(businessResult);
        when(imageResourceValidator.validateImageResources(any(JSONObject.class), eq(imagePath)))
            .thenReturn(imageResult);
        
        // Execute
        ValidationResult result = configurationValidator.validateConfiguration(projectJson, dslJson, imagePath);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isValid());
        assertFalse(result.hasCriticalErrors());
        
        verify(schemaValidator).validateProjectSchema(projectJson);
        verify(schemaValidator).validateDSLSchema(dslJson);
        verify(referenceValidator).validateReferences(any(JSONObject.class), any(JSONObject.class));
        verify(businessRuleValidator).validateRules(any(JSONObject.class), any(JSONObject.class));
        verify(imageResourceValidator).validateImageResources(any(JSONObject.class), eq(imagePath));
    }
    
    @Test
    void testValidateConfiguration_ProjectSchemaFailure() {
        // Setup
        String projectJson = "{\"invalid\": true}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        schemaResult.addError(new ValidationError("Schema", "Invalid schema", ValidationSeverity.CRITICAL));
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        
        // Execute & Verify
        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
            () -> configurationValidator.validateConfiguration(projectJson, dslJson, imagePath));
        
        assertTrue(exception.getMessage().contains("Schema validation failed"));
        assertNotNull(exception.getValidationResult());
        assertTrue(exception.getValidationResult().hasCriticalErrors());
        
        // Should not proceed to other validations
        verify(schemaValidator).validateProjectSchema(projectJson);
        verify(schemaValidator, never()).validateDSLSchema(anyString());
        verifyNoInteractions(referenceValidator, businessRuleValidator, imageResourceValidator);
    }
    
    @Test
    void testValidateConfiguration_DslSchemaFailure() {
        // Setup
        String projectJson = "{\"states\": []}";
        String dslJson = "{\"invalid\": true}";
        Path imagePath = tempDir;
        
        ValidationResult projectSchemaResult = new ValidationResult();
        ValidationResult dslSchemaResult = new ValidationResult();
        dslSchemaResult.addError(new ValidationError("DSL Schema", "Invalid DSL", ValidationSeverity.CRITICAL));
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(projectSchemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(dslSchemaResult);
        
        // Execute & Verify
        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
            () -> configurationValidator.validateConfiguration(projectJson, dslJson, imagePath));
        
        assertTrue(exception.getMessage().contains("Schema validation failed"));
        
        verify(schemaValidator).validateProjectSchema(projectJson);
        verify(schemaValidator).validateDSLSchema(dslJson);
        verifyNoInteractions(referenceValidator, businessRuleValidator, imageResourceValidator);
    }
    
    @Test
    void testValidateConfiguration_WithWarnings() throws Exception {
        // Setup
        String projectJson = "{\"states\": []}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult refResult = new ValidationResult();
        refResult.addError(new ValidationError("Reference", "Unused state", ValidationSeverity.WARNING));
        ValidationResult businessResult = new ValidationResult();
        ValidationResult imageResult = new ValidationResult();
        imageResult.addError(new ValidationError("Image", "Image found", ValidationSeverity.INFO));
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(schemaResult);
        when(referenceValidator.validateReferences(any(), any())).thenReturn(refResult);
        when(businessRuleValidator.validateRules(any(), any())).thenReturn(businessResult);
        when(imageResourceValidator.validateImageResources(any(), eq(imagePath))).thenReturn(imageResult);
        
        // Execute
        ValidationResult result = configurationValidator.validateConfiguration(projectJson, dslJson, imagePath);
        
        // Verify
        assertTrue(result.isValid()); // Warnings don't make it invalid
        assertFalse(result.hasCriticalErrors());
        assertEquals(1, result.getWarnings().size());
        assertEquals(1, result.getInfoMessages().size());
    }
    
    @Test
    void testValidateConfiguration_InvalidJson() {
        // Setup
        String projectJson = "{invalid json}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(schemaResult);
        
        // Execute & Verify
        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
            () -> configurationValidator.validateConfiguration(projectJson, dslJson, imagePath));
        
        assertTrue(exception.getMessage().contains("Failed to parse JSON"));
        assertTrue(exception.getCause().getClass().getName().contains("JSONException"));
    }
    
    @Test
    void testValidateConfiguration_ReferenceValidationError() throws Exception {
        // Setup
        String projectJson = "{\"states\": []}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult refResult = new ValidationResult();
        refResult.addError(new ValidationError("Reference", "Invalid reference", ValidationSeverity.CRITICAL));
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(schemaResult);
        when(referenceValidator.validateReferences(any(), any())).thenReturn(refResult);
        when(businessRuleValidator.validateRules(any(), any())).thenReturn(new ValidationResult());
        when(imageResourceValidator.validateImageResources(any(), eq(imagePath))).thenReturn(new ValidationResult());
        
        // Execute & Verify
        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
            () -> configurationValidator.validateConfiguration(projectJson, dslJson, imagePath));
        
        assertTrue(exception.getMessage().contains("Validation failed with critical errors"));
    }
    
    @Test
    void testValidateProjectSchemaOnly() {
        // Setup
        String projectJson = "{\"states\": []}";
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(new ValidationError("Schema", "Minor issue", ValidationSeverity.WARNING));
        
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(expectedResult);
        
        // Execute
        ValidationResult result = configurationValidator.validateProjectSchemaOnly(projectJson);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(schemaValidator).validateProjectSchema(projectJson);
        verifyNoInteractions(referenceValidator, businessRuleValidator, imageResourceValidator);
    }
    
    @Test
    void testValidateDslSchemaOnly() {
        // Setup
        String dslJson = "{\"functions\": []}";
        ValidationResult expectedResult = new ValidationResult();
        
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(expectedResult);
        
        // Execute
        ValidationResult result = configurationValidator.validateDslSchemaOnly(dslJson);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(schemaValidator).validateDSLSchema(dslJson);
        verifyNoInteractions(referenceValidator, businessRuleValidator, imageResourceValidator);
    }
    
    @Test
    void testValidateImageResourcesOnly() {
        // Setup
        String projectJson = "{\"states\": [{\"images\": [\"test.png\"]}]}";
        Path imagePath = tempDir;
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(new ValidationError("Image", "Image validated", ValidationSeverity.INFO));
        
        when(imageResourceValidator.validateImageResources(any(JSONObject.class), eq(imagePath)))
            .thenReturn(expectedResult);
        
        // Execute
        ValidationResult result = configurationValidator.validateImageResourcesOnly(projectJson, imagePath);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.getInfoMessages().size());
        verify(imageResourceValidator).validateImageResources(any(JSONObject.class), eq(imagePath));
        verifyNoInteractions(schemaValidator, referenceValidator, businessRuleValidator);
    }
    
    @Test
    void testValidateImageResourcesOnly_InvalidJson() {
        // Setup
        String projectJson = "{invalid json}";
        Path imagePath = tempDir;
        
        // Execute
        ValidationResult result = configurationValidator.validateImageResourcesOnly(projectJson, imagePath);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.hasCriticalErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).message().contains("Failed to parse JSON"));
        verifyNoInteractions(imageResourceValidator);
    }
    
    @Test
    void testValidateConfiguration_UnexpectedException() throws Exception {
        // Setup
        String projectJson = "{\"states\": []}";
        String dslJson = "{\"functions\": []}";
        Path imagePath = tempDir;
        
        ValidationResult schemaResult = new ValidationResult();
        when(schemaValidator.validateProjectSchema(projectJson)).thenReturn(schemaResult);
        when(schemaValidator.validateDSLSchema(dslJson)).thenReturn(schemaResult);
        when(referenceValidator.validateReferences(any(), any()))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        // Execute & Verify
        ConfigValidationException exception = assertThrows(ConfigValidationException.class,
            () -> configurationValidator.validateConfiguration(projectJson, dslJson, imagePath));
        
        assertTrue(exception.getMessage().contains("Validation failed due to an unexpected error"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
}