package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;
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

class AtlantaConfigUIFactoryTest extends ImprovedJavaFXTestBase {
    
    private AtlantaConfigUIFactory factory;
    
    @BeforeEach
    void setUp() {
        factory = new AtlantaConfigUIFactory();
        // JavaFX initialization is handled by ImprovedJavaFXTestBase
    }
    
    @Test
    void testCreateActionBar() throws InterruptedException {
        final HBox[] actionBar = new HBox[1];
        runAndWait(() -> {
            // When
            actionBar[0] = factory.createActionBar();
        });
        
        // Then
        assertNotNull(actionBar[0]);
        assertTrue(actionBar[0].getStyleClass().contains("action-bar"));
        assertEquals(Pos.CENTER_LEFT, actionBar[0].getAlignment());
        assertEquals(8, actionBar[0].getSpacing());
    }
    
    @Test
    void testCreatePrimaryButton() throws InterruptedException {
        final BrobotButton[] button = new BrobotButton[1];
        runAndWait(() -> {
            // When
            button[0] = factory.createPrimaryButton("Test Button");
        });
        
        // Then
        assertNotNull(button[0]);
        assertEquals("Test Button", button[0].getText());
        // BrobotButton.primary() should add appropriate styles
    }
    
    @Test
    void testCreateSecondaryButton() throws InterruptedException {
        final BrobotButton[] button = new BrobotButton[1];
        runAndWait(() -> {
            // When
            button[0] = factory.createSecondaryButton("Test Button");
        });
        
        // Then
        assertNotNull(button[0]);
        assertEquals("Test Button", button[0].getText());
        // BrobotButton.secondary() should add appropriate styles
    }
    
    @Test
    void testCreateButton() throws InterruptedException {
        final Button[] button = new Button[1];
        runAndWait(() -> {
            // When
            button[0] = factory.createButton("Test", "custom", "style");
        });
        
        // Then
        assertNotNull(button[0]);
        assertEquals("Test", button[0].getText());
        assertTrue(button[0].getStyleClass().contains("custom"));
        assertTrue(button[0].getStyleClass().contains("style"));
    }
    
    @Test
    void testCreateSplitLayout() throws InterruptedException {
        final HBox[] splitLayout = new HBox[1];
        runAndWait(() -> {
            // When
            splitLayout[0] = factory.createSplitLayout();
        });
        
        // Then
        assertNotNull(splitLayout[0]);
        assertTrue(splitLayout[0].getStyleClass().contains("split-layout"));
        assertEquals(24, splitLayout[0].getSpacing());
    }
    
    @Test
    void testCreateCard() throws InterruptedException {
        final AtlantaCard[] card = new AtlantaCard[1];
        runAndWait(() -> {
            // When
            card[0] = factory.createCard("Test Card", 500, "test-card");
        });
        
        // Then
        assertNotNull(card[0]);
        assertTrue(card[0].getStyleClass().contains("test-card"));
        assertEquals(500, card[0].getMinWidth());
        // Expand property is set via HBox.setHgrow, not a property on the card itself
        assertEquals(Priority.ALWAYS, HBox.getHgrow(card[0]));
    }
    
    @Test
    void testCreateSearchBar() throws InterruptedException {
        final TextField[] searchField = new TextField[1];
        final ComboBox<Integer>[] itemsPerPage = new ComboBox[1];
        final HBox[] searchBar = new HBox[1];
        
        runAndWait(() -> {
            // Given
            searchField[0] = new TextField();
            itemsPerPage[0] = new ComboBox<>();
            
            // When
            searchBar[0] = factory.createSearchBar(searchField[0], itemsPerPage[0]);
        });
        
        // Then
        assertNotNull(searchBar[0]);
        assertTrue(searchBar[0].getStyleClass().contains("search-bar"));
        assertEquals(Pos.CENTER_LEFT, searchBar[0].getAlignment());
        assertEquals(12, searchBar[0].getSpacing());
        assertEquals(3, searchBar[0].getChildren().size());
        
        // Verify search field configuration
        assertTrue(searchField[0].getStyleClass().contains("search-input"));
        assertEquals("Search configurations...", searchField[0].getPromptText());
        assertEquals(Priority.ALWAYS, HBox.getHgrow(searchField[0]));
    }
    
