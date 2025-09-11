package io.github.jspinak.brobot.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for ActionFailedException. Achieves 100% coverage of all constructors,
 * methods, and edge cases.
 */
@DisplayName("ActionFailedException Tests")
public class ActionFailedExceptionTest extends BrobotTestBase {

    @Test
    @DisplayName("Should create exception with action type and message")
    void testExceptionWithActionTypeAndMessage() {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.CLICK;
        String message = "Element not found";

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message);

        // Then
        assertEquals(actionType, exception.getActionType());
        assertEquals(message, exception.getActionDetails());
        assertEquals("Action CLICK failed: Element not found", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with action type, message and cause")
    void testExceptionWithActionTypeMessageAndCause() {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.FIND;
        String message = "Pattern matching failed";
        IOException cause = new IOException("Image file not accessible");

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message, cause);

        // Then
        assertEquals(actionType, exception.getActionType());
        assertEquals(message, exception.getActionDetails());
        assertEquals("Action FIND failed: Pattern matching failed", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertSame(cause, exception.getCause());
    }

    @ParameterizedTest
    @EnumSource(ActionInterface.Type.class)
    @DisplayName("Should handle all action types")
    void testAllActionTypes(ActionInterface.Type actionType) {
        // Given
        String message = "Operation failed for " + actionType;

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message);

        // Then
        assertEquals(actionType, exception.getActionType());
        assertEquals(message, exception.getActionDetails());
        assertTrue(exception.getMessage().contains(actionType.toString()));
        assertTrue(exception.getMessage().contains(message));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "\t",
                "\n",
                "Simple error",
                "Error with special chars: @#$%",
                "Very long error message that exceeds normal length expectations and contains"
                    + " multiple sentences. This tests the handling of verbose error descriptions."
            })
    @DisplayName("Should handle various message formats")
    void testVariousMessageFormats(String message) {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.TYPE;

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message);

        // Then
        assertEquals(message, exception.getActionDetails());
        String expectedMessage = String.format("Action %s failed: %s", actionType, message);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause in three-parameter constructor")
    void testNullCause() {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.DRAG;
        String message = "Drag operation failed";

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message, null);

        // Then
        assertEquals(actionType, exception.getActionType());
        assertEquals(message, exception.getActionDetails());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should inherit from BrobotRuntimeException")
    void testInheritance() {
        // Given
        ActionFailedException exception =
                new ActionFailedException(ActionInterface.Type.VANISH, "Timeout occurred");

        // Then
        assertTrue(exception instanceof BrobotRuntimeException);
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }

    @Test
    @DisplayName("Should be throwable and catchable as ActionFailedException")
    void testThrowAndCatch() {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.MOUSE_DOWN;
        String message = "Mouse down failed";

        // When/Then
        ActionFailedException caught =
                assertThrows(
                        ActionFailedException.class,
                        () -> {
                            throw new ActionFailedException(actionType, message);
                        });

        assertEquals(actionType, caught.getActionType());
        assertEquals(message, caught.getActionDetails());
    }

    @Test
    @DisplayName("Should be catchable as BrobotRuntimeException")
    void testCatchAsBrobotRuntimeException() {
        // When/Then
        BrobotRuntimeException caught =
                assertThrows(
                        BrobotRuntimeException.class,
                        () -> {
                            throw new ActionFailedException(
                                    ActionInterface.Type.SCROLL_MOUSE_WHEEL, "Scroll failed");
                        });

        assertTrue(caught instanceof ActionFailedException);
    }

    @ParameterizedTest
    @MethodSource("provideActionScenarios")
    @DisplayName("Should handle realistic action failure scenarios")
    void testRealisticScenarios(
            ActionInterface.Type actionType,
            String details,
            Throwable cause,
            String expectedMessagePart) {
        // When
        ActionFailedException exception =
                (cause != null)
                        ? new ActionFailedException(actionType, details, cause)
                        : new ActionFailedException(actionType, details);

        // Then
        assertEquals(actionType, exception.getActionType());
        assertEquals(details, exception.getActionDetails());
        assertTrue(exception.getMessage().contains(expectedMessagePart));
        assertEquals(cause, exception.getCause());
    }

    private static Stream<Arguments> provideActionScenarios() {
        return Stream.of(
                Arguments.of(
                        ActionInterface.Type.CLICK,
                        "Button not visible",
                        null,
                        "Button not visible"),
                Arguments.of(
                        ActionInterface.Type.FIND,
                        "Pattern score too low: 0.65",
                        null,
                        "Pattern score too low"),
                Arguments.of(
                        ActionInterface.Type.TYPE,
                        "Input field disabled",
                        new IllegalStateException("Field state"),
                        "Input field disabled"),
                Arguments.of(
                        ActionInterface.Type.DRAG,
                        "Source element not found",
                        new NullPointerException(),
                        "Source element not found"),
                Arguments.of(
                        ActionInterface.Type.VANISH,
                        "Timeout after 30 seconds",
                        null,
                        "Timeout after 30 seconds"),
                Arguments.of(
                        ActionInterface.Type.SCROLL_MOUSE_WHEEL,
                        "Reached end of scrollable area",
                        null,
                        "Reached end of scrollable area"),
                Arguments.of(
                        ActionInterface.Type.MOUSE_DOWN,
                        "Coordinates out of bounds",
                        new IllegalArgumentException("Invalid coords"),
                        "Coordinates out of bounds"));
    }

    @Test
    @DisplayName("Should preserve stack trace information")
    void testStackTracePreservation() {
        // Given
        ActionInterface.Type actionType = ActionInterface.Type.HIGHLIGHT;
        String message = "Highlight rendering failed";

        // When
        ActionFailedException exception = new ActionFailedException(actionType, message);
        StackTraceElement[] stackTrace = exception.getStackTrace();

        // Then
        assertTrue(stackTrace.length > 0);
        assertEquals(this.getClass().getName(), stackTrace[0].getClassName());
    }

    @Test
    @DisplayName("Should support exception chaining")
    void testExceptionChaining() {
        // Given
        Exception root = new Exception("Root cause");
        RuntimeException middle = new RuntimeException("Middle layer", root);
        ActionFailedException top =
                new ActionFailedException(ActionInterface.Type.VANISH, "Vanish failed", middle);

        // When
        Throwable rootCause = getRootCause(top);

        // Then
        assertEquals(middle, top.getCause());
        assertEquals(root, rootCause);
    }

    @Test
    @DisplayName("Should format message correctly with null action type")
    void testNullActionType() {
        // Note: This tests defensive programming even though it shouldn't happen in practice
        // When
        ActionFailedException exception = new ActionFailedException(null, "Test message");

        // Then
        assertNull(exception.getActionType());
        assertEquals("Test message", exception.getActionDetails());
        assertEquals("Action null failed: Test message", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle concurrent access to getters")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        ActionFailedException exception =
                new ActionFailedException(ActionInterface.Type.CLICK, "Concurrent test");

        // When - Access from multiple threads
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                for (int j = 0; j < 100; j++) {
                                    assertEquals(
                                            ActionInterface.Type.CLICK, exception.getActionType());
                                    assertEquals("Concurrent test", exception.getActionDetails());
                                }
                            });
            threads[i].start();
        }

        // Then - Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // Helper method
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause == null) ? throwable : getRootCause(cause);
    }
}
