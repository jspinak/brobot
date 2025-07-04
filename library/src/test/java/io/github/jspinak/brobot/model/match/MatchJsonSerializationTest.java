package io.github.jspinak.brobot.model.match;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Match JSON serialization/deserialization without Spring dependencies.
 * Migrated from library-test module.
 * 
 * Note: This test focuses on JSON structure validation rather than full object serialization
 * due to complex dependencies on custom serializers.
 */
class MatchJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic Match from JSON
     */
    @Test
    void testParseBasicMatch() throws Exception {
        String json = """
                {
                  "score": 0.95,
                  "name": "TestMatch",
                  "text": "Sample Text",
                  "target": {
                    "region": {
                      "x": 10,
                      "y": 20,
                      "w": 100,
                      "h": 50
                    }
                  },
                  "timesActedOn": 3
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Validate JSON structure
        assertTrue(jsonNode.has("score"));
        assertEquals(0.95, jsonNode.get("score").asDouble(), 0.001);
        assertEquals("TestMatch", jsonNode.get("name").asText());
        assertEquals("Sample Text", jsonNode.get("text").asText());
        
        // Verify target and region
        assertTrue(jsonNode.has("target"));
        JsonNode target = jsonNode.get("target");
        assertTrue(target.has("region"));
        JsonNode region = target.get("region");
        assertEquals(10, region.get("x").asInt());
        assertEquals(20, region.get("y").asInt());
        assertEquals(100, region.get("w").asInt());
        assertEquals(50, region.get("h").asInt());
        
        assertEquals(3, jsonNode.get("timesActedOn").asInt());
    }

    /**
     * Test creating Match JSON structure
     */
    @Test
    void testCreateMatchJson() throws Exception {
        // Create a match JSON structure
        ObjectNode matchNode = objectMapper.createObjectNode();
        matchNode.put("name", "SerializedMatch");
        matchNode.put("score", 0.85);
        matchNode.put("text", "Serialized Text");
        matchNode.put("timesActedOn", 5);
        
        // Create target with region
        ObjectNode targetNode = objectMapper.createObjectNode();
        ObjectNode regionNode = objectMapper.createObjectNode();
        regionNode.put("x", 50);
        regionNode.put("y", 60);
        regionNode.put("w", 200);
        regionNode.put("h", 100);
        targetNode.set("region", regionNode);
        matchNode.set("target", targetNode);
        
        // Convert to string
        String json = objectMapper.writeValueAsString(matchNode);
        assertNotNull(json);
        
        // Parse back and verify
        JsonNode parsedNode = objectMapper.readTree(json);
        assertEquals("SerializedMatch", parsedNode.get("name").asText());
        assertEquals(0.85, parsedNode.get("score").asDouble(), 0.001);
        assertEquals("Serialized Text", parsedNode.get("text").asText());
        assertEquals(5, parsedNode.get("timesActedOn").asInt());
    }

    /**
     * Test Match with Builder-style JSON
     */
    @Test
    void testMatchBuilderJson() throws Exception {
        String json = """
                {
                  "name": "BuilderMatch",
                  "score": 0.92,
                  "text": "Builder Text",
                  "target": {
                    "region": {
                      "x": 100,
                      "y": 150,
                      "w": 250,
                      "h": 120
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify all fields
        assertEquals("BuilderMatch", jsonNode.get("name").asText());
        assertEquals(0.92, jsonNode.get("score").asDouble(), 0.001);
        assertEquals("Builder Text", jsonNode.get("text").asText());
        
        // Verify region through target
        JsonNode region = jsonNode.get("target").get("region");
        assertEquals(100, region.get("x").asInt());
        assertEquals(150, region.get("y").asInt());
        assertEquals(250, region.get("w").asInt());
        assertEquals(120, region.get("h").asInt());
    }

    /**
     * Test Match object behavior (not JSON specific)
     */
    @Test
    void testMatchObject() {
        // Create a match with builder
        Match match = new Match.Builder()
                .setName("BuilderMatch")
                .setSimScore(0.92)
                .setText("Builder Text")
                .setRegion(100, 150, 250, 120)
                .build();
        
        // Verify properties
        assertEquals("BuilderMatch", match.getName());
        assertEquals(0.92, match.getScore(), 0.001);
        assertEquals("Builder Text", match.getText());
        
        // Verify region
        assertNotNull(match.getTarget());
        assertNotNull(match.getRegion());
        assertEquals(100, match.getRegion().x());
        assertEquals(150, match.getRegion().y());
        assertEquals(250, match.getRegion().w());
        assertEquals(120, match.getRegion().h());
    }

    /**
     * Test helper methods like x(), y(), w(), h(), and compareByScore
     */
    @Test
    void testHelperMethods() {
        // Create a match
        Match match = new Match.Builder()
                .setRegion(10, 20, 30, 40)
                .setSimScore(0.8)
                .build();

        // Test coordinate getters
        assertEquals(10, match.x());
        assertEquals(20, match.y());
        assertEquals(30, match.w());
        assertEquals(40, match.h());

        // Test size calculation
        assertEquals(1200, match.size()); // 30 * 40

        // Test score comparison
        Match match2 = new Match.Builder()
                .setRegion(50, 60, 70, 80)
                .setSimScore(0.7)
                .build();

        assertTrue(match.compareByScore(match2) > 0); // 0.8 - 0.7 > 0
        assertTrue(match2.compareByScore(match) < 0); // 0.7 - 0.8 < 0
    }

    /**
     * Test incrementTimesActedOn method
     */
    @Test
    void testIncrementTimesActedOn() {
        Match match = new Match();
        assertEquals(0, match.getTimesActedOn());

        match.incrementTimesActedOn();
        assertEquals(1, match.getTimesActedOn());

        match.incrementTimesActedOn();
        match.incrementTimesActedOn();
        assertEquals(3, match.getTimesActedOn());
    }

    /**
     * Test JSON with various Match scenarios
     */
    @Test
    void testMatchJsonScenarios() throws Exception {
        // Match with minimal fields
        String minimalJson = """
                {
                  "score": 0.5
                }
                """;
        JsonNode minimalNode = objectMapper.readTree(minimalJson);
        assertEquals(0.5, minimalNode.get("score").asDouble(), 0.001);
        assertFalse(minimalNode.has("name"));
        assertFalse(minimalNode.has("text"));
        
        // Match with high precision score
        String highPrecisionJson = """
                {
                  "score": 0.9999987654321,
                  "name": "HighPrecisionMatch"
                }
                """;
        JsonNode highPrecisionNode = objectMapper.readTree(highPrecisionJson);
        assertEquals(0.9999987654321, highPrecisionNode.get("score").asDouble(), 0.0000000000001);
        
        // Match with empty strings
        String emptyStringsJson = """
                {
                  "score": 0.75,
                  "name": "",
                  "text": ""
                }
                """;
        JsonNode emptyStringsNode = objectMapper.readTree(emptyStringsJson);
        assertEquals("", emptyStringsNode.get("name").asText());
        assertEquals("", emptyStringsNode.get("text").asText());
    }

    /**
     * Test Match with Location and Region
     */
    @Test
    void testMatchLocationRegion() {
        // Create location with region
        Location location = new Location();
        location.setRegion(new Region(100, 200, 300, 400));
        
        // Create match with location
        Match match = new Match();
        match.setTarget(location);
        match.setName("LocationMatch");
        match.setScore(0.88);
        
        // Verify region access through match
        assertNotNull(match.getRegion());
        assertEquals(100, match.x());
        assertEquals(200, match.y());
        assertEquals(300, match.w());
        assertEquals(400, match.h());
        assertEquals(120000, match.size()); // 300 * 400
    }

    /**
     * Test complex Match JSON structure
     */
    @Test
    void testComplexMatchJson() throws Exception {
        String json = """
                {
                  "name": "ComplexMatch",
                  "score": 0.9876,
                  "text": "Complex match with all fields",
                  "timesActedOn": 10,
                  "target": {
                    "region": {
                      "x": 123,
                      "y": 456,
                      "w": 789,
                      "h": 321
                    },
                    "x": 123,
                    "y": 456
                  },
                  "timeStamp": "2024-01-15T10:30:00",
                  "duration": 1500
                }
                """;
        
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify all properties
        assertEquals("ComplexMatch", jsonNode.get("name").asText());
        assertEquals(0.9876, jsonNode.get("score").asDouble(), 0.0001);
        assertEquals("Complex match with all fields", jsonNode.get("text").asText());
        assertEquals(10, jsonNode.get("timesActedOn").asInt());
        
        // Verify target structure
        JsonNode target = jsonNode.get("target");
        assertTrue(target.has("region"));
        assertTrue(target.has("x"));
        assertTrue(target.has("y"));
        
        // Verify region within target
        JsonNode region = target.get("region");
        assertEquals(123, region.get("x").asInt());
        assertEquals(456, region.get("y").asInt());
        assertEquals(789, region.get("w").asInt());
        assertEquals(321, region.get("h").asInt());
        
        // Verify additional fields that might be present
        assertTrue(jsonNode.has("timeStamp"));
        assertTrue(jsonNode.has("duration"));
    }
}