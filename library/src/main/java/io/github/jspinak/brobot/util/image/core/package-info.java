/**
 * Provides fundamental image conversion and manipulation operations.
 * 
 * <p>This package contains the core image processing utilities that form the
 * foundation of Brobot's visual capabilities. These utilities handle essential
 * conversions between different image formats, basic image operations, and
 * provide the building blocks for more complex image processing tasks.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.core.ImageConverter} - 
 *       Handles conversions between Image, byte[], and various formats</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.core.BufferedImageUtilities} - 
 *       Comprehensive BufferedImage operations including conversions and manipulations</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.core.MatrixUtilities} - 
 *       OpenCV Mat operations for debugging, analysis, and conversion</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities} - 
 *       Specialized utilities for 3-channel color image operations</li>
 * </ul>
 * 
 * <h2>Image Format Support</h2>
 * <p>The core utilities support conversions between:
 * <ul>
 *   <li><strong>BufferedImage</strong> - Java's standard image representation</li>
 *   <li><strong>Mat</strong> - OpenCV's matrix representation</li>
 *   <li><strong>byte[]</strong> - Raw image data for serialization</li>
 *   <li><strong>Image</strong> - AWT Image for compatibility</li>
 *   <li><strong>IplImage</strong> - Legacy OpenCV format support</li>
 * </ul>
 * 
 * <h2>Key Operations</h2>
 * <ul>
 *   <li><strong>Format Conversion</strong>: Seamless conversion between all supported
 *       formats with proper color space handling</li>
 *   <li><strong>Color Space Management</strong>: Conversion between BGR, RGB, HSV,
 *       and grayscale color spaces</li>
 *   <li><strong>Image Manipulation</strong>: Cropping, resizing, rotation, and
 *       other geometric transformations</li>
 *   <li><strong>Pixel Operations</strong>: Direct pixel access and manipulation
 *       for custom algorithms</li>
 *   <li><strong>Memory Management</strong>: Efficient handling of image data to
 *       prevent memory leaks</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>Important considerations for concurrent usage:
 * <ul>
 *   <li>BufferedImageUtilities methods are generally thread-safe for read operations</li>
 *   <li>Write operations on shared images require external synchronization</li>
 *   <li>Mat operations should be synchronized when sharing Mat objects between threads</li>
 *   <li>Static utility methods are stateless and thread-safe</li>
 * </ul>
 * 
 * <h2>Performance Guidelines</h2>
 * <ul>
 *   <li>Prefer Mat operations for heavy processing (leverages native code)</li>
 *   <li>Minimize format conversions in performance-critical paths</li>
 *   <li>Reuse Mat and BufferedImage objects when possible</li>
 *   <li>Use appropriate color spaces for specific operations</li>
 * </ul>
 * 
 * <h2>Common Conversion Patterns</h2>
 * <pre>{@code
 * // BufferedImage to Mat
 * BufferedImageUtilities bufUtils = context.getBean(BufferedImageUtilities.class);
 * Mat mat = bufUtils.convertToMat(bufferedImage);
 * 
 * // Mat to BufferedImage
 * BufferedImage image = bufUtils.convertFromMat(mat);
 * 
 * // Image serialization
 * ImageConverter converter = context.getBean(ImageConverter.class);
 * byte[] imageData = converter.imageToByteArray(image, "png");
 * Image restored = converter.byteArrayToImage(imageData);
 * 
 * // Color space conversion
 * MatrixUtilities matUtils = context.getBean(MatrixUtilities.class);
 * Mat hsv = matUtils.convertColorSpace(mat, ColorSpace.BGR2HSV);
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Always release Mat objects when done to free native memory</li>
 *   <li>Check for null returns from conversion operations</li>
 *   <li>Validate image dimensions before operations</li>
 *   <li>Use try-with-resources for automatic resource management where applicable</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * <p>Core utilities handle errors gracefully:
 * <ul>
 *   <li>Invalid inputs typically return null rather than throwing exceptions</li>
 *   <li>Detailed logging helps diagnose conversion failures</li>
 *   <li>Defensive copying prevents external modification of internal state</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.util.image.capture
 * @see io.github.jspinak.brobot.util.image.recognition
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.core;