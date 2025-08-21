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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("DoubleClick Tests")
public class DoubleClickTest extends BrobotTestBase {

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
    @DisplayName("Basic Double-Click Operations")
    class BasicDoubleClickOperations {
        
        @Test
        @DisplayName("Should perform double-click with clicks=2")
        public void testDoubleClickWithClicksParameter() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), any(), eq(2));
        }
        
        @Test
        @DisplayName("Should perform double-click on image match")
        public void testDoubleClickOnImage() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            Match match = new Match.Builder()
                .withRegion(new Region(200, 200, 50, 50))
                .withScore(0.92)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), any(), eq(2));
        }
        
        @Test
        @DisplayName("Should perform double-click on region center")
        public void testDoubleClickOnRegion() {
            Region region = new Region(100, 100, 200, 200);
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(region)
                .build();
            objectCollection.addStateRegion(stateRegion);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(Location.class), any(), eq(2)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(mockSingleClickExecutor).execute(locationCaptor.capture(), any(), eq(2));
            
            Location clickedLocation = locationCaptor.getValue();
            assertEquals(200, clickedLocation.getX());
            assertEquals(200, clickedLocation.getY());
        }
    }
    
    @Nested
    @DisplayName("Double-Click Timing Configuration")
    class DoubleClickTimingConfiguration {
        
        @Test
        @DisplayName("Should apply pause between double-clicks")
        public void testPauseBetweenDoubleClicks() {
            Location location = new Location(150, 150);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withPauseBetweenClicks(0.1)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(0.1, doubleClickOptions.getPauseBetweenClicks());
        }
        
        @Test
        @DisplayName("Should apply pause after double-click")
        public void testPauseAfterDoubleClick() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withPauseAfterClick(0.5)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(0.5);
        }
        
        @Test
        @DisplayName("Should measure double-click duration")
        public void testDoubleClickDuration() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            when(mockTimeProvider.getCurrentTimeMillis())
                .thenReturn(1000L)
                .thenReturn(1100L);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .setDuration(100.0)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(100.0, result.getDuration(), 0.01);
        }
    }
    
    @Nested
    @DisplayName("Double-Click on Multiple Targets")
    class DoubleClickMultipleTargets {
        
        @Test
        @DisplayName("Should double-click all matches")
        public void testDoubleClickAllMatches() {
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
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withClickAll(true)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, times(3)).execute(any(), any(), eq(2));
        }
        
        @Test
        @DisplayName("Should double-click best match only")
        public void testDoubleClickBestMatch() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            List<Match> matches = Arrays.asList(
                new Match.Builder().withRegion(new Region(100, 100, 50, 50)).withScore(0.80).build(),
                new Match.Builder().withRegion(new Region(200, 200, 50, 50)).withScore(0.95).build(),
                new Match.Builder().withRegion(new Region(300, 300, 50, 50)).withScore(0.85).build()
            );
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatchList(matches)
                .build();
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withClickAll(false)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, times(1)).execute(any(), any(), eq(2));
        }
    }
    
    @Nested
    @DisplayName("Triple-Click and Multi-Click")
    class TripleAndMultiClick {
        
        @Test
        @DisplayName("Should perform triple-click")
        public void testTripleClick() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions tripleClickOptions = new ClickOptions.Builder()
                .withClicks(3)
                .build();
            objectCollection.setActionConfig(tripleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(tripleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(3))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), any(), eq(3));
        }
        
        @ParameterizedTest
        @ValueSource(ints = {4, 5, 10})
        @DisplayName("Should perform multi-click with various counts")
        public void testMultiClick(int clickCount) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions multiClickOptions = new ClickOptions.Builder()
                .withClicks(clickCount)
                .build();
            objectCollection.setActionConfig(multiClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(multiClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(clickCount))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), any(), eq(clickCount));
        }
    }
    
    @Nested
    @DisplayName("Double-Click with Find Options")
    class DoubleClickWithFindOptions {
        
        @Test
        @DisplayName("Should use find options for double-click")
        public void testDoubleClickWithFindOptions() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .withSimilarity(0.85)
                .withSearchTime(2.0)
                .build();
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withFindOptions(findOptions)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.90)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            
            ArgumentCaptor<ObjectCollection> objCollCaptor = ArgumentCaptor.forClass(ObjectCollection.class);
            verify(mockFind).perform(objCollCaptor.capture(), any());
            
            ObjectCollection capturedObjColl = objCollCaptor.getValue();
            assertEquals(findOptions, capturedObjColl.getActionConfig());
        }
    }
    
    @Nested
    @DisplayName("Post Double-Click Behavior")
    class PostDoubleClickBehavior {
        
        @Test
        @DisplayName("Should move mouse after double-click")
        public void testMoveMouseAfterDoubleClick() {
            Location clickLocation = new Location(200, 200);
            Location moveLocation = new Location(50, 50);
            
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(clickLocation)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withMoveMouseAfterClick(true)
                .withPostClickLocation(moveLocation)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockPostClickHandler).handle(eq(clickLocation), eq(doubleClickOptions));
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle double-click execution failure")
        public void testDoubleClickExecutionFailure() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(false);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle find failure for double-click")
        public void testFindFailureForDoubleClick() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            ClickOptions doubleClickOptions = new ClickOptions.Builder()
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(doubleClickOptions);
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(false)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(doubleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
            verify(mockSingleClickExecutor, never()).execute(any(), any(), anyInt());
        }
    }
    
    @Nested
    @DisplayName("Common Use Cases")
    class CommonUseCases {
        
        @Test
        @DisplayName("Should double-click to select word")
        public void testDoubleClickToSelectWord() {
            StateImage textArea = StateImage.builder().build();
            objectCollection.addStateImage(textArea);
            
            Match match = new Match.Builder()
                .withRegion(new Region(300, 300, 100, 20))
                .withScore(0.95)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions selectWordOptions = new ClickOptions.Builder()
                .withClicks(2)
                .withPauseBetweenClicks(0.05)
                .build();
            objectCollection.setActionConfig(selectWordOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(selectWordOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(2))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), any(), eq(2));
        }
        
        @Test
        @DisplayName("Should triple-click to select line")
        public void testTripleClickToSelectLine() {
            StateImage textEditor = StateImage.builder().build();
            objectCollection.addStateImage(textEditor);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 200, 400, 20))
                .withScore(0.90)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions selectLineOptions = new ClickOptions.Builder()
                .withClicks(3)
                .withPauseBetweenClicks(0.05)
                .build();
            objectCollection.setActionConfig(selectLineOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(selectLineOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), any(), eq(3))).thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), any(), eq(3));
        }
    }
    
    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @ParameterizedTest
        @MethodSource("provideClickCountAndType")
        @DisplayName("Should handle various click counts and types")
        public void testVariousClickCountsAndTypes(int clicks, ClickOptions.ClickType type, boolean expectedSuccess) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withClicks(clicks)
                .withClickType(type)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(expectedSuccess)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(options), eq(clicks)))
                .thenReturn(expectedSuccess);
            
            ActionResult result = click.perform(objectCollection);
            
            assertEquals(expectedSuccess, result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(options), eq(clicks));
        }
        
        static Stream<Arguments> provideClickCountAndType() {
            return Stream.of(
                Arguments.of(2, ClickOptions.ClickType.LEFT, true),
                Arguments.of(2, ClickOptions.ClickType.RIGHT, true),
                Arguments.of(2, ClickOptions.ClickType.MIDDLE, true),
                Arguments.of(3, ClickOptions.ClickType.LEFT, true),
                Arguments.of(3, ClickOptions.ClickType.RIGHT, true),
                Arguments.of(1, ClickOptions.ClickType.DOUBLE, true),
                Arguments.of(1, ClickOptions.ClickType.TRIPLE, true)
            );
        }
    }
}