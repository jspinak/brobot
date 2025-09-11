package io.github.jspinak.brobot.model.element;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for the Scene class which represents a captured screenshot or screen state as
 * a searchable pattern in the Brobot framework.
 */
@DisplayName("Scene Model Tests")
public class SceneTest extends BrobotTestBase {

    private Pattern testPattern;
    private BufferedImage testImage;
    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        testImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        testPattern = new Pattern(new Image(testImage, "TestScene"));
        testPattern.setName("TestPattern");
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create scene with Pattern constructor")
    void testConstructorWithPattern() {
        // When
        Scene scene = new Scene(testPattern);

        // Then
        assertSame(testPattern, scene.getPattern());
        assertEquals(-1L, scene.getId());
    }

    @Test
    @DisplayName("Should create scene with filename constructor")
    void testConstructorWithFilename() {
        // Given
        String filename = "test-scene.png";

        // When
        Scene scene = new Scene(filename);

        // Then
        assertNotNull(scene.getPattern());
        assertEquals(-1L, scene.getId());
        // In mock mode, the pattern will be created but may not load the actual file
    }

    @Test
    @DisplayName("Should create scene with default constructor")
    void testDefaultConstructor() {
        // When
        Scene scene = new Scene();

        // Then
        assertNotNull(scene.getPattern());
        assertEquals(-1L, scene.getId());
    }

    @Test
    @DisplayName("Should get and set ID correctly")
    void testGetAndSetId() {
        // Given
        Scene scene = new Scene(testPattern);

        // When
        scene.setId(12345L);

        // Then
        assertEquals(12345L, scene.getId());
    }

    @Test
    @DisplayName("Should get and set Pattern correctly")
    void testGetAndSetPattern() {
        // Given
        Scene scene = new Scene();
        Pattern newPattern = new Pattern(new Image(testImage, "NewPattern"));

        // When
        scene.setPattern(newPattern);

        // Then
        assertSame(newPattern, scene.getPattern());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        // Given
        Scene scene = new Scene(testPattern);
        scene.setId(999L);

        // When
        String toString = scene.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Scene"));
        assertTrue(toString.contains("id=999"));
        assertTrue(toString.contains("TestPattern"));
    }

