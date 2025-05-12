package io.github.jspinak.brobot.json.schemaValidation.model;

import java.util.Objects;

/**
 * Represents an error found during configuration validation.
 */
public record ValidationError(String errorCode, String message, ValidationSeverity severity) {

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", severity, errorCode, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationError that = (ValidationError) o;
        return Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(message, that.message) &&
                severity == that.severity;
    }

}