    @Test
    void testCreateTableContent() throws InterruptedException {
        final VBox[] tableContent = new VBox[1];
        runAndWait(() -> {
            // When
            tableContent[0] = factory.createTableContent();
        });
        
        // Then
        assertNotNull(tableContent[0]);
        assertEquals(16, tableContent[0].getSpacing());
    }
    
    @Test
    void testCreateSpacer() throws InterruptedException {
        final Region[] spacer = new Region[1];
        runAndWait(() -> {
            // When
            spacer[0] = factory.createSpacer();
        });
        
        // Then
        assertNotNull(spacer[0]);
        assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer[0]));
    }
    
    @Test
    void testCreateLabel() throws InterruptedException {
        final Label[] label = new Label[1];
        runAndWait(() -> {
            // When
            label[0] = factory.createLabel("Test Label", "style1", "style2");
        });
        
        // Then
        assertNotNull(label[0]);
        assertEquals("Test Label", label[0].getText());
        assertTrue(label[0].getStyleClass().contains("style1"));
        assertTrue(label[0].getStyleClass().contains("style2"));
    }
    
    @Test
    void testCreateItemsPerPageCombo() throws InterruptedException {
        final ComboBox<Integer>[] combo = new ComboBox[1];
        runAndWait(() -> {
            // When
            combo[0] = factory.createItemsPerPageCombo();
        });
        
        // Then
        assertNotNull(combo[0]);
        assertTrue(combo[0].getStyleClass().contains("select"));
        assertEquals(3, combo[0].getItems().size());
        assertTrue(combo[0].getItems().contains(25));
        assertTrue(combo[0].getItems().contains(50));
        assertTrue(combo[0].getItems().contains(100));
        assertEquals(25, combo[0].getValue());
    }
    
    @Test
    void testCreateSearchField() throws InterruptedException {
        final TextField[] searchField = new TextField[1];
        runAndWait(() -> {
            // When
            searchField[0] = factory.createSearchField();
        });
        
        // Then
        assertNotNull(searchField[0]);
        assertTrue(searchField[0].getStyleClass().contains("search-input"));
        assertEquals("Search configurations...", searchField[0].getPromptText());
        assertEquals(300, searchField[0].getPrefWidth());
    }
    
    @Test
    void testCreateActionBarComponents() throws InterruptedException {
        final AtlantaConfigUIFactory.ActionBarComponents[] components = new AtlantaConfigUIFactory.ActionBarComponents[1];
        runAndWait(() -> {
            // When
            components[0] = factory.createActionBarComponents("/test/config/path");
        });
        
        // Then
        assertNotNull(components[0]);
        assertNotNull(components[0].getNewConfigBtn());
        assertNotNull(components[0].getImportBtn());
        assertNotNull(components[0].getRefreshBtn());
        assertNotNull(components[0].getConfigPathLabel());
        assertNotNull(components[0].getChangePathBtn());
        assertNotNull(components[0].getOpenFolderBtn());
        assertNotNull(components[0].getImportConfigBtn());
        
        assertEquals("+ New Configuration", components[0].getNewConfigBtn().getText());
        assertEquals("📁 Import", components[0].getImportBtn().getText());
        assertEquals("🔄 Refresh", components[0].getRefreshBtn().getText());
        assertEquals("Config Path: /test/config/path", components[0].getConfigPathLabel().getText());
        assertEquals("🔧 Change...", components[0].getChangePathBtn().getText());
        assertEquals("📂 Open Folder", components[0].getOpenFolderBtn().getText());
        assertEquals("Import Config", components[0].getImportConfigBtn().getText());
    }
    
    @Test
    void testCustomConfiguration() throws InterruptedException {
        final TextField[] searchField = new TextField[1];
        final HBox[] splitLayout = new HBox[1];
        
        runAndWait(() -> {
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
            searchField[0] = factory.createSearchField();
            splitLayout[0] = factory.createSplitLayout();
        });
        
        assertEquals(400, searchField[0].getPrefWidth());
        assertEquals(30, splitLayout[0].getSpacing());
    }
}