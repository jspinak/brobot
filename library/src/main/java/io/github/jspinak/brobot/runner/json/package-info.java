/**
 * JSON processing infrastructure for Brobot automation projects.
 * 
 * <p>This package provides comprehensive JSON handling capabilities including
 * parsing, serialization, validation, and schema management. It enables Brobot
 * to work with JSON-based configuration files while maintaining type safety
 * and providing detailed error reporting.</p>
 * 
 * <h2>Key Components</h2>
 * 
 * <h3>Core Processing</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser} - 
 *       Central JSON parsing utility</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper} - 
 *       Configured Jackson ObjectMapper</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.SchemaManager} - 
 *       JSON Schema loading and caching</li>
 * </ul>
 * 
 * <h3>Validation</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.ConfigurationValidator} - 
 *       Orchestrates all validation types</li>
 *   <li>Schema validators for structural validation</li>
 *   <li>Reference validators for cross-references</li>
 *   <li>Business rule validators for semantic correctness</li>
 *   <li>Resource validators for external dependencies</li>
 * </ul>
 * 
 * <h3>Serialization</h3>
 * <ul>
 *   <li>Custom serializers for Brobot domain objects</li>
 *   <li>Deserializers for complex type reconstruction</li>
 *   <li>Mixin classes for third-party types</li>
 * </ul>
 * 
 * <h2>JSON Processing Pipeline</h2>
 * 
 * <ol>
 *   <li><b>Parse</b> - Convert JSON text to object tree</li>
 *   <li><b>Schema Validate</b> - Check structure against JSON Schema</li>
 *   <li><b>Reference Validate</b> - Verify all references exist</li>
 *   <li><b>Business Validate</b> - Check semantic rules</li>
 *   <li><b>Deserialize</b> - Convert to Java objects</li>
 * </ol>
 * 
 * <h2>Subpackages</h2>
 * 
 * <h3>config</h3>
 * <p>JSON configuration classes and settings</p>
 * 
 * <h3>parsing</h3>
 * <p>Core parsing utilities and object mappers</p>
 * 
 * <h3>validation</h3>
 * <p>Multi-level validation framework including:</p>
 * <ul>
 *   <li>schema - JSON Schema validation</li>
 *   <li>crossref - Reference integrity checking</li>
 *   <li>business - Domain-specific rule validation</li>
 *   <li>resource - External resource validation</li>
 * </ul>
 * 
 * <h3>serializers</h3>
 * <p>Custom Jackson serializers and deserializers</p>
 * 
 * <h3>mixins</h3>
 * <p>Jackson mixins for third-party classes</p>
 * 
 * <h3>module</h3>
 * <p>Jackson module configuration</p>
 * 
 * <h3>utils</h3>
 * <p>Utility classes for JSON operations</p>
 * 
 * <h2>Schema Support</h2>
 * 
 * <p>The package supports JSON Schema draft v7 for:</p>
 * <ul>
 *   <li>Project configuration schema</li>
 *   <li>Automation DSL schema</li>
 *   <li>State definition schema</li>
 *   <li>Custom validation schemas</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * 
 * <p>Comprehensive error reporting includes:</p>
 * <ul>
 *   <li>Detailed validation messages with paths</li>
 *   <li>Multiple error aggregation</li>
 *   <li>Severity levels (ERROR, WARNING, INFO)</li>
 *   <li>Suggestions for common issues</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Parsing Configuration</h3>
 * <pre>{@code
 * ConfigurationParser parser = new ConfigurationParser();
 * AutomationProject project = parser.parse(
 *     jsonFile, 
 *     AutomationProject.class
 * );
 * }</pre>
 * 
 * <h3>Validation</h3>
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
 *   <li><b>Type Safety</b> - Strong typing despite JSON flexibility</li>
 *   <li><b>Fail Fast</b> - Early validation with clear errors</li>
 *   <li><b>Extensibility</b> - Easy to add new validators and serializers</li>
 *   <li><b>Performance</b> - Caching and lazy loading where appropriate</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.fasterxml.jackson.databind
 * @see com.networknt.schema
 * @see io.github.jspinak.brobot.runner.dsl
 */
package io.github.jspinak.brobot.runner.json;