package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.project.TaskButton;
import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;

class TreeManagementServiceTest {

    private TreeManagementService service;

    @BeforeEach
    void setUp() {
        service = new TreeManagementService();

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testCreateRootItem() {
        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = service.createRootItem("Test Root");

        // Then
        assertNotNull(rootItem);
        assertEquals("Test Root", rootItem.getValue().getName());
        assertEquals(ConfigBrowserPanel.ConfigItemType.ROOT, rootItem.getValue().getType());
        assertTrue(rootItem.isExpanded());
    }

    @Test
    void testClearTree() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = service.createRootItem("Root");
        TreeItem<ConfigBrowserPanel.ConfigItem> child1 = service.createFolderItem("Child1");
        TreeItem<ConfigBrowserPanel.ConfigItem> child2 = service.createFolderItem("Child2");
        rootItem.getChildren().addAll(child1, child2);

        State mockState = mock(State.class);
        when(mockState.getName()).thenReturn("TestState");
        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem = service.createStateItem(mockState);
        service.addStateItem("TestState", stateItem);

        // When
        service.clearTree(rootItem);

        // Then
        assertTrue(rootItem.getChildren().isEmpty());
        assertNull(service.getStateItem("TestState"));
    }

    @Test
    void testStateItemManagement() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem1 = service.createFolderItem("State1");
        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem2 = service.createFolderItem("State2");

        // When
        service.addStateItem("State1", stateItem1);
        service.addStateItem("State2", stateItem2);

        // Then
        assertEquals(stateItem1, service.getStateItem("State1"));
        assertEquals(stateItem2, service.getStateItem("State2"));
        assertNull(service.getStateItem("NonExistent"));
    }

    @Test
    void testCreateFolderItem() {
        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> folderItem =
                service.createFolderItem("Test Folder");

        // Then
        assertNotNull(folderItem);
        assertEquals("Test Folder", folderItem.getValue().getName());
        assertEquals(ConfigBrowserPanel.ConfigItemType.FOLDER, folderItem.getValue().getType());
        assertFalse(folderItem.isExpanded());
    }

    @Test
    void testCreateConfigFileItem() {
        // Given
        Path testPath = Path.of("/test/config.json");

        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> fileItem =
                service.createConfigFileItem(
                        "config.json", ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG, testPath);

        // Then
        assertNotNull(fileItem);
        assertEquals("config.json", fileItem.getValue().getName());
        assertEquals(
                ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG, fileItem.getValue().getType());
        assertEquals(testPath, fileItem.getValue().getData());
    }

    @Test
    void testCreateStateItem() {
        // Given
        State mockState = mock(State.class);
        when(mockState.getName()).thenReturn("Test State");

        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem = service.createStateItem(mockState);

        // Then
        assertNotNull(stateItem);
        assertEquals("Test State", stateItem.getValue().getName());
        assertEquals(ConfigBrowserPanel.ConfigItemType.STATE, stateItem.getValue().getType());
        assertEquals(mockState, stateItem.getValue().getData());
    }

    @Test
    void testCreateStateImageItem() {
        // Given
        StateImage mockStateImage = mock(StateImage.class);
        when(mockStateImage.getName()).thenReturn("Test Image");

        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> imageItem =
                service.createStateImageItem(mockStateImage);

        // Then
        assertNotNull(imageItem);
        assertEquals("Test Image", imageItem.getValue().getName());
        assertEquals(ConfigBrowserPanel.ConfigItemType.STATE_IMAGE, imageItem.getValue().getType());
        assertEquals(mockStateImage, imageItem.getValue().getData());
    }

    @Test
    void testCreateAutomationButtonItem() {
        // Given
        TaskButton mockButton = new TaskButton();
        mockButton.setLabel("Test Button");

        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> buttonItem =
                service.createAutomationButtonItem(mockButton);

        // Then
        assertNotNull(buttonItem);
        assertEquals("Test Button", buttonItem.getValue().getName());
        assertEquals(
                ConfigBrowserPanel.ConfigItemType.AUTOMATION_BUTTON,
                buttonItem.getValue().getType());
        assertEquals(mockButton, buttonItem.getValue().getData());
    }

    @Test
    void testCreateMetadataItem() {
        // When
        TreeItem<ConfigBrowserPanel.ConfigItem> metadataItem =
                service.createMetadataItem("Version: 1.0.0");

        // Then
        assertNotNull(metadataItem);
        assertEquals("Version: 1.0.0", metadataItem.getValue().getName());
        assertEquals(ConfigBrowserPanel.ConfigItemType.METADATA, metadataItem.getValue().getType());
        assertNull(metadataItem.getValue().getData());
    }
}
