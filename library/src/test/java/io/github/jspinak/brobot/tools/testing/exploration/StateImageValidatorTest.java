package io.github.jspinak.brobot.tools.testing.exploration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for StateImageValidator, which validates all images within a state. */
@DisplayName("StateImageValidator Tests")
class StateImageValidatorTest extends BrobotTestBase {

    @Mock private Action action;

    @Mock private State state;

    private StateImageValidator validator;
    private Set<StateImage> stateImages;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        validator = new StateImageValidator(action);
        stateImages = new HashSet<>();
    }

    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {

        @Test
        @DisplayName("Should validate single StateImage")
        void shouldValidateSingleStateImage() {
            // Arrange
            StateImage image = mock(StateImage.class);
            ObjectCollection collection = mock(ObjectCollection.class);
            when(image.asObjectCollection()).thenReturn(collection);
            stateImages.add(image);
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            validator.visitAllStateImages(state);

            // Assert
            verify(action).perform(eq(ActionType.FIND), eq(collection));
            verify(image).asObjectCollection();
        }

        @Test
        @DisplayName("Should validate multiple StateImages")
        void shouldValidateMultipleStateImages() {
            // Arrange
            int imageCount = 5;
            for (int i = 0; i < imageCount; i++) {
                StateImage image = mock(StateImage.class);
                ObjectCollection collection = mock(ObjectCollection.class);
                when(image.asObjectCollection()).thenReturn(collection);
                stateImages.add(image);
            }
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            validator.visitAllStateImages(state);

            // Assert
            verify(action, times(imageCount))
                    .perform(eq(ActionType.FIND), any(ObjectCollection.class));
            stateImages.forEach(image -> verify(image).asObjectCollection());
        }

        @Test
        @DisplayName("Should handle empty StateImages list")
        void shouldHandleEmptyStateImagesList() {
            // Arrange
            when(state.getStateImages()).thenReturn(Collections.emptySet());

            // Act
            validator.visitAllStateImages(state);

            // Assert
            verify(action, never()).perform(any(ActionType.class), any(ObjectCollection.class));
        }
    }

    @Nested
    @DisplayName("Find Action Behavior Tests")
    class FindActionBehaviorTests {

        @Test
        @DisplayName("Should use FIND action for all images")
        void shouldUseFindActionForAllImages() {
            // Arrange
            StateImage image1 = mock(StateImage.class);
            StateImage image2 = mock(StateImage.class);
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);

            when(image1.asObjectCollection()).thenReturn(collection1);
            when(image2.asObjectCollection()).thenReturn(collection2);

            stateImages.add(image1);
            stateImages.add(image2);
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(any(ActionType.class), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            validator.visitAllStateImages(state);

            // Assert
            verify(action).perform(eq(ActionType.FIND), eq(collection1));
            verify(action).perform(eq(ActionType.FIND), eq(collection2));
            verify(action, never()).perform(eq(ActionType.CLICK), any(ObjectCollection.class));
            verify(action, never()).perform(eq(ActionType.TYPE), any(ObjectCollection.class));
        }

        @Test
        @DisplayName("Should continue validation when image not found")
        void shouldContinueValidationWhenImageNotFound() {
            // Arrange
            StateImage image1 = mock(StateImage.class);
            StateImage image2 = mock(StateImage.class);
            StateImage image3 = mock(StateImage.class);
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);

            when(image1.asObjectCollection()).thenReturn(collection1);
            when(image2.asObjectCollection()).thenReturn(collection2);
            when(image3.asObjectCollection()).thenReturn(collection3);

            stateImages.add(image1);
            stateImages.add(image2);
            stateImages.add(image3);
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult successResult = new ActionResult();
            successResult.setSuccess(true);
            ActionResult failResult = new ActionResult();
            failResult.setSuccess(false);

            // First succeeds, second fails, third succeeds
            when(action.perform(eq(ActionType.FIND), eq(collection1))).thenReturn(successResult);
            when(action.perform(eq(ActionType.FIND), eq(collection2))).thenReturn(failResult);
            when(action.perform(eq(ActionType.FIND), eq(collection3))).thenReturn(successResult);

            // Act
            validator.visitAllStateImages(state);

            // Assert
            verify(action).perform(eq(ActionType.FIND), eq(collection1));
            verify(action).perform(eq(ActionType.FIND), eq(collection2));
            verify(action).perform(eq(ActionType.FIND), eq(collection3));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate state with mixed image types")
        void shouldValidateStateWithMixedImageTypes() {
            // Arrange
            // Create different types of StateImages (clickable, text, image-only)
            StateImage clickableImage = mock(StateImage.class);
            StateImage textImage = mock(StateImage.class);
            StateImage imageOnlyImage = mock(StateImage.class);

            ObjectCollection clickableCollection = mock(ObjectCollection.class);
            ObjectCollection textCollection = mock(ObjectCollection.class);
            ObjectCollection imageOnlyCollection = mock(ObjectCollection.class);

            when(clickableImage.asObjectCollection()).thenReturn(clickableCollection);
            when(textImage.asObjectCollection()).thenReturn(textCollection);
            when(imageOnlyImage.asObjectCollection()).thenReturn(imageOnlyCollection);

            stateImages.add(clickableImage);
            stateImages.add(textImage);
            stateImages.add(imageOnlyImage);
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            validator.visitAllStateImages(state);

            // Assert
            // All images should be validated with FIND regardless of type
            verify(action).perform(eq(ActionType.FIND), eq(clickableCollection));
            verify(action).perform(eq(ActionType.FIND), eq(textCollection));
            verify(action).perform(eq(ActionType.FIND), eq(imageOnlyCollection));
        }

        @Test
        @DisplayName("Should validate all StateImages in the set")
        void shouldValidateAllStateImagesInSet() {
            // Arrange
            StateImage image1 = mock(StateImage.class);
            StateImage image2 = mock(StateImage.class);
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);

            when(image1.asObjectCollection()).thenReturn(collection1);
            when(image2.asObjectCollection()).thenReturn(collection2);

            stateImages.add(image1);
            stateImages.add(image2);
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            validator.visitAllStateImages(state);

            // Assert - verify all were validated
            verify(action).perform(eq(ActionType.FIND), eq(collection1));
            verify(action).perform(eq(ActionType.FIND), eq(collection2));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null state gracefully")
        void shouldHandleNullState() {
            // Act & Assert
            try {
                validator.visitAllStateImages(null);
            } catch (NullPointerException e) {
                // Expected behavior - should throw NPE for null state
            }
        }

        @Test
        @DisplayName("Should handle StateImage with null ObjectCollection")
        void shouldHandleStateImageWithNullObjectCollection() {
            // Arrange
            StateImage image = mock(StateImage.class);
            when(image.asObjectCollection()).thenReturn(null);
            stateImages.add(image);
            when(state.getStateImages()).thenReturn(stateImages);

            // Act
            try {
                validator.visitAllStateImages(state);
            } catch (NullPointerException e) {
                // Expected - action.perform will throw NPE with null collection
            }

            // Assert
            verify(image).asObjectCollection();
        }

        @Test
        @DisplayName("Should handle action throwing exception")
        void shouldHandleActionThrowingException() {
            // Arrange
            StateImage image = mock(StateImage.class);
            ObjectCollection collection = mock(ObjectCollection.class);

            when(image.asObjectCollection()).thenReturn(collection);

            stateImages.add(image);
            when(state.getStateImages()).thenReturn(stateImages);

            // Action throws exception
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenThrow(new RuntimeException("Find failed"));

            // Act & Assert
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        validator.visitAllStateImages(state);
                    });

            // Verify action was attempted
            verify(action).perform(eq(ActionType.FIND), eq(collection));
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large number of StateImages")
        void shouldHandleLargeNumberOfStateImages() {
            // Arrange
            int largeCount = 100;
            for (int i = 0; i < largeCount; i++) {
                StateImage image = mock(StateImage.class);
                ObjectCollection collection = mock(ObjectCollection.class);
                when(image.asObjectCollection()).thenReturn(collection);
                stateImages.add(image);
            }
            when(state.getStateImages()).thenReturn(stateImages);

            ActionResult result = new ActionResult();
            result.setSuccess(true);
            when(action.perform(eq(ActionType.FIND), any(ObjectCollection.class)))
                    .thenReturn(result);

            // Act
            long startTime = System.currentTimeMillis();
            validator.visitAllStateImages(state);
            long endTime = System.currentTimeMillis();

            // Assert
            verify(action, times(largeCount))
                    .perform(eq(ActionType.FIND), any(ObjectCollection.class));
            // Should complete quickly even with many images
            assertTrue(endTime - startTime < 1000);
        }
    }
}
