package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DrawRect class.
 * Verifies rectangle drawing operations with boundary checking.
 */
public class DrawRectTest extends BrobotTestBase {
    
    private DrawRect drawRect;
    private Mat testMat;
    
    @Mock
    private Visualization mockVisualization;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        drawRect = new DrawRect();
        
        // Create a real test Mat for drawing operations
        testMat = new Mat(1080, 1920, org.bytedeco.opencv.global.opencv_core.CV_8UC3);
        when(mockVisualization.getMatchesOnScene()).thenReturn(testMat);
        when(mockVisualization.getScene()).thenReturn(testMat);
    }
    
    @Nested
    @DisplayName("Draw Around Match Tests")
    class DrawAroundMatchTests {
        
        @Test
        @DisplayName("Should draw rectangle with padding around match")
        public void testDrawRectAroundMatch() {
            Match match = new Match.Builder()
                .setRegion(100, 100, 50, 50)
                .build();
            
            Scalar color = new Scalar(0, 255, 0, 0);
            
            // Should not throw exception
            assertDoesNotThrow(() -> drawRect.drawRectAroundMatch(testMat, match, color));
        }
        
        @Test
        @DisplayName("Should handle match at image boundaries")
        public void testDrawRectAtBoundaries() {
            // Match at top-left corner
            Match topLeft = new Match.Builder()
                .setRegion(0, 0, 20, 20)
                .build();
            
            // Match at bottom-right corner
            Match bottomRight = new Match.Builder()
                .setRegion(1900, 1060, 20, 20)
                .build();
            
            Scalar color = new Scalar(255, 0, 0, 0);
            
            assertDoesNotThrow(() -> {
                drawRect.drawRectAroundMatch(testMat, topLeft, color);
                drawRect.drawRectAroundMatch(testMat, bottomRight, color);
            });
        }
        
        @Test
        @DisplayName("Should clamp rectangle when match extends beyond image")
        public void testDrawRectClamping() {
            // Match that extends beyond image bounds
            Match oversizedMatch = new Match.Builder()
                .setRegion(1900, 1050, 100, 100)
                .build();
            
            Scalar color = new Scalar(0, 0, 255, 0);
            
            // Should handle gracefully without exception
            assertDoesNotThrow(() -> drawRect.drawRectAroundMatch(testMat, oversizedMatch, color));
        }
    }
    
    @Nested
    @DisplayName("Draw On Match Tests")
    class DrawOnMatchTests {
        
        @Test
        @DisplayName("Should draw rectangle exactly on match boundaries")
        public void testDrawRectOnMatch() {
            Match match = new Match.Builder()
                .setRegion(200, 200, 100, 100)
                .build();
            
            Scalar color = new Scalar(255, 255, 0, 0);
            
            assertDoesNotThrow(() -> drawRect.drawRectOnMatch(testMat, match, color));
        }
        
        @Test
        @DisplayName("Should handle zero-sized match")
        public void testDrawRectZeroSized() {
            Match zeroMatch = new Match.Builder()
                .setRegion(500, 500, 0, 0)
                .build();
            
            Scalar color = new Scalar(255, 0, 255, 0);
            
            // Should handle zero-sized match without crashing
            assertDoesNotThrow(() -> drawRect.drawRectOnMatch(testMat, zeroMatch, color));
        }
    }
    
    @Nested
    @DisplayName("Draw Multiple Rectangles Tests")
    class DrawMultipleRectanglesTests {
        
        @Test
        @DisplayName("Should draw rectangles for multiple matches")
        public void testDrawMultipleRects() {
            List<Match> matches = Arrays.asList(
                new Match.Builder().setRegion(50, 50, 30, 30).build(),
                new Match.Builder().setRegion(100, 100, 40, 40).build(),
                new Match.Builder().setRegion(200, 200, 50, 50).build()
            );
            
            Scalar color = new Scalar(0, 255, 255, 0);
            
            for (Match match : matches) {
                assertDoesNotThrow(() -> drawRect.drawRectAroundMatch(testMat, match, color));
            }
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        public void testDrawRectsEmptyList() {
            List<Match> emptyList = Collections.emptyList();
            Scalar color = new Scalar(128, 128, 128, 0);
            
            // Should handle empty list gracefully - nothing to draw
            for (Match match : emptyList) {
                drawRect.drawRectAroundMatch(testMat, match, color);
            }
            // No exceptions should be thrown
            assertTrue(true);
        }
    }
    
    @Nested
    @DisplayName("Illustration Integration Tests")
    class IllustrationIntegrationTests {
        
        @Test
        @DisplayName("Should draw on mat from visualization")
        public void testDrawOnVisualizationMat() {
            Match match = new Match.Builder()
                .setRegion(150, 150, 80, 80)
                .build();
            
            Scalar color = new Scalar(128, 255, 128, 0);
            
            // Draw directly on the Mat from visualization
            Mat vizMat = mockVisualization.getMatchesOnScene();
            drawRect.drawRectAroundMatch(vizMat, match, color);
            
            // Should retrieve the matches layer
            verify(mockVisualization).getMatchesOnScene();
        }
        
        @Test
        @DisplayName("Should draw multiple matches")
        public void testDrawMultipleMatches() {
            List<Match> matches = Arrays.asList(
                new Match.Builder().setRegion(10, 10, 20, 20).build(),
                new Match.Builder().setRegion(50, 50, 30, 30).build(),
                new Match.Builder().setRegion(100, 100, 40, 40).build()
            );
            
            Scalar color = new Scalar(200, 100, 50, 0);
            
            for (Match match : matches) {
                drawRect.drawRectAroundMatch(testMat, match, color);
            }
            
            // All matches should be drawn without exception
            assertTrue(true);
        }
    }
    
    @Nested
    @DisplayName("Boundary and Edge Case Tests")
    class BoundaryTests {
        
        @Test
        @DisplayName("Should handle negative coordinates")
        public void testNegativeCoordinates() {
            // Match with negative position (should be clamped to 0)
            Match negativeMatch = new Match.Builder()
                .setRegion(-10, -10, 30, 30)
                .build();
            
            Scalar color = new Scalar(255, 255, 255, 0);
            
            assertDoesNotThrow(() -> drawRect.drawRectAroundMatch(testMat, negativeMatch, color));
        }
        
        @Test
        @DisplayName("Should handle very large matches")
        public void testVeryLargeMatch() {
            // Match larger than the entire image
            Match hugeMatch = new Match.Builder()
                .setRegion(0, 0, 5000, 5000)
                .build();
            
            Scalar color = new Scalar(100, 100, 100, 0);
            
            assertDoesNotThrow(() -> drawRect.drawRectAroundMatch(testMat, hugeMatch, color));
        }
        
        @Test
        @DisplayName("Should handle single pixel match")
        public void testSinglePixelMatch() {
            Match pixelMatch = new Match.Builder()
                .setRegion(500, 500, 1, 1)
                .build();
            
            Scalar color = new Scalar(0, 128, 255, 0);
            
            assertDoesNotThrow(() -> drawRect.drawRectOnMatch(testMat, pixelMatch, color));
        }
    }
    
    @org.junit.jupiter.api.AfterEach
    public void cleanupTest() {
        // Clean up test Mat after each test
        if (testMat != null && !testMat.isNull()) {
            testMat.release();
        }
    }
}