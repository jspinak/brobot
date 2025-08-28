package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.test.BrobotTestBase;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AutomationDsl (InstructionSet) JSON parsing.
 * 
 * Key points:
 * - InstructionSet is the root data structure for DSL
 * - Contains list of BusinessTask (automation functions)
 * - Each BusinessTask has parameters and statements
 * - Statements are polymorphic with statementType discriminator
 */
@DisplayName("AutomationDsl JSON Parser Tests")
class AutomationDslJsonParserTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @DisplayName("Should parse simple InstructionSet from JSON")
    void testParseSimpleAutomationDsl() throws Exception {
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

        InstructionSet dsl = objectMapper.readValue(json, InstructionSet.class);

        assertNotNull(dsl);
        assertNotNull(dsl.getAutomationFunctions());
        assertEquals(1, dsl.getAutomationFunctions().size());

        BusinessTask function = dsl.getAutomationFunctions().get(0);
        assertEquals(Integer.valueOf(1), function.getId());
        assertEquals("sampleFunction", function.getName());
        assertEquals("void", function.getReturnType());
        assertNotNull(function.getStatements());
        assertEquals(0, function.getStatements().size());
    }

    @Test
    @DisplayName("Should parse complex InstructionSet with multiple functions")
    void testParseComplexAutomationDsl() throws Exception {
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
                                "valueType": "string",
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

        InstructionSet dsl = objectMapper.readValue(json, InstructionSet.class);

        assertNotNull(dsl);
        assertNotNull(dsl.getAutomationFunctions());
        assertEquals(2, dsl.getAutomationFunctions().size());

        // Check first function
        BusinessTask function1 = dsl.getAutomationFunctions().get(0);
        assertEquals(Integer.valueOf(1), function1.getId());
        assertEquals("handleLogin", function1.getName());
        assertEquals("Handles the login process", function1.getDescription());
        assertEquals("boolean", function1.getReturnType());

        // Check parameters
        assertNotNull(function1.getParameters());
        assertEquals(2, function1.getParameters().size());
        assertEquals("username", function1.getParameters().get(0).getName());
        assertEquals("string", function1.getParameters().get(0).getType());
        assertEquals("password", function1.getParameters().get(1).getName());

        // Check statements
        assertNotNull(function1.getStatements());
        assertEquals(3, function1.getStatements().size());
        assertTrue(function1.getStatements().get(0) instanceof VariableDeclarationStatement);
        assertTrue(function1.getStatements().get(1) instanceof MethodCallStatement);
        assertTrue(function1.getStatements().get(2) instanceof ReturnStatement);

        // Check second function
        BusinessTask function2 = dsl.getAutomationFunctions().get(1);
        assertEquals(Integer.valueOf(2), function2.getId());
        assertEquals("logout", function2.getName());
        assertEquals("void", function2.getReturnType());

        assertNotNull(function2.getStatements());
        assertEquals(1, function2.getStatements().size());
        assertTrue(function2.getStatements().get(0) instanceof MethodCallStatement);
        assertEquals("clickLogout", ((MethodCallStatement) function2.getStatements().get(0)).getMethod());
    }

    @Test
    @DisplayName("Should serialize and deserialize InstructionSet")
    void testSerializeDeserializeAutomationDsl() throws Exception {
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
        String json = objectMapper.writeValueAsString(dsl);
        assertNotNull(json);
        assertTrue(json.contains("\"automationFunctions\""));

        // Deserialize
        InstructionSet deserializedDsl = objectMapper.readValue(json, InstructionSet.class);

        // Verify structure
        assertNotNull(deserializedDsl);
        assertNotNull(deserializedDsl.getAutomationFunctions());
        assertEquals(1, deserializedDsl.getAutomationFunctions().size());

        BusinessTask deserializedFunction = deserializedDsl.getAutomationFunctions().get(0);
        assertEquals(Integer.valueOf(1), deserializedFunction.getId());
        assertEquals("testFunction", deserializedFunction.getName());
        assertEquals("void", deserializedFunction.getReturnType());

        assertNotNull(deserializedFunction.getParameters());
        assertEquals(1, deserializedFunction.getParameters().size());
        assertEquals("input", deserializedFunction.getParameters().get(0).getName());

        assertNotNull(deserializedFunction.getStatements());
        assertEquals(2, deserializedFunction.getStatements().size());
        assertTrue(deserializedFunction.getStatements().get(0) instanceof VariableDeclarationStatement);
        assertTrue(deserializedFunction.getStatements().get(1) instanceof MethodCallStatement);
    }

    @Test
    @DisplayName("Should handle empty InstructionSet")
    void testEmptyInstructionSet() throws Exception {
        String json = """
        {
            "automationFunctions": []
        }
        """;

        InstructionSet dsl = objectMapper.readValue(json, InstructionSet.class);

        assertNotNull(dsl);
        assertNotNull(dsl.getAutomationFunctions());
        assertTrue(dsl.getAutomationFunctions().isEmpty());
    }

    @Test
    @DisplayName("Should handle InstructionSet with null fields")
    void testInstructionSetWithNullFields() throws Exception {
        String json = """
        {
            "automationFunctions": [
                {
                    "id": 1,
                    "name": "testFunc",
                    "returnType": "void"
                }
            ]
        }
        """;

        InstructionSet dsl = objectMapper.readValue(json, InstructionSet.class);

        assertNotNull(dsl);
        BusinessTask function = dsl.getAutomationFunctions().get(0);
        assertNull(function.getDescription());
        assertNull(function.getParameters());
        assertNull(function.getStatements());
    }
}