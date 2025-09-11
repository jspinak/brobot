package io.github.jspinak.brobot.exception;

/**
 * Thrown when there is an error in the configuration of actions or the framework.
 *
 * <p>This exception indicates problems with how actions are configured, such as invalid parameter
 * combinations, missing required settings, or incompatible option values. It helps catch
 * configuration errors early, before actions are executed.
 *
 * @since 1.0
 */
public class ConfigurationException extends BrobotRuntimeException {

    private final String configurationItem;

    /**
     * Constructs a new configuration exception.
     *
     * @param message a description of the configuration problem
     */
    public ConfigurationException(String message) {
        super(message);
        this.configurationItem = null;
    }

    /**
     * Constructs a new configuration exception for a specific configuration item.
     *
     * @param configurationItem the name of the configuration item that has a problem
     * @param message a description of the configuration problem
     */
    public ConfigurationException(String configurationItem, String message) {
        super(String.format("Configuration error in '%s': %s", configurationItem, message));
        this.configurationItem = configurationItem;
    }

    /**
     * Constructs a new configuration exception with an underlying cause.
     *
     * @param message a description of the configuration problem
     * @param cause the underlying cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationItem = null;
    }

    /**
     * Gets the name of the configuration item that caused the exception.
     *
     * @return the configuration item name, or null if not specific to an item
     */
    public String getConfigurationItem() {
        return configurationItem;
    }
}
