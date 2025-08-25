package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.dsl.expressions.BinaryOperationExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.model.BuilderMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BuilderMethodJsonParserTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a BuilderMethod from JSON
     */
    @Test
    public void testParseBuilderMethod() throws Exception {
        String json = """
                {
                  "method": "setAction",
                  "arguments": [
                    {
                      "expressionType": "literal",
                      "valueType": "string",
                      "value": "CLICK"
                    }
                  ]
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        BuilderMethod method = objectMapper.treeToValue(jsonNode, BuilderMethod.class);

        assertNotNull(method);
        assertEquals("setAction", method.getMethod());
        assertNotNull(method.getArguments());
        assertEquals(1, method.getArguments().size());

        Expression arg = method.getArguments().getFirst();
        assertInstanceOf(LiteralExpression.class, arg);
        LiteralExpression literal = (LiteralExpression) arg;
        assertEquals("string", literal.getValueType());
        assertEquals("CLICK", literal.getValue());
    }

    /**
     * Test parsing a BuilderMethod with multiple arguments
     */
    @Test
    public void testParseWithMultipleArguments() throws Exception {
        String json = """
                {
                  "method": "add",
                  "arguments": [
                    {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 10
                    },
                    {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 20
                    },
                    {
                      "expressionType": "variable",
                      "name": "offset"
                    }
                  ]
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        BuilderMethod method = objectMapper.treeToValue(jsonNode, BuilderMethod.class);

        assertNotNull(method);
        assertEquals("add", method.getMethod());
        assertNotNull(method.getArguments());
        assertEquals(3, method.getArguments().size());

        // Verify argument types
        assertInstanceOf(LiteralExpression.class, method.getArguments().getFirst());
        assertInstanceOf(LiteralExpression.class, method.getArguments().get(1));
        assertInstanceOf(VariableExpression.class, method.getArguments().get(2));

        // Verify first argument
        LiteralExpression arg1 = (LiteralExpression) method.getArguments().getFirst();
        assertEquals("integer", arg1.getValueType());
        assertEquals(10, ((Number) arg1.getValue()).intValue());

        // Verify third argument
        VariableExpression arg3 = (VariableExpression) method.getArguments().get(2);
        assertEquals("offset", arg3.getName());
    }

    /**
     * Test serialization and deserialization of BuilderMethod
     */
    @Test
    public void testSerializeDeserializeBuilderMethod() throws Exception {
        // Create a builder method
        BuilderMethod method = new BuilderMethod();
        method.setMethod("setSimilarity");

        List<Expression> arguments = new ArrayList<>();

        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("double");
        literal.setValue(0.85);

        arguments.add(literal);
        method.setArguments(arguments);

        // Serialize
        String json = objectMapper.writeValueAsString(method);
        System.out.println("DEBUG: Serialized BuilderMethod: " + json);

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        BuilderMethod deserializedMethod = objectMapper.treeToValue(jsonNode, BuilderMethod.class);

        // Verify
        assertNotNull(deserializedMethod);
        assertEquals("setSimilarity", deserializedMethod.getMethod());
        assertNotNull(deserializedMethod.getArguments());
        assertEquals(1, deserializedMethod.getArguments().size());

        assertInstanceOf(LiteralExpression.class, deserializedMethod.getArguments().getFirst());
        LiteralExpression deserializedArg = (LiteralExpression) deserializedMethod.getArguments().getFirst();
        assertEquals("double", deserializedArg.getValueType());
        assertEquals(0.85, ((Number) deserializedArg.getValue()).doubleValue(), 0.001);
    }

    /**
     * Test empty arguments list
     */
    @Test
    public void testEmptyArguments() throws Exception {
        String json = """
                {
                  "method": "reset",
                  "arguments": []
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        BuilderMethod method = objectMapper.treeToValue(jsonNode, BuilderMethod.class);

        assertNotNull(method);
        assertEquals("reset", method.getMethod());
        assertNotNull(method.getArguments());
        assertEquals(0, method.getArguments().size());
    }

    /**
     * Test with complex argument (nested expression)
     */
    @Test
    public void testComplexArgument() throws Exception {
        String json = """
                {
                  "method": "setCondition",
                  "arguments": [
                    {
                      "expressionType": "binaryOperation",
                      "operator": ">",
                      "left": {
                        "expressionType": "variable",
                        "name": "count"
                      },
                      "right": {
                        "expressionType": "literal",
                        "valueType": "integer",
                        "value": 5
                      }
                    }
                  ]
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        BuilderMethod method = objectMapper.treeToValue(jsonNode, BuilderMethod.class);

        assertNotNull(method);
        assertEquals("setCondition", method.getMethod());
        assertNotNull(method.getArguments());
        assertEquals(1, method.getArguments().size());

        // Verify complex argument
        assertInstanceOf(BinaryOperationExpression.class, method.getArguments().getFirst());
        BinaryOperationExpression binOp = (BinaryOperationExpression) method.getArguments().getFirst();
        assertEquals(">", binOp.getOperator());

        assertInstanceOf(VariableExpression.class, binOp.getLeft());
        assertEquals("count", ((VariableExpression) binOp.getLeft()).getName());

        assertInstanceOf(LiteralExpression.class, binOp.getRight());
        assertEquals(5, ((Number) ((LiteralExpression) binOp.getRight()).getValue()).intValue());
    }
}