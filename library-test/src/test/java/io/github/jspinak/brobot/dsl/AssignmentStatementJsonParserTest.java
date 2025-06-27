package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class AssignmentStatementJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    private ObjectMapper jacksonMapper;

    @BeforeEach
    public void setup() {
        jacksonMapper = new ObjectMapper();
    }

    /**
     * Test parsing JSON into an AssignmentStatement with a variable expression.
     */
    @Test
    public void testParseWithVariableExpression() throws ConfigurationException {
        // Create sample JSON for AssignmentStatement with a variable expression
        String json = """
                {
                  "statementType": "assignment",
                  "variable": "testVar",
                  "value": {
                    "expressionType": "variable",
                    "name": "sourceVar"
                  }
                }
                """;

        // Parse JSON to AssignmentStatement
        JsonNode jsonNode = jsonParser.parseJson(json);

        // Print the JSON node for debugging
        System.out.println("DEBUG: JsonNode structure: " + jsonNode.toString());

        // Check if fields exist in the JSON
        assertTrue(jsonNode.has("variable"), "JSON should have 'variable' field");
        assertEquals("testVar", jsonNode.get("variable").asText(), "JSON 'variable' field should be 'testVar'");

        AssignmentStatement statement = jsonParser.convertJson(jsonNode, AssignmentStatement.class);

        // Add additional debug output
        System.out.println("DEBUG: AssignmentStatement after parsing: " + statement);
        System.out.println("DEBUG: Variable field value: " + statement.getVariable());

        // Verify the parsed AssignmentStatement
        assertNotNull(statement, "AssignmentStatement should not be null");
        assertEquals("testVar", statement.getVariable(), "Variable field should be 'testVar'");
        assertNotNull(statement.getValue(), "Value field should not be null");
    }

    /**
     * Test parsing JSON with a literal expression into an AssignmentStatement
     */
    @Test
    public void testParseWithLiteralExpression() throws ConfigurationException {
        // Create sample JSON for AssignmentStatement with a literal expression
        String json = """
                {
                  "statementType": "assignment",
                  "variable": "literalVar",
                  "value": {
                    "expressionType": "literal",
                    "valueType": "string",
                    "value": "Hello, World!"
                  }
                }
                """;

        // Parse JSON to AssignmentStatement
        JsonNode jsonNode = jsonParser.parseJson(json);
        AssignmentStatement statement = jsonParser.convertJson(jsonNode, AssignmentStatement.class);

        // Verify the parsed AssignmentStatement
        assertNotNull(statement);
        assertEquals("literalVar", statement.getVariable());
        assertNotNull(statement.getValue());
    }

    /**
     * Test with direct Jackson deserialization to compare with JsonParser
     */
    @Test
    public void testDirectDeserialization() throws Exception {
        String json = """
                {
                  "statementType": "assignment",
                  "variable": "directVar",
                  "value": {
                    "expressionType": "variable",
                    "name": "sourceVar"
                  }
                }
                """;

        // Try direct deserialization with standard Jackson
        AssignmentStatement statement = jacksonMapper.readValue(json, AssignmentStatement.class);

        // Verify
        assertNotNull(statement);
        assertEquals("directVar", statement.getVariable());
    }

    /**
     * Test serialization of a manually created AssignmentStatement
     */
    @Test
    public void testSerializeAssignmentStatement() throws ConfigurationException {
        // Create a statement manually
        AssignmentStatement statement = new AssignmentStatement();
        statement.setVariable("testVar");

        // Create a VariableExpression instead of a custom TestExpression
        VariableExpression varExpr = new VariableExpression();
        varExpr.setExpressionType("variable");
        varExpr.setName("sourceVar");
        statement.setValue(varExpr);

        // Serialize
        String json = jsonUtils.toJsonSafe(statement);
        System.out.println("DEBUG: Serialized JSON: " + json);

        // Verify serialized JSON contains expected fields
        JsonNode jsonNode = jsonParser.parseJson(json);
        assertTrue(jsonNode.has("variable"));
        assertEquals("testVar", jsonNode.get("variable").asText());
    }
}