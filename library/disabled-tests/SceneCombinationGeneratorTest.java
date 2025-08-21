package io.github.jspinak.brobot.analysis.scene;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.model.analysis.scene.SceneCombination;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SceneCombinationGenerator Tests")
public class SceneCombinationGeneratorTest extends BrobotTestBase {

    @Mock
    private FindDynamicPixelMatches findDynamicPixelMatches;
    
    @Mock
    private ActionResultFactory matchesInitializer;
    
    @Mock
    private Scene scene1;
    
    @Mock
    private Scene scene2;
    
    @Mock
    private Scene scene3;
    
    @InjectMocks
    private SceneCombinationGenerator sceneCombinationGenerator;
    
    private Mat testMat;
    private List<ObjectCollection> objectCollections;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        testMat = new Mat(100, 100, CV_8UC1, new Scalar(255));
        objectCollections = new ArrayList<>();
    }
    
    @AfterEach
    public void tearDown() {
        if (testMat != null && !testMat.isNull()) {
            testMat.release();
        }
    }
    
    @Test
    @DisplayName("Should generate all scene combinations for two collections")
    void shouldGenerateAllCombinationsForTwoCollections() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        
        objectCollections.add(collection1);
        objectCollections.add(collection2);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(objectCollections);
        
        // For 2 collections, we expect 3 combinations: (0,0), (0,1), (1,1)
        assertEquals(3, combinations.size());
        
        // Verify combinations have correct indices
        assertTrue(hasCombination(combinations, 0, 0));
        assertTrue(hasCombination(combinations, 0, 1));
        assertTrue(hasCombination(combinations, 1, 1));
    }
    
    @Test
    @DisplayName("Should generate all scene combinations for three collections")
    void shouldGenerateAllCombinationsForThreeCollections() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        ObjectCollection collection3 = createObjectCollectionWithScene(scene3);
        
        objectCollections.add(collection1);
        objectCollections.add(collection2);
        objectCollections.add(collection3);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(objectCollections);
        
        // For 3 collections, we expect 6 combinations: (0,0), (0,1), (0,2), (1,1), (1,2), (2,2)
        assertEquals(6, combinations.size());
        
        // Verify all expected combinations exist
        assertTrue(hasCombination(combinations, 0, 0));
        assertTrue(hasCombination(combinations, 0, 1));
        assertTrue(hasCombination(combinations, 0, 2));
        assertTrue(hasCombination(combinations, 1, 1));
        assertTrue(hasCombination(combinations, 1, 2));
        assertTrue(hasCombination(combinations, 2, 2));
    }
    
    @Test
    @DisplayName("Should handle empty object collections list")
    void shouldHandleEmptyObjectCollectionsList() {
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(new ArrayList<>());
        
        assertNotNull(combinations);
        assertTrue(combinations.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle single object collection")
    void shouldHandleSingleObjectCollection() {
        ObjectCollection collection = createObjectCollectionWithScene(scene1);
        objectCollections.add(collection);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(objectCollections);
        
        // For 1 collection, we expect 1 combination: (0,0)
        assertEquals(1, combinations.size());
        assertEquals(0, combinations.get(0).getSceneIndex1());
        assertEquals(0, combinations.get(0).getSceneIndex2());
    }
    
    @Test
    @DisplayName("Should get dynamic pixel mat for two collections with scenes")
    void shouldGetDynamicPixelMatForTwoCollections() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        Optional<Mat> result = sceneCombinationGenerator.getDynamicPixelMat(collection1, collection2);
        
        assertTrue(result.isPresent());
        assertEquals(testMat, result.get());
        
        verify(findDynamicPixelMatches).perform(any(), any(), any());
    }
    
    @Test
    @DisplayName("Should return empty optional when first collection has no scenes")
    void shouldReturnEmptyWhenFirstCollectionHasNoScenes() {
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        
        Optional<Mat> result = sceneCombinationGenerator.getDynamicPixelMat(emptyCollection, collection2);
        
        assertFalse(result.isPresent());
        verify(findDynamicPixelMatches, never()).perform(any(), any(), any());
    }
    
    @Test
    @DisplayName("Should return empty optional when second collection has no scenes")
    void shouldReturnEmptyWhenSecondCollectionHasNoScenes() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection emptyCollection = new ObjectCollection.Builder().build();
        
        Optional<Mat> result = sceneCombinationGenerator.getDynamicPixelMat(collection1, emptyCollection);
        
        assertFalse(result.isPresent());
        verify(findDynamicPixelMatches, never()).perform(any(), any(), any());
    }
    
    @Test
    @DisplayName("Should return empty optional when both collections have no scenes")
    void shouldReturnEmptyWhenBothCollectionsHaveNoScenes() {
        ObjectCollection emptyCollection1 = new ObjectCollection.Builder().build();
        ObjectCollection emptyCollection2 = new ObjectCollection.Builder().build();
        
        Optional<Mat> result = sceneCombinationGenerator.getDynamicPixelMat(emptyCollection1, emptyCollection2);
        
        assertFalse(result.isPresent());
        verify(findDynamicPixelMatches, never()).perform(any(), any(), any());
    }
    
    @Test
    @DisplayName("Should handle null dynamic pixel mask from action result")
    void shouldHandleNullDynamicPixelMask() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(null);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        Optional<Mat> result = sceneCombinationGenerator.getDynamicPixelMat(collection1, collection2);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Should skip combinations when dynamic pixel mat is not present")
    void shouldSkipCombinationsWhenDynamicPixelMatNotPresent() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = new ObjectCollection.Builder().build(); // Empty collection
        ObjectCollection collection3 = createObjectCollectionWithScene(scene3);
        
        objectCollections.add(collection1);
        objectCollections.add(collection2);
        objectCollections.add(collection3);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(objectCollections);
        
        // Should only have combinations between collections with scenes
        // (0,0), (0,2), (2,2) - skipping all combinations with index 1
        assertEquals(3, combinations.size());
        
        assertTrue(hasCombination(combinations, 0, 0));
        assertTrue(hasCombination(combinations, 0, 2));
        assertTrue(hasCombination(combinations, 2, 2));
        assertFalse(hasCombination(combinations, 0, 1));
        assertFalse(hasCombination(combinations, 1, 1));
        assertFalse(hasCombination(combinations, 1, 2));
    }
    
    @Test
    @DisplayName("Should use upper triangular matrix approach to avoid duplicates")
    void shouldUseUpperTriangularMatrixApproach() {
        ObjectCollection collection1 = createObjectCollectionWithScene(scene1);
        ObjectCollection collection2 = createObjectCollectionWithScene(scene2);
        
        objectCollections.add(collection1);
        objectCollections.add(collection2);
        
        ActionResult actionResult = mock(ActionResult.class);
        when(actionResult.getDynamicPixelMask()).thenReturn(testMat);
        
        when(matchesInitializer.create(eq(ActionType.FIND_DYNAMIC), any(), any(), any()))
            .thenReturn(actionResult);
        when(findDynamicPixelMatches.perform(any(), any(), any()))
            .thenReturn(actionResult);
        
        List<SceneCombination> combinations = sceneCombinationGenerator.getAllSceneCombinations(objectCollections);
        
        // Should not have duplicate combinations like (1,0) when (0,1) exists
        assertFalse(hasCombination(combinations, 1, 0));
        assertTrue(hasCombination(combinations, 0, 1));
    }
    
    private ObjectCollection createObjectCollectionWithScene(Scene scene) {
        return new ObjectCollection.Builder()
            .withScenes(scene)
            .build();
    }
    
    private boolean hasCombination(List<SceneCombination> combinations, int index1, int index2) {
        return combinations.stream()
            .anyMatch(c -> c.getSceneIndex1() == index1 && c.getSceneIndex2() == index2);
    }
}