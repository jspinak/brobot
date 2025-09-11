/**
 * Internal utilities for screen capture and region definition operations.
 *
 * <p>This package contains internal support classes that handle the low-level details of capturing
 * screen regions, defining boundaries, and managing capture-related operations. These utilities are
 * used by the public capture and region definition actions to perform their core functionality.
 *
 * <h2>Key Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.capture.RegionDefinitionHelper}</b> -
 *       Common utilities for region definition operations
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.capture.DefinedBorders}</b> - Manages
 *       border definitions and constraints for regions
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.capture.AnchorRegion}</b> - Calculates
 *       regions based on anchor points and relationships
 * </ul>
 *
 * <h2>Region Definition Support</h2>
 *
 * <h3>Coordinate Calculations</h3>
 *
 * <ul>
 *   <li>Convert between different coordinate systems
 *   <li>Calculate bounding boxes from multiple points
 *   <li>Apply offsets and margins to regions
 *   <li>Ensure regions stay within screen bounds
 * </ul>
 *
 * <h3>Anchor-based Calculations</h3>
 *
 * <ul>
 *   <li>Find regions between anchor points
 *   <li>Calculate relative positions from anchors
 *   <li>Expand regions to include all anchors
 *   <li>Define exclusion zones around anchors
 * </ul>
 *
 * <h3>Border Management</h3>
 *
 * <ul>
 *   <li>Define fixed borders for regions
 *   <li>Calculate dynamic borders based on content
 *   <li>Apply border constraints during region operations
 *   <li>Handle border intersections and overlaps
 * </ul>
 *
 * <h2>Capture Utilities</h2>
 *
 * <ul>
 *   <li><b>Screen Capture</b> - Low-level screen grabbing operations
 *   <li><b>Region Validation</b> - Ensure regions are valid and within bounds
 *   <li><b>Coordinate Transformation</b> - Handle different coordinate spaces
 *   <li><b>Image Cropping</b> - Extract specific regions from captures
 * </ul>
 *
 * <h2>Common Operations</h2>
 *
 * <h3>Region Expansion/Contraction</h3>
 *
 * <pre>{@code
 * // Internal utility usage (not public API)
 * Region original = new Region(100, 100, 200, 150);
 * Region expanded = DefineHelper.expandRegion(original, 10); // 10 pixel margin
 * Region contracted = DefineHelper.contractRegion(original, 5); // 5 pixel inset
 * }</pre>
 *
 * <h3>Anchor Calculations</h3>
 *
 * <pre>{@code
 * // Calculate region between anchors
 * List<Match> anchors = Arrays.asList(topLeft, bottomRight);
 * Region between = AnchorRegion.calculateBetween(anchors);
 *
 * // Calculate region including all anchors with margin
 * Region including = AnchorRegion.calculateIncluding(anchors, 20);
 * }</pre>
 *
 * <h3>Border Constraints</h3>
 *
 * <pre>{@code
 * // Apply border constraints to region
 * DefinedBorders borders = new DefinedBorders(10, 10, 50, 50); // margins
 * Region constrained = borders.constrainRegion(originalRegion);
 * }</pre>
 *
 * <h2>Integration with Public API</h2>
 *
 * <p>These internal utilities support the public capture actions:
 *
 * <ul>
 *   <li>{@code DefineRegion} uses these for basic region operations
 *   <li>{@code DefineInsideAnchors} uses anchor calculations
 *   <li>{@code DefineWithMatch} uses match-based region calculations
 *   <li>{@code Highlight} uses capture utilities for visual feedback
 * </ul>
 *
 * <h2>Performance Optimizations</h2>
 *
 * <ul>
 *   <li>Cached screen dimensions to avoid repeated queries
 *   <li>Efficient boundary checking algorithms
 *   <li>Minimal object allocation in calculation paths
 *   <li>Reusable capture buffers where possible
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <ul>
 *   <li>Graceful handling of invalid regions
 *   <li>Automatic clamping to screen boundaries
 *   <li>Clear error messages for debugging
 *   <li>Fallback strategies for edge cases
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.basic.region.DefineRegion
 * @see io.github.jspinak.brobot.action.basic.region.DefineInsideAnchors
 * @see io.github.jspinak.brobot.model.Region
 */
package io.github.jspinak.brobot.action.internal.capture;
