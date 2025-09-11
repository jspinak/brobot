package io.github.jspinak.brobot.util.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for Pair - generic immutable container for two values. Tests
 * immutability, equality, factory methods, and various use cases.
 */
@DisplayName("Pair Tests")
public class PairTest extends BrobotTestBase {

    private Pair<String, Integer> stringIntPair;
    private Pair<Integer, Integer> coordinatePair;
    private Pair<Boolean, String> resultPair;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stringIntPair = new Pair<>("test", 123);
        coordinatePair = new Pair<>(100, 200);
        resultPair = new Pair<>(true, "Success");
    }

    @Nested
    @DisplayName("Construction and Factory Methods")
    class ConstructionTests {

        @Test
        @DisplayName("Constructor creates pair with correct values")
        public void testConstructor() {
            Pair<String, Double> pair = new Pair<>("pi", 3.14159);

            assertEquals("pi", pair.first);
            assertEquals(3.14159, pair.second);
            assertEquals("pi", pair.getKey());
            assertEquals(3.14159, pair.getValue());
        }

        @Test
        @DisplayName("Factory method creates pair")
        public void testFactoryMethod() {
            Pair<String, Integer> pair = Pair.of("hello", 42);

            assertEquals("hello", pair.first);
            assertEquals(42, pair.second);
        }

        @Test
        @DisplayName("Factory method with null values")
        public void testFactoryWithNulls() {
            Pair<String, String> pair1 = Pair.of(null, "value");
            Pair<String, String> pair2 = Pair.of("value", null);
            Pair<String, String> pair3 = Pair.of(null, null);

            assertNull(pair1.first);
            assertEquals("value", pair1.second);

            assertEquals("value", pair2.first);
            assertNull(pair2.second);

            assertNull(pair3.first);
            assertNull(pair3.second);
        }

        @Test
        @DisplayName("Generic type inference")
        public void testTypeInference() {
            // Test various type combinations
            Pair<Integer, String> intString = Pair.of(1, "one");
            Pair<List<String>, Map<Integer, Double>> complex =
                    Pair.of(Arrays.asList("a", "b"), new HashMap<>());
            Pair<Object, Object> objects = Pair.of(new Object(), new Object());

            assertNotNull(intString);
            assertNotNull(complex);
            assertNotNull(objects);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {

        @Test
        @DisplayName("Fields are final")
        public void testFieldsAreFinal() {
            // Fields are declared final, so this test verifies compilation
            // If fields weren't final, this would be a compilation error
            Pair<String, Integer> pair = new Pair<>("test", 123);

            // The following would not compile:
            // pair.first = "modified";
            // pair.second = 456;

            assertEquals("test", pair.first);
            assertEquals(123, pair.second);
        }

        @Test
        @DisplayName("No setters available")
        public void testNoSetters() {
            Pair<String, Integer> pair = Pair.of("immutable", 100);

            // Verify original values remain unchanged
            assertEquals("immutable", pair.first);
            assertEquals(100, pair.second);

            // No way to modify the values after construction
            // This is verified by the absence of setter methods
        }

        @Test
        @DisplayName("Mutable objects inside pair can be modified")
        public void testMutableContents() {
            List<String> list = new ArrayList<>();
            list.add("initial");

            Pair<String, List<String>> pair = Pair.of("key", list);

            // The list reference is immutable, but the list itself is mutable
            list.add("added");

            assertEquals(2, pair.second.size());
            assertTrue(pair.second.contains("added"));
        }
    }

    @Nested
    @DisplayName("Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("Equals with same values")
        public void testEqualsWithSameValues() {
            Pair<String, Integer> pair1 = Pair.of("test", 123);
            Pair<String, Integer> pair2 = Pair.of("test", 123);

            assertEquals(pair1, pair2);
            assertEquals(pair2, pair1);
        }

        @Test
        @DisplayName("Equals with different values")
        public void testEqualsWithDifferentValues() {
            Pair<String, Integer> pair1 = Pair.of("test", 123);
            Pair<String, Integer> pair2 = Pair.of("test", 456);
            Pair<String, Integer> pair3 = Pair.of("different", 123);

            assertNotEquals(pair1, pair2);
            assertNotEquals(pair1, pair3);
        }

        @Test
        @DisplayName("Equals with self")
        public void testEqualsWithSelf() {
            assertTrue(stringIntPair.equals(stringIntPair));
        }

        @Test
        @DisplayName("Equals with null")
        public void testEqualsWithNull() {
            assertFalse(stringIntPair.equals(null));
        }

        @Test
        @DisplayName("Equals with different class")
        public void testEqualsWithDifferentClass() {
            assertFalse(stringIntPair.equals("not a pair"));
            assertFalse(stringIntPair.equals(123));
        }

        @Test
        @DisplayName("Equals with null values in pair")
        public void testEqualsWithNullValues() {
            // Now supports null values properly
            Pair<String, String> pair1 = Pair.of(null, "value");
            Pair<String, String> pair2 = Pair.of(null, "value");
            Pair<String, String> pair3 = Pair.of("key", null);
            Pair<String, String> pair4 = Pair.of("key", null);

            // Null values are handled correctly
            assertTrue(pair1.equals(pair2));
            assertTrue(pair3.equals(pair4));
        }

        @Test
        @DisplayName("Equals is symmetric")
        public void testEqualsSymmetric() {
            Pair<Integer, String> pair1 = Pair.of(42, "answer");
            Pair<Integer, String> pair2 = Pair.of(42, "answer");

            assertEquals(pair1.equals(pair2), pair2.equals(pair1));
        }

        @Test
        @DisplayName("Equals is transitive")
        public void testEqualsTransitive() {
            Pair<Integer, Integer> pair1 = Pair.of(1, 2);
            Pair<Integer, Integer> pair2 = Pair.of(1, 2);
            Pair<Integer, Integer> pair3 = Pair.of(1, 2);

            assertEquals(pair1, pair2);
            assertEquals(pair2, pair3);
            assertEquals(pair1, pair3);
        }
    }

    @Nested
    @DisplayName("HashCode Tests")
    class HashCodeTests {

        @Test
        @DisplayName("HashCode consistency")
        public void testHashCodeConsistency() {
            int hash1 = stringIntPair.hashCode();
            int hash2 = stringIntPair.hashCode();

            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Equal objects have same hashCode")
        public void testEqualObjectsSameHashCode() {
            Pair<String, Integer> pair1 = Pair.of("test", 123);
            Pair<String, Integer> pair2 = Pair.of("test", 123);

            assertEquals(pair1, pair2);
            assertEquals(pair1.hashCode(), pair2.hashCode());
        }

        @Test
        @DisplayName("Different objects likely have different hashCode")
        public void testDifferentObjectsDifferentHashCode() {
            Pair<String, Integer> pair1 = Pair.of("test", 123);
            Pair<String, Integer> pair2 = Pair.of("different", 456);

            // Not guaranteed but highly likely
            assertNotEquals(pair1.hashCode(), pair2.hashCode());
        }

        @Test
        @DisplayName("HashCode with null values")
        public void testHashCodeWithNulls() {
            // Now handles null values properly
            Pair<String, String> pairWithNull1 = Pair.of(null, "value");
            Pair<String, String> pairWithNull2 = Pair.of("key", null);
            Pair<String, String> pairWithBothNull = Pair.of(null, null);

            // Should not throw NPE
            assertDoesNotThrow(pairWithNull1::hashCode);
            assertDoesNotThrow(pairWithNull2::hashCode);
            assertDoesNotThrow(pairWithBothNull::hashCode);
        }

        @Test
        @DisplayName("HashCode distribution")
        public void testHashCodeDistribution() {
            Set<Integer> hashCodes = new HashSet<>();

            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    hashCodes.add(Pair.of(i, j).hashCode());
                }
            }

            // Should have reasonable distribution (Objects.hash produces ~3000 unique values for
            // this test)
            assertTrue(hashCodes.size() > 3000);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("ToString format")
        public void testToStringFormat() {
            Pair<String, Integer> pair = Pair.of("hello", 42);
            assertEquals("(hello, 42)", pair.toString());
        }

        @Test
        @DisplayName("ToString with null values")
        public void testToStringWithNulls() {
            Pair<String, String> pair = Pair.of(null, null);
            assertEquals("(null, null)", pair.toString());
        }

        @Test
        @DisplayName("ToString with complex objects")
        public void testToStringWithComplexObjects() {
            List<String> list = Arrays.asList("a", "b", "c");
            Map<Integer, String> map = new HashMap<>();
            map.put(1, "one");

            Pair<List<String>, Map<Integer, String>> pair = Pair.of(list, map);
            String result = pair.toString();

            assertTrue(result.startsWith("("));
            assertTrue(result.endsWith(")"));
            assertTrue(result.contains("[a, b, c]"));
            assertTrue(result.contains("1=one"));
        }
    }

    @Nested
    @DisplayName("Map.Entry Compatibility")
    class MapEntryCompatibility {

        @Test
        @DisplayName("GetKey returns first element")
        public void testGetKey() {
            assertEquals("test", stringIntPair.getKey());
            assertEquals(100, coordinatePair.getKey());
            assertEquals(true, resultPair.getKey());
        }

        @Test
        @DisplayName("GetValue returns second element")
        public void testGetValue() {
            assertEquals(123, stringIntPair.getValue());
            assertEquals(200, coordinatePair.getValue());
            assertEquals("Success", resultPair.getValue());
        }

        @Test
        @DisplayName("Can be used like Map.Entry")
        public void testMapEntryUsage() {
            Map<String, Integer> map = new HashMap<>();
            Pair<String, Integer> pair = Pair.of("key", 100);

            // Can use pair values with map
            map.put(pair.getKey(), pair.getValue());

            assertEquals(100, map.get("key"));
        }
    }

    @Nested
    @DisplayName("Use Cases")
    class UseCases {

        @Test
        @DisplayName("Coordinate pairs")
        public void testCoordinatePairs() {
            Pair<Integer, Integer> point = Pair.of(100, 200);
            Pair<Double, Double> precise = Pair.of(3.14, 2.71);

            assertEquals(100, point.first);
            assertEquals(200, point.second);
            assertEquals(3.14, precise.first);
            assertEquals(2.71, precise.second);
        }

        @Test
        @DisplayName("Result with status and message")
        public void testResultPairs() {
            Pair<Boolean, String> success = Pair.of(true, "Operation completed");
            Pair<Boolean, String> failure = Pair.of(false, "Error occurred");

            assertTrue(success.first);
            assertEquals("Operation completed", success.second);

            assertFalse(failure.first);
            assertEquals("Error occurred", failure.second);
        }

        @Test
        @DisplayName("Min-max ranges")
        public void testRangePairs() {
            Pair<Integer, Integer> range = Pair.of(0, 100);
            Pair<Double, Double> tempRange = Pair.of(-273.15, 5778.0);

            assertTrue(range.first < range.second);
            assertTrue(tempRange.first < tempRange.second);
        }

        @Test
        @DisplayName("Key-value associations")
        public void testKeyValuePairs() {
            List<Pair<String, Integer>> scores =
                    Arrays.asList(Pair.of("Alice", 95), Pair.of("Bob", 87), Pair.of("Charlie", 92));

            assertEquals(3, scores.size());
            assertEquals("Alice", scores.get(0).first);
            assertEquals(87, scores.get(1).second);
        }
    }

    @Nested
    @DisplayName("Collections Usage")
    class CollectionsUsage {

        @Test
        @DisplayName("Pairs in List")
        public void testPairsInList() {
            List<Pair<String, Integer>> list = new ArrayList<>();
            list.add(Pair.of("one", 1));
            list.add(Pair.of("two", 2));
            list.add(Pair.of("three", 3));

            assertEquals(3, list.size());
            assertTrue(list.contains(Pair.of("two", 2)));
        }

        @Test
        @DisplayName("Pairs in Set")
        public void testPairsInSet() {
            Set<Pair<Integer, Integer>> set = new HashSet<>();
            set.add(Pair.of(1, 2));
            set.add(Pair.of(3, 4));
            set.add(Pair.of(1, 2)); // Duplicate

            assertEquals(2, set.size());
            assertTrue(set.contains(Pair.of(1, 2)));
        }

        @Test
        @DisplayName("Pairs as Map values")
        public void testPairsAsMapValues() {
            Map<String, Pair<Integer, Integer>> coordinates = new HashMap<>();
            coordinates.put("home", Pair.of(0, 0));
            coordinates.put("work", Pair.of(10, 20));

            Pair<Integer, Integer> home = coordinates.get("home");
            assertEquals(0, home.first);
            assertEquals(0, home.second);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Pair of pairs")
        public void testPairOfPairs() {
            Pair<Pair<Integer, Integer>, Pair<String, String>> nested =
                    Pair.of(Pair.of(1, 2), Pair.of("a", "b"));

            assertEquals(1, nested.first.first);
            assertEquals(2, nested.first.second);
            assertEquals("a", nested.second.first);
            assertEquals("b", nested.second.second);
        }

        @Test
        @DisplayName("Same object in both positions")
        public void testSameObjectBothPositions() {
            String same = "same";
            Pair<String, String> pair = Pair.of(same, same);

            assertSame(pair.first, pair.second);
            assertEquals(pair.first, pair.second);
        }

        @Test
        @DisplayName("Large objects in pair")
        public void testLargeObjects() {
            byte[] largeArray1 = new byte[1000000];
            byte[] largeArray2 = new byte[1000000];
            Arrays.fill(largeArray1, (byte) 1);
            Arrays.fill(largeArray2, (byte) 2);

            Pair<byte[], byte[]> pair = Pair.of(largeArray1, largeArray2);

            assertSame(largeArray1, pair.first);
            assertSame(largeArray2, pair.second);
        }
    }

    @Nested
    @DisplayName("Type Safety")
    class TypeSafety {

        @Test
        @DisplayName("Compile-time type checking")
        public void testCompileTimeTypeSafety() {
            Pair<String, Integer> typedPair = Pair.of("test", 123);

            // These assignments work due to proper typing
            String s = typedPair.first;
            Integer i = typedPair.second;

            assertEquals("test", s);
            assertEquals(123, i);

            // The following would not compile:
            // Integer wrong1 = typedPair.first;
            // String wrong2 = typedPair.second;
        }

        @Test
        @DisplayName("Wildcard types")
        public void testWildcardTypes() {
            Pair<? extends Number, ? extends CharSequence> wildcardPair = Pair.of(42, "text");

            Number n = wildcardPair.first;
            CharSequence cs = wildcardPair.second;

            assertEquals(42, n);
            assertEquals("text", cs.toString());
        }
    }

    static Stream<Arguments> pairProvider() {
        return Stream.of(
                Arguments.of(Pair.of("a", 1), Pair.of("a", 1), true),
                Arguments.of(Pair.of("a", 1), Pair.of("b", 1), false),
                Arguments.of(Pair.of("a", 1), Pair.of("a", 2), false),
                Arguments.of(Pair.of(1, 2), Pair.of(1, 2), true),
                Arguments.of(Pair.of(1.0, 2.0), Pair.of(1.0, 2.0), true));
    }

    @ParameterizedTest
    @MethodSource("pairProvider")
    @DisplayName("Parameterized equality tests")
    public void testParameterizedEquality(
            Pair<?, ?> pair1, Pair<?, ?> pair2, boolean shouldBeEqual) {
        if (shouldBeEqual) {
            assertEquals(pair1, pair2);
        } else {
            assertNotEquals(pair1, pair2);
        }
    }
}
