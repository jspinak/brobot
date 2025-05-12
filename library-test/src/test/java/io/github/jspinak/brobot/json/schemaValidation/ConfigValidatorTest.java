package io.github.jspinak.brobot.json.schemaValidation;

import io.github.jspinak.brobot.json.schemaValidation.exception.ConfigValidationException;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import io.github.jspinak.brobot.json.schemaValidation.resource.ImageResourceValidator;
import io.github.jspinak.brobot.json.schemaValidation.schema.SchemaValidator;
import io.github.jspinak.brobot.json.schemaValidation.crossref.ReferenceValidator;
import io.github.jspinak.brobot.json.schemaValidation.business.BusinessRuleValidator;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorTest {

    @Mock
    private SchemaValidator schemaValidatorMock;

    @Mock
    private ReferenceValidator referenceValidatorMock;

    @Mock
    private BusinessRuleValidator businessRuleValidatorMock;

    @Mock
    private ImageResourceValidator imageResourceValidatorMock;

    @InjectMocks
    private ConfigValidator configValidator;

    private String validProjectJson;
    private String validDslJson;
    private String invalidProjectJson;
    private Path imageBasePath;

    @BeforeEach
    void setUp() {
        validProjectJson = "{\"id\":1,\"name\":\"Valid Project\"}";
        validDslJson = "{\"automationFunctions\":[]}";
        invalidProjectJson = "{\"id\":\"invalid\"}";
        imageBasePath = Path.of("src/test/resources/images");
    }

    @Test
    void validateConfiguration_whenAllValid_shouldReturnSuccessfulResult() {
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult referenceResult = new ValidationResult();
        ValidationResult businessResult = new ValidationResult();
        ValidationResult imageResult = new ValidationResult();

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(schemaResult);
        when(referenceValidatorMock.validateReferences(any(JSONObject.class), any(JSONObject.class))).thenReturn(referenceResult);
        when(businessRuleValidatorMock.validateRules(any(JSONObject.class), any(JSONObject.class))).thenReturn(businessResult);
        when(imageResourceValidatorMock.validateImageResources(any(JSONObject.class), eq(imageBasePath))).thenReturn(imageResult);

        ValidationResult result = configValidator.validateConfiguration(validProjectJson, validDslJson, imageBasePath);

        assertTrue(result.isValid());
        verify(schemaValidatorMock).validateProjectSchema(validProjectJson);
        verify(schemaValidatorMock).validateDSLSchema(validDslJson);
        verify(referenceValidatorMock).validateReferences(any(JSONObject.class), any(JSONObject.class));
        verify(businessRuleValidatorMock).validateRules(any(JSONObject.class), any(JSONObject.class));
        verify(imageResourceValidatorMock).validateImageResources(any(JSONObject.class), eq(imageBasePath));
    }

    @Test
    void validateConfiguration_whenSchemaValidationFails_shouldThrowException() {
        ValidationResult schemaResult = new ValidationResult();
        schemaResult.addError(new ValidationError("SchemaError", "Invalid schema", ValidationSeverity.CRITICAL));

        when(schemaValidatorMock.validateProjectSchema(invalidProjectJson)).thenReturn(schemaResult);

        ConfigValidationException exception = assertThrows(ConfigValidationException.class, () ->
                configValidator.validateConfiguration(invalidProjectJson, validDslJson, imageBasePath));

        assertTrue(exception.getValidationResult().hasCriticalErrors());
        verify(schemaValidatorMock).validateProjectSchema(invalidProjectJson);
        verify(schemaValidatorMock, never()).validateDSLSchema(anyString());
    }

    @Test
    void validateImageResourcesOnly_whenImagesAreInvalid_shouldReturnErrors() {
        ValidationResult imageResult = new ValidationResult();
        imageResult.addError(new ValidationError("ImageError", "Missing image", ValidationSeverity.ERROR));

        when(imageResourceValidatorMock.validateImageResources(any(JSONObject.class), eq(imageBasePath))).thenReturn(imageResult);

        ValidationResult result = configValidator.validateImageResourcesOnly(validProjectJson, imageBasePath);

        assertFalse(result.isValid());
        assertTrue(result.hasSevereErrors());
        verify(imageResourceValidatorMock).validateImageResources(any(JSONObject.class), eq(imageBasePath));
    }
}