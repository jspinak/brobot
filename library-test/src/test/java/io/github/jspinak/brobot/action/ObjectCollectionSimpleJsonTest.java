package io.github.jspinak.brobot.action;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Simple JSON serialization test for ObjectCollection without Spring context. */
class ObjectCollectionSimpleJsonTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
        // Configure ObjectMapper for Brobot classes
        objectMapper.findAndRegisterModules();

        // Create a custom module to handle OpenCV Mat serialization issues
        SimpleModule module = new SimpleModule();
        // Ignore Mat class completely to avoid conflicting setter issues
        module.addSerializer(
                org.bytedeco.opencv.opencv_core.Mat.class,
                new com.fasterxml.jackson.databind.ser.std.ToStringSerializer());
        module.addDeserializer(
                org.bytedeco.opencv.opencv_core.Mat.class,
                new com.fasterxml.jackson.databind.deser.std.FromStringDeserializer<
                        org.bytedeco.opencv.opencv_core.Mat>(
                        org.bytedeco.opencv.opencv_core.Mat.class) {
                    @Override
                    protected org.bytedeco.opencv.opencv_core.Mat _deserialize(
                            String value,
                            com.fasterxml.jackson.databind.DeserializationContext ctxt) {
                        // Return null for Mat deserialization as these fields should be ignored
                        return null;
                    }
                });
        objectMapper.registerModule(module);

        // Configure to ignore unknown properties and avoid failures
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
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
    void testSerializeAndDeserializeWithBasicData() throws Exception {
        // Create an ObjectCollection with various data
        ObjectCollection collection =
                new ObjectCollection.Builder()
                        .withLocations(new Location(10, 20), new Location(30, 40))
                        .withRegions(new Region(50, 60, 100, 80))
                        .withStrings("Test String 1", "Test String 2")
                        .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Verify JSON contains expected values
        assertTrue(json.contains("Test String 1"));
        assertTrue(json.contains("Test String 2"));

        // Deserialize back to ObjectCollection
        ObjectCollection deserializedCollection =
                objectMapper.readValue(json, ObjectCollection.class);

        // Verify the object was correctly deserialized
        assertNotNull(deserializedCollection);
        assertFalse(deserializedCollection.isEmpty());

        // Verify state locations
        assertEquals(2, deserializedCollection.getStateLocations().size());
        assertEquals(10, deserializedCollection.getStateLocations().get(0).getLocation().getX());
        assertEquals(20, deserializedCollection.getStateLocations().get(0).getLocation().getY());
        assertEquals(30, deserializedCollection.getStateLocations().get(1).getLocation().getX());
        assertEquals(40, deserializedCollection.getStateLocations().get(1).getLocation().getY());

        // Verify state regions
        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals(50, deserializedCollection.getStateRegions().get(0).getSearchRegion().x());
        assertEquals(60, deserializedCollection.getStateRegions().get(0).getSearchRegion().y());
        assertEquals(100, deserializedCollection.getStateRegions().get(0).getSearchRegion().w());
        assertEquals(80, deserializedCollection.getStateRegions().get(0).getSearchRegion().h());

        // Verify state strings
        assertEquals(2, deserializedCollection.getStateStrings().size());
        assertEquals("Test String 1", deserializedCollection.getStateStrings().get(0).getString());
        assertEquals("Test String 2", deserializedCollection.getStateStrings().get(1).getString());
    }

    @Test
    void testSerializeAndDeserializeWithStateObjects() throws Exception {
        // Create a comprehensive ObjectCollection using state objects
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
                        .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(collection);
        assertNotNull(json);
        assertFalse(json.isEmpty());

        // Verify JSON contains expected fields
        assertTrue(json.contains("LocationState"));
        assertTrue(json.contains("TestLocation"));
        assertTrue(json.contains("RegionState"));
        assertTrue(json.contains("TestRegion"));
        assertTrue(json.contains("StringState"));
        assertTrue(json.contains("TestString"));
        assertTrue(json.contains("Lorem ipsum"));

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
                deserializedCollection.getStateLocations().get(0).getOwnerStateName());
        assertEquals("TestLocation", deserializedCollection.getStateLocations().get(0).getName());

        assertEquals(1, deserializedCollection.getStateRegions().size());
        assertEquals(
                "RegionState", deserializedCollection.getStateRegions().get(0).getOwnerStateName());
        assertEquals("TestRegion", deserializedCollection.getStateRegions().get(0).getName());

        assertEquals(1, deserializedCollection.getStateStrings().size());
        assertEquals(
                "StringState", deserializedCollection.getStateStrings().get(0).getOwnerStateName());
        assertEquals("TestString", deserializedCollection.getStateStrings().get(0).getName());
        assertEquals("Lorem ipsum", deserializedCollection.getStateStrings().get(0).getString());
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
}
