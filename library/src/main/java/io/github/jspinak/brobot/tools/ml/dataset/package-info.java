/**
 * Provides machine learning dataset management capabilities for GUI automation training.
 * 
 * <p>This package forms the foundation of Brobot's machine learning infrastructure,
 * offering tools to capture, encode, store, and retrieve training data from automated
 * GUI interactions. The dataset framework is designed to support neural network training
 * for predicting and executing GUI automation actions.
 * 
 * <h2>Core Component</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.DatasetManager} - Orchestrates
 *       the collection and storage of training data during automation execution</li>
 * </ul>
 * 
 * <h2>Architecture Overview</h2>
 * <p>The dataset framework is organized into specialized subpackages:
 * <ul>
 *   <li><strong>model</strong> - Domain models representing training data structures</li>
 *   <li><strong>encoding</strong> - Vector encoding strategies for action representation</li>
 *   <li><strong>io</strong> - Persistence layer for reading and writing training data</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Automated capture of before/after screenshots during action execution</li>
 *   <li>Flexible vector encoding supporting various action types</li>
 *   <li>Configurable persistence with custom file paths</li>
 *   <li>Extensible architecture for different encoding strategies</li>
 *   <li>Type-safe builder patterns for data construction</li>
 * </ul>
 * 
 * <h2>Supported Actions</h2>
 * <p>The framework focuses on GUI-modifying actions suitable for ML training:
 * <ul>
 *   <li>CLICK - Mouse click interactions</li>
 *   <li>DRAG - Drag and drop operations</li>
 *   <li>TYPE - Keyboard input</li>
 *   <li>MOVE - Mouse movement</li>
 *   <li>SCROLL_MOUSE_WHEEL - Scrolling operations</li>
 *   <li>HIGHLIGHT - Visual highlighting (for testing)</li>
 * </ul>
 * 
 * <h2>Workflow Example</h2>
 * <pre>{@code
 * // Configure dataset manager (typically via Spring injection)
 * DatasetManager datasetManager = new DatasetManager(
 *     imageUtils, trainingDataWriter, bufferedImageOps, actionVectorEncoder
 * );
 * 
 * // During automation execution
 * ActionResult result = performAction(clickAction);
 * boolean saved = datasetManager.addSetOfData(result);
 * 
 * // After collecting data
 * trainingDataWriter.saveAllDataToFile();
 * }</pre>
 * 
 * <h2>Data Flow</h2>
 * <ol>
 *   <li>Action execution produces an {@link io.github.jspinak.brobot.action.ActionResult}</li>
 *   <li>DatasetManager validates the action type</li>
 *   <li>Before screenshot is extracted from the result</li>
 *   <li>Action is encoded into a numerical vector</li>
 *   <li>After screenshot is captured from current screen</li>
 *   <li>Training example is persisted to disk</li>
 * </ol>
 * 
 * <h2>Extension Points</h2>
 * <p>The framework can be extended by:
 * <ul>
 *   <li>Implementing custom {@link io.github.jspinak.brobot.tools.ml.dataset.encoding.ActionVectorTranslator}
 *       for alternative encoding strategies</li>
 *   <li>Adding new action types to the allowed set</li>
 *   <li>Customizing screenshot capture logic</li>
 *   <li>Implementing alternative storage formats</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.ml.dataset;