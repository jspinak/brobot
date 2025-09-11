package io.github.jspinak.brobot.runner.ui.management;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.Label;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages label creation and updates to prevent duplication issues. This service ensures that
 * labels are properly tracked and updated rather than recreated, which prevents UI duplication
 * problems.
 */
@Slf4j
@Component
public class LabelManager {

    // Track labels by their identifier
    private final Map<String, Label> labelRegistry = new ConcurrentHashMap<>();

    // Track which labels belong to which component (weak references to allow GC)
    private final Map<Object, Map<String, Label>> componentLabels = new WeakHashMap<>();

    // Scheduled executor for periodic cleanup
    private ScheduledExecutorService cleanupExecutor;

    // Configuration for cleanup
    private static final int INITIAL_CLEANUP_DELAY_MINUTES = 5;
    private static final int CLEANUP_PERIOD_MINUTES = 10;
    private static final int MAX_LABEL_COUNT = 10000; // Threshold for aggressive cleanup

    /**
     * Creates or retrieves a label with the given identifier. If a label with this ID already
     * exists, it will be returned and updated.
     *
     * @param labelId Unique identifier for the label
     * @param text Initial text for the label
     * @return The label instance
     */
    public Label getOrCreateLabel(String labelId, String text) {
        return labelRegistry.computeIfAbsent(
                labelId,
                id -> {
                    Label label = new Label(text);
                    label.setId(labelId);
                    log.debug("Created new label with ID: {}", labelId);
                    return label;
                });
    }

    /**
     * Creates or retrieves a label with component context. This helps track which labels belong to
     * which UI component.
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
        componentLabels
                .computeIfAbsent(component, k -> new ConcurrentHashMap<>())
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
        log.warn(
                "Attempted to update non-existent label: {} in component: {}",
                labelId,
                component.getClass().getSimpleName());
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
            labels.forEach(
                    (id, label) -> {
                        String fullId = component.getClass().getSimpleName() + "_" + id;
                        labelRegistry.remove(fullId);
                    });
            log.debug(
                    "Removed {} labels for component: {}",
                    labels.size(),
                    component.getClass().getSimpleName());
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

    /** Clears all managed labels. This should only be used during application shutdown or reset. */
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

        componentLabels.forEach(
                (component, labels) -> {
                    summary.append("  - ")
                            .append(component.getClass().getSimpleName())
                            .append(": ")
                            .append(labels.size())
                            .append(" labels\n");
                });

        return summary.toString();
    }

    /** Initializes the cleanup scheduler. */
    @PostConstruct
    public void init() {
        cleanupExecutor =
                Executors.newSingleThreadScheduledExecutor(
                        r -> {
                            Thread thread = new Thread(r, "LabelManager-Cleanup");
                            thread.setDaemon(true);
                            return thread;
                        });

        cleanupExecutor.scheduleWithFixedDelay(
                this::performCleanup,
                INITIAL_CLEANUP_DELAY_MINUTES,
                CLEANUP_PERIOD_MINUTES,
                TimeUnit.MINUTES);

        log.info(
                "LabelManager initialized with automatic cleanup every {} minutes",
                CLEANUP_PERIOD_MINUTES);
    }

    /** Shuts down the cleanup scheduler. */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down LabelManager cleanup scheduler");

        if (cleanupExecutor != null) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Clear all labels on shutdown
        clear();
    }

    /** Performs periodic cleanup of orphaned labels. */
    private void performCleanup() {
        try {
            int initialCount = labelRegistry.size();

            // Remove orphaned labels (labels not referenced by any component)
            labelRegistry
                    .entrySet()
                    .removeIf(
                            entry -> {
                                String fullId = entry.getKey();
                                boolean isOrphaned =
                                        componentLabels.values().stream()
                                                .noneMatch(
                                                        labelMap ->
                                                                labelMap.values()
                                                                        .contains(
                                                                                entry.getValue()));

                                if (isOrphaned) {
                                    log.debug("Removing orphaned label: {}", fullId);
                                }
                                return isOrphaned;
                            });

            // Aggressive cleanup if over threshold
            if (labelRegistry.size() > MAX_LABEL_COUNT) {
                log.warn(
                        "Label count {} exceeds maximum {}, performing aggressive cleanup",
                        labelRegistry.size(),
                        MAX_LABEL_COUNT);

                // Keep only labels that are currently referenced
                Map<String, Label> activeLabels = new ConcurrentHashMap<>();
                componentLabels
                        .values()
                        .forEach(
                                labelMap ->
                                        labelMap.forEach(
                                                (id, label) -> {
                                                    String fullId = label.getId();
                                                    if (fullId != null) {
                                                        activeLabels.put(fullId, label);
                                                    }
                                                }));

                labelRegistry.clear();
                labelRegistry.putAll(activeLabels);
            }

            int removedCount = initialCount - labelRegistry.size();
            if (removedCount > 0) {
                log.info(
                        "Cleanup removed {} orphaned labels, {} labels remaining",
                        removedCount,
                        labelRegistry.size());
            }

        } catch (Exception e) {
            log.error("Error during label cleanup", e);
        }
    }
}
