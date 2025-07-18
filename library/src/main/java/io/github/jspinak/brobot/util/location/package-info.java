/**
 * Location manipulation utilities for coordinate and position calculations.
 * <p>
 * This package provides comprehensive utilities for working with Location objects
 * in the Brobot framework. Locations are fundamental to GUI automation, representing
 * points on the screen either as absolute coordinates or relative positions within
 * regions. The utilities handle conversions, calculations, and transformations while
 * maintaining the dual nature of location representations.
 * 
 * <h2>Core Component</h2>
 * 
 * <h3>LocationUtils</h3>
 * Extensive static utility methods for location operations:
 * <ul>
 *   <li>Conversion between Brobot and SikuliX location formats</li>
 *   <li>Type checking for absolute vs. relative locations</li>
 *   <li>Geometric calculations (opposites, angles, distances)</li>
 *   <li>Transformations to other Brobot model objects</li>
 *   <li>Boundary adjustments and validation</li>
 * </ul>
 * 
 * <h2>Location Types</h2>
 * 
 * <h3>Absolute Locations</h3>
 * Defined by x,y coordinates:
 * <ul>
 *   <li>Direct screen coordinates</li>
 *   <li>Independent of any region</li>
 *   <li>Used for precise positioning</li>
 * </ul>
 * 
 * <h3>Relative Locations</h3>
 * Defined by position within a region:
 * <ul>
 *   <li>Position enum (CENTER, TOP_LEFT, etc.)</li>
 *   <li>Percentage-based positioning</li>
 *   <li>Adapts to region size changes</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Location Conversions</h3>
 * <pre>{@code
 * // Convert to SikuliX format
 * org.sikuli.script.Location sikuliLoc = LocationUtils.getSikuliLocation(location);
 * 
 * // Convert with offset
 * Location offsetLoc = LocationUtils.getSikuliLocation(x, y, offset);
 * }</pre>
 * 
 * <h3>Type Checking</h3>
 * <pre>{@code
 * if (LocationUtils.isDefinedByXY(location)) {
 *     // Handle absolute location
 * } else if (LocationUtils.isDefinedByRegion(location)) {
 *     // Handle relative location
 * }
 * }</pre>
 * 
 * <h3>Geometric Operations</h3>
 * <pre>{@code
 * // Find opposite corner
 * Location opposite = LocationUtils.getOpposite(location);
 * 
 * // Position by angle and distance
 * Location radialPos = LocationUtils.setFromCenter(center, angle, distance);
 * 
 * // Add locations
 * Location combined = LocationUtils.add(loc1, loc2);
 * }</pre>
 * 
 * <h3>Boundary Adjustments</h3>
 * <pre>{@code
 * // Ensure location is within region
 * Location adjusted = LocationUtils.adjustToRegion(location, region);
 * 
 * // Check if location is inside region
 * boolean inside = LocationUtils.isInsideRegion(location, region);
 * }</pre>
 * 
 * <h2>Conversion Operations</h2>
 * 
 * <h3>To Other Types</h3>
 * <pre>{@code
 * // Convert to Match
 * Match match = LocationUtils.toMatch(location);
 * 
 * // Convert to StateLocation
 * StateLocation stateLoc = LocationUtils.toStateLocation(location);
 * 
 * // Convert to ObjectCollection
 * ObjectCollection collection = LocationUtils.asObjectCollection(location);
 * }</pre>
 * 
 * <h2>Position Calculations</h2>
 * The package supports sophisticated position calculations:
 * <ul>
 *   <li>Random positions within regions</li>
 *   <li>Grid-based positioning</li>
 *   <li>Radial positioning using angles</li>
 *   <li>Relative positioning using Position enums</li>
 * </ul>
 * 
 * <h2>Integration with Brobot</h2>
 * LocationUtils integrates with:
 * <ul>
 *   <li>Action system for click/move operations</li>
 *   <li>Region calculations for boundary checks</li>
 *   <li>Match objects for pattern-based locations</li>
 *   <li>State system for persistent locations</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Type Flexibility</b>: Support both absolute and relative locations</li>
 *   <li><b>Null Safety</b>: Graceful handling of null inputs</li>
 *   <li><b>Immutability</b>: Operations return new objects</li>
 *   <li><b>SikuliX Compatibility</b>: Seamless conversion when needed</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * <ul>
 *   <li>Click position calculations</li>
 *   <li>Drag path generation</li>
 *   <li>Random click variations</li>
 *   <li>UI element positioning</li>
 *   <li>Gesture path creation</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.element.Location
 * @see io.github.jspinak.brobot.model.element.Region
 * @see io.github.jspinak.brobot.model.element.Position
 */
package io.github.jspinak.brobot.util.location;