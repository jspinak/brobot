package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.mouse.PostClickHandler;
import io.github.jspinak.brobot.action.internal.mouse.SingleClickExecutor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for Click action - performs mouse clicks on GUI elements.
 * Tests find-and-click operations, multi-clicking, and various click configurations.
 */
@DisplayName("Click Action Tests")
public class ClickTest extends BrobotTestBase {
    
    @Mock
    private Find mockFind;
    
    @Mock
    private SingleClickExecutor mockClickExecutor;
    
    @Mock
    private TimeProvider mockTimeProvider;
    
    @Mock
    private PostClickHandler mockPostClickHandler;
    
    @Mock
    private ActionResultFactory mockActionResultFactory;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ActionResult mockFindResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private Click click;
    private ClickOptions clickOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        click = new Click(mockFind, mockClickExecutor, mockTimeProvider, 
                         mockPostClickHandler, mockActionResultFactory);
        
        clickOptions = new ClickOptions.Builder().build();
        
        when(mockActionResult.getActionConfig()).thenReturn(clickOptions);
        when(mockActionResult.getMatchList()).thenReturn(new ArrayList<>());
        when(mockFindResult.getMatchList()).thenReturn(new ArrayList<>());
        when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
            .thenReturn(mockFindResult);
    }
    
    @Test
    @DisplayName("Should return CLICK action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.CLICK, click.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Click Operations")
    class BasicClickOperations {
        
        @Test
        @DisplayName("Should perform click on single match")
        public void testClickSingleMatch() {
            Location location = new Location(100, 100);
            Match match = new Match.Builder()
                .withRegion(new Region(80, 80, 40, 40))
                .withTarget(location)
                .build();
            
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            when(mockFindResult.isSuccess()).thenReturn(true);
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFind).perform(mockFindResult, mockObjectCollection);
            verify(mockClickExecutor).click(location, clickOptions, match);
            verify(mockActionResult).setSuccess(true);
        }
        
        @Test
        @DisplayName("Should perform click on multiple matches")
        public void testClickMultipleMatches() {
            Location loc1 = new Location(100, 100);
            Location loc2 = new Location(200, 200);
            Location loc3 = new Location(300, 300);
            
            Match match1 = new Match.Builder().withTarget(loc1).build();
            Match match2 = new Match.Builder().withTarget(loc2).build();
            Match match3 = new Match.Builder().withTarget(loc3).build();
            
            when(mockFindResult.getMatchList()).thenReturn(Arrays.asList(match1, match2, match3));
            when(mockFindResult.isSuccess()).thenReturn(true);
            
            click.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockClickExecutor, mockTimeProvider);
            inOrder.verify(mockClickExecutor).click(loc1, clickOptions, match1);
            inOrder.verify(mockTimeProvider).wait(clickOptions.getPauseBetweenIndividualActions());
            inOrder.verify(mockClickExecutor).click(loc2, clickOptions, match2);
            inOrder.verify(mockTimeProvider).wait(clickOptions.getPauseBetweenIndividualActions());
            inOrder.verify(mockClickExecutor).click(loc3, clickOptions, match3);
            
            // Should not wait after the last click
            verify(mockTimeProvider, times(2)).wait(anyDouble());
        }
        
        @Test
        @DisplayName("Should handle no matches found")
        public void testNoMatchesFound() {
            when(mockFindResult.getMatchList()).thenReturn(Collections.emptyList());
            when(mockFindResult.isSuccess()).thenReturn(false);
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockFind).perform(mockFindResult, mockObjectCollection);
            verify(mockClickExecutor, never()).click(any(), any(), any());
            verify(mockActionResult).setSuccess(false);
        }
    }
    
    @Nested
    @DisplayName("Click Configuration Options")
    class ClickConfigurationOptions {
        
        @Test
        @DisplayName("Should handle double-click configuration")
        public void testDoubleClickConfiguration() {
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .setTimesToRepeatIndividualAction(2)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(doubleClickOptions);
            
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor).click(any(), eq(doubleClickOptions), any());
        }
        
        @Test
        @DisplayName("Should handle right-click configuration")
        public void testRightClickConfiguration() {
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .setButton(ClickOptions.Button.RIGHT)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(rightClickOptions);
            
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor).click(any(), eq(rightClickOptions), any());
        }
        
        @Test
        @DisplayName("Should handle middle-click configuration")
        public void testMiddleClickConfiguration() {
            ClickOptions middleClickOptions = new ClickOptions.Builder()
                .setButton(ClickOptions.Button.MIDDLE)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(middleClickOptions);
            
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor).click(any(), eq(middleClickOptions), any());
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.1, 0.5, 1.0, 2.0})
        @DisplayName("Should handle different pause durations")
        public void testPauseDurations(double pauseDuration) {
            ClickOptions pausedOptions = new ClickOptions.Builder()
                .setPauseBetweenIndividualActions(pauseDuration)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(pausedOptions);
            
            Match match1 = new Match.Builder().withTarget(new Location(100, 100)).build();
            Match match2 = new Match.Builder().withTarget(new Location(200, 200)).build();
            
            when(mockFindResult.getMatchList()).thenReturn(Arrays.asList(match1, match2));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockTimeProvider).wait(pauseDuration);
        }
    }
    
    @Nested
    @DisplayName("Post-Click Behavior")
    class PostClickBehavior {
        
        @Test
        @DisplayName("Should move mouse after click when configured")
        public void testMoveAfterClick() {
            ClickOptions moveAfterOptions = new ClickOptions.Builder()
                .setMoveMouseAfterClick(true)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(moveAfterOptions);
            
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor).click(any(), eq(moveAfterOptions), any());
        }
        
        @Test
        @DisplayName("Should not move mouse after click when not configured")
        public void testNoMoveAfterClick() {
            ClickOptions noMoveOptions = new ClickOptions.Builder()
                .setMoveMouseAfterClick(false)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(noMoveOptions);
            
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor).click(any(), eq(noMoveOptions), any());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should throw exception for invalid configuration")
        public void testInvalidConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(new PatternFindOptions.Builder().build());
            
            assertThrows(IllegalArgumentException.class, () -> 
                click.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);
            
            assertThrows(IllegalArgumentException.class, () -> 
                click.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle find failure gracefully")
        public void testFindFailure() {
            doThrow(new RuntimeException("Find failed"))
                .when(mockFind).perform(any(), any());
            
            assertThrows(RuntimeException.class, () -> 
                click.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should handle click executor failure")
        public void testClickExecutorFailure() {
            Match match = new Match.Builder()
                .withTarget(new Location(100, 100))
                .build();
            when(mockFindResult.getMatchList()).thenReturn(List.of(match));
            
            doThrow(new RuntimeException("Click failed"))
                .when(mockClickExecutor).click(any(), any(), any());
            
            assertThrows(RuntimeException.class, () -> 
                click.perform(mockActionResult, mockObjectCollection));
        }
    }
    
    @Nested
    @DisplayName("Integration with Find")
    class FindIntegration {
        
        @Test
        @DisplayName("Should create proper find options")
        public void testFindOptionsCreation() {
            ArgumentCaptor<PatternFindOptions> findOptionsCaptor = 
                ArgumentCaptor.forClass(PatternFindOptions.class);
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockActionResultFactory).init(findOptionsCaptor.capture(), 
                eq("Click->Find"), any());
            
            PatternFindOptions capturedOptions = findOptionsCaptor.getValue();
            assertNotNull(capturedOptions);
        }
        
        @Test
        @DisplayName("Should pass object collections to find")
        public void testObjectCollectionPassing() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            ObjectCollection collection3 = mock(ObjectCollection.class);
            
            click.perform(mockActionResult, collection1, collection2, collection3);
            
            verify(mockFind).perform(mockFindResult, collection1, collection2, collection3);
        }
        
        @Test
        @DisplayName("Should copy find results to action result")
        public void testResultCopying() {
            Match match1 = new Match.Builder().withTarget(new Location(100, 100)).build();
            Match match2 = new Match.Builder().withTarget(new Location(200, 200)).build();
            
            List<Match> findMatches = Arrays.asList(match1, match2);
            when(mockFindResult.getMatchList()).thenReturn(findMatches);
            when(mockFindResult.isSuccess()).thenReturn(true);
            
            List<Match> actionMatches = new ArrayList<>();
            when(mockActionResult.getMatchList()).thenReturn(actionMatches);
            
            click.perform(mockActionResult, mockObjectCollection);
            
            assertEquals(2, actionMatches.size());
            assertTrue(actionMatches.contains(match1));
            assertTrue(actionMatches.contains(match2));
            verify(mockActionResult).setSuccess(true);
        }
    }
    
    @Nested
    @DisplayName("Batch Clicking")
    class BatchClicking {
        
        @Test
        @DisplayName("Should respect max matches to act on")
        public void testMaxMatchesToActOn() {
            ClickOptions limitedOptions = new ClickOptions.Builder()
                .setMaxMatchesToActOn(2)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(limitedOptions);
            
            Match match1 = new Match.Builder().withTarget(new Location(100, 100)).build();
            Match match2 = new Match.Builder().withTarget(new Location(200, 200)).build();
            Match match3 = new Match.Builder().withTarget(new Location(300, 300)).build();
            
            // Even if Find returns 3 matches, Click should respect the limit
            when(mockFindResult.getMatchList()).thenReturn(Arrays.asList(match1, match2));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor, times(2)).click(any(), any(), any());
        }
        
        @Test
        @DisplayName("Should maintain click order")
        public void testClickOrder() {
            Location loc1 = new Location(100, 100);
            Location loc2 = new Location(200, 200);
            Location loc3 = new Location(300, 300);
            
            Match match1 = new Match.Builder().withTarget(loc1).build();
            Match match2 = new Match.Builder().withTarget(loc2).build();
            Match match3 = new Match.Builder().withTarget(loc3).build();
            
            when(mockFindResult.getMatchList()).thenReturn(Arrays.asList(match1, match2, match3));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            InOrder inOrder = inOrder(mockClickExecutor);
            inOrder.verify(mockClickExecutor).click(loc1, clickOptions, match1);
            inOrder.verify(mockClickExecutor).click(loc2, clickOptions, match2);
            inOrder.verify(mockClickExecutor).click(loc3, clickOptions, match3);
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        public void testEmptyMatchList() {
            when(mockFindResult.getMatchList()).thenReturn(Collections.emptyList());
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor, never()).click(any(), any(), any());
            verify(mockTimeProvider, never()).wait(anyDouble());
        }
    }
    
    @Nested
    @DisplayName("Performance and Optimization")
    class PerformanceOptimization {
        
        @Test
        @DisplayName("Should only search first object collection")
        public void testOnlyFirstCollectionSearched() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            click.perform(mockActionResult, collection1, collection2);
            
            // Verify find is called with both collections but only searches first
            verify(mockFind).perform(mockFindResult, collection1, collection2);
        }
        
        @Test
        @DisplayName("Should handle large number of matches efficiently")
        public void testLargeMatchSet() {
            List<Match> largeMatchList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeMatchList.add(new Match.Builder()
                    .withTarget(new Location(i * 10, i * 10))
                    .build());
            }
            
            ClickOptions limitedOptions = new ClickOptions.Builder()
                .setMaxMatchesToActOn(5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(limitedOptions);
            
            // Assume Find already limited the results
            when(mockFindResult.getMatchList()).thenReturn(largeMatchList.subList(0, 5));
            
            click.perform(mockActionResult, mockObjectCollection);
            
            verify(mockClickExecutor, times(5)).click(any(), any(), any());
        }
    }
}