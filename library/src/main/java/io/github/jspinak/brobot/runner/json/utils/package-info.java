/**
 * Utility classes for JSON operations and transformations.
 *
 * <p>This package provides helper utilities that simplify common JSON operations, data
 * transformations, and object manipulations used throughout the Brobot JSON processing
 * infrastructure. These utilities complement the serializers and deserializers with reusable
 * functionality.
 *
 * <h2>Utility Classes</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.utils.JsonUtils} - General-purpose JSON
 *       manipulation utilities
 *   <li>{@link io.github.jspinak.brobot.runner.json.utils.ActionOptionsJsonUtils} - Utilities
 *       specific to ActionOptions processing
 *   <li>{@link io.github.jspinak.brobot.runner.json.utils.MatchesJsonUtils} - Utilities for match
 *       result handling
 *   <li>{@link io.github.jspinak.brobot.runner.json.utils.ObjectCollectionJsonUtils} - Helper
 *       methods for object collections
 * </ul>
 *
 * <h2>Common Operations</h2>
 *
 * <h3>JSON Node Manipulation</h3>
 *
 * <pre>{@code
 * // Safe value extraction
 * String value = JsonUtils.getStringOrDefault(
 *     node,
 *     "fieldName",
 *     "defaultValue"
 * );
 *
 * // Deep merging
 * JsonNode merged = JsonUtils.deepMerge(node1, node2);
 *
 * // Path-based updates
 * JsonUtils.setValueAtPath(
 *     node,
 *     "$.settings.timeout",
 *     5000
 * );
 * }</pre>
 *
 * <h3>ActionOptions Utilities</h3>
 *
 * <pre>{@code
 * // Convert to simple map
 * Map<String, Object> map = ActionOptionsJsonUtils.toMap(options);
 *
 * // Filter null values
 * JsonNode cleaned = ActionOptionsJsonUtils.removeNulls(optionsNode);
 *
 * // Merge with defaults
 * ActionOptions merged = ActionOptionsJsonUtils.mergeWithDefaults(
 *     partial,
 *     defaults
 * );
 * }</pre>
 *
 * <h3>Match Processing</h3>
 *
 * <pre>{@code
 * // Extract match summaries
 * List<Map<String, Object>> summaries =
 *     MatchesJsonUtils.extractMatchSummaries(actionResult);
 *
 * // Convert to display format
 * JsonNode display = MatchesJsonUtils.toDisplayFormat(matches);
 *
 * // Filter by score
 * List<Match> filtered = MatchesJsonUtils.filterByScore(
 *     matches,
 *     0.9
 * );
 * }</pre>
 *
 * <h3>Collection Handling</h3>
 *
 * <pre>{@code
 * // Resolve state references
 * ObjectCollection resolved =
 *     ObjectCollectionJsonUtils.resolveReferences(
 *         collection,
 *         stateService
 *     );
 *
 * // Convert to ID list
 * List<String> ids = ObjectCollectionJsonUtils.extractIds(collection);
 *
 * // Validate collection
 * boolean valid = ObjectCollectionJsonUtils.isValid(collection);
 * }</pre>
 *
 * <h2>Type Conversions</h2>
 *
 * <p>Safe type conversion utilities:
 *
 * <pre>{@code
 * // String to enum
 * Action action = JsonUtils.toEnum(
 *     "CLICK",
 *     Action.class,
 *     Action.FIND
 * );
 *
 * // Node to specific type
 * Point point = JsonUtils.treeToValue(
 *     pointNode,
 *     Point.class
 * );
 *
 * // Collection conversions
 * List<String> list = JsonUtils.nodeToStringList(arrayNode);
 * }</pre>
 *
 * <h2>Validation Helpers</h2>
 *
 * <pre>{@code
 * // Check required fields
 * JsonUtils.validateRequired(
 *     node,
 *     "name", "type", "value"
 * );
 *
 * // Type checking
 * if (JsonUtils.isNumeric(node)) {
 *     double value = node.asDouble();
 * }
 *
 * // Range validation
 * JsonUtils.validateRange(
 *     node,
 *     "score",
 *     0.0,
 *     1.0
 * );
 * }</pre>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Utilities provide consistent error handling:
 *
 * <ul>
 *   <li>Safe default values for missing fields
 *   <li>Clear error messages with context
 *   <li>Null-safe operations
 *   <li>Type conversion validation
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Utilities are stateless and thread-safe
 *   <li>Avoid repeated parsing of same data
 *   <li>Use streaming for large collections
 *   <li>Cache converted values when appropriate
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.serializers
 * @see com.fasterxml.jackson.databind.JsonNode
 */
package io.github.jspinak.brobot.runner.json.utils;
