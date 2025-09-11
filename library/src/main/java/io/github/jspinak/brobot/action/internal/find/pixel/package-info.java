/**
 * Pixel-level color analysis and matching for visual pattern recognition.
 *
 * <p>This package provides sophisticated color-based matching capabilities that complement
 * traditional template matching. It implements pixel-level analysis techniques for finding GUI
 * elements based on color characteristics rather than exact image matches.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link
 *       io.github.jspinak.brobot.action.internal.find.pixel.ColorAnalysisOrchestrator}</b> -
 *       Central coordinator for pixel-level color analysis, managing the complete workflow from
 *       color extraction to similarity scoring
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.pixel.PixelRegionExtractor}</b> -
 *       Identifies and extracts contiguous regions from pixel classification results, converting
 *       them into actionable Match objects
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.pixel.PixelScoreCalculator}</b> -
 *       Transforms raw color distance measurements into normalized similarity scores using
 *       sophisticated scoring algorithms
 * </ul>
 *
 * <h2>Color Analysis Process</h2>
 *
 * <ol>
 *   <li><b>Color Profile Extraction</b> - Extract representative colors from target images using
 *       k-means clustering or mean color calculation
 *   <li><b>Scene Analysis</b> - Analyze scene pixels in both BGR and HSV color spaces for robust
 *       matching across lighting conditions
 *   <li><b>Distance Calculation</b> - Compute color distances between scene pixels and target color
 *       profiles
 *   <li><b>Score Generation</b> - Convert distances to similarity scores using hyperbolic tangent
 *       functions for smooth gradients
 *   <li><b>Region Identification</b> - Find contiguous regions of similar pixels that meet
 *       threshold criteria
 *   <li><b>Match Creation</b> - Convert identified regions into Match objects with location and
 *       confidence data
 * </ol>
 *
 * <h2>Key Features</h2>
 *
 * <ul>
 *   <li><b>Multi-Color Space Support</b> - Analyzes in both BGR and HSV for lighting invariance
 *   <li><b>Flexible Thresholding</b> - Configurable similarity thresholds and region size
 *       constraints
 *   <li><b>Scene-Wide Classification</b> - Can classify entire scenes for comprehensive analysis
 *   <li><b>Performance Optimization</b> - Efficient algorithms for real-time processing
 * </ul>
 *
 * <h2>Use Cases</h2>
 *
 * <p>Pixel analysis is particularly effective for:
 *
 * <ul>
 *   <li>Finding UI elements that change appearance but maintain color schemes
 *   <li>Detecting regions with specific color characteristics
 *   <li>Identifying elements in varying lighting conditions
 *   <li>Segmenting scenes based on color properties
 * </ul>
 *
 * <h2>Integration with Find System</h2>
 *
 * <p>These components integrate seamlessly with the broader find system:
 *
 * <ul>
 *   <li>Called by {@link io.github.jspinak.brobot.action.basic.find.FindColor} for color-based
 *       searches
 *   <li>Works with scene analysis collections for comprehensive matching
 *   <li>Produces standard Match objects compatible with all post-processing
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.basic.find.FindColor
 * @see io.github.jspinak.brobot.model.analysis.PixelAnalysis
 * @see io.github.jspinak.brobot.model.coloranalysis.ColorProfile
 */
package io.github.jspinak.brobot.action.internal.find.pixel;
