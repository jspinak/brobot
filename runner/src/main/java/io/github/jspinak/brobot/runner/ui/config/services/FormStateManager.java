package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.*;

import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing form state and tracking changes. Monitors form fields for modifications and
 * handles state persistence.
 */
@Slf4j
@Service
public class FormStateManager {

    // Track original values for each field
    private final Map<String, String> originalValues = new ConcurrentHashMap<>();

    // Track current values for each field
    private final Map<String, String> currentValues = new ConcurrentHashMap<>();

    // Track field nodes
    private final Map<String, Node> trackedFields = new ConcurrentHashMap<>();

    // Change listeners for cleanup
    private final Map<String, ChangeListener<String>> changeListeners = new ConcurrentHashMap<>();

    // Modified state property
    @Getter private final BooleanProperty modifiedProperty = new SimpleBooleanProperty(false);

    // Enabled state
    private boolean trackingEnabled = true;

    /**
     * Tracks a text field for changes.
     *
     * @param field TextField to track
     * @param key Unique key for the field
     */
    public void trackTextField(TextField field, String key) {
        if (field == null || key == null) {
            return;
        }

        log.debug("Tracking text field: {}", key);

        // Store original value
        String originalValue = field.getText();
        originalValues.put(key, originalValue != null ? originalValue : "");
        currentValues.put(key, originalValue != null ? originalValue : "");
        trackedFields.put(key, field);

        // Remove any existing listener
        removeListener(key);

        // Add change listener
        ChangeListener<String> listener =
                (obs, oldVal, newVal) -> {
                    if (trackingEnabled) {
                        handleFieldChange(key, newVal);
                    }
                };

        field.textProperty().addListener(listener);
        changeListeners.put(key, listener);
    }

    /**
     * Tracks a text area for changes.
     *
     * @param area TextArea to track
     * @param key Unique key for the field
     */
    public void trackTextArea(TextArea area, String key) {
        if (area == null || key == null) {
            return;
        }

        log.debug("Tracking text area: {}", key);

        // Store original value
        String originalValue = area.getText();
        originalValues.put(key, originalValue != null ? originalValue : "");
        currentValues.put(key, originalValue != null ? originalValue : "");
        trackedFields.put(key, area);

        // Remove any existing listener
        removeListener(key);

        // Add change listener
        ChangeListener<String> listener =
                (obs, oldVal, newVal) -> {
                    if (trackingEnabled) {
                        handleFieldChange(key, newVal);
                    }
                };

        area.textProperty().addListener(listener);
        changeListeners.put(key, listener);
    }

