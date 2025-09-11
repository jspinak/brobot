package io.github.jspinak.brobot.runner.ui.illustration.streaming;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.UIUpdateEvent;
import io.github.jspinak.brobot.runner.ui.illustration.IllustrationViewer;

/**
 * Panel for displaying real-time illustration streams during automation execution.
 *
 * <p>This panel provides a live view of actions as they execute, with features:
 *
 * <ul>
 *   <li>Auto-updating illustration display
 *   <li>Queue status and performance metrics
 *   <li>Filtering and pause controls
 *   <li>History navigation
 * </ul>
 *
 * @see IllustrationStreamService
 * @see IllustrationViewer
 */
@Component
public class IllustrationStreamPanel extends BorderPane {

    private final IllustrationStreamService streamService;
    private final EventBus eventBus;

    // UI Components
    private final IllustrationViewer viewer;
    private final Label statusLabel;
    private final Label queueLabel;
    private final Label performanceLabel;
    private final ToggleButton pauseButton;
    private final CheckBox autoFitCheckBox;
    private final ComboBox<FilterMode> filterCombo;
    private final ListView<StreamHistoryItem> historyList;

    // State
    private final Queue<StreamHistoryItem> history = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 100;
    private boolean isPaused = false;

    // Animation
    private FadeTransition fadeTransition;

    @Autowired
    public IllustrationStreamPanel(IllustrationStreamService streamService, EventBus eventBus) {
        this.streamService = streamService;
        this.eventBus = eventBus;

        // Initialize components
        viewer = new IllustrationViewer();
        statusLabel = new Label("Stream Active");
        queueLabel = new Label("Queue: 0");
        performanceLabel = new Label("Processing: 0ms");
        pauseButton = new ToggleButton("Pause");
        autoFitCheckBox = new CheckBox("Auto-fit");
        filterCombo = new ComboBox<>();
        historyList = new ListView<>();

        // Setup UI
        setupUI();

        // Setup stream consumer
        setupStreamConsumer();

        // Subscribe to events
        subscribeToEvents();
    }

    /** Sets up the UI layout. */
    private void setupUI() {
        // Top controls
        HBox topControls = createTopControls();

        // Center - illustration viewer with overlay
        StackPane centerStack = new StackPane();

        // Status overlay
        VBox statusOverlay = createStatusOverlay();
        StackPane.setAlignment(statusOverlay, Pos.TOP_RIGHT);
        StackPane.setMargin(statusOverlay, new Insets(10));

        centerStack.getChildren().addAll(viewer, statusOverlay);

        // Right - history panel
        VBox historyPanel = createHistoryPanel();

        // Layout
        setTop(topControls);
        setCenter(centerStack);
        setRight(historyPanel);

        // Styling
        getStyleClass().add("illustration-stream-panel");
    }

