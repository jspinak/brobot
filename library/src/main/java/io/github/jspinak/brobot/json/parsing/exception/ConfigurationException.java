package io.github.jspinak.brobot.json.parsing.exception;

/**
 * Exception thrown when there is an error in processing Brobot Runner configurations.
 * This includes errors with parsing, validation, or other configuration-related problems.
 */
public class ConfigurationException extends Exception {

    /**
     * Create a new configuration exception with a message
     *
     * @param message The error message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Create a new configuration exception with a message and cause
     *
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new configuration exception with a formatted message
     *
     * @param format The format string
     * @param args The format arguments
     */
    public static ConfigurationException formatted(String format, Object... args) {
        return new ConfigurationException(String.format(format, args));
    }
}