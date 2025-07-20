package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigBrowserPanel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigBrowserUIFactoryTest {
    
    private ConfigBrowserUIFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new ConfigBrowserUIFactory();
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testCreateTreeView() {
        // Given
        TreeItem<ConfigBrowserPanel.ConfigItem> rootItem = new TreeItem<>(
            new ConfigBrowserPanel.ConfigItem("Root", ConfigBrowserPanel.ConfigItemType.ROOT)
        );
        
        // When
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = factory.createTreeView(rootItem);
        
        // Then
        assertNotNull(treeView);
        assertEquals(rootItem, treeView.getRoot());
        assertNotNull(treeView.getCellFactory());
    }
    
    @Test
    void testCreateDetailsTextArea() {
        // When
        TextArea textArea = factory.createDetailsTextArea();
        
        // Then
        assertNotNull(textArea);
        assertFalse(textArea.isEditable());
        assertTrue(textArea.isWrapText());
    }
    
    @Test
    void testCreateImagePreview() {
        // When
        ImageView imageView = factory.createImagePreview();
        
        // Then
        assertNotNull(imageView);
        assertEquals(200, imageView.getFitHeight());
        assertEquals(200, imageView.getFitWidth());
        assertTrue(imageView.isPreserveRatio());
    }
    
    @Test
    void testCreateImagePreviewWithCustomSize() {
        // Given
        factory.setConfiguration(
            ConfigBrowserUIFactory.UIConfiguration.builder()
                .imagePreviewHeight(300)
                .imagePreviewWidth(400)
                .build()
        );
        
        // When
        ImageView imageView = factory.createImagePreview();
        
        // Then
        assertEquals(300, imageView.getFitHeight());
        assertEquals(400, imageView.getFitWidth());
    }
    
    @Test
    void testCreatePreviewPanel() {
        // Given
        ImageView imageView = factory.createImagePreview();
        
        // When
        VBox previewPanel = factory.createPreviewPanel(imageView);
        
        // Then
        assertNotNull(previewPanel);
        assertEquals(10, previewPanel.getSpacing());
        assertEquals(new Insets(10), previewPanel.getPadding());
        assertFalse(previewPanel.isVisible());
        assertEquals(2, previewPanel.getChildren().size());
        assertTrue(previewPanel.getChildren().get(0) instanceof Label);
        assertEquals("Preview", ((Label) previewPanel.getChildren().get(0)).getText());
        assertEquals(imageView, previewPanel.getChildren().get(1));
    }
    
    @Test
    void testCreateTreeBox() {
        // Given
        TreeView<ConfigBrowserPanel.ConfigItem> treeView = 
            factory.createTreeView(new TreeItem<>(new ConfigBrowserPanel.ConfigItem("Root", ConfigBrowserPanel.ConfigItemType.ROOT)));
        
        // When
        VBox treeBox = factory.createTreeBox(treeView);
        
        // Then
        assertNotNull(treeBox);
        assertEquals(5, treeBox.getSpacing());
        assertEquals(2, treeBox.getChildren().size());
        assertTrue(treeBox.getChildren().get(0) instanceof Label);
        assertEquals("Configuration Structure", ((Label) treeBox.getChildren().get(0)).getText());
        assertEquals(treeView, treeBox.getChildren().get(1));
    }
    
    @Test
    void testCreateDetailsBox() {
        // Given
        TextArea textArea = factory.createDetailsTextArea();
        
        // When
        VBox detailsBox = factory.createDetailsBox(textArea);
        
        // Then
        assertNotNull(detailsBox);
        assertEquals(5, detailsBox.getSpacing());
        assertEquals(2, detailsBox.getChildren().size());
        assertTrue(detailsBox.getChildren().get(0) instanceof Label);
        assertEquals("Details", ((Label) detailsBox.getChildren().get(0)).getText());
        assertEquals(textArea, detailsBox.getChildren().get(1));
        assertEquals(Priority.ALWAYS, VBox.getVgrow(textArea));
    }
    
    @Test
    void testCreateSplitPane() {
        // Given
        VBox treeBox = new VBox();
        VBox detailsBox = new VBox();
        
        // When
        SplitPane splitPane = factory.createSplitPane(treeBox, detailsBox);
        
        // Then
        assertNotNull(splitPane);
        assertEquals(2, splitPane.getItems().size());
        assertEquals(treeBox, splitPane.getItems().get(0));
        assertEquals(detailsBox, splitPane.getItems().get(1));
        assertEquals(0.4, splitPane.getDividerPositions()[0], 0.01);
    }
    
    @Test
    void testCreateSearchField() {
        // When
        TextField searchField = factory.createSearchField();
        
        // Then
        assertNotNull(searchField);
        assertEquals("Search configuration...", searchField.getPromptText());
    }
    
    @Test
    void testCreateToolbar() {
        // Given
        TextField searchField = factory.createSearchField();
        
        // When
        HBox toolbar = factory.createToolbar(searchField);
        
        // Then
        assertNotNull(toolbar);
        assertEquals(10, toolbar.getSpacing());
        assertEquals(new Insets(5), toolbar.getPadding());
        assertEquals(2, toolbar.getChildren().size());
        assertTrue(toolbar.getChildren().get(0) instanceof Label);
        assertEquals("Search:", ((Label) toolbar.getChildren().get(0)).getText());
        assertEquals(searchField, toolbar.getChildren().get(1));
        assertEquals(Priority.ALWAYS, HBox.getHgrow(searchField));
    }
    
    @Test
    void testFullConfiguration() {
        // Given
        ConfigBrowserUIFactory.UIConfiguration config = 
            ConfigBrowserUIFactory.UIConfiguration.builder()
                .imagePreviewHeight(250)
                .imagePreviewWidth(350)
                .splitPaneDividerPosition(0.3)
                .previewPanelPadding(new Insets(20))
                .toolbarPadding(new Insets(10))
                .build();
        
        factory.setConfiguration(config);
        
        // When - Create all components
        ImageView imageView = factory.createImagePreview();
        assertEquals(250, imageView.getFitHeight());
        assertEquals(350, imageView.getFitWidth());
        
        VBox previewPanel = factory.createPreviewPanel(imageView);
        assertEquals(new Insets(20), previewPanel.getPadding());
        
        VBox treeBox = new VBox();
        VBox detailsBox = new VBox();
        SplitPane splitPane = factory.createSplitPane(treeBox, detailsBox);
        assertEquals(0.3, splitPane.getDividerPositions()[0], 0.01);
        
        HBox toolbar = factory.createToolbar(factory.createSearchField());
        assertEquals(new Insets(10), toolbar.getPadding());
    }
}