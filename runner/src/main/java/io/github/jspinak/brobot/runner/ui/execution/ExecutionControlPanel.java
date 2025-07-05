package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control panel component for managing automation execution.
 * 
 * <p>This panel provides play, pause, and stop buttons for controlling
 * the execution flow of automation scripts. It automatically updates
 * button states based on the current execution state.</p>
 */
public class ExecutionControlPanel extends HBox {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionControlPanel.class);

    private final EventBus eventBus;
    private final AutomationOrchestrator automationOrchestrator;

    @Getter
    private Button playButton;
    @Getter
    private Button pauseButton;
    @Getter
    private Button stopButton;

    /**
     * Creates a new ExecutionControlPanel.
     *
     * @param eventBus The event bus for communication
     * @param automationExecutor The automation executor for controlling execution
     */
    public ExecutionControlPanel(EventBus eventBus, AutomationOrchestrator automationOrchestrator) {
        this.eventBus = eventBus;
        this.automationOrchestrator = automationOrchestrator;
        
        setupUI();
    }

    private void setupUI() {
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label titleLabel = new Label("Execution Control");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        playButton = new Button("▶ Play");
        playButton.getStyleClass().add("button-primary");
        playButton.setOnAction(e -> resumeExecution());

        pauseButton = new Button("⏸ Pause");
        pauseButton.setOnAction(e -> pauseExecution());

        stopButton = new Button("⏹ Stop");
        stopButton.getStyleClass().add("button-danger");
        stopButton.setOnAction(e -> stopExecution());

        // Initial button states
        updateButtonStates(ExecutionState.IDLE);

        getChildren().addAll(titleLabel, spacer, playButton, pauseButton, stopButton);
    }

    /**
     * Updates the enabled/disabled state of control buttons based on execution state.
     *
     * @param state The current execution state
     */
    public void updateButtonStates(ExecutionState state) {
        switch (state) {
            case IDLE, STOPPED, COMPLETED, ERROR:
                playButton.setDisable(false);
                pauseButton.setDisable(true);
                stopButton.setDisable(true);
                break;
            case RUNNING:
                playButton.setDisable(true);
                pauseButton.setDisable(false);
                stopButton.setDisable(false);
                break;
            case PAUSED:
                playButton.setDisable(false);
                pauseButton.setDisable(true);
                stopButton.setDisable(false);
                break;
            case STOPPING:
                playButton.setDisable(true);
                pauseButton.setDisable(true);
                stopButton.setDisable(true);
                break;
        }
    }

    private void resumeExecution() {
        logger.info("Resuming execution");
        if (automationOrchestrator.isPaused()) {
            automationOrchestrator.resume();
        } else {
            automationOrchestrator.startExecution();
        }
    }

    private void pauseExecution() {
        logger.info("Pausing execution");
        automationOrchestrator.pause();
    }

    private void stopExecution() {
        logger.info("Stopping execution");
        automationOrchestrator.stop();
    }
}