package io.github.jspinak.brobot.datatypes.primitives.image;

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
public class SceneJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Scene from JSON
     */
    @Test
    public void testParseBasicScene() throws ConfigurationException {
        String json = """
                {
                  "id": 123,
                  "pattern": {
                    "name": "ScenePattern",
                    "fixed": false
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Scene scene = jsonParser.convertJson(jsonNode, Scene.class);

        assertNotNull(scene);
        assertEquals(123L, scene.getId());
        assertNotNull(scene.getPattern());
        assertEquals("ScenePattern", scene.getPattern().getName());
        assertFalse(scene.getPattern().isFixed());
    }

    /**
     * Test serializing and deserializing a Scene
     */
    @Test
    public void testSerializeDeserializeScene() throws ConfigurationException {
        // Create a scene
        Pattern pattern = new Pattern();
        pattern.setName("TestScenePattern");

        Scene scene = new Scene(pattern);
        scene.setId(456L);

        // Serialize
        String json = jsonUtils.toJsonSafe(scene);
        System.out.println("DEBUG: Serialized Scene: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Scene deserializedScene = jsonParser.convertJson(jsonNode, Scene.class);

        // Verify
        assertNotNull(deserializedScene);
        assertEquals(456L, deserializedScene.getId());
        assertNotNull(deserializedScene.getPattern());
        assertEquals("TestScenePattern", deserializedScene.getPattern().getName());
    }

    /**
     * Test Scene constructor from filename
     */
    @Test
    public void testSceneFromFilename() throws ConfigurationException {
        // This test will fail in a real environment because the file doesn't exist
        // We're just testing the JSON serialization/deserialization

        // Create a scene from a filename
        Scene scene = new Scene("test/scene.png");
        scene.setId(789L);

        // Serialize
        String json = jsonUtils.toJsonSafe(scene);
        System.out.println("DEBUG: Serialized Scene from filename: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Scene deserializedScene = jsonParser.convertJson(jsonNode, Scene.class);

        // Verify
        assertNotNull(deserializedScene);
        assertEquals(789L, deserializedScene.getId());
        assertNotNull(deserializedScene.getPattern());
        // The pattern name might not be preserved exactly as the input filename
    }

    /**
     * Test toString method
     */
    @Test
    public void testToString() {
        // Create a scene
        Pattern pattern = new Pattern();
        pattern.setName("ToStringPattern");

        Scene scene = new Scene(pattern);
        scene.setId(999L);

        String result = scene.toString();

        assertNotNull(result);
        assertTrue(result.contains("999"));
        assertTrue(result.contains("ToStringPattern"));
    }
}