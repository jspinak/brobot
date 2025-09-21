package io.github.jspinak.brobot.model.analysis.color;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static org.bytedeco.opencv.global.opencv_core.CV_32FC1;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for PixelProfiles. Tests pixel analysis collection management and
 * aggregate scoring.
 */
@DisplayName("PixelProfiles Tests")
public class PixelProfilesTest extends BrobotTestBase {

    private PixelProfiles pixelProfiles;
    private Scene mockScene;
    private Pattern mockPattern;
    private Image mockImage;
    private Mat matBGR;
    private Mat matHSV;
    private PixelProfile mockPixelProfile1;
    private PixelProfile mockPixelProfile2;
    private StateImage mockStateImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();

        // Create test Mats
        matBGR = new Mat(100, 100, CV_8UC3);
        matHSV = new Mat(100, 100, CV_8UC3);

        // Setup mock scene
        mockScene = mock(Scene.class);
        mockPattern = mock(Pattern.class);
        mockImage = mock(Image.class);

        when(mockScene.getPattern()).thenReturn(mockPattern);
        when(mockPattern.getImage()).thenReturn(mockImage);
        when(mockImage.getMatBGR()).thenReturn(matBGR);
        when(mockImage.getMatHSV()).thenReturn(matHSV);

        // Setup mock PixelProfiles
        mockPixelProfile1 = mock(PixelProfile.class);
        mockPixelProfile2 = mock(PixelProfile.class);

        // Setup mock StateImage
        mockStateImage = mock(StateImage.class);
        when(mockStateImage.getName()).thenReturn("testImage");
        when(mockStateImage.getIndex()).thenReturn(1);

