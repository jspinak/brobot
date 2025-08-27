package io.github.jspinak.brobot.exception;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
            assertTrue(exception.getMessage().contains(actionType.name()));
            assertTrue(exception.getMessage().contains("failed"));
        }

        @Test
        @DisplayName("Should create exception with cause")
        void shouldCreateExceptionWithCause() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.FIND;
            String message = "Cannot find image.png";
            Exception cause = new RuntimeException("Underlying error");
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message, cause);
            
            // Then
            assertNotNull(exception);
            assertEquals(cause, exception.getCause());
            assertEquals(actionType, exception.getActionType());
            assertEquals(message, exception.getActionDetails());
            assertTrue(exception.getMessage().contains("FIND"));
            assertTrue(exception.getMessage().contains("Cannot find"));
        }

        @Test
        @DisplayName("Should handle null action type")
        void shouldHandleNullActionType() {
            // When
            ActionFailedException exception = new ActionFailedException(null, "Error message");
            
            // Then
            assertNotNull(exception);
            assertNull(exception.getActionType());
            assertEquals("Error message", exception.getActionDetails());
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should handle null message")
        void shouldHandleNullMessage() {
            // When
            ActionFailedException exception = new ActionFailedException(ActionInterface.Type.TYPE, null);
            
            // Then
            assertNotNull(exception);
            assertEquals(ActionInterface.Type.TYPE, exception.getActionType());
            assertNull(exception.getActionDetails());
            assertTrue(exception.getMessage().contains("TYPE"));
        }
    }

    @Nested
    @DisplayName("Action Type Specific Tests")
    class ActionTypeSpecificTests {

        @Test
        @DisplayName("Should handle CLICK action failure")
        void shouldHandleClickActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.CLICK;
            String message = "Button not found at location (100, 200)";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertEquals("Action CLICK failed: Button not found at location (100, 200)", 
                        exception.getMessage());
        }

        @Test
        @DisplayName("Should handle FIND action failure")
        void shouldHandleFindActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.FIND;
            String message = "Pattern not found with similarity 0.95";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("FIND"));
            assertTrue(exception.getMessage().contains("0.95"));
        }

        @Test
        @DisplayName("Should handle TYPE action failure")
        void shouldHandleTypeActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.TYPE;
            String message = "Input field is disabled";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("TYPE"));
            assertTrue(exception.getMessage().contains("disabled"));
        }

        @Test
        @DisplayName("Should handle DRAG action failure")
        void shouldHandleDragActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.DRAG;
            String message = "Source element not found";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("DRAG"));
            assertTrue(exception.getMessage().contains("Source element"));
        }

        @Test
        @DisplayName("Should handle VANISH action timeout")
        void shouldHandleVanishActionTimeout() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.VANISH;
            String message = "Timeout after 30.5 seconds waiting for element to vanish";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("VANISH"));
            assertTrue(exception.getMessage().contains("30.5"));
            assertTrue(exception.getMessage().contains("Timeout"));
        }

        @Test
        @DisplayName("Should handle MOVE action failure")
        void shouldHandleMoveActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.MOVE;
            String message = "Cannot move to location outside screen boundaries";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("MOVE"));
            assertTrue(exception.getMessage().contains("boundaries"));
        }

        @Test
        @DisplayName("Should handle SCROLL_MOUSE_WHEEL action failure")
        void shouldHandleScrollMouseWheelActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.SCROLL_MOUSE_WHEEL;
            String message = "Scrollable area not found";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("SCROLL_MOUSE_WHEEL"));
            assertTrue(exception.getMessage().contains("Scrollable area"));
        }

        @Test
        @DisplayName("Should handle KEY_DOWN action failure")
        void shouldHandleKeyDownActionFailure() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.KEY_DOWN;
            String message = "Key combination not supported";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, message);
            
            // Then
            assertTrue(exception.getMessage().contains("KEY_DOWN"));
            assertTrue(exception.getMessage().contains("not supported"));
        }
    }

    @Nested
    @DisplayName("Exception Chaining Tests")
    class ExceptionChainingTests {

        @Test
        @DisplayName("Should preserve exception chain")
        void shouldPreserveExceptionChain() {
            // Given
            Exception root = new IllegalStateException("Screen not ready");
            RuntimeException middle = new RuntimeException("Cannot capture", root);
            ActionFailedException top = new ActionFailedException(
                ActionInterface.Type.CLICK, 
                "Button click failed", 
                middle
            );
            
            // Then
            assertEquals(middle, top.getCause());
            assertEquals(root, top.getCause().getCause());
            assertEquals("Screen not ready", top.getCause().getCause().getMessage());
        }

        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.FIND,
                "Pattern not found",
                null
            );
            
            // Then
            assertNull(exception.getCause());
            assertNotNull(exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Context Information Tests")
    class ContextInformationTests {

        @Test
        @DisplayName("Should preserve coordinates in message")
        void shouldPreserveCoordinatesInMessage() {
            // Given
            String message = "Element not found at location (1024, 768)";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.FIND,
                message
            );
            
            // Then
            assertTrue(exception.getActionDetails().contains("(1024, 768)"));
        }

        @Test
        @DisplayName("Should preserve similarity threshold")
        void shouldPreserveSimilarityThreshold() {
            // Given
            String message = "No match found with similarity >= 0.85";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.FIND,
                message
            );
            
            // Then
            assertTrue(exception.getActionDetails().contains("0.85"));
        }

        @Test
        @DisplayName("Should preserve retry information")
        void shouldPreserveRetryInformation() {
            // Given
            String message = "Failed after 3 retries with 2s delay";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.CLICK,
                message
            );
            
            // Then
            assertTrue(exception.getActionDetails().contains("3 retries"));
            assertTrue(exception.getActionDetails().contains("2s delay"));
        }

        @Test
        @DisplayName("Should preserve duration information")
        void shouldPreserveDurationInformation() {
            // Given
            String message = "Operation timed out after 45.3 seconds";
            
            // When
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.VANISH,
                message
            );
            
            // Then
            assertTrue(exception.getActionDetails().contains("45.3 seconds"));
        }
    }

    @Nested
    @DisplayName("ToString and Display Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have informative toString")
        void shouldHaveInformativeToString() {
            // Given
            ActionFailedException exception = new ActionFailedException(
                ActionInterface.Type.CLICK,
                "Button not clickable"
            );
            
            // When
            String str = exception.toString();
            
            // Then
            assertNotNull(str);
            assertTrue(str.contains("ActionFailedException"));
            assertTrue(str.contains("CLICK"));
        }

        @Test
        @DisplayName("Should format message consistently")
        void shouldFormatMessageConsistently() {
            // Given
            ActionInterface.Type actionType = ActionInterface.Type.FIND;
            String details = "Image not found";
            
            // When
            ActionFailedException exception = new ActionFailedException(actionType, details);
            
            // Then
            assertEquals("Action FIND failed: Image not found", exception.getMessage());
        }
    }
}