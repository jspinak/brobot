package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.mouse.PostClickHandler;
import io.github.jspinak.brobot.action.internal.mouse.SingleClickExecutor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.stateObject.StateImage;
import io.github.jspinak.brobot.model.state.stateObject.StateLocation;
import io.github.jspinak.brobot.model.state.stateObject.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Click Action Tests")
public class ClickActionTest extends BrobotTestBase {

    @Mock
    private Find mockFind;
    
    @Mock
    private SingleClickExecutor mockSingleClickExecutor;
    
    @Mock
    private PostClickHandler mockPostClickHandler;
    
    @Mock
    private ActionResultFactory mockActionResultFactory;
    
    @Mock
    private TimeProvider mockTimeProvider;
    
    private Click click;
    private ObjectCollection objectCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        click = new Click(mockFind, mockSingleClickExecutor, mockPostClickHandler, 
                         mockActionResultFactory, mockTimeProvider);
        objectCollection = new ObjectCollection();
    }
    
    @Nested
    @DisplayName("Basic Click Operations")
    class BasicClickOperations {
        
        @Test
        @DisplayName("Should return CLICK action type")
        public void testGetActionType() {
            assertEquals(ActionInterface.Type.CLICK, click.getActionType());
        }
        
        @Test
        @DisplayName("Should click on image match")
        public void testClickOnImageMatch() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.95)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockFind).perform(any(), any());
            verify(mockSingleClickExecutor).execute(any(), any(), eq(1));
            verify(mockPostClickHandler).handle(any(), any());
        }
        
        @Test
        @DisplayName("Should click on location")
        public void testClickOnLocation() {
            Location location = new Location(200, 200);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(eq(location), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), any(), eq(1));
            verify(mockPostClickHandler).handle(eq(location), any());
        }
        
        @Test
        @DisplayName("Should click on region center")
        public void testClickOnRegion() {
            Region region = new Region(50, 50, 100, 100);
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(region)
                .build();
            objectCollection.addStateRegion(stateRegion);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(Location.class), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(mockSingleClickExecutor).execute(locationCaptor.capture(), any(), eq(1));
            
            Location clickedLocation = locationCaptor.getValue();
            assertEquals(100, clickedLocation.getX());
            assertEquals(100, clickedLocation.getY());
        }
    }
    
    @Nested
    @DisplayName("Click Options Configuration")
    class ClickOptionsConfiguration {
        
        @Test
        @DisplayName("Should perform multiple clicks")
        public void testMultipleClicks() {
            Location location = new Location(150, 150);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withClicks(3)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), any(), eq(3));
        }
        
        @Test
        @DisplayName("Should apply pause after click")
        public void testPauseAfterClick() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withPauseAfterClick(0.5)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(0.5);
        }
        
        @Test
        @DisplayName("Should use configured click type")
        public void testClickType() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withClickType(ClickOptions.ClickType.RIGHT)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(options), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(options), anyInt());
        }
    }
    
    @Nested
    @DisplayName("Multiple Target Handling")
    class MultipleTargetHandling {
        
        @Test
        @DisplayName("Should click on all matches")
        public void testClickAllMatches() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).build(),
                new Match.Builder().withRegion(new Region(200, 200, 50, 50)).build(),
                new Match.Builder().withRegion(new Region(300, 300, 50, 50)).build()
            );
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatchList(matches)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .withClickAll(true)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, times(3)).execute(any(), any(), anyInt());
        }
        
        @Test
        @DisplayName("Should click on best match only")
        public void testClickBestMatch() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).withScore(0.85).build(),
                new Match.Builder().withRegion(new Region(200, 200, 50, 50)).withScore(0.95).build(),
                new Match.Builder().withRegion(new Region(300, 300, 50, 50)).withScore(0.90).build()
            );
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatchList(matches)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .withClickAll(false)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, times(1)).execute(any(), any(), anyInt());
        }
        
        @Test
        @DisplayName("Should handle mixed object types")
        public void testMixedObjectTypes() {
            StateImage stateImage = StateImage.builder().build();
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(new Location(400, 400))
                .build();
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(new Region(500, 500, 100, 100))
                .build();
            
            objectCollection.addStateImage(stateImage);
            objectCollection.addStateLocation(stateLocation);
            objectCollection.addStateRegion(stateRegion);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions options = new ClickOptions.Builder()
                .withClickAll(true)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, atLeast(3)).execute(any(), any(), anyInt());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle find failure")
        public void testFindFailure() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(false)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            verify(mockSingleClickExecutor, never()).execute(any(), any(), anyInt());
        }
        
        @Test
        @DisplayName("Should handle click execution failure")
        public void testClickExecutionFailure() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(false);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle empty object collection")
        public void testEmptyObjectCollection() {
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            verify(mockSingleClickExecutor, never()).execute(any(), any(), anyInt());
        }
        
        @Test
        @DisplayName("Should handle null object collection")
        public void testNullObjectCollection() {
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            
            ActionResult result = click.perform(null);
            
            assertFalse(result.isSuccess());
            verify(mockSingleClickExecutor, never()).execute(any(), any(), anyInt());
        }
    }
    
    @Nested
    @DisplayName("Integration with Find")
    class FindIntegration {
        
        @Test
        @DisplayName("Should use find options from click options")
        public void testFindOptionsIntegration() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withSimilarity(0.8)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withFindOptions(findOptions)
                .build();
            objectCollection.setActionConfig(clickOptions);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(clickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            ArgumentCaptor<ObjectCollection> objCollCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            verify(mockFind).perform(objCollCaptor.capture(), any());
            
            ObjectCollection capturedObjColl = objCollCaptor.getValue();
            assertEquals(findOptions, capturedObjColl.getActionConfig());
        }
        
        @Test
        @DisplayName("Should reuse existing matches")
        public void testReuseExistingMatches() {
            Match existingMatch = new Match.Builder()
                .withRegion(new Region(150, 150, 60, 60))
                .withScore(0.92)
                .build();
            
            objectCollection.addMatch(existingMatch);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(new ClickOptions())
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockFind, never()).perform(any(), any());
            verify(mockSingleClickExecutor).execute(any(), any(), anyInt());
        }
    }
    
    @Nested
    @DisplayName("Post-Click Behavior")
    class PostClickBehavior {
        
        @Test
        @DisplayName("Should move mouse after click")
        public void testPostClickMouseMove() {
            Location clickLocation = new Location(200, 200);
            Location moveLocation = new Location(100, 100);
            
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(clickLocation)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withMoveMouseAfterClick(true)
                .withPostClickLocation(moveLocation)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockPostClickHandler).handle(eq(clickLocation), eq(options));
        }
        
        @Test
        @DisplayName("Should execute post-click wait")
        public void testPostClickWait() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withPauseAfterClick(1.5)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(1.5);
        }
    }
    
    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 5, 10})
        @DisplayName("Should perform specified number of clicks")
        public void testVariousClickCounts(int clickCount) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withClicks(clickCount)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(clickCount))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), any(), eq(clickCount));
        }
        
        @ParameterizedTest
        @MethodSource("provideClickTypes")
        @DisplayName("Should handle different click types")
        public void testDifferentClickTypes(ClickOptions.ClickType clickType, boolean expectedSuccess) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withClickType(clickType)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(expectedSuccess)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(options), anyInt())).thenReturn(expectedSuccess);
            
            ActionResult result = click.perform(objectCollection);
            
            assertEquals(expectedSuccess, result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(options), anyInt());
        }
        
        static Stream<Arguments> provideClickTypes() {
            return Stream.of(
                Arguments.of(ClickOptions.ClickType.LEFT, true),
                Arguments.of(ClickOptions.ClickType.RIGHT, true),
                Arguments.of(ClickOptions.ClickType.MIDDLE, true),
                Arguments.of(ClickOptions.ClickType.DOUBLE, true),
                Arguments.of(ClickOptions.ClickType.TRIPLE, true)
            );
        }
    }
    
    @Nested
    @DisplayName("Performance and Timing")
    class PerformanceAndTiming {
        
        @Test
        @DisplayName("Should measure click duration")
        public void testClickDuration() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            when(mockTimeProvider.getCurrentTimeMillis())
                .thenReturn(1000L)
                .thenReturn(1050L);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(new ClickOptions())
                .setDuration(50.0)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), anyInt())).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(50.0, result.getDuration(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle timeout scenarios")
        public void testTimeoutHandling() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withSearchTime(0.1)
                .build();
            
            ClickOptions clickOptions = new ClickOptions.Builder()
                .withFindOptions(findOptions)
                .build();
            objectCollection.setActionConfig(clickOptions);
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(false)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(clickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
        }
    }
}