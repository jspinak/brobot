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
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.action.MouseButton;
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

@DisplayName("RightClick Tests")
public class RightClickTest extends BrobotTestBase {

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
    @DisplayName("Basic Right-Click Operations")
    class BasicRightClickOperations {
        
        @Test
        @DisplayName("Should perform right-click on location")
        public void testRightClickOnLocation() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(eq(location), eq(rightClickOptions), eq(1)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), eq(rightClickOptions), eq(1));
        }
        
        @Test
        @DisplayName("Should perform right-click on image match")
        public void testRightClickOnImage() {
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
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), eq(1)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(rightClickOptions), eq(1));
        }
        
        @Test
        @DisplayName("Should perform right-click on region center")
        public void testRightClickOnRegion() {
            Region region = new Region(100, 100, 200, 200);
            StateRegion stateRegion = new StateRegion.Builder()
                .withRegion(region)
                .build();
            objectCollection.addStateRegion(stateRegion);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(Location.class), eq(rightClickOptions), eq(1)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            
            ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
            verify(mockSingleClickExecutor).execute(locationCaptor.capture(), eq(rightClickOptions), eq(1));
            
            Location clickedLocation = locationCaptor.getValue();
            assertEquals(200, clickedLocation.getX());
            assertEquals(200, clickedLocation.getY());
        }
    }
    
    @Nested
    @DisplayName("Context Menu Operations")
    class ContextMenuOperations {
        
        @Test
        @DisplayName("Should right-click and wait for context menu")
        public void testRightClickWithWait() {
            Location location = new Location(150, 150);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withPauseAfterClick(0.5)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(0.5);
        }
        
        @Test
        @DisplayName("Should right-click with offset for precise menu positioning")
        public void testRightClickWithOffset() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            Match match = new Match.Builder()
                .withRegion(new Region(100, 100, 50, 50))
                .withScore(0.90)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withOffset(new Location(10, 10))
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(new Location(10, 10), rightClickOptions.getOffset());
        }
    }
    
    @Nested
    @DisplayName("Multiple Right-Click Operations")
    class MultipleRightClickOperations {
        
        @Test
        @DisplayName("Should right-click all matches")
        public void testRightClickAllMatches() {
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
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withClickAll(true)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor, times(3)).execute(any(), eq(rightClickOptions), anyInt());
        }
        
        @Test
        @DisplayName("Should right-click with multiple clicks")
        public void testMultipleRightClicks() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withClicks(2)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), eq(2)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), eq(rightClickOptions), eq(2));
        }
    }
    
    @Nested
    @DisplayName("Middle-Click Operations")
    class MiddleClickOperations {
        
        @Test
        @DisplayName("Should perform middle-click")
        public void testMiddleClick() {
            Location location = new Location(200, 200);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions middleClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.MIDDLE)
                .build();
            objectCollection.setActionConfig(middleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(middleClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(eq(location), eq(middleClickOptions), eq(1)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(eq(location), eq(middleClickOptions), eq(1));
        }
        
        @Test
        @DisplayName("Should perform middle-click on link for new tab")
        public void testMiddleClickForNewTab() {
            StateImage linkImage = StateImage.builder().build();
            objectCollection.addStateImage(linkImage);
            
            Match match = new Match.Builder()
                .withRegion(new Region(300, 300, 100, 20))
                .withScore(0.95)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions middleClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.MIDDLE)
                .withPauseAfterClick(1.0)
                .build();
            objectCollection.setActionConfig(middleClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(middleClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(middleClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(1.0);
        }
    }
    
    @Nested
    @DisplayName("Common Use Cases")
    class CommonUseCases {
        
        @Test
        @DisplayName("Should right-click for context menu copy")
        public void testRightClickForCopy() {
            StateImage textSelection = StateImage.builder().build();
            objectCollection.addStateImage(textSelection);
            
            Match match = new Match.Builder()
                .withRegion(new Region(150, 150, 200, 30))
                .withScore(0.88)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withPauseAfterClick(0.3)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(rightClickOptions), eq(1));
            verify(mockTimeProvider).pause(0.3);
        }
        
        @Test
        @DisplayName("Should right-click for file properties")
        public void testRightClickForProperties() {
            StateImage fileIcon = StateImage.builder().build();
            objectCollection.addStateImage(fileIcon);
            
            Match match = new Match.Builder()
                .withRegion(new Region(400, 400, 64, 64))
                .withScore(0.93)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withPauseAfterClick(0.5)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should right-click for delete operation")
        public void testRightClickForDelete() {
            StateImage itemToDelete = StateImage.builder().build();
            objectCollection.addStateImage(itemToDelete);
            
            Match match = new Match.Builder()
                .withRegion(new Region(250, 250, 100, 100))
                .withScore(0.91)
                .build();
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(true)
                .setMatch(match)
                .build();
            
            ClickOptions deleteClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withPauseAfterClick(0.4)
                .build();
            objectCollection.setActionConfig(deleteClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(deleteClickOptions)
                .build();
            
            when(mockFind.perform(any(), any())).thenReturn(findResult);
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(deleteClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            assertEquals(MouseButton.RIGHT, deleteClickOptions.getMousePressOptions().getButton());
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle right-click execution failure")
        public void testRightClickExecutionFailure() {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(false);
            
            ActionResult result = click.perform(objectCollection);
            
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle find failure for right-click")
        public void testFindFailureForRightClick() {
            StateImage stateImage = StateImage.builder().build();
            objectCollection.addStateImage(stateImage);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult findResult = new ActionResult.Builder()
                .setSuccess(false)
                .build();
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(false)
                .setActionConfig(rightClickOptions)
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
    @DisplayName("Post Right-Click Behavior")
    class PostRightClickBehavior {
        
        @Test
        @DisplayName("Should move mouse after right-click")
        public void testMoveMouseAfterRightClick() {
            Location clickLocation = new Location(200, 200);
            Location moveLocation = new Location(50, 50);
            
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(clickLocation)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withMoveMouseAfterClick(true)
                .withPostClickLocation(moveLocation)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockPostClickHandler).handle(eq(clickLocation), eq(rightClickOptions));
        }
    }
    
    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @ParameterizedTest
        @MethodSource("provideClickTypesAndButtons")
        @DisplayName("Should handle different mouse button clicks")
        public void testDifferentMouseButtons(MouseButton clickType, int expectedClicks) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions options = new ClickOptions.Builder()
                .withButton(clickType)
                .withClicks(expectedClicks)
                .build();
            objectCollection.setActionConfig(options);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(options)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(options), eq(expectedClicks)))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockSingleClickExecutor).execute(any(), eq(options), eq(expectedClicks));
        }
        
        static Stream<Arguments> provideClickTypesAndButtons() {
            return Stream.of(
                Arguments.of(MouseButton.LEFT, 1),
                Arguments.of(MouseButton.RIGHT, 1),
                Arguments.of(MouseButton.MIDDLE, 1),
                Arguments.of(MouseButton.RIGHT, 2),
                Arguments.of(MouseButton.MIDDLE, 2)
            );
        }
        
        @ParameterizedTest
        @ValueSource(doubles = {0.1, 0.3, 0.5, 1.0, 2.0})
        @DisplayName("Should apply various pause durations after right-click")
        public void testVariousPauseDurations(double pauseDuration) {
            Location location = new Location(100, 100);
            StateLocation stateLocation = new StateLocation.Builder()
                .withLocation(location)
                .build();
            objectCollection.addStateLocation(stateLocation);
            
            ClickOptions rightClickOptions = new ClickOptions.Builder()
                .withButton(MouseButton.RIGHT)
                .withPauseAfterClick(pauseDuration)
                .build();
            objectCollection.setActionConfig(rightClickOptions);
            
            ActionResult clickResult = new ActionResult.Builder()
                .setSuccess(true)
                .setActionConfig(rightClickOptions)
                .build();
            
            when(mockActionResultFactory.getActionResult(any(), any(), anyInt()))
                .thenReturn(clickResult);
            when(mockSingleClickExecutor.execute(any(), eq(rightClickOptions), anyInt()))
                .thenReturn(true);
            
            ActionResult result = click.perform(objectCollection);
            
            assertTrue(result.isSuccess());
            verify(mockTimeProvider).pause(pauseDuration);
        }
    }
}