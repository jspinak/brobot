package io.github.jspinak.brobot.runner.ui.execution;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.runner.execution.ExecutionState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import atlantafx.base.theme.Styles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Status panel component for displaying execution progress and current state.
 * 
 * <p>This panel shows:
 * <ul>
 *   <li>Execution status indicator and label</li>
 *   <li>Progress bar for overall execution progress</li>
 *   <li>Current action being performed</li>
 *   <li>Current state in the state machine</li>
 *   <li>Elapsed time counter</li>
 * </ul>
 * </p>
 */
public class ExecutionStatusPanel extends BrobotCard {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Getter
    private Circle statusIndicator;
    @Getter
    private Label statusLabel;
    @Getter
    private Label currentActionLabel;
    @Getter
    private Label elapsedTimeLabel;
    @Getter
    private Label currentStateLabel;
    @Getter
    private ProgressBar progressBar;

    private LocalDateTime executionStartTime;
    private Timeline elapsedTimeUpdater;
    private final ObjectProperty<State> currentState = new SimpleObjectProperty<>();

    /**
     * Creates a new ExecutionStatusPanel.
     */
    public ExecutionStatusPanel() {
        super("Execution Status");
        setupUI();
    }

    private void setupUI() {
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(16));

        // Status indicator and label
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        statusIndicator = new Circle(8);
        statusIndicator.setFill(Color.LIGHTGRAY);

        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().addAll(Styles.TITLE_4, Styles.TEXT_BOLD);

        elapsedTimeLabel = new Label("00:00:00");
        elapsedTimeLabel.getStyleClass().add("monospace");
        elapsedTimeLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusRow.getChildren().addAll(statusIndicator, statusLabel, spacer, 
            createLabel("Elapsed Time:"), elapsedTimeLabel);

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);

        // Current action and state
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(5);

        infoGrid.add(createLabel("Current Action:"), 0, 0);
        currentActionLabel = new Label("None");
        infoGrid.add(currentActionLabel, 1, 0);

        infoGrid.add(createLabel("Current State:"), 0, 1);
        currentStateLabel = new Label("None");
        infoGrid.add(currentStateLabel, 1, 1);

        GridPane.setHgrow(currentActionLabel, Priority.ALWAYS);
        GridPane.setHgrow(currentStateLabel, Priority.ALWAYS);

        contentBox.getChildren().addAll(statusRow, progressBar, infoGrid);
        
        // Add content box to card
        addContent(contentBox);
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        return label;
    }

    /**
     * Updates the status indicator color and label based on execution state.
     *
     * @param state The current execution state
     */
    public void updateStatus(ExecutionState state) {
        Color indicatorColor;
        String statusText;
        
        switch (state) {
            case IDLE:
                indicatorColor = Color.LIGHTGRAY;
                statusText = "Ready";
                break;
            case RUNNING:
                indicatorColor = Color.LIMEGREEN;
                statusText = "Running";
                break;
            case PAUSED:
                indicatorColor = Color.ORANGE;
                statusText = "Paused";
                break;
            case STOPPING:
                indicatorColor = Color.YELLOW;
                statusText = "Stopping...";
                break;
            case STOPPED:
                indicatorColor = Color.DARKGRAY;
                statusText = "Stopped";
                break;
            case COMPLETED:
                indicatorColor = Color.DODGERBLUE;
                statusText = "Completed";
                break;
            case FAILED:
                indicatorColor = Color.RED;
                statusText = "Failed";
                break;
            default:
                indicatorColor = Color.LIGHTGRAY;
                statusText = state.toString();
        }
        
        statusIndicator.setFill(indicatorColor);
        statusLabel.setText(statusText);
    }

    /**
     * Starts the elapsed time updater.
     */
    public void startElapsedTimeUpdater() {
        executionStartTime = LocalDateTime.now();
        
        elapsedTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateElapsedTime()));
        elapsedTimeUpdater.setCycleCount(Timeline.INDEFINITE);
        elapsedTimeUpdater.play();
    }

    /**
     * Stops the elapsed time updater.
     */
    public void stopElapsedTimeUpdater() {
        if (elapsedTimeUpdater != null) {
            elapsedTimeUpdater.stop();
        }
    }

    private void updateElapsedTime() {
        if (executionStartTime != null) {
            long seconds = ChronoUnit.SECONDS.between(executionStartTime, LocalDateTime.now());
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            
            elapsedTimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, secs));
        }
    }

    /**
     * Updates the current action label.
     *
     * @param action The current action being performed
     */
    public void updateCurrentAction(String action) {
        currentActionLabel.setText(action != null ? action : "None");
    }

    /**
     * Updates the current state label.
     *
     * @param state The current state
     */
    public void updateCurrentState(State state) {
        currentState.set(state);
        currentStateLabel.setText(state != null ? state.getName() : "None");
    }

    /**
     * Updates the progress bar.
     *
     * @param progress Progress value between 0.0 and 1.0
     */
    public void updateProgress(double progress) {
        progressBar.setProgress(progress);
    }

    /**
     * Resets the panel to initial state.
     */
    public void reset() {
        updateStatus(ExecutionState.IDLE);
        updateProgress(0);
        updateCurrentAction(null);
        updateCurrentState(null);
        elapsedTimeLabel.setText("00:00:00");
        stopElapsedTimeUpdater();
    }

    /**
     * Gets the current state property.
     *
     * @return The current state property
     */
    public ObjectProperty<State> currentStateProperty() {
        return currentState;
    }
}