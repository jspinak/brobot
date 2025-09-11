package io.github.jspinak.brobot.model.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the EmptyMatch class which represents the absence of a match in the
 * Brobot model-based GUI automation framework.
 */
@DisplayName("EmptyMatch Model Tests")
public class EmptyMatchTest extends BrobotTestBase {

    private Image testImage;
    private Scene testScene;
    private StateObjectMetadata testMetadata;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testImage = new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
        testScene = mock(Scene.class);
        testMetadata = mock(StateObjectMetadata.class);
    }

    @Test
    @DisplayName("Should create EmptyMatch with default values")
    void testDefaultConstructor() {
        // When
        EmptyMatch emptyMatch = new EmptyMatch();

        // Then
        assertEquals("no match", emptyMatch.getName());
        assertNotNull(emptyMatch.getRegion());
        assertEquals(0, emptyMatch.getRegion().x());
        assertEquals(0, emptyMatch.getRegion().y());
        assertEquals(0, emptyMatch.getRegion().w());
        assertEquals(0, emptyMatch.getRegion().h());
        assertEquals(0.0, emptyMatch.getScore());
    }

    @Test
    @DisplayName("Should be instance of Match")
    void testInheritance() {
        // When
        EmptyMatch emptyMatch = new EmptyMatch();

        // Then
        assertTrue(emptyMatch instanceof Match);
        assertNotNull(emptyMatch);
    }

    @Test
    @DisplayName("Should create Match with Builder using defaults")
    void testBuilderWithDefaults() {
        // When
        Match match = new EmptyMatch.Builder().build();

        // Then
        assertEquals("no match", match.getName());
        assertNotNull(match.getRegion());
        assertEquals(0, match.getRegion().x());
        assertEquals(0, match.getRegion().y());
        assertEquals(0, match.getRegion().w());
        assertEquals(0, match.getRegion().h());
        assertEquals(0.0, match.getScore());
        assertNull(match.getSearchImage());
        assertNull(match.getScene());
        assertNull(match.getStateObjectData());
    }

    @Test
    @DisplayName("Should set custom name via Builder")
    void testBuilderWithCustomName() {
        // When
        Match match = new EmptyMatch.Builder().setName("custom empty match").build();

        // Then
        assertEquals("custom empty match", match.getName());
        assertEquals(0.0, match.getScore()); // Score should still be 0
    }

    @Test
    @DisplayName("Should set custom region via Builder")
    void testBuilderWithCustomRegion() {
        // Given
        Region customRegion = new Region(10, 20, 30, 40);

        // When
        Match match = new EmptyMatch.Builder().setRegion(customRegion).build();

        // Then
        assertSame(customRegion, match.getRegion());
        assertEquals(10, match.getRegion().x());
        assertEquals(20, match.getRegion().y());
        assertEquals(30, match.getRegion().w());
        assertEquals(40, match.getRegion().h());
        assertEquals(0.0, match.getScore()); // Score should still be 0
    }

    @Test
    @DisplayName("Should set search image via Builder")
    void testBuilderWithSearchImage() {
        // When
        Match match = new EmptyMatch.Builder().setSearchImage(testImage).build();

        // Then
        assertSame(testImage, match.getSearchImage());
        assertEquals(0.0, match.getScore());
    }

    @Test
    @DisplayName("Should set scene via Builder")
    void testBuilderWithScene() {
        // When
        Match match = new EmptyMatch.Builder().setScene(testScene).build();

        // Then
        assertSame(testScene, match.getScene());
        assertEquals(0.0, match.getScore());
    }

    @Test
    @DisplayName("Should set state object metadata via Builder")
    void testBuilderWithStateObjectMetadata() {
        // When
        Match match = new EmptyMatch.Builder().setStateObjectData(testMetadata).build();

        // Then
        assertSame(testMetadata, match.getStateObjectData());
        assertEquals(0.0, match.getScore());
    }

    @Test
    @DisplayName("Should set all properties via Builder")
    void testBuilderWithAllProperties() {
        // Given
        String customName = "fully configured empty match";
        Region customRegion = new Region(5, 10, 15, 20);

        // When
        Match match =
                new EmptyMatch.Builder()
                        .setName(customName)
                        .setRegion(customRegion)
                        .setSearchImage(testImage)
                        .setScene(testScene)
                        .setStateObjectData(testMetadata)
                        .build();

        // Then
        assertEquals(customName, match.getName());
        assertSame(customRegion, match.getRegion());
        assertSame(testImage, match.getSearchImage());
        assertSame(testScene, match.getScene());
        assertSame(testMetadata, match.getStateObjectData());
        assertEquals(0.0, match.getScore()); // Score always 0
    }

    @Test
    @DisplayName("Should always have zero score regardless of builder settings")
    void testScoreAlwaysZero() {
        // When - Try various builder configurations
        Match match1 = new EmptyMatch.Builder().build();
        Match match2 =
                new EmptyMatch.Builder()
                        .setName("test")
                        .setRegion(new Region(100, 100, 100, 100))
                        .build();
        Match match3 =
                new EmptyMatch.Builder().setSearchImage(testImage).setScene(testScene).build();

        // Then - All should have score of 0
        assertEquals(0.0, match1.getScore());
        assertEquals(0.0, match2.getScore());
        assertEquals(0.0, match3.getScore());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should handle null values in Builder")
    void testBuilderWithNullValues(String nullValue) {
        // When
        Match match =
                new EmptyMatch.Builder()
                        .setName(nullValue)
                        .setRegion(null)
                        .setSearchImage(null)
                        .setScene(null)
                        .setStateObjectData(null)
                        .build();

        // Then
        assertNull(match.getName());
        assertNull(match.getRegion());
        assertNull(match.getSearchImage());
        assertNull(match.getScene());
        assertNull(match.getStateObjectData());
        assertEquals(0.0, match.getScore());
    }

    @Test
    @DisplayName("Should support method chaining in Builder")
    void testBuilderMethodChaining() {
        // When - All methods should return Builder
        Match match =
                new EmptyMatch.Builder()
                        .setName("chained")
                        .setRegion(new Region())
                        .setSearchImage(testImage)
                        .setScene(testScene)
                        .setStateObjectData(testMetadata)
                        .build();

        // Then
        assertNotNull(match);
        assertEquals("chained", match.getName());
    }

    @TestFactory
    @DisplayName("Null Object Pattern compliance")
    Stream<DynamicTest> testNullObjectPattern() {
        return Stream.of(
                dynamicTest(
                        "Can be used in collections",
                        () -> {
                            EmptyMatch emptyMatch = new EmptyMatch();
                            var matches = new java.util.ArrayList<Match>();
                            matches.add(emptyMatch);
                            assertEquals(1, matches.size());
                            assertEquals(0.0, matches.get(0).getScore());
                        }),
                dynamicTest(
                        "Can be compared without null checks",
                        () -> {
                            EmptyMatch emptyMatch = new EmptyMatch();
                            Match regularMatch = new Match();
                            regularMatch.setScore(0.95);

                            // No null pointer exception
                            assertTrue(emptyMatch.getScore() < regularMatch.getScore());
                        }),
                dynamicTest(
                        "Can access all properties safely",
                        () -> {
                            EmptyMatch emptyMatch = new EmptyMatch();

                            // All of these should work without NPE
                            assertNotNull(emptyMatch.getName());
                            assertNotNull(emptyMatch.getRegion());
                            assertEquals(0, emptyMatch.getRegion().w());
                            assertEquals(0.0, emptyMatch.getScore());
                        }),
                dynamicTest(
                        "Can be used as default value",
                        () -> {
                            Match defaultMatch = new EmptyMatch();
                            Match result = findMatch() != null ? findMatch() : defaultMatch;
                            assertNotNull(result);
                            assertEquals("no match", result.getName());
                        }));
    }

    // Helper method for Null Object Pattern test
    private Match findMatch() {
        return null; // Simulates failed search
    }

    @Test
    @DisplayName("Should maintain EmptyMatch semantics after modifications")
    void testEmptyMatchSemantics() {
        // Given
        EmptyMatch emptyMatch = new EmptyMatch();

        // When - Try to modify the match
        emptyMatch.setScore(0.95); // Try to set a non-zero score
        emptyMatch.setName("modified");
        emptyMatch.setRegion(new Region(10, 10, 10, 10));

        // Then - It behaves as a regular Match after modification
        assertEquals(0.95, emptyMatch.getScore());
        assertEquals("modified", emptyMatch.getName());
        assertEquals(10, emptyMatch.getRegion().w());

        // Note: EmptyMatch constructor ensures initial state,
        // but doesn't prevent modification afterwards
    }

    @ParameterizedTest
    @ValueSource(strings = {"failed search", "not found", "missing element", "timeout", ""})
    @DisplayName("Should support various failure descriptions")
    void testVariousFailureDescriptions(String description) {
        // When
        Match match = new EmptyMatch.Builder().setName(description).build();

        // Then
        assertEquals(description, match.getName());
        assertEquals(0.0, match.getScore());
    }

    @Test
    @DisplayName("Should be useful for debugging with context")
    void testDebuggingContext() {
        // Given - A failed search scenario
        Image searchedImage =
                new Image(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB), "button.png");
        Scene searchedScene = mock(Scene.class);

        // When - Create EmptyMatch with debugging context
        Match failedMatch =
                new EmptyMatch.Builder()
                        .setName("button.png not found")
                        .setSearchImage(searchedImage)
                        .setScene(searchedScene)
                        .build();

        // Then - Context is preserved for debugging
        assertEquals("button.png not found", failedMatch.getName());
        assertSame(searchedImage, failedMatch.getSearchImage());
        assertSame(searchedScene, failedMatch.getScene());
        assertEquals(0.0, failedMatch.getScore());

        // This information can be used to understand why the match failed
        assertNotNull(failedMatch.getSearchImage().getName());
        assertEquals("button.png", failedMatch.getSearchImage().getName());
    }

    @Test
    @DisplayName("Should compare EmptyMatch instances")
    void testEmptyMatchComparison() {
        // Given
        EmptyMatch empty1 = new EmptyMatch();
        EmptyMatch empty2 = new EmptyMatch();

        Match configured1 = new EmptyMatch.Builder().setName("empty1").build();
        Match configured2 = new EmptyMatch.Builder().setName("empty1").build();

        // Then - Different instances but same properties
        assertNotSame(empty1, empty2);
        assertEquals(empty1.getName(), empty2.getName());
        assertEquals(empty1.getScore(), empty2.getScore());

        // Configured matches with same name
        assertEquals(configured1.getName(), configured2.getName());
        assertNotSame(configured1, configured2);
    }

    @Test
    @DisplayName("Should be distinguishable from successful Match")
    void testDistinguishFromSuccessfulMatch() {
        // Given
        EmptyMatch emptyMatch = new EmptyMatch();
        Match successfulMatch = new Match();
        successfulMatch.setScore(0.85);
        successfulMatch.setRegion(new Region(10, 10, 100, 100));
        successfulMatch.setName("found element");

        // Then
        assertTrue(emptyMatch.getScore() < successfulMatch.getScore());
        assertTrue(emptyMatch.getRegion().w() == 0);
        assertTrue(successfulMatch.getRegion().w() > 0);
        assertNotEquals(emptyMatch.getName(), successfulMatch.getName());
    }
}
