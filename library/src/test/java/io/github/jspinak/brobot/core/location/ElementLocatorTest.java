package io.github.jspinak.brobot.core.location;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for ElementLocator interface. Tests element location operations and
 * strategies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ElementLocator Interface Tests")
public class ElementLocatorTest extends BrobotTestBase {

    @Mock private ElementLocator elementLocator;

    @Mock private Pattern mockPattern;

    @Mock private StateImage mockStateImage;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should locate elements with default request")
    void testLocateWithDefaults() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        ElementLocator.Element element = createTestElement(100, 200, 50, 50, 0.9);
        when(elementLocator.locate(request)).thenReturn(Arrays.asList(element));

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(100, results.get(0).getX());
        assertEquals(200, results.get(0).getY());
    }

    @Test
    @DisplayName("Should locate elements with ALL strategy")
    void testLocateWithAllStrategy() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withStrategy(ElementLocator.FindStrategy.ALL)
                        .withSimilarity(0.8)
                        .build();

        List<ElementLocator.Element> elements =
                Arrays.asList(
                        createTestElement(100, 100, 50, 50, 0.95),
                        createTestElement(200, 200, 50, 50, 0.85),
                        createTestElement(300, 300, 50, 50, 0.82));
        when(elementLocator.locate(request)).thenReturn(elements);

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Should locate single best element with BEST strategy")
    void testLocateWithBestStrategy() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withStrategy(ElementLocator.FindStrategy.BEST)
                        .build();

        ElementLocator.Element bestElement = createTestElement(150, 150, 60, 60, 0.98);
        when(elementLocator.locate(request)).thenReturn(Arrays.asList(bestElement));

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(0.98, results.get(0).getConfidence());
    }

    @Test
    @DisplayName("Should locate first element with FIRST strategy")
    void testLocateWithFirstStrategy() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withStrategy(ElementLocator.FindStrategy.FIRST)
                        .build();

        ElementLocator.Element firstElement = createTestElement(50, 50, 40, 40, 0.85);
        when(elementLocator.locate(request)).thenReturn(Arrays.asList(firstElement));

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should locate one per pattern with EACH strategy")
    void testLocateWithEachStrategy() {
        // Arrange
        Pattern pattern1 = mock(Pattern.class);
        Pattern pattern2 = mock(Pattern.class);

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(pattern1, pattern2))
                        .withStrategy(ElementLocator.FindStrategy.EACH)
                        .build();

        List<ElementLocator.Element> elements =
                Arrays.asList(
                        createTestElement(100, 100, 50, 50, 0.9),
                        createTestElement(200, 200, 50, 50, 0.85));
        when(elementLocator.locate(request)).thenReturn(elements);

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Should locate elements within region")
    void testLocateInRegion() {
        // Arrange
        Region searchRegion = new Region(100, 100, 400, 300);
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        ElementLocator.Element element = createTestElement(150, 150, 50, 50, 0.92);
        when(elementLocator.locateInRegion(request, searchRegion))
                .thenReturn(Arrays.asList(element));

        // Act
        List<ElementLocator.Element> results = elementLocator.locateInRegion(request, searchRegion);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(150, results.get(0).getX());
        assertEquals(150, results.get(0).getY());
    }

    @Test
    @DisplayName("Should return empty list when no elements found")
    void testLocateNoResults() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .withSimilarity(0.99)
                        .build();

        when(elementLocator.locate(request)).thenReturn(new ArrayList<>());

        // Act
        List<ElementLocator.Element> results = elementLocator.locate(request);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should verify element still exists")
    void testVerifyElement() {
        // Arrange
        ElementLocator.Element element = createTestElement(200, 300, 60, 40, 0.88);
        when(elementLocator.verifyElement(element)).thenReturn(true);

        // Act
        boolean exists = elementLocator.verifyElement(element);

        // Assert
        assertTrue(exists);
        verify(elementLocator).verifyElement(element);
    }

    @Test
    @DisplayName("Should return false when element no longer exists")
    void testVerifyElementNotFound() {
        // Arrange
        ElementLocator.Element element = createTestElement(200, 300, 60, 40, 0.88);
        when(elementLocator.verifyElement(element)).thenReturn(false);

        // Act
        boolean exists = elementLocator.verifyElement(element);

        // Assert
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should wait for element to appear")
    void testWaitForElement() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        ElementLocator.Element element = createTestElement(100, 100, 50, 50, 0.9);
        when(elementLocator.waitForElement(request, 5.0)).thenReturn(Arrays.asList(element));

        // Act
        List<ElementLocator.Element> results = elementLocator.waitForElement(request, 5.0);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Should timeout when element doesn't appear")
    void testWaitForElementTimeout() {
        // Arrange
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withPatterns(Arrays.asList(mockPattern))
                        .build();

        when(elementLocator.waitForElement(request, 2.0)).thenReturn(new ArrayList<>());

        // Act
        List<ElementLocator.Element> results = elementLocator.waitForElement(request, 2.0);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should wait for element to vanish")
    void testWaitForVanish() {
        // Arrange
        ElementLocator.Element element = createTestElement(200, 200, 50, 50, 0.85);
        when(elementLocator.waitForVanish(element, 5.0)).thenReturn(true);

        // Act
        boolean vanished = elementLocator.waitForVanish(element, 5.0);

        // Assert
        assertTrue(vanished);
        verify(elementLocator).waitForVanish(element, 5.0);
    }

    @Test
    @DisplayName("Should timeout when element doesn't vanish")
    void testWaitForVanishTimeout() {
        // Arrange
        ElementLocator.Element element = createTestElement(200, 200, 50, 50, 0.85);
        when(elementLocator.waitForVanish(element, 2.0)).thenReturn(false);

        // Act
        boolean vanished = elementLocator.waitForVanish(element, 2.0);

        // Assert
        assertFalse(vanished);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Arrange
        when(elementLocator.getImplementationName()).thenReturn("TestLocator");

        // Act
        String name = elementLocator.getImplementationName();

        // Assert
        assertEquals("TestLocator", name);
    }

    @Test
    @DisplayName("Should build request with StateImages")
    void testRequestWithStateImages() {
        // Arrange
        List<StateImage> stateImages = Arrays.asList(mockStateImage);
        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder()
                        .withStateImages(stateImages)
                        .withSimilarity(0.85)
                        .withMaxMatches(5)
                        .build();

        // Assert
        assertNotNull(request.getStateImages());
        assertEquals(1, request.getStateImages().size());
        assertEquals(0.85, request.getSimilarity());
        assertEquals(5, request.getMaxMatches());
    }

    @Test
    @DisplayName("Should build request with search regions")
    void testRequestWithSearchRegions() {
        // Arrange
        List<Region> regions =
                Arrays.asList(new Region(0, 0, 500, 500), new Region(500, 0, 500, 500));

        ElementLocator.LocateRequest request =
                new ElementLocator.LocateRequest.Builder().withSearchRegions(regions).build();

        // Assert
        assertNotNull(request.getSearchRegions());
        assertEquals(2, request.getSearchRegions().size());
    }

    @Test
    @DisplayName("Should get element location and region")
    void testElementLocationAndRegion() {
        // Arrange
        ElementLocator.Element element = createTestElement(100, 200, 60, 40, 0.9);

        // Act
        io.github.jspinak.brobot.model.element.Location location = element.getLocation();
        Region region = element.getRegion();

        // Assert
        assertNotNull(location);
        assertEquals(130, location.getX()); // 100 + 60/2
        assertEquals(220, location.getY()); // 200 + 40/2

        assertNotNull(region);
        assertEquals(100, region.x());
        assertEquals(200, region.y());
        assertEquals(60, region.w());
        assertEquals(40, region.h());
    }

    @Test
    @DisplayName("Should set and get element properties")
    void testElementProperties() {
        // Arrange
        ElementLocator.Element element = new ElementLocator.Element();

        // Act
        element.setX(150);
        element.setY(250);
        element.setWidth(80);
        element.setHeight(60);
        element.setConfidence(0.95);
        element.setName("TestElement");
        element.setSourcePattern(mockPattern);
        element.setSourceStateImage(mockStateImage);

        // Assert
        assertEquals(150, element.getX());
        assertEquals(250, element.getY());
        assertEquals(80, element.getWidth());
        assertEquals(60, element.getHeight());
        assertEquals(0.95, element.getConfidence());
        assertEquals("TestElement", element.getName());
        assertEquals(mockPattern, element.getSourcePattern());
        assertEquals(mockStateImage, element.getSourceStateImage());
    }

    @Test
    @DisplayName("Should use default values in LocateRequest")
    void testLocateRequestDefaults() {
        // Arrange
        ElementLocator.LocateRequest request = new ElementLocator.LocateRequest();

        // Assert
        assertEquals(ElementLocator.FindStrategy.ALL, request.getStrategy());
        assertEquals(0.7, request.getSimilarity());
        assertEquals(Integer.MAX_VALUE, request.getMaxMatches());
    }

    @Test
    @DisplayName("Should mutate LocateRequest through setters")
    void testLocateRequestSetters() {
        // Arrange
        ElementLocator.LocateRequest request = new ElementLocator.LocateRequest();
        List<Pattern> patterns = Arrays.asList(mockPattern);
        List<StateImage> stateImages = Arrays.asList(mockStateImage);
        List<Region> regions = Arrays.asList(new Region(0, 0, 100, 100));

        // Act
        request.setPatterns(patterns);
        request.setStateImages(stateImages);
        request.setSearchRegions(regions);
        request.setStrategy(ElementLocator.FindStrategy.BEST);
        request.setSimilarity(0.9);
        request.setMaxMatches(10);

        // Assert
        assertEquals(patterns, request.getPatterns());
        assertEquals(stateImages, request.getStateImages());
        assertEquals(regions, request.getSearchRegions());
        assertEquals(ElementLocator.FindStrategy.BEST, request.getStrategy());
        assertEquals(0.9, request.getSimilarity());
        assertEquals(10, request.getMaxMatches());
    }

    // Helper method to create test elements
    private ElementLocator.Element createTestElement(
            int x, int y, int w, int h, double confidence) {
        ElementLocator.Element element = new ElementLocator.Element();
        element.setX(x);
        element.setY(y);
        element.setWidth(w);
        element.setHeight(h);
        element.setConfidence(confidence);
        return element;
    }
}
