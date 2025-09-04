package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.model.analysis.color.*;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
// import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.HUE; // Compilation issue - commented temporarily
// import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE; // Visibility issue - using qualified name instead
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.bytedeco.opencv.global.opencv_core.*;

/**
 * Comprehensive test suite for SceneAnalysis.
 * Tests scene analysis capabilities including color analysis, pixel profiles,
 * and various analysis types.
 */
@DisplayName("SceneAnalysis Tests")
public class SceneAnalysisTest extends BrobotTestBase {
    
    private SceneAnalysis sceneAnalysis;
    private Scene mockScene;
    private Mat mockMatBGR;
    private Mat mockMatHSV;
    private List<PixelProfiles> pixelAnalysisCollections;
    
    @Mock
    private Pattern mockPattern;
    @Mock
    private Image mockImage;
    @Mock
    private PixelProfiles mockPixelProfiles1;
    @Mock
    private PixelProfiles mockPixelProfiles2;
    @Mock
    private StateImage mockStateImage1;
    @Mock
    private StateImage mockStateImage2;
    @Mock
    private ColorCluster mockColorCluster;
    @Mock
    private ColorSchema mockColorSchema;
    @Mock
    private ColorStatistics mockColorStatistics;
    
    private AutoCloseable closeable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        closeable = MockitoAnnotations.openMocks(this);
        
        // Create mock Mats with proper dimensions
        mockMatBGR = new Mat(100, 100, CV_8UC3);
        mockMatHSV = new Mat(100, 100, CV_8UC3);
        
        // Setup scene mock
        mockScene = mock(Scene.class);
        when(mockScene.getPattern()).thenReturn(mockPattern);
        when(mockPattern.getImage()).thenReturn(mockImage);
        when(mockImage.getMatBGR()).thenReturn(mockMatBGR);
        when(mockImage.getMatHSV()).thenReturn(mockMatHSV);
        
        // Setup pixel analysis collections
        pixelAnalysisCollections = new ArrayList<>();
        pixelAnalysisCollections.add(mockPixelProfiles1);
        pixelAnalysisCollections.add(mockPixelProfiles2);
        
