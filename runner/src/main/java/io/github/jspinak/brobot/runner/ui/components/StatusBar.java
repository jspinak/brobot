package io.github.jspinak.brobot.runner.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import lombok.Getter;

/**
 * A status bar component for displaying application status information. Can include status
 * messages, progress indicators, and additional status items.
 */
public class StatusBar extends HBox {

    // Main status message
    private final ObjectProperty<String> statusMessage = new SimpleObjectProperty<>("Ready");

    // Progress indicator
    private final ObjectProperty<Double> progress =
            new SimpleObjectProperty<>(-1.0); // -1 = indeterminate

    // Status items on the right side
    @Getter private final ObservableList<Node> statusItems = FXCollections.observableArrayList();

    // UI components
    private final Label messageLabel;
    private final ProgressBar progressBar;
    private final HBox itemsBox;

    /** Creates a new StatusBar. */
    public StatusBar() {
        // Setup layout
        getStyleClass().add("status-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(2, 10, 2, 10));
        setSpacing(10);

        // Create status message label
        messageLabel = new Label();
        messageLabel.textProperty().bind(statusMessage);

        // Create progress bar
        progressBar = new ProgressBar();
        progressBar.progressProperty().bind(progress);
        progressBar.setPrefWidth(100);
        progressBar.setMinHeight(10);
        progressBar.setMaxHeight(10);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        // Create items container
        itemsBox = new HBox();
        itemsBox.setAlignment(Pos.CENTER_RIGHT);
        itemsBox.setSpacing(10);

        // Create spacer to push items to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Add components to the status bar
        getChildren().addAll(messageLabel, progressBar, spacer, itemsBox);

        // Bind visibility of progress bar to progress value
        progress.addListener(
                (obs, oldVal, newVal) -> {
                    boolean visible = newVal != null && newVal >= 0;
                    progressBar.setVisible(visible);
                    progressBar.setManaged(visible);
                });

        // Listen for changes to status items
        statusItems.addListener(
                (ListChangeListener<Node>)
                        change -> {
                            refreshStatusItems();
                        });
    }

    /**
     * Sets the status message.
     *
     * @param message The message to display
     */
    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }

    /**
     * Gets the current status message.
     *
     * @return The status message
     */
    public String getStatusMessage() {
        return statusMessage.get();
    }

    /**
     * Gets the status message property.
     *
     * @return The status message property
     */
    public ObjectProperty<String> statusMessageProperty() {
        return statusMessage;
    }

    /**
     * Sets the progress value.
     *
     * @param value The progress value (0.0 to 1.0, or -1 for indeterminate)
     */
    public void setProgress(double value) {
        progress.set(value);
    }

    /**
     * Gets the current progress value.
     *
     * @return The progress value
     */
    public double getProgress() {
        return progress.get();
    }

    /**
     * Gets the progress property.
     *
     * @return The progress property
     */
    public ObjectProperty<Double> progressProperty() {
        return progress;
    }

    /** Shows the progress bar in indeterminate mode. */
    public void showIndeterminateProgress() {
        progress.set(-1.0);
        progressBar.setVisible(true);
        progressBar.setManaged(true);
    }

    /** Hides the progress bar. */
    public void hideProgress() {
        progressBar.setVisible(false);
        progressBar.setManaged(false);
    }

    /**
     * Adds a status item to the right side of the status bar.
     *
     * @param item The item to add
     */
    public void addStatusItem(Node item) {
        if (item != null) {
            statusItems.add(item);
        }
    }

    /**
     * Creates and adds a status indicator label.
     *
     * @param text The indicator text
     * @param styleClass The style class for the indicator
     * @return The created label
     */
    public Label addStatusIndicator(String text, String styleClass) {
        Label indicator = new Label(text);
        indicator.getStyleClass().addAll("status-indicator", styleClass);
        addStatusItem(indicator);
        return indicator;
    }

    /**
     * Removes a status item.
     *
     * @param item The item to remove
     */
    public void removeStatusItem(Node item) {
        statusItems.remove(item);
    }

    /** Clears all status items. */
    public void clearStatusItems() {
        statusItems.clear();
    }

    /** Refreshes the status items display. */
    private void refreshStatusItems() {
        itemsBox.getChildren().clear();

        for (int i = 0; i < statusItems.size(); i++) {
            // Add separator before items (except first)
            if (i > 0) {
                Separator separator = new Separator();
                separator.setOrientation(javafx.geometry.Orientation.VERTICAL);
                separator.setPadding(new Insets(0, 5, 0, 5));
                itemsBox.getChildren().add(separator);
            }

            // Add the item
            itemsBox.getChildren().add(statusItems.get(i));
        }
    }

    /**
     * Creates a status indicator for online/connected status.
     *
     * @param online True if online, false otherwise
     * @return The status indicator label
     */
    public Label createOnlineIndicator(boolean online) {
        if (online) {
            return addStatusIndicator("Online", "online");
        } else {
            return addStatusIndicator("Offline", "offline");
        }
    }

    /**
     * Creates a memory usage indicator.
     *
     * @return The status indicator label
     */
    public Label createMemoryIndicator() {
        Label memoryLabel = new Label();
        memoryLabel.getStyleClass().add("status-indicator");

        // Update memory usage periodically
        javafx.animation.Timeline timeline =
                new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.seconds(2),
                                event -> updateMemoryUsage(memoryLabel)));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();

        // Initial update
        updateMemoryUsage(memoryLabel);

        addStatusItem(memoryLabel);
        return memoryLabel;
    }

    /**
     * Updates the memory usage display.
     *
     * @param label The label to update
     */
    private void updateMemoryUsage(Label label) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        label.setText(String.format("Memory: %d MB / %d MB", usedMemory, totalMemory));
    }

    /**
     * Creates a status indicator for warning messages.
     *
     * @param message The warning message
     * @return The status indicator label
     */
    public Label createWarningIndicator(String message) {
        Label warningLabel = addStatusIndicator(message, "warning");

        // Make the label disappear after a certain time (if desired)
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(10));
        pause.setOnFinished(event -> removeStatusItem(warningLabel));
        pause.play();

        return warningLabel;
    }
}
