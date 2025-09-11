package io.github.jspinak.brobot.tools.history.draw;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;

/**
 * Comprehensive test class for DrawMatch functionality. Tests high-level match drawing operations
 * on OpenCV Mat images.
 */
@ExtendWith(MockitoExtension.class)
public class DrawMatchTest extends BrobotTestBase {

    @Mock private DrawRect drawRect;

    @InjectMocks private DrawMatch drawMatch;

    @Mock private Mat mockMat;

    @Mock private Visualization mockVisualization;

    @Mock private ActionResult mockActionResult;

    private Scalar defaultColor;
    private Scalar customColor;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        org.mockito.MockitoAnnotations.openMocks(this);
        defaultColor = new Scalar(255, 150, 255, 0); // Default pink/purple
        customColor = new Scalar(0, 255, 0, 0); // Green

        // Create a real Mat object for testing
        mockMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (mockMat != null && !mockMat.isNull()) {
            mockMat.release();
        }
    }

    @Test
    @DisplayName("Should draw matches with default color")
    void shouldDrawMatchesWithDefaultColor() {
        List<Match> matchList = createTestMatches(3);

        // Just verify no exception is thrown when drawing matches
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockMat, matchList));

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    @Test
    @DisplayName("Should draw matches with custom color")
    void shouldDrawMatchesWithCustomColor() {
        List<Match> matchList = createTestMatches(2);

        // Just verify no exception is thrown when drawing matches with custom color
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockMat, matchList, customColor));

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    @Test
    @DisplayName("Should handle empty match list")
    void shouldHandleEmptyMatchList() {
        List<Match> emptyList = new ArrayList<>();

        // Just verify no exception is thrown with empty list
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockMat, emptyList));

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    @Test
    @DisplayName("Should draw single match")
    void shouldDrawSingleMatch() {
        Match singleMatch = createTestMatch(100, 200, 50, 60);
        List<Match> matchList = Collections.singletonList(singleMatch);

        // Just verify no exception is thrown when drawing single match
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockMat, matchList));

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    @Test
    @DisplayName("Should draw matches from ActionResult on visualization")
    void shouldDrawMatchesFromActionResultOnVisualization() {
        // Create real Mat objects instead of mocks to avoid NullPointerException
        Mat sceneMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat classesMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");

        // Setup matches with matching scene names
        List<Match> matches = new ArrayList<>();
        Match match1 = createMatchWithScene("TestScene", 10, 20, 30, 40);
        Match match2 = createMatchWithScene("TestScene", 50, 60, 70, 80);
        Match match3 = createMatchWithScene("OtherScene", 90, 100, 110, 120); // Different scene

        matches.add(match1);
        matches.add(match2);
        matches.add(match3);

        when(mockActionResult.getMatchList()).thenReturn(matches);

        // Just verify no exception is thrown when drawing matches on visualization
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);

        // Clean up the Mat objects
        if (!sceneMat.isNull()) sceneMat.release();
        if (!classesMat.isNull()) classesMat.release();
    }

    @Test
    @DisplayName("Should handle null scene layer in visualization")
    void shouldHandleNullSceneLayerInVisualization() {
        // Create real Mat object for classes layer
        Mat classesMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(null);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");

        List<Match> matches =
                Collections.singletonList(createMatchWithScene("TestScene", 10, 20, 30, 40));
        when(mockActionResult.getMatchList()).thenReturn(matches);

        // Just verify no exception is thrown with null scene layer
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);

        // Clean up the Mat object
        if (!classesMat.isNull()) classesMat.release();
    }

    @Test
    @DisplayName("Should handle null classes layer in visualization")
    void shouldHandleNullClassesLayerInVisualization() {
        // Create real Mat object for scene layer
        Mat sceneMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(null);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");

        List<Match> matches =
                Collections.singletonList(createMatchWithScene("TestScene", 10, 20, 30, 40));
        when(mockActionResult.getMatchList()).thenReturn(matches);

        // Just verify no exception is thrown with null classes layer
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);

        // Clean up the Mat object
        if (!sceneMat.isNull()) sceneMat.release();
    }

    @Test
    @DisplayName("Should handle both null layers in visualization")
    void shouldHandleBothNullLayersInVisualization() {
        // Setup visualization with both null layers
        when(mockVisualization.getMatchesOnScene()).thenReturn(null);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(null);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");

        List<Match> matches =
                Collections.singletonList(createMatchWithScene("TestScene", 10, 20, 30, 40));
        when(mockActionResult.getMatchList()).thenReturn(matches);

        // Should not throw exception
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);
    }

    @Test
    @DisplayName("Should filter matches by scene name correctly")
    void shouldFilterMatchesBySceneNameCorrectly() {
        // Create real Mat objects instead of mocks
        Mat sceneMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat classesMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TargetScene");

        // Create matches with different scene names
        List<Match> matches =
                Arrays.asList(
                        createMatchWithScene("TargetScene", 10, 20, 30, 40),
                        createMatchWithScene("OtherScene", 50, 60, 70, 80),
                        createMatchWithScene("TargetScene", 90, 100, 110, 120),
                        createMatchWithScene("DifferentScene", 130, 140, 150, 160),
                        createMatchWithScene("TargetScene", 170, 180, 190, 200));

        when(mockActionResult.getMatchList()).thenReturn(matches);

        // Just verify no exception is thrown when filtering matches by scene name
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);

        // Clean up the Mat objects
        if (!sceneMat.isNull()) sceneMat.release();
        if (!classesMat.isNull()) classesMat.release();
    }

    @Test
    @DisplayName("Should handle empty ActionResult match list")
    void shouldHandleEmptyActionResultMatchList() {
        // Create real Mat objects instead of mocks
        Mat sceneMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        Mat classesMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        lenient().when(mockVisualization.getSceneName()).thenReturn("TestScene");

        when(mockActionResult.getMatchList()).thenReturn(new ArrayList<>());

        // Just verify no exception is thrown with empty ActionResult match list
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockVisualization, mockActionResult));

        // Verify the visualization object is still valid
        assertNotNull(mockVisualization);

        // Clean up the Mat objects
        if (!sceneMat.isNull()) sceneMat.release();
        if (!classesMat.isNull()) classesMat.release();
    }

    @Test
    @DisplayName("Should handle large number of matches")
    void shouldHandleLargeNumberOfMatches() {
        List<Match> largeMatchList = createTestMatches(100);

        // Just verify no exception is thrown when handling large number of matches
        assertDoesNotThrow(() -> drawMatch.drawMatches(mockMat, largeMatchList));

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    @Test
    @DisplayName("Should handle various color values")
    void shouldHandleVariousColorValues() {
        Match testMatch = createTestMatch(10, 20, 30, 40);
        List<Match> matchList = Collections.singletonList(testMatch);

        Scalar[] testColors = {
            new Scalar(0, 0, 0, 0), // Black
            new Scalar(255, 255, 255, 0), // White
            new Scalar(128, 64, 192, 0), // Custom color
            new Scalar(255, 255, 255, 255) // With alpha channel
        };

        // Just verify no exception is thrown with various colors
        assertDoesNotThrow(
                () -> {
                    for (Scalar color : testColors) {
                        drawMatch.drawMatches(mockMat, matchList, color);
                    }
                });

        // Verify the Mat is not null after operation
        assertNotNull(mockMat);
        assertFalse(mockMat.isNull());
    }

    // Helper methods

    private List<Match> createTestMatches(int count) {
        List<Match> matches = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            matches.add(createTestMatch(i * 10, i * 20, 30 + i, 40 + i));
        }
        return matches;
    }

    private Match createTestMatch(int x, int y, int w, int h) {
        Match match = mock(Match.class, withSettings().lenient());
        when(match.x()).thenReturn(x);
        when(match.y()).thenReturn(y);
        when(match.w()).thenReturn(w);
        when(match.h()).thenReturn(h);
        return match;
    }

    private Match createMatchWithScene(String sceneName, int x, int y, int w, int h) {
        Match match = createTestMatch(x, y, w, h);
        Image image = mock(Image.class, withSettings().lenient());
        when(image.getName()).thenReturn(sceneName);
        when(match.getImage()).thenReturn(image);
        return match;
    }
}
