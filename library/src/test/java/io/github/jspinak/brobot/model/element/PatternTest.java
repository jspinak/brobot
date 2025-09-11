package io.github.jspinak.brobot.model.element;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Stream;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the Pattern class which represents a visual template for pattern matching
 * in the Brobot GUI automation framework.
 */
@DisplayName("Pattern Model Tests")
public class PatternTest extends BrobotTestBase {

    private BufferedImage testBufferedImage;
    private Image testImage;
    private Region testRegion;
    private Match testMatch;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testBufferedImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        testImage = new Image(testBufferedImage, "TestImage");
        testRegion = new Region(10, 20, 100, 50);

        testMatch = new Match();
        testMatch.setRegion(testRegion);
        testMatch.setName("TestMatch");
        testMatch.setImage(testImage);
    }

    @Test
    @DisplayName("Should create pattern with default constructor")
    void testDefaultConstructor() {
        // When
        Pattern pattern = new Pattern();

        // Then
        assertNull(pattern.getUrl());
        assertNull(pattern.getImgpath());
        assertNull(pattern.getName());
        assertFalse(pattern.isFixed());
        assertNotNull(pattern.getSearchRegions());
        assertFalse(pattern.isSetKmeansColorProfiles());
        assertNotNull(pattern.getMatchHistory());
        assertEquals(0, pattern.getIndex());
        assertFalse(pattern.isDynamic());
        assertNotNull(pattern.getTargetPosition());
        assertEquals(0.5, pattern.getTargetPosition().getPercentW());
        assertEquals(0.5, pattern.getTargetPosition().getPercentH());
        assertNotNull(pattern.getTargetOffset());
        assertEquals(0, pattern.getTargetOffset().getX());
        assertEquals(0, pattern.getTargetOffset().getY());
        assertNotNull(pattern.getAnchors());
        assertNull(pattern.getImage());
    }

    @Test
    @DisplayName("Should create pattern from BufferedImage")
    void testConstructorWithBufferedImage() {
        // When
        Pattern pattern = new Pattern(testBufferedImage);

        // Then
        assertNotNull(pattern.getImage());
        assertEquals(100, pattern.w());
        assertEquals(50, pattern.h());
    }

    @Test
    @DisplayName("Should create pattern from Image")
    void testConstructorWithImage() {
        // When
        Pattern pattern = new Pattern(testImage);

        // Then
        assertSame(testImage, pattern.getImage());
        assertEquals("TestImage", pattern.getName());
    }

    @Test
    @DisplayName("Should create pattern from Match")
    void testConstructorWithMatch() {
        // When
        Pattern pattern = new Pattern(testMatch);

        // Then
        assertTrue(pattern.isFixed());
        assertNotNull(pattern.getSearchRegions());
        assertEquals(testImage, pattern.getImage());
        assertEquals("TestMatch", pattern.getName());

        // Verify the search region was set
        Region fixedRegion = pattern.getRegion();
        assertEquals(testRegion, fixedRegion);
    }

    @Test
    @DisplayName("Should create pattern from Match without image")
    void testConstructorWithMatchNoImage() {
        // Given
        Match matchNoImage = new Match();
        matchNoImage.setName("NoImageMatch");
        matchNoImage.setRegion(testRegion);

        // When
        Pattern pattern = new Pattern(matchNoImage);

        // Then
        assertTrue(pattern.isFixed());
        assertEquals("NoImageMatch", pattern.getImgpath());
        assertNull(pattern.getImage());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("Should handle null and empty image path in constructor")
    void testConstructorWithInvalidPath(String path) {
        // When
        Pattern pattern = new Pattern(path);

        // Then
        assertNull(pattern.getImage());
    }

    @Test
    @DisplayName("Should get width and height correctly")
    void testWidthAndHeight() {
        // Given
        Pattern pattern = new Pattern(testImage);

        // Then
        assertEquals(100, pattern.w());
        assertEquals(50, pattern.h());
        assertEquals(5000, pattern.size());
    }

    @Test
    @DisplayName("Should handle null image for dimensions")
    void testWidthAndHeightWithNullImage() {
        // Given
        Pattern pattern = new Pattern();

        // Then
        assertEquals(0, pattern.w());
        assertEquals(0, pattern.h());
        assertEquals(0, pattern.size());
    }

    @Test
    @DisplayName("Should manage search regions correctly")
    void testSearchRegionManagement() {
        // Given
        Pattern pattern = new Pattern();
        Region region1 = new Region(0, 0, 50, 50);
        Region region2 = new Region(50, 50, 50, 50);

        // When
        pattern.addSearchRegion(region1);
        pattern.addSearchRegion(region2);

        // Then
        List<Region> regions = pattern.getRegions();
        assertTrue(regions.contains(region1) || regions.contains(region2));
    }

    @Test
    @DisplayName("Should set search regions directly")
    void testSetSearchRegions() {
        // Given
        Pattern pattern = new Pattern();
        Region region1 = new Region(10, 10, 30, 30);
        Region region2 = new Region(40, 40, 30, 30);

        // When
        pattern.setSearchRegionsTo(region1, region2);

        // Then
        List<Region> regions = pattern.getRegions();
        assertEquals(2, regions.size());
    }

    @Test
    @DisplayName("Should get regions for search with default when empty")
    void testGetRegionsForSearchDefault() {
        // Given
        Pattern pattern = new Pattern();

        // When
        List<Region> regions = pattern.getRegionsForSearch();

        // Then
        assertEquals(1, regions.size());
        Region defaultRegion = regions.get(0);
        // Default region should be full screen (0, 0, 0, 0 or similar)
        assertNotNull(defaultRegion);
    }

    @Test
    @DisplayName("Should handle fixed pattern behavior")
    void testFixedPattern() {
        // Given
        Pattern pattern = new Pattern();
        Region fixedRegion = new Region(100, 100, 50, 50);

        // When
        pattern.setFixed(true);
        pattern.getSearchRegions().setFixedRegion(fixedRegion);

        // Then
        assertTrue(pattern.isFixed());
        Region retrievedRegion = pattern.getRegion();
        assertEquals(fixedRegion, retrievedRegion);
    }

    @Test
    @DisplayName("Should reset fixed search region")
    void testResetFixedSearchRegion() {
        // Given
        Pattern pattern = new Pattern();
        pattern.setFixed(true);
        pattern.getSearchRegions().setFixedRegion(new Region(10, 10, 10, 10));

        // When
        pattern.resetFixedSearchRegion();

        // Then
        // Fixed region should be reset
        assertTrue(
                pattern.getSearchRegions().getRegions(true).isEmpty()
                        || pattern.getSearchRegions().getFixedRegion() == null);
    }

    @Test
    @DisplayName("Should add match snapshots")
    void testAddMatchSnapshot() {
        // Given
        Pattern pattern = new Pattern();
        ActionRecord snapshot = new ActionRecord();
        snapshot.setActionSuccess(true);

        // When
        pattern.addMatchSnapshot(snapshot);

        // Then
        ActionHistory history = pattern.getMatchHistory();
        assertNotNull(history);
        assertTrue(history.getSnapshots().contains(snapshot));
    }

    @Test
    @DisplayName("Should add match snapshot from coordinates")
    void testAddMatchSnapshotFromCoordinates() {
        // Given
        Pattern pattern = new Pattern();

        // When
        pattern.addMatchSnapshot(10, 20, 30, 40);

        // Then
        ActionHistory history = pattern.getMatchHistory();
        assertEquals(1, history.getSnapshots().size());
    }

    @Test
    @DisplayName("Should create StateImage in null state")
    void testInNullState() {
        // Given
        Pattern pattern = new Pattern(testImage);
        pattern.setName("TestPattern");

        // When
        StateImage stateImage = pattern.inNullState();

        // Then
        assertNotNull(stateImage);
        assertEquals("TestPattern", stateImage.getName());
        assertEquals("null", stateImage.getOwnerStateName());
        assertTrue(stateImage.getPatterns().contains(pattern));
    }

    @Test
    @DisplayName("Should check if pattern is defined")
    void testIsDefined() {
        // Given
        Pattern pattern = new Pattern();

        // When not defined
        assertFalse(pattern.isDefined());

        // When defined
        pattern.addSearchRegion(new Region(10, 10, 10, 10));
        assertTrue(pattern.isDefined());
    }

    @Test
    @DisplayName("Should check if pattern is empty")
    void testIsEmpty() {
        // Given
        Pattern emptyPattern = new Pattern();
        Pattern nonEmptyPattern = new Pattern(testImage);

        // Then
        assertTrue(emptyPattern.isEmpty());
        assertFalse(nonEmptyPattern.isEmpty());
    }

    @TestFactory
    @DisplayName("Pattern properties tests")
    Stream<DynamicTest> testPatternProperties() {
        return Stream.of(
                dynamicTest(
                        "Should set and get URL",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setUrl("http://example.com/image.png");
                            assertEquals("http://example.com/image.png", pattern.getUrl());
                        }),
                dynamicTest(
                        "Should set and get image path",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setImgpath("/path/to/image.png");
                            assertEquals("/path/to/image.png", pattern.getImgpath());
                        }),
                dynamicTest(
                        "Should set and get name",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setName("PatternName");
                            assertEquals("PatternName", pattern.getName());
                        }),
                dynamicTest(
                        "Should set and get dynamic flag",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setDynamic(true);
                            assertTrue(pattern.isDynamic());
                        }),
                dynamicTest(
                        "Should set and get index",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setIndex(42);
                            assertEquals(42, pattern.getIndex());
                        }),
                dynamicTest(
                        "Should set and get kmeans color profiles flag",
                        () -> {
                            Pattern pattern = new Pattern();
                            pattern.setSetKmeansColorProfiles(true);
                            assertTrue(pattern.isSetKmeansColorProfiles());
                        }));
    }

    @Test
    @DisplayName("Should manage target position and offset")
    void testTargetPositionAndOffset() {
        // Given
        Pattern pattern = new Pattern();
        Position newPosition = new Position(25, 75);
        Location newOffset = new Location(10, -10);

        // When
        pattern.setTargetPosition(newPosition);
        pattern.setTargetOffset(newOffset);

        // Then
        assertEquals(0.25, pattern.getTargetPosition().getPercentW());
        assertEquals(0.75, pattern.getTargetPosition().getPercentH());
        assertEquals(10, pattern.getTargetOffset().getX());
        assertEquals(-10, pattern.getTargetOffset().getY());
    }

    @Test
    @DisplayName("Should manage anchors")
    void testAnchors() {
        // Given
        Pattern pattern = new Pattern();
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0, 0));
        Anchor anchor2 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(100, 100));

        // When
        pattern.getAnchors().add(anchor1);
        pattern.getAnchors().add(anchor2);

        // Then
        assertEquals(2, pattern.getAnchors().size());
        assertTrue(pattern.getAnchors().getAnchorList().contains(anchor1));
        assertTrue(pattern.getAnchors().getAnchorList().contains(anchor2));
    }

    @ParameterizedTest
    @CsvSource({"true,true", "false,false"})
    @DisplayName("Should handle fixed pattern state correctly")
    void testFixedPatternState(boolean isFixed, boolean expected) {
        // Given
        Pattern pattern = new Pattern();

        // When
        pattern.setFixed(isFixed);

        // Then
        assertEquals(expected, pattern.isFixed());
    }

    @Test
    @DisplayName("Should handle Mat conversion")
    void testMatConversion() {
        // Given
        Pattern pattern = new Pattern(testImage);

        // When
        Mat bgrMat = pattern.getMat();
        Mat hsvMat = pattern.getMatHSV();

        // Then
        assertNotNull(bgrMat);
        assertNotNull(hsvMat);
    }

    @Test
    @DisplayName("Should handle equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        Pattern pattern1 = new Pattern(testImage);
        pattern1.setName("Pattern1");

        Pattern pattern2 = new Pattern(testImage);
        pattern2.setName("Pattern1");

        Pattern pattern3 = new Pattern(testImage);
        pattern3.setName("Pattern2");

        // Then - Same object
        assertEquals(pattern1, pattern1);
        assertEquals(pattern1.hashCode(), pattern1.hashCode());

        // Different instances with same properties
        assertEquals(pattern1, pattern2);
        assertEquals(pattern1.hashCode(), pattern2.hashCode());

        // Different name
        assertNotEquals(pattern1, pattern3);

        // Null safety
        assertNotEquals(pattern1, null);
        assertNotEquals(pattern1, "not a pattern");
    }

    @Test
    @DisplayName("Should handle complex pattern initialization")
    void testComplexPatternInitialization() {
        // Given
        Pattern pattern = new Pattern(testImage);

        // When - Configure the pattern
        pattern.setName("ComplexPattern");
        pattern.setFixed(true);
        pattern.setDynamic(false);
        pattern.setIndex(5);
        pattern.addSearchRegion(new Region(0, 0, 100, 100));
        pattern.addSearchRegion(new Region(100, 100, 100, 100));
        pattern.setTargetPosition(new Position(75, 25));
        pattern.setTargetOffset(new Location(5, -5));
        pattern.getAnchors().add(new Anchor(Positions.Name.TOPLEFT, new Position(0, 0)));

        // Then
        assertEquals("ComplexPattern", pattern.getName());
        assertTrue(pattern.isFixed());
        assertFalse(pattern.isDynamic());
        assertEquals(5, pattern.getIndex());
        assertFalse(pattern.getRegions().isEmpty());
        assertEquals(0.75, pattern.getTargetPosition().getPercentW());
        assertEquals(0.25, pattern.getTargetPosition().getPercentH());
        assertEquals(5, pattern.getTargetOffset().getX());
        assertEquals(-5, pattern.getTargetOffset().getY());
        assertEquals(1, pattern.getAnchors().size());
    }
}
