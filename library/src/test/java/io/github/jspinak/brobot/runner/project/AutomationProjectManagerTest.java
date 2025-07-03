package io.github.jspinak.brobot.runner.project;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationProjectManagerTest {

    @Mock
    private ConfigurationParser configurationParser;
    
    @Mock
    private AutomationProject automationProject;
    
    private AutomationProjectManager projectManager;
    
    @BeforeEach
    void setUp() {
        projectManager = new AutomationProjectManager(configurationParser);
    }
    
    @Test
    void testConstructor() {
        // Verify
        assertNotNull(projectManager);
        assertNull(projectManager.getActiveProject());
    }
    
    @Test
    void testLoadProject_Success() throws ConfigurationException {
        // Setup
        String projectJson = "{\"name\": \"TestProject\", \"states\": []}";
        when(configurationParser.convertJson(projectJson, AutomationProject.class))
            .thenReturn(automationProject);
        
        // Execute
        projectManager.loadProject(projectJson);
        
        // Verify
        assertEquals(automationProject, projectManager.getActiveProject());
        verify(configurationParser).convertJson(projectJson, AutomationProject.class);
    }
    
    @Test
    void testLoadProject_InvalidJson() throws ConfigurationException {
        // Setup
        String invalidJson = "{invalid json}";
        when(configurationParser.convertJson(invalidJson, AutomationProject.class))
            .thenThrow(new ConfigurationException("Invalid JSON"));
        
        // Execute & Verify
        assertThrows(ConfigurationException.class, () -> projectManager.loadProject(invalidJson));
        assertNull(projectManager.getActiveProject());
        verify(configurationParser).convertJson(invalidJson, AutomationProject.class);
    }
    
    @Test
    void testLoadProject_ReplacesExistingProject() throws ConfigurationException {
        // Setup
        AutomationProject firstProject = mock(AutomationProject.class);
        AutomationProject secondProject = mock(AutomationProject.class);
        
        String firstJson = "{\"name\": \"FirstProject\"}";
        String secondJson = "{\"name\": \"SecondProject\"}";
        
        when(configurationParser.convertJson(firstJson, AutomationProject.class))
            .thenReturn(firstProject);
        when(configurationParser.convertJson(secondJson, AutomationProject.class))
            .thenReturn(secondProject);
        
        // Execute
        projectManager.loadProject(firstJson);
        assertEquals(firstProject, projectManager.getActiveProject());
        
        projectManager.loadProject(secondJson);
        
        // Verify
        assertEquals(secondProject, projectManager.getActiveProject());
        verify(configurationParser).convertJson(firstJson, AutomationProject.class);
        verify(configurationParser).convertJson(secondJson, AutomationProject.class);
    }
    
    @Test
    void testLoadProject_MaintainsPreviousOnFailure() throws ConfigurationException {
        // Setup
        String validJson = "{\"name\": \"ValidProject\"}";
        String invalidJson = "{\"name\": \"InvalidProject\"}";
        
        when(configurationParser.convertJson(validJson, AutomationProject.class))
            .thenReturn(automationProject);
        when(configurationParser.convertJson(invalidJson, AutomationProject.class))
            .thenThrow(new ConfigurationException("Parse error"));
        
        // Execute
        projectManager.loadProject(validJson);
        AutomationProject previousProject = projectManager.getActiveProject();
        
        // Try to load invalid project
        assertThrows(ConfigurationException.class, () -> projectManager.loadProject(invalidJson));
        
        // Verify - previous project should still be active
        assertEquals(previousProject, projectManager.getActiveProject());
        assertEquals(automationProject, projectManager.getActiveProject());
    }
    
    @Test
    void testSetActiveProject() {
        // Execute
        projectManager.setActiveProject(automationProject);
        
        // Verify
        assertEquals(automationProject, projectManager.getActiveProject());
    }
    
    @Test
    void testGetActiveProject_InitiallyNull() {
        // Verify
        assertNull(projectManager.getActiveProject());
    }
    
    @Test
    void testLoadProject_NullJson() throws ConfigurationException {
        // Setup
        when(configurationParser.convertJson((String) null, AutomationProject.class))
            .thenThrow(new ConfigurationException("Null JSON"));
        
        // Execute & Verify
        assertThrows(ConfigurationException.class, () -> projectManager.loadProject(null));
    }
    
    @Test
    void testLoadProject_EmptyJson() throws ConfigurationException {
        // Setup
        String emptyJson = "";
        when(configurationParser.convertJson(emptyJson, AutomationProject.class))
            .thenThrow(new ConfigurationException("Empty JSON"));
        
        // Execute & Verify
        assertThrows(ConfigurationException.class, () -> projectManager.loadProject(emptyJson));
    }
}