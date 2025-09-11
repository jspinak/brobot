package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.models.ConfigFormModel;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing configuration state and tracking changes. Handles undo/redo, change
 * tracking, and confirmation dialogs.
 */
@Slf4j
@Service
public class ConfigStateManager {

    private ConfigEntry currentConfig;
    private ConfigFormModel currentModel;
    private ConfigFormModel originalModel;
    private Map<String, ConfigFormModel> stateHistory = new HashMap<>();
    private boolean hasUnsavedChanges = false;

    /** Sets the current configuration being edited. */
    public void setCurrentConfig(ConfigEntry config, ConfigFormModel model) {
        this.currentConfig = config;
        this.currentModel = model;
        this.originalModel = model.copy();
        this.hasUnsavedChanges = false;

        log.debug("Set current config: {}", config.getName());
    }

    /** Marks the current state as modified. */
    public void markAsModified() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            if (currentModel != null) {
                currentModel.markAsModified();
            }
            log.debug("Configuration marked as modified");
        }
    }

    /** Resets the modified state after successful save. */
    public void resetModifiedState() {
        hasUnsavedChanges = false;
        if (currentModel != null) {
            currentModel.resetModified();
            originalModel = currentModel.copy();
        }
        log.debug("Configuration state reset");
    }

    /** Checks if there are unsaved changes. */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Shows a confirmation dialog if there are unsaved changes. Returns true if the user wants to
     * proceed (discard changes).
     */
    public boolean confirmDiscardChanges() {
        if (!hasUnsavedChanges) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to discard your changes?");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType discardButton = new ButtonType("Discard");
        ButtonType cancelButton = ButtonType.CANCEL;

        alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                // Trigger save through the model's save handler
                if (currentModel != null && currentModel.getSaveHandler() != null) {
                    currentModel.getSaveHandler().run();
                }
                return false; // Don't proceed yet, save is async
            } else if (result.get() == discardButton) {
                resetModifiedState();
                return true;
            }
        }

        return false; // User cancelled
    }

    /** Saves the current state to history. */
    public void saveStateToHistory(String label) {
        if (currentModel != null && currentConfig != null) {
            String key = currentConfig.getName() + "_" + label;
            stateHistory.put(key, currentModel.copy());
            log.debug("Saved state to history: {}", key);
        }
    }

    /** Restores a state from history. */
    public void restoreStateFromHistory(String label) {
        if (currentConfig != null) {
            String key = currentConfig.getName() + "_" + label;
            ConfigFormModel savedState = stateHistory.get(key);
            if (savedState != null) {
                // Copy values from saved state to current model
                currentModel.setName(savedState.getName());
                currentModel.setProject(savedState.getProject());
                currentModel.setVersion(savedState.getVersion());
                currentModel.setDescription(savedState.getDescription());
                currentModel.setAuthor(savedState.getAuthor());
                currentModel.setAdditionalFields(new HashMap<>(savedState.getAdditionalFields()));

                markAsModified();
                log.debug("Restored state from history: {}", key);
            }
        }
    }

    /** Reverts to the original state. */
    public void revertToOriginal() {
        if (originalModel != null && currentModel != null) {
            // Copy values from original to current
            currentModel.setName(originalModel.getName());
            currentModel.setProject(originalModel.getProject());
            currentModel.setVersion(originalModel.getVersion());
            currentModel.setDescription(originalModel.getDescription());
            currentModel.setAuthor(originalModel.getAuthor());
            currentModel.setAdditionalFields(new HashMap<>(originalModel.getAdditionalFields()));

            resetModifiedState();
            log.info("Reverted to original state");
        }
    }

    /** Gets a summary of changes made. */
    public Map<String, String> getChangeSummary() {
        Map<String, String> changes = new HashMap<>();

        if (originalModel == null || currentModel == null) {
            return changes;
        }

        // Compare basic fields
        compareField(changes, "name", originalModel.getName(), currentModel.getName());
        compareField(changes, "project", originalModel.getProject(), currentModel.getProject());
        compareField(changes, "version", originalModel.getVersion(), currentModel.getVersion());
        compareField(
                changes,
                "description",
                originalModel.getDescription(),
                currentModel.getDescription());
        compareField(changes, "author", originalModel.getAuthor(), currentModel.getAuthor());

        // Compare additional fields
        Map<String, String> originalAdditional = originalModel.getAdditionalFields();
        Map<String, String> currentAdditional = currentModel.getAdditionalFields();

        // Check for added or modified fields
        for (Map.Entry<String, String> entry : currentAdditional.entrySet()) {
            String key = entry.getKey();
            String currentValue = entry.getValue();
            String originalValue = originalAdditional.get(key);

            if (originalValue == null) {
                changes.put(key, "[Added] " + currentValue);
            } else if (!originalValue.equals(currentValue)) {
                changes.put(key, originalValue + " → " + currentValue);
            }
        }

        // Check for removed fields
        for (String key : originalAdditional.keySet()) {
            if (!currentAdditional.containsKey(key)) {
                changes.put(key, "[Removed] " + originalAdditional.get(key));
            }
        }

        return changes;
    }

    /** Compares a field and adds to changes if different. */
    private void compareField(
            Map<String, String> changes, String fieldName, String original, String current) {
        if (original == null && current == null) {
            return;
        }

        if (original == null) {
            changes.put(fieldName, "[Added] " + current);
        } else if (current == null) {
            changes.put(fieldName, "[Removed] " + original);
        } else if (!original.equals(current)) {
            changes.put(fieldName, original + " → " + current);
        }
    }

    /** Clears all state history for memory management. */
    public void clearHistory() {
        stateHistory.clear();
        log.debug("Cleared state history");
    }

    /** Gets the current configuration entry. */
    public ConfigEntry getCurrentConfig() {
        return currentConfig;
    }

    /** Gets the current form model. */
    public ConfigFormModel getCurrentModel() {
        return currentModel;
    }
}
