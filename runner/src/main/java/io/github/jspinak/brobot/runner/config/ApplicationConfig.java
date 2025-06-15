package io.github.jspinak.brobot.runner.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

/**
 * Manages application configuration and user preferences.
 * Provides methods for saving and loading settings persistently.
 */
@Component
public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;

    // User preferences for application settings
    private final Preferences userPrefs = Preferences.userNodeForPackage(ApplicationConfig.class);

    // Properties for more complex configuration
    private final Properties appProperties = new Properties();

    // Config file path
    private Path configFilePath;

    // Autosave executor
    private ScheduledExecutorService autosaveExecutor;

    @Getter @Setter
    private boolean autosaveEnabled = true;

    @Autowired
    public ApplicationConfig(EventBus eventBus, BrobotRunnerProperties properties) {
        this.eventBus = eventBus;
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {
        // Set up config file path
        configFilePath = Paths.get(properties.getConfigPath(), "app.properties");

        // Create parent directories if they don't exist
        try {
            Files.createDirectories(configFilePath.getParent());
        } catch (IOException e) {
            logger.error("Failed to create config directory", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to initialize configuration: " + e.getMessage(), "Config", e));
        }

        // Load existing configuration
        loadConfiguration();

        // Start autosave
        if (autosaveEnabled) {
            startAutosave();
        }

        logger.info("ApplicationConfig initialized");
        eventBus.publish(LogEvent.info(this, "Application configuration initialized", "Config"));
    }

    /**
     * Loads the configuration from the properties file.
     */
    public void loadConfiguration() {
        // First try to load from file
        if (Files.exists(configFilePath)) {
            try (InputStream is = Files.newInputStream(configFilePath)) {
                appProperties.load(is);
                logger.info("Loaded configuration from {}", configFilePath);
                eventBus.publish(LogEvent.info(this,
                        "Configuration loaded from " + configFilePath, "Config"));
            } catch (IOException e) {
                logger.error("Failed to load configuration from file", e);
                eventBus.publish(LogEvent.error(this,
                        "Failed to load configuration from file: " + e.getMessage(), "Config", e));
            }
        } else {
            logger.info("No configuration file found at {}, using defaults", configFilePath);
            loadDefaults();
        }
    }

    /**
     * Saves the configuration to the properties file.
     */
    public void saveConfiguration() {
        try (OutputStream os = Files.newOutputStream(configFilePath)) {
            appProperties.store(os, "Brobot Runner Application Settings");
            logger.info("Saved configuration to {}", configFilePath);
            eventBus.publish(LogEvent.info(this,
                    "Configuration saved to " + configFilePath, "Config"));
        } catch (IOException e) {
            logger.error("Failed to save configuration to file", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to save configuration to file: " + e.getMessage(), "Config", e));
        }
    }

    /**
     * Loads default configuration settings.
     */
    private void loadDefaults() {
        // Set default values for configuration properties
        appProperties.setProperty("app.version", "1.0.0");
        appProperties.setProperty("ui.theme", "light");
        appProperties.setProperty("ui.fontScale", "1.0");
        appProperties.setProperty("automation.autoStart", "false");

        logger.info("Loaded default configuration settings");
        eventBus.publish(LogEvent.info(this, "Loaded default configuration settings", "Config"));
    }

    /**
     * Gets a string property from the configuration.
     *
     * @param key The property key
     * @param defaultValue The default value if the key is not found
     * @return The property value
     */
    public String getString(String key, String defaultValue) {
        return appProperties.getProperty(key, defaultValue);
    }

    /**
     * Sets a string property in the configuration.
     *
     * @param key The property key
     * @param value The property value
     */
    public void setString(String key, String value) {
        appProperties.setProperty(key, value);

        // Log the change
        logger.debug("Config key '{}' set to '{}'", key, value);
    }

    /**
     * Gets a boolean property from the configuration.
     *
     * @param key The property key
     * @param defaultValue The default value if the key is not found
     * @return The property value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = appProperties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Sets a boolean property in the configuration.
     *
     * @param key The property key
     * @param value The property value
     */
    public void setBoolean(String key, boolean value) {
        appProperties.setProperty(key, Boolean.toString(value));

        // Log the change
        logger.debug("Config key '{}' set to '{}'", key, value);
    }

    /**
     * Gets an integer property from the configuration.
     *
     * @param key The property key
     * @param defaultValue The default value if the key is not found
     * @return The property value
     */
    public int getInt(String key, int defaultValue) {
        String value = appProperties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Sets an integer property in the configuration.
     *
     * @param key The property key
     * @param value The property value
     */
    public void setInt(String key, int value) {
        appProperties.setProperty(key, Integer.toString(value));

        // Log the change
        logger.debug("Config key '{}' set to '{}'", key, value);
    }

    /**
     * Gets a double property from the configuration.
     *
     * @param key The property key
     * @param defaultValue The default value if the key is not found
     * @return The property value
     */
    public double getDouble(String key, double defaultValue) {
        String value = appProperties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Sets a double property in the configuration.
     *
     * @param key The property key
     * @param value The property value
     */
    public void setDouble(String key, double value) {
        appProperties.setProperty(key, Double.toString(value));

        // Log the change
        logger.debug("Config key '{}' set to '{}'", key, value);
    }

    /**
     * Gets a user preference string value.
     *
     * @param key The preference key
     * @param defaultValue The default value if the key is not found
     * @return The preference value
     */
    public String getUserPreference(String key, String defaultValue) {
        return userPrefs.get(key, defaultValue);
    }

    /**
     * Sets a user preference string value.
     *
     * @param key The preference key
     * @param value The preference value
     */
    public void setUserPreference(String key, String value) {
        userPrefs.put(key, value);

        // Log the change
        logger.debug("User preference '{}' set to '{}'", key, value);
    }

    /**
     * Clears all user preferences.
     */
    public void clearUserPreferences() {
        try {
            userPrefs.clear();
            logger.info("User preferences cleared");
            eventBus.publish(LogEvent.info(this, "User preferences cleared", "Config"));
        } catch (Exception e) {
            logger.error("Failed to clear user preferences", e);
            eventBus.publish(LogEvent.error(this,
                    "Failed to clear user preferences: " + e.getMessage(), "Config", e));
        }
    }

    /**
     * Starts the autosave mechanism.
     */
    protected void startAutosave() {
        autosaveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Config-Autosave");
            t.setDaemon(true);
            return t;
        });

        // Schedule autosave every 5 minutes
        autosaveExecutor.scheduleAtFixedRate(() -> {
            try {
                saveConfiguration();
            } catch (Exception e) {
                logger.error("Error during configuration autosave", e);
            }
        }, 5, 5, TimeUnit.MINUTES);

        logger.info("Configuration autosave started");
    }

    /**
     * Stops the autosave mechanism.
     */
    public void stopAutosave() {
        if (autosaveExecutor != null) {
            autosaveExecutor.shutdown();
            try {
                if (!autosaveExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    autosaveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                autosaveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("Configuration autosave stopped");
        }
    }

    /**
     * Cleans up resources when the application is shutting down.
     */
    @PreDestroy
    public void shutdown() {
        // Save configuration one final time
        saveConfiguration();

        // Stop autosave
        stopAutosave();

        logger.info("ApplicationConfig shutdown");
    }
}