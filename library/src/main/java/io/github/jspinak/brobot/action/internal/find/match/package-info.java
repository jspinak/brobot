/**
 * Match processing, transformation, and collection management utilities.
 * 
 * <p>This package provides comprehensive tools for working with Match objects,
 * including adjustment, filtering, transformation, and collection management.
 * These utilities ensure that raw matches from various sources are properly
 * processed into actionable results.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchAdjuster}</b> - 
 *       Applies position and dimension adjustments to matches based on
 *       ActionOptions settings</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities}</b> - 
 *       Provides essential operations for managing match collections including
 *       filtering and limiting</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor}</b> - 
 *       Captures visual and text content from matched regions for further
 *       processing</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchToStateConverter}</b> - 
 *       Transforms matches into State objects for dynamic state generation
 *       and learning</li>
 * </ul>
 * 
 * <h2>Match Processing Pipeline</h2>
 * 
 * <ol>
 *   <li><b>Raw Match Creation</b> - Matches created by various find strategies</li>
 *   <li><b>Position Adjustment</b> - Apply offsets and resize operations</li>
 *   <li><b>Content Extraction</b> - Capture images and text from match regions</li>
 *   <li><b>Collection Management</b> - Filter, sort, and limit match sets</li>
 *   <li><b>State Conversion</b> - Transform matches into reusable states when needed</li>
 * </ol>
 * 
 * <h2>Match Adjustment Features</h2>
 * 
 * <p>The MatchAdjuster supports various transformations:</p>
 * <ul>
 *   <li><b>Position Offsets</b> - Move match locations by specified amounts (addX/addY)</li>
 *   <li><b>Relative Resizing</b> - Adjust dimensions proportionally (addW/addH)</li>
 *   <li><b>Absolute Resizing</b> - Set exact dimensions (absoluteW/absoluteH)</li>
 *   <li><b>Batch Processing</b> - Apply adjustments to entire collections efficiently</li>
 * </ul>
 * 
 * <h2>Collection Management</h2>
 * 
 * <p>MatchCollectionUtilities provides:</p>
 * <ul>
 *   <li>Adding matches from various sources to ActionResults</li>
 *   <li>Limiting match counts based on action strategies</li>
 *   <li>Filtering matches by score or other criteria</li>
 *   <li>Sorting matches for optimal selection</li>
 * </ul>
 * 
 * <h2>Content Extraction</h2>
 * 
 * <p>MatchContentExtractor captures:</p>
 * <ul>
 *   <li><b>Visual Content</b> - BufferedImage and Mat representations</li>
 *   <li><b>Text Content</b> - OCR extraction when required</li>
 *   <li><b>Scene Context</b> - Links to source scenes for reference</li>
 *   <li><b>Timing</b> - Runs after all adjustments for accuracy</li>
 * </ul>
 * 
 * <h2>Dynamic State Learning</h2>
 * 
 * <p>MatchToStateConverter enables:</p>
 * <ul>
 *   <li>Converting successful matches into reusable StateImage objects</li>
 *   <li>Grouping related matches into cohesive State objects</li>
 *   <li>Supporting adaptive automation that learns from results</li>
 *   <li>Building new states during exploration phases</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * 
 * <p>These utilities integrate throughout the find system:</p>
 * <ul>
 *   <li>Called by all find strategies to process results</li>
 *   <li>Work with ActionOptions to apply user preferences</li>
 *   <li>Support both individual and batch operations</li>
 *   <li>Maintain consistency across different match sources</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.model.match.Match
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.model.state.State
 */
package io.github.jspinak.brobot.action.internal.find.match;