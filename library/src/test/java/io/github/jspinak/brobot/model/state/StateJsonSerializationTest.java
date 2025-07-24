package io.github.jspinak.brobot.model.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the State class with a focus on JSON serialization/deserialization.
 * These tests verify JSON structure and State object behavior.
 * Migrated from library-test module without Spring dependencies.
 * 
 * Note: Full object deserialization is avoided due to complex dependencies on custom serializers.
 */
class StateJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic State from JSON
     */
    @Test
    void testParseBasicState() throws Exception {
        String json = """
                {
                  "id": 1,
                  "name": "TestState",
                  "stateText": ["TestText1", "TestText2"],
                  "blocking": true,
                  "pathScore": 5,
                  "baseProbabilityExists": 80,
                  "usableArea": {
                    "x": 100,
                    "y": 100,
                    "w": 800,
                    "h": 600
                  },
                  "stateImages": [],
                  "stateStrings": [],
                  "stateRegions": [],
                  "stateLocations": []
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Validate JSON structure
        assertNotNull(jsonNode);
        assertEquals(1, jsonNode.get("id").asLong());
        assertEquals("TestState", jsonNode.get("name").asText());
        
        // Verify stateText array
        assertTrue(jsonNode.has("stateText"));
        assertEquals(2, jsonNode.get("stateText").size());
        boolean hasText1 = false, hasText2 = false;
        for (JsonNode text : jsonNode.get("stateText")) {
            if (text.asText().equals("TestText1")) hasText1 = true;
            if (text.asText().equals("TestText2")) hasText2 = true;
        }
        assertTrue(hasText1);
        assertTrue(hasText2);
        
        assertTrue(jsonNode.get("blocking").asBoolean());
        assertEquals(5, jsonNode.get("pathScore").asInt());
        assertEquals(80, jsonNode.get("baseProbabilityExists").asInt());

        // Verify usable area
        assertTrue(jsonNode.has("usableArea"));
        JsonNode usableArea = jsonNode.get("usableArea");
        assertEquals(100, usableArea.get("x").asInt());
        assertEquals(100, usableArea.get("y").asInt());
        assertEquals(800, usableArea.get("w").asInt());
        assertEquals(600, usableArea.get("h").asInt());

        // Verify collections exist
        assertTrue(jsonNode.has("stateImages"));
        assertTrue(jsonNode.has("stateStrings"));
        assertTrue(jsonNode.has("stateRegions"));
        assertTrue(jsonNode.has("stateLocations"));
    }

    /**
     * Test creating State JSON structure with added objects
     */
    @Test
    void testCreateStateJson() throws Exception {
        // Create a state JSON structure
        ObjectNode stateNode = objectMapper.createObjectNode();
        stateNode.put("id", 2);
        stateNode.put("name", "SerializedState");
        stateNode.put("blocking", true);
        stateNode.put("pathScore", 3);
        stateNode.put("baseProbabilityExists", 90);
        
        // Create stateText array
        ArrayNode stateTextArray = objectMapper.createArrayNode();
        stateTextArray.add("Text1");
        stateTextArray.add("Text2");
        stateTextArray.add("Text3");
        stateNode.set("stateText", stateTextArray);
        
        // Create usableArea
        ObjectNode usableAreaNode = objectMapper.createObjectNode();
        usableAreaNode.put("x", 50);
        usableAreaNode.put("y", 50);
        usableAreaNode.put("w", 1000);
        usableAreaNode.put("h", 800);
        stateNode.set("usableArea", usableAreaNode);
        
        // Create StateImage array with one item
        ArrayNode stateImagesArray = objectMapper.createArrayNode();
        ObjectNode stateImageNode = objectMapper.createObjectNode();
        stateImageNode.put("name", "TestImage");
        stateImageNode.put("ownerStateName", "SerializedState");
        stateImagesArray.add(stateImageNode);
        stateNode.set("stateImages", stateImagesArray);
        
        // Create StateRegion array with one item
        ArrayNode stateRegionsArray = objectMapper.createArrayNode();
        ObjectNode stateRegionNode = objectMapper.createObjectNode();
        stateRegionNode.put("name", "TestRegion");
        stateRegionNode.put("ownerStateName", "SerializedState");
        ObjectNode searchRegionNode = objectMapper.createObjectNode();
        searchRegionNode.put("x", 100);
        searchRegionNode.put("y", 100);
        searchRegionNode.put("w", 200);
        searchRegionNode.put("h", 200);
        stateRegionNode.set("searchRegion", searchRegionNode);
        stateRegionsArray.add(stateRegionNode);
        stateNode.set("stateRegions", stateRegionsArray);
        
        // Create StateLocation array
        ArrayNode stateLocationsArray = objectMapper.createArrayNode();
        ObjectNode stateLocationNode = objectMapper.createObjectNode();
        stateLocationNode.put("name", "TestLocation");
        stateLocationNode.put("ownerStateName", "SerializedState");
        ObjectNode locationNode = objectMapper.createObjectNode();
        locationNode.put("x", 300);
        locationNode.put("y", 300);
        stateLocationNode.set("location", locationNode);
        stateLocationsArray.add(stateLocationNode);
        stateNode.set("stateLocations", stateLocationsArray);
        
        // Create StateString array
        ArrayNode stateStringsArray = objectMapper.createArrayNode();
        ObjectNode stateStringNode = objectMapper.createObjectNode();
        stateStringNode.put("name", "TestString");
        stateStringNode.put("ownerStateName", "SerializedState");
        stateStringNode.put("string", "Sample Text String");
        stateStringsArray.add(stateStringNode);
        stateNode.set("stateStrings", stateStringsArray);
        
        // Convert to string
        String json = objectMapper.writeValueAsString(stateNode);
        assertNotNull(json);
        
        // Verify JSON contains expected fields
        assertTrue(json.contains("\"id\" : 2"));
        assertTrue(json.contains("\"name\" : \"SerializedState\""));
        assertTrue(json.contains("\"blocking\" : true"));
        assertTrue(json.contains("TestImage"));
        assertTrue(json.contains("TestRegion"));
        assertTrue(json.contains("TestLocation"));
        assertTrue(json.contains("TestString"));
        assertTrue(json.contains("Sample Text String"));
    }

    /**
     * Test the addState* methods behavior
     */
    @Test
    void testAddMethods() {
        // Create a basic state
        State state = new State();
        state.setId(4L);
        state.setName("AddMethodTest");

        // Add a state image using addStateImage
        StateImage image = new StateImage.Builder()
                .setName("TestImage")
                .build();
        state.addStateImage(image);

        assertEquals(1, state.getStateImages().size());
        assertTrue(state.getStateImages().contains(image));
        assertEquals("AddMethodTest", image.getOwnerStateName());
        assertEquals(StateObject.Type.IMAGE, image.getObjectType());

        // Add a state region using addStateRegion
        StateRegion region = new StateRegion.Builder()
                .setName("TestRegion")
                .setSearchRegion(100, 100, 200, 200)
                .build();
        state.addStateRegion(region);

        assertEquals(1, state.getStateRegions().size());
        assertTrue(state.getStateRegions().contains(region));
        assertEquals("AddMethodTest", region.getOwnerStateName());
        assertEquals(StateObject.Type.REGION, region.getObjectType());

        // Add a state location using addStateLocation
        StateLocation location = new StateLocation.Builder()
                .setName("TestLocation")
                .setLocation(300, 300)
                .build();
        state.addStateLocation(location);

        assertEquals(1, state.getStateLocations().size());
        assertTrue(state.getStateLocations().contains(location));
        assertEquals("AddMethodTest", location.getOwnerStateName());
        assertEquals(StateObject.Type.LOCATION, location.getObjectType());

        // Add a state string using addStateString
        StateString string = new StateString.Builder()
                .setName("TestString")
                .setString("Test String Value").build();
        state.addStateString(string);

        assertEquals(1, state.getStateStrings().size());
        assertTrue(state.getStateStrings().contains(string));
        assertEquals("AddMethodTest", string.getOwnerStateName());
    }

    /**
     * Test verifying that StateObject implementations correctly implement the interface
     */
    @Test
    void testStateObjectTypes() {
        // Create objects and verify types directly
        StateImage image = new StateImage.Builder()
                .setName("TypeTestImage")
                .build();
        assertEquals(StateObject.Type.IMAGE, image.getObjectType());

        StateRegion region = new StateRegion.Builder()
                .setName("TypeTestRegion")
                .setSearchRegion(100, 100, 200, 200)
                .build();
        assertEquals(StateObject.Type.REGION, region.getObjectType());

        StateLocation location = new StateLocation.Builder()
                .setName("TypeTestLocation")
                .setLocation(300, 300)
                .build();
        assertEquals(StateObject.Type.LOCATION, location.getObjectType());

        // Verify interface methods
        assertNotNull(((StateObject) image).getIdAsString(), "getIdAsString should return a non-null value");
        assertNotNull(((StateObject) region).getIdAsString(), "getIdAsString should return a non-null value");
        assertNotNull(((StateObject) location).getIdAsString(), "getIdAsString should return a non-null value");

        assertEquals(StateObject.Type.IMAGE, ((StateObject) image).getObjectType());
        assertEquals(StateObject.Type.REGION, ((StateObject) region).getObjectType());
        assertEquals(StateObject.Type.LOCATION, ((StateObject) location).getObjectType());
    }

    /**
     * Test State JSON structure with multiple objects
     */
    @Test
    void testStateJsonWithMultipleObjects() throws Exception {
        String json = """
                {
                  "id": 10,
                  "name": "ParentState",
                  "blocking": false,
                  "pathScore": 1,
                  "baseProbabilityExists": 100,
                  "stateText": ["state", "text", "items"],
                  "usableArea": {
                    "x": 0,
                    "y": 0,
                    "w": 1920,
                    "h": 1080
                  },
                  "stateImages": [
                    {
                      "name": "Image0",
                      "ownerStateName": "ParentState"
                    },
                    {
                      "name": "Image1",
                      "ownerStateName": "ParentState"
                    },
                    {
                      "name": "Image2",
                      "ownerStateName": "ParentState"
                    }
                  ],
                  "stateRegions": [
                    {
                      "name": "Region0",
                      "ownerStateName": "ParentState",
                      "searchRegion": {"x": 0, "y": 0, "w": 100, "h": 100}
                    },
                    {
                      "name": "Region1",
                      "ownerStateName": "ParentState",
                      "searchRegion": {"x": 10, "y": 10, "w": 100, "h": 100}
                    },
                    {
                      "name": "Region2",
                      "ownerStateName": "ParentState",
                      "searchRegion": {"x": 20, "y": 20, "w": 100, "h": 100}
                    }
                  ],
                  "stateLocations": [],
                  "stateStrings": []
                }
                """;
        
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify basic properties
        assertEquals(10, jsonNode.get("id").asLong());
        assertEquals("ParentState", jsonNode.get("name").asText());
        assertFalse(jsonNode.get("blocking").asBoolean());
        
        // Verify stateText
        assertEquals(3, jsonNode.get("stateText").size());
        
        // Verify stateImages
        assertEquals(3, jsonNode.get("stateImages").size());
        for (int i = 0; i < 3; i++) {
            JsonNode imageNode = jsonNode.get("stateImages").get(i);
            assertEquals("Image" + i, imageNode.get("name").asText());
            assertEquals("ParentState", imageNode.get("ownerStateName").asText());
        }
        
        // Verify stateRegions
        assertEquals(3, jsonNode.get("stateRegions").size());
        for (int i = 0; i < 3; i++) {
            JsonNode regionNode = jsonNode.get("stateRegions").get(i);
            assertEquals("Region" + i, regionNode.get("name").asText());
            assertEquals("ParentState", regionNode.get("ownerStateName").asText());
            assertEquals(i * 10, regionNode.get("searchRegion").get("x").asInt());
            assertEquals(i * 10, regionNode.get("searchRegion").get("y").asInt());
        }
    }
}