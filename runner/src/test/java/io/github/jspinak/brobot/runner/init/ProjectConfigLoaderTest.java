package io.github.jspinak.brobot.runner.init;

import io.github.jspinak.brobot.json.schemaValidation.ConfigValidator;
import io.github.jspinak.brobot.json.schemaValidation.exception.ConfigValidationException;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProjectConfigLoaderTest {

    @Mock
    private ConfigValidator configValidator;

    private ProjectConfigLoader projectConfigLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectConfigLoader = new ProjectConfigLoader(configValidator);
    }

    @Test
    void loadAndValidate_success() throws IOException, ConfigValidationException {
        // Create test files with content
        Path projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        Files.writeString(projectConfigPath, "{\"test\": \"project\"}");

        Path dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));
        Files.writeString(dslConfigPath, "{\"test\": \"dsl\"}");

        // Set up validation result
        ValidationResult expectedResult = new ValidationResult();
        when(configValidator.validateConfiguration(anyString(), anyString(), any(Path.class)))
                .thenReturn(expectedResult);

        // Call the method to test
        ValidationResult result = projectConfigLoader.loadAndValidate(
                projectConfigPath, dslConfigPath, tempDir);

        // Verify the result
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void loadAndValidate_fileNotFound() {
        // Use non-existent files
        Path projectConfigPath = tempDir.resolve("nonexistent_project.json");
        Path dslConfigPath = tempDir.resolve("nonexistent_dsl.json");

        // Verify that exception is thrown
        assertThrows(IOException.class, () -> projectConfigLoader.loadAndValidate(
                projectConfigPath, dslConfigPath, tempDir));
    }

    @Test
    void loadAndValidate_validationError() throws IOException {
        // Create test files with content
        Path projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        Files.writeString(projectConfigPath, "{\"test\": \"project\"}");

        Path dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));
        Files.writeString(dslConfigPath, "{\"test\": \"dsl\"}");

        // Set up validation to throw exception
        when(configValidator.validateConfiguration(anyString(), anyString(), any(Path.class)))
                .thenThrow(new ConfigValidationException("Validation failed"));

        // Verify that exception is thrown
        assertThrows(ConfigValidationException.class, () -> projectConfigLoader.loadAndValidate(
                projectConfigPath, dslConfigPath, tempDir));
    }
}