/**
 * Generic data structures and common utilities used throughout the Brobot framework.
 *
 * <p>This package provides fundamental building blocks that support various operations across the
 * framework. Currently focused on generic containers, this package may expand to include other
 * common utilities as the framework evolves.
 *
 * <h2>Core Components</h2>
 *
 * <h3>Pair</h3>
 *
 * An immutable generic container for holding two related values:
 *
 * <ul>
 *   <li>Type-safe grouping of two objects
 *   <li>Immutable design ensures thread safety
 *   <li>Value semantics with proper equals/hashCode
 *   <li>Factory method pattern for easy instantiation
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>Coordinate Representation</h3>
 *
 * <pre>{@code
 * Pair<Integer, Integer> mousePosition = Pair.of(x, y);
 * Pair<Double, Double> percentageCoords = Pair.of(0.5, 0.75);
 * }</pre>
 *
 * <h3>Result Handling</h3>
 *
 * <pre>{@code
 * Pair<Boolean, String> operationResult = Pair.of(success, message);
 * if (operationResult.getFirst()) {
 *     log.info("Operation succeeded: {}", operationResult.getSecond());
 * }
 * }</pre>
 *
 * <h3>State Relationships</h3>
 *
 * <pre>{@code
 * Pair<State, StateTransition> stateMapping = Pair.of(fromState, transition);
 * Map<String, Pair<Pattern, Region>> searchContexts = new HashMap<>();
 * }</pre>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Immutability</b>: All data structures are immutable by default
 *   <li><b>Type Safety</b>: Extensive use of generics for compile-time safety
 *   <li><b>Value Semantics</b>: Objects compared by value, not reference
 *   <li><b>Null Handling</b>: Graceful handling of null values
 * </ul>
 *
 * <h2>Integration with Brobot</h2>
 *
 * Common utilities in this package are used extensively for:
 *
 * <ul>
 *   <li>Passing multiple return values from methods
 *   <li>Representing relationships between domain objects
 *   <li>Storing coordinate pairs and ranges
 *   <li>Grouping related configuration values
 * </ul>
 *
 * <h2>Future Expansion</h2>
 *
 * Potential additions to this package might include:
 *
 * <ul>
 *   <li>Triple - For three-value containers
 *   <li>Either - For representing alternative outcomes
 *   <li>Optional extensions - Enhanced Optional operations
 *   <li>Collection utilities - Common collection operations
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * All classes in this package are designed to be thread-safe through immutability. This makes them
 * suitable for use in concurrent automation scenarios without additional synchronization.
 *
 * @since 1.0.0
 * @see io.github.jspinak.brobot.util
 */
package io.github.jspinak.brobot.util.common;
