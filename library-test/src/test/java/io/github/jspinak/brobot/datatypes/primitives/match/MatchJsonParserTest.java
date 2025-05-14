package io.github.jspinak.brobot.datatypes.primitives.match;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class MatchJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Match from JSON
     */
    @Test
    public void testParseBasicMatch() throws ConfigurationException {
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

        JsonNode jsonNode = jsonParser.parseJson(json);
        Match match = jsonParser.convertJson(jsonNode, Match.class);

        assertNotNull(match);
        assertEquals(0.95, match.getScore(), 0.001);
        assertEquals("TestMatch", match.getName());
        assertEquals("Sample Text", match.getText());

        // Verify region
        assertNotNull(match.getTarget());
        assertNotNull(match.getRegion());
        assertEquals(10, match.getRegion().x());
        assertEquals(20, match.getRegion().y());
        assertEquals(100, match.getRegion().w());
        assertEquals(50, match.getRegion().h());

        assertEquals(3, match.getTimesActedOn());
    }

    /**
     * Test serializing and deserializing a Match
     */
    @Test
    public void testSerializeDeserializeMatch() throws ConfigurationException {
        // Create a match
        Match match = new Match();
        match.setName("SerializedMatch");
        match.setScore(0.85);
        match.setText("Serialized Text");

        Location location = new Location();
        location.setRegion(new Region(50, 60, 200, 100));
        match.setTarget(location);

        match.setTimesActedOn(5);

        // Serialize
        String json = jsonUtils.toJsonSafe(match);
        System.out.println("DEBUG: Serialized Match: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Match deserializedMatch = jsonParser.convertJson(jsonNode, Match.class);

        // Verify
        assertNotNull(deserializedMatch);
        assertEquals("SerializedMatch", deserializedMatch.getName());
        assertEquals(0.85, deserializedMatch.getScore(), 0.001);
        assertEquals("Serialized Text", deserializedMatch.getText());

        // Verify region
        assertNotNull(deserializedMatch.getTarget());
        assertNotNull(deserializedMatch.getRegion());
        assertEquals(50, deserializedMatch.getRegion().x());
        assertEquals(60, deserializedMatch.getRegion().y());
        assertEquals(200, deserializedMatch.getRegion().w());
        assertEquals(100, deserializedMatch.getRegion().h());

        assertEquals(5, deserializedMatch.getTimesActedOn());
    }

    /**
     * Test the Builder pattern
     */
    @Test
    public void testMatchBuilder() throws ConfigurationException {
        // Create a match with builder
        Match match = new Match.Builder()
                .setName("BuilderMatch")
                .setSimScore(0.92)
                .setText("Builder Text")
                .setRegion(100, 150, 250, 120)
                .build();

        // Serialize
        String json = jsonUtils.toJsonSafe(match);
        System.out.println("DEBUG: Serialized Builder Match: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Match deserializedMatch = jsonParser.convertJson(jsonNode, Match.class);

        // Verify
        assertNotNull(deserializedMatch);
        assertEquals("BuilderMatch", deserializedMatch.getName());
        assertEquals(0.92, deserializedMatch.getScore(), 0.001);
        assertEquals("Builder Text", deserializedMatch.getText());

        // Verify region
        assertNotNull(deserializedMatch.getTarget());
        assertNotNull(deserializedMatch.getRegion());
        assertEquals(100, deserializedMatch.getRegion().x());
        assertEquals(150, deserializedMatch.getRegion().y());
        assertEquals(250, deserializedMatch.getRegion().w());
        assertEquals(120, deserializedMatch.getRegion().h());
    }

    /**
     * Test helper methods like x(), y(), w(), h(), and compareByScore
     */
    @Test
    public void testHelperMethods() {
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
    public void testIncrementTimesActedOn() {
        Match match = new Match();
        assertEquals(0, match.getTimesActedOn());

        match.incrementTimesActedOn();
        assertEquals(1, match.getTimesActedOn());

        match.incrementTimesActedOn();
        match.incrementTimesActedOn();
        assertEquals(3, match.getTimesActedOn());
    }
}