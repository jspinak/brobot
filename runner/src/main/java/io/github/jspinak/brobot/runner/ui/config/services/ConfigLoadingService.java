package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for loading configurations and initializing the Brobot library. Handles the actual
 * loading process and error handling.
 */
@Slf4j
@Service
public class ConfigLoadingService {

    private final BrobotLibraryInitializer libraryInitializer;
    private final EventBus eventBus;

    @Getter @Setter private LoadingConfiguration configuration;

    private Consumer<LoadingResult> loadingCompleteHandler;

    /** Configuration for loading behavior. */
    @Getter
    @Setter
    public static class LoadingConfiguration {
        private boolean showSuccessAlert;
        private boolean showErrorAlert;
        private boolean publishEvents;
        private boolean asyncLoading;
        private int loadTimeoutSeconds;

        public static LoadingConfigurationBuilder builder() {
            return new LoadingConfigurationBuilder();
        }

        public static class LoadingConfigurationBuilder {
            private boolean showSuccessAlert = true;
            private boolean showErrorAlert = true;
            private boolean publishEvents = true;
            private boolean asyncLoading = false;
            private int loadTimeoutSeconds = 30;

            public LoadingConfigurationBuilder showSuccessAlert(boolean show) {
                this.showSuccessAlert = show;
                return this;
            }

            public LoadingConfigurationBuilder showErrorAlert(boolean show) {
                this.showErrorAlert = show;
                return this;
            }

            public LoadingConfigurationBuilder publishEvents(boolean publish) {
                this.publishEvents = publish;
                return this;
            }

            public LoadingConfigurationBuilder asyncLoading(boolean async) {
                this.asyncLoading = async;
                return this;
            }

            public LoadingConfigurationBuilder loadTimeoutSeconds(int timeout) {
                this.loadTimeoutSeconds = timeout;
                return this;
            }

            public LoadingConfiguration build() {
                LoadingConfiguration config = new LoadingConfiguration();
                config.showSuccessAlert = showSuccessAlert;
                config.showErrorAlert = showErrorAlert;
                config.publishEvents = publishEvents;
                config.asyncLoading = asyncLoading;
                config.loadTimeoutSeconds = loadTimeoutSeconds;
                return config;
            }
        }
    }

    /** Result of a configuration loading operation. */
    @Getter
    public static class LoadingResult {
        private final boolean success;
        private final ConfigEntry configEntry;
        private final String message;
        private final Exception error;

        private LoadingResult(
                boolean success, ConfigEntry configEntry, String message, Exception error) {
            this.success = success;
            this.configEntry = configEntry;
            this.message = message;
            this.error = error;
        }

        public static LoadingResult success(ConfigEntry configEntry, String message) {
            return new LoadingResult(true, configEntry, message, null);
        }

        public static LoadingResult failure(
                ConfigEntry configEntry, String message, Exception error) {
            return new LoadingResult(false, configEntry, message, error);
        }
    }

    @Autowired
    public ConfigLoadingService(BrobotLibraryInitializer libraryInitializer, EventBus eventBus) {
        this.libraryInitializer = libraryInitializer;
        this.eventBus = eventBus;
        this.configuration = LoadingConfiguration.builder().build();
    }

    /**
     * Loads a configuration entry.
     *
     * @param entry The configuration to load
     * @return CompletableFuture with the loading result
     */
    public CompletableFuture<LoadingResult> loadConfiguration(ConfigEntry entry) {
        if (configuration.asyncLoading) {
            return CompletableFuture.supplyAsync(() -> loadConfigurationSync(entry));
        } else {
            return CompletableFuture.completedFuture(loadConfigurationSync(entry));
        }
    }

    /**
     * Synchronously loads a configuration entry.
     *
     * @param entry The configuration to load
     * @return The loading result
     */
    private LoadingResult loadConfigurationSync(ConfigEntry entry) {
        try {
            log.info("Loading configuration: {}", entry.getName());

            // Publish start event
            if (configuration.publishEvents) {
                String message = "Loading configuration: " + entry.getName();
                eventBus.publish(LogEvent.info(this, message, "Configuration"));
            }

            // Initialize with config
            boolean success =
                    libraryInitializer.initializeWithConfig(
                            entry.getProjectConfigPath(), entry.getDslConfigPath());

            if (success) {
                String message = "Configuration loaded successfully: " + entry.getName();
                log.info(message);

                // Publish success event
                if (configuration.publishEvents) {
                    eventBus.publish(LogEvent.info(this, message, "Configuration"));
                }

                // Show success alert
                if (configuration.showSuccessAlert) {
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Configuration Loaded",
                            "Configuration loaded successfully",
                            "Project: " + entry.getProject());
                }

                LoadingResult result = LoadingResult.success(entry, message);

                // Notify handler
                if (loadingCompleteHandler != null) {
                    loadingCompleteHandler.accept(result);
                }

                return result;

            } else {
                String errorMessage = libraryInitializer.getLastErrorMessage();
                if (errorMessage == null) {
                    errorMessage = "Unknown error";
                }

                log.error("Failed to load configuration: {}", errorMessage);

                // Publish error event
                if (configuration.publishEvents) {
                    eventBus.publish(
                            LogEvent.error(
                                    this,
                                    "Failed to load configuration: " + errorMessage,
                                    "Configuration",
                                    null));
                }

                // Show error alert
                if (configuration.showErrorAlert) {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Load Failed",
                            "Failed to load configuration",
                            errorMessage);
                }

                LoadingResult result = LoadingResult.failure(entry, errorMessage, null);

                // Notify handler
                if (loadingCompleteHandler != null) {
                    loadingCompleteHandler.accept(result);
                }

                return result;
            }

        } catch (Exception e) {
            log.error("Error loading configuration", e);

            // Publish error event
            if (configuration.publishEvents) {
                eventBus.publish(
                        LogEvent.error(
                                this,
                                "Error loading configuration: " + e.getMessage(),
                                "Configuration",
                                e));
            }

            // Show error alert
            if (configuration.showErrorAlert) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Load Error",
                        "Error loading configuration",
                        e.getMessage());
            }

            LoadingResult result = LoadingResult.failure(entry, e.getMessage(), e);

            // Notify handler
            if (loadingCompleteHandler != null) {
                loadingCompleteHandler.accept(result);
            }

            return result;
        }
    }

    /**
     * Shows a confirmation dialog before removing a configuration.
     *
     * @param entry The configuration to remove
     * @return Optional containing true if confirmed, empty if cancelled
     */
    public Optional<Boolean> confirmRemoval(ConfigEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Configuration");
        alert.setHeaderText("Remove configuration from recent list?");
        alert.setContentText(
                "This will only remove the entry from the recent list, not delete the files.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.map(response -> response == ButtonType.OK);
    }

    /**
     * Sets the handler for loading completion.
     *
     * @param handler The completion handler
     */
    public void setLoadingCompleteHandler(Consumer<LoadingResult> handler) {
        this.loadingCompleteHandler = handler;
    }

    /**
     * Gets the last error message from the library initializer.
     *
     * @return The last error message or null
     */
    public String getLastErrorMessage() {
        return libraryInitializer.getLastErrorMessage();
    }

    /**
     * Checks if the library is currently initialized.
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return libraryInitializer.isInitialized();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(type);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }
}
