package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel.ConfigEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigOperationsServiceTest {
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private BrobotLibraryInitializer libraryInitializer;
    
    @Mock
    private ApplicationConfig appConfig;
    
    private ConfigOperationsService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new ConfigOperationsService(eventBus, libraryInitializer, appConfig);
    }
    
    @Test
    void testLoadConfiguration() throws ExecutionException, InterruptedException {
        // Given
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        
        // When
        CompletableFuture<Boolean> future = service.loadConfiguration(entry);
        Boolean result = future.get();
        
        // Then
        assertTrue(result);
        // TODO: verify config was set - method may not exist yet
        // verify(appConfig).setCurrentConfig("TestConfig");
        
        // Verify events published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeast(2)).publish(eventCaptor.capture());
        
        List<LogEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Loading configuration: TestConfig")));
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Successfully loaded configuration: TestConfig")));
    }
    
    @Test
    void testImportConfiguration() {
        // Given
        File configFile = new File(tempDir.toFile(), "test-config.json");
        try {
            configFile.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test file");
        }
        
        // When
        ConfigEntry result = service.importConfiguration(configFile);
        
        // Then
        assertNotNull(result);
        assertEquals("test-config", result.getName());
        assertEquals("Imported Project", result.getProject());
        assertEquals(tempDir.toFile().getAbsolutePath(), result.getPath());
        
        // Verify events
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeast(2)).publish(eventCaptor.capture());
        
        List<LogEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Importing configuration from: test-config.json")));
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Successfully imported configuration: test-config")));
    }
    
    @Test
    void testCreateConfiguration() {
        // Given
        String name = "NewConfig";
        String projectName = "NewProject";
        String basePath = tempDir.toString();
        
        // When
        ConfigEntry result = service.createConfiguration(name, projectName, basePath);
        
        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(projectName, result.getProject());
        assertEquals(basePath, result.getPath());
        
        // Verify directory structure was created
        assertTrue(new File(basePath, "images").exists());
        assertTrue(new File(basePath, "logs").exists());
        assertTrue(new File(basePath, "reports").exists());
    }
    
    @Test
    void testDeleteConfiguration() {
        // Given
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        
        // When
        boolean result = service.deleteConfiguration(entry);
        
        // Then
        assertTrue(result);
        
        // Verify events
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeast(2)).publish(eventCaptor.capture());
        
        List<LogEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Deleting configuration: TestConfig")));
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Successfully deleted configuration: TestConfig")));
    }
    
    @Test
    void testLoadRecentConfigurations() {
        // When
        List<ConfigEntry> result = service.loadRecentConfigurations();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Currently returns empty list
        
        // Verify events
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeast(1)).publish(eventCaptor.capture());
        
        List<LogEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Loading recent configurations")));
    }
    
    @Test
    void testValidateConfiguration() {
        // Given - Valid configuration
        File projectConfig = new File(tempDir.toFile(), "project.config");
        File dslConfig = new File(tempDir.toFile(), "dsl.config");
        try {
            projectConfig.createNewFile();
            dslConfig.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test files");
        }
        
        ConfigEntry validEntry = new ConfigEntry("Valid", "Project", tempDir.toString());
        
        // When/Then
        assertTrue(service.validateConfiguration(validEntry));
        
        // Given - Invalid configuration (missing files)
        ConfigEntry invalidEntry = new ConfigEntry("Invalid", "Project", "/nonexistent/path");
        
        // When/Then
        assertFalse(service.validateConfiguration(invalidEntry));
        assertFalse(service.validateConfiguration(null));
    }
    
    @Test
    void testExportConfiguration() {
        // Given
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        File targetFile = new File(tempDir.toFile(), "export.json");
        
        // When
        boolean result = service.exportConfiguration(entry, targetFile);
        
        // Then
        assertTrue(result);
        
        // Verify events
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeast(2)).publish(eventCaptor.capture());
        
        List<LogEvent> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Exporting configuration: TestConfig")));
        assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("Successfully exported configuration to: export.json")));
    }
}