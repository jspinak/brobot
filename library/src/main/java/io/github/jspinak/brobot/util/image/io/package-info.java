/**
 * Provides image file input/output operations and scene creation utilities.
 *
 * <p>This package contains utilities for reading and writing image files, managing image
 * persistence, and creating Scene objects from stored images. These tools handle the file system
 * interactions necessary for image-based automation workflows.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.io.ImageFileUtilities} - Comprehensive image
 *       file operations with automatic naming
 *   <li>{@link io.github.jspinak.brobot.util.image.io.SceneCreator} - Creates Scene objects from
 *       screenshot files
 * </ul>
 *
 * <h2>File Operations</h2>
 *
 * <p>ImageFileUtilities provides:
 *
 * <ul>
 *   <li><strong>Smart Saving</strong>: Automatic filename generation with timestamps
 *   <li><strong>Format Support</strong>: PNG, JPEG, BMP, and other common formats
 *   <li><strong>Directory Management</strong>: Automatic creation of output directories
 *   <li><strong>Batch Operations</strong>: Save multiple images with sequential naming
 *   <li><strong>Metadata Preservation</strong>: Maintain image properties during save
 * </ul>
 *
 * <h2>Automatic Naming</h2>
 *
 * <p>File naming strategies include:
 *
 * <ul>
 *   <li><strong>Timestamp-based</strong>: Uses current date/time for uniqueness
 *   <li><strong>Sequential</strong>: Incremental numbering for image series
 *   <li><strong>Contextual</strong>: Incorporates action or state information
 *   <li><strong>Custom Prefixes</strong>: User-defined naming patterns
 * </ul>
 *
 * <h2>Scene Creation</h2>
 *
 * <p>SceneCreator enables:
 *
 * <ul>
 *   <li><strong>File to Scene</strong>: Load screenshots as Scene objects
 *   <li><strong>Batch Loading</strong>: Process multiple screenshots efficiently
 *   <li><strong>Format Detection</strong>: Automatic image format identification
 *   <li><strong>Error Recovery</strong>: Graceful handling of corrupted files
 * </ul>
 *
 * <h2>Directory Structure</h2>
 *
 * <p>Recommended organization:
 *
 * <pre>
 * project/
 * ├── screenshots/
 * │   ├── capture/     # Raw captures
 * │   ├── debug/       # Debug visualizations
 * │   └── reference/   # Template images
 * ├── logs/
 * │   └── images/      # Action-related images
 * └── temp/            # Temporary processing
 * </pre>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>{@code
 * // Save image with automatic naming
 * ImageFileUtilities fileUtils = context.getBean(ImageFileUtilities.class);
 * fileUtils.saveImage(bufferedImage, "debug_match");
 * // Creates: debug_match_2024-01-15_14-30-45.png
 *
 * // Save with specific format
 * fileUtils.saveImageAs(bufferedImage, "screenshot", "jpg", 0.8f);
 *
 * // Batch save operation
 * List<BufferedImage> images = getDebugImages();
 * fileUtils.saveImageBatch(images, "test_sequence");
 * // Creates: test_sequence_001.png, test_sequence_002.png, etc.
 *
 * // Create Scene from file
 * SceneCreator creator = context.getBean(SceneCreator.class);
 * Scene scene = creator.createFromFile("screenshots/app_state.png");
 *
 * // Load and process directory
 * List<Scene> scenes = creator.createFromDirectory("screenshots/capture/");
 * }</pre>
 *
 * <h2>File Management Best Practices</h2>
 *
 * <ul>
 *   <li>Implement retention policies to prevent disk space issues
 *   <li>Use consistent naming conventions across the application
 *   <li>Compress or archive old debug images
 *   <li>Validate file permissions before operations
 *   <li>Clean up temporary files after processing
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Common issues and solutions:
 *
 * <ul>
 *   <li><strong>Disk Full</strong>: Implement space checks before saving
 *   <li><strong>Permission Denied</strong>: Verify write access to directories
 *   <li><strong>Invalid Format</strong>: Fallback to PNG for unsupported formats
 *   <li><strong>Corrupted Files</strong>: Skip and log problematic images
 * </ul>
 *
 * <h2>Performance Tips</h2>
 *
 * <ul>
 *   <li>Use appropriate compression for different image types
 *   <li>Batch operations reduce file system overhead
 *   <li>Async saving for non-critical debug images
 *   <li>Memory-mapped files for large image sets
 * </ul>
 *
 * <h2>Integration Points</h2>
 *
 * <p>File utilities integrate with:
 *
 * <ul>
 *   <li>Logging system for operation tracking
 *   <li>Configuration for default paths and formats
 *   <li>Testing framework for mock file operations
 *   <li>Visualization tools for debug image generation
 * </ul>
 *
 * @see io.github.jspinak.brobot.util.image.core
 * @see io.github.jspinak.brobot.util.image.visualization
 * @see io.github.jspinak.brobot.model.scene.Scene
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.io;
