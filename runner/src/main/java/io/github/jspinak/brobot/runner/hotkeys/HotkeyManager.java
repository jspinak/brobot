package io.github.jspinak.brobot.runner.hotkeys;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manages global hotkeys for automation control. Provides configurable key combinations for pause,
 * resume, and stop operations.
 */
@Component
public class HotkeyManager {
    private static final Logger logger = LoggerFactory.getLogger(HotkeyManager.class);

    // Default hotkeys
    private static final KeyCombination DEFAULT_PAUSE_KEY =
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination DEFAULT_RESUME_KEY =
            new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination DEFAULT_STOP_KEY =
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination DEFAULT_TOGGLE_PAUSE_KEY =
            new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);

    private final Map<HotkeyAction, KeyCombination> hotkeys = new HashMap<>();
    private final Map<HotkeyAction, Runnable> actions = new HashMap<>();
    private Scene registeredScene;

    public enum HotkeyAction {
        PAUSE("Pause Automation"),
        RESUME("Resume Automation"),
        STOP("Stop Automation"),
        TOGGLE_PAUSE("Toggle Pause/Resume");

        private final String displayName;

        HotkeyAction(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public HotkeyManager() {
        // Initialize with default hotkeys
        hotkeys.put(HotkeyAction.PAUSE, DEFAULT_PAUSE_KEY);
        hotkeys.put(HotkeyAction.RESUME, DEFAULT_RESUME_KEY);
        hotkeys.put(HotkeyAction.STOP, DEFAULT_STOP_KEY);
        hotkeys.put(HotkeyAction.TOGGLE_PAUSE, DEFAULT_TOGGLE_PAUSE_KEY);
    }

    /** Registers hotkey actions */
    public void registerAction(HotkeyAction action, Runnable handler) {
        actions.put(action, handler);
        logger.info("Registered action for {}", action);
    }

    /** Registers the hotkeys with a JavaFX scene */
    public void registerWithScene(Scene scene) {
        if (registeredScene != null) {
            unregisterFromScene();
        }

        registeredScene = scene;

        // Register all hotkeys
        for (Map.Entry<HotkeyAction, KeyCombination> entry : hotkeys.entrySet()) {
            HotkeyAction action = entry.getKey();
            KeyCombination keyCombination = entry.getValue();

            scene.getAccelerators()
                    .put(
                            keyCombination,
                            () -> {
                                Runnable handler = actions.get(action);
                                if (handler != null) {
                                    logger.debug(
                                            "Hotkey triggered: {} ({})", action, keyCombination);
                                    Platform.runLater(handler);
                                } else {
                                    logger.warn(
                                            "No handler registered for hotkey action: {}", action);
                                }
                            });
        }

        logger.info("Hotkeys registered with scene");
    }

    /** Unregisters hotkeys from the current scene */
    public void unregisterFromScene() {
        if (registeredScene != null) {
            for (KeyCombination keyCombination : hotkeys.values()) {
                registeredScene.getAccelerators().remove(keyCombination);
            }
            registeredScene = null;
            logger.info("Hotkeys unregistered from scene");
        }
    }

    /** Updates a hotkey combination */
    public void updateHotkey(HotkeyAction action, KeyCombination newCombination) {
        KeyCombination oldCombination = hotkeys.get(action);

        // Remove old accelerator if scene is registered
        if (registeredScene != null && oldCombination != null) {
            registeredScene.getAccelerators().remove(oldCombination);
        }

        // Update the hotkey
        hotkeys.put(action, newCombination);

        // Register new accelerator if scene is registered
        if (registeredScene != null) {
            registeredScene
                    .getAccelerators()
                    .put(
                            newCombination,
                            () -> {
                                Runnable handler = actions.get(action);
                                if (handler != null) {
                                    Platform.runLater(handler);
                                }
                            });
        }

        logger.info("Updated hotkey for {}: {}", action, newCombination);
    }

    /** Gets the current hotkey for an action */
    public KeyCombination getHotkey(HotkeyAction action) {
        return hotkeys.get(action);
    }

    /** Gets all hotkey mappings */
    public Map<HotkeyAction, KeyCombination> getAllHotkeys() {
        return new HashMap<>(hotkeys);
    }

    /** Resets all hotkeys to defaults */
    public void resetToDefaults() {
        updateHotkey(HotkeyAction.PAUSE, DEFAULT_PAUSE_KEY);
        updateHotkey(HotkeyAction.RESUME, DEFAULT_RESUME_KEY);
        updateHotkey(HotkeyAction.STOP, DEFAULT_STOP_KEY);
        updateHotkey(HotkeyAction.TOGGLE_PAUSE, DEFAULT_TOGGLE_PAUSE_KEY);
        logger.info("Hotkeys reset to defaults");
    }

    /** Gets a string representation of a hotkey */
    public String getHotkeyDisplayString(HotkeyAction action) {
        KeyCombination combo = hotkeys.get(action);
        return combo != null ? combo.getDisplayText() : "Not Set";
    }
}
