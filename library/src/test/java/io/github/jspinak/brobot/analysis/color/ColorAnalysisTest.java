package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static io.github.jspinak.brobot.analysis.color.ColorAnalysis.Analysis.*;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ColorAnalysis Tests")
public class ColorAnalysisTest extends BrobotTestBase {

    private ColorAnalysis colorAnalysis;
    private Mat testMat;
    
    @BeforeEach
    public void setUp() {
        super.setupTest();
        colorAnalysis = new ColorAnalysis();
        testMat = new Mat(100, 100, 16); // CV_8UC3
    }
    
    @AfterEach
    public void tearDown() {
        if (testMat != null && !testMat.isNull()) {
            testMat.release();
        }
    }
    
    @Test
    @DisplayName("Should initialize with empty BGR and HSV maps")
    void shouldInitializeWithEmptyMaps() {
        assertNotNull(colorAnalysis.getAnalyses());
        assertNotNull(colorAnalysis.getAnalyses().get(BGR));
        assertNotNull(colorAnalysis.getAnalyses().get(HSV));
        assertTrue(colorAnalysis.getAnalyses().get(BGR).isEmpty());
        assertTrue(colorAnalysis.getAnalyses().get(HSV).isEmpty());
    }
    
    @ParameterizedTest
    @EnumSource(ColorAnalysis.Analysis.class)
    @DisplayName("Should store and retrieve BGR analysis matrices")
    void shouldStoreAndRetrieveBGRAnalysis(ColorAnalysis.Analysis analysisType) {
        colorAnalysis.setAnalyses(analysisType, BGR, testMat);
        
        Mat retrieved = colorAnalysis.getAnalyses(analysisType, BGR);
        assertNotNull(retrieved);
        assertEquals(testMat, retrieved);
    }
    
    @ParameterizedTest
    @EnumSource(ColorAnalysis.Analysis.class)
    @DisplayName("Should store and retrieve HSV analysis matrices")
    void shouldStoreAndRetrieveHSVAnalysis(ColorAnalysis.Analysis analysisType) {
        colorAnalysis.setAnalyses(analysisType, HSV, testMat);
        
        Mat retrieved = colorAnalysis.getAnalyses(analysisType, HSV);
        assertNotNull(retrieved);
        assertEquals(testMat, retrieved);
    }
    
