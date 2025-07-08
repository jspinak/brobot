package io.github.jspinak.brobot.runner.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Event representing a configuration-related event.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConfigurationEvent extends BrobotEvent {
    private final String configName;
    private final String details;
    private final Exception error;

    public ConfigurationEvent(EventType eventType, Object source, String configName,
                              String details, Exception error) {
        super(eventType, source);
        this.configName = configName;
        this.details = details;
        this.error = error;
    }

    public String getConfigName() {
        return configName;
    }

    public String getDetails() {
        return details;
    }

    public Exception getError() {
        return error;
    }

    /**
     * Factory method to create a config loaded event
     */
    public static ConfigurationEvent loaded(Object source, String configName, String details) {
        return new ConfigurationEvent(EventType.CONFIG_LOADED, source, configName, details, null);
    }

    /**
     * Factory method to create a config loading failed event
     */
    public static ConfigurationEvent loadingFailed(Object source, String configName, String details, Exception error) {
        return new ConfigurationEvent(EventType.CONFIG_LOADING_FAILED, source, configName, details, error);
    }
}