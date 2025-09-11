/**
 * Multi-level validation framework for JSON configurations.
 *
 * <p>This package implements a comprehensive validation system that ensures JSON configurations are
 * both syntactically correct (schema validation) and semantically valid (business rules,
 * references, resources). The validation framework uses a pipeline approach with multiple
 * specialized validators.
 *
 * <h2>Validation Pipeline</h2>
 *
 * <ol>
 *   <li><b>Schema Validation</b> - Structure and type checking
 *   <li><b>Reference Validation</b> - Cross-reference integrity
 *   <li><b>Business Rule Validation</b> - Domain-specific rules
 *   <li><b>Resource Validation</b> - External resource availability
 * </ol>
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator} - Main
 *       validator orchestrating all validation types
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationResult} - Aggregated
 *       validation results
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationError} - Individual
 *       validation error details
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity} - Error
 *       severity levels (ERROR, WARNING, INFO)
 * </ul>
 *
 * <h2>Validation Types</h2>
 *
 * <h3>Schema Validation</h3>
 *
 * <p>Validates JSON structure against JSON Schema:
 *
 * <ul>
 *   <li>Required fields presence
 *   <li>Data type correctness
 *   <li>Value constraints
 *   <li>Pattern matching
 * </ul>
 *
 * <h3>Reference Validation</h3>
 *
 * <p>Ensures all references resolve correctly:
 *
 * <ul>
 *   <li>State references exist
 *   <li>Function references are valid
 *   <li>Image references point to defined images
 *   <li>No circular dependencies
 * </ul>
 *
 * <h3>Business Rule Validation</h3>
 *
 * <p>Enforces domain-specific constraints:
 *
 * <ul>
 *   <li>Transition logic validity
 *   <li>Action sequence coherence
 *   <li>Parameter compatibility
 *   <li>State reachability
 * </ul>
 *
 * <h3>Resource Validation</h3>
 *
 * <p>Verifies external resources:
 *
 * <ul>
 *   <li>Image files exist
 *   <li>File permissions
 *   <li>Resource accessibility
 *   <li>Format compatibility
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create validator
 * ConfigurationValidator validator = new ConfigurationValidator(
 *     schemaValidator,
 *     referenceValidator,
 *     businessRuleValidator,
 *     resourceValidator
 * );
 *
 * // Validate configuration
 * ValidationResult result = validator.validate(project);
 *
 * // Check results
 * if (result.hasErrors()) {
 *     for (ValidationError error : result.getErrors()) {
 *         logger.error("{}: {} at {}",
 *             error.getSeverity(),
 *             error.getMessage(),
 *             error.getPath()
 *         );
 *     }
 *     throw new ConfigValidationException(result);
 * }
 *
 * // Handle warnings
 * for (ValidationError warning : result.getWarnings()) {
 *     logger.warn("Warning: {}", warning.getMessage());
 * }
 * }</pre>
 *
 * <h2>Subpackages</h2>
 *
 * <h3>schema</h3>
 *
 * <p>JSON Schema-based structural validation
 *
 * <h3>crossref</h3>
 *
 * <p>Reference integrity validation
 *
 * <h3>business</h3>
 *
 * <p>Business rule and logic validation
 *
 * <h3>resource</h3>
 *
 * <p>External resource validation
 *
 * <h3>model</h3>
 *
 * <p>Validation result data structures
 *
 * <h3>exception</h3>
 *
 * <p>Validation-specific exceptions
 *
 * <h2>Custom Validators</h2>
 *
 * <p>Creating custom validators:
 *
 * <pre>{@code
 * @Component
 * public class MyValidator implements Validator<MyType> {
 *     @Override
 *     public ValidationResult validate(MyType object) {
 *         ValidationResult result = new ValidationResult();
 *
 *         // Perform validation
 *         if (object.getValue() < 0) {
 *             result.addError(new ValidationError(
 *                 ValidationSeverity.ERROR,
 *                 "Value must be non-negative",
 *                 "$.value"
 *             ));
 *         }
 *
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Validate early and comprehensively
 *   <li>Provide clear, actionable error messages
 *   <li>Include path information in errors
 *   <li>Use appropriate severity levels
 *   <li>Batch related validations for efficiency
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.parsing
 * @see com.networknt.schema
 */
package io.github.jspinak.brobot.runner.json.validation;
