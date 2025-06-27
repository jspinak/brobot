/**
 * Pattern matching and visual recognition actions for locating GUI elements.
 * 
 * <p>This package contains the core visual recognition capabilities of Brobot. Find actions
 * locate GUI elements using various matching strategies including image pattern matching,
 * text recognition (OCR), color analysis, histogram comparison, and motion detection.</p>
 * 
 * <h2>Find Strategies</h2>
 * 
 * <p>The Find action supports multiple strategies for different use cases:</p>
 * <ul>
 *   <li><b>FIRST</b> - Returns the first match found (fastest)</li>
 *   <li><b>BEST</b> - Returns the match with highest similarity score</li>
 *   <li><b>EACH</b> - Returns one match per target object</li>
 *   <li><b>ALL</b> - Returns all matches above the similarity threshold</li>
 *   <li><b>CUSTOM</b> - User-defined matching criteria</li>
 * </ul>
 * 
 * <h2>Specialized Find Actions</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.Find}</b> - Main pattern matching action supporting all strategies</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindImage}</b> - Optimized for image pattern matching</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindText}</b> - OCR-based text recognition</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindColor}</b> - Color-based element detection</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindState}</b> - State recognition using multiple patterns</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.FindAll}</b> - Batch finding with multiple strategies</li>
 * </ul>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <h3>Color Analysis (color subpackage)</h3>
 * <ul>
 *   <li>Pixel-level color matching and analysis</li>
 *   <li>Scene detection using color profiles</li>
 *   <li>Color distribution scoring</li>
 * </ul>
 * 
 * <h3>Histogram Matching (histogram subpackage)</h3>
 * <ul>
 *   <li>Image comparison using histogram analysis</li>
 *   <li>Robust to minor lighting variations</li>
 * </ul>
 * 
 * <h3>Motion Detection (motion subpackage)</h3>
 * <ul>
 *   <li>Dynamic element tracking</li>
 *   <li>Motion region identification</li>
 *   <li>Fixed vs. dynamic pixel classification</li>
 * </ul>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <pre>{@code
 * // Find the best match for an image
 * ActionOptions options = new ActionOptions.Builder()
 *     .setFind(Find.BEST)
 *     .setSimilarity(0.8)
 *     .build();
 * 
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("button.png")
 *     .build();
 * 
 * Find find = new Find(...);
 * ActionResult result = find.perform(new ActionResult(), targets);
 * 
 * // Find text on screen
 * ActionOptions textOptions = new ActionOptions.Builder()
 *     .setFind(Find.ALL)
 *     .build();
 * 
 * ObjectCollection textTargets = new ObjectCollection.Builder()
 *     .withStrings("Submit")
 *     .build();
 * 
 * FindText findText = new FindText(...);
 * ActionResult textResult = findText.perform(new ActionResult(), textTargets);
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.ActionOptions.Find
 * @see io.github.jspinak.brobot.model.Match
 */
package io.github.jspinak.brobot.action.basic.find;