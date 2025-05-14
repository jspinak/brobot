package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ExpressionJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    private ObjectMapper jacksonMapper;

    @BeforeEach
    public void setup() {
        jacksonMapper = new ObjectMapper();
    }

    /**
     * Test parsing a LiteralExpression from JSON
     */
    @Test
    public void testParseLiteralExpression() throws ConfigurationException {
        // Test boolean literal
        String booleanJson = """
                {
                  "expressionType": "literal",
                  "valueType": "boolean",
                  "value": true
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(booleanJson);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(LiteralExpression.class, expression);
        LiteralExpression literal = (LiteralExpression) expression;
        assertEquals("literal", literal.getExpressionType());
        assertEquals("boolean", literal.getValueType());
        assertEquals(true, literal.getValue());

        // Test string literal
        String stringJson = """
                {
                  "expressionType": "literal",
                  "valueType": "string",
                  "value": "Hello, World!"
                }
                """;

        jsonNode = jsonParser.parseJson(stringJson);
        expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(LiteralExpression.class, expression);
        literal = (LiteralExpression) expression;
        assertEquals("literal", literal.getExpressionType());
        assertEquals("string", literal.getValueType());
        assertEquals("Hello, World!", literal.getValue());

        // Test integer literal
        String intJson = """
                {
                  "expressionType": "literal",
                  "valueType": "integer",
                  "value": 42
                }
                """;

        jsonNode = jsonParser.parseJson(intJson);
        expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(LiteralExpression.class, expression);
        literal = (LiteralExpression) expression;
        assertEquals("literal", literal.getExpressionType());
        assertEquals("integer", literal.getValueType());

        // The value might be deserialized as Integer or Long depending on Jackson configuration
        assertInstanceOf(Number.class, literal.getValue());
        assertEquals(42, ((Number) literal.getValue()).intValue());

        // Test double literal
        String doubleJson = """
                {
                  "expressionType": "literal",
                  "valueType": "double",
                  "value": 3.14159
                }
                """;

        jsonNode = jsonParser.parseJson(doubleJson);
        expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(LiteralExpression.class, expression);
        literal = (LiteralExpression) expression;
        assertEquals("literal", literal.getExpressionType());
        assertEquals("double", literal.getValueType());
        assertInstanceOf(Number.class, literal.getValue());
        assertEquals(3.14159, ((Number) literal.getValue()).doubleValue(), 0.00001);

        // Test null literal
        String nullJson = """
                {
                  "expressionType": "literal",
                  "valueType": "null",
                  "value": null
                }
                """;

        jsonNode = jsonParser.parseJson(nullJson);
        expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(LiteralExpression.class, expression);
        literal = (LiteralExpression) expression;
        assertEquals("literal", literal.getExpressionType());
        assertEquals("null", literal.getValueType());
        assertNull(literal.getValue());
    }

    /**
     * Test parsing a VariableExpression from JSON
     */
    @Test
    public void testParseVariableExpression() throws ConfigurationException {
        String json = """
                {
                  "expressionType": "variable",
                  "name": "counter"
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(VariableExpression.class, expression);
        VariableExpression variable = (VariableExpression) expression;
        assertEquals("variable", variable.getExpressionType());
        assertEquals("counter", variable.getName());
    }

    /**
     * Test parsing a MethodCallExpression from JSON
     */
    @Test
    public void testParseMethodCallExpression() throws ConfigurationException {
        String json = """
                {
                  "expressionType": "methodCall",
                  "object": "calculator",
                  "method": "add",
                  "arguments": [
                    {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 5
                    },
                    {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 10
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(MethodCallExpression.class, expression);
        MethodCallExpression methodCall = (MethodCallExpression) expression;
        assertEquals("methodCall", methodCall.getExpressionType());
        assertEquals("calculator", methodCall.getObject());
        assertEquals("add", methodCall.getMethod());
        assertNotNull(methodCall.getArguments());
        assertEquals(2, methodCall.getArguments().size());

        // Verify arguments
        assertInstanceOf(LiteralExpression.class, methodCall.getArguments().getFirst());
        LiteralExpression arg1 = (LiteralExpression) methodCall.getArguments().getFirst();
        assertEquals("integer", arg1.getValueType());
        assertEquals(5, ((Number) arg1.getValue()).intValue());

        assertInstanceOf(LiteralExpression.class, methodCall.getArguments().get(1));
        LiteralExpression arg2 = (LiteralExpression) methodCall.getArguments().get(1);
        assertEquals("integer", arg2.getValueType());
        assertEquals(10, ((Number) arg2.getValue()).intValue());
    }

    /**
     * Test parsing a BinaryOperationExpression from JSON
     */
    @Test
    public void testParseBinaryOperationExpression() throws ConfigurationException {
        String json = """
                {
                  "expressionType": "binaryOperation",
                  "operator": "+",
                  "left": {
                    "expressionType": "variable",
                    "name": "x"
                  },
                  "right": {
                    "expressionType": "variable",
                    "name": "y"
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(BinaryOperationExpression.class, expression);
        BinaryOperationExpression binOp = (BinaryOperationExpression) expression;
        assertEquals("binaryOperation", binOp.getExpressionType());
        assertEquals("+", binOp.getOperator());

        // Verify left operand
        assertNotNull(binOp.getLeft());
        assertInstanceOf(VariableExpression.class, binOp.getLeft());
        VariableExpression left = (VariableExpression) binOp.getLeft();
        assertEquals("x", left.getName());

        // Verify right operand
        assertNotNull(binOp.getRight());
        assertInstanceOf(VariableExpression.class, binOp.getRight());
        VariableExpression right = (VariableExpression) binOp.getRight();
        assertEquals("y", right.getName());
    }

    /**
     * Test parsing a BuilderExpression from JSON
     */
    @Test
    public void testParseBuilderExpression() throws ConfigurationException {
        String json = """
                {
                  "expressionType": "builder",
                  "builderType": "actionOptions",
                  "methods": [
                    {
                      "method": "setAction",
                      "arguments": [
                        {
                          "expressionType": "literal",
                          "valueType": "string",
                          "value": "CLICK"
                        }
                      ]
                    },
                    {
                      "method": "setSimilarity",
                      "arguments": [
                        {
                          "expressionType": "literal",
                          "valueType": "double",
                          "value": 0.8
                        }
                      ]
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(BuilderExpression.class, expression);
        BuilderExpression builder = (BuilderExpression) expression;
        assertEquals("builder", builder.getExpressionType());
        assertEquals("actionOptions", builder.getBuilderType());
        assertNotNull(builder.getMethods());
        assertEquals(2, builder.getMethods().size());

        // Verify first method
        BuilderMethod method1 = builder.getMethods().getFirst();
        assertEquals("setAction", method1.getMethod());
        assertEquals(1, method1.getArguments().size());
        assertInstanceOf(LiteralExpression.class, method1.getArguments().getFirst());
        LiteralExpression arg1 = (LiteralExpression) method1.getArguments().getFirst();
        assertEquals("string", arg1.getValueType());
        assertEquals("CLICK", arg1.getValue());

        // Verify second method
        BuilderMethod method2 = builder.getMethods().get(1);
        assertEquals("setSimilarity", method2.getMethod());
        assertEquals(1, method2.getArguments().size());
        assertInstanceOf(LiteralExpression.class, method2.getArguments().getFirst());
        LiteralExpression arg2 = (LiteralExpression) method2.getArguments().getFirst();
        assertEquals("double", arg2.getValueType());
        assertEquals(0.8, ((Number) arg2.getValue()).doubleValue(), 0.00001);
    }

    /**
     * Test parsing complex nested expressions
     */
    @Test
    public void testParseComplexNestedExpression() throws ConfigurationException {
        String json = """
                {
                  "expressionType": "binaryOperation",
                  "operator": "&&",
                  "left": {
                    "expressionType": "binaryOperation",
                    "operator": ">",
                    "left": {
                      "expressionType": "variable",
                      "name": "score"
                    },
                    "right": {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 80
                    }
                  },
                  "right": {
                    "expressionType": "binaryOperation",
                    "operator": "<",
                    "left": {
                      "expressionType": "methodCall",
                      "method": "getCurrentTime",
                      "arguments": []
                    },
                    "right": {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 120
                    }
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(BinaryOperationExpression.class, expression);
        BinaryOperationExpression rootOp = (BinaryOperationExpression) expression;
        assertEquals("&&", rootOp.getOperator());

        // Verify left branch
        assertInstanceOf(BinaryOperationExpression.class, rootOp.getLeft());
        BinaryOperationExpression leftOp = (BinaryOperationExpression) rootOp.getLeft();
        assertEquals(">", leftOp.getOperator());
        assertInstanceOf(VariableExpression.class, leftOp.getLeft());
        assertInstanceOf(LiteralExpression.class, leftOp.getRight());

        // Verify right branch
        assertInstanceOf(BinaryOperationExpression.class, rootOp.getRight());
        BinaryOperationExpression rightOp = (BinaryOperationExpression) rootOp.getRight();
        assertEquals("<", rightOp.getOperator());
        assertInstanceOf(MethodCallExpression.class, rightOp.getLeft());
        assertInstanceOf(LiteralExpression.class, rightOp.getRight());

        MethodCallExpression methodCall = (MethodCallExpression) rightOp.getLeft();
        assertEquals("getCurrentTime", methodCall.getMethod());
        assertEquals(0, methodCall.getArguments().size());
    }

    /**
     * Test serialization of a LiteralExpression
     */
    @Test
    public void testSerializeLiteralExpression() throws ConfigurationException {
        // Create a literal expression
        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("string");
        literal.setValue("test value");

        // Serialize
        String json = jsonUtils.toJsonSafe(literal);
        System.out.println("DEBUG: Serialized LiteralExpression: " + json);

        // Verify JSON contains expected fields
        JsonNode jsonNode = jsonParser.parseJson(json);
        assertEquals("literal", jsonNode.get("expressionType").asText());
        assertEquals("string", jsonNode.get("valueType").asText());
        assertEquals("test value", jsonNode.get("value").asText());

        // Deserialize and verify round-trip
        Expression deserialized = jsonParser.convertJson(jsonNode, Expression.class);
        assertInstanceOf(LiteralExpression.class, deserialized);
        LiteralExpression deserializedLiteral = (LiteralExpression) deserialized;
        assertEquals("literal", deserializedLiteral.getExpressionType());
        assertEquals("string", deserializedLiteral.getValueType());
        assertEquals("test value", deserializedLiteral.getValue());
    }

    /**
     * Test serialization of a VariableExpression
     */
    @Test
    public void testSerializeVariableExpression() throws ConfigurationException {
        // Create a variable expression
        VariableExpression variable = new VariableExpression();
        variable.setExpressionType("variable");
        variable.setName("testVar");

        // Serialize
        String json = jsonUtils.toJsonSafe(variable);
        System.out.println("DEBUG: Serialized VariableExpression: " + json);

        // Verify JSON contains expected fields
        JsonNode jsonNode = jsonParser.parseJson(json);
        assertEquals("variable", jsonNode.get("expressionType").asText());
        assertEquals("testVar", jsonNode.get("name").asText());

        // Deserialize and verify round-trip
        Expression deserialized = jsonParser.convertJson(jsonNode, Expression.class);
        assertInstanceOf(VariableExpression.class, deserialized);
        VariableExpression deserializedVar = (VariableExpression) deserialized;
        assertEquals("variable", deserializedVar.getExpressionType());
        assertEquals("testVar", deserializedVar.getName());
    }

    /**
     * Test serialization of a complex nested expression
     */
    @Test
    public void testSerializeComplexExpression() throws ConfigurationException {
        // Create a complex expression: (x + 5) > 10
        BinaryOperationExpression rootOp = new BinaryOperationExpression();
        rootOp.setExpressionType("binaryOperation");
        rootOp.setOperator(">");

        // Left side: (x + 5)
        BinaryOperationExpression leftOp = new BinaryOperationExpression();
        leftOp.setExpressionType("binaryOperation");
        leftOp.setOperator("+");

        VariableExpression varX = new VariableExpression();
        varX.setExpressionType("variable");
        varX.setName("x");

        LiteralExpression lit5 = new LiteralExpression();
        lit5.setExpressionType("literal");
        lit5.setValueType("integer");
        lit5.setValue(5);

        leftOp.setLeft(varX);
        leftOp.setRight(lit5);

        // Right side: 10
        LiteralExpression lit10 = new LiteralExpression();
        lit10.setExpressionType("literal");
        lit10.setValueType("integer");
        lit10.setValue(10);

        rootOp.setLeft(leftOp);
        rootOp.setRight(lit10);

        // Serialize
        String json = jsonUtils.toJsonSafe(rootOp);
        System.out.println("DEBUG: Serialized Complex Expression: " + json);

        // Deserialize and verify structure
        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression deserialized = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(BinaryOperationExpression.class, deserialized);
        BinaryOperationExpression deserializedRoot = (BinaryOperationExpression) deserialized;
        assertEquals(">", deserializedRoot.getOperator());

        assertInstanceOf(BinaryOperationExpression.class, deserializedRoot.getLeft());
        BinaryOperationExpression deserializedLeft = (BinaryOperationExpression) deserializedRoot.getLeft();
        assertEquals("+", deserializedLeft.getOperator());

        assertInstanceOf(VariableExpression.class, deserializedLeft.getLeft());
        VariableExpression deserializedVar = (VariableExpression) deserializedLeft.getLeft();
        assertEquals("x", deserializedVar.getName());

        assertInstanceOf(LiteralExpression.class, deserializedLeft.getRight());
        LiteralExpression deserializedLit5 = (LiteralExpression) deserializedLeft.getRight();
        assertEquals(5, ((Number)deserializedLit5.getValue()).intValue());

        assertInstanceOf(LiteralExpression.class, deserializedRoot.getRight());
        LiteralExpression deserializedLit10 = (LiteralExpression) deserializedRoot.getRight();
        assertEquals(10, ((Number)deserializedLit10.getValue()).intValue());
    }

    /**
     * Test handling of invalid expression type
     */
    @Test
    public void testInvalidExpressionType() {
        String json = """
                {
                  "expressionType": "invalidType",
                  "someField": "someValue"
                }
                """;

        Exception exception = assertThrows(ConfigurationException.class, () -> {
            JsonNode jsonNode = jsonParser.parseJson(json);
            Expression expression = jsonParser.convertJson(jsonNode, Expression.class);
        });

        // Verify exception contains useful message about the invalid type
        assertTrue(exception.getMessage().contains("Failed to convert JSON") ||
                exception.getMessage().contains("invalidType"));
    }

    /**
     * Test missing required fields
     */
    @Test
    public void testMissingRequiredFields() {
        // Missing 'name' in variable expression
        String json = """
                {
                  "expressionType": "variable"
                  // Missing required 'name' field
                }
                """;

        try {
            JsonNode jsonNode = jsonParser.parseJson(json);
            Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

            // If no exception, verify the field is null
            assertInstanceOf(VariableExpression.class, expression);
            VariableExpression variable = (VariableExpression) expression;
            assertNull(variable.getName());
        } catch (ConfigurationException e) {
            System.out.println("DEBUG: Exception message: " + e.getMessage());
            assertTrue(e.getMessage().contains("Failed to convert JSON") ||
                    e.getMessage().toLowerCase().contains("missing"));

        }
    }
}