package io.github.jspinak.brobot.model.state.special;

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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive tests for the SpecialStateType enum which defines special state types with reserved
 * negative IDs in the Brobot framework.
 */
@DisplayName("SpecialStateType Enum Tests")
@DisabledInCI
public class SpecialStateTypeTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should have exactly five special state types")
    void testEnumConstants() {
        // When
        SpecialStateType[] types = SpecialStateType.values();

        // Then
        assertEquals(5, types.length);
        assertEquals(SpecialStateType.UNKNOWN, types[0]);
        assertEquals(SpecialStateType.PREVIOUS, types[1]);
        assertEquals(SpecialStateType.CURRENT, types[2]);
        assertEquals(SpecialStateType.EXPECTED, types[3]);
        assertEquals(SpecialStateType.NULL, types[4]);
    }

    @Test
    @DisplayName("Should have correct negative IDs")
    void testNegativeIds() {
        // Then
        assertEquals(-1L, SpecialStateType.UNKNOWN.getId());
        assertEquals(-2L, SpecialStateType.PREVIOUS.getId());
        assertEquals(-3L, SpecialStateType.CURRENT.getId());
        assertEquals(-4L, SpecialStateType.EXPECTED.getId());
        assertEquals(-5L, SpecialStateType.NULL.getId());
    }

    @Test
    @DisplayName("Should have unique IDs")
    void testUniqueIds() {
        // Given
        Set<Long> ids = new HashSet<>();

        // When
        for (SpecialStateType type : SpecialStateType.values()) {
            ids.add(type.getId());
        }

        // Then - All IDs are unique
        assertEquals(SpecialStateType.values().length, ids.size());
    }

    @Test
    @DisplayName("Should have all negative IDs")
    void testAllNegativeIds() {
        // Then - All special states have negative IDs
        for (SpecialStateType type : SpecialStateType.values()) {
            assertTrue(type.getId() < 0, type.name() + " should have negative ID");
        }
    }

    @ParameterizedTest
    @EnumSource(SpecialStateType.class)
    @DisplayName("Should handle all enum values")
    void testAllEnumValues(SpecialStateType type) {
        // Then
        assertNotNull(type);
        assertNotNull(type.getId());
        assertNotNull(type.name());
        assertTrue(type.getId() < 0);
        assertTrue(type.ordinal() >= 0);
        assertTrue(type.ordinal() < 5);
    }

    @ParameterizedTest
    @CsvSource({"-1,UNKNOWN", "-2,PREVIOUS", "-3,CURRENT", "-4,EXPECTED", "-5,NULL"})
    @DisplayName("Should lookup type from ID correctly")
    void testFromId(Long id, String expectedName) {
        // When
        SpecialStateType type = SpecialStateType.fromId(id);

        // Then
        assertEquals(expectedName, type.name());
        assertEquals(id, type.getId());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, -6L, -100L, 100L, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Should throw exception for invalid IDs")
    void testFromIdInvalid(Long id) {
        // Then
        assertThrows(IllegalArgumentException.class, () -> SpecialStateType.fromId(id));
    }

    @Test
    @DisplayName("Should handle null ID in fromId")
    void testFromIdNull() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> SpecialStateType.fromId(null));
    }

    @ParameterizedTest
    @CsvSource({
        "-1,true",
        "-2,true",
        "-3,true",
        "-4,true",
        "-5,true",
        "0,false",
        "1,false",
        "-6,false",
        "100,false",
        "-100,false"
    })
    @DisplayName("Should check if ID is special state ID")
    void testIsSpecialStateId(Long id, boolean expected) {
        // When
        boolean result = SpecialStateType.isSpecialStateId(id);

        // Then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should handle null in isSpecialStateId")
    void testIsSpecialStateIdNull() {
        // When
        boolean result = SpecialStateType.isSpecialStateId(null);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should work in switch statements")
    void testSwitchStatement() {
        // Given
        SpecialStateType type = SpecialStateType.CURRENT;

        // When
        String description =
                switch (type) {
                    case UNKNOWN -> "State is unknown";
                    case PREVIOUS -> "Previous state";
                    case CURRENT -> "Current state";
                    case EXPECTED -> "Expected state";
                    case NULL -> "No state";
                };

        // Then
        assertEquals("Current state", description);
    }

    @TestFactory
    @DisplayName("SpecialStateType usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
                dynamicTest(
                        "State identification",
                        () -> {
                            Long stateId = -3L;
                            assertTrue(SpecialStateType.isSpecialStateId(stateId));
                            assertEquals(
                                    SpecialStateType.CURRENT, SpecialStateType.fromId(stateId));
                        }),
                dynamicTest(
                        "Regular vs special state distinction",
                        () -> {
                            Long regularStateId = 1L;
                            Long specialStateId = -1L;

                            assertFalse(SpecialStateType.isSpecialStateId(regularStateId));
                            assertTrue(SpecialStateType.isSpecialStateId(specialStateId));
                        }),
                dynamicTest(
                        "State history tracking",
                        () -> {
                            SpecialStateType previousType = SpecialStateType.PREVIOUS;
                            SpecialStateType currentType = SpecialStateType.CURRENT;

                            assertNotEquals(previousType.getId(), currentType.getId());
                            // PREVIOUS is -2, CURRENT is -3, so PREVIOUS > CURRENT
                            assertTrue(previousType.getId() > currentType.getId());
                        }),
                dynamicTest(
                        "Stateless element handling",
                        () -> {
                            SpecialStateType nullType = SpecialStateType.NULL;
                            assertEquals(-5L, nullType.getId());
                            assertTrue(SpecialStateType.isSpecialStateId(nullType.getId()));
                        }),
                dynamicTest(
                        "Unknown state detection",
                        () -> {
                            SpecialStateType unknownType = SpecialStateType.UNKNOWN;
                            assertEquals(-1L, unknownType.getId());
                            assertEquals(0, unknownType.ordinal());
                        }));
    }

    @Test
    @DisplayName("Should maintain ordinal order")
    void testOrdinalOrder() {
        // Given
        SpecialStateType[] types = SpecialStateType.values();

        // Then - Ordinals are sequential
        for (int i = 0; i < types.length; i++) {
            assertEquals(i, types[i].ordinal());
        }
    }

    @Test
    @DisplayName("Should parse from string using valueOf")
    void testValueOf() {
        // Then
        assertEquals(SpecialStateType.UNKNOWN, SpecialStateType.valueOf("UNKNOWN"));
        assertEquals(SpecialStateType.PREVIOUS, SpecialStateType.valueOf("PREVIOUS"));
        assertEquals(SpecialStateType.CURRENT, SpecialStateType.valueOf("CURRENT"));
        assertEquals(SpecialStateType.EXPECTED, SpecialStateType.valueOf("EXPECTED"));
        assertEquals(SpecialStateType.NULL, SpecialStateType.valueOf("NULL"));
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void testValueOfInvalid() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> SpecialStateType.valueOf("INVALID"));
        assertThrows(
                IllegalArgumentException.class,
                () -> SpecialStateType.valueOf("unknown")); // Case sensitive
    }

    @Test
    @DisplayName("Should support collections")
    void testCollections() {
        // Given
        Set<SpecialStateType> stateTypes =
                EnumSet.of(SpecialStateType.CURRENT, SpecialStateType.PREVIOUS);

        // Then
        assertEquals(2, stateTypes.size());
        assertTrue(stateTypes.contains(SpecialStateType.CURRENT));
        assertTrue(stateTypes.contains(SpecialStateType.PREVIOUS));
        assertFalse(stateTypes.contains(SpecialStateType.NULL));
    }

    @Test
    @DisplayName("Should demonstrate ID collision prevention")
    void testIdCollisionPrevention() {
        // Given - Regular states use positive IDs
        Long regularStateId = 1L;
        Long anotherRegularStateId = 100L;

        // Special states use negative IDs
        for (SpecialStateType type : SpecialStateType.values()) {
            Long specialId = type.getId();

            // Then - No collision possible
            assertNotEquals(regularStateId, specialId);
            assertNotEquals(anotherRegularStateId, specialId);
            assertTrue(specialId < 0);
        }
    }

    @Test
    @DisplayName("Should verify enum semantics")
    void testEnumSemantics() {
        // UNKNOWN - unidentified state
        assertEquals("UNKNOWN", SpecialStateType.UNKNOWN.name());
        assertEquals(-1L, SpecialStateType.UNKNOWN.getId());

        // PREVIOUS - state before transition
        assertEquals("PREVIOUS", SpecialStateType.PREVIOUS.name());
        assertEquals(-2L, SpecialStateType.PREVIOUS.getId());

        // CURRENT - active state
        assertEquals("CURRENT", SpecialStateType.CURRENT.name());
        assertEquals(-3L, SpecialStateType.CURRENT.getId());

        // EXPECTED - anticipated state
        assertEquals("EXPECTED", SpecialStateType.EXPECTED.name());
        assertEquals(-4L, SpecialStateType.EXPECTED.getId());

        // NULL - stateless
        assertEquals("NULL", SpecialStateType.NULL.name());
        assertEquals(-5L, SpecialStateType.NULL.getId());
    }

    @Test
    @DisplayName("Should support EnumMap for performance")
    void testEnumMap() {
        // Given
        EnumMap<SpecialStateType, String> descriptions = new EnumMap<>(SpecialStateType.class);

        // When
        descriptions.put(SpecialStateType.UNKNOWN, "State not identified");
        descriptions.put(SpecialStateType.PREVIOUS, "Last known state");
        descriptions.put(SpecialStateType.CURRENT, "Active state");
        descriptions.put(SpecialStateType.EXPECTED, "Target state");
        descriptions.put(SpecialStateType.NULL, "No state");

        // Then
        assertEquals(5, descriptions.size());
        assertEquals("Active state", descriptions.get(SpecialStateType.CURRENT));

        // EnumMap maintains enum order
        Iterator<SpecialStateType> iterator = descriptions.keySet().iterator();
        assertEquals(SpecialStateType.UNKNOWN, iterator.next());
        assertEquals(SpecialStateType.PREVIOUS, iterator.next());
    }

    @Test
    @DisplayName("Should demonstrate lookup performance")
    void testLookupPerformance() {
        // Given
        Map<Long, SpecialStateType> lookupMap = new HashMap<>();
        for (SpecialStateType type : SpecialStateType.values()) {
            lookupMap.put(type.getId(), type);
        }

        // When - Fast lookup
        SpecialStateType found = lookupMap.get(-3L);

        // Then
        assertEquals(SpecialStateType.CURRENT, found);

        // Alternative: Direct method
        assertEquals(SpecialStateType.CURRENT, SpecialStateType.fromId(-3L));
    }

    @Test
    @DisplayName("Should handle boundary IDs")
    void testBoundaryIds() {
        // Test that special state IDs don't conflict with edge cases

        // Zero is not a special state (reserved for regular states)
        assertFalse(SpecialStateType.isSpecialStateId(0L));

        // Very large negative number is not a special state
        assertFalse(SpecialStateType.isSpecialStateId(Long.MIN_VALUE));

        // Positive numbers are never special states
        assertFalse(SpecialStateType.isSpecialStateId(Long.MAX_VALUE));
    }

    @Test
    @DisplayName("Should demonstrate enum immutability")
    void testEnumImmutability() {
        // Enum instances are immutable singletons
        SpecialStateType type1 = SpecialStateType.CURRENT;
        SpecialStateType type2 = SpecialStateType.valueOf("CURRENT");
        SpecialStateType type3 = SpecialStateType.fromId(-3L);

        // All references point to same instance
        assertSame(type1, type2);
        assertSame(type2, type3);

        // ID is immutable
        Long id = type1.getId();
        assertEquals(-3L, id);
        // Cannot modify the ID (no setter method)
    }

    @Test
    @DisplayName("Should follow enum best practices")
    void testEnumBestPractices() {
        // Verify enum follows best practices

        // 1. Enum is final (can't be extended)
        assertTrue(java.lang.reflect.Modifier.isFinal(SpecialStateType.class.getModifiers()));

        // 2. Has proper enum methods
        assertDoesNotThrow(() -> SpecialStateType.valueOf("CURRENT"));
        assertDoesNotThrow(() -> SpecialStateType.values());

        // 3. Implements Comparable
        assertTrue(Comparable.class.isAssignableFrom(SpecialStateType.class));

        // 4. Implements Serializable
        assertTrue(java.io.Serializable.class.isAssignableFrom(SpecialStateType.class));
    }
}
