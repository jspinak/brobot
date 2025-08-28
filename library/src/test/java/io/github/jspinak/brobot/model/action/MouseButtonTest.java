package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Comprehensive tests for the MouseButton enum which represents
 * physical mouse buttons in the Brobot framework.
 */
@DisplayName("MouseButton Enum Tests")
public class MouseButtonTest extends BrobotTestBase {

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should have exactly three mouse button constants")
    void testEnumConstants() {
        // When
        MouseButton[] buttons = MouseButton.values();
        
        // Then
        assertEquals(3, buttons.length);
        assertEquals(MouseButton.LEFT, buttons[0]);
        assertEquals(MouseButton.RIGHT, buttons[1]);
        assertEquals(MouseButton.MIDDLE, buttons[2]);
    }

    @Test
    @DisplayName("Should have correct enum names")
    void testEnumNames() {
        // Then
        assertEquals("LEFT", MouseButton.LEFT.name());
        assertEquals("RIGHT", MouseButton.RIGHT.name());
        assertEquals("MIDDLE", MouseButton.MIDDLE.name());
    }

    @Test
    @DisplayName("Should have correct ordinal values")
    void testOrdinalValues() {
        // Then
        assertEquals(0, MouseButton.LEFT.ordinal());
        assertEquals(1, MouseButton.RIGHT.ordinal());
        assertEquals(2, MouseButton.MIDDLE.ordinal());
    }

