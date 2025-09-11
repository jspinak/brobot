package io.github.jspinak.brobot.runner.ui.automation.services;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Alert;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing hotkey integration with automation controls. Provides centralized hotkey
 * registration and configuration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotkeyIntegrationService {

    private final HotkeyManager hotkeyManager;

    // Action handlers
    private final Map<HotkeyAction, Runnable> actionHandlers = new HashMap<>();

    // Configuration
    private HotkeyConfiguration configuration = HotkeyConfiguration.builder().build();

    // State
    private boolean hotkeyListenerActive = false;

    /** Hotkey configuration. */
    public static class HotkeyConfiguration {
        private boolean autoStart = true;
        private boolean showDialogOnError = true;
        private Map<HotkeyAction, String> customDescriptions = new HashMap<>();

        public static HotkeyConfigurationBuilder builder() {
            return new HotkeyConfigurationBuilder();
        }

        public static class HotkeyConfigurationBuilder {
            private HotkeyConfiguration config = new HotkeyConfiguration();

            public HotkeyConfigurationBuilder() {
                // Default descriptions
                config.customDescriptions.put(HotkeyAction.PAUSE, "Pause automation");
                config.customDescriptions.put(HotkeyAction.RESUME, "Resume automation");
                config.customDescriptions.put(HotkeyAction.STOP, "Stop all automation");
                config.customDescriptions.put(HotkeyAction.TOGGLE_PAUSE, "Toggle pause/resume");
            }

            public HotkeyConfigurationBuilder autoStart(boolean autoStart) {
                config.autoStart = autoStart;
                return this;
            }

            public HotkeyConfigurationBuilder showDialogOnError(boolean show) {
                config.showDialogOnError = show;
                return this;
            }

            public HotkeyConfigurationBuilder customDescription(
                    HotkeyAction action, String description) {
                config.customDescriptions.put(action, description);
                return this;
            }

            public HotkeyConfiguration build() {
                return config;
            }
        }
    }

    /** Sets the configuration. */
    public void setConfiguration(HotkeyConfiguration configuration) {
        this.configuration = configuration;
    }

    /** Registers automation actions with hotkey manager. */
    public void registerAutomationActions(
            Runnable pauseAction,
            Runnable resumeAction,
            Runnable stopAction,
            Runnable togglePauseAction) {

        log.info("Registering hotkey actions");

        // Store handlers
        actionHandlers.put(HotkeyAction.PAUSE, pauseAction);
        actionHandlers.put(HotkeyAction.RESUME, resumeAction);
        actionHandlers.put(HotkeyAction.STOP, stopAction);
        actionHandlers.put(HotkeyAction.TOGGLE_PAUSE, togglePauseAction);

        // Register with hotkey manager
        hotkeyManager.registerAction(HotkeyAction.PAUSE, createSafeAction(pauseAction, "Pause"));
        hotkeyManager.registerAction(HotkeyAction.RESUME, createSafeAction(resumeAction, "Resume"));
        hotkeyManager.registerAction(HotkeyAction.STOP, createSafeAction(stopAction, "Stop"));
        hotkeyManager.registerAction(
                HotkeyAction.TOGGLE_PAUSE, createSafeAction(togglePauseAction, "Toggle Pause"));

        // Start listening if configured
        if (configuration.autoStart) {
            startListening();
        }
    }

    /** Creates a safe action wrapper with error handling. */
    private Runnable createSafeAction(Runnable action, String actionName) {
        return () -> {
            try {
                log.debug("Executing hotkey action: {}", actionName);
                action.run();
            } catch (Exception e) {
                log.error("Error executing hotkey action: " + actionName, e);
                if (configuration.showDialogOnError) {
                    showErrorDialog(
                            "Hotkey Action Error",
                            "Failed to execute " + actionName + ": " + e.getMessage());
                }
            }
        };
    }

    /** Starts listening for hotkeys. */
    public void startListening() {
        if (!hotkeyListenerActive) {
            // TODO: Implement when HotkeyManager has startListening method
            // hotkeyManager.startListening();
            hotkeyListenerActive = true;
            log.info("Hotkey listening started");
        }
    }

    /** Stops listening for hotkeys. */
    public void stopListening() {
        if (hotkeyListenerActive) {
            // TODO: Implement when HotkeyManager has stopListening method
            // hotkeyManager.stopListening();
            hotkeyListenerActive = false;
            log.info("Hotkey listening stopped");
        }
    }

    /** Shows the hotkey configuration dialog. */
    public void showConfigurationDialog() {
        // TODO: Implement custom dialog when available
        // For now, show info dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotkey Configuration");
        alert.setHeaderText("Keyboard Shortcuts");
        alert.setContentText(getHotkeyInfo());
        alert.showAndWait();
    }

    /** Gets formatted hotkey information. */
    public String getHotkeyInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Default hotkeys:\n\n");

        // Default mappings - these should come from HotkeyManager
        Map<HotkeyAction, String> defaultMappings = new HashMap<>();
        defaultMappings.put(HotkeyAction.PAUSE, "Ctrl+P");
        defaultMappings.put(HotkeyAction.RESUME, "Ctrl+R");
        defaultMappings.put(HotkeyAction.STOP, "Ctrl+S");
        defaultMappings.put(HotkeyAction.TOGGLE_PAUSE, "Ctrl+Space");

        for (Map.Entry<HotkeyAction, String> entry : defaultMappings.entrySet()) {
            HotkeyAction action = entry.getKey();
            String keys = entry.getValue();
            String description =
                    configuration.customDescriptions.getOrDefault(action, action.toString());

            info.append(String.format("%-15s - %s\n", keys, description));
        }

        return info.toString();
    }

    /** Gets a short hotkey hint for UI display. */
    public String getHotkeyHint() {
        return "(Ctrl+P: Pause, Ctrl+R: Resume, Ctrl+S: Stop)";
    }

    /** Updates a specific action handler. */
    public void updateActionHandler(HotkeyAction action, Runnable handler) {
        actionHandlers.put(action, handler);
        hotkeyManager.registerAction(action, createSafeAction(handler, action.toString()));
        log.debug("Updated hotkey handler for action: {}", action);
    }

    /** Triggers a hotkey action programmatically. */
    public void triggerAction(HotkeyAction action) {
        Runnable handler = actionHandlers.get(action);
        if (handler != null) {
            handler.run();
        } else {
            log.warn("No handler registered for hotkey action: {}", action);
        }
    }

    /** Checks if a hotkey action is registered. */
    public boolean isActionRegistered(HotkeyAction action) {
        return actionHandlers.containsKey(action);
    }

    /** Gets all registered actions. */
    public Map<HotkeyAction, String> getRegisteredActions() {
        Map<HotkeyAction, String> registered = new HashMap<>();
        for (HotkeyAction action : actionHandlers.keySet()) {
            String description =
                    configuration.customDescriptions.getOrDefault(action, action.toString());
            registered.put(action, description);
        }
        return registered;
    }

    /** Shows an error dialog. */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Clears all registered actions. */
    public void clearActions() {
        actionHandlers.clear();
        log.info("Cleared all hotkey actions");
    }

    /** Gets the current state of hotkey listening. */
    public boolean isListening() {
        return hotkeyListenerActive;
    }
}
