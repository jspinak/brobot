package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtlantaConfigUIFactoryTest {
    
    private AtlantaConfigUIFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new AtlantaConfigUIFactory();
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testCreateActionBar() {
        // When
        HBox actionBar = factory.createActionBar();
        
        // Then
        assertNotNull(actionBar);
        assertTrue(actionBar.getStyleClass().contains("action-bar"));
        assertEquals(Pos.CENTER_LEFT, actionBar.getAlignment());
        assertEquals(8, actionBar.getSpacing());
    }
    
    @Test
    void testCreatePrimaryButton() {
        // When
        BrobotButton button = factory.createPrimaryButton("Test Button");
        
        // Then
        assertNotNull(button);
        assertEquals("Test Button", button.getText());
        // BrobotButton.primary() should add appropriate styles
    }
    
    @Test
    void testCreateSecondaryButton() {
        // When
        BrobotButton button = factory.createSecondaryButton("Test Button");
        
        // Then
        assertNotNull(button);
        assertEquals("Test Button", button.getText());
        // BrobotButton.secondary() should add appropriate styles
    }
    
    @Test
    void testCreateButton() {
        // When
        Button button = factory.createButton("Test", "custom", "style");
        
        // Then
        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertTrue(button.getStyleClass().contains("custom"));
        assertTrue(button.getStyleClass().contains("style"));
    }
    
    @Test
    void testCreateSplitLayout() {
        // When
        HBox splitLayout = factory.createSplitLayout();
        
        // Then
        assertNotNull(splitLayout);
        assertTrue(splitLayout.getStyleClass().contains("split-layout"));
        assertEquals(24, splitLayout.getSpacing());
    }
    
    @Test
    void testCreateCard() {
        // When
        AtlantaCard card = factory.createCard("Test Card", 500, "test-card");
        
        // Then
        assertNotNull(card);
        assertTrue(card.getStyleClass().contains("test-card"));
        assertEquals(500, card.getMinWidth());
        assertTrue(card.isExpand());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(card));
    }
    
    @Test
    void testCreateSearchBar() {
        // Given
        TextField searchField = new TextField();
        ComboBox<Integer> itemsPerPage = new ComboBox<>();
        
        // When
        HBox searchBar = factory.createSearchBar(searchField, itemsPerPage);
        
        // Then
        assertNotNull(searchBar);
        assertTrue(searchBar.getStyleClass().contains("search-bar"));
        assertEquals(Pos.CENTER_LEFT, searchBar.getAlignment());
        assertEquals(12, searchBar.getSpacing());
        assertEquals(3, searchBar.getChildren().size());
        
        // Verify search field configuration
        assertTrue(searchField.getStyleClass().contains("search-input"));
        assertEquals("Search configurations...", searchField.getPromptText());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(searchField));
    }
    
    @Test
    void testCreateTableContent() {
        // When
        VBox tableContent = factory.createTableContent();
        
        // Then
        assertNotNull(tableContent);
        assertEquals(16, tableContent.getSpacing());
    }
    
    @Test
    void testCreateSpacer() {
        // When
        Region spacer = factory.createSpacer();
        
        // Then
        assertNotNull(spacer);
        assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer));
    }
    
    @Test
    void testCreateLabel() {
        // When
        Label label = factory.createLabel("Test Label", "style1", "style2");
        
        // Then
        assertNotNull(label);
        assertEquals("Test Label", label.getText());
        assertTrue(label.getStyleClass().contains("style1"));
        assertTrue(label.getStyleClass().contains("style2"));
    }
    
    @Test
    void testCreateItemsPerPageCombo() {
        // When
        ComboBox<Integer> combo = factory.createItemsPerPageCombo();
        
        // Then
        assertNotNull(combo);
        assertTrue(combo.getStyleClass().contains("select"));
        assertEquals(3, combo.getItems().size());
        assertTrue(combo.getItems().contains(25));
        assertTrue(combo.getItems().contains(50));
        assertTrue(combo.getItems().contains(100));
        assertEquals(25, combo.getValue());
    }
    
    @Test
    void testCreateSearchField() {
        // When
        TextField searchField = factory.createSearchField();
        
        // Then
        assertNotNull(searchField);
        assertTrue(searchField.getStyleClass().contains("search-input"));
        assertEquals("Search configurations...", searchField.getPromptText());
        assertEquals(300, searchField.getPrefWidth());
    }
    
    @Test
    void testCreateActionBarComponents() {
        // When
        AtlantaConfigUIFactory.ActionBarComponents components = 
            factory.createActionBarComponents("/test/config/path");
        
        // Then
        assertNotNull(components);
        assertNotNull(components.getNewConfigBtn());
        assertNotNull(components.getImportBtn());
        assertNotNull(components.getRefreshBtn());
        assertNotNull(components.getConfigPathLabel());
        assertNotNull(components.getChangePathBtn());
        assertNotNull(components.getOpenFolderBtn());
        assertNotNull(components.getImportConfigBtn());
        
        assertEquals("+ New Configuration", components.getNewConfigBtn().getText());
        assertEquals("📁 Import", components.getImportBtn().getText());
        assertEquals("🔄 Refresh", components.getRefreshBtn().getText());
        assertEquals("Config Path: /test/config/path", components.getConfigPathLabel().getText());
        assertEquals("🔧 Change...", components.getChangePathBtn().getText());
        assertEquals("📂 Open Folder", components.getOpenFolderBtn().getText());
        assertEquals("Import Config", components.getImportConfigBtn().getText());
    }
    
    @Test
    void testCustomConfiguration() {
        // Given
        AtlantaConfigUIFactory.UIConfiguration config = 
            AtlantaConfigUIFactory.UIConfiguration.builder()
                .searchFieldWidth(400)
                .leftCardMinWidth(700)
                .rightCardMinWidth(600)
                .splitLayoutSpacing(30)
                .build();
        
        factory.setConfiguration(config);
        
        // When
        TextField searchField = factory.createSearchField();
        assertEquals(400, searchField.getPrefWidth());
        
        HBox splitLayout = factory.createSplitLayout();
        assertEquals(30, splitLayout.getSpacing());
    }
}