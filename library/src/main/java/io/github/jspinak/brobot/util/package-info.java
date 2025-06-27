/**
 * Core utility classes providing common functionality across the Brobot framework.
 * <p>
 * This package serves as the central hub for utility classes that support various
 * aspects of GUI automation, image processing, and data manipulation. The utilities
 * are organized into specialized subpackages for better modularity and maintenance.
 * 
 * <h2>Package Structure</h2>
 * <ul>
 *   <li><b>common</b> - Generic data structures and utilities used throughout the framework</li>
 *   <li><b>file</b> - File handling, naming conventions, and I/O operations</li>
 *   <li><b>geometry</b> - Geometric calculations, clustering, and spatial analysis</li>
 *   <li><b>image</b> - Comprehensive image processing, recognition, and visualization</li>
 *   <li><b>location</b> - Location manipulation and coordinate system utilities</li>
 *   <li><b>region</b> - Region-based operations and search area management</li>
 *   <li><b>string</b> - String manipulation, similarity analysis, and text processing</li>
 * </ul>
 * 
 * <h2>Design Philosophy</h2>
 * <ul>
 *   <li><b>Stateless Design</b>: Most utilities are implemented as static methods for thread safety</li>
 *   <li><b>Spring Integration</b>: Component-based utilities leverage Spring's dependency injection</li>
 *   <li><b>Null Safety</b>: Methods handle null inputs gracefully with clear documentation</li>
 *   <li><b>Performance Focus</b>: Optimized algorithms for real-time automation needs</li>
 * </ul>
 * 
 * <h2>Key Capabilities</h2>
 * 
 * <h3>Image Processing</h3>
 * The image subpackage provides extensive capabilities for:
 * <ul>
 *   <li>Screenshot capture and recording</li>
 *   <li>Image format conversion between BufferedImage and Mat</li>
 *   <li>Computer vision operations using OpenCV/JavaCV</li>
 *   <li>Visualization and debugging tools</li>
 * </ul>
 * 
 * <h3>Geometric Operations</h3>
 * <ul>
 *   <li>Distance calculations between points and regions</li>
 *   <li>Movement vector generation for mouse automation</li>
 *   <li>Clustering algorithms for grouping spatial data</li>
 *   <li>Sector-based directional analysis</li>
 * </ul>
 * 
 * <h3>File Management</h3>
 * <ul>
 *   <li>Intelligent filename generation and manipulation</li>
 *   <li>Recording session management</li>
 *   <li>Safe file writing with error handling</li>
 * </ul>
 * 
 * <h3>String Operations</h3>
 * <ul>
 *   <li>Fuzzy string matching using Levenshtein distance</li>
 *   <li>Text selection from stochastic variations</li>
 *   <li>Base64 encoding/decoding for data serialization</li>
 *   <li>Regular expression utilities</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Static Utility Pattern</h3>
 * <pre>{@code
 * // Most utilities provide static methods
 * double similarity = StringSimilarity.similarity("hello", "hallo");
 * Mat screenshot = ScreenshotCapture.capture(region);
 * }</pre>
 * 
 * <h3>Spring Component Pattern</h3>
 * <pre>{@code
 * @Autowired
 * private TextSelector textSelector;
 * 
 * String selected = textSelector.getString(Method.MOST_SIMILAR, text);
 * }</pre>
 * 
 * <h2>Integration with Brobot Core</h2>
 * These utilities support the framework's core functionality:
 * <ul>
 *   <li>State pattern matching through image utilities</li>
 *   <li>Action execution through location and region utilities</li>
 *   <li>Mock operation recording through file utilities</li>
 *   <li>OCR result processing through string utilities</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Image operations are optimized for repeated execution</li>
 *   <li>Geometric calculations use efficient algorithms</li>
 *   <li>String operations cache results where appropriate</li>
 *   <li>File operations use buffered I/O</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <ul>
 *   <li>Static utility methods are inherently thread-safe</li>
 *   <li>Spring components are singleton-scoped by default</li>
 *   <li>Mutable operations clearly documented</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model
 * @see io.github.jspinak.brobot.actions
 */
package io.github.jspinak.brobot.util;