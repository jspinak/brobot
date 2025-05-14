package io.github.jspinak.brobot.datatypes.primitives.location;

import com.fasterxml.jackson.databind.JsonNode;
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
public class PositionJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Position from JSON
     */
    @Test
    public void testParseBasicPosition() throws ConfigurationException {
        String json = """
                {
                  "percentW": 0.25,
                  "percentH": 0.75
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Position position = jsonParser.convertJson(jsonNode, Position.class);

        assertNotNull(position);
        assertEquals(0.25, position.getPercentW(), 0.001);
        assertEquals(0.75, position.getPercentH(), 0.001);
    }

    /**
     * Test serializing and deserializing a Position
     */
    @Test
    public void testSerializeDeserializePosition() throws ConfigurationException {
        // Create a position
        Position position = new Position(0.33, 0.67);

        // Serialize
        String json = jsonUtils.toJsonSafe(position);
        System.out.println("DEBUG: Serialized Position: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Position deserializedPosition = jsonParser.convertJson(jsonNode, Position.class);

        // Verify
        assertNotNull(deserializedPosition);
        assertEquals(0.33, deserializedPosition.getPercentW(), 0.001);
        assertEquals(0.67, deserializedPosition.getPercentH(), 0.001);
    }

    /**
     * Test various constructors
     */
    @Test
    public void testPositionConstructors() {
        // Test default constructor
        Position pos1 = new Position();
        assertEquals(0.5, pos1.getPercentW(), 0.001); // Default is center
        assertEquals(0.5, pos1.getPercentH(), 0.001);

        // Test constructor with double values
        Position pos2 = new Position(0.25, 0.75);
        assertEquals(0.25, pos2.getPercentW(), 0.001);
        assertEquals(0.75, pos2.getPercentH(), 0.001);

        // Test constructor with integer percentages
        Position pos3 = new Position(25, 75);
        assertEquals(0.25, pos3.getPercentW(), 0.001);
        assertEquals(0.75, pos3.getPercentH(), 0.001);

        // Test constructor with position name
        Position pos4 = new Position(Positions.Name.TOPLEFT);
        assertEquals(0.0, pos4.getPercentW(), 0.001);
        assertEquals(0.0, pos4.getPercentH(), 0.001);

        // Test clone constructor
        Position pos5 = new Position(pos2);
        assertEquals(0.25, pos5.getPercentW(), 0.001);
        assertEquals(0.75, pos5.getPercentH(), 0.001);
    }

    /**
     * Test the add and multiply methods
     */
    @Test
    public void testAddAndMultiply() {
        // Create a position
        Position position = new Position(0.25, 0.75);

        // Test add methods
        position.addPercentW(0.1);
        position.addPercentH(-0.1);
        assertEquals(0.35, position.getPercentW(), 0.001);
        assertEquals(0.65, position.getPercentH(), 0.001);

        // Test multiply methods
        position.multiplyPercentW(2.0);
        position.multiplyPercentH(0.5);
        assertEquals(0.7, position.getPercentW(), 0.001);
        assertEquals(0.325, position.getPercentH(), 0.001);
    }

    /**
     * Test the equals method
     */
    @Test
    public void testEquals() {
        // Create two identical positions
        Position pos1 = new Position(0.25, 0.75);
        Position pos2 = new Position(0.25, 0.75);

        // Should be equal
        assertTrue(pos1.equals(pos2));

        // Create a different position
        Position pos3 = new Position(0.75, 0.25);

        // Should not be equal
        assertFalse(pos1.equals(pos3));
    }

    /**
     * Test the toString method
     */
    @Test
    public void testToString() {
        // Create a position
        Position position = new Position(0.25, 0.75);

        // Get string representation
        String str = position.toString();

        // Verify it contains needed info
        assertTrue(str.contains("0.3"));
        assertTrue(str.contains("0.8"));
    }
}