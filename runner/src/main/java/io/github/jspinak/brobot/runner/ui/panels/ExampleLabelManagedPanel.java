package io.github.jspinak.brobot.runner.ui.panels;

import java.util.concurrent.TimeUnit;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Example panel demonstrating how to use LabelManager and UIUpdateManager to prevent label
 * duplication and manage UI updates efficiently.
 */
@Slf4j
@Component
public class ExampleLabelManagedPanel extends VBox {

    @Autowired private LabelManager labelManager;

    @Autowired private UIUpdateManager updateManager;

    // Label identifiers
    private static final String STATUS_LABEL_ID = "statusLabel";
    private static final String COUNTER_LABEL_ID = "counterLabel";
    private static final String TIME_LABEL_ID = "timeLabel";

    // Counter for demonstration
    private int counter = 0;

    @PostConstruct
    public void initialize() {
        setupUI();
        startPeriodicUpdates();
        log.info("ExampleLabelManagedPanel initialized");
    }

    @PreDestroy
    public void cleanup() {
        // Cancel scheduled updates
        updateManager.cancelScheduledUpdate("counterUpdate");
        updateManager.cancelScheduledUpdate("timeUpdate");

        // Remove labels from management
        labelManager.removeComponentLabels(this);

        log.info("ExampleLabelManagedPanel cleaned up");
    }

    private void setupUI() {
        setPadding(new Insets(20));
        setSpacing(10);
        getStyleClass().add("example-panel");

        // Create title
        Label title = new Label("Label Management Example");
        title.getStyleClass().add("title-label");

        // Create managed labels
        Label statusLabel = labelManager.getOrCreateLabel(this, STATUS_LABEL_ID, "Status: Ready");
        statusLabel.getStyleClass().add("status-label");

        Label counterLabel = labelManager.getOrCreateLabel(this, COUNTER_LABEL_ID, "Counter: 0");
        counterLabel.getStyleClass().add("counter-label");

        Label timeLabel = labelManager.getOrCreateLabel(this, TIME_LABEL_ID, "Time: --:--:--");
        timeLabel.getStyleClass().add("time-label");

        // Add to layout
        getChildren().addAll(title, statusLabel, counterLabel, timeLabel);
    }

    private void startPeriodicUpdates() {
        // Schedule counter updates every second
        updateManager.schedulePeriodicUpdate(
                "counterUpdate", this::updateCounter, 0, 1, TimeUnit.SECONDS);

        // Schedule time updates every 100ms
        updateManager.schedulePeriodicUpdate(
                "timeUpdate", this::updateTime, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void updateCounter() {
        counter++;

        // Update labels using LabelManager
        labelManager.updateLabel(this, COUNTER_LABEL_ID, "Counter: " + counter);

        // Update status based on counter
        String status = counter % 5 == 0 ? "Milestone reached!" : "Counting...";
        labelManager.updateLabel(this, STATUS_LABEL_ID, "Status: " + status);

        log.debug("Counter updated to: {}", counter);
    }

    private void updateTime() {
        String currentTime =
                java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Use queued update for high-frequency updates
        updateManager.queueUpdate(
                "timeDisplay",
                () -> labelManager.updateLabel(this, TIME_LABEL_ID, "Time: " + currentTime));
    }

    /** Demonstrates immediate update functionality. */
    public void performImmediateUpdate(String message) {
        updateManager.executeUpdate(
                "immediateStatusUpdate",
                () -> {
                    labelManager.updateLabel(this, STATUS_LABEL_ID, "Status: " + message);
                    log.info("Immediate update performed: {}", message);
                });
    }

    /** Gets performance metrics for this panel's updates. */
    public void logPerformanceMetrics() {
        log.info("Performance Metrics for ExampleLabelManagedPanel:");

        UIUpdateManager.UpdateMetrics counterMetrics = updateManager.getMetrics("counterUpdate");
        if (counterMetrics != null) {
            log.info("  Counter Updates: {}", counterMetrics);
        }

        UIUpdateManager.UpdateMetrics timeMetrics = updateManager.getMetrics("timeDisplay");
        if (timeMetrics != null) {
            log.info("  Time Updates: {}", timeMetrics);
        }
    }
}
