/**
 * Validation-specific exception classes.
 *
 * <p>This package contains specialized exceptions for validation failures, providing detailed
 * information about validation errors including severity, location, and suggestions for resolution.
 * These exceptions support comprehensive error reporting for configuration problems.
 *
 * <h2>Exception Types</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.exception.ConfigValidationException}
 *       - Thrown when configuration validation fails
 * </ul>
 *
 * <h2>Exception Features</h2>
 *
 * <h3>Validation Result Integration</h3>
 *
 * <pre>{@code
 * ValidationResult result = validator.validate(config);
 * if (result.hasErrors()) {
 *     throw new ConfigValidationException(result);
 * }
 * }</pre>
 *
 * <h3>Detailed Error Information</h3>
 *
 * <pre>{@code
 * try {
 *     validateConfiguration(config);
 * } catch (ConfigValidationException e) {
 *     // Access validation results
 *     ValidationResult result = e.getValidationResult();
 *
 *     // Get all errors
 *     List<ValidationError> errors = result.getErrors();
 *
 *     // Get errors by severity
 *     List<ValidationError> criticals = result.getBySeverity(ERROR);
 *
 *     // Check specific error types
 *     boolean hasSchemaErrors = result.hasErrorsOfType(SCHEMA);
 * }
 * }</pre>
 *
 * <h3>Formatted Error Messages</h3>
 *
 * <pre>{@code
 * catch (ConfigValidationException e) {
 *     // Single-line summary
 *     logger.error("Validation failed: {}", e.getMessage());
 *
 *     // Detailed multi-line format
 *     String detailed = e.getDetailedMessage();
 *      * Configuration validation failed with 3 errors:
 *      *
 *      * ERROR: Missing required field 'name' at $.project
 *      * ERROR: Invalid state reference 'unknownState' at $.transitions[0].to
 *      *        Suggestion: Did you mean 'loginState'?
 *      * WARNING: Unused function 'helperFunction' at $.functions[2]
 * }
 * }</pre>
 *
 * <h2>Exception Handling Patterns</h2>
 *
 * <h3>User-Friendly Display</h3>
 *
 * <pre>{@code
 * catch (ConfigValidationException e) {
 *     // Show errors in UI
 *     for (ValidationError error : e.getValidationResult().getErrors()) {
 *         ui.showError(
 *             error.getMessage(),
 *             error.getPath(),
 *             error.getSuggestion()
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h3>Selective Handling</h3>
 *
 * <pre>{@code
 * catch (ConfigValidationException e) {
 *     ValidationResult result = e.getValidationResult();
 *
 *     // Handle different error types
 *     if (result.hasSchemaErrors()) {
 *         handleSchemaErrors(result.getSchemaErrors());
 *     }
 *
 *     if (result.hasReferenceErrors()) {
 *         handleReferenceErrors(result.getReferenceErrors());
 *     }
 *
 *     // Continue with warnings only
 *     if (!result.hasErrors() && result.hasWarnings()) {
 *         logWarnings(result.getWarnings());
 *         proceed();
 *     }
 * }
 * }</pre>
 *
 * <h3>Error Recovery</h3>
 *
 * <pre>{@code
 * catch (ConfigValidationException e) {
 *     // Attempt to fix common issues
 *     Configuration fixed = configFixer.tryFix(
 *         config,
 *         e.getValidationResult()
 *     );
 *
 *     if (fixed != null) {
 *         // Re-validate fixed configuration
 *         return validateConfiguration(fixed);
 *     }
 *
 *     throw e; // Can't fix, re-throw
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Always include ValidationResult in exception
 *   <li>Provide both summary and detailed messages
 *   <li>Include error location (JSON path)
 *   <li>Add suggestions when possible
 *   <li>Support error grouping and filtering
 *   <li>Enable programmatic error analysis
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.validation.model.ValidationResult
 * @see io.github.jspinak.brobot.runner.json.validation.model.ValidationError
 */
package io.github.jspinak.brobot.runner.json.validation.exception;
