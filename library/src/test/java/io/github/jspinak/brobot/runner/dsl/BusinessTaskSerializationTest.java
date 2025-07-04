package io.github.jspinak.brobot.runner.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import io.github.jspinak.brobot.runner.dsl.statements.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessTask (AutomationFunction) JSON serialization without Spring dependencies.
 * Migrated from library-test module.
 */
class BusinessTaskSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
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

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify structure
        assertEquals(1, jsonNode.get("id").asInt());
        assertEquals("simpleFunction", jsonNode.get("name").asText());
        assertEquals("A simple test function", jsonNode.get("description").asText());
        assertEquals("void", jsonNode.get("returnType").asText());
        assertTrue(jsonNode.get("statements").isArray());
        assertEquals(0, jsonNode.get("statements").size());
    }

    @Test
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

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify basic properties
        assertEquals("functionWithParams", jsonNode.get("name").asText());
        assertEquals("boolean", jsonNode.get("returnType").asText());
        
        // Check parameters
        JsonNode parametersNode = jsonNode.get("parameters");
        assertNotNull(parametersNode);
        assertTrue(parametersNode.isArray());
        assertEquals(2, parametersNode.size());
        
        JsonNode param1 = parametersNode.get(0);
        assertEquals("inputText", param1.get("name").asText());
        assertEquals("string", param1.get("type").asText());
        
        JsonNode param2 = parametersNode.get(1);
        assertEquals("maxAttempts", param2.get("name").asText());
        assertEquals("int", param2.get("type").asText());
    }

    @Test
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

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify basic properties
        assertEquals("functionWithStatements", jsonNode.get("name").asText());
        
        // Check statements
        JsonNode statementsNode = jsonNode.get("statements");
        assertNotNull(statementsNode);
        assertEquals(4, statementsNode.size());
        
        // Verify variable declaration
        JsonNode varDeclNode = statementsNode.get(0);
        assertEquals("variableDeclaration", varDeclNode.get("statementType").asText());
        assertEquals("result", varDeclNode.get("name").asText());
        assertEquals("boolean", varDeclNode.get("type").asText());
        assertEquals("literal", varDeclNode.get("value").get("expressionType").asText());
        assertFalse(varDeclNode.get("value").get("value").asBoolean());
        
        // Verify method call
        JsonNode methodCallNode = statementsNode.get(1);
        assertEquals("methodCall", methodCallNode.get("statementType").asText());
        assertEquals("performAction", methodCallNode.get("method").asText());
        
        // Verify assignment
        JsonNode assignmentNode = statementsNode.get(2);
        assertEquals("assignment", assignmentNode.get("statementType").asText());
        assertEquals("result", assignmentNode.get("variable").asText());
        
        // Verify return statement
        JsonNode returnNode = statementsNode.get(3);
        assertEquals("return", returnNode.get("statementType").asText());
        assertEquals("variable", returnNode.get("value").get("expressionType").asText());
        assertEquals("result", returnNode.get("value").get("name").asText());
    }

    @Test
    void testSerializeBusinessTask() throws Exception {
        // Create a BusinessTask
        BusinessTask task = new BusinessTask();
        task.setId(10);
        task.setName("testFunction");
        task.setDescription("Function for testing serialization");
        task.setReturnType("int");
        
        // Add parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter param = new Parameter();
        param.setName("value");
        param.setType("int");
        parameters.add(param);
        task.setParameters(parameters);
        
        // Add statements
        List<Statement> statements = new ArrayList<>();
        
        // Create a return statement
        ReturnStatement returnStmt = new ReturnStatement();
        returnStmt.setStatementType("return");
        
        // Create a literal expression
        LiteralExpression literal = new LiteralExpression();
        literal.setExpressionType("literal");
        literal.setValueType("integer");
        literal.setValue(42);
        
        returnStmt.setValue(literal);
        statements.add(returnStmt);
        
        task.setStatements(statements);
        
        // Serialize
        String json = objectMapper.writeValueAsString(task);
        
        // Verify JSON structure
        assertNotNull(json);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertEquals(10, jsonNode.get("id").asInt());
        assertEquals("testFunction", jsonNode.get("name").asText());
        assertEquals("Function for testing serialization", jsonNode.get("description").asText());
        assertEquals("int", jsonNode.get("returnType").asText());
        
        // Verify parameters in JSON
        JsonNode paramsNode = jsonNode.get("parameters");
        assertEquals(1, paramsNode.size());
        assertEquals("value", paramsNode.get(0).get("name").asText());
        assertEquals("int", paramsNode.get(0).get("type").asText());
        
        // Verify statements in JSON
        JsonNode stmtsNode = jsonNode.get("statements");
        assertEquals(1, stmtsNode.size());
        
        JsonNode returnNode = stmtsNode.get(0);
        assertEquals("return", returnNode.get("statementType").asText());
        
        JsonNode valueNode = returnNode.get("value");
        assertEquals("literal", valueNode.get("expressionType").asText());
        assertEquals("integer", valueNode.get("valueType").asText());
        assertEquals(42, valueNode.get("value").asInt());
    }

    @Test
    void testComplexBusinessTask() throws Exception {
        // Create a more complex task
        BusinessTask task = new BusinessTask();
        task.setName("complexTask");
        task.setReturnType("boolean");
        
        List<Statement> statements = new ArrayList<>();
        
        // Variable declaration
        VariableDeclarationStatement varDecl = new VariableDeclarationStatement();
        varDecl.setStatementType("variableDeclaration");
        varDecl.setName("success");
        varDecl.setType("boolean");
        
        LiteralExpression falseLiteral = new LiteralExpression();
        falseLiteral.setExpressionType("literal");
        falseLiteral.setValueType("boolean");
        falseLiteral.setValue(false);
        varDecl.setValue(falseLiteral);
        
        statements.add(varDecl);
        
        // Method call
        MethodCallStatement methodCall = new MethodCallStatement();
        methodCall.setStatementType("methodCall");
        methodCall.setMethod("executeAction");
        methodCall.setArguments(new ArrayList<>());
        
        statements.add(methodCall);
        
        // Assignment
        AssignmentStatement assignment = new AssignmentStatement();
        assignment.setStatementType("assignment");
        assignment.setVariable("success");
        
        LiteralExpression trueLiteral = new LiteralExpression();
        trueLiteral.setExpressionType("literal");
        trueLiteral.setValueType("boolean");
        trueLiteral.setValue(true);
        assignment.setValue(trueLiteral);
        
        statements.add(assignment);
        
        // Return
        ReturnStatement returnStmt = new ReturnStatement();
        returnStmt.setStatementType("return");
        
        VariableExpression varExpr = new VariableExpression();
        varExpr.setExpressionType("variable");
        varExpr.setName("success");
        returnStmt.setValue(varExpr);
        
        statements.add(returnStmt);
        
        task.setStatements(statements);
        
        // Serialize and verify
        String json = objectMapper.writeValueAsString(task);
        assertNotNull(json);
        
        // Verify it contains expected elements
        assertTrue(json.contains("complexTask"));
        assertTrue(json.contains("variableDeclaration"));
        assertTrue(json.contains("methodCall"));
        assertTrue(json.contains("assignment"));
        assertTrue(json.contains("return"));
    }

    @Test
    void testEmptyBusinessTask() throws Exception {
        BusinessTask task = new BusinessTask();
        
        String json = objectMapper.writeValueAsString(task);
        assertNotNull(json);
        
        JsonNode jsonNode = objectMapper.readTree(json);
        // Empty task should still serialize basic structure
        assertNotNull(jsonNode);
    }
}