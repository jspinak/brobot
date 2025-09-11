/**
 * Image comparison and analysis algorithms.
 *
 * <p>This package provides utilities for comparing images, extracting contours, and analyzing
 * visual differences. These tools support advanced matching operations by enabling detailed
 * comparison of visual elements beyond simple template matching.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.compare.ImageComparer} - Comprehensive image
 *       comparison with multiple metrics
 *   <li>{@link io.github.jspinak.brobot.analysis.compare.ContourExtractor} - Extracts and analyzes
 *       object contours from images
 *   <li>{@link io.github.jspinak.brobot.analysis.compare.SizeComparator} - Compares dimensions and
 *       sizes of visual elements
 * </ul>
 *
 * <h2>Comparison Techniques</h2>
 *
 * <h3>Pixel-Level Comparison</h3>
 *
 * <pre>{@code
 * // Compare two images pixel by pixel
 * ImageComparer comparer = new ImageComparer();
 * ComparisonResult result = comparer.compare(image1, image2);
 *
 * double similarity = result.getSimilarityScore();
 * Mat diffMap = result.getDifferenceMap();
 * List<Rect> changedRegions = result.getChangedRegions();
 * }</pre>
 *
 * <h3>Contour-Based Analysis</h3>
 *
 * <pre>{@code
 * // Extract contours from classification results
 * ContourExtractor extractor = new ContourExtractor();
 * List<MatOfPoint> contours = extractor.extract(binaryImage);
 *
 * // Filter by size and shape
 * List<MatOfPoint> filtered = extractor.filterContours(
 *     contours,
 *     minArea,
 *     maxArea
 * );
 *
 * // Convert to bounding boxes
 * List<Rect> boundingBoxes = extractor.toBoundingBoxes(filtered);
 * }</pre>
 *
 * <h3>Size-Based Comparison</h3>
 *
 * <pre>{@code
 * // Compare element sizes
 * SizeComparator sizeComp = new SizeComparator();
 *
 * boolean sameSize = sizeComp.areSimilar(rect1, rect2, tolerance);
 * double sizeRatio = sizeComp.getSizeRatio(rect1, rect2);
 * boolean aspectMatch = sizeComp.aspectRatiosMatch(rect1, rect2);
 * }</pre>
 *
 * <h2>Comparison Metrics</h2>
 *
 * <ul>
 *   <li><b>Structural Similarity (SSIM)</b> - Perceptual similarity metric
 *   <li><b>Mean Squared Error (MSE)</b> - Pixel-wise difference measurement
 *   <li><b>Histogram Comparison</b> - Statistical color distribution analysis
 *   <li><b>Edge Comparison</b> - Structural element matching
 *   <li><b>Contour Matching</b> - Shape-based similarity
 * </ul>
 *
 * <h2>Use Cases</h2>
 *
 * <h3>Change Detection</h3>
 *
 * <pre>{@code
 * // Detect UI changes between states
 * ImageComparer comparer = new ImageComparer();
 * ComparisonResult changes = comparer.detectChanges(
 *     previousScreen,
 *     currentScreen
 * );
 *
 * if (changes.hasSignificantChanges()) {
 *     processUIUpdate(changes.getChangedRegions());
 * }
 * }</pre>
 *
 * <h3>Element Extraction</h3>
 *
 * <pre>{@code
 * // Extract UI elements from classification
 * ContourExtractor extractor = new ContourExtractor();
 * Mat classified = classifier.classify(scene);
 *
 * List<Match> elements = extractor.extractElements(
 *     classified,
 *     targetClassIndex
 * );
 * }</pre>
 *
 * <h3>Validation</h3>
 *
 * <pre>{@code
 * // Validate match results
 * SizeComparator validator = new SizeComparator();
 *
 * boolean validSize = validator.isWithinExpectedSize(
 *     foundElement,
 *     expectedSize,
 *     tolerance
 * );
 * }</pre>
 *
 * <h2>Algorithm Options</h2>
 *
 * <h3>Comparison Sensitivity</h3>
 *
 * <ul>
 *   <li>High: Detect pixel-level changes
 *   <li>Medium: Ignore minor variations
 *   <li>Low: Major structural changes only
 * </ul>
 *
 * <h3>Contour Extraction</h3>
 *
 * <ul>
 *   <li>External only: Outer boundaries
 *   <li>Hierarchical: Nested contours
 *   <li>Simple: Compressed contours
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Full image comparison is computationally expensive
 *   <li>Consider region-of-interest comparison for speed
 *   <li>Contour extraction benefits from binary input
 *   <li>Cache comparison results when possible
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.analysis
 * @see io.github.jspinak.brobot.imageUtils
 */
package io.github.jspinak.brobot.analysis.compare;
