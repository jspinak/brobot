/**
 * Color profile construction and management utilities.
 *
 * <p>This package provides builders and initializers for creating various types of color profiles
 * used in GUI element recognition. It supports both statistical profiles based on mean/standard
 * deviation and k-means clustering profiles for more complex color distributions.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.analysis.color.profiles.ColorStatProfile} - Statistical
 *       color profile with mean and standard deviation
 *   <li>{@link io.github.jspinak.brobot.analysis.color.profiles.ProfileMatrixBuilder} - Builds
 *       matrices of color values from images
 *   <li>{@link io.github.jspinak.brobot.analysis.color.profiles.ProfileMatrixInitializer} -
 *       Initializes profile matrices from sample images
 *   <li>{@link io.github.jspinak.brobot.analysis.color.profiles.KmeansProfileBuilder} - Creates
 *       k-means clustering profiles
 *   <li>{@link io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder} - Builds
 *       collections of related profiles
 * </ul>
 *
 * <h2>Profile Creation Process</h2>
 *
 * <ol>
 *   <li><b>Sample Collection</b> - Gather representative images
 *   <li><b>Matrix Building</b> - Extract color values into matrices
 *   <li><b>Statistical Analysis</b> - Calculate means and deviations
 *   <li><b>Profile Generation</b> - Create ColorCluster objects
 *   <li><b>Validation</b> - Test profiles against sample data
 * </ol>
 *
 * <h2>Profile Types</h2>
 *
 * <h3>Statistical Profiles</h3>
 *
 * <pre>{@code
 * // Create profile from image samples
 * ProfileMatrixInitializer initializer = new ProfileMatrixInitializer();
 * Mat colorMatrix = initializer.buildFromImages(buttonSamples);
 *
 * ColorStatProfile statProfile = new ColorStatProfile();
 * statProfile.calculate(colorMatrix);
 *
 * // Results: mean and stddev for each channel
 * Scalar mean = statProfile.getMean();
 * Scalar stddev = statProfile.getStddev();
 * }</pre>
 *
 * <h3>K-means Profiles</h3>
 *
 * <pre>{@code
 * // Build k-means profile with 5 clusters
 * KmeansProfileBuilder kmeansBuilder = new KmeansProfileBuilder();
 * KmeansProfile profile = kmeansBuilder.build(images, 5);
 *
 * // Each cluster represents a dominant color
 * for (KmeansCluster cluster : profile.getClusters()) {
 *     Scalar color = cluster.getCenter();
 *     double weight = cluster.getWeight();
 * }
 * }</pre>
 *
 * <h3>Profile Sets</h3>
 *
 * <pre>{@code
 * // Build profiles for multiple UI elements
 * ProfileSetBuilder setBuilder = new ProfileSetBuilder();
 *
 * setBuilder.addElement("button", buttonImages);
 * setBuilder.addElement("header", headerImages);
 * setBuilder.addElement("background", backgroundImages);
 *
 * ColorProfiles profiles = setBuilder.build();
 * }</pre>
 *
 * <h2>Color Spaces</h2>
 *
 * <p>Profiles can be built in different color spaces:
 *
 * <ul>
 *   <li><b>BGR</b> - Standard color space, sensitive to lighting
 *   <li><b>HSV</b> - Hue-based, more tolerant to illumination changes
 *   <li><b>Combined</b> - Uses both spaces for robust matching
 * </ul>
 *
 * <h2>Matrix Structure</h2>
 *
 * <p>Color matrices are organized as:
 *
 * <ul>
 *   <li>Rows: Individual pixels from all samples
 *   <li>Columns: Color channels (3 for BGR/HSV)
 *   <li>Values: Normalized 0-255 for each channel
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Use multiple sample images for stable profiles
 *   <li>Capture samples under expected lighting conditions
 *   <li>Validate profiles with test images before deployment
 *   <li>Consider both statistical and k-means approaches
 *   <li>Cache built profiles for performance
 * </ol>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Matrix building is memory intensive for large images
 *   <li>K-means clustering is iterative and can be slow
 *   <li>Profile creation should be done during initialization
 *   <li>Consider downsampling very large images
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.analysis.color
 * @see io.github.jspinak.brobot.analysis.color
 */
package io.github.jspinak.brobot.analysis.color.profiles;
