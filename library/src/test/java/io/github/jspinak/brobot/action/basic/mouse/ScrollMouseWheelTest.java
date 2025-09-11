package io.github.jspinak.brobot.action.basic.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.MouseWheelScroller;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ScrollMouseWheel action - mouse wheel scrolling operation. Tests
 * scrolling directions, step counts, and configuration validation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScrollMouseWheelTest extends BrobotTestBase {

    @Mock private MouseWheelScroller mouseWheelScroller;
    @Mock private ActionResult actionResult;
    @Mock private ObjectCollection objectCollection;

    private ScrollMouseWheel scrollMouseWheel;
    private ScrollOptions scrollOptions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        scrollMouseWheel = new ScrollMouseWheel(mouseWheelScroller);

        // Setup default scroll options
        scrollOptions =
                new ScrollOptions.Builder()
                        .setDirection(ScrollOptions.Direction.DOWN)
                        .setScrollSteps(3)
                        .build();

        // Setup default mock behaviors
        when(actionResult.getActionConfig()).thenReturn(scrollOptions);
        when(mouseWheelScroller.scroll(any())).thenReturn(true);
    }

    @Nested
    @DisplayName("Constructor and Type Tests")
    class ConstructorAndTypeTests {

        @Test
        @DisplayName("Should create ScrollMouseWheel with dependencies")
        void shouldCreateScrollMouseWheelWithDependencies() {
            assertNotNull(scrollMouseWheel);
            assertEquals(ActionInterface.Type.SCROLL_MOUSE_WHEEL, scrollMouseWheel.getActionType());
        }

        @Test
        @DisplayName("Should return correct action type")
        void shouldReturnCorrectActionType() {
            assertEquals(ActionInterface.Type.SCROLL_MOUSE_WHEEL, scrollMouseWheel.getActionType());
        }
    }

    @Nested
    @DisplayName("Basic Scrolling Tests")
    class BasicScrollingTests {

        @Test
        @DisplayName("Should scroll down with default configuration")
        void shouldScrollDownWithDefaultConfiguration() {
            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());

            ScrollOptions captured = captor.getValue();
            assertEquals(ScrollOptions.Direction.DOWN, captured.getDirection());
            assertEquals(3, captured.getScrollSteps());
        }

        @Test
        @DisplayName("Should scroll up when configured")
        void shouldScrollUpWhenConfigured() {
            // Given
            ScrollOptions upOptions =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(5)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(upOptions);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());

            ScrollOptions captured = captor.getValue();
            assertEquals(ScrollOptions.Direction.UP, captured.getDirection());
            assertEquals(5, captured.getScrollSteps());
        }
    }

    @Nested
    @DisplayName("Scroll Steps Tests")
    class ScrollStepsTests {

        @Test
        @DisplayName("Should scroll single step")
        void shouldScrollSingleStep() {
            // Given
            ScrollOptions singleStep =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(1)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(singleStep);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            assertEquals(1, captor.getValue().getScrollSteps());
        }

        @Test
        @DisplayName("Should scroll multiple steps")
        void shouldScrollMultipleSteps() {
            // Given
            ScrollOptions multipleSteps =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(10)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(multipleSteps);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            assertEquals(10, captor.getValue().getScrollSteps());
        }

        @Test
        @DisplayName("Should scroll large number of steps")
        void shouldScrollLargeNumberOfSteps() {
            // Given
            ScrollOptions largeSteps =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(100)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(largeSteps);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            assertEquals(100, captor.getValue().getScrollSteps());
        }
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("Should require ScrollOptions configuration")
        void shouldRequireScrollOptionsConfiguration() {
            // Given
            when(actionResult.getActionConfig())
                    .thenReturn(mock(io.github.jspinak.brobot.action.ActionConfig.class));

            // When/Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> scrollMouseWheel.perform(actionResult, objectCollection),
                    "ScrollMouseWheel requires ScrollOptions configuration");
        }

        @Test
        @DisplayName("Should validate configuration type at runtime")
        void shouldValidateConfigurationTypeAtRuntime() {
            // Given - wrong config type
            io.github.jspinak.brobot.action.ActionConfig wrongConfig =
                    mock(io.github.jspinak.brobot.action.ActionConfig.class);
            when(actionResult.getActionConfig()).thenReturn(wrongConfig);

            // When/Then
            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> scrollMouseWheel.perform(actionResult, objectCollection));
            assertTrue(exception.getMessage().contains("ScrollOptions"));
        }

        @Test
        @DisplayName("Should accept valid ScrollOptions configuration")
        void shouldAcceptValidScrollOptionsConfiguration() {
            // Given
            ScrollOptions validOptions =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(3)
                            .setPauseAfterEnd(0.5)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(validOptions);

            // When/Then - should not throw
            assertDoesNotThrow(() -> scrollMouseWheel.perform(actionResult, objectCollection));
            verify(mouseWheelScroller).scroll(validOptions);
        }
    }

    @Nested
    @DisplayName("Direction Combination Tests")
    class DirectionCombinationTests {

        @Test
        @DisplayName("Should handle UP direction with minimal steps")
        void shouldHandleUpDirectionWithMinimalSteps() {
            // Given
            ScrollOptions upMinimal =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(1)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(upMinimal);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            ScrollOptions captured = captor.getValue();
            assertEquals(ScrollOptions.Direction.UP, captured.getDirection());
            assertEquals(1, captured.getScrollSteps());
        }

        @Test
        @DisplayName("Should handle DOWN direction with maximal steps")
        void shouldHandleDownDirectionWithMaximalSteps() {
            // Given
            ScrollOptions downMaximal =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(Integer.MAX_VALUE)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(downMaximal);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            ScrollOptions captured = captor.getValue();
            assertEquals(ScrollOptions.Direction.DOWN, captured.getDirection());
            assertEquals(Integer.MAX_VALUE, captured.getScrollSteps());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate exception from scroller")
        void shouldPropagateExceptionFromScroller() {
            // Given
            when(mouseWheelScroller.scroll(any())).thenThrow(new RuntimeException("Scroll failed"));

            // When/Then
            assertThrows(
                    RuntimeException.class,
                    () -> scrollMouseWheel.perform(actionResult, objectCollection));
        }

        @Test
        @DisplayName("Should handle null configuration gracefully")
        void shouldHandleNullConfigurationGracefully() {
            // Given
            when(actionResult.getActionConfig()).thenReturn(null);

            // When/Then - ScrollMouseWheel throws IllegalArgumentException for null config
            assertThrows(
                    IllegalArgumentException.class,
                    () -> scrollMouseWheel.perform(actionResult, objectCollection));
        }

        @Test
        @DisplayName("Should handle scroller returning false")
        void shouldHandleScrollerReturningFalse() {
            // Given
            when(mouseWheelScroller.scroll(any())).thenReturn(false);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            verify(mouseWheelScroller).scroll(any());
            // Action completes even if scroller returns false
        }
    }

    @Nested
    @DisplayName("Multiple ObjectCollection Tests")
    class MultipleObjectCollectionTests {

        @Test
        @DisplayName("Should ignore additional ObjectCollections")
        void shouldIgnoreAdditionalObjectCollections() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);

            // When
            scrollMouseWheel.perform(actionResult, collection1, collection2, collection3);

            // Then
            verify(mouseWheelScroller, times(1)).scroll(any());
            // ScrollMouseWheel doesn't iterate through collections - just scrolls once
        }

        @Test
        @DisplayName("Should work with no ObjectCollections")
        void shouldWorkWithNoObjectCollections() {
            // When
            scrollMouseWheel.perform(actionResult);

            // Then
            verify(mouseWheelScroller).scroll(scrollOptions);
        }

        @Test
        @DisplayName("Should work with single ObjectCollection")
        void shouldWorkWithSingleObjectCollection() {
            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            verify(mouseWheelScroller).scroll(scrollOptions);
        }
    }

    @Nested
    @DisplayName("ScrollOptions Builder Tests")
    class ScrollOptionsBuilderTests {

        @Test
        @DisplayName("Should use default values when not specified")
        void shouldUseDefaultValuesWhenNotSpecified() {
            // Given
            ScrollOptions defaultOptions = new ScrollOptions.Builder().build();
            when(actionResult.getActionConfig()).thenReturn(defaultOptions);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            ScrollOptions captured = captor.getValue();
            assertEquals(ScrollOptions.Direction.DOWN, captured.getDirection()); // Default
            assertEquals(3, captured.getScrollSteps()); // Default
        }

        @Test
        @DisplayName("Should preserve pause timing configuration")
        void shouldPreservePauseTimingConfiguration() {
            // Given
            ScrollOptions withPause =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(5)
                            .setPauseAfterEnd(2.5)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(withPause);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            ArgumentCaptor<ScrollOptions> captor = ArgumentCaptor.forClass(ScrollOptions.class);
            verify(mouseWheelScroller).scroll(captor.capture());
            ScrollOptions captured = captor.getValue();
            assertEquals(2.5, captured.getPauseAfterEnd(), 0.01);
        }

        @Test
        @DisplayName("Should handle minimal scroll steps")
        void shouldHandleMinimalScrollSteps() {
            // Given - Builder ensures minimum of 1
            ScrollOptions minimalSteps =
                    new ScrollOptions.Builder()
                            .setScrollSteps(0) // Should be corrected to 1
                            .build();

            // Then
            assertTrue(minimalSteps.getScrollSteps() >= 1);
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Should perform rapid scroll down")
        void shouldPerformRapidScrollDown() {
            // Given - simulating rapid page scroll
            ScrollOptions rapidScroll =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(20)
                            .setPauseAfterEnd(0.1) // Short pause
                            .build();
            when(actionResult.getActionConfig()).thenReturn(rapidScroll);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            verify(mouseWheelScroller).scroll(rapidScroll);
        }

        @Test
        @DisplayName("Should perform precision scroll up")
        void shouldPerformPrecisionScrollUp() {
            // Given - simulating precise positioning
            ScrollOptions precisionScroll =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(1)
                            .setPauseAfterEnd(1.0) // Longer pause for precision
                            .build();
            when(actionResult.getActionConfig()).thenReturn(precisionScroll);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            verify(mouseWheelScroller).scroll(precisionScroll);
        }

        @Test
        @DisplayName("Should support zoom operation pattern")
        void shouldSupportZoomOperationPattern() {
            // Given - some apps use wheel for zoom
            ScrollOptions zoomIn =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP) // Zoom in
                            .setScrollSteps(3)
                            .build();
            when(actionResult.getActionConfig()).thenReturn(zoomIn);

            // When
            scrollMouseWheel.perform(actionResult, objectCollection);

            // Then
            verify(mouseWheelScroller).scroll(zoomIn);
        }
    }
}
