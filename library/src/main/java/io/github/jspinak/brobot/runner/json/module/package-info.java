/**
 * Jackson module configuration for Brobot JSON processing.
 * 
 * <p>This package contains the Jackson module that configures custom serializers,
 * deserializers, and mixins for the Brobot framework. The module provides
 * centralized configuration for all JSON processing customizations.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.module.BrobotJsonModule} - 
 *       Jackson SimpleModule configuring all custom JSON handlers</li>
 * </ul>
 * 
 * <h2>Module Features</h2>
 * 
 * <h3>Custom Serializers</h3>
 * <ul>
 *   <li><b>ActionOptions</b> - Serializes action configurations</li>
 *   <li><b>ActionResult</b> - Serializes match results</li>
 *   <li><b>ObjectCollection</b> - Handles state references</li>
 *   <li><b>Mat</b> - Converts OpenCV matrices to base64</li>
 *   <li><b>Image</b> - Serializes Brobot images</li>
 * </ul>
 * 
 * <h3>Custom Deserializers</h3>
 * <ul>
 *   <li><b>Image</b> - Reconstructs images from JSON</li>
 *   <li><b>SearchRegions</b> - Builds search configurations</li>
 * </ul>
 * 
 * <h3>Mixin Registration</h3>
 * <p>The module works with mixin classes to handle third-party types from:</p>
 * <ul>
 *   <li>Sikuli (Region, Match, Location)</li>
 *   <li>OpenCV (Mat, Rect)</li>
 *   <li>Java AWT (BufferedImage, Rectangle)</li>
 * </ul>
 * 
 * <h2>Module Registration</h2>
 * 
 * <pre>{@code
 * // Module is automatically registered via Spring
 * @Bean
 * public ObjectMapper objectMapper(BrobotJsonModule module) {
 *     ObjectMapper mapper = new ObjectMapper();
 *     mapper.registerModule(module);
 *     return mapper;
 * }
 * }</pre>
 * 
 * <h2>Serialization Examples</h2>
 * 
 * <h3>ActionOptions</h3>
 * <pre>{@code
 * {
 *   "action": "CLICK",
 *   "pauseAfter": 1000,
 *   "similarity": 0.95
 * }
 * }</pre>
 * 
 * <h3>ObjectCollection</h3>
 * <pre>{@code
 * {
 *   "stateImages": ["button1", "button2"],
 *   "regions": [
 *     {"x": 10, "y": 20, "w": 100, "h": 50}
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Image</h3>
 * <pre>{@code
 * {
 *   "name": "loginButton",
 *   "path": "images/login.png",
 *   "similarity": 0.9
 * }
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><b>Centralized</b> - All JSON customizations in one place</li>
 *   <li><b>Modular</b> - Easy to add/remove serializers</li>
 *   <li><b>Type-safe</b> - Compile-time type checking</li>
 *   <li><b>Efficient</b> - Minimal overhead in processing</li>
 * </ul>
 * 
 * <h2>Extension Points</h2>
 * 
 * <p>To add new serializers/deserializers:</p>
 * <ol>
 *   <li>Create the serializer/deserializer class</li>
 *   <li>Add as Spring component</li>
 *   <li>Inject into BrobotJsonModule</li>
 *   <li>Register in module configuration</li>
 * </ol>
 * 
 * @since 1.0
 * @see com.fasterxml.jackson.databind.module.SimpleModule
 * @see io.github.jspinak.brobot.runner.json.serializers
 * @see io.github.jspinak.brobot.runner.json.mixins
 */
package io.github.jspinak.brobot.runner.json.module;