        // Create PixelProfiles instance
        pixelProfiles = new PixelProfiles(mockScene);
    }

    @AfterEach
    void tearDown() {
        if (matBGR != null && !matBGR.isNull()) matBGR.release();
        if (matHSV != null && !matHSV.isNull()) matHSV.release();
    }

    @Nested
    @DisplayName("Constructor and Initialization")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with scene images")
        void shouldInitializeWithSceneImages() {
            assertNotNull(pixelProfiles);
            assertNotNull(pixelProfiles.getPixelAnalyses());
            assertTrue(pixelProfiles.getPixelAnalyses().isEmpty());

            // Check scene images are stored
            assertEquals(matBGR, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCENE, BGR));
            assertEquals(matHSV, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCENE, HSV));
        }

        @Test
        @DisplayName("Should initialize analysis maps for BGR and HSV")
        void shouldInitializeAnalysisMaps() {
            Map<ColorCluster.ColorSchemaName, Map<PixelProfiles.Analysis, Mat>> analyses =
                    pixelProfiles.getAnalyses();

            assertNotNull(analyses);
            assertTrue(analyses.containsKey(BGR));
            assertTrue(analyses.containsKey(HSV));
            assertNotNull(analyses.get(BGR));
            assertNotNull(analyses.get(HSV));
        }

        @Test
        @DisplayName("Should not have StateImage initially")
        void shouldNotHaveStateImageInitially() {
            assertNull(pixelProfiles.getStateImage());
        }
    }

    @Nested
    @DisplayName("PixelProfile Management")
    class PixelProfileManagement {

        @Test
        @DisplayName("Should add single PixelProfile")
        void shouldAddSinglePixelProfile() {
            pixelProfiles.add(mockPixelProfile1);

            assertEquals(1, pixelProfiles.getPixelAnalyses().size());
            assertTrue(pixelProfiles.getPixelAnalyses().contains(mockPixelProfile1));
        }

        @Test
        @DisplayName("Should add multiple PixelProfiles")
        void shouldAddMultiplePixelProfiles() {
            pixelProfiles.add(mockPixelProfile1);
            pixelProfiles.add(mockPixelProfile2);

            assertEquals(2, pixelProfiles.getPixelAnalyses().size());
            assertTrue(pixelProfiles.getPixelAnalyses().contains(mockPixelProfile1));
            assertTrue(pixelProfiles.getPixelAnalyses().contains(mockPixelProfile2));
        }

        @Test
        @DisplayName("Should get PixelProfile by index")
        void shouldGetPixelProfileByIndex() {
            pixelProfiles.add(mockPixelProfile1);
            pixelProfiles.add(mockPixelProfile2);

            assertEquals(mockPixelProfile1, pixelProfiles.getPixelAnalyses().get(0));
            assertEquals(mockPixelProfile2, pixelProfiles.getPixelAnalyses().get(1));
        }

        @Test
        @DisplayName("Should throw exception for invalid index")
        void shouldThrowExceptionForInvalidIndex() {
            pixelProfiles.add(mockPixelProfile1);

            assertThrows(
                    IndexOutOfBoundsException.class, () -> pixelProfiles.getPixelAnalyses().get(5));
        }

        @Test
        @DisplayName("Should return correct size")
        void shouldReturnCorrectSize() {
            assertEquals(0, pixelProfiles.getPixelAnalyses().size());

            pixelProfiles.add(mockPixelProfile1);
            assertEquals(1, pixelProfiles.getPixelAnalyses().size());

            pixelProfiles.add(mockPixelProfile2);
            assertEquals(2, pixelProfiles.getPixelAnalyses().size());
        }
    }

    @Nested
    @DisplayName("StateImage Management")
    class StateImageManagement {

        @Test
        @DisplayName("Should set and get StateImage")
        void shouldSetAndGetStateImage() {
            pixelProfiles.setStateImage(mockStateImage);

            assertEquals(mockStateImage, pixelProfiles.getStateImage());
        }

        @Test
        @DisplayName("Should get image name from StateImage")
        void shouldGetImageNameFromStateImage() {
            pixelProfiles.setStateImage(mockStateImage);

            String imageName = pixelProfiles.getImageName();

            assertEquals("testImage", imageName);
        }

        @Test
        @DisplayName("Should throw exception when StateImage is null")
        void shouldThrowExceptionWhenStateImageNull() {
            pixelProfiles.setStateImage(null);

            assertThrows(NullPointerException.class, () -> pixelProfiles.getImageName());
        }
    }

    @Nested
    @DisplayName("Analysis Data Management")
    class AnalysisDataManagement {

        private Mat scoreMat;
        private Mat thresholdMat;

        @BeforeEach
        void setup() {
            scoreMat = new Mat(100, 100, CV_32FC1);
            thresholdMat = new Mat(100, 100, CV_32FC1);
        }

        @AfterEach
        void cleanup() {
            if (scoreMat != null && !scoreMat.isNull()) scoreMat.release();
            if (thresholdMat != null && !thresholdMat.isNull()) thresholdMat.release();
        }

        @Test
        @DisplayName("Should add analysis data for BGR")
        void shouldAddAnalysisForBGR() {
            pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, scoreMat);

            Mat retrieved = pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR);
            assertEquals(scoreMat, retrieved);
        }

        @Test
        @DisplayName("Should add analysis data for HSV")
        void shouldAddAnalysisForHSV() {
            pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, HSV, scoreMat);

            Mat retrieved = pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, HSV);
            assertEquals(scoreMat, retrieved);
        }

        @Test
        @DisplayName("Should store multiple analysis types")
        void shouldStoreMultipleAnalysisTypes() {
            pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, scoreMat);
            pixelProfiles.setAnalyses(
                    PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD, BGR, thresholdMat);

            assertEquals(scoreMat, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
            assertEquals(
                    thresholdMat,
                    pixelProfiles.getAnalysis(
                            PixelProfiles.Analysis.SCORE_DIST_BELOW_THRESHHOLD, BGR));
        }

        @Test
        @DisplayName("Should overwrite existing analysis")
        void shouldOverwriteExistingAnalysis() {
            Mat originalMat = new Mat(50, 50, CV_32FC1);
            Mat newMat = new Mat(75, 75, CV_32FC1);

            try {
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, originalMat);
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, newMat);

                assertEquals(newMat, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
            } finally {
                originalMat.release();
                newMat.release();
            }
        }

        @ParameterizedTest
        @EnumSource(PixelProfiles.Analysis.class)
        @DisplayName("Should handle all analysis types")
        void shouldHandleAllAnalysisTypes(PixelProfiles.Analysis analysisType) {
            if (analysisType
                    != PixelProfiles.Analysis.SCENE) { // SCENE is already set in constructor
                Mat testMat = new Mat(10, 10, CV_32FC1);
                try {
                    pixelProfiles.setAnalyses(analysisType, BGR, testMat);
                    assertEquals(testMat, pixelProfiles.getAnalysis(analysisType, BGR));
                } finally {
                    testMat.release();
                }
            }
        }

        @Test
        @DisplayName("Should return null for non-existent analysis")
        void shouldReturnNullForNonExistentAnalysis() {
            Mat result = pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Score Management")
    class ScoreManagement {

        @Test
        @DisplayName("Should check if has scores")
        void shouldCheckIfHasScores() {
            // Initially no scores
            assertNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
            assertNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, HSV));

            Mat scoreMat = new Mat(100, 100, CV_32FC1);
            try {
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, scoreMat);
                assertNotNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
            } finally {
                scoreMat.release();
            }
        }

        @Test
        @DisplayName("Should check scores for both color schemas")
        void shouldCheckScoresForBothColorSchemas() {
            assertNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
            assertNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, HSV));

            Mat scoreBGR = new Mat(100, 100, CV_32FC1);
            Mat scoreHSV = new Mat(100, 100, CV_32FC1);

            try {
                // Add only BGR score
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, scoreBGR);
                assertNotNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));

                // Clear and add only HSV score
                pixelProfiles.getAnalyses().get(BGR).remove(PixelProfiles.Analysis.SCORE);
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, HSV, scoreHSV);
                assertNotNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, HSV));
            } finally {
                scoreBGR.release();
                scoreHSV.release();
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty PixelProfile list")
        void shouldHandleEmptyPixelProfileList() {
            assertEquals(0, pixelProfiles.getPixelAnalyses().size());
            assertThrows(
                    IndexOutOfBoundsException.class, () -> pixelProfiles.getPixelAnalyses().get(0));
        }

        @Test
        @DisplayName("Should handle null Mat in analysis")
        void shouldHandleNullMatInAnalysis() {
            pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, null);
            assertNull(pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCORE, BGR));
        }

        @Test
        @DisplayName("Should maintain scene images after other operations")
        void shouldMaintainSceneImagesAfterOperations() {
            // Add other analyses
            Mat scoreMat = new Mat(100, 100, CV_32FC1);
            try {
                pixelProfiles.setAnalyses(PixelProfiles.Analysis.SCORE, BGR, scoreMat);
                pixelProfiles.add(mockPixelProfile1);
                pixelProfiles.setStateImage(mockStateImage);

                // Scene images should still be present
                assertEquals(matBGR, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCENE, BGR));
                assertEquals(matHSV, pixelProfiles.getAnalysis(PixelProfiles.Analysis.SCENE, HSV));
            } finally {
                scoreMat.release();
            }
        }

        @Test
        @DisplayName("Should handle large number of PixelProfiles")
        void shouldHandleLargeNumberOfPixelProfiles() {
            for (int i = 0; i < 100; i++) {
                PixelProfile mockProfile = mock(PixelProfile.class);
                pixelProfiles.add(mockProfile);
            }

            assertEquals(100, pixelProfiles.getPixelAnalyses().size());
        }
    }
}
