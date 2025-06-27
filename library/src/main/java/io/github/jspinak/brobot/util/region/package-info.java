/**
 * Region manipulation utilities for spatial operations and search area management.
 * <p>
 * This package provides comprehensive utilities for working with Region and SearchRegions
 * objects, which are fundamental to GUI automation in Brobot. These utilities handle
 * geometric calculations, spatial relationships, grid operations, and safe manipulation
 * of search areas used throughout the framework.
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>RegionUtils</h3>
 * Extensive geometric and spatial operations on regions:
 * <ul>
 *   <li>Type conversions between Region, Match, Location, and Rect</li>
 *   <li>Overlap detection and intersection calculations</li>
 *   <li>Containment checks and boundary operations</li>
 *   <li>Grid division for systematic processing</li>
 *   <li>Set operations (union, subtraction)</li>
 * </ul>
 * 
 * <h3>SearchRegionsUtils</h3>
 * Safe manipulation of SearchRegions collections:
 * <ul>
 *   <li>Defensive copying to handle immutable lists</li>
 *   <li>Merging regions from multiple sources</li>
 *   <li>Random region selection for testing</li>
 *   <li>Deep cloning for independent copies</li>
 *   <li>Debug string generation</li>
 * </ul>
 * 
 * <h2>Spatial Operations</h2>
 * 
 * <h3>Overlap Detection</h3>
 * <pre>{@code
 * // Check if two regions overlap
 * boolean overlapping = RegionUtils.overlaps(region1, region2);
 * 
 * // Get the overlapping area
 * Region overlap = RegionUtils.getOverlappingRegion(region1, region2);
 * }</pre>
 * 
 * <h3>Containment Checks</h3>
 * <pre>{@code
 * // Check if one region contains another
 * boolean inside = RegionUtils.contains(outer, inner);
 * 
 * // Check if location is within region
 * boolean inBounds = RegionUtils.contains(region, location);
 * }</pre>
 * 
 * <h3>Set Operations</h3>
 * <pre>{@code
 * // Union multiple regions
 * Region combined = RegionUtils.getUnion(region1, region2);
 * 
 * // Subtract one region from another
 * List<Region> remainder = RegionUtils.minus(base, subtracted);
 * }</pre>
 * 
 * <h2>Grid Operations</h2>
 * The package supports dividing regions into grids for systematic processing:
 * 
 * <h3>Grid Division</h3>
 * <pre>{@code
 * // Divide region into 3x3 grid
 * List<List<Region>> grid = RegionUtils.divideIntoGrid(region, 3, 3);
 * 
 * // Get specific cell
 * Region cell = grid.get(row).get(column);
 * }</pre>
 * 
 * <h3>Grid Mapping</h3>
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
 * <pre>{@code
 * // Create mutable copy for safe modification
 * SearchRegions mutable = SearchRegionsUtils.createMutableCopy(searchRegions);
 * 
 * // Merge multiple search regions
 * SearchRegions merged = SearchRegionsUtils.merge(regions1, regions2);
 * }</pre>
 * 
 * <h3>Random Selection</h3>
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
 * <ul>
 *   <li>Defining clickable areas in UI</li>
 *   <li>Creating exclusion zones</li>
 *   <li>Limiting pattern search scope</li>
 * </ul>
 * 
 * <h3>Spatial Analysis</h3>
 * <ul>
 *   <li>Finding overlapping UI elements</li>
 *   <li>Calculating relative positions</li>
 *   <li>Boundary collision detection</li>
 * </ul>
 * 
 * <h3>Grid-Based Processing</h3>
 * <ul>
 *   <li>Systematic screen scanning</li>
 *   <li>Tile-based game automation</li>
 *   <li>Table cell navigation</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Immutability</b>: Operations return new objects, preserving inputs</li>
 *   <li><b>Defensive Copying</b>: Protection against JSON deserialization issues</li>
 *   <li><b>Null Safety</b>: Graceful handling with fallback to empty regions</li>
 *   <li><b>Thread Safety</b>: Stateless methods except for shared Random instance</li>
 * </ul>
 * 
 * <h2>Integration Notes</h2>
 * <ul>
 *   <li>SikuliX compatibility for raster operations (5x5 minimum grid)</li>
 *   <li>Works with Brobot's pattern matching system</li>
 *   <li>Supports both absolute and relative positioning</li>
 *   <li>Handles coordinate system transformations</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.element.Region
 * @see io.github.jspinak.brobot.model.region.SearchRegions
 */
package io.github.jspinak.brobot.util.region;