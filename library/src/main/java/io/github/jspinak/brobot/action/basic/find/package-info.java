/**
 * Pattern matching and visual recognition actions for locating GUI elements.
 *
 * <p>This package contains the core visual recognition capabilities of Brobot. Find actions locate
 * GUI elements using various matching strategies including image pattern matching, text recognition
 * (OCR), color analysis, histogram comparison, and motion detection.
 *
 * <h2>Find Strategies</h2>
 *
 * <p>Find actions support multiple strategies through {@link
 * io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Strategy}:
 *
 * <ul>
 *   <li><b>FIRST</b> - Returns the first match found (fastest)
 *   <li><b>BEST</b> - Returns the match with highest similarity score
 *   <li><b>EACH</b> - Returns one match per target object
 *   <li><b>ALL</b> - Returns all matches above the similarity threshold
 *   <li><b>CUSTOM</b> - User-defined matching criteria via custom predicates
 * </ul>
 *
 * <h2>Specialized Find Actions</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.Find}</b> - Main pattern matching
 *       action supporting all strategies
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindImage}</b> - Optimized for image
 *       pattern matching
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindText}</b> - OCR-based text
 *       recognition
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindColor}</b> - Color-based element
 *       detection
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindState}</b> - State recognition
 *       using multiple patterns
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindAll}</b> - Batch finding with
 *       multiple strategies
 * </ul>
 *
 * <h2>Advanced Features</h2>
 *
 * <h3>Color Analysis (color subpackage)</h3>
 *
 * <ul>
 *   <li>Pixel-level color matching and analysis
 *   <li>Scene detection using color profiles
 *   <li>Color distribution scoring
 * </ul>
 *
 * <h3>Histogram Matching (histogram subpackage)</h3>
 *
 * <ul>
 *   <li>Image comparison using histogram analysis
 *   <li>Robust to minor lighting variations
 * </ul>
 *
 * <h3>Motion Detection (motion subpackage)</h3>
 *
 * <ul>
 *   <li>Dynamic element tracking
 *   <li>Motion region identification
 *   <li>Fixed vs. dynamic pixel classification
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <pre>{@code
 * // Find the best match for an image using ActionConfig (recommended)
 * PatternFindOptions findOptions = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .setSimilarity(0.8)
 *     .build();
 *
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("button.png")
 *     .build();
 *
 * Find find = new Find(...);
 * ActionResult result = find.perform(findOptions, targets);
 *
 * // Find all matches above threshold
 * PatternFindOptions findAllOptions = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.ALL)
 *     .setSimilarity(0.85)
 *     .setMaxMatches(10)
 *     .build();
 *
 * // Find text on screen using pattern matching
 * ObjectCollection textTargets = new ObjectCollection.Builder()
 *     .withStrings("Submit")
 *     .build();
 *
 * FindText findText = new FindText(...);
 * ActionResult textResult = findText.perform(findAllOptions, textTargets);
 *
 * // Color-based finding
 * ColorFindOptions colorOptions = new ColorFindOptions.Builder()
 *     .setColorStrategy(ColorFindOptions.Color.TARGET_REGION_HSVA_PROFILE)
 *     .setMaxColorDistance(50)
 *     .build();
 *
 * FindColor findColor = new FindColor(...);
 * ActionResult colorResult = findColor.perform(colorOptions, targets);
 * }</pre>
 *
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.ActionConfig.Find
 * @see io.github.jspinak.brobot.model.Match
 */
package io.github.jspinak.brobot.action.basic.find;
