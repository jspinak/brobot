package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectionServiceTest {
    
    private SelectionService service;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        service = new SelectionService();
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testProcessSelectionWithNull() {
        // When
        String result = service.processSelection(null);
        
        // Then
        assertEquals("", result);
    }
    
    @Test
    void testProcessSelectionWithNullValue() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> item = new TreeItem<>(null);
        
        // When
        String result = service.processSelection(item);
        
        // Then
        assertEquals("", result);
    }
    
    @Test
    void testProcessConfigFile() throws IOException {
        // Given
        Path configFile = tempDir.resolve("config.json");
        String content = "{\n  \"test\": \"config\"\n}";
        Files.writeString(configFile, content);
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "config.json", ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG
        );
        configItem.setData(configFile);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertEquals(content, result);
    }
    
    @Test
    void testProcessConfigFileWithoutPath() {
        // Given
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "config.json", ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG
        );
        configItem.setData("not a path");
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertEquals("File path not available", result);
    }
    
    @Test
    void testProcessState() {
        // Given
        State mockState = mock(State.class);
        when(mockState.getName()).thenReturn("TestState");
        when(mockState.getId()).thenReturn(123L);
        
        StateImage mockImage1 = mock(StateImage.class);
        when(mockImage1.getName()).thenReturn("Image1");
        StateImage mockImage2 = mock(StateImage.class);
        when(mockImage2.getName()).thenReturn("Image2");
        
        Set<StateImage> stateImages = new HashSet<>(Arrays.asList(mockImage1, mockImage2));
        when(mockState.getStateImages()).thenReturn(stateImages);
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "TestState", ConfigBrowserPanel.ConfigItemType.STATE
        );
        configItem.setData(mockState);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertTrue(result.contains("State: TestState"));
        assertTrue(result.contains("ID: 123"));
        assertTrue(result.contains("Images: 2"));
        assertTrue(result.contains("Image1") || result.contains("Image2")); // Set order not guaranteed
    }
    
    @Test
    void testProcessStateImage() {
        // Given
        StateImage mockStateImage = mock(StateImage.class);
        when(mockStateImage.getName()).thenReturn("TestImage");
        
        Pattern mockPattern = mock(Pattern.class);
        when(mockPattern.getImgpath()).thenReturn("test/image.png");
        when(mockStateImage.getPatterns()).thenReturn(Collections.singletonList(mockPattern));
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "TestImage", ConfigBrowserPanel.ConfigItemType.STATE_IMAGE
        );
        configItem.setData(mockStateImage);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertTrue(result.contains("State Image: TestImage"));
        assertTrue(result.contains("Path: test/image.png"));
    }
    
    @Test
    void testProcessAutomationButton() {
        // Given
        TaskButton button = new TaskButton();
        button.setLabel("Test Button");
        button.setFunctionName("testFunction");
        button.setCategory("Test Category");
        button.setConfirmationRequired(true);
        button.setTooltip("Test tooltip");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        parameters.put("param2", 42);
        button.setParameters(parameters);
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "Test Button", ConfigBrowserPanel.ConfigItemType.AUTOMATION_BUTTON
        );
        configItem.setData(button);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertTrue(result.contains("Button: Test Button"));
        assertTrue(result.contains("Function: testFunction"));
        assertTrue(result.contains("Category: Test Category"));
        assertTrue(result.contains("Confirmation Required: true"));
        assertTrue(result.contains("param1: value1"));
        assertTrue(result.contains("param2: 42"));
        assertTrue(result.contains("Tooltip: Test tooltip"));
    }
    
    @Test
    void testProcessDefaultCase() {
        // Given
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "Test", ConfigBrowserPanel.ConfigItemType.FOLDER
        );
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String result = service.processSelection(treeItem);
        
        // Then
        assertEquals("Select an item to view details", result);
    }
    
    @Test
    void testGetImageNameForPreview() {
        // Given
        StateImage mockStateImage = mock(StateImage.class);
        Pattern mockPattern = mock(Pattern.class);
        when(mockPattern.getImgpath()).thenReturn("test/image");
        when(mockStateImage.getPatterns()).thenReturn(Collections.singletonList(mockPattern));
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "TestImage", ConfigBrowserPanel.ConfigItemType.STATE_IMAGE
        );
        configItem.setData(mockStateImage);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String imageName = service.getImageNameForPreview(treeItem);
        
        // Then
        assertEquals("test/image", imageName);
    }
    
    @Test
    void testGetImageNameForPreviewWithNonImageItem() {
        // Given
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "Test", ConfigBrowserPanel.ConfigItemType.FOLDER
        );
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String imageName = service.getImageNameForPreview(treeItem);
        
        // Then
        assertNull(imageName);
    }
    
    @Test
    void testGetImageNameForPreviewWithEmptyPatterns() {
        // Given
        StateImage mockStateImage = mock(StateImage.class);
        when(mockStateImage.getPatterns()).thenReturn(Collections.emptyList());
        
        ConfigBrowserPanel.ConfigItem configItem = new ConfigBrowserPanel.ConfigItem(
            "TestImage", ConfigBrowserPanel.ConfigItemType.STATE_IMAGE
        );
        configItem.setData(mockStateImage);
        TreeItem<ConfigBrowserPanel.ConfigItem> treeItem = new TreeItem<>(configItem);
        
        // When
        String imageName = service.getImageNameForPreview(treeItem);
        
        // Then
        assertNull(imageName);
    }
}