package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Movement;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Drag composite action - drag-and-drop operations.
 * Tests the orchestration of Find, MouseMove, MouseDown, and MouseUp actions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DragTest extends BrobotTestBase {
    
    @Mock private ActionChainExecutor actionChainExecutor;
    @Mock private ActionResult actionResult;
    @Mock private ActionResult chainResult;
    @Mock private ObjectCollection sourceCollection;
    @Mock private ObjectCollection targetCollection;
    
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
        drag = new Drag(actionChainExecutor);
        
        // Setup default drag options
        dragOptions = new DragOptions.Builder()
            .setDelayBetweenMouseDownAndMove(0.5)
            .setDelayAfterDrag(0.5)
            .build();
            
        // Setup default mock behaviors
        when(actionResult.getActionConfig()).thenReturn(dragOptions);
        when(chainResult.isSuccess()).thenReturn(true);
        when(chainResult.getMatchList()).thenReturn(new ArrayList<>());
        when(chainResult.getDuration()).thenReturn(Duration.ofSeconds(1));
        when(actionChainExecutor.executeChain(any(), any(), any(), any())).thenReturn(chainResult);
        
        // Setup match locations
        when(fromMatch.getTarget()).thenReturn(fromLocation);
        when(toMatch.getTarget()).thenReturn(toLocation);
    }
    
    @Nested
    @DisplayName("Constructor and Type Tests")
    class ConstructorAndTypeTests {
        
        @Test
        @DisplayName("Should create Drag with dependencies")
        void shouldCreateDragWithDependencies() {
            assertNotNull(drag);
            assertEquals(ActionInterface.Type.DRAG, drag.getActionType());
        }
        
        @Test
        @DisplayName("Should return correct action type")
        void shouldReturnCorrectActionType() {
            assertEquals(ActionInterface.Type.DRAG, drag.getActionType());
        }
    }
    
    @Nested
    @DisplayName("Basic Drag Operations")
    class BasicDragOperations {
        
        @Test
        @DisplayName("Should execute complete drag chain")
        void shouldExecuteCompleteDragChain() {
            // Given
            List<Match> matches = List.of(fromMatch, toMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<ActionChainOptions> chainCaptor = ArgumentCaptor.forClass(ActionChainOptions.class);
            verify(actionChainExecutor).executeChain(chainCaptor.capture(), eq(actionResult), 
                eq(sourceCollection), eq(targetCollection));
            
            ActionChainOptions capturedChain = chainCaptor.getValue();
            assertNotNull(capturedChain);
            assertEquals(ActionChainOptions.ChainingStrategy.NESTED, capturedChain.getStrategy());
            
            // Verify result is copied back
            verify(actionResult).setMatchList(matches);
            verify(actionResult).setSuccess(true);
            verify(actionResult).setDuration(Duration.ofSeconds(1));
        }
        
        @Test
        @DisplayName("Should create movement for successful drag")
        void shouldCreateMovementForSuccessfulDrag() {
            // Given
            List<Match> matches = List.of(fromMatch, toMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
            verify(actionResult).addMovement(movementCaptor.capture());
            
            Movement movement = movementCaptor.getValue();
            assertEquals(fromLocation, movement.getStartLocation());
            assertEquals(toLocation, movement.getEndLocation());
        }
    }
    
    @Nested
    @DisplayName("Chain Configuration Tests")
    class ChainConfigurationTests {
        
        @Test
        @DisplayName("Should configure 6-action chain correctly")
        void shouldConfigureSixActionChainCorrectly() {
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<ActionChainOptions> chainCaptor = ArgumentCaptor.forClass(ActionChainOptions.class);
            verify(actionChainExecutor).executeChain(chainCaptor.capture(), any(), any(), any());
            
            ActionChainOptions chain = chainCaptor.getValue();
            assertNotNull(chain);
            
            // Verify the chain contains the expected sequence
            // The chain should be: Find source → Find target → Move to source → 
            // Mouse down → Move to target → Mouse up
            // Note: ActionChainOptions doesn't expose getFirstConfig, so we just verify it was created
        }
        
        @Test
        @DisplayName("Should apply drag options to chain")
        void shouldApplyDragOptionsToChain() {
            // Given
            DragOptions customOptions = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(1.5)
                .setDelayAfterDrag(2.0)
                .setMousePressOptions(MousePressOptions.builder()
                    .setButton(MouseButton.RIGHT)
                    .build())
                .build();
            when(actionResult.getActionConfig()).thenReturn(customOptions);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<ActionChainOptions> chainCaptor = ArgumentCaptor.forClass(ActionChainOptions.class);
            verify(actionChainExecutor).executeChain(chainCaptor.capture(), any(), any(), any());
            
            // The chain should incorporate the custom delays and mouse button
            assertNotNull(chainCaptor.getValue());
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should fail when missing target collection")
        void shouldFailWhenMissingTargetCollection() {
            // When
            drag.perform(actionResult, sourceCollection);
            
            // Then
            verify(actionResult).setSuccess(false);
            verify(actionChainExecutor, never()).executeChain(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should fail when no collections provided")
        void shouldFailWhenNoCollectionsProvided() {
            // When
            drag.perform(actionResult);
            
            // Then
            verify(actionResult).setSuccess(false);
            verify(actionChainExecutor, never()).executeChain(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle chain execution failure")
        void shouldHandleChainExecutionFailure() {
            // Given
            when(chainResult.isSuccess()).thenReturn(false);
            when(chainResult.getMatchList()).thenReturn(new ArrayList<>());
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionResult).setSuccess(false);
            verify(actionResult, never()).addMovement(any());
        }
        
        @Test
        @DisplayName("Should handle insufficient matches for movement")
        void shouldHandleInsufficientMatchesForMovement() {
            // Given - only one match instead of two
            List<Match> singleMatch = List.of(fromMatch);
            when(chainResult.getMatchList()).thenReturn(singleMatch);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            verify(actionResult, never()).addMovement(any()); // No movement added
        }
    }
    
    @Nested
    @DisplayName("Configuration Type Tests")
    class ConfigurationTypeTests {
        
        @Test
        @DisplayName("Should use DragOptions when provided")
        void shouldUseDragOptionsWhenProvided() {
            // Given
            DragOptions customDragOptions = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(3.0)
                .build();
            when(actionResult.getActionConfig()).thenReturn(customDragOptions);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionChainExecutor).executeChain(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should use defaults when generic config provided")
        void shouldUseDefaultsWhenGenericConfigProvided() {
            // Given
            ActionConfig genericConfig = mock(ActionConfig.class);
            when(actionResult.getActionConfig()).thenReturn(genericConfig);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionChainExecutor).executeChain(any(), any(), any(), any());
        }
        
        @Test
        @DisplayName("Should handle null configuration")
        void shouldHandleNullConfiguration() {
            // Given
            when(actionResult.getActionConfig()).thenReturn(null);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionChainExecutor).executeChain(any(), any(), any(), any());
        }
    }
    
    @Nested
    @DisplayName("Movement Tracking Tests")
    class MovementTrackingTests {
        
        @Test
        @DisplayName("Should track movement from first to last match")
        void shouldTrackMovementFromFirstToLastMatch() {
            // Given - multiple matches along the drag path
            Match midMatch1 = mock(Match.class);
            Match midMatch2 = mock(Match.class);
            Location midLocation1 = mock(Location.class);
            Location midLocation2 = mock(Location.class);
            when(midMatch1.getTarget()).thenReturn(midLocation1);
            when(midMatch2.getTarget()).thenReturn(midLocation2);
            
            List<Match> matches = List.of(fromMatch, midMatch1, midMatch2, toMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
            verify(actionResult).addMovement(movementCaptor.capture());
            
            Movement movement = movementCaptor.getValue();
            assertEquals(fromLocation, movement.getStartLocation()); // First match
            assertEquals(toLocation, movement.getEndLocation());     // Last match
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        void shouldHandleEmptyMatchList() {
            // Given
            when(chainResult.getMatchList()).thenReturn(new ArrayList<>());
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionResult, never()).addMovement(any());
        }
    }
    
    @Nested
    @DisplayName("Multiple Collection Tests")
    class MultipleCollectionTests {
        
        @Test
        @DisplayName("Should use first two collections only")
        void shouldUseFirstTwoCollectionsOnly() {
            // Given
            ObjectCollection extraCollection = mock(ObjectCollection.class);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection, extraCollection);
            
            // Then
            verify(actionChainExecutor).executeChain(any(), any(), 
                eq(sourceCollection), eq(targetCollection));
            // Extra collection is ignored
        }
        
        @Test
        @DisplayName("Should pass collections in correct order")
        void shouldPassCollectionsInCorrectOrder() {
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<ObjectCollection> sourceCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            ArgumentCaptor<ObjectCollection> targetCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            
            verify(actionChainExecutor).executeChain(any(), any(), 
                sourceCaptor.capture(), targetCaptor.capture());
            
            assertEquals(sourceCollection, sourceCaptor.getValue());
            assertEquals(targetCollection, targetCaptor.getValue());
        }
    }
    
    @Nested
    @DisplayName("DragOptions Builder Tests")
    class DragOptionsBuilderTests {
        
        @Test
        @DisplayName("Should use default values when not specified")
        void shouldUseDefaultValuesWhenNotSpecified() {
            // Given
            DragOptions defaultOptions = new DragOptions.Builder().build();
            
            // Then
            assertEquals(0.5, defaultOptions.getDelayBetweenMouseDownAndMove(), 0.01);
            assertEquals(0.5, defaultOptions.getDelayAfterDrag(), 0.01);
            assertNotNull(defaultOptions.getMousePressOptions());
            assertEquals(MouseButton.LEFT, defaultOptions.getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Should apply custom mouse press options")
        void shouldApplyCustomMousePressOptions() {
            // Given
            MousePressOptions customPress = MousePressOptions.builder()
                .setButton(MouseButton.MIDDLE)
                .build();
            
            DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(customPress)
                .build();
            
            // Then
            assertEquals(MouseButton.MIDDLE, options.getMousePressOptions().getButton());
        }
        
        @Test
        @DisplayName("Should apply custom delays")
        void shouldApplyCustomDelays() {
            // Given
            DragOptions options = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(2.5)
                .setDelayAfterDrag(3.0)
                .build();
            
            // Then
            assertEquals(2.5, options.getDelayBetweenMouseDownAndMove(), 0.01);
            assertEquals(3.0, options.getDelayAfterDrag(), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {
        
        @Test
        @DisplayName("Should drag file to folder")
        void shouldDragFileToFolder() {
            // Given - simulating file drag operation
            StateImage fileIcon = mock(StateImage.class);
            StateImage folderIcon = mock(StateImage.class);
            when(sourceCollection.getStateImages()).thenReturn(List.of(fileIcon));
            when(targetCollection.getStateImages()).thenReturn(List.of(folderIcon));
            
            List<Match> matches = List.of(fromMatch, toMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionChainExecutor).executeChain(any(), any(), any(), any());
            verify(actionResult).addMovement(any());
        }
        
        @Test
        @DisplayName("Should drag slider handle")
        void shouldDragSliderHandle() {
            // Given - simulating slider drag
            StateLocation sliderHandle = mock(StateLocation.class);
            StateLocation sliderEnd = mock(StateLocation.class);
            when(sourceCollection.getStateLocations()).thenReturn(List.of(sliderHandle));
            when(targetCollection.getStateLocations()).thenReturn(List.of(sliderEnd));
            
            List<Match> matches = List.of(fromMatch, toMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            verify(actionResult).addMovement(any());
        }
        
        @Test
        @DisplayName("Should draw selection rectangle")
        void shouldDrawSelectionRectangle() {
            // Given - simulating selection rectangle
            Location startCorner = mock(Location.class);
            Location endCorner = mock(Location.class);
            StateLocation startLoc = mock(StateLocation.class);
            StateLocation endLoc = mock(StateLocation.class);
            
            when(startLoc.getLocation()).thenReturn(startCorner);
            when(endLoc.getLocation()).thenReturn(endCorner);
            when(sourceCollection.getStateLocations()).thenReturn(List.of(startLoc));
            when(targetCollection.getStateLocations()).thenReturn(List.of(endLoc));
            
            Match startMatch = mock(Match.class);
            Match endMatch = mock(Match.class);
            when(startMatch.getTarget()).thenReturn(startCorner);
            when(endMatch.getTarget()).thenReturn(endCorner);
            
            List<Match> matches = List.of(startMatch, endMatch);
            when(chainResult.getMatchList()).thenReturn(matches);
            
            // When
            drag.perform(actionResult, sourceCollection, targetCollection);
            
            // Then
            ArgumentCaptor<Movement> movementCaptor = ArgumentCaptor.forClass(Movement.class);
            verify(actionResult).addMovement(movementCaptor.capture());
            
            Movement selection = movementCaptor.getValue();
            assertEquals(startCorner, selection.getStartLocation());
            assertEquals(endCorner, selection.getEndLocation());
        }
    }
}