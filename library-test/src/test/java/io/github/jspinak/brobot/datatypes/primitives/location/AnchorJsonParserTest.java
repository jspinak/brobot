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
public class AnchorJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Anchor from JSON
     */
    @Test
    public void testParseBasicAnchor() throws ConfigurationException {
        String json = """
                {
                  "anchorInNewDefinedRegion": "TOPLEFT",
                  "positionInMatch": {
                    "percentW": 0.25,
                    "percentH": 0.75
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Anchor anchor = jsonParser.convertJson(jsonNode, Anchor.class);

        assertNotNull(anchor);
        assertEquals(Positions.Name.TOPLEFT, anchor.getAnchorInNewDefinedRegion());
        assertNotNull(anchor.getPositionInMatch());
        assertEquals(0.25, anchor.getPositionInMatch().getPercentW(), 0.001);
        assertEquals(0.75, anchor.getPositionInMatch().getPercentH(), 0.001);
    }

    /**
     * Test serializing and deserializing an Anchor
     */
    @Test
    public void testSerializeDeserializeAnchor() throws ConfigurationException {
        // Create an anchor
        Anchor anchor = new Anchor();
        anchor.setAnchorInNewDefinedRegion(Positions.Name.BOTTOMRIGHT);
        anchor.setPositionInMatch(new Position(0.75, 0.25));

        // Serialize
        String json = jsonUtils.toJsonSafe(anchor);
        System.out.println("DEBUG: Serialized Anchor: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Anchor deserializedAnchor = jsonParser.convertJson(jsonNode, Anchor.class);

        // Verify
        assertNotNull(deserializedAnchor);
        assertEquals(Positions.Name.BOTTOMRIGHT, deserializedAnchor.getAnchorInNewDefinedRegion());
        assertNotNull(deserializedAnchor.getPositionInMatch());
        assertEquals(0.75, deserializedAnchor.getPositionInMatch().getPercentW(), 0.001);
        assertEquals(0.25, deserializedAnchor.getPositionInMatch().getPercentH(), 0.001);
    }

    /**
     * Test anchor constructor with parameters
     */
    @Test
    public void testAnchorConstructor() throws ConfigurationException {
        // Create an anchor with constructor
        Position position = new Position(0.33, 0.67);
        Anchor anchor = new Anchor(Positions.Name.MIDDLERIGHT, position);

        // Serialize
        String json = jsonUtils.toJsonSafe(anchor);
        System.out.println("DEBUG: Serialized Anchor from constructor: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Anchor deserializedAnchor = jsonParser.convertJson(jsonNode, Anchor.class);

        // Verify
        assertNotNull(deserializedAnchor);
        assertEquals(Positions.Name.MIDDLERIGHT, deserializedAnchor.getAnchorInNewDefinedRegion());
        assertNotNull(deserializedAnchor.getPositionInMatch());
        assertEquals(0.33, deserializedAnchor.getPositionInMatch().getPercentW(), 0.001);
        assertEquals(0.67, deserializedAnchor.getPositionInMatch().getPercentH(), 0.001);
    }

    /**
     * Test equals method
     */
    @Test
    public void testEquals() {
        // Create two identical anchors
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5));
        Anchor anchor2 = new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5));

        // Should be equal
        assertTrue(anchor1.equals(anchor2));

        // Create a different anchor
        Anchor anchor3 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(0.5, 0.5));

        // Should not be equal
        assertFalse(anchor1.equals(anchor3));
    }

    /**
     * Test toString method
     */
    @Test
    public void testToString() {
        // Create an anchor
        Anchor anchor = new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5));

        // Get string representation
        String str = anchor.toString();

        // Verify it contains needed info
        assertTrue(str.contains("TOPLEFT"));
        assertTrue(str.contains("0.5"));
    }
}