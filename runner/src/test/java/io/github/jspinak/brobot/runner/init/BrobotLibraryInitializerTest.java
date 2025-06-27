package io.github.jspinak.brobot.runner.init;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.config.FrameworkInitializer;
import io.github.jspinak.brobot.navigation.service.StateService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BrobotLibraryInitializerTest {

    @Mock
    private Init initService;

    @Mock
    private BrobotRunnerProperties properties;

    @Mock
    private ConfigurationParser jsonParser;

    @Mock
    private ProjectConfigLoader projectConfigLoader;

    @Mock
    private ConfigurationValidator configValidator;

    @Mock
    private ImageResourceManager imageResourceManager;

    @Mock
    private StateService allStatesInProjectService;

    private BrobotLibraryInitializer initializer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup properties mock
        when(properties.getConfigPath()).thenReturn(tempDir.toString());
        when(properties.getImagePath()).thenReturn(tempDir.toString());
        when(properties.getLogPath()).thenReturn(tempDir.toString());
        when(properties.getTempPath()).thenReturn(tempDir.toString());
        when(properties.isValidateConfiguration()).thenReturn(true);

        initializer = new BrobotLibraryInitializer(
                initService, properties, jsonParser, projectConfigLoader,
                configValidator, imageResourceManager, allStatesInProjectService);
    }

    @Test
    void onApplicationReady() {
        // Call the method to test
        initializer.onApplicationReady();

        // Verify that the init service was called
        verify(initService).setBundlePathAndPreProcessImages(anyString());
    }

    @Test
    void initializeWithConfig_success() throws Exception {
        // Create test files
        Path projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        Path dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));

        // Set up validation result
        ValidationResult validationResult = new ValidationResult();
        when(configValidator.validateConfiguration(anyString(), anyString(), any(Path.class)))
                .thenReturn(validationResult);

        // Call the method to test
        boolean result = initializer.initializeWithConfig(projectConfigPath, dslConfigPath);

        // Verify the result
        assertTrue(result);
        assertTrue(initializer.isInitialized());

        // Verify that necessary methods were called
        verify(projectConfigLoader).loadAndValidate(eq(projectConfigPath), eq(dslConfigPath), any(Path.class));
        verify(initService).initializeStateStructure();
    }

    @Test
    void initializeWithConfig_validationFailed() throws Exception {
        // Create test files
        Path projectConfigPath = Files.createFile(tempDir.resolve("project.json"));
        Path dslConfigPath = Files.createFile(tempDir.resolve("dsl.json"));

        // Set up validation result with critical errors
        ValidationResult validationResult = new ValidationResult();
        validationResult.addError(new ValidationError(
                "Test error", "Error message", ValidationSeverity.CRITICAL));
        when(configValidator.validateConfiguration(anyString(), anyString(), any(Path.class)))
                .thenReturn(validationResult);

        // Setup projectConfigLoader to return the error result
        when(projectConfigLoader.loadAndValidate(any(), any(), any())).thenReturn(validationResult);

        // Call the method to test
        boolean result = initializer.initializeWithConfig(projectConfigPath, dslConfigPath);

        // Verify the result
        assertFalse(result);
        assertFalse(initializer.isInitialized());
        assertNotNull(initializer.getLastErrorMessage());
        assertTrue(initializer.getLastErrorMessage().contains("Configuration validation failed"));
    }

    @Test
    void initializeWithConfig_projectConfigNotFound() {
        // Use non-existent file
        Path projectConfigPath = tempDir.resolve("nonexistent.json");
        Path dslConfigPath = tempDir.resolve("dsl.json");

        // Call the method to test
        boolean result = initializer.initializeWithConfig(projectConfigPath, dslConfigPath);

        // Verify the result
        assertFalse(result);
        assertEquals("Project configuration file not found: " + projectConfigPath,
                initializer.getLastErrorMessage());
    }

    @Test
    void updateImagePath_success() {
        // Set up mock
        doNothing().when(initService).setBundlePathAndPreProcessImages(anyString());

        // Call the method to test
        initializer.updateImagePath(tempDir.toString());

        // Verify that methods were called
        verify(properties).setImagePath(tempDir.toString());
        verify(initService).setBundlePathAndPreProcessImages(tempDir.toString());
    }

    @Test
    void updateImagePath_invalidPath() {
        // Set up an invalid path
        String invalidPath = tempDir.resolve("nonexistent").toString();

        // Verify that exception is thrown
        assertThrows(IllegalArgumentException.class, () -> initializer.updateImagePath(invalidPath));
    }
}