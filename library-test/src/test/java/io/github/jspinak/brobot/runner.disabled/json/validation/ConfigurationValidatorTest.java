package io.github.jspinak.brobot.runner.json.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.json.validation.business.BusinessRuleValidator;
import io.github.jspinak.brobot.runner.json.validation.crossref.ReferenceValidator;
import io.github.jspinak.brobot.runner.json.validation.exception.ConfigValidationException;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.resource.ImageResourceValidator;
import io.github.jspinak.brobot.runner.json.validation.schema.SchemaValidator;

@ExtendWith(MockitoExtension.class)
class ConfigurationValidatorTest {

    @Mock private SchemaValidator schemaValidatorMock;

    @Mock private ReferenceValidator referenceValidatorMock;

    @Mock private BusinessRuleValidator businessRuleValidatorMock;

    @Mock private ImageResourceValidator imageResourceValidatorMock;

    @InjectMocks private ConfigurationValidator configValidator;

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
        when(referenceValidatorMock.validateReferences(
                        any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(referenceResult);
        when(businessRuleValidatorMock.validateRules(any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(businessResult);
        when(imageResourceValidatorMock.validateImageResources(
                        any(JSONObject.class), eq(imageBasePath)))
                .thenReturn(imageResult);

        ValidationResult result =
                configValidator.validateConfiguration(
                        validProjectJson, validDslJson, imageBasePath);

        assertTrue(result.isValid());
        verify(schemaValidatorMock).validateProjectSchema(validProjectJson);
        verify(schemaValidatorMock).validateDSLSchema(validDslJson);
        verify(referenceValidatorMock)
                .validateReferences(any(JSONObject.class), any(JSONObject.class));
        verify(businessRuleValidatorMock)
                .validateRules(any(JSONObject.class), any(JSONObject.class));
        verify(imageResourceValidatorMock)
                .validateImageResources(any(JSONObject.class), eq(imageBasePath));
    }

    @Test
    void validateConfiguration_whenSchemaValidationFails_shouldThrowException() {
        ValidationResult schemaResult = new ValidationResult();
        schemaResult.addError(
                new ValidationError("SchemaError", "Invalid schema", ValidationSeverity.CRITICAL));

        when(schemaValidatorMock.validateProjectSchema(invalidProjectJson))
                .thenReturn(schemaResult);

        ConfigValidationException exception =
                assertThrows(
                        ConfigValidationException.class,
                        () ->
                                configValidator.validateConfiguration(
                                        invalidProjectJson, validDslJson, imageBasePath));

        assertTrue(exception.getValidationResult().hasCriticalErrors());
        verify(schemaValidatorMock).validateProjectSchema(invalidProjectJson);
        verify(schemaValidatorMock, never()).validateDSLSchema(anyString());
    }

    @Test
    void validateImageResourcesOnly_whenImagesAreInvalid_shouldReturnErrors() {
        ValidationResult imageResult = new ValidationResult();
        imageResult.addError(
                new ValidationError("ImageError", "Missing image", ValidationSeverity.ERROR));

        when(imageResourceValidatorMock.validateImageResources(
                        any(JSONObject.class), eq(imageBasePath)))
                .thenReturn(imageResult);

        ValidationResult result =
                configValidator.validateImageResourcesOnly(validProjectJson, imageBasePath);

        assertFalse(result.isValid());
        assertTrue(result.hasSevereErrors());
        verify(imageResourceValidatorMock)
                .validateImageResources(any(JSONObject.class), eq(imageBasePath));
    }

    @Test
    void validateConfiguration_whenDslSchemaFails_shouldThrowException() {
        ValidationResult validSchemaResult = new ValidationResult();
        ValidationResult invalidSchemaResult = new ValidationResult();
        invalidSchemaResult.addError(
                new ValidationError("DslError", "Invalid DSL", ValidationSeverity.CRITICAL));

        when(schemaValidatorMock.validateProjectSchema(validProjectJson))
                .thenReturn(validSchemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(invalidSchemaResult);

        ConfigValidationException exception =
                assertThrows(
                        ConfigValidationException.class,
                        () ->
                                configValidator.validateConfiguration(
                                        validProjectJson, validDslJson, imageBasePath));

        assertTrue(exception.getValidationResult().hasCriticalErrors());
        verify(schemaValidatorMock).validateProjectSchema(validProjectJson);
        verify(schemaValidatorMock).validateDSLSchema(validDslJson);
        verify(referenceValidatorMock, never())
                .validateReferences(any(JSONObject.class), any(JSONObject.class));
    }

    @Test
    void validateConfiguration_whenReferenceValidationFails_shouldNotThrowForWarnings() {
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult referenceResult = new ValidationResult();
        referenceResult.addError(
                new ValidationError(
                        "RefWarning", "Minor reference issue", ValidationSeverity.WARNING));
        ValidationResult businessResult = new ValidationResult();
        ValidationResult imageResult = new ValidationResult();

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(schemaResult);
        when(referenceValidatorMock.validateReferences(
                        any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(referenceResult);
        when(businessRuleValidatorMock.validateRules(any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(businessResult);
        when(imageResourceValidatorMock.validateImageResources(
                        any(JSONObject.class), eq(imageBasePath)))
                .thenReturn(imageResult);

        ValidationResult result =
                configValidator.validateConfiguration(
                        validProjectJson, validDslJson, imageBasePath);

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.getWarnings().size());
    }

    @Test
    void validateConfiguration_whenBusinessRulesHaveErrors_shouldMergeAllResults() {
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult referenceResult = new ValidationResult();
        ValidationResult businessResult = new ValidationResult();
        businessResult.addError(
                new ValidationError(
                        "BusinessError", "Invalid transition cycle", ValidationSeverity.ERROR));
        ValidationResult imageResult = new ValidationResult();

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(schemaResult);
        when(referenceValidatorMock.validateReferences(
                        any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(referenceResult);
        when(businessRuleValidatorMock.validateRules(any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(businessResult);
        when(imageResourceValidatorMock.validateImageResources(
                        any(JSONObject.class), eq(imageBasePath)))
                .thenReturn(imageResult);

        ValidationResult result =
                configValidator.validateConfiguration(
                        validProjectJson, validDslJson, imageBasePath);

        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("BusinessError", result.getErrors().getFirst().errorCode());
    }

    @Test
    void validateConfiguration_whenJsonIsInvalid_shouldThrowExceptionWithJsonError() {
        ValidationResult schemaResult = new ValidationResult();
        String malformedJson = "{ invalid json";

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(malformedJson)).thenReturn(schemaResult);

        ConfigValidationException exception =
                assertThrows(
                        ConfigValidationException.class,
                        () ->
                                configValidator.validateConfiguration(
                                        validProjectJson, malformedJson, imageBasePath));

        assertTrue(exception.getValidationResult().hasCriticalErrors());
        assertTrue(
                exception.getValidationResult().getErrors().stream()
                        .anyMatch(e -> e.errorCode().equals("JSON parsing error")));
    }

    @Test
    void validateProjectSchemaOnly_shouldDelegateToSchemaValidator() {
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(
                new ValidationError(
                        "SchemaError", "Project schema invalid", ValidationSeverity.ERROR));

        when(schemaValidatorMock.validateProjectSchema(invalidProjectJson))
                .thenReturn(expectedResult);

        ValidationResult result = configValidator.validateProjectSchemaOnly(invalidProjectJson);

        assertSame(expectedResult, result);
        verify(schemaValidatorMock).validateProjectSchema(invalidProjectJson);
        verifyNoInteractions(
                referenceValidatorMock, businessRuleValidatorMock, imageResourceValidatorMock);
    }

    @Test
    void validateDslSchemaOnly_shouldDelegateToSchemaValidator() {
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(
                new ValidationError("DslError", "DSL schema invalid", ValidationSeverity.ERROR));

        String invalidDsl = "{\"functions\": []}";
        when(schemaValidatorMock.validateDSLSchema(invalidDsl)).thenReturn(expectedResult);

        ValidationResult result = configValidator.validateDslSchemaOnly(invalidDsl);

        assertSame(expectedResult, result);
        verify(schemaValidatorMock).validateDSLSchema(invalidDsl);
        verifyNoInteractions(
                referenceValidatorMock, businessRuleValidatorMock, imageResourceValidatorMock);
    }

    @Test
    void validateImageResourcesOnly_whenJsonIsMalformed_shouldReturnJsonError() {
        String malformedJson = "not json at all";

        ValidationResult result =
                configValidator.validateImageResourcesOnly(malformedJson, imageBasePath);

        assertFalse(result.isValid());
        assertTrue(result.hasCriticalErrors());
        assertEquals("JSON parsing error", result.getErrors().getFirst().errorCode());
        verifyNoInteractions(imageResourceValidatorMock);
    }

    @Test
    void validateConfiguration_whenMultipleValidatorsReturnErrors_shouldMergeAll() {
        ValidationResult schemaResult = new ValidationResult();
        ValidationResult referenceResult = new ValidationResult();
        referenceResult.addError(
                new ValidationError("RefError", "Invalid reference", ValidationSeverity.ERROR));
        ValidationResult businessResult = new ValidationResult();
        businessResult.addError(
                new ValidationError(
                        "BizError", "Business rule violated", ValidationSeverity.ERROR));
        ValidationResult imageResult = new ValidationResult();
        imageResult.addError(
                new ValidationError("ImgError", "Image not found", ValidationSeverity.WARNING));

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(schemaResult);
        when(referenceValidatorMock.validateReferences(
                        any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(referenceResult);
        when(businessRuleValidatorMock.validateRules(any(JSONObject.class), any(JSONObject.class)))
                .thenReturn(businessResult);
        when(imageResourceValidatorMock.validateImageResources(
                        any(JSONObject.class), eq(imageBasePath)))
                .thenReturn(imageResult);

        ValidationResult result =
                configValidator.validateConfiguration(
                        validProjectJson, validDslJson, imageBasePath);

        assertFalse(result.isValid());
        assertEquals(3, result.getErrors().size()); // Total errors includes warnings
        assertEquals(1, result.getWarnings().size());
        // Check for specific ERROR severity items
        assertEquals(
                2,
                result.getErrors().stream()
                        .filter(e -> e.severity() == ValidationSeverity.ERROR)
                        .count());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("RefError")));
        assertTrue(result.getErrors().stream().anyMatch(e -> e.errorCode().equals("BizError")));
        assertTrue(result.getWarnings().stream().anyMatch(e -> e.errorCode().equals("ImgError")));
    }

    @Test
    void validateConfiguration_whenUnexpectedExceptionOccurs_shouldWrapInCriticalError() {
        ValidationResult schemaResult = new ValidationResult();

        when(schemaValidatorMock.validateProjectSchema(validProjectJson)).thenReturn(schemaResult);
        when(schemaValidatorMock.validateDSLSchema(validDslJson)).thenReturn(schemaResult);
        when(referenceValidatorMock.validateReferences(
                        any(JSONObject.class), any(JSONObject.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ConfigValidationException exception =
                assertThrows(
                        ConfigValidationException.class,
                        () ->
                                configValidator.validateConfiguration(
                                        validProjectJson, validDslJson, imageBasePath));

        assertTrue(exception.getValidationResult().hasCriticalErrors());
        assertTrue(
                exception.getValidationResult().getErrors().stream()
                        .anyMatch(
                                e ->
                                        e.errorCode().equals("Validation error")
                                                && e.message().contains("Unexpected error")));
    }
}
