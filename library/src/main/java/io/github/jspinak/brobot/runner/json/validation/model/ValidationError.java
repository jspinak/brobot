package io.github.jspinak.brobot.runner.json.validation.model;

import java.util.Objects;

/**
 * Represents a single validation error found during Brobot configuration validation.
 * 
 * <p>This immutable record encapsulates all information about a validation issue,
 * including an error code for programmatic handling, a human-readable message,
 * and a severity level indicating the impact of the error. Using Java records
 * provides automatic implementations of equals, hashCode, and toString.</p>
 * 
 * <h2>Record Components:</h2>
 * <ul>
 *   <li><b>errorCode</b> - A short, stable identifier for the error type (e.g., "Invalid state reference")</li>
 *   <li><b>message</b> - A detailed, human-readable description of the specific error</li>
 *   <li><b>severity</b> - The impact level of this error on system operation</li>
 * </ul>
 * 
 * <h2>Error Code Conventions:</h2>
 * <ul>
 *   <li>Use descriptive, space-separated phrases</li>
 *   <li>Keep codes stable across versions for programmatic handling</li>
 *   <li>Examples: "Missing image resource", "Duplicate function name", "Unreachable state"</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Creating a validation error
 * ValidationError error = new ValidationError(
 *     "Invalid state reference",
 *     "Transition #5 references non-existent state ID 99",
 *     ValidationSeverity.ERROR
 * );
 * 
 * // Using the error
 * if (error.severity() == ValidationSeverity.ERROR) {
 *     logger.error("[{}] {}", error.errorCode(), error.message());
 * }
 * 
 * // Formatted output
 * System.out.println(error); // [ERROR] Invalid state reference: Transition #5...
 * }</pre>
 * 
 * @param errorCode A stable identifier for this type of error, used for programmatic
 *                  error handling and filtering
 * @param message A detailed description of the error including context about where
 *                and why it occurred
 * @param severity The severity level indicating whether this error blocks execution
 *                 (CRITICAL/ERROR) or is advisory (WARNING/INFO)
 * 
 * @see ValidationSeverity for understanding severity levels
 * @see ValidationResult for collecting multiple validation errors
 * @author jspinak
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
