package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Find Action Tests")
public class FindTest extends BrobotTestBase {

    private Find find;
    
    @Mock
    private FindPipeline mockFindPipeline;
    
    private AutoCloseable mocks;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mocks = MockitoAnnotations.openMocks(this);
        find = new Find(mockFindPipeline);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should have correct action type")
        public void testActionType() {
            assertEquals(ActionInterface.Type.FIND, find.getActionType());
        }
        
        @Test
        @DisplayName("Should delegate to FindPipeline with correct parameters")
        public void testDelegatesToPipeline() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline, times(1)).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should throw exception for non-BaseFindOptions configuration")
        public void testInvalidConfiguration() {
            // Arrange
            ClickOptions wrongOptions = new ClickOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(wrongOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> find.perform(actionResult, objectCollection),
                "Find requires BaseFindOptions configuration");
        }
    }
    
    @Nested
    @DisplayName("Find Strategy Tests")
    class FindStrategyTests {
        
        @Test
        @DisplayName("Should handle FIRST strategy")
        public void testFirstStrategy() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Mock pipeline to add a single match
            doAnswer(invocation -> {
                ActionResult ar = invocation.getArgument(1);
                Match match = new Match(new Region(100, 100, 50, 50));
                match.setScore(0.95);
                ar.add(match);
                ar.setSuccess(true);
                return null;
            }).when(mockFindPipeline).execute(any(BaseFindOptions.class), any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should handle ALL strategy")
        public void testAllStrategy() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Mock pipeline to add multiple matches
            doAnswer(invocation -> {
                ActionResult ar = invocation.getArgument(1);
                Match match1 = new Match(new Region(100, 100, 50, 50));
                match1.setScore(0.95);
                Match match2 = new Match(new Region(200, 200, 50, 50));
                match2.setScore(0.90);
                Match match3 = new Match(new Region(300, 300, 50, 50));
                match3.setScore(0.85);
                ar.add(match1, match2, match3);
                ar.setSuccess(true);
                return null;
            }).when(mockFindPipeline).execute(any(BaseFindOptions.class), any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
            assertTrue(actionResult.isSuccess());
            assertEquals(3, actionResult.getMatchList().size());
        }
        
        @Test
        @DisplayName("Should handle BEST strategy")
        public void testBestStrategy() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Mock pipeline to add best match
            doAnswer(invocation -> {
                ActionResult ar = invocation.getArgument(1);
                Match bestMatch = new Match(new Region(150, 150, 60, 60));
                bestMatch.setScore(0.98);
                ar.add(bestMatch);
                ar.setSuccess(true);
                return null;
            }).when(mockFindPipeline).execute(any(BaseFindOptions.class), any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
            assertTrue(actionResult.isSuccess());
            assertEquals(1, actionResult.getMatchList().size());
            assertEquals(0.98, actionResult.getMatchList().get(0).getScore());
        }
    }
    
    @Nested
    @DisplayName("Object Collection Handling")
    class ObjectCollectionHandling {
        
        @Test
        @DisplayName("Should handle StateImage objects")
        public void testFindStateImage() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage1 = new StateImage.Builder().setName("button1").build();
            StateImage stateImage2 = new StateImage.Builder().setName("button2").build();
            objectCollection.getStateImages().add(stateImage1);
            objectCollection.getStateImages().add(stateImage2);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle StateRegion objects")
        public void testFindStateRegion() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(50, 50, 200, 200))
                .build();
            objectCollection.getStateRegions().add(stateRegion);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle StateLocation objects")
        public void testFindStateLocation() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(new Location(250, 250))
                .build();
            objectCollection.getStateLocations().add(stateLocation);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle empty ObjectCollection")
        public void testEmptyObjectCollection() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection emptyCollection = new ObjectCollection();
            
            // Mock pipeline to return no matches
            doAnswer(invocation -> {
                ActionResult ar = invocation.getArgument(1);
                ar.setSuccess(false);
                return null;
            }).when(mockFindPipeline).execute(any(BaseFindOptions.class), any(ActionResult.class), any(ObjectCollection.class));
            
            // Act
            find.perform(actionResult, emptyCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(emptyCollection));
            assertFalse(actionResult.isSuccess());
            assertTrue(actionResult.getMatchList().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle multiple ObjectCollections")
        public void testMultipleObjectCollections() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection collection1 = new ObjectCollection();
            StateImage stateImage1 = new StateImage.Builder().build();
            collection1.getStateImages().add(stateImage1);
            
            ObjectCollection collection2 = new ObjectCollection();
            StateImage stateImage2 = new StateImage.Builder().build();
            collection2.getStateImages().add(stateImage2);
            
            // Act
            find.perform(actionResult, collection1, collection2);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(collection1), eq(collection2));
        }
    }
    
    @Nested
    @DisplayName("Find Options Configuration")
    class FindOptionsConfiguration {
        
        @Test
        @DisplayName("Should handle similarity threshold configuration")
        public void testSimilarityThreshold() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
            assertEquals(0.85, findOptions.getSimilarity());
        }
        
        @Test
        @DisplayName("Should handle DoOnEach configuration")
        public void testDoOnEachConfiguration() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.EACH)
                .setDoOnEach(PatternFindOptions.DoOnEach.BEST)
                .build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(findOptions), eq(actionResult), eq(objectCollection));
            assertEquals(PatternFindOptions.DoOnEach.BEST, findOptions.getDoOnEach());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Should handle null ActionResult configuration")
        public void testNullConfiguration() {
            // Arrange
            ActionResult actionResult = new ActionResult();
            // actionResult.setActionConfig(null); // configuration is null
            
            ObjectCollection objectCollection = new ObjectCollection();
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, 
                () -> find.perform(actionResult, objectCollection));
        }
        
        @Test
        @DisplayName("Should handle pipeline execution failure")
        public void testPipelineExecutionFailure() {
            // Arrange
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            ActionResult actionResult = new ActionResult();
            actionResult.setActionConfig(findOptions);
            
            ObjectCollection objectCollection = new ObjectCollection();
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Mock pipeline to throw exception
            doThrow(new RuntimeException("Pipeline execution failed"))
                .when(mockFindPipeline)
                .execute(any(BaseFindOptions.class), any(ActionResult.class), any(ObjectCollection.class));
            
            // Act & Assert
            assertThrows(RuntimeException.class, 
                () -> find.perform(actionResult, objectCollection),
                "Pipeline execution failed");
        }
    }
}