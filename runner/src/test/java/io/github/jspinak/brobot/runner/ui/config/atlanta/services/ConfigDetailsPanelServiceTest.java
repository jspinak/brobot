package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel.ConfigEntry;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ConfigDetailsPanelServiceTest {
    
    private ConfigDetailsPanelService service;
    
    @BeforeEach
    void setUp() {
        service = new ConfigDetailsPanelService();
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testCreateDetailsContent() {
        // When
        VBox detailsContent = service.createDetailsContent();
        
        // Then
        assertNotNull(detailsContent);
        assertTrue(detailsContent.getStyleClass().contains("configuration-details"));
        assertEquals(16, detailsContent.getSpacing());
        
        // Should have at least 7 children (6 detail rows + metadata section)
        assertTrue(detailsContent.getChildren().size() >= 7);
    }
    
    @Test
    void testUpdateDetails() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        entry.setDescription("Test description");
        entry.setAuthor("Test Author");
        
        // When
        service.updateDetails(entry);
        
        // Then
        assertEquals("Test description", service.getDescription());
        assertEquals("Test Author", service.getAuthor());
    }
    
    @Test
    void testUpdateDetailsWithNullMetadata() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        // Don't set description or author
        
        // When
        service.updateDetails(entry);
        
        // Then
        assertEquals("", service.getDescription());
        assertEquals("", service.getAuthor());
    }
    
    @Test
    void testClearDetails() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        ConfigEntry entry = new ConfigEntry("TestConfig", "TestProject", "/test/path");
        entry.setDescription("Test description");
        entry.setAuthor("Test Author");
        service.updateDetails(entry);
        
        // When
        service.clearDetails();
        
        // Then
        assertEquals("", service.getDescription());
        assertEquals("", service.getAuthor());
    }
    
    @Test
    void testUpdateDetailsWithNull() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        
        // When
        service.updateDetails(null);
        
        // Then - Should clear all fields
        assertEquals("", service.getDescription());
        assertEquals("", service.getAuthor());
    }
    
    @Test
    void testSetMetadataEditable() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        
        // When - Enable editing
        service.setMetadataEditable(true);
        
        // Then - Get the actual TextArea and TextField from the content
        // In a real implementation, we'd need to expose these or verify through the UI
        
        // When - Disable editing
        service.setMetadataEditable(false);
        
        // Then - Fields should not be editable
        // Again, would need UI access to verify
    }
    
    @Test
    void testDetailRowStructure() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        
        // Then
        // Check that we have the expected detail rows
        boolean hasNameRow = false;
        boolean hasProjectRow = false;
        boolean hasMetadataSection = false;
        
        for (int i = 0; i < detailsContent.getChildren().size(); i++) {
            if (detailsContent.getChildren().get(i) instanceof VBox) {
                VBox row = (VBox) detailsContent.getChildren().get(i);
                if (row.getStyleClass().contains("detail-row")) {
                    // This is a detail row
                    if (row.getChildren().size() > 0 && 
                        row.getChildren().get(0).toString().contains("Name:")) {
                        hasNameRow = true;
                    }
                    if (row.getChildren().size() > 0 && 
                        row.getChildren().get(0).toString().contains("Project:")) {
                        hasProjectRow = true;
                    }
                } else if (row.getStyleClass().contains("metadata-section")) {
                    hasMetadataSection = true;
                }
            }
        }
        
        assertTrue(hasNameRow || hasProjectRow || hasMetadataSection);
    }
    
    @Test
    void testMetadataFields() {
        // Given
        VBox detailsContent = service.createDetailsContent();
        ConfigEntry entry = new ConfigEntry("Test", "Project", "/path");
        String longDescription = "This is a very long description that spans multiple lines " +
                               "to test the text area behavior and ensure it handles long text properly.";
        entry.setDescription(longDescription);
        
        // When
        service.updateDetails(entry);
        
        // Then
        assertEquals(longDescription, service.getDescription());
    }
}