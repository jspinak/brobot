package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class AutomationFunctionJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a simple AutomationFunction from JSON
     */
    @Test
    public void testParseSimpleFunction() throws ConfigurationException {
        String json = """
                {
                  "id": 1,
                  "name": "simpleFunction",
                  "description": "A simple test function",
                  "returnType": "void",
                  "statements": []
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationFunction function = jsonParser.convertJson(jsonNode, AutomationFunction.class);

        assertNotNull(function);
        assertEquals(Integer.valueOf(1), function.getId());
        assertEquals("simpleFunction", function.getName());
        assertEquals("A simple test function", function.getDescription());
        assertEquals("void", function.getReturnType());
        assertNotNull(function.getStatements());
        assertEquals(0, function.getStatements().size());
    }

    /**
     * Test parsing a function with parameters
     */
    @Test
    public void testParseFunctionWithParameters() throws ConfigurationException {
        String json = """
                {
                  "id": 2,
                  "name": "functionWithParams",
                  "returnType": "boolean",
                  "parameters": [
                    {
                      "name": "inputText",
                      "type": "string"
                    },
                    {
                      "name": "maxAttempts",
                      "type": "int"
                    }
                  ],
                  "statements": []
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationFunction function = jsonParser.convertJson(jsonNode, AutomationFunction.class);

        assertNotNull(function);
        assertEquals("functionWithParams", function.getName());
        assertEquals("boolean", function.getReturnType());

        // Check parameters
        assertNotNull(function.getParameters());
        assertEquals(2, function.getParameters().size());

        Parameter param1 = function.getParameters().getFirst();
        assertEquals("inputText", param1.getName());
        assertEquals("string", param1.getType());

        Parameter param2 = function.getParameters().get(1);
        assertEquals("maxAttempts", param2.getName());
        assertEquals("int", param2.getType());
    }

    /**
     * Test parsing a function with statements
     */
    @Test
    public void testParseFunctionWithStatements() throws ConfigurationException {
        String json = """
                {
                  "id": 3,
                  "name": "functionWithStatements",
                  "returnType": "boolean",
                  "statements": [
                    {
                      "statementType": "variableDeclaration",
                      "name": "result",
                      "type": "boolean",
                      "value": {
                        "expressionType": "literal",
                        "valueType": "boolean",
                        "value": false
                      }
                    },
                    {
                      "statementType": "methodCall",
                      "method": "performAction",
                      "arguments": []
                    },
                    {
                      "statementType": "assignment",
                      "variable": "result",
                      "value": {
                        "expressionType": "literal",
                        "valueType": "boolean",
                        "value": true
                      }
                    },
                    {
                      "statementType": "return",
                      "value": {
                        "expressionType": "variable",
                        "name": "result"
                      }
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationFunction function = jsonParser.convertJson(jsonNode, AutomationFunction.class);

        assertNotNull(function);
        assertEquals("functionWithStatements", function.getName());

        // Check statements
        assertNotNull(function.getStatements());
        assertEquals(4, function.getStatements().size());

        // Verify statement types
        assertInstanceOf(VariableDeclarationStatement.class, function.getStatements().getFirst());
        assertInstanceOf(MethodCallStatement.class, function.getStatements().get(1));
        assertInstanceOf(AssignmentStatement.class, function.getStatements().get(2));
        assertInstanceOf(ReturnStatement.class, function.getStatements().get(3));

        // Check variable declaration
        VariableDeclarationStatement varDecl = (VariableDeclarationStatement) function.getStatements().getFirst();
        assertEquals("result", varDecl.getName());
        assertEquals("boolean", varDecl.getType());
        assertInstanceOf(LiteralExpression.class, varDecl.getValue());

        // Check return statement
        ReturnStatement returnStmt = (ReturnStatement) function.getStatements().get(3);
        assertInstanceOf(VariableExpression.class, returnStmt.getValue());
        assertEquals("result", ((VariableExpression) returnStmt.getValue()).getName());
    }

    /**
     * Test serialization and deserialization of AutomationFunction
     */
    @Test
    public void testSerializeDeserializeFunction() throws ConfigurationException {
        // Create a function
        AutomationFunction function = new AutomationFunction();
        function.setId(10);
        function.setName("testFunction");
        function.setDescription("Function for testing serialization");
        function.setReturnType("int");

        // Add parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter param = new Parameter();
        param.setName("value");
        param.setType("int");
        parameters.add(param);
        function.setParameters(parameters);

        // Add statements
        List<Statement> statements = new ArrayList<>();

        // Return statement
        ReturnStatement returnStmt = new ReturnStatement();
        returnStmt.setStatementType("return");

        // Create a literal for return value
        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("integer");
        literal.setValue(42);

        returnStmt.setValue(literal);
        statements.add(returnStmt);

        function.setStatements(statements);

        // Serialize
        String json = jsonUtils.toJsonSafe(function);
        System.out.println("DEBUG: Serialized AutomationFunction: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationFunction deserializedFunction = jsonParser.convertJson(jsonNode, AutomationFunction.class);

        // Verify structure
        assertNotNull(deserializedFunction);
        assertEquals(Integer.valueOf(10), deserializedFunction.getId());
        assertEquals("testFunction", deserializedFunction.getName());
        assertEquals("Function for testing serialization", deserializedFunction.getDescription());
        assertEquals("int", deserializedFunction.getReturnType());

        // Verify parameters
        assertNotNull(deserializedFunction.getParameters());
        assertEquals(1, deserializedFunction.getParameters().size());
        assertEquals("value", deserializedFunction.getParameters().getFirst().getName());
        assertEquals("int", deserializedFunction.getParameters().getFirst().getType());

        // Verify statements
        assertNotNull(deserializedFunction.getStatements());
        assertEquals(1, deserializedFunction.getStatements().size());
        assertInstanceOf(ReturnStatement.class, deserializedFunction.getStatements().getFirst());

        ReturnStatement deserializedReturn = (ReturnStatement) deserializedFunction.getStatements().getFirst();
        assertInstanceOf(LiteralExpression.class, deserializedReturn.getValue());

        LiteralExpression deserializedLiteral = (LiteralExpression) deserializedReturn.getValue();
        assertEquals("integer", deserializedLiteral.getValueType());
        assertEquals(42, ((Number) deserializedLiteral.getValue()).intValue());
    }
}