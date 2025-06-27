/**
 * Color-based analysis models for robust pattern matching.
 * 
 * <p>This package provides sophisticated color analysis capabilities that enable
 * pattern matching based on statistical color properties rather than exact pixel
 * matching. This approach provides tolerance to lighting variations, display
 * differences, and minor visual changes while maintaining high accuracy.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Color Profiles</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorCluster} - 
 *       Container for multi-color-space profiles</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorSchema} - 
 *       Abstract base for color space representations</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorSchemaBGR} - 
 *       Blue-Green-Red color space implementation</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorSchemaHSV} - 
 *       Hue-Saturation-Value color space implementation</li>
 * </ul>
 * 
 * <h3>Statistical Models</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorInfo} - 
 *       Statistical data for individual color channels</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.ColorStatistics} - 
 *       Aggregated statistics across channels</li>
 * </ul>
 * 
 * <h3>Pixel Analysis</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.PixelProfile} - 
 *       Individual pixel classification</li>
 *   <li>{@link io.github.jspinak.brobot.model.analysis.color.PixelProfiles} - 
 *       Collection of pixel analyses for state images</li>
 * </ul>
 * 
 * <h2>Color Analysis Approach</h2>
 * 
 * <p>Instead of exact pixel matching, the system uses statistical ranges:</p>
 * <ol>
 *   <li>Define color ranges through min/max values per channel</li>
 *   <li>Calculate statistical properties (mean, stddev) within ranges</li>
 *   <li>Score pixels based on deviation from expected statistics</li>
 *   <li>Aggregate scores for overall match confidence</li>
 * </ol>
 * 
 * <h2>Color Spaces</h2>
 * 
 * <h3>BGR (Blue-Green-Red)</h3>
 * <p>Standard color space used by OpenCV:</p>
 * <ul>
 *   <li>Direct representation of display colors</li>
 *   <li>Sensitive to lighting changes</li>
 *   <li>Good for exact color matching</li>
 * </ul>
 * 
 * <h3>HSV (Hue-Saturation-Value)</h3>
 * <p>Perceptually uniform color space:</p>
 * <ul>
 *   <li>Hue: Color type (0-179 in OpenCV)</li>
 *   <li>Saturation: Color purity (0-255)</li>
 *   <li>Value: Brightness (0-255)</li>
 *   <li>More tolerant to lighting variations</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Creating Color Profiles</h3>
 * <pre>{@code
 * // Define a blue button color profile
 * ColorCluster blueButton = new ColorCluster();
 * 
 * // HSV profile (more lighting-tolerant)
 * ColorSchemaHSV hsv = new ColorSchemaHSV();
 * hsv.setValues(ColorValue.HUE, 100, 130, 115, 5);        // Blue hues
 * hsv.setValues(ColorValue.SATURATION, 150, 255, 200, 20); // High saturation
 * hsv.setValues(ColorValue.VALUE, 100, 255, 180, 30);      // Medium-bright
 * blueButton.setSchema(ColorSchemaName.HSV, hsv);
 * 
 * // BGR profile (exact matching)
 * ColorSchemaBGR bgr = new ColorSchemaBGR();
 * bgr.setValues(ColorValue.BLUE, 150, 255, 200, 20);
 * bgr.setValues(ColorValue.GREEN, 50, 150, 100, 20);
 * bgr.setValues(ColorValue.RED, 0, 100, 50, 20);
 * blueButton.setSchema(ColorSchemaName.BGR, bgr);
 * }</pre>
 * 
 * <h3>Pixel Classification</h3>
 * <pre>{@code
 * PixelProfile profile = new PixelProfile(targetImage, blueButton);
 * 
 * // Analyze a scene
 * Mat scene = captureScreen();
 * Mat scores = profile.calculateScores(scene);
 * 
 * // Find high-scoring regions
 * List<Region> blueRegions = profile.findMatchingRegions(scores, 0.8);
 * }</pre>
 * 
 * <h3>Multi-Image Analysis</h3>
 * <pre>{@code
 * PixelProfiles profiles = new PixelProfiles();
 * profiles.add(new PixelProfile(buttonImage, buttonColor));
 * profiles.add(new PixelProfile(headerImage, headerColor));
 * 
 * // Classify each pixel in the scene
 * Mat classification = profiles.classifyScene(scene);
 * 
 * // Get scores for specific image
 * Mat buttonScores = profiles.getScores(buttonImage);
 * }</pre>
 * 
 * <h2>Statistical Properties</h2>
 * 
 * <p>Each color channel maintains four statistics:</p>
 * <ul>
 *   <li><b>MIN</b> - Minimum acceptable value</li>
 *   <li><b>MAX</b> - Maximum acceptable value</li>
 *   <li><b>MEAN</b> - Expected average value</li>
 *   <li><b>STDDEV</b> - Expected standard deviation</li>
 * </ul>
 * 
 * <pre>{@code
 * ColorInfo hueInfo = colorSchema.getColorInfo(ColorValue.HUE);
 * double minHue = hueInfo.getStat(ColorStat.MIN);
 * double meanHue = hueInfo.getStat(ColorStat.MEAN);
 * double stddevHue = hueInfo.getStat(ColorStat.STDDEV);
 * }</pre>
 * 
 * <h2>Scoring Algorithm</h2>
 * 
 * <p>Pixels are scored based on statistical deviation:</p>
 * <ol>
 *   <li>Check if pixel is within min/max range</li>
 *   <li>Calculate z-score from mean and stddev</li>
 *   <li>Convert z-score to confidence (0.0-1.0)</li>
 *   <li>Combine scores across color channels</li>
 * </ol>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Use HSV for lighting-tolerant matching</li>
 *   <li>Use BGR when exact colors matter</li>
 *   <li>Define profiles from multiple sample images</li>
 *   <li>Set appropriate standard deviations for tolerance</li>
 *   <li>Validate profiles under different conditions</li>
 *   <li>Cache profiles for performance</li>
 * </ol>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <h3>Adaptive Profiles</h3>
 * <p>Profiles can be updated based on successful matches:</p>
 * <pre>{@code
 * profile.updateStatistics(successfulMatch);
 * profile.expandRange(0.1); // Add 10% tolerance
 * }</pre>
 * 
 * <h3>Profile Combination</h3>
 * <p>Multiple profiles can be combined for complex matching:</p>
 * <pre>{@code
 * ColorCluster combined = ColorCluster.combine(
 *     blueButtonNormal,
 *     blueButtonHover,
 *     blueButtonPressed
 * );
 * }</pre>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.analysis.scene
 * @see io.github.jspinak.brobot.action.basic.find.color
 * @see io.github.jspinak.brobot.imageUtils.color
 */
package io.github.jspinak.brobot.model.analysis.color;