/**
 * File handling utilities for saving and managing automation artifacts.
 * <p>
 * This package provides comprehensive file management capabilities for the Brobot
 * framework, focusing on saving screenshots, XML documents, and other artifacts
 * generated during automation runs. It includes utilities for filename manipulation,
 * conflict prevention, and organized storage of automation data.
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>SaveToFile Interface</h3>
 * Defines the contract for file saving operations:
 * <ul>
 *   <li>Folder creation with automatic parent directory handling</li>
 *   <li>Image saving with configurable naming strategies</li>
 *   <li>XML document persistence for structured data</li>
 *   <li>Extensible design for different storage backends</li>
 * </ul>
 * 
 * <h3>RecorderSaveToFile</h3>
 * Production implementation of SaveToFile:
 * <ul>
 *   <li>Timestamp-based image naming for chronological ordering</li>
 *   <li>Centralized recording folder management</li>
 *   <li>Integration with FrameworkSettings for configuration</li>
 *   <li>Automatic folder structure creation</li>
 * </ul>
 * 
 * <h3>FilenameAllocator</h3>
 * Thread-safe filename reservation system:
 * <ul>
 *   <li>Prevents file overwrites in concurrent scenarios</li>
 *   <li>Automatic index generation for duplicate names</li>
 *   <li>In-memory tracking of reserved filenames</li>
 *   <li>Coordination with filesystem checks</li>
 * </ul>
 * 
 * <h3>FilenameUtils</h3>
 * Low-level filename manipulation utilities:
 * <ul>
 *   <li>Extension handling (defaulting to PNG)</li>
 *   <li>Filename extraction and transformation</li>
 *   <li>Simple, stateless operations</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Recording Automation Sessions</h3>
 * <pre>{@code
 * @Autowired
 * private SaveToFile saveToFile;
 * 
 * // Save screenshot with timestamp
 * String filename = saveToFile.saveImage(bufferedImage, "test-action");
 * 
 * // Save XML report
 * saveToFile.saveXML(document, "results.xml");
 * }</pre>
 * 
 * <h3>Preventing Filename Conflicts</h3>
 * <pre>{@code
 * @Autowired
 * private FilenameAllocator filenameAllocator;
 * 
 * // Reserve unique filename
 * String uniqueName = filenameAllocator.reserve(baseFolder, "screenshot");
 * // Returns: screenshot.png, screenshot_1.png, etc.
 * }</pre>
 * 
 * <h3>Filename Manipulation</h3>
 * <pre>{@code
 * // Add extension if needed
 * String pngFile = FilenameUtils.addPngExtension("screenshot");
 * 
 * // Extract name without extension
 * String baseName = FilenameUtils.getFilenameWithoutExtension("image.png");
 * }</pre>
 * 
 * <h2>File Organization</h2>
 * The package supports organized file storage:
 * <ul>
 *   <li>Timestamp-based folder naming: recordings/{timestamp}/</li>
 *   <li>Chronological image naming: {base}-{milliseconds}.png</li>
 *   <li>Automatic directory creation</li>
 *   <li>Configurable base paths via FrameworkSettings</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Thread Safety</b>: FilenameAllocator uses synchronization for concurrent access</li>
 *   <li><b>Extensibility</b>: SaveToFile interface allows custom implementations</li>
 *   <li><b>Convention over Configuration</b>: PNG as default format, timestamp naming</li>
 *   <li><b>Fail-Safe</b>: Automatic directory creation, conflict prevention</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * This package integrates with:
 * <ul>
 *   <li>Recording subsystem for session capture</li>
 *   <li>Illustration generation for visual debugging</li>
 *   <li>Report generation for test results</li>
 *   <li>Mock operation logging</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>In-memory filename tracking minimizes disk I/O</li>
 *   <li>Buffered writing for large files</li>
 *   <li>Lazy directory creation</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see io.github.jspinak.brobot.illustratedHistory
 * @see io.github.jspinak.brobot.recording
 */
package io.github.jspinak.brobot.util.file;