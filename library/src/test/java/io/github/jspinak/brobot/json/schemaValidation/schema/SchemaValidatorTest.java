package io.github.jspinak.brobot.json.schemaValidation.schema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.json.validation.schema.AutomationDSLValidator;
import io.github.jspinak.brobot.runner.json.validation.schema.ProjectSchemaValidator;
import io.github.jspinak.brobot.runner.json.validation.schema.SchemaValidator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchemaValidatorTest {

    @Mock
    private ProjectSchemaValidator projectValidatorMock;

    @Mock
    private AutomationDSLValidator dslValidatorMock;

    @InjectMocks // This will inject the mocks into a real SchemaValidator instance
    private SchemaValidator schemaValidator;

    private String validProjectJson;
    private String invalidProjectJson;
    private String validDslJson;
    private String invalidDslJson;

    // Helper method to load JSON content from test resources
    private String loadJsonFromFile(String filePath) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource(filePath)).toURI()
        )));
    }

    @BeforeEach
    void setUp() throws IOException, URISyntaxException {
        // Initialize a real SchemaValidator but with mocked internal validators
        // schemaValidator = new SchemaValidator(); // Not needed with @InjectMocks

        // Load test JSON data
        // Ensure these files are in src/test/resources
        validProjectJson = loadJsonFromFile("floranext-project.json");
        validDslJson = loadJsonFromFile("floranext-automation.json");

        // For invalid JSON, you can create simple malformed strings or separate files
        invalidProjectJson = "{ \"name\": \"test\", \"id\": \"not_an_int\" }"; // Example of schema violation
        invalidDslJson = "{\"automationFunctions\": [{\"name\": null}]}"; // Example of schema violation

    }

    @Test
    void validateProjectSchema_whenProjectJsonIsValid_shouldReturnSuccessfulResult() {
        ValidationResult expectedResult = new ValidationResult(); // Empty means success
        when(projectValidatorMock.validate(validProjectJson)).thenReturn(expectedResult);

        ValidationResult actualResult = schemaValidator.validateProjectSchema(validProjectJson);

        assertTrue(actualResult.isValid());
        assertEquals(0, actualResult.getErrors().size());
        verify(projectValidatorMock, times(1)).validate(validProjectJson);
    }

    @Test
    void validateProjectSchema_whenProjectJsonIsInvalid_shouldReturnErrorResult() {
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(new ValidationError("Schema error", "Invalid ID", ValidationSeverity.ERROR));
        when(projectValidatorMock.validate(invalidProjectJson)).thenReturn(expectedResult);

        ValidationResult actualResult = schemaValidator.validateProjectSchema(invalidProjectJson);

        assertFalse(actualResult.isValid());
        assertEquals(1, actualResult.getErrors().size());
        assertEquals("Schema error", actualResult.getErrors().get(0).errorCode());
        verify(projectValidatorMock, times(1)).validate(invalidProjectJson);
    }

    @Test
    void validateDSLSchema_whenDslJsonIsValid_shouldReturnSuccessfulResult() {
        ValidationResult expectedResult = new ValidationResult();
        when(dslValidatorMock.validate(validDslJson)).thenReturn(expectedResult);

        ValidationResult actualResult = schemaValidator.validateDSLSchema(validDslJson);

        assertTrue(actualResult.isValid());
        assertEquals(0, actualResult.getErrors().size());
        verify(dslValidatorMock, times(1)).validate(validDslJson);
    }

    @Test
    void validateDSLSchema_whenDslJsonIsInvalid_shouldReturnErrorResult() {
        ValidationResult expectedResult = new ValidationResult();
        expectedResult.addError(new ValidationError("DSL error", "Null function name", ValidationSeverity.ERROR));
        when(dslValidatorMock.validate(invalidDslJson)).thenReturn(expectedResult);

        ValidationResult actualResult = schemaValidator.validateDSLSchema(invalidDslJson);

        assertFalse(actualResult.isValid());
        assertEquals(1, actualResult.getErrors().size());
        assertEquals("DSL error", actualResult.getErrors().get(0).errorCode());
        verify(dslValidatorMock, times(1)).validate(invalidDslJson);
    }

    @Test
    void validateAll_whenBothAreValid_shouldReturnSuccessfulResult() {
        ValidationResult projectResult = new ValidationResult();
        ValidationResult dslResult = new ValidationResult();

        when(projectValidatorMock.validate(validProjectJson)).thenReturn(projectResult);
        when(dslValidatorMock.validate(validDslJson)).thenReturn(dslResult);

        ValidationResult actualResult = schemaValidator.validateAll(validProjectJson, validDslJson);

        assertTrue(actualResult.isValid());
        assertEquals(0, actualResult.getErrors().size());
        verify(projectValidatorMock, times(1)).validate(validProjectJson);
        verify(dslValidatorMock, times(1)).validate(validDslJson);
    }

    @Test
    void validateAll_whenProjectIsInvalidAndDslIsValid_shouldReturnProjectErrors() {
        ValidationResult projectResult = new ValidationResult();
        projectResult.addError(new ValidationError("ProjectFail", "Bad project", ValidationSeverity.CRITICAL));
        ValidationResult dslResult = new ValidationResult();

        when(projectValidatorMock.validate(invalidProjectJson)).thenReturn(projectResult);
        when(dslValidatorMock.validate(validDslJson)).thenReturn(dslResult);

        ValidationResult actualResult = schemaValidator.validateAll(invalidProjectJson, validDslJson);

        assertFalse(actualResult.isValid());
        assertEquals(1, actualResult.getErrors().size());
        assertEquals("ProjectFail", actualResult.getErrors().get(0).errorCode());
        verify(projectValidatorMock, times(1)).validate(invalidProjectJson);
        verify(dslValidatorMock, times(1)).validate(validDslJson);
    }

    @Test
    void validateAll_whenProjectIsValidAndDslIsInvalid_shouldReturnDslErrors() {
        ValidationResult projectResult = new ValidationResult();
        ValidationResult dslResult = new ValidationResult();
        dslResult.addError(new ValidationError("DslFail", "Bad DSL", ValidationSeverity.ERROR));


        when(projectValidatorMock.validate(validProjectJson)).thenReturn(projectResult);
        when(dslValidatorMock.validate(invalidDslJson)).thenReturn(dslResult);

        ValidationResult actualResult = schemaValidator.validateAll(validProjectJson, invalidDslJson);

        assertFalse(actualResult.isValid());
        assertEquals(1, actualResult.getErrors().size());
        assertEquals("DslFail", actualResult.getErrors().get(0).errorCode());
        verify(projectValidatorMock, times(1)).validate(validProjectJson);
        verify(dslValidatorMock, times(1)).validate(invalidDslJson);
    }

    @Test
    void validateAll_whenBothAreInvalid_shouldReturnMergedErrors() {
        ValidationResult projectResult = new ValidationResult();
        projectResult.addError(new ValidationError("ProjectIssue", "Proj issue", ValidationSeverity.WARNING));
        ValidationResult dslResult = new ValidationResult();
        dslResult.addError(new ValidationError("DslIssue", "DSL issue", ValidationSeverity.ERROR));

        when(projectValidatorMock.validate(invalidProjectJson)).thenReturn(projectResult);
        when(dslValidatorMock.validate(invalidDslJson)).thenReturn(dslResult);

        ValidationResult actualResult = schemaValidator.validateAll(invalidProjectJson, invalidDslJson);

        assertFalse(actualResult.isValid());
        assertEquals(2, actualResult.getErrors().size());
        assertTrue(actualResult.getErrors().stream().anyMatch(e -> "ProjectIssue".equals(e.errorCode())));
        assertTrue(actualResult.getErrors().stream().anyMatch(e -> "DslIssue".equals(e.errorCode())));
        verify(projectValidatorMock, times(1)).validate(invalidProjectJson);
        verify(dslValidatorMock, times(1)).validate(invalidDslJson);
    }
}