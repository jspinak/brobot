/**
 * Histogram-based image matching for robust pattern recognition.
 * 
 * <p>This package provides histogram comparison techniques for finding images that may
 * have variations in lighting, contrast, or minor color shifts. Histogram matching is
 * more tolerant of environmental variations than pixel-perfect pattern matching.</p>
 * 
 * <h2>Key Features</h2>
 * 
 * <ul>
 *   <li><b>Lighting Invariance</b> - Robust to brightness and contrast variations</li>
 *   <li><b>Color Distribution Analysis</b> - Matches based on overall color patterns rather than exact pixels</li>
 *   <li><b>Multi-Channel Support</b> - Analyzes RGB channels independently or combined</li>
 *   <li><b>Statistical Comparison</b> - Uses proven histogram comparison metrics</li>
 * </ul>
 * 
 * <h2>Primary Class</h2>
 * 
 * <p><b>{@link io.github.jspinak.brobot.action.basic.find.histogram.FindHistogram}</b> - 
 * Performs histogram-based image matching using various comparison methods including
 * correlation, chi-square, intersection, and Bhattacharyya distance.</p>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Finding UI elements under varying lighting conditions</li>
 *   <li>Matching images captured at different times of day</li>
 *   <li>Detecting elements on screens with dynamic brightness</li>
 *   <li>Cross-platform matching where rendering may vary slightly</li>
 * </ul>
 * 
 * <h2>Histogram Matching Process</h2>
 * 
 * <ol>
 *   <li>Extract histogram from target image pattern</li>
 *   <li>Scan search region and compute histograms for sub-regions</li>
 *   <li>Compare histograms using selected metric</li>
 *   <li>Identify regions with histogram similarity above threshold</li>
 *   <li>Return matches ranked by similarity score</li>
 * </ol>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Find an image using histogram matching
 * FindHistogram findHistogram = new FindHistogram(...);
 * 
 * ActionOptions options = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.FIND)
 *     .setFind(Find.HISTOGRAM)
 *     .setSimilarity(0.85)  // Histogram similarity threshold
 *     .build();
 * 
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("button_any_lighting.png")
 *     .build();
 * 
 * ActionResult result = findHistogram.perform(new ActionResult(), targets);
 * 
 * if (result.isSuccess()) {
 *     System.out.println("Found element despite lighting variations");
 * }
 * }</pre>
 * 
 * <h2>Advantages over Pixel Matching</h2>
 * 
 * <ul>
 *   <li>More tolerant of environmental variations</li>
 *   <li>Better for natural images or photos</li>
 *   <li>Useful when exact pixel matching is too strict</li>
 *   <li>Can find similar but not identical images</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.imageUtils.HistogramAnalysis
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
package io.github.jspinak.brobot.action.basic.find.histogram;