        // Setup state images
        when(mockPixelProfiles1.getStateImage()).thenReturn(mockStateImage1);
        when(mockPixelProfiles2.getStateImage()).thenReturn(mockStateImage2);
        when(mockPixelProfiles1.getImageName()).thenReturn("Image1");
        when(mockPixelProfiles2.getImageName()).thenReturn("Image2");
        when(mockStateImage1.getIndex()).thenReturn(0);
        when(mockStateImage2.getIndex()).thenReturn(1);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockMatBGR != null && !mockMatBGR.isNull()) mockMatBGR.close();
        if (mockMatHSV != null && !mockMatHSV.isNull()) mockMatHSV.close();
        if (closeable != null) closeable.close();
    }
    
    @Test
    @DisplayName("Should create scene analysis with pixel analysis collections")
    void shouldCreateSceneAnalysisWithPixelCollections() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertNotNull(sceneAnalysis);
        assertEquals(pixelAnalysisCollections, sceneAnalysis.getPixelAnalysisCollections());
        assertEquals(mockScene, sceneAnalysis.getScene());
        assertNotNull(sceneAnalysis.getAnalysis(BGR, SCENE));
        assertNotNull(sceneAnalysis.getAnalysis(HSV, SCENE));
        assertNotNull(sceneAnalysis.getIllustrations());
    }
    
    @Test
    @DisplayName("Should create minimal scene analysis without pixel collections")
    void shouldCreateMinimalSceneAnalysis() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        
        assertNotNull(sceneAnalysis);
        assertEquals(mockScene, sceneAnalysis.getScene());
        assertTrue(sceneAnalysis.getPixelAnalysisCollections().isEmpty());
        assertNotNull(sceneAnalysis.getAnalysis(BGR, SCENE));
        assertNotNull(sceneAnalysis.getAnalysis(HSV, SCENE));
        assertNotNull(sceneAnalysis.getIllustrations());
    }
    
    @Test
    @DisplayName("Should add and retrieve analysis matrices")
    void shouldAddAndRetrieveAnalysisMatrices() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        Mat testMat = new Mat(50, 50, CV_8UC3);
        
        try {
            sceneAnalysis.addAnalysis(BGR, INDICES_3D, testMat);
            Mat retrieved = sceneAnalysis.getAnalysis(BGR, INDICES_3D);
            
            assertNotNull(retrieved);
            assertEquals(testMat, retrieved);
        } finally {
            testMat.close();
        }
    }
    
    @Test
    @DisplayName("Should update illustrations when adding BGR_FROM_INDICES_2D")
    void shouldUpdateIllustrationsWithBGRFromIndices2D() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        Mat testMat = new Mat(50, 50, CV_8UC3);
        
        try {
            sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D, testMat);
            
            assertNotNull(sceneAnalysis.getIllustrations());
            assertNotNull(sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D));
        } finally {
            testMat.close();
        }
    }
    
    @Test
    @DisplayName("Should get state image objects from pixel collections")
    void shouldGetStateImageObjects() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        List<StateImage> stateImages = sceneAnalysis.getStateImageObjects();
        
        assertEquals(2, stateImages.size());
        assertTrue(stateImages.contains(mockStateImage1));
        assertTrue(stateImages.contains(mockStateImage2));
    }
    
    @Test
    @DisplayName("Should concatenate image names")
    void shouldConcatenateImageNames() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        String names = sceneAnalysis.getImageNames();
        
        assertEquals("Image1Image2", names);
    }
    
    @Test
    @DisplayName("Should return correct size")
    void shouldReturnCorrectSize() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertEquals(2, sceneAnalysis.size());
    }
    
    @Test
    @DisplayName("Should get pixel analysis collection by index")
    void shouldGetPixelAnalysisCollectionByIndex() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertEquals(mockPixelProfiles1, sceneAnalysis.getPixelAnalysisCollection(0));
        assertEquals(mockPixelProfiles2, sceneAnalysis.getPixelAnalysisCollection(1));
    }
    
    @Test
    @DisplayName("Should get last index from pixel collections")
    void shouldGetLastIndex() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        assertEquals(1, sceneAnalysis.getLastIndex());
    }
    
    @Test
    @DisplayName("Should return 0 for last index when no collections exist")
    void shouldReturnZeroForLastIndexWhenEmpty() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        
        assertEquals(0, sceneAnalysis.getLastIndex());
    }
    
    @Test
    @DisplayName("Should get color stat profiles")
    void shouldGetColorStatProfiles() {
        // Setup color cluster mocks
        when(mockStateImage1.getColorCluster()).thenReturn(mockColorCluster);
        when(mockStateImage2.getColorCluster()).thenReturn(mockColorCluster);
        when(mockColorCluster.getSchema(BGR)).thenReturn(mockColorSchema);
        when(mockColorSchema.getColorStatistics(ColorInfo.ColorStat.MEAN)).thenReturn(mockColorStatistics);
        
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        List<ColorStatistics> profiles = sceneAnalysis.getColorStatProfiles(BGR, ColorInfo.ColorStat.MEAN);
        
        assertNotNull(profiles);
        assertEquals(2, profiles.size());
        verify(mockColorCluster, times(2)).getSchema(BGR);
        verify(mockColorSchema, times(2)).getColorStatistics(ColorInfo.ColorStat.MEAN);
    }
    
    @Test
    @DisplayName("Should get color values from stat profiles")
    void shouldGetColorValues() {
        // Setup color statistics
        when(mockStateImage1.getColorCluster()).thenReturn(mockColorCluster);
        when(mockStateImage2.getColorCluster()).thenReturn(mockColorCluster);
        when(mockColorCluster.getSchema(HSV)).thenReturn(mockColorSchema);
        when(mockColorSchema.getColorStatistics(ColorInfo.ColorStat.MEAN)).thenReturn(mockColorStatistics);
        when(mockColorStatistics.getStat(ColorSchema.ColorValue.HUE)).thenReturn(120.5).thenReturn(180.7);
        
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        List<Integer> colorValues = sceneAnalysis.getColorValues(HSV, ColorInfo.ColorStat.MEAN, ColorSchema.ColorValue.HUE);
        
        assertNotNull(colorValues);
        assertEquals(2, colorValues.size());
        assertEquals(120, colorValues.get(0));
        assertEquals(180, colorValues.get(1));
    }
    
    @Test
    @DisplayName("Should find pixel analysis collection by state image")
    void shouldFindPixelAnalysisCollectionByStateImage() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        Optional<PixelProfiles> result = sceneAnalysis.getPixelAnalysisCollection(mockStateImage1);
        
        assertTrue(result.isPresent());
        assertEquals(mockPixelProfiles1, result.get());
    }
    
    @Test
    @DisplayName("Should return empty when state image not found")
    void shouldReturnEmptyWhenStateImageNotFound() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        StateImage unknownImage = mock(StateImage.class);
        
        Optional<PixelProfiles> result = sceneAnalysis.getPixelAnalysisCollection(unknownImage);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Should get scores for state image")
    void shouldGetScoresForStateImage() {
        Mat scoresMat = new Mat(50, 50, CV_32FC1);
        when(mockPixelProfiles1.getAnalysis(PixelProfiles.Analysis.SCORE, BGR)).thenReturn(scoresMat);
        
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        
        try {
            Optional<Mat> scores = sceneAnalysis.getScores(mockStateImage1);
            
            assertTrue(scores.isPresent());
            assertEquals(scoresMat, scores.get());
        } finally {
            scoresMat.close();
        }
    }
    
    @Test
    @DisplayName("Should return empty Mat when scores not found")
    void shouldReturnEmptyMatWhenScoresNotFound() {
        sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, mockScene);
        StateImage unknownImage = mock(StateImage.class);
        
        Mat scores = sceneAnalysis.getScoresMat(unknownImage);
        
        assertNotNull(scores);
        assertTrue(scores.empty());
        scores.close();
    }
    
    @Test
    @DisplayName("Should manage match list")
    void shouldManageMatchList() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        Match match1 = mock(Match.class);
        Match match2 = mock(Match.class);
        List<Match> matches = new ArrayList<>();
        matches.add(match1);
        matches.add(match2);
        
        sceneAnalysis.setMatchList(matches);
        
        assertEquals(matches, sceneAnalysis.getMatchList());
        assertEquals(2, sceneAnalysis.getMatchList().size());
    }
    
    @Test
    @DisplayName("Should set and get contour extractor")
    void shouldSetAndGetContourExtractor() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        ContourExtractor contourExtractor = mock(ContourExtractor.class);
        
        sceneAnalysis.setContours(contourExtractor);
        
        assertEquals(contourExtractor, sceneAnalysis.getContours());
    }
    
    @Test
    @DisplayName("Should handle all analysis types")
    void shouldHandleAllAnalysisTypes() {
        sceneAnalysis = new SceneAnalysis(mockScene);
        
        // Test all analysis types
        SceneAnalysis.Analysis[] allTypes = SceneAnalysis.Analysis.values();
        assertEquals(6, allTypes.length);
        
        // Verify each can be stored and retrieved
        for (SceneAnalysis.Analysis analysisType : allTypes) {
            if (analysisType != SCENE) { // SCENE is already added in constructor
                Mat testMat = new Mat(10, 10, CV_8UC1);
                try {
                    sceneAnalysis.addAnalysis(BGR, analysisType, testMat);
                    assertNotNull(sceneAnalysis.getAnalysis(BGR, analysisType));
                } finally {
                    testMat.close();
                }
            }
        }
    }
}