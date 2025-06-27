/**
 * Data models for validation results and error reporting.
 * 
 * <p>This package contains the data structures used to represent validation
 * results, including errors, warnings, and informational messages. These models
 * provide a consistent way to collect, organize, and report validation issues
 * across all validation types.</p>
 * 
 * <h2>Core Models</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationResult} - 
 *       Aggregates all validation messages</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationError} - 
 *       Individual validation issue details</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity} - 
 *       Issue severity levels (ERROR, WARNING, INFO)</li>
 * </ul>
 * 
 * <h2>ValidationResult</h2>
 * 
 * <p>Container for all validation findings:</p>
 * <pre>{@code
 * ValidationResult result = new ValidationResult();
 * 
 * // Add errors during validation
 * result.addError(new ValidationError(
 *     ValidationSeverity.ERROR,
 *     "Missing required field 'name'",
 *     "$.project.name"
 * ));
 * 
 * // Check results
 * if (result.hasErrors()) {
 *     int errorCount = result.getErrorCount();
 *     List<ValidationError> errors = result.getErrors();
 * }
 * 
 * // Filter by severity
 * List<ValidationError> warnings = result.getWarnings();
 * List<ValidationError> infos = result.getInfos();
 * }</pre>
 * 
 * <h2>ValidationError</h2>
 * 
 * <p>Detailed information about a single issue:</p>
 * <pre>{@code
 * ValidationError error = new ValidationError(
 *     ValidationSeverity.ERROR,
 *     "State 'loginPage' referenced but not defined",
 *     "$.transitions[0].from",
 *     "REFERENCE_ERROR"
 * );
 * 
 * // Optional fields
 * error.setSuggestion("Did you mean 'login_page'?");
 * error.setDetails("Available states: login_page, home_page");
 * error.setSource("StateReferenceValidator");
 * 
 * // Access error information
 * String message = error.getMessage();
 * String path = error.getPath();
 * String suggestion = error.getSuggestion();
 * }</pre>
 * 
 * <h2>ValidationSeverity</h2>
 * 
 * <p>Three levels of validation issues:</p>
 * <ul>
 *   <li><b>ERROR</b> - Must be fixed, prevents execution</li>
 *   <li><b>WARNING</b> - Should be addressed, may cause issues</li>
 *   <li><b>INFO</b> - Informational, best practice suggestions</li>
 * </ul>
 * 
 * <pre>{@code
 * // Severity examples
 * ERROR: "Required field missing"
 * WARNING: "Deprecated feature used"
 * INFO: "Consider using newer syntax"
 * }</pre>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Building Results</h3>
 * <pre>{@code
 * public ValidationResult validate(Config config) {
 *     ValidationResult result = new ValidationResult();
 *     
 *     // Validate various aspects
 *     validateStructure(config, result);
 *     validateReferences(config, result);
 *     validateBusinessRules(config, result);
 *     
 *     return result;
 * }
 * 
 * private void validateStructure(Config config, 
 *                               ValidationResult result) {
 *     if (config.getName() == null) {
 *         result.addError(new ValidationError(
 *             ERROR,
 *             "Configuration name is required",
 *             "$.name"
 *         ));
 *     }
 * }
 * }</pre>
 * 
 * <h3>Merging Results</h3>
 * <pre>{@code
 * ValidationResult combined = new ValidationResult();
 * combined.merge(schemaValidation);
 * combined.merge(referenceValidation);
 * combined.merge(businessValidation);
 * }</pre>
 * 
 * <h3>Formatting Output</h3>
 * <pre>{@code
 * // Simple format
 * String summary = result.getSummary();
 * // "3 errors, 2 warnings, 1 info"
 * 
 * // Detailed format
 * String report = result.format();
 *  * Validation completed with issues:
 *  * 
 *  * ERRORS (3):
 *  * - Missing required field at $.name
 *  * - Invalid reference at $.transitions[0].to
 *  * - Type mismatch at $.timeout
 *  * 
 *  * WARNINGS (2):
 *  * - Unused state 'tempState'
 *  * - Low similarity threshold 0.5
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Always include JSON path for context</li>
 *   <li>Use appropriate severity levels</li>
 *   <li>Provide actionable error messages</li>
 *   <li>Include suggestions when possible</li>
 *   <li>Group related errors together</li>
 *   <li>Support i18n for messages</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.validation
 * @see io.github.jspinak.brobot.runner.json.validation.exception.ConfigValidationException
 */
package io.github.jspinak.brobot.runner.json.validation.model;