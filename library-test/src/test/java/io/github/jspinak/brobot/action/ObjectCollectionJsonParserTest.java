package io.github.jspinak.brobot.action;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.testutils.TestPaths;

/**
 * Tests for ObjectCollection JSON serialization with Spring context. Uses Spring Boot's configured
 * ObjectMapper with all necessary Jackson modules.
 */
@SpringBootTest(
        classes = BrobotTestApplication.class,
        properties = {
            "spring.main.lazy-initialization=true",
            "brobot.mock.enabled=true",
            "brobot.illustration.disabled=true",
            "brobot.scene.analysis.disabled=true",
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false"
        })
@Import({
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class ObjectCollectionJsonParserTest {

    @Autowired private ObjectMapper objectMapper;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        ImagePath.setBundlePath("images");
    }

    @BeforeEach
    void setUp() {
        // ObjectMapper is autowired with all necessary modules
    }

    @Test
    void testSerializeAndDeserializeEmptyObjectCollection() throws Exception {
        // Create an empty ObjectCollection
        ObjectCollection collection = new ObjectCollection.Builder().build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Deserialize back to ObjectCollection
        ObjectCollection deserializedCollection =
                objectMapper.readValue(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertTrue(deserializedCollection.isEmpty());
        assertTrue(deserializedCollection.getStateImages().isEmpty());
        assertTrue(deserializedCollection.getStateLocations().isEmpty());
        assertTrue(deserializedCollection.getStateRegions().isEmpty());
        assertTrue(deserializedCollection.getStateStrings().isEmpty());
        assertTrue(deserializedCollection.getMatches().isEmpty());
        assertTrue(deserializedCollection.getScenes().isEmpty());
    }

    @Test
    void testSerializeAndDeserializeObjectCollectionWithData() throws Exception {
        // Create an ObjectCollection with various data
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(new Location(10, 20), new Location(30, 40))
                        .withRegions(new Region(50, 60, 100, 80))
                        .withStrings("Test String 1", "Test String 2")
                        .withScenes(TestPaths.getImagePath("bottomR"))
                        .build();

        // Add a StateImage (normally would be created with more context)
        StateImage stateImage =
                new StateImage.Builder()
                        .setOwnerStateName("TestState")
                        .setName("TestImage")
                        .addPattern(new Pattern(TestPaths.getImagePath("topLeft")))
                        .build();

        collection.getStateImages().add(stateImage);

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized ObjectCollection JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("TestState"));
        assertTrue(json.contains("TestImage"));
        assertTrue(json.contains("topLeft"));
        assertTrue(json.contains("Test String 1"));
        assertTrue(json.contains("bottomR"));

        // Deserialize back to ObjectCollection
        ObjectCollection deserializedCollection =
                objectMapper.readValue(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertFalse(deserializedCollection.isEmpty());

        // Verify state locations
        assertEquals(2, deserializedCollection.getStateLocations().size());
        assertEquals(
                10, deserializedCollection.getStateLocations().getFirst().getLocation().getX());
        assertEquals(
                20, deserializedCollection.getStateLocations().getFirst().getLocation().getY());
        assertEquals(30, deserializedCollection.getStateLocations().get(1).getLocation().getX());
        assertEquals(40, deserializedCollection.getStateLocations().get(1).getLocation().getY());

        // Verify state regions
        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals(50, deserializedCollection.getStateRegions().getFirst().getSearchRegion().x());
        assertEquals(60, deserializedCollection.getStateRegions().getFirst().getSearchRegion().y());
        assertEquals(
                100, deserializedCollection.getStateRegions().getFirst().getSearchRegion().w());
        assertEquals(80, deserializedCollection.getStateRegions().getFirst().getSearchRegion().h());

        // Verify state strings
        assertEquals(2, deserializedCollection.getStateStrings().size());
        assertEquals(
                "Test String 1", deserializedCollection.getStateStrings().getFirst().getString());
        assertEquals("Test String 2", deserializedCollection.getStateStrings().get(1).getString());

        // Verify state images
        assertEquals(1, deserializedCollection.getStateImages().size());
        assertEquals(
                "TestState",
                deserializedCollection.getStateImages().getFirst().getOwnerStateName());
        assertEquals("TestImage", deserializedCollection.getStateImages().getFirst().getName());
        assertEquals(
                TestPaths.getImagePath("topLeft"),
                deserializedCollection
                        .getStateImages()
                        .getFirst()
                        .getPatterns()
                        .getFirst()
                        .getImgpath());

        // Verify scenes
        assertEquals(1, deserializedCollection.getScenes().size());
        assertEquals(
                TestPaths.getImagePath("bottomR"),
                deserializedCollection.getScenes().getFirst().getPattern().getImgpath());
    }

    @Test
    void testUsingBuilderWithAllTypes() throws Exception {
        // Create a comprehensive ObjectCollection using all builder methods
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        // Add locations
                        .withLocations(
                                new StateLocation.Builder()
                                        .setOwnerStateName("LocationState")
                                        .setName("TestLocation")
                                        .setLocation(new Location(100, 200))
                                        .build())

                        // Add regions
                        .withRegions(
                                new StateRegion.Builder()
                                        .setOwnerStateName("RegionState")
                                        .setName("TestRegion")
                                        .setSearchRegion(new Region(300, 400, 200, 150))
                                        .build())

                        // Add strings
                        .withStrings(
                                new StateString.Builder()
                                        .setOwnerStateName("StringState")
                                        .setName("TestString")
                                        .setString("Lorem ipsum")
                                        .build())

                        // Add patterns
                        .withPatterns(new Pattern(TestPaths.getImagePath("topLeft")))

                        // Add scenes
                        .withScenes(new Scene(TestPaths.getImagePath("bottomR")))

                        // Add a ActionResult object
                        .withMatches(new ActionResult())
                        .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Print the JSON for debugging
        System.out.println("Serialized Complex ObjectCollection JSON:");
        System.out.println(json);

        // Verify JSON contains expected fields
        assertTrue(json.contains("LocationState"));
        assertTrue(json.contains("TestLocation"));
        assertTrue(json.contains("RegionState"));
        assertTrue(json.contains("TestRegion"));
        assertTrue(json.contains("StringState"));
        assertTrue(json.contains("TestString"));
        assertTrue(json.contains("Lorem ipsum"));
        assertTrue(json.contains("topLeft"));
        assertTrue(json.contains("bottomR"));

        // Deserialize back to ObjectCollection
        ObjectCollection deserializedCollection =
                objectMapper.readValue(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertFalse(deserializedCollection.isEmpty());

        // Verify all parts of the collection
        assertEquals(1, deserializedCollection.getStateLocations().size());
        assertEquals(
                "LocationState",
                deserializedCollection.getStateLocations().getFirst().getOwnerStateName());
        assertEquals(
                "TestLocation", deserializedCollection.getStateLocations().getFirst().getName());

        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals(
                "RegionState",
                deserializedCollection.getStateRegions().getFirst().getOwnerStateName());
        assertEquals("TestRegion", deserializedCollection.getStateRegions().getFirst().getName());

        assertEquals(1, deserializedCollection.getStateStrings().size());
        assertEquals(
                "StringState",
                deserializedCollection.getStateStrings().getFirst().getOwnerStateName());
        assertEquals("TestString", deserializedCollection.getStateStrings().getFirst().getName());
        assertEquals(
                "Lorem ipsum", deserializedCollection.getStateStrings().getFirst().getString());

        assertEquals(1, deserializedCollection.getStateImages().size());
        assertEquals(
                TestPaths.getImagePath("topLeft"),
                deserializedCollection
                        .getStateImages()
                        .getFirst()
                        .getPatterns()
                        .getFirst()
                        .getImgpath());

        assertEquals(1, deserializedCollection.getScenes().size());
        assertEquals(
                TestPaths.getImagePath("bottomR"),
                deserializedCollection.getScenes().getFirst().getPattern().getImgpath());

        assertEquals(1, deserializedCollection.getMatches().size());
    }

    @Test
    void testSerializeAndDeserializeWithNullFields() throws Exception {
        // Create ObjectCollection with some null fields
        ObjectCollection collection = new ObjectCollection.Builder().build();
        collection.getStateImages().add(new StateImage());

        // Serialize
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);

        // Deserialize
        ObjectCollection deserialized = objectMapper.readValue(json, ObjectCollection.class);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.getStateImages().size());
    }

    @Test
    void testRoundTripWithComplexPattern() throws Exception {
        // Create complex pattern with search regions
        Pattern pattern =
                new Pattern.Builder()
                        .setFilename(TestPaths.getImagePath("topLeft"))
                        .addSearchRegion(new Region(0, 0, 100, 100))
                        .addSearchRegion(new Region(100, 100, 200, 200))
                        .build();

        StateImage stateImage =
                new StateImage.Builder()
                        .setOwnerStateName("ComplexState")
                        .addPattern(pattern)
                        .build();

        ObjectCollection collection = new ObjectCollection.Builder().withImages(stateImage).build();

        // Round trip
        String json = objectMapper.writeValueAsString(collection);
        System.out.println("testRoundTripWithComplexPattern JSON: " + json);
        ObjectCollection deserialized = objectMapper.readValue(json, ObjectCollection.class);

        // Verify
        assertNotNull(deserialized);
        assertEquals(1, deserialized.getStateImages().size());
        StateImage deserializedImage = deserialized.getStateImages().getFirst();
        assertEquals("ComplexState", deserializedImage.getOwnerStateName());
        assertEquals(1, deserializedImage.getPatterns().size());

        Pattern deserializedPattern = deserializedImage.getPatterns().getFirst();
        // The pattern name should be preserved
        assertEquals("topLeft", deserializedPattern.getNameWithoutExtension());
        // The imgpath might not be preserved during serialization/deserialization
        // but the pattern should still have the correct search regions
        assertEquals(2, deserializedPattern.getSearchRegions().getRegions().size());
    }
}
