package io.github.jspinak.brobot.runner.ui.management;

import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages label creation and updates to prevent duplication issues.
 * This service ensures that labels are properly tracked and updated
 * rather than recreated, which prevents UI duplication problems.
 */
@Slf4j
@Component
public class LabelManager {
    
    // Track labels by their identifier
    private final Map<String, Label> labelRegistry = new ConcurrentHashMap<>();
    
    // Track which labels belong to which component (weak references to allow GC)
    private final Map<Object, Map<String, Label>> componentLabels = new WeakHashMap<>();
    
    /**
     * Creates or retrieves a label with the given identifier.
     * If a label with this ID already exists, it will be returned and updated.
     * 
     * @param labelId Unique identifier for the label
     * @param text Initial text for the label
     * @return The label instance
     */
    public Label getOrCreateLabel(String labelId, String text) {
        return labelRegistry.computeIfAbsent(labelId, id -> {
            Label label = new Label(text);
            label.setId(labelId);
            log.debug("Created new label with ID: {}", labelId);
            return label;
        });
    }
    
    /**
     * Creates or retrieves a label with component context.
     * This helps track which labels belong to which UI component.
     * 
     * @param component The owning component
     * @param labelId Unique identifier within the component context
     * @param text Initial text for the label
     * @return The label instance
     */
    public Label getOrCreateLabel(Object component, String labelId, String text) {
        String fullId = component.getClass().getSimpleName() + "_" + labelId;
        Label label = getOrCreateLabel(fullId, text);
        
        // Track component ownership
        componentLabels.computeIfAbsent(component, k -> new ConcurrentHashMap<>())
                      .put(labelId, label);
        
        return label;
    }
    
    /**
     * Updates the text of an existing label.
     * 
     * @param labelId The label identifier
     * @param text The new text
     * @return true if the label was found and updated, false otherwise
     */
    public boolean updateLabel(String labelId, String text) {
        Label label = labelRegistry.get(labelId);
        if (label != null) {
            label.setText(text);
            return true;
        }
        log.warn("Attempted to update non-existent label: {}", labelId);
        return false;
    }
    
    /**
     * Updates a label within a component context.
     * 
     * @param component The owning component
     * @param labelId The label identifier within the component
     * @param text The new text
     * @return true if the label was found and updated, false otherwise
     */
    public boolean updateLabel(Object component, String labelId, String text) {
        Map<String, Label> labels = componentLabels.get(component);
        if (labels != null) {
            Label label = labels.get(labelId);
            if (label != null) {
                label.setText(text);
                return true;
            }
        }
        log.warn("Attempted to update non-existent label: {} in component: {}", 
                 labelId, component.getClass().getSimpleName());
        return false;
    }
    
    /**
     * Removes a label from management.
     * 
     * @param labelId The label identifier
     * @return The removed label, or null if not found
     */
    public Label removeLabel(String labelId) {
        Label removed = labelRegistry.remove(labelId);
        if (removed != null) {
            log.debug("Removed label with ID: {}", labelId);
        }
        return removed;
    }
    
    /**
     * Removes all labels associated with a component.
     * 
     * @param component The component whose labels should be removed
     */
    public void removeComponentLabels(Object component) {
        Map<String, Label> labels = componentLabels.remove(component);
        if (labels != null) {
            labels.forEach((id, label) -> {
                String fullId = component.getClass().getSimpleName() + "_" + id;
                labelRegistry.remove(fullId);
            });
            log.debug("Removed {} labels for component: {}", 
                     labels.size(), component.getClass().getSimpleName());
        }
    }
    
    /**
     * Gets the total number of managed labels.
     * 
     * @return The number of labels currently managed
     */
    public int getLabelCount() {
        return labelRegistry.size();
    }
    
    /**
     * Gets the number of components with managed labels.
     * 
     * @return The number of components
     */
    public int getComponentCount() {
        return componentLabels.size();
    }
    
    /**
     * Clears all managed labels.
     * This should only be used during application shutdown or reset.
     */
    public void clear() {
        int labelCount = labelRegistry.size();
        int componentCount = componentLabels.size();
        
        labelRegistry.clear();
        componentLabels.clear();
        
        log.info("Cleared {} labels from {} components", labelCount, componentCount);
    }
    
    /**
     * Gets a summary of the current label management state.
     * 
     * @return A string describing the current state
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("LabelManager Summary:\n");
        summary.append("  Total labels: ").append(labelRegistry.size()).append("\n");
        summary.append("  Components tracked: ").append(componentLabels.size()).append("\n");
        
        componentLabels.forEach((component, labels) -> {
            summary.append("  - ").append(component.getClass().getSimpleName())
                   .append(": ").append(labels.size()).append(" labels\n");
        });
        
        return summary.toString();
    }
}