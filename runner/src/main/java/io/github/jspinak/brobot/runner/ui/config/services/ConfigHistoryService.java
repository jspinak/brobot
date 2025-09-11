package io.github.jspinak.brobot.runner.ui.config.services;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing configuration history and recent configurations. Handles loading, saving,
 * and managing the list of recent configurations.
 */
@Slf4j
@Service
public class ConfigHistoryService {

    private static final String RECENT_CONFIGS_KEY = "recentConfigurations";
    private static final int DEFAULT_MAX_RECENT_CONFIGS = 10;

    private final ApplicationConfig appConfig;
    private final List<ConfigEntry> recentConfigs;

    @Getter @Setter private HistoryConfiguration configuration;

    /** Configuration for history management. */
    @Getter
    @Setter
    public static class HistoryConfiguration {
        private int maxRecentConfigs;
        private boolean autoSave;
        private boolean moveToTopOnLoad;

        public static HistoryConfigurationBuilder builder() {
            return new HistoryConfigurationBuilder();
        }

        public static class HistoryConfigurationBuilder {
            private int maxRecentConfigs = DEFAULT_MAX_RECENT_CONFIGS;
            private boolean autoSave = true;
            private boolean moveToTopOnLoad = true;

            public HistoryConfigurationBuilder maxRecentConfigs(int max) {
                this.maxRecentConfigs = max;
                return this;
            }

            public HistoryConfigurationBuilder autoSave(boolean autoSave) {
                this.autoSave = autoSave;
                return this;
            }

            public HistoryConfigurationBuilder moveToTopOnLoad(boolean moveToTop) {
                this.moveToTopOnLoad = moveToTop;
                return this;
            }

            public HistoryConfiguration build() {
                HistoryConfiguration config = new HistoryConfiguration();
                config.maxRecentConfigs = maxRecentConfigs;
                config.autoSave = autoSave;
                config.moveToTopOnLoad = moveToTopOnLoad;
                return config;
            }
        }
    }

    @Autowired
    public ConfigHistoryService(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
        this.recentConfigs = new CopyOnWriteArrayList<>();
        this.configuration = HistoryConfiguration.builder().build();
    }

    /**
     * Loads recent configurations from persistent storage.
     *
     * @return List of loaded configurations
     */
    public List<ConfigEntry> loadRecentConfigurations() {
        try {
            recentConfigs.clear();

            // Get recent configs from app config
            String configsJson = appConfig.getString(RECENT_CONFIGS_KEY, null);
            if (configsJson != null && !configsJson.isEmpty()) {
                // Parse JSON and populate recentConfigs
                List<ConfigEntry> loaded = parseConfigurationsJson(configsJson);
                recentConfigs.addAll(loaded);
                log.info("Loaded {} recent configurations", loaded.size());
            } else {
                // Load default/demo configurations
                loadDefaultConfigurations();
            }

            return Collections.unmodifiableList(recentConfigs);

        } catch (Exception e) {
            log.error("Error loading recent configurations", e);
            return Collections.emptyList();
        }
    }

    /** Saves the current list of recent configurations to persistent storage. */
    public void saveRecentConfigurations() {
        if (!configuration.autoSave) {
            return;
        }

        try {
            // Enforce max size
            while (recentConfigs.size() > configuration.maxRecentConfigs) {
                recentConfigs.remove(recentConfigs.size() - 1);
            }

            // Convert to JSON
            String json = convertConfigurationsToJson(recentConfigs);

            // Save to app config
            appConfig.setString(RECENT_CONFIGS_KEY, json);
            log.debug("Saved {} recent configurations", recentConfigs.size());

        } catch (Exception e) {
            log.error("Error saving recent configurations", e);
        }
    }

