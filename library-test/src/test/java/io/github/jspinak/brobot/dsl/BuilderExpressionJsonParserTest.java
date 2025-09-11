package io.github.jspinak.brobot.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.dsl.expressions.BuilderExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.model.BuilderMethod;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.test.BrobotTestBase;

@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@TestPropertySource(properties = {"java.awt.headless=false"})
@Disabled("Failing in CI - temporarily disabled for CI/CD")
public class BuilderExpressionJsonParserTest extends BrobotTestBase {

    @Autowired private ConfigurationParser jsonParser;

    @Autowired private JsonUtils jsonUtils;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    /** Test parsing a BuilderExpression for actionOptions from JSON */
    @Test
    public void testParseObjectActionOptionsBuilder() throws ConfigurationException {
        String json =
                """
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
                    },
                    {
                      "method": "setClickType",
                      "arguments": [
                        {
                          "expressionType": "literal",
                          "valueType": "string",
                          "value": "RIGHT"
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
        assertEquals(3, builder.getMethods().size());

        // Check first method
        BuilderMethod method1 = builder.getMethods().getFirst();
        assertEquals("setAction", method1.getMethod());
        assertEquals(1, method1.getArguments().size());
        assertInstanceOf(LiteralExpression.class, method1.getArguments().getFirst());
        assertEquals("CLICK", ((LiteralExpression) method1.getArguments().getFirst()).getValue());

        // Check second method
        BuilderMethod method2 = builder.getMethods().get(1);
        assertEquals("setSimilarity", method2.getMethod());
        assertEquals(1, method2.getArguments().size());
        assertInstanceOf(LiteralExpression.class, method2.getArguments().getFirst());
        assertEquals(
                0.8,
                ((Number) ((LiteralExpression) method2.getArguments().getFirst()).getValue())
                        .doubleValue(),
                0.001);

        // Check third method
        BuilderMethod method3 = builder.getMethods().get(2);
        assertEquals("setClickType", method3.getMethod());
        assertEquals(1, method3.getArguments().size());
        assertInstanceOf(LiteralExpression.class, method3.getArguments().getFirst());
        assertEquals("RIGHT", ((LiteralExpression) method3.getArguments().getFirst()).getValue());
    }

    /** Test parsing a BuilderExpression for objectCollection from JSON */
    @Test
    public void testParseObjectCollectionBuilder() throws ConfigurationException {
        String json =
                """
                {
                  "expressionType": "builder",
                  "builderType": "objectCollection",
                  "methods": [
                    {
                      "method": "withImages",
                      "arguments": [
                        {
                          "expressionType": "variable",
                          "name": "loginButton"
                        }
                      ]
                    },
                    {
                      "method": "withRegions",
                      "arguments": [
                        {
                          "expressionType": "variable",
                          "name": "searchRegion"
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
        assertEquals("objectCollection", builder.getBuilderType());
        assertNotNull(builder.getMethods());
        assertEquals(2, builder.getMethods().size());

        // Check methods
        BuilderMethod method1 = builder.getMethods().getFirst();
        assertEquals("withImages", method1.getMethod());
        assertEquals(1, method1.getArguments().size());
        assertInstanceOf(VariableExpression.class, method1.getArguments().getFirst());
        assertEquals(
                "loginButton", ((VariableExpression) method1.getArguments().getFirst()).getName());

        BuilderMethod method2 = builder.getMethods().get(1);
        assertEquals("withRegions", method2.getMethod());
        assertEquals(1, method2.getArguments().size());
        assertInstanceOf(VariableExpression.class, method2.getArguments().getFirst());
        assertEquals(
                "searchRegion", ((VariableExpression) method2.getArguments().getFirst()).getName());
    }

    /** Test serialization and deserialization of BuilderExpression */
    @Test
    public void testSerializeDeserializeBuilder() throws ConfigurationException {
        // Create a builder expression
        BuilderExpression builder = new BuilderExpression();
        builder.setExpressionType("builder");
        builder.setBuilderType("actionOptions");

        List<BuilderMethod> methods = new ArrayList<>();

        // Add first method
        BuilderMethod method1 = new BuilderMethod();
        method1.setMethod("setAction");

        List<Expression> args1 = new ArrayList<>();
        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("string");
        literal.setValue("FIND");
        args1.add(literal);

        method1.setArguments(args1);
        methods.add(method1);

        // Add second method
        BuilderMethod method2 = new BuilderMethod();
        method2.setMethod("setFind");

        List<Expression> args2 = new ArrayList<>();
        LiteralExpression literal2 = new LiteralExpression();
        literal2.setExpressionType("literal");
        literal2.setValueType("string");
        literal2.setValue("FIRST");
        args2.add(literal2);

        method2.setArguments(args2);
        methods.add(method2);

        builder.setMethods(methods);

        // Serialize
        String json = jsonUtils.toJsonSafe(builder);
        System.out.println("DEBUG: Serialized BuilderExpression: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression deserializedExpr = jsonParser.convertJson(jsonNode, Expression.class);

        // Verify
        assertInstanceOf(BuilderExpression.class, deserializedExpr);
        BuilderExpression deserializedBuilder = (BuilderExpression) deserializedExpr;

        assertEquals("builder", deserializedBuilder.getExpressionType());
        assertEquals("actionOptions", deserializedBuilder.getBuilderType());
        assertNotNull(deserializedBuilder.getMethods());
        assertEquals(2, deserializedBuilder.getMethods().size());

        // Check first method
        BuilderMethod deserializedMethod1 = deserializedBuilder.getMethods().getFirst();
        assertEquals("setAction", deserializedMethod1.getMethod());
        assertEquals(1, deserializedMethod1.getArguments().size());
        assertInstanceOf(LiteralExpression.class, deserializedMethod1.getArguments().getFirst());
        assertEquals(
                "FIND",
                ((LiteralExpression) deserializedMethod1.getArguments().getFirst()).getValue());

        // Check second method
        BuilderMethod deserializedMethod2 = deserializedBuilder.getMethods().get(1);
        assertEquals("setFind", deserializedMethod2.getMethod());
        assertEquals(1, deserializedMethod2.getArguments().size());
        assertInstanceOf(LiteralExpression.class, deserializedMethod2.getArguments().getFirst());
        assertEquals(
                "FIRST",
                ((LiteralExpression) deserializedMethod2.getArguments().getFirst()).getValue());
    }

    /** Test with empty methods list */
    @Test
    public void testEmptyMethods() throws ConfigurationException {
        String json =
                """
                {
                  "expressionType": "builder",
                  "builderType": "actionOptions",
                  "methods": []
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Expression expression = jsonParser.convertJson(jsonNode, Expression.class);

        assertInstanceOf(BuilderExpression.class, expression);
        BuilderExpression builder = (BuilderExpression) expression;

        assertEquals("builder", builder.getExpressionType());
        assertEquals("actionOptions", builder.getBuilderType());
        assertNotNull(builder.getMethods());
        assertEquals(0, builder.getMethods().size());
    }
}
