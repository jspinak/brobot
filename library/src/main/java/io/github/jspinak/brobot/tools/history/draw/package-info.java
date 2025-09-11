/**
 * Low-level drawing primitives for visualization components.
 *
 * <p>This package contains specialized drawing classes that handle the rendering of specific visual
 * elements in Brobot's history visualization system. Each class focuses on drawing a particular
 * type of visual annotation, providing a modular approach to building complex visualizations.
 *
 * <h2>Drawing Components</h2>
 *
 * <h3>Basic Shapes</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawPoint} - Draws circular points for
 *       click locations
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawRect} - Draws rectangles for regions
 *       and matches
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawLine} - Draws lines for connections
 *       and paths
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawArrow} - Draws arrows for drag
 *       operations and directions
 * </ul>
 *
 * <h3>Match Visualization</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawMatch} - Draws match results with
 *       confidence indicators
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.HighlightMatchedRegion} - Highlights
 *       matched regions with transparency effects
 * </ul>
 *
 * <h3>Analysis Visualization</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawClassifications} - Visualizes pixel
 *       classification results
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawColorProfile} - Renders color
 *       profile information
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawHistogram} - Creates histogram
 *       visualizations
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawContours} - Draws detected contours
 *       and boundaries
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawMotion} - Visualizes motion
 *       detection results
 * </ul>
 *
 * <h3>Legends and Labels</h3>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.history.draw.DrawClassesLegend} - Creates legends for
 *       classification visualizations
 * </ul>
 *
 * <h2>Drawing Standards</h2>
 *
 * <h3>Color Conventions</h3>
 *
 * <pre>{@code
 * // Standard colors used across visualizations
 * RED     - Failed matches, errors, regular elements
 * GREEN   - Successful matches, transitions
 * BLUE    - Information, state boundaries
 * YELLOW  - Warnings, search regions
 * WHITE   - Text labels, highlights
 * BLACK   - Borders, shadows
 * }</pre>
 *
 * <h3>Line Styles</h3>
 *
 * <ul>
 *   <li><b>Solid</b> - Confirmed matches and boundaries
 *   <li><b>Dashed</b> - Potential matches or search areas
 *   <li><b>Dotted</b> - Historical or reference data
 * </ul>
 *
 * <h3>Thickness Guidelines</h3>
 *
 * <ul>
 *   <li><b>1px</b> - Fine details, text
 *   <li><b>2px</b> - Standard shapes
 *   <li><b>3px</b> - Important highlights
 *   <li><b>4px+</b> - Critical focus areas
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Drawing Click Points</h3>
 *
 * <pre>{@code
 * DrawPoint drawer = new DrawPoint();
 * drawer.draw(image, clickLocation, Color.BLUE, 5); // 5px radius
 * drawer.drawWithLabel(image, clickLocation, "Click", Color.BLUE);
 * }</pre>
 *
 * <h3>Drawing Match Results</h3>
 *
 * <pre>{@code
 * DrawMatch matchDrawer = new DrawMatch();
 * matchDrawer.draw(image, match);
 *
 * // With confidence display
 * matchDrawer.drawWithScore(image, match, showPercentage);
 * }</pre>
 *
 * <h3>Drawing Classifications</h3>
 *
 * <pre>{@code
 * DrawClassifications classifier = new DrawClassifications();
 * Mat classified = classifier.drawPixelClasses(
 *     classificationMat,
 *     colorMap
 * );
 *
 * // Add legend
 * DrawClassesLegend legend = new DrawClassesLegend();
 * legend.draw(image, classifications, position);
 * }</pre>
 *
 * <h3>Composite Drawing</h3>
 *
 * <pre>{@code
 * // Combine multiple drawing operations
 * Mat visualization = scene.clone();
 *
 * // Draw search region
 * DrawRect.draw(visualization, searchRegion, Color.YELLOW, 1);
 *
 * // Draw matches
 * for (Match match : matches) {
 *     DrawMatch.draw(visualization, match);
 * }
 *
 * // Draw click point
 * DrawPoint.draw(visualization, clickPoint, Color.RED, 3);
 *
 * // Add arrow for drag
 * DrawArrow.draw(visualization, startPoint, endPoint, Color.GREEN);
 * }</pre>
 *
 * <h2>OpenCV Integration</h2>
 *
 * <p>All drawing classes work with OpenCV Mat objects:
 *
 * <ul>
 *   <li>Support BGR color format
 *   <li>Handle coordinate transformations
 *   <li>Provide anti-aliasing options
 *   <li>Support transparency/alpha blending
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Use consistent colors across visualization types
 *   <li>Apply appropriate line thickness for visibility
 *   <li>Add labels for clarity when needed
 *   <li>Consider overlay transparency for readability
 *   <li>Test visualizations at different scales
 * </ul>
 *
 * <h2>Performance Notes</h2>
 *
 * <ul>
 *   <li>Drawing operations modify Mat objects in-place
 *   <li>Clone images if original must be preserved
 *   <li>Batch similar operations when possible
 *   <li>Consider resolution for performance vs quality
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.tools.history
 * @see org.bytedeco.opencv.opencv_imgproc
 */
package io.github.jspinak.brobot.tools.history.draw;
