package io.github.jspinak.brobot.dsl;

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
public class ParameterJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a Parameter from JSON
     */
    @Test
    public void testParseParameter() throws ConfigurationException {
        String json = """
                {
                  "name": "inputValue",
                  "type": "string"
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Parameter parameter = jsonParser.convertJson(jsonNode, Parameter.class);

        assertNotNull(parameter);
        assertEquals("inputValue", parameter.getName());
        assertEquals("string", parameter.getType());
    }

    /**
     * Test parsing all parameter types
     */
    @Test
    public void testParseAllParameterTypes() throws ConfigurationException {
        // Test all supported parameter types
        String[] types = {"boolean", "string", "int", "double", "region", "matches", "stateImage", "stateRegion", "object"};

        for (String type : types) {
            String json = String.format("""
                    {
                      "name": "param_%s",
                      "type": "%s"
                    }
                    """, type, type);

            JsonNode jsonNode = jsonParser.parseJson(json);
            Parameter parameter = jsonParser.convertJson(jsonNode, Parameter.class);

            assertNotNull(parameter);
            assertEquals("param_" + type, parameter.getName());
            assertEquals(type, parameter.getType());
        }
    }

    /**
     * Test serialization and deserialization of Parameter
     */
    @Test
    public void testSerializeDeserializeParameter() throws ConfigurationException {
        // Create a parameter
        Parameter parameter = new Parameter();
        parameter.setName("testParam");
        parameter.setType("double");

        // Serialize
        String json = jsonUtils.toJsonSafe(parameter);
        System.out.println("DEBUG: Serialized Parameter: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Parameter deserializedParam = jsonParser.convertJson(jsonNode, Parameter.class);

        // Verify
        assertNotNull(deserializedParam);
        assertEquals("testParam", deserializedParam.getName());
        assertEquals("double", deserializedParam.getType());
    }

    /**
     * Test with missing fields
     */
    @Test
    public void testMissingFields() throws ConfigurationException {
        String json = """
                {
                  "name": "partialParam"
                  // Missing type field
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Parameter parameter = jsonParser.convertJson(jsonNode, Parameter.class);

        assertNotNull(parameter);
        assertEquals("partialParam", parameter.getName());
        assertNull(parameter.getType());
    }
}