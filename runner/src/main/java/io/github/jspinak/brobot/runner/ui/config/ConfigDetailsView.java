package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.ui.config.model.ConfigEntry;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import io.github.jspinak.brobot.runner.ui.components.BrobotFormGrid;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration details view component.
 * Single responsibility: Displays and edits configuration details.
 */
@Slf4j
public class ConfigDetailsView extends VBox {
    
    private final EventBus eventBus;
    
    // Detail fields
    private final Label nameValue;
    private final Label projectValue;
    private final Label projectConfigValue;
    private final Label dslConfigValue;
    private final Label imagePathValue;
    private final Label lastModifiedValue;
    private final TextArea descriptionArea;
    private final TextField authorField;
    
    // Action button
    private final BrobotButton importConfigBtn;
    
    // Current configuration
    private ConfigEntry currentConfig;
    
    public ConfigDetailsView(EventBus eventBus) {
        this.eventBus = eventBus;
        
        // Initialize fields
        nameValue = new Label("—");
        projectValue = new Label("—");
        projectConfigValue = new Label("—");
        dslConfigValue = new Label("—");
        imagePathValue = new Label("—");
        lastModifiedValue = new Label("—");
        
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("No description available");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        
        authorField = new TextField();
        authorField.setPromptText("—");
        
        importConfigBtn = BrobotButton.primary("Import Config");
        importConfigBtn.setOnAction(e -> importCurrentConfig());
        importConfigBtn.setDisable(true); // Disabled until a config is selected
        
        // Setup layout
        setupLayout();
        
        // Apply styling
        getStyleClass().add("config-details-view");
    }
    
    private void setupLayout() {
        setSpacing(24);
        setPadding(new Insets(16));
        
        // Basic information section
        VBox basicInfo = createBasicInfoSection();
        
        // Metadata section
        VBox metadata = createMetadataSection();
        
        // Import button at the bottom
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(importConfigBtn);
        
        // Add all sections
        getChildren().addAll(basicInfo, metadata, buttonBox);
    }
    
    private VBox createBasicInfoSection() {
        VBox section = new VBox(12);
        
        BrobotFormGrid grid = new BrobotFormGrid();
        
        grid.addRow(0, new Label("Name:"), nameValue);
        grid.addRow(1, new Label("Project:"), projectValue);
        grid.addRow(2, new Label("Project Config:"), projectConfigValue);
        grid.addRow(3, new Label("DSL Config:"), dslConfigValue);
        grid.addRow(4, new Label("Image Path:"), imagePathValue);
        grid.addRow(5, new Label("Last Modified:"), lastModifiedValue);
        
        section.getChildren().add(grid);
        
        return section;
    }
    
    private VBox createMetadataSection() {
        VBox section = new VBox(12);
        
        Label metadataLabel = new Label("Configuration Metadata");
        metadataLabel.getStyleClass().add("section-title");
        
        BrobotFormGrid grid = new BrobotFormGrid();
        
        Label descLabel = new Label("Description:");
        GridPane.setValignment(descLabel, javafx.geometry.VPos.TOP);
        grid.add(descLabel, 0, 0);
        grid.add(descriptionArea, 1, 0);
        
        grid.addRow(1, new Label("Author:"), authorField);
        
        section.getChildren().addAll(metadataLabel, grid);
        
        return section;
    }
    
    /**
     * Shows the details of the given configuration.
     */
    public void showConfiguration(ConfigEntry config) {
        this.currentConfig = config;
        
        if (config != null) {
            nameValue.setText(config.getName());
            projectValue.setText(config.getProject());
            projectConfigValue.setText(config.getProjectConfig() != null ? config.getProjectConfig() : "—");
            dslConfigValue.setText(config.getDslConfig() != null ? config.getDslConfig() : "—");
            imagePathValue.setText(config.getImagePath() != null ? config.getImagePath() : "—");
            lastModifiedValue.setText(config.getLastModified().toString());
            
            descriptionArea.setText(config.getDescription() != null ? config.getDescription() : "");
            authorField.setText(config.getAuthor() != null ? config.getAuthor() : "");
            
            importConfigBtn.setDisable(false);
        } else {
            clear();
        }
    }
    
    /**
     * Clears all fields.
     */
    public void clear() {
        currentConfig = null;
        
        nameValue.setText("—");
        projectValue.setText("—");
        projectConfigValue.setText("—");
        dslConfigValue.setText("—");
        imagePathValue.setText("—");
        lastModifiedValue.setText("—");
        
        descriptionArea.clear();
        authorField.clear();
        
        importConfigBtn.setDisable(true);
    }
    
    private void importCurrentConfig() {
        if (currentConfig != null) {
            log.info("Importing configuration: {}", currentConfig.getName());
            // TODO: Implement configuration import
        }
    }
}