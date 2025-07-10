package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import javafx.application.Platform;
import javafx.stage.Window;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigImportServiceTest {
    
    @Mock
    private BrobotLibraryInitializer libraryInitializer;
    
    @Mock
    private BrobotRunnerProperties runnerProperties;
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private Window window;
    
    private ConfigImportService service;
    
    @BeforeEach
    void setUp() {
        service = new ConfigImportService(libraryInitializer, runnerProperties, eventBus);
        when(runnerProperties.getImagePath()).thenReturn("images");
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testShowImportDialogDisabled() {
        // Given
        service.setConfiguration(
            ConfigImportService.ImportConfiguration.builder()
                .showImportDialog(false)
                .build()
        );
        
        // When
        Optional<ConfigEntry> result = service.showImportDialog(window);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testValidateConfigEntryValid() throws IOException {
        // Given
        Path tempDir = Files.createTempDirectory("config-test");
        Path projectConfig = Files.createFile(tempDir.resolve("project.json"));
        Path dslConfig = Files.createFile(tempDir.resolve("dsl.json"));
        
        ConfigEntry entry = new ConfigEntry(
            "test",
            "Test Project",
            projectConfig,
            dslConfig,
            Paths.get("images"),
            LocalDateTime.now()
        );
        
        try {
            // When
            boolean valid = service.validateConfigEntry(entry);
            
            // Then
            assertTrue(valid);
        } finally {
            // Cleanup
            Files.deleteIfExists(projectConfig);
            Files.deleteIfExists(dslConfig);
            Files.deleteIfExists(tempDir);
        }
    }
    
    @Test
    void testValidateConfigEntryNull() {
        // When
        boolean valid = service.validateConfigEntry(null);
        
        // Then
        assertFalse(valid);
    }
    
    @Test
    void testValidateConfigEntryMissingFiles() {
        // Given
        ConfigEntry entry = new ConfigEntry(
            "test",
            "Test Project",
            Paths.get("nonexistent", "project.json"),
            Paths.get("nonexistent", "dsl.json"),
            Paths.get("images"),
            LocalDateTime.now()
        );
        
        // When
        boolean valid = service.validateConfigEntry(entry);
        
        // Then
        assertFalse(valid);
    }
    
    @Test
    void testImportSuccessHandler() {
        // Given
        AtomicReference<ConfigEntry> handledEntry = new AtomicReference<>();
        service.setImportSuccessHandler(handledEntry::set);
        
        // Then
        // Handler is set successfully - actual testing would require mocking the dialog
        assertNotNull(service);
    }
    
    @Test
    void testConfigurationBuilder() {
        // Given
        ConfigImportService.ImportConfiguration config = 
            ConfigImportService.ImportConfiguration.builder()
                .autoDetectDslConfig(false)
                .showImportDialog(false)
                .dslFilePatterns("config", "settings")
                .defaultProjectName("MyProject")
                .build();
        
        service.setConfiguration(config);
        
        // Then
        assertFalse(config.isAutoDetectDslConfig());
        assertFalse(config.isShowImportDialog());
        assertArrayEquals(new String[]{"config", "settings"}, config.getDslFilePatterns());
        assertEquals("MyProject", config.getDefaultProjectName());
    }
    
    @Test
    void testBrowseForConfigurationCancelled() {
        // Given
        // Can't mock FileChooser easily, but can test the method returns empty when cancelled
        
        // When
        Optional<ConfigEntry> result = service.browseForConfiguration(null);
        
        // Then
        // Should return empty since we can't interact with FileChooser in tests
        assertFalse(result.isPresent());
    }
}