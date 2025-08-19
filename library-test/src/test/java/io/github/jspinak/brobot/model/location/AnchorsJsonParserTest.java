package io.github.jspinak.brobot.model.location;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.element.Anchor;
import io.github.jspinak.brobot.model.element.Position;
import io.github.jspinak.brobot.model.element.Anchors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AnchorsJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Anchors from JSON
     */
    @Test
    public void testParseBasicAnchors() throws ConfigurationException {
        String json = """
                {
                  "anchorList": [
                    {
                      "anchorInNewDefinedRegion": "TOPLEFT",
                      "positionInMatch": {
                        "percentW": 0.25,
                        "percentH": 0.75
                      }
                    },
                    {
                      "anchorInNewDefinedRegion": "BOTTOMRIGHT",
                      "positionInMatch": {
                        "percentW": 0.75,
                        "percentH": 0.25
                      }
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Anchors anchors = jsonParser.convertJson(jsonNode, Anchors.class);

        assertNotNull(anchors);
        assertNotNull(anchors.getAnchorList());
        assertEquals(2, anchors.getAnchorList().size());
        assertEquals(Positions.Name.TOPLEFT, anchors.getAnchorList().get(0).getAnchorInNewDefinedRegion());
        assertEquals(Positions.Name.BOTTOMRIGHT, anchors.getAnchorList().get(1).getAnchorInNewDefinedRegion());
    }

    /**
     * Test serializing and deserializing Anchors
     */
    @Test
    public void testSerializeDeserializeAnchors() throws ConfigurationException {
        // Create anchors
        Anchors anchors = new Anchors();

        // Add anchors
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0.25, 0.75));
        Anchor anchor2 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(0.75, 0.25));
        anchors.add(anchor1);
        anchors.add(anchor2);

        // Serialize
        String json = jsonUtils.toJsonSafe(anchors);
        System.out.println("DEBUG: Serialized Anchors: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Anchors deserializedAnchors = jsonParser.convertJson(jsonNode, Anchors.class);

        // Verify
        assertNotNull(deserializedAnchors);
        assertNotNull(deserializedAnchors.getAnchorList());
        assertEquals(2, deserializedAnchors.getAnchorList().size());
        assertEquals(Positions.Name.TOPLEFT, deserializedAnchors.getAnchorList().get(0).getAnchorInNewDefinedRegion());
        assertEquals(Positions.Name.BOTTOMRIGHT, deserializedAnchors.getAnchorList().get(1).getAnchorInNewDefinedRegion());
    }

    /**
     * Test add method
     */
    @Test
    public void testAddAnchor() {
        // Create anchors
        Anchors anchors = new Anchors();

        // Initially empty
        assertEquals(0, anchors.size());

        // Add an anchor
        Anchor anchor = new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5));
        anchors.add(anchor);

        // Verify it was added
        assertEquals(1, anchors.size());
        assertEquals(Positions.Name.TOPLEFT, anchors.getAnchorList().getFirst().getAnchorInNewDefinedRegion());
    }

    /**
     * Test size method
     */
    @Test
    public void testSize() {
        // Create anchors
        Anchors anchors = new Anchors();

        // Initially empty
        assertEquals(0, anchors.size());

        // Add anchors
        anchors.add(new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5)));
        anchors.add(new Anchor(Positions.Name.BOTTOMRIGHT, new Position(0.5, 0.5)));

        // Verify size
        assertEquals(2, anchors.size());
    }

    /**
     * Test toString method
     */
    @Test
    public void testToString() {
        // Create anchors
        Anchors anchors = new Anchors();

        // Add an anchor
        anchors.add(new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5)));

        // Get string representation
        String str = anchors.toString();

        // Verify it contains needed info
        assertTrue(str.contains("TOPLEFT"));
        assertTrue(str.contains("0,5") || str.contains("0.5")); // European and American representations
    }

    /**
     * Test equals method (auto-generated by Lombok)
     */
    @Test
    public void testEquals() {
        // Create two identical anchors lists
        Anchors anchors1 = new Anchors();
        anchors1.add(new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5)));

        Anchors anchors2 = new Anchors();
        anchors2.add(new Anchor(Positions.Name.TOPLEFT, new Position(0.5, 0.5)));

        // Should be equal
        assertEquals(anchors1, anchors2);

        // Add another anchor to one
        anchors1.add(new Anchor(Positions.Name.BOTTOMRIGHT, new Position(0.5, 0.5)));

        // Should no longer be equal
        assertNotEquals(anchors1, anchors2);
    }
}