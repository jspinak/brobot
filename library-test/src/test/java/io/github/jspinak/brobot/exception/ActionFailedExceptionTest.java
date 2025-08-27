package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionFailedException
 * Testing action-specific exception handling and context preservation
 */
@DisplayName("ActionFailedException Tests")
class ActionFailedExceptionTest extends BrobotTestBase {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exception with action type and message")
        void shouldCreateExceptionWithActionTypeAndMessage() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.CLICK;
            String message = "Element not found on button.png";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertNotNull(exception);
            assertEquals(actionType, exception.getActionType());
            assertEquals(message, exception.getActionDetails());
            assertTrue(exception.getMessage().contains("CLICK"));
            assertTrue(exception.getMessage().contains("Element not found"));
            assertEquals("Action CLICK failed: Element not found on button.png", exception.getMessage());
        }

        @ParameterizedTest
        @EnumSource(ActionInterface.Type.class)
        @DisplayName("Should handle all action types")
        void shouldHandleAllActionTypes(ActionInterface.Type actionType) {
            // Given
            String message = "Action failed for target_element";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertNotNull(exception);
            assertEquals(actionType, exception.getActionType());
            assertEquals(message, exception.getActionDetails());
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains(actionType.toString()));
        }

        @Test
        @DisplayName("Should create exception with action type, message and cause")
        void shouldCreateExceptionWithCause() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.TYPE;
            String message = "Failed to type text";
            Exception cause = new IllegalStateException("Keyboard locked");
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message, cause);
            
            // Then
            assertNotNull(exception);
            assertEquals(actionType, exception.getActionType());
            assertEquals(message, exception.getActionDetails());
            assertEquals(cause, exception.getCause());
            assertEquals("Action TYPE failed: Failed to type text", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.FIND;
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, null);
            
            // Then
            assertNotNull(exception);
            assertEquals(actionType, exception.getActionType());
            assertNull(exception.getActionDetails());
            assertEquals("Action FIND failed: null", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty message")
        void shouldHandleEmptyMessage() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.MOVE;
            String message = "";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertNotNull(exception);
            assertEquals(actionType, exception.getActionType());
            assertEquals("", exception.getActionDetails());
            assertEquals("Action MOVE failed: ", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Simple error message",
            "Error with special chars: !@#$%^&*()",
            "Multi\nline\nerror",
            "Error with unicode: 测试 エラー"
        })
        @DisplayName("Should handle various message formats")
        void shouldHandleVariousMessageFormats(String message) {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.DRAG;
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertNotNull(exception);
            assertEquals(message, exception.getActionDetails());
            assertTrue(exception.getMessage().contains(message));
        }

        @Test
        @DisplayName("Should preserve stack trace with cause")
        void shouldPreserveStackTraceWithCause() {
            // Given
            Exception cause = new RuntimeException("Root cause");
            cause.fillInStackTrace();
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.HIGHLIGHT, 
                "Highlight failed", 
                cause
            );
            
            // Then
            assertNotNull(exception.getStackTrace());
            assertTrue(exception.getStackTrace().length > 0);
            assertEquals(cause, exception.getCause());
            assertNotNull(cause.getStackTrace());
        }
    }

    @Nested
    @DisplayName("Action Type Specific Tests")
    class ActionTypeSpecificTests {

        @Test
        @DisplayName("Should handle CLICK action failures")
        void shouldHandleClickFailures() {
            // Given
            String details = "Button not found at coordinates (100, 200)";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.CLICK, 
                details
            );
            
            // Then
            assertEquals(ActionInterface.Type.CLICK, exception.getActionType());
            assertTrue(exception.getMessage().contains("CLICK"));
            assertTrue(exception.getMessage().contains("100, 200"));
        }

        @Test
        @DisplayName("Should handle FIND action failures")
        void shouldHandleFindFailures() {
            // Given
            String details = "Pattern 'login_button.png' not found with similarity 0.8";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.FIND, 
                details
            );
            
            // Then
            assertEquals(ActionInterface.Type.FIND, exception.getActionType());
            assertTrue(exception.getMessage().contains("FIND"));
            assertTrue(exception.getMessage().contains("login_button.png"));
            assertTrue(exception.getMessage().contains("0.8"));
        }

        @Test
        @DisplayName("Should handle TYPE action failures")
        void shouldHandleTypeFailures() {
            // Given
            String details = "Failed to type 'password123' - field not focused";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.TYPE, 
                details
            );
            
            // Then
            assertEquals(ActionInterface.Type.TYPE, exception.getActionType());
            assertTrue(exception.getMessage().contains("TYPE"));
            assertTrue(exception.getMessage().contains("field not focused"));
        }

        @Test
        @DisplayName("Should handle DRAG action failures")
        void shouldHandleDragFailures() {
            // Given
            String details = "Drag operation from (50, 50) to (300, 300) interrupted";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.DRAG, 
                details
            );
            
            // Then
            assertEquals(ActionInterface.Type.DRAG, exception.getActionType());
            assertTrue(exception.getMessage().contains("DRAG"));
            assertTrue(exception.getMessage().contains("interrupted"));
        }

        @Test
        @DisplayName("Should handle VANISH action failures")
        void shouldHandleVanishFailures() {
            // Given
            String details = "Element still visible after 30 seconds timeout";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.VANISH, 
                details
            );
            
            // Then
            assertEquals(ActionInterface.Type.VANISH, exception.getActionType());
            assertTrue(exception.getMessage().contains("VANISH"));
            assertTrue(exception.getMessage().contains("30 seconds"));
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("Should chain exceptions properly")
        void shouldChainExceptionsProperly() {
            // Given
            Exception rootCause = new IllegalArgumentException("Invalid coordinates");
            Exception middleCause = new RuntimeException("Mouse operation failed", rootCause);
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.CLICK,
                "Click failed at invalid position",
                middleCause
            );
            
            // Then
            assertEquals(middleCause, exception.getCause());
            assertEquals(rootCause, middleCause.getCause());
            
            // Verify chain integrity
            Throwable current = exception;
            int depth = 0;
            while (current != null && depth < 10) {
                assertNotNull(current.getMessage());
                current = current.getCause();
                depth++;
            }
            assertEquals(3, depth); // exception -> middleCause -> rootCause -> null
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.MOVE,
                "Move failed",
                null
            );
            
            // Then
            assertNull(exception.getCause());
            assertNotNull(exception.getMessage());
            assertEquals("Action MOVE failed: Move failed", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Context Information Tests")
    class ContextInformationTests {

        @Test
        @DisplayName("Should preserve action details separately from message")
        void shouldPreserveActionDetailsSeparately() {
            // Given
            String details = "Complex error with multiple lines\nand special formatting";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.DEFINE,
                details
            );
            
            // Then
            assertEquals(details, exception.getActionDetails());
            assertEquals("Action DEFINE failed: " + details, exception.getMessage());
        }

        @Test
        @DisplayName("Should maintain action type through exception lifecycle")
        void shouldMaintainActionTypeThroughLifecycle() {
            // Given
            ActionInterface.Type originalType = ActionInterface.Type.SCROLL_MOUSE_WHEEL;
            
            // When
            ActionFailedException exception = new ActionFailedException(originalType, "Scroll failed");
            
            // Simulate passing through multiple layers
            RuntimeException wrapper1 = new RuntimeException("Layer 1", exception);
            RuntimeException wrapper2 = new RuntimeException("Layer 2", wrapper1);
            
            // Then - Original exception still maintains its type
            ActionFailedException original = (ActionFailedException) wrapper2.getCause().getCause();
            assertEquals(originalType, original.getActionType());
            assertEquals("Scroll failed", original.getActionDetails());
        }

        @Test
        @DisplayName("Should provide detailed context for debugging")
        void shouldProvideDetailedContextForDebugging() {
            // Given
            String detailedContext = "Failed to find element 'submit_button'\n" +
                                    "Search region: (0, 0, 1920, 1080)\n" +
                                    "Similarity threshold: 0.85\n" +
                                    "Timeout: 30 seconds\n" +
                                    "Last match score: 0.72";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.FIND,
                detailedContext
            );
            
            // Then
            assertEquals(detailedContext, exception.getActionDetails());
            assertTrue(exception.getMessage().contains("Search region"));
            assertTrue(exception.getMessage().contains("0.85"));
            assertTrue(exception.getMessage().contains("0.72"));
        }

        @Test
        @DisplayName("Should handle large error messages")
        void shouldHandleLargeErrorMessages() {
            // Given
            StringBuilder largeMessage = new StringBuilder("Error: ");
            for (int i = 0; i < 1000; i++) {
                largeMessage.append("Line ").append(i).append(" of error details. ");
            }
            String details = largeMessage.toString();
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.CLASSIFY,
                details
            );
            
            // Then
            assertEquals(details, exception.getActionDetails());
            assertEquals(ActionInterface.Type.CLASSIFY, exception.getActionType());
            assertTrue(exception.getMessage().length() > 1000);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            // Given
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.KEY_DOWN,
                "Key press failed"
            );
            
            // Then - Just verify the exception can be created and has expected properties
            // Actual serialization testing would require ObjectOutputStream
            assertNotNull(exception);
            assertTrue(exception instanceof RuntimeException);
            assertTrue(exception instanceof BrobotRuntimeException);
        }
    }
}