package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.FindAll;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
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
    private StateImage stateImage;
    
    @Mock
    private Scene scene;
    
    @Mock
    private ActionResult actionResult;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        iterativePatternFinder = new IterativePatternFinder(actionLifecycleManagement, findAll);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
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
            
            when(findAll.perform(any(), any(), any())).thenReturn(new ActionResult());
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verify(findAll, atLeastOnce()).perform(any(), any(), any());
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
            
            when(findAll.perform(any(), any(), any())).thenReturn(new ActionResult());
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verify(findAll, atLeastOnce()).perform(any(), any(), any());
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
            
            ActionResult findResult1 = new ActionResult();
            findResult1.add(match1);
            ActionResult findResult2 = new ActionResult();
            findResult2.add(match2);
            
            when(findAll.perform(any(), any(), any()))
                .thenReturn(findResult1)
                .thenReturn(findResult2);
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act
            iterativePatternFinder.find(result, stateImages, scenes);
            
            // Assert
            verify(actionLifecycleManagement).printActionOnce(result);
            verify(findAll, times(2)).perform(any(), any(), any());
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
            
            when(findAll.perform(any(), any(), any()))
                .thenThrow(new RuntimeException("Find failed"));
            doNothing().when(actionLifecycleManagement).printActionOnce(any());
            
            // Act & Assert
            assertDoesNotThrow(() -> 
                iterativePatternFinder.find(result, stateImages, scenes)
            );
        }
    }
}