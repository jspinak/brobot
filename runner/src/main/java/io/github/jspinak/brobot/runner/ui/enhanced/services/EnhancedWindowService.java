package io.github.jspinak.brobot.runner.ui.enhanced.services;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.AutomationWindowController;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing window state during automation. Handles window minimization and restoration.
 */
@Slf4j
@Service
public class EnhancedWindowService {

    private final AutomationWindowController windowController;

    @Getter @Setter private WindowConfiguration configuration;

    private Consumer<String> logHandler;

    /** Configuration for window behavior. */
    @Getter
    @Setter
    public static class WindowConfiguration {
        private boolean autoMinimizeEnabled;
        private boolean restoreAfterAutomation;
        private boolean logWindowActions;
        private int minimizeDelayMs;
        private int restoreDelayMs;

        public static WindowConfigurationBuilder builder() {
            return new WindowConfigurationBuilder();
        }

        public static class WindowConfigurationBuilder {
            private boolean autoMinimizeEnabled = true;
            private boolean restoreAfterAutomation = true;
            private boolean logWindowActions = true;
            private int minimizeDelayMs = 100;
            private int restoreDelayMs = 100;

            public WindowConfigurationBuilder autoMinimizeEnabled(boolean enabled) {
                this.autoMinimizeEnabled = enabled;
                return this;
            }

            public WindowConfigurationBuilder restoreAfterAutomation(boolean restore) {
                this.restoreAfterAutomation = restore;
                return this;
            }

            public WindowConfigurationBuilder logWindowActions(boolean log) {
                this.logWindowActions = log;
                return this;
            }

            public WindowConfigurationBuilder minimizeDelayMs(int delay) {
                this.minimizeDelayMs = delay;
                return this;
            }

            public WindowConfigurationBuilder restoreDelayMs(int delay) {
                this.restoreDelayMs = delay;
                return this;
            }

            public WindowConfiguration build() {
                WindowConfiguration config = new WindowConfiguration();
                config.autoMinimizeEnabled = autoMinimizeEnabled;
                config.restoreAfterAutomation = restoreAfterAutomation;
                config.logWindowActions = logWindowActions;
                config.minimizeDelayMs = minimizeDelayMs;
                config.restoreDelayMs = restoreDelayMs;
                return config;
            }
        }
    }

    @Autowired
    public EnhancedWindowService(AutomationWindowController windowController) {
        this.windowController = windowController;
        this.configuration = WindowConfiguration.builder().build();
    }

    /** Initializes the window service with current settings. */
    public void initialize() {
        // Apply initial configuration
        setAutoMinimizeEnabled(configuration.autoMinimizeEnabled);
    }

    /** Minimizes the window for automation if auto-minimize is enabled. */
    public void minimizeForAutomation() {
        if (configuration.autoMinimizeEnabled) {
            logWindowAction("Minimizing window for automation");

            if (configuration.minimizeDelayMs > 0) {
                try {
                    Thread.sleep(configuration.minimizeDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            windowController.minimizeForAutomation();
        }
    }

    /** Restores the window after automation if configured. */
    public void restoreAfterAutomation() {
        if (configuration.restoreAfterAutomation) {
            logWindowAction("Restoring window after automation");

            if (configuration.restoreDelayMs > 0) {
                try {
                    Thread.sleep(configuration.restoreDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            windowController.restoreAfterAutomation();
        }
    }

    /**
     * Checks if auto-minimize is enabled.
     *
     * @return true if auto-minimize is enabled
     */
    public boolean isAutoMinimizeEnabled() {
        return windowController.isAutoMinimizeEnabled();
    }

    /**
     * Sets the auto-minimize enabled state.
     *
     * @param enabled true to enable auto-minimize
     */
    public void setAutoMinimizeEnabled(boolean enabled) {
        configuration.autoMinimizeEnabled = enabled;
        windowController.setAutoMinimizeEnabled(enabled);
        logWindowAction("Auto-minimize " + (enabled ? "enabled" : "disabled"));
    }

    /**
     * Sets the log handler for window events.
     *
     * @param logHandler The log handler
     */
    public void setLogHandler(Consumer<String> logHandler) {
        this.logHandler = logHandler;
    }

    private void logWindowAction(String action) {
        if (configuration.logWindowActions) {
            log.debug("Window action: {}", action);
            if (logHandler != null) {
                logHandler.accept(action);
            }
        }
    }
}
