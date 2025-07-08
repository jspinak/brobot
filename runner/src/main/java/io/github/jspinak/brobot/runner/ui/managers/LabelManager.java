package io.github.jspinak.brobot.runner.ui.managers;

import javafx.application.Platform;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages label creation and updates to prevent duplication.
 * This component ensures that labels are created only once and
 * provides thread-safe updates to label text.
 * 
 * Key features:
 * - Prevents duplicate label creation
 * - Thread-safe label updates
 * - Centralized label lifecycle management
 * - Memory-efficient label reuse
 */
@Slf4j
@Component
public class LabelManager {
    private final Map<String, Label> managedLabels = new ConcurrentHashMap<>();
    
    /**
     * Gets or creates a label with the specified ID and text.
     * If a label with the given ID already exists, it will be returned
     * and its text will be updated if different.
     * 
     * @param id Unique identifier for the label
     * @param text Initial text for the label
     * @return The managed label instance
     * @throws IllegalArgumentException if id is null or empty
     */
    public Label getOrCreateLabel(String id, String text) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Label ID cannot be null or empty");
        }
        
        return managedLabels.compute(id, (key, existingLabel) -> {
            if (existingLabel == null) {
                log.debug("Creating new label with id: {}", id);
                Label newLabel = new Label(text != null ? text : "");
                newLabel.setId(id);
                return newLabel;
            } else {
                // Update text if different
                if (text != null && !existingLabel.getText().equals(text)) {
                    Platform.runLater(() -> existingLabel.setText(text));
                }
                return existingLabel;
            }
        });
    }
    
    /**
     * Creates a label with the specified ID, text, and style classes.
     * 
     * @param id Unique identifier for the label
     * @param text Initial text for the label
     * @param styleClasses CSS style classes to apply
     * @return The managed label instance
     */
    public Label getOrCreateLabel(String id, String text, String... styleClasses) {
        Label label = getOrCreateLabel(id, text);
        
        // Apply style classes if not already applied
        if (styleClasses != null && styleClasses.length > 0) {
            Platform.runLater(() -> {
                for (String styleClass : styleClasses) {
                    if (styleClass != null && !styleClass.trim().isEmpty() 
                        && !label.getStyleClass().contains(styleClass)) {
                        label.getStyleClass().add(styleClass);
                    }
                }
            });
        }
        
        return label;
    }
    
    /**
     * Updates the text of an existing label.
     * The update is performed on the JavaFX Application Thread.
     * 
     * @param id The label ID
     * @param text The new text
     * @throws IllegalArgumentException if id is null or empty
     */
    public void updateLabel(String id, String text) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Label ID cannot be null or empty");
        }
        
        Label label = managedLabels.get(id);
        if (label != null) {
            Platform.runLater(() -> label.setText(text != null ? text : ""));
        } else {
            log.warn("Attempted to update non-existent label: {}", id);
        }
    }
    
    /**
     * Updates a label with custom logic.
     * 
     * @param id The label ID
     * @param updater Function to update the label
     */
    public void updateLabel(String id, Consumer<Label> updater) {
        Label label = managedLabels.get(id);
        if (label != null) {
            Platform.runLater(() -> updater.accept(label));
        } else {
            log.warn("Attempted to update non-existent label: {}", id);
        }
    }
    
    /**
     * Checks if a label with the given ID exists.
     * 
     * @param id The label ID
     * @return true if the label exists
     */
    public boolean hasLabel(String id) {
        return managedLabels.containsKey(id);
    }
    
    /**
     * Removes a label from management.
     * 
     * @param id The label ID to remove
     * @return The removed label, or null if not found
     */
    public Label removeLabel(String id) {
        Label removed = managedLabels.remove(id);
        if (removed != null) {
            log.debug("Removed label: {}", id);
        }
        return removed;
    }
    
    /**
     * Clears all managed labels.
     */
    public void clear() {
        int count = managedLabels.size();
        managedLabels.clear();
        log.info("Cleared {} managed labels", count);
    }
    
    /**
     * Gets the count of managed labels.
     * 
     * @return The number of labels being managed
     */
    public int size() {
        return managedLabels.size();
    }
    
    /**
     * Gets a snapshot of all label IDs and their current text.
     * Useful for debugging.
     * 
     * @return Map of label IDs to their current text
     */
    public Map<String, String> getLabelSnapshot() {
        Map<String, String> snapshot = new ConcurrentHashMap<>();
        managedLabels.forEach((id, label) -> 
            snapshot.put(id, label.getText())
        );
        return snapshot;
    }
    
    /**
     * Logs the current state of all managed labels.
     * Useful for debugging label duplication issues.
     */
    public void logLabelState() {
        log.info("Label Manager State:");
        log.info("Total managed labels: {}", managedLabels.size());
        
        if (log.isDebugEnabled()) {
            managedLabels.forEach((id, label) -> 
                log.debug("  {} -> '{}' (classes: {})", 
                    id, 
                    label.getText(), 
                    String.join(", ", label.getStyleClass()))
            );
        }
    }
    
    /**
     * Creates a unique ID for a label based on its context.
     * 
     * @param prefix A prefix for the ID (e.g., "category", "button", "status")
     * @param context Additional context to make the ID unique
     * @return A unique label ID
     */
    public static String createLabelId(String prefix, String context) {
        return String.format("%s_%s_%d", prefix, context, context.hashCode());
    }
}