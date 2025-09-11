/**
 * Visual feedback and region definition actions.
 *
 * <p>This package provides actions for defining screen regions and providing visual feedback during
 * automation. These actions help with debugging, region selection, and visual confirmation of
 * automation targets.
 *
 * <h2>Region Definition Actions</h2>
 *
 * <p>These actions create or modify regions based on various criteria:
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineRegion}</b> - Basic region
 *       definition using coordinates or existing regions
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineWithMatch}</b> - Define
 *       regions based on found matches
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors}</b> - Create
 *       regions bounded by anchor points
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineOutsideAnchors}</b> - Define
 *       regions outside of anchor boundaries
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineIncludingMatches}</b> - Expand
 *       regions to include all specified matches
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.region.DefineWithWindow}</b> - Define
 *       regions based on application windows
 * </ul>
 *
 * <h2>Visual Feedback</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.highlight.Highlight}</b> - Visually
 *       highlight regions on screen for debugging and confirmation
 * </ul>
 *
 * <h2>Region Definition Strategies</h2>
 *
 * <h3>Anchor-Based Definition</h3>
 *
 * <p>Use visual elements as reference points to define dynamic regions:
 *
 * <ul>
 *   <li>Inside anchors - Region bounded by surrounding elements
 *   <li>Outside anchors - Region excluding certain areas
 *   <li>Relative positioning - Define regions relative to found elements
 * </ul>
 *
 * <h3>Match-Based Definition</h3>
 *
 * <p>Create regions based on pattern matching results:
 *
 * <ul>
 *   <li>Single match - Region around a specific found element
 *   <li>Multiple matches - Encompassing region for all matches
 *   <li>Extended regions - Add margins around matches
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Define a fixed region
 * DefineRegion defineRegion = new DefineRegion(...);
 *
 * ObjectCollection regionDef = new ObjectCollection.Builder()
 *     .withRegions(new Region(100, 100, 200, 150))
 *     .build();
 *
 * ActionResult regionResult = defineRegion.perform(new ActionResult(), regionDef);
 *
 * // Define region based on anchors
 * DefineInsideAnchors defineInside = new DefineInsideAnchors(...);
 *
 * ObjectCollection anchors = new ObjectCollection.Builder()
 *     .withImages("top_left_corner.png", "bottom_right_corner.png")
 *     .build();
 *
 * ActionResult anchorResult = defineInside.perform(new ActionResult(), anchors);
 *
 * // Highlight a region for visual confirmation
 * Highlight highlight = new Highlight(...);
 *
 * ActionOptions highlightOptions = new ActionOptions.Builder()
 *     .setAction(ActionType.HIGHLIGHT)
 *     .setHighlightDuration(3.0)  // Show for 3 seconds
 *     .setHighlightColor("RED")
 *     .build();
 *
 * highlight.perform(regionResult, new ObjectCollection.Builder().build());
 *
 * // Define region including all matches
 * DefineIncludingMatches defineIncluding = new DefineIncludingMatches(...);
 *
 * // First find all buttons
 * Find find = new Find(...);
 * ActionResult findResult = find.perform(new ActionResult(),
 *     new ObjectCollection.Builder().withImages("button.png").build());
 *
 * // Create region encompassing all found buttons
 * ActionResult encompassingRegion = defineIncluding.perform(findResult,
 *     new ObjectCollection.Builder().build());
 * }</pre>
 *
 * <h2>Use Cases</h2>
 *
 * <ul>
 *   <li>Dynamic region selection based on GUI layout
 *   <li>Debugging automation scripts with visual feedback
 *   <li>Creating adaptive regions that adjust to UI changes
 *   <li>Defining search areas to improve performance
 *   <li>Visual confirmation of automation targets
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Use anchor-based definitions for dynamic layouts
 *   <li>Add margins to regions to account for slight UI variations
 *   <li>Highlight regions during development to verify correctness
 *   <li>Cache defined regions when layout is stable
 *   <li>Combine with state management for context-aware regions
 * </ul>
 *
 * @see io.github.jspinak.brobot.model.Region
 * @see io.github.jspinak.brobot.action.basic.find.Find
 */
package io.github.jspinak.brobot.action.basic.region;
