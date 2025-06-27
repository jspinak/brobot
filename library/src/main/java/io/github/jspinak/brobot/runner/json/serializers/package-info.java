/**
 * Custom Jackson serializers and deserializers for Brobot types.
 * 
 * <p>This package contains specialized serializers and deserializers that handle
 * the conversion between Brobot domain objects and their JSON representations.
 * These components ensure proper serialization of complex types while maintaining
 * readability and avoiding circular references.</p>
 * 
 * <h2>Serializers</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.ActionOptionsSerializer} - 
 *       Serializes action configuration with readable format</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.MatchesSerializer} - 
 *       Serializes ActionResult with match information</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.ObjectCollectionSerializer} - 
 *       Handles object collections with state references</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.MatSerializer} - 
 *       Converts OpenCV Mat to base64 encoded data</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.ImageSerializer} - 
 *       Serializes Brobot Image objects</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.RectSerializer} - 
 *       Serializes rectangle geometry</li>
 * </ul>
 * 
 * <h2>Deserializers</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.ImageDeserializer} - 
 *       Reconstructs Image objects from JSON</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.serializers.SearchRegionsDeserializer} - 
 *       Builds SearchRegions from configuration</li>
 * </ul>
 * 
 * <h2>Serialization Examples</h2>
 * 
 * <h3>ActionOptions</h3>
 * <pre>{@code
 * // Java object
 * ActionOptions options = new ActionOptions.Builder()
 *     .action(Action.CLICK)
 *     .similarity(0.95)
 *     .pauseAfter(1000)
 *     .build();
 * 
 * // JSON output
 * {
 *   "action": "CLICK",
 *   "similarity": 0.95,
 *   "pauseAfter": 1000
 * }
 * }</pre>
 * 
 * <h3>ObjectCollection</h3>
 * <pre>{@code
 * // References by name instead of full objects
 * {
 *   "stateImages": ["loginButton", "submitButton"],
 *   "regions": [
 *     {"x": 100, "y": 200, "w": 150, "h": 50}
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Mat (OpenCV)</h3>
 * <pre>{@code
 * // Binary data as base64
 * {
 *   "rows": 480,
 *   "cols": 640,
 *   "type": 16,
 *   "data": "iVBORw0KGgoAAAANS..."
 * }
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <h3>Readability</h3>
 * <ul>
 *   <li>Human-readable field names</li>
 *   <li>Logical structure</li>
 *   <li>Minimal nesting</li>
 * </ul>
 * 
 * <h3>Efficiency</h3>
 * <ul>
 *   <li>Reference objects by ID/name</li>
 *   <li>Avoid circular references</li>
 *   <li>Compress binary data</li>
 * </ul>
 * 
 * <h3>Compatibility</h3>
 * <ul>
 *   <li>Handle missing fields gracefully</li>
 *   <li>Support version migration</li>
 *   <li>Preserve unknown properties</li>
 * </ul>
 * 
 * <h2>Custom Serializer Pattern</h2>
 * 
 * <pre>{@code
 * @Component
 * public class MyTypeSerializer extends JsonSerializer<MyType> {
 *     @Override
 *     public void serialize(
 *             MyType value,
 *             JsonGenerator gen,
 *             SerializerProvider provider) throws IOException {
 *         
 *         gen.writeStartObject();
 *         gen.writeStringField("id", value.getId());
 *         gen.writeNumberField("score", value.getScore());
 *         // ... other fields
 *         gen.writeEndObject();
 *     }
 * }
 * }</pre>
 * 
 * <h2>Utility Classes</h2>
 * 
 * <p>The package includes utility classes for common operations:</p>
 * <ul>
 *   <li>ActionOptionsJsonUtils - Helper methods for ActionOptions</li>
 *   <li>MatchesJsonUtils - Helper methods for match results</li>
 *   <li>ObjectCollectionJsonUtils - Collection handling utilities</li>
 *   <li>JsonUtils - General JSON utilities</li>
 * </ul>
 * 
 * @since 1.0
 * @see com.fasterxml.jackson.databind.JsonSerializer
 * @see com.fasterxml.jackson.databind.JsonDeserializer
 * @see io.github.jspinak.brobot.runner.json.module.BrobotJsonModule
 */
package io.github.jspinak.brobot.runner.json.serializers;