    /**
     * Tracks a combo box for changes.
     *
     * @param comboBox ComboBox to track
     * @param key Unique key for the field
     */
    public <T> void trackComboBox(ComboBox<T> comboBox, String key) {
        if (comboBox == null || key == null) {
            return;
        }

        log.debug("Tracking combo box: {}", key);

        // Store original value
        T originalValue = comboBox.getValue();
        String originalString = originalValue != null ? originalValue.toString() : "";
        originalValues.put(key, originalString);
        currentValues.put(key, originalString);
        trackedFields.put(key, comboBox);

        // Add change listener
        comboBox.valueProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (trackingEnabled) {
                                String newString = newVal != null ? newVal.toString() : "";
                                handleFieldChange(key, newString);
                            }
                        });
    }

    /**
     * Tracks a generic node for changes.
     *
     * @param node Node to track
     * @param key Unique key for the field
     * @param valueExtractor Function to extract current value
     */
    public void trackNode(
            Node node, String key, java.util.function.Supplier<String> valueExtractor) {
        if (node == null || key == null || valueExtractor == null) {
            return;
        }

        log.debug("Tracking node: {}", key);

        String originalValue = valueExtractor.get();
        originalValues.put(key, originalValue != null ? originalValue : "");
        currentValues.put(key, originalValue != null ? originalValue : "");
        trackedFields.put(key, node);
    }

    /**
     * Handles field value change.
     *
     * @param key Field key
     * @param newValue New value
     */
    private void handleFieldChange(String key, String newValue) {
        currentValues.put(key, newValue != null ? newValue : "");
        updateModifiedState();
    }

    /** Updates the modified state based on current values. */
    private void updateModifiedState() {
        boolean hasChanges = false;

        for (Map.Entry<String, String> entry : currentValues.entrySet()) {
            String key = entry.getKey();
            String currentValue = entry.getValue();
            String originalValue = originalValues.get(key);

            if (!Objects.equals(currentValue, originalValue)) {
                hasChanges = true;
                break;
            }
        }

        modifiedProperty.set(hasChanges);
    }

    /**
     * Checks if the form has unsaved changes.
     *
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return modifiedProperty.get();
    }

    /** Marks the form as modified. */
    public void markAsModified() {
        modifiedProperty.set(true);
    }

    /** Resets the modified state, updating original values to current. */
    public void resetModifiedState() {
        log.debug("Resetting modified state");

        // Update original values to current
        originalValues.clear();
        originalValues.putAll(currentValues);

        // Clear modified flag
        modifiedProperty.set(false);
    }

    /** Resets all fields to their original values. */
    public void resetToOriginal() {
        log.debug("Resetting fields to original values");

        trackingEnabled = false;

        try {
            for (Map.Entry<String, String> entry : originalValues.entrySet()) {
                String key = entry.getKey();
                String originalValue = entry.getValue();
                Node node = trackedFields.get(key);

                if (node instanceof TextField) {
                    ((TextField) node).setText(originalValue);
                } else if (node instanceof TextArea) {
                    ((TextArea) node).setText(originalValue);
                } else if (node instanceof ComboBox<?>) {
                    // For combo boxes, we stored the string representation
                    // This is a limitation - combo box reset may not work perfectly
                    log.warn(
                            "ComboBox reset may not restore exact original value for key: {}", key);
                }
            }

            // Update current values
            currentValues.clear();
            currentValues.putAll(originalValues);

        } finally {
            trackingEnabled = true;
        }

        updateModifiedState();
    }

    /**
     * Shows a confirmation dialog for discarding changes.
     *
     * @return true if user confirms discarding changes
     */
    public boolean confirmDiscardChanges() {
        if (!hasUnsavedChanges()) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to discard your changes?");

        ButtonType saveButton = new ButtonType("Save Changes", ButtonBar.ButtonData.YES);
        ButtonType discardButton = new ButtonType("Discard", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                return false; // Don't discard, need to save
            } else if (result.get() == discardButton) {
                return true; // Discard changes
            }
        }

        return false; // Cancel - don't discard
    }

    /** Clears all tracking data. */
    public void clear() {
        log.debug("Clearing form state manager");

        // Remove all listeners
        for (String key : changeListeners.keySet()) {
            removeListener(key);
        }

        originalValues.clear();
        currentValues.clear();
        trackedFields.clear();
        changeListeners.clear();
        modifiedProperty.set(false);
    }

    /**
     * Removes change listener for a field.
     *
     * @param key Field key
     */
    private void removeListener(String key) {
        ChangeListener<String> listener = changeListeners.get(key);
        if (listener != null) {
            Node node = trackedFields.get(key);
            if (node instanceof TextField) {
                ((TextField) node).textProperty().removeListener(listener);
            } else if (node instanceof TextArea) {
                ((TextArea) node).textProperty().removeListener(listener);
            }
            changeListeners.remove(key);
        }
    }

    /**
     * Gets the current value of a tracked field.
     *
     * @param key Field key
     * @return Current value or null if not tracked
     */
    public String getCurrentValue(String key) {
        return currentValues.get(key);
    }

    /**
     * Gets the original value of a tracked field.
     *
     * @param key Field key
     * @return Original value or null if not tracked
     */
    public String getOriginalValue(String key) {
        return originalValues.get(key);
    }

    /**
     * Gets all changed fields.
     *
     * @return Map of changed fields (key -> current value)
     */
    public Map<String, String> getChangedFields() {
        Map<String, String> changes = new HashMap<>();

        for (Map.Entry<String, String> entry : currentValues.entrySet()) {
            String key = entry.getKey();
            String currentValue = entry.getValue();
            String originalValue = originalValues.get(key);

            if (!Objects.equals(currentValue, originalValue)) {
                changes.put(key, currentValue);
            }
        }

        return changes;
    }

    /**
     * Enables or disables change tracking.
     *
     * @param enabled true to enable tracking
     */
    public void setTrackingEnabled(boolean enabled) {
        this.trackingEnabled = enabled;
    }

    /**
     * Checks if tracking is enabled.
     *
     * @return true if tracking is enabled
     */
    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }
}