    @Test
    @DisplayName("Should parse from string using valueOf")
    void testValueOf() {
        // Then
        assertEquals(MouseButton.LEFT, MouseButton.valueOf("LEFT"));
        assertEquals(MouseButton.RIGHT, MouseButton.valueOf("RIGHT"));
        assertEquals(MouseButton.MIDDLE, MouseButton.valueOf("MIDDLE"));
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void testValueOfInvalid() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> MouseButton.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> MouseButton.valueOf("left")); // Case sensitive
        assertThrows(IllegalArgumentException.class, () -> MouseButton.valueOf(""));
        assertThrows(NullPointerException.class, () -> MouseButton.valueOf(null));
    }

    @ParameterizedTest
    @EnumSource(MouseButton.class)
    @DisplayName("Should handle all enum values")
    void testAllEnumValues(MouseButton button) {
        // Then
        assertNotNull(button);
        assertNotNull(button.name());
        assertTrue(button.ordinal() >= 0);
        assertTrue(button.ordinal() < 3);
    }

    @Test
    @DisplayName("Should work in switch statements")
    void testSwitchStatement() {
        // Given
        MouseButton button = MouseButton.RIGHT;
        
        // When
        String action = switch (button) {
            case LEFT -> "Primary click";
            case RIGHT -> "Context menu";
            case MIDDLE -> "Auxiliary action";
        };
        
        // Then
        assertEquals("Context menu", action);
    }

    @Test
    @DisplayName("Should support enum comparison")
    void testEnumComparison() {
        // Given
        MouseButton button1 = MouseButton.LEFT;
        MouseButton button2 = MouseButton.LEFT;
        MouseButton button3 = MouseButton.RIGHT;
        
        // Then - Reference equality
        assertSame(button1, button2);
        assertNotSame(button1, button3);
        
        // Equality comparison
        assertEquals(button1, button2);
        assertNotEquals(button1, button3);
        
        // Comparable
        assertTrue(button1.compareTo(button3) < 0);
        assertTrue(button3.compareTo(button1) > 0);
        assertEquals(0, button1.compareTo(button2));
    }

    @Test
    @DisplayName("Should support collections")
    void testCollections() {
        // Given
        Set<MouseButton> usedButtons = new HashSet<>();
        
        // When
        usedButtons.add(MouseButton.LEFT);
        usedButtons.add(MouseButton.RIGHT);
        usedButtons.add(MouseButton.LEFT); // Duplicate
        
        // Then
        assertEquals(2, usedButtons.size());
        assertTrue(usedButtons.contains(MouseButton.LEFT));
        assertTrue(usedButtons.contains(MouseButton.RIGHT));
        assertFalse(usedButtons.contains(MouseButton.MIDDLE));
    }

    @Test
    @DisplayName("Should provide consistent toString")
    void testToString() {
        // Then
        assertEquals("LEFT", MouseButton.LEFT.toString());
        assertEquals("RIGHT", MouseButton.RIGHT.toString());
        assertEquals("MIDDLE", MouseButton.MIDDLE.toString());
    }

    @TestFactory
    @DisplayName("Mouse button usage scenarios")
    Stream<DynamicTest> testUsageScenarios() {
        return Stream.of(
            dynamicTest("Primary action mapping", () -> {
                MouseButton primaryButton = MouseButton.LEFT;
                assertTrue(primaryButton == MouseButton.LEFT);
                assertEquals("LEFT", primaryButton.name());
            }),
            
            dynamicTest("Context menu mapping", () -> {
                MouseButton contextButton = MouseButton.RIGHT;
                assertTrue(contextButton == MouseButton.RIGHT);
                assertEquals(1, contextButton.ordinal());
            }),
            
            dynamicTest("Auxiliary action mapping", () -> {
                MouseButton auxButton = MouseButton.MIDDLE;
                assertTrue(auxButton == MouseButton.MIDDLE);
                assertEquals(2, auxButton.ordinal());
            }),
            
            dynamicTest("Button preference handling", () -> {
                // Simulate user preference
                MouseButton preferredButton = MouseButton.LEFT;
                MouseButton alternateButton = MouseButton.RIGHT;
                
                assertNotEquals(preferredButton, alternateButton);
                assertTrue(preferredButton.ordinal() < alternateButton.ordinal());
            }),
            
            dynamicTest("Multi-button combination", () -> {
                Set<MouseButton> combo = Set.of(
                    MouseButton.LEFT,
                    MouseButton.RIGHT
                );
                
                assertEquals(2, combo.size());
                assertFalse(combo.contains(MouseButton.MIDDLE));
            })
        );
    }

    @Test
    @DisplayName("Should handle array operations")
    void testArrayOperations() {
        // Given
        MouseButton[] buttons = MouseButton.values();
        
        // When
        MouseButton[] copy = Arrays.copyOf(buttons, buttons.length);
        
        // Then
        assertArrayEquals(buttons, copy);
        assertEquals(3, copy.length);
        
        // Modify copy doesn't affect original
        copy[0] = MouseButton.MIDDLE;
        assertEquals(MouseButton.LEFT, buttons[0]);
        assertEquals(MouseButton.MIDDLE, copy[0]);
    }

    @Test
    @DisplayName("Should demonstrate enum singleton pattern")
    void testEnumSingleton() {
        // Enum instances are singletons
        MouseButton button1 = MouseButton.valueOf("LEFT");
        MouseButton button2 = MouseButton.valueOf("LEFT");
        MouseButton button3 = MouseButton.LEFT;
        
        // All references point to same instance
        assertSame(button1, button2);
        assertSame(button2, button3);
        assertSame(button1, button3);
        
        // Same for array access
        assertSame(MouseButton.LEFT, MouseButton.values()[0]);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LEFT", "RIGHT", "MIDDLE"})
    @DisplayName("Should round-trip through string conversion")
    void testStringRoundTrip(String buttonName) {
        // Given
        MouseButton original = MouseButton.valueOf(buttonName);
        
        // When
        String asString = original.toString();
        MouseButton reconstructed = MouseButton.valueOf(asString);
        
        // Then
        assertEquals(original, reconstructed);
        assertSame(original, reconstructed); // Same instance
        assertEquals(buttonName, asString);
    }

    @Test
    @DisplayName("Should iterate through all values")
    void testIteration() {
        // Given
        int count = 0;
        Set<String> names = new HashSet<>();
        
        // When
        for (MouseButton button : MouseButton.values()) {
            count++;
            names.add(button.name());
        }
        
        // Then
        assertEquals(3, count);
        assertEquals(3, names.size());
        assertTrue(names.contains("LEFT"));
        assertTrue(names.contains("RIGHT"));
        assertTrue(names.contains("MIDDLE"));
    }

    @Test
    @DisplayName("Should handle null-safe operations")
    void testNullSafety() {
        // Given
        MouseButton button = MouseButton.LEFT;
        MouseButton nullButton = null;
        
        // Then
        assertNotNull(button);
        assertNull(nullButton);
        
        // Comparison with null
        assertNotEquals(button, nullButton);
        assertNotEquals(null, button);
        
        // Can't call methods on null
        assertThrows(NullPointerException.class, () -> nullButton.name());
    }

    @Test
    @DisplayName("Should provide type safety")
    void testTypeSafety() {
        // MouseButton provides compile-time type safety
        MouseButton button = MouseButton.LEFT;
        
        // Can't assign invalid values
        // button = "LEFT"; // Compilation error
        // button = 0; // Compilation error
        
        // Type checking
        assertTrue(button instanceof MouseButton);
        assertTrue(button instanceof Enum);
        assertEquals(MouseButton.class, button.getDeclaringClass());
    }

    @Test
    @DisplayName("Should support functional programming patterns")
    void testFunctionalPatterns() {
        // Given
        MouseButton[] buttons = MouseButton.values();
        
        // When - Filter and map
        long leftAndRightCount = Arrays.stream(buttons)
            .filter(b -> b != MouseButton.MIDDLE)
            .count();
        
        String[] buttonNames = Arrays.stream(buttons)
            .map(MouseButton::name)
            .toArray(String[]::new);
        
        // Then
        assertEquals(2, leftAndRightCount);
        assertArrayEquals(new String[]{"LEFT", "RIGHT", "MIDDLE"}, buttonNames);
    }

    @Test
    @DisplayName("Should maintain consistent hashCode")
    void testHashCode() {
        // Given
        MouseButton button1 = MouseButton.LEFT;
        MouseButton button2 = MouseButton.LEFT;
        MouseButton button3 = MouseButton.RIGHT;
        
        // Then
        assertEquals(button1.hashCode(), button2.hashCode());
        assertNotEquals(button1.hashCode(), button3.hashCode()); // Usually different
        
        // HashCode consistency
        int hash1 = button1.hashCode();
        int hash2 = button1.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should follow enum best practices")
    void testEnumBestPractices() {
        // Verify enum follows best practices
        
        // 1. Enum is final (can't be extended)
        assertTrue(java.lang.reflect.Modifier.isFinal(MouseButton.class.getModifiers()));
        
        // 2. Has proper enum methods
        assertDoesNotThrow(() -> MouseButton.valueOf("LEFT"));
        assertDoesNotThrow(() -> MouseButton.values());
        
        // 3. Implements Comparable
        assertTrue(Comparable.class.isAssignableFrom(MouseButton.class));
        
        // 4. Implements Serializable
        assertTrue(java.io.Serializable.class.isAssignableFrom(MouseButton.class));
    }
}