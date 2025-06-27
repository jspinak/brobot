/**
 * Provides image recognition and template matching capabilities.
 * 
 * <p>This package contains utilities for image recognition tasks, including
 * template matching, image loading, and pattern detection. These tools form
 * the visual recognition backbone of Brobot's automation capabilities, enabling
 * reliable detection of GUI elements across different applications and states.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.recognition.ImageLoader} - 
 *       Versatile image loading from files, resources, and URLs</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.recognition.MatImageRecognition} - 
 *       OpenCV-based template matching implementation</li>
 * </ul>
 * 
 * <h2>Image Loading Capabilities</h2>
 * <p>ImageLoader provides flexible image acquisition:
 * <ul>
 *   <li><strong>File System</strong>: Load images from absolute or relative paths</li>
 *   <li><strong>Classpath Resources</strong>: Access bundled images in JAR files</li>
 *   <li><strong>URL Loading</strong>: Fetch images from web resources</li>
 *   <li><strong>Format Support</strong>: PNG, JPEG, BMP, and other common formats</li>
 *   <li><strong>Error Handling</strong>: Graceful fallbacks for missing images</li>
 * </ul>
 * 
 * <h2>Template Matching Features</h2>
 * <p>MatImageRecognition implements sophisticated matching:
 * <ul>
 *   <li><strong>Multiple Algorithms</strong>: Support for various OpenCV matching methods
 *       (TM_CCOEFF_NORMED, TM_SQDIFF, etc.)</li>
 *   <li><strong>Multi-Scale Matching</strong>: Find patterns at different scales</li>
 *   <li><strong>Rotation Tolerance</strong>: Optional rotation-invariant matching</li>
 *   <li><strong>Color Space Flexibility</strong>: Match in RGB, grayscale, or HSV</li>
 *   <li><strong>Performance Optimization</strong>: GPU acceleration when available</li>
 * </ul>
 * 
 * <h2>Recognition Process</h2>
 * <ol>
 *   <li><strong>Image Preparation</strong>: Load and preprocess target and template images</li>
 *   <li><strong>Template Matching</strong>: Apply correlation algorithms</li>
 *   <li><strong>Peak Detection</strong>: Find local maxima in correlation map</li>
 *   <li><strong>Threshold Filtering</strong>: Remove low-confidence matches</li>
 *   <li><strong>Result Refinement</strong>: Eliminate overlapping detections</li>
 * </ol>
 * 
 * <h2>Matching Strategies</h2>
 * <ul>
 *   <li><strong>Exact Matching</strong>: Pixel-perfect pattern detection</li>
 *   <li><strong>Fuzzy Matching</strong>: Tolerance for minor variations</li>
 *   <li><strong>Feature Matching</strong>: SIFT/SURF for complex patterns</li>
 *   <li><strong>Color Matching</strong>: HSV-based color similarity</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Load images for matching
 * ImageLoader loader = context.getBean(ImageLoader.class);
 * Mat screenshot = loader.loadMat("screenshot.png");
 * Mat template = loader.loadMatFromResource("/templates/button.png");
 * 
 * // Perform template matching
 * MatImageRecognition recognition = context.getBean(MatImageRecognition.class);
 * MatchResult result = recognition.findTemplate(screenshot, template, 0.9);
 * 
 * // Find multiple instances
 * List<MatchResult> allMatches = recognition.findAllTemplates(
 *     screenshot, template, 0.8, 10); // max 10 matches
 * 
 * // Multi-scale matching
 * MatchResult bestMatch = recognition.findTemplateMultiScale(
 *     screenshot, template, 0.8, 1.2, 0.9); // 80-120% scale
 * }</pre>
 * 
 * <h2>Performance Optimization</h2>
 * <ul>
 *   <li><strong>Template Caching</strong>: Reuse loaded templates across searches</li>
 *   <li><strong>Region of Interest</strong>: Limit search area when possible</li>
 *   <li><strong>Pyramid Search</strong>: Start with downscaled images</li>
 *   <li><strong>Early Termination</strong>: Stop after finding sufficient matches</li>
 * </ul>
 * 
 * <h2>Accuracy Considerations</h2>
 * <ul>
 *   <li>Higher thresholds reduce false positives but may miss valid matches</li>
 *   <li>Lighting variations can affect matching accuracy</li>
 *   <li>Anti-aliasing and scaling artifacts impact exact matching</li>
 *   <li>Dynamic content requires adaptive thresholds</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * <p>Recognition operations handle common issues:
 * <ul>
 *   <li>Invalid image formats return empty results</li>
 *   <li>Missing files are logged with descriptive errors</li>
 *   <li>Memory constraints trigger automatic downscaling</li>
 *   <li>Template larger than target detected and reported</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.find
 * @see io.github.jspinak.brobot.util.image.core
 * @see org.bytedeco.opencv.global.opencv_imgproc
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.recognition;