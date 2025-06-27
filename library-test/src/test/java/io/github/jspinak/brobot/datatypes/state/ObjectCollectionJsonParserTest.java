package io.github.jspinak.brobot.datatypes.state;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.testutils.TestPaths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ObjectCollectionJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Test
    void testSerializeAndDeserializeEmptyObjectCollection() throws ConfigurationException {
        // Create an empty ObjectCollection
        ObjectCollection collection = new ObjectCollection();

        // Serialize to JSON
        String json = jsonParser.toJson(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Deserialize back to ObjectCollection
        ObjectCollection deserializedCollection = jsonParser.convertJson(json, ObjectCollection.class);

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
    void testSerializeAndDeserializeObjectCollectionWithData() throws ConfigurationException {
        // Create an ObjectCollection with various data
        ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(new Location(10, 20), new Location(30, 40))
                .withRegions(new Region(50, 60, 100, 80))
                .withStrings("Test String 1", "Test String 2")
                .withScenes(TestPaths.getImagePath("bottomR"))
                .build();

        // Add a StateImage (normally would be created with more context)
        StateImage stateImage = new StateImage.Builder()
                .setOwnerStateName("TestState")
                .setName("TestImage")
                .addPattern(new Pattern(TestPaths.getImagePath("topLeft")))
                .build();

        collection.getStateImages().add(stateImage);

        // Serialize to JSON
        String json = jsonParser.toJson(collection);
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
        ObjectCollection deserializedCollection = jsonParser.convertJson(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertFalse(deserializedCollection.isEmpty());

        // Verify state locations
        assertEquals(2, deserializedCollection.getStateLocations().size());
        assertEquals(10, deserializedCollection.getStateLocations().getFirst().getLocation().getX());
        assertEquals(20, deserializedCollection.getStateLocations().getFirst().getLocation().getY());
        assertEquals(30, deserializedCollection.getStateLocations().get(1).getLocation().getX());
        assertEquals(40, deserializedCollection.getStateLocations().get(1).getLocation().getY());

        // Verify state regions
        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals(50, deserializedCollection.getStateRegions().getFirst().getSearchRegion().x());
        assertEquals(60, deserializedCollection.getStateRegions().getFirst().getSearchRegion().y());
        assertEquals(100, deserializedCollection.getStateRegions().getFirst().getSearchRegion().w());
        assertEquals(80, deserializedCollection.getStateRegions().getFirst().getSearchRegion().h());

        // Verify state strings
        assertEquals(2, deserializedCollection.getStateStrings().size());
        assertEquals("Test String 1", deserializedCollection.getStateStrings().getFirst().getString());
        assertEquals("Test String 2", deserializedCollection.getStateStrings().get(1).getString());

        // Verify state images
        assertEquals(1, deserializedCollection.getStateImages().size());
        assertEquals("TestState", deserializedCollection.getStateImages().getFirst().getOwnerStateName());
        assertEquals("TestImage", deserializedCollection.getStateImages().getFirst().getName());
        assertEquals(TestPaths.getImagePath("topLeft"), deserializedCollection.getStateImages().getFirst().getPatterns().getFirst().getImgpath());

        // Verify scenes
        assertEquals(1, deserializedCollection.getScenes().size());
        assertEquals(TestPaths.getImagePath("bottomR"), deserializedCollection.getScenes().getFirst().getPattern().getImgpath());
    }

    @Test
    void testUsingBuilderWithAllTypes() throws ConfigurationException {
        // Create a comprehensive ObjectCollection using all builder methods
        ObjectCollection collection = new ObjectCollection.Builder()
                // Add locations
                .withLocations(new StateLocation.Builder()
                        .setOwnerStateName("LocationState")
                        .setName("TestLocation")
                        .setLocation(new Location(100, 200))
                        .build())

                // Add regions
                .withRegions(new StateRegion.Builder()
                        .setOwnerStateName("RegionState")
                        .setName("TestRegion")
                        .setSearchRegion(new Region(300, 400, 200, 150))
                        .build())

                // Add strings
                .withStrings(new StateString.Builder()
                        .setOwnerStateName("StringState")
                        .setName("TestString")
                        .build("Lorem ipsum"))

                // Add patterns
                .withPatterns(new Pattern(TestPaths.getImagePath("topLeft")))

                // Add scenes
                .withScenes(new Scene(TestPaths.getImagePath("bottomR")))

                // Add a Matches object
                .withMatches(new ActionResult())

                .build();

        // Serialize to JSON
        String json = jsonParser.toJson(collection);
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
        ObjectCollection deserializedCollection = jsonParser.convertJson(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertFalse(deserializedCollection.isEmpty());

        // Verify all parts of the collection
        assertEquals(1, deserializedCollection.getStateLocations().size());
        assertEquals("LocationState", deserializedCollection.getStateLocations().getFirst().getOwnerStateName());
        assertEquals("TestLocation", deserializedCollection.getStateLocations().getFirst().getName());

        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals("RegionState", deserializedCollection.getStateRegions().getFirst().getOwnerStateName());
        assertEquals("TestRegion", deserializedCollection.getStateRegions().getFirst().getName());

        assertEquals(1, deserializedCollection.getStateStrings().size());
        assertEquals("StringState", deserializedCollection.getStateStrings().getFirst().getOwnerStateName());
        assertEquals("TestString", deserializedCollection.getStateStrings().getFirst().getName());
        assertEquals("Lorem ipsum", deserializedCollection.getStateStrings().getFirst().getString());

        assertEquals(1, deserializedCollection.getStateImages().size());
        assertEquals(TestPaths.getImagePath("topLeft"), deserializedCollection.getStateImages().getFirst().getPatterns().getFirst().getImgpath());

        assertEquals(1, deserializedCollection.getScenes().size());
        assertEquals(TestPaths.getImagePath("bottomR"), deserializedCollection.getScenes().getFirst().getPattern().getImgpath());

        assertEquals(1, deserializedCollection.getMatches().size());
    }
}