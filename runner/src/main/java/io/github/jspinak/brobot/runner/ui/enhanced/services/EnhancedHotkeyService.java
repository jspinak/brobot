package io.github.jspinak.brobot.runner.ui.enhanced.services;

import java.util.function.Consumer;
import javafx.scene.Scene;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;
import io.github.jspinak.brobot.runner.ui.HotkeyConfigDialog;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing hotkeys in the enhanced automation panel. Handles hotkey registration,
 * configuration, and action binding.
 */
@Slf4j
@Service
public class EnhancedHotkeyService {

    private final HotkeyManager hotkeyManager;

    @Getter @Setter private HotkeyConfiguration configuration;

    private Consumer<String> logHandler;

    /** Configuration for hotkey behavior. */
    @Getter
    @Setter
    public static class HotkeyConfiguration {
        private boolean autoRegisterWithScene;
        private boolean showConfigDialog;
        private boolean logHotkeyActions;

        public static HotkeyConfigurationBuilder builder() {
            return new HotkeyConfigurationBuilder();
        }

        public static class HotkeyConfigurationBuilder {
            private boolean autoRegisterWithScene = true;
            private boolean showConfigDialog = true;
            private boolean logHotkeyActions = true;

            public HotkeyConfigurationBuilder autoRegisterWithScene(boolean autoRegister) {
                this.autoRegisterWithScene = autoRegister;
                return this;
            }

            public HotkeyConfigurationBuilder showConfigDialog(boolean show) {
                this.showConfigDialog = show;
                return this;
            }

            public HotkeyConfigurationBuilder logHotkeyActions(boolean log) {
                this.logHotkeyActions = log;
                return this;
            }

            public HotkeyConfiguration build() {
                HotkeyConfiguration config = new HotkeyConfiguration();
                config.autoRegisterWithScene = autoRegisterWithScene;
                config.showConfigDialog = showConfigDialog;
                config.logHotkeyActions = logHotkeyActions;
                return config;
            }
        }
    }

    @Autowired
    public EnhancedHotkeyService(HotkeyManager hotkeyManager) {
        this.hotkeyManager = hotkeyManager;
        this.configuration = HotkeyConfiguration.builder().build();
    }

    /**
     * Registers hotkey actions with their handlers.
     *
     * @param pauseAction Handler for pause action
     * @param resumeAction Handler for resume action
     * @param stopAction Handler for stop action
     * @param togglePauseAction Handler for toggle pause/resume action
     */
    public void registerHotkeyActions(
            Runnable pauseAction,
            Runnable resumeAction,
            Runnable stopAction,
            Runnable togglePauseAction) {
        log.debug("Registering hotkey actions");

        hotkeyManager.registerAction(
                HotkeyAction.PAUSE,
                () -> {
                    logHotkeyAction("PAUSE");
                    pauseAction.run();
                });

        hotkeyManager.registerAction(
                HotkeyAction.RESUME,
                () -> {
                    logHotkeyAction("RESUME");
                    resumeAction.run();
                });

        hotkeyManager.registerAction(
                HotkeyAction.STOP,
                () -> {
                    logHotkeyAction("STOP");
                    stopAction.run();
                });

        hotkeyManager.registerAction(
                HotkeyAction.TOGGLE_PAUSE,
                () -> {
                    logHotkeyAction("TOGGLE_PAUSE");
                    togglePauseAction.run();
                });
    }

    /**
     * Registers hotkeys with the scene when available.
     *
     * @param scene The scene to register with
     */
    public void registerWithScene(Scene scene) {
        if (scene != null && configuration.autoRegisterWithScene) {
            hotkeyManager.registerWithScene(scene);
            String toggleKey = hotkeyManager.getHotkeyDisplayString(HotkeyAction.TOGGLE_PAUSE);
            logInfo("Hotkeys registered. Use " + toggleKey + " to pause/resume automation.");
        }
    }

    /**
     * Shows the hotkey configuration dialog.
     *
     * @param statusPanel The status panel to update after configuration
     * @return true if configuration was saved
     */
    public boolean showConfigurationDialog(AutomationStatusPanel statusPanel) {
        if (!configuration.showConfigDialog) {
            return false;
        }

        HotkeyConfigDialog dialog = new HotkeyConfigDialog(hotkeyManager);
        dialog.showAndWait();

        if (dialog.isSaved()) {
            if (statusPanel != null) {
                statusPanel.updateHotkeyDisplay();
            }
            logInfo("Hotkey configuration updated");
            return true;
        }

        return false;
    }

    /**
     * Gets the display string for a specific hotkey action.
     *
     * @param action The hotkey action
     * @return The display string
     */
    public String getHotkeyDisplayString(HotkeyAction action) {
        return hotkeyManager.getHotkeyDisplayString(action);
    }

    /**
     * Checks if a hotkey is registered for the given action.
     *
     * @param action The hotkey action
     * @return true if registered
     */
    public boolean isHotkeyRegistered(HotkeyAction action) {
        return hotkeyManager.getHotkey(action) != null;
    }

    /**
     * Sets the log handler for hotkey events.
     *
     * @param logHandler The log handler
     */
    public void setLogHandler(Consumer<String> logHandler) {
        this.logHandler = logHandler;
    }

    private void logHotkeyAction(String action) {
        if (configuration.logHotkeyActions) {
            log.debug("Hotkey action triggered: {}", action);
        }
    }

    private void logInfo(String message) {
        log.info(message);
        if (logHandler != null) {
            logHandler.accept(message);
        }
    }
}
