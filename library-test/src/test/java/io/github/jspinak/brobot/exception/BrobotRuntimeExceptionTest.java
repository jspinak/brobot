package io.github.jspinak.brobot.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Unit tests for BrobotRuntimeException Testing all constructors, message handling, and cause
 * propagation
 */
@DisplayName("BrobotRuntimeException Tests")
class BrobotRuntimeExceptionTest extends BrobotTestBase {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with message")
        void shouldCreateExceptionWithMessage() {
            // Given
            String message = "Test error message";

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(message);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with null message")
        void shouldCreateExceptionWithNullMessage() {
            // When
            BrobotRuntimeException exception = new BrobotRuntimeException((String) null);

            // Then
            assertNotNull(exception);
            assertNull(exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("Should create exception with message and cause")
        void shouldCreateExceptionWithMessageAndCause() {
            // Given
            String causeMessage = "Root cause";
            IllegalArgumentException cause = new IllegalArgumentException(causeMessage);
            String message = "Wrapper exception";

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(message, cause);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
            assertEquals(causeMessage, exception.getCause().getMessage());
        }

        @Test
        @DisplayName("Should create exception with cause only")
        void shouldCreateExceptionWithCauseOnly() {
            // Given
            RuntimeException cause = new RuntimeException("Original error");

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(cause);

            // Then
            assertNotNull(exception);
            assertEquals(cause, exception.getCause());
            // The message should contain the cause's toString()
            assertTrue(exception.getMessage().contains(cause.toString()));
        }

        @Test
        @DisplayName("Should handle null cause in constructor")
        void shouldHandleNullCause() {
            // When
            BrobotRuntimeException exception = new BrobotRuntimeException("Test", null);

            // Then
            assertNotNull(exception);
            assertNull(exception.getCause());
            assertEquals("Test", exception.getMessage());
        }

        @Test
        @DisplayName("Should create exception with null cause only")
        void shouldCreateExceptionWithNullCauseOnly() {
            // When
            BrobotRuntimeException exception = new BrobotRuntimeException((Throwable) null);

            // Then
            assertNotNull(exception);
            assertNull(exception.getCause());
            assertNull(exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Exception Behavior Tests")
    class BehaviorTests {

        @Test
        @DisplayName("Should be throwable as RuntimeException")
        void shouldBeThrowableAsRuntimeException() {
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        throw new BrobotRuntimeException("Test exception");
                    });
        }

        @Test
        @DisplayName("Should preserve stack trace")
        void shouldPreserveStackTrace() {
            // When
            BrobotRuntimeException exception = new BrobotRuntimeException("Stack trace test");

            // Then
            assertNotNull(exception.getStackTrace());
            assertTrue(exception.getStackTrace().length > 0);
            assertEquals(this.getClass().getName(), exception.getStackTrace()[0].getClassName());
        }

        @Test
        @DisplayName("Should support exception chaining")
        void shouldSupportExceptionChaining() {
            // Given
            Exception level3 = new Exception("Level 3");
            RuntimeException level2 = new RuntimeException("Level 2", level3);
            BrobotRuntimeException level1 = new BrobotRuntimeException("Level 1", level2);

            // Then
            assertEquals("Level 1", level1.getMessage());
            assertEquals("Level 2", level1.getCause().getMessage());
            assertEquals("Level 3", level1.getCause().getCause().getMessage());
        }

        @Test
        @DisplayName("Should support suppressed exceptions")
        void shouldSupportSuppressedExceptions() {
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
            assertEquals("Suppressed 1", suppressed[0].getMessage());
            assertEquals("Suppressed 2", suppressed[1].getMessage());
        }

        @Test
        @DisplayName("Should extend RuntimeException")
        void shouldExtendRuntimeException() {
            // Given
            BrobotRuntimeException exception = new BrobotRuntimeException("Test");

            // Then
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof Exception);
            assertTrue(exception instanceof Throwable);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "\n", "\t"})
        @DisplayName("Should handle various message formats")
        void shouldHandleVariousMessageFormats(String message) {
            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(message);

            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("Should handle very long message")
        void shouldHandleVeryLongMessage() {
            // Given
            String longMessage = "A".repeat(10000);

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(longMessage);

            // Then
            assertNotNull(exception);
            assertEquals(longMessage, exception.getMessage());
        }

        @Test
        @DisplayName("Should prevent re-initialization of cause")
        void shouldPreventReInitializationOfCause() {
            // Given
            BrobotRuntimeException exception = new BrobotRuntimeException("Main exception");
            Exception cause1 = new Exception("Cause 1");
            Exception cause2 = new Exception("Cause 2");

            // When
            exception.initCause(cause1);

            // Then - Second initCause should throw IllegalStateException
            assertThrows(
                    IllegalStateException.class,
                    () -> {
                        exception.initCause(cause2);
                    });
        }

        @Test
        @DisplayName("Should handle deep exception chain")
        void shouldHandleDeepExceptionChain() {
            // Given - Create a deep chain of exceptions
            Throwable current = new Exception("Root");
            for (int i = 0; i < 100; i++) {
                current = new BrobotRuntimeException("Level " + i, current);
            }

            // Then - Should preserve the entire chain
            int depth = 0;
            Throwable cause = current.getCause();
            while (cause != null) {
                depth++;
                cause = cause.getCause();
            }
            assertEquals(100, depth);
        }

        @Test
        @DisplayName("Should handle unicode in message")
        void shouldHandleUnicodeInMessage() {
            // Given
            String unicode = "Error: 日本語 🚀 éñ";

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(unicode);

            // Then
            assertEquals(unicode, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("ToString and Serialization Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should provide meaningful toString")
        void shouldProvideMeaningfulToString() {
            // Given
            BrobotRuntimeException exception = new BrobotRuntimeException("Test error");

            // When
            String string = exception.toString();

            // Then
            assertNotNull(string);
            assertTrue(string.contains("BrobotRuntimeException"));
            assertTrue(string.contains("Test error"));
        }

        @Test
        @DisplayName("Should handle toString with null message")
        void shouldHandleToStringWithNullMessage() {
            // Given
            BrobotRuntimeException exception = new BrobotRuntimeException((String) null);

            // When
            String string = exception.toString();

            // Then
            assertNotNull(string);
            assertTrue(string.contains("BrobotRuntimeException"));
        }

        @Test
        @DisplayName("Should include cause in getMessage when created with cause only")
        void shouldIncludeCauseInGetMessage() {
            // Given
            Exception cause = new IllegalStateException("Original problem");

            // When
            BrobotRuntimeException exception = new BrobotRuntimeException(cause);

            // Then
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("IllegalStateException"));
            assertTrue(exception.getMessage().contains("Original problem"));
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should work as base class for other exceptions")
        void shouldWorkAsBaseClass() {
            // Given - Create a custom exception extending BrobotRuntimeException
            class CustomBrobotException extends BrobotRuntimeException {
                public CustomBrobotException(String message) {
                    super(message);
                }
            }

            // When
            CustomBrobotException custom = new CustomBrobotException("Custom error");

            // Then
            assertTrue(custom instanceof BrobotRuntimeException);
            assertTrue(custom instanceof RuntimeException);
            assertEquals("Custom error", custom.getMessage());
        }

        @Test
        @DisplayName("Should support polymorphism")
        void shouldSupportPolymorphism() {
            // Given
            BrobotRuntimeException base = new BrobotRuntimeException("Base error");
            StateNotFoundException state = new StateNotFoundException("MissingState");
            ActionFailedException action =
                    new ActionFailedException(
                            io.github.jspinak.brobot.action.ActionInterface.Type.CLICK,
                            "Click failed");

            // Then - All should be BrobotRuntimeExceptions
            assertTrue(base instanceof BrobotRuntimeException);
            assertTrue(state instanceof BrobotRuntimeException);
            assertTrue(action instanceof BrobotRuntimeException);

            // Can catch all with base type
            try {
                throw state;
            } catch (BrobotRuntimeException e) {
                assertEquals("MissingState", ((StateNotFoundException) e).getStateName());
            }
        }
    }

    @Nested
    @DisplayName("Use Case Scenarios")
    class UseCaseScenarios {

        @Test
        @DisplayName("Should wrap checked exceptions")
        void shouldWrapCheckedExceptions() {
            // Given
            Exception checked = new Exception("Checked exception");

            // When
            BrobotRuntimeException wrapped =
                    new BrobotRuntimeException("Wrapping checked exception", checked);

            // Then
            assertEquals(checked, wrapped.getCause());
            assertTrue(wrapped.getMessage().contains("Wrapping"));
        }

        @Test
        @DisplayName("Should preserve context through layers")
        void shouldPreserveContextThroughLayers() {
            // Given - Simulate a layered architecture error
            Exception dao = new Exception("Database connection failed");
            BrobotRuntimeException service =
                    new BrobotRuntimeException("Could not fetch user data", dao);
            BrobotRuntimeException controller =
                    new BrobotRuntimeException("User request failed", service);

            // Then
            assertEquals("User request failed", controller.getMessage());
            assertEquals("Could not fetch user data", controller.getCause().getMessage());
            assertEquals(
                    "Database connection failed", controller.getCause().getCause().getMessage());
        }

        @Test
        @DisplayName("Should handle concurrent exception creation")
        void shouldHandleConcurrentExceptionCreation() throws InterruptedException {
            // Given
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            BrobotRuntimeException[] exceptions = new BrobotRuntimeException[threadCount];

            // When
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] =
                        new Thread(
                                () -> {
                                    exceptions[index] =
                                            new BrobotRuntimeException("Thread " + index);
                                });
                threads[i].start();
            }

            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            for (int i = 0; i < threadCount; i++) {
                assertNotNull(exceptions[i]);
                assertEquals("Thread " + i, exceptions[i].getMessage());
            }
        }
    }
}
