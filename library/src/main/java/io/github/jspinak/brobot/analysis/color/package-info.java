/**
 * Color-based analysis and pixel classification algorithms.
 * 
 * <p>This package provides sophisticated color analysis capabilities that enable
 * robust pattern matching based on statistical color properties. It implements
 * multi-channel color space analysis, pixel classification, and color clustering
 * algorithms that provide tolerance to lighting variations and display differences.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Analysis and Classification</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.ColorAnalysis} - 
 *       Stores comprehensive pixel-level color analysis results</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.PixelClassifier} - 
 *       Classifies pixels based on color profiles</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.ColorClassifier} - 
 *       High-level color-based classification</li>
 * </ul>
 * 
 * <h3>Color Metrics and Scoring</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.DistanceMatrixCalculator} - 
 *       Computes color distance matrices for similarity analysis</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.SceneScoreCalculator} - 
 *       Calculates scene matching scores based on color analysis</li>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.ColorClusterFactory} - 
 *       Creates color clusters from sample data</li>
 * </ul>
 * 
 * <h2>Color Analysis Process</h2>
 * 
 * <ol>
 *   <li><b>Profile Creation</b> - Define color profiles from sample images</li>
 *   <li><b>Distance Calculation</b> - Compute pixel distances to color profiles</li>
 *   <li><b>Score Generation</b> - Convert distances to similarity scores</li>
 *   <li><b>Classification</b> - Assign pixels to best-matching profiles</li>
 *   <li><b>Aggregation</b> - Combine pixel scores for overall match confidence</li>
 * </ol>
 * 
 * <h2>Color Spaces</h2>
 * 
 * <p>The package operates on multiple color spaces simultaneously:</p>
 * <ul>
 *   <li><b>BGR</b> - Standard OpenCV color space, good for exact matching</li>
 *   <li><b>HSV</b> - Hue-Saturation-Value, tolerant to lighting changes</li>
 * </ul>
 * 
 * <h2>Analysis Metrics</h2>
 * 
 * <p>ColorAnalysis tracks multiple distance and score metrics:</p>
 * <ul>
 *   <li><b>DIST_TO_TARGET</b> - Euclidean distance to color center</li>
 *   <li><b>DIST_OUTSIDE_RANGE</b> - Distance beyond acceptable boundaries</li>
 *   <li><b>DIST_TO_BOUNDARY</b> - Signed distance to range edge</li>
 *   <li><b>SCORES</b> - Normalized similarity scores (0-1)</li>
 *   <li><b>SCORE_DISTANCE</b> - Distance from score threshold</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Color Profile Creation</h3>
 * <pre>{@code
 * // Create color profile from samples
 * ColorClusterFactory factory = new ColorClusterFactory();
 * List<Mat> buttonSamples = loadButtonImages();
 * ColorCluster buttonProfile = factory.create(buttonSamples);
 * }</pre>
 * 
 * <h3>Pixel Classification</h3>
 * <pre>{@code
 * // Classify pixels in a scene
 * PixelClassifier classifier = new PixelClassifier();
 * Mat scene = captureScreen();
 * Mat classification = classifier.classify(scene, List.of(
 *     buttonProfile,
 *     backgroundProfile,
 *     textProfile
 * ));
 * 
 * // Each pixel value indicates which profile matched best
 * }</pre>
 * 
 * <h3>Scene Scoring</h3>
 * <pre>{@code
 * // Calculate how well a scene matches color profiles
 * SceneScoreCalculator scorer = new SceneScoreCalculator();
 * double matchScore = scorer.calculateScore(scene, targetProfile);
 * 
 * if (matchScore > 0.85) {
 *     // High confidence match
 * }
 * }</pre>
 * 
 * <h3>Distance Analysis</h3>
 * <pre>{@code
 * // Analyze color distances for debugging
 * DistanceMatrixCalculator calculator = new DistanceMatrixCalculator();
 * ColorAnalysis analysis = calculator.analyze(image, colorProfile);
 * 
 * Mat distances = analysis.getAnalyses(
 *     ColorAnalysis.Analysis.DIST_TO_TARGET, 
 *     ColorCluster.ColorSchemaName.HSV
 * );
 * }</pre>
 * 
 * <h2>Subpackages</h2>
 * 
 * <h3>kmeans</h3>
 * <p>K-means clustering for color segmentation:</p>
 * <ul>
 *   <li>Automatic color cluster discovery</li>
 *   <li>Multi-center color profiles</li>
 *   <li>Adaptive color matching</li>
 * </ul>
 * 
 * <h3>profiles</h3>
 * <p>Color profile management and construction:</p>
 * <ul>
 *   <li>Statistical profile building</li>
 *   <li>Profile matrix initialization</li>
 *   <li>Profile collection management</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Use HSV for lighting-tolerant matching</li>
 *   <li>Create profiles from multiple sample images</li>
 *   <li>Cache color profiles for performance</li>
 *   <li>Validate profiles under different conditions</li>
 *   <li>Use appropriate score thresholds for your use case</li>
 * </ol>
 * 
 * <h2>Performance Tips</h2>
 * 
 * <ul>
 *   <li>Downsample large images before analysis</li>
 *   <li>Limit analysis to regions of interest</li>
 *   <li>Pre-compute color profiles during initialization</li>
 *   <li>Use parallel processing for multi-image analysis</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.analysis.color
 * @see io.github.jspinak.brobot.action.basic.find.color
 */
package io.github.jspinak.brobot.analysis.color;