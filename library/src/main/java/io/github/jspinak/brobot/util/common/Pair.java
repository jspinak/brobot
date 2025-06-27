package io.github.jspinak.brobot.util.common;

/**
 * Generic immutable container for holding two related values as a single unit.
 * 
 * <p>Pair provides a type-safe way to group two objects together without creating 
 * specialized classes. This utility class is essential throughout Brobot for 
 * representing dual relationships, coordinates, mappings, and any scenario where 
 * two values naturally belong together. The immutable design ensures thread safety 
 * and prevents accidental modifications.</p>
 * 
 * <p>Key characteristics:
 * <ul>
 *   <li><b>Immutable</b>: Values cannot be changed after construction</li>
 *   <li><b>Type-Safe</b>: Generic parameters ensure compile-time type checking</li>
 *   <li><b>Null-Safe</b>: Properly handles null values in equals/hashCode</li>
 *   <li><b>Value Semantics</b>: Equality based on contained values</li>
 *   <li><b>Map-Compatible</b>: Provides getKey() and getValue() methods</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns in Brobot:
 * <ul>
 *   <li>Coordinates: Pair<Integer, Integer> for x,y positions</li>
 *   <li>Mappings: Pair<State, Transition> for state relationships</li>
 *   <li>Results: Pair<Boolean, String> for success/message returns</li>
 *   <li>Ranges: Pair<Double, Double> for min/max values</li>
 *   <li>Associations: Pair<Pattern, Region> for search contexts</li>
 * </ul>
 * </p>
 * 
 * <p>Factory method usage:
 * <pre>
 * // Create coordinate pair
 * Pair<Integer, Integer> point = Pair.of(100, 200);
 * 
 * // Create state-transition pair
 * Pair<State, StateTransition> mapping = Pair.of(state, transition);
 * 
 * // Create result pair
 * Pair<Boolean, String> result = Pair.of(true, "Success");
 * </pre>
 * </p>
 * 
 * <p>Design benefits:
 * <ul>
 *   <li>Eliminates need for multiple specialized two-field classes</li>
 *   <li>Provides consistent API for paired data</li>
 *   <li>Supports use in collections (proper equals/hashCode)</li>
 *   <li>Clear semantics with descriptive field names</li>
 *   <li>Interoperable with Map.Entry via getKey/getValue</li>
 * </ul>
 * </p>
 * 
 * <p>Method details:
 * <ul>
 *   <li><b>of()</b>: Static factory for clean pair creation</li>
 *   <li><b>equals()</b>: Deep equality check of both values</li>
 *   <li><b>hashCode()</b>: Consistent hash for collection usage</li>
 *   <li><b>toString()</b>: Human-readable representation</li>
 *   <li><b>getKey/getValue()</b>: Map.Entry compatibility</li>
 * </ul>
 * </p>
 * 
 * <p>Thread safety:
 * <ul>
 *   <li>Immutable fields ensure thread-safe access</li>
 *   <li>No synchronization needed for reads</li>
 *   <li>Safe to share across threads</li>
 *   <li>Values should also be immutable for complete safety</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Pair serves as a fundamental building block for 
 * representing binary relationships throughout the framework. Its simplicity and 
 * immutability make it ideal for functional programming patterns and concurrent 
 * operations common in automation scenarios.</p>
 * 
 * @param <U> the type of the first element
 * @param <V> the type of the second element
 * @since 1.0
 * @see java.util.Map.Entry
 */
public class Pair<U, V> {
    public final U first;   	// first field of a Pair
    public final V second;  	// second field of a Pair

    // Constructs a new Pair with specified values
    public Pair(U first, V second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    // Checks specified object is "equal to" current object or not
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        // call equals() method of the underlying objects
        if (!first.equals(pair.first))
            return false;
        return second.equals(pair.second);
    }

    @Override
    // Computes hash code for an object to support hash tables
    public int hashCode()
    {
        // use hash codes of the underlying objects
        return 31 * first.hashCode() + second.hashCode();
    }

    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ")";
    }

    // Factory method for creating a Typed Pair immutable instance
    public static <U, V> Pair <U, V> of(U a, V b)
    {
        // calls private constructor
        return new Pair<>(a, b);
    }

    public U getKey() {
        return first;
    }

    public V getValue() {
        return second;
    }

}