    /**
     * Adds a configuration to the recent list.
     *
     * @param entry The configuration to add
     */
    public void addRecentConfiguration(ConfigEntry entry) {
        // Remove if already exists
        recentConfigs.removeIf(
                c ->
                        c.getProjectConfigPath().equals(entry.getProjectConfigPath())
                                && c.getDslConfigPath().equals(entry.getDslConfigPath()));

        // Add to beginning of list
        recentConfigs.add(0, entry);

        // Save if auto-save is enabled
        if (configuration.autoSave) {
            saveRecentConfigurations();
        }
    }

    /**
     * Removes a configuration from the recent list.
     *
     * @param entry The configuration to remove
     * @return true if removed, false otherwise
     */
    public boolean removeConfiguration(ConfigEntry entry) {
        boolean removed = recentConfigs.remove(entry);

        if (removed && configuration.autoSave) {
            saveRecentConfigurations();
        }

        return removed;
    }

    /**
     * Updates a configuration's last modified time and optionally moves it to the top.
     *
     * @param entry The configuration to update
     */
    public void updateConfigurationAccess(ConfigEntry entry) {
        // Find the entry
        Optional<ConfigEntry> found =
                recentConfigs.stream()
                        .filter(
                                c ->
                                        c.getProjectConfigPath()
                                                        .equals(entry.getProjectConfigPath())
                                                && c.getDslConfigPath()
                                                        .equals(entry.getDslConfigPath()))
                        .findFirst();

        if (found.isPresent()) {
            ConfigEntry existing = found.get();
            existing.setLastModified(LocalDateTime.now());

            if (configuration.moveToTopOnLoad) {
                // Move to top
                recentConfigs.remove(existing);
                recentConfigs.add(0, existing);
            }

            if (configuration.autoSave) {
                saveRecentConfigurations();
            }
        }
    }

    /**
     * Gets the current list of recent configurations.
     *
     * @return Unmodifiable list of recent configurations
     */
    public List<ConfigEntry> getRecentConfigurations() {
        return Collections.unmodifiableList(recentConfigs);
    }

    /** Clears all recent configurations. */
    public void clearRecentConfigurations() {
        recentConfigs.clear();
        if (configuration.autoSave) {
            saveRecentConfigurations();
        }
    }

    /**
     * Finds a configuration by its paths.
     *
     * @param projectConfigPath The project config path
     * @param dslConfigPath The DSL config path
     * @return Optional containing the found configuration
     */
    public Optional<ConfigEntry> findConfiguration(Path projectConfigPath, Path dslConfigPath) {
        return recentConfigs.stream()
                .filter(
                        c ->
                                c.getProjectConfigPath().equals(projectConfigPath)
                                        && c.getDslConfigPath().equals(dslConfigPath))
                .findFirst();
    }

    /**
     * Parses configurations from JSON string. In a real implementation, this would use Jackson or
     * Gson.
     */
    private List<ConfigEntry> parseConfigurationsJson(String json) {
        // Placeholder implementation
        // In real implementation, use Jackson or Gson to parse JSON
        List<ConfigEntry> configs = new ArrayList<>();

        // For now, return empty list
        // Real implementation would parse the JSON and create ConfigEntry objects

        return configs;
    }

    /**
     * Converts configurations to JSON string. In a real implementation, this would use Jackson or
     * Gson.
     */
    private String convertConfigurationsToJson(List<ConfigEntry> configs) {
        // Placeholder implementation
        // In real implementation, use Jackson or Gson to convert to JSON
        return "{}";
    }

    /** Loads default/demo configurations for testing. */
    private void loadDefaultConfigurations() {
        // Mock data for demonstration
        for (int i = 1; i <= 3; i++) {
            Path projectConfigPath = Paths.get("config", "project" + i + ".json");
            Path dslConfigPath = Paths.get("config", "dsl" + i + ".json");
            Path imagePath = Paths.get("images");

            ConfigEntry entry =
                    new ConfigEntry(
                            "Project " + i,
                            "Demo Project " + i,
                            projectConfigPath,
                            dslConfigPath,
                            imagePath,
                            LocalDateTime.now().minusDays(i));

            recentConfigs.add(entry);
        }
    }
}
