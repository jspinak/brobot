/**
 * Provides image visualization tools for debugging and analysis.
 * 
 * <p>This package contains utilities for visualizing image processing results,
 * debugging template matching operations, and creating visual representations
 * of recognition scores. These tools are essential for understanding and
 * optimizing the visual automation process.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer} - 
 *       Comprehensive Mat visualization for debugging and analysis</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.visualization.ScoringVisualizer} - 
 *       Visualizes color matching scores and similarity maps</li>
 *   <li>{@link io.github.jspinak.brobot.util.image.visualization.MatBuilder} - 
 *       Flexible builder for composing multiple images into visualizations</li>
 * </ul>
 * 
 * <h2>Visualization Capabilities</h2>
 * <ul>
 *   <li><strong>Match Highlighting</strong>: Draw bounding boxes around detected patterns</li>
 *   <li><strong>Score Heatmaps</strong>: Visualize similarity scores as color gradients</li>
 *   <li><strong>Image Composition</strong>: Combine multiple images with labels</li>
 *   <li><strong>Debug Annotations</strong>: Add text, arrows, and markers</li>
 *   <li><strong>Matrix Analysis</strong>: Visualize Mat data as grayscale or color maps</li>
 * </ul>
 * 
 * <h2>MatrixVisualizer Features</h2>
 * <p>Comprehensive debugging capabilities:
 * <ul>
 *   <li><strong>Value Visualization</strong>: Display matrix values as intensity maps</li>
 *   <li><strong>Channel Separation</strong>: View individual color channels</li>
 *   <li><strong>Histogram Display</strong>: Visualize value distributions</li>
 *   <li><strong>Grid Overlay</strong>: Add coordinate grids for precision</li>
 *   <li><strong>ROI Highlighting</strong>: Mark regions of interest</li>
 * </ul>
 * 
 * <h2>ScoringVisualizer Features</h2>
 * <p>Score-based visualizations:
 * <ul>
 *   <li><strong>Heatmap Generation</strong>: Convert scores to color gradients</li>
 *   <li><strong>Threshold Visualization</strong>: Show score cutoff boundaries</li>
 *   <li><strong>Multi-Class Scoring</strong>: Different colors for score ranges</li>
 *   <li><strong>Confidence Indicators</strong>: Visual confidence representations</li>
 * </ul>
 * 
 * <h2>MatBuilder Capabilities</h2>
 * <p>Flexible image composition:
 * <ul>
 *   <li><strong>Grid Layouts</strong>: Arrange images in rows and columns</li>
 *   <li><strong>Custom Spacing</strong>: Control gaps between images</li>
 *   <li><strong>Label Support</strong>: Add descriptive text to each image</li>
 *   <li><strong>Border Options</strong>: Frame images for clarity</li>
 *   <li><strong>Size Normalization</strong>: Auto-resize for consistent layouts</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Visualize template matching results
 * MatrixVisualizer visualizer = context.getBean(MatrixVisualizer.class);
 * Mat visualization = visualizer.drawMatches(screenshot, matches);
 * 
 * // Create score heatmap
 * ScoringVisualizer scorer = context.getBean(ScoringVisualizer.class);
 * Mat heatmap = scorer.createHeatmap(scoreMatrix, 0.0, 1.0);
 * 
 * // Build composite debug image
 * MatBuilder builder = new MatBuilder();
 * Mat composite = builder
 *     .addImage(original, "Original")
 *     .addImage(template, "Template")
 *     .addImage(result, "Matches")
 *     .withColumns(3)
 *     .withSpacing(10)
 *     .build();
 * 
 * // Visualize individual channels
 * Mat channels = visualizer.visualizeChannels(colorImage);
 * 
 * // Show value distribution
 * Mat histogram = visualizer.createHistogram(grayImage);
 * }</pre>
 * 
 * <h2>Debugging Workflows</h2>
 * <ol>
 *   <li><strong>Capture Failure</strong>: Visualize what was actually captured</li>
 *   <li><strong>Match Analysis</strong>: Show why matches succeeded or failed</li>
 *   <li><strong>Score Distribution</strong>: Understand confidence patterns</li>
 *   <li><strong>Process Flow</strong>: Track image transformations step-by-step</li>
 * </ol>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Visualization is computationally expensive; use only for debugging</li>
 *   <li>Large images should be downscaled for display</li>
 *   <li>Disable visualization in production environments</li>
 *   <li>Cache visualizations when showing the same data repeatedly</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Use consistent color schemes across visualizations</li>
 *   <li>Add clear labels and legends</li>
 *   <li>Save debug images with descriptive filenames</li>
 *   <li>Include timestamp and context information</li>
 *   <li>Implement toggleable visualization for development/production modes</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.util.image.core
 * @see io.github.jspinak.brobot.util.image.io
 * @see io.github.jspinak.brobot.action.results
 * @since 1.0
 */
package io.github.jspinak.brobot.util.image.visualization;