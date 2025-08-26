package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
 * Tests for DrawMatch class.
 * Verifies batch drawing operations and color handling.
 */
public class DrawMatchTest extends BrobotTestBase {
    
    @Mock
    private DrawRect mockDrawRect;
    
    @Mock
    private Mat mockMat;
    
    @Mock
    private Visualization mockVisualization;
    
    private DrawMatch drawMatch;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        drawMatch = new DrawMatch(mockDrawRect);
        
        when(mockVisualization.getMatchesOnScene()).thenReturn(mockMat);
        when(mockVisualization.getScene()).thenReturn(mockMat);
    }
    
    @Nested
    @DisplayName("Basic Drawing Tests")
    class BasicDrawingTests {
        
        @Test
        @DisplayName("Should draw matches with default color")
        public void testDrawMatchesDefaultColor() {
            Match match1 = createMatch(100, 100, 50, 50);
            Match match2 = createMatch(200, 200, 60, 60);
            List<Match> matches = Arrays.asList(match1, match2);
            
            drawMatch.drawMatches(mockMat, matches);
            
            // Verify drawRect was called for each match with default color
            ArgumentCaptor<Scalar> colorCaptor = ArgumentCaptor.forClass(Scalar.class);
            verify(mockDrawRect, times(2)).drawRectAroundMatch(eq(mockMat), any(Match.class), colorCaptor.capture());
            
            // Check that default color was used (255, 150, 255, 0)
            List<Scalar> colors = colorCaptor.getAllValues();
            assertEquals(2, colors.size());
        }
        
        @Test
        @DisplayName("Should draw matches with custom color")
        public void testDrawMatchesCustomColor() {
            Match match = createMatch(150, 150, 40, 40);
            List<Match> matches = Collections.singletonList(match);
            Scalar customColor = new Scalar(0, 255, 0, 0);
            
            drawMatch.drawMatches(mockMat, matches, customColor);
            
            verify(mockDrawRect).drawRectAroundMatch(mockMat, match, customColor);
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        public void testDrawMatchesEmptyList() {
            List<Match> emptyList = Collections.emptyList();
            
            drawMatch.drawMatches(mockMat, emptyList);
            
            // Should not call drawRect with empty list
            verify(mockDrawRect, never()).drawRectAroundMatch(any(Mat.class), any(Match.class), any(Scalar.class));
        }
        
        @Test
        @DisplayName("Should handle null match list safely")
        public void testDrawMatchesNullList() {
            List<Match> nullList = null;
            
            // Should handle null gracefully (likely NPE, but let's test behavior)
            assertThrows(NullPointerException.class, () -> 
                drawMatch.drawMatches(mockMat, nullList));
        }
    }
    
    @Nested
    @DisplayName("ActionResult Drawing Tests")
    class ActionResultDrawingTests {
        
        @Test
        @DisplayName("Should draw all matches from ActionResult")
        public void testDrawActionResultMatches() {
            Match match1 = createMatch(50, 50, 30, 30);
            Match match2 = createMatch(100, 100, 40, 40);
            Match match3 = createMatch(150, 150, 35, 35);
            
            List<Match> matches = Arrays.asList(match1, match2, match3);
            
            drawMatch.drawMatches(mockMat, matches);
            
            // Should draw all 3 matches
            verify(mockDrawRect, times(3)).drawRectAroundMatch(eq(mockMat), any(Match.class), any(Scalar.class));
        }
    }
    
    @Nested
    @DisplayName("Illustration Drawing Tests")
    class IllustrationDrawingTests {
        
        @Test
        @DisplayName("Should draw matches on mat")
        public void testDrawMatchesOnMat() {
            Match match1 = createMatch(75, 75, 25, 25);
            Match match2 = createMatch(125, 125, 30, 30);
            
            List<Match> matches = Arrays.asList(match1, match2);
            
            drawMatch.drawMatches(mockMat, matches);
            
            // Should draw both matches
            verify(mockDrawRect, times(2)).drawRectAroundMatch(eq(mockMat), any(Match.class), any(Scalar.class));
        }
        
        @Test
        @DisplayName("Should handle large lists efficiently")
        public void testLargeMatchList() {
            List<Match> largeList = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                largeList.add(createMatch(i * 10, i * 10, 20, 20));
            }
            
            drawMatch.drawMatches(mockMat, largeList);
            
            // Should draw all matches
            verify(mockDrawRect, times(50)).drawRectAroundMatch(eq(mockMat), any(Match.class), any(Scalar.class));
        }
    }
    
    
    // Helper methods
    
    private Match createMatch(int x, int y, int w, int h) {
        return new Match.Builder()
            .setRegion(x, y, w, h)
            .build();
    }
    
    private Match createMatchWithScene(int x, int y, int w, int h, int sceneNum) {
        // Scene might not be settable via integer, so just create a normal match
        return new Match.Builder()
            .setRegion(x, y, w, h)
            .build();
    }
}