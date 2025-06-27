/**
 * Contains domain models for machine learning training data representation.
 * 
 * <p>This package defines the core data structures used to represent training
 * examples for GUI automation machine learning. The models encapsulate the
 * essential information needed to train neural networks on automation tasks,
 * including action representations, contextual information, and visual data.
 * 
 * <h2>Core Models</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector} - 
 *       Fixed-size numerical representation of GUI actions</li>
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.model.TrainingExample} - 
 *       Complete training instance with vectors, text, and screenshots</li>
 * </ul>
 * 
 * <h2>ActionVector Structure</h2>
 * <p>The ActionVector provides a fixed-size array of 100 short values to encode:
 * <ul>
 *   <li><strong>Action Type</strong>: Categorical representation of the action</li>
 *   <li><strong>Spatial Data</strong>: Coordinates, dimensions, and positions</li>
 *   <li><strong>Action Parameters</strong>: Additional options and settings</li>
 *   <li><strong>Reserved Space</strong>: Room for future feature expansion</li>
 * </ul>
 * 
 * <h2>TrainingExample Components</h2>
 * <p>Each training example consists of:
 * <ul>
 *   <li><strong>Vector</strong>: The numerical action representation</li>
 *   <li><strong>Text</strong>: Human-readable description of the action</li>
 *   <li><strong>Screenshots</strong>: Visual context (before/after states)</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><strong>Immutability</strong>: Models use defensive copying to prevent
 *       unintended modifications</li>
 *   <li><strong>Serialization Support</strong>: Custom serialization handles
 *       complex image data efficiently</li>
 *   <li><strong>Builder Pattern</strong>: TrainingExample provides a fluent
 *       builder for safe construction</li>
 *   <li><strong>Flexibility</strong>: Screenshot lists support variable numbers
 *       of images for different training scenarios</li>
 * </ul>
 * 
 * <h2>Data Type Choices</h2>
 * <ul>
 *   <li><strong>short[]</strong>: Provides sufficient range (-32,768 to 32,767)
 *       for screen coordinates while being memory efficient</li>
 *   <li><strong>ArrayList&lt;BufferedImage&gt;</strong>: Allows dynamic screenshot
 *       counts for research flexibility</li>
 *   <li><strong>Transient Images</strong>: Custom serialization prevents default
 *       Java serialization issues with BufferedImage</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Using the builder pattern
 * TrainingExample example = new TrainingExample.Builder()
 *     .withVector(new short[]{1, 0, 0, 0, 0, 0, 100, 200, 50, 30})
 *     .withText("Click submit button at (100, 200)")
 *     .addScreenshot(beforeImage)
 *     .addScreenshot(afterImage)
 *     .build();
 * 
 * // Direct construction (legacy)
 * ActionVector vector = new ActionVector();
 * short[] data = vector.getVector();
 * data[0] = 1; // Set action type
 * }</pre>
 * 
 * <h2>Serialization Details</h2>
 * <p>TrainingExample implements custom serialization to handle BufferedImage:
 * <ul>
 *   <li>Images are converted to PNG byte arrays during serialization</li>
 *   <li>Deserialization reconstructs BufferedImage from PNG data</li>
 *   <li>This approach ensures compatibility and reduces file size</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>Models in this package are not thread-safe. If concurrent access is needed:
 * <ul>
 *   <li>Use external synchronization</li>
 *   <li>Create defensive copies</li>
 *   <li>Consider using immutable wrappers</li>
 * </ul>
 * 
 * <h2>Future Considerations</h2>
 * <ul>
 *   <li>Consider making models fully immutable</li>
 *   <li>Add validation for vector ranges and sizes</li>
 *   <li>Support for additional metadata fields</li>
 *   <li>Integration with standard ML data formats</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.ml.dataset.encoding
 * @see io.github.jspinak.brobot.tools.ml.dataset.io
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.ml.dataset.model;