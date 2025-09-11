package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;

@ExtendWith(MockitoExtension.class)
class NavigationServiceTest {

    @Mock private TreeManagementService treeManagementService;

    private NavigationService service;

    @BeforeEach
    void setUp() {
        service = new NavigationService(treeManagementService);

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testNavigateToStateSuccess() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        TreeItem<ConfigBrowserPanel.ConfigItem> stateItem =
                rootItem.getChildren().get(0).getChildren().get(0);
        when(treeManagementService.getStateItem("TestState")).thenReturn(stateItem);

        // When
        boolean result = service.navigateToState("TestState", treeView);

        // Then
        assertTrue(result);
        assertEquals(stateItem, treeView.getSelectionModel().getSelectedItem());

        // Verify parents are expanded
        assertTrue(stateItem.getParent().isExpanded());
        assertTrue(rootItem.isExpanded());
    }

    @Test
    void testNavigateToStateNotFound() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        when(treeManagementService.getStateItem("NonExistent")).thenReturn(null);

        // When
        boolean result = service.navigateToState("NonExistent", treeView);

        // Then
        assertFalse(result);
        assertNull(treeView.getSelectionModel().getSelectedItem());
    }

    @Test
    void testNavigateToItem() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        TreeItem<ConfigBrowserPanel.ConfigItem> targetItem =
                rootItem.getChildren().get(1); // config.json

        // When
        service.navigateToItem(targetItem, treeView);

        // Then
        assertEquals(targetItem, treeView.getSelectionModel().getSelectedItem());
        assertTrue(rootItem.isExpanded());
    }

    @Test
    void testNavigateToItemNull() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        // When
        service.navigateToItem(null, treeView);

        // Then
        assertNull(treeView.getSelectionModel().getSelectedItem());
    }

    @Test
    void testNavigateToFirstItemOfTypeFound() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        // When
        boolean result =
                service.navigateToFirstItemOfType(
                        ConfigBrowserPanel.ConfigItemType.STATE, rootItem, treeView);

        // Then
        assertTrue(result);
        TreeItem<ConfigBrowserPanel.ConfigItem> selected =
                treeView.getSelectionModel().getSelectedItem();
        assertNotNull(selected);
        assertEquals(ConfigBrowserPanel.ConfigItemType.STATE, selected.getValue().getType());
        assertEquals("TestState", selected.getValue().getName());
    }

    @Test
    void testNavigateToFirstItemOfTypeNotFound() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        // When
        boolean result =
                service.navigateToFirstItemOfType(
                        ConfigBrowserPanel.ConfigItemType.METADATA, rootItem, treeView);

        // Then
        assertFalse(result);
        assertNull(treeView.getSelectionModel().getSelectedItem());
    }

    @Test
    void testNavigateToDeepItem() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);

        // Add a deeply nested item
        TreeItem<ConfigBrowserPanel.ConfigItem> deepFolder =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "Deep", ConfigBrowserPanel.ConfigItemType.FOLDER));
        TreeItem<ConfigBrowserPanel.ConfigItem> deeperFolder =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "Deeper", ConfigBrowserPanel.ConfigItemType.FOLDER));
        TreeItem<ConfigBrowserPanel.ConfigItem> deepestItem =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "Deepest", ConfigBrowserPanel.ConfigItemType.METADATA));

        deeperFolder.getChildren().add(deepestItem);
        deepFolder.getChildren().add(deeperFolder);
        rootItem.getChildren().add(deepFolder);

        // All should be collapsed initially
        deepFolder.setExpanded(false);
        deeperFolder.setExpanded(false);

        // When
        service.navigateToItem(deepestItem, treeView);

        // Then
        assertEquals(deepestItem, treeView.getSelectionModel().getSelectedItem());

        // All parents should be expanded
        assertTrue(deeperFolder.isExpanded());
        assertTrue(deepFolder.isExpanded());
        assertTrue(rootItem.isExpanded());
    }

    private TreeItem<ConfigBrowserPanel.ConfigItem> createTestTree() {
        TreeItem<ConfigBrowserPanel.ConfigItem> root =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "Root", ConfigBrowserPanel.ConfigItemType.ROOT));

        TreeItem<ConfigBrowserPanel.ConfigItem> statesFolder =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "States", ConfigBrowserPanel.ConfigItemType.FOLDER));

        TreeItem<ConfigBrowserPanel.ConfigItem> state =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "TestState", ConfigBrowserPanel.ConfigItemType.STATE));

        TreeItem<ConfigBrowserPanel.ConfigItem> configFile =
                new TreeItem<>(
                        new ConfigBrowserPanel.ConfigItem(
                                "config.json", ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG));

        statesFolder.getChildren().add(state);
        root.getChildren().addAll(statesFolder, configFile);

        return root;
    }
}
