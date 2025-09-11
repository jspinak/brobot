package io.github.jspinak.brobot.action.composite.drag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for refactored Drag composite action - drag-and-drop operations. Tests
 * the orchestration of MouseDown, MoveMouse, and MouseUp actions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DragTest extends BrobotTestBase {

    @Mock private MouseDown mockMouseDown;
    @Mock private MoveMouse mockMoveMouse;
    @Mock private MouseUp mockMouseUp;

    @Mock private StateImage sourceImage;
    @Mock private StateImage targetImage;
    @Mock private StateLocation sourceLocation;
    @Mock private StateLocation targetLocation;
    @Mock private StateRegion sourceRegion;
    @Mock private StateRegion targetRegion;

    @Mock private Location fromLocation;
    @Mock private Location toLocation;
    @Mock private Match fromMatch;
    @Mock private Match toMatch;

    private Drag drag;
    private DragOptions dragOptions;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        drag = new Drag(mockMouseDown, mockMoveMouse, mockMouseUp);

        // Setup default drag options
        dragOptions =
                new DragOptions.Builder()
                        .setDelayBetweenMouseDownAndMove(0.5)
                        .setDelayAfterDrag(0.5)
                        .build();

        // Setup match locations
        when(fromLocation.getX()).thenReturn(100);
        when(fromLocation.getY()).thenReturn(100);
        when(toLocation.getX()).thenReturn(300);
        when(toLocation.getY()).thenReturn(300);

        when(fromMatch.getTarget()).thenReturn(fromLocation);
        when(toMatch.getTarget()).thenReturn(toLocation);

        // Setup default mock behaviors for successful operations
        doAnswer(
                        invocation -> {
                            ActionResult result = invocation.getArgument(0);
                            result.setSuccess(true);
                            return null;
                        })
                .when(mockMoveMouse)
                .perform(any(), any());

        doAnswer(
                        invocation -> {
                            ActionResult result = invocation.getArgument(0);
                            result.setSuccess(true);
                            return null;
                        })
                .when(mockMouseDown)
                .perform(any(), any());

        doAnswer(
                        invocation -> {
                            ActionResult result = invocation.getArgument(0);
                            result.setSuccess(true);
                            return null;
                        })
                .when(mockMouseUp)
                .perform(any(), any());
    }

    @Nested
    @DisplayName("Basic Drag Operations")
    class BasicDragOperations {

        @Test
        @DisplayName("Should perform drag between two locations")
        void shouldPerformDragBetweenTwoLocations() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            when(targetLocation.getLocation()).thenReturn(toLocation);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());
            assertNotNull(actionResult.getMatchList());
            assertEquals(2, actionResult.getMatchList().size());

            // Verify the correct sequence
            verify(mockMoveMouse, times(2)).perform(any(), any());
            verify(mockMouseDown, times(1)).perform(any(), any());
            verify(mockMouseUp, times(1)).perform(any(), any());
        }

        @Test
        @DisplayName("Should drag from StateRegion to StateRegion")
        void shouldDragFromRegionToRegion() {
            // Arrange
            Region sourceSearchRegion = mock(Region.class);
            Region targetSearchRegion = mock(Region.class);
            when(sourceSearchRegion.getX()).thenReturn(100);
            when(sourceSearchRegion.getY()).thenReturn(100);
            when(sourceSearchRegion.getW()).thenReturn(50);
            when(sourceSearchRegion.getH()).thenReturn(50);
            when(targetSearchRegion.getX()).thenReturn(300);
            when(targetSearchRegion.getY()).thenReturn(300);
            when(targetSearchRegion.getW()).thenReturn(50);
            when(targetSearchRegion.getH()).thenReturn(50);

            when(sourceRegion.getSearchRegion()).thenReturn(sourceSearchRegion);
            when(targetRegion.getSearchRegion()).thenReturn(targetSearchRegion);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withRegions(sourceRegion).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withRegions(targetRegion).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());
            verify(mockMoveMouse, times(2)).perform(any(), any());
            verify(mockMouseDown, times(1)).perform(any(), any());
            verify(mockMouseUp, times(1)).perform(any(), any());
        }

        @Test
        @DisplayName("Should use previous ActionResult matches")
        void shouldUsePreviousActionResultMatches() {
            // Arrange
            ActionResult sourceResult = new ActionResult();
            sourceResult.setSuccess(true);
            List<Match> sourceMatches = new ArrayList<>();
            sourceMatches.add(fromMatch);
            sourceResult.setMatchList(sourceMatches);

            ActionResult targetResult = new ActionResult();
            targetResult.setSuccess(true);
            List<Match> targetMatches = new ArrayList<>();
            targetMatches.add(toMatch);
            targetResult.setMatchList(targetMatches);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withMatches(sourceResult).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withMatches(targetResult).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());
            verify(mockMoveMouse, times(2)).perform(any(), any());
            verify(mockMouseDown, times(1)).perform(any(), any());
            verify(mockMouseUp, times(1)).perform(any(), any());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should fail when no object collections provided")
        void shouldFailWithNoObjectCollections() {
            // Arrange
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult);

            // Assert
            assertFalse(actionResult.isSuccess());
        }

        @Test
        @DisplayName("Should fail when only one object collection provided")
        void shouldFailWithOnlyOneObjectCollection() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection);

            // Assert
            assertFalse(actionResult.isSuccess());
        }

        @Test
        @DisplayName("Should fail when source collection is empty")
        void shouldFailWhenSourceCollectionIsEmpty() {
            // Arrange
            ObjectCollection emptySource = new ObjectCollection.Builder().build();
            when(targetLocation.getLocation()).thenReturn(toLocation);
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, emptySource, targetCollection);

            // Assert
            assertFalse(actionResult.isSuccess());
        }

        @Test
        @DisplayName("Should fail when target collection is empty")
        void shouldFailWhenTargetCollectionIsEmpty() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection emptyTarget = new ObjectCollection.Builder().build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, emptyTarget);

            // Assert
            assertFalse(actionResult.isSuccess());
        }

        @Test
        @DisplayName("Should release mouse button if move to target fails")
        void shouldReleaseMouseButtonIfMoveToTargetFails() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            when(targetLocation.getLocation()).thenReturn(toLocation);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            // Make the second move fail
            doAnswer(
                            invocation -> {
                                ActionResult result = invocation.getArgument(0);
                                result.setSuccess(true);
                                return null;
                            })
                    .doAnswer(
                            invocation -> {
                                ActionResult result = invocation.getArgument(0);
                                result.setSuccess(false);
                                return null;
                            })
                    .when(mockMoveMouse)
                    .perform(any(), any());

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertFalse(actionResult.isSuccess());
            verify(mockMouseUp, times(1)).perform(any(), any()); // Should still release
        }
    }

    @Nested
    @DisplayName("Configuration Options")
    class ConfigurationOptions {

        @Test
        @DisplayName("Should apply custom drag options")
        void shouldApplyCustomDragOptions() {
            // Arrange
            // Using builder to create custom press options
            MousePressOptions customPressOptions = MousePressOptions.builder().build();

            DragOptions customDragOptions =
                    new DragOptions.Builder()
                            .setDelayBetweenMouseDownAndMove(1.0)
                            .setDelayAfterDrag(2.0)
                            .setMousePressOptions(customPressOptions)
                            .build();

            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            when(targetLocation.getLocation()).thenReturn(toLocation);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(customDragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());

            // Verify custom options were used
            ArgumentCaptor<ActionResult> mouseDownCaptor =
                    ArgumentCaptor.forClass(ActionResult.class);
            verify(mockMouseDown).perform(mouseDownCaptor.capture(), any());
            MouseDownOptions capturedMouseDownOptions =
                    (MouseDownOptions) mouseDownCaptor.getValue().getActionConfig();
            assertEquals(1.0, capturedMouseDownOptions.getPauseAfterEnd());

            ArgumentCaptor<ActionResult> mouseUpCaptor =
                    ArgumentCaptor.forClass(ActionResult.class);
            verify(mockMouseUp).perform(mouseUpCaptor.capture(), any());
            MouseUpOptions capturedMouseUpOptions =
                    (MouseUpOptions) mouseUpCaptor.getValue().getActionConfig();
            assertEquals(2.0, capturedMouseUpOptions.getPauseAfterEnd());
        }
    }

    @Nested
    @DisplayName("Result Validation")
    class ResultValidation {

        @Test
        @DisplayName("Should add movement to action result")
        void shouldAddMovementToActionResult() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            when(targetLocation.getLocation()).thenReturn(toLocation);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());
            assertNotNull(actionResult.getMovements());
            assertEquals(1, actionResult.getMovements().size());

            Movement movement = actionResult.getMovements().get(0);
            assertEquals(fromLocation, movement.getStartLocation());
            assertEquals(toLocation, movement.getEndLocation());
        }

        @Test
        @DisplayName("Should create matches for source and target")
        void shouldCreateMatchesForSourceAndTarget() {
            // Arrange
            when(sourceLocation.getLocation()).thenReturn(fromLocation);
            when(targetLocation.getLocation()).thenReturn(toLocation);

            ObjectCollection sourceCollection =
                    new ObjectCollection.Builder().withLocations(sourceLocation).build();
            ObjectCollection targetCollection =
                    new ObjectCollection.Builder().withLocations(targetLocation).build();

            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(dragOptions);

            // Act
            drag.perform(actionResult, sourceCollection, targetCollection);

            // Assert
            assertTrue(actionResult.isSuccess());
            assertNotNull(actionResult.getMatchList());
            assertEquals(2, actionResult.getMatchList().size());

            Match sourceMatch = actionResult.getMatchList().get(0);
            assertEquals(fromLocation, sourceMatch.getTarget());
            assertEquals(1.0, sourceMatch.getScore());

            Match targetMatch = actionResult.getMatchList().get(1);
            assertEquals(toLocation, targetMatch.getTarget());
            assertEquals(1.0, targetMatch.getScore());
        }
    }
}
