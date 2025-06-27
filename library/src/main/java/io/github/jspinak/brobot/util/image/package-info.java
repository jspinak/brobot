/**
 * Provides comprehensive image processing and manipulation utilities for GUI automation.
 * 
 * <p>This package forms the foundation of Brobot's visual processing capabilities,
 * offering a complete suite of tools for image capture, conversion, analysis, and
 * visualization. Built on top of OpenCV and JavaCV, it provides both low-level
 * operations and high-level utilities optimized for GUI automation tasks.
 * 
 * <h2>Core Capabilities</h2>
 * <ul>
 *   <li><strong>Image Conversion</strong>: Seamless conversion between different
 *       image formats (BufferedImage, Mat, byte arrays, etc.)</li>
 *   <li><strong>Screen Capture</strong>: High-performance screenshot capture with
 *       multiple strategies and continuous recording</li>
 *   <li><strong>Image Recognition</strong>: Template matching and image analysis
 *       using OpenCV algorithms</li>
 *   <li><strong>Visualization</strong>: Debug visualization tools for understanding
 *       image processing results</li>
 *   <li><strong>File I/O</strong>: Efficient image file handling with automatic
 *       naming and format conversion</li>
 * </ul>
 * 
 * <h2>Package Organization</h2>
 * <p>The utilities are organized into specialized subpackages:
 * <ul>
 *   <li><strong>core</strong> - Fundamental image operations and conversions</li>
 *   <li><strong>capture</strong> - Screen capture and screenshot utilities</li>
 *   <li><strong>recognition</strong> - Image matching and loading operations</li>
 *   <li><strong>visualization</strong> - Debug and analysis visualization tools</li>
 *   <li><strong>io</strong> - File input/output and scene creation</li>
 *   <li><strong>constants</strong> - Predefined color constants and enumerations</li>
 * </ul>
 * 
 * <h2>Key Design Principles</h2>
 * <ul>
 *   <li><strong>Performance</strong>: Optimized for real-time GUI automation with
 *       minimal overhead</li>
 *   <li><strong>Flexibility</strong>: Support for multiple image formats and
 *       conversion strategies</li>
 *   <li><strong>Thread Safety</strong>: Careful attention to concurrent access
 *       patterns in utilities</li>
 *   <li><strong>Memory Efficiency</strong>: Proper resource management for image
 *       data to prevent memory leaks</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * <ul>
 *   <li>Capturing screenshots for pattern matching</li>
 *   <li>Converting between OpenCV Mat and Java BufferedImage</li>
 *   <li>Visualizing template matching results</li>
 *   <li>Saving debug images with automatic naming</li>
 *   <li>Loading and processing image files for automation</li>
 * </ul>
 * 
 * <h2>Integration with Brobot Framework</h2>
 * <p>These utilities integrate seamlessly with:
 * <ul>
 *   <li>Action framework for visual element detection</li>
 *   <li>State management for screenshot-based state verification</li>
 *   <li>Match history for storing and retrieving visual data</li>
 *   <li>Testing framework for mock image operations</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Image operations can be memory-intensive; proper cleanup is essential</li>
 *   <li>Native OpenCV operations are generally faster than Java equivalents</li>
 *   <li>Screenshot capture performance varies by platform and method</li>
 *   <li>Caching frequently used images can improve performance</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Capture a screenshot
 * ScreenshotCapture capture = context.getBean(ScreenshotCapture.class);
 * BufferedImage screenshot = capture.captureScreen();
 * 
 * // Convert to OpenCV Mat
 * ImageConverter converter = context.getBean(ImageConverter.class);
 * Mat mat = converter.bufferedImageToMat(screenshot);
 * 
 * // Perform template matching
 * MatImageRecognition recognition = context.getBean(MatImageRecognition.class);
 * List<Match> matches = recognition.findTemplate(mat, template);
 * 
 * // Visualize results
 * MatrixVisualizer visualizer = context.getBean(MatrixVisualizer.class);
 * Mat visualization = visualizer.drawMatches(mat, matches);
 * 
 * // Save for debugging
 * ImageFileUtilities fileUtils = context.getBean(ImageFileUtilities.class);
 * fileUtils.saveImage(visualization, "debug_matches");
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.model.image
 * @see org.bytedeco.opencv
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image;