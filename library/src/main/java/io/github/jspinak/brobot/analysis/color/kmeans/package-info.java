/**
 * K-means clustering algorithms for color segmentation and analysis.
 * 
 * <p>This package implements k-means clustering specifically tailored for color
 * analysis in GUI automation. It enables automatic discovery of dominant colors
 * and creation of adaptive color profiles that can handle variations in UI
 * element appearance.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.kmeans.KmeansCluster} - 
 *       Individual cluster with center and member pixels</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfile} - 
 *       Color profile based on k-means clustering</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfiles} - 
 *       Collection of k-means profiles for multi-class analysis</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.kmeans.KmeansProfilesAllSchemas} - 
 *       K-means profiles across multiple color spaces</li>
 * </ul>
 * 
 * <h2>K-means Color Clustering</h2>
 * 
 * <p>The k-means algorithm for color analysis:</p>
 * <ol>
 *   <li><b>Initialization</b> - Select k initial cluster centers</li>
 *   <li><b>Assignment</b> - Assign each pixel to nearest cluster</li>
 *   <li><b>Update</b> - Recalculate cluster centers</li>
 *   <li><b>Iterate</b> - Repeat until convergence</li>
 *   <li><b>Profile Creation</b> - Build color profiles from final clusters</li>
 * </ol>
 * 
 * <h2>Use Cases</h2>
 * 
 * <h3>Dominant Color Extraction</h3>
 * <pre>{@code
 * // Extract dominant colors from UI element
 * KmeansProfile profile = new KmeansProfile();
 * profile.initializeFromImage(buttonImage, 5); // 5 clusters
 * 
 * List<KmeansCluster> dominantColors = profile.getClusters();
 * for (KmeansCluster cluster : dominantColors) {
 *     Scalar color = cluster.getCenter();
 *     double weight = cluster.getWeight();
 *     System.out.println("Color: " + color + ", Weight: " + weight);
 * }
 * }</pre>
 * 
 * <h3>Multi-Element Profiling</h3>
 * <pre>{@code
 * // Create profiles for multiple UI elements
 * KmeansProfiles profiles = new KmeansProfiles();
 * 
 * profiles.addProfile("button", buttonKmeansProfile);
 * profiles.addProfile("header", headerKmeansProfile);
 * profiles.addProfile("background", backgroundKmeansProfile);
 * 
 * // Use for scene classification
 * String bestMatch = profiles.classifyPixel(pixelColor);
 * }</pre>
 * 
 * <h3>Cross-Color-Space Analysis</h3>
 * <pre>{@code
 * // Analyze in both BGR and HSV
 * KmeansProfilesAllSchemas allSchemas = new KmeansProfilesAllSchemas();
 * 
 * allSchemas.addProfile(ColorSchemaName.BGR, bgrKmeansProfile);
 * allSchemas.addProfile(ColorSchemaName.HSV, hsvKmeansProfile);
 * 
 * // Combined analysis for robust matching
 * double bgrScore = allSchemas.scorePixel(pixel, ColorSchemaName.BGR);
 * double hsvScore = allSchemas.scorePixel(pixel, ColorSchemaName.HSV);
 * double combinedScore = (bgrScore + hsvScore) / 2.0;
 * }</pre>
 * 
 * <h2>Advantages of K-means</h2>
 * 
 * <ul>
 *   <li><b>Automatic Discovery</b> - No manual color selection needed</li>
 *   <li><b>Adaptive</b> - Adjusts to actual color distribution</li>
 *   <li><b>Compact</b> - Represents complex colors with few clusters</li>
 *   <li><b>Robust</b> - Handles color variations naturally</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <h3>Cluster Count</h3>
 * <ul>
 *   <li>2-3 clusters: Simple two-tone elements</li>
 *   <li>4-6 clusters: Standard UI elements</li>
 *   <li>7-10 clusters: Complex gradients</li>
 *   <li>10+ clusters: Photographic content</li>
 * </ul>
 * 
 * <h3>Distance Metrics</h3>
 * <ul>
 *   <li>Euclidean: Standard color distance</li>
 *   <li>Weighted: Emphasize certain channels</li>
 *   <li>Perceptual: Human vision-based metrics</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Choose k based on visual complexity</li>
 *   <li>Validate clusters visually before use</li>
 *   <li>Use multiple samples for stable profiles</li>
 *   <li>Consider both BGR and HSV clustering</li>
 *   <li>Monitor convergence for quality</li>
 * </ol>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>K-means is iterative - cache results</li>
 *   <li>Larger k increases computation time</li>
 *   <li>Downsample images for faster clustering</li>
 *   <li>Use parallel processing for multiple images</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.analysis.color
 * @see io.github.jspinak.brobot.model.analysis.color
 */
package io.github.jspinak.brobot.analysis.color.kmeans;