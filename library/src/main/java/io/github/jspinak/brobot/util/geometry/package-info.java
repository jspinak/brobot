/**
 * Geometric utilities for spatial analysis and calculations in GUI automation.
 *
 * <p>This package provides comprehensive geometric tools for analyzing spatial relationships,
 * calculating distances and angles, tracking movements, and clustering visual elements. These
 * utilities are essential for Brobot's ability to understand and interact with graphical
 * interfaces, particularly in gaming and complex UI scenarios.
 *
 * <h2>Core Components</h2>
 *
 * <h3>DistanceCalculator</h3>
 *
 * Fundamental geometric calculations:
 *
 * <ul>
 *   <li>Euclidean distance between points, locations, and matches
 *   <li>Angle calculations with normalization (-180° to 180°)
 *   <li>Vector operations and transformations
 *   <li>Sector-based angle analysis
 *   <li>Screen coordinate system aware (Y increases downward)
 * </ul>
 *
 * <h3>MovementAnalyzer</h3>
 *
 * Movement pattern detection and analysis:
 *
 * <ul>
 *   <li>Displacement vector calculation between object states
 *   <li>Voting algorithm for consensus movement detection
 *   <li>Tolerance handling for measurement variations
 *   <li>Multi-match analysis for improved accuracy
 * </ul>
 *
 * <h3>GridBasedClusterer</h3>
 *
 * Fast spatial clustering algorithm:
 *
 * <ul>
 *   <li>O(n) time complexity for efficient processing
 *   <li>Overlapping grid partitioning prevents boundary splits
 *   <li>Density-based region identification
 *   <li>No need for predetermined cluster centers
 * </ul>
 *
 * <h3>Sector</h3>
 *
 * Angular sector representation:
 *
 * <ul>
 *   <li>Arc representation with automatic optimization
 *   <li>Angle normalization for any input range
 *   <li>Clockwise convention from left to right angle
 *   <li>Support for full 360° rotation
 * </ul>
 *
 * <h2>Coordinate System</h2>
 *
 * The package uses standard screen coordinates:
 *
 * <ul>
 *   <li>Origin (0,0) at top-left corner
 *   <li>X increases rightward
 *   <li>Y increases downward
 *   <li>Angles: 0° = East, 90° = North, 180° = West, 270° = South
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Distance Calculations</h3>
 *
 * <pre>{@code
 * // Calculate distance between UI elements
 * double dist = DistanceCalculator.euclideanDistance(button1.getLocation(), button2.getLocation());
 *
 * // Get angle between points
 * double angle = DistanceCalculator.getAngleInDegrees(center, target);
 * }</pre>
 *
 * <h3>Movement Tracking</h3>
 *
 * <pre>{@code
 * // Track map scrolling in a game
 * MovementAnalyzer movement = new MovementAnalyzer();
 * Location displacement = movement.getMovement(beforeMatches, afterMatches);
 * }</pre>
 *
 * <h3>Spatial Clustering</h3>
 *
 * <pre>{@code
 * // Find dense groups of UI elements
 * GridBasedClusterer cluster = new GridBasedClusterer(3, 3);
 * List<Region> denseRegions = cluster.cluster(searchRegion, matches);
 * }</pre>
 *
 * <h3>Angular Sectors</h3>
 *
 * <pre>{@code
 * // Define a sector for radial menu
 * Sector menuSection = new Sector(45, 135); // 90° arc
 * boolean inSector = menuSection.contains(clickAngle);
 * }</pre>
 *
 * <h2>Common Applications</h2>
 *
 * <h3>Game Automation</h3>
 *
 * <ul>
 *   <li>Map navigation and scrolling detection
 *   <li>Camera movement tracking
 *   <li>Radial menu interaction
 *   <li>Minimap analysis
 * </ul>
 *
 * <h3>UI Testing</h3>
 *
 * <ul>
 *   <li>Element proximity analysis
 *   <li>Gesture path validation
 *   <li>Layout verification
 *   <li>Scroll behavior testing
 * </ul>
 *
 * <h3>Pattern Recognition</h3>
 *
 * <ul>
 *   <li>Spatial grouping of similar elements
 *   <li>Hotspot identification
 *   <li>Movement pattern analysis
 *   <li>Layout structure detection
 * </ul>
 *
 * <h2>Performance Characteristics</h2>
 *
 * <ul>
 *   <li>Distance calculations: O(1) constant time
 *   <li>Movement detection: O(n×m) for n before and m after matches
 *   <li>GridBasedClusterer: O(n) linear time clustering
 *   <li>All operations optimized for real-time automation
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Efficiency</b>: Algorithms optimized for real-time processing
 *   <li><b>Accuracy</b>: Proper handling of edge cases and normalization
 *   <li><b>Flexibility</b>: Support for various coordinate systems and units
 *   <li><b>Integration</b>: Seamless work with Brobot's model classes
 * </ul>
 *
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.location
 * @see io.github.jspinak.brobot.model.region
 */
package io.github.jspinak.brobot.util.geometry;
