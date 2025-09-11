package io.github.jspinak.brobot.runner.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.services.ScreenshotService;

import atlantafx.base.theme.Styles;

/**
 * Toolbar component for screenshot capture functionality. Provides buttons for various screenshot
 * capture operations.
 */
@Component
public class ScreenshotToolbar extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotToolbar.class);

    @Autowired private ScreenshotService screenshotService;

    private Button captureAppButton;
    private Button captureDesktopButton;
    private Button captureSeriesButton;
    private Button startRecordingButton;
    private Button stopRecordingButton;
    private Label statusLabel;

    private boolean isRecording = false;

    public ScreenshotToolbar() {
        initialize();
    }

    private void initialize() {
        // Create title
        Label titleLabel = new Label("Screenshot Tools");
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        // Create buttons
        createButtons();

        // Create button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10));
        buttonBox
                .getChildren()
                .addAll(
                        captureAppButton,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        captureDesktopButton,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        captureSeriesButton,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        startRecordingButton,
                        stopRecordingButton);

        // Status label
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setPadding(new Insets(5, 10, 5, 10));

        // Configure toolbar
        setSpacing(5);
        setPadding(new Insets(10));
        getStyleClass().addAll("screenshot-toolbar");
        setStyle(
                "-fx-background-color: -color-bg-default; -fx-border-color: -color-border-default;"
                        + " -fx-border-width: 0 0 1 0;");

        getChildren().addAll(titleLabel, buttonBox, statusLabel);
    }

    private void createButtons() {
        // Capture Application Button
        captureAppButton = new Button("📷 App");
        captureAppButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        captureAppButton.setTooltip(new Tooltip("Capture Brobot Runner window (F12)"));
        captureAppButton.setOnAction(e -> captureApplication());

        // Capture Desktop Button
        captureDesktopButton = new Button("🖥️ Desktop");
        captureDesktopButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        captureDesktopButton.setTooltip(new Tooltip("Capture desktop screen"));
        captureDesktopButton.setOnAction(e -> captureDesktop());

        // Capture Series Button
        captureSeriesButton = new Button("📸 Series");
        captureSeriesButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED);
        captureSeriesButton.setTooltip(new Tooltip("Capture timed series of screenshots"));
        captureSeriesButton.setOnAction(e -> showSeriesDialog());

        // Recording Buttons
        startRecordingButton = new Button("⏺️ Record");
        startRecordingButton.getStyleClass().addAll(Styles.ACCENT);
        startRecordingButton.setTooltip(new Tooltip("Start continuous screenshot recording"));
        startRecordingButton.setOnAction(e -> startRecording());

        stopRecordingButton = new Button("⏹️ Stop");
        stopRecordingButton.getStyleClass().addAll(Styles.DANGER);
        stopRecordingButton.setTooltip(new Tooltip("Stop screenshot recording"));
        stopRecordingButton.setOnAction(e -> stopRecording());
        stopRecordingButton.setDisable(true);
    }

    private void captureApplication() {
        updateStatus("Capturing application...");
        String filePath = screenshotService.captureApplication();
        if (filePath != null) {
            updateStatus("App screenshot saved: " + filePath);
            logger.info("Application screenshot saved to: {}", filePath);
        } else {
            updateStatus("Failed to capture application");
            logger.error("Failed to capture application screenshot");
        }
    }

    private void captureDesktop() {
        updateStatus("Capturing desktop...");
        String filePath = screenshotService.captureDesktop("desktop-screenshot");
        if (filePath != null) {
            updateStatus("Desktop screenshot saved: " + filePath);
            logger.info("Desktop screenshot saved to: {}", filePath);
        } else {
            updateStatus("Failed to capture desktop");
            logger.error("Failed to capture desktop screenshot");
        }
    }

    private void showSeriesDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Capture Screenshot Series");
        dialog.setHeaderText("Configure timed screenshot series");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Spinner<Integer> durationSpinner = new Spinner<>(1, 60, 10);
        durationSpinner.setEditable(true);

        Spinner<Double> frequencySpinner = new Spinner<>(0.5, 10.0, 1.0, 0.5);
        frequencySpinner.setEditable(true);

        grid.add(new Label("Duration (seconds):"), 0, 0);
        grid.add(durationSpinner, 1, 0);
        grid.add(new Label("Interval (seconds):"), 0, 1);
        grid.add(frequencySpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait()
                .ifPresent(
                        response -> {
                            if (response == ButtonType.OK) {
                                captureSeries(
                                        durationSpinner.getValue(), frequencySpinner.getValue());
                            }
                        });
    }

    private void captureSeries(int duration, double frequency) {
        updateStatus(String.format("Capturing %d second series...", duration));

        // Run in background thread
        new Thread(
                        () -> {
                            screenshotService.captureTimedSeries(duration, frequency);
                            javafx.application.Platform.runLater(
                                    () -> {
                                        updateStatus(
                                                String.format(
                                                        "Series complete: %d screenshots",
                                                        (int) (duration / frequency)));
                                    });
                        })
                .start();
    }

    private void startRecording() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Start Recording");
        dialog.setHeaderText("Configure screenshot recording");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField baseNameField = new TextField("recording");
        baseNameField.setPromptText("Base filename");

        Spinner<Double> intervalSpinner = new Spinner<>(0.5, 30.0, 2.0, 0.5);
        intervalSpinner.setEditable(true);

        grid.add(new Label("Base filename:"), 0, 0);
        grid.add(baseNameField, 1, 0);
        grid.add(new Label("Interval (seconds):"), 0, 1);
        grid.add(intervalSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait()
                .ifPresent(
                        response -> {
                            if (response == ButtonType.OK) {
                                isRecording = true;
                                startRecordingButton.setDisable(true);
                                stopRecordingButton.setDisable(false);

                                screenshotService.startRecording(
                                        baseNameField.getText(), intervalSpinner.getValue());
                                updateStatus("Recording started");
                            }
                        });
    }

    private void stopRecording() {
        screenshotService.stopRecording();
        isRecording = false;
        startRecordingButton.setDisable(false);
        stopRecordingButton.setDisable(true);
        updateStatus("Recording stopped");
    }

    private void updateStatus(String message) {
        javafx.application.Platform.runLater(() -> statusLabel.setText(message));
    }

    /** Sets the screenshot service (for manual injection if needed). */
    public void setScreenshotService(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }
}
