package io.github.jspinak.brobot.runner.ui.components;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import atlantafx.base.theme.Styles;

/**
 * A loading indicator component that can be overlaid on any content to show loading state with
 * optional message.
 */
public class LoadingIndicator extends StackPane {

    private final ProgressIndicator spinner;
    private final Label messageLabel;
    private final VBox container;
    private final StackPane overlay;

    /** Creates a new LoadingIndicator. */
    public LoadingIndicator() {
        this("Loading...");
    }

    /**
     * Creates a new LoadingIndicator with a custom message.
     *
     * @param message The loading message to display
     */
    public LoadingIndicator(String message) {
        // Create semi-transparent overlay
        overlay = new StackPane();
        overlay.getStyleClass().add("loading-overlay");
        overlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9);");

        // Create spinner
        spinner = new ProgressIndicator();
        spinner.getStyleClass().add("loading-spinner");
        spinner.setPrefSize(50, 50);

        // Add rotation animation
        RotateTransition rotation = new RotateTransition(Duration.seconds(1), spinner);
        rotation.setByAngle(360);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.play();

        // Create message label
        messageLabel = new Label(message);
        messageLabel.getStyleClass().addAll(Styles.TEXT_MUTED, "loading-message");

        // Container for spinner and message
        container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(spinner, messageLabel);

        overlay.getChildren().add(container);
        getChildren().add(overlay);

        // Initially hidden
        setVisible(false);
        setManaged(false);
    }

    /** Shows the loading indicator. */
    public void show() {
        setVisible(true);
        setManaged(true);
    }

    /** Hides the loading indicator. */
    public void hide() {
        setVisible(false);
        setManaged(false);
    }

    /**
     * Sets the loading message.
     *
     * @param message The message to display
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Gets the current loading message.
     *
     * @return The loading message
     */
    public String getMessage() {
        return messageLabel.getText();
    }

    /**
     * Creates a loading indicator that overlays the given content.
     *
     * @param content The content to overlay
     * @param message The loading message
     * @return A StackPane containing the content and loading indicator
     */
    public static StackPane wrapWithLoading(Node content, String message) {
        StackPane wrapper = new StackPane();
        LoadingIndicator loadingIndicator = new LoadingIndicator(message);
        wrapper.getChildren().addAll(content, loadingIndicator);
        return wrapper;
    }

    /**
     * Creates a loading indicator that overlays the given content.
     *
     * @param content The content to overlay
     * @return A StackPane containing the content and loading indicator
     */
    public static StackPane wrapWithLoading(Node content) {
        return wrapWithLoading(content, "Loading...");
    }
}
