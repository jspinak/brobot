package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement;
import io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement;
import io.github.jspinak.brobot.runner.dsl.statements.ReturnStatement;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BusinessTask (AutomationFunction) JSON parsing.
 * 
 * Key points:
 * - BusinessTask represents a single automation function
 * - Contains parameters, statements, and return type
 * - Statements use polymorphic deserialization with statementType
 */
@DisplayName("AutomationFunction JSON Parser Tests")
class AutomationFunctionJsonParserTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @DisplayName("Should parse simple BusinessTask from JSON")
    void testParseSimpleFunction() throws Exception {
        String json = """
        {
            "id": 1,
            "name": "simpleFunction",
            "description": "A simple test function",
            "returnType": "void",
            "statements": []
        }
        """;

        BusinessTask function = objectMapper.readValue(json, BusinessTask.class);

        assertNotNull(function);
        assertEquals(Integer.valueOf(1), function.getId());
        assertEquals("simpleFunction", function.getName());
        assertEquals("A simple test function", function.getDescription());
        assertEquals("void", function.getReturnType());
        assertNotNull(function.getStatements());
        assertEquals(0, function.getStatements().size());
    }

    @Test
    @DisplayName("Should parse BusinessTask with parameters")
    void testParseFunctionWithParameters() throws Exception {
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

        BusinessTask function = objectMapper.readValue(json, BusinessTask.class);

        assertNotNull(function);
        assertEquals("functionWithParams", function.getName());
        assertEquals("boolean", function.getReturnType());

        // Check parameters
        assertNotNull(function.getParameters());
        assertEquals(2, function.getParameters().size());

        Parameter param1 = function.getParameters().get(0);
        assertEquals("inputText", param1.getName());
        assertEquals("string", param1.getType());

        Parameter param2 = function.getParameters().get(1);
        assertEquals("maxAttempts", param2.getName());
        assertEquals("int", param2.getType());
    }

    @Test
    @DisplayName("Should parse BusinessTask with statements")
    void testParseFunctionWithStatements() throws Exception {
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

        BusinessTask function = objectMapper.readValue(json, BusinessTask.class);

        assertNotNull(function);
        assertEquals("functionWithStatements", function.getName());

        // Check statements
        assertNotNull(function.getStatements());
        assertEquals(4, function.getStatements().size());

        // Verify statement types
        assertTrue(function.getStatements().get(0) instanceof VariableDeclarationStatement);
        assertTrue(function.getStatements().get(1) instanceof MethodCallStatement);
        assertTrue(function.getStatements().get(2) instanceof AssignmentStatement);
        assertTrue(function.getStatements().get(3) instanceof ReturnStatement);

        // Check variable declaration
        VariableDeclarationStatement varDecl = (VariableDeclarationStatement) function.getStatements().get(0);
        assertEquals("result", varDecl.getName());
        assertEquals("boolean", varDecl.getType());
        assertTrue(varDecl.getValue() instanceof LiteralExpression);

        // Check return statement
        ReturnStatement returnStmt = (ReturnStatement) function.getStatements().get(3);
        assertTrue(returnStmt.getValue() instanceof VariableExpression);
        assertEquals("result", ((VariableExpression) returnStmt.getValue()).getName());
    }

    @Test
    @DisplayName("Should serialize and deserialize BusinessTask")
    void testSerializeDeserializeFunction() throws Exception {
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
        assertNotNull(json);
        assertTrue(json.contains("\"name\":") && json.contains("\"testFunction\""));
        assertTrue(json.contains("\"returnType\":") && json.contains("\"int\""));

        // Deserialize
        BusinessTask deserializedFunction = objectMapper.readValue(json, BusinessTask.class);

        // Verify structure
        assertNotNull(deserializedFunction);
        assertEquals(Integer.valueOf(10), deserializedFunction.getId());
        assertEquals("testFunction", deserializedFunction.getName());
        assertEquals("Function for testing serialization", deserializedFunction.getDescription());
        assertEquals("int", deserializedFunction.getReturnType());

        // Verify parameters
        assertNotNull(deserializedFunction.getParameters());
        assertEquals(1, deserializedFunction.getParameters().size());
        assertEquals("value", deserializedFunction.getParameters().get(0).getName());
        assertEquals("int", deserializedFunction.getParameters().get(0).getType());

        // Verify statements
        assertNotNull(deserializedFunction.getStatements());
        assertEquals(1, deserializedFunction.getStatements().size());
        assertTrue(deserializedFunction.getStatements().get(0) instanceof ReturnStatement);

        ReturnStatement deserializedReturn = (ReturnStatement) deserializedFunction.getStatements().get(0);
        assertTrue(deserializedReturn.getValue() instanceof LiteralExpression);

        LiteralExpression deserializedLiteral = (LiteralExpression) deserializedReturn.getValue();
        assertEquals("integer", deserializedLiteral.getValueType());
        assertEquals(42, deserializedLiteral.getValue());
    }

    @Test
    @DisplayName("Should handle BusinessTask with null fields")
    void testBusinessTaskWithNullFields() throws Exception {
        String json = """
        {
            "id": 5,
            "name": "minimalFunction",
            "returnType": "void"
        }
        """;

        BusinessTask function = objectMapper.readValue(json, BusinessTask.class);

        assertNotNull(function);
        assertEquals(Integer.valueOf(5), function.getId());
        assertEquals("minimalFunction", function.getName());
        assertEquals("void", function.getReturnType());
        assertNull(function.getDescription());
        assertNull(function.getParameters());
        assertNull(function.getStatements());
    }

    @Test
    @DisplayName("Should parse BusinessTask with complex nested statements")
    void testComplexNestedStatements() throws Exception {
        String json = """
        {
            "id": 4,
            "name": "complexFunction",
            "returnType": "string",
            "statements": [
                {
                    "statementType": "variableDeclaration",
                    "name": "message",
                    "type": "string",
                    "value": {
                        "expressionType": "literal",
                        "valueType": "string",
                        "value": "Hello"
                    }
                },
                {
                    "statementType": "methodCall",
                    "method": "processMessage",
                    "arguments": [
                        {
                            "expressionType": "variable",
                            "name": "message"
                        },
                        {
                            "expressionType": "literal",
                            "valueType": "integer",
                            "value": 5
                        }
                    ]
                },
                {
                    "statementType": "return",
                    "value": {
                        "expressionType": "variable",
                        "name": "message"
                    }
                }
            ]
        }
        """;

        BusinessTask function = objectMapper.readValue(json, BusinessTask.class);

        assertNotNull(function);
        assertEquals("complexFunction", function.getName());
        assertEquals(3, function.getStatements().size());

        // Check method call with multiple arguments
        MethodCallStatement methodCall = (MethodCallStatement) function.getStatements().get(1);
        assertEquals("processMessage", methodCall.getMethod());
        assertNotNull(methodCall.getArguments());
        assertEquals(2, methodCall.getArguments().size());
        assertTrue(methodCall.getArguments().get(0) instanceof VariableExpression);
        assertTrue(methodCall.getArguments().get(1) instanceof LiteralExpression);
    }
}