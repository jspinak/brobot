/**
 * Core JSON parsing utilities and configuration.
 * 
 * <p>This package provides the fundamental JSON parsing infrastructure for Brobot,
 * including configured object mappers, parsing utilities, schema management, and
 * JSON path operations. It forms the foundation for all JSON processing in the
 * framework.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser} - 
 *       Central JSON parsing utility with error handling</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper} - 
 *       Configured Jackson ObjectMapper for Brobot</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.SchemaManager} - 
 *       JSON Schema loading and validation</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.JsonPathUtils} - 
 *       JSON path query utilities</li>
 * </ul>
 * 
 * <h2>Configuration Parser</h2>
 * 
 * <p>The ConfigurationParser provides robust JSON parsing:</p>
 * <pre>{@code
 * ConfigurationParser parser = new ConfigurationParser();
 * 
 * // Parse with validation
 * AutomationProject project = parser.parseWithValidation(
 *     jsonFile,
 *     AutomationProject.class,
 *     SchemaManager.PROJECT_SCHEMA_PATH
 * );
 * 
 * // Parse without validation
 * InstructionSet instructions = parser.parse(
 *     jsonString,
 *     InstructionSet.class
 * );
 * }</pre>
 * 
 * <h2>Object Mapper Configuration</h2>
 * 
 * <p>BrobotObjectMapper provides a pre-configured mapper:</p>
 * <ul>
 *   <li>Brobot custom module registered</li>
 *   <li>All mixins configured</li>
 *   <li>Pretty printing enabled</li>
 *   <li>Unknown properties ignored</li>
 *   <li>Java 8 time support</li>
 * </ul>
 * 
 * <h2>Schema Management</h2>
 * 
 * <p>SchemaManager provides schema operations:</p>
 * <pre>{@code
 * SchemaManager schemas = new SchemaManager();
 * 
 * // Validate against schema
 * Set<ValidationMessage> errors = schemas.validate(
 *     jsonNode,
 *     SchemaManager.PROJECT_SCHEMA_PATH
 * );
 * 
 * // Get specific schemas
 * JsonSchema projectSchema = schemas.getProjectSchema();
 * JsonSchema dslSchema = schemas.getAutomationDslSchema();
 * }</pre>
 * 
 * <h2>JSON Path Operations</h2>
 * 
 * <p>JsonPathUtils enables path-based queries:</p>
 * <pre>{@code
 * // Query JSON with path
 * String stateName = JsonPathUtils.getString(
 *     json,
 *     "$.states[0].name"
 * );
 * 
 * // Extract all matching values
 * List<String> allNames = JsonPathUtils.getList(
 *     json,
 *     "$..name"
 * );
 * }</pre>
 * 
 * <h2>Error Handling</h2>
 * 
 * <p>Comprehensive error handling includes:</p>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException} - 
 *       For parsing and configuration errors</li>
 *   <li>Detailed error messages with context</li>
 *   <li>JSON path information in errors</li>
 *   <li>Suggestions for common issues</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Always validate against schemas for user input</li>
 *   <li>Use the shared BrobotObjectMapper instance</li>
 *   <li>Cache parsed configurations when possible</li>
 *   <li>Handle ConfigurationException explicitly</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>Schema compilation is cached</li>
 *   <li>Object mapper is thread-safe</li>
 *   <li>Large file streaming supported</li>
 *   <li>Lazy loading for nested objects</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.networknt.schema
 * @see com.jayway.jsonpath
 */
package io.github.jspinak.brobot.runner.json.parsing;