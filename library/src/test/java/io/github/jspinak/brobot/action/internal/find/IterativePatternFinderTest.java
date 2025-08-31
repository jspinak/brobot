package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.FindAll;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.action.internal.region.DynamicRegionResolver;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

/**
 * Test suite for IterativePatternFinder class.
 * Tests iterative pattern finding with multiple attempts and strategies.
 */
@DisplayName("IterativePatternFinder Tests")
public class IterativePatternFinderTest extends BrobotTestBase {

    private IterativePatternFinder iterativePatternFinder;
    
    @Mock
    private ActionLifecycleManagement actionLifecycleManagement;
    
    @Mock
    private FindAll findAll;
    
    @Mock
    private DynamicRegionResolver dynamicRegionResolver;
    
    @Mock
    private StateImage stateImage;
    
    @Mock
    private Scene scene;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private io.github.jspinak.brobot.model.element.Image image;
    
    @Mock
    private ActionResult actionResult;
    
    private AutoCloseable mockCloseable;
    private Mat sceneMat;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        iterativePatternFinder = new IterativePatternFinder(actionLifecycleManagement, findAll, dynamicRegionResolver);
        
        // Create a real Mat for scene
        sceneMat = new Mat(100, 100, CV_8UC3);
        // Initialize with a scalar value (black image)
        sceneMat.put(Scalar.all(0));
        
        // Setup scene mock to return a pattern
        when(scene.getPattern()).thenReturn(pattern);
        when(pattern.getImage()).thenReturn(image);
        when(image.getMatBGR()).thenReturn(sceneMat);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
        if (sceneMat != null && !sceneMat.isNull()) {
            sceneMat.close();
        }
    }
    
    @Nested
    @DisplayName("Pattern Finding")
    class PatternFinding {
        
        @Test
        @DisplayName("Should find patterns in single scene")
        void shouldFindPatternsInSingleScene() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            List<Scene> scenes = Collections.singletonList(scene);
            ActionResult result = new ActionResult();
            
            Match match = new Match.Builder()
                .setSimScore(0.95)
                .build();
            
            when(findAll.find(any(), any(), any())).thenReturn(Collections.singletonList(match));
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt())).thenReturn(true);
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            ArgumentCaptor<StateImage> stateImageCaptor = ArgumentCaptor.forClass(StateImage.class);
            ArgumentCaptor<Scene> sceneCaptor = ArgumentCaptor.forClass(Scene.class);
            ArgumentCaptor<ActionConfig> configCaptor = ArgumentCaptor.forClass(ActionConfig.class);
            verify(findAll).find(stateImageCaptor.capture(), sceneCaptor.capture(), configCaptor.capture());
            assertEquals(stateImage, stateImageCaptor.getValue());
            assertEquals(scene, sceneCaptor.getValue());
            assertNull(configCaptor.getValue());
        }
        
        @Test
        @DisplayName("Should handle multiple scenes")
        void shouldHandleMultipleScenes() {
            // Arrange
            List<StateImage> stateImages = Arrays.asList(stateImage, stateImage);
            Scene scene1 = mock(Scene.class);
            Scene scene2 = mock(Scene.class);
            List<Scene> scenes = Arrays.asList(scene1, scene2);
            ActionResult result = new ActionResult();
            
            Pattern pattern1 = mock(Pattern.class);
            Pattern pattern2 = mock(Pattern.class);
            io.github.jspinak.brobot.model.element.Image image1 = mock(io.github.jspinak.brobot.model.element.Image.class);
            io.github.jspinak.brobot.model.element.Image image2 = mock(io.github.jspinak.brobot.model.element.Image.class);
            
            lenient().when(findAll.find(any(), any(), any())).thenReturn(new ArrayList<>());
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt())).thenReturn(true);
            when(scene1.getPattern()).thenReturn(pattern1);
            when(scene2.getPattern()).thenReturn(pattern2);
            when(pattern1.getImage()).thenReturn(image1);
            when(pattern2.getImage()).thenReturn(image2);
            when(image1.getMatBGR()).thenReturn(sceneMat);
            when(image2.getMatBGR()).thenReturn(sceneMat);
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verify(findAll, times(4)).find(any(StateImage.class), any(Scene.class), nullable(ActionConfig.class));
        }
        
        @Test
        @DisplayName("Should handle empty state images")
        void shouldHandleEmptyStateImages() {
            // Arrange
            List<StateImage> stateImages = new ArrayList<>();
            List<Scene> scenes = Collections.singletonList(scene);
            ActionResult result = new ActionResult();
            
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
        }
        
        @Test
        @DisplayName("Should handle empty scenes")
        void shouldHandleEmptyScenes() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            List<Scene> scenes = new ArrayList<>();
            ActionResult result = new ActionResult();
            
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verifyNoInteractions(findAll);
        }
        
        @Test
        @DisplayName("Should accumulate matches from multiple scenes")
        void shouldAccumulateMatchesFromMultipleScenes() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            List<Scene> scenes = Arrays.asList(scene, scene);
            ActionResult result = new ActionResult();
            
            Match match1 = new Match.Builder().setSimScore(0.9).build();
            Match match2 = new Match.Builder().setSimScore(0.85).build();
            
            lenient().when(findAll.find(any(), any(), any()))
                .thenReturn(Collections.singletonList(match1))
                .thenReturn(Collections.singletonList(match2));
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            when(actionLifecycleManagement.isOkToContinueAction(any(), anyInt())).thenReturn(true);
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verify(findAll, times(2)).find(any(StateImage.class), any(Scene.class), nullable(ActionConfig.class));
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null state images")
        void shouldHandleNullStateImages() {
            // Arrange
            List<Scene> scenes = Collections.singletonList(scene);
            ActionResult result = new ActionResult();
            
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                iterativePatternFinder.find(result, null, scenes)
            );
        }
        
        @Test
        @DisplayName("Should handle null scenes")
        void shouldHandleNullScenes() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            ActionResult result = new ActionResult();
            
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                iterativePatternFinder.find(result, stateImages, null)
            );
        }
        
        @Test
        @DisplayName("Should handle null action result")
        void shouldHandleNullActionResult() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            List<Scene> scenes = Collections.singletonList(scene);
            
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                iterativePatternFinder.find(null, stateImages, scenes)
            );
        }
        
        @Test
        @DisplayName("Should handle exception from FindAll")
        void shouldHandleExceptionFromFindAll() {
            // Arrange
            List<StateImage> stateImages = Collections.singletonList(stateImage);
            List<Scene> scenes = Collections.singletonList(scene);
            ActionResult result = new ActionResult();
            
            when(findAll.find(any(StateImage.class), any(Scene.class), any()))
                .thenThrow(new RuntimeException("Find failed"));
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                iterativePatternFinder.find(result, stateImages, scenes)
            );
        }
    }
}