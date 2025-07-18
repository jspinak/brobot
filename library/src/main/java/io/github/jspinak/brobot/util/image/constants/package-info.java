/**
 * Provides predefined constants and enumerations for image processing.
 * 
 * <p>This package contains constant definitions, particularly color constants
 * in BGR format used throughout the image processing utilities. These constants
 * ensure consistency in color representation and provide convenient access to
 * commonly used colors in computer vision operations.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.constants.BgrColorConstants} - 
 *       Predefined BGR color values for OpenCV operations</li>
 * </ul>
 * 
 * <h2>BGR Color Format</h2>
 * <p>OpenCV uses BGR (Blue-Green-Red) color ordering by default, which differs
 * from the more common RGB format. This package provides constants in the correct
 * BGR format to avoid color channel confusion.
 * 
 * <h2>Available Colors</h2>
 * <p>BgrColorConstants provides BGR values for:
 * <ul>
 *   <li><strong>Primary Colors</strong>: Blue, Green, Red</li>
 *   <li><strong>Secondary Colors</strong>: Cyan, Magenta, Yellow</li>
 *   <li><strong>Neutral Colors</strong>: Black, White, Gray variations</li>
 *   <li><strong>Common Colors</strong>: Orange, Purple, Pink, Brown</li>
 *   <li><strong>Special Values</strong>: Transparent, Default markers</li>
 * </ul>
 * 
 * <h2>Color Value Structure</h2>
 * <p>Each color is defined as a Scalar with three or four components:
 * <ul>
 *   <li><strong>Blue Channel</strong>: First component (0-255)</li>
 *   <li><strong>Green Channel</strong>: Second component (0-255)</li>
 *   <li><strong>Red Channel</strong>: Third component (0-255)</li>
 *   <li><strong>Alpha Channel</strong>: Optional fourth component (0-255)</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Draw a red rectangle on an image
 * Imgproc.rectangle(
 *     image,
 *     new Point(10, 10),
 *     new Point(100, 100),
 *     BgrColorConstants.RED,
 *     2  // thickness
 * );
 * 
 * // Fill region with blue color
 * Mat mask = new Mat(image.size(), CvType.CV_8UC3, BgrColorConstants.BLUE);
 * 
 * // Create color range for detection
 * Scalar lowerBound = BgrColorConstants.GREEN.mul(0.8);
 * Scalar upperBound = BgrColorConstants.GREEN.mul(1.2);
 * Core.inRange(image, lowerBound, upperBound, mask);
 * 
 * // Draw with custom opacity
 * Scalar semiTransparentRed = new Scalar(0, 0, 255, 128); // 50% opacity
 * }</pre>
 * 
 * <h2>Color Space Considerations</h2>
 * <ul>
 *   <li>BGR is OpenCV's default; RGB requires conversion</li>
 *   <li>HSV color space may be better for color detection</li>
 *   <li>Grayscale operations ignore color channels</li>
 *   <li>Alpha channel support depends on image type</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * <ul>
 *   <li><strong>Debug Visualization</strong>: Highlight regions with distinct colors</li>
 *   <li><strong>Annotation</strong>: Draw bounding boxes and markers</li>
 *   <li><strong>Masking</strong>: Create color-based masks</li>
 *   <li><strong>Thresholding</strong>: Define color ranges for detection</li>
 *   <li><strong>UI Elements</strong>: Consistent colors across visualizations</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Use constants instead of hardcoded BGR values</li>
 *   <li>Document any custom color definitions</li>
 *   <li>Consider color blindness in visualization choices</li>
 *   <li>Test colors on different backgrounds</li>
 *   <li>Validate color space before using constants</li>
 * </ul>
 * 
 * <h2>Extension Guidelines</h2>
 * <p>When adding new constants:
 * <ul>
 *   <li>Follow BGR ordering convention</li>
 *   <li>Provide clear, descriptive names</li>
 *   <li>Include Javadoc with RGB equivalent</li>
 *   <li>Consider adding color variations (light/dark)</li>
 *   <li>Test visual appearance in target context</li>
 * </ul>
 * 
 * @see org.bytedeco.opencv.opencv_core.Scalar
 * @see org.bytedeco.opencv.global.opencv_imgproc
 * @see io.github.jspinak.brobot.util.image.visualization
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.constants;