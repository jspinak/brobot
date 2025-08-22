package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.color.*;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.builders.MockSceneBuilder;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Using ColorCluster.ColorSchemaName directly in code instead of static imports
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ColorClassifier Tests")
public class ColorClassifierTest extends BrobotTestBase {

    @Mock
    private ColorMatrixUtilities matOps3d;
    
    @Mock
    private Scene scene;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private Image image;
    
    // StateImage can't be mocked due to bytecode instrumentation issues
    // Will create real instances as needed in tests
    
    @Mock
    private PixelProfile pixelProfile;
    
    @Mock
    private ColorCluster colorCluster;
    
    @Mock
    private ColorSchema colorSchemaBGR;
    
    @Mock
    private ColorSchema colorSchemaHSV;
    
    @Mock
    private ColorStatistics colorStatistics;
    
    @InjectMocks
    private ColorClassifier colorClassifier;
    
    private List<PixelProfiles> pixelAnalysisCollections;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        // Don't configure mock Mats here - they may not be initialized yet
        
        pixelAnalysisCollections = new ArrayList<>();
        
        // StateImage setup will be done in individual tests as needed
    }
    
    // No need for cleanup with mock Mat objects
    
    @Test
    @DisplayName("Should return empty SceneAnalysis for empty collections")
    void shouldReturnEmptySceneAnalysisForEmptyCollections() {
        // Use MockSceneBuilder to create a properly initialized scene
        Scene mockScene = MockSceneBuilder.createMockScene();
        
        SceneAnalysis result = colorClassifier.getSceneAnalysis(new ArrayList<>(), mockScene);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    @DisplayName("Should create SceneAnalysis with BGR and HSV indices")
    void shouldCreateSceneAnalysisWithIndices() {
        // Use MockSceneBuilder for properly initialized data
        PixelProfiles profiles = MockSceneBuilder.createMockPixelProfile(1);
        pixelAnalysisCollections.add(profiles);
        
        doNothing().when(matOps3d).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), anyInt());
        
        Scene mockScene = MockSceneBuilder.createMockScene();
        SceneAnalysis result = colorClassifier.getSceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertNotNull(result);
        assertEquals(pixelAnalysisCollections, result.getPixelAnalysisCollections());
        assertEquals(mockScene, result.getScene());
        verify(matOps3d, atLeastOnce()).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), eq(1));
    }
    
    @Test
    @DisplayName("Should handle multiple pixel profiles in scene analysis")
    void shouldHandleMultiplePixelProfiles() {
        // Use MockSceneBuilder to create properly initialized profiles
        PixelProfiles profiles1 = MockSceneBuilder.createMockPixelProfile(1);
        PixelProfiles profiles2 = MockSceneBuilder.createMockPixelProfile(2);
        
        pixelAnalysisCollections.add(profiles1);
        pixelAnalysisCollections.add(profiles2);
        
        // Use mock scene from builder
        Scene mockScene = MockSceneBuilder.createMockScene();
        
        SceneAnalysis result = colorClassifier.getSceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        // The minIndex is called once per profile in getImageIndices, 
        // and once per color schema (BGR and HSV), so 2 profiles * 2 schemas = 4 calls
        verify(matOps3d, times(4)).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), anyInt());
    }
    
    @Test
    @DisplayName("Should return empty Mat for empty analysis")
    void shouldReturnEmptyMatForEmptyAnalysis() {
        // Use MockSceneBuilder to create a properly initialized scene
        Scene mockScene = MockSceneBuilder.createMockScene();
        
        SceneAnalysis sceneAnalysis = new SceneAnalysis(new ArrayList<>(), mockScene);
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, ColorCluster.ColorSchemaName.BGR);
        
        assertNotNull(result);
        assertTrue(result.empty());
    }
    
    @Test
    @DisplayName("Should process BGR color schema")
    void shouldProcessBGRColorSchema() {
        // Use MockSceneBuilder for properly initialized data
        SceneAnalysis sceneAnalysis = MockSceneBuilder.sceneAnalysis()
            .withDefaultColorCluster()
            .withPixelProfile(1)
            .build();
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, ColorCluster.ColorSchemaName.BGR);
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should process HSV color schema")
    void shouldProcessHSVColorSchema() {
        // Use MockSceneBuilder for properly initialized data
        SceneAnalysis sceneAnalysis = MockSceneBuilder.sceneAnalysis()
            .withDefaultColorCluster()
            .withPixelProfile(1)
            .build();
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, ColorCluster.ColorSchemaName.HSV);
        
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should handle state images with different indices")
    void shouldHandleStateImagesWithDifferentIndices() {
        // Use MockSceneBuilder to create properly initialized profiles
        PixelProfiles profiles1 = MockSceneBuilder.createMockPixelProfile(0);
        PixelProfiles profiles2 = MockSceneBuilder.createMockPixelProfile(5);
        PixelProfiles profiles3 = MockSceneBuilder.createMockPixelProfile(10);
        
        pixelAnalysisCollections.add(profiles1);
        pixelAnalysisCollections.add(profiles2);
        pixelAnalysisCollections.add(profiles3);
        
        Scene mockScene = MockSceneBuilder.createMockScene();
        SceneAnalysis sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, ColorCluster.ColorSchemaName.BGR);
        
        assertNotNull(result);
        verify(matOps3d).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), eq(0));
        verify(matOps3d).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), eq(5));
        verify(matOps3d).minIndex(any(Mat.class), any(Mat.class), any(Mat.class), eq(10));
    }
    
    @Test
    @DisplayName("Should create scene analysis with proper structure")
    void shouldCreateSceneAnalysisWithProperStructure() {
        // Use MockSceneBuilder for properly initialized data
        PixelProfiles profiles = MockSceneBuilder.createMockPixelProfile(1);
        pixelAnalysisCollections.add(profiles);
        
        Scene mockScene = MockSceneBuilder.createMockScene();
        SceneAnalysis result = colorClassifier.getSceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertNotNull(result);
        assertNotNull(result.getPixelAnalysisCollections());
        assertEquals(1, result.getPixelAnalysisCollections().size());
        assertEquals(profiles, result.getPixelAnalysisCollection(0));
        assertEquals(mockScene, result.getScene());
    }
    
    @Test
    @DisplayName("Should throw exception with null scene")
    void shouldThrowExceptionWithNullScene() {
        PixelProfiles profiles = mock(PixelProfiles.class);
        pixelAnalysisCollections.add(profiles);
        
        assertThrows(NullPointerException.class, () -> {
            colorClassifier.getSceneAnalysis(pixelAnalysisCollections, null);
        });
    }
}