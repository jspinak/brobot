/**
 * Region manipulation utilities for spatial operations and search area management.
 *
 * <p>This package provides comprehensive utilities for working with Region and SearchRegions
 * objects, which are fundamental to GUI automation in Brobot. These utilities handle geometric
 * calculations, spatial relationships, grid operations, and safe manipulation of search areas used
 * throughout the framework.
 *
 * <h2>Core Components</h2>
 *
 * <h3>RegionUtils</h3>
 *
 * Extensive geometric and spatial operations on regions:
 *
 * <ul>
 *   <li>Type conversions between Region, Match, Location, and Rect
 *   <li>Overlap detection and intersection calculations
 *   <li>Containment checks and boundary operations
 *   <li>Grid division for systematic processing
 *   <li>Set operations (union, subtraction)
 * </ul>
 *
 * <h3>SearchRegionsUtils</h3>
 *
 * Safe manipulation of SearchRegions collections:
 *
 * <ul>
 *   <li>Defensive copying to handle immutable lists
 *   <li>Merging regions from multiple sources
 *   <li>Random region selection for testing
 *   <li>Deep cloning for independent copies
 *   <li>Debug string generation
 * </ul>
 *
 * <h2>Spatial Operations</h2>
 *
 * <h3>Overlap Detection</h3>
 *
 * <pre>{@code
 * // Check if two regions overlap
 * boolean overlapping = RegionUtils.overlaps(region1, region2);
 *
 * // Get the overlapping area
 * Region overlap = RegionUtils.getOverlappingRegion(region1, region2);
 * }</pre>
 *
 * <h3>Containment Checks</h3>
 *
 * <pre>{@code
 * // Check if one region contains another
 * boolean inside = RegionUtils.contains(outer, inner);
 *
 * // Check if location is within region
 * boolean inBounds = RegionUtils.contains(region, location);
 * }</pre>
 *
 * <h3>Set Operations</h3>
 *
 * <pre>{@code
 * // Union multiple regions
 * Region combined = RegionUtils.getUnion(region1, region2);
 *
 * // Subtract one region from another
 * List<Region> remainder = RegionUtils.minus(base, subtracted);
 * }</pre>
 *
 * <h2>Grid Operations</h2>
 *
 * The package supports dividing regions into grids for systematic processing:
 *
 * <h3>Grid Division</h3>
 *
 * <pre>{@code
 * // Divide region into 3x3 grid
 * List<List<Region>> grid = RegionUtils.divideIntoGrid(region, 3, 3);
 *
 * // Get specific cell
 * Region cell = grid.get(row).get(column);
 * }</pre>
 *
 * <h3>Grid Mapping</h3>
 *
 * <pre>{@code
 * // Map location to grid cell
 * int cellIndex = RegionUtils.getCellIndex(location, region, gridSize);
 *
 * // Convert between grid coordinates
 * Point gridCoord = RegionUtils.toGridCoordinate(location, region, rows, cols);
 * }</pre>
 *
 * <h2>SearchRegions Management</h2>
 *
 * <h3>Safe List Handling</h3>
 *
 * <pre>{@code
 * // Create mutable copy for safe modification
 * SearchRegions mutable = SearchRegionsUtils.createMutableCopy(searchRegions);
 *
 * // Merge multiple search regions
 * SearchRegions merged = SearchRegionsUtils.merge(regions1, regions2);
 * }</pre>
 *
 * <h3>Random Selection</h3>
 *
 * <pre>{@code
 * // Select random region for testing
 * Region random = SearchRegionsUtils.getRandomRegion(searchRegions);
 *
 * // Get random subset
 * List<Region> subset = SearchRegionsUtils.getRandomSubset(searchRegions, count);
 * }</pre>
 *
 * <h2>Common Use Cases</h2>
 *
 * <h3>Search Area Definition</h3>
 *
 * <ul>
 *   <li>Defining clickable areas in UI
 *   <li>Creating exclusion zones
 *   <li>Limiting pattern search scope
 * </ul>
 *
 * <h3>Spatial Analysis</h3>
 *
 * <ul>
 *   <li>Finding overlapping UI elements
 *   <li>Calculating relative positions
 *   <li>Boundary collision detection
 * </ul>
 *
 * <h3>Grid-Based Processing</h3>
 *
 * <ul>
 *   <li>Systematic screen scanning
 *   <li>Tile-based game automation
 *   <li>Table cell navigation
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Immutability</b>: Operations return new objects, preserving inputs
 *   <li><b>Defensive Copying</b>: Protection against JSON deserialization issues
 *   <li><b>Null Safety</b>: Graceful handling with fallback to empty regions
 *   <li><b>Thread Safety</b>: Stateless methods except for shared Random instance
 * </ul>
 *
 * <h2>Integration Notes</h2>
 *
 * <ul>
 *   <li>SikuliX compatibility for raster operations (5x5 minimum grid)
 *   <li>Works with Brobot's pattern matching system
 *   <li>Supports both absolute and relative positioning
 *   <li>Handles coordinate system transformations
 * </ul>
 *
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.element.Region
 * @see io.github.jspinak.brobot.model.region.SearchRegions
 */
package io.github.jspinak.brobot.util.region;
