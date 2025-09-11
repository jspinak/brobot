/**
 * Histogram-based image analysis and comparison.
 *
 * <p>This package provides comprehensive histogram analysis capabilities for robust image matching
 * and comparison. Histogram-based techniques are particularly effective for matching images that
 * may have slight variations in appearance while maintaining the same overall color distribution.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.histogram.HistogramExtractor} - Extracts color
 *       histograms from images
 *   <li>{@link io.github.jspinak.brobot.analysis.histogram.SingleRegionHistogramExtractor} -
 *       Specialized extractor for single regions
 *   <li>{@link io.github.jspinak.brobot.analysis.histogram.HistogramComparator} - Compares
 *       histograms using various metrics
 *   <li>{@link io.github.jspinak.brobot.analysis.histogram.HistogramRegion} - Histogram data for a
 *       specific image region
 *   <li>{@link io.github.jspinak.brobot.analysis.histogram.HistogramRegions} - Collection of
 *       histogram regions for multi-region analysis
 * </ul>
 *
 * <h2>Histogram Analysis Process</h2>
 *
 * <ol>
 *   <li><b>Extraction</b> - Compute histograms from image regions
 *   <li><b>Normalization</b> - Normalize for size-independent comparison
 *   <li><b>Comparison</b> - Calculate similarity using various metrics
 *   <li><b>Thresholding</b> - Determine matches based on similarity scores
 * </ol>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Histogram Extraction</h3>
 *
 * <pre>{@code
 * // Extract histogram from entire image
 * HistogramExtractor extractor = new HistogramExtractor();
 * Mat histogram = extractor.extract(image);
 *
 * // Extract from specific region
 * Rect roi = new Rect(10, 10, 100, 100);
 * Mat regionHist = extractor.extract(image, roi);
 * }</pre>
 *
 * <h3>Multi-Region Analysis</h3>
 *
 * <pre>{@code
 * // Analyze multiple regions
 * HistogramRegions regions = new HistogramRegions();
 *
 * // Add regions of interest
 * regions.addRegion("button", buttonRect, buttonHistogram);
 * regions.addRegion("header", headerRect, headerHistogram);
 * regions.addRegion("content", contentRect, contentHistogram);
 *
 * // Compare against target
 * double similarity = regions.compareWith(targetRegions);
 * }</pre>
 *
 * <h3>Histogram Comparison</h3>
 *
 * <pre>{@code
 * // Compare histograms using different methods
 * HistogramComparator comparator = new HistogramComparator();
 *
 * // Correlation (1 = perfect match, -1 = inverse)
 * double correlation = comparator.compare(
 *     hist1, hist2,
 *     HistogramComparator.Method.CORRELATION
 * );
 *
 * // Chi-square (0 = perfect match)
 * double chiSquare = comparator.compare(
 *     hist1, hist2,
 *     HistogramComparator.Method.CHI_SQUARE
 * );
 *
 * // Bhattacharyya distance (0 = perfect match)
 * double bhattacharyya = comparator.compare(
 *     hist1, hist2,
 *     HistogramComparator.Method.BHATTACHARYYA
 * );
 * }</pre>
 *
 * <h3>Region-Based Matching</h3>
 *
 * <pre>{@code
 * // Match using regional histograms
 * SingleRegionHistogramExtractor regionExtractor =
 *     new SingleRegionHistogramExtractor();
 *
 * HistogramRegion targetRegion = regionExtractor.extract(
 *     targetImage,
 *     targetROI
 * );
 *
 * // Search in scene
 * List<Match> matches = regionExtractor.findMatches(
 *     scene,
 *     targetRegion,
 *     threshold
 * );
 * }</pre>
 *
 * <h2>Histogram Types</h2>
 *
 * <ul>
 *   <li><b>Color Histograms</b> - Distribution of color values
 *   <li><b>Grayscale Histograms</b> - Intensity distribution
 *   <li><b>Multi-dimensional</b> - Combined color channels
 *   <li><b>Local Histograms</b> - Region-specific distributions
 * </ul>
 *
 * <h2>Comparison Metrics</h2>
 *
 * <ul>
 *   <li><b>Correlation</b> - Linear correlation coefficient
 *   <li><b>Chi-Square</b> - Statistical difference measure
 *   <li><b>Intersection</b> - Overlap between histograms
 *   <li><b>Bhattacharyya</b> - Statistical similarity measure
 * </ul>
 *
 * <h2>Advantages</h2>
 *
 * <ul>
 *   <li>Rotation invariant (color distribution unchanged)
 *   <li>Tolerant to small position changes
 *   <li>Robust against noise and compression artifacts
 *   <li>Computationally efficient
 *   <li>Works well with color variations
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Normalize histograms for size-independent comparison
 *   <li>Use appropriate bin counts (32-256 typical)
 *   <li>Consider color space (HSV often better than BGR)
 *   <li>Combine with other methods for robustness
 *   <li>Use regional histograms for structured content
 * </ol>
 *
 * <h2>Performance Tips</h2>
 *
 * <ul>
 *   <li>Pre-compute histograms for known targets
 *   <li>Use integral histograms for sliding window search
 *   <li>Reduce bin count for faster comparison
 *   <li>Cache normalized histograms
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.analysis
 * @see io.github.jspinak.brobot.imageUtils
 */
package io.github.jspinak.brobot.analysis.histogram;
