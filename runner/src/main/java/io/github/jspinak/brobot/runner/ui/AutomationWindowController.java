package io.github.jspinak.brobot.runner.ui;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.window.WindowManager;

/**
 * Controls window behavior during automation execution. Handles minimizing/restoring the main
 * window when automation starts/stops.
 */
@Component
public class AutomationWindowController {
    private static final Logger logger = LoggerFactory.getLogger(AutomationWindowController.class);
    private static final String MAIN_WINDOW_ID = "main";

    private final WindowManager windowManager;
    private boolean wasMinimizedByAutomation = false;
    private boolean autoMinimizeEnabled = true;

    @Autowired
    public AutomationWindowController(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /** Minimizes the main window when automation starts */
    public void minimizeForAutomation() {
        if (!autoMinimizeEnabled) {
            return;
        }

        Platform.runLater(
                () -> {
                    windowManager
                            .getStage(MAIN_WINDOW_ID)
                            .ifPresent(
                                    stage -> {
                                        if (!stage.isIconified()) {
                                            wasMinimizedByAutomation = true;
                                            stage.setIconified(true);
                                            logger.info("Main window minimized for automation");
                                        }
                                    });
                });
    }

    /** Restores the main window when automation completes */
    public void restoreAfterAutomation() {
        if (!wasMinimizedByAutomation) {
            return;
        }

        Platform.runLater(
                () -> {
                    windowManager
                            .getStage(MAIN_WINDOW_ID)
                            .ifPresent(
                                    stage -> {
                                        if (stage.isIconified()) {
                                            stage.setIconified(false);
                                            stage.toFront();
                                            wasMinimizedByAutomation = false;
                                            logger.info("Main window restored after automation");
                                        }
                                    });
                });
    }

    /** Toggles auto-minimize feature */
    public void setAutoMinimizeEnabled(boolean enabled) {
        this.autoMinimizeEnabled = enabled;
        logger.info("Auto-minimize {}", enabled ? "enabled" : "disabled");
    }

    /** Gets whether auto-minimize is enabled */
    public boolean isAutoMinimizeEnabled() {
        return autoMinimizeEnabled;
    }

    /** Shows the main window and brings it to front */
    public void showMainWindow() {
        Platform.runLater(
                () -> {
                    windowManager
                            .getStage(MAIN_WINDOW_ID)
                            .ifPresent(
                                    stage -> {
                                        if (stage.isIconified()) {
                                            stage.setIconified(false);
                                        }
                                        stage.show();
                                        stage.toFront();
                                        stage.requestFocus();
                                    });
                });
    }
}
