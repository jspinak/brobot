/**
 * JSON processing infrastructure for Brobot automation projects.
 *
 * <p>This package provides comprehensive JSON handling capabilities including parsing,
 * serialization, validation, and schema management. It enables Brobot to work with JSON-based
 * configuration files while maintaining type safety and providing detailed error reporting.
 *
 * <h2>Key Components</h2>
 *
 * <h3>Core Processing</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser} - Central JSON
 *       parsing utility
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper} - Configured
 *       Jackson ObjectMapper
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.SchemaManager} - JSON Schema loading
 *       and caching
 * </ul>
 *
 * <h3>Validation</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator} -
 *       Orchestrates all validation types
 *   <li>Schema validators for structural validation
 *   <li>Reference validators for cross-references
 *   <li>Business rule validators for semantic correctness
 *   <li>Resource validators for external dependencies
 * </ul>
 *
 * <h3>Serialization</h3>
 *
 * <ul>
 *   <li>Custom serializers for Brobot domain objects
 *   <li>Deserializers for complex type reconstruction
 *   <li>Mixin classes for third-party types
 * </ul>
 *
 * <h2>JSON Processing Pipeline</h2>
 *
 * <ol>
 *   <li><b>Parse</b> - Convert JSON text to object tree
 *   <li><b>Schema Validate</b> - Check structure against JSON Schema
 *   <li><b>Reference Validate</b> - Verify all references exist
 *   <li><b>Business Validate</b> - Check semantic rules
 *   <li><b>Deserialize</b> - Convert to Java objects
 * </ol>
 *
 * <h2>Subpackages</h2>
 *
 * <h3>config</h3>
 *
 * <p>JSON configuration classes and settings
 *
 * <h3>parsing</h3>
 *
 * <p>Core parsing utilities and object mappers
 *
 * <h3>validation</h3>
 *
 * <p>Multi-level validation framework including:
 *
 * <ul>
 *   <li>schema - JSON Schema validation
 *   <li>crossref - Reference integrity checking
 *   <li>business - Domain-specific rule validation
 *   <li>resource - External resource validation
 * </ul>
 *
 * <h3>serializers</h3>
 *
 * <p>Custom Jackson serializers and deserializers
 *
 * <h3>mixins</h3>
 *
 * <p>Jackson mixins for third-party classes
 *
 * <h3>module</h3>
 *
 * <p>Jackson module configuration
 *
 * <h3>utils</h3>
 *
 * <p>Utility classes for JSON operations
 *
 * <h2>Schema Support</h2>
 *
 * <p>The package supports JSON Schema draft v7 for:
 *
 * <ul>
 *   <li>Project configuration schema
 *   <li>Automation DSL schema
 *   <li>State definition schema
 *   <li>Custom validation schemas
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Comprehensive error reporting includes:
 *
 * <ul>
 *   <li>Detailed validation messages with paths
 *   <li>Multiple error aggregation
 *   <li>Severity levels (ERROR, WARNING, INFO)
 *   <li>Suggestions for common issues
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Parsing Configuration</h3>
 *
 * <pre>{@code
 * ConfigurationParser parser = new ConfigurationParser();
 * AutomationProject project = parser.parse(
 *     jsonFile,
 *     AutomationProject.class
 * );
 * }</pre>
 *
 * <h3>Validation</h3>
 *
 * <pre>{@code
 * ConfigurationValidator validator = new ConfigurationValidator();
 * ValidationResult result = validator.validate(project);
 *
 * if (result.hasErrors()) {
 *     result.getErrors().forEach(error ->
 *         logger.error(error.getMessage())
 *     );
 * }
 * }</pre>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Type Safety</b> - Strong typing despite JSON flexibility
 *   <li><b>Fail Fast</b> - Early validation with clear errors
 *   <li><b>Extensibility</b> - Easy to add new validators and serializers
 *   <li><b>Performance</b> - Caching and lazy loading where appropriate
 * </ul>
 *
 * @since 1.0
 * @see com.fasterxml.jackson.databind
 * @see com.networknt.schema
 * @see io.github.jspinak.brobot.runner.dsl
 */
package io.github.jspinak.brobot.runner.json;
