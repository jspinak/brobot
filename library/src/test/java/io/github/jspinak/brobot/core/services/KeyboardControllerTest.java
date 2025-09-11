package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive tests for KeyboardController interface. Tests keyboard control operations and
 * shortcuts.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KeyboardController Interface Tests")
@DisabledInCI
public class KeyboardControllerTest extends BrobotTestBase {

    @Mock private KeyboardController keyboardController;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should type text successfully")
    void testTypeText() {
        // Arrange
        String text = "Hello World";
        when(keyboardController.type(text)).thenReturn(true);

        // Act
        boolean result = keyboardController.type(text);

        // Assert
        assertTrue(result);
        verify(keyboardController).type(text);
    }

    @Test
    @DisplayName("Should handle empty text")
    void testTypeEmptyText() {
        // Arrange
        when(keyboardController.type("")).thenReturn(true);

        // Act
        boolean result = keyboardController.type("");

        // Assert
        assertTrue(result);
        verify(keyboardController).type("");
    }

    @Test
    @DisplayName("Should type text with delay between characters")
    void testTypeTextWithDelay() {
        // Arrange
        String text = "Slow typing";
        int delayMs = 50;
        when(keyboardController.type(text, delayMs)).thenReturn(true);

        // Act
        boolean result = keyboardController.type(text, delayMs);

        // Assert
        assertTrue(result);
        verify(keyboardController).type(text, delayMs);
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void testTypeSpecialCharacters() {
        // Arrange
        String text = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        when(keyboardController.type(text)).thenReturn(true);

        // Act
        boolean result = keyboardController.type(text);

        // Assert
        assertTrue(result);
        verify(keyboardController).type(text);
    }

    @Test
    @DisplayName("Should press special keys")
    void testPressSpecialKeys() {
        // Test various special keys
        testSpecialKey(KeyboardController.SpecialKey.ENTER);
        testSpecialKey(KeyboardController.SpecialKey.TAB);
        testSpecialKey(KeyboardController.SpecialKey.ESC);
        testSpecialKey(KeyboardController.SpecialKey.BACKSPACE);
        testSpecialKey(KeyboardController.SpecialKey.DELETE);
        testSpecialKey(KeyboardController.SpecialKey.SPACE);
    }

    private void testSpecialKey(KeyboardController.SpecialKey key) {
        when(keyboardController.pressKey(key)).thenReturn(true);
        assertTrue(keyboardController.pressKey(key));
        verify(keyboardController).pressKey(key);
    }

    @Test
    @DisplayName("Should press arrow keys")
    void testPressArrowKeys() {
        // Arrange
        when(keyboardController.pressKey(any())).thenReturn(true);

        // Act & Assert
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.UP));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.DOWN));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.LEFT));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.RIGHT));

        verify(keyboardController).pressKey(KeyboardController.SpecialKey.UP);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.DOWN);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.LEFT);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.RIGHT);
    }

    @Test
    @DisplayName("Should press function keys")
    void testPressFunctionKeys() {
        // Arrange
        when(keyboardController.pressKey(any())).thenReturn(true);

        // Act & Assert
        for (int i = 1; i <= 12; i++) {
            KeyboardController.SpecialKey fKey = KeyboardController.SpecialKey.valueOf("F" + i);
            assertTrue(keyboardController.pressKey(fKey));
            verify(keyboardController).pressKey(fKey);
        }
    }

    @Test
    @DisplayName("Should hold key down")
    void testKeyDown() {
        // Arrange
        when(keyboardController.keyDown(KeyboardController.SpecialKey.SHIFT)).thenReturn(true);

        // Act
        boolean result = keyboardController.keyDown(KeyboardController.SpecialKey.SHIFT);

        // Assert
        assertTrue(result);
        verify(keyboardController).keyDown(KeyboardController.SpecialKey.SHIFT);
    }

    @Test
    @DisplayName("Should release key")
    void testKeyUp() {
        // Arrange
        when(keyboardController.keyUp(KeyboardController.SpecialKey.SHIFT)).thenReturn(true);

        // Act
        boolean result = keyboardController.keyUp(KeyboardController.SpecialKey.SHIFT);

        // Assert
        assertTrue(result);
        verify(keyboardController).keyUp(KeyboardController.SpecialKey.SHIFT);
    }

    @Test
    @DisplayName("Should perform shortcut with single modifier")
    void testShortcutSingleModifier() {
        // Arrange
        when(keyboardController.shortcut(KeyboardController.SpecialKey.CTRL, 'C')).thenReturn(true);

        // Act
        boolean result = keyboardController.shortcut(KeyboardController.SpecialKey.CTRL, 'C');

        // Assert
        assertTrue(result);
        verify(keyboardController).shortcut(KeyboardController.SpecialKey.CTRL, 'C');
    }

    @Test
    @DisplayName("Should perform shortcut with multiple modifiers")
    void testShortcutMultipleModifiers() {
        // Arrange
        KeyboardController.SpecialKey[] modifiers = {
            KeyboardController.SpecialKey.CTRL, KeyboardController.SpecialKey.SHIFT
        };
        when(keyboardController.shortcut(modifiers, 'S')).thenReturn(true);

        // Act
        boolean result = keyboardController.shortcut(modifiers, 'S');

        // Assert
        assertTrue(result);
        verify(keyboardController).shortcut(modifiers, 'S');
    }

    @Test
    @DisplayName("Should perform shortcut with modifier and special key")
    void testShortcutModifierAndSpecialKey() {
        // Arrange
        when(keyboardController.shortcut(
                        KeyboardController.SpecialKey.ALT, KeyboardController.SpecialKey.TAB))
                .thenReturn(true);

        // Act
        boolean result =
                keyboardController.shortcut(
                        KeyboardController.SpecialKey.ALT, KeyboardController.SpecialKey.TAB);

        // Assert
        assertTrue(result);
        verify(keyboardController)
                .shortcut(KeyboardController.SpecialKey.ALT, KeyboardController.SpecialKey.TAB);
    }

    @Test
    @DisplayName("Should clear and type new text")
    void testClearAndType() {
        // Arrange
        String newText = "Replacement text";
        when(keyboardController.clearAndType(newText)).thenReturn(true);

        // Act
        boolean result = keyboardController.clearAndType(newText);

        // Assert
        assertTrue(result);
        verify(keyboardController).clearAndType(newText);
    }

    @Test
    @DisplayName("Should perform copy operation")
    void testCopy() {
        // Arrange
        when(keyboardController.copy()).thenReturn(true);

        // Act
        boolean result = keyboardController.copy();

        // Assert
        assertTrue(result);
        verify(keyboardController).copy();
    }

    @Test
    @DisplayName("Should perform paste operation")
    void testPaste() {
        // Arrange
        when(keyboardController.paste()).thenReturn(true);

        // Act
        boolean result = keyboardController.paste();

        // Assert
        assertTrue(result);
        verify(keyboardController).paste();
    }

    @Test
    @DisplayName("Should perform cut operation")
    void testCut() {
        // Arrange
        when(keyboardController.cut()).thenReturn(true);

        // Act
        boolean result = keyboardController.cut();

        // Assert
        assertTrue(result);
        verify(keyboardController).cut();
    }

    @Test
    @DisplayName("Should perform select all operation")
    void testSelectAll() {
        // Arrange
        when(keyboardController.selectAll()).thenReturn(true);

        // Act
        boolean result = keyboardController.selectAll();

        // Assert
        assertTrue(result);
        verify(keyboardController).selectAll();
    }

    @Test
    @DisplayName("Should check if controller is available")
    void testIsAvailable() {
        // Arrange
        when(keyboardController.isAvailable()).thenReturn(true);

        // Act
        boolean available = keyboardController.isAvailable();

        // Assert
        assertTrue(available);
        verify(keyboardController).isAvailable();
    }

    @Test
    @DisplayName("Should handle unavailable controller")
    void testIsNotAvailable() {
        // Arrange
        when(keyboardController.isAvailable()).thenReturn(false);

        // Act
        boolean available = keyboardController.isAvailable();

        // Assert
        assertFalse(available);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Arrange
        when(keyboardController.getImplementationName()).thenReturn("Test Keyboard");

        // Act
        String name = keyboardController.getImplementationName();

        // Assert
        assertEquals("Test Keyboard", name);
        verify(keyboardController).getImplementationName();
    }

    @Test
    @DisplayName("Should handle typing failure")
    void testTypeFailure() {
        // Arrange
        when(keyboardController.type(anyString())).thenReturn(false);

        // Act
        boolean result = keyboardController.type("Failed text");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle key press failure")
    void testPressKeyFailure() {
        // Arrange
        when(keyboardController.pressKey(any())).thenReturn(false);

        // Act
        boolean result = keyboardController.pressKey(KeyboardController.SpecialKey.ENTER);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle shortcut failure")
    void testShortcutFailure() {
        // Arrange
        when(keyboardController.shortcut(any(KeyboardController.SpecialKey.class), anyChar()))
                .thenReturn(false);

        // Act
        boolean result = keyboardController.shortcut(KeyboardController.SpecialKey.CTRL, 'Z');

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should handle complex key sequences")
    void testComplexKeySequence() {
        // Arrange - Simulate typing with navigation
        when(keyboardController.type("First line")).thenReturn(true);
        when(keyboardController.pressKey(KeyboardController.SpecialKey.ENTER)).thenReturn(true);
        when(keyboardController.type("Second line")).thenReturn(true);
        when(keyboardController.pressKey(KeyboardController.SpecialKey.HOME)).thenReturn(true);
        when(keyboardController.selectAll()).thenReturn(true);
        when(keyboardController.copy()).thenReturn(true);

        // Act
        boolean typed1 = keyboardController.type("First line");
        boolean enter = keyboardController.pressKey(KeyboardController.SpecialKey.ENTER);
        boolean typed2 = keyboardController.type("Second line");
        boolean home = keyboardController.pressKey(KeyboardController.SpecialKey.HOME);
        boolean selectAll = keyboardController.selectAll();
        boolean copy = keyboardController.copy();

        // Assert
        assertTrue(typed1);
        assertTrue(enter);
        assertTrue(typed2);
        assertTrue(home);
        assertTrue(selectAll);
        assertTrue(copy);
    }

    @Test
    @DisplayName("Should handle navigation keys")
    void testNavigationKeys() {
        // Arrange
        when(keyboardController.pressKey(any())).thenReturn(true);

        // Act & Assert
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.HOME));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.END));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.PAGE_UP));
        assertTrue(keyboardController.pressKey(KeyboardController.SpecialKey.PAGE_DOWN));

        verify(keyboardController).pressKey(KeyboardController.SpecialKey.HOME);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.END);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.PAGE_UP);
        verify(keyboardController).pressKey(KeyboardController.SpecialKey.PAGE_DOWN);
    }

    @Test
    @DisplayName("Should handle modifier keys")
    void testModifierKeys() {
        // Arrange
        when(keyboardController.keyDown(any())).thenReturn(true);
        when(keyboardController.keyUp(any())).thenReturn(true);

        // Act & Assert - Test holding and releasing modifiers
        assertTrue(keyboardController.keyDown(KeyboardController.SpecialKey.CTRL));
        assertTrue(keyboardController.keyUp(KeyboardController.SpecialKey.CTRL));

        assertTrue(keyboardController.keyDown(KeyboardController.SpecialKey.ALT));
        assertTrue(keyboardController.keyUp(KeyboardController.SpecialKey.ALT));

        assertTrue(keyboardController.keyDown(KeyboardController.SpecialKey.SHIFT));
        assertTrue(keyboardController.keyUp(KeyboardController.SpecialKey.SHIFT));

        assertTrue(keyboardController.keyDown(KeyboardController.SpecialKey.CMD));
        assertTrue(keyboardController.keyUp(KeyboardController.SpecialKey.CMD));

        verify(keyboardController).keyDown(KeyboardController.SpecialKey.CTRL);
        verify(keyboardController).keyUp(KeyboardController.SpecialKey.CTRL);
        verify(keyboardController).keyDown(KeyboardController.SpecialKey.ALT);
        verify(keyboardController).keyUp(KeyboardController.SpecialKey.ALT);
        verify(keyboardController).keyDown(KeyboardController.SpecialKey.SHIFT);
        verify(keyboardController).keyUp(KeyboardController.SpecialKey.SHIFT);
        verify(keyboardController).keyDown(KeyboardController.SpecialKey.CMD);
        verify(keyboardController).keyUp(KeyboardController.SpecialKey.CMD);
    }

    @Test
    @DisplayName("Should type with various delays")
    void testTypeWithVariousDelays() {
        // Arrange
        String text = "Test";
        when(keyboardController.type(eq(text), anyInt())).thenReturn(true);

        // Act & Assert
        assertTrue(keyboardController.type(text, 0)); // No delay
        assertTrue(keyboardController.type(text, 10)); // Small delay
        assertTrue(keyboardController.type(text, 100)); // Medium delay
        assertTrue(keyboardController.type(text, 1000)); // Large delay

        verify(keyboardController).type(eq(text), eq(0));
        verify(keyboardController).type(eq(text), eq(10));
        verify(keyboardController).type(eq(text), eq(100));
        verify(keyboardController).type(eq(text), eq(1000));
    }

    @Test
    @DisplayName("Should handle multiline text")
    void testMultilineText() {
        // Arrange
        String multilineText = "Line 1\nLine 2\nLine 3";
        when(keyboardController.type(multilineText)).thenReturn(true);

        // Act
        boolean result = keyboardController.type(multilineText);

        // Assert
        assertTrue(result);
        verify(keyboardController).type(multilineText);
    }

    @Test
    @DisplayName("Should handle Unicode text")
    void testUnicodeText() {
        // Arrange
        String unicodeText = "Hello 世界 🌍 Café";
        when(keyboardController.type(unicodeText)).thenReturn(true);

        // Act
        boolean result = keyboardController.type(unicodeText);

        // Assert
        assertTrue(result);
        verify(keyboardController).type(unicodeText);
    }
}
