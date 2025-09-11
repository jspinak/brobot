package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import java.time.format.DateTimeFormatter;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel.ConfigEntry;

/** Service for managing the configuration details panel. */
@Service
public class ConfigDetailsPanelService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Detail components
    private Label nameValue;
    private Label projectValue;
    private Label projectConfigValue;
    private Label dslConfigValue;
    private Label imagePathValue;
    private Label lastModifiedValue;
    private TextArea descriptionArea;
    private TextField authorField;

    /**
     * Creates the configuration details content.
     *
     * @return The details content VBox
     */
    public VBox createDetailsContent() {
        VBox detailsContent = new VBox(16);
        detailsContent.getStyleClass().add("configuration-details");

        // Initialize detail fields
        nameValue = createDetailValue();
        projectValue = createDetailValue();
        projectConfigValue = createDetailValue();
        dslConfigValue = createDetailValue();
        imagePathValue = createDetailValue();
        lastModifiedValue = createDetailValue();

        // Add detail rows
        detailsContent
                .getChildren()
                .addAll(
                        createDetailRow("Name:", nameValue),
                        createDetailRow("Project:", projectValue),
                        createDetailRow("Project Config:", projectConfigValue),
                        createDetailRow("DSL Config:", dslConfigValue),
                        createDetailRow("Image Path:", imagePathValue),
                        createDetailRow("Last Modified:", lastModifiedValue));

        // Add metadata section
        VBox metadataSection = createMetadataSection();
        detailsContent.getChildren().add(metadataSection);

        return detailsContent;
    }

    /**
     * Creates the metadata section.
     *
     * @return The metadata section
     */
    private VBox createMetadataSection() {
        VBox metadataSection = new VBox(16);
        metadataSection.getStyleClass().add("metadata-section");

        Label metadataTitle = new Label("Configuration Metadata");
        metadataTitle.getStyleClass().add("metadata-title");

        // Description
        VBox descriptionRow = createDetailRow("Description:", null);
        descriptionArea = new TextArea();
        descriptionArea.getStyleClass().add("metadata-input");
        descriptionArea.setPromptText("No description available");
        descriptionArea.setPrefRowCount(3);
        descriptionRow.getChildren().add(descriptionArea);

        // Author
        VBox authorRow = createDetailRow("Author:", null);
        authorField = new TextField();
        authorField.getStyleClass().add("form-control");
        authorField.setPromptText("—");
        authorRow.getChildren().add(authorField);

        metadataSection.getChildren().addAll(metadataTitle, descriptionRow, authorRow);

        return metadataSection;
    }

    /**
     * Creates a detail row with label and optional value.
     *
     * @param labelText The label text
     * @param value The value label (can be null)
     * @return The detail row VBox
     */
    private VBox createDetailRow(String labelText, Label value) {
        VBox row = new VBox(4);
        row.getStyleClass().add("detail-row");

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        row.getChildren().add(label);

        if (value != null) {
            row.getChildren().add(value);
        }

        return row;
    }

    /**
     * Creates a styled detail value label.
     *
     * @return The styled label
     */
    private Label createDetailValue() {
        Label value = new Label("—");
        value.getStyleClass().add("detail-value");
        return value;
    }

    /**
     * Updates the details panel with configuration data.
     *
     * @param entry The configuration entry
     */
    public void updateDetails(ConfigEntry entry) {
        if (entry == null) {
            clearDetails();
            return;
        }

        nameValue.setText(entry.getName());
        projectValue.setText(entry.getProject());
        projectConfigValue.setText(entry.getProjectConfig());
        dslConfigValue.setText(entry.getDslConfig());
        imagePathValue.setText(entry.getImagePath());
        lastModifiedValue.setText(entry.getLastModified().format(DATE_FORMATTER));

        // Update metadata
        descriptionArea.setText(entry.getDescription() != null ? entry.getDescription() : "");
        authorField.setText(entry.getAuthor() != null ? entry.getAuthor() : "");
    }

    /** Clears all detail fields. */
    public void clearDetails() {
        nameValue.setText("—");
        projectValue.setText("—");
        projectConfigValue.setText("—");
        dslConfigValue.setText("—");
        imagePathValue.setText("—");
        lastModifiedValue.setText("—");
        descriptionArea.clear();
        authorField.clear();
    }

    /**
     * Gets the current description text.
     *
     * @return The description
     */
    public String getDescription() {
        return descriptionArea.getText();
    }

    /**
     * Gets the current author text.
     *
     * @return The author
     */
    public String getAuthor() {
        return authorField.getText();
    }

    /**
     * Enables or disables editing of metadata fields.
     *
     * @param editable True to enable editing
     */
    public void setMetadataEditable(boolean editable) {
        descriptionArea.setEditable(editable);
        authorField.setEditable(editable);
    }
}
