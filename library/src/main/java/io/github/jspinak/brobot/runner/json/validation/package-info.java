/**
 * Multi-level validation framework for JSON configurations.
 * 
 * <p>This package implements a comprehensive validation system that ensures
 * JSON configurations are both syntactically correct (schema validation) and
 * semantically valid (business rules, references, resources). The validation
 * framework uses a pipeline approach with multiple specialized validators.</p>
 * 
 * <h2>Validation Pipeline</h2>
 * 
 * <ol>
 *   <li><b>Schema Validation</b> - Structure and type checking</li>
 *   <li><b>Reference Validation</b> - Cross-reference integrity</li>
 *   <li><b>Business Rule Validation</b> - Domain-specific rules</li>
 *   <li><b>Resource Validation</b> - External resource availability</li>
 * </ol>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator} - 
 *       Main validator orchestrating all validation types</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationResult} - 
 *       Aggregated validation results</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationError} - 
 *       Individual validation error details</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity} - 
 *       Error severity levels (ERROR, WARNING, INFO)</li>
 * </ul>
 * 
 * <h2>Validation Types</h2>
 * 
 * <h3>Schema Validation</h3>
 * <p>Validates JSON structure against JSON Schema:</p>
 * <ul>
 *   <li>Required fields presence</li>
 *   <li>Data type correctness</li>
 *   <li>Value constraints</li>
 *   <li>Pattern matching</li>
 * </ul>
 * 
 * <h3>Reference Validation</h3>
 * <p>Ensures all references resolve correctly:</p>
 * <ul>
 *   <li>State references exist</li>
 *   <li>Function references are valid</li>
 *   <li>Image references point to defined images</li>
 *   <li>No circular dependencies</li>
 * </ul>
 * 
 * <h3>Business Rule Validation</h3>
 * <p>Enforces domain-specific constraints:</p>
 * <ul>
 *   <li>Transition logic validity</li>
 *   <li>Action sequence coherence</li>
 *   <li>Parameter compatibility</li>
 *   <li>State reachability</li>
 * </ul>
 * 
 * <h3>Resource Validation</h3>
 * <p>Verifies external resources:</p>
 * <ul>
 *   <li>Image files exist</li>
 *   <li>File permissions</li>
 *   <li>Resource accessibility</li>
 *   <li>Format compatibility</li>
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
 * <p>JSON Schema-based structural validation</p>
 * 
 * <h3>crossref</h3>
 * <p>Reference integrity validation</p>
 * 
 * <h3>business</h3>
 * <p>Business rule and logic validation</p>
 * 
 * <h3>resource</h3>
 * <p>External resource validation</p>
 * 
 * <h3>model</h3>
 * <p>Validation result data structures</p>
 * 
 * <h3>exception</h3>
 * <p>Validation-specific exceptions</p>
 * 
 * <h2>Custom Validators</h2>
 * 
 * <p>Creating custom validators:</p>
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
 *   <li>Validate early and comprehensively</li>
 *   <li>Provide clear, actionable error messages</li>
 *   <li>Include path information in errors</li>
 *   <li>Use appropriate severity levels</li>
 *   <li>Batch related validations for efficiency</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.parsing
 * @see com.networknt.schema
 */
package io.github.jspinak.brobot.runner.json.validation;