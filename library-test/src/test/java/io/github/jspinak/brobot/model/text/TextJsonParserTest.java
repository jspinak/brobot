package io.github.jspinak.brobot.model.text;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.element.Text;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class TextJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Text from JSON
     */
    @Test
    public void testParseBasicText() throws ConfigurationException {
        String json = """
                {
                  "strings": [
                    "First String",
                    "Second String",
                    "Third String"
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Text text = jsonParser.convertJson(jsonNode, Text.class);

        assertNotNull(text);
        assertEquals(3, text.size());
        assertEquals("First String", text.get(0));
        assertEquals("Second String", text.get(1));
        assertEquals("Third String", text.get(2));
    }

    /**
     * Test serializing and deserializing a Text
     */
    @Test
    public void testSerializeDeserializeText() throws ConfigurationException {
        // Create a text object
        Text text = new Text();
        text.add("String One");
        text.add("String Two");
        text.add("String Three");

        // Serialize
        String json = jsonUtils.toJsonSafe(text);
        System.out.println("DEBUG: Serialized Text: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Text deserializedText = jsonParser.convertJson(jsonNode, Text.class);

        // Verify
        assertNotNull(deserializedText);
        assertEquals(3, deserializedText.size());
        assertEquals("String One", deserializedText.get(0));
        assertEquals("String Two", deserializedText.get(1));
        assertEquals("String Three", deserializedText.get(2));
    }

    /**
     * Test the add, addAll, and getAll methods
     */
    @Test
    public void testAddMethods() {
        Text text1 = new Text();
        text1.add("First");
        text1.add("Second");

        assertEquals(2, text1.size());

        // Test getAll
        List<String> allStrings = text1.getAll();
        assertEquals(2, allStrings.size());
        assertEquals("First", allStrings.get(0));
        assertEquals("Second", allStrings.get(1));

        // Test addAll
        Text text2 = new Text();
        text2.add("Third");
        text2.add("Fourth");

        text1.addAll(text2);

        assertEquals(4, text1.size());
        assertEquals("First", text1.get(0));
        assertEquals("Second", text1.get(1));
        assertEquals("Third", text1.get(2));
        assertEquals("Fourth", text1.get(3));
    }

    /**
     * Test the isEmpty method
     */
    @Test
    public void testIsEmpty() {
        Text emptyText = new Text();
        assertTrue(emptyText.isEmpty());

        Text nonEmptyText = new Text();
        nonEmptyText.add("Not Empty");
        assertFalse(nonEmptyText.isEmpty());
    }
}