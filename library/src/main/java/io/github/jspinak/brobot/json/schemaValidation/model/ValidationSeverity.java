package io.github.jspinak.brobot.json.schemaValidation.model;

/**
 * Represents the severity of a validation error.
 */
public enum ValidationSeverity {
    /**
     * Minor issue, the configuration can still be used.
     */
    WARNING,

    /**
     * Significant issue, the configuration may not behave as expected.
     */
    ERROR,

    /**
     * Severe issue, the configuration cannot be used.
     */
    CRITICAL,

    /**
     * Informational message, no action required.
     */
    INFO
}
