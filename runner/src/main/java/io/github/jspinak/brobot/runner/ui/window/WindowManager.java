package io.github.jspinak.brobot.runner.ui.window;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Manages the application windows and dialogs.
 * Handles window creation, positioning, and state persistence.
 */
@Component
public class WindowManager {
    private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);

    private final EventBus eventBus;
    private final ThemeManager themeManager;

    // Map of registered windows
    private final Map<String, Stage> stages = new HashMap<>();

    // Preferences for window state persistence
    private final Preferences preferences = Preferences.userNodeForPackage(WindowManager.class);

    @Autowired
    public WindowManager(EventBus eventBus, ThemeManager themeManager) {
        this.eventBus = eventBus;
        this.themeManager = themeManager;
    }

    @PostConstruct
    public void initialize() {
        logger.info("WindowManager initialized");
    }

    /**
     * Registers a stage with the window manager.
     *
     * @param id The unique ID for the stage
     * @param stage The stage to register
     */
    public void registerStage(String id, Stage stage) {
        if (id == null || stage == null) {
            logger.warn("Attempted to register null ID or stage");
            return;
        }

        if (stages.containsKey(id)) {
            logger.warn("Stage with ID {} already registered, overwriting", id);
        }

        stages.put(id, stage);

        // Apply theme to the stage
        if (stage.getScene() != null) {
            themeManager.registerScene(stage.getScene());
        }

        // Setup window state persistence
        setupWindowStatePersistence(id, stage);

        logger.debug("Registered stage: {}", id);
    }

    /**
     * Gets a registered stage by ID.
     *
     * @param id The ID of the stage
     * @return An Optional containing the stage, or empty if not found
     */
    public Optional<Stage> getStage(String id) {
        return Optional.ofNullable(stages.get(id));
    }

    /**
     * Creates a new window.
     *
     * @param id The unique ID for the window
     * @param title The window title
     * @param content The window content
     * @param width The window width
     * @param height The window height
     * @return The created Stage
     */
    public Stage createWindow(String id, String title, javafx.scene.Parent content,
                              double width, double height) {
        Stage stage = new Stage();
        stage.setTitle(title);

        Scene scene = new Scene(content, width, height);
        stage.setScene(scene);

        // Apply theme to the new scene
        themeManager.registerScene(scene);

        // Register the stage
        registerStage(id, stage);

        return stage;
    }

    /**
     * Creates a new dialog.
     *
     * @param id The unique ID for the dialog
     * @param title The dialog title
     * @param content The dialog content
     * @param width The dialog width
     * @param height The dialog height
     * @param modality The dialog modality
     * @param owner The owner window for the dialog
     * @return The created Stage
     */
    public Stage createDialog(String id, String title, javafx.scene.Parent content,
                              double width, double height, Modality modality, Stage owner) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(modality);
        stage.initOwner(owner);

        Scene scene = new Scene(content, width, height);
        stage.setScene(scene);

        // Apply theme to the new scene
        themeManager.registerScene(scene);

        // Register the stage
        registerStage(id, stage);

        // Center on owner
        stage.setOnShown(e -> centerOnOwner(stage, owner));

        return stage;
    }

    /**
     * Centers a stage on its owner.
     *
     * @param stage The stage to center
     * @param owner The owner stage
     */
    private void centerOnOwner(Stage stage, Stage owner) {
        if (owner != null) {
            stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
        } else {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        }
    }

    /**
     * Creates a utility window with no decorations.
     *
     * @param id The unique ID for the window
     * @param content The window content
     * @param width The window width
     * @param height The window height
     * @return The created Stage
     */
    public Stage createUtilityWindow(String id, javafx.scene.Parent content,
                                     double width, double height) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);

        Scene scene = new Scene(content, width, height);
        stage.setScene(scene);

        // Apply theme to the new scene
        themeManager.registerScene(scene);

        // Register the stage
        registerStage(id, stage);

        return stage;
    }

    /**
     * Closes a window by ID.
     *
     * @param id The ID of the window to close
     */
    public void closeWindow(String id) {
        Stage stage = stages.get(id);
        if (stage != null) {
            stage.close();
            stages.remove(id);
            logger.debug("Closed window: {}", id);
        }
    }

    /**
     * Closes all windows.
     */
    public void closeAllWindows() {
        for (Stage stage : stages.values()) {
            stage.close();
        }
        stages.clear();
        logger.debug("Closed all windows");
    }

    /**
     * Sets up window state persistence for a stage.
     *
     * @param id The window ID
     * @param stage The stage
     */
    private void setupWindowStatePersistence(String id, Stage stage) {
        // Load saved state
        double x = preferences.getDouble(id + ".x", -1);
        double y = preferences.getDouble(id + ".y", -1);
        double width = preferences.getDouble(id + ".width", stage.getWidth());
        double height = preferences.getDouble(id + ".height", stage.getHeight());
        boolean maximized = preferences.getBoolean(id + ".maximized", false);

        // Apply saved state if exists
        if (x >= 0 && y >= 0) {
            // Check if the saved position is still on a screen
            Rectangle2D bounds = new Rectangle2D(x, y, width, height);
            boolean isOnScreen = false;

            for (javafx.stage.Screen screen : javafx.stage.Screen.getScreens()) {
                if (screen.getVisualBounds().intersects(bounds)) {
                    isOnScreen = true;
                    break;
                }
            }

            if (isOnScreen) {
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(width);
                stage.setHeight(height);

                if (maximized) {
                    stage.setMaximized(true);
                }
            }
        }

        // Save state on window close
        stage.setOnCloseRequest(event -> {
            saveWindowState(id, stage);
        });
    }

    /**
     * Saves the window state to preferences.
     *
     * @param id The window ID
     * @param stage The stage
     */
    private void saveWindowState(String id, Stage stage) {
        if (stage.isMaximized()) {
            preferences.putBoolean(id + ".maximized", true);
        } else {
            preferences.putDouble(id + ".x", stage.getX());
            preferences.putDouble(id + ".y", stage.getY());
            preferences.putDouble(id + ".width", stage.getWidth());
            preferences.putDouble(id + ".height", stage.getHeight());
            preferences.putBoolean(id + ".maximized", false);
        }

        logger.debug("Saved window state for: {}", id);
    }

    /**
     * Shows a message dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     */
    public void showMessageDialog(Stage owner, String title, String message) {
        DialogFactory.createMessageDialog(owner, title, message);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @return true if confirmed, false otherwise
     */
    public boolean showConfirmDialog(Stage owner, String title, String message) {
        return DialogFactory.createConfirmDialog(owner, title, message);
    }

    /**
     * Shows an error dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The error message
     * @param exception The exception that caused the error, or null if none
     */
    public void showErrorDialog(Stage owner, String title, String message, Exception exception) {
        DialogFactory.createErrorDialog(owner, title, message, exception);
    }

    /**
     * Shows an input dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @param defaultValue The default input value
     * @return The input value, or empty if cancelled
     */
    public Optional<String> showInputDialog(Stage owner, String title, String message, String defaultValue) {
        return DialogFactory.createInputDialog(owner, title, message, defaultValue);
    }

    /**
     * Shows a progress dialog.
     *
     * @param owner The owner window
     * @param title The dialog title
     * @param message The message to display
     * @return The progress dialog
     */
    public DialogFactory.ProgressDialog showProgressDialog(Stage owner, String title, String message) {
        return DialogFactory.createProgressDialog(owner, title, message);
    }
}