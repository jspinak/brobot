package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the Find action - the core pattern matching action in Brobot.
 * These tests verify the Find action's interaction with FindPipeline.
 */
@DisplayName("Find Action Tests")
public class FindActionTest extends BrobotTestBase {

    @Mock
    private FindPipeline mockFindPipeline;
    
    private Find find;
    private ObjectCollection objectCollection;
    private ActionResult actionResult;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(mockFindPipeline);
        objectCollection = new ObjectCollection();
        actionResult = new ActionResult();
    }
    
    @Nested
    @DisplayName("Basic Find Operations")
    class BasicFindOperations {
        
        @Test
        @DisplayName("Should return FIND action type")
        public void testGetActionType() {
            assertEquals(ActionInterface.Type.FIND, find.getActionType());
        }
        
        @Test
        @DisplayName("Should delegate to FindPipeline for image pattern")
        public void testFindImagePattern() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .addPattern("test.png")
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle multiple StateImages")
        public void testFindMultipleImages() {
            // Arrange
            StateImage image1 = new StateImage.Builder()
                .setName("image1")
                .addPattern("image1.png")
                .build();
            StateImage image2 = new StateImage.Builder()
                .setName("image2")
                .addPattern("image2.png")
                .build();
            
            objectCollection.getStateImages().add(image1);
            objectCollection.getStateImages().add(image2);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertEquals(2, objectCollection.getStateImages().size());
        }
        
        @Test
        @DisplayName("Should handle StateString for text finding")
        public void testFindText() {
            // Arrange
            StateString stateString = new StateString.Builder()
                .setString("Hello World")
                .build();
            objectCollection.getStateStrings().add(stateString);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should handle StateRegion for region searching")
        public void testFindInRegion() {
            // Arrange
            Region searchRegion = new Region(100, 100, 400, 400);
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(searchRegion)
                .build();
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            
            objectCollection.getStateRegions().add(stateRegion);
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should require BaseFindOptions configuration")
        public void testRequiresBaseFindOptions() {
            // Arrange
            StateImage stateImage = new StateImage.Builder().build();
            objectCollection.getStateImages().add(stateImage);
            
            // Don't set proper config - use a different type
            actionResult.setActionConfig(null);
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                find.perform(actionResult, objectCollection);
            });
        }
    }
    
    @Nested
    @DisplayName("Find Options Configuration")
    class FindOptionsConfiguration {
        
        @Test
        @DisplayName("Should pass similarity threshold to pipeline")
        public void testSimilarityThreshold() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSimilarity(0.85)
                .build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(argThat(arg -> 
                arg instanceof PatternFindOptions && 
                ((PatternFindOptions) arg).getSimilarity() == 0.85
            ), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should pass search time limit to pipeline")
        public void testSearchTimeLimit() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setSearchDuration(2.0)
                .build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(argThat(arg ->
                arg instanceof PatternFindOptions &&
                ((PatternFindOptions) arg).getSearchDuration() == 2.0
            ), eq(actionResult), eq(objectCollection));
        }
        
        @Test
        @DisplayName("Should pass find strategy to pipeline")
        public void testFindStrategy() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(argThat(arg ->
                arg instanceof PatternFindOptions &&
                ((PatternFindOptions) arg).getStrategy() == PatternFindOptions.Strategy.ALL
            ), eq(actionResult), eq(objectCollection));
        }
    }
    
    @Nested
    @DisplayName("Multiple Object Collections")
    class MultipleObjectCollections {
        
        @Test
        @DisplayName("Should handle multiple ObjectCollections")
        public void testMultipleObjectCollections() {
            // Arrange
            StateImage image1 = new StateImage.Builder()
                .setName("image1")
                .build();
            StateImage image2 = new StateImage.Builder()
                .setName("image2")
                .build();
            
            ObjectCollection collection1 = new ObjectCollection();
            collection1.getStateImages().add(image1);
            
            ObjectCollection collection2 = new ObjectCollection();
            collection2.getStateImages().add(image2);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, collection1, collection2);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(collection1), eq(collection2));
        }
        
        @Test
        @DisplayName("Should handle empty ObjectCollection")
        public void testEmptyObjectCollection() {
            // Arrange
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertTrue(objectCollection.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complex Object Types")
    class ComplexObjectTypes {
        
        @Test
        @DisplayName("Should handle mixed object types in collection")
        public void testMixedObjectTypes() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            StateString stateString = new StateString.Builder()
                .setString("test-text")
                .build();
            StateRegion stateRegion = new StateRegion.Builder()
                .setSearchRegion(new Region(0, 0, 800, 600))
                .build();
            StateLocation stateLocation = new StateLocation.Builder()
                .setLocation(new Location(400, 300))
                .build();
            
            objectCollection.getStateImages().add(stateImage);
            objectCollection.getStateStrings().add(stateString);
            objectCollection.getStateRegions().add(stateRegion);
            objectCollection.getStateLocations().add(stateLocation);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertFalse(objectCollection.isEmpty());
        }
        
        @Test
        @DisplayName("Should handle previous ActionResult as input")
        public void testPreviousActionResultAsInput() {
            // Arrange
            Match previousMatch = new Match.Builder()
                .setRegion(new Region(100, 100, 50, 50))
                .setSimScore(0.95)
                .build();
            
            ActionResult previousResult = new ActionResult();
            previousResult.setSuccess(true);
            previousResult.getMatchList().add(previousMatch);
            
            objectCollection.getMatches().add(previousResult);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertEquals(1, objectCollection.getMatches().size());
        }
    }
    
    @Nested
    @DisplayName("StateImage with Patterns")
    class StateImageWithPatterns {
        
        @Test
        @DisplayName("Should handle StateImage with multiple patterns")
        public void testStateImageWithMultiplePatterns() {
            // Arrange
            Pattern pattern1 = new Pattern("pattern1.png");
            Pattern pattern2 = new Pattern("pattern2.png");
            
            StateImage stateImage = new StateImage.Builder()
                .setName("multi-pattern-image")
                .addPattern(pattern1)
                .addPattern(pattern2)
                .build();
            
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertEquals(2, stateImage.getPatterns().size());
        }
        
        @Test
        @DisplayName("Should handle StateImage with search regions")
        public void testStateImageWithSearchRegions() {
            // Arrange
            Region region1 = new Region(0, 0, 400, 300);
            Region region2 = new Region(400, 0, 400, 300);
            
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .setSearchRegionForAllPatterns(region1)
                .build();
            // Add a pattern so setSearchRegions has something to work on
            stateImage.getPatterns().add(new Pattern.Builder().build());
            stateImage.setSearchRegions(region1, region2);
            
            objectCollection.getStateImages().add(stateImage);
            
            PatternFindOptions options = new PatternFindOptions.Builder().build();
            actionResult.setActionConfig(options);
            
            // Act
            find.perform(actionResult, objectCollection);
            
            // Assert
            verify(mockFindPipeline).execute(eq(options), eq(actionResult), eq(objectCollection));
            assertTrue(stateImage.hasDefinedSearchRegion());
        }
    }
}