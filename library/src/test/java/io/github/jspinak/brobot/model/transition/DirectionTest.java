package io.github.jspinak.brobot.model.transition;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the Direction enum which represents directional relationships in state
 * transitions and navigation.
 */
@DisplayName("Direction Enum Tests")
public class DirectionTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should have exactly two direction constants")
    void testEnumConstants() {
        // When
        Direction[] directions = Direction.values();

        // Then
        assertEquals(2, directions.length);
        assertEquals(Direction.TO, directions[0]);
        assertEquals(Direction.FROM, directions[1]);
    }

    @Test
    @DisplayName("Should have correct enum names")
    void testEnumNames() {
        // Then
        assertEquals("TO", Direction.TO.name());
        assertEquals("FROM", Direction.FROM.name());
    }

    @Test
    @DisplayName("Should have correct ordinal values")
    void testOrdinalValues() {
        // Then
        assertEquals(0, Direction.TO.ordinal());
        assertEquals(1, Direction.FROM.ordinal());
    }

    @Test
    @DisplayName("Should parse from string using valueOf")
    void testValueOf() {
        // Then
        assertEquals(Direction.TO, Direction.valueOf("TO"));
        assertEquals(Direction.FROM, Direction.valueOf("FROM"));
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void testValueOfInvalid() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> Direction.valueOf("INVALID"));
        assertThrows(
                IllegalArgumentException.class, () -> Direction.valueOf("to")); // Case sensitive
        assertThrows(IllegalArgumentException.class, () -> Direction.valueOf(""));
        assertThrows(NullPointerException.class, () -> Direction.valueOf(null));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Should handle all enum values")
    void testAllEnumValues(Direction direction) {
        // Then
        assertNotNull(direction);
        assertNotNull(direction.name());
        assertTrue(direction.ordinal() >= 0);
        assertTrue(direction.ordinal() < 2);
    }

    @Test
    @DisplayName("Should work in switch statements")
    void testSwitchStatement() {
        // Given
        Direction direction = Direction.TO;

        // When
        String description =
                switch (direction) {
                    case TO -> "Moving towards target";
                    case FROM -> "Coming from source";
                };

        // Then
        assertEquals("Moving towards target", description);
    }

    @Test
    @DisplayName("Should support enum comparison")
    void testEnumComparison() {
        // Given
        Direction dir1 = Direction.TO;
        Direction dir2 = Direction.TO;
        Direction dir3 = Direction.FROM;

        // Then - Reference equality
        assertSame(dir1, dir2);
        assertNotSame(dir1, dir3);

        // Equality comparison
        assertEquals(dir1, dir2);
        assertNotEquals(dir1, dir3);

        // Comparable
        assertTrue(dir1.compareTo(dir3) < 0);
        assertTrue(dir3.compareTo(dir1) > 0);
        assertEquals(0, dir1.compareTo(dir2));
    }

    @Test
    @DisplayName("Should support collections")
    void testCollections() {
        // Given
        Set<Direction> usedDirections = new HashSet<>();

        // When
        usedDirections.add(Direction.TO);
        usedDirections.add(Direction.FROM);
        usedDirections.add(Direction.TO); // Duplicate

        // Then
        assertEquals(2, usedDirections.size());
        assertTrue(usedDirections.contains(Direction.TO));
        assertTrue(usedDirections.contains(Direction.FROM));
    }

    @Test
    @DisplayName("Should provide consistent toString")
    void testToString() {
        // Then
        assertEquals("TO", Direction.TO.toString());
        assertEquals("FROM", Direction.FROM.toString());
    }

    @TestFactory
    @DisplayName("Direction usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
                dynamicTest(
                        "State transition direction",
                        () -> {
                            // Simulate state transition
                            Direction transitionDirection = Direction.TO;
                            assertEquals(Direction.TO, transitionDirection);
                            assertEquals("TO", transitionDirection.name());
                        }),
                dynamicTest(
                        "Reverse navigation",
                        () -> {
                            // Simulate reverse navigation
                            Direction forward = Direction.TO;
                            Direction reverse = Direction.FROM;

                            assertNotEquals(forward, reverse);
                            assertTrue(forward.ordinal() < reverse.ordinal());
                        }),
                dynamicTest(
                        "Bidirectional relationship",
                        () -> {
                            // Track both directions
                            Map<Direction, String> connections = new EnumMap<>(Direction.class);
                            connections.put(Direction.TO, "TargetState");
                            connections.put(Direction.FROM, "SourceState");

                            assertEquals(2, connections.size());
                            assertEquals("TargetState", connections.get(Direction.TO));
                            assertEquals("SourceState", connections.get(Direction.FROM));
                        }),
                dynamicTest(
                        "Path traversal direction",
                        () -> {
                            // Simulate path traversal
                            List<Direction> path =
                                    Arrays.asList(Direction.TO, Direction.TO, Direction.FROM);

                            assertEquals(3, path.size());
                            assertEquals(Direction.FROM, path.get(2));
                        }),
                dynamicTest(
                        "Query filter by direction",
                        () -> {
                            // Simulate filtering transitions
                            Map<String, Direction> transitions = new HashMap<>();
                            transitions.put("Login->Home", Direction.TO);
                            transitions.put("Home->Settings", Direction.TO);
                            transitions.put("Settings->Home", Direction.FROM);

                            long toCount =
                                    transitions.values().stream()
                                            .filter(d -> d == Direction.TO)
                                            .count();

                            assertEquals(2, toCount);
                        }));
    }

    @Test
    @DisplayName("Should handle array operations")
    void testArrayOperations() {
        // Given
        Direction[] directions = Direction.values();

        // When
        Direction[] copy = Arrays.copyOf(directions, directions.length);

        // Then
        assertArrayEquals(directions, copy);
        assertEquals(2, copy.length);

        // Modify copy doesn't affect original
        copy[0] = Direction.FROM;
        assertEquals(Direction.TO, directions[0]);
        assertEquals(Direction.FROM, copy[0]);
    }

    @Test
    @DisplayName("Should demonstrate enum singleton pattern")
    void testEnumSingleton() {
        // Enum instances are singletons
        Direction dir1 = Direction.valueOf("TO");
        Direction dir2 = Direction.valueOf("TO");
        Direction dir3 = Direction.TO;

        // All references point to same instance
        assertSame(dir1, dir2);
        assertSame(dir2, dir3);
        assertSame(dir1, dir3);

        // Same for array access
        assertSame(Direction.TO, Direction.values()[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TO", "FROM"})
    @DisplayName("Should round-trip through string conversion")
    void testStringRoundTrip(String directionName) {
        // Given
        Direction original = Direction.valueOf(directionName);

        // When
        String asString = original.toString();
        Direction reconstructed = Direction.valueOf(asString);

        // Then
        assertEquals(original, reconstructed);
        assertSame(original, reconstructed); // Same instance
        assertEquals(directionName, asString);
    }

    @Test
    @DisplayName("Should iterate through all values")
    void testIteration() {
        // Given
        int count = 0;
        Set<String> names = new HashSet<>();

        // When
        for (Direction direction : Direction.values()) {
            count++;
            names.add(direction.name());
        }

        // Then
        assertEquals(2, count);
        assertEquals(2, names.size());
        assertTrue(names.contains("TO"));
        assertTrue(names.contains("FROM"));
    }

    @Test
    @DisplayName("Should handle null-safe operations")
    void testNullSafety() {
        // Given
        Direction direction = Direction.TO;
        Direction nullDirection = null;

        // Then
        assertNotNull(direction);
        assertNull(nullDirection);

        // Comparison with null
        assertNotEquals(direction, nullDirection);
        assertNotEquals(null, direction);

        // Can't call methods on null
        assertThrows(NullPointerException.class, () -> nullDirection.name());
    }

    @Test
    @DisplayName("Should provide type safety")
    void testTypeSafety() {
        // Direction provides compile-time type safety
        Direction direction = Direction.TO;

        // Type checking
        assertTrue(direction instanceof Direction);
        assertTrue(direction instanceof Enum);
        assertEquals(Direction.class, direction.getDeclaringClass());
    }

    @Test
    @DisplayName("Should support functional programming patterns")
    void testFunctionalPatterns() {
        // Given
        Direction[] directions = Direction.values();

        // When - Map to descriptions
        String[] descriptions =
                Arrays.stream(directions)
                        .map(d -> d == Direction.TO ? "forward" : "backward")
                        .toArray(String[]::new);

        // Then
        assertArrayEquals(new String[] {"forward", "backward"}, descriptions);
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void testHashCode() {
        // Given
        Direction dir1 = Direction.TO;
        Direction dir2 = Direction.TO;
        Direction dir3 = Direction.FROM;

        // Then
        assertEquals(dir1.hashCode(), dir2.hashCode());
        assertNotEquals(dir1.hashCode(), dir3.hashCode()); // Usually different

        // HashCode consistency
        int hash1 = dir1.hashCode();
        int hash2 = dir1.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should work with EnumMap for performance")
    void testEnumMap() {
        // EnumMap is optimized for enum keys
        EnumMap<Direction, Integer> counts = new EnumMap<>(Direction.class);

        // When
        counts.put(Direction.TO, 5);
        counts.put(Direction.FROM, 3);

        // Then
        assertEquals(2, counts.size());
        assertEquals(5, counts.get(Direction.TO));
        assertEquals(3, counts.get(Direction.FROM));

        // EnumMap maintains enum order
        Iterator<Direction> iterator = counts.keySet().iterator();
        assertEquals(Direction.TO, iterator.next());
        assertEquals(Direction.FROM, iterator.next());
    }

    @Test
    @DisplayName("Should work with EnumSet for performance")
    void testEnumSet() {
        // EnumSet is optimized for enum values
        EnumSet<Direction> allDirections = EnumSet.allOf(Direction.class);
        EnumSet<Direction> noDirections = EnumSet.noneOf(Direction.class);
        EnumSet<Direction> toOnly = EnumSet.of(Direction.TO);

        // Then
        assertEquals(2, allDirections.size());
        assertEquals(0, noDirections.size());
        assertEquals(1, toOnly.size());

        assertTrue(allDirections.contains(Direction.TO));
        assertTrue(allDirections.contains(Direction.FROM));
        assertFalse(noDirections.contains(Direction.TO));
        assertTrue(toOnly.contains(Direction.TO));
        assertFalse(toOnly.contains(Direction.FROM));
    }

    @Test
    @DisplayName("Should demonstrate opposite direction logic")
    void testOppositeDirection() {
        // Given - Using lambda for opposite logic
        java.util.function.Function<Direction, Direction> getOpposite =
                dir -> dir == Direction.TO ? Direction.FROM : Direction.TO;

        // Then
        assertEquals(Direction.FROM, getOpposite.apply(Direction.TO));
        assertEquals(Direction.TO, getOpposite.apply(Direction.FROM));

        // Double opposite returns original
        assertEquals(Direction.TO, getOpposite.apply(getOpposite.apply(Direction.TO)));
    }

    @Test
    @DisplayName("Should follow enum best practices")
    void testEnumBestPractices() {
        // Verify enum follows best practices

        // 1. Enum is final (can't be extended)
        assertTrue(java.lang.reflect.Modifier.isFinal(Direction.class.getModifiers()));

        // 2. Has proper enum methods
        assertDoesNotThrow(() -> Direction.valueOf("TO"));
        assertDoesNotThrow(() -> Direction.values());

        // 3. Implements Comparable
        assertTrue(Comparable.class.isAssignableFrom(Direction.class));

        // 4. Implements Serializable
        assertTrue(java.io.Serializable.class.isAssignableFrom(Direction.class));
    }

    @Test
    @DisplayName("Should represent semantic relationships")
    void testSemanticRelationships() {
        // Direction represents semantic relationships

        // TO implies forward/target
        Direction toTarget = Direction.TO;
        assertEquals("TO", toTarget.name());

        // FROM implies backward/source
        Direction fromSource = Direction.FROM;
        assertEquals("FROM", fromSource.name());

        // They are mutually exclusive
        assertNotEquals(toTarget, fromSource);

        // Complete the direction space
        Set<Direction> allDirections = EnumSet.allOf(Direction.class);
        assertEquals(2, allDirections.size());
    }
}
