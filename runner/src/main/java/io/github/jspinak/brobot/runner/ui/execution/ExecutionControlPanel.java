package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import atlantafx.base.theme.Styles;

/**
 * Control panel component for managing automation execution.
 * 
 * <p>This panel provides play, pause, and stop buttons for controlling
 * the execution flow of automation scripts. It automatically updates
 * button states based on the current execution state.</p>
 */
public class ExecutionControlPanel extends BrobotCard {
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
        super("Execution Control");
        this.eventBus = eventBus;
        this.automationOrchestrator = automationOrchestrator;
        
        setupUI();
    }

    private void setupUI() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(16));

        playButton = new Button("Play");
        playButton.getStyleClass().add(Styles.ACCENT);
        playButton.setOnAction(e -> resumeExecution());

        pauseButton = new Button("Pause");
        pauseButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        pauseButton.setOnAction(e -> pauseExecution());

        stopButton = new Button("Stop");
        stopButton.getStyleClass().add(Styles.DANGER);
        stopButton.setOnAction(e -> stopExecution());

        // Initial button states
        updateButtonStates(ExecutionState.IDLE);

        buttonBox.getChildren().addAll(playButton, pauseButton, stopButton);
        
        // Add button box to card
        addContent(buttonBox);
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