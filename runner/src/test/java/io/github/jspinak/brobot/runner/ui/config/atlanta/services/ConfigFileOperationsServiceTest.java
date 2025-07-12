package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigFileOperationsServiceTest {
    
    @Mock
    private EventBus eventBus;
    
    @Mock
    private BrobotRunnerProperties runnerProperties;
    
    private ConfigFileOperationsService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new ConfigFileOperationsService(eventBus, runnerProperties);
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testValidateConfigurationFile() {
        // Given
        File jsonFile = new File(tempDir.toFile(), "config.json");
        File yamlFile = new File(tempDir.toFile(), "config.yml");
        File yamlFile2 = new File(tempDir.toFile(), "config.yaml");
        File otherFile = new File(tempDir.toFile(), "config.txt");
        
        // Create the files
        try {
            jsonFile.createNewFile();
            yamlFile.createNewFile();
            yamlFile2.createNewFile();
            otherFile.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test files");
        }
        
        // When/Then
        assertTrue(service.validateConfigurationFile(jsonFile));
        assertTrue(service.validateConfigurationFile(yamlFile));
        assertTrue(service.validateConfigurationFile(yamlFile2));
        assertFalse(service.validateConfigurationFile(otherFile));
        assertFalse(service.validateConfigurationFile(null));
        assertFalse(service.validateConfigurationFile(new File("nonexistent.json")));
    }
    
    @Test
    void testGetConfigPath() {
        // Given
        String expectedPath = "/test/config/path";
        when(runnerProperties.getConfigPath()).thenReturn(expectedPath);
        
        // When
        String actualPath = service.getConfigPath();
        
        // Then
        assertEquals(expectedPath, actualPath);
    }
    
    @Test
    void testUpdateConfigPath() {
        // Given
        String newPath = "/new/config/path";
        
        // When
        service.updateConfigPath(newPath);
        
        // Then
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        LogEvent event = eventCaptor.getValue();
        assertEquals(service, event.getSource());
        assertTrue(event.getMessage().contains("Configuration path updated to: " + newPath));
        assertEquals("Config", event.getCategory());
    }
    
    @Test
    void testOpenConfigFolderWithNonExistentDirectory() {
        // Given
        String nonExistentPath = "/non/existent/path";
        when(runnerProperties.getConfigPath()).thenReturn(nonExistentPath);
        
        // When
        service.openConfigFolder();
        
        // Then
        // Should not publish success log event
        verify(eventBus, never()).publish(argThat(event -> 
            event instanceof LogEvent && ((LogEvent) event).getMessage().contains("Opened configuration folder")
        ));
    }
    
    @Test
    void testOpenConfigFolderWithValidDirectory() {
        // Given
        when(runnerProperties.getConfigPath()).thenReturn(tempDir.toString());
        
        // When
        service.openConfigFolder();
        
        // Then
        // Should attempt to open the folder
        // Note: Actually opening the folder might not work in test environment
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());
        
        boolean foundOpenEvent = eventCaptor.getAllValues().stream()
            .anyMatch(event -> event.getMessage().contains("Opened configuration folder") ||
                              event.getMessage().contains("Failed to open configuration folder"));
        assertTrue(foundOpenEvent);
    }
    
    @Test
    void testValidateConfigurationFileWithMixedCase() {
        // Given
        File jsonFile = new File(tempDir.toFile(), "Config.JSON");
        File yamlFile = new File(tempDir.toFile(), "Config.YML");
        
        // Create the files
        try {
            jsonFile.createNewFile();
            yamlFile.createNewFile();
        } catch (Exception e) {
            fail("Failed to create test files");
        }
        
        // When/Then - Should handle case-insensitive extensions
        assertTrue(service.validateConfigurationFile(jsonFile));
        assertTrue(service.validateConfigurationFile(yamlFile));
    }
}