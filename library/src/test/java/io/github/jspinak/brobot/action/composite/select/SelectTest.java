package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Select Tests")

@DisabledInCI
public class SelectTest extends BrobotTestBase {
    
    private Select select;
    
    @Mock
    private Action mockAction;
    
    @Mock
    private StateImage mockStateImage;
    
    @Mock
    private StateImage mockConfirmImage;
    
    @Mock
    private Match mockMatch;
    
    private ObjectCollection findCollection;
    private ObjectCollection confirmCollection;
    private ObjectCollection swipeFromCollection;
    private ObjectCollection swipeToCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        select = new Select(mockAction);
        
        findCollection = new ObjectCollection.Builder()
            .withImages(mockStateImage)
            .build();
        
        confirmCollection = new ObjectCollection.Builder()
            .withImages(mockConfirmImage)
            .build();
        
        swipeFromCollection = new ObjectCollection.Builder()
            .withLocations(new Location(100, 500))
            .build();
        
        swipeToCollection = new ObjectCollection.Builder()
            .withLocations(new Location(100, 100))
            .build();
    }
    
    @Nested
    @DisplayName("Basic Selection Operations")
    class BasicSelectionOperations {
        
        @Test
        @DisplayName("Should find and click without confirmation")
        public void testFindAndClickNoConfirmation() {
            ActionResult successResult = new ActionResult();
            successResult.setSuccess(true);
            successResult.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(successResult);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(0, sao.getTotalSwipes());
            assertEquals(successResult, sao.getFoundMatches());
            
            verify(mockAction, times(1)).perform(any(ActionConfig.class), eq(findCollection));
        }
        
        @Test
        @DisplayName("Should find, click and confirm")
        public void testFindClickAndConfirm() {
            ActionResult findSuccess = new ActionResult();
            findSuccess.setSuccess(true);
            findSuccess.setMatchList(Arrays.asList(mockMatch));
            ActionResult confirmSuccess = new ActionResult();
            confirmSuccess.setSuccess(true);
            confirmSuccess.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setConfirmationObjectCollection(confirmCollection)
                .setConfirmActionConfig(new PatternFindOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(findSuccess);
            when(mockAction.perform(any(PatternFindOptions.class), eq(confirmCollection)))
                .thenReturn(confirmSuccess);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(findSuccess, sao.getFoundMatches());
            assertEquals(confirmSuccess, sao.getFoundConfirmations());
            
            verify(mockAction, times(1)).perform(any(ActionConfig.class), eq(findCollection));
            verify(mockAction, times(1)).perform(any(ActionConfig.class), eq(confirmCollection));
        }
        
        @Test
        @DisplayName("Should swipe when target not found")
        public void testSwipeWhenNotFound() {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            ActionResult foundAfterSwipe = new ActionResult();
            foundAfterSwipe.setSuccess(true);
            foundAfterSwipe.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(notFound)
                .thenReturn(foundAfterSwipe);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(1, sao.getTotalSwipes());
            
            verify(mockAction, times(2)).perform(any(ActionConfig.class), eq(findCollection));
            verify(mockAction, times(1)).perform(any(ActionConfig.class), eq(swipeFromCollection), eq(swipeToCollection));
        }
    }
    
    @Nested
    @DisplayName("Max Swipes Behavior")
    class MaxSwipesBehavior {
        
        @Test
        @DisplayName("Should stop after max swipes reached")
        public void testMaxSwipesReached() {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(3)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(notFound);
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertFalse(sao.isSuccess());
            assertEquals(3, sao.getTotalSwipes());
            
            verify(mockAction, times(3)).perform(any(ActionConfig.class), eq(findCollection));
            verify(mockAction, times(3)).perform(any(ActionConfig.class), eq(swipeFromCollection), eq(swipeToCollection));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10, 20})
        @DisplayName("Should respect different max swipe values")
        public void testDifferentMaxSwipeValues(int maxSwipes) {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(maxSwipes)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                .thenReturn(notFound);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class), any(ObjectCollection.class)))
                .thenReturn(notFound);
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertEquals(maxSwipes, sao.getTotalSwipes());
        }
    }
    
    @Nested
    @DisplayName("Confirmation Scenarios")
    class ConfirmationScenarios {
        
        @Test
        @DisplayName("Should fail if confirmation not found")
        public void testConfirmationNotFound() {
            ActionResult findSuccess = new ActionResult();
            findSuccess.setSuccess(true);
            findSuccess.setMatchList(Arrays.asList(mockMatch));
            ActionResult confirmFailed = new ActionResult();
            confirmFailed.setSuccess(false);
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setConfirmationObjectCollection(confirmCollection)
                .setConfirmActionConfig(new PatternFindOptions.Builder().build())
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(2)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(findSuccess)
                .thenReturn(notFound);
            when(mockAction.perform(any(PatternFindOptions.class), eq(confirmCollection)))
                .thenReturn(confirmFailed);
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertFalse(sao.isSuccess());
            assertEquals(2, sao.getTotalSwipes());
            assertEquals(confirmFailed, sao.getFoundConfirmations());
        }
        
        @Test
        @DisplayName("Should retry after confirmation failure")
        public void testRetryAfterConfirmationFailure() {
            ActionResult findSuccess = new ActionResult();
            findSuccess.setSuccess(true);
            findSuccess.setMatchList(Arrays.asList(mockMatch));
            ActionResult confirmFailed = new ActionResult();
            confirmFailed.setSuccess(false);
            ActionResult confirmSuccess = new ActionResult();
            confirmSuccess.setSuccess(true);
            confirmSuccess.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setConfirmationObjectCollection(confirmCollection)
                .setConfirmActionConfig(new PatternFindOptions.Builder().build())
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(3)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(findSuccess);
            when(mockAction.perform(any(PatternFindOptions.class), eq(confirmCollection)))
                .thenReturn(confirmFailed)
                .thenReturn(confirmSuccess);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(confirmSuccess, sao.getFoundConfirmations());
        }
    }
    
    @Nested
    @DisplayName("State Management")
    class StateManagement {
        
        @Test
        @DisplayName("Should reset total swipes on each select call")
        public void testResetTotalSwipes() {
            ActionResult foundImmediately = new ActionResult();
            foundImmediately.setSuccess(true);
            foundImmediately.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            // Artificially set total swipes to non-zero
            sao.addSwipe();
            sao.addSwipe();
            assertEquals(2, sao.getTotalSwipes());
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(foundImmediately);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertEquals(0, sao.getTotalSwipes()); // Should be reset
        }
        
        @Test
        @DisplayName("Should track swipe count accurately")
        public void testSwipeCountTracking() {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class)))
                .thenReturn(notFound);
            when(mockAction.perform(any(ActionConfig.class), any(ObjectCollection.class), any(ObjectCollection.class)))
                .thenReturn(notFound);
            
            select.select(sao);
            
            assertEquals(5, sao.getTotalSwipes());
            
            // Verify swipe was called 5 times
            verify(mockAction, times(5)).perform(any(ActionConfig.class), eq(swipeFromCollection), eq(swipeToCollection));
        }
    }
    
    @Nested
    @DisplayName("Null Configuration Handling")
    class NullConfigurationHandling {
        
        @Test
        @DisplayName("Should handle null find configuration")
        public void testNullFindConfiguration() {
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(null)
                .setMaxSwipes(1)
                .build();
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertFalse(sao.isSuccess());
            verify(mockAction, never()).perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
        
        @Test
        @DisplayName("Should handle null click configuration")
        public void testNullClickConfiguration() {
            ActionResult findSuccess = new ActionResult();
            findSuccess.setSuccess(true);
            findSuccess.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(null)
                .setMaxSwipes(1)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(findSuccess);
            
            boolean result = select.select(sao);
            
            assertTrue(result); // Should still succeed without click
            assertTrue(sao.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle null swipe configuration")
        public void testNullSwipeConfiguration() {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setSwipeActionConfig(null)
                .setMaxSwipes(2)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(notFound);
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertEquals(2, sao.getTotalSwipes()); // Still counts iterations
            verify(mockAction, never()).perform(any(ActionConfig.class), any(ObjectCollection.class), any(ObjectCollection.class));
        }
    }
    
    @Nested
    @DisplayName("Complex Selection Patterns")
    class ComplexSelectionPatterns {
        
        @Test
        @DisplayName("Should find on third swipe")
        public void testFindOnThirdSwipe() {
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            ActionResult foundOnThird = new ActionResult();
            foundOnThird.setSuccess(true);
            foundOnThird.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(notFound)
                .thenReturn(notFound)
                .thenReturn(foundOnThird);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(2, sao.getTotalSwipes()); // Two swipes before finding
            
            verify(mockAction, times(3)).perform(any(ActionConfig.class), eq(findCollection));
            verify(mockAction, times(2)).perform(any(ActionConfig.class), eq(swipeFromCollection), eq(swipeToCollection));
        }
        
        @Test
        @DisplayName("Should handle alternating find and confirmation failures")
        public void testAlternatingFailures() {
            ActionResult findSuccess = new ActionResult();
            findSuccess.setSuccess(true);
            findSuccess.setMatchList(Arrays.asList(mockMatch));
            ActionResult notFound = new ActionResult();
            notFound.setSuccess(false);
            ActionResult confirmFailed = new ActionResult();
            confirmFailed.setSuccess(false);
            ActionResult confirmSuccess = new ActionResult();
            confirmSuccess.setSuccess(true);
            confirmSuccess.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setConfirmationObjectCollection(confirmCollection)
                .setConfirmActionConfig(new PatternFindOptions.Builder().build())
                .setSwipeFromObjColl(swipeFromCollection)
                .setSwipeToObjColl(swipeToCollection)
                .setSwipeActionConfig(new ScrollOptions.Builder().build())
                .setMaxSwipes(5)
                .build();
            
            when(mockAction.perform(any(PatternFindOptions.class), eq(findCollection)))
                .thenReturn(findSuccess)
                .thenReturn(notFound)
                .thenReturn(findSuccess);
            when(mockAction.perform(any(PatternFindOptions.class), eq(confirmCollection)))
                .thenReturn(confirmFailed)
                .thenReturn(confirmSuccess);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertTrue(sao.isSuccess());
            assertEquals(2, sao.getTotalSwipes());
        }
    }
    
    @Nested
    @DisplayName("Performance and Efficiency")
    class PerformanceAndEfficiency {
        
        @Test
        @DisplayName("Should stop immediately on first success")
        public void testStopOnFirstSuccess() {
            ActionResult immediateSuccess = new ActionResult();
            immediateSuccess.setSuccess(true);
            immediateSuccess.setMatchList(Arrays.asList(mockMatch));
            
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setClickActionConfig(new ClickOptions.Builder().build())
                .setMaxSwipes(100)
                .build();
            
            when(mockAction.perform(any(ActionConfig.class), eq(findCollection)))
                .thenReturn(immediateSuccess);
            
            boolean result = select.select(sao);
            
            assertTrue(result);
            assertEquals(0, sao.getTotalSwipes());
            
            // Should only call find once
            verify(mockAction, times(1)).perform(any(ActionConfig.class), eq(findCollection));
        }
        
        @Test
        @DisplayName("Should handle zero max swipes")
        public void testZeroMaxSwipes() {
            SelectActionObject sao = new SelectActionObject.Builder()
                .setFindActionConfig(new PatternFindOptions.Builder().build())
                .setFindObjectCollection(findCollection)
                .setMaxSwipes(0)
                .build();
            
            boolean result = select.select(sao);
            
            assertFalse(result);
            assertEquals(0, sao.getTotalSwipes());
            
            verify(mockAction, never()).perform(any(ActionConfig.class), any(ObjectCollection.class));
        }
    }
}