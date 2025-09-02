package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for DrawMatch functionality.
 * Tests high-level match drawing operations on OpenCV Mat images.
 */
@ExtendWith(MockitoExtension.class)
public class DrawMatchTest extends BrobotTestBase {

    @Mock
    private DrawRect drawRect;
    
    @InjectMocks
    private DrawMatch drawMatch;
    
    @Mock
    private Mat mockMat;
    
    @Mock
    private Visualization mockVisualization;
    
    @Mock
    private ActionResult mockActionResult;
    
    private Scalar defaultColor;
    private Scalar customColor;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        defaultColor = new Scalar(255, 150, 255, 0); // Default pink/purple
        customColor = new Scalar(0, 255, 0, 0); // Green
    }
    
    @Test
    @DisplayName("Should draw matches with default color")
    void shouldDrawMatchesWithDefaultColor() {
        List<Match> matchList = createTestMatches(3);
        
        drawMatch.drawMatches(mockMat, matchList);
        
        // Verify each match is drawn with default color
        verify(drawRect, times(3)).drawRectAroundMatch(
            eq(mockMat), any(Match.class), eq(defaultColor)
        );
    }
    
    @Test
    @DisplayName("Should draw matches with custom color")
    void shouldDrawMatchesWithCustomColor() {
        List<Match> matchList = createTestMatches(2);
        
        drawMatch.drawMatches(mockMat, matchList, customColor);
        
        // Verify each match is drawn with custom color
        verify(drawRect, times(2)).drawRectAroundMatch(
            eq(mockMat), any(Match.class), eq(customColor)
        );
    }
    
    @Test
    @DisplayName("Should handle empty match list")
    void shouldHandleEmptyMatchList() {
        List<Match> emptyList = new ArrayList<>();
        
        drawMatch.drawMatches(mockMat, emptyList);
        
        // Should not call drawRect at all
        verify(drawRect, never()).drawRectAroundMatch(
            any(Mat.class), any(Match.class), any(Scalar.class)
        );
    }
    
    @Test
    @DisplayName("Should draw single match")
    void shouldDrawSingleMatch() {
        Match singleMatch = createTestMatch(100, 200, 50, 60);
        List<Match> matchList = Collections.singletonList(singleMatch);
        
        drawMatch.drawMatches(mockMat, matchList);
        
        verify(drawRect).drawRectAroundMatch(mockMat, singleMatch, defaultColor);
    }
    
    @Test
    @DisplayName("Should draw matches from ActionResult on visualization")
    void shouldDrawMatchesFromActionResultOnVisualization() {
        // Setup visualization
        Mat sceneMat = mock(Mat.class);
        Mat classesMat = mock(Mat.class);
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
        
        drawMatch.drawMatches(mockVisualization, mockActionResult);
        
        // Should only draw matches from "TestScene" (2 matches) on both layers
        verify(drawRect, times(2)).drawRectAroundMatch(
            eq(sceneMat), any(Match.class), eq(defaultColor)
        );
        verify(drawRect, times(2)).drawRectAroundMatch(
            eq(classesMat), any(Match.class), eq(defaultColor)
        );
    }
    
    @Test
    @DisplayName("Should handle null scene layer in visualization")
    void shouldHandleNullSceneLayerInVisualization() {
        // Setup visualization with null scene layer
        Mat classesMat = mock(Mat.class);
        when(mockVisualization.getMatchesOnScene()).thenReturn(null);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");
        
        List<Match> matches = Collections.singletonList(
            createMatchWithScene("TestScene", 10, 20, 30, 40)
        );
        when(mockActionResult.getMatchList()).thenReturn(matches);
        
        drawMatch.drawMatches(mockVisualization, mockActionResult);
        
        // Should only draw on classes layer
        verify(drawRect, never()).drawRectAroundMatch(
            eq((Mat)null), any(Match.class), any(Scalar.class)
        );
        verify(drawRect).drawRectAroundMatch(
            eq(classesMat), any(Match.class), eq(defaultColor)
        );
    }
    
    @Test
    @DisplayName("Should handle null classes layer in visualization")
    void shouldHandleNullClassesLayerInVisualization() {
        // Setup visualization with null classes layer
        Mat sceneMat = mock(Mat.class);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(null);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");
        
        List<Match> matches = Collections.singletonList(
            createMatchWithScene("TestScene", 10, 20, 30, 40)
        );
        when(mockActionResult.getMatchList()).thenReturn(matches);
        
        drawMatch.drawMatches(mockVisualization, mockActionResult);
        
        // Should only draw on scene layer
        verify(drawRect).drawRectAroundMatch(
            eq(sceneMat), any(Match.class), eq(defaultColor)
        );
        verify(drawRect, never()).drawRectAroundMatch(
            eq((Mat)null), any(Match.class), any(Scalar.class)
        );
    }
    
    @Test
    @DisplayName("Should handle both null layers in visualization")
    void shouldHandleBothNullLayersInVisualization() {
        // Setup visualization with both null layers
        when(mockVisualization.getMatchesOnScene()).thenReturn(null);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(null);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");
        
        List<Match> matches = Collections.singletonList(
            createMatchWithScene("TestScene", 10, 20, 30, 40)
        );
        when(mockActionResult.getMatchList()).thenReturn(matches);
        
        // Should not throw exception
        assertDoesNotThrow(() -> 
            drawMatch.drawMatches(mockVisualization, mockActionResult)
        );
        
        // Should not draw anything
        verify(drawRect, never()).drawRectAroundMatch(
            any(Mat.class), any(Match.class), any(Scalar.class)
        );
    }
    
    @Test
    @DisplayName("Should filter matches by scene name correctly")
    void shouldFilterMatchesBySceneNameCorrectly() {
        Mat sceneMat = mock(Mat.class);
        Mat classesMat = mock(Mat.class);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TargetScene");
        
        // Create matches with different scene names
        List<Match> matches = Arrays.asList(
            createMatchWithScene("TargetScene", 10, 20, 30, 40),
            createMatchWithScene("OtherScene", 50, 60, 70, 80),
            createMatchWithScene("TargetScene", 90, 100, 110, 120),
            createMatchWithScene("DifferentScene", 130, 140, 150, 160),
            createMatchWithScene("TargetScene", 170, 180, 190, 200)
        );
        
        when(mockActionResult.getMatchList()).thenReturn(matches);
        
        drawMatch.drawMatches(mockVisualization, mockActionResult);
        
        // Should draw only 3 matches from "TargetScene" on each layer
        verify(drawRect, times(3)).drawRectAroundMatch(
            eq(sceneMat), any(Match.class), eq(defaultColor)
        );
        verify(drawRect, times(3)).drawRectAroundMatch(
            eq(classesMat), any(Match.class), eq(defaultColor)
        );
    }
    
    @Test
    @DisplayName("Should handle empty ActionResult match list")
    void shouldHandleEmptyActionResultMatchList() {
        Mat sceneMat = mock(Mat.class);
        Mat classesMat = mock(Mat.class);
        when(mockVisualization.getMatchesOnScene()).thenReturn(sceneMat);
        when(mockVisualization.getMatchesOnClasses()).thenReturn(classesMat);
        when(mockVisualization.getSceneName()).thenReturn("TestScene");
        
        when(mockActionResult.getMatchList()).thenReturn(new ArrayList<>());
        
        drawMatch.drawMatches(mockVisualization, mockActionResult);
        
        // Should not draw anything
        verify(drawRect, never()).drawRectAroundMatch(
            any(Mat.class), any(Match.class), any(Scalar.class)
        );
    }
    
    @Test
    @DisplayName("Should handle large number of matches")
    void shouldHandleLargeNumberOfMatches() {
        List<Match> largeMatchList = createTestMatches(100);
        
        drawMatch.drawMatches(mockMat, largeMatchList);
        
        // Should draw all 100 matches
        verify(drawRect, times(100)).drawRectAroundMatch(
            eq(mockMat), any(Match.class), eq(defaultColor)
        );
    }
    
    @Test
    @DisplayName("Should handle various color values")
    void shouldHandleVariousColorValues() {
        Match testMatch = createTestMatch(10, 20, 30, 40);
        List<Match> matchList = Collections.singletonList(testMatch);
        
        Scalar[] testColors = {
            new Scalar(0, 0, 0, 0),       // Black
            new Scalar(255, 255, 255, 0), // White
            new Scalar(128, 64, 192, 0),  // Custom color
            new Scalar(255, 255, 255, 255) // With alpha channel
        };
        
        for (Scalar color : testColors) {
            drawMatch.drawMatches(mockMat, matchList, color);
        }
        
        // Verify each color was used
        for (Scalar color : testColors) {
            verify(drawRect).drawRectAroundMatch(mockMat, testMatch, color);
        }
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
        Match match = mock(Match.class);
        when(match.x()).thenReturn(x);
        when(match.y()).thenReturn(y);
        when(match.w()).thenReturn(w);
        when(match.h()).thenReturn(h);
        return match;
    }
    
    private Match createMatchWithScene(String sceneName, int x, int y, int w, int h) {
        Match match = createTestMatch(x, y, w, h);
        Image image = mock(Image.class);
        when(image.getName()).thenReturn(sceneName);
        when(match.getImage()).thenReturn(image);
        return match;
    }
}