package io.github.jspinak.brobot.runner.ui;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages window state for the automation runner.
 * Handles minimizing/restoring the window when automation starts/stops.
 */
@Component
public class WindowManager {
    private static final Logger logger = LoggerFactory.getLogger(WindowManager.class);
    
    private Stage primaryStage;
    private boolean wasMinimizedByAutomation = false;
    private boolean autoMinimizeEnabled = true;
    
    /**
     * Sets the primary stage to manage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        logger.info("Primary stage registered with WindowManager");
    }
    
    /**
     * Minimizes the window when automation starts
     */
    public void minimizeForAutomation() {
        if (primaryStage == null || !autoMinimizeEnabled) {
            return;
        }
        
        Platform.runLater(() -> {
            if (!primaryStage.isIconified()) {
                wasMinimizedByAutomation = true;
                primaryStage.setIconified(true);
                logger.info("Window minimized for automation");
            }
        });
    }
    
    /**
     * Restores the window when automation completes
     */
    public void restoreAfterAutomation() {
        if (primaryStage == null || !wasMinimizedByAutomation) {
            return;
        }
        
        Platform.runLater(() -> {
            if (primaryStage.isIconified()) {
                primaryStage.setIconified(false);
                primaryStage.toFront();
                wasMinimizedByAutomation = false;
                logger.info("Window restored after automation");
            }
        });
    }
    
    /**
     * Toggles auto-minimize feature
     */
    public void setAutoMinimizeEnabled(boolean enabled) {
        this.autoMinimizeEnabled = enabled;
        logger.info("Auto-minimize {}", enabled ? "enabled" : "disabled");
    }
    
    /**
     * Gets whether auto-minimize is enabled
     */
    public boolean isAutoMinimizeEnabled() {
        return autoMinimizeEnabled;
    }
    
    /**
     * Shows the window and brings it to front
     */
    public void showWindow() {
        if (primaryStage == null) {
            return;
        }
        
        Platform.runLater(() -> {
            if (primaryStage.isIconified()) {
                primaryStage.setIconified(false);
            }
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();
        });
    }
}