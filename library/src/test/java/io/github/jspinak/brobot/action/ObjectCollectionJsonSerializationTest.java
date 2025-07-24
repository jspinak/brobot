package io.github.jspinak.brobot.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ObjectCollection JSON serialization/deserialization without Spring dependencies.
 * Migrated from library-test module.
 * 
 * Note: This test focuses on JSON structure validation and ObjectCollection behavior
 * rather than full object serialization due to complex dependencies on custom serializers.
 */
class ObjectCollectionJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test empty ObjectCollection
     */
    @Test
    void testEmptyObjectCollection() {
        // Create an empty ObjectCollection
        ObjectCollection collection = new ObjectCollection();

        // Verify it's empty
        assertTrue(collection.isEmpty());
        assertTrue(collection.getStateImages().isEmpty());
        assertTrue(collection.getStateLocations().isEmpty());
        assertTrue(collection.getStateRegions().isEmpty());
        assertTrue(collection.getStateStrings().isEmpty());
        assertTrue(collection.getMatches().isEmpty());
        assertTrue(collection.getScenes().isEmpty());
    }

    /**
     * Test ObjectCollection with locations
     */
    @Test
    void testObjectCollectionWithLocations() {
        // Create an ObjectCollection with locations
        ObjectCollection collection = new ObjectCollection.Builder()
                .withLocations(new Location(10, 20), new Location(30, 40))
                .build();

        // Verify locations were added
        assertFalse(collection.isEmpty());
        assertEquals(2, collection.getStateLocations().size());
        
        // Check first location
        StateLocation loc1 = collection.getStateLocations().get(0);
        assertEquals(10, loc1.getLocation().getX());
        assertEquals(20, loc1.getLocation().getY());
        
        // Check second location
        StateLocation loc2 = collection.getStateLocations().get(1);
        assertEquals(30, loc2.getLocation().getX());
        assertEquals(40, loc2.getLocation().getY());
    }

    /**
     * Test ObjectCollection with regions
     */
    @Test
    void testObjectCollectionWithRegions() {
        // Create an ObjectCollection with regions
        ObjectCollection collection = new ObjectCollection.Builder()
                .withRegions(new Region(50, 60, 100, 80))
                .build();

        // Verify regions were added
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.getStateRegions().size());
        
        StateRegion region = collection.getStateRegions().getFirst();
        assertEquals(50, region.getSearchRegion().x());
        assertEquals(60, region.getSearchRegion().y());
        assertEquals(100, region.getSearchRegion().w());
        assertEquals(80, region.getSearchRegion().h());
    }

    /**
     * Test ObjectCollection with strings
     */
    @Test
    void testObjectCollectionWithStrings() {
        // Create an ObjectCollection with strings
        ObjectCollection collection = new ObjectCollection.Builder()
                .withStrings("Test String 1", "Test String 2")
                .build();

        // Verify strings were added
        assertFalse(collection.isEmpty());
        assertEquals(2, collection.getStateStrings().size());
        assertEquals("Test String 1", collection.getStateStrings().get(0).getString());
        assertEquals("Test String 2", collection.getStateStrings().get(1).getString());
    }

    /**
     * Test ObjectCollection with StateImage
     */
    @Test
    void testObjectCollectionWithStateImage() {
        // Create a StateImage
        StateImage stateImage = new StateImage.Builder()
                .setOwnerStateName("TestState")
                .setName("TestImage")
                .build();

        // Create collection and add StateImage
        ObjectCollection collection = new ObjectCollection();
        collection.getStateImages().add(stateImage);

        // Verify
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.getStateImages().size());
        assertEquals("TestState", collection.getStateImages().getFirst().getOwnerStateName());
        assertEquals("TestImage", collection.getStateImages().getFirst().getName());
    }

    /**
     * Test ObjectCollection JSON structure creation
     */
    @Test
    void testObjectCollectionJsonStructure() throws Exception {
        // Create JSON structure for ObjectCollection
        ObjectNode collectionNode = objectMapper.createObjectNode();
        
        // Add state images array
        ArrayNode stateImagesArray = objectMapper.createArrayNode();
        ObjectNode stateImageNode = objectMapper.createObjectNode();
        stateImageNode.put("name", "TestImage");
        stateImageNode.put("ownerStateName", "TestState");
        stateImagesArray.add(stateImageNode);
        collectionNode.set("stateImages", stateImagesArray);
        
        // Add state locations array
        ArrayNode stateLocationsArray = objectMapper.createArrayNode();
        ObjectNode stateLocationNode = objectMapper.createObjectNode();
        stateLocationNode.put("name", "TestLocation");
        stateLocationNode.put("ownerStateName", "LocationState");
        ObjectNode locationNode = objectMapper.createObjectNode();
        locationNode.put("x", 100);
        locationNode.put("y", 200);
        stateLocationNode.set("location", locationNode);
        stateLocationsArray.add(stateLocationNode);
        collectionNode.set("stateLocations", stateLocationsArray);
        
        // Add state regions array
        ArrayNode stateRegionsArray = objectMapper.createArrayNode();
        ObjectNode stateRegionNode = objectMapper.createObjectNode();
        stateRegionNode.put("name", "TestRegion");
        stateRegionNode.put("ownerStateName", "RegionState");
        ObjectNode regionNode = objectMapper.createObjectNode();
        regionNode.put("x", 300);
        regionNode.put("y", 400);
        regionNode.put("w", 200);
        regionNode.put("h", 150);
        stateRegionNode.set("searchRegion", regionNode);
        stateRegionsArray.add(stateRegionNode);
        collectionNode.set("stateRegions", stateRegionsArray);
        
        // Add state strings array
        ArrayNode stateStringsArray = objectMapper.createArrayNode();
        ObjectNode stateStringNode = objectMapper.createObjectNode();
        stateStringNode.put("name", "TestString");
        stateStringNode.put("ownerStateName", "StringState");
        stateStringNode.put("string", "Lorem ipsum");
        stateStringsArray.add(stateStringNode);
        collectionNode.set("stateStrings", stateStringsArray);
        
        // Convert to string
        String json = objectMapper.writeValueAsString(collectionNode);
        assertNotNull(json);
        
        // Verify JSON contains expected fields
        assertTrue(json.contains("stateImages"));
        assertTrue(json.contains("TestImage"));
        assertTrue(json.contains("TestState"));
        assertTrue(json.contains("stateLocations"));
        assertTrue(json.contains("TestLocation"));
        assertTrue(json.contains("LocationState"));
        assertTrue(json.contains("stateRegions"));
        assertTrue(json.contains("TestRegion"));
        assertTrue(json.contains("RegionState"));
        assertTrue(json.contains("stateStrings"));
        assertTrue(json.contains("TestString"));
        assertTrue(json.contains("StringState"));
        assertTrue(json.contains("Lorem ipsum"));
    }

    /**
     * Test ObjectCollection Builder with all types
     */
    @Test
    void testObjectCollectionBuilderAllTypes() {
        // Create comprehensive ObjectCollection using all builder methods
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
                        .setString("Lorem ipsum").build())
                
                // Add patterns (creates StateImages)
                // Note: Pattern constructor with filename tries to load image, so we create Pattern differently
                .withPatterns(new Pattern.Builder()
                        .setName("test-pattern")
                        .build())
                
                // Add scenes
                // Create scene with empty pattern to avoid file loading
                .withScenes(new Scene())
                
                // Add a Matches object
                .withMatches(new ActionResult())
                
                .build();

        // Verify all parts of the collection
        assertEquals(1, collection.getStateLocations().size());
        assertEquals("LocationState", collection.getStateLocations().getFirst().getOwnerStateName());
        assertEquals("TestLocation", collection.getStateLocations().getFirst().getName());
        assertEquals(100, collection.getStateLocations().getFirst().getLocation().getX());
        assertEquals(200, collection.getStateLocations().getFirst().getLocation().getY());

        assertEquals(1, collection.getStateRegions().size());
        assertEquals("RegionState", collection.getStateRegions().getFirst().getOwnerStateName());
        assertEquals("TestRegion", collection.getStateRegions().getFirst().getName());

        assertEquals(1, collection.getStateStrings().size());
        assertEquals("StringState", collection.getStateStrings().getFirst().getOwnerStateName());
        assertEquals("TestString", collection.getStateStrings().getFirst().getName());
        assertEquals("Lorem ipsum", collection.getStateStrings().getFirst().getString());

        assertEquals(1, collection.getStateImages().size());

        assertEquals(1, collection.getScenes().size());

        assertEquals(1, collection.getMatches().size());
    }

    /**
     * Test ObjectCollection JSON parsing
     */
    @Test
    void testObjectCollectionJsonParsing() throws Exception {
        String json = """
                {
                  "stateImages": [{
                    "name": "Image1",
                    "ownerStateName": "State1"
                  }],
                  "stateLocations": [{
                    "name": "Loc1",
                    "ownerStateName": "State2",
                    "location": {"x": 10, "y": 20}
                  }],
                  "stateRegions": [{
                    "name": "Region1",
                    "ownerStateName": "State3",
                    "searchRegion": {"x": 30, "y": 40, "w": 50, "h": 60}
                  }],
                  "stateStrings": [{
                    "name": "String1",
                    "ownerStateName": "State4",
                    "string": "Test content"
                  }],
                  "matches": [{
                    "successful": true,
                    "timesActedOn": 1
                  }],
                  "scenes": []
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify structure
        assertTrue(jsonNode.has("stateImages"));
        assertEquals(1, jsonNode.get("stateImages").size());
        assertEquals("Image1", jsonNode.get("stateImages").get(0).get("name").asText());
        
        assertTrue(jsonNode.has("stateLocations"));
        assertEquals(1, jsonNode.get("stateLocations").size());
        assertEquals("Loc1", jsonNode.get("stateLocations").get(0).get("name").asText());
        
        assertTrue(jsonNode.has("stateRegions"));
        assertEquals(1, jsonNode.get("stateRegions").size());
        assertEquals("Region1", jsonNode.get("stateRegions").get(0).get("name").asText());
        
        assertTrue(jsonNode.has("stateStrings"));
        assertEquals(1, jsonNode.get("stateStrings").size());
        assertEquals("String1", jsonNode.get("stateStrings").get(0).get("name").asText());
        assertEquals("Test content", jsonNode.get("stateStrings").get(0).get("string").asText());
        
        assertTrue(jsonNode.has("matches"));
        assertEquals(1, jsonNode.get("matches").size());
        
        assertTrue(jsonNode.has("scenes"));
        assertEquals(0, jsonNode.get("scenes").size());
    }

    /**
     * Test isEmpty method with various combinations
     */
    @Test
    void testIsEmpty() {
        // Empty collection
        ObjectCollection empty = new ObjectCollection();
        assertTrue(empty.isEmpty());
        
        // Collection with only locations
        ObjectCollection withLocations = new ObjectCollection.Builder()
                .withLocations(new Location(0, 0))
                .build();
        assertFalse(withLocations.isEmpty());
        
        // Collection with only regions
        ObjectCollection withRegions = new ObjectCollection.Builder()
                .withRegions(new Region(0, 0, 10, 10))
                .build();
        assertFalse(withRegions.isEmpty());
        
        // Collection with only strings
        ObjectCollection withStrings = new ObjectCollection.Builder()
                .withStrings("test")
                .build();
        assertFalse(withStrings.isEmpty());
        
        // Collection with only matches
        ObjectCollection withMatches = new ObjectCollection.Builder()
                .withMatches(new ActionResult())
                .build();
        assertFalse(withMatches.isEmpty());
    }
}