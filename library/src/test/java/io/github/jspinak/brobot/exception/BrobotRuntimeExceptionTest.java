package io.github.jspinak.brobot.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for BrobotRuntimeException. Achieves 100% coverage of all constructors and
 * methods.
 */
@DisplayName("BrobotRuntimeException Tests")
public class BrobotRuntimeExceptionTest extends BrobotTestBase {

    @Test
    @DisplayName("Should create exception with message only")
    void testExceptionWithMessage() {
        // Given
        String message = "Test error message";

        // When
        BrobotRuntimeException exception = new BrobotRuntimeException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void testExceptionWithMessageAndCause() {
        // Given
        String message = "Wrapper exception message";
        IOException cause = new IOException("IO operation failed");

        // When
        BrobotRuntimeException exception = new BrobotRuntimeException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertSame(cause, exception.getCause());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should create exception with cause only")
    void testExceptionWithCauseOnly() {
        // Given
        IllegalArgumentException cause = new IllegalArgumentException("Invalid argument");

        // When
        BrobotRuntimeException exception = new BrobotRuntimeException(cause);

        // Then
        assertEquals(cause, exception.getCause());
        assertSame(cause, exception.getCause());
        // Message should contain the cause's class name and message
        assertTrue(exception.getMessage().contains("IllegalArgumentException"));
        assertTrue(exception.getMessage().contains("Invalid argument"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n", "Simple message", "Message with special chars: @#$%"})
    @DisplayName("Should handle various message formats")
    void testVariousMessageFormats(String message) {
        // When
        BrobotRuntimeException exception = new BrobotRuntimeException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle null cause in constructor with message and cause")
    void testNullCauseWithMessage() {
        // Given
        String message = "Exception with null cause";

        // When
        BrobotRuntimeException exception = new BrobotRuntimeException(message, null);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should handle null cause in cause-only constructor")
    void testNullCauseOnly() {
        // When
        BrobotRuntimeException exception = new BrobotRuntimeException((Throwable) null);

        // Then
        assertNull(exception.getCause());
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should preserve exception chain")
    void testExceptionChain() {
        // Given
        Exception root = new Exception("Root cause");
        RuntimeException middle = new RuntimeException("Middle exception", root);
        BrobotRuntimeException top = new BrobotRuntimeException("Top level", middle);

        // When
        Throwable rootCause = getRootCause(top);

        // Then
        assertEquals(middle, top.getCause());
        assertEquals(root, middle.getCause());
        assertEquals(root, rootCause);
    }

    @Test
    @DisplayName("Should be throwable and catchable as RuntimeException")
    void testThrowAndCatch() {
        // Given
        String message = "Thrown exception";

        // When/Then
        assertThrows(
                RuntimeException.class,
                () -> {
                    throw new BrobotRuntimeException(message);
                });

        assertThrows(
                BrobotRuntimeException.class,
                () -> {
                    throw new BrobotRuntimeException(message);
                });
    }

    @Test
    @DisplayName("Should serialize stack trace correctly")
    void testStackTraceSerialization() {
        // Given
        BrobotRuntimeException exception = new BrobotRuntimeException("Test stack trace");

        // When
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Then
        assertTrue(stackTrace.contains("BrobotRuntimeException"));
        assertTrue(stackTrace.contains("Test stack trace"));
        assertTrue(stackTrace.contains(this.getClass().getName()));
    }

    @ParameterizedTest
    @MethodSource("provideExceptionHierarchy")
    @DisplayName("Should maintain proper inheritance hierarchy")
    void testInheritanceHierarchy(Class<?> expectedSuperclass) {
        // Given
        BrobotRuntimeException exception = new BrobotRuntimeException("Test");

        // Then
        assertTrue(expectedSuperclass.isInstance(exception));
    }

    private static Stream<Arguments> provideExceptionHierarchy() {
        return Stream.of(
                Arguments.of(RuntimeException.class),
                Arguments.of(Exception.class),
                Arguments.of(Throwable.class),
                Arguments.of(Object.class));
    }

    @Test
    @DisplayName("Should support suppressed exceptions")
    void testSuppressedExceptions() {
        // Given
        BrobotRuntimeException main = new BrobotRuntimeException("Main exception");
        Exception suppressed1 = new Exception("Suppressed 1");
        Exception suppressed2 = new Exception("Suppressed 2");

        // When
        main.addSuppressed(suppressed1);
        main.addSuppressed(suppressed2);

        // Then
        Throwable[] suppressed = main.getSuppressed();
        assertEquals(2, suppressed.length);
        assertEquals(suppressed1, suppressed[0]);
        assertEquals(suppressed2, suppressed[1]);
    }

    @Test
    @DisplayName("Should be fillable in stack trace")
    void testFillInStackTrace() {
        // Given
        BrobotRuntimeException exception = new BrobotRuntimeException("Test fillInStackTrace");
        int originalLength = exception.getStackTrace().length;

        // When
        Throwable filled = exception.fillInStackTrace();

        // Then
        assertSame(exception, filled);
        assertTrue(exception.getStackTrace().length >= originalLength);
    }

    @Test
    @DisplayName("Should provide localized message")
    void testLocalizedMessage() {
        // Given
        String message = "Localized message test";
        BrobotRuntimeException exception = new BrobotRuntimeException(message);

        // When
        String localizedMessage = exception.getLocalizedMessage();

        // Then
        assertEquals(message, localizedMessage);
    }

    // Helper method
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }
}
