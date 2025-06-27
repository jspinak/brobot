/**
 * Utility classes and helper components for action operations.
 * 
 * <p>This package contains utility classes that provide common functionality
 * used throughout the action framework. These utilities handle specific tasks
 * like success evaluation, configuration management, and coordinate calculations.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria}</b> - 
 *       Evaluates whether actions have succeeded based on configurable criteria
 *       and match results</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.CopyActionOptions}</b> - 
 *       Provides deep cloning and modification utilities for ActionOptions,
 *       ensuring immutability patterns</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.DragCoordinateCalculator}</b> - 
 *       Calculates start and end coordinates for drag operations based on
 *       various offset and position strategies</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.ActionTextUtilities}</b> - 
 *       Text manipulation and processing utilities specific to action operations</li>
 * </ul>
 * 
 * <h2>Utility Categories</h2>
 * 
 * <h3>Success Evaluation</h3>
 * <p>The ActionSuccessCriteria utility provides:</p>
 * <ul>
 *   <li>Configurable success criteria (match count, score thresholds)</li>
 *   <li>Multiple evaluation strategies</li>
 *   <li>Detailed failure reasoning</li>
 *   <li>Performance metric integration</li>
 * </ul>
 * 
 * <h3>Configuration Management</h3>
 * <p>CopyActionOptions handles:</p>
 * <ul>
 *   <li>Deep cloning of complex configuration objects</li>
 *   <li>Safe modification without side effects</li>
 *   <li>Merging of configuration overlays</li>
 *   <li>Validation of configuration consistency</li>
 * </ul>
 * 
 * <h3>Coordinate Calculations</h3>
 * <p>DragLocation provides:</p>
 * <ul>
 *   <li>Multiple coordinate systems support</li>
 *   <li>Offset calculations (absolute and relative)</li>
 *   <li>Boundary validation and clamping</li>
 *   <li>Smooth path interpolation</li>
 * </ul>
 * 
 * <h3>Text Operations</h3>
 * <p>ActionText utilities include:</p>
 * <ul>
 *   <li>Text extraction from action results</li>
 *   <li>Format conversions for different systems</li>
 *   <li>Pattern matching helpers</li>
 *   <li>Encoding and sanitization</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><b>Stateless Functions</b> - Utilities are pure functions where possible</li>
 *   <li><b>Null Safety</b> - Proper handling of null inputs with clear contracts</li>
 *   <li><b>Performance Focus</b> - Optimized for frequent calls in tight loops</li>
 *   <li><b>Clear Contracts</b> - Well-documented behavior and edge cases</li>
 * </ul>
 * 
 * <h2>Common Patterns</h2>
 * 
 * <ul>
 *   <li><b>Builder Pattern</b> - For complex object construction</li>
 *   <li><b>Fluent Interfaces</b> - For configuration and setup</li>
 *   <li><b>Defensive Copying</b> - To maintain immutability</li>
 *   <li><b>Validation Methods</b> - For input verification</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * 
 * <p>All utilities are designed to be thread-safe through:</p>
 * <ul>
 *   <li>Immutable operation on parameters</li>
 *   <li>No shared mutable state</li>
 *   <li>Thread-local storage where needed</li>
 *   <li>Atomic operations for counters</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.action.ActionResult
 */
package io.github.jspinak.brobot.action.internal.utility;