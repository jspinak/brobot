package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.find.DefinedRegionConverter;
import io.github.jspinak.brobot.action.internal.find.IterativePatternFinder;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
 * Unit tests for ImageFinder class.
 * Tests various image finding strategies and operations.
 */
@DisplayName("ImageFinder Tests")
public class ImageFinderTest extends BrobotTestBase {
    
    @Mock
    private DefinedRegionConverter mockDefinedRegionConverter;
    @Mock
    private ActionLifecycleManagement mockActionLifecycleManagement;
    @Mock
    private SceneProvider mockSceneProvider;
    @Mock
    private IterativePatternFinder mockIterativePatternFinder;
    @Mock
    private FindImage mockLegacyFindImage;
    
    private ImageFinder imageFinder;
    private ActionResult actionResult;
    private ObjectCollection objectCollection;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        imageFinder = new ImageFinder(
            mockDefinedRegionConverter,
            mockActionLifecycleManagement,
            mockSceneProvider,
            mockIterativePatternFinder,
            mockLegacyFindImage
        );
        
        actionResult = new ActionResult();
        objectCollection = new ObjectCollection();
    }
    
    @Nested
    @DisplayName("Find All Strategy")
    class FindAllStrategy {
        
        @Test
        @DisplayName("Should find all matches for provided images")
        public void testFindAll() {
            // Arrange
            StateImage stateImage1 = new StateImage.Builder()
                .setName("image1")
                .addPattern("pattern1.png")
                .build();
            StateImage stateImage2 = new StateImage.Builder()
                .setName("image2")
                .addPattern("pattern2.png")
                .build();
            
            objectCollection.getStateImages().add(stateImage1);
            objectCollection.getStateImages().add(stateImage2);
            
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findAll(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findAll(eq(actionResult), eq(collections));
        }
        
        @Test
        @DisplayName("Should handle empty object collections")
        public void testFindAllWithEmptyCollections() {
            // Arrange
            List<ObjectCollection> emptyCollections = new ArrayList<>();
            
            // Act
            imageFinder.findAll(actionResult, emptyCollections);
            
            // Assert
            verify(mockLegacyFindImage).findAll(eq(actionResult), eq(emptyCollections));
        }
        
        @Test
        @DisplayName("Should handle multiple object collections")
        public void testFindAllWithMultipleCollections() {
            // Arrange
            ObjectCollection collection1 = new ObjectCollection();
            collection1.getStateImages().add(new StateImage.Builder().setName("image1").build());
            
            ObjectCollection collection2 = new ObjectCollection();
            collection2.getStateImages().add(new StateImage.Builder().setName("image2").build());
            
            List<ObjectCollection> collections = Arrays.asList(collection1, collection2);
            
            // Act
            imageFinder.findAll(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findAll(eq(actionResult), eq(collections));
        }
    }
    
    @Nested
    @DisplayName("Find Best Strategy")
    class FindBestStrategy {
        
        @Test
        @DisplayName("Should find only the best match")
        public void testFindBest() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("image")
                .addPattern("pattern.png")
                .build();
            
            objectCollection.getStateImages().add(stateImage);
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findBest(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findBest(eq(actionResult), eq(collections));
        }
        
        @Test
        @DisplayName("Should delegate to legacy implementation")
        public void testFindBestDelegation() {
            // Arrange
            StateImage stateImage1 = new StateImage.Builder()
                .setName("image1")
                .build();
            StateImage stateImage2 = new StateImage.Builder()
                .setName("image2")
                .build();
            
            objectCollection.getStateImages().add(stateImage1);
            objectCollection.getStateImages().add(stateImage2);
            
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Setup mock behavior
            doAnswer(invocation -> {
                ActionResult result = invocation.getArgument(0);
                result.getMatchList().add(new Match.Builder()
                    .setRegion(new Region(100, 100, 50, 50))
                    .setSimScore(0.95)
                    .build());
                return null;
            }).when(mockLegacyFindImage).findBest(any(), any());
            
            // Act
            imageFinder.findBest(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findBest(eq(actionResult), eq(collections));
            assertEquals(1, actionResult.getMatchList().size());
        }
    }
    
    @Nested
    @DisplayName("Find Each State Object Strategy")
    class FindEachStateObjectStrategy {
        
        @Test
        @DisplayName("Should find best match for each state object")
        public void testFindEach() {
            // Arrange
            StateImage stateImage1 = new StateImage.Builder()
                .setName("image1")
                .build();
            StateImage stateImage2 = new StateImage.Builder()
                .setName("image2")
                .build();
            
            objectCollection.getStateImages().add(stateImage1);
            objectCollection.getStateImages().add(stateImage2);
            
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findEachStateObject(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findEachStateObject(eq(actionResult), eq(collections));
        }
        
        @Test
        @DisplayName("Should handle single state image")
        public void testFindEachSingleImage() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("image")
                .build();
            
            objectCollection.getStateImages().add(stateImage);
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findEachStateObject(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findEachStateObject(eq(actionResult), eq(collections));
        }
    }
    
    @Nested
    @DisplayName("Find Each Scene Strategy")
    class FindEachSceneStrategy {
        
        @Test
        @DisplayName("Should find best match for each scene")
        public void testFindEachScene() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("image")
                .build();
            
            Scene scene1 = new Scene();
            Scene scene2 = new Scene();
            
            objectCollection.getStateImages().add(stateImage);
            objectCollection.getScenes().add(scene1);
            objectCollection.getScenes().add(scene2);
            
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findEachScene(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findEachScene(eq(actionResult), eq(collections));
        }
        
        @Test
        @DisplayName("Should handle collections without scenes")
        public void testFindEachSceneNoScenes() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("image")
                .build();
            
            objectCollection.getStateImages().add(stateImage);
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findEachScene(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findEachScene(eq(actionResult), eq(collections));
        }
    }
    
    
    @Nested
    @DisplayName("Legacy Delegation")
    class LegacyDelegation {
        
        @Test
        @DisplayName("Should delegate all methods to legacy implementation")
        public void testAllMethodsDelegateProperly() {
            // Arrange
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act & Assert - Test each method delegates
            imageFinder.findAll(actionResult, collections);
            verify(mockLegacyFindImage).findAll(any(), any());
            
            imageFinder.findBest(actionResult, collections);
            verify(mockLegacyFindImage).findBest(any(), any());
            
            imageFinder.findEachStateObject(actionResult, collections);
            verify(mockLegacyFindImage).findEachStateObject(any(), any());
            
            imageFinder.findEachScene(actionResult, collections);
            verify(mockLegacyFindImage).findEachScene(any(), any());
        }
        
        @Test
        @DisplayName("Should pass parameters unmodified to legacy implementation")
        public void testParametersPassedUnmodified() {
            // Arrange
            StateImage stateImage = new StateImage.Builder()
                .setName("test-image")
                .build();
            objectCollection.getStateImages().add(stateImage);
            
            List<ObjectCollection> collections = Collections.singletonList(objectCollection);
            
            // Act
            imageFinder.findAll(actionResult, collections);
            
            // Assert
            verify(mockLegacyFindImage).findAll(
                same(actionResult),
                same(collections)
            );
        }
    }
}