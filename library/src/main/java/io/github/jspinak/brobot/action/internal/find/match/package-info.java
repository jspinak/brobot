/**
 * Match processing, transformation, and collection management utilities.
 *
 * <p>This package provides comprehensive tools for working with Match objects, including
 * adjustment, filtering, transformation, and collection management. These utilities ensure that raw
 * matches from various sources are properly processed into actionable results.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchAdjuster}</b> - Applies
 *       position and dimension adjustments to matches based on ActionConfig settings
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchCollectionUtilities}</b>
 *       - Provides essential operations for managing match collections including filtering and
 *       limiting
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchContentExtractor}</b> -
 *       Captures visual and text content from matched regions for further processing
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match.MatchToStateConverter}</b> -
 *       Transforms matches into State objects for dynamic state generation and learning
 * </ul>
 *
 * <h2>Match Processing Pipeline</h2>
 *
 * <ol>
 *   <li><b>Raw Match Creation</b> - Matches created by various find strategies
 *   <li><b>Position Adjustment</b> - Apply offsets and resize operations
 *   <li><b>Content Extraction</b> - Capture images and text from match regions
 *   <li><b>Collection Management</b> - Filter, sort, and limit match sets
 *   <li><b>State Conversion</b> - Transform matches into reusable states when needed
 * </ol>
 *
 * <h2>Match Adjustment Features</h2>
 *
 * <p>The MatchAdjuster supports various transformations:
 *
 * <ul>
 *   <li><b>Position Offsets</b> - Move match locations by specified amounts (addX/addY)
 *   <li><b>Relative Resizing</b> - Adjust dimensions proportionally (addW/addH)
 *   <li><b>Absolute Resizing</b> - Set exact dimensions (absoluteW/absoluteH)
 *   <li><b>Batch Processing</b> - Apply adjustments to entire collections efficiently
 * </ul>
 *
 * <h2>Collection Management</h2>
 *
 * <p>MatchCollectionUtilities provides:
 *
 * <ul>
 *   <li>Adding matches from various sources to ActionResults
 *   <li>Limiting match counts based on action strategies
 *   <li>Filtering matches by score or other criteria
 *   <li>Sorting matches for optimal selection
 * </ul>
 *
 * <h2>Content Extraction</h2>
 *
 * <p>MatchContentExtractor captures:
 *
 * <ul>
 *   <li><b>Visual Content</b> - BufferedImage and Mat representations
 *   <li><b>Text Content</b> - OCR extraction when required
 *   <li><b>Scene Context</b> - Links to source scenes for reference
 *   <li><b>Timing</b> - Runs after all adjustments for accuracy
 * </ul>
 *
 * <h2>Dynamic State Learning</h2>
 *
 * <p>MatchToStateConverter enables:
 *
 * <ul>
 *   <li>Converting successful matches into reusable StateImage objects
 *   <li>Grouping related matches into cohesive State objects
 *   <li>Supporting adaptive automation that learns from results
 *   <li>Building new states during exploration phases
 * </ul>
 *
 * <h2>Integration Points</h2>
 *
 * <p>These utilities integrate throughout the find system:
 *
 * <ul>
 *   <li>Called by all find strategies to process results
 *   <li>Work with ActionConfig to apply user preferences
 *   <li>Support both individual and batch operations
 *   <li>Maintain consistency across different match sources
 * </ul>
 *
 * @see io.github.jspinak.brobot.model.match.Match
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.model.state.State
 */
package io.github.jspinak.brobot.action.internal.find.match;
