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

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
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
    
    @Mock
    private StateImage stateImage;
    
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
    
    private Mat testMat;
    private Mat sceneBGR;
    private Mat sceneHSV;
    private List<PixelProfiles> pixelAnalysisCollections;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        testMat = new Mat(100, 100, 16); // CV_8UC3
        sceneBGR = new Mat(100, 100, 16); // CV_8UC3
        sceneHSV = new Mat(100, 100, 16); // CV_8UC3
        pixelAnalysisCollections = new ArrayList<>();
        
        // Setup scene mock hierarchy
        when(scene.getPattern()).thenReturn(pattern);
        when(pattern.getImage()).thenReturn(image);
        when(image.getMatBGR()).thenReturn(sceneBGR);
        when(image.getMatHSV()).thenReturn(sceneHSV);
        
        // Create real StateImage and set up its color cluster
        stateImage = new StateImage();
        stateImage.setIndex(1);
        stateImage.setColorCluster(colorCluster);
        
        when(colorCluster.getSchema(BGR)).thenReturn(colorSchemaBGR);
        when(colorCluster.getSchema(HSV)).thenReturn(colorSchemaHSV);
        when(colorSchemaBGR.getColorStatistics(any())).thenReturn(colorStatistics);
        when(colorSchemaHSV.getColorStatistics(any())).thenReturn(colorStatistics);
        when(colorStatistics.getStat(any())).thenReturn(100.0);
    }
    
    @AfterEach
    public void tearDown() {
        if (testMat != null && !testMat.isNull()) {
            testMat.release();
        }
        if (sceneBGR != null && !sceneBGR.isNull()) {
            sceneBGR.release();
        }
        if (sceneHSV != null && !sceneHSV.isNull()) {
            sceneHSV.release();
        }
    }
    
    @Test
    @DisplayName("Should return empty SceneAnalysis for empty collections")
    void shouldReturnEmptySceneAnalysisForEmptyCollections() {
        SceneAnalysis result = colorClassifier.getSceneAnalysis(new ArrayList<>(), scene);
        
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
        SceneAnalysis sceneAnalysis = new SceneAnalysis(new ArrayList<>(), scene);
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, BGR);
        
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
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, BGR);
        
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
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, HSV);
        
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
        
        Mat result = colorClassifier.getImageIndices(sceneAnalysis, BGR);
        
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