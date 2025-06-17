package io.github.jspinak.brobot.runner.ui.components;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modern status bar with clean design and useful indicators.
 */
@Component
@RequiredArgsConstructor
public class ModernStatusBar extends HBox {
    private final EventBus eventBus;
    private final IconRegistry iconRegistry;
    
    private Label statusMessage;
    private Label memoryLabel;
    private ProgressBar memoryProgress;
    private Circle connectionIndicator;
    private Label connectionLabel;
    private Label clockLabel;
    
    private Timeline memoryUpdateTimeline;
    private Timeline clockUpdateTimeline;
    
    public void initialize() {
        getStyleClass().add("modern-status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(4, 16, 4, 16));
        setSpacing(16);
        setPrefHeight(28);
        setMinHeight(28);
        setMaxHeight(28);
        
        // Status message
        statusMessage = new Label("Ready");
        statusMessage.getStyleClass().add("status-message");
        
        // Spacer
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        // Connection indicator
        HBox connectionBox = createConnectionIndicator();
        
        // Separator
        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep1.getStyleClass().add("status-separator");
        
        // Memory indicator
        HBox memoryBox = createMemoryIndicator();
        
        // Separator
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep2.getStyleClass().add("status-separator");
        
        // Clock
        clockLabel = new Label();
        clockLabel.getStyleClass().add("status-clock");
        startClockUpdate();
        
        getChildren().addAll(
            statusMessage,
            leftSpacer,
            connectionBox,
            sep1,
            memoryBox,
            sep2,
            clockLabel
        );
    }
    
    private HBox createConnectionIndicator() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        
        connectionIndicator = new Circle(4);
        connectionIndicator.setFill(Color.web("#28A745"));
        connectionIndicator.getStyleClass().add("connection-indicator");
        
        connectionLabel = new Label("Connected");
        connectionLabel.getStyleClass().add("status-label");
        
        box.getChildren().addAll(connectionIndicator, connectionLabel);
        return box;
    }
    
    private HBox createMemoryIndicator() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        
        memoryLabel = new Label("Memory: 0 MB");
        memoryLabel.getStyleClass().add("status-label");
        
        memoryProgress = new ProgressBar(0);
        memoryProgress.setPrefWidth(80);
        memoryProgress.getStyleClass().add("memory-progress");
        
        box.getChildren().addAll(memoryLabel, memoryProgress);
        
        // Start memory monitoring
        startMemoryMonitoring();
        
        return box;
    }
    
    private void startMemoryMonitoring() {
        memoryUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> updateMemoryInfo())
        );
        memoryUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        memoryUpdateTimeline.play();
        
        // Initial update
        updateMemoryInfo();
    }
    
    private void updateMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed() / (1024 * 1024); // Convert to MB
        long max = heapUsage.getMax() / (1024 * 1024);
        double percentage = (double) used / max;
        
        Platform.runLater(() -> {
            memoryLabel.setText(String.format("Memory: %d MB", used));
            memoryProgress.setProgress(percentage);
            
            // Add tooltip with details
            Tooltip tooltip = new Tooltip(String.format(
                "Heap Memory:\nUsed: %d MB\nMax: %d MB\nUsage: %.1f%%",
                used, max, percentage * 100
            ));
            Tooltip.install(memoryProgress, tooltip);
            
            // Change color based on usage
            if (percentage > 0.9) {
                memoryProgress.setStyle("-fx-accent: #DC3545;"); // Red
            } else if (percentage > 0.7) {
                memoryProgress.setStyle("-fx-accent: #FFC107;"); // Yellow
            } else {
                memoryProgress.setStyle("-fx-accent: #007ACC;"); // Default blue
            }
        });
    }
    
    private void startClockUpdate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        clockUpdateTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {
                Platform.runLater(() -> {
                    clockLabel.setText(LocalDateTime.now().format(formatter));
                });
            })
        );
        clockUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        clockUpdateTimeline.play();
        
        // Initial update
        clockLabel.setText(LocalDateTime.now().format(formatter));
    }
    
    public void setStatusMessage(String message) {
        Platform.runLater(() -> statusMessage.setText(message));
    }
    
    public void setConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                connectionIndicator.setFill(Color.web("#28A745"));
                connectionLabel.setText("Connected");
            } else {
                connectionIndicator.setFill(Color.web("#DC3545"));
                connectionLabel.setText("Disconnected");
            }
        });
    }
    
    public void cleanup() {
        if (memoryUpdateTimeline != null) {
            memoryUpdateTimeline.stop();
        }
        if (clockUpdateTimeline != null) {
            clockUpdateTimeline.stop();
        }
    }
}