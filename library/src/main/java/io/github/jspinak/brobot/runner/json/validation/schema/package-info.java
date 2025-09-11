/**
 * JSON Schema validators for structural validation.
 *
 * <p>This package contains validators that use JSON Schema to ensure configurations conform to
 * their expected structure. Schema validation provides the first line of defense against malformed
 * configurations by checking data types, required fields, and value constraints.
 *
 * <h2>Validators</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.schema.SchemaValidator} - Base
 *       schema validation interface
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.schema.ProjectSchemaValidator} -
 *       Validates project configurations
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.schema.AutomationDSLValidator} -
 *       Validates DSL definitions
 * </ul>
 *
 * <h2>JSON Schema Features</h2>
 *
 * <h3>Type Validation</h3>
 *
 * <pre>{@code
 * {
 *   "type": "object",
 *   "properties": {
 *     "name": {"type": "string"},
 *     "timeout": {"type": "number"},
 *     "enabled": {"type": "boolean"},
 *     "tags": {
 *       "type": "array",
 *       "items": {"type": "string"}
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h3>Required Fields</h3>
 *
 * <pre>{@code
 * {
 *   "type": "object",
 *   "required": ["name", "version", "states"],
 *   "properties": {
 *     "name": {"type": "string"},
 *     "version": {"type": "string"},
 *     "states": {"type": "array"}
 *   }
 * }
 * }</pre>
 *
 * <h3>Value Constraints</h3>
 *
 * <pre>{@code
 * {
 *   "properties": {
 *     "similarity": {
 *       "type": "number",
 *       "minimum": 0.0,
 *       "maximum": 1.0
 *     },
 *     "retries": {
 *       "type": "integer",
 *       "minimum": 0,
 *       "maximum": 10
 *     },
 *     "action": {
 *       "type": "string",
 *       "enum": ["CLICK", "TYPE", "FIND", "DRAG"]
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h3>Pattern Matching</h3>
 *
 * <pre>{@code
 * {
 *   "properties": {
 *     "id": {
 *       "type": "string",
 *       "pattern": "^[a-zA-Z][a-zA-Z0-9_]*$"
 *     },
 *     "email": {
 *       "type": "string",
 *       "format": "email"
 *     },
 *     "url": {
 *       "type": "string",
 *       "format": "uri"
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h2>Schema Organization</h2>
 *
 * <h3>Main Schemas</h3>
 *
 * <ul>
 *   <li><b>project-schema.json</b> - Overall project structure
 *   <li><b>automation-dsl-schema.json</b> - DSL language constructs
 *   <li><b>state-schema.json</b> - State definitions
 *   <li><b>transition-schema.json</b> - Transition specifications
 * </ul>
 *
 * <h3>Schema Composition</h3>
 *
 * <pre>{@code
 * {
 *   "$ref": "#/definitions/State",
 *   "definitions": {
 *     "State": {
 *       "type": "object",
 *       "properties": {
 *         "name": {"type": "string"},
 *         "images": {
 *           "type": "array",
 *           "items": {"$ref": "#/definitions/StateImage"}
 *         }
 *       }
 *     },
 *     "StateImage": {
 *       "type": "object",
 *       "properties": {
 *         "name": {"type": "string"},
 *         "path": {"type": "string"}
 *       }
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h2>Validation Process</h2>
 *
 * <pre>{@code
 * // Load and compile schema
 * JsonSchema schema = schemaManager.getProjectSchema();
 *
 * // Validate JSON
 * Set<ValidationMessage> errors = schema.validate(jsonNode);
 *
 * // Convert to ValidationResult
 * ValidationResult result = new ValidationResult();
 * for (ValidationMessage msg : errors) {
 *     result.addError(new ValidationError(
 *         ERROR,
 *         msg.getMessage(),
 *         msg.getPath()
 *     ));
 * }
 * }</pre>
 *
 * <h2>Custom Keywords</h2>
 *
 * <p>Extend schemas with custom validation:
 *
 * <pre>{@code
 * {
 *   "properties": {
 *     "state": {
 *       "type": "string",
 *       "stateReference": true  // Custom keyword
 *     }
 *   }
 * }
 * }</pre>
 *
 * <h2>Error Messages</h2>
 *
 * <p>Schema validation provides detailed errors:
 *
 * <pre>{@code
 * - $.project.name: is missing but it is required
 * - $.timeout: 1500.5 is not a valid integer
 * - $.action: "HOVER" is not one of ["CLICK","TYPE","FIND"]
 * - $.id: "123abc" does not match pattern ^[a-zA-Z][a-zA-Z0-9_]*$
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Keep schemas versioned and documented
 *   <li>Use clear, descriptive property names
 *   <li>Provide examples in schema descriptions
 *   <li>Compose complex schemas from definitions
 *   <li>Include format validations where applicable
 *   <li>Test schemas with valid and invalid examples
 * </ul>
 *
 * @since 1.0
 * @see com.networknt.schema
 * @see io.github.jspinak.brobot.runner.json.parsing.SchemaManager
 */
package io.github.jspinak.brobot.runner.json.validation.schema;
