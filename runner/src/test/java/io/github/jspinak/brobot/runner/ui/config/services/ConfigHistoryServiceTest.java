package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigHistoryServiceTest {
    
    @Mock
    private ApplicationConfig appConfig;
    
    private ConfigHistoryService service;
    
    @BeforeEach
    void setUp() {
        service = new ConfigHistoryService(appConfig);
    }
    
    @Test
    void testLoadRecentConfigurationsWithEmptyHistory() {
        // Given
        when(appConfig.getString("recentConfigurations", null)).thenReturn(null);
        
        // When
        List<ConfigEntry> configs = service.loadRecentConfigurations();
        
        // Then
        assertNotNull(configs);
        assertEquals(3, configs.size()); // Default demo configs
    }
    
    @Test
    void testAddRecentConfiguration() {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Test Project 1");
        
        // When
        service.addRecentConfiguration(entry);
        
        // Then
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertEquals(1, configs.size());
        assertEquals(entry, configs.get(0));
        verify(appConfig).setString(eq("recentConfigurations"), anyString());
    }
    
    @Test
    void testAddDuplicateConfiguration() {
        // Given
        ConfigEntry entry1 = createTestEntry("test1", "Test Project 1");
        ConfigEntry entry2 = createTestEntry("test1", "Test Project 1 Updated");
        
        // When
        service.addRecentConfiguration(entry1);
        service.addRecentConfiguration(entry2);
        
        // Then
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertEquals(1, configs.size());
        assertEquals("Test Project 1 Updated", configs.get(0).getProject());
    }
    
    @Test
    void testMaxRecentConfigs() {
        // Given
        service.setConfiguration(
            ConfigHistoryService.HistoryConfiguration.builder()
                .maxRecentConfigs(3)
                .autoSave(true)
                .build()
        );
        
        // When - Add 5 entries
        for (int i = 1; i <= 5; i++) {
            service.addRecentConfiguration(createTestEntry("test" + i, "Project " + i));
        }
        
        // Then - Should only keep the most recent 3
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertEquals(3, configs.size());
        assertEquals("Project 5", configs.get(0).getProject());
        assertEquals("Project 4", configs.get(1).getProject());
        assertEquals("Project 3", configs.get(2).getProject());
    }
    
    @Test
    void testRemoveConfiguration() {
        // Given
        ConfigEntry entry1 = createTestEntry("test1", "Project 1");
        ConfigEntry entry2 = createTestEntry("test2", "Project 2");
        service.addRecentConfiguration(entry1);
        service.addRecentConfiguration(entry2);
        
        // When
        boolean removed = service.removeConfiguration(entry1);
        
        // Then
        assertTrue(removed);
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertEquals(1, configs.size());
        assertEquals(entry2, configs.get(0));
    }
    
    @Test
    void testRemoveNonExistentConfiguration() {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");
        
        // When
        boolean removed = service.removeConfiguration(entry);
        
        // Then
        assertFalse(removed);
    }
    
    @Test
    void testUpdateConfigurationAccess() {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");
        LocalDateTime originalTime = entry.getLastModified();
        service.addRecentConfiguration(entry);
        
        // Sleep to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // When
        service.updateConfigurationAccess(entry);
        
        // Then
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertTrue(configs.get(0).getLastModified().isAfter(originalTime));
    }
    
    @Test
    void testUpdateConfigurationAccessWithMoveToTop() {
        // Given
        service.setConfiguration(
            ConfigHistoryService.HistoryConfiguration.builder()
                .moveToTopOnLoad(true)
                .autoSave(false)
                .build()
        );
        
        ConfigEntry entry1 = createTestEntry("test1", "Project 1");
        ConfigEntry entry2 = createTestEntry("test2", "Project 2");
        service.addRecentConfiguration(entry1);
        service.addRecentConfiguration(entry2);
        
        // When
        service.updateConfigurationAccess(entry1);
        
        // Then
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertEquals(entry1, configs.get(0));
        assertEquals(entry2, configs.get(1));
    }
    
    @Test
    void testFindConfiguration() {
        // Given
        ConfigEntry entry1 = createTestEntry("test1", "Project 1");
        ConfigEntry entry2 = createTestEntry("test2", "Project 2");
        service.addRecentConfiguration(entry1);
        service.addRecentConfiguration(entry2);
        
        // When
        Optional<ConfigEntry> found = service.findConfiguration(
            entry1.getProjectConfigPath(),
            entry1.getDslConfigPath()
        );
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(entry1, found.get());
    }
    
    @Test
    void testFindConfigurationNotFound() {
        // Given
        Path projectPath = Paths.get("config", "nonexistent.json");
        Path dslPath = Paths.get("config", "nonexistent-dsl.json");
        
        // When
        Optional<ConfigEntry> found = service.findConfiguration(projectPath, dslPath);
        
        // Then
        assertFalse(found.isPresent());
    }
    
    @Test
    void testClearRecentConfigurations() {
        // Given
        service.addRecentConfiguration(createTestEntry("test1", "Project 1"));
        service.addRecentConfiguration(createTestEntry("test2", "Project 2"));
        
        // When
        service.clearRecentConfigurations();
        
        // Then
        List<ConfigEntry> configs = service.getRecentConfigurations();
        assertTrue(configs.isEmpty());
        verify(appConfig, atLeastOnce()).setString(eq("recentConfigurations"), anyString());
    }
    
    @Test
    void testAutoSaveDisabled() {
        // Given
        service.setConfiguration(
            ConfigHistoryService.HistoryConfiguration.builder()
                .autoSave(false)
                .build()
        );
        reset(appConfig);
        
        // When
        service.addRecentConfiguration(createTestEntry("test1", "Project 1"));
        
        // Then
        verify(appConfig, never()).setString(anyString(), anyString());
    }
    
    private ConfigEntry createTestEntry(String name, String project) {
        Path projectConfigPath = Paths.get("config", name + ".json");
        Path dslConfigPath = Paths.get("config", name + "-dsl.json");
        Path imagePath = Paths.get("images");
        
        return new ConfigEntry(
            name,
            project,
            projectConfigPath,
            dslConfigPath,
            imagePath,
            LocalDateTime.now()
        );
    }
}