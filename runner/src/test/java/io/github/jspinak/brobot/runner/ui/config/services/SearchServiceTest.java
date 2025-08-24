package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceTest {
    
    private SearchService service;
    
    @BeforeEach
    void setUp() {
        service = new SearchService();
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testSearchTreeWithNull() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        
        // When
        SearchService.SearchResult result = service.searchTree(rootItem, null);
        
        // Then
        assertFalse(result.isFound());
        assertTrue(result.getMatchingItems().isEmpty());
    }
    
    @Test
    void testSearchTreeWithEmptyString() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        
        // When
        SearchService.SearchResult result = service.searchTree(rootItem, "");
        
        // Then
        assertFalse(result.isFound());
        assertTrue(result.getMatchingItems().isEmpty());
        
        // Verify tree is reset to default expansion
        assertTrue(rootItem.isExpanded());
        assertTrue(rootItem.getChildren().get(0).isExpanded()); // Folder should be expanded
        assertFalse(rootItem.getChildren().get(1).isExpanded()); // File should not be expanded
    }
    
    @Test
    void testSearchTreeWithMatch() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        
        // When
        SearchService.SearchResult result = service.searchTree(rootItem, "state");
        
        // Then
        assertTrue(result.isFound());
        assertEquals(2, result.getMatchingItems().size());
        
        // Verify matching items
        assertTrue(result.getMatchingItems().stream()
            .anyMatch(item -> item.getValue().getName().equals("States")));
        assertTrue(result.getMatchingItems().stream()
            .anyMatch(item -> item.getValue().getName().equals("TestState")));
        
        // Verify expansion - path to matches should be expanded
        assertTrue(rootItem.isExpanded());
        assertTrue(rootItem.getChildren().get(0).isExpanded()); // States folder expanded
    }
    
    @Test
    void testSearchTreeCaseInsensitive() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        
        // When
        SearchService.SearchResult result = service.searchTree(rootItem, "BUTTON");
        
        // Then
        assertTrue(result.isFound());
        assertEquals(1, result.getMatchingItems().size());
        assertEquals("Button1", result.getMatchingItems().get(0).getValue().getName());
    }
    
    @Test
    void testResetTreeExpansion() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        
        // Expand all items
        expandAll(rootItem);
        
        // When
        service.resetTreeExpansion(rootItem);
        
        // Then
        assertTrue(rootItem.isExpanded()); // ROOT should be expanded
        assertTrue(rootItem.getChildren().get(0).isExpanded()); // FOLDER should be expanded
        assertFalse(rootItem.getChildren().get(1).isExpanded()); // Non-folder should not be expanded
        assertFalse(rootItem.getChildren().get(0).getChildren().get(0).isExpanded()); // STATE should not be expanded
    }
    
    @Test
    void testHighlightFirstMatch() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);
        
        SearchService.SearchResult result = service.searchTree(rootItem, "button");
        
        // When
        service.highlightFirstMatch(result, treeView);
        
        // Then
        TreeItem<ConfigBrowserPanel.ConfigItem> selected = treeView.getSelectionModel().getSelectedItem();
        assertNotNull(selected);
        assertEquals("Button1", selected.getValue().getName());
        
        // Verify parents are expanded
        TreeItem<ConfigBrowserPanel.ConfigItem> parent = selected.getParent();
        while (parent != null) {
            assertTrue(parent.isExpanded());
            parent = parent.getParent();
        }
    }
    
    @Test
    void testHighlightFirstMatchWithNoMatches() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = createTestTree();
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = new TreeView<>(rootItem);
        
        SearchService.SearchResult result = service.searchTree(rootItem, "nonexistent");
        
        // When
        service.highlightFirstMatch(result, treeView);
        
        // Then
        assertNull(treeView.getSelectionModel().getSelectedItem());
    }
    
    private TreeItem<ConfigBrowserPanel.ConfigItem> createTestTree() {
        TreeItem<ConfigBrowserPanel.ConfigItem> root = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("Root", ConfigBrowserPanel.ConfigItemType.ROOT)
        );
        
        TreeItem<ConfigBrowserPanel.ConfigItem> statesFolder = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("States", ConfigBrowserPanel.ConfigItemType.FOLDER)
        );
        
        TreeItem<ConfigBrowserPanel.ConfigItem> state = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("TestState", ConfigBrowserPanel.ConfigItemType.STATE)
        );
        
        TreeItem<ConfigBrowserPanel.ConfigItem> button = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("Button1", ConfigBrowserPanel.ConfigItemType.AUTOMATION_BUTTON)
        );
        
        TreeItem<ConfigBrowserPanel.ConfigItem> configFile = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("config.json", ConfigBrowserPanel.ConfigItemType.PROJECT_CONFIG)
        );
        
        statesFolder.getChildren().add(state);
        root.getChildren().addAll(statesFolder, configFile, button);
        
        return root;
    }
    
    private void expandAll(TreeItem<ConfigBrowserPanel.ConfigItem> item) {
        item.setExpanded(true);
        for (TreeItem<ConfigBrowserPanel.ConfigItem> child : item.getChildren()) {
            expandAll(child);
        }
    }
}