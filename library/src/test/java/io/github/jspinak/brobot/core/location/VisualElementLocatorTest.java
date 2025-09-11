package io.github.jspinak.brobot.core.location;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.core.services.PatternMatcher;
import io.github.jspinak.brobot.core.services.ScreenCaptureService;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for VisualElementLocator implementation. Tests the visual pattern matching
 * implementation of element location.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisualElementLocator Implementation Tests")
public class VisualElementLocatorTest extends BrobotTestBase {

    private VisualElementLocator visualLocator;

    @Mock private PatternMatcher patternMatcher;

    @Mock private ScreenCaptureService screenCapture;

    @Mock private BufferedImage mockScreen;

    @Mock private BufferedImage mockRegionImage;

    @Mock private Pattern mockPattern;

    @Mock private StateImage mockStateImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        visualLocator = new VisualElementLocator(patternMatcher, screenCapture);
    }

    @Test
    @DisplayName("Should locate elements on full screen")
    void testLocateFullScreen() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);
        when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(mockPattern));

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withStateImages(Arrays.asList(mockStateImage))
                        .withSimilarity(0.8)
                        .withStrategy(ElementLocator.FindStrategy.ALL)
                        .build();

        PatternMatcher.MatchResult matchResult =
                new PatternMatcher.MatchResult(100, 200, 50, 50, 0.95);

        when(patternMatcher.findPatterns(eq(mockScreen), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(matchResult));

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100, results.get(0).getX());
        assertEquals(200, results.get(0).getY());
        assertEquals(0.95, results.get(0).getConfidence());
        verify(screenCapture).captureScreen();
    }

    @Test
    @DisplayName("Should handle screen capture failure")
    void testLocateScreenCaptureFailure() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(null);

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(screenCapture).captureScreen();
        verifyNoInteractions(patternMatcher);
    }

    @Test
    @DisplayName("Should locate elements in specific region")
    void testLocateInRegion() {
        // Arrange
        Region searchRegion = new Region(100, 100, 400, 300);
        when(screenCapture.captureRegion(searchRegion)).thenReturn(mockRegionImage);
        when(mockPattern.getName()).thenReturn("TestPattern");

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withSimilarity(0.75)
                        .build();

        PatternMatcher.MatchResult matchResult =
                new PatternMatcher.MatchResult(50, 60, 40, 40, 0.88);

        when(patternMatcher.findPatterns(eq(mockRegionImage), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(matchResult));

        // Act
        List<ElementLocator.Element> results = visualLocator.locateInRegion(request, searchRegion);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        // Coordinates should be adjusted to screen space
        assertEquals(150, results.get(0).getX()); // 50 + 100
        assertEquals(160, results.get(0).getY()); // 60 + 100
        assertEquals("TestPattern", results.get(0).getName());
        verify(screenCapture).captureRegion(searchRegion);
    }

    @Test
    @DisplayName("Should handle null request")
    void testLocateNullRequest() {
        // Act
        List<ElementLocator.Element> results = visualLocator.locate(null);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(screenCapture);
    }

    @Test
    @DisplayName("Should handle null region")
    void testLocateInNullRegion() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        // Act
        List<ElementLocator.Element> results = visualLocator.locateInRegion(request, null);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(screenCapture);
    }

    @Test
    @DisplayName("Should handle empty pattern list")
    void testLocateEmptyPatterns() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder().build(); // No patterns or StateImages

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(screenCapture).captureScreen();
        verifyNoInteractions(patternMatcher);
    }

    @Test
    @DisplayName("Should apply BEST strategy")
    void testApplyBestStrategy() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withStrategy(ElementLocator.FindStrategy.BEST)
                        .build();

        List<PatternMatcher.MatchResult> matchResults =
                Arrays.asList(
                        new PatternMatcher.MatchResult(100, 100, 50, 50, 0.85),
                        new PatternMatcher.MatchResult(200, 200, 50, 50, 0.95), // Best
                        new PatternMatcher.MatchResult(300, 300, 50, 50, 0.75));

        when(patternMatcher.findPatterns(eq(mockScreen), eq(mockPattern), any()))
                .thenReturn(matchResults);

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(0.95, results.get(0).getConfidence());
        assertEquals(200, results.get(0).getX());
    }

    @Test
    @DisplayName("Should apply FIRST strategy")
    void testApplyFirstStrategy() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withStrategy(ElementLocator.FindStrategy.FIRST)
                        .withSimilarity(0.7)
                        .build();

        List<PatternMatcher.MatchResult> matchResults =
                Arrays.asList(
                        new PatternMatcher.MatchResult(100, 100, 50, 50, 0.85),
                        new PatternMatcher.MatchResult(200, 200, 50, 50, 0.95));

        when(patternMatcher.findPatterns(eq(mockScreen), eq(mockPattern), any()))
                .thenReturn(matchResults);

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100, results.get(0).getX());
    }

    @Test
    @DisplayName("Should apply EACH strategy")
    void testApplyEachStrategy() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);

        Pattern pattern1 = mock(Pattern.class);
        Pattern pattern2 = mock(Pattern.class);
        when(pattern1.getName()).thenReturn("Pattern1");
        when(pattern2.getName()).thenReturn("Pattern2");

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(pattern1, pattern2))
                        .withStrategy(ElementLocator.FindStrategy.EACH)
                        .build();

        // Multiple matches for pattern1
        when(patternMatcher.findPatterns(eq(mockScreen), eq(pattern1), any()))
                .thenReturn(
                        Arrays.asList(
                                new PatternMatcher.MatchResult(100, 100, 50, 50, 0.9),
                                new PatternMatcher.MatchResult(150, 150, 50, 50, 0.85)));

        // Multiple matches for pattern2
        when(patternMatcher.findPatterns(eq(mockScreen), eq(pattern2), any()))
                .thenReturn(
                        Arrays.asList(
                                new PatternMatcher.MatchResult(200, 200, 50, 50, 0.88),
                                new PatternMatcher.MatchResult(250, 250, 50, 50, 0.82)));

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // One per pattern
        assertEquals("Pattern1", results.get(0).getName());
        assertEquals("Pattern2", results.get(1).getName());
    }

    @Test
    @DisplayName("Should verify element exists")
    void testVerifyElement() {
        // Arrange
        ElementLocator.Element element = new ElementLocator.Element();
        element.setX(100);
        element.setY(200);
        element.setWidth(50);
        element.setHeight(60);
        element.setConfidence(0.9);
        element.setSourcePattern(mockPattern);

        Region elementRegion = element.getRegion();
        when(screenCapture.captureRegion(elementRegion)).thenReturn(mockRegionImage);

        PatternMatcher.MatchResult matchResult = new PatternMatcher.MatchResult(0, 0, 50, 60, 0.85);

        when(patternMatcher.findPatterns(eq(mockRegionImage), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(matchResult));

        // Act
        boolean exists = visualLocator.verifyElement(element);

        // Assert
        assertTrue(exists);
        verify(screenCapture).captureRegion(elementRegion);
    }

    @Test
    @DisplayName("Should return false when element not verified")
    void testVerifyElementNotFound() {
        // Arrange
        ElementLocator.Element element = new ElementLocator.Element();
        element.setX(100);
        element.setY(200);
        element.setWidth(50);
        element.setHeight(60);
        element.setSourcePattern(mockPattern);

        Region elementRegion = element.getRegion();
        when(screenCapture.captureRegion(elementRegion)).thenReturn(mockRegionImage);

        when(patternMatcher.findPatterns(eq(mockRegionImage), eq(mockPattern), any()))
                .thenReturn(new ArrayList<>());

        // Act
        boolean exists = visualLocator.verifyElement(element);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should handle verify with null element")
    void testVerifyNullElement() {
        // Act
        boolean exists = visualLocator.verifyElement(null);

        // Assert
        assertFalse(exists);
        verifyNoInteractions(screenCapture);
    }

    @Test
    @DisplayName("Should wait for element to appear")
    void testWaitForElement() throws InterruptedException {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        when(screenCapture.captureScreen())
                .thenReturn(null) // First attempt - not found
                .thenReturn(null) // Second attempt - not found
                .thenReturn(mockScreen); // Third attempt - found

        PatternMatcher.MatchResult matchResult =
                new PatternMatcher.MatchResult(100, 100, 50, 50, 0.9);

        when(patternMatcher.findPatterns(eq(mockScreen), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(matchResult));

        // Act
        List<ElementLocator.Element> results = visualLocator.waitForElement(request, 1.0);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(screenCapture, atLeast(3)).captureScreen();
    }

    @Test
    @DisplayName("Should timeout waiting for element")
    void testWaitForElementTimeout() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        when(screenCapture.captureScreen()).thenReturn(mockScreen);
        when(patternMatcher.findPatterns(any(), any(), any())).thenReturn(new ArrayList<>());

        // Act
        List<ElementLocator.Element> results = visualLocator.waitForElement(request, 0.5);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should wait for element to vanish")
    void testWaitForVanish() {
        // Arrange
        ElementLocator.Element element = new ElementLocator.Element();
        element.setX(100);
        element.setY(200);
        element.setWidth(50);
        element.setHeight(60);
        element.setSourcePattern(mockPattern);

        Region elementRegion = element.getRegion();
        when(screenCapture.captureRegion(elementRegion))
                .thenReturn(mockRegionImage) // First check - still exists
                .thenReturn(mockRegionImage) // Second check - still exists
                .thenReturn(mockRegionImage); // Third check - gone

        when(patternMatcher.findPatterns(eq(mockRegionImage), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(new PatternMatcher.MatchResult(0, 0, 50, 60, 0.9)))
                .thenReturn(Arrays.asList(new PatternMatcher.MatchResult(0, 0, 50, 60, 0.9)))
                .thenReturn(new ArrayList<>()); // Element vanished

        // Act
        boolean vanished = visualLocator.waitForVanish(element, 1.0);

        // Assert
        assertTrue(vanished);
        verify(screenCapture, atLeast(3)).captureRegion(elementRegion);
    }

    @Test
    @DisplayName("Should timeout waiting for vanish")
    void testWaitForVanishTimeout() {
        // Arrange
        ElementLocator.Element element = new ElementLocator.Element();
        element.setX(100);
        element.setY(200);
        element.setWidth(50);
        element.setHeight(60);
        element.setSourcePattern(mockPattern);

        Region elementRegion = element.getRegion();
        when(screenCapture.captureRegion(elementRegion)).thenReturn(mockRegionImage);

        // Element always found
        when(patternMatcher.findPatterns(eq(mockRegionImage), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(new PatternMatcher.MatchResult(0, 0, 50, 60, 0.9)));

        // Act
        boolean vanished = visualLocator.waitForVanish(element, 0.5);

        // Assert
        assertFalse(vanished);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Act
        String name = visualLocator.getImplementationName();

        // Assert
        assertEquals("Visual", name);
    }

    @Test
    @DisplayName("Should extract patterns from StateImages")
    void testExtractPatternsFromStateImages() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);

        Pattern pattern1 = mock(Pattern.class);
        Pattern pattern2 = mock(Pattern.class);
        when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(pattern1, pattern2));

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withStateImages(Arrays.asList(mockStateImage))
                        .build();

        when(patternMatcher.findPatterns(eq(mockScreen), any(Pattern.class), any()))
                .thenReturn(Arrays.asList(new PatternMatcher.MatchResult(100, 100, 50, 50, 0.9)));

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size()); // One result per pattern
        verify(patternMatcher).findPatterns(eq(mockScreen), eq(pattern1), any());
        verify(patternMatcher).findPatterns(eq(mockScreen), eq(pattern2), any());
    }

    @Test
    @DisplayName("Should set source StateImage on found elements")
    void testSetSourceStateImage() {
        // Arrange
        when(screenCapture.captureScreen()).thenReturn(mockScreen);
        when(mockStateImage.getPatterns()).thenReturn(Arrays.asList(mockPattern));

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withStateImages(Arrays.asList(mockStateImage))
                        .build();

        when(patternMatcher.findPatterns(eq(mockScreen), eq(mockPattern), any()))
                .thenReturn(Arrays.asList(new PatternMatcher.MatchResult(100, 100, 50, 50, 0.9)));

        // Act
        List<ElementLocator.Element> results = visualLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(mockStateImage, results.get(0).getSourceStateImage());
        assertEquals(mockPattern, results.get(0).getSourcePattern());
    }
}
