/**
 * Provides input/output operations for machine learning training data persistence.
 *
 * <p>This package contains components responsible for reading and writing training data to
 * persistent storage. The I/O layer handles the serialization and deserialization of complex
 * training examples, including action vectors, descriptive text, and screenshot images.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.io.TrainingExampleWriter} - Accumulates
 *       and persists training data to disk
 *   <li>{@link io.github.jspinak.brobot.tools.ml.dataset.io.TrainingExampleReader} - Reads
 *       serialized training data from persistent storage
 * </ul>
 *
 * <h2>Storage Format</h2>
 *
 * <p>Training data is stored in a custom binary format optimized for the specific needs of GUI
 * automation training:
 *
 * <ol>
 *   <li>File header: Integer count of training examples
 *   <li>For each training example:
 *       <ul>
 *         <li>Action vector (short array)
 *         <li>Description text (String)
 *         <li>Screenshot count (Integer)
 *         <li>For each screenshot:
 *             <ul>
 *               <li>Image size (Integer)
 *               <li>PNG-encoded image data (byte array)
 *             </ul>
 *       </ul>
 * </ol>
 *
 * <h2>Key Features</h2>
 *
 * <ul>
 *   <li><strong>Configurable File Paths</strong>: Both reader and writer support custom filenames
 *       (default: "trainingdata.dat")
 *   <li><strong>Efficient Image Handling</strong>: Screenshots are serialized as PNG byte arrays to
 *       reduce file size
 *   <li><strong>Batch Operations</strong>: Data is accumulated in memory and written in a single
 *       operation for efficiency
 *   <li><strong>Error Recovery</strong>: Comprehensive error handling with meaningful error
 *       messages
 *   <li><strong>Resource Safety</strong>: Uses try-with-resources for proper cleanup
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Writing training data
 * TrainingExampleWriter writer = new TrainingExampleWriter("mydata.dat");
 * writer.addData(actionVector, "Click submit button", screenshots);
 * writer.saveAllDataToFile();
 *
 * // Reading training data
 * TrainingExampleReader reader = new TrainingExampleReader("mydata.dat");
 * reader.getDataFromFile();
 * List<TrainingExample> examples = reader.getTrainingData();
 * }</pre>
 *
 * <h2>Design Decisions</h2>
 *
 * <ul>
 *   <li><strong>Custom Serialization</strong>: Java's default serialization is avoided for
 *       BufferedImage to control the format and size
 *   <li><strong>PNG Format</strong>: Screenshots are stored as PNG for lossless compression and
 *       wide compatibility
 *   <li><strong>Memory Accumulation</strong>: Training data is collected in memory before writing
 *       to minimize I/O operations
 *   <li><strong>Exception Propagation</strong>: I/O errors are wrapped in RuntimeException to
 *       ensure they're not silently ignored
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Large datasets may require significant memory during accumulation
 *   <li>PNG encoding/decoding can be CPU intensive for many screenshots
 *   <li>Consider implementing streaming for very large datasets
 *   <li>File operations are not thread-safe; synchronization may be needed
 * </ul>
 *
 * <h2>Future Enhancements</h2>
 *
 * <p>Potential improvements for the I/O layer:
 *
 * <ul>
 *   <li>Support for multiple file formats (JSON, HDF5, TFRecord)
 *   <li>Compression options for the entire file
 *   <li>Streaming API for processing large datasets
 *   <li>Versioning support for format evolution
 *   <li>Concurrent read/write capabilities
 * </ul>
 *
 * @see io.github.jspinak.brobot.tools.ml.dataset.model.TrainingExample
 * @see java.io.Serializable
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.ml.dataset.io;
