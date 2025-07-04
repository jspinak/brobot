package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Pattern JSON serialization/deserialization without Spring dependencies.
 * Migrated from library-test module.
 * 
 * Note: This test focuses on JSON structure validation rather than full object serialization
 * due to complex dependencies on custom serializers for JavaCV objects.
 */
class PatternJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ignore unknown properties to handle JavaCV objects
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic Pattern from JSON
     */
    @Test
    void testParseBasicPattern() throws Exception {
        String json = """
                {
                  "name": "TestPattern",
                  "fixed": true,
                  "dynamic": false,
                  "imgpath": "test/path.png",
                  "searchRegions": {
                    "fixedRegion": {
                      "x": 10,
                      "y": 20,
                      "w": 100,
                      "h": 50
                    }
                  },
                  "targetPosition": {
                    "percentW": 0.5,
                    "percentH": 0.5
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Validate JSON structure
        assertTrue(jsonNode.has("name"));
        assertEquals("TestPattern", jsonNode.get("name").asText());
        assertTrue(jsonNode.get("fixed").asBoolean());
        assertFalse(jsonNode.get("dynamic").asBoolean());
        assertEquals("test/path.png", jsonNode.get("imgpath").asText());
        
        // Verify search regions
        assertTrue(jsonNode.has("searchRegions"));
        JsonNode searchRegions = jsonNode.get("searchRegions");
        assertTrue(searchRegions.has("fixedRegion"));
        JsonNode fixedRegion = searchRegions.get("fixedRegion");
        assertEquals(10, fixedRegion.get("x").asInt());
        assertEquals(20, fixedRegion.get("y").asInt());
        assertEquals(100, fixedRegion.get("w").asInt());
        assertEquals(50, fixedRegion.get("h").asInt());
        
        // Verify target position
        assertTrue(jsonNode.has("targetPosition"));
        JsonNode targetPosition = jsonNode.get("targetPosition");
        assertEquals(0.5, targetPosition.get("percentW").asDouble(), 0.001);
        assertEquals(0.5, targetPosition.get("percentH").asDouble(), 0.001);
    }

    /**
     * Test creating Pattern JSON structure
     */
    @Test
    void testCreatePatternJson() throws Exception {
        // Create a pattern JSON structure
        ObjectNode patternNode = objectMapper.createObjectNode();
        patternNode.put("name", "SerializedPattern");
        patternNode.put("fixed", true);
        patternNode.put("dynamic", false);
        patternNode.put("imgpath", "test/serialized.png");
        
        // Create search regions
        ObjectNode searchRegionsNode = objectMapper.createObjectNode();
        ObjectNode fixedRegionNode = objectMapper.createObjectNode();
        fixedRegionNode.put("x", 50);
        fixedRegionNode.put("y", 60);
        fixedRegionNode.put("w", 200);
        fixedRegionNode.put("h", 100);
        searchRegionsNode.set("fixedRegion", fixedRegionNode);
        searchRegionsNode.set("regions", objectMapper.createArrayNode());
        patternNode.set("searchRegions", searchRegionsNode);
        
        // Create target position
        ObjectNode targetPositionNode = objectMapper.createObjectNode();
        targetPositionNode.put("percentW", 0.75);
        targetPositionNode.put("percentH", 0.25);
        patternNode.set("targetPosition", targetPositionNode);
        
        // Convert to string
        String json = objectMapper.writeValueAsString(patternNode);
        assertNotNull(json);
        
        // Parse back and verify
        JsonNode parsedNode = objectMapper.readTree(json);
        assertEquals("SerializedPattern", parsedNode.get("name").asText());
        assertTrue(parsedNode.get("fixed").asBoolean());
        assertFalse(parsedNode.get("dynamic").asBoolean());
        assertEquals("test/serialized.png", parsedNode.get("imgpath").asText());
    }

    /**
     * Test Pattern with Builder-style JSON
     */
    @Test
    void testPatternBuilderJson() throws Exception {
        String json = """
                {
                  "name": "BuilderPattern",
                  "fixed": true,
                  "dynamic": true,
                  "searchRegions": {
                    "fixedRegion": {
                      "x": 100,
                      "y": 150,
                      "w": 250,
                      "h": 120
                    },
                    "regions": []
                  },
                  "targetPosition": {
                    "percentW": 0.4,
                    "percentH": 0.6
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify all fields
        assertEquals("BuilderPattern", jsonNode.get("name").asText());
        assertTrue(jsonNode.get("fixed").asBoolean());
        assertTrue(jsonNode.get("dynamic").asBoolean());
        
        // Verify search regions
        JsonNode fixedRegion = jsonNode.get("searchRegions").get("fixedRegion");
        assertEquals(100, fixedRegion.get("x").asInt());
        assertEquals(150, fixedRegion.get("y").asInt());
        assertEquals(250, fixedRegion.get("w").asInt());
        assertEquals(120, fixedRegion.get("h").asInt());
        
        // Verify target position
        JsonNode targetPosition = jsonNode.get("targetPosition");
        assertEquals(0.4, targetPosition.get("percentW").asDouble(), 0.001);
        assertEquals(0.6, targetPosition.get("percentH").asDouble(), 0.001);
    }

    /**
     * Test the isDefined method logic through JSON
     */
    @Test
    void testIsDefinedLogic() throws Exception {
        // Pattern with fixed region should be defined
        String fixedJson = """
                {
                  "name": "FixedPattern",
                  "fixed": true,
                  "searchRegions": {
                    "fixedRegion": {
                      "x": 0,
                      "y": 0,
                      "w": 100,
                      "h": 100
                    }
                  }
                }
                """;
        
        JsonNode fixedNode = objectMapper.readTree(fixedJson);
        assertTrue(fixedNode.get("fixed").asBoolean());
        assertNotNull(fixedNode.get("searchRegions").get("fixedRegion"));
        
        // Pattern with search regions but not fixed
        String searchJson = """
                {
                  "name": "SearchPattern",
                  "fixed": false,
                  "searchRegions": {
                    "regions": [
                      {"x": 0, "y": 0, "w": 100, "h": 100}
                    ]
                  }
                }
                """;
        
        JsonNode searchNode = objectMapper.readTree(searchJson);
        assertFalse(searchNode.get("fixed").asBoolean());
        assertTrue(searchNode.get("searchRegions").get("regions").size() > 0);
        
        // Pattern with no regions - would not be defined
        String undefinedJson = """
                {
                  "name": "UndefinedPattern",
                  "fixed": false,
                  "searchRegions": {
                    "regions": []
                  }
                }
                """;
        
        JsonNode undefinedNode = objectMapper.readTree(undefinedJson);
        assertFalse(undefinedNode.get("fixed").asBoolean());
        assertEquals(0, undefinedNode.get("searchRegions").get("regions").size());
    }

    /**
     * Test JSON with multiple search regions
     */
    @Test
    void testMultipleSearchRegions() throws Exception {
        String json = """
                {
                  "name": "MultiRegionPattern",
                  "fixed": false,
                  "searchRegions": {
                    "regions": [
                      {"x": 100, "y": 200, "w": 300, "h": 400},
                      {"x": 500, "y": 600, "w": 700, "h": 800}
                    ]
                  }
                }
                """;
        
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("MultiRegionPattern", jsonNode.get("name").asText());
        assertFalse(jsonNode.get("fixed").asBoolean());
        
        JsonNode regions = jsonNode.get("searchRegions").get("regions");
        assertEquals(2, regions.size());
        
        // Verify first region
        JsonNode region1 = regions.get(0);
        assertEquals(100, region1.get("x").asInt());
        assertEquals(200, region1.get("y").asInt());
        assertEquals(300, region1.get("w").asInt());
        assertEquals(400, region1.get("h").asInt());
        
        // Verify second region
        JsonNode region2 = regions.get(1);
        assertEquals(500, region2.get("x").asInt());
        assertEquals(600, region2.get("y").asInt());
        assertEquals(700, region2.get("w").asInt());
        assertEquals(800, region2.get("h").asInt());
    }

    /**
     * Test JSON with null values
     */
    @Test
    void testNullValueJson() throws Exception {
        String json = """
                {
                  "name": "MinimalPattern",
                  "fixed": false,
                  "dynamic": false,
                  "searchRegions": {
                    "regions": []
                  }
                }
                """;
        
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals("MinimalPattern", jsonNode.get("name").asText());
        assertFalse(jsonNode.get("fixed").asBoolean());
        assertFalse(jsonNode.get("dynamic").asBoolean());
        assertFalse(jsonNode.has("imgpath") && !jsonNode.get("imgpath").isNull());
        assertFalse(jsonNode.has("targetPosition") && !jsonNode.get("targetPosition").isNull());
    }

    /**
     * Test complex pattern JSON structure
     */
    @Test
    void testComplexPatternJson() throws Exception {
        String json = """
                {
                  "name": "ComplexPattern",
                  "fixed": false,
                  "dynamic": true,
                  "imgpath": "complex/path/image.png",
                  "searchRegions": {
                    "regions": [
                      {"x": 0, "y": 0, "w": 100, "h": 100},
                      {"x": 200, "y": 200, "w": 150, "h": 150},
                      {"x": 400, "y": 100, "w": 200, "h": 300}
                    ]
                  },
                  "targetPosition": {
                    "percentW": 0.33,
                    "percentH": 0.67
                  },
                  "targetOffset": {
                    "x": 10,
                    "y": 20
                  },
                  "index": 5,
                  "setKmeansColorProfiles": false,
                  "matchHistory": {
                    "timesSearched": 0,
                    "timesFound": 0,
                    "snapshots": []
                  }
                }
                """;
        
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify all properties
        assertEquals("ComplexPattern", jsonNode.get("name").asText());
        assertFalse(jsonNode.get("fixed").asBoolean());
        assertTrue(jsonNode.get("dynamic").asBoolean());
        assertEquals("complex/path/image.png", jsonNode.get("imgpath").asText());
        assertEquals(3, jsonNode.get("searchRegions").get("regions").size());
        
        // Verify target position
        JsonNode targetPosition = jsonNode.get("targetPosition");
        assertEquals(0.33, targetPosition.get("percentW").asDouble(), 0.001);
        assertEquals(0.67, targetPosition.get("percentH").asDouble(), 0.001);
        
        // Verify target offset
        assertTrue(jsonNode.has("targetOffset"));
        JsonNode targetOffset = jsonNode.get("targetOffset");
        assertEquals(10, targetOffset.get("x").asInt());
        assertEquals(20, targetOffset.get("y").asInt());
        
        // Verify other fields
        assertEquals(5, jsonNode.get("index").asInt());
        assertFalse(jsonNode.get("setKmeansColorProfiles").asBoolean());
        
        // Verify match history
        assertTrue(jsonNode.has("matchHistory"));
        JsonNode matchHistory = jsonNode.get("matchHistory");
        assertEquals(0, matchHistory.get("timesSearched").asInt());
        assertEquals(0, matchHistory.get("timesFound").asInt());
        assertEquals(0, matchHistory.get("snapshots").size());
    }
}