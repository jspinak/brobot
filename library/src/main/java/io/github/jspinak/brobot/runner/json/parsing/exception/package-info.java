/**
 * Exception classes for JSON parsing and configuration errors.
 *
 * <p>This package contains specialized exception types that provide detailed error information for
 * JSON parsing failures, configuration problems, and validation errors. These exceptions support
 * comprehensive error reporting with context and suggestions.
 *
 * <h2>Exception Types</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException} -
 *       General configuration and parsing errors
 * </ul>
 *
 * <h2>Error Information</h2>
 *
 * <p>ConfigurationException provides:
 *
 * <ul>
 *   <li>Detailed error messages
 *   <li>Root cause exceptions
 *   <li>JSON path context when available
 *   <li>Suggestions for resolution
 * </ul>
 *
 * <h2>Common Error Scenarios</h2>
 *
 * <h3>Schema Validation Failure</h3>
 *
 * <pre>{@code
 * throw new ConfigurationException(
 *     "Invalid project configuration:\n" +
 *     "- Missing required field 'name' at $.project\n" +
 *     "- Invalid type for 'timeout' at $.settings.timeout (expected number)"
 * );
 * }</pre>
 *
 * <h3>Resource Not Found</h3>
 *
 * <pre>{@code
 * throw new ConfigurationException(
 *     "Schema not found: /schemas/project-schema.json",
 *     ioException
 * );
 * }</pre>
 *
 * <h3>Parse Error</h3>
 *
 * <pre>{@code
 * throw new ConfigurationException(
 *     "Failed to parse JSON at line 42, column 15: " +
 *     "Unexpected character '}' - missing comma?",
 *     parseException
 * );
 * }</pre>
 *
 * <h2>Exception Handling</h2>
 *
 * <pre>{@code
 * try {
 *     project = parser.parse(json, AutomationProject.class);
 * } catch (ConfigurationException e) {
 *     logger.error("Configuration error: {}", e.getMessage());
 *
 *     if (e.getCause() != null) {
 *         logger.debug("Root cause:", e.getCause());
 *     }
 *
 *     // Show user-friendly error
 *     showError("Invalid configuration: " + e.getMessage());
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Include actionable error messages
 *   <li>Preserve root cause for debugging
 *   <li>Add context (file name, path, line)
 *   <li>Suggest fixes when possible
 *   <li>Use appropriate logging levels
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser
 * @see io.github.jspinak.brobot.runner.json.validation.exception
 */
package io.github.jspinak.brobot.runner.json.parsing.exception;
