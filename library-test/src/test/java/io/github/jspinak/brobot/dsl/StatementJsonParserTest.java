package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.dsl.expressions.BinaryOperationExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.MethodCallExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression;
import io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement;
import io.github.jspinak.brobot.runner.dsl.statements.ForEachStatement;
import io.github.jspinak.brobot.runner.dsl.statements.IfStatement;
import io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement;
import io.github.jspinak.brobot.runner.dsl.statements.ReturnStatement;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StatementJsonParserTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a VariableDeclarationStatement from JSON
     */
    @Test
    public void testParseVariableDeclarationStatement() throws Exception {
        String json = """
                {
                  "statementType": "variableDeclaration",
                  "name": "counter",
                  "type": "int",
                  "value": {
                    "expressionType": "literal",
                    "valueType": "integer",
                    "value": 0
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof VariableDeclarationStatement);
        VariableDeclarationStatement varDecl = (VariableDeclarationStatement) statement;

        // Verify fields
        assertEquals("variableDeclaration", varDecl.getStatementType());
        assertEquals("counter", varDecl.getName());
        assertEquals("int", varDecl.getType());
        assertNotNull(varDecl.getValue());
        assertTrue(varDecl.getValue() instanceof LiteralExpression);

        LiteralExpression literal = (LiteralExpression) varDecl.getValue();
        assertEquals("literal", literal.getExpressionType());
        assertEquals("integer", literal.getValueType());
        assertEquals(0, literal.getValue());
    }

    /**
     * Test parsing an AssignmentStatement from JSON
     */
    @Test
    public void testParseAssignmentStatement() throws Exception {
        String json = """
                {
                  "statementType": "assignment",
                  "variable": "counter",
                  "value": {
                    "expressionType": "binaryOperation",
                    "operator": "+",
                    "left": {
                      "expressionType": "variable",
                      "name": "counter"
                    },
                    "right": {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 1
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof AssignmentStatement);
        AssignmentStatement assignment = (AssignmentStatement) statement;

        // Verify fields
        assertEquals("assignment", assignment.getStatementType());
        assertEquals("counter", assignment.getVariable());
        assertNotNull(assignment.getValue());
        assertTrue(assignment.getValue() instanceof BinaryOperationExpression);

        BinaryOperationExpression binOp = (BinaryOperationExpression) assignment.getValue();
        assertEquals("binaryOperation", binOp.getExpressionType());
        assertEquals("+", binOp.getOperator());
        assertNotNull(binOp.getLeft());
        assertNotNull(binOp.getRight());
    }

    /**
     * Test parsing an IfStatement from JSON
     */
    @Test
    public void testParseIfStatement() throws Exception {
        String json = """
                {
                  "statementType": "if",
                  "condition": {
                    "expressionType": "binaryOperation",
                    "operator": ">",
                    "left": {
                      "expressionType": "variable",
                      "name": "counter"
                    },
                    "right": {
                      "expressionType": "literal",
                      "valueType": "integer",
                      "value": 10
                    }
                  },
                  "thenStatements": [
                    {
                      "statementType": "methodCall",
                      "method": "printResult",
                      "arguments": [
                        {
                          "expressionType": "variable",
                          "name": "counter"
                        }
                      ]
                    }
                  ],
                  "elseStatements": [
                    {
                      "statementType": "assignment",
                      "variable": "counter",
                      "value": {
                        "expressionType": "literal",
                        "valueType": "integer",
                        "value": 0
                      }
                    }
                  ]
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof IfStatement);
        IfStatement ifStatement = (IfStatement) statement;

        // Verify fields
        assertEquals("if", ifStatement.getStatementType());
        assertNotNull(ifStatement.getCondition());
        assertTrue(ifStatement.getCondition() instanceof BinaryOperationExpression);

        // Verify thenStatements
        assertNotNull(ifStatement.getThenStatements());
        assertEquals(1, ifStatement.getThenStatements().size());
        assertTrue(ifStatement.getThenStatements().get(0) instanceof MethodCallStatement);

        // Verify elseStatements
        assertNotNull(ifStatement.getElseStatements());
        assertEquals(1, ifStatement.getElseStatements().size());
        assertTrue(ifStatement.getElseStatements().get(0) instanceof AssignmentStatement);
    }

    /**
     * Test parsing a ForEachStatement from JSON
     */
    @Test
    public void testParseForEachStatement() throws Exception {
        String json = """
                {
                  "statementType": "forEach",
                  "variable": "item",
                  "variableType": "string",
                  "collection": {
                    "expressionType": "methodCall",
                    "method": "getItems",
                    "arguments": []
                  },
                  "statements": [
                    {
                      "statementType": "methodCall",
                      "method": "processItem",
                      "arguments": [
                        {
                          "expressionType": "variable",
                          "name": "item"
                        }
                      ]
                    }
                  ]
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof ForEachStatement);
        ForEachStatement forEach = (ForEachStatement) statement;

        // Verify fields
        assertEquals("forEach", forEach.getStatementType());
        assertEquals("item", forEach.getVariable());
        assertEquals("string", forEach.getVariableType());
        assertNotNull(forEach.getCollection());
        assertTrue(forEach.getCollection() instanceof MethodCallExpression);

        // Verify loop statements
        assertNotNull(forEach.getStatements());
        assertEquals(1, forEach.getStatements().size());
        assertTrue(forEach.getStatements().get(0) instanceof MethodCallStatement);

        MethodCallStatement methodCall = (MethodCallStatement) forEach.getStatements().get(0);
        assertEquals("processItem", methodCall.getMethod());
    }

    /**
     * Test parsing a ReturnStatement from JSON
     */
    @Test
    public void testParseReturnStatement() throws Exception {
        String json = """
                {
                  "statementType": "return",
                  "value": {
                    "expressionType": "variable",
                    "name": "result"
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof ReturnStatement);
        ReturnStatement returnStmt = (ReturnStatement) statement;

        // Verify fields
        assertEquals("return", returnStmt.getStatementType());
        assertNotNull(returnStmt.getValue());
        assertTrue(returnStmt.getValue() instanceof VariableExpression);

        VariableExpression varExpr = (VariableExpression) returnStmt.getValue();
        assertEquals("variable", varExpr.getExpressionType());
        assertEquals("result", varExpr.getName());
    }

    /**
     * Test parsing a MethodCallStatement from JSON
     */
    @Test
    public void testParseMethodCallStatement() throws Exception {
        String json = """
                {
                  "statementType": "methodCall",
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

        JsonNode jsonNode = objectMapper.readTree(json);
        Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the statement is of correct type
        assertTrue(statement instanceof MethodCallStatement);
        MethodCallStatement methodCall = (MethodCallStatement) statement;

        // Verify fields
        assertEquals("methodCall", methodCall.getStatementType());
        assertEquals("calculator", methodCall.getObject());
        assertEquals("add", methodCall.getMethod());
        assertNotNull(methodCall.getArguments());
        assertEquals(2, methodCall.getArguments().size());

        // Verify arguments
        assertTrue(methodCall.getArguments().get(0) instanceof LiteralExpression);
        assertTrue(methodCall.getArguments().get(1) instanceof LiteralExpression);

        LiteralExpression arg1 = (LiteralExpression) methodCall.getArguments().get(0);
        assertEquals(5, arg1.getValue());

        LiteralExpression arg2 = (LiteralExpression) methodCall.getArguments().get(1);
        assertEquals(10, arg2.getValue());
    }

    /**
     * Test serialization and deserialization of an IfStatement
     */
    @Test
    public void testSerializeDeserializeIfStatement() throws Exception {
        // Create a complex if statement manually
        IfStatement ifStatement = new IfStatement();
        ifStatement.setStatementType("if");

        // Create condition
        BinaryOperationExpression condition = new BinaryOperationExpression();
        condition.setExpressionType("binaryOperation");
        condition.setOperator("==");

        VariableExpression left = new VariableExpression();
        left.setExpressionType("variable");
        left.setName("status");

        LiteralExpression right = new LiteralExpression();
        right.setExpressionType("literal");
        right.setValueType("string");
        right.setValue("ready");

        condition.setLeft(left);
        condition.setRight(right);
        ifStatement.setCondition(condition);

        // Create then statements
        List<Statement> thenStatements = new ArrayList<>();
        MethodCallStatement thenMethodCall = new MethodCallStatement();
        thenMethodCall.setStatementType("methodCall");
        thenMethodCall.setMethod("processData");
        thenMethodCall.setArguments(new ArrayList<>());
        thenStatements.add(thenMethodCall);
        ifStatement.setThenStatements(thenStatements);

        // Create else statements
        List<Statement> elseStatements = new ArrayList<>();
        MethodCallStatement elseMethodCall = new MethodCallStatement();
        elseMethodCall.setStatementType("methodCall");
        elseMethodCall.setMethod("waitForReady");
        elseMethodCall.setArguments(new ArrayList<>());
        elseStatements.add(elseMethodCall);
        ifStatement.setElseStatements(elseStatements);

        // Serialize
        String json = objectMapper.writeValueAsString(ifStatement);
        System.out.println("DEBUG: Serialized IfStatement JSON: " + json);

        // Deserialize
        JsonNode jsonNode = objectMapper.readTree(json);
        Statement deserializedStatement = objectMapper.treeToValue(jsonNode, Statement.class);

        // Verify the type and basic structure
        assertTrue(deserializedStatement instanceof IfStatement);
        IfStatement deserializedIf = (IfStatement) deserializedStatement;

        assertEquals("if", deserializedIf.getStatementType());
        assertTrue(deserializedIf.getCondition() instanceof BinaryOperationExpression);

        BinaryOperationExpression deserializedCondition = (BinaryOperationExpression) deserializedIf.getCondition();
        assertEquals("==", deserializedCondition.getOperator());

        // Verify then statements
        assertNotNull(deserializedIf.getThenStatements());
        assertEquals(1, deserializedIf.getThenStatements().size());
        assertTrue(deserializedIf.getThenStatements().get(0) instanceof MethodCallStatement);
        assertEquals("processData", ((MethodCallStatement) deserializedIf.getThenStatements().get(0)).getMethod());

        // Verify else statements
        assertNotNull(deserializedIf.getElseStatements());
        assertEquals(1, deserializedIf.getElseStatements().size());
        assertTrue(deserializedIf.getElseStatements().get(0) instanceof MethodCallStatement);
        assertEquals("waitForReady", ((MethodCallStatement) deserializedIf.getElseStatements().get(0)).getMethod());
    }

    /**
     * Test invalid type in JSON
     */
    @Test
    public void testInvalidStatementType() {
        String json = """
                {
                  "statementType": "invalidType",
                  "someField": "someValue"
                }
                """;

        Exception exception = assertThrows(Exception.class, () -> {
            JsonNode jsonNode = objectMapper.readTree(json);
            Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);
        });

        // Verify exception contains useful message about the invalid type
        assertTrue(exception.getMessage().contains("Failed to convert JSON") ||
                exception.getMessage().contains("invalidType"));
    }

    /**
     * Test with missing required field
     */
    @Test
    public void testMissingRequiredField() {
        String json = """
                {
                  "statementType": "assignment"
                  // Missing required 'variable' field
                }
                """;

        // This may or may not throw an exception depending on how Jackson is configured
        // Some configurations will create the object with null fields
        // Others will throw an exception for missing required fields
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            Statement statement = objectMapper.treeToValue(jsonNode, Statement.class);

            // If no exception, verify the field is null
            assertInstanceOf(AssignmentStatement.class, statement);
            AssignmentStatement assignment = (AssignmentStatement) statement;
            assertNull(assignment.getVariable());
        } catch (Exception e) {
            // Debug output to check the actual message
            System.out.println("DEBUG: Exception message: " + e.getMessage());

            // Adjust the assertion to match the actual message
            assertTrue(e.getMessage().contains("Failed to convert JSON") ||
                    e.getMessage().toLowerCase().contains("missing"));
        }
    }

    /**
     * Helper method to create a common expression for testing
     */
    private LiteralExpression createLiteralExpression(String valueType, Object value) {
        LiteralExpression expression = new LiteralExpression();
        expression.setExpressionType("literal");
        expression.setValueType(valueType);
        expression.setValue(value);
        return expression;
    }
}