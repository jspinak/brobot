package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.RepetitionOptions;
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
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Click Action Tests")
public class ClickActionTest extends BrobotTestBase {

    private Click click;
    
    @Mock
    private Find mockFind;
    
    @Mock
    private SingleClickExecutor mockSingleClickExecutor;
    
    @Mock
    private TimeProvider mockTimeProvider;
    
    @Mock
    private PostClickHandler mockPostClickHandler;
    
    @Mock
    private ActionResultFactory mockActionResultFactory;
    
    private AutoCloseable mocks;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);
        click = new Click(mockFind, mockSingleClickExecutor, mockTimeProvider, 
                         mockPostClickHandler, mockActionResultFactory);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Nested
    @DisplayName("Basic Click Operations")
    class BasicClickOperations {
        
        @Test
        @DisplayName("Should have correct action type")
        public void testActionType() {
            assertEquals(ActionInterface.Type.CLICK, click.getActionType());
        }
        
        @Test
        @DisplayName("Should perform click on found match")
        public void testClickOnMatch() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            actionResult.setSuccess(false);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            Region matchRegion = new Region(100, 100, 50, 50);
            Match match = new Match(matchRegion);
            match.setScore(0.95);
            
            // Mock the ActionResultFactory to return a findResult for the Find operation
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(true);
            findResult.add(match);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has the match
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            assertEquals(match, actionResult.getMatchList().get(0));
            verify(mockSingleClickExecutor, times(1)).click(any(Location.class), eq(clickOptions));
            assertEquals(1, match.getTimesActedOn());
        }
        
        @Test
        @DisplayName("Should handle no matches found")
        public void testNoMatchesFound() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(false);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has no matches
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            assertFalse(actionResult.isSuccess());
            assertTrue(actionResult.getMatchList().isEmpty());
            verify(mockSingleClickExecutor, never()).click(any(Location.class), any(ClickOptions.class));
        }
        
        @Test
        @DisplayName("Should click on location objects")
        public void testClickOnLocation() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(new Location(200, 200))
                .build();
            objectCollection.getStateLocations().add(stateLocation);
            
            Match match = new Match(new Region(200, 200, 1, 1));
            match.setScore(1.0);
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(true);
            findResult.add(match);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has the match
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            verify(mockSingleClickExecutor, times(1)).click(any(Location.class), eq(clickOptions));
        }
        
        @Test
        @DisplayName("Should handle multiple matches")
        public void testMultipleMatches() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            Match match1 = new Match(new Region(100, 100, 50, 50));
            match1.setScore(0.95);
            Match match2 = new Match(new Region(200, 200, 50, 50));
            match2.setScore(0.90);
            Match match3 = new Match(new Region(300, 300, 50, 50));
            match3.setScore(0.85);
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(true);
            findResult.add(match1, match2, match3);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has the matches
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
            verify(mockSingleClickExecutor, times(3)).click(any(Location.class), eq(clickOptions));
            verify(mockTimeProvider, times(2)).wait(clickOptions.getPauseBetweenIndividualActions()); // Pause between matches, not after last
            assertEquals(1, match1.getTimesActedOn());
            assertEquals(1, match2.getTimesActedOn());
            assertEquals(1, match3.getTimesActedOn());
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should respect times to repeat individual action")
        public void testMultipleClicksPerMatch() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder()
                .setRepetition(RepetitionOptions.builder()
                    .setTimesToRepeatIndividualAction(3)
                    .build())
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            Match match = new Match(new Region(100, 100, 50, 50));
            match.setScore(0.95);
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(true);
            findResult.add(match);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has the match
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockSingleClickExecutor, times(3)).click(any(Location.class), eq(clickOptions));
            verify(mockTimeProvider, times(2)).wait(anyDouble()); // Pause between clicks, not after last
            assertEquals(3, match.getTimesActedOn());
        }
        
        @Test
        @DisplayName("Should throw exception for non-ClickOptions configuration")
        public void testInvalidConfiguration() {
            // Arrange
            PatternFindOptions wrongOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(wrongOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> click.perform(actionResult, objectCollection),
                "Click requires ClickOptions configuration");
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should work with StateRegion objects")
        public void testClickOnStateRegion() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(150, 150, 100, 100))
                .build();
            objectCollection.getStateRegions().add(stateRegion);
            
            Match match = new Match(new Region(150, 150, 100, 100));
            match.setScore(1.0);
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(true);
            findResult.add(match);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to do nothing - the findResult already has the match
            doNothing().when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, objectCollection);
            
            // Assert
            assertTrue(actionResult.isSuccess());
            verify(mockSingleClickExecutor, times(1)).click(any(Location.class), eq(clickOptions));
        }
        
        @Test
        @DisplayName("Should handle empty object collection")
        public void testEmptyObjectCollection() {
            // Arrange
            ClickOptions clickOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(clickOptions);
            
            ObjectCollection emptyCollection = new ObjectCollection();
            
            // Mock the ActionResultFactory
            ActionResult findResult = new ActionResult();
            findResult.setActionConfig(new PatternFindOptions.Builder().build());
            findResult.setSuccess(false);
            when(mockActionResultFactory.init(any(PatternFindOptions.class), anyString(), any()))
                .thenReturn(findResult);
            
            // Mock Find to return no matches for empty collection
            doAnswer(invocation -> {
                ActionResult ar = invocation.getArgument(0);
                ar.setSuccess(false);
                return null;
            }).when(mockFind).perform(any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            click.perform(actionResult, emptyCollection);
            
            // Assert
            assertFalse(actionResult.isSuccess());
            assertTrue(actionResult.getMatchList().isEmpty());
            verify(mockSingleClickExecutor, never()).click(any(Location.class), any(ClickOptions.class));
        }
    }
}