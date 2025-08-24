package io.github.jspinak.brobot.action.basic.wait;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for WaitVanish action - waiting for elements to disappear.
 * Tests vanish detection, timeout handling, and partial vanishing scenarios.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WaitVanishTest extends BrobotTestBase {
    
    @Mock private Find find;
    @Mock private ActionLifecycleManagement actionLifecycleManagement;
    @Mock private ActionResult actionResult;
    @Mock private ObjectCollection objectCollection;
    @Mock private StateImage stateImage1;
    @Mock private StateImage stateImage2;
    @Mock private Location location;
    
    private WaitVanish waitVanish;
    private VanishOptions vanishOptions;
    private List<Location> matchLocations;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        waitVanish = new WaitVanish(find, actionLifecycleManagement);
        
        // Setup default vanish options
        vanishOptions = new VanishOptions.Builder()
            .setTimeout(5.0)
            .build();
            
        // Setup mutable match locations list
        matchLocations = new ArrayList<>();
        
        // Setup default mock behaviors
        when(actionResult.getActionConfig()).thenReturn(vanishOptions);
        when(actionResult.getMatchLocations()).thenReturn(matchLocations);
        when(objectCollection.getStateImages()).thenReturn(List.of(stateImage1));
        when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt())).thenReturn(true);
    }
    
    @Nested
    @DisplayName("Constructor and Type Tests")
    class ConstructorAndTypeTests {
        
        @Test
        @DisplayName("Should create WaitVanish with dependencies")
        void shouldCreateWaitVanishWithDependencies() {
            assertNotNull(waitVanish);
            assertEquals(ActionInterface.Type.VANISH, waitVanish.getActionType());
        }
        
        @Test
        @DisplayName("Should return correct action type")
        void shouldReturnCorrectActionType() {
            assertEquals(ActionInterface.Type.VANISH, waitVanish.getActionType());
        }
    }
    
    @Nested
    @DisplayName("Successful Vanish Tests")
    class SuccessfulVanishTests {
        
        @Test
        @DisplayName("Should succeed when object vanishes immediately")
        void shouldSucceedWhenObjectVanishesImmediately() {
            // Given - object not found on first check
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(1)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should succeed when object vanishes after multiple checks")
        void shouldSucceedWhenObjectVanishesAfterMultipleChecks() {
            // Given - object found first 2 times, then vanishes
            AtomicInteger callCount = new AtomicInteger(0);
            doAnswer(invocation -> {
                if (callCount.incrementAndGet() <= 2) {
                    matchLocations.clear();
                    matchLocations.add(location);
                } else {
                    matchLocations.clear();
                }
                return null;
            }).when(find).perform(any(), any());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(3)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should succeed when all objects vanish")
        void shouldSucceedWhenAllObjectsVanish() {
            // Given - multiple objects that all vanish
            when(objectCollection.getStateImages()).thenReturn(List.of(stateImage1, stateImage2));
            matchLocations.clear(); // All vanished
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(1)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
            verify(actionLifecycleManagement).isOkToContinueAction(actionResult, 2);
        }
    }
    
    @Nested
    @DisplayName("Timeout Tests")
    class TimeoutTests {
        
        @Test
        @DisplayName("Should respect timeout when object persists")
        void shouldRespectTimeoutWhenObjectPersists() {
            // Given - object never vanishes
            matchLocations.add(location);
            
            // Mock time-based termination
            AtomicInteger checkCount = new AtomicInteger(0);
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt()))
                .thenAnswer(inv -> checkCount.incrementAndGet() < 5);
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, atLeast(4)).perform(actionResult, objectCollection);
            verify(actionResult, never()).setSuccess(true); // Should not succeed
        }
        
        @Test
        @DisplayName("Should use custom timeout from options")
        void shouldUseCustomTimeoutFromOptions() {
            // Given
            VanishOptions customTimeout = new VanishOptions.Builder()
                .setTimeout(2.0)
                .build();
            when(actionResult.getActionConfig()).thenReturn(customTimeout);
            matchLocations.add(location); // Never vanishes
            
            // Mock rapid time passage
            long startTime = System.currentTimeMillis();
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt()))
                .thenAnswer(inv -> (System.currentTimeMillis() - startTime) < 2100);
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, atLeastOnce()).perform(actionResult, objectCollection);
        }
        
        @Test
        @DisplayName("Should use default timeout when not specified")
        void shouldUseDefaultTimeoutWhenNotSpecified() {
            // Given - non-VanishOptions config
            ActionConfig genericConfig = mock(ActionConfig.class);
            when(actionResult.getActionConfig()).thenReturn(genericConfig);
            matchLocations.clear(); // Vanishes immediately
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(1)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
    }
    
    @Nested
    @DisplayName("Lifecycle Management Tests")
    class LifecycleManagementTests {
        
        @Test
        @DisplayName("Should stop when lifecycle indicates termination")
        void shouldStopWhenLifecycleIndicatesTermination() {
            // Given
            matchLocations.add(location);
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false); // Stop after 2 iterations
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(2)).perform(actionResult, objectCollection);
            verify(actionLifecycleManagement, times(3)).isOkToContinueAction(any(), anyInt());
        }
        
        @Test
        @DisplayName("Should pass correct image count to lifecycle")
        void shouldPassCorrectImageCountToLifecycle() {
            // Given
            List<StateImage> images = List.of(stateImage1, stateImage2, mock(StateImage.class));
            when(objectCollection.getStateImages()).thenReturn(images);
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(actionLifecycleManagement).isOkToContinueAction(eq(actionResult), countCaptor.capture());
            assertEquals(3, countCaptor.getValue());
        }
    }
    
    @Nested
    @DisplayName("Multiple ObjectCollection Tests")
    class MultipleObjectCollectionTests {
        
        @Test
        @DisplayName("Should process only first ObjectCollection")
        void shouldProcessOnlyFirstObjectCollection() {
            // Given
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);
            
            when(collection1.getStateImages()).thenReturn(List.of(stateImage1));
            when(collection2.getStateImages()).thenReturn(List.of(stateImage2));
            when(collection3.getStateImages()).thenReturn(List.of());
            
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, collection1, collection2, collection3);
            
            // Then
            verify(find).perform(actionResult, collection1);
            verify(find, never()).perform(actionResult, collection2);
            verify(find, never()).perform(actionResult, collection3);
        }
        
        @Test
        @DisplayName("Should handle empty ObjectCollections array")
        void shouldHandleEmptyObjectCollectionsArray() {
            // When
            waitVanish.perform(actionResult);
            
            // Then
            verify(find, never()).perform(any(), any());
            verify(actionResult, never()).setSuccess(anyBoolean());
        }
        
        @Test
        @DisplayName("Should handle single ObjectCollection")
        void shouldHandleSingleObjectCollection() {
            // Given
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
    }
    
    @Nested
    @DisplayName("Partial Vanish Tests")
    class PartialVanishTests {
        
        @Test
        @DisplayName("Should continue checking when some objects remain")
        void shouldContinueCheckingWhenSomeObjectsRemain() {
            // Given - starts with 2 locations, reduces to 1, then 0
            AtomicInteger callCount = new AtomicInteger(0);
            doAnswer(invocation -> {
                matchLocations.clear();
                int count = callCount.incrementAndGet();
                if (count == 1) {
                    matchLocations.add(location);
                    matchLocations.add(mock(Location.class));
                } else if (count == 2) {
                    matchLocations.add(location);
                }
                // count == 3: empty list
                return null;
            }).when(find).perform(any(), any());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(3)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should track match history before vanishing")
        void shouldTrackMatchHistoryBeforeVanishing() {
            // Given
            Location loc1 = mock(Location.class);
            Location loc2 = mock(Location.class);
            
            AtomicInteger callCount = new AtomicInteger(0);
            doAnswer(invocation -> {
                matchLocations.clear();
                if (callCount.incrementAndGet() == 1) {
                    matchLocations.add(loc1);
                    matchLocations.add(loc2);
                }
                return null;
            }).when(find).perform(any(), any());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            // The match locations from the last successful find are preserved
        }
    }
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle empty state images list")
        void shouldHandleEmptyStateImagesList() {
            // Given
            when(objectCollection.getStateImages()).thenReturn(new ArrayList<>());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, atLeastOnce()).perform(actionResult, objectCollection);
            verify(actionLifecycleManagement).isOkToContinueAction(actionResult, 0);
        }
        
        @Test
        @DisplayName("Should handle null state images list")
        void shouldHandleNullStateImagesList() {
            // Given
            when(objectCollection.getStateImages()).thenReturn(null);
            
            // When/Then
            assertThrows(NullPointerException.class,
                () -> waitVanish.perform(actionResult, objectCollection));
        }
        
        @Test
        @DisplayName("Should handle immediate lifecycle termination")
        void shouldHandleImmediateLifecycleTermination() {
            // Given
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt()))
                .thenReturn(false);
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, never()).perform(any(), any());
            verify(actionResult, never()).setSuccess(anyBoolean());
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should accept VanishOptions configuration")
        void shouldAcceptVanishOptionsConfiguration() {
            // Given
            VanishOptions options = new VanishOptions.Builder()
                .setTimeout(3.0)
                .setPauseAfterEnd(0.5)
                .build();
            when(actionResult.getActionConfig()).thenReturn(options);
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            assertEquals(3.0, options.getTimeout(), 0.01);
        }
        
        @Test
        @DisplayName("Should work with generic ActionConfig")
        void shouldWorkWithGenericActionConfig() {
            // Given
            ActionConfig genericConfig = mock(ActionConfig.class);
            when(actionResult.getActionConfig()).thenReturn(genericConfig);
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            // Uses default timeout
        }
        
        @Test
        @DisplayName("Should handle null configuration")
        void shouldHandleNullConfiguration() {
            // Given
            when(actionResult.getActionConfig()).thenReturn(null);
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(actionResult).setSuccess(true);
            // Uses default timeout
        }
    }
    
    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {
        
        @Test
        @DisplayName("Should wait for loading screen to vanish")
        void shouldWaitForLoadingScreenToVanish() {
            // Given - simulating loading screen disappearing after 3 checks
            StateImage loadingSpinner = mock(StateImage.class);
            when(objectCollection.getStateImages()).thenReturn(List.of(loadingSpinner));
            
            AtomicInteger checkCount = new AtomicInteger(0);
            doAnswer(inv -> {
                matchLocations.clear();
                if (checkCount.incrementAndGet() < 3) {
                    matchLocations.add(location);
                }
                return null;
            }).when(find).perform(any(), any());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(3)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should detect dialog dismissal")
        void shouldDetectDialogDismissal() {
            // Given - dialog vanishes immediately
            StateImage dialogImage = mock(StateImage.class);
            when(objectCollection.getStateImages()).thenReturn(List.of(dialogImage));
            matchLocations.clear();
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(1)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should handle animation completion")
        void shouldHandleAnimationCompletion() {
            // Given - animation elements gradually disappear
            StateImage animElement1 = mock(StateImage.class);
            StateImage animElement2 = mock(StateImage.class);
            when(objectCollection.getStateImages()).thenReturn(List.of(animElement1, animElement2));
            
            AtomicInteger checkCount = new AtomicInteger(0);
            doAnswer(inv -> {
                matchLocations.clear();
                int count = checkCount.incrementAndGet();
                if (count == 1) {
                    matchLocations.add(location);
                    matchLocations.add(mock(Location.class));
                } else if (count == 2) {
                    matchLocations.add(location);
                }
                // count >= 3: all vanished
                return null;
            }).when(find).perform(any(), any());
            
            // When
            waitVanish.perform(actionResult, objectCollection);
            
            // Then
            verify(find, times(3)).perform(actionResult, objectCollection);
            verify(actionResult).setSuccess(true);
        }
    }
}