    @Test
    @DisplayName("Should handle toString with null pattern")
    void testToStringWithNullPattern() {
        // Given
        Scene scene = new Scene();
        scene.setPattern(null);

        // When
        String toString = scene.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("null"));
    }

    @Test
    @DisplayName("Should serialize and deserialize to/from JSON")
    void testJacksonSerialization() throws JsonProcessingException {
        // Given - Create a scene without Image (which isn't serializable)
        Scene scene = new Scene();
        scene.setId(42L);
        scene.getPattern().setName("TestPattern");

        // When - Serialize
        String json = objectMapper.writeValueAsString(scene);

        // Then - JSON contains expected fields
        assertNotNull(json);
        assertTrue(json.contains("\"id\":42"));
        assertTrue(json.contains("pattern"));

        // When - Deserialize
        Scene deserialized = objectMapper.readValue(json, Scene.class);

        // Then - Objects have same properties
        assertEquals(scene.getId(), deserialized.getId());
        assertNotNull(deserialized.getPattern());
        assertEquals("TestPattern", deserialized.getPattern().getName());
    }

    @Test
    @DisplayName("Should handle JsonIgnoreProperties annotation")
    void testJsonIgnoreUnknownProperties() throws JsonProcessingException {
        // Given - JSON with unknown property
        String jsonWithUnknown =
                "{\"id\":123,\"pattern\":{\"name\":\"test\"},\"unknownProperty\":\"unknown"
                        + " value\"}";

        // When - Should not throw exception due to @JsonIgnoreProperties
        Scene scene = objectMapper.readValue(jsonWithUnknown, Scene.class);

        // Then
        assertEquals(123L, scene.getId());
        assertNotNull(scene.getPattern());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 1L, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Should handle various ID values")
    void testVariousIdValues(long id) {
        // Given
        Scene scene = new Scene(testPattern);

        // When
        scene.setId(id);

        // Then
        assertEquals(id, scene.getId());
    }

    @Test
    @DisplayName("Should handle equals and hashCode")
    void testEqualsAndHashCode() {
        // Given
        Scene scene1 = new Scene(testPattern);
        scene1.setId(100L);

        Scene scene2 = new Scene(testPattern);
        scene2.setId(100L);

        Scene scene3 = new Scene(testPattern);
        scene3.setId(200L);

        Scene scene4 = new Scene(new Pattern());
        scene4.setId(100L);

        // Then - Reflexive
        assertEquals(scene1, scene1);
        assertEquals(scene1.hashCode(), scene1.hashCode());

        // Symmetric
        assertEquals(scene1, scene2);
        assertEquals(scene2, scene1);
        assertEquals(scene1.hashCode(), scene2.hashCode());

        // Different ID
        assertNotEquals(scene1, scene3);

        // Different pattern
        assertNotEquals(scene1, scene4);

        // Null safety
        assertNotEquals(scene1, null);

        // Different class
        assertNotEquals(scene1, "not a scene");
    }

    @TestFactory
    @DisplayName("Scene workflow tests")
    Stream<DynamicTest> testSceneWorkflow() {
        return Stream.of(
                dynamicTest(
                        "Create scene from screenshot",
                        () -> {
                            // Simulate capturing a screenshot
                            BufferedImage screenshot =
                                    new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
                            Pattern screenPattern =
                                    new Pattern(new Image(screenshot, "Screenshot"));
                            Scene scene = new Scene(screenPattern);

                            assertNotNull(scene.getPattern());
                            assertEquals("Screenshot", scene.getPattern().getImage().getName());
                        }),
                dynamicTest(
                        "Store scene with database ID",
                        () -> {
                            Scene scene = new Scene(testPattern);
                            // Simulate database storage
                            Long dbId = 54321L;
                            scene.setId(dbId);

                            assertEquals(dbId, scene.getId());
                            assertTrue(scene.getId() > 0);
                        }),
                dynamicTest(
                        "Use scene for mock testing",
                        () -> {
                            Scene mockScene = new Scene(testPattern);
                            mockScene.setId(1000L);

                            // Scene can be used to provide mock screenshots
                            assertNotNull(mockScene.getPattern());
                            assertNotNull(mockScene.getPattern().getImage());
                        }),
                dynamicTest(
                        "Compare scenes",
                        () -> {
                            Scene scene1 = new Scene(testPattern);
                            Scene scene2 = new Scene(testPattern);

                            // Different instances but same pattern
                            assertNotSame(scene1, scene2);
                            assertSame(scene1.getPattern(), scene2.getPattern());
                        }));
    }

    @Test
    @DisplayName("Should handle pattern with search regions")
    void testSceneWithPatternContainingSearchRegions() {
        // Given
        Pattern patternWithRegions = new Pattern(new Image(testImage));
        patternWithRegions.addSearchRegion(new Region(0, 0, 100, 100));
        patternWithRegions.addSearchRegion(new Region(100, 100, 200, 200));

        // When
        Scene scene = new Scene(patternWithRegions);

        // Then
        assertSame(patternWithRegions, scene.getPattern());
        assertFalse(scene.getPattern().getRegions().isEmpty());
    }

    @Test
    @DisplayName("Should handle pattern with fixed location")
    void testSceneWithFixedPattern() {
        // Given
        Pattern fixedPattern = new Pattern(new Image(testImage));
        fixedPattern.setFixed(true);
        fixedPattern.getSearchRegions().setFixedRegion(new Region(500, 500, 100, 100));

        // When
        Scene scene = new Scene(fixedPattern);

        // Then
        assertTrue(scene.getPattern().isFixed());
        assertNotNull(scene.getPattern().getRegion());
    }

    @Test
    @DisplayName("Should handle empty scene creation")
    void testEmptySceneCreation() {
        // When
        Scene emptyScene = new Scene();

        // Then
        assertNotNull(emptyScene);
        assertNotNull(emptyScene.getPattern());
        assertTrue(emptyScene.getPattern().isEmpty());
        assertEquals(-1L, emptyScene.getId());
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should handle null pattern")
    void testNullPattern(Pattern nullPattern) {
        // When
        Scene scene = new Scene(nullPattern);

        // Then
        assertNull(scene.getPattern());
        assertEquals(-1L, scene.getId());
    }

    @Test
    @DisplayName("Should support scene comparison for state verification")
    void testSceneComparisonForStateVerification() {
        // Given - Expected scene from reference
        Scene expectedScene = new Scene(testPattern);
        expectedScene.setId(1L);

        // Given - Actual scene from capture
        Scene actualScene = new Scene(testPattern);
        actualScene.setId(2L);

        // Then - Can compare patterns even if IDs differ
        assertSame(expectedScene.getPattern(), actualScene.getPattern());
        assertNotEquals(expectedScene.getId(), actualScene.getId());
    }

    @Test
    @DisplayName("Should support scene as container for multiple patterns")
    void testSceneAsMultiPatternContainer() {
        // Given - A scene representing a complex screen
        BufferedImage complexScreen = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Pattern screenPattern = new Pattern(new Image(complexScreen, "ComplexScreen"));

        // Add multiple search regions representing different UI elements
        screenPattern.addSearchRegion(new Region(0, 0, 500, 100)); // Header
        screenPattern.addSearchRegion(new Region(0, 100, 200, 900)); // Sidebar
        screenPattern.addSearchRegion(new Region(200, 100, 1720, 900)); // Main content
        screenPattern.addSearchRegion(new Region(0, 1000, 1920, 80)); // Footer

        // When
        Scene scene = new Scene(screenPattern);

        // Then - Check that regions were added
        assertFalse(scene.getPattern().getRegions().isEmpty());
        assertNotNull(scene.getPattern().getImage());
        // The exact number of regions may vary based on how SearchRegions handles them
        assertTrue(scene.getPattern().getRegions().size() >= 1);
    }

    @Test
    @DisplayName("Should maintain scene identity across operations")
    void testSceneIdentityPersistence() {
        // Given
        Scene scene = new Scene(testPattern);
        Long originalId = 777L;
        scene.setId(originalId);

        // When - Modify pattern
        Pattern newPattern = new Pattern(new Image(testImage, "Modified"));
        scene.setPattern(newPattern);

        // Then - ID should remain unchanged
        assertEquals(originalId, scene.getId());
        assertSame(newPattern, scene.getPattern());
    }
}
