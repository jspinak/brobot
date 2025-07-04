package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement;
import io.github.jspinak.brobot.runner.dsl.statements.ReturnStatement;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {"java.awt.headless=false"})
public class AutomationDslJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a simple AutomationDsl from JSON
     */
    @Test
    public void testParseSimpleAutomationDsl() throws ConfigurationException {
        String json = """
                {
                  "automationFunctions": [
                    {
                      "id": 1,
                      "name": "sampleFunction",
                      "returnType": "void",
                      "statements": []
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        InstructionSet dsl = jsonParser.convertJson(jsonNode, InstructionSet.class);

        assertNotNull(dsl);
        assertNotNull(dsl.getAutomationFunctions());
        assertEquals(1, dsl.getAutomationFunctions().size());

        BusinessTask function = dsl.getAutomationFunctions().getFirst();
        assertEquals(Integer.valueOf(1), function.getId());
        assertEquals("sampleFunction", function.getName());
        assertEquals("void", function.getReturnType());
        assertNotNull(function.getStatements());
        assertEquals(0, function.getStatements().size());
    }

    /**
     * Test parsing a more complex AutomationDsl with multiple functions and statements
     */
    @Test
    public void testParseComplexAutomationDsl() throws ConfigurationException {
        String json = """
                {
                  "automationFunctions": [
                    {
                      "id": 1,
                      "name": "handleLogin",
                      "description": "Handles the login process",
                      "returnType": "boolean",
                      "parameters": [
                        {
                          "name": "username",
                          "type": "string"
                        },
                        {
                          "name": "password",
                          "type": "string"
                        }
                      ],
                      "statements": [
                        {
                          "statementType": "variableDeclaration",
                          "name": "success",
                          "type": "boolean",
                          "value": {
                            "expressionType": "literal",
                            "valueType": "boolean",
                            "value": false
                          }
                        },
                        {
                          "statementType": "methodCall",
                          "method": "typeText",
                          "arguments": [
                            {
                              "expressionType": "variable",
                              "name": "username"
                            }
                          ]
                        },
                        {
                          "statementType": "return",
                          "value": {
                            "expressionType": "variable",
                            "name": "success"
                          }
                        }
                      ]
                    },
                    {
                      "id": 2,
                      "name": "logout",
                      "returnType": "void",
                      "statements": [
                        {
                          "statementType": "methodCall",
                          "method": "clickLogout",
                          "arguments": []
                        }
                      ]
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        InstructionSet dsl = jsonParser.convertJson(jsonNode, InstructionSet.class);

        assertNotNull(dsl);
        assertNotNull(dsl.getAutomationFunctions());
        assertEquals(2, dsl.getAutomationFunctions().size());

        // Check first function
        BusinessTask function1 = dsl.getAutomationFunctions().getFirst();
        assertEquals(Integer.valueOf(1), function1.getId());
        assertEquals("handleLogin", function1.getName());
        assertEquals("Handles the login process", function1.getDescription());
        assertEquals("boolean", function1.getReturnType());

        // Check parameters
        assertNotNull(function1.getParameters());
        assertEquals(2, function1.getParameters().size());
        assertEquals("username", function1.getParameters().getFirst().getName());
        assertEquals("string", function1.getParameters().getFirst().getType());
        assertEquals("password", function1.getParameters().get(1).getName());

        // Check statements
        assertNotNull(function1.getStatements());
        assertEquals(3, function1.getStatements().size());
        assertInstanceOf(VariableDeclarationStatement.class, function1.getStatements().getFirst());
        assertInstanceOf(MethodCallStatement.class, function1.getStatements().get(1));
        assertInstanceOf(ReturnStatement.class, function1.getStatements().get(2));

        // Check second function
        BusinessTask function2 = dsl.getAutomationFunctions().get(1);
        assertEquals(Integer.valueOf(2), function2.getId());
        assertEquals("logout", function2.getName());
        assertEquals("void", function2.getReturnType());

        assertNotNull(function2.getStatements());
        assertEquals(1, function2.getStatements().size());
        assertInstanceOf(MethodCallStatement.class, function2.getStatements().getFirst());
        assertEquals("clickLogout", ((MethodCallStatement) function2.getStatements().getFirst()).getMethod());
    }

    /**
     * Test serialization and deserialization cycle of AutomationDsl
     */
    @Test
    public void testSerializeDeserializeAutomationDsl() throws ConfigurationException {
        // Create a sample DSL
        InstructionSet dsl = new InstructionSet();
        List<BusinessTask> functions = new ArrayList<>();

        // Create a function
        BusinessTask function = new BusinessTask();
        function.setId(1);
        function.setName("testFunction");
        function.setReturnType("void");

        // Create parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter param = new Parameter();
        param.setName("input");
        param.setType("string");
        parameters.add(param);
        function.setParameters(parameters);

        // Create statements
        List<Statement> statements = new ArrayList<>();

        // Add a variable declaration
        VariableDeclarationStatement varDecl = new VariableDeclarationStatement();
        varDecl.setStatementType("variableDeclaration");
        varDecl.setName("message");
        varDecl.setType("string");

        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("string");
        literal.setValue("Hello");

        varDecl.setValue(literal);
        statements.add(varDecl);

        // Add a method call
        MethodCallStatement methodCall = new MethodCallStatement();
        methodCall.setStatementType("methodCall");
        methodCall.setMethod("printMessage");

        List<Expression> args = new ArrayList<>();
        VariableExpression varExpr = new VariableExpression();
        varExpr.setExpressionType("variable");
        varExpr.setName("message");
        args.add(varExpr);

        methodCall.setArguments(args);
        statements.add(methodCall);

        function.setStatements(statements);
        functions.add(function);

        dsl.setAutomationFunctions(functions);

        // Serialize
        String json = jsonUtils.toJsonSafe(dsl);
        System.out.println("DEBUG: Serialized AutomationDsl: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        InstructionSet deserializedDsl = jsonParser.convertJson(jsonNode, InstructionSet.class);

        // Verify structure
        assertNotNull(deserializedDsl);
        assertNotNull(deserializedDsl.getAutomationFunctions());
        assertEquals(1, deserializedDsl.getAutomationFunctions().size());

        BusinessTask deserializedFunction = deserializedDsl.getAutomationFunctions().getFirst();
        assertEquals(Integer.valueOf(1), deserializedFunction.getId());
        assertEquals("testFunction", deserializedFunction.getName());
        assertEquals("void", deserializedFunction.getReturnType());

        assertNotNull(deserializedFunction.getParameters());
        assertEquals(1, deserializedFunction.getParameters().size());
        assertEquals("input", deserializedFunction.getParameters().getFirst().getName());

        assertNotNull(deserializedFunction.getStatements());
        assertEquals(2, deserializedFunction.getStatements().size());
        assertInstanceOf(VariableDeclarationStatement.class, deserializedFunction.getStatements().getFirst());
        assertInstanceOf(MethodCallStatement.class, deserializedFunction.getStatements().get(1));
    }

    /**
     * Test validation against the automation DSL schema
     */
    @Test
    public void testValidationAgainstSchema() throws ConfigurationException {
        // This test requires the SchemaManager class with validation capabilities
        // If available, you could validate the JSON against the automation-dsl-schema.json

        // Example:
        /*
        String json = "..."; // Valid JSON according to the schema
        JsonNode jsonNode = jsonParser.parseJson(json);
        boolean isValid = schemaManager.isValid(jsonNode, schemaManager.AUTOMATION_DSL_SCHEMA_PATH);
        assertTrue(isValid);
        */

        // This is a placeholder test - implement actual validation if SchemaManager is accessible
        assertTrue(true, "Schema validation test is a placeholder");
    }
}