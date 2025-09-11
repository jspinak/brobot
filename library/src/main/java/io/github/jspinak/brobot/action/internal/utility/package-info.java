/**
 * Utility classes and helper components for action operations.
 *
 * <p>This package contains utility classes that provide common functionality used throughout the
 * action framework. These utilities handle specific tasks like success evaluation, configuration
 * management, and coordinate calculations.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.ActionSuccessCriteria}</b> -
 *       Evaluates whether actions have succeeded based on configurable criteria and match results
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.DragCoordinateCalculator}</b> -
 *       Calculates start and end coordinates for drag operations based on various offset and
 *       position strategies
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.utility.ActionTextUtilities}</b> - Text
 *       manipulation and processing utilities specific to action operations
 * </ul>
 *
 * <h2>Utility Categories</h2>
 *
 * <h3>Success Evaluation</h3>
 *
 * <p>The ActionSuccessCriteria utility provides:
 *
 * <ul>
 *   <li>Configurable success criteria (match count, score thresholds)
 *   <li>Multiple evaluation strategies
 *   <li>Detailed failure reasoning
 *   <li>Performance metric integration
 * </ul>
 *
 * <h3>Coordinate Calculations</h3>
 *
 * <p>DragLocation provides:
 *
 * <ul>
 *   <li>Multiple coordinate systems support
 *   <li>Offset calculations (absolute and relative)
 *   <li>Boundary validation and clamping
 *   <li>Smooth path interpolation
 * </ul>
 *
 * <h3>Text Operations</h3>
 *
 * <p>ActionText utilities include:
 *
 * <ul>
 *   <li>Text extraction from action results
 *   <li>Format conversions for different systems
 *   <li>Pattern matching helpers
 *   <li>Encoding and sanitization
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Stateless Functions</b> - Utilities are pure functions where possible
 *   <li><b>Null Safety</b> - Proper handling of null inputs with clear contracts
 *   <li><b>Performance Focus</b> - Optimized for frequent calls in tight loops
 *   <li><b>Clear Contracts</b> - Well-documented behavior and edge cases
 * </ul>
 *
 * <h2>Common Patterns</h2>
 *
 * <ul>
 *   <li><b>Builder Pattern</b> - For complex object construction
 *   <li><b>Fluent Interfaces</b> - For configuration and setup
 *   <li><b>Defensive Copying</b> - To maintain immutability
 *   <li><b>Validation Methods</b> - For input verification
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>All utilities are designed to be thread-safe through:
 *
 * <ul>
 *   <li>Immutable operation on parameters
 *   <li>No shared mutable state
 *   <li>Thread-local storage where needed
 *   <li>Atomic operations for counters
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.action.ActionResult
 */
package io.github.jspinak.brobot.action.internal.utility;
