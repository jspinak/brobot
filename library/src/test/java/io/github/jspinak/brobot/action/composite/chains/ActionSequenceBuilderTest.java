package io.github.jspinak.brobot.action.composite.chains;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ActionSequenceBuilder Tests")
public class ActionSequenceBuilderTest extends BrobotTestBase {
    
    private ActionSequenceBuilder actionSequenceBuilder;
    
    @Mock
    private ActionChainExecutor mockChainExecutor;
    
    @Mock
    private Action mockAction;
    
    @Mock
    private ActionResult mockSuccessResult;
    
    @Mock
    private ActionResult mockFailureResult;
    
    @Mock
    private StateImage mockStateImage;
    
    @Mock
    private Region mockRegion;
    
    @Mock
    private Location mockLocation;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        actionSequenceBuilder = new ActionSequenceBuilder(mockChainExecutor, mockAction);
        
        // Setup default mock behaviors
        when(mockSuccessResult.isSuccess()).thenReturn(true);
        when(mockFailureResult.isSuccess()).thenReturn(false);
        when(mockStateImage.getName()).thenReturn("TestImage");
        when(mockRegion.getCenter()).thenReturn(mockLocation);
        when(mockLocation.getX()).thenReturn(100);
        when(mockLocation.getY()).thenReturn(200);
    }
    
    @Nested
    @DisplayName("Right Click and Move Until Vanishes")
    class RightClickAndMoveUntilVanishes {
        
        @Test
        @DisplayName("Should perform right click and move sequence successfully")
        public void testSuccessfulRightClickAndMove() {
            // Setup mock to return success result
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                3, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should return false when action fails")
        public void testFailedRightClickAndMove() {
            // Setup mock to return failure result
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockFailureResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                3, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertFalse(result);
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5, 10})
        @DisplayName("Should attempt clicks specified number of times")
        public void testMultipleClickAttempts(int timesToClick) {
            // Setup mock to return failure then success
            when(mockChainExecutor.executeChain(any(), any()))
                .thenReturn(mockFailureResult)
                .thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                timesToClick, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should handle zero click attempts")
        public void testZeroClickAttempts() {
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                0, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertFalse(result);
            verify(mockChainExecutor, never()).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should handle negative click attempts")
        public void testNegativeClickAttempts() {
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                -1, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertFalse(result);
            verify(mockChainExecutor, never()).executeChain(any(), any());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 0.5, 0.2",
            "1.0, 0.0, 0.2",
            "1.0, 0.5, 0.0",
            "2.0, 1.0, 0.5"
        })
        @DisplayName("Should handle various pause timings")
        public void testVariousPauseTimings(double pauseBetween, double pauseBefore, double pauseAfter) {
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                1, pauseBetween, pauseBefore, pauseAfter, mockStateImage, 50, 50, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "100, 100",
            "-50, -50",
            "200, -100"
        })
        @DisplayName("Should handle various mouse movement offsets")
        public void testVariousMouseOffsets(int xMove, int yMove) {
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                1, 1.0, 0.5, 0.2, mockStateImage, xMove, yMove, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should handle null state image")
        public void testNullStateImage() {
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                3, 1.0, 0.5, 0.2, null, 50, 50, mockRegion
            );
            
            assertFalse(result);
        }
        
        @Test
        @DisplayName("Should handle null region")
        public void testNullRegion() {
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                3, 1.0, 0.5, 0.2, mockStateImage, 50, 50, null
            );
            
            // Should still work without region (region is for highlighting)
            assertTrue(result);
            verify(mockChainExecutor, atLeastOnce()).executeChain(any(), any());
        }
    }
    
    @Nested
    @DisplayName("Action Chain Building")
    class ActionChainBuilding {
        
        @Test
        @DisplayName("Should build action chain with correct options")
        public void testActionChainOptionsBuilding() {
            ArgumentCaptor<ObjectCollection> collectionCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            when(mockChainExecutor.executeChain(any(), collectionCaptor.capture())).thenReturn(mockSuccessResult);
            
            actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                1, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            ObjectCollection capturedCollection = collectionCaptor.getValue();
            assertNotNull(capturedCollection);
            assertTrue(capturedCollection.getStateImages().contains(mockStateImage));
        }
        
        @Test
        @DisplayName("Should execute chain executor with proper parameters")
        public void testChainExecutorInvocation() {
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                1, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            verify(mockChainExecutor).executeChain(any(), any());
        }
    }
    
    @Nested
    @DisplayName("Vanish Detection")
    class VanishDetection {
        
        @Test
        @DisplayName("Should return true immediately when vanish detected")
        public void testImmediateVanishDetection() {
            // First execution returns success (vanished)
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                5, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertTrue(result);
            // Should only execute once since vanish was detected
            verify(mockChainExecutor, times(1)).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should continue clicking until vanish or max attempts")
        public void testContinueUntilVanish() {
            // Return failure for first 2 attempts, then success
            when(mockChainExecutor.executeChain(any(), any()))
                .thenReturn(mockFailureResult)
                .thenReturn(mockFailureResult)
                .thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                5, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, times(3)).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should return false when max attempts reached without vanish")
        public void testMaxAttemptsWithoutVanish() {
            // Always return failure (no vanish)
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockFailureResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                3, 1.0, 0.5, 0.2, mockStateImage, 50, 50, mockRegion
            );
            
            assertFalse(result);
            verify(mockChainExecutor, times(3)).executeChain(any(), any());
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should handle typical UI interaction scenario")
        public void testTypicalUIInteraction() {
            // Simulate typical scenario: fails twice, then succeeds
            when(mockChainExecutor.executeChain(any(), any()))
                .thenReturn(mockFailureResult)
                .thenReturn(mockFailureResult)
                .thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                10, 2.0, 1.0, 0.5, mockStateImage, 100, 100, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, times(3)).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should handle fast UI response")
        public void testFastUIResponse() {
            // Succeeds immediately
            when(mockChainExecutor.executeChain(any(), any())).thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                10, 0.1, 0.1, 0.1, mockStateImage, 10, 10, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, times(1)).executeChain(any(), any());
        }
        
        @Test
        @DisplayName("Should handle slow UI response")
        public void testSlowUIResponse() {
            // Fails many times before succeeding
            when(mockChainExecutor.executeChain(any(), any()))
                .thenReturn(mockFailureResult)
                .thenReturn(mockFailureResult)
                .thenReturn(mockFailureResult)
                .thenReturn(mockFailureResult)
                .thenReturn(mockSuccessResult);
            
            boolean result = actionSequenceBuilder.rightClickAndMoveUntilVanishes(
                10, 5.0, 2.0, 1.0, mockStateImage, 200, 200, mockRegion
            );
            
            assertTrue(result);
            verify(mockChainExecutor, times(5)).executeChain(any(), any());
        }
    }
}