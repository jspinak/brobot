package io.github.jspinak.brobot.action.internal.find.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.find.pixel.ColorAnalysisOrchestrator;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;

@DisplayName("SceneAnalysisCollectionBuilder Simple Tests")
class SceneAnalysisCollectionBuilderSimpleTest extends BrobotTestBase {

    private SceneAnalysisCollectionBuilder builder;

    @Mock private BrobotProperties brobotProperties;

    @Mock private SceneProvider sceneProvider;

    @Mock private ColorAnalysisOrchestrator colorAnalysisOrchestrator;

    @Mock private StateService stateService;

    @Mock private StateMemory stateMemory;

    @Mock private PatternFindOptions patternFindOptions;

    @Mock private ActionConfig mockActionConfig;

    @Mock private ObjectCollection objectCollection;

    private Scene scene;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        scene = MockSceneBuilder.createMockScene(); // Create proper Scene with Pattern and Image
        builder =
                new SceneAnalysisCollectionBuilder(
                        brobotProperties,
                        sceneProvider,
                        colorAnalysisOrchestrator,
                        stateService,
                        stateMemory);
    }

    @Test
    @DisplayName("Should create scene analysis collection without color analysis")
    void testGet_NoColorAnalysis() {
        // Arrange
        List<ObjectCollection> collections = Arrays.asList(objectCollection);
        List<Scene> scenes = Arrays.asList(scene);

        when(sceneProvider.getScenes(any(), anyList(), anyInt(), anyDouble())).thenReturn(scenes);

        // Act
        SceneAnalyses result = builder.get(collections, 3, 0.5, patternFindOptions);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSceneAnalyses().size());
        verify(sceneProvider).getScenes(eq(patternFindOptions), eq(collections), eq(3), eq(0.5));
        verifyNoInteractions(colorAnalysisOrchestrator);
    }

    @Test
    @DisplayName("Should handle non-PatternFindOptions ActionConfig")
    void testGet_NonPatternFindOptions() {
        // Arrange
        List<ObjectCollection> collections = Arrays.asList(objectCollection);
        List<Scene> scenes = Arrays.asList(scene);

        when(sceneProvider.getScenes(any(), anyList(), anyInt(), anyDouble())).thenReturn(scenes);

        // Act
        SceneAnalyses result = builder.get(collections, 3, 0.5, mockActionConfig);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSceneAnalyses().size());
        verifyNoInteractions(colorAnalysisOrchestrator);
    }

    @Test
    @DisplayName("Should handle multiple scenes")
    void testGet_MultipleScenes() {
        // Arrange
        List<ObjectCollection> collections = Arrays.asList(objectCollection);
        Scene scene1 = MockSceneBuilder.createMockScene();
        Scene scene2 = MockSceneBuilder.createMockScene();
        Scene scene3 = MockSceneBuilder.createMockScene();
        List<Scene> scenes = Arrays.asList(scene1, scene2, scene3);

        when(sceneProvider.getScenes(any(), anyList(), anyInt(), anyDouble())).thenReturn(scenes);

        // Act
        SceneAnalyses result = builder.get(collections, 3, 0.5, patternFindOptions);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getSceneAnalyses().size());
    }

    @Test
    @DisplayName("Should handle empty scenes list")
    void testGet_EmptyScenes() {
        // Arrange
        List<ObjectCollection> collections = Arrays.asList(objectCollection);
        List<Scene> scenes = Collections.emptyList();

        when(sceneProvider.getScenes(any(), anyList(), anyInt(), anyDouble())).thenReturn(scenes);

        // Act
        SceneAnalyses result = builder.get(collections, 1, 0.5, patternFindOptions);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSceneAnalyses().isEmpty());
    }

    @Test
    @DisplayName("Should handle null collections")
    void testGet_NullCollections() {
        // Arrange
        List<Scene> scenes = Arrays.asList(scene);
        when(sceneProvider.getScenes(any(), isNull(), anyInt(), anyDouble())).thenReturn(scenes);

        // Act
        SceneAnalyses result = builder.get(null, 1, 0.5, patternFindOptions);

        // Assert
        assertNotNull(result);
    }
}
