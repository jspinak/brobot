package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement;
import io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement;
import io.github.jspinak.brobot.runner.dsl.statements.ReturnStatement;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AutomationFunctionJsonParserTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a simple AutomationFunction from JSON
     */
    @Test
    public void testParseSimpleFunction() throws Exception {
        String json = """
                {
                  "id": 1,
                  "name": "simpleFunction",
                  "description": "A simple test function",
                  "returnType": "void",
                  "statements": []
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        BusinessTask function = objectMapper.treeToValue(jsonNode, BusinessTask.class);

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
    public void testParseFunctionWithParameters() throws Exception {
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

        JsonNode jsonNode = objectMapper.readTree(json);
        BusinessTask function = objectMapper.treeToValue(jsonNode, BusinessTask.class);

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
    public void testParseFunctionWithStatements() throws Exception {
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

        JsonNode jsonNode = objectMapper.readTree(json);
        BusinessTask function = objectMapper.treeToValue(jsonNode, BusinessTask.class);

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
    public void testSerializeDeserializeFunction() throws Exception {
        // Create a function
        BusinessTask function = new BusinessTask();
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
        String json = objectMapper.writeValueAsString(function);
        System.out.println("DEBUG: Serialized AutomationFunction: " + json);

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        BusinessTask deserializedFunction = objectMapper.treeToValue(jsonNode, BusinessTask.class);

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