    /** Creates the top control bar. */
    private HBox createTopControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);

        // Pause button
        pauseButton.setOnAction(
                e -> {
                    isPaused = pauseButton.isSelected();
                    updateStatus();
                });

        // Auto-fit checkbox
        autoFitCheckBox.setSelected(true);

        // Filter combo
        filterCombo.getItems().addAll(FilterMode.values());
        filterCombo.setValue(FilterMode.ALL);
        filterCombo.setOnAction(e -> applyFilter());

        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearHistory());

        // Separator
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Performance info
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren()
                .addAll(
                        pauseButton,
                        new Separator(),
                        autoFitCheckBox,
                        new Label("Filter:"),
                        filterCombo,
                        clearButton,
                        spacer,
                        performanceLabel);

        return controls;
    }

    /** Creates the status overlay panel. */
    private VBox createStatusOverlay() {
        VBox overlay = new VBox(5);
        overlay.setPadding(new Insets(10));
        overlay.setMaxWidth(200);
        overlay.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7);"
                        + "-fx-background-radius: 5;"
                        + "-fx-text-fill: white;");

        statusLabel.setTextFill(Color.WHITE);
        queueLabel.setTextFill(Color.WHITE);

        overlay.getChildren().addAll(statusLabel, queueLabel);

        // Make semi-transparent
        overlay.setOpacity(0.8);

        return overlay;
    }

    /** Creates the history panel. */
    private VBox createHistoryPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: -color-bg-subtle;");

        Label historyLabel = new Label("History");
        historyLabel.setStyle("-fx-font-weight: bold;");

        // History list
        historyList.setCellFactory(lv -> new StreamHistoryCell());
        historyList.setOnMouseClicked(
                e -> {
                    if (e.getClickCount() == 2) {
                        StreamHistoryItem item = historyList.getSelectionModel().getSelectedItem();
                        if (item != null) {
                            displayHistoryItem(item);
                        }
                    }
                });

        VBox.setVgrow(historyList, Priority.ALWAYS);

        panel.getChildren().addAll(historyLabel, historyList);

        return panel;
    }

    /** Sets up the stream consumer to handle incoming illustrations. */
    private void setupStreamConsumer() {
        streamService.setStreamConsumer(
                event -> {
                    if (!isPaused) {
                        displayStreamEvent(event);
                        addToHistory(event);
                        updateMetrics(event);
                    }
                });
    }

    /** Subscribes to relevant events. */
    private void subscribeToEvents() {
        // Subscribe to UI update events for illustration captures
        eventBus.subscribe(
                BrobotEvent.EventType.UI_STATE_CHANGED,
                event -> {
                    if (event instanceof UIUpdateEvent) {
                        UIUpdateEvent uiEvent = (UIUpdateEvent) event;
                        if ("ILLUSTRATION_CAPTURED".equals(uiEvent.getUpdateType())
                                && uiEvent.getUpdateData() instanceof IllustrationStreamEvent) {
                            displayStreamEvent((IllustrationStreamEvent) uiEvent.getUpdateData());
                        }
                    }
                });
    }

    /** Displays a stream event in the viewer. */
    private void displayStreamEvent(IllustrationStreamEvent event) {
        Platform.runLater(
                () -> {
                    // Load illustration
                    viewer.loadIllustration(event.getImage(), event.getMetadata());

                    // Auto-fit if enabled
                    if (autoFitCheckBox.isSelected()) {
                        viewer.fitToCanvas();
                    }

                    // Animate transition
                    animateTransition();
                });
    }

    /** Adds an event to the history. */
    private void addToHistory(IllustrationStreamEvent event) {
        Platform.runLater(
                () -> {
                    // Create history item
                    StreamHistoryItem item = new StreamHistoryItem(event);

                    // Add to queue with size limit
                    history.offer(item);
                    if (history.size() > MAX_HISTORY_SIZE) {
                        history.poll();
                    }

                    // Update list based on filter
                    if (shouldShowInHistory(item)) {
                        historyList.getItems().add(0, item); // Add to top

                        // Limit list size
                        if (historyList.getItems().size() > MAX_HISTORY_SIZE) {
                            historyList.getItems().remove(historyList.getItems().size() - 1);
                        }
                    }
                });
    }

    /** Updates performance metrics. */
    private void updateMetrics(IllustrationStreamEvent event) {
        Platform.runLater(
                () -> {
                    queueLabel.setText("Queue: " + event.getQueueSize());
                    performanceLabel.setText("Processing: " + event.getProcessingTimeMs() + "ms");
                });
    }

    /** Updates the status display. */
    private void updateStatus() {
        String status = isPaused ? "Stream Paused" : "Stream Active";
        statusLabel.setText(status);
        statusLabel.setTextFill(isPaused ? Color.YELLOW : Color.LIGHTGREEN);
    }

    /** Animates the illustration transition. */
    private void animateTransition() {
        if (fadeTransition != null) {
            fadeTransition.stop();
        }

        fadeTransition = new FadeTransition(Duration.millis(300), viewer);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    /** Displays a history item in the viewer. */
    private void displayHistoryItem(StreamHistoryItem item) {
        if (item.getEvent() != null) {
            displayStreamEvent(item.getEvent());
        }
    }

    /** Applies the current filter to the history. */
    private void applyFilter() {
        historyList.getItems().clear();

        history.stream()
                .filter(this::shouldShowInHistory)
                .forEach(item -> historyList.getItems().add(item));
    }

    /** Checks if an item should be shown based on current filter. */
    private boolean shouldShowInHistory(StreamHistoryItem item) {
        FilterMode filter = filterCombo.getValue();
        if (filter == null || filter == FilterMode.ALL) {
            return true;
        }

        IllustrationStreamEvent event = item.getEvent();
        if (event == null || event.getMetadata() == null) {
            return false;
        }

        switch (filter) {
            case FAILURES_ONLY:
                return !event.getMetadata().isSuccess();
            case HIGH_PRIORITY:
                return event.isHighPriority();
            case CLICKS_ONLY:
                return "CLICK".equals(event.getMetadata().getActionType());
            case FINDS_ONLY:
                return "FIND".equals(event.getMetadata().getActionType());
            default:
                return true;
        }
    }

    /** Clears the history. */
    private void clearHistory() {
        history.clear();
        historyList.getItems().clear();
    }

    /** Filter modes for the history display. */
    public enum FilterMode {
        ALL("All"),
        FAILURES_ONLY("Failures Only"),
        HIGH_PRIORITY("High Priority"),
        CLICKS_ONLY("Clicks Only"),
        FINDS_ONLY("Finds Only");

        private final String displayName;

        FilterMode(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /** History item wrapper. */
    private static class StreamHistoryItem {
        private final IllustrationStreamEvent event;
        private final String displayText;

        public StreamHistoryItem(IllustrationStreamEvent event) {
            this.event = event;

            // Create display text
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String time = event.getTimestamp().format(formatter);
            String action =
                    event.getMetadata() != null ? event.getMetadata().getActionType() : "Unknown";
            String status =
                    event.getMetadata() != null && event.getMetadata().isSuccess() ? "✓" : "✗";

            this.displayText = String.format("%s %s %s", time, action, status);
        }

        public IllustrationStreamEvent getEvent() {
            return event;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    /** Custom cell for history items. */
    private static class StreamHistoryCell extends ListCell<StreamHistoryItem> {
        @Override
        protected void updateItem(StreamHistoryItem item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());

                // Style based on success/failure
                if (item.getEvent() != null
                        && item.getEvent().getMetadata() != null
                        && !item.getEvent().getMetadata().isSuccess()) {
                    setTextFill(Color.RED);
                } else {
                    setTextFill(null);
                }
            }
        }
    }
}