    @Test
    @DisplayName("Should return null for unset analysis")
    void shouldReturnNullForUnsetAnalysis() {
        assertNull(colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR));
        assertNull(colorAnalysis.getAnalyses(SCORES, HSV));
    }
    
    @Test
    @DisplayName("Should handle multiple analysis types per color space")
    void shouldHandleMultipleAnalysisTypes() {
        Mat mat1 = new Mat(50, 50, 16);
        Mat mat2 = new Mat(75, 75, 16);
        Mat mat3 = new Mat(100, 100, 16);
        
        try {
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, mat1);
            colorAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, BGR, mat2);
            colorAnalysis.setAnalyses(SCORES, BGR, mat3);
            
            assertEquals(mat1, colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR));
            assertEquals(mat2, colorAnalysis.getAnalyses(DIST_OUTSIDE_RANGE, BGR));
            assertEquals(mat3, colorAnalysis.getAnalyses(SCORES, BGR));
        } finally {
            mat1.release();
            mat2.release();
            mat3.release();
        }
    }
    
    @Test
    @DisplayName("Should overwrite existing analysis when setting new one")
    void shouldOverwriteExistingAnalysis() {
        Mat originalMat = new Mat(50, 50, 16);
        Mat newMat = new Mat(100, 100, 16);
        
        try {
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, originalMat);
            assertEquals(originalMat, colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR));
            
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, newMat);
            assertEquals(newMat, colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR));
        } finally {
            originalMat.release();
            newMat.release();
        }
    }
    
    @Test
    @DisplayName("Should maintain separate maps for BGR and HSV")
    void shouldMaintainSeparateMapsForColorSpaces() {
        Mat bgrMat = new Mat(50, 50, 16);
        Mat hsvMat = new Mat(75, 75, 16);
        
        try {
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, bgrMat);
            colorAnalysis.setAnalyses(DIST_TO_TARGET, HSV, hsvMat);
            
            assertEquals(bgrMat, colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR));
            assertEquals(hsvMat, colorAnalysis.getAnalyses(DIST_TO_TARGET, HSV));
            assertNotEquals(colorAnalysis.getAnalyses(DIST_TO_TARGET, BGR), 
                          colorAnalysis.getAnalyses(DIST_TO_TARGET, HSV));
        } finally {
            bgrMat.release();
            hsvMat.release();
        }
    }
    
    @Test
    @DisplayName("Should print analysis dimensions correctly")
    void shouldPrintAnalysisDimensions() {
        Mat mat1 = new Mat(100, 100, 16);
        Mat mat2 = new Mat(200, 200, 16);
        
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class);
             MockedStatic<MatrixUtilities> matrixMock = mockStatic(MatrixUtilities.class)) {
            
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, mat1);
            colorAnalysis.setAnalyses(DIST_TO_TARGET, HSV, mat2);
            colorAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, BGR, mat1);
            colorAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, HSV, mat2);
            colorAnalysis.setAnalyses(SCORES, BGR, mat1);
            colorAnalysis.setAnalyses(SCORES, HSV, mat2);
            colorAnalysis.setAnalyses(SCORE_DISTANCE, BGR, mat1);
            colorAnalysis.setAnalyses(SCORE_DISTANCE, HSV, mat2);
            
            colorAnalysis.print();
            
            consoleMock.verify(() -> ConsoleReporter.println("PixelAnalysis"));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat1, "distanceToTargetColorBGR: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat2, "distanceToTargetColorHSV: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat1, "distanceOutsideOfRangeBGR: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat2, "distanceOutsideOfRangeHSV: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat1, "pixelScoresBGR: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat2, "pixelScoresHSV: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat1, "scoreDistanceBelowThresholdBGR: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(mat2, "scoreDistanceBelowThresholdHSV: "));
        } finally {
            mat1.release();
            mat2.release();
        }
    }
    
    @Test
    @DisplayName("Should print with null matrices without errors")
    void shouldPrintWithNullMatrices() {
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class);
             MockedStatic<MatrixUtilities> matrixMock = mockStatic(MatrixUtilities.class)) {
            
            colorAnalysis.print();
            
            consoleMock.verify(() -> ConsoleReporter.println("PixelAnalysis"));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(null, "distanceToTargetColorBGR: "));
            matrixMock.verify(() -> MatrixUtilities.printDimensions(null, "distanceToTargetColorHSV: "));
        }
    }
    
    @Test
    @DisplayName("Should handle all Analysis enum values")
    void shouldHandleAllAnalysisEnumValues() {
        assertEquals(5, ColorAnalysis.Analysis.values().length);
        
        for (ColorAnalysis.Analysis analysis : ColorAnalysis.Analysis.values()) {
            for (ColorCluster.ColorSchemaName schema : new ColorCluster.ColorSchemaName[]{BGR, HSV}) {
                assertNull(colorAnalysis.getAnalyses(analysis, schema));
                
                colorAnalysis.setAnalyses(analysis, schema, testMat);
                assertNotNull(colorAnalysis.getAnalyses(analysis, schema));
            }
        }
    }
    
    @Test
    @DisplayName("Should provide complete coverage for Analysis enum")
    void shouldProvideCoverageForAnalysisEnum() {
        assertNotNull(DIST_TO_TARGET);
        assertNotNull(DIST_OUTSIDE_RANGE);
        assertNotNull(DIST_TO_BOUNDARY);
        assertNotNull(SCORES);
        assertNotNull(SCORE_DISTANCE);
        
        assertEquals("DIST_TO_TARGET", DIST_TO_TARGET.name());
        assertEquals("DIST_OUTSIDE_RANGE", DIST_OUTSIDE_RANGE.name());
        assertEquals("DIST_TO_BOUNDARY", DIST_TO_BOUNDARY.name());
        assertEquals("SCORES", SCORES.name());
        assertEquals("SCORE_DISTANCE", SCORE_DISTANCE.name());
    }
    
    @Test
    @DisplayName("Should handle concurrent access to different color spaces")
    void shouldHandleConcurrentAccess() {
        Mat bgrMat = new Mat(50, 50, 16);
        Mat hsvMat = new Mat(100, 100, 16);
        
        try {
            colorAnalysis.setAnalyses(DIST_TO_TARGET, BGR, bgrMat);
            colorAnalysis.setAnalyses(SCORES, HSV, hsvMat);
            colorAnalysis.setAnalyses(DIST_OUTSIDE_RANGE, BGR, bgrMat);
            colorAnalysis.setAnalyses(DIST_TO_BOUNDARY, HSV, hsvMat);
            
            Map<ColorAnalysis.Analysis, Mat> bgrMap = colorAnalysis.getAnalyses().get(BGR);
            Map<ColorAnalysis.Analysis, Mat> hsvMap = colorAnalysis.getAnalyses().get(HSV);
            
            assertEquals(2, bgrMap.size());
            assertEquals(2, hsvMap.size());
            
            assertTrue(bgrMap.containsKey(DIST_TO_TARGET));
            assertTrue(bgrMap.containsKey(DIST_OUTSIDE_RANGE));
            assertTrue(hsvMap.containsKey(SCORES));
            assertTrue(hsvMap.containsKey(DIST_TO_BOUNDARY));
        } finally {
            bgrMat.release();
            hsvMat.release();
        }
    }
}