/**
 * Provides action encoding strategies for machine learning vector representations.
 *
 * <p>This package contains the framework for converting GUI automation actions into numerical
 * vectors suitable for neural network training. The encoding strategies transform high-level action
 * descriptions into fixed-size vector representations that preserve the essential characteristics
 * of each action.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.encoding.ActionVectorTranslator} -
 *       Interface defining the contract for bidirectional action-vector conversion
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.encoding.OneHotActionVectorEncoder} -
 *       Implementation using one-hot encoding for categorical action types
 * </ul>
 *
 * <h2>Encoding Strategy</h2>
 *
 * <p>The current one-hot encoding implementation structures vectors as follows:
 *
 * <ul>
 *   <li><strong>Positions 0-5</strong>: One-hot encoded action type (CLICK, DRAG, TYPE, MOVE,
 *       SCROLL, HIGHLIGHT)
 *   <li><strong>Positions 6-9</strong>: Spatial coordinates (x, y, width, height)
 *   <li><strong>Positions 10-11</strong>: Action-specific options (e.g., highlight color, flags)
 *   <li><strong>Positions 12-99</strong>: Reserved for future expansion
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><strong>Categorical Independence</strong>: One-hot encoding prevents the model from
 *       inferring false ordinal relationships between action types
 *   <li><strong>Fixed Size</strong>: All vectors have a consistent size (100 elements) to maintain
 *       compatibility with neural network architectures
 *   <li><strong>Extensibility</strong>: The vector size provides room for additional features
 *       without breaking existing models
 *   <li><strong>Type Safety</strong>: Uses enums and constants to prevent encoding errors
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Encoding an action result
 * ActionVectorTranslator encoder = new OneHotActionVectorEncoder();
 * ActionResult result = performClickAction();
 * ActionVector vector = encoder.toVector(result);
 *
 * // Decoding back to action options (partial implementation)
 * ActionConfig decoded = encoder.toActionConfig(vector);
 * }</pre>
 *
 * <h2>Highlight Color Support</h2>
 *
 * <p>The encoder includes an enum for highlight colors with predefined mappings:
 *
 * <ul>
 *   <li>BLUE (0), RED (1), YELLOW (2), GREEN (3), ORANGE (4)
 *   <li>PURPLE (5), WHITE (6), BLACK (7), GREY (8)
 * </ul>
 *
 * <h2>Implementation Notes</h2>
 *
 * <ul>
 *   <li>Failed actions (empty matches) return zero vectors
 *   <li>Coordinates are extracted from the best match in the action result
 *   <li>The short data type (-32,768 to 32,767) accommodates screen coordinates and preserves
 *       precision for continuous values
 *   <li>Future encoders might implement alternative strategies like embeddings or continuous
 *       representations
 * </ul>
 *
 * <h2>Creating Custom Encoders</h2>
 *
 * <p>To implement a custom encoding strategy:
 *
 * <ol>
 *   <li>Implement the {@link
 *       io.github.jspinak.brobot.tools.ml.dataset.encoding.ActionVectorTranslator} interface
 *   <li>Define your vector structure and document the position mappings
 *   <li>Implement both toVector() and toActionConfig() methods
 *   <li>Consider implementing toObjectCollection() for spatial data reconstruction
 *   <li>Ensure thread safety if the encoder maintains state
 * </ol>
 *
 * @see io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector
 * @see io.github.jspinak.brobot.action.ActionResult
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.ml.dataset